<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <xsl:output method="xml" indent="yes" encoding="UTF-8" omit-xml-declaration="no"/>
  <xsl:template match="/">
    <xsl:element name="csw:GetRecords" use-attribute-sets="GetRecordsAttributes" 
                 xmlns:csw="http://www.opengis.net/cat/csw/2.0.2" 
                 xmlns:dc="http://purl.org/dc/elements/1.1/" 
                 xmlns:dct="http://purl.org/dc/terms/"
                 xmlns:gmd="http://www.isotc211.org/2005/gmd"
                 xmlns:gml="http://www.opengis.net/gml" 
                 xmlns:ogc="http://www.opengis.net/ogc" 
                 xmlns:ows="http://www.opengis.net/ows">
      <csw:Query typeNames="csw:Record">
        <csw:ElementSetName>full</csw:ElementSetName>
        <csw:Constraint version="1.1.0">
          <ogc:Filter>
            <ogc:BBOX>
            <ogc:PropertyName>ows:BoundingBox</ogc:PropertyName> 
            <gml:Envelope>
                <gml:lowerCorner>-180 -90</gml:lowerCorner> 
                <gml:upperCorner>180 90</gml:upperCorner> 
            </gml:Envelope>
            </ogc:BBOX>      
          </ogc:Filter>
        </csw:Constraint>
      </csw:Query>
    </xsl:element>
  </xsl:template>
	
	
  <xsl:attribute-set name="GetRecordsAttributes">
    <xsl:attribute name="version">2.0.2</xsl:attribute>
    <xsl:attribute name="service">CSW</xsl:attribute>
    <xsl:attribute name="outputSchema">http://www.opengis.net/cat/csw/2.0.2</xsl:attribute>
    <xsl:attribute name="outputFormat">application/xml</xsl:attribute>
    <xsl:attribute name="resultType">results</xsl:attribute>
    <xsl:attribute name="startPosition">
      <xsl:value-of select="/GetRecords/StartPosition"/>
    </xsl:attribute>
    <xsl:attribute name="maxRecords">
      <xsl:value-of select="/GetRecords/MaxRecords"/>
    </xsl:attribute>
  </xsl:attribute-set>
</xsl:stylesheet>
