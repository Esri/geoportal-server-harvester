<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"   xmlns:gmi="http://www.isotc211.org/2005/gmi" xmlns:gmd="http://www.isotc211.org/2005/gmd" xmlns:gco="http://www.isotc211.org/2005/gco" xmlns:srv="http://www.isotc211.org/2005/srv" xmlns:gml="http://www.opengis.net/gml">
	<xsl:output method="text" version="1.0" encoding="UTF-8" indent="no"/>
  <xsl:variable name="lowercase" select="'abcdefghijklmnopqrstuvwxyz'" />
  <xsl:variable name="uppercase" select="'ABCDEFGHIJKLMNOPQRSTUVWXYZ'" />
  <xsl:template match="/">
identifier=<xsl:value-of select="/gmi:MI_Metadata/gmd:fileIdentifier/gco:CharacterString"/>
title=<xsl:value-of select="/gmi:MI_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:citation/gmd:CI_Citation/gmd:title/gco:CharacterString"/>
description=<xsl:call-template name="replace-string">
  <xsl:with-param name="text" select="/gmi:MI_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:abstract/gco:CharacterString"/>
  <xsl:with-param name="replace" select="'&#xA;'" />
  <xsl:with-param name="with" select="'\\n'"/>
</xsl:call-template>
xdescription=<xsl:value-of select="translate(/gmi:MI_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:abstract/gco:CharacterString, '&#xA;', '\')" />
<!--xsl:value-of select="/gmi:MI_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:abstract/gco:CharacterString"/ -->
modified=<xsl:value-of select="/gmi:MI_Metadata/gmd:dateStamp/gco:Date | /gmi:MI_Metadata/gmd:dateStamp/gco:DateTime"/>
<xsl:choose>
    <xsl:when test="count(/gmi:MI_Metadata/gmd:distributionInfo/gmd:MD_Distribution/gmd:transferOptions/gmd:MD_DigitalTransferOptions/gmd:onLine/gmd:CI_OnlineResource/gmd:linkage/gmd:URL)>0">
resource.url=<xsl:value-of select="/gmi:MI_Metadata/gmd:distributionInfo/gmd:MD_Distribution/gmd:transferOptions/gmd:MD_DigitalTransferOptions/gmd:onLine/gmd:CI_OnlineResource/gmd:linkage/gmd:URL"/>
    </xsl:when>
    <xsl:otherwise>
resource.url=<xsl:value-of select="/gmi:MI_Metadata/gmd:distributionInfo/gmd:MD_Distribution/gmd:distributor/gmd:MD_Distributor/gmd:distributorTransferOptions/gmd:MD_DigitalTransferOptions/gmd:onLine/gmd:CI_OnlineResource/gmd:linkage/gmd:URL"/>
    </xsl:otherwise>
</xsl:choose>
<!--
resource.url=<xsl:for-each select="/gmi:MI_Metadata/gmd:distributionInfo/gmd:MD_Distribution/gmd:distributor/gmd:MD_Distributor/gmd:distributorTransferOptions/gmd:MD_DigitalTransferOptions/gmd:onLine/gmd:CI_OnlineResource/gmd:linkage/gmd:URL">
  <xsl:variable name="link" select="translate(., $uppercase, $lowercase)" />
  <xsl:choose>
    <xsl:when test="contains($link,'/mapserver')">
      <xsl:value-of select="."/>
    </xsl:when>
    <xsl:when test="contains($link,'/imageserver')">
      <xsl:value-of select="."/>
    </xsl:when>
    <xsl:when test="contains($link,'/featureserver')">
      <xsl:value-of select="."/>
    </xsl:when>
    <xsl:otherwise>
    </xsl:otherwise>
  </xsl:choose>  
</xsl:for-each>
-->

<!--    <xsl:value-of select="/gmi:MI_Metadata/gmd:distributionInfo/gmd:MD_Distribution/gmd:distributor/gmd:MD_Distributor/gmd:distributorTransferOptions/gmd:MD_DigitalTransferOptions/gmd:onLine/gmd:CI_OnlineResource/gmd:linkage/gmd:URL"/> -->
thumbnail.url=<xsl:value-of select="/gmi:MI_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:graphicOverview/gmd:MD_BrowseGraphic/gmd:fileName/gco:CharacterString"/>
bbox=<xsl:value-of select="/gmi:MI_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:extent/gmd:EX_Extent/gmd:geographicElement/gmd:EX_GeographicBoundingBox/gmd:westBoundLongitude/gco:Decimal"/><xsl:text> </xsl:text><xsl:value-of select="/gmi:MI_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:extent/gmd:EX_Extent/gmd:geographicElement/gmd:EX_GeographicBoundingBox/gmd:southBoundLatitude/gco:Decimal"/>,<xsl:value-of select="/gmi:MI_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:extent/gmd:EX_Extent/gmd:geographicElement/gmd:EX_GeographicBoundingBox/gmd:eastBoundLongitude/gco:Decimal"/><xsl:text> </xsl:text><xsl:value-of select="/gmi:MI_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:extent/gmd:EX_Extent/gmd:geographicElement/gmd:EX_GeographicBoundingBox/gmd:northBoundLatitude/gco:Decimal"/>
	</xsl:template>

  <xsl:template name="replace-string">
    <xsl:param name="text"/>
    <xsl:param name="replace"/>
    <xsl:param name="with"/>
    <xsl:choose>
      <xsl:when test="contains($text,$replace)">
        <xsl:value-of select="substring-before($text,$replace)"/>
        <xsl:value-of select="$with"/>
        <xsl:call-template name="replace-string">
          <xsl:with-param name="text" select="substring-after($text,$replace)"/>
          <xsl:with-param name="replace" select="$replace"/>
          <xsl:with-param name="with" select="$with"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="$text"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

</xsl:stylesheet>
