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
import java.util.Properties;

import javax.xml.transform.TransformerException;

import com.esri.geoportal.commons.meta.AttributeUtils;
import com.esri.geoportal.commons.meta.MapAttribute;
import com.esri.geoportal.commons.meta.MetaException;
import com.esri.geoportal.commons.meta.util.WKAConstants;
import com.esri.geoportal.commons.meta.xml.SimpleDcMetaBuilder;
import com.esri.geoportal.commons.utils.XmlUtils;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
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