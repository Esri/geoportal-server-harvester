package com.esri.geoportal.commons.pdf;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Properties;

import org.junit.Test;

public class PdfUtilsTest {
    
    private static final String DEFAULT_GEOM = "https://utility.arcgisonline.com/ArcGIS/rest/services/Geometry/GeometryServer";

    @Test
    public void testReadMetadata_readGeoPDF_UT () throws Exception {
        byte[] bytes = getBytes("src/test/data/geoeyeuscapitalimage.pdf");

        Properties props = PdfUtils.readMetadata(bytes, "defaultTitle", DEFAULT_GEOM);
        
        assertNotNull("null value returned", props);
        assertEquals("wrong title", "defaultTitle", props.getProperty(PdfUtils.PROP_TITLE));
        assertEquals("wrong description", "\nAuthor: null\nCreator: Adobe Acrobat 9.0\nProducer: Adobe Acrobat 9.0 Image Conversion Plug-in", props.getProperty(PdfUtils.PROP_SUBJECT));
        assertEquals("Wrong bbox", "-77.05301318703646 38.88323180425436, -77.00746902033382 38.898891017940734", props.getProperty(PdfUtils.PROP_BBOX));
    }

    @Test
    public void testReadMetadata_readGeospatialPDF() throws Exception {
        byte[] bytes = getBytes("src/test/data/MANAmap1.pdf");

        Properties props = PdfUtils.readMetadata(bytes, "defaultTitle", DEFAULT_GEOM);
        assertNotNull("null value returned", props);
        assertEquals("wrong title", "MANAmap1", props.getProperty(PdfUtils.PROP_TITLE));
        assertEquals("wrong description", "\nAuthor: null\nCreator: Adobe Illustrator CS5\nProducer: Adobe PDF library 9.90", props.getProperty(PdfUtils.PROP_SUBJECT));
        assertEquals("Wrong bbox", "-77.54026794433594 38.815128326416016, -77.5213394165039 38.82757568359375", props.getProperty(PdfUtils.PROP_BBOX));
    }

    private byte[] getBytes(String path) throws IOException{
        File pdf = new File(path);
        return Files.readAllBytes(pdf.toPath());
    }
}