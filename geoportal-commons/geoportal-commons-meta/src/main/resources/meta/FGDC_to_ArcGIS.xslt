<xsl:stylesheet version="1.0" 
  exclude-result-prefixes="gmd gco gmi gml gts srv xlink xsi xsl t esri res msxsl" 
	xmlns:gmd="http://www.isotc211.org/2005/gmd" 
	xmlns:gco="http://www.isotc211.org/2005/gco" 
	xmlns:gmi="http://www.isotc211.org/2005/gmi"
	xmlns:gml="http://www.opengis.net/gml/3.2" 
	xmlns:gts="http://www.isotc211.org/2005/gts" 
	xmlns:srv="http://www.isotc211.org/2005/srv" 
	xmlns:xlink="http://www.w3.org/1999/xlink" 
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
	xmlns:t="http://www.esri.com/xslt/translator" 
	xmlns:esri="http://www.esri.com/metadata/" 
	xmlns:res="http://www.esri.com/metadata/res/">

	<xsl:output method="xml" version="1.0" encoding="UTF-8" indent="yes" omit-xml-declaration="no"/>
	
  <xsl:key name="ctryName" match="country" use="name"/>
  <xsl:key name="ctrya3" match="country" use="@alpha3"/>

  <xsl:key name="langName" match="language" use="name"/>
  <xsl:key name="langa2" match="language" use="@alpha2"/>
  <xsl:key name="langa3" match="language" use="@alpha3"/>
  <xsl:key name="langa3t" match="language" use="@alpha3t"/>
 
  <xsl:variable name="lower" select="'abcdefghijklmnopqrstuvwxyz'" />
  <xsl:variable name="upper" select="'ABCDEFGHIJKLMNOPQRSTUVWXYZ'" />

  <!-- TODO include full set of country and language codes -->
  <!--
  <xsl:variable name="codes">
    <xsl:copy-of select="document('codes.xml')/*" />
  </xsl:variable>
  <xsl:variable name="countries_3166" select="msxsl:node-set($codes)/codes/countryCodes" />
  <xsl:variable name="languages_632" select="msxsl:node-set($codes)/codes/languageCodes" />
