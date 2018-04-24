/*
 * Copyright 2018 Esri, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.esri.geoportal.commons.pdf;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

import javax.xml.transform.TransformerException;

import com.esri.core.geometry.MultiPoint;
import com.esri.core.geometry.Point;
import com.esri.geoportal.commons.meta.AttributeUtils;
import com.esri.geoportal.commons.meta.MapAttribute;
import com.esri.geoportal.commons.meta.MetaException;
import com.esri.geoportal.commons.meta.util.WKAConstants;
import com.esri.geoportal.commons.meta.xml.SimpleDcMetaBuilder;
import com.esri.geoportal.commons.utils.XmlUtils;
import com.esri.geoportal.geoportal.commons.geometry.GeometryService;

import org.apache.commons.text.StrSubstitutor;
import org.apache.commons.text.StringSubstitutor;
import org.apache.http.impl.client.HttpClients;
import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSObject;
import org.apache.pdfbox.cos.COSString;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.pdmodel.PDPage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

/**
 * Utilities for reading PDF file metadata
 */
public class PdfUtils {
    private static final Logger LOG = LoggerFactory.getLogger(PdfUtils.class);

    // Metadata properties read from PDF file
    public static final String PROP_TITLE = "title";
    public static final String PROP_SUBJECT = "description";
    public static final String PROP_MODIFICATION_DATE = "modification_date";

    /**
     * Reads metadata values from a PDF file.
     * 
     * @param rawBytes the PDF to read
     * @return metadata properties or null if the PDF cannot be read.
     * @throws IOException on parsing error
     */
    public static Properties readMetadata(byte[] rawBytes, String defaultTitle) throws IOException {
        Properties ret = new Properties();

        // Attempt to read in the PDF file
        try (PDDocument document = PDDocument.load(rawBytes)) {

            // See if we can read the PDF
            if (!document.isEncrypted()) {
                // Get document metadata
                PDDocumentInformation info = document.getDocumentInformation();

                if (info != null) {

                    if (info.getTitle() != null) {
                        ret.put(PROP_TITLE, info.getTitle());
                    } else {
                        ret.put(PROP_TITLE, defaultTitle);
                    }

                    if (info.getSubject() != null) {
                        ret.put(PROP_SUBJECT, info.getSubject());
                    } else {
                        
                        StringBuilder psudoSubject = new StringBuilder("");
                        psudoSubject.append("\nAuthor: " + info.getAuthor());
                        psudoSubject.append("\nCreator: " + info.getCreator());
                        psudoSubject.append("\nProducer: " + info.getProducer());

                        ret.put(PROP_SUBJECT, psudoSubject.toString());
                    }

                    if (info.getModificationDate() != null) {
                        ret.put(PROP_MODIFICATION_DATE, info.getModificationDate().getTime());
                    } else {
                        ret.put(PROP_MODIFICATION_DATE, info.getCreationDate().getTime());
                    }
                } else {
                    LOG.warn("Got null metadata for PDF file");
                    return null;
                }
                
                COSObject measure = document.getDocument().getObjectByType(COSName.getPDFName("Measure"));
                if (measure != null) {
                    System.out.println("Found Measure element");
                    // COSDictionary dictionary = (COSDictionary) measure;
                    COSBase coords = measure.getItem(COSName.getPDFName("GPTS"));
                    System.out.printf("\tCoordinates: %s\n", coords.toString());
                }

                PDPage page = document.getPage(0);
                if (page.getCOSObject().containsKey(COSName.getPDFName("LGIDict"))) {
                    extractGeoPDFProps(page);
                }
            } else {
                LOG.warn("Cannot read encrypted PDF file");
                return null;
            }

        } catch (IOException ex) {
            LOG.error("Exception reading PDF", ex);
            throw ex;
        }

        return ret;
    }

