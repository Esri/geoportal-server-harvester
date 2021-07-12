<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:csw="http://www.opengis.net/cat/csw/2.0.2" xmlns:dc="http://purl.org/dc/elements/1.1/" xmlns:dct="http://purl.org/dc/terms/" xmlns:ows="http://www.opengis.net/ows" xmlns:gmd="http://www.isotc211.org/2005/gmd" xmlns:gco="http://www.isotc211.org/2005/gco" xmlns:srv="http://www.isotc211.org/2005/srv" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:gmi="http://www.isotc211.org/2005/gmi"  xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#">
  <xsl:output indent="yes" method="xml" omit-xml-declaration="no"/>
  <xsl:template match="/">
    <xsl:choose>
      <xsl:when test="/csw:GetRecordByIdResponse/ows:ExceptionReport">
        <exception>
          <exceptionText>
            <xsl:for-each select="/ows:ExceptionReport/ows:Exception">
              <xsl:value-of select="ows:ExceptionText"/>
            </xsl:for-each>
          </exceptionText>
        </exception>
      </xsl:when>
      <xsl:otherwise>
		<rdf:RDF xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#" xmlns:dc="http://purl.org/dc/elements/1.1/" xmlns:dct="http://purl.org/dc/terms/" xmlns:dcmiBox="http://dublincore.org/documents/2000/07/11/dcmi-box/" xmlns:ows="http://www.opengis.net/ows" xmlns:gml="http://www.opengis.net/gml" xmlns:xs="http://www.w3.org/2001/XMLSchema">
			<rdf:Description>
				<xsl:apply-templates select="/csw:GetRecordByIdResponse/csw:Record"/>
			</rdf:Description>
		</rdf:RDF>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template> 
	<xsl:template match="/csw:GetRecordByIdResponse/csw:Record">
		<xsl:attribute name="rdf:about"><xsl:value-of select="normalize-space(dc:identifier)"/></xsl:attribute>
		<dc:identifier><xsl:value-of select="normalize-space(dc:identifier)"/></dc:identifier>
		<xsl:if test="string-length(normalize-space(dc:title))>0">
			<dc:title><xsl:value-of select="dc:title"/></dc:title>
		</xsl:if>
		<xsl:if test="string-length(normalize-space(dc:title))=0">
			<dc:title><xsl:value-of select="dc:identifier"/></dc:title>
		</xsl:if>
		<xsl:if test="string-length(dc:description | dct:abstract)>0">
			<dc:description><xsl:value-of select="dc:description | dct:abstract"/></dc:description>
		</xsl:if>
		<xsl:if test="string-length(dct:abstract | dc:description)>0">
			<dct:abstract><xsl:value-of select="dct:abstract | dc:description"/></dct:abstract>
		</xsl:if>
		<xsl:if test="string-length(dct:modified)>0">
			<dc:date><xsl:value-of select="dct:modified"/></dc:date>
		</xsl:if>
		<xsl:copy-of select="dct:references"  />
		<xsl:copy-of select="ows:WGS84BoundingBox"  />
	</xsl:template>
</xsl:stylesheet>