-->
  <xsl:variable name="countries_3166" select='US'></xsl:variable>
  <xsl:variable name="languages_632" select='en'></xsl:variable>
	<xsl:template match="/metadata">
		<metadata>
			<Esri>
				<ArcGISFormat>1.0</ArcGISFormat>
        <!--
          <CreaDate>20111017</CreaDate>
          <CreaTime>16451700</CreaTime>
          <SyncOnce>FALSE</SyncOnce>
          <SyncDate>20111020</SyncDate>
          <SyncTime>16140500</SyncTime>
          <ModDate>20121019</ModDate>
          <ModTime>11383400</ModTime>
        -->
        <xsl:for-each select="distinfo/resdesc[(text() = 'Live Data and Maps') or (text() = 'Downloadable Data') or (text() = 'Offline Data') or (text() = 'Static Map Images') or (text() = 'Other Documents') or (text() = 'Clearinghouses') or (text() = 'Applications') or (text() = 'Geographic Services') or (text() = 'Map Files') or (text() = 'Geographic Activities')]">
          <DataProperties>
            <itemProps>
              <imsContentType>
                <xsl:call-template name="contentTypeCode">
                  <xsl:with-param name="source" select="."/> 
                </xsl:call-template>
              </imsContentType>
            </itemProps>
          </DataProperties>
        </xsl:for-each>
        
        <xsl:if test="(dataqual/lineage/srcinfo/srcscale != '') and (count(dataqual/lineage/srcinfo/srcscale) >1)">
          <xsl:variable name="source" select="dataqual/lineage/srcinfo" />
          <scaleRange>
            <xsl:for-each select="$source">
              <xsl:sort select="srcscale" data-type="number" order="descending"/>
              <xsl:if test="position() = last()">
                <maxScale><xsl:value-of select="srcscale" /></maxScale>
              </xsl:if>
              <xsl:if test="position() = 1">
                <minScale><xsl:value-of select="srcscale" /></minScale>
              </xsl:if>
            </xsl:for-each>
          </scaleRange>
        </xsl:if>
			</Esri>

      <xsl:apply-templates select="metainfo" />
		</metadata>
	</xsl:template>
	
	<!-- MD_Metadata -->
	<xsl:template name="metadataContent">
			<xsl:for-each select="gmd:contentInfo/child::*">
				<contInfo>
					<xsl:call-template name="MD_ContentInformation" />
				</contInfo>
			</xsl:for-each>
	</xsl:template>
	

	<!-- Metadata Information -->
	<xsl:template match="metainfo">
			<xsl:for-each select="langmeta">
        <xsl:variable name="lowerValue" select="translate(., $upper, $lower)" />
        <mdLang>
          <languageCode>
            <xsl:attribute name="value">
              <xsl:apply-templates select="$languages_632">
                <xsl:with-param name="lower" select="$lowerValue" />
                <xsl:with-param name="original" select="." />
              </xsl:apply-templates>
            </xsl:attribute>
          </languageCode>
        </mdLang>
      </xsl:for-each>
      <mdChar>
        <CharSetCd>
          <xsl:attribute name="value">
            <xsl:call-template name="MD_CharacterSetCode">
              <xsl:with-param name="source">utf8</xsl:with-param> 
            </xsl:call-template>
          </xsl:attribute>
        </CharSetCd>
      </mdChar>
      <mdHrLv>
        <ScopeCd>
          <xsl:attribute name="value">
            <xsl:call-template name="MD_ScopeCode">
              <xsl:with-param name="source">dataset</xsl:with-param> 
            </xsl:call-template>
          </xsl:attribute>
        </ScopeCd>
      </mdHrLv>
			<xsl:for-each select="metc">
				<mdContact>
          <xsl:apply-templates select="cntinfo">
            <xsl:with-param name="role">pointofcontact</xsl:with-param> 
          </xsl:apply-templates>
				</mdContact>
			</xsl:for-each>
			<xsl:for-each select="metd">
        <mdDateSt><xsl:value-of select="." /></mdDateSt>
			</xsl:for-each>
			<mdStanName>
				<xsl:text>ArcGIS Metadata</xsl:text>
			</mdStanName>
			<mdStanVer>
				<xsl:text>1.0</xsl:text>
			</mdStanVer>
			<!-- TODO: mettc -->
      <xsl:for-each select="metextns">
        <xsl:if test="(onlink != 'http://www.esri.com/metadata/esriprof80.html') and (onlink != 'http://www.esri.com/') and (onlink != 'http://www.esri.com')">
          <mdExtInfo>
            <extOnRes>
              <xsl:call-template name="CI_OnlineResource" />
            </extOnRes>
          </mdExtInfo>
        </xsl:if>
      </xsl:for-each>
			
      <xsl:apply-templates select="../spdoinfo" />
      <xsl:apply-templates select="../idinfo" />
      <xsl:if test="../distinfo">
        <xsl:call-template name="MD_Distribution" />
      </xsl:if>
      <xsl:apply-templates select="../dataqual" />
      
			<xsl:for-each select="metuc[(. != '') and (translate(., $upper, $lower) != 'none')]">
        <mdConst>
          <xsl:call-template name="MD_Constraints" />
        </mdConst>
			</xsl:for-each>
			<xsl:if test="metac[(. != '') and (translate(., $upper, $lower) != 'none')]">
        <mdConst>
          <xsl:call-template name="MD_LegalConstraints" />
        </mdConst>
			</xsl:if>
			<xsl:for-each select="metsi">
        <mdConst>
          <xsl:call-template name="MD_SecurityConstraints" />
        </mdConst>
			</xsl:for-each>
      <xsl:if test="(metrd != '') or (metfrd != '')">
        <mdMaint>
          <xsl:call-template name="MD_MaintenanceInformation" />
        </mdMaint>
      </xsl:if>

      <xsl:copy-of select="../eainfo" />
	</xsl:template>
	
	<!-- Identification Information -->
	<xsl:template match="idinfo">
    <dataIdInfo>
      <xsl:for-each select="citation">
        <idCitation>
          <xsl:apply-templates select="citeinfo" />
        </idCitation>
      </xsl:for-each>
      <xsl:for-each select="descript/abstract">
        <idAbs><xsl:value-of select="."/></idAbs>
      </xsl:for-each>
      <xsl:for-each select="descript/purpose">
        <idPurp><xsl:value-of select="."/></idPurp>
      </xsl:for-each>
      <xsl:for-each select="datacred">
        <idCredit><xsl:value-of select="."/></idCredit>
      </xsl:for-each>
      <xsl:for-each select="status/progress">
        <idStatus>
          <ProgCd>
            <xsl:attribute name="value">
              <xsl:call-template name="MD_ProgressCode">
                <xsl:with-param name="source" select="translate(., $upper, $lower)" />
              </xsl:call-template>
            </xsl:attribute>
          </ProgCd>
        </idStatus>
      </xsl:for-each>
      <xsl:for-each select="ptcontac">
        <idPoC>
          <xsl:apply-templates select="cntinfo">
            <xsl:with-param name="role">pointofcontact</xsl:with-param> 
          </xsl:apply-templates>
        </idPoC>
      </xsl:for-each>
      <xsl:for-each select="status[update != '']">
        <resMaint>
          <xsl:call-template name="MD_MaintenanceInformation" />
        </resMaint>
      </xsl:for-each>
      <xsl:for-each select="browse">
        <graphOver>
          <xsl:call-template name="MD_BrowseGraphic" />
        </graphOver>
      </xsl:for-each>
      
      <xsl:for-each select="keywords">
        <xsl:for-each select="place">
          <placeKeys>
            <xsl:call-template name="MD_Keywords" />
          </placeKeys>
        </xsl:for-each>
        <xsl:for-each select="stratum">
          <stratKeys>
            <xsl:call-template name="MD_Keywords" />
          </stratKeys>
        </xsl:for-each>
        <xsl:for-each select="temporal">
          <tempKeys>
            <xsl:call-template name="MD_Keywords" />
          </tempKeys>
        </xsl:for-each>
        <xsl:for-each select="theme">
          <themeKeys>
            <xsl:call-template name="MD_Keywords" />
          </themeKeys>
        </xsl:for-each>
        <searchKeys>
          <xsl:for-each select="place/placekey | stratum/stratkey | temporal/tempkey | theme/themekey">
            <keyword><xsl:value-of select="." /></keyword>
          </xsl:for-each>
        </searchKeys>
      </xsl:for-each>

			<xsl:for-each select="useconst[(. != '') and (translate(., $upper, $lower) != 'none')]">
        <resConst>
          <xsl:call-template name="MD_Constraints" />
        </resConst>
			</xsl:for-each>
      <xsl:if test="((accconst != '') and (translate(accconst, $upper, $lower) != 'none')) or (../distinfo/distliab != '')">
        <resConst>
          <xsl:call-template name="MD_LegalConstraints" />
        </resConst>
      </xsl:if>
			<xsl:for-each select="secinfo">
        <resConst>
          <xsl:call-template name="MD_SecurityConstraints" />
        </resConst>
			</xsl:for-each>
			
      <xsl:for-each select="citation/citeinfo/lworkcit">
        <aggrInfo>
          <aggrDSName>
            <xsl:apply-templates select="citeinfo" />
          </aggrDSName>
          <assocType>
            <AscTypeCd>
              <xsl:attribute name="value">
                <xsl:call-template name="DS_AssociationTypeCode">
                  <xsl:with-param name="source">largerWorkCitation</xsl:with-param>
                </xsl:call-template>
              </xsl:attribute>
            </AscTypeCd>
          </assocType>
        </aggrInfo>
      </xsl:for-each>
      <xsl:for-each select="crossref">
        <!-- we lose assndesc? -->
        <aggrInfo>
          <aggrDSName>
            <xsl:apply-templates select="citeinfo" />
          </aggrDSName>
          <assocType>
            <AscTypeCd>
              <xsl:attribute name="value">
                <xsl:call-template name="DS_AssociationTypeCode">
                  <xsl:with-param name="source">crossReference</xsl:with-param>
                </xsl:call-template>
              </xsl:attribute>
            </AscTypeCd>
          </assocType>
        </aggrInfo>
      </xsl:for-each>
      
      <xsl:for-each select="../spdoinfo/direct">
        <spatRpType>
          <SpatRepTypCd>
            <xsl:attribute name="value">
              <xsl:call-template name="MD_SpatialRepresentationTypeCode">
                <xsl:with-param name="source" select="translate(., $upper, $lower)" />
              </xsl:call-template>
            </xsl:attribute>
          </SpatRepTypCd>
        </spatRpType>
      </xsl:for-each>
      <xsl:for-each select="descript/langdata">
        <xsl:variable name="lowerValue" select="translate(., $upper, $lower)" />
        <dataLang>
          <languageCode>
            <xsl:attribute name="value">
              <xsl:apply-templates select="$languages_632">
                <xsl:with-param name="lower" select="$lowerValue" />
                <xsl:with-param name="original" select="." />
              </xsl:apply-templates>
            </xsl:attribute>
          </languageCode>
        </dataLang>
      </xsl:for-each>
      <xsl:if test="(keywords/theme/themekey != '')">
        <xsl:variable name="topics">
          <xsl:for-each select="keywords/theme/themekey">
            <topic>
              <xsl:call-template name="MD_TopicCategoryCode">
                <xsl:with-param name="source"><xsl:value-of select="translate(., $upper, $lower)" /></xsl:with-param>
              </xsl:call-template>
            </topic>
          </xsl:for-each>
        </xsl:variable>
        <xsl:for-each select="$topics"> <!-- /topic[(. != '')]"> -->
          <!-- <xsl:sort select="." /> -->
	  <xsl:if test="(not(. = preceding-sibling::*))">
            <tpCat>
              <TopicCatCd>
                <xsl:attribute name="value"><xsl:value-of select="." /></xsl:attribute>
              </TopicCatCd>
            </tpCat>
          </xsl:if>
        </xsl:for-each>
      </xsl:if>
      <xsl:for-each select="native">
        <envirDesc><xsl:value-of select="."/></envirDesc>
      </xsl:for-each>
      <xsl:for-each select="spdom[(bounding/* != '') or (dsgpoly//* != '') or (minalti != '') or (maxalti != '')]">
        <dataExt>
          <xsl:for-each select="bounding">
            <geoEle>
              <GeoBndBox>
                <xsl:call-template name="EX_GeographicBoundingBox" />
              </GeoBndBox>
            </geoEle>
          </xsl:for-each>
          <xsl:for-each select="dsgpoly">
            <geoEle>
              <BoundPoly>
                <xsl:call-template name="EX_BoundingPolygon" />
              </BoundPoly>
            </geoEle>
          </xsl:for-each>
          <xsl:if test="(minalti != '') or (maxalti != '')">
            <vertEle>
              <xsl:call-template name="EX_VerticalExtent" />
            </vertEle>
         </xsl:if>
        </dataExt>
      </xsl:for-each>
      <xsl:for-each select="timeperd">
        <dataExt>
          <xsl:call-template name="EX_TemporalExtent" />
        </dataExt>
      </xsl:for-each>
      <xsl:for-each select="descript/supplinf">
        <suppInfo><xsl:value-of select="." /></suppInfo>
      </xsl:for-each>
		</dataIdInfo>
	</xsl:template>
	
	<!-- MD_BrowseGraphic -->
	<xsl:template name="MD_BrowseGraphic">
    <xsl:for-each select="browsen">
      <bgFileName><xsl:value-of select="."/></bgFileName>
    </xsl:for-each>
    <xsl:for-each select="browsed">
      <bgFileDesc><xsl:value-of select="."/></bgFileDesc>		
    </xsl:for-each>
    <xsl:for-each select="browset">
      <bgFileType><xsl:value-of select="."/></bgFileType>
    </xsl:for-each>
	</xsl:template>
	
	<!-- MD_Keywords -->
	<xsl:template name="MD_Keywords">
		<xsl:for-each select="themekey | placekey | stratkey | tempkey">
			<keyword><xsl:value-of select="."/></keyword>
		</xsl:for-each>
		<xsl:for-each select="themekt | placekt | stratkt | tempkt">
      <xsl:if test="(. != 'None') and (. != 'none')">
        <thesaName>
          <resTitle><xsl:value-of select="."/></resTitle>
        </thesaName>
      </xsl:if>
		</xsl:for-each>
	</xsl:template>
	
	<!-- MD_MaintenanceInformation -->
	<xsl:template name="MD_MaintenanceInformation">
		<xsl:for-each select="update">
      <maintFreq>
        <MaintFreqCd>
          <xsl:attribute name="value">
            <xsl:call-template name="MD_MaintenanceFrequencyCode">
              <xsl:with-param name="source" select="translate(., $upper, $lower)" />
            </xsl:call-template>
          </xsl:attribute>
        </MaintFreqCd>
      </maintFreq>
    </xsl:for-each>
		<xsl:if test="not(update)">
      <maintFreq>
        <MaintFreqCd>
          <xsl:attribute name="value">
            <xsl:call-template name="MD_MaintenanceFrequencyCode">
              <xsl:with-param name="source">unknown</xsl:with-param>
            </xsl:call-template>
          </xsl:attribute>
        </MaintFreqCd>
      </maintFreq>
    </xsl:if>
    <xsl:for-each select="metfrd">
      <xsl:variable name="date">
        <xsl:call-template name="dateHandler">
          <xsl:with-param name="source" select="." /><!-- Domain: free date -->
        </xsl:call-template>
      </xsl:variable>
      <xsl:if test="($date != '')">
        <dateNext><xsl:value-of select="$date"/></dateNext>
      </xsl:if>
    </xsl:for-each>
		<xsl:for-each select="metrd">
      <xsl:variable name="date">
        <xsl:call-template name="dateHandler">
          <xsl:with-param name="source" select="." /><!-- Domain: free date -->
        </xsl:call-template>
      </xsl:variable>
      <xsl:if test="($date != '')">
        <maintNote>Last metadata review date: <xsl:value-of select="$date"/></maintNote>
      </xsl:if>
		</xsl:for-each>
	</xsl:template>
	
	<!-- MD_Constraints -->
	<xsl:template name="MD_Constraints">
    <Consts><!-- not handling "none" -->
      <useLimit><xsl:value-of select="." /></useLimit>
    </Consts>
  </xsl:template>

	<!-- MD_LegalConstraints -->
	<xsl:template name="MD_LegalConstraints">
    <LegConsts>
      <xsl:if test="(local-name() = 'idinfo')">
        <xsl:for-each select="../distinfo/distliab">
          <useLimit><xsl:value-of select="." /></useLimit>
        </xsl:for-each>
      </xsl:if>
      <xsl:for-each select="accconst[(. != '') and (translate(., $upper, $lower) != 'none')] | metac[(. != '') and (translate(., $upper, $lower) != 'none')]">
        <accessConsts>
          <RestrictCd>
            <xsl:attribute name="value">
              <xsl:call-template name="MD_RestrictionCode">
                <xsl:with-param name="source">otherRestrictions</xsl:with-param> 
              </xsl:call-template>
            </xsl:attribute>
          </RestrictCd>
        </accessConsts>
        <othConsts><xsl:value-of select="." /></othConsts>
      </xsl:for-each>
		</LegConsts>
	</xsl:template>

	<!-- MD_SecurityConstraints -->
	<xsl:template name="MD_SecurityConstraints">
    <SecConsts>
      <xsl:for-each select="metsc | secclass">
        <class>
          <ClasscationCd>
            <xsl:attribute name="value">
              <xsl:call-template name="MD_ClassificationCode">
                <xsl:with-param name="source" select="translate(., $upper, $lower)" />
              </xsl:call-template>
            </xsl:attribute>
          </ClasscationCd>
        </class>
      </xsl:for-each>
      <xsl:for-each select="metscs | secsys">
        <classSys>
          <xsl:value-of select="." />
        </classSys>
      </xsl:for-each>
      <xsl:for-each select="metshd | sechandl">
        <handDesc>
          <xsl:value-of select="." />
        </handDesc>
      </xsl:for-each>
    </SecConsts>
	</xsl:template>


	<!-- Data Quality Information -->
	<xsl:template match="dataqual">
    <xsl:if test="(attracc//* != '') or (logic != '') or (complete != '') or (posacc//* != '') or (lineage/* != '')">
      <dqInfo>
        <dqScope>
          <scpLvl>
            <ScopeCd>
              <xsl:attribute name="value">
                <xsl:call-template name="MD_ScopeCode">
                  <xsl:with-param name="source">dataset</xsl:with-param>
                </xsl:call-template>
              </xsl:attribute>
            </ScopeCd>
          </scpLvl>
        </dqScope>
        <xsl:for-each select="attracc/qattracc">
          <report>
            <xsl:attribute name="type">
              <xsl:call-template name="DQ_ElementType">
                <xsl:with-param name="source">DQ_QuantitativeAttributeAccuracy</xsl:with-param>
              </xsl:call-template>
            </xsl:attribute>
            <xsl:for-each select="../attraccr">
              <measDesc><xsl:value-of select="." /></measDesc>
            </xsl:for-each>
            <xsl:for-each select="attracce">
              <evalMethDesc><xsl:value-of select="." /></evalMethDesc>
            </xsl:for-each>
            <xsl:for-each select="attraccv">
              <measResult>
                <QuanResult>
                  <quanVal><xsl:value-of select="." /></quanVal>
                </QuanResult>
              </measResult>
            </xsl:for-each>
          </report>
        </xsl:for-each>
        <xsl:for-each select="logic">
          <report>
            <xsl:attribute name="type">
              <xsl:call-template name="DQ_ElementType">
                <xsl:with-param name="source">DQ_ConceptualConsistency</xsl:with-param>
              </xsl:call-template>
            </xsl:attribute>
            <measDesc><xsl:value-of select="." /></measDesc>
          </report>
        </xsl:for-each>
        <xsl:for-each select="complete">
          <report>
            <xsl:attribute name="type">
              <xsl:call-template name="DQ_ElementType">
                <xsl:with-param name="source">DQ_CompletenessOmission</xsl:with-param>
              </xsl:call-template>
            </xsl:attribute>
            <measDesc><xsl:value-of select="." /></measDesc>
          </report>
        </xsl:for-each>
        <xsl:for-each select="posacc/horizpa/qhorizpa">
          <report>
            <xsl:attribute name="type">
              <xsl:call-template name="DQ_ElementType">
                <xsl:with-param name="source">DQ_AbsoluteExternalPositionalAccuracy</xsl:with-param>
              </xsl:call-template>
            </xsl:attribute>
            <xsl:attribute name="dimension">horizontal</xsl:attribute>
            <xsl:for-each select="../horizpar">
              <measDesc><xsl:value-of select="." /></measDesc>
            </xsl:for-each>
            <xsl:for-each select="horizpae">
              <evalMethDesc><xsl:value-of select="." /></evalMethDesc>
            </xsl:for-each>
            <xsl:for-each select="horizpav">
              <measResult>
                <QuanResult>
                  <quanVal><xsl:value-of select="." /></quanVal>
                </QuanResult>
              </measResult>
            </xsl:for-each>
          </report>
        </xsl:for-each>
        <xsl:for-each select="posacc/vertacc/qvertpa">
          <report>
            <xsl:attribute name="type">
              <xsl:call-template name="DQ_ElementType">
                <xsl:with-param name="source">DQ_AbsoluteExternalPositionalAccuracy</xsl:with-param>
              </xsl:call-template>
            </xsl:attribute>
            <xsl:attribute name="dimension">vertical</xsl:attribute>
            <xsl:for-each select="../vertaccr">
              <measDesc><xsl:value-of select="." /></measDesc>
            </xsl:for-each>
            <xsl:for-each select="vertacce">
              <evalMethDesc><xsl:value-of select="." /></evalMethDesc>
            </xsl:for-each>
            <xsl:for-each select="vertaccv">
              <measResult>
                <QuanResult>
                  <quanVal><xsl:value-of select="." /></quanVal>
                </QuanResult>
              </measResult>
            </xsl:for-each>
          </report>
        </xsl:for-each>
        <xsl:for-each select="lineage">
          <dataLineage>
            <xsl:call-template name="LI_Lineage" />
          </dataLineage>
        </xsl:for-each>
      </dqInfo>
    </xsl:if>
	</xsl:template>
	
	<!-- LI_Lineage -->
	<xsl:template name="LI_Lineage">
		<xsl:for-each select="procstep[(procdesc != 'Dataset copied.')]">
			<prcStep>
				<xsl:call-template name="LI_ProcessStep" />
			</prcStep>
		</xsl:for-each>
		<xsl:for-each select="srcinfo">
			<dataSource>
				<xsl:call-template name="LI_Source" />
			</dataSource>
		</xsl:for-each>
	</xsl:template>
	
	<!-- LI_ProcessStep -->
	<xsl:template name="LI_ProcessStep">
    <xsl:for-each select="procdesc">
      <stepDesc><xsl:value-of select="." /></stepDesc>
    </xsl:for-each>
    <!-- not handling procsv -->
    
    <xsl:variable name="date">
      <xsl:call-template name="dateHandler">
        <xsl:with-param name="source" select="procdate" /><!-- Domain: "Unknown" "Not complete" free date -->
      </xsl:call-template>
    </xsl:variable>
    <xsl:variable name="time">
      <xsl:call-template name="timeHandler">
        <xsl:with-param name="source" select="proctime" /><!-- Domain: free time -->
      </xsl:call-template>
    </xsl:variable>
    <xsl:variable name="dateText">
      <xsl:value-of select="translate(procdate, $upper, $lower)" />
    </xsl:variable>
    <xsl:variable name="timeText">
      <xsl:value-of select="translate(proctime, $upper, $lower)" />
    </xsl:variable>
    <xsl:for-each select="procdate[1]">
      <xsl:if test="(. != '') or (../proctime != '')">
        <stepDateTm>
          <xsl:choose>
            <xsl:when test="($dateText = 'unknown')">
              <xsl:attribute name="date">unknown</xsl:attribute>
              <xsl:if test="($timeText = 'unknown')">
                <xsl:attribute name="time">unknown</xsl:attribute>
              </xsl:if>
            </xsl:when>
            <xsl:when test="($dateText = 'not complete')">
              <xsl:attribute name="date">inapplicable</xsl:attribute>
              <xsl:if test="($timeText = 'unknown')">
                <xsl:attribute name="time">unknown</xsl:attribute>
              </xsl:if>
            </xsl:when>
            <xsl:otherwise>
              <xsl:if test="($timeText = 'unknown')">
                <xsl:attribute name="time">unknown</xsl:attribute>
              </xsl:if>
              <xsl:if test="($date != '')">
                <xsl:value-of select="$date" /><xsl:if test="($time != '')">T<xsl:value-of select="$time" /></xsl:if>
              </xsl:if>
            </xsl:otherwise>
          </xsl:choose>
        </stepDateTm>
      </xsl:if>
    </xsl:for-each>
    
		<xsl:for-each select="proccont">
			<stepProc>
        <xsl:apply-templates select="cntinfo">
          <xsl:with-param name="role">processor</xsl:with-param> 
        </xsl:apply-templates>
			</stepProc>
		</xsl:for-each>
		<xsl:for-each select="srcused">
			<stepSrc>
        <xsl:attribute name="type">used</xsl:attribute>
        <srcCitatn>
          <resAltTitle><xsl:value-of select="." /></resAltTitle>
        </srcCitatn>
			</stepSrc>
		</xsl:for-each>
		<xsl:for-each select="srcprod">
			<stepSrc>
        <xsl:attribute name="type">produced</xsl:attribute>
        <srcCitatn>
          <resAltTitle><xsl:value-of select="." /></resAltTitle>
        </srcCitatn>
			</stepSrc>
		</xsl:for-each>
	</xsl:template>
	
	<!-- LI_Source -->
	<xsl:template name="LI_Source">
    <xsl:for-each select="srccontr">
      <srcDesc><xsl:value-of select="." /></srcDesc>
    </xsl:for-each>
		<xsl:for-each select="typesrc">
      <srcMedName>
        <MedNameCd>
          <xsl:attribute name="value">
            <xsl:call-template name="MD_MediumNameCode">
              <xsl:with-param name="source" select="translate(., $upper, $lower)" />
            </xsl:call-template>
          </xsl:attribute>
        </MedNameCd>
      </srcMedName>
    </xsl:for-each>
		<xsl:for-each select="srcscale">
			<srcScale>
        <rfDenom><xsl:value-of select="."/></rfDenom>
			</srcScale>
		</xsl:for-each>
		<xsl:for-each select="srccite">
			<srcCitatn>
        <xsl:apply-templates select="citeinfo" />
			</srcCitatn>
		</xsl:for-each>
		<xsl:for-each select="srctime">
			<srcExt>
				<xsl:call-template name="EX_TemporalExtent" />
			</srcExt>
		</xsl:for-each>
	</xsl:template>
	
	<!-- Spatial Data Information -->
	<xsl:template match="spdoinfo">
		<xsl:for-each select="rastinfo">
      <spatRepInfo>
        <Georect>
          <xsl:call-template name="MD_GridSpatialRepresentation" />
        </Georect>
      </spatRepInfo>
		</xsl:for-each>
		<xsl:for-each select="ptvctinf">
      <spatRepInfo>
        <VectSpatRep>
          <xsl:call-template name="MD_VectorSpatialRepresentation" />
        </VectSpatRep>
      </spatRepInfo>
		</xsl:for-each>
		<xsl:for-each select="indspref">
      <spatRepInfo>
        <Indref><xsl:value-of select="." /></Indref>
      </spatRepInfo>
		</xsl:for-each>
	</xsl:template>

	<!-- MD_GridSpatialRepresentation -->
	<xsl:template name="MD_GridSpatialRepresentation">
		<numDims><xsl:value-of select="count(rowcount[. != ''] | colcount[. != ''] | vrtcount[. != ''])"/></numDims>
		<xsl:for-each select="rowcount | colcount | vrtcount">
			<axisDimension>
				<xsl:call-template name="MD_Dimension" />
			</axisDimension>
		</xsl:for-each>
		<!--
		<xsl:for-each select="rowcount | colcount | vrtcount[. &gt; 1]">
			<axisDimension>
				<xsl:call-template name="MD_Dimension" />
			</axisDimension>
		</xsl:for-each>
		-->
		<xsl:for-each select="rasttype">
			<cellGeo>
				<CellGeoCd>
					<xsl:attribute name="value">
						<xsl:call-template name="MD_CellGeometryCode">
							<xsl:with-param name="source" select="translate(., $upper, $lower)" />
						</xsl:call-template>
					</xsl:attribute>
				</CellGeoCd>
			</cellGeo>
		</xsl:for-each>
	</xsl:template>
	
	<!-- MD_VectorSpatialRepresentation -->
	<xsl:template name="MD_VectorSpatialRepresentation">
		<xsl:for-each select="vpfterm/vpflevel">
			<topLvl>
				<TopoLevCd>
					<xsl:attribute name="value">
						<xsl:call-template name="MD_TopologyLevelCode">
							<xsl:with-param name="source" select="translate(., $upper, $lower)" />
						</xsl:call-template>
					</xsl:attribute>
				</TopoLevCd>
			</topLvl>
		</xsl:for-each>
    <xsl:for-each select="sdtsterm">
      <geometObjs>
        <xsl:call-template name="MD_GeometricObjects" />
      </geometObjs>
    </xsl:for-each>
    <xsl:for-each select="vpftype">
      <geometObjs>
        <xsl:call-template name="MD_GeometricObjects" />
      </geometObjs>
    </xsl:for-each>
	</xsl:template>
	
	<!-- MD_Dimension -->
	<xsl:template name="MD_Dimension">
		<xsl:choose>
      <xsl:when test="name() = 'rowcount'">
        <xsl:attribute name="type">
          <xsl:call-template name="MD_DimensionNameTypeCode">
            <xsl:with-param name="source">row</xsl:with-param>
          </xsl:call-template>
        </xsl:attribute>
      </xsl:when>
      <xsl:when test="name() = 'colcount'">
        <xsl:attribute name="type">
          <xsl:call-template name="MD_DimensionNameTypeCode">
            <xsl:with-param name="source">column</xsl:with-param>
          </xsl:call-template>
        </xsl:attribute>
      </xsl:when>
      <xsl:when test="name() = 'vrtcount'">
        <xsl:attribute name="type">
          <xsl:call-template name="MD_DimensionNameTypeCode">
            <xsl:with-param name="source">vertical</xsl:with-param>
          </xsl:call-template>
        </xsl:attribute>
      </xsl:when>
    </xsl:choose>
    <dimSize><xsl:value-of select="."/></dimSize>
	</xsl:template>
	
	<!-- MD_GeometricObjects -->
	<xsl:template name="MD_GeometricObjects">
		<xsl:for-each select="sdtstype | vpftype">
			<geoObjTyp>
				<GeoObjTypCd>
					<xsl:attribute name="value">
						<xsl:call-template name="MD_GeometricObjectTypeCode">
							<xsl:with-param name="source" select="translate(., $upper, $lower)" />
						</xsl:call-template>
					</xsl:attribute>
				</GeoObjTypCd>
			</geoObjTyp>
		</xsl:for-each>
    <xsl:for-each select="ptvctcnt">
      <geoObjCnt><xsl:value-of select="."/></geoObjCnt>
    </xsl:for-each>
	</xsl:template>


	<!-- MD_ContentInformation (abstract) -->
	<xsl:template name="MD_ContentInformation">
		<xsl:variable name="subclass">
			<xsl:value-of select="name()" />
		</xsl:variable>
		<xsl:choose>
			<xsl:when test="$subclass = 'MD_CoverageDescription'">
        <CovDesc>
          <xsl:call-template name="MD_CoverageDescription" />
        </CovDesc>
			</xsl:when>
			<xsl:otherwise>
				<ERROR><xsl:value-of select="$subclass" /></ERROR>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	
	<!-- MD_CoverageDescription -->
	<xsl:template name="MD_CoverageDescription">
    <xsl:for-each select="gmd:attributeDescription/gco:RecordType">
      <attDesc>
        <xsl:value-of select="." />
      </attDesc>
    </xsl:for-each>
		<xsl:for-each select="gmd:contentType/gmd:MD_CoverageContentTypeCode">
			<imagCond>
				<ImgCondCd>
					<xsl:attribute name="value">
						<xsl:call-template name="MD_CoverageContentTypeCode">
							<xsl:with-param name="source"><xsl:value-of select="@codeListValue" /></xsl:with-param>
						</xsl:call-template>
					</xsl:attribute>
				</ImgCondCd>
			</imagCond>
		</xsl:for-each>
		<xsl:for-each select="gmd:dimension">
      <covDim>
        <xsl:for-each select="gmd:MD_RangeDimension">
          <RangeDim>
            <xsl:call-template name="MD_RangeDimension" />
          </RangeDim>
        </xsl:for-each>
      </covDim>
		</xsl:for-each>
	</xsl:template>
	
	<!-- B.2.8.2 Range dimension information (includes Band information) -->
	<!-- MD_RangeDimension -->
	<xsl:template name="MD_RangeDimension">
    <xsl:for-each select="gmd:sequenceIdentifier/gco:MemberName">
      <seqID>
      </seqID>
    </xsl:for-each>
    <xsl:for-each select="gmd:descriptor/gco:CharacterString">
      <dimDescrp><xsl:value-of select="." /></dimDescrp>
    </xsl:for-each>
	</xsl:template>
	
	
	<!-- Distribution information -->
	<xsl:template name="MD_Distribution">
    <distInfo>
      <xsl:for-each select="../distinfo/stdorder/digform | ../distinfo/stdorder/nondig | ../distinfo/custom">
        <distributor>
          <xsl:call-template name="MD_Distributor"/>
        </distributor>
      </xsl:for-each>
      <xsl:for-each select="/metadata/idinfo/citation/citeinfo[onlink != '']">
        <distTranOps>
          <onLineSrc>
            <xsl:call-template name="CI_OnlineResource"/>
          </onLineSrc>
        </distTranOps>
      </xsl:for-each>
    </distInfo>
    <!-- not handled: resdesc -->
	</xsl:template>
	
	<!-- MD_Distributor -->
	<xsl:template name="MD_Distributor">
		<xsl:for-each select="../distrib | ../../distrib">
			<distorCont>
        <xsl:apply-templates select="cntinfo">
          <xsl:with-param name="role">distributor</xsl:with-param>
        </xsl:apply-templates>
			</distorCont>
		</xsl:for-each>
		<xsl:if test="(../fees != '') or (../ordering != '') or (../turnarnd != '') or (../../availabl != '') or (local-name() = 'custom')">
      <distorOrdPrc>
        <xsl:call-template name="MD_StandardOrderProcess"/>
      </distorOrdPrc>
    </xsl:if>
    <xsl:if test="(local-name() = 'digform')">
      <xsl:for-each select="digtinfo">
        <distorFormat>
          <xsl:call-template name="MD_Format"/>
        </distorFormat>
      </xsl:for-each>
      <xsl:for-each select="digtopt/offoptn[* != '']">
        <distorTran>
          <xsl:call-template name="MD_DigitalTransferOptions"/>
        </distorTran>
      </xsl:for-each>
      <xsl:for-each select="digtopt/onlinopt[computer/networka/networkr != '']">
        <distorTran>
          <xsl:call-template name="MD_DigitalTransferOptions"/>
        </distorTran>
      </xsl:for-each>
    </xsl:if>
		<xsl:if test="(local-name() = 'nondig')">
			<distorTran>
        <offLineMed>
          <xsl:call-template name="MD_Medium"/>
        </offLineMed>
			</distorTran>
    </xsl:if>
	</xsl:template>
	
	<!-- MD_Format -->
	<xsl:template name="MD_Format">
    <xsl:for-each select="formname">
      <formatName><xsl:value-of select="."/></formatName>
    </xsl:for-each>
    <xsl:if test="(formvern != '') or (formverd != '')">
      <xsl:variable name="versionDate">
        <xsl:call-template name="dateHandler">
          <xsl:with-param name="source" select="formverd" /><!-- Domain: "Unknown" free date -->
        </xsl:call-template>
      </xsl:variable>
      <xsl:choose>
        <xsl:when test="(formvern != '') and (formverd != '')">
          <formatVer><xsl:value-of select="formvern"/>; <xsl:choose>
              <xsl:when test="($versionDate != '')"><xsl:value-of select="$versionDate"/></xsl:when>
              <xsl:otherwise><xsl:value-of select="formverd"/></xsl:otherwise>
            </xsl:choose>
          </formatVer>
        </xsl:when>
        <xsl:when test="(formvern != '')">
          <formatVer><xsl:value-of select="formvern"/></formatVer>
        </xsl:when>
        <xsl:when test="(formverd != '')">
          <formatVer>
            <xsl:choose>
              <xsl:when test="($versionDate != '')"><xsl:value-of select="$versionDate"/></xsl:when>
              <xsl:otherwise><xsl:value-of select="formverd"/></xsl:otherwise>
            </xsl:choose></formatVer>
        </xsl:when>
      </xsl:choose>
    </xsl:if>
    <xsl:for-each select="gmd:amendmentNumber/gco:CharacterString">
      <formatAmdNum><xsl:value-of select="."/></formatAmdNum>
    </xsl:for-each>
    <xsl:for-each select="formspec">
      <formatSpec><xsl:value-of select="."/></formatSpec>
    </xsl:for-each>
    <xsl:for-each select="filedec">
      <fileDecmTech><xsl:value-of select="."/></fileDecmTech>
    </xsl:for-each>
    <xsl:for-each select="formcont">
      <formatInfo><xsl:value-of select="."/></formatInfo>
    </xsl:for-each>
    <xsl:for-each select="../../../techpreq">
      <formatTech><xsl:value-of select="."/></formatTech>
    </xsl:for-each>
	</xsl:template>
	
	<!-- MD_DigitalTransferOptions -->
	<xsl:template name="MD_DigitalTransferOptions">
    <xsl:if test="(../../digtinfo/transize != '')">
      <transSize><xsl:value-of select="../../digtinfo/transize"/></transSize>
    </xsl:if>
    <xsl:if test="(local-name() = 'offoptn')">
			<offLineMed>
				<xsl:call-template name="MD_Medium"/>
			</offLineMed>
    </xsl:if>
    <xsl:for-each select="computer/networka/networkr[. != '']">
      <onLineSrc>
        <xsl:call-template name="CI_OnlineResource"/>
      </onLineSrc>
    </xsl:for-each>
	</xsl:template>
	
	<!-- MD_Medium -->
	<xsl:template name="MD_Medium">
	  <xsl:for-each select="offmedia">
      <medName>
        <MedNameCd>
          <xsl:attribute name="value">
            <xsl:call-template name="MD_MediumNameCode">
              <xsl:with-param name="source" select="translate(., $upper, $lower)" />
            </xsl:call-template>
          </xsl:attribute>
        </MedNameCd>
      </medName>
    </xsl:for-each>
		<xsl:for-each select="reccap/recden">
			<medDensity><xsl:value-of select="." /></medDensity>
		</xsl:for-each>
	  <xsl:for-each select="reccap/recdenu">
      <medDenUnits><xsl:value-of select="." /></medDenUnits>
    </xsl:for-each>
		<xsl:for-each select="recfmt">
			<medFormat>
				<MedFormCd>
					<xsl:attribute name="value">
						<xsl:call-template name="MD_MediumFormatCode">
							<xsl:with-param name="source" select="translate(., $upper, $lower)" />
						</xsl:call-template>
					</xsl:attribute>
				</MedFormCd>
			</medFormat>
		</xsl:for-each>
	  <xsl:for-each select="compat">
      <medNote><xsl:value-of select="." /></medNote>
    </xsl:for-each>
	  <xsl:if test="(local-name() = 'nondig')">
      <medName>
        <MedNameCd>
          <xsl:attribute name="value">
            <xsl:call-template name="MD_MediumNameCode">
              <xsl:with-param name="source">hardcopy</xsl:with-param>
            </xsl:call-template>
          </xsl:attribute>
        </MedNameCd>
      </medName>
      <medNote><xsl:value-of select="." /></medNote>
    </xsl:if>
	</xsl:template>
	
	<!-- MD_StandardOrderProcess -->	
	<xsl:template name="MD_StandardOrderProcess">
	  <xsl:for-each select="fees | ../fees">
      <resFees>
        <!-- units="USD"
        <xsl:attribute name="value">
          <xsl:call-template name="MD_MediumNameCode">
          <xsl:with-param name="source"><xsl:value-of select="gmd:offLine/gmd:MD_Medium/gmd:name/gmd:MD_MediumNameCode/@codeListValue"/></xsl:with-param> 
        </xsl:call-template>
        </xsl:attribute>
        -->
        <xsl:value-of select="." />
      </resFees>
    </xsl:for-each>
		<xsl:for-each select="../availabl/timeinfo | ../../availabl/timeinfo">
      <xsl:for-each select="rngdates">
        <planAvTmPd>
          <xsl:call-template name="timePeriod" />
        </planAvTmPd>
      </xsl:for-each>
      <xsl:for-each select=".//sngdate">
        <planAvDtTm>
          <xsl:call-template name="timeInstant" />
        </planAvDtTm>
      </xsl:for-each>
		</xsl:for-each>
	  <xsl:for-each select="ordering | ../ordering">
      <ordInstr><xsl:value-of select="." /></ordInstr>
    </xsl:for-each>
		<xsl:if test="(local-name() = 'custom')">
      <ordInstr><xsl:value-of select="." /></ordInstr>
    </xsl:if>
	  <xsl:for-each select="turnarnd | ../turnarnd">
      <ordTurn><xsl:value-of select="." /></ordTurn>	
    </xsl:for-each>
	</xsl:template>


	<!-- EX_GeographicBoundingBox -->
	<xsl:template name="EX_GeographicBoundingBox">
    <exTypeCode>true</exTypeCode>
    <xsl:for-each select="westbc">
      <westBL><xsl:value-of select="." /></westBL>
    </xsl:for-each>
    <xsl:for-each select="eastbc">
      <eastBL><xsl:value-of select="." /></eastBL>
    </xsl:for-each>
    <xsl:for-each select="northbc">
      <northBL><xsl:value-of select="." /></northBL>
    </xsl:for-each>
    <xsl:for-each select="southbc">
      <southBL><xsl:value-of select="." /></southBL>
    </xsl:for-each>
	</xsl:template>
	
	<!-- EX_BoundingPolygon -->
	<xsl:template name="EX_BoundingPolygon">
    <exTypeCode>true</exTypeCode>
    <polygon>
      <xsl:for-each select="dsgpolyo">
        <exterior>
          <xsl:for-each select="gring">
            <posList><xsl:value-of select="translate(.,',',' ')" /></posList>
          </xsl:for-each>
          <xsl:for-each select="grngpoin">
            <pos><xsl:value-of select="gringlon" /><xsl:text> </xsl:text><xsl:value-of select="gringlat" /></pos>
          </xsl:for-each>
        </exterior>
      </xsl:for-each>
      <xsl:for-each select="dsgpolyx">
        <interior>
          <xsl:for-each select="gring">
            <posList><xsl:value-of select="translate(.,',',' ')" /></posList>
          </xsl:for-each>
          <xsl:for-each select="grngpoin">
            <pos><xsl:value-of select="gringlon" /><xsl:text> </xsl:text><xsl:value-of select="gringlat" /></pos>
          </xsl:for-each>
        </interior>
      </xsl:for-each>
    </polygon>
	</xsl:template>

	<!-- EX_TemporalExtent -->
	<xsl:template name="EX_TemporalExtent">
    <xsl:for-each select="current | srccurr"><!-- Domain: "ground condition" "publication date" free text -->
      <exDesc><xsl:value-of select="."/></exDesc>
    </xsl:for-each>
		<xsl:for-each select="timeinfo">
      <xsl:for-each select="rngdates">
        <tempEle>
          <TempExtent>
            <exTemp>
              <TM_Period>
                <xsl:call-template name="timePeriod" />
            </TM_Period>
          </exTemp>
        </TempExtent>
      </tempEle>
      </xsl:for-each>
      <xsl:for-each select=".//sngdate">
        <tempEle>
          <TempExtent>
            <exTemp>
              <TM_Instant>
                <tmPosition>
                  <xsl:call-template name="timeInstant" />
                </tmPosition>
              </TM_Instant>
            </exTemp>
          </TempExtent>
        </tempEle>
      </xsl:for-each>
		</xsl:for-each>
	</xsl:template>
	
	<!-- EX_VerticalExtent -->
	<xsl:template name="EX_VerticalExtent">
    <xsl:for-each select="minalti">
      <vertMinVal><xsl:value-of select="." /></vertMinVal>
    </xsl:for-each>
    <xsl:for-each select="maxalti">
      <vertMaxVal><xsl:value-of select="." /></vertMaxVal>
    </xsl:for-each>
	</xsl:template>
	

	<!-- Citation Information -->
	<xsl:template match="citeinfo">
    <xsl:for-each select="title">
      <resTitle><xsl:value-of select="."/></resTitle>
    </xsl:for-each>
    <xsl:for-each select="../../srccitea">
      <resAltTitle><xsl:value-of select="."/></resAltTitle>
    </xsl:for-each>
    
    <xsl:variable name="date">
      <xsl:call-template name="dateHandler">
        <xsl:with-param name="source" select="pubdate" /><!-- Domain: "Unknown" "Unpublished material" free date -->
      </xsl:call-template>
    </xsl:variable>
    <xsl:variable name="time">
      <xsl:call-template name="timeHandler">
        <xsl:with-param name="source" select="pubtime" /><!-- Domain: "Unknown" free time -->
      </xsl:call-template>
    </xsl:variable>
    <xsl:variable name="dateText">
      <xsl:value-of select="translate(pubdate, $upper, $lower)" />
    </xsl:variable>
    <xsl:variable name="timeText">
      <xsl:value-of select="translate(pubtime, $upper, $lower)" />
    </xsl:variable>
    <xsl:for-each select="pubdate[1]">
      <date>
        <xsl:if test="(. != '') or (../pubtime != '')">
          <pubDate>
            <xsl:choose>
              <xsl:when test="($dateText = 'unknown')">
                <xsl:attribute name="date">unknown</xsl:attribute>
                <xsl:if test="($timeText = 'unknown')">
                  <xsl:attribute name="time">unknown</xsl:attribute>
                </xsl:if>
              </xsl:when>
              <xsl:when test="($dateText = 'unpublished material')">
                <xsl:attribute name="date">inapplicable</xsl:attribute>
                <xsl:if test="($timeText = 'unknown')">
                  <xsl:attribute name="time">unknown</xsl:attribute>
                </xsl:if>
              </xsl:when>
              <xsl:otherwise>
                <xsl:if test="($timeText = 'unknown')">
                  <xsl:attribute name="time">unknown</xsl:attribute>
                </xsl:if>
                <xsl:if test="($date != '')">
                  <xsl:value-of select="$date" /><xsl:if test="($time != '')">T<xsl:value-of select="$time" /></xsl:if>
                </xsl:if>
              </xsl:otherwise>
            </xsl:choose>
          </pubDate>
        </xsl:if>
      </date>
    </xsl:for-each>

 		<xsl:for-each select="edition">
      <resEd><xsl:value-of select="."/></resEd>
    </xsl:for-each>
		<xsl:for-each select="origin">
			<citRespParty>
        <rpOrgName><xsl:value-of select="."/><xsl:comment> WARNING: translation from FGDC is ambiguous, this may require correction </xsl:comment></rpOrgName>
        <role>
          <RoleCd>
            <xsl:attribute name="value">
              <xsl:call-template name="CI_RoleCode">
                <xsl:with-param name="source">originator</xsl:with-param>
              </xsl:call-template>
            </xsl:attribute>
          </RoleCd>
        </role>
			</citRespParty>
		</xsl:for-each>
		<xsl:for-each select="pubinfo">
			<citRespParty>
        <xsl:for-each select="publish">
          <rpOrgName><xsl:value-of select="."/><xsl:comment> WARNING: translation from FGDC is ambiguous, this may require correction </xsl:comment></rpOrgName>
        </xsl:for-each>
        <xsl:for-each select="pubplace">
          <rpCntInfo>
            <cntAddress>
              <delPoint><xsl:value-of select="."/><xsl:comment> WARNING: translation from FGDC is ambiguous, this may require correction </xsl:comment></delPoint>
            </cntAddress>
          </rpCntInfo>
        </xsl:for-each>
        <role>
          <RoleCd>
            <xsl:attribute name="value">
              <xsl:call-template name="CI_RoleCode">
                <xsl:with-param name="source">publisher</xsl:with-param>
              </xsl:call-template>
            </xsl:attribute>
          </RoleCd>
        </role>
			</citRespParty>
		</xsl:for-each>
		<xsl:for-each select="geoform">
			<presForm>
        <PresFormCd>
          <xsl:attribute name="value">
            <xsl:call-template name="CI_PresentationFormCode">
              <xsl:with-param name="source" select="translate(., $upper, $lower)" />
            </xsl:call-template>
          </xsl:attribute>
        </PresFormCd>
			</presForm>
			<presForm>
        <fgdcGeoform><xsl:value-of select="." /></fgdcGeoform>
			</presForm>
		</xsl:for-each>
		<xsl:if test="(serinfo/* != '')">
			<datasetSeries>
				<xsl:apply-templates select="serinfo" />
			</datasetSeries>
		</xsl:if>
		<xsl:for-each select="othercit">
      <otherCitDet><xsl:value-of select="." /></otherCitDet>
    </xsl:for-each>
    <xsl:for-each select="lworkcit/citeinfo/title">
      <collTitle><xsl:value-of select="."/></collTitle>
    </xsl:for-each>
    <xsl:if test="(local-name(..) != 'citation')">
      <xsl:for-each select="onlink">
        <citOnlineRes>
          <linkage><xsl:value-of select="." /></linkage>
        </citOnlineRes>
      </xsl:for-each>
    </xsl:if>
	</xsl:template>
	
	<!-- Contact Information -->
	<xsl:template match="cntinfo">
		<xsl:param name="role" />
    <xsl:for-each select="cntperp/cntper | cntorgp/cntper">
     <rpIndName><xsl:value-of select="."/></rpIndName>
    </xsl:for-each>
    <xsl:for-each select="cntperp/cntorg | cntorgp/cntorg">
     <rpOrgName><xsl:value-of select="."/></rpOrgName>
    </xsl:for-each>
    <xsl:for-each select="cntpos">
      <rpPosName><xsl:value-of select="."/></rpPosName>
    </xsl:for-each>
		<xsl:if test="(cntaddr/* != '') or (cntvoice != '') or (cntfax != '') or (cntemail != '') or (cnttdd != '')">
			<rpCntInfo>
				<xsl:call-template name="CI_Contact" />
			</rpCntInfo>
		</xsl:if>
    <role>
      <RoleCd>
        <xsl:attribute name="value">
          <xsl:choose>
            <xsl:when test="($role != '')">
              <xsl:call-template name="CI_RoleCode">
                <xsl:with-param name="source" select="translate($role, $upper, $lower)" />
              </xsl:call-template>
            </xsl:when>
            <xsl:otherwise>
              <xsl:call-template name="CI_RoleCode">
                <xsl:with-param name="source">pointofcontact</xsl:with-param>
              </xsl:call-template>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:attribute>
      </RoleCd>
    </role>
	</xsl:template>
	
	<!-- CI_Contact -->
	<xsl:template name="CI_Contact">
		<xsl:if test="(cntvoice != '') or (cntfax != '') or (cnttdd != '')">
      <cntPhone>
        <xsl:call-template name="CI_Telephone" />
      </cntPhone>
		</xsl:if>
		<xsl:if test="(cntaddr/* != '') or (cntemail != '')">
      <cntAddress>
        <xsl:call-template name="CI_Address" />
      </cntAddress>
		</xsl:if>
    <xsl:for-each select="hours">
      <cntHours><xsl:value-of select="."/></cntHours>
    </xsl:for-each>
    <xsl:for-each select="cntinst">
      <cntInstr><xsl:value-of select="."/></cntInstr>
    </xsl:for-each>
	</xsl:template>

	<!-- CI_Address -->
	<xsl:template name="CI_Address">
    <xsl:for-each select="cntaddr/addrtype">
      <xsl:attribute name="addressType">
        <xsl:call-template name="AddressTypeCode">
          <xsl:with-param name="source" select="translate(., $upper, $lower)" />
        </xsl:call-template>
      </xsl:attribute>
    </xsl:for-each>
    <xsl:for-each select="cntaddr/address"><!-- how to keep whitespace? -->
      <delPoint><xsl:value-of select="."/></delPoint>
    </xsl:for-each>
    <xsl:for-each select="cntaddr/city">
      <city><xsl:value-of select="."/></city>
    </xsl:for-each>
    <xsl:for-each select="cntaddr/state">
      <adminArea><xsl:value-of select="."/></adminArea>
    </xsl:for-each>
    <xsl:for-each select="cntaddr/postal">
      <postCode><xsl:value-of select="."/></postCode>
    </xsl:for-each>
    <xsl:for-each select="cntaddr/country">
      <xsl:variable name="upperValue" select="translate(., $lower, $upper)" />
      <xsl:variable name="lowerValue" select="translate(., $upper, $lower)" />
      <country>
        <xsl:apply-templates select="$countries_3166">
          <xsl:with-param name="upper" select="$upperValue" />
          <xsl:with-param name="lower" select="$lowerValue" />
          <xsl:with-param name="original" select="." />
        </xsl:apply-templates>
      </country>
    </xsl:for-each>
    <xsl:for-each select="cntemail">
      <eMailAdd><xsl:value-of select="."/></eMailAdd>
    </xsl:for-each>
	</xsl:template>	

	<!-- CI_OnlineResource -->
	<xsl:template name="CI_OnlineResource">
    <xsl:for-each select="onlink">
      <linkage><xsl:value-of select="."/></linkage>
    </xsl:for-each>
    <xsl:if test="(local-name() = 'networkr')">
      <linkage><xsl:value-of select="."/></linkage>
      <xsl:for-each select="../../../accinstr">
        <orDesc><xsl:value-of select="."/></orDesc>
      </xsl:for-each>
    </xsl:if>
    <xsl:for-each select="metprof">
      <orName><xsl:value-of select="."/></orName>
    </xsl:for-each>
    <xsl:for-each select="accinstr">
      <orDesc><xsl:value-of select="."/></orDesc>
    </xsl:for-each>
	</xsl:template>
	
	<!-- Series Information -->
	<xsl:template match="serinfo">
    <xsl:for-each select="sername">
      <seriesName><xsl:value-of select="."/></seriesName>
    </xsl:for-each>
    <xsl:for-each select="issue">
      <issId><xsl:value-of select="."/></issId>
    </xsl:for-each>
	</xsl:template>
	
	<!-- CI_Telephone -->
	<xsl:template name="CI_Telephone">
    <xsl:for-each select="cntvoice">
      <voiceNum><xsl:value-of select="."/></voiceNum>
    </xsl:for-each>
    <xsl:for-each select="cnttdd">
      <voiceNum tddtty='True'><xsl:value-of select="."/></voiceNum>
    </xsl:for-each>
    <xsl:for-each select="cntfax">
      <faxNum><xsl:value-of select="."/></faxNum>
    </xsl:for-each>
	</xsl:template>


	<!-- TM_Primitive -->
	<xsl:template name="timePeriod">
    <xsl:variable name="begdate">
      <xsl:call-template name="dateHandler">
        <xsl:with-param name="source" select="begdate" /><!-- Domain: "Unknown" free date -->
      </xsl:call-template>
    </xsl:variable>
    <xsl:variable name="begtime">
      <xsl:call-template name="timeHandler">
        <xsl:with-param name="source" select="begtime" /><!-- Domain: "Unknown" free time -->
      </xsl:call-template>
    </xsl:variable>
    <xsl:variable name="enddate">
      <xsl:call-template name="dateHandler">
        <xsl:with-param name="source" select="enddate" /><!-- Domain: "Unknown" "Present" free date -->
      </xsl:call-template>
    </xsl:variable>
    <xsl:variable name="endtime">
      <xsl:call-template name="timeHandler">
        <xsl:with-param name="source" select="endtime" /><!-- Domain: "Unknown" free time -->
      </xsl:call-template>
    </xsl:variable>
    <xsl:variable name="begdateText">
      <xsl:value-of select="translate(begdate, $upper, $lower)" />
    </xsl:variable>
    <xsl:variable name="begtimeText">
      <xsl:value-of select="translate(begtime, $upper, $lower)" />
    </xsl:variable>
    <xsl:variable name="enddateText">
      <xsl:value-of select="translate(enddate, $upper, $lower)" />
    </xsl:variable>
    <xsl:variable name="endtimeText">
      <xsl:value-of select="translate(endtime, $upper, $lower)" />
    </xsl:variable>

    <xsl:if test="(begdate != '') or (begtime != '')">
      <tmBegin>
        <xsl:choose>
          <xsl:when test="($begdateText = 'unknown')">
            <xsl:attribute name="date">unknown</xsl:attribute>
            <xsl:if test="($begtimeText = 'unknown')">
              <xsl:attribute name="time">unknown</xsl:attribute>
            </xsl:if>
          </xsl:when>
          <xsl:otherwise>
            <xsl:if test="($begtimeText = 'unknown')">
              <xsl:attribute name="time">unknown</xsl:attribute>
            </xsl:if>
            <xsl:if test="($begdate != '')">
              <xsl:value-of select="$begdate" /><xsl:if test="($begtime != '')">T<xsl:value-of select="$begtime" /></xsl:if>
            </xsl:if>
          </xsl:otherwise>
        </xsl:choose>
      </tmBegin>
    </xsl:if>
    <xsl:if test="(enddate != '') or (endtime != '')">
      <tmEnd>
        <xsl:choose>
          <xsl:when test="($enddateText = 'unknown')">
            <xsl:attribute name="date">unknown</xsl:attribute>
            <xsl:if test="($endtimeText = 'unknown')">
              <xsl:attribute name="time">unknown</xsl:attribute>
            </xsl:if>
          </xsl:when>
          <xsl:when test="($enddateText = 'present')">
            <xsl:attribute name="date">now</xsl:attribute>
            <xsl:if test="($endtimeText = 'unknown')">
              <xsl:attribute name="time">unknown</xsl:attribute>
            </xsl:if>
          </xsl:when>
          <xsl:otherwise>
            <xsl:if test="($endtimeText = 'unknown')">
              <xsl:attribute name="time">unknown</xsl:attribute>
            </xsl:if>
            <xsl:if test="($enddate != '')">
              <xsl:value-of select="$enddate" /><xsl:if test="($endtime != '')">T<xsl:value-of select="$endtime" /></xsl:if>
            </xsl:if>
          </xsl:otherwise>
        </xsl:choose>
      </tmEnd>
    </xsl:if>
	</xsl:template>

	<xsl:template name="timeInstant">
    <xsl:variable name="date">
      <xsl:call-template name="dateHandler">
        <xsl:with-param name="source" select="caldate" /><!-- Domain: "Unknown" free date -->
      </xsl:call-template>
    </xsl:variable>
    <xsl:variable name="time">
      <xsl:call-template name="timeHandler">
        <xsl:with-param name="source" select="time" /><!-- Domain: "Unknown" free time -->
      </xsl:call-template>
    </xsl:variable>
    <xsl:variable name="dateText">
      <xsl:value-of select="translate(caldate, $upper, $lower)" />
    </xsl:variable>
    <xsl:variable name="timeText">
      <xsl:value-of select="translate(time, $upper, $lower)" />
    </xsl:variable>
    <xsl:for-each select="caldate">
      <xsl:if test="(. != '') or (../time != '')">
        <xsl:choose>
          <xsl:when test="($dateText = 'unknown')">
            <xsl:attribute name="date">unknown</xsl:attribute>
            <xsl:if test="($timeText = 'unknown')">
              <xsl:attribute name="time">unknown</xsl:attribute>
            </xsl:if>
          </xsl:when>
          <xsl:otherwise>
            <xsl:if test="($timeText = 'unknown')">
              <xsl:attribute name="time">unknown</xsl:attribute>
            </xsl:if>
            <xsl:if test="($date != '')">
              <xsl:value-of select="$date" /><xsl:if test="($time != '')">T<xsl:value-of select="$time" /></xsl:if>
            </xsl:if>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:if>
    </xsl:for-each>
	</xsl:template>
		
		<xsl:template name="dateHandler">
      <xsl:param name="source"/>
      <xsl:if test="($source != '') and ((number($source) > 0) or (number($source) &lt; 0))">
        <xsl:variable name="year" select="substring($source, 1, 4)" />
        <xsl:variable name="month" select="substring($source, 5, 2)" />
        <xsl:variable name="day" select="substring($source, 7, 2)" />
  
        <xsl:variable name="isoDate">
          <xsl:if test="(string-length($year) = 4)"><xsl:value-of select="$year" /></xsl:if>
          <xsl:if test="(string-length($month) = 2) and (($month > 0) and ($month &lt; 13))">-<xsl:value-of select="$month" /></xsl:if>
          <xsl:if test="(string-length($day) = 2) and (($day > 0) and ($day &lt; 32))">-<xsl:value-of select="$day" /></xsl:if>
        </xsl:variable>
        <xsl:value-of select="$isoDate" />
      </xsl:if>
    </xsl:template>
	
		<xsl:template name="timeHandler">
      <xsl:param name="source"/>
      <xsl:if test="($source != '') and ((number($source) > 0) or (number($source) &lt; 0))">
        <xsl:variable name="hour" select="substring($source, 1, 2)" />
        <xsl:variable name="minute" select="substring($source, 3, 2)" />
        <xsl:variable name="second" select="substring($source, 5, 2)" />
  
        <xsl:variable name="isoTime">
          <xsl:if test="(string-length($hour) = 2) and (($hour >= 0) and ($hour &lt; 25))"><xsl:value-of select="$hour" /></xsl:if>
          <xsl:if test="(string-length($minute) = 2) and (($minute >= 0) and ($minute &lt; 60))">:<xsl:value-of select="$minute" /></xsl:if>
          <xsl:if test="(string-length($second) = 2) and (($second >= 0) and ($minute &lt; 61))">:<xsl:value-of select="$second" /></xsl:if>
        </xsl:variable>
        <xsl:value-of select="$isoTime" />
      </xsl:if>
    </xsl:template>

	<!-- DOMAIN Conversions below -->

	<!-- FGDC address type -->
	<xsl:template name="AddressTypeCode">
		<xsl:param name="source" />
		<xsl:choose>
			<xsl:when test="$source = 'mailing'"><xsl:text>postal</xsl:text></xsl:when>
			<xsl:when test="$source = 'mailing address'"><xsl:text>postal</xsl:text></xsl:when>
			<xsl:when test="$source = 'mailing and physical'"><xsl:text>both</xsl:text></xsl:when>
			<xsl:when test="$source = 'mailing and physical address'"><xsl:text>both</xsl:text></xsl:when>
			<xsl:when test="$source = 'physical'"><xsl:text>physical</xsl:text></xsl:when>
			<xsl:when test="$source = 'physical address'"><xsl:text>physical</xsl:text></xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="$source"/>
			</xsl:otherwise>
		</xsl:choose>		
	</xsl:template>

	<!-- CI_PresentationFormCode -->
	<xsl:template name="CI_PresentationFormCode">
		<xsl:param name="source" />
		<xsl:choose>
			<xsl:when test="$source = 'documentdigital'"><xsl:text>001</xsl:text></xsl:when>
			<xsl:when test="$source = 'documenthardcopy'"><xsl:text>002</xsl:text></xsl:when>
			<xsl:when test="$source = 'imagedigital'"><xsl:text>003</xsl:text></xsl:when>
			<xsl:when test="$source = 'imagehardcopy'"><xsl:text>004</xsl:text></xsl:when>
			<xsl:when test="$source = 'mapdigital'"><xsl:text>005</xsl:text></xsl:when>
			<xsl:when test="$source = 'maphardcopy'"><xsl:text>006</xsl:text></xsl:when>
			<xsl:when test="$source = 'modeldigital'"><xsl:text>007</xsl:text></xsl:when>
			<xsl:when test="$source = 'modelhardcopy'"><xsl:text>008</xsl:text></xsl:when>
			<xsl:when test="$source = 'profiledigital'"><xsl:text>009</xsl:text></xsl:when>
			<xsl:when test="$source = 'profilehardcopy'"><xsl:text>010</xsl:text></xsl:when>
			<xsl:when test="$source = 'tabledigital'"><xsl:text>011</xsl:text></xsl:when>
			<xsl:when test="$source = 'tablehardcopy'"><xsl:text>012</xsl:text></xsl:when>
			<xsl:when test="$source = 'videodigital'"><xsl:text>013</xsl:text></xsl:when>
			<xsl:when test="$source = 'videohardcopy'"><xsl:text>014</xsl:text></xsl:when>
			<xsl:when test="$source = 'audiodigital'"><xsl:text>015</xsl:text></xsl:when>
			<xsl:when test="$source = 'audiohardcopy'"><xsl:text>016</xsl:text></xsl:when>
			<xsl:when test="$source = 'multimediadigital'"><xsl:text>017</xsl:text></xsl:when>
			<xsl:when test="$source = 'multimediahardcopy'"><xsl:text>018</xsl:text></xsl:when>
			<xsl:when test="$source = 'diagramdigital'"><xsl:text>019</xsl:text></xsl:when>
			<xsl:when test="$source = 'diagramhardcopy'"><xsl:text>020</xsl:text></xsl:when>
			<!-- fgdc geoform -->
			<xsl:when test="$source = 'atlas'"><xsl:text>006</xsl:text></xsl:when>
			<xsl:when test="$source = 'audio'"><xsl:text>016</xsl:text></xsl:when>
			<xsl:when test="$source = 'diagram'"><xsl:text>002</xsl:text></xsl:when>
			<xsl:when test="$source = 'document'"><xsl:text>002</xsl:text></xsl:when>
			<xsl:when test="$source = 'globe'"><xsl:text>006</xsl:text></xsl:when>
			<xsl:when test="$source = 'map'"><xsl:text>006</xsl:text></xsl:when>
			<xsl:when test="$source = 'model'"><xsl:text>008</xsl:text></xsl:when>
			<xsl:when test="$source = 'multimedia presentation'"><xsl:text>017</xsl:text></xsl:when>
			<xsl:when test="$source = 'profile'"><xsl:text>010</xsl:text></xsl:when>
			<xsl:when test="$source = 'raster digital data'"><xsl:text>005</xsl:text></xsl:when>
			<xsl:when test="$source = 'remote-sensing image'"><xsl:text>005</xsl:text></xsl:when>
			<xsl:when test="$source = 'spreadsheet'"><xsl:text>011</xsl:text></xsl:when>
			<xsl:when test="$source = 'tabular digital data'"><xsl:text>011</xsl:text></xsl:when>
			<xsl:when test="$source = 'vector digital data'"><xsl:text>005</xsl:text></xsl:when>
			<xsl:when test="$source = 'video'"><xsl:text>014</xsl:text></xsl:when>
			<!-- iso codes as text -->
			<xsl:when test="$source = 'document digital'"><xsl:text>001</xsl:text></xsl:when>
			<xsl:when test="$source = 'document hardcopy'"><xsl:text>002</xsl:text></xsl:when>
			<xsl:when test="$source = 'image digital'"><xsl:text>003</xsl:text></xsl:when>
			<xsl:when test="$source = 'image hardcopy'"><xsl:text>004</xsl:text></xsl:when>
			<xsl:when test="$source = 'map digital'"><xsl:text>005</xsl:text></xsl:when>
			<xsl:when test="$source = 'map hardcopy'"><xsl:text>006</xsl:text></xsl:when>
			<xsl:when test="$source = 'model digital'"><xsl:text>007</xsl:text></xsl:when>
			<xsl:when test="$source = 'model hardcopy'"><xsl:text>008</xsl:text></xsl:when>
			<xsl:when test="$source = 'profile digital'"><xsl:text>009</xsl:text></xsl:when>
			<xsl:when test="$source = 'profile hardcopy'"><xsl:text>010</xsl:text></xsl:when>
			<xsl:when test="$source = 'table digital'"><xsl:text>011</xsl:text></xsl:when>
			<xsl:when test="$source = 'table hardcopy'"><xsl:text>012</xsl:text></xsl:when>
			<xsl:when test="$source = 'video digital'"><xsl:text>013</xsl:text></xsl:when>
			<xsl:when test="$source = 'video hardcopy'"><xsl:text>014</xsl:text></xsl:when>
			<xsl:when test="$source = 'audio digital'"><xsl:text>015</xsl:text></xsl:when>
			<xsl:when test="$source = 'audio hardcopy'"><xsl:text>016</xsl:text></xsl:when>
			<xsl:when test="$source = 'multimedia digital'"><xsl:text>017</xsl:text></xsl:when>
			<xsl:when test="$source = 'multimedia hardcopy'"><xsl:text>018</xsl:text></xsl:when>
			<xsl:when test="$source = 'diagram digital'"><xsl:text>019</xsl:text></xsl:when>
			<xsl:when test="$source = 'diagram hardcopy'"><xsl:text>020</xsl:text></xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="$source"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<!-- CI_RoleCode -->
	<xsl:template name="CI_RoleCode">
		<xsl:param name="source" />
		<xsl:choose>
			<xsl:when test="$source = 'resourceprovider'"><xsl:text>001</xsl:text></xsl:when>
			<xsl:when test="$source = 'custodian'"><xsl:text>002</xsl:text></xsl:when>
			<xsl:when test="$source = 'owner'"><xsl:text>003</xsl:text></xsl:when>
			<xsl:when test="$source = 'user'"><xsl:text>004</xsl:text></xsl:when>
			<xsl:when test="$source = 'distributor'"><xsl:text>005</xsl:text></xsl:when>
			<xsl:when test="$source = 'originator'"><xsl:text>006</xsl:text></xsl:when>
			<xsl:when test="$source = 'pointofcontact'"><xsl:text>007</xsl:text></xsl:when>
			<xsl:when test="$source = 'principalinvestigator'"><xsl:text>008</xsl:text></xsl:when>
			<xsl:when test="$source = 'processor'"><xsl:text>009</xsl:text></xsl:when>
			<xsl:when test="$source = 'publisher'"><xsl:text>010</xsl:text></xsl:when>
			<xsl:when test="$source = 'author'"><xsl:text>011</xsl:text></xsl:when>
			<xsl:when test="$source = 'collaborator'"><xsl:text>012</xsl:text></xsl:when>
			<xsl:when test="$source = 'editor'"><xsl:text>013</xsl:text></xsl:when>
			<xsl:when test="$source = 'mediator'"><xsl:text>014</xsl:text></xsl:when>
			<xsl:when test="$source = 'rightsholder'"><xsl:text>015</xsl:text></xsl:when>
			<xsl:when test="$source = 'resource provider'"><xsl:text>001</xsl:text></xsl:when>
			<xsl:when test="$source = 'point of contact'"><xsl:text>007</xsl:text></xsl:when>
			<xsl:when test="$source = 'principal investigator'"><xsl:text>008</xsl:text></xsl:when>
			<xsl:when test="$source = 'rights holder'"><xsl:text>015</xsl:text></xsl:when>
			<xsl:when test="$source = 'rights-holder'"><xsl:text>015</xsl:text></xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="$source"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<!-- DS_AssociationTypeCode -->
	<xsl:template name="DS_AssociationTypeCode">
		<xsl:param name="source" />
		<xsl:choose>
			<xsl:when test="$source = 'crossReference'"><xsl:text>001</xsl:text></xsl:when>
			<xsl:when test="$source = 'largerWorkCitation'"><xsl:text>002</xsl:text></xsl:when>
			<xsl:when test="$source = 'partOfSeamlessDatabase'"><xsl:text>003</xsl:text></xsl:when>
			<xsl:when test="$source = 'source'"><xsl:text>004</xsl:text></xsl:when>
			<xsl:when test="$source = 'stereoMate'"><xsl:text>005</xsl:text></xsl:when>
			<xsl:when test="$source = 'isComposedOf'"><xsl:text>006</xsl:text></xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="$source"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<!-- MD_CellGeometryCode -->
	<xsl:template name="MD_CellGeometryCode">
		<xsl:param name="source" />
		<xsl:choose>
			<xsl:when test="$source = 'point'"><xsl:text>001</xsl:text></xsl:when>
			<xsl:when test="$source = 'area'"><xsl:text>002</xsl:text></xsl:when>
			<xsl:when test="$source = 'voxel'"><xsl:text>003</xsl:text></xsl:when>
			<!-- fgdc raster object type -->
			<xsl:when test="$source = 'pixel'"><xsl:text>002</xsl:text></xsl:when>
			<xsl:when test="$source = 'grid cell'"><xsl:text>002</xsl:text></xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="$source"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<!-- MD_CharacterSetCode -->
	<xsl:template name="MD_CharacterSetCode">
		<xsl:param name="source" />
		<xsl:choose>
			<xsl:when test="$source = 'ucs2'"><xsl:text>001</xsl:text></xsl:when>
			<xsl:when test="$source = 'ucs4'"><xsl:text>002</xsl:text></xsl:when>
			<xsl:when test="$source = 'utf7'"><xsl:text>003</xsl:text></xsl:when>
			<xsl:when test="$source = 'utf8'"><xsl:text>004</xsl:text></xsl:when>
			<xsl:when test="$source = 'utf16'"><xsl:text>005</xsl:text></xsl:when>
			<xsl:when test="$source = '8859part1'"><xsl:text>006</xsl:text></xsl:when>
			<xsl:when test="$source = '8859part2'"><xsl:text>007</xsl:text></xsl:when>
			<xsl:when test="$source = '8859part3'"><xsl:text>008</xsl:text></xsl:when>
			<xsl:when test="$source = '8859part4'"><xsl:text>009</xsl:text></xsl:when>
			<xsl:when test="$source = '8859part5'"><xsl:text>010</xsl:text></xsl:when>
			<xsl:when test="$source = '8859part6'"><xsl:text>011</xsl:text></xsl:when>
			<xsl:when test="$source = '8859part7'"><xsl:text>012</xsl:text></xsl:when>
			<xsl:when test="$source = '8859part8'"><xsl:text>013</xsl:text></xsl:when>
			<xsl:when test="$source = '8859part9'"><xsl:text>014</xsl:text></xsl:when>
			<xsl:when test="$source = '8859part10'"><xsl:text>015</xsl:text></xsl:when>
			<xsl:when test="$source = '8859part11'"><xsl:text>016</xsl:text></xsl:when>
			<xsl:when test="$source = '8859part13'"><xsl:text>018</xsl:text></xsl:when>
			<xsl:when test="$source = '8859part14'"><xsl:text>019</xsl:text></xsl:when>
			<xsl:when test="$source = '8859part15'"><xsl:text>020</xsl:text></xsl:when>
			<xsl:when test="$source = '8859part16'"><xsl:text>021</xsl:text></xsl:when>
			<xsl:when test="$source = 'jis'"><xsl:text>022</xsl:text></xsl:when>
			<xsl:when test="$source = 'shiftJIS'"><xsl:text>023</xsl:text></xsl:when>
			<xsl:when test="$source = 'eucJP'"><xsl:text>024</xsl:text></xsl:when>
			<xsl:when test="$source = 'usAscii'"><xsl:text>025</xsl:text></xsl:when>
			<xsl:when test="$source = 'ebcdic'"><xsl:text>026</xsl:text></xsl:when>
			<xsl:when test="$source = 'eucKR'"><xsl:text>027</xsl:text></xsl:when>
			<xsl:when test="$source = 'big5'"><xsl:text>028</xsl:text></xsl:when>
			<xsl:when test="$source = 'GB2312'"><xsl:text>029</xsl:text></xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="$source"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	
	<!-- MD_ClassificationCode -->
	<xsl:template name="MD_ClassificationCode">
		<xsl:param name="source" />
		<xsl:choose>
			<xsl:when test="$source = 'unclassified'"><xsl:text>001</xsl:text></xsl:when>
			<xsl:when test="$source = 'restricted'"><xsl:text>002</xsl:text></xsl:when>
			<xsl:when test="$source = 'confidential'"><xsl:text>003</xsl:text></xsl:when>
			<xsl:when test="$source = 'secret'"><xsl:text>004</xsl:text></xsl:when>
			<xsl:when test="$source = 'topsecret'"><xsl:text>005</xsl:text></xsl:when>
			<xsl:when test="$source = 'sensitive'"><xsl:text>006</xsl:text></xsl:when>
			<xsl:when test="$source = 'forofficialuseonly'"><xsl:text>007</xsl:text></xsl:when>
			<!-- fgdc classification codes -->
			<xsl:when test="$source = 'top secret'"><xsl:text>005</xsl:text></xsl:when>
			<xsl:when test="$source = 'sensitive'"><xsl:text>003</xsl:text></xsl:when>
			<xsl:when test="$source = 'for official use only'"><xsl:text>007</xsl:text></xsl:when>
			<xsl:when test="$source = 'official use only'"><xsl:text>007</xsl:text></xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="$source"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	
	<!-- MD_CoverageContentTypeCode -->
	<xsl:template name="MD_CoverageContentTypeCode">
		<xsl:param name="source" />
		<xsl:choose>
			<xsl:when test="$source = 'image'"><xsl:text>001</xsl:text></xsl:when>
			<xsl:when test="$source = 'thematicClassification'"><xsl:text>002</xsl:text></xsl:when>
			<xsl:when test="$source = 'physicalMeasurement'"><xsl:text>003</xsl:text></xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="$source"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	
	<!-- MD_DatatypeCode -->
	<xsl:template name="MD_DatatypeCode">
		<xsl:param name="source" />
		<xsl:choose>
			<xsl:when test="$source = 'class'"><xsl:text>001</xsl:text></xsl:when>
			<xsl:when test="$source = 'codelist'"><xsl:text>002</xsl:text></xsl:when>
			<xsl:when test="$source = 'enumeration'"><xsl:text>003</xsl:text></xsl:when>
			<xsl:when test="$source = 'codelistElement'"><xsl:text>004</xsl:text></xsl:when>
			<xsl:when test="$source = 'abstractClass'"><xsl:text>005</xsl:text></xsl:when>
			<xsl:when test="$source = 'aggregateClass'"><xsl:text>006</xsl:text></xsl:when>
			<xsl:when test="$source = 'specifiedClass'"><xsl:text>007</xsl:text></xsl:when>
			<xsl:when test="$source = 'datatypeClass'"><xsl:text>008</xsl:text></xsl:when>
			<xsl:when test="$source = 'interfaceClass'"><xsl:text>009</xsl:text></xsl:when>
			<xsl:when test="$source = 'unionClass'"><xsl:text>010</xsl:text></xsl:when>
			<xsl:when test="$source = 'metaClass'"><xsl:text>011</xsl:text></xsl:when>
			<xsl:when test="$source = 'typeClass'"><xsl:text>012</xsl:text></xsl:when>
			<xsl:when test="$source = 'characterString'"><xsl:text>013</xsl:text></xsl:when>
			<xsl:when test="$source = 'integer'"><xsl:text>014</xsl:text></xsl:when>
			<xsl:when test="$source = 'association'"><xsl:text>015</xsl:text></xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="$source"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<!-- MD_DimensionNameTypeCode -->
	<xsl:template name="MD_DimensionNameTypeCode">
		<xsl:param name="source" />
		<xsl:choose>
			<xsl:when test="$source = 'row'"><xsl:text>001</xsl:text></xsl:when>
			<xsl:when test="$source = 'column'"><xsl:text>002</xsl:text></xsl:when>
			<xsl:when test="$source = 'vertical'"><xsl:text>003</xsl:text></xsl:when>
			<xsl:when test="$source = 'track'"><xsl:text>004</xsl:text></xsl:when>
			<xsl:when test="$source = 'crossTrack'"><xsl:text>005</xsl:text></xsl:when>
			<xsl:when test="$source = 'line'"><xsl:text>006</xsl:text></xsl:when>
			<xsl:when test="$source = 'sample'"><xsl:text>007</xsl:text></xsl:when>
			<xsl:when test="$source = 'time'"><xsl:text>008</xsl:text></xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="$source"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<!-- MD_GeometricObjectTypeCode -->
	<xsl:template name="MD_GeometricObjectTypeCode">
		<xsl:param name="source" />
		<xsl:choose>
			<xsl:when test="$source = 'complex'"><xsl:text>001</xsl:text></xsl:when>
			<xsl:when test="$source = 'composite'"><xsl:text>002</xsl:text></xsl:when>
			<xsl:when test="$source = 'curve'"><xsl:text>003</xsl:text></xsl:when>
			<xsl:when test="$source = 'point'"><xsl:text>004</xsl:text></xsl:when>
			<xsl:when test="$source = 'solid'"><xsl:text>005</xsl:text></xsl:when>
			<xsl:when test="$source = 'surface'"><xsl:text>006</xsl:text></xsl:when>
			<!-- vpf geometry type terms -->
			<xsl:when test="$source = 'face'"><xsl:text>001</xsl:text></xsl:when>
			<xsl:when test="$source = 'edge'"><xsl:text>002</xsl:text></xsl:when>
			<xsl:when test="$source = 'node'"><xsl:text>004</xsl:text></xsl:when>
			<!-- sdts geometry type terms -->
			<xsl:when test="$source = 'entity point'"><xsl:text>004</xsl:text></xsl:when>
			<xsl:when test="$source = 'label point'"><xsl:text>004</xsl:text></xsl:when>
			<xsl:when test="$source = 'area point'"><xsl:text>004</xsl:text></xsl:when>
			<xsl:when test="$source = 'node, planar graph'"><xsl:text>004</xsl:text></xsl:when>
			<xsl:when test="$source = 'node, network'"><xsl:text>004</xsl:text></xsl:when>
			<xsl:when test="$source = 'string'"><xsl:text>002</xsl:text></xsl:when>
			<xsl:when test="$source = 'link'"><xsl:text>003</xsl:text></xsl:when>
			<xsl:when test="$source = 'complete chain'"><xsl:text>002</xsl:text></xsl:when>
			<xsl:when test="$source = 'area chain'"><xsl:text>001</xsl:text></xsl:when>
			<xsl:when test="$source = 'network chain, planar graph'"><xsl:text>002</xsl:text></xsl:when>
			<xsl:when test="$source = 'network chain, nonplanar graph'"><xsl:text>002</xsl:text></xsl:when>
			<xsl:when test="$source = 'circular arc, three point center'"><xsl:text>003</xsl:text></xsl:when>
			<xsl:when test="$source = 'elliptical arc'"><xsl:text>003</xsl:text></xsl:when>
			<xsl:when test="$source = 'uniform b-spline'"><xsl:text>003</xsl:text></xsl:when>
			<xsl:when test="$source = 'piecewise bezier'"><xsl:text>003</xsl:text></xsl:when>
			<xsl:when test="$source = 'ring with mixed composition'"><xsl:text>001</xsl:text></xsl:when>
			<xsl:when test="$source = 'ring composed of strings'"><xsl:text>001</xsl:text></xsl:when>
			<xsl:when test="$source = 'ring composed of chains'"><xsl:text>001</xsl:text></xsl:when>
			<xsl:when test="$source = 'ring composed of arcs'"><xsl:text>001</xsl:text></xsl:when>
			<xsl:when test="$source = 'g-polygon'"><xsl:text>001</xsl:text></xsl:when>
			<xsl:when test="$source = 'gt-polygon composed of rings'"><xsl:text>001</xsl:text></xsl:when>
			<xsl:when test="$source = 'gt-polygon composed of chains'"><xsl:text>001</xsl:text></xsl:when>
			<xsl:when test="$source = 'universe polygon composed of rings'"><xsl:text>001</xsl:text></xsl:when>
			<xsl:when test="$source = 'universe polygon composed of chains'"><xsl:text>001</xsl:text></xsl:when>
			<xsl:when test="$source = 'void polygon composed of rings'"><xsl:text>001</xsl:text></xsl:when>
			<xsl:when test="$source = 'void polygon composed of chains'"><xsl:text>001</xsl:text></xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="$source"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<!-- MD_ImagingConditionCode -->
	<xsl:template name="MD_ImagingConditionCode">
		<xsl:param name="source" />
		<xsl:choose>
			<xsl:when test="$source = 'blurredImage'"><xsl:text>001</xsl:text></xsl:when>
			<xsl:when test="$source = 'cloud'"><xsl:text>002</xsl:text></xsl:when>
			<xsl:when test="$source = 'degradingobliquity'"><xsl:text>003</xsl:text></xsl:when>
			<xsl:when test="$source = 'fog'"><xsl:text>004</xsl:text></xsl:when>
			<xsl:when test="$source = 'heavySmokeOrDust'"><xsl:text>005</xsl:text></xsl:when>
			<xsl:when test="$source = 'night'"><xsl:text>006</xsl:text></xsl:when>
			<xsl:when test="$source = 'rain'"><xsl:text>007</xsl:text></xsl:when>
			<xsl:when test="$source = 'semiDarkness'"><xsl:text>008</xsl:text></xsl:when>
			<xsl:when test="$source = 'shadow'"><xsl:text>009</xsl:text></xsl:when>
			<xsl:when test="$source = 'snow'"><xsl:text>010</xsl:text></xsl:when>
			<xsl:when test="$source = 'terrainMasking'"><xsl:text>011</xsl:text></xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="$source"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<!-- MD_MaintenanceFrequencyCode -->
	<xsl:template name="MD_MaintenanceFrequencyCode">
		<xsl:param name="source" />
		<xsl:choose>
			<xsl:when test="$source = 'continual'"><xsl:text>001</xsl:text></xsl:when>
			<xsl:when test="$source = 'daily'"><xsl:text>002</xsl:text></xsl:when>
			<xsl:when test="$source = 'weekly'"><xsl:text>003</xsl:text></xsl:when>
			<xsl:when test="$source = 'fortnightly'"><xsl:text>004</xsl:text></xsl:when>
			<xsl:when test="$source = 'monthly'"><xsl:text>005</xsl:text></xsl:when>
			<xsl:when test="$source = 'biannually'"><xsl:text>007</xsl:text></xsl:when>
			<xsl:when test="$source = 'annually'"><xsl:text>008</xsl:text></xsl:when>
			<xsl:when test="$source = 'asneeded'"><xsl:text>009</xsl:text></xsl:when>
			<xsl:when test="$source = 'irregular'"><xsl:text>010</xsl:text></xsl:when>
			<xsl:when test="$source = 'notplanned'"><xsl:text>011</xsl:text></xsl:when>
			<xsl:when test="$source = 'unknown'"><xsl:text>012</xsl:text></xsl:when>
			<xsl:when test="$source = 'semimonthly'"><xsl:text>013</xsl:text></xsl:when>
			<!-- fgdc update frequency -->
			<xsl:when test="$source = 'continually'"><xsl:text>001</xsl:text></xsl:when>
			<xsl:when test="$source = 'yearly'"><xsl:text>008</xsl:text></xsl:when>
			<xsl:when test="$source = 'as needed'"><xsl:text>009</xsl:text></xsl:when>
			<xsl:when test="$source = 'as necessary'"><xsl:text>009</xsl:text></xsl:when>
			<xsl:when test="$source = 'not regular'"><xsl:text>010</xsl:text></xsl:when>
			<xsl:when test="$source = 'not regularly'"><xsl:text>010</xsl:text></xsl:when>
			<xsl:when test="$source = 'none planned'"><xsl:text>011</xsl:text></xsl:when>
			<xsl:when test="$source = 'not planned'"><xsl:text>011</xsl:text></xsl:when>
			<xsl:when test="$source = 'unplanned'"><xsl:text>011</xsl:text></xsl:when>
			<xsl:when test="$source = 'biweekly'"><xsl:text>004</xsl:text></xsl:when>
			<xsl:when test="$source = 'bi-weekly'"><xsl:text>004</xsl:text></xsl:when>
			<xsl:when test="$source = 'bi weekly'"><xsl:text>004</xsl:text></xsl:when>
			<xsl:when test="$source = 'quarterly'"><xsl:text>006</xsl:text></xsl:when>
			<xsl:when test="$source = 'quartely'"><xsl:text>006</xsl:text></xsl:when>
			<xsl:when test="$source = 'semi-annually'"><xsl:text>007</xsl:text></xsl:when>
			<xsl:when test="$source = 'semiannually'"><xsl:text>007</xsl:text></xsl:when>
			<xsl:when test="$source = 'semi annually'"><xsl:text>007</xsl:text></xsl:when>
			<xsl:when test="$source = 'bimonthly'"><xsl:text>013</xsl:text></xsl:when>
			<xsl:when test="$source = 'bi-monthly'"><xsl:text>013</xsl:text></xsl:when>
			<xsl:when test="$source = 'semi-monthly'"><xsl:text>013</xsl:text></xsl:when>
			<xsl:when test="$source = 'semi monthly'"><xsl:text>013</xsl:text></xsl:when>
			<xsl:when test="$source = 'not known'"><xsl:text>012</xsl:text></xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="$source"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<!-- MD_MediumFormatCode -->
	<xsl:template name="MD_MediumFormatCode">
		<xsl:param name="source" />
		<xsl:choose>
			<xsl:when test="$source = 'cpio'"><xsl:text>001</xsl:text></xsl:when>
			<xsl:when test="$source = 'tar'"><xsl:text>002</xsl:text></xsl:when>
			<xsl:when test="$source = 'highsierra'"><xsl:text>003</xsl:text></xsl:when>
			<xsl:when test="$source = 'iso9660'"><xsl:text>004</xsl:text></xsl:when>
			<xsl:when test="$source = 'iso9660rockridge'"><xsl:text>005</xsl:text></xsl:when>
			<xsl:when test="$source = 'iso9660applehfs'"><xsl:text>006</xsl:text></xsl:when>
			<xsl:when test="$source = 'udf'"><xsl:text>007</xsl:text></xsl:when>
			<!-- fgdc medium format -->
			<xsl:when test="$source = 'high sierra'"><xsl:text>003</xsl:text></xsl:when>
			<xsl:when test="$source = 'iso 9660'"><xsl:text>004</xsl:text></xsl:when>
			<xsl:when test="$source = 'iso-9660'"><xsl:text>004</xsl:text></xsl:when>
			<xsl:when test="$source = 'iso9660 rock ridge'"><xsl:text>005</xsl:text></xsl:when>
			<xsl:when test="$source = 'iso 9660 rock ridge'"><xsl:text>005</xsl:text></xsl:when>
			<xsl:when test="$source = 'rock ridge'"><xsl:text>005</xsl:text></xsl:when>
			<xsl:when test="$source = 'unix'"><xsl:text>005</xsl:text></xsl:when>
			<xsl:when test="$source = 'iso9660 apple hfs'"><xsl:text>006</xsl:text></xsl:when>
			<xsl:when test="$source = 'iso 9660 apple hfs'"><xsl:text>006</xsl:text></xsl:when>
			<xsl:when test="$source = 'apple hfs'"><xsl:text>006</xsl:text></xsl:when>
			<xsl:when test="$source = 'macintosh'"><xsl:text>006</xsl:text></xsl:when>
			<xsl:when test="$source = 'mac'"><xsl:text>006</xsl:text></xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="$source"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<!-- MD_MediumNameCode -->
	<xsl:template name="MD_MediumNameCode">
		<xsl:param name="source" />
		<xsl:choose>
			<xsl:when test="$source = 'cdrom'"><xsl:text>001</xsl:text></xsl:when>
			<xsl:when test="$source = 'dvd'"><xsl:text>002</xsl:text></xsl:when>
			<xsl:when test="$source = 'dvdrom'"><xsl:text>003</xsl:text></xsl:when>
			<xsl:when test="$source = '3halfinchfloppy'"><xsl:text>004</xsl:text></xsl:when>
			<xsl:when test="$source = '5quarterinchfloppy'"><xsl:text>005</xsl:text></xsl:when>
			<xsl:when test="$source = '7tracktape'"><xsl:text>006</xsl:text></xsl:when>
			<xsl:when test="$source = '9tracktape'"><xsl:text>007</xsl:text></xsl:when>
			<xsl:when test="$source = '3480cartridge'"><xsl:text>008</xsl:text></xsl:when>
			<xsl:when test="$source = '3490cartridge'"><xsl:text>009</xsl:text></xsl:when>
			<xsl:when test="$source = '3580cartridge'"><xsl:text>010</xsl:text></xsl:when>
			<xsl:when test="$source = '4mmcartridgetape'"><xsl:text>011</xsl:text></xsl:when>
			<xsl:when test="$source = '8mmcartridgetape'"><xsl:text>012</xsl:text></xsl:when>
			<xsl:when test="$source = '1quarterinchcartridgetape'"><xsl:text>013</xsl:text></xsl:when>
			<xsl:when test="$source = 'digitallineartape'"><xsl:text>014</xsl:text></xsl:when>
			<xsl:when test="$source = 'online'"><xsl:text>015</xsl:text></xsl:when>
			<xsl:when test="$source = 'satellite'"><xsl:text>016</xsl:text></xsl:when>
			<xsl:when test="$source = 'telephonelink'"><xsl:text>017</xsl:text></xsl:when>
			<xsl:when test="$source = 'hardcopy'"><xsl:text>018</xsl:text></xsl:when>
			<xsl:when test="$source = 'hardcopydiazopolyester08'"><xsl:text>019</xsl:text></xsl:when>
			<xsl:when test="$source = 'hardcopycardmicrofilm'"><xsl:text>020</xsl:text></xsl:when>
			<xsl:when test="$source = 'hardcopymicrofilm240'"><xsl:text>021</xsl:text></xsl:when>
			<xsl:when test="$source = 'hardcopymicrofilm35'"><xsl:text>022</xsl:text></xsl:when>
			<xsl:when test="$source = 'hardcopymicrofilm70'"><xsl:text>023</xsl:text></xsl:when>
			<xsl:when test="$source = 'hardcopymicrofilmgeneral'"><xsl:text>024</xsl:text></xsl:when>
			<xsl:when test="$source = 'hardcopymicrofilmmicrofiche'"><xsl:text>025</xsl:text></xsl:when>
			<xsl:when test="$source = 'hardcopynegativephoto'"><xsl:text>026</xsl:text></xsl:when>
			<xsl:when test="$source = 'hardcopypaper'"><xsl:text>027</xsl:text></xsl:when>
			<xsl:when test="$source = 'hardcopydiazo'"><xsl:text>028</xsl:text></xsl:when>
			<xsl:when test="$source = 'hardcopyphoto'"><xsl:text>029</xsl:text></xsl:when>
			<xsl:when test="$source = 'hardcopytracedpaper'"><xsl:text>030</xsl:text></xsl:when>
			<xsl:when test="$source = 'harddisk'"><xsl:text>031</xsl:text></xsl:when>
			<xsl:when test="$source = 'usbflashdrive'"><xsl:text>032</xsl:text></xsl:when>
			<!-- fgdc offline media -->
			<xsl:when test="$source = 'cd-rom'"><xsl:text>001</xsl:text></xsl:when>
			<xsl:when test="$source = 'cd rom'"><xsl:text>001</xsl:text></xsl:when>
			<xsl:when test="$source = 'cd'"><xsl:text>001</xsl:text></xsl:when>
			<xsl:when test="$source = 'dvd-rom'"><xsl:text>003</xsl:text></xsl:when>
			<xsl:when test="$source = 'dvd rom'"><xsl:text>003</xsl:text></xsl:when>
			<xsl:when test="$source = 'dvd'"><xsl:text>002</xsl:text></xsl:when>
			<xsl:when test="$source = '3 half inch floppy'"><xsl:text>004</xsl:text></xsl:when>
			<xsl:when test="$source = '3 half inch floppy disk'"><xsl:text>004</xsl:text></xsl:when>
			<xsl:when test="$source = '3-1/2 inch floppy'"><xsl:text>004</xsl:text></xsl:when>
			<xsl:when test="$source = '3-1/2 inch floppy disk'"><xsl:text>004</xsl:text></xsl:when>
			<xsl:when test="$source = '3 1/2 inch floppy'"><xsl:text>004</xsl:text></xsl:when>
			<xsl:when test="$source = '3 1/2 inch floppy disk'"><xsl:text>004</xsl:text></xsl:when>
			<xsl:when test="$source = '5 quarter inch floppy'"><xsl:text>005</xsl:text></xsl:when>
			<xsl:when test="$source = '5 quarter inch floppy disk'"><xsl:text>005</xsl:text></xsl:when>
			<xsl:when test="$source = '5-1/4 inch floppy'"><xsl:text>005</xsl:text></xsl:when>
			<xsl:when test="$source = '5-1/4 inch floppy disk'"><xsl:text>005</xsl:text></xsl:when>
			<xsl:when test="$source = '5 1/4 inch floppy'"><xsl:text>005</xsl:text></xsl:when>
			<xsl:when test="$source = '5 1/4 inch floppy disk'"><xsl:text>005</xsl:text></xsl:when>
			<xsl:when test="$source = '7 track tape'"><xsl:text>006</xsl:text></xsl:when>
			<xsl:when test="$source = '7-track tape'"><xsl:text>006</xsl:text></xsl:when>
			<xsl:when test="$source = '9 track tape'"><xsl:text>007</xsl:text></xsl:when>
			<xsl:when test="$source = '9-track tape'"><xsl:text>007</xsl:text></xsl:when>
			<xsl:when test="$source = '3480 cartridge'"><xsl:text>008</xsl:text></xsl:when>
			<xsl:when test="$source = '3490 cartridge'"><xsl:text>009</xsl:text></xsl:when>
			<xsl:when test="$source = '3580 cartridge'"><xsl:text>010</xsl:text></xsl:when>
			<xsl:when test="$source = '4mm cartridge tape'"><xsl:text>011</xsl:text></xsl:when>
			<xsl:when test="$source = '4 mm cartridge tape'"><xsl:text>011</xsl:text></xsl:when>
			<xsl:when test="$source = 'cartridge tape'"><xsl:text>011</xsl:text></xsl:when>
			<xsl:when test="$source = '8mm cartridge tape'"><xsl:text>012</xsl:text></xsl:when>
			<xsl:when test="$source = '8 mm cartridge tape'"><xsl:text>012</xsl:text></xsl:when>
			<xsl:when test="$source = '1 quarter inch cartridge tape'"><xsl:text>013</xsl:text></xsl:when>
			<xsl:when test="$source = '1/4 inch cartridge tape'"><xsl:text>013</xsl:text></xsl:when>
			<xsl:when test="$source = '1/4-inch cartridge tape'"><xsl:text>013</xsl:text></xsl:when>
			<xsl:when test="$source = 'digital linear tape'"><xsl:text>014</xsl:text></xsl:when>
			<xsl:when test="$source = 'magnetic tape'"><xsl:text>014</xsl:text></xsl:when>
			<xsl:when test="$source = 'on line'"><xsl:text>015</xsl:text></xsl:when>
			<xsl:when test="$source = 'computer program'"><xsl:text>015</xsl:text></xsl:when>
			<xsl:when test="$source = 'electronic bulletin board'"><xsl:text>015</xsl:text></xsl:when>
			<xsl:when test="$source = 'electronic mail system'"><xsl:text>015</xsl:text></xsl:when>
			<xsl:when test="$source = 'email'"><xsl:text>015</xsl:text></xsl:when>
			<xsl:when test="$source = 'satellite'"><xsl:text>016</xsl:text></xsl:when>
			<xsl:when test="$source = 'telephone'"><xsl:text>017</xsl:text></xsl:when>
			<xsl:when test="$source = 'telephone link'"><xsl:text>017</xsl:text></xsl:when>
			<xsl:when test="$source = 'hard copy'"><xsl:text>018</xsl:text></xsl:when>
			<xsl:when test="$source = 'hardcopy'"><xsl:text>018</xsl:text></xsl:when>
			<xsl:when test="$source = 'chart'"><xsl:text>018</xsl:text></xsl:when>
			<xsl:when test="$source = 'physical model'"><xsl:text>018</xsl:text></xsl:when>
			<xsl:when test="$source = 'hardcopy diazo polyester 08'"><xsl:text>019</xsl:text></xsl:when>
			<xsl:when test="$source = 'polyester 08'"><xsl:text>019</xsl:text></xsl:when>
			<xsl:when test="$source = 'polyester'"><xsl:text>019</xsl:text></xsl:when>
			<xsl:when test="$source = 'hardcopy card microfilm'"><xsl:text>020</xsl:text></xsl:when>
			<xsl:when test="$source = 'card microfilm'"><xsl:text>020</xsl:text></xsl:when>
			<xsl:when test="$source = 'hardcopy microfilm 240'"><xsl:text>021</xsl:text></xsl:when>
			<xsl:when test="$source = 'microfilm 240'"><xsl:text>021</xsl:text></xsl:when>
			<xsl:when test="$source = 'microfilm 240mm'"><xsl:text>021</xsl:text></xsl:when>
			<xsl:when test="$source = 'microfilm 240 mm'"><xsl:text>021</xsl:text></xsl:when>
			<xsl:when test="$source = '240mm microfilm'"><xsl:text>021</xsl:text></xsl:when>
			<xsl:when test="$source = '240 mm microfilm'"><xsl:text>021</xsl:text></xsl:when>
			<xsl:when test="$source = 'hardcopy microfilm 35'"><xsl:text>022</xsl:text></xsl:when>
			<xsl:when test="$source = 'microfilm 35'"><xsl:text>022</xsl:text></xsl:when>
			<xsl:when test="$source = 'microfilm 35mm'"><xsl:text>022</xsl:text></xsl:when>
			<xsl:when test="$source = 'microfilm 35 mm'"><xsl:text>022</xsl:text></xsl:when>
			<xsl:when test="$source = '35mm microfilm '"><xsl:text>022</xsl:text></xsl:when>
			<xsl:when test="$source = '35 mm microfilm'"><xsl:text>022</xsl:text></xsl:when>
			<xsl:when test="$source = 'hardcopy microfilm 70'"><xsl:text>023</xsl:text></xsl:when>
			<xsl:when test="$source = 'microfilm 70'"><xsl:text>023</xsl:text></xsl:when>
			<xsl:when test="$source = 'microfilm 70mm'"><xsl:text>023</xsl:text></xsl:when>
			<xsl:when test="$source = 'microfilm 70 mm'"><xsl:text>023</xsl:text></xsl:when>
			<xsl:when test="$source = '70mm microfilm'"><xsl:text>023</xsl:text></xsl:when>
			<xsl:when test="$source = '70 mm microfilm'"><xsl:text>023</xsl:text></xsl:when>
			<xsl:when test="$source = 'hardcopy microfilm general'"><xsl:text>024</xsl:text></xsl:when>
			<xsl:when test="$source = 'microfilm'"><xsl:text>024</xsl:text></xsl:when>
			<xsl:when test="$source = 'microfiche'"><xsl:text>025</xsl:text></xsl:when>
			<xsl:when test="$source = 'hardcopy microfilm microfiche'"><xsl:text>025</xsl:text></xsl:when>
			<xsl:when test="$source = 'hardcopy negative photo'"><xsl:text>026</xsl:text></xsl:when>
			<xsl:when test="$source = 'negative photo'"><xsl:text>026</xsl:text></xsl:when>
			<xsl:when test="$source = 'photo negative'"><xsl:text>026</xsl:text></xsl:when>
			<xsl:when test="$source = 'negative'"><xsl:text>026</xsl:text></xsl:when>
			<xsl:when test="$source = 'hardcopy paper'"><xsl:text>027</xsl:text></xsl:when>
			<xsl:when test="$source = 'paper'"><xsl:text>027</xsl:text></xsl:when>
			<xsl:when test="$source = 'hardcopy diazo'"><xsl:text>028</xsl:text></xsl:when>
			<xsl:when test="$source = 'stable base material'"><xsl:text>028</xsl:text></xsl:when>
			<xsl:when test="$source = 'stable-base material'"><xsl:text>028</xsl:text></xsl:when>
			<xsl:when test="$source = 'transparency'"><xsl:text>028</xsl:text></xsl:when>
			<xsl:when test="$source = 'diazo'"><xsl:text>028</xsl:text></xsl:when>
			<xsl:when test="$source = 'hardcopy photo'"><xsl:text>029</xsl:text></xsl:when>
			<xsl:when test="$source = 'photo'"><xsl:text>029</xsl:text></xsl:when>
			<xsl:when test="$source = 'hardcopy traced paper'"><xsl:text>030</xsl:text></xsl:when>
			<xsl:when test="$source = 'traced paper'"><xsl:text>030</xsl:text></xsl:when>
			<xsl:when test="$source = 'tracing'"><xsl:text>030</xsl:text></xsl:when>
			<xsl:when test="$source = 'hard disk'"><xsl:text>031</xsl:text></xsl:when>
			<xsl:when test="$source = 'disk'"><xsl:text>031</xsl:text></xsl:when>
			<xsl:when test="$source = 'disc'"><xsl:text>031</xsl:text></xsl:when>
			<xsl:when test="$source = 'usb flash drive'"><xsl:text>032</xsl:text></xsl:when>
			<xsl:when test="$source = 'usb'"><xsl:text>032</xsl:text></xsl:when>
			<xsl:when test="$source = 'flash drive'"><xsl:text>032</xsl:text></xsl:when>
			<xsl:when test="$source = 'usb drive'"><xsl:text>032</xsl:text></xsl:when>
			<!-- <xsl:when test="$source = 'audio cassette'"><xsl:text></xsl:text></xsl:when> -->
			<!-- <xsl:when test="$source = 'filmstrip'"><xsl:text></xsl:text></xsl:when> -->
			<!-- <xsl:when test="$source = 'video cassette'"><xsl:text></xsl:text></xsl:when> -->
			<!-- <xsl:when test="$source = 'video disc'"><xsl:text></xsl:text></xsl:when> -->
			<xsl:otherwise>
				<xsl:value-of select="$source"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<!-- MD_ObligationCode -->
	<xsl:template name="MD_ObligationCode">
		<xsl:param name="source" />
		<xsl:choose>
			<xsl:when test="$source = 'mandatory'"><xsl:text>001</xsl:text></xsl:when>
			<xsl:when test="$source = 'optional'"><xsl:text>002</xsl:text></xsl:when>
			<xsl:when test="$source = 'conditional'"><xsl:text>003</xsl:text></xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="$source"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<!-- MD_PixelOrientationCode -->
	<xsl:template name="MD_PixelOrientationCode">
		<xsl:param name="source" />
		<xsl:choose>
			<xsl:when test="$source = 'center'"><xsl:text>001</xsl:text></xsl:when>
			<xsl:when test="$source = 'lowerLeft'"><xsl:text>002</xsl:text></xsl:when>
			<xsl:when test="$source = 'lowerRight'"><xsl:text>003</xsl:text></xsl:when>
			<xsl:when test="$source = 'upperRight'"><xsl:text>004</xsl:text></xsl:when>
			<xsl:when test="$source = 'upperLeft'"><xsl:text>005</xsl:text></xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="$source"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<!-- MD_ProgressCode -->
	<xsl:template name="MD_ProgressCode">
		<xsl:param name="source" />
		<xsl:choose>
			<xsl:when test="$source = 'completed'"><xsl:text>001</xsl:text></xsl:when>
			<xsl:when test="$source = 'historicalarchive'"><xsl:text>002</xsl:text></xsl:when>
			<xsl:when test="$source = 'obsolete'"><xsl:text>003</xsl:text></xsl:when>
			<xsl:when test="$source = 'ongoing'"><xsl:text>004</xsl:text></xsl:when>
			<xsl:when test="$source = 'planned'"><xsl:text>005</xsl:text></xsl:when>
			<xsl:when test="$source = 'required'"><xsl:text>006</xsl:text></xsl:when>
			<xsl:when test="$source = 'underdevelopment'"><xsl:text>007</xsl:text></xsl:when>
			<xsl:when test="$source = 'proposed'"><xsl:text>008</xsl:text></xsl:when>
			<!-- fgdc progress -->
			<xsl:when test="$source = 'complete'"><xsl:text>001</xsl:text></xsl:when>
			<xsl:when test="$source = 'in work'"><xsl:text>007</xsl:text></xsl:when>
			<xsl:when test="$source = 'historical archive'"><xsl:text>002</xsl:text></xsl:when>
			<xsl:when test="$source = 'on going'"><xsl:text>004</xsl:text></xsl:when>
			<xsl:when test="$source = 'on-going'"><xsl:text>004</xsl:text></xsl:when>
			<xsl:when test="$source = 'under development'"><xsl:text>007</xsl:text></xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="$source"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<!-- MD_RestrictionCode -->
	<xsl:template name="MD_RestrictionCode">
		<xsl:param name="source" />
		<xsl:choose>
			<xsl:when test="$source = 'copyright'"><xsl:text>001</xsl:text></xsl:when>
			<xsl:when test="$source = 'patent'"><xsl:text>002</xsl:text></xsl:when>
			<xsl:when test="$source = 'patentPending'"><xsl:text>003</xsl:text></xsl:when>
			<xsl:when test="$source = 'trademark'"><xsl:text>004</xsl:text></xsl:when>
			<xsl:when test="$source = 'license'"><xsl:text>005</xsl:text></xsl:when>
			<xsl:when test="$source = 'intellectualPropertyRights'"><xsl:text>006</xsl:text></xsl:when>
			<xsl:when test="$source = 'restricted'"><xsl:text>007</xsl:text></xsl:when>
			<xsl:when test="$source = 'otherRestrictions'"><xsl:text>008</xsl:text></xsl:when>
			<xsl:when test="$source = 'licenseUnrestricted'"><xsl:text>009</xsl:text></xsl:when>
			<xsl:when test="$source = 'licenseEndUser'"><xsl:text>010</xsl:text></xsl:when>
			<xsl:when test="$source = 'licenseDistributor'"><xsl:text>011</xsl:text></xsl:when>
			<xsl:when test="$source = 'privacy'"><xsl:text>012</xsl:text></xsl:when>
			<xsl:when test="$source = 'statutory'"><xsl:text>013</xsl:text></xsl:when>
			<xsl:when test="$source = 'confidential'"><xsl:text>014</xsl:text></xsl:when>
			<xsl:when test="$source = 'sensitivity'"><xsl:text>015</xsl:text></xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="$source"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<!-- MD_ScopeCode -->
	<xsl:template name="MD_ScopeCode">
		<xsl:param name="source" />
		<xsl:choose>
			<xsl:when test="$source = 'attribute'"><xsl:text>001</xsl:text></xsl:when>
			<xsl:when test="$source = 'attributeType'"><xsl:text>002</xsl:text></xsl:when>
			<xsl:when test="$source = 'collectionHardware'"><xsl:text>003</xsl:text></xsl:when>
			<xsl:when test="$source = 'collectionSession'"><xsl:text>004</xsl:text></xsl:when>
			<xsl:when test="$source = 'dataset'"><xsl:text>005</xsl:text></xsl:when>
			<xsl:when test="$source = 'series'"><xsl:text>006</xsl:text></xsl:when>
			<xsl:when test="$source = 'nonGeographicDataset'"><xsl:text>007</xsl:text></xsl:when>
			<xsl:when test="$source = 'dimensionGroup'"><xsl:text>008</xsl:text></xsl:when>
			<xsl:when test="$source = 'feature'"><xsl:text>009</xsl:text></xsl:when>
			<xsl:when test="$source = 'featureType'"><xsl:text>010</xsl:text></xsl:when>
			<xsl:when test="$source = 'propertyType'"><xsl:text>011</xsl:text></xsl:when>
			<xsl:when test="$source = 'fieldSession'"><xsl:text>012</xsl:text></xsl:when>
			<xsl:when test="$source = 'software'"><xsl:text>013</xsl:text></xsl:when>
			<xsl:when test="$source = 'service'"><xsl:text>014</xsl:text></xsl:when>
			<xsl:when test="$source = 'model'"><xsl:text>015</xsl:text></xsl:when>
			<xsl:when test="$source = 'tile'"><xsl:text>016</xsl:text></xsl:when>
			<xsl:when test="$source = 'initiative'"><xsl:text>017</xsl:text></xsl:when>
			<xsl:when test="$source = 'stereomate'"><xsl:text>018</xsl:text></xsl:when>
			<xsl:when test="$source = 'sensor'"><xsl:text>019</xsl:text></xsl:when>
			<xsl:when test="$source = 'platformSeries'"><xsl:text>020</xsl:text></xsl:when>
			<xsl:when test="$source = 'sensorSeries'"><xsl:text>021</xsl:text></xsl:when>
			<xsl:when test="$source = 'productionSeries'"><xsl:text>022</xsl:text></xsl:when>
			<xsl:when test="$source = 'transferAggregate'"><xsl:text>023</xsl:text></xsl:when>
			<xsl:when test="$source = 'otherAggregate'"><xsl:text>024</xsl:text></xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="$source"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<!-- MD_SpatialRepresentationTypeCode -->
	<xsl:template name="MD_SpatialRepresentationTypeCode">
		<xsl:param name="source" />
		<xsl:choose>
			<xsl:when test="$source = 'vector'"><xsl:text>001</xsl:text></xsl:when>
			<xsl:when test="$source = 'grid'"><xsl:text>002</xsl:text></xsl:when>
			<xsl:when test="$source = 'texttable'"><xsl:text>003</xsl:text></xsl:when>
			<xsl:when test="$source = 'tin'"><xsl:text>004</xsl:text></xsl:when>
			<xsl:when test="$source = 'stereomodel'"><xsl:text>005</xsl:text></xsl:when>
			<xsl:when test="$source = 'video'"><xsl:text>006</xsl:text></xsl:when>
			<!-- fgdc direct -->
			<xsl:when test="$source = 'raster'"><xsl:text>002</xsl:text></xsl:when>
			<xsl:when test="$source = 'point'"><xsl:text>001</xsl:text></xsl:when>
			<xsl:when test="$source = 'text table'"><xsl:text>003</xsl:text></xsl:when>
			<xsl:when test="$source = 'stereo model'"><xsl:text>005</xsl:text></xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="$source"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<!-- MD_TopicCategoryCode -->
	<xsl:template name="MD_TopicCategoryCode">
		<xsl:param name="source" />
		<xsl:choose>
			<xsl:when test="$source = 'farming'"><xsl:text>001</xsl:text></xsl:when>
			<xsl:when test="$source = 'biota'"><xsl:text>002</xsl:text></xsl:when>
			<xsl:when test="$source = 'boundaries'"><xsl:text>003</xsl:text></xsl:when>
			<xsl:when test="$source = 'climatologymeteorologyatmosphere'"><xsl:text>004</xsl:text></xsl:when>
			<xsl:when test="$source = 'economy'"><xsl:text>005</xsl:text></xsl:when>
			<xsl:when test="$source = 'elevation'"><xsl:text>006</xsl:text></xsl:when>
			<xsl:when test="$source = 'environment'"><xsl:text>007</xsl:text></xsl:when>
			<xsl:when test="$source = 'geoscientificinformation'"><xsl:text>008</xsl:text></xsl:when>
			<xsl:when test="$source = 'health'"><xsl:text>009</xsl:text></xsl:when>
			<xsl:when test="$source = 'imagerybasemapsearthcover'"><xsl:text>010</xsl:text></xsl:when>
			<xsl:when test="$source = 'intelligencemilitary'"><xsl:text>011</xsl:text></xsl:when>
			<xsl:when test="$source = 'inlandwaters'"><xsl:text>012</xsl:text></xsl:when>
			<xsl:when test="$source = 'location'"><xsl:text>013</xsl:text></xsl:when>
			<xsl:when test="$source = 'oceans'"><xsl:text>014</xsl:text></xsl:when>
			<xsl:when test="$source = 'planningcadastre'"><xsl:text>015</xsl:text></xsl:when>
			<xsl:when test="$source = 'society'"><xsl:text>016</xsl:text></xsl:when>
			<xsl:when test="$source = 'structure'"><xsl:text>017</xsl:text></xsl:when>
			<xsl:when test="$source = 'transportation'"><xsl:text>018</xsl:text></xsl:when>
			<xsl:when test="$source = 'utilitiescommunication'"><xsl:text>019</xsl:text></xsl:when>
			<!-- fgdc theme keywords matched to topic categories -->
			<xsl:when test="$source = 'agriculture'"><xsl:text>001</xsl:text></xsl:when>
			<xsl:when test="$source = 'climatology meteorology atmosphere'"><xsl:text>004</xsl:text></xsl:when>
			<xsl:when test="$source = 'climatology'"><xsl:text>004</xsl:text></xsl:when>
			<xsl:when test="$source = 'meteorology'"><xsl:text>004</xsl:text></xsl:when>
			<xsl:when test="$source = 'atmosphere'"><xsl:text>004</xsl:text></xsl:when>
			<xsl:when test="$source = 'atmospheric'"><xsl:text>004</xsl:text></xsl:when>
			<xsl:when test="$source = 'geoscientific information'"><xsl:text>008</xsl:text></xsl:when>
			<xsl:when test="$source = 'geology'"><xsl:text>008</xsl:text></xsl:when>
			<xsl:when test="$source = 'imagery base maps earth cover'"><xsl:text>010</xsl:text></xsl:when>
			<xsl:when test="$source = 'imagery'"><xsl:text>010</xsl:text></xsl:when>
			<xsl:when test="$source = 'base maps'"><xsl:text>010</xsl:text></xsl:when>
			<xsl:when test="$source = 'basemaps'"><xsl:text>010</xsl:text></xsl:when>
			<xsl:when test="$source = 'earth cover'"><xsl:text>010</xsl:text></xsl:when>
			<xsl:when test="$source = 'land cover'"><xsl:text>010</xsl:text></xsl:when>
			<xsl:when test="$source = 'intelligence military'"><xsl:text>011</xsl:text></xsl:when>
			<xsl:when test="$source = 'intelligence'"><xsl:text>011</xsl:text></xsl:when>
			<xsl:when test="$source = 'military'"><xsl:text>011</xsl:text></xsl:when>
			<xsl:when test="$source = 'inland waters'"><xsl:text>012</xsl:text></xsl:when>
			<xsl:when test="$source = 'inland'"><xsl:text>012</xsl:text></xsl:when>
			<xsl:when test="$source = 'waters'"><xsl:text>012</xsl:text></xsl:when>
			<xsl:when test="$source = 'lakes'"><xsl:text>012</xsl:text></xsl:when>
			<xsl:when test="$source = 'rivers'"><xsl:text>012</xsl:text></xsl:when>
			<xsl:when test="$source = 'streams'"><xsl:text>012</xsl:text></xsl:when>
			<xsl:when test="$source = 'ponds'"><xsl:text>012</xsl:text></xsl:when>
			<xsl:when test="$source = 'ocean'"><xsl:text>014</xsl:text></xsl:when>
			<xsl:when test="$source = 'planning cadastre'"><xsl:text>015</xsl:text></xsl:when>
			<xsl:when test="$source = 'planning'"><xsl:text>015</xsl:text></xsl:when>
			<xsl:when test="$source = 'cadastre'"><xsl:text>015</xsl:text></xsl:when>
			<xsl:when test="$source = 'cadastral'"><xsl:text>015</xsl:text></xsl:when>
			<xsl:when test="$source = 'transportation'"><xsl:text>018</xsl:text></xsl:when>
			<xsl:when test="$source = 'roads'"><xsl:text>018</xsl:text></xsl:when>
			<xsl:when test="$source = 'streets'"><xsl:text>018</xsl:text></xsl:when>
			<xsl:when test="$source = 'highways'"><xsl:text>018</xsl:text></xsl:when>
			<xsl:when test="$source = 'trails'"><xsl:text>018</xsl:text></xsl:when>
			<xsl:when test="$source = 'utilities communication'"><xsl:text>019</xsl:text></xsl:when>
			<xsl:when test="$source = 'utilities'"><xsl:text>019</xsl:text></xsl:when>
			<xsl:when test="$source = 'communication'"><xsl:text>019</xsl:text></xsl:when>
			<!-- 
			<xsl:otherwise>
				<xsl:value-of select="$source"/>
			</xsl:otherwise>
      -->
		</xsl:choose>
	</xsl:template>

	<!-- MD_TopologyLevelCode -->
	<xsl:template name="MD_TopologyLevelCode">
		<xsl:param name="source" />
		<xsl:choose>
			<xsl:when test="$source = 'geometryOnly'"><xsl:text>001</xsl:text></xsl:when>
			<xsl:when test="$source = 'topology1D'"><xsl:text>002</xsl:text></xsl:when>
			<xsl:when test="$source = 'planarGraph'"><xsl:text>003</xsl:text></xsl:when>
			<xsl:when test="$source = 'fullPlanarGraph'"><xsl:text>004</xsl:text></xsl:when>
			<xsl:when test="$source = 'surfaceGraph'"><xsl:text>005</xsl:text></xsl:when>
			<xsl:when test="$source = 'fullSurfaceGraph'"><xsl:text>006</xsl:text></xsl:when>
			<xsl:when test="$source = 'topology3D'"><xsl:text>007</xsl:text></xsl:when>
			<xsl:when test="$source = 'fullTopology3D'"><xsl:text>008</xsl:text></xsl:when>
			<xsl:when test="$source = 'abstract'"><xsl:text>009</xsl:text></xsl:when>
			<xsl:when test="$source = '0'"><xsl:text>001</xsl:text></xsl:when>
			<xsl:when test="$source = '1'"><xsl:text>002</xsl:text></xsl:when>
			<xsl:when test="$source = '2'"><xsl:text>003</xsl:text></xsl:when>
			<xsl:when test="$source = '3'"><xsl:text>008</xsl:text></xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="$source"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>


  <!-- Esri Geoportal Content Type Code -->
	<xsl:template name="contentTypeCode">
		<xsl:param name="source" />
		<xsl:choose>
			<xsl:when test="$source = 'Live Data and Maps'"><xsl:text>001</xsl:text></xsl:when>
			<xsl:when test="$source = 'Downloadable Data'"><xsl:text>002</xsl:text></xsl:when>
			<xsl:when test="$source = 'Offline Data'"><xsl:text>003</xsl:text></xsl:when>
			<xsl:when test="$source = 'Static Map Images'"><xsl:text>004</xsl:text></xsl:when>
			<xsl:when test="$source = 'Other Documents'"><xsl:text>005</xsl:text></xsl:when>
			<xsl:when test="$source = 'Applications'"><xsl:text>006</xsl:text></xsl:when>
			<xsl:when test="$source = 'Geographic Services'"><xsl:text>007</xsl:text></xsl:when>
			<xsl:when test="$source = 'Clearinghouses'"><xsl:text>008</xsl:text></xsl:when>
			<xsl:when test="$source = 'Map Files'"><xsl:text>009</xsl:text></xsl:when>
			<xsl:when test="$source = 'Geographic Activities'"><xsl:text>010</xsl:text></xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="$source"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

  <!-- Esri Data Quality Element Type -->
	<xsl:template name="DQ_ElementType">
		<xsl:param name="source" />
		<xsl:choose>
			<xsl:when test="$source = 'DQ_Completeness'"><xsl:text>DQComplete</xsl:text></xsl:when>
			<xsl:when test="$source = 'DQ_CompletenessCommission'"><xsl:text>DQCompComm</xsl:text></xsl:when>
			<xsl:when test="$source = 'DQ_CompletenessOmission'"><xsl:text>DQCompOm</xsl:text></xsl:when>
			<xsl:when test="$source = 'DQ_ConceptualConsistency'"><xsl:text>DQConcConsis</xsl:text></xsl:when>
			<xsl:when test="$source = 'DQ_DomainConsistency'"><xsl:text>DQDomConsis</xsl:text></xsl:when>
			<xsl:when test="$source = 'DQ_FormatConsistency'"><xsl:text>DQFormConsis</xsl:text></xsl:when>
			<xsl:when test="$source = 'DQ_TopologicalConsistency'"><xsl:text>DQTopConsis</xsl:text></xsl:when>
			<xsl:when test="$source = 'DQ_PositionalAccuracy'"><xsl:text>DQPosAcc</xsl:text></xsl:when>
			<xsl:when test="$source = 'DQ_AbsoluteExternalPositionalAccuracy'"><xsl:text>DQAbsExtPosAcc</xsl:text></xsl:when>
			<xsl:when test="$source = 'DQ_GriddedDataPositionalAccuracy'"><xsl:text>DQGridDataPosAcc</xsl:text></xsl:when>
			<xsl:when test="$source = 'DQ_RelativeInternalPositionalAccuracy'"><xsl:text>DQRelIntPosAcc</xsl:text></xsl:when>
			<xsl:when test="$source = 'DQ_TemporalAccuracy'"><xsl:text>DQTempAcc</xsl:text></xsl:when>
			<xsl:when test="$source = 'DQ_ThematicAccuracy'"><xsl:text>DQThemAcc</xsl:text></xsl:when>
			<xsl:when test="$source = 'DQ_ThematicClassificationCorrectness'"><xsl:text>DQThemClassCor</xsl:text></xsl:when>
			<xsl:when test="$source = 'DQ_NonQuantitativeAttributeAccuracy'"><xsl:text>DQNonQuanAttAcc</xsl:text></xsl:when>
			<xsl:when test="$source = 'DQ_QuantitativeAttributeAccuracy'"><xsl:text>DQQuanAttAcc</xsl:text></xsl:when>
			<xsl:when test="$source = 'DQ_AccuracyOfATimeMeasurement'"><xsl:text>DQAccTimeMeas</xsl:text></xsl:when>
			<xsl:when test="$source = 'DQ_TemporalConsistency'"><xsl:text>DQTempConsis</xsl:text></xsl:when>
			<xsl:when test="$source = 'DQ_TemporalValidity'"><xsl:text>DQTempValid</xsl:text></xsl:when>
			<xsl:when test="$source = 'QeUsability'"><xsl:text>QeUsability</xsl:text></xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="$source"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>	

  <xsl:template match="countryCodes">
    <xsl:param name="upper" />
    <xsl:param name="lower" />
    <xsl:param name="original" />
    <xsl:variable name="name">
      <xsl:for-each select="key('ctryName',$lower)">
        <xsl:value-of select="@alpha2"/>
      </xsl:for-each>
    </xsl:variable>
    <xsl:variable name="alpha3">
      <xsl:for-each select="key('ctrya3',$upper)">
        <xsl:value-of select="@alpha2"/>
      </xsl:for-each>
    </xsl:variable>
    <xsl:choose>
      <xsl:when test="($name != '')"><xsl:value-of select="$name"/></xsl:when>
      <xsl:when test="($alpha3 != '')"><xsl:value-of select="$alpha3"/></xsl:when>
      <xsl:otherwise><xsl:value-of select="$original"/></xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="languageCodes">
    <xsl:param name="lower" />
    <xsl:param name="original" />
    <xsl:variable name="name">
      <xsl:for-each select="key('langName',$lower)">
        <xsl:value-of select="@alpha3"/>
      </xsl:for-each>
    </xsl:variable>
    <xsl:variable name="alpha2">
      <xsl:for-each select="key('langa2',$lower)">
        <xsl:value-of select="@alpha3"/>
      </xsl:for-each>
    </xsl:variable>
    <xsl:variable name="alpha3t">
      <xsl:for-each select="key('langa3t',$lower)">
        <xsl:value-of select="@alpha3"/>
      </xsl:for-each>
    </xsl:variable>
    <xsl:variable name="alpha3">
      <xsl:for-each select="key('langa3',$lower)">
        <xsl:value-of select="@alpha3"/>
      </xsl:for-each>
    </xsl:variable>
    <xsl:choose>
      <xsl:when test="($name != '')"><xsl:value-of select="$name"/></xsl:when>
      <xsl:when test="($alpha2 != '')"><xsl:value-of select="$alpha2"/></xsl:when>
      <xsl:when test="($alpha3t != '')"><xsl:value-of select="$alpha3t"/></xsl:when>
      <xsl:when test="($alpha3 != '')"><xsl:value-of select="$alpha3"/></xsl:when>
      <xsl:otherwise><xsl:value-of select="$original"/></xsl:otherwise>
    </xsl:choose>
  </xsl:template>

</xsl:stylesheet>
