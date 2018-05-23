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


/**
 * Utilities For Reading Various Flat Files
 */

package com.esri.geoportal.commons.doc;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Properties;
import java.util.Date;

import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.TikaCoreProperties;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.sax.BodyContentHandler;
import org.w3c.dom.Document;

import com.esri.geoportal.commons.meta.AttributeUtils;
import com.esri.geoportal.commons.meta.MapAttribute;
import com.esri.geoportal.commons.meta.util.WKAConstants;
import com.esri.geoportal.commons.meta.xml.SimpleDcMetaBuilder;
import com.esri.geoportal.commons.utils.XmlUtils;

public class DocUtils {
	
//	static String file_pth = new String("C:\\Temp\\Files\\Army_Report.docx");
	static String file_pth = new String("C:\\Temp\\Files\\Presentation1.pptx");
//	static String file_pth = new String("C:\\Temp\\Files\\Scarmazzi_343971.xlsm");
//	static String file_pth = new String("C:\\Temp\\Files\\TaxCollectionGRB_05.csv");
//	static String file_pth = new String("C:\\Temp\\Files\\group.jpg");

    public static void main(String[] args) throws IOException, TikaException {
    	
        byte[] in_bytes  = bytes_from_file(file_pth);
        
        byte[] out_bytes = generateMetadataXML(in_bytes);
        
        System.out.println(String.format("Bytes Returned: %s", out_bytes instanceof byte[]));

    }

    public static byte[] bytes_from_file(String filePath) {

        FileInputStream fileInputStream = null;
        byte[]          bytesArray      = null;

        try {
   
            File file  = new File(filePath);
            bytesArray = new byte[(int) file.length()];

            fileInputStream = new FileInputStream(file);
            fileInputStream.read(bytesArray);

        } catch (IOException e) {
            e.printStackTrace();
            
        } finally {
        	
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            
        }

        return bytesArray;
    }

    public static byte[] generateMetadataXML(byte[] file_bytes) throws IOException {
    	
    	// Input & Output Variables
    	ByteArrayInputStream base_input  = new ByteArrayInputStream(file_bytes);
    	byte[]               xml_bytes   = null;
    	
    	// Tika Parser Objects
        Parser               parser      = new AutoDetectParser();
        BodyContentHandler   handler     = new BodyContentHandler();
        Metadata             metadata    = new Metadata();
        ParseContext         context     = new ParseContext();
	  
        try {
        	
        	// Populate Metadata Object with Tika Parser
        	parser.parse(base_input, handler, metadata, context);
        	
        	Properties meta_props  = new Properties();
        	
        	String     meta_descr  = metadata.get(TikaCoreProperties.DESCRIPTION);
        	String     meta_modif  = metadata.get(TikaCoreProperties.MODIFIED);
        	String     meta_title  = metadata.get(TikaCoreProperties.TITLE);
        	
        	DateFormat date_format = new SimpleDateFormat("yyyy/MM/dd");
        	Date       date        = new Date();
        	String     date_today  = date_format.format(date);
        	String     tika_label  = String.format("TIKA_%s", date_today);
        	
        	// Check For Null Values & Set Defaults
        	if (meta_descr == null) {
        		meta_props.put(WKAConstants.WKA_DESCRIPTION, tika_label);
        	} else {
        		meta_props.put(WKAConstants.WKA_DESCRIPTION, meta_descr);
        	}
        	
        	if (meta_modif == null) {
        		meta_props.put(WKAConstants.WKA_MODIFIED, tika_label);
        	} else {
        		meta_props.put(WKAConstants.WKA_MODIFIED, meta_modif);
        	}
        	
        	if (meta_title == null) {
        		meta_props.put(WKAConstants.WKA_TITLE, tika_label);
        	} else {
        		meta_props.put(WKAConstants.WKA_TITLE, meta_title);
        	}

        	// Put Tika Information in Properties
        	for(String name : metadata.names()) {
        		if (!metadata.get(name).isEmpty()) {
        			meta_props.put(name, metadata.get(name));
        		}
        	}
        	meta_props.list(System.out);
     	
        	// Build XML as Bytes
        	MapAttribute attr = AttributeUtils.fromProperties(meta_props);
    		Document document = new SimpleDcMetaBuilder().create(attr);
    		xml_bytes = XmlUtils.toString(document).getBytes("UTF-8");
        		
        } catch (Exception ex) {
        	ex.printStackTrace();
        } finally {
        	base_input.close();
        }
    	
    	return xml_bytes;
    	
    }
    
}