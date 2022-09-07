<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"  xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#" xmlns:dc="http://purl.org/dc/elements/1.1/" xmlns:dct="http://purl.org/dc/terms/" xmlns:dcmiBox="http://dublincore.org/documents/2000/07/11/dcmi-box/" xmlns:ows="http://www.opengis.net/ows">
	<xsl:output method="text" version="1.0" encoding="UTF-8" indent="no"/>
	<xsl:template match="/">
identifier=<xsl:value-of select="/metadata/mdFileID"/>
title=<xsl:value-of select="/metadata/dataIdInfo/idCitation/resTitle"/>
description=<xsl:value-of select="/metadata/dataIdInfo/idAbs"/>
modified=<xsl:value-of select="/metadata/mdDateSt"/>
resource.url=<xsl:value-of select="/metadata/distInfo[1]/distTranOps/onLineSrc/linkage"/>
resource.url.scheme=
bbox=<xsl:value-of select="/metadata/dataIdInfo/DataExt/geoEle/GeoBndBox/westBL"/> <xsl:value-of select="/metadata/dataIdInfo/DataExt/geoEle/GeoBndBox/southBL"/>, <xsl:value-of select="/metadata/dataIdInfo/DataExt/geoEle/GeoBndBox/eastBL"/> <xsl:value-of select="/metadata/dataIdInfo/DataExt/geoEle/GeoBndBox/northBL"/>
	</xsl:template>
</xsl:stylesheet>
