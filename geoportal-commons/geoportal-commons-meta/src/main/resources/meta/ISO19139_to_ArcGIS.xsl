<?xml version="1.0" encoding="utf-8" standalone="yes"?>
<xsl:stylesheet version="1.0" 
                xmlns:gmd="http://www.isotc211.org/2005/gmd" 
                xmlns:gco="http://www.isotc211.org/2005/gco" 
                xmlns:gmi="http://www.isotc211.org/2005/gmi"
                xmlns:gml="http://www.opengis.net/gml" 
                xmlns:gts="http://www.isotc211.org/2005/gts" 
                xmlns:srv="http://www.isotc211.org/2005/srv" 
                xmlns:xlink="http://www.w3.org/1999/xlink" 
                xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                exclude-result-prefixes="gmd gco gmi gml gts srv xlink xsi xsl">

    <xsl:output method="xml" version="1.0" encoding="UTF-8" indent="yes" omit-xml-declaration="no" />

    <xsl:variable name="lowercase" select="'abcdefghijklmnopqrstuvwxyz'" />
    <xsl:variable name="uppercase" select="'ABCDEFGHIJKLMNOPQRSTUVWXYZ'" />

    <!-- B.2.1 Metadata entity set information -->

    <!-- MD_Metadata -->
    <xsl:template match="/gmi:MI_Metadata| /gmd:MD_Metadata">
        <metadata>
            <xsl:attribute name="xml:lang">
                <xsl:value-of select="gmd:language/gco:CharacterString" />
            </xsl:attribute>
            <Esri>
                <xsl:choose>
                    <xsl:when test="count(/gmd:MD_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:citation/gmd:CI_Citation/gmd:date/gmd:CI_Date[gmd:dateType/gmd:CI_DateTypeCode/@codeListValue='creation']/gmd:date/gco:Date)>0">
                        <CreaDate>
                            <xsl:value-of select="/gmd:MD_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:citation/gmd:CI_Citation/gmd:date/gmd:CI_Date[gmd:dateType/gmd:CI_DateTypeCode/@codeListValue='creation']/gmd:date/gco:Date" />
                        </CreaDate>
                        <CreaTime>00:00:00.00</CreaTime>
                    </xsl:when>
                    <xsl:otherwise>
                        <CreaDate>
                            <xsl:value-of select="/gmd:MD_Metadata/gmd:dateStamp/gco:Date" />
                        </CreaDate>
                        <CreaTime>00:00:00.00</CreaTime>
                    </xsl:otherwise>
                </xsl:choose>
                <xsl:choose>
                    <xsl:when test="count(/gmd:MD_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:citation/gmd:CI_Citation/gmd:date/gmd:CI_Date[gmd:dateType/gmd:CI_DateTypeCode/@codeListValue='revision']/gmd:date/gco:Date)>0">
                        <ModDate>
                            <xsl:value-of select="/gmd:MD_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:citation/gmd:CI_Citation/gmd:date/gmd:CI_Date[gmd:dateType/gmd:CI_DateTypeCode/@codeListValue='revision']/gmd:date/gco:Date" />
                        </ModDate>
                        <ModTime>00:00:00.00</ModTime>
                    </xsl:when>
                    <xsl:otherwise>
                        <ModDate>
                            <xsl:value-of select="/gmd:MD_Metadata/gmd:dateStamp/gco:Date" />
                        </ModDate>
                        <ModTime>00:00:00.00</ModTime>
                    </xsl:otherwise>
                </xsl:choose>
                <PublishStatus>editor:esri.dijit.metadata.editor</PublishStatus>
                <ArcGISFormat>1.0</ArcGISFormat>
                <ArcGISstyle>ISO 19139 Metadata Implementation Specification GML3.2</ArcGISstyle>
                <ArcGISProfile>ISO19139</ArcGISProfile>
                <xsl:if test="count(/gmd:MD_Metadata/gmd:locale/gmd:PT_Locale)>0">
                    <locales>
                        <xsl:for-each select="/gmd:MD_Metadata/gmd:locale/gmd:PT_Locale">
                            <locale>
                                <xsl:attribute name="language">
                                    <xsl:value-of select="./gmd:language/gmd:LanguageCode/@codeListValue"/>
                                </xsl:attribute>
                                <xsl:attribute name="country">
                                    <xsl:value-of select="translate(./gmd:country/gmd:CountryCode/@codeListValue, $lowercase, $uppercase)"/>
                                </xsl:attribute>
                                <id>
                                    <xsl:value-of select="@id"/>
                                </id>
                                <xsl:choose>
                                    <xsl:when test="@id='CAT'">
                                        <resTitle>
                                            <xsl:value-of select="/gmd:MD_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:citation/gmd:CI_Citation/gmd:title/gmd:PT_FreeText/gmd:textGroup/gmd:LocalisedCharacterString[@locale='CAT']"/>
                                        </resTitle>
                                        <idAbs>
                                            <xsl:value-of select="/gmd:MD_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:abstract/gmd:PT_FreeText/gmd:textGroup/gmd:LocalisedCharacterString[@locale='CAT']"/>
                                        </idAbs>
                                    </xsl:when>
                                    <xsl:when test="@id='ENG'">
                                        <resTitle>
                                            <xsl:value-of select="/gmd:MD_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:citation/gmd:CI_Citation/gmd:title/gmd:PT_FreeText/gmd:textGroup/gmd:LocalisedCharacterString[@locale='ENG']"/>
                                        </resTitle>
                                        <idAbs>
                                            <xsl:value-of select="/gmd:MD_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:abstract/gmd:PT_FreeText/gmd:textGroup/gmd:LocalisedCharacterString[@locale='ENG']"/>
                                        </idAbs>
                                    </xsl:when>
                                    <xsl:when test="@id='ESP'">
                                        <resTitle>
                                            <xsl:value-of select="/gmd:MD_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:citation/gmd:CI_Citation/gmd:title/gmd:PT_FreeText/gmd:textGroup/gmd:LocalisedCharacterString[@locale='ESP']"/>
                                        </resTitle>
                                        <idAbs>
                                            <xsl:value-of select="/gmd:MD_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:abstract/gmd:PT_FreeText/gmd:textGroup/gmd:LocalisedCharacterString[@locale='ESP']"/>
                                        </idAbs>
                                    </xsl:when>
                                    <xsl:otherwise>
                                        <resTitle>
                                            <xsl:value-of select="/gmd:MD_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:citation/gmd:CI_Citation/gmd:title/gco:CharacterString"/>
                                        </resTitle>
                                        <idAbs>
                                            <xsl:value-of select="/gmd:MD_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:abstract/gmd:PT_FreeText/gmd:textGroup/gmd:LocalisedCharacterString[@locale='ENG']"/>
                                        </idAbs>
                                    </xsl:otherwise>
                                </xsl:choose>
                                <!--
                                <idAbs>
                                        <xsl:value-of select="/gmd:MD_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:abstract/gmd:PT_FreeText/gmd:textGroup/gmd:LocalisedCharacterString[@locale=@id]"/>
                                </idAbs>
                                -->
                            </locale>
                        </xsl:for-each>
                    </locales>
                </xsl:if>
            </Esri>

            <mdFileID>
                <xsl:value-of select="gmd:fileIdentifier/gco:CharacterString" />
            </mdFileID>

            <xsl:for-each select="gmd:language/gmd:LanguageCode">
                <mdLang>
                    <languageCode>
                        <xsl:attribute name="value">
                            <xsl:value-of select="@codeListValue" />
                        </xsl:attribute>
                    </languageCode>
                </mdLang>
            </xsl:for-each>

            <xsl:for-each select="gmd:characterSet/gmd:MD_CharacterSetCode">
                <mdChar>
                    <CharSetCd>
                        <xsl:attribute name="value">
                            <xsl:call-template name="MD_CharacterSetCode">
                                <xsl:with-param name="source">
                                    <xsl:value-of select="@codeListValue"/>
                                </xsl:with-param> 
                            </xsl:call-template>
                        </xsl:attribute>
                    </CharSetCd>
                </mdChar>
            </xsl:for-each>

            <mdParentID>
                <xsl:value-of select="gmd:parentIdentifier/gco:CharacterString" />
            </mdParentID>

            <mdHrLv>
                <xsl:element name="ScopeCd">
                    <xsl:attribute name="value">
                        <xsl:call-template name="MD_ScopeCode">
                            <xsl:with-param name="source">
                                <xsl:value-of select="gmd:hierarchyLevel/gmd:MD_ScopeCode/@codeListValue"/>
                            </xsl:with-param> 
                        </xsl:call-template>
                    </xsl:attribute>
                </xsl:element>
            </mdHrLv>

            <mdHrLvName>
                <CharSetCd>
                    <xsl:attribute name="value">
                        <xsl:call-template name="MD_CharacterSetCode">
                            <xsl:with-param name="source">
                                <xsl:value-of select="gmd:characterSet/gmd:MD_CharacterSetCode/@codeListValue"/>
                            </xsl:with-param> 
                        </xsl:call-template>
                    </xsl:attribute>
                </CharSetCd>
            </mdHrLvName>

            <xsl:for-each select="gmd:contact/gmd:CI_ResponsibleParty">
                <mdContact>
                    <xsl:call-template name="CI_ResponsibleParty">
                        <xsl:with-param name="role" select="'pointOfContact'"/> 
                    </xsl:call-template>
                </mdContact>
            </xsl:for-each>

            <mdDateSt>
                <xsl:value-of select="gmd:dateStamp/gco:Date" />
            </mdDateSt>

            <dataSetURI>
                <xsl:value-of select="gmd:dataSetURI/gco:CharacterString" />
            </dataSetURI>

            <xsl:for-each select="gmd:spatialRepresentationInfo/child::*">
                <spatRepInfo>
                    <xsl:call-template name="MD_SpatialRepresentation" />
                </spatRepInfo>
            </xsl:for-each>

            <xsl:for-each select="gmd:referenceSystemInfo/gmd:MD_ReferenceSystem">
                <refSysInfo>
                    <xsl:call-template name="MD_ReferenceSystem" />
                </refSysInfo>
            </xsl:for-each>

            <xsl:for-each select="gmd:metadataExtensionInfo/gmd:MD_MetadataExtensionInformation">
                <mdExtInfo>
                    <xsl:call-template name="MD_MetadataExtensionInformation" />
                </mdExtInfo>
            </xsl:for-each>

            <xsl:for-each select="gmd:contentInfo/gmd:MD_Identification">
                <dataIdInfo>
                    <xsl:call-template name="MD_Identification" />
                </dataIdInfo>
            </xsl:for-each>

            <xsl:for-each select="gmd:identificationInfo/gmd:MD_DataIdentification">
                <dataIdInfo>
                    <xsl:call-template name="MD_Identification" />
                </dataIdInfo>
            </xsl:for-each>

            <xsl:for-each select="gmd:contentInfo/child::*">
                <contInfo>
                    <xsl:call-template name="MD_ContentInformation" />
                </contInfo>
            </xsl:for-each>

            <xsl:for-each select="gmd:distributionInfo/gmd:MD_Distribution">
                <distInfo>
                    <xsl:call-template name="MD_Distribution" />
                </distInfo>
            </xsl:for-each>

            <xsl:for-each select="gmd:resourceMaintenance/gmd:MD_MaintenanceInformation">
                <mdMaint>
                    <xsl:call-template name="MD_MaintenanceInformation" />
                </mdMaint>
            </xsl:for-each>

            <xsl:for-each select="gmd:dataQualityInfo/gmd:DQ_DataQuality">
                <dqInfo>
                    <xsl:call-template name="DQ_DataQuality" />
                </dqInfo>
            </xsl:for-each>

            <xsl:for-each select="gmd:portrayalCatalogueInfo/gmd:MD_PortrayalCatalogueReference">
                <porCatInfo>
                    <xsl:call-template name="MD_PortrayalCatalogueReference" />
                </porCatInfo>
            </xsl:for-each>

            <xsl:for-each select="gmd:metadataConstraints/gmd:MD_Constraints">
                <mdConst>
                    <Consts>
                        <xsl:call-template name="MD_Constraints" />
                    </Consts>
                </mdConst>
            </xsl:for-each>

            <xsl:for-each select="gmd:metadataConstraints/gmd:MD_LegalConstraints">
                <mdConst>
                    <SecConsts>
                        <xsl:call-template name="MD_LegalConstraints" />
                    </SecConsts>
                </mdConst>
            </xsl:for-each>

            <xsl:for-each select="gmd:metadataConstraints/gmd:MD_SecurityConstraints">
                <mdConst>
                    <SecConsts>
                        <xsl:call-template name="MD_SecurityConstraints" />
                    </SecConsts>
                </mdConst>
            </xsl:for-each>

            <xsl:for-each select="gmd:applicationSchemaInfo/gmd:MD_ApplicationSchemaInformation">
                <appSchInfo>
                    <xsl:call-template name="MD_ApplicationSchemaInformation" />
                </appSchInfo>
            </xsl:for-each>

            <xsl:for-each select="gmd:metadataMaintenance/gmd:MD_MaintenanceInformation">
                <mdMaint>
                    <xsl:call-template name="MD_MaintenanceInformation" />
                </mdMaint>
            </xsl:for-each>
        </metadata>
    </xsl:template>
	
	
    <!-- B.2.2 Identification information (includes data and service identification) -->
	
    <!-- B.2.2.1 General identification information -->
    <!-- MD_Identification -->
    <xsl:template name="MD_Identification">
        <!-- idCitation -->
        <xsl:for-each select="gmd:citation/gmd:CI_Citation">
            <idCitation>
                <xsl:call-template name="CI_Citation"/>
            </idCitation>
        </xsl:for-each>

        <!-- idAbs -->
        <idAbs>
            <xsl:value-of select="gmd:abstract/gco:CharacterString"/>
        </idAbs>
		
        <!-- idPurp -->
        <idPurp>
            <xsl:value-of select="gmd:purpose/gco:CharacterString"/>
        </idPurp>
		
        <!-- idCredit -->
        <xsl:for-each select="gmd:credit/gco:CharacterString">
            <idCredit>
                <xsl:value-of select="."/>
            </idCredit>
        </xsl:for-each>
		
        <!-- idStatus -->
        <xsl:for-each select="gmd:status/gmd:MD_ProgressCode">
            <idStatus>
                <ProgCd>
                    <xsl:attribute name="value">
                        <xsl:call-template name="MD_ProgressCode">
                            <xsl:with-param name="source" select="@codeListValue" />
                        </xsl:call-template>
                    </xsl:attribute>
                </ProgCd>
            </idStatus>
        </xsl:for-each>
		
        <!-- idPoc -->
        <xsl:for-each select="gmd:pointOfContact/gmd:CI_ResponsibleParty">
            <idPoc>
                <xsl:call-template name="CI_ResponsibleParty">
                    <xsl:with-param name="role" select="'pointOfContact'" />
                </xsl:call-template>
            </idPoc>
        </xsl:for-each>
	
        <!-- graphOver -->
        <xsl:for-each select="gmd:graphicOverview/gmd:MD_BrowseGraphic">
            <graphOver>
                <xsl:call-template name="MD_BrowseGraphic" />
            </graphOver>
        </xsl:for-each>

        <!-- place keywords -->		
        <xsl:for-each select="gmd:descriptiveKeywords/gmd:MD_Keywords[gmd:type/gmd:MD_KeywordTypeCode/@codeListValue='place']">
            <placeKeys>
                <xsl:call-template name="MD_Keywords" />
            </placeKeys>
        </xsl:for-each>
		
        <!-- stratum keywords -->		
        <xsl:for-each select="gmd:descriptiveKeywords/gmd:MD_Keywords[gmd:type/gmd:MD_KeywordTypeCode/@codeListValue='stratum']">
            <stratKeys>
                <xsl:call-template name="MD_Keywords" />
            </stratKeys>
        </xsl:for-each>
		
        <!-- temporal keywords -->
        <xsl:for-each select="gmd:descriptiveKeywords/gmd:MD_Keywords[gmd:type/gmd:MD_KeywordTypeCode/@codeListValue='temporal']">
            <tempKeys>
                <xsl:call-template name="MD_Keywords" />
            </tempKeys>
        </xsl:for-each>

        <!-- theme keywords -->		
        <xsl:for-each select="gmd:descriptiveKeywords/gmd:MD_Keywords[gmd:type/gmd:MD_KeywordTypeCode/@codeListValue='theme']">
            <themeKeys>
                <xsl:call-template name="MD_Keywords" />
            </themeKeys>
        </xsl:for-each>
		
        <!-- resConst -->
        <xsl:for-each select="gmd:resourceConstraints/gmd:MD_Constraints">
            <resConst>
                <xsl:call-template name="MD_Constraints" />
            </resConst>
        </xsl:for-each>

        <!-- resource constraints -->
        <xsl:for-each select="gmd:resourceConstraints/gmd:MD_LegalConstraints">
            <mdConst>
                <LegConsts>
                    <xsl:call-template name="MD_LegalConstraints" />
                </LegConsts>
            </mdConst>
        </xsl:for-each>
		
        <!-- resMaint -->
        <xsl:for-each select="gmd:resourceMaintenance/gmd:MD_MaintenanceInformation">
            <resMaint>
                <xsl:call-template name="MD_MaintenanceInformation" />
            </resMaint>
        </xsl:for-each>

        <!-- aggrInfo -->
        <xsl:for-each select="gmd:aggregationInfo/gmd:MD_AggregateInformation">
            <aggrInfo>
                <xsl:call-template name="MD_AggregateInformation" />
            </aggrInfo>
        </xsl:for-each>
		
        <!-- searchKeys -->

        <!-- discKeys -->

        <!-- otherKeys -->
        <xsl:for-each select="gmd:descriptiveKeywords/gmd:MD_Keywords[count(gmd:type/gmd:MD_KeywordTypeCode)=0]">
            <otherKeys>
                <xsl:call-template name="MD_Keywords" />
            </otherKeys>
        </xsl:for-each>
		
        <!-- idSpecUse -->
        <xsl:for-each select="gmd:resourceSpecificUsage/gmd:MD_Usage">
            <idSpecUse>
                <xsl:call-template name="MD_Usage" />
            </idSpecUse>
        </xsl:for-each>
			
        <!-- dsFormat -->
        <xsl:for-each select="gmd:resourceFormat/gmd:MD_Format">
            <dsFormat>
                <xsl:call-template name="MD_Format"/>
            </dsFormat>
        </xsl:for-each>
		
        <!-- subTopicCatKeys -->
        <xsl:for-each select="gmd:descriptiveKeywords/gmd:MD_Keywords[gmd:type/gmd:MD_KeywordTypeCode/@codeListValue='subTopicCategory']">
            <subTopicCatKeys>
                <xsl:call-template name="MD_Keywords" />
            </subTopicCatKeys>
        </xsl:for-each>
		
        <!-- productKeys -->
        <xsl:for-each select="gmd:descriptiveKeywords/gmd:MD_Keywords[gmd:type/gmd:MD_KeywordTypeCode/@codeListValue='product']">
            <productKeys>
                <xsl:call-template name="MD_Keywords" />
            </productKeys>
        </xsl:for-each>
    </xsl:template>
	
    <!-- MD_DataIdentification -->
    <xsl:template name="MD_DataIdentification">
        <xsl:call-template name="MD_Identification" />

        <!-- spatRpType -->
        <xsl:for-each select="gmd:characterSet">
            <spatRpType>
                <SpatRepTypCd>
                    <xsl:attribute name="value">
                        <xsl:call-template name="MD_SpatialRepresentationTypeCode">
                            <xsl:with-param name="source">
                                <xsl:value-of select="gmd:spatialRepresentationType/@codeListValue"/>
                            </xsl:with-param> 
                        </xsl:call-template>
                    </xsl:attribute>
                </SpatRepTypCd>
            </spatRpType>
        </xsl:for-each>

        <!-- dataScale -->
        <xsl:for-each select="gmd:spatialResolution/gmd:MD_Resolution">
            <dataScale>
                <xsl:call-template name="MD_Resolution" />
            </dataScale>
        </xsl:for-each>

        <!-- dataLang -->
        <xsl:for-each select="gmd:language">
            <dataLang>
                <languageCode>
                    <xsl:variable name="value">
                        <xsl:value-of select="gmd:languageCode/@codeListValue" />
                    </xsl:variable>
                </languageCode>
                <countryCode>
                    <xsl:variable name="value">
                        <xsl:value-of select="gmd:languageCode/@codeListValue" />
                    </xsl:variable>
                </countryCode>
            </dataLang>
        </xsl:for-each>
		
        <!-- dataChar -->
        <xsl:for-each select="gmd:characterSet">
            <dataChar>
                <CharSetCd>
                    <xsl:attribute name="value">
                        <xsl:call-template name="MD_CharacterSetCode">
                            <xsl:with-param name="source">
                                <xsl:value-of select="gmd:MD_CharacterSetCode/@codeListValue"/>
                            </xsl:with-param> 
                        </xsl:call-template>
                    </xsl:attribute>
                </CharSetCd>
            </dataChar>
        </xsl:for-each>

        <!-- tpCat -->
        <xsl:for-each select="gmd:topicCategory">
            <tpCat>
                <TopicCatCd>
                    <xsl:attribute name="value">
                        <xsl:call-template name="MD_TopicCategoryCode">
                            <xsl:with-param name="source">
                                <xsl:value-of select="gmd:MD_TopicCategoryCode" />
                            </xsl:with-param>
                        </xsl:call-template>
                    </xsl:attribute>
                </TopicCatCd>
            </tpCat>
        </xsl:for-each>

        <!-- envirDesc -->
        <envirDesc>
            <xsl:value-of select="gmd:environmentDescription/gco:CharacterString"/>
        </envirDesc>

        <!-- dataExt -->
        <xsl:for-each select="gmd:extent/gmd:EX_Extent">
            <dataExt>
                <xsl:call-template name="EX_Extent" />
            </dataExt>
        </xsl:for-each>
		
        <!-- suppInfo -->
        <suppInfo>
            <xsl:value-of select="gmd:supplementalInformation/gco:CharacterString" />
        </suppInfo>
	
    </xsl:template>
	
    <!-- B.2.2.2 Browse graphic information -->
    <!-- MD_BrowseGraphic -->
    <xsl:template name="MD_BrowseGraphic">
        <bgFileName>
            <xsl:value-of select="gmd:fileName/gco:CharacterString"/>
        </bgFileName>
        <bgFileType>
            <xsl:value-of select="gmd:fileType/gco:CharacterString"/>
        </bgFileType>
        <bgFileDesc>
            <xsl:value-of select="gmd:fileDescription/gco:CharacterString"/>
        </bgFileDesc>		
    </xsl:template>
	
    <!-- B.2.2.3 Keyword information -->
    <!-- MD_Keywords -->
    <xsl:template name="MD_Keywords">
        <xsl:for-each select="gmd:thesaurusName/gmd:CI_Citation">
            <thesaName>
                <xsl:call-template name="CI_Citation" />
            </thesaName>
        </xsl:for-each>
		
        <thesaLang>
            <languageCode>
                <xsl:variable name="value">
                    <xsl:value-of select="gmd:languageCode/@codeListValue" />
                </xsl:variable>
            </languageCode>
            <countryCode>
                <xsl:variable name="value">
                    <xsl:value-of select="gmd:languageCode/@codeListValue" />
                </xsl:variable>
            </countryCode>
        </thesaLang>
		
        <xsl:for-each select="gmd:keyword">
            <keyword>
                <xsl:value-of select="gco:CharacterString"/>
            </keyword>
        </xsl:for-each>
    </xsl:template>
	
    <!-- B.2.2.4 Representative fraction information -->
    <!-- MD_RepresentativeFraction -->
    <xsl:template name="MD_RepresentativeFraction">
        <rfDenom>
            <xsl:value-of select="gmd:denominator"/>
        </rfDenom>
    </xsl:template>

    <!-- B.2.2.5 Resolution information -->
    <!-- MD_Resolution -->
    <xsl:template name="MD_Resolution">
        <xsl:for-each select="gmd:equivalentScale/gmd:MD_RepresentativeFraction">
            <equScale>
                <xsl:call-template name="MD_RepresentativeFraction" />
            </equScale>
        </xsl:for-each>
		
        <xsl:for-each select="gmd:distance">
            <scaleDist>
                <value uom="m">
                    <xsl:attribute name="uom">
                        <xsl:value-of select="gco:Distance/@uom"/>
                    </xsl:attribute>
                    <xsl:value-of select="gco:Distance"/>
                </value>
            </scaleDist>
        </xsl:for-each>		
    </xsl:template>
	
    <!-- B.2.2.6 Usage information -->
    <!-- MD_Usage -->
    <xsl:template name="MD_Usage">
        <specUsage>
            <xsl:value-of select="gmd:specificUsage/gco:CharacterString"/>
        </specUsage>
		
        <usageDate>
            <xsl:value-of select="gmd:usageDateTime"/>
        </usageDate>
		
        <usrDetLim>
            <xsl:value-of select="gmd:userDetermindLimitations/gco:CharacterString"/>
        </usrDetLim>

        <xsl:for-each select="gmd:userContactInfo/gmd:CI_ResponsibleParty">
            <usrCntInfo>
                <xsl:call-template name="CI_ResponsibleParty" />
            </usrCntInfo>
        </xsl:for-each>
    </xsl:template>
	
    <!-- B.2.2.7 Aggregation information -->
    <!-- MD_AggregateInformation -->
    <xsl:template name="MD_AggregateInformation">
        <xsl:for-each select="gmd:aggregateDataSetName/gmd:CI_Citation">
            <aggrDSName>
                <xsl:call-template name="CI_Citation" />
            </aggrDSName>
        </xsl:for-each>
    </xsl:template>
	
    <!-- B.2.3 Constraint information (includes legal and security) -->
    <!-- MD_Constraints -->
    <xsl:template name="MD_Constraints">
        <xsl:for-each select="gmd:useLimitation/gco:CharacterString">
            <useLimit>
                <xsl:value-of select="." />
            </useLimit>
        </xsl:for-each>
        <xsl:for-each select="gmd:MD_LegalConstraints">
            <legConst>
                <xsl:call-template name="MD_LegalConstraints" />
            </legConst>
        </xsl:for-each>
        <xsl:for-each select="gmd:MD_SecurityConstraints">
            <SecConsts>
                <xsl:call-template name="MD_SecurityConstraints" />
            </SecConsts>
        </xsl:for-each>
    </xsl:template>

    <!-- MD_SecurityConstraints -->
    <xsl:template name="MD_SecurityConstraints">
        <xsl:for-each select="gmd:useLimitation/gco:CharacterString">
            <useLimit>
                <xsl:value-of select="." />
            </useLimit>
        </xsl:for-each>

        <xsl:for-each select="gmd:classification/gmd:MD_Classification/@codeListValue">
            <class>
                <ClasscationCd>
                    <xsl:attribute name="value">
                        <xsl:call-template name="MD_ClassificationCode">
                            <xsl:with-param name="source">
                                <xsl:value-of select="."/>
                            </xsl:with-param> 
                        </xsl:call-template>
                    </xsl:attribute>
                </ClasscationCd>
            </class>
        </xsl:for-each>


        <userNote>
            <xsl:value-of select="gmd:userNote/gco:CharacterString" />
        </userNote>

        <classSys>
            <xsl:value-of select="gmd:classificationSystem/gco:CharacterString" />
        </classSys>

        <handDesc>
            <xsl:value-of select="gmd:handlingDescription/gco:CharacterString" />
        </handDesc>
		
    </xsl:template>

    <!-- MD_LegalConstraints -->
    <xsl:template name="MD_LegalConstraints">
        <xsl:for-each select="gmd:useLimitation/gco:CharacterString">
            <useLimit>
                <xsl:value-of select="." />
            </useLimit>
        </xsl:for-each>

        <xsl:for-each select="gmd:accessConstraints/gmd:MD_RestrictionCode/@codeListValue">
            <accessConsts>
                <RestrictCd>
                    <xsl:attribute name="value">
                        <xsl:call-template name="MD_RestrictionCode">
                            <xsl:with-param name="source">
                                <xsl:value-of select="."/>
                            </xsl:with-param> 
                        </xsl:call-template>
                    </xsl:attribute>
                </RestrictCd>
            </accessConsts>
        </xsl:for-each>
        <xsl:for-each select="gmd:useConstraints/gmd:MD_RestrictionCode/@codeListValue">
            <useConsts>
                <RestrictCd>
                    <xsl:attribute name="value">
                        <xsl:call-template name="MD_RestrictionCode">
                            <xsl:with-param name="source">
                                <xsl:value-of select="."/>
                            </xsl:with-param> 
                        </xsl:call-template>
                    </xsl:attribute>
                </RestrictCd>
            </useConsts>
        </xsl:for-each>

        <xsl:for-each select="gmd:otherConstraints/gco:CharacterString">
            <othConsts>
                <xsl:value-of select="." />
            </othConsts>
        </xsl:for-each>
    </xsl:template>

    <!-- B.2.4 Data quality information -->
	
    <!-- B.2.4.1 General data quality information -->
    <!-- DQ_DataQuality -->
    <xsl:template name="DQ_DataQuality">
        <xsl:for-each select="gmd:scope/gmd:DQ_Scope">
            <dqScope>
                <xsl:call-template name="DQ_Scope" />
            </dqScope>
        </xsl:for-each>

        <xsl:for-each select="gmd:scope/gmd:DQ_Scope">
            <dqScope>
                <xsl:call-template name="DQ_Scope" />
            </dqScope>
        </xsl:for-each>

        <xsl:for-each select="gmd:lineage/gmd:LI_Lineage">
            <dqScope>
                <xsl:call-template name="LI_Lineage" />
            </dqScope>
        </xsl:for-each>
		
        <xsl:for-each select="gmd:report/child::*">
            <dqReport>
                <xsl:call-template name="DQ_Element" />
            </dqReport>
        </xsl:for-each>
        <!--
                        SAMPLES
        
                        simple primitive type
                        <transDimDesc>
                                <xsl:value-of select="gmd:transformationDimensionDescription/gco:CharacterString" />
                        </transDimDesc>
        
                        type is complex data type
                        <xsl:for-each select="gmd:element/gmd:Class">
                                <centerPt>
                                        <xsl:call-template name="Class" />
                                </centerPt>
                        </xsl:for-each>
                
                        type is codelist object
                        <xsl:for-each select="gmd:topologyLevel/gmd:MD_PixelOrientationCode">
                                <ptInPixel>
                                        <PixOrientCd>
                                                <xsl:attribute name="value">
                                                        <xsl:call-template name="MD_PixelOrientationCode">
                                                                <xsl:with-param name="source"><xsl:value-of select="@codeListValue" /></xsl:with-param>
                                                        </xsl:call-template>
                                                </xsl:attribute>
                                        </PixOrientCd>
                                </ptInPixel>
                        </xsl:for-each>
        -->
    </xsl:template>
	
    <!-- B.2.4.2 Lineage information -->
	
    <!-- B.2.4.2.1 General lineage information-->
    <!-- LI_Lineage -->
    <xsl:template name="LI_Lineage">
        <statement>
            <xsl:value-of select="gmd:statement/gco:CharacterString" />
        </statement>
		
        <xsl:for-each select="gmd:processStep/gmd:LI_ProcessStep">
            <prcStep>
                <xsl:call-template name="LI_ProcessStep" />
            </prcStep>
        </xsl:for-each>

        <xsl:for-each select="gmd:source/gmd:LI_Source">
            <dataSource>
                <xsl:call-template name="LI_Source" />
            </dataSource>
        </xsl:for-each>
    </xsl:template>
	
    <!-- B.2.4.2.2 Process step information -->
    <!-- LI_ProcessStep -->
    <xsl:template name="LI_ProcessStep">
        <stepDesc>
            <xsl:value-of select="gmd:description/gco:CharacterString" />
        </stepDesc>

        <stepRat>
            <xsl:value-of select="gmd:rationale/gco:CharacterString" />
        </stepRat>

        <stepDateTm>
            <xsl:value-of select="gmd:dateTime/gco:DateTime" />
        </stepDateTm>
		
        <xsl:for-each select="gmd:processor/gmd:CI_ResponsibleParty">
            <stepProc>
                <xsl:call-template name="CI_ResponsibleParty" />
            </stepProc>
        </xsl:for-each>
		
        <xsl:for-each select="gmd:source/gmd:LI_Source">
            <stepSrc>
                <xsl:call-template name="LI_Source" />
            </stepSrc>
        </xsl:for-each>
    </xsl:template>
	
    <!-- B.2.4.2.3 Source information -->
    <!-- LI_Source -->
    <xsl:template name="LI_Source">
        <srcDesc>
            <xsl:value-of select="gmd:description/gco:CharacterString" />
        </srcDesc>
		
        <xsl:for-each select="gmd:scaleDenominator/gmd:MD_RepresentativeFraction">
            <srcScale>
                <xsl:call-template name="MD_RepresentativeFraction" />
            </srcScale>
        </xsl:for-each>

        <xsl:for-each select="gmd:sourceReferenceSystem/gmd:MD_ReferenceSystem">
            <srcRefSys>
                <xsl:call-template name="MD_ReferenceSystem" />
            </srcRefSys>
        </xsl:for-each>

        <xsl:for-each select="gmd:sourceCitation/gmd:CI_Citation">
            <srcCitatn>
                <xsl:call-template name="CI_Citation" />
            </srcCitatn>
        </xsl:for-each>

        <xsl:for-each select="gmd:sourceExtent/gmd:EX_Extent">
            <srcExt>
                <xsl:call-template name="EX_Extent" />
            </srcExt>
        </xsl:for-each>

        <xsl:for-each select="gmd:sourceStep/gmd:LI_ProcessStep">
            <srcStep>
                <xsl:call-template name="LI_ProcessStep" />
            </srcStep>
        </xsl:for-each>
    </xsl:template>
	
    <!-- B.2.4.3 Data quality element information -->
    <!-- DQ_Element -->
    <xsl:template name="DQ_Element">
        <xsl:attribute name="type">
            <xsl:call-template name="DQ_ElementType">
                <xsl:with-param name="source">
                    <xsl:value-of select="name()" />
                </xsl:with-param>
            </xsl:call-template>
        </xsl:attribute>

        <measName>
            <xsl:value-of select="gmd:nameOfMeasure/gco:CharacterString" />
        </measName>
		
        <xsl:for-each select="gmd:measureIdentification/gmd:MD_Identifier">
            <measId>
                <xsl:call-template name="MD_Identifier" />
            </measId>
        </xsl:for-each>

        <measDesc>
            <xsl:value-of select="gmd:measureDescription/gco:CharacterString" />
        </measDesc>
		
        <xsl:for-each select="gmd:evaluationMethodType/gmd:DQ_EvaluationMethodTypeCode">
            <evalMethType>
                <EvalMethTypeCd>
                    <xsl:attribute name="value">
                        <xsl:call-template name="DQ_EvaluationMethodTypeCode">
                            <xsl:with-param name="source">
                                <xsl:value-of select="@codeListValue" />
                            </xsl:with-param>
                        </xsl:call-template>
                    </xsl:attribute>
                </EvalMethTypeCd>
            </evalMethType>
        </xsl:for-each>
		
        <evalMethDesc>
            <xsl:value-of select="gmd:evaluationMethodDescription/gco:CharacterString" />
        </evalMethDesc>

        <xsl:for-each select="gmd:evaluationProcedure/gmd:CI_Citation">
            <evalProc>
                <xsl:call-template name="CI_Citation" />
            </evalProc>
        </xsl:for-each>

        <xsl:for-each select="gmd:evaluationProcedure/gmd:CI_Citation">
            <measDateTm>
                <xsl:value-of select="gmd:dateTime/gco:DateTime" />
            </measDateTm>
        </xsl:for-each>
		
        <xsl:for-each select="gmd:result">
            <measResult>
                <xsl:call-template name="DQ_Result" />
            </measResult>
        </xsl:for-each>
    </xsl:template>

    <!-- DQ_Completeness -->
    <xsl:template name="DQ_Completeness">
        <xsl:call-template name="DQ_Element" />
    </xsl:template>

    <!-- DQ_CompletenessCommission -->
    <xsl:template name="DQ_CompletenessCommission">
        <xsl:call-template name="DQ_Element" />
    </xsl:template>

    <!-- DQ_CompletenessOmission -->
    <xsl:template name="DQ_CompletenessOmission">
        <xsl:call-template name="DQ_Element" />
    </xsl:template>

    <!-- DQ_LogicalConsistency -->
    <xsl:template name="DQ_LogicalConsistency">
        <xsl:call-template name="DQ_Element" />
    </xsl:template>

    <!-- DQ_ConceptualConsistency -->
    <xsl:template name="DQ_ConceptualConsistency">
        <xsl:call-template name="DQ_Element" />
    </xsl:template>

    <!-- DQ_DomainConsistency -->
    <xsl:template name="DQ_DomainConsistency">
        <xsl:call-template name="DQ_Element" />
    </xsl:template>

    <!-- DQ_FormatConsistency -->
    <xsl:template name="DQ_FormatConsistency">
        <xsl:call-template name="DQ_Element" />
    </xsl:template>

    <!-- DQ_TopologicalConsistency -->
    <xsl:template name="DQ_TopologicalConsistency">
        <xsl:call-template name="DQ_Element" />
    </xsl:template>

    <!-- DQ_PositionalAccuracy -->
    <xsl:template name="DQ_PositionalAccuracy">
        <xsl:call-template name="DQ_Element" />
    </xsl:template>

    <!-- DQ_AbsoluteExternalPositionalAccuracy -->
    <xsl:template name="DQ_AbsoluteExternalPositionalAccuracy">
        <xsl:call-template name="DQ_Element" />
    </xsl:template>

    <!-- DQ_GriddedDataPositionalAccuracy -->
    <xsl:template name="DQ_GriddedDataPositionalAccuracy">
        <xsl:call-template name="DQ_Element" />
    </xsl:template>

    <!-- DQ_RelativeInternalPositionalAccuracy -->
    <xsl:template name="DQ_RelativeInternalPositionalAccuracy">
        <xsl:call-template name="DQ_Element" />
    </xsl:template>

    <!-- DQ_TemporalAccuracy -->
    <xsl:template name="DQ_TemporalAccuracy">
        <xsl:call-template name="DQ_Element" />
    </xsl:template>

    <!-- DQ_AccuracyOfATimeMeasurement -->
    <xsl:template name="DQ_AccuracyOfATimeMeasurement">
        <xsl:call-template name="DQ_Element" />
    </xsl:template>

    <!-- DQ_TemporalConsistency -->
    <xsl:template name="DQ_TemporalConsistency">
        <xsl:call-template name="DQ_Element" />
    </xsl:template>

    <!-- DQ_TemporalValidity -->
    <xsl:template name="DQ_TemporalValidity">
        <xsl:call-template name="DQ_Element" />
    </xsl:template>

    <!-- DQ_ThematicAccuracy -->
    <xsl:template name="DQ_ThematicAccuracy">
        <xsl:call-template name="DQ_Element" />
    </xsl:template>

    <!-- DQ_ThematicClassificationCorrectness -->
    <xsl:template name="DQ_ThematicClassificationCorrectness">
        <xsl:call-template name="DQ_Element" />
    </xsl:template>

    <!-- DQ_NonQuantitativeAttributeAccuracy -->
    <xsl:template name="DQ_NonQuantitativeAttributeAccuracy">
        <xsl:call-template name="DQ_Element" />
    </xsl:template>

    <!-- DQ_QuantitativeAttributeAccuracy -->
    <xsl:template name="DQ_QuantitativeAttributeAccuracy">
        <xsl:call-template name="DQ_Element" />
    </xsl:template>
	
    <!-- B.2.4.4 Result information -->
    <!-- DQ_Result -->
    <xsl:template name="DQ_Result">
        <xsl:for-each select="gmd:DQ_ConformanceResult">
            <xsl:call-template name="DQ_ConformanceResult" />
        </xsl:for-each>
        <xsl:for-each select="gmd:DQ_QuantitativeResult">
            <xsl:call-template name="DQ_QuantitativeResult" />
        </xsl:for-each>
    </xsl:template>

    <!-- DQ_ConformanceResult -->
    <xsl:template name="DQ_ConformanceResult">
        <xsl:for-each select="gmd:specification/gmd:CI_Citation">
            <conSpec>
                <xsl:call-template name="CI_Citation" />
            </conSpec>
        </xsl:for-each>
		
        <conExpl>
            <xsl:value-of select="gmd:explanation/gco:CharacterString" />
        </conExpl>
		
        <conPass>
            <xsl:value-of select="gmd:pass/gco:Boolean" />
        </conPass>
    </xsl:template>
	
    <!-- DQ_QuantitativeResult -->
    <xsl:template name="DQ_QuantitativeResult">
        <quanValType>
            <xsl:value-of select="gmd:valueType/gco:RecordType" />
        </quanValType>
		
        <xsl:for-each select="gmd:valueUnit/gml:UnitDefinition">
            <quanValUnit>
                <xsl:call-template name="UnitOfMeasure" />
            </quanValUnit>
        </xsl:for-each>
		
        <errStat>
            <xsl:value-of select="gmd:errorStatistic/gco:CharacterString" />
        </errStat>

        <xsl:for-each select="gmd:value">
            <quanVal>
                <xsl:value-of select="gco:Record" />
            </quanVal>
        </xsl:for-each>	
    </xsl:template>
	
    <!-- B.2.4.5 Scope information -->
    <!-- DQ_Scope -->
    <xsl:template name="DQ_Scope">
        <xsl:for-each select="gmd:level/gmd:MD_ScopeCode">
            <scpLvl>
                <ScopeCd>
                    <xsl:attribute name="value">
                        <xsl:call-template name="MD_ScopeCode">
                            <xsl:with-param name="source">
                                <xsl:value-of select="@codeListValue" />
                            </xsl:with-param>
                        </xsl:call-template>
                    </xsl:attribute>
                </ScopeCd>
            </scpLvl>
        </xsl:for-each>
        <xsl:for-each select="gmd:extent/gmd:EX_Extent">
            <scpExt>
                <xsl:call-template name="EX_Extent" />
            </scpExt>
        </xsl:for-each>
        <xsl:for-each select="gmd:levelDescription/gmd:MD_ScopeDescription">
            <scpLvlDesc>
                <xsl:call-template name="MD_ScopeDescription" />
            </scpLvlDesc>
        </xsl:for-each>	
    </xsl:template>
	
    <!-- B.2.5 Maintenance information -->

    <!-- B.2.5.1 General maintenance information -->
    <!-- MD_MaintenanceInformation -->
    <xsl:template name="MD_MaintenanceInformation">
        <!-- maintFreq -->
        <maintFreq>
            <MaintFreqCd>
                <xsl:attribute name="value">
                    <xsl:call-template name="MD_MaintenanceFrequencyCode">
                        <xsl:with-param name="source">
                            <xsl:value-of select="gmd:maintenanceAndUpdateFrequency/gmd:MD_MaintenanceFrequencyCode/@codeListValue"/>
                        </xsl:with-param>
                    </xsl:call-template>
                </xsl:attribute>
            </MaintFreqCd>
        </maintFreq>
		
        <!-- dateNext -->
        <dateNext>
            <xsl:value-of select="gmd:dateOfNextUpdate"/>
        </dateNext>
				
        <!-- userDefFreq -->
        <userDefFreq>
            <duration>
                <xsl:value-of select="gmd:userDefinedMaintenanceFrequency/gts:TM_PeriodDuration"/>
            </duration>
        </userDefFreq>
		
        <!-- maintScp -->
        <xsl:for-each select="gmd:updateScope/gmd:MD_ScopeCode">
            <maintScp>
                <xsl:call-template name="MD_ScopeCode"/>
            </maintScp>
        </xsl:for-each>

        <!-- upScpDesc -->
        <upScpDesc>
            <xsl:for-each select="gmd:updateScopeDescription/gmd:MD_ScopeDescription">
                <xsl:call-template name="MD_ScopeDescription"/>
            </xsl:for-each>
        </upScpDesc>
		
        <!-- maintNote -->
        <xsl:for-each select="gmd:maintenanceNote">
            <maintNote>
                <xsl:value-of select="gmd:maintenanceNote/gco:CharacterString" />
            </maintNote>
        </xsl:for-each>

        <!-- maintCont -->
        <xsl:for-each select="gmd:contact/gmd:CI_ResponsibleParty">
            <maintCont>
                <xsl:call-template name="CI_ResponsibleParty"/>
            </maintCont>
        </xsl:for-each>
    </xsl:template>
	
	
    <!-- B.2.5.2 Scope description information -->
    <!-- MD_ScopeDescription -->
    <xsl:template name="MD_ScopeDescription">
        <xsl:for-each select="gmd:attributes">
            <attribSet>
                <xsl:value-of select="@uuidref" />
            </attribSet>
        </xsl:for-each>
        <xsl:for-each select="gmd:features">
            <featSet>
                <xsl:value-of select="@uuidref" />
            </featSet>
        </xsl:for-each>
        <xsl:for-each select="gmd:featureInstances">
            <featIntSet>
                <xsl:value-of select="@uuidref" />
            </featIntSet>
        </xsl:for-each>
        <xsl:for-each select="gmd:attributeInstances">
            <attribIntSet>
                <xsl:value-of select="@uuidref" />
            </attribIntSet>
        </xsl:for-each>
        <xsl:for-each select="gmd:dataset">
            <datasetSet>
                <xsl:value-of select="@uuidref" />
            </datasetSet>
        </xsl:for-each>
        <xsl:for-each select="gmd:other">
            <other>
                <xsl:value-of select="@uuidref" />
            </other>
        </xsl:for-each>
    </xsl:template>

    <!-- B.2.6 Spatial representation information (includes grid and vector representation) -->
	
    <!-- B.2.6.1 General spatial representation information -->
    <!-- MD_SpatialRepresentation -->
    <xsl:template name="MD_SpatialRepresentation">
        <xsl:variable name="spatialRepresentationInfoSubclass">
            <xsl:value-of select="name()" />
        </xsl:variable>
        <xsl:choose>
            <xsl:when test="$spatialRepresentationInfoSubclass = 'MD_GridSpatialRepresentation'">
                <xsl:call-template name="MD_GridSpatialRepresentation" />
            </xsl:when>
            <xsl:when test="$spatialRepresentationInfoSubclass = 'MD_Georectified'">
                <xsl:call-template name="MD_Georectified" />
            </xsl:when>
            <xsl:when test="$spatialRepresentationInfoSubclass = 'MD_Georeferenceable'">
                <xsl:call-template name="MD_Georeferenceable" />
            </xsl:when>
            <xsl:when test="$spatialRepresentationInfoSubclass = 'MD_VectorSpatialRepresentation'">
                <xsl:call-template name="MD_VectorSpatialRepresentation" />
            </xsl:when>
        </xsl:choose>
    </xsl:template>

    <!-- MD_GridSpatialRepresentation -->
    <xsl:template name="MD_GridSpatialRepresentation">
        <xsl:for-each select="gmd:numberOfDimensions">
            <numDims>
                <xsl:value-of select="gco:Integer"/>
            </numDims>
        </xsl:for-each>

        <xsl:for-each select="gmd:axisDimensionProperties/gmd:MD_Dimension">
            <axisDimension>
                <xsl:call-template name="MD_Dimension" />
            </axisDimension>
        </xsl:for-each>
		
        <xsl:for-each select="gmd:cellGeometry/gmd:MD_CellGeometryCode">
            <cellGeo>
                <CellGeoCd>
                    <xsl:attribute name="value">
                        <xsl:call-template name="MD_CellGeometryCode">
                            <xsl:with-param name="source">
                                <xsl:value-of select="@codeListValue" />
                            </xsl:with-param>
                        </xsl:call-template>
                    </xsl:attribute>
                </CellGeoCd>
            </cellGeo>
        </xsl:for-each>
		
        <tranParaAv>
            <xsl:value-of select="gmd:transformationParameterAvailability/gco:Boolean" />
        </tranParaAv>
    </xsl:template>
	
    <!-- MD_Georectified -->
    <xsl:template name="MD_Georectified">
        <xsl:call-template name="MD_GridSpatialRepresentation"/>
		
        <chkPtDesc>
            <xsl:value-of select="gmd:checkPointAvailability/gco:Boolean" />
        </chkPtDesc>
		
        <xsl:for-each select="gmd:cornerPoints/gml:Point">
            <cornerPts>
                <xsl:call-template name="GM_Point" />
            </cornerPts>
        </xsl:for-each>

        <xsl:for-each select="gmd:centerPoint/gml:Point">
            <centerPt>
                <xsl:call-template name="GM_Point" />
            </centerPt>
        </xsl:for-each>
	
        <xsl:for-each select="gmd:topologyLevel/gmd:MD_PixelOrientationCode">
            <ptInPixel>
                <PixOrientCd>
                    <xsl:attribute name="value">
                        <xsl:call-template name="MD_PixelOrientationCode">
                            <xsl:with-param name="source">
                                <xsl:value-of select="@codeListValue" />
                            </xsl:with-param>
                        </xsl:call-template>
                    </xsl:attribute>
                </PixOrientCd>
            </ptInPixel>
        </xsl:for-each>
		
        <transDimDesc>
            <xsl:value-of select="gmd:transformationDimensionDescription/gco:CharacterString" />
        </transDimDesc>
		
        <transDimMap>
            <xsl:value-of select="gmd:transformationDimensionMapping/gco:CharacterString" />
        </transDimMap>		
    </xsl:template>
	
    <!-- MD_Georeferenceable -->
    <xsl:template name="MD_Georeferenceable">
        <xsl:call-template name="MD_GridSpatialRepresentation"/>
		
        <ctrlPtAv>
            <xsl:value-of select="gmd:controlPointAvailability/gco:Boolean" />
        </ctrlPtAv>
        <orieParaAv>
            <xsl:value-of select="gmd:orientationParameterAvailability/gco:Boolean" />
        </orieParaAv>
        <orieParaDs>
            <xsl:value-of select="gmd:orientationParameterDescription/gco:CharacterString" />
        </orieParaDs>
        <georefPars>
            <xsl:value-of select="gmd:georeferencedParameters/gco:Record" />
        </georefPars>
        <xsl:for-each select="gmd:cornerPoints/gml:Point">
            <paraCit>
                <xsl:call-template name="CI_Citation" />
            </paraCit>
        </xsl:for-each>
    </xsl:template>
	
    <!-- MD_VectorSpatialRepresentation -->
    <xsl:template name="MD_VectorSpatialRepresentation">
        <xsl:for-each select="gmd:topologyLevel/gmd:MD_TopologyLevelCode">
            <topLvl>
                <TopoLevCd>
                    <xsl:attribute name="value">
                        <xsl:call-template name="MD_TopologyLevelCode">
                            <xsl:with-param name="source">
                                <xsl:value-of select="@codeListValue" />
                            </xsl:with-param>
                        </xsl:call-template>
                    </xsl:attribute>
                </TopoLevCd>
            </topLvl>
        </xsl:for-each>
	
        <xsl:for-each select="gmd:geometricObjects/gmd:MD_GeometricObjects">
            <geometObjs>
                <xsl:call-template name="MD_GeometricObjects" />
            </geometObjs>
        </xsl:for-each>		
    </xsl:template>
	
    <!-- B.2.6.2 Dimension information -->
    <!-- MD_Dimension -->
    <xsl:template name="MD_Dimension">
        <xsl:for-each select="gmd:dimensionName/gmd:MD_DimensionNameTypeCode">
            <dimName>
                <DimNameCd>
                    <xsl:attribute name="value">
                        <xsl:call-template name="MD_DimensionNameTypeCode">
                            <xsl:with-param name="source">
                                <xsl:value-of select="@codeListValue" />
                            </xsl:with-param>
                        </xsl:call-template>
                    </xsl:attribute>
                </DimNameCd>
            </dimName>
        </xsl:for-each>
		
        <dimSize>
            <xsl:value-of select="gmd:dimensionSize"/>
        </dimSize>
		
        <xsl:for-each select="gmd:resolution/gmd:Measure">
            <dimResol>
                <xsl:call-template name="Measure" />
            </dimResol>
        </xsl:for-each>		
    </xsl:template>
	
    <!-- B.2.6.3 Geometric object information -->
    <!-- MD_GeometricObjects -->
    <xsl:template name="MD_GeometricObjects">
        <xsl:for-each select="gmd:geometricObjectType/gmd:MD_GeometricObjectTypeCode">
            <geoObjTyp>
                <GeoObjTypCd>
                    <xsl:attribute name="value">
                        <xsl:call-template name="MD_GeometricObjectTypeCode">
                            <xsl:with-param name="source">
                                <xsl:value-of select="@codeListValue" />
                            </xsl:with-param>
                        </xsl:call-template>
                    </xsl:attribute>
                </GeoObjTypCd>
            </geoObjTyp>
        </xsl:for-each>
	
        <geoObjCnt>
            <xsl:value-of select="gmd:geometricObjectCount"/>
        </geoObjCnt>
    </xsl:template>

    <!-- B.2.7 Reference system information (includes temporal, coordinate and geographic identifiers) -->
	
    <!-- B.2.7.1 General reference system inforation -->
    <!-- MD_ReferenceSystem -->
    <xsl:template name="MD_ReferenceSystem">
        <xsl:for-each select="gmd:referenceSystemIdentifier/gmd:RS_Identifier">
            <refSysId>
                <xsl:call-template name="RS_Identifier" />
            </refSysId>
        </xsl:for-each>		
    </xsl:template>
	
    <!-- RS_ReferenceSystem -->
    <xsl:template name="RS_ReferenceSystem">
        <xsl:for-each select="gmd:name/gmd:RS_Identifier">
            <refSysName>
                <xsl:call-template name="RS_Identifier" />
            </refSysName>
        </xsl:for-each>		

        <xsl:for-each select="gmd:domainOfValidity/gmd:EX_Extent">
            <domOValid>
                <xsl:call-template name="EX_Extent" />
            </domOValid>
        </xsl:for-each>	
    </xsl:template>
	
    <!-- B.2.7.2 Ellipsoid parameter information -->
    <!-- see ISO 19111 -->
	
    <!-- B.2.7.3 Identifier information -->
    <!-- MD_Identifier -->
    <xsl:template name="MD_Identifier">
        <xsl:for-each select="gmd:authority/gmd:CI_Citation">
            <identAuth>
                <xsl:call-template name="CI_Citation"/>
            </identAuth>
        </xsl:for-each>
        <identCode>
            <xsl:value-of select="gmd:code/gco:CharacterString"/>
        </identCode>
    </xsl:template>
	
    <!-- RS_Identifier -->
    <xsl:template name="RS_Identifier">
        <xsl:call-template name="MD_Identifier" />
		
        <identCodeSpace>
            <xsl:value-of select="gmd:codeSpace/gco:CharacterString"/>
        </identCodeSpace>
        <identVrsn>
            <xsl:value-of select="gmd:version/gco:CharacterString"/>
        </identVrsn>
    </xsl:template>
	
	
    <!-- B.2.8 Content information (includes Feature catalogue and Coverage descriptions) -->

    <!-- B.2.8.1 General content information -->
    <!-- MD_ContentInformation (abstract) -->
    <xsl:template name="MD_ContentInformation">
        <xsl:variable name="subclass">
            <xsl:value-of select="name()" />
        </xsl:variable>
        <xsl:choose>
            <xsl:when test="$subclass = 'MD_FeatureCatalogueDescription'">
                <xsl:call-template name="MD_FeatureCatalogueDescription" />
            </xsl:when>
            <xsl:when test="$subclass = 'MD_CoverageDescription'">
                <xsl:call-template name="MD_CoverageDescription" />
            </xsl:when>
            <xsl:when test="$subclass = 'gmi:MI_CoverageDescription'">
                <xsl:call-template name="MD_CoverageDescription" />
            </xsl:when>
            <xsl:when test="$subclass = 'MD_ImageDescription'">
                <xsl:call-template name="MD_ImageDescription" />
            </xsl:when>
            <xsl:when test="$subclass = 'gmi:MI_ImageDescription'">
                <xsl:call-template name="MD_ImageDescription" />
            </xsl:when>
            <xsl:otherwise>
                <ERROR>
                    <xsl:value-of select="$subclass" />
                </ERROR>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
	
    <!-- MD_FeatureCatalogueDescription -->
    <xsl:template name="MD_FeatureCatalogueDescription">
        <compCode>
            <xsl:value-of select="gmd:complianceCode" />
        </compCode>
		
        <xsl:for-each select="gmd:language">
            <catLang>
                <xsl:value-of select="gco:CharacterString" />
            </catLang>
        </xsl:for-each>
		
        <incWithDS>
            <xsl:value-of select="gmd:includedWithDataset" />
        </incWithDS>
		
        <xsl:for-each select="gmd:featureTypes">
            <catFetTyps>
                <xsl:value-of select="gco:LocalName" />
            </catFetTyps>
        </xsl:for-each>

        <xsl:for-each select="gmd:featureCatalogueCitation/gmd:CI_Citation">
            <catCitation>
                <xsl:call-template name="CI_Citation" />
            </catCitation>
        </xsl:for-each>		
    </xsl:template>

    <!-- MD_CoverageDescription -->
    <xsl:template name="MD_CoverageDescription">
        <attDesc>
            <xsl:value-of select="gmd:attributeDescription/gco:RecordType" />
        </attDesc>
		
        <xsl:for-each select="gmd:contentType/gmd:MD_CoverageContentTypeCode">
            <imagCond>
                <ImgCondCd>
                    <xsl:attribute name="value">
                        <xsl:call-template name="MD_CoverageContentTypeCode">
                            <xsl:with-param name="source">
                                <xsl:value-of select="@codeListValue" />
                            </xsl:with-param>
                        </xsl:call-template>
                    </xsl:attribute>
                </ImgCondCd>
            </imagCond>
        </xsl:for-each>

        <xsl:for-each select="gmd:dimension/gmd:MD_RangeDimension">
            <covDim>
                <xsl:call-template name="MD_RangeDimension" />
            </covDim>
        </xsl:for-each>		
		
        <xsl:for-each select="gmd:dimension/gmd:MD_Band">
            <Band>
                <xsl:call-template name="MD_Band" />
            </Band>
        </xsl:for-each>		
		
        <xsl:for-each select="gmi:rangeElementDescription/gmi:MI_RangeElementDescription">
            <covDim>
                <xsl:call-template name="MI_RangeElementDescription" />
            </covDim>
        </xsl:for-each>		
    </xsl:template>
	
    <!-- MD_ImageDescription -->
    <xsl:template name="MD_ImageDescription">
        <xsl:call-template name="MD_CoverageDescription" />
	
        <illElevAng>
            <xsl:value-of select="gmd:illuminationElevationAngle" />
        </illElevAng>
        <illAziAng>
            <xsl:value-of select="gmd:illuminationAzimuthAngle" />
        </illAziAng>
		
        <xsl:for-each select="gmd:imagingCondition/MD_ImagingConditionCode">
            <imagCond>
                <ImgCondCd>
                    <xsl:attribute name="value">
                        <xsl:call-template name="MD_ImagingConditionCode">
                            <xsl:with-param name="source">
                                <xsl:value-of select="@codeListValue" />
                            </xsl:with-param>
                        </xsl:call-template>
                    </xsl:attribute>
                </ImgCondCd>
            </imagCond>
        </xsl:for-each>
		
        <xsl:for-each select="gmd:imageQualityCode/gmd:MD_Identifier">
            <imagQuCode>
                <xsl:call-template name="MD_Identifier" />
            </imagQuCode>
        </xsl:for-each>
		
        <cloudCovPer>
            <xsl:value-of select="gmd:cloudCoverPercentage" />
        </cloudCovPer>
        <prcTypCde>
            <xsl:value-of select="gmd:processingLevelCode" />
        </prcTypCde>
        <cmpGenQuan>
            <xsl:value-of select="gmd:compressionGenerationQuantity" />
        </cmpGenQuan>
        <trianInd>
            <xsl:value-of select="gmd:triangulationIndicator" />
        </trianInd>
		
        <radCalDatAv>
            <xsl:value-of select="gmd:radiometricCalibrationDataAvailability" />
        </radCalDatAv>
        <camCalInAv>
            <xsl:value-of select="gmd:radiometricCalibrationDataAvailability" />
        </camCalInAv>
        <filmDistInAv>
            <xsl:value-of select="gmd:filmDistortionInformationAvailability" />
        </filmDistInAv>
        <lensDistInAv>
            <xsl:value-of select="gmd:lensDistortionInformationAvailability" />
        </lensDistInAv>
		
    </xsl:template>

		
    <!-- B.2.8.2 Range dimension information (includes Band information) -->
    <!-- MD_RangeDimension -->
    <xsl:template name="MD_RangeDimension">
        <seqID>
            <aName>
                <xsl:value-of select="gco:MemberName/gco:aName/gco:CharacterString" />
            </aName>
            <attributeType>
                <aName>
                    <xsl:value-of select="gco:MemberName/gco:attributeType/gco:TypeName/gco:aName/gco:CharacterString" />
                </aName>
            </attributeType>
        </seqID>
        <dimDescrp>
            <xsl:value-of select="gmd:descriptor/gco:CharacterString" />
        </dimDescrp>
    </xsl:template>
	
    <!-- MD_Band -->
    <xsl:template name="MD_Band">
        <seqID>
            <aName>
                <xsl:value-of select="gmd:sequenceIdentifier/gco:MemberName/gco:aName/gco:CharacterString" />
            </aName>
            <attributeType>
                <aName>
                    <xsl:value-of select="gmd:sequenceIdentifier/gco:MemberName/gco:attributeType/gco:TypeName/gco:aName/gco:CharacterString" />
                </aName>
            </attributeType>
        </seqID>
		
        <xsl:if test="count(gmd:descriptor)>0">
            <dimDescrp>
                <xsl:value-of select="gmd:descriptor/gco:CharacterString" />
            </dimDescrp>
        </xsl:if>
		
        <xsl:if test="count(gmd:maxValue)>0">
            <maxVal>
                <xsl:value-of select="gmd:maxValue"/>
            </maxVal>
        </xsl:if>
		
        <xsl:if test="count(gmd:minValue)>0">
            <minVal>
                <xsl:value-of select="gmd:minValue"/>
            </minVal>
        </xsl:if>
		
        <xsl:if test="count(gmd:units)>0">
            <valUnit>
                <xsl:for-each select="gmd:units/gmd:UomLength">
                    <xsl:call-template name="UnitOfMeasure" />
                </xsl:for-each>
            </valUnit>
        </xsl:if>
		
        <xsl:if test="count(gmd:peakResponse)>0">
            <pkResp>
                <xsl:value-of select="gmd:peakResponse" />
            </pkResp>
        </xsl:if>
		
        <xsl:if test="count(gmd:bitsPerValue)>0">
            <bitsPerVal>
                <xsl:value-of select="gmd:bitsPerValue" />
            </bitsPerVal>
        </xsl:if>
		
        <xsl:if test="count(gmd:toneGradation)>0">
            <toneGrad>
                <xsl:value-of select="gmd:toneGradation" />
            </toneGrad>
        </xsl:if>
		
        <xsl:if test="count(gmd:scaleFactor)>0">
            <sclFac>
                <xsl:value-of select="gmd:scaleFactor" />
            </sclFac>
        </xsl:if>
		
        <xsl:if test="count(gmd:offset)>0">
            <offset>
                <xsl:value-of select="gmd:offset" />
            </offset>
        </xsl:if>
    </xsl:template>
	
    <!-- B.2.9 Portrayal catalogue information -->
    <!-- MD_PortrayalCatalogueReference -->
    <xsl:template name="MD_PortrayalCatalogueReference">
        <xsl:for-each select="gmd:MD_PortrayalCatalogueReference/gmd:CI_Citation">
            <portCatCit>
                <xsl:call-template name="CI_Citation" />
            </portCatCit>
        </xsl:for-each>
    </xsl:template>
	
    <!-- B.2.10 Distribution information -->
	
    <!-- B.2.10.1 General distribution information -->
	
    <!-- MD_Distribution -->
    <xsl:template name="MD_Distribution">
        <xsl:for-each select="gmd:distributionFormat/gmd:MD_Format">
            <distFormat>
                <xsl:call-template name="MD_Format"/>
            </distFormat>
        </xsl:for-each>
	
        <xsl:for-each select="gmd:distributor/gmd:MD_Distributor">
            <distributor>
                <xsl:call-template name="MD_Distributor"/>
            </distributor>
        </xsl:for-each>
	
        <xsl:for-each select="gmd:transferOptions/gmd:MD_DigitalTransferOptions">
            <distTranOps>
                <xsl:call-template name="MD_DigitalTransferOptions"/>
            </distTranOps>
        </xsl:for-each>
    </xsl:template>
	
	
    <!-- B.2.10.2 Digital transfer options information -->
    <!-- MD_DigitalTransferOptions -->
    <xsl:template name="MD_DigitalTransferOptions">
        <unitsODist>
            <xsl:value-of select="gmd:unitsOfDistribution/gco:CharacterString"/>
        </unitsODist>
        <transSize>
            <xsl:value-of select="gmd:transferSize"/>
        </transSize>

        <xsl:for-each select="gmd:offLine/gmd:MD_Medium">
            <offLineMed>
                <xsl:call-template name="MD_Medium"/>
            </offLineMed>
        </xsl:for-each>

        <!--
                        <offLineMed>
        
                                <medName>
                                        <MedNameCd>
                                                <xsl:attribute name="value">
                                                        <xsl:call-template name="MD_MediumNameCode">
                                                        <xsl:with-param name="source"><xsl:value-of select="gmd:offLine/gmd:MD_Medium/gmd:name/gmd:MD_MediumNameCode/@codeListValue"/></xsl:with-param> 
                                                </xsl:call-template>
                                                </xsl:attribute>
                                        </MedNameCd>
                                </medName>
                                <medNote>
                                        <xsl:value-of select="gmd:offLine/gmd:MD_Medium/gmd:mediumNote/gco:CharacterString"/>
                                </medNote>
                        </offLineMed>
        -->
        <xsl:for-each select="gmd:onLine/gmd:CI_OnlineResource">
            <onLineSrc>
                <xsl:call-template name="CI_OnlineResource"/>
            </onLineSrc>
        </xsl:for-each>
    </xsl:template>
	
	
    <!-- B.2.10.3 Distributor information -->
    <!-- MD_Distributor -->
    <xsl:template name="MD_Distributor">
        <xsl:for-each select="gmd:distributorContact/gmd:CI_ResponsibleParty">
            <distorCont>
                <xsl:call-template name="CI_ResponsibleParty"/>
            </distorCont>
        </xsl:for-each>
        <role>
            <RoleCd value="005"/>
        </role>

        <xsl:for-each select="gmd:distributionFormat/gmd:MD_Format">
            <distorFormat>
                <formatName>
                    <xsl:value-of select="gmd:name/gco:CharacterString"/>
                </formatName>
                <formatVer>
                    <xsl:value-of select="gmd:version/gco:CharacterString"/>
                </formatVer>
            </distorFormat>
        </xsl:for-each>
        <xsl:for-each select="gmd:transferOptions/gmd:MD_DigitalTransferOptions">
            <distorTran>
                <xsl:call-template name="MD_DigitalTransferOptions"/>
            </distorTran>
        </xsl:for-each>
    
        <xsl:for-each select="gmd:distributorTransferOptions/gmd:MD_DigitalTransferOptions">
            <distorTran>
                <xsl:call-template name="MD_DigitalTransferOptions"/>
            </distorTran>
        </xsl:for-each>

        <xsl:for-each select="gmd:distributionOrderProcess/gmd:MD_StandardOrderProcess">
            <distorOrdPrc>
                <xsl:call-template name="MD_StandardOrderProcess"/>
            </distorOrdPrc>
        </xsl:for-each>	
    </xsl:template>
	
    <!-- B.2.10.4 Format information -->
    <!-- MD_Format -->
    <xsl:template name="MD_Format">
        <formatName>
            <xsl:value-of select="gmd:name/gco:CharacterString"/>
        </formatName>
        <formatVer>
            <xsl:value-of select="gmd:version/gco:CharacterString"/>
        </formatVer>
        <formatAmdNum>
            <xsl:value-of select="gmd:amendmentNumber/gco:CharacterString"/>
        </formatAmdNum>
        <formatSpec>
            <xsl:value-of select="gmd:specification/gco:CharacterString"/>
        </formatSpec>
        <fileDecmTech>
            <xsl:value-of select="gmd:fileDecompressionTechnique/gco:CharacterString"/>
        </fileDecmTech>
        <xsl:for-each select="gmd:formatDistributor/gmd:MD_Distributor">
            <formatDist>
                <xsl:call-template name="MD_Distributor"/>
            </formatDist>
        </xsl:for-each>
    </xsl:template>
	
    <!-- B.2.10.5 Medium information -->
    <!-- MD_Medium -->
    <xsl:template name="MD_Medium">
        <medName>
            <xsl:call-template name="MD_MediumNameCode">
                <xsl:with-param name="source">
                    <xsl:value-of select="gmd:name/gmd:MD_MediumNameCode"/>
                </xsl:with-param>
            </xsl:call-template>
        </medName>
        <xsl:for-each select="gmd:density">
            <medDensity>
                <xsl:value-of select="gmd:density" />
            </medDensity>
        </xsl:for-each>
        <medDenUnits>
            <xsl:value-of select="gmd:densityUnits/gco:CharacterString" />
        </medDenUnits>
        <medVol>
            <xsl:value-of select="gmd:volumes" />
        </medVol>
		
        <xsl:for-each select="gmd:mediumFormat/gmd:MD_MediumFormatCode">
            <medDensity>
                <xsl:call-template name="MD_MediumFormatCode"/>
            </medDensity>
        </xsl:for-each>
		
    </xsl:template>
	
    <!-- B.2.10.6 Standard order process information -->
    <!-- MD_StandardOrderProcess -->	
    <xsl:template name="MD_StandardOrderProcess">
        <resFees units="USD">
            <xsl:value-of select="gmd:fees/gco:CharacterString" />
        </resFees>
        <planAvDtTm>
            <xsl:value-of select="gmd:plannedAvailableDateTime/gco:DateTime" />
        </planAvDtTm>
        <ordInstr>
            <xsl:value-of select="gmd:orderingInstructions/gco:CharacterString" />
        </ordInstr>
        <ordTurn>
            <xsl:value-of select="gmd:turnaround/gco:CharacterString" />
        </ordTurn>	
    </xsl:template>


    <!-- B.2.11 Metadata extension information -->

    <!-- B.2.11.1 General extension information -->
    <!-- MD_MetadataExtensionInformation -->
    <xsl:template name="MD_MetadataExtensionInformation">
        <xsl:for-each select="gmd:extensionOnlineResource/gmd:CI_OnlineResource">
            <extOnRes>
                <xsl:call-template name="CI_OnlineResource" />
            </extOnRes>
        </xsl:for-each>
		
        <xsl:for-each select="gmd:extendedElementInformation/gmd:MD_ExtendedElementInformation">
            <extEleInfo>
                <xsl:call-template name="MD_ExtendedElementInformation" />
            </extEleInfo>
        </xsl:for-each>
    </xsl:template>

    <!-- B.2.11.2 Extended element information -->
    <!-- MD_ExtendedElementInformation -->
    <xsl:template name="MD_ExtendedElementInformation">
        <!-- extEleName -->
        <extEleName>
            <xsl:value-of select="gmd:name/gco:CharacterString"/>
        </extEleName>
		
        <!-- extShortName -->
        <extShortName>
            <xsl:value-of select="gmd:shortName/gco:CharacterString"/>
        </extShortName>

        <!-- extDomCode -->
        <extDomCode>
            <xsl:value-of select="gmd:domainCode"/>
        </extDomCode>

        <!-- extEleDef -->
        <extEleDef>
            <xsl:value-of select="gmd:definition/gco:CharacterString"/>
        </extEleDef>

        <!-- extEleOb -->
        <extEleOb>
            <xsl:for-each select="gmd:obligation/gmd:MD_ObligationCode">
                <xsl:call-template name="MD_ObligationCode">
                    <xsl:with-param name="source" select="@codeListValue"/>
                </xsl:call-template>
            </xsl:for-each>
        </extEleOb>

        <!-- extEleCond -->
        <extEleCond>
            <xsl:value-of select="gmd:condition/gco:CharacterString"/>
        </extEleCond>

        <!-- extEleDataType -->
        <extEleDataType>
            <xsl:for-each select="gmd:dataType/gmd:MD_DatatypeCode">
                <xsl:call-template name="MD_DatatypeCode">
                    <xsl:with-param name="source" select="@codeListValue"/>
                </xsl:call-template>
            </xsl:for-each>
        </extEleDataType>

        <!-- extEleMxOc -->
        <extEleMxOc>
            <xsl:value-of select="gmd:maximumOccurrence/gco:CharacterString"/>
        </extEleMxOc>

        <!-- extEleDomVal -->
        <extEleDomVal>
            <xsl:value-of select="gmd:domainValue/gco:CharacterString"/>
        </extEleDomVal>

        <!-- extEleParEnt -->
        <xsl:for-each select="gmd:parentEntity">
            <extEleParEnt>
                <xsl:value-of select="gco:CharacterString"/>
            </extEleParEnt>
        </xsl:for-each>

        <!-- extEleRule -->
        <extEleRule>
            <xsl:value-of select="gmd:rule/gco:CharacterString"/>
        </extEleRule>

        <!-- extEleRat -->
        <xsl:for-each select="gmd:rationale">
            <extEleRat>
                <xsl:value-of select="gco:CharacterString"/>
            </extEleRat>
        </xsl:for-each>
		
        <!-- extEleSrc -->
        <xsl:for-each select="gmd:source/gmd:CI_ResponsibleParty">
            <extEleSrc>
                <xsl:call-template name="CI_ResponsibleParty"/>
            </extEleSrc>
        </xsl:for-each>
    </xsl:template>
		
    <!-- B.2.12 Application schema information -->
    <!-- MD_ApplicationSchemaInformation -->
    <xsl:template name="MD_ApplicationSchemaInformation">
        <!-- asName -->
        <xsl:for-each select="gmd:name/gmd:CI_Citation">
            <asName>
                <xsl:call-template name="CI_Citation" />
            </asName>
        </xsl:for-each>
		
        <!-- asSchLang -->
        <asSchLang>
            <xsl:value-of select="gmd:schemaLanguage/gco:CharacterString"/>
        </asSchLang>
		
        <!-- asCstLang -->
        <asCstLang>
            <xsl:value-of select="gmd:constraintLanguage/gco:CharacterString"/>
        </asCstLang>
		
        <!-- asAscii -->
        <asAscii>
            <xsl:value-of select="gmd:schemaAscii/gco:CharacterString"/>
        </asAscii>

        <!-- asGraFile -->
        <asGraFile>
            <xsl:value-of select="gmd:graphicsFile/gco:Binary/@src"/>
        </asGraFile>

        <!-- asSwDevFile -->
        <asSwDevFile>
            <xsl:value-of select="gmd:softwareDevelopmentFile/gco:Binary/@src"/>
        </asSwDevFile>

        <!-- asSwDevFiFt -->
        <asSwDevFiFt>
            <xsl:value-of select="gmd:softwareDevelopmentFileFormat/gco:CharacterString"/>
        </asSwDevFiFt>
    </xsl:template>
	
    <!-- B.3 Data type information -->
	
    <!-- B.3.1 Extent information -->
	
    <!-- B.3.1.1 General extent information -->
    <!-- EX_Extent -->
    <xsl:template name="EX_Extent">
        <exDesc>
            <xsl:value-of select="gmd:EX_Extent/gco:CharacterString"/>
        </exDesc>

        <xsl:for-each select="gmd:geographicElement/gmd:EX_GeographicExtent">
            <geoEle>
                <xsl:call-template name="EX_GeographicExtent" />
            </geoEle>
        </xsl:for-each>		

        <xsl:for-each select="gmd:temporalElement/gmd:EX_TemporalExtent">
            <tempEle>
                <xsl:call-template name="EX_TemporalExtent" />
            </tempEle>
        </xsl:for-each>		

        <xsl:for-each select="gmd:verticalElement/gmd:EX_VerticalExtent">
            <vertEle>
                <xsl:call-template name="EX_VerticalExtent" />
            </vertEle>
        </xsl:for-each>		
    </xsl:template>
	
    <!-- B.3.1.2 Geographic extent information -->
    <!-- EX_GeographicExtent -->
    <xsl:template name="EX_GeographicExtent">
        <xsl:for-each select="gmd:EX_GeographicBoundingBox">
            <GeoBndBox>
                <xsl:call-template name="EX_GeographicBoundingBox" />
            </GeoBndBox>
        </xsl:for-each>
        <xsl:for-each select="gmd:EX_BoundingPolygon">
            <BoundPoly>
                <xsl:call-template name="EX_BoundingPolygon" />
            </BoundPoly>
        </xsl:for-each>
        <xsl:for-each select="gmd:EX_GeographicDescription">
            <GeoDesc>
                <xsl:call-template name="EX_GeographicDescription" />
            </GeoDesc>
        </xsl:for-each>
    </xsl:template>
	
    <!-- EX_BoundingPolygon -->
    <xsl:template name="EX_BoundingPolygon">
        <exTypeCode>
            <xsl:choose>
                <xsl:when test="gmd:extentTypeCode/gco:Boolean = 'true'">1</xsl:when>
                <xsl:otherwise>0</xsl:otherwise>
            </xsl:choose>
        </exTypeCode>
		
        <!-- TODO -->
    </xsl:template>

    <!-- EX_GeographicBoundingBox -->
    <xsl:template name="EX_GeographicBoundingBox">
        <exTypeCode>
            <xsl:choose>
                <xsl:when test="gmd:extentTypeCode/gco:Boolean = 'true'">1</xsl:when>
                <xsl:otherwise>0</xsl:otherwise>
            </xsl:choose>
        </exTypeCode>
        <westBL>
            <xsl:value-of select="gmd:westBoundLongitude/gco:Decimal" />
        </westBL>
        <eastBL>
            <xsl:value-of select="gmd:eastBoundLongitude/gco:Decimal" />
        </eastBL>
        <northBL>
            <xsl:value-of select="gmd:northBoundLongitude/gco:Decimal" />
        </northBL>
        <southBL>
            <xsl:value-of select="gmd:southBoundLongitude/gco:Decimal" />
        </southBL>
    </xsl:template>
	
    <!-- EX_GeographicDescription -->
    <xsl:template name="EX_GeographicDescription">
        <exTypeCode>
            <xsl:choose>
                <xsl:when test="gmd:extentTypeCode/gco:Boolean = 'true'">1</xsl:when>
                <xsl:otherwise>0</xsl:otherwise>
            </xsl:choose>
        </exTypeCode>
        <xsl:for-each select="gmd:geographicIdentifier">
            <geoId>
                <xsl:call-template name="MD_Identifier" />
            </geoId>
        </xsl:for-each>
    </xsl:template>
	
    <!-- B.3.1.3 Temporal extent information -->
    <!-- EX_TemporalExtent -->
    <xsl:template name="EX_TemporalExtent">
        <xsl:for-each select="gmd:extent/gmd:TM_Primitive">
            <exTemp>
                <xsl:call-template name="TM_Primitive" />
            </exTemp>
        </xsl:for-each>
    </xsl:template>
	
    <!-- EX_SpatialTemporalExtent -->
    <xsl:template name="EX_SpatialTemporalExtent">
        <xsl:for-each select="gmd:extent/gmd:TM_Primitive">
            <exTemp>
                <xsl:call-template name="TM_Primitive" />
            </exTemp>
        </xsl:for-each>

        <xsl:for-each select="gmd:extent/gmd:geographicElement">
            <geoEle>
                <xsl:call-template name="EX_GeographicExtent" />
            </geoEle>
        </xsl:for-each>
    </xsl:template>	

    <!-- B.3.1.4 Vertical extent information -->
    <!-- EX_VerticalExtent -->
    <xsl:template name="EX_VerticalExtent">
        <vertEle>
            <vertMinVal>
                <xsl:value-of select="gmd:minimumValue" />
            </vertMinVal>
            <vertMaxVal>
                <xsl:value-of select="gmd:maximumValue" />
            </vertMaxVal>
            <vertCRS>
                <xsl:value-of select="gmd:verticalCRS" />
            </vertCRS>
        </vertEle>
    </xsl:template>
	
    <!-- B.3.2 Citation and responsible party information -->
    <!-- B.3.2.1 General citation information -->
    <!-- CI_Citation -->
    <xsl:template name="CI_Citation">
        <resTitle>
            <xsl:value-of select="gmd:title/gco:CharacterString"/>
        </resTitle>
        <date>
            <xsl:for-each select="gmd:date/gmd:CI_Date[gmd:dateType/gmd:CI_DateTypeCode/@codeListValue='publication']">
                <pubDate>
                    <xsl:call-template name="CI_Date"/>
                </pubDate>
            </xsl:for-each>
            <xsl:for-each select="gmd:date/gmd:CI_Date[gmd:dateType/gmd:CI_DateTypeCode/@codeListValue='creation']">
                <createDate>
                    <xsl:call-template name="CI_Date"/>
                </createDate>
            </xsl:for-each>
            <xsl:for-each select="gmd:date/gmd:CI_Date[gmd:dateType/gmd:CI_DateTypeCode/@codeListValue='revision']">
                <reviseDate>
                    <xsl:call-template name="CI_Date"/>
                </reviseDate>
            </xsl:for-each>
            <xsl:for-each select="gmd:date/gmd:CI_Date[gmd:dateType/gmd:CI_DateTypeCode/@codeListValue='notAvailable']">
                <notavailDate>
                    <xsl:call-template name="CI_Date"/>
                </notavailDate>
            </xsl:for-each>
            <xsl:for-each select="gmd:date/gmd:CI_Date[gmd:dateType/gmd:CI_DateTypeCode/@codeListValue='adopted']">
                <adoptDate>
                    <xsl:call-template name="CI_Date"/>
                </adoptDate>
            </xsl:for-each>
            <xsl:for-each select="gmd:date/gmd:CI_Date[gmd:dateType/gmd:CI_DateTypeCode/@codeListValue='inForce']">
                <inforceDate>
                    <xsl:call-template name="CI_Date"/>
                </inforceDate>
            </xsl:for-each>
            <xsl:for-each select="gmd:date/gmd:CI_Date[gmd:dateType/gmd:CI_DateTypeCode/@codeListValue='deprecated']">
                <deprecDate>
                    <xsl:call-template name="CI_Date"/>
                </deprecDate>
            </xsl:for-each>
            <xsl:for-each select="gmd:date/gmd:CI_Date[gmd:dateType/gmd:CI_DateTypeCode/@codeListValue='superseded']">
                <supersDate>
                    <xsl:call-template name="CI_Date"/>
                </supersDate>
            </xsl:for-each>
        </date>
        <resEd>
            <xsl:value-of select="gmd:edition/gco:CharacterString"/>
        </resEd>

        <xsl:for-each select="gmd:series/gmd:CI_Series">
            <datasetSeries>
                <xsl:call-template name="CI_Series" />
            </datasetSeries>
        </xsl:for-each>
		
        <otherCitDet>
            <xsl:value-of select="gmd:otherCitationDetails/gco:CharacterString" />
        </otherCitDet>
		
        <xsl:for-each select="gmd:presentationForm/gmd:CI_PresentationFormCode">
            <presForm>
                <xsl:call-template name="CI_PresentationFormCode">
                    <xsl:with-param name="source" select="@codeListValue"/>
                </xsl:call-template>
            </presForm>
        </xsl:for-each>

        <collTitle>
            <xsl:value-of select="gmd:collectiveTitle/gco:CharacterString" />
        </collTitle>

        <resAltTitle>
            <xsl:value-of select="gmd:alternateTitle/gco:CharacterString"/>
        </resAltTitle>

        <xsl:for-each select="gmd:editionDate/gmd:CI_Date">
            <resEdDate>
                <xsl:call-template name="CI_Date"/>
            </resEdDate>
        </xsl:for-each>

        <xsl:for-each select="gmd:identifier/gmd:MD_Identifier">
            <citId>
                <xsl:call-template name="MD_Identifier"/>
            </citId>
        </xsl:for-each>
		
        <isbn>
            <xsl:value-of select="gmd:ISBN/gco:CharacterString"/>
        </isbn>
        <issn>
            <xsl:value-of select="gmd:ISSN/gco:CharacterString"/>
        </issn>

        <xsl:for-each select="gmd:citedResponsibleParty/gmd:CI_ResponsibleParty">
            <citRespParty>
                <xsl:call-template name="CI_ResponsibleParty"/>
            </citRespParty>
        </xsl:for-each>
    </xsl:template>
	
    <!-- CI_ResponsibleParty -->
    <xsl:template name="CI_ResponsibleParty">
        <xsl:param name="role"/>

        <rpIndName>
            <xsl:value-of select="gmd:individualName/gco:CharacterString"/>
        </rpIndName>
        <rpOrgName>
            <xsl:value-of select="gmd:organisationName/gco:CharacterString"/>
        </rpOrgName>
        <rpPosName>
            <xsl:value-of select="gmd:positionName/gco:CharacterString"/>
        </rpPosName>
        <xsl:for-each select="gmd:contactInfo/gmd:CI_Contact">
            <rpCntInfo>
                <xsl:call-template name="CI_Contact" />
            </rpCntInfo>
        </xsl:for-each>
        <role>
            <RoleCd>
                <xsl:attribute name="value">
                    <xsl:call-template name="CI_RoleCode">
                        <xsl:with-param name="source" select="$role"/>
                    </xsl:call-template>
                </xsl:attribute>
            </RoleCd>
        </role>
    </xsl:template>
	
    <!-- B.3.2.2 Address information -->
    <!-- CI_Address -->
    <xsl:template name="CI_Address">
        <cntAddress addressType="">
            <xsl:for-each select="gmd:deliveryPoint">
                <delPoint>
                    <xsl:value-of select="gco:CharacterString"/>
                </delPoint>
            </xsl:for-each>
			
            <city>
                <xsl:value-of select="gmd:city/gco:CharacterString"/>
            </city>
            <adminArea>
                <xsl:value-of select="gmd:administrativeArea/gco:CharacterString"/>
            </adminArea>
            <postCode>
                <xsl:value-of select="gmd:postalCode/gco:CharacterString"/>
            </postCode>
            <country>
                <xsl:value-of select="gmd:country/gco:CharacterString"/>
            </country>

            <xsl:for-each select="gmd:electronicMailAddress">
                <eMailAdd>
                    <xsl:value-of select="gco:CharacterString"/>
                </eMailAdd>
            </xsl:for-each>
        </cntAddress>
    </xsl:template>	
		
    <!-- B.3.2.3 Contact information -->
    <!-- CI_Contact -->
    <xsl:template name="CI_Contact">
        <xsl:for-each select="gmd:address/gmd:CI_Address">
            <xsl:call-template name="CI_Address" />
        </xsl:for-each>

        <xsl:for-each select="gmd:phone/gmd:CI_Telephone">
            <xsl:call-template name="CI_Telephone" />
        </xsl:for-each>
		
        <xsl:for-each select="gmd:onlineResource/gmd:CI_OnlineResource">
            <xsl:call-template name="CI_OnlineResource" />
        </xsl:for-each>
		
        <cntHours>
            <xsl:value-of select="gmd:hoursOfService/gco:CharacterString"/>
        </cntHours>
        <cntInstr>
            <xsl:value-of select="gmd:contactInstructions/gco:CharacterString"/>
        </cntInstr>
    </xsl:template>

    <!-- B.3.2.4 Date information -->
    <!-- CI_Date -->
    <xsl:template name="CI_Date">
        <xsl:value-of select="gmd:date/gco:Date | gmd:date/gco:DateTime"/>
        <!--
                        <xsl:for-each select="gmd:dateType/gmd:CI_DateTypeCode">
                                <refDateType>		
                                        <xsl:call-template name="CI_DateTypeCode">
                                                <xsl:with-param name="source" select="@codeListValue"/>
                                        </xsl:call-template>
                                </refDateType>
                        </xsl:for-each>
        -->
    </xsl:template>
	
	
    <!-- B.3.2.5 OnLine resource information -->
    <!-- CI_OnlineResource -->
    <xsl:template name="CI_OnlineResource">
        <linkage>
            <xsl:value-of select="gmd:linkage/gmd:URL"/>
        </linkage>
        <protocol>
            <xsl:value-of select="gmd:protocol/gco:CharacterString"/>
        </protocol>
        <appProfile>
            <xsl:value-of select="gmd:applicationProfile/gco:CharacterString"/>
        </appProfile>
        <orName>
            <xsl:value-of select="gmd:name/gco:CharacterString"/>
        </orName>
        <orDesc>
            <xsl:value-of select="gmd:description/gco:CharacterString"/>
        </orDesc>
        <orFunct>
            <OnFunctCd>
                <xsl:attribute name="value">
                    <xsl:call-template name="CI_OnLineFunctionCode">
                        <xsl:with-param name="source" select="gmd:function/gmd:CI_OnLineFunctionCode/@codeListValue"/>
                    </xsl:call-template>
                </xsl:attribute>
            </OnFunctCd>
        </orFunct>
    </xsl:template>
	
	
    <!-- B.3.2.6 Series information -->
    <!-- CI_Series -->
    <xsl:template name="CI_Series">
        <seriesName>
            <xsl:value-of select="gmd:name/gco:CharacterString"/>
        </seriesName>
        <issId>
            <xsl:value-of select="gmd:issueIdentification/gco:CharacterString"/>
        </issId>
        <page>
            <xsl:value-of select="gmd:page/gco:CharacterString"/>
        </page>
    </xsl:template>
	
	
    <!-- B.3.2.7 Telephone information -->
    <!-- CI_Telephone -->
    <xsl:template name="CI_Telephone">
        <cntPhone>
            <voiceNum tddtty="">
                <xsl:value-of select="gmd:voice/gco:CharacterString"/>
            </voiceNum>
            <faxNum>
                <xsl:value-of select="gmd:facsimile/gco:CharacterString"/>
            </faxNum>
        </cntPhone>
    </xsl:template>

    <!-- B.4.3 Measure -->
    <xsl:template name="Measure">
        <value uom="m">
            <xsl:attribute name="uom">
                <xsl:value-of select="@oum" />
            </xsl:attribute>
            <xsl:value-of select="." />
        </value>		
    </xsl:template>

    <!-- B.4.3 UomLength -->
    <xsl:template name="UnitOfMeasure">
        <UOM>
            <xsl:attribute name="gmlID">
                <xsl:value-of select="@gml:id"/>
            </xsl:attribute>
            <xsl:attribute name="type"></xsl:attribute>
            <unitSymbol>
                <xsl:value-of select="gml:catalogSymbol" />
            </unitSymbol>
        </UOM>
    </xsl:template>

    <!-- B.4.5 TM_Primitive -->
    <xsl:template name="TM_Primitive">
        <xsl:for-each select="gml:TimePeriod">
            <TM_Period xmlns="">
                <tmBegin>
                    <xsl:value-of select="gml:beginPosition"/>
                </tmBegin>
                <tmEnd>
                    <xsl:value-of select="gml:endPosition"/>
                </tmEnd>
            </TM_Period>
        </xsl:for-each>

        <xsl:for-each select="gml:TimeInstant">
            <TM_Period xmlns="">
                <tmPosition>
                    <xsl:value-of select="gml:timePosition"/>
                </tmPosition>
            </TM_Period>
        </xsl:for-each>
    </xsl:template>
	
	
    <!-- ISO 19119 below -->

    <!-- SV_ServiceSpecitication -->
    <!-- SV_Interface -->
    <!-- SV_Operation -->
    <!-- SV_ServiceIdentification -->
    <!-- SV_OperationMetadata -->
    <!-- SV_OperationChainMetadata -->
    <!-- SV_Parameter -->
    <!-- SV_CoupledResource -->
	
	
	
    <!-- DOMAIN Conversions below -->

    <!-- B.5.2 CI_DateTypeCode -->
    <xsl:template name="CI_DateTypeCode">
        <xsl:param name="source" />
        <xsl:choose>
            <xsl:when test="$source = 'creation'">
                <xsl:text>001</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'publication'">
                <xsl:text>002</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'revision'">
                <xsl:text>003</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'notAvailable'">
                <xsl:text>004</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'inForce'">
                <xsl:text>005</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'adopted'">
                <xsl:text>006</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'deprecated'">
                <xsl:text>007</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'superseded'">
                <xsl:text>008</xsl:text>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="$source"/>
            </xsl:otherwise>
        </xsl:choose>		
    </xsl:template>
	
    <!-- B.5.3 CI_OnLineFunctionCode -->
    <xsl:template name="CI_OnLineFunctionCode">
        <xsl:param name="source" />
        <xsl:choose>
            <xsl:when test="$source = 'download'">
                <xsl:text>001</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'information'">
                <xsl:text>002</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'offlineAccess'">
                <xsl:text>003</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'order'">
                <xsl:text>004</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'search'">
                <xsl:text>005</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'upload'">
                <xsl:text>006</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'webService'">
                <xsl:text>007</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'emailService'">
                <xsl:text>008</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'browsing'">
                <xsl:text>009</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'fileAccess'">
                <xsl:text>010</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'webMapService'">
                <xsl:text>011</xsl:text>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="$source"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
	
    <!-- B.5.4 CI_PresentationFormCode -->
    <xsl:template name="CI_PresentationFormCode">
        <xsl:param name="source" />
        <xsl:choose>
            <xsl:when test="$source = 'documentDigital'">
                <xsl:text>001</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'documentHardcopy'">
                <xsl:text>002</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'imageDigital'">
                <xsl:text>003</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'imageHardcopy'">
                <xsl:text>004</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'mapDigital'">
                <xsl:text>005</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'mapHardcopy'">
                <xsl:text>006</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'modelDigital'">
                <xsl:text>007</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'modelHardcopy'">
                <xsl:text>008</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'profileDigital'">
                <xsl:text>009</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'profileHardcopy'">
                <xsl:text>010</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'tableDigital'">
                <xsl:text>011</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'tableHardcopy'">
                <xsl:text>012</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'videoDigital'">
                <xsl:text>013</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'videoHardcopy'">
                <xsl:text>014</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'audioDigital'">
                <xsl:text>015</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'audioHardcopy'">
                <xsl:text>016</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'multimediaDigital'">
                <xsl:text>017</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'multimediaHardcopy'">
                <xsl:text>018</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'diagramDigital'">
                <xsl:text>019</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'diagramHardcopy'">
                <xsl:text>020</xsl:text>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="$source"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <!-- B.5.5 CI_RoleCode -->
    <xsl:template name="CI_RoleCode">
        <xsl:param name="source" />
        <xsl:choose>
            <xsl:when test="$source = 'resourceProvider'">
                <xsl:text>001</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'custodian'">
                <xsl:text>002</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'owner'">
                <xsl:text>003</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'user'">
                <xsl:text>004</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'distributor'">
                <xsl:text>005</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'originator'">
                <xsl:text>006</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'pointOfContact'">
                <xsl:text>007</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'principalInvestigator'">
                <xsl:text>008</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'processor'">
                <xsl:text>009</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'publisher'">
                <xsl:text>010</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'author'">
                <xsl:text>011</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'collaborator'">
                <xsl:text>012</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'editor'">
                <xsl:text>013</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'mediator'">
                <xsl:text>014</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'rightsHolder'">
                <xsl:text>015</xsl:text>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="$source"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <!-- B.5.6 DQ_EvaluationMethodTypeCode -->
    <xsl:template name="DQ_EvaluationMethodTypeCode">
        <xsl:param name="source" />
        <xsl:choose>
            <xsl:when test="$source = 'directInternal'">
                <xsl:text>001</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'directExternal'">
                <xsl:text>002</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'indirect'">
                <xsl:text>003</xsl:text>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="$source"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <!-- B.5.7 DS_AssociationTypeCode -->
    <xsl:template name="DS_AssociationTypeCode">
        <xsl:param name="source" />
        <xsl:choose>
            <xsl:when test="$source = 'crossReference'">
                <xsl:text>001</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'largerWorkCitation'">
                <xsl:text>002</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'partOfSeamlessDatabase'">
                <xsl:text>003</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'source'">
                <xsl:text>004</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'stereoMate'">
                <xsl:text>005</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'isComposedOf'">
                <xsl:text>006</xsl:text>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="$source"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <!-- B.5.8 DS_InitiativeTypeCode -->
    <xsl:template name="DS_InitiativeTypeCode">
        <xsl:param name="source" />
        <xsl:choose>
            <xsl:when test="$source = 'campaign'">
                <xsl:text>001</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'collection'">
                <xsl:text>002</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'exercise'">
                <xsl:text>003</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'experiment'">
                <xsl:text>004</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'investigation'">
                <xsl:text>005</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'mission'">
                <xsl:text>006</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'sensor'">
                <xsl:text>007</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'operation'">
                <xsl:text>008</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'platform'">
                <xsl:text>009</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'process'">
                <xsl:text>010</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'program'">
                <xsl:text>011</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'project'">
                <xsl:text>012</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'study'">
                <xsl:text>013</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'task'">
                <xsl:text>014</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'trial'">
                <xsl:text>015</xsl:text>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="$source"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <!-- B.5.9 MD_CellGeometryCode -->
    <xsl:template name="MD_CellGeometryCode">
        <xsl:param name="source" />
        <xsl:choose>
            <xsl:when test="$source = 'point'">
                <xsl:text>001</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'area'">
                <xsl:text>002</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'voxel'">
                <xsl:text>003</xsl:text>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="$source"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <!-- B.5.10 MD_CharacterSetCode -->
    <xsl:template name="MD_CharacterSetCode">
        <xsl:param name="source" />
        <xsl:choose>
            <xsl:when test="$source = 'ucs2'">
                <xsl:text>001</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'ucs4'">
                <xsl:text>002</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'utf7'">
                <xsl:text>003</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'utf8'">
                <xsl:text>004</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'utf16'">
                <xsl:text>005</xsl:text>
            </xsl:when>
            <xsl:when test="$source = '8859part1'">
                <xsl:text>006</xsl:text>
            </xsl:when>
            <xsl:when test="$source = '8859part2'">
                <xsl:text>007</xsl:text>
            </xsl:when>
            <xsl:when test="$source = '8859part3'">
                <xsl:text>008</xsl:text>
            </xsl:when>
            <xsl:when test="$source = '8859part4'">
                <xsl:text>009</xsl:text>
            </xsl:when>
            <xsl:when test="$source = '8859part5'">
                <xsl:text>010</xsl:text>
            </xsl:when>
            <xsl:when test="$source = '8859part6'">
                <xsl:text>011</xsl:text>
            </xsl:when>
            <xsl:when test="$source = '8859part7'">
                <xsl:text>012</xsl:text>
            </xsl:when>
            <xsl:when test="$source = '8859part8'">
                <xsl:text>013</xsl:text>
            </xsl:when>
            <xsl:when test="$source = '8859part9'">
                <xsl:text>014</xsl:text>
            </xsl:when>
            <xsl:when test="$source = '8859part10'">
                <xsl:text>015</xsl:text>
            </xsl:when>
            <xsl:when test="$source = '8859part11'">
                <xsl:text>016</xsl:text>
            </xsl:when>
            <xsl:when test="$source = '8859part13'">
                <xsl:text>018</xsl:text>
            </xsl:when>
            <xsl:when test="$source = '8859part14'">
                <xsl:text>019</xsl:text>
            </xsl:when>
            <xsl:when test="$source = '8859part15'">
                <xsl:text>020</xsl:text>
            </xsl:when>
            <xsl:when test="$source = '8859part16'">
                <xsl:text>021</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'jis'">
                <xsl:text>022</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'shiftJIS'">
                <xsl:text>023</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'eucJP'">
                <xsl:text>024</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'usAscii'">
                <xsl:text>025</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'ebcdic'">
                <xsl:text>026</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'eucKR'">
                <xsl:text>027</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'big5'">
                <xsl:text>028</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'GB2312'">
                <xsl:text>029</xsl:text>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="$source"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
	
    <!-- B.5.11 MD_ClassificationCode -->
    <xsl:template name="MD_ClassificationCode">
        <xsl:param name="source" />
        <xsl:choose>
            <xsl:when test="$source = 'unclassified'">
                <xsl:text>001</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'restricted'">
                <xsl:text>002</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'confidential'">
                <xsl:text>003</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'secret'">
                <xsl:text>004</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'topSecret'">
                <xsl:text>005</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'sensitive'">
                <xsl:text>006</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'forOfficialUseOnly'">
                <xsl:text>007</xsl:text>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="$source"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
	
    <!-- B.5.12 MD_CoverageContentTypeCode -->
    <xsl:template name="MD_CoverageContentTypeCode">
        <xsl:param name="source" />
        <xsl:choose>
            <xsl:when test="$source = 'image'">
                <xsl:text>001</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'thematicClassification'">
                <xsl:text>002</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'physicalMeasurement'">
                <xsl:text>003</xsl:text>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="$source"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
	
    <!-- B.5.13 MD_DatatypeCode -->
    <xsl:template name="MD_DatatypeCode">
        <xsl:param name="source" />
        <xsl:choose>
            <xsl:when test="$source = 'class'">
                <xsl:text>001</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'codelist'">
                <xsl:text>002</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'enumeration'">
                <xsl:text>003</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'codelistElement'">
                <xsl:text>004</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'abstractClass'">
                <xsl:text>005</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'aggregateClass'">
                <xsl:text>006</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'specifiedClass'">
                <xsl:text>007</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'datatypeClass'">
                <xsl:text>008</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'interfaceClass'">
                <xsl:text>009</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'unionClass'">
                <xsl:text>010</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'metaClass'">
                <xsl:text>011</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'typeClass'">
                <xsl:text>012</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'characterString'">
                <xsl:text>013</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'integer'">
                <xsl:text>014</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'association'">
                <xsl:text>015</xsl:text>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="$source"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <!-- B.5.14 MD_DimensionNameTypeCode -->
    <xsl:template name="MD_DimensionNameTypeCode">
        <xsl:param name="source" />
        <xsl:choose>
            <xsl:when test="$source = 'row'">
                <xsl:text>001</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'column'">
                <xsl:text>002</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'vertical'">
                <xsl:text>003</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'track'">
                <xsl:text>004</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'crossTrack'">
                <xsl:text>005</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'line'">
                <xsl:text>006</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'sample'">
                <xsl:text>007</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'time'">
                <xsl:text>008</xsl:text>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="$source"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <!-- B.5.15 MD_GeometricObjectTypeCode -->
    <xsl:template name="MD_GeometricObjectTypeCode">
        <xsl:param name="source" />
        <xsl:choose>
            <xsl:when test="$source = 'complex'">
                <xsl:text>001</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'composite'">
                <xsl:text>002</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'curve'">
                <xsl:text>003</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'point'">
                <xsl:text>004</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'solid'">
                <xsl:text>005</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'surface'">
                <xsl:text>006</xsl:text>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="$source"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <!-- B.5.16 MD_ImagingConditionCode -->
    <xsl:template name="MD_ImagingConditionCode">
        <xsl:param name="source" />
        <xsl:choose>
            <xsl:when test="$source = 'blurredImage'">
                <xsl:text>001</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'cloud'">
                <xsl:text>002</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'degradingobliquity'">
                <xsl:text>003</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'fog'">
                <xsl:text>004</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'heavySmokeOrDust'">
                <xsl:text>005</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'night'">
                <xsl:text>006</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'rain'">
                <xsl:text>007</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'semiDarkness'">
                <xsl:text>008</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'shadow'">
                <xsl:text>009</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'snow'">
                <xsl:text>010</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'terrainMasking'">
                <xsl:text>011</xsl:text>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="$source"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <!-- B.5.17 MD_KeywordTypeCode -->
    <!-- TODO -->

    <!-- B.5.18 MD_MaintenanceFrequencyCode -->
    <xsl:template name="MD_MaintenanceFrequencyCode">
        <xsl:param name="source" />
        <xsl:choose>
            <xsl:when test="$source = 'continual'">
                <xsl:text>001</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'daily'">
                <xsl:text>002</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'weekly'">
                <xsl:text>003</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'fortnightly'">
                <xsl:text>004</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'monthly'">
                <xsl:text>005</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'quarterly'">
                <xsl:text>006</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'biannually'">
                <xsl:text>007</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'annually'">
                <xsl:text>008</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'asNeeded'">
                <xsl:text>009</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'irregular'">
                <xsl:text>010</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'notPlanned'">
                <xsl:text>011</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'unknown'">
                <xsl:text>012</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'semimonthly'">
                <xsl:text>013</xsl:text>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="$source"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <!-- B.5.19 MD_MediumFormatCode -->
    <xsl:template name="MD_MediumFormatCode">
        <xsl:param name="source" />
        <xsl:choose>
            <xsl:when test="$source = 'cpio'">
                <xsl:text>001</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'tar'">
                <xsl:text>002</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'highSierra'">
                <xsl:text>003</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'iso9660'">
                <xsl:text>004</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'iso9660RockRidge'">
                <xsl:text>005</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'iso9660AppleHFS'">
                <xsl:text>006</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'UDF'">
                <xsl:text>007</xsl:text>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="$source"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <!-- B.5.20 MD_MediumNameCode -->
    <xsl:template name="MD_MediumNameCode">
        <xsl:param name="source" />
        <xsl:choose>
            <xsl:when test="$source = 'cdRom'">
                <xsl:text>001</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'dvd'">
                <xsl:text>002</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'dvdRom'">
                <xsl:text>003</xsl:text>
            </xsl:when>
            <xsl:when test="$source = '3halfInchFloppy'">
                <xsl:text>004</xsl:text>
            </xsl:when>
            <xsl:when test="$source = '5quarterInchFloppy'">
                <xsl:text>005</xsl:text>
            </xsl:when>
            <xsl:when test="$source = '7trackTape'">
                <xsl:text>006</xsl:text>
            </xsl:when>
            <xsl:when test="$source = '9trackTape'">
                <xsl:text>007</xsl:text>
            </xsl:when>
            <xsl:when test="$source = '3480Cartridge'">
                <xsl:text>008</xsl:text>
            </xsl:when>
            <xsl:when test="$source = '3490Cartridge'">
                <xsl:text>009</xsl:text>
            </xsl:when>
            <xsl:when test="$source = '3580Cartridge'">
                <xsl:text>010</xsl:text>
            </xsl:when>
            <xsl:when test="$source = '4mmCartridgeTape'">
                <xsl:text>011</xsl:text>
            </xsl:when>
            <xsl:when test="$source = '8mmCartridgeTape'">
                <xsl:text>012</xsl:text>
            </xsl:when>
            <xsl:when test="$source = '1quarterInchCartridgeTape'">
                <xsl:text>013</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'digitalLinearTape'">
                <xsl:text>014</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'onLine'">
                <xsl:text>015</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'satellite'">
                <xsl:text>016</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'telephoneLink'">
                <xsl:text>017</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'hardcopy'">
                <xsl:text>018</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'hardcopyDiazoPolyester08'">
                <xsl:text>019</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'hardcopyCardMicrofilm'">
                <xsl:text>020</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'hardcopyMicrofilm240'">
                <xsl:text>021</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'hardcopyMicrofilm35'">
                <xsl:text>022</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'hardcopyMicrofilm70'">
                <xsl:text>023</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'hardcopyMicrofilmGeneral'">
                <xsl:text>024</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'hardcopyMicrofilmMicrofiche'">
                <xsl:text>025</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'hardcopyNegativePhoto'">
                <xsl:text>026</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'hardcopyPaper'">
                <xsl:text>027</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'hardcopyDiazo'">
                <xsl:text>028</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'hardcopyPhoto'">
                <xsl:text>029</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'hardcopyTracedPaper'">
                <xsl:text>030</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'hardDisk'">
                <xsl:text>031</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'USBFlashDrive'">
                <xsl:text>032</xsl:text>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="$source"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <!-- B.5.21 MD_ObligationCode -->
    <xsl:template name="MD_ObligationCode">
        <xsl:param name="source" />
        <xsl:choose>
            <xsl:when test="$source = 'mandatory'">
                <xsl:text>001</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'optional'">
                <xsl:text>002</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'conditional'">
                <xsl:text>003</xsl:text>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="$source"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <!-- B.5.22 MD_PixelOrientationCode -->
    <xsl:template name="MD_PixelOrientationCode">
        <xsl:param name="source" />
        <xsl:choose>
            <xsl:when test="$source = 'center'">
                <xsl:text>001</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'lowerLeft'">
                <xsl:text>002</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'lowerRight'">
                <xsl:text>003</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'upperRight'">
                <xsl:text>004</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'upperLeft'">
                <xsl:text>005</xsl:text>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="$source"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <!-- B.5.23 MD_ProgressCode -->
    <xsl:template name="MD_ProgressCode">
        <xsl:param name="source" />
        <xsl:choose>
            <xsl:when test="$source = 'completed'">
                <xsl:text>001</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'historicalArchive'">
                <xsl:text>002</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'obsolete'">
                <xsl:text>003</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'onGoing'">
                <xsl:text>004</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'planned'">
                <xsl:text>005</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'required'">
                <xsl:text>006</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'underDevelopment'">
                <xsl:text>007</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'proposed'">
                <xsl:text>008</xsl:text>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="$source"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <!-- B.5.24 MD_RestrictionCode -->
    <xsl:template name="MD_RestrictionCode">
        <xsl:param name="source" />
        <xsl:choose>
            <xsl:when test="$source = 'copyright'">
                <xsl:text>001</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'patent'">
                <xsl:text>002</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'patentPending'">
                <xsl:text>003</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'trademark'">
                <xsl:text>004</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'license'">
                <xsl:text>005</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'intellectualPropertyRights'">
                <xsl:text>006</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'restricted'">
                <xsl:text>007</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'otherRestrictions'">
                <xsl:text>008</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'licenseUnrestricted'">
                <xsl:text>009</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'licenseEndUser'">
                <xsl:text>010</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'licenseDistributor'">
                <xsl:text>011</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'privacy'">
                <xsl:text>012</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'statutory'">
                <xsl:text>013</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'confidential'">
                <xsl:text>014</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'sensitivity'">
                <xsl:text>015</xsl:text>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="$source"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <!-- B.5.25 MD_ScopeCode -->
    <xsl:template name="MD_ScopeCode">
        <xsl:param name="source" />
        <xsl:choose>
            <xsl:when test="$source = 'attribute'">
                <xsl:text>001</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'attributeType'">
                <xsl:text>002</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'collectionHardware'">
                <xsl:text>003</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'collectionSession'">
                <xsl:text>004</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'dataset'">
                <xsl:text>005</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'series'">
                <xsl:text>006</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'nonGeographicDataset'">
                <xsl:text>007</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'dimensionGroup'">
                <xsl:text>008</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'feature'">
                <xsl:text>009</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'featureType'">
                <xsl:text>010</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'propertyType'">
                <xsl:text>011</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'fieldSession'">
                <xsl:text>012</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'software'">
                <xsl:text>013</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'service'">
                <xsl:text>014</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'model'">
                <xsl:text>015</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'tile'">
                <xsl:text>016</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'initiative'">
                <xsl:text>017</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'stereomate'">
                <xsl:text>018</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'sensor'">
                <xsl:text>019</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'platformSeries'">
                <xsl:text>020</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'sensorSeries'">
                <xsl:text>021</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'productionSeries'">
                <xsl:text>022</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'transferAggregate'">
                <xsl:text>023</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'otherAggregate'">
                <xsl:text>024</xsl:text>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="$source"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <!-- B.5.26 MD_SpatialRepresentationTypeCode -->
    <xsl:template name="MD_SpatialRepresentationTypeCode">
        <xsl:param name="source" />
        <xsl:choose>
            <xsl:when test="$source = 'vector'">
                <xsl:text>001</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'grid'">
                <xsl:text>002</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'textTable'">
                <xsl:text>003</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'tin'">
                <xsl:text>004</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'stereoModel'">
                <xsl:text>005</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'video'">
                <xsl:text>006</xsl:text>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="$source"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <!-- B.5.27 MD_TopicCategoryCode -->
    <xsl:template name="MD_TopicCategoryCode">
        <xsl:param name="source" />
        <xsl:choose>
            <xsl:when test="$source = 'farming'">
                <xsl:text>001</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'biota'">
                <xsl:text>002</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'boundaries'">
                <xsl:text>003</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'climatologyMeteorologyAtmosphere'">
                <xsl:text>004</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'economy'">
                <xsl:text>005</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'elevation'">
                <xsl:text>006</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'environment'">
                <xsl:text>007</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'geoscientificInformation'">
                <xsl:text>008</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'health'">
                <xsl:text>009</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'imageryBaseMapsEarthCover'">
                <xsl:text>010</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'intelligenceMilitary'">
                <xsl:text>011</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'inlandWaters'">
                <xsl:text>012</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'location'">
                <xsl:text>013</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'oceans'">
                <xsl:text>014</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'planningCadastre'">
                <xsl:text>015</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'society'">
                <xsl:text>016</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'structure'">
                <xsl:text>017</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'transportation'">
                <xsl:text>018</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'utilitiesCommunication'">
                <xsl:text>019</xsl:text>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="$source"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <!-- B.5.28 MD_TopologyLevelCode -->
    <xsl:template name="MD_TopologyLevelCode">
        <xsl:param name="source" />
        <xsl:choose>
            <xsl:when test="$source = 'geometryOnly'">
                <xsl:text>001</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'topology1D'">
                <xsl:text>002</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'planarGraph'">
                <xsl:text>003</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'fullPlanarGraph'">
                <xsl:text>004</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'surfaceGraph'">
                <xsl:text>005</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'fullSurfaceGraph'">
                <xsl:text>006</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'topology3D'">
                <xsl:text>007</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'fullTopology3D'">
                <xsl:text>008</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'abstract'">
                <xsl:text>009</xsl:text>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="$source"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template name="couplTypCode">
        <xsl:param name="source" />
        <xsl:choose>
            <xsl:when test="$source = 'loose'">
                <xsl:text>001</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'mixed'">
                <xsl:text>002</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'tight'">
                <xsl:text>003</xsl:text>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="$source"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    <xsl:template name="dcpCode">
        <xsl:param name="source" />
        <xsl:choose>
            <xsl:when test="$source = 'XML'">
                <xsl:text>001</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'CORBA'">
                <xsl:text>002</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'JAVA'">
                <xsl:text>003</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'COM'">
                <xsl:text>004</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'SQL'">
                <xsl:text>005</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'WebServices'">
                <xsl:text>006</xsl:text>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="$source"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    <xsl:template name="paramDirCode">
        <xsl:param name="source" />
        <xsl:choose>
            <xsl:when test="$source = 'in'">
                <xsl:text>001</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'out'">
                <xsl:text>002</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'in/out'">
                <xsl:text>003</xsl:text>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="$source"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    <xsl:template name="fileFormatCode">
        <xsl:param name="source" />
        <xsl:choose>
            <xsl:when test="$source = 'bil'">
                <xsl:text>001</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'bmp'">
                <xsl:text>002</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'bsq'">
                <xsl:text>003</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'bzip2'">
                <xsl:text>004</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'cdr'">
                <xsl:text>005</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'cgm'">
                <xsl:text>006</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'cover'">
                <xsl:text>007</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'csv'">
                <xsl:text>008</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'dbf'">
                <xsl:text>009</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'dgn'">
                <xsl:text>010</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'doc'">
                <xsl:text>011</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'dwg'">
                <xsl:text>012</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'dxf'">
                <xsl:text>013</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'e00'">
                <xsl:text>014</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'ecw'">
                <xsl:text>015</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'eps'">
                <xsl:text>016</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'ers'">
                <xsl:text>017</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'gdb'">
                <xsl:text>018</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'geotiff'">
                <xsl:text>019</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'gif'">
                <xsl:text>020</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'gml'">
                <xsl:text>021</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'grid'">
                <xsl:text>022</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'gzip'">
                <xsl:text>023</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'html'">
                <xsl:text>024</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'jpg'">
                <xsl:text>025</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'mdb'">
                <xsl:text>026</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'mif'">
                <xsl:text>027</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'pbm'">
                <xsl:text>028</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'pdf'">
                <xsl:text>029</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'png'">
                <xsl:text>030</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'ps'">
                <xsl:text>031</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'rtf'">
                <xsl:text>032</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'sdc'">
                <xsl:text>033</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'shp'">
                <xsl:text>034</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'sid'">
                <xsl:text>035</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'svg'">
                <xsl:text>036</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'tab'">
                <xsl:text>037</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'tar'">
                <xsl:text>038</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'tiff'">
                <xsl:text>039</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'txt'">
                <xsl:text>040</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'xhtml'">
                <xsl:text>041</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'xls'">
                <xsl:text>042</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'xml'">
                <xsl:text>043</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'xwd'">
                <xsl:text>044</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'zip'">
                <xsl:text>045</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'wpd'">
                <xsl:text>046</xsl:text>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="$source"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    <xsl:template name="lang639_code">
        <xsl:param name="source" />
        <xsl:choose>
            <xsl:when test="$source = ''">
                <xsl:text>aa</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>ab</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>ace</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>ach</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>ada</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>ady</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>afa</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>afh</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>af</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>ain</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>ak</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>akk</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>sq</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>ale</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>alg</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>alt</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>am</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>ang</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>anp</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>apa</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>ar</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>arc</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>an</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>hy</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>arn</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>arp</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>art</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>arw</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>as</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>ast</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>ath</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>aus</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>av</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>ae</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>ay</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>awa</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>az</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>bad</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>bai</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>ba</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>bal</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>bm</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>ban</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>eu</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>bas</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>bat</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>bej</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>be</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>bem</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>bn</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>ber</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>bho</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>bh</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>bik</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>bin</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>bi</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>bla</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>bnt</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>bs</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>bra</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>br</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>btk</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>bua</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>bug</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>bg</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>my</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>byn</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>cad</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>cai</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>car</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>ca</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>cau</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>ceb</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>cel</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>ch</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>chb</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>ce</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>chg</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>zh</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>chk</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>chm</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>chn</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>cho</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>chp</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>chr</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>cu</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>cv</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>chy</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>cmc</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>cop</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>kw</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>co</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>cpe</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>cpf</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>cpp</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>cr</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>crh</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>crp</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>csb</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>cus</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>cs</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>dak</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>da</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>dar</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>day</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>del</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>den</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>dgr</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>din</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>dv</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>doi</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>dra</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>dsb</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>dua</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>dum</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>nl</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>dyu</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>dz</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>efi</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>egy</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>eka</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>elx</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>en</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>enm</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>eo</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>et</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>ee</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>ewo</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>fan</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>fo</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>fat</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>fj</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>fil</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>fi</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>fiu</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>fon</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>fr</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>frm</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>fro</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>frr</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>frs</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>fy</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>ff</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>fur</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>gaa</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>gay</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>gba</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>gem</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>ka</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>de</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>gez</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>gil</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>gd</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>ga</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>gl</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>gv</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>gmh</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>goh</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>gon</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>gor</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>got</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>grb</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>grc</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>el</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>gn</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>gsw</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>gu</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>gwi</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>hai</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>ht</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>ha</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>haw</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>he</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>hz</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>hil</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>him</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>hi</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>hit</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>hmn</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>ho</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>hr</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>hsb</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>hu</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>hup</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>iba</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>ig</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>is</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>io</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>ii</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>ijo</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>iu</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>ie</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>ilo</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>ia</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>inc</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>id</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>ine</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>inh</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>ik</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>ira</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>iro</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>it</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>jv</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>jbo</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>ja</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>jpr</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>jrb</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>kaa</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>kab</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>kac</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>kl</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>kam</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>kn</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>kar</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>ks</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>kr</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>kaw</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>kk</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>kbd</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>kha</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>khi</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>km</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>kho</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>ki</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>rw</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>ky</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>kmb</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>kok</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>kv</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>kg</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>ko</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>kos</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>kpe</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>krc</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>krl</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>kro</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>kru</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>kj</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>kum</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>ku</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>kut</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>lad</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>lah</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>lam</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>lo</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>la</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>lv</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>lez</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>li</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>ln</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>lt</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>lol</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>loz</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>lb</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>lua</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>lu</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>lg</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>lui</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>lun</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>luo</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>lus</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>mk</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>mad</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>mag</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>mh</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>mai</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>mak</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>ml</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>man</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>mi</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>map</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>mr</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>mas</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>ms</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>mdf</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>mdr</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>men</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>mga</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>mic</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>min</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>mis</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>mkh</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>mg</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>mt</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>mnc</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>mni</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>mno</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>moh</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>mn</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>mos</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>mul</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>mun</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>mus</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>mwl</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>mwr</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>myn</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>myv</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>nah</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>nai</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>nap</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>na</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>nv</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>nr</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>nd</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>ng</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>nds</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>ne</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>new</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>nia</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>nic</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>niu</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>nn</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>nb</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>nog</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>non</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>no</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>nqo</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>nso</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>nub</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>nwc</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>ny</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>nym</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>nyn</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>nyo</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>nzi</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>oc</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>oj</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>or</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>om</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>osa</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>os</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>ota</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>oto</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>paa</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>pag</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>pal</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>pam</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>pa</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>pap</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>pau</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>peo</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>fa</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>phi</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>phn</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>pi</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>pl</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>pon</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>pt</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>pra</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>pro</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>ps</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>qu</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>raj</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>rap</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>rar</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>roa</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>rm</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>rom</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>ro</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>rn</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>rup</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>ru</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>sad</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>sg</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>sah</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>sai</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>sal</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>sam</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>sa</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>sas</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>sat</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>scn</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>sco</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>sel</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>sem</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>sga</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>sgn</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>shn</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>sid</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>si</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>sio</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>sit</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>sla</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>sk</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>sl</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>sma</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>se</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>smi</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>smj</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>smn</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>sm</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>sms</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>sn</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>sd</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>snk</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>sog</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>so</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>son</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>st</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>es</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>sc</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>srn</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>sr</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>srr</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>ssa</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>ss</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>suk</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>su</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>sus</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>sux</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>sw</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>sv</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>syc</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>syr</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>ty</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>tai</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>ta</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>tt</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>te</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>tem</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>ter</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>tet</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>tg</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>tl</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>th</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>bo</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>tig</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>ti</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>tiv</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>tkl</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>tlh</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>tli</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>tmh</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>tog</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>to</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>tpi</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>tsi</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>tn</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>ts</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>tk</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>tum</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>tup</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>tr</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>tut</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>tvl</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>tw</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>tyv</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>udm</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>uga</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>ug</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>uk</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>umb</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>und</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>ur</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>uz</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>vai</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>ve</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>vi</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>vo</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>vot</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>wak</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>wal</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>war</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>was</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>cy</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>wen</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>wa</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>wo</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>xal</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>xh</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>yao</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>yap</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>yi</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>yo</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>ypk</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>zap</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>zbl</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>zen</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>za</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>znd</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>zu</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>zun</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>zxx</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>zza</xsl:text>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="$source"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    <xsl:template name="cntry3166_code">
        <xsl:param name="source" />
        <xsl:choose>
            <xsl:when test="$source = ''">
                <xsl:text>af</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>ax</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>al</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>dz</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>as</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>ad</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>ao</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>ai</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>aq</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>ag</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>ar</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>am</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>aw</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>au</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>at</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>az</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>bs</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>bh</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>bd</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>bb</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>by</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>be</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>bz</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>bj</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>bm</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>bt</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>bo</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>bq</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>ba</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>bw</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>bv</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>br</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>io</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>bn</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>bg</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>bf</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>bi</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>kh</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>cm</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>ca</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>cv</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>ky</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>cf</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>td</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>cl</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>cn</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>cx</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>cc</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>co</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>km</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>cg</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>cd</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>ck</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>cr</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>ci</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>hr</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>cu</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>cw</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>cy</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>cz</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>dk</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>dj</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>dm</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>do</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>ec</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>eg</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>sv</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>gq</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>er</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>ee</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>et</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>fk</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>fo</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>fj</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>fi</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>fr</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>gf</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>pf</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>tf</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>ga</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>gm</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>ge</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>de</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>gh</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>gi</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>gr</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>gl</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>gd</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>gp</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>gu</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>gt</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>gg</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>gn</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>gw</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>gy</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>ht</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>hm</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>va</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>hn</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>hk</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>hu</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>is</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>in</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>id</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>ir</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>iq</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>ie</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>im</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>il</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>it</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>jm</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>jp</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>je</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>jo</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>kz</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>ke</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>ki</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>kp</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>kr</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>kw</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>kg</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>la</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>lv</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>lb</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>ls</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>lr</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>ly</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>li</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>lt</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>lu</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>mo</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>mk</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>mg</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>mw</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>my</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>mv</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>ml</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>mt</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>mh</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>mq</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>mr</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>mu</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>yt</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>mx</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>fm</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>md</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>mc</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>mn</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>me</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>ms</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>ma</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>mz</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>mm</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>na</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>nr</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>np</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>nl</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>nc</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>nz</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>ni</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>ne</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>ng</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>nu</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>nf</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>mp</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>no</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>om</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>pk</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>pw</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>ps</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>pa</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>pg</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>py</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>pe</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>ph</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>pn</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>pl</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>pt</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>pr</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>qa</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>re</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>ro</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>ru</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>rw</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>bl</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>sh</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>kn</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>lc</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>mf</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>pm</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>vc</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>ws</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>sm</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>st</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>sa</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>sn</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>rs</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>sc</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>sl</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>sg</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>sx</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>sk</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>si</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>sb</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>so</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>za</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>gs</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>es</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>lk</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>sd</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>sr</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>sj</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>sz</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>se</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>ch</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>sy</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>tw</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>tj</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>tz</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>th</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>tl</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>tg</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>tk</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>to</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>tt</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>tn</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>tr</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>tm</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>tc</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>tv</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>ug</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>ua</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>ae</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>gb</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>us</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>um</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>uy</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>uz</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>vu</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>ve</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>vn</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>vg</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>vi</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>wf</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>eh</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>ye</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>zm</xsl:text>
            </xsl:when>
            <xsl:when test="$source = ''">
                <xsl:text>zw</xsl:text>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="$source"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template name="contentTypeCode">
        <xsl:param name="source" />
        <xsl:choose>
            <xsl:when test="$source = 'Live Data and Maps'">
                <xsl:text>001</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'Downloadable Data'">
                <xsl:text>002</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'Offline Data'">
                <xsl:text>003</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'Static Map Images'">
                <xsl:text>004</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'Other Documents'">
                <xsl:text>005</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'Applications'">
                <xsl:text>006</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'Geographic Services'">
                <xsl:text>007</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'Clearinghouses'">
                <xsl:text>008</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'Map Files'">
                <xsl:text>009</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'Geographic Activities'">
                <xsl:text>010</xsl:text>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="$source"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template name="DQ_ElementType">
        <xsl:param name="source" />
        <xsl:choose>
            <xsl:when test="$source = 'DQ_Completeness'">
                <xsl:text>DQComplete</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'DQ_CompletenessCommission'">
                <xsl:text>DQCompComm</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'DQ_CompletenessOmission'">
                <xsl:text>DQCompOm</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'DQ_ConceptualConsistency'">
                <xsl:text>DQConcConsis</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'DQ_DomainConsistency'">
                <xsl:text>DQDomConsis</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'DQ_FormatConsistency'">
                <xsl:text>DQFormConsis</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'DQ_TopologicalConsistency'">
                <xsl:text>DQTopConsis</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'DQ_PositionalAccuracy'">
                <xsl:text>DQPosAcc</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'DQ_AbsoluteExternalPositionalAccuracy'">
                <xsl:text>DQAbsExtPosAcc</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'DQ_GriddedDataPositionalAccuracy'">
                <xsl:text>DQGridDataPosAcc</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'DQ_RelativeInternalPositionalAccuracy'">
                <xsl:text>DQRelIntPosAcc</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'DQ_TemporalAccuracy'">
                <xsl:text>DQTempAcc</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'DQ_ThematicAccuracy'">
                <xsl:text>DQThemAcc</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'DQ_ThematicClassificationCorrectness'">
                <xsl:text>DQThemClassCor</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'DQ_NonQuantitativeAttributeAccuracy'">
                <xsl:text>DQNonQuanAttAcc</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'DQ_QuantitativeAttributeAccuracy'">
                <xsl:text>DQQuanAttAcc</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'DQ_AccuracyOfATimeMeasurement'">
                <xsl:text>DQAccTimeMeas</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'DQ_TemporalConsistency'">
                <xsl:text>DQTempConsis</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'DQ_TemporalValidity'">
                <xsl:text>DQTempValid</xsl:text>
            </xsl:when>
            <xsl:when test="$source = 'QeUsability'">
                <xsl:text>QeUsability</xsl:text>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="$source"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>	
	
	
    <!-- ISO 19115-2 TEMPLATES START HERE -->
    <!-- B.2.4.3 Coverage and image description - Extensions -->

    <xsl:template name="MI_RangeElementDescription">
        <rgEltName>
            <xsl:value-of select="gmi:name/gco:CharacterString" />
        </rgEltName>
        <rgEltDef>
            <xsl:value-of select="gmi:definition/gco:CharacterString" />
        </rgEltDef>
        <rgElt>
            <xsl:value-of select="gmi:definition/gco:Record" />
        </rgElt>
    </xsl:template>


    <!-- GML TEMPLATES START HERE -->
    <xsl:template name="GM_Point">
        <!-- TODO ? attribute gml:id -->
        <gmlDesc>
            <xsl:value-of select="gml:description" />
        </gmlDesc>
        <gmlDescRef>
            <xsl:value-of select="gml:descriptionReference/@href" />
        </gmlDescRef>
        <gmlIdent>
            <xsl:attribute name="codeSpace">
                <xsl:value-of select="gml:identifier/@codeSpace" />
            </xsl:attribute>
            <xsl:value-of select="gml:identifier" />
        </gmlIdent>
        <pos>
            <xsl:value-of select="gml:pos" />
        </pos>
    </xsl:template>

    <!-- GENERIC TEMPLATES START HERE -->
    <xsl:template name="thePath">
        <xsl:param name="pNode" select="node()"/>
		
        <xsl:text>/</xsl:text>
        <xsl:for-each select="$pNode/ancestor::*">    
            <xsl:value-of select="name()" />
            <xsl:text>/</xsl:text>    
        </xsl:for-each>    
		
        <xsl:if test="count($pNode/child::*)=0">
            <xsl:value-of select="$pNode" />
        </xsl:if>     
    </xsl:template>    
	
</xsl:stylesheet>