	private static void extractGeoPDFProps(PDPage page) {
        LOG.info("Found LGI dictionary");
        
        COSArray lgi = (COSArray) page.getCOSObject().getDictionaryObject("LGIDict");

        lgi.iterator().forEachRemaining(item -> {

            // Set up the Coordinate Transformation Matrix
            Double [][] ctmValues = null;

            COSDictionary dictionary = (COSDictionary) item;
            if (dictionary.containsKey("CTM")) {
                System.out.println("\tCTM");
                ctmValues = new Double[3][3];
                ctmValues[0][2] = 0.0;
                ctmValues[1][2] = 0.0;
                ctmValues[2][2] = 1.0; 

                COSArray ctm = (COSArray) dictionary.getDictionaryObject("CTM");
                for (int i = 0; i < ctm.toList().size(); i += 2) {
                    int ctmRow = i / 2;
                    ctmValues[ctmRow][0] = Double.parseDouble(((COSString)ctm.get(i)).getString());
                    ctmValues[ctmRow][1] = Double.parseDouble(((COSString)ctm.get(i + 1)).getString());
                    System.out.printf("\t\t%s %s\n", ctm.get(i), ctm.get(i+1));
                }
            }

            Double[][] neatLineValues =  null;
            int neatLineLength = 0;

            if (dictionary.containsKey("Neatline")) {
                System.out.println("\tNeatline");

                COSArray neatline = (COSArray) dictionary.getDictionaryObject("Neatline");
                neatLineLength = neatline.toList().size();
                neatLineValues = new Double[neatLineLength / 2][3];

                for (int i = 0; i < neatline.toList().size(); i += 2) {
                    int neatLineRow = i / 2;
                    neatLineValues[neatLineRow][0] = Double.parseDouble(((COSString)neatline.get(i)).getString());
                    neatLineValues[neatLineRow][1] = Double.parseDouble(((COSString)neatline.get(i + 1)).getString());
                    neatLineValues[neatLineRow][2] = 1.0;
                    System.out.printf("\t\t%s, %s\n", neatline.get(i), neatline.get(i+1));
                }
            }

            MultiPoint mp = new MultiPoint();

            if  (ctmValues != null && neatLineValues != null) {
                // Transform the PDF coordinates to geospatial ones
                Double [][] resultCoords = new Double[neatLineLength / 2][3];
                for (int z = 0; z < neatLineLength / 2; z ++) {
                    for (int i = 0; i < 3; i++) {
                        resultCoords[z][i] = neatLineValues[z][0] * ctmValues[0][i] + neatLineValues[z][1] * ctmValues[1][i] + neatLineValues[z][2] * ctmValues[2][i];
                    }

                    // Set up the multipoint object
                    mp.add(resultCoords[z][0], resultCoords[z][1]);
                }
            }

            if (dictionary.containsKey("Projection")) {
                String wkt = getProjectionWKT((COSDictionary)dictionary.getDictionaryObject("Projection"));

                if (wkt != null) {

                    try (GeometryService svc = new GeometryService(HttpClients.custom().useSystemProperties().build(), new URL("https://utility.arcgisonline.com/ArcGIS/rest/services/Geometry/GeometryServer"));) {
                        MultiPoint reproj = svc.project(mp, wkt, 4326);
                        
                        int count = reproj.getPointCount();
                        Double xMax = -Double.MAX_VALUE;
                        Double yMax = -Double.MAX_VALUE;
                        Double xMin = Double.MAX_VALUE;
                        Double yMin = Double.MAX_VALUE;

                        for (int i = 0; i < count; i++) {
                            Point pt = reproj.getPoint(i);
                            
                            if (pt.getX() > xMax) {
                                xMax = pt.getX();
                            }
                            if (pt.getX() < xMin) {
                                xMin = pt.getX();
                            }

                            if (pt.getY() > yMax) {
                                yMax = pt.getY();
                            }
                            if (pt.getY() < yMin) {
                                yMin = pt.getY();
                            }
                        }

                        System.out.printf("\n%s %s %s %s\n", xMin, yMin, xMax, yMax);

                    } catch (Exception e) {
                        LOG.error("Exception reprojecting geometry", e);
                    }
                    
                }
            }
        });
    }

