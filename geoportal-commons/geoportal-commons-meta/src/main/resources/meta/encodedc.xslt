<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:output method="xml" version="1.0" encoding="UTF-8" indent="yes"/>
	
	<xsl:param name="identifier"/>
	<xsl:param name="title"/>
	<xsl:param name="description"/>
	<xsl:param name="modified"/>
	<xsl:param name="resource.url"/>
	<xsl:param name="resource.url.scheme"/>
	<xsl:param name="bbox"/>
	
	<xsl:template match="/">
<rdf:RDF xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#" xmlns:dc="http://purl.org/dc/elements/1.1/" xmlns:dct="http://purl.org/dc/terms/" xmlns:dcmiBox="http://dublincore.org/documents/2000/07/11/dcmi-box/" xmlns:ows="http://www.opengis.net/ows">
	<rdf:Description>
		<xsl:attribute name="rdf:about"><xsl:value-of select="$identifier"/></xsl:attribute>
		<dc:identifier><xsl:value-of select="$identifier"/></dc:identifier>
		<dc:title><xsl:value-of select="$title"/></dc:title>
		<dc:description><xsl:value-of select="$description"/></dc:description>
		<dc:date><xsl:value-of select="$modified"/></dc:date>
		<dct:references>
			<xsl:attribute name="scheme"><xsl:value-of select="$resource.url.scheme"/></xsl:attribute>
			<xsl:value-of select="$resource.url"/>
		</dct:references>
		<ows:WGS84BoundingBox>
			<ows:LowerCorner><xsl:value-of select="substring-before($bbox,',')"/></ows:LowerCorner>
			<ows:UpperCorner><xsl:value-of select="substring-after($bbox,',')"/></ows:UpperCorner>
		</ows:WGS84BoundingBox>        
	</rdf:Description>
</rdf:RDF>
	</xsl:template>
	
</xsl:stylesheet>
