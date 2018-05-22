package com.esri.geoportal.commons.pdf;

import static org.junit.Assert.*;

import java.io.File;
import java.nio.file.Files;
import java.util.Properties;

import org.junit.Test;

public class PdfUtilsTest {
    
    private static final String DEFAULT_GEOM = "https://utility.arcgisonline.com/ArcGIS/rest/services/Geometry/GeometryServer";

    @Test
    public void testReadMetadata_readGeospatialPDF () throws Exception {
        File pdf = new File("src/test/data/geoeyeuscapitalimage.pdf");
        byte[] bytes = Files.readAllBytes(pdf.toPath());

        Properties props = PdfUtils.readMetadata(bytes, "defaultTitle", DEFAULT_GEOM);
        
        assertNotNull("null value returned", props);
        assertEquals("wrong title", "defaultTitle", props.getProperty(PdfUtils.PROP_TITLE));
        assertEquals("wrong description", "\nAuthor: null\nCreator: Adobe Acrobat 9.0\nProducer: Adobe Acrobat 9.0 Image Conversion Plug-in", props.getProperty(PdfUtils.PROP_SUBJECT));
        assertEquals("Wrong bbox", "-77.05301318703646 38.88323180425436, -77.00746902033382 38.898891017940734", props.getProperty(PdfUtils.PROP_BBOX));
    }
}