    private static final String PROJ_WKT_TEMPLATE = "PROJCS[\"${name}\", ${geo_cs}, ${projection}, ${parameters}, ${linear_unit}]";
    private static final String GEO_WKT_TEMPLATE = "GEOGCS[\"${name}\", ${datum}, ${prime_meridian}, ${angular_unit}]";
    private static final String DATUM_TEMPLATE = "DATUM[\"${name}\", ${spheroid} ${to_wgs84}]";
    
    private static String getProjectionWKT(COSDictionary projectionDictionary) {
        Map<String,String> tokens = new HashMap<>();

        String type = projectionDictionary.getString("Type");
        String projectionType = projectionDictionary.getString("ProjectionType");
        tokens.put("name", projectionType);

        System.out.println("Projection Type: " + projectionType);

        if ("UT".equalsIgnoreCase(projectionType)) {
            String parameterString = "PARAMETER[\"zone\", ${zone}]";
            // Map<String,String> zone = 
        } else if ("LE".equalsIgnoreCase(projectionType)) {
            tokens.put("projection", "PROJECTION[\"Lambert_Conformal_Conic\"]");
            
            // Set up the projection parameters
            Properties parameters = new Properties();
            parameters.put("Central_Meridian", projectionDictionary.getString("CentralMeridian"));
            parameters.put("Latitude_Of_Origin", projectionDictionary.getString("OriginLatitude"));
            parameters.put("Standard_Parallel_1", projectionDictionary.getString("StandardParallelOne"));
            parameters.put("Standard_Parallel_2", projectionDictionary.getString("StandardParallelTwo"));
            parameters.put("False_Easting", projectionDictionary.getString("FalseEasting"));
            parameters.put("False_Northing", projectionDictionary.getString("FalseNorthing"));

            String paramsString = parameters.entrySet().stream().map(entry -> "PARAMETER[\""+ entry.getKey() + "\", "+ entry.getValue() + "]").collect(Collectors.joining(","));
            tokens.put("parameters", paramsString);

            tokens.put("linear_unit", "UNIT[\"Meter\",1.0]");

            // Get the datum
            COSBase datumObj = projectionDictionary.getDictionaryObject("Datum");
            if (datumObj instanceof COSString) {
                String datum = ((COSString) datumObj).getString();

                // TODO datum lookup
                String geogcs = "GEOGCS[\"GCS_North_American_1927\",DATUM[\"D_North_American_1927\",SPHEROID[\"Clarke_1866\",6378206.4,294.9786982]],PRIMEM[\"Greenwich\",0],UNIT[\"Degree\",0.017453292519943295]]";
                tokens.put("geo_cs", geogcs);
            }

             // StrSubstitutor subber = new StrSubstitutor(tokens);
            StringSubstitutor subber = new StringSubstitutor(tokens);
            return subber.replace(PROJ_WKT_TEMPLATE);            
        }

        return null;
    }

    public static byte[] generateMetadataXML(byte[] pdfBytes, String fileName, String url) throws IOException {
        byte[] bytes = null;
        Properties metaProps = readMetadata(pdfBytes, fileName);

        if (metaProps != null) {
            Properties props = new Properties();
            props.put(WKAConstants.WKA_TITLE, metaProps.get(PdfUtils.PROP_TITLE));
            props.put(WKAConstants.WKA_DESCRIPTION, metaProps.get(PdfUtils.PROP_SUBJECT));
            props.put(WKAConstants.WKA_MODIFIED, metaProps.get(PdfUtils.PROP_MODIFICATION_DATE));
            props.put(WKAConstants.WKA_RESOURCE_URL, url);

            try {
                MapAttribute attr = AttributeUtils.fromProperties(props);
                Document document = new SimpleDcMetaBuilder().create(attr);
                bytes = XmlUtils.toString(document).getBytes("UTF-8");
            } catch (MetaException | TransformerException ex) {
                throw new IOException(ex);
            }
        }

        return bytes;
    }
}