<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:output method="text" version="1.0" encoding="UTF-8" indent="no"/>
	<xsl:template match="/">
title=<xsl:value-of select="/metadata/idinfo/citation/citeinfo/title"/>
description=<xsl:value-of select="/metadata/idinfo/descript/abstract"/>
resource.url=<xsl:value-of select="/metadata/idinfo/citation/citeinfo/onlink[1]"/>
bbox=<xsl:value-of select="/metadata/idinfo/spdom/bounding/westbc"/><xsl:text> </xsl:text><xsl:value-of select="/metadata/idinfo/spdom/bounding/southbc"/>,<xsl:value-of select="/metadata/idinfo/spdom/bounding/eastbc"/><xsl:text> </xsl:text><xsl:value-of select="/metadata/idinfo/spdom/bounding/northbc"/>
	</xsl:template>
</xsl:stylesheet>
