<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" 
    xmlns:dct="http://purl.org/dc/terms/"
    xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
    xmlns:rim="urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0"
    xmlns:fo="http://www.w3.org/1999/XSL/Format"
    xmlns:ows20="http://www.opengis.net/ows/2.0"
    xmlns:exslt="http://exslt.org/common"
    xmlns:dc="http://purl.org/dc/elements/1.1/"
    xmlns:dcmiBox="http://dublincore.org/documents/2000/07/11/dcmi-box/"
    xmlns:xlink="http://www.w3.org/1999/xlink"
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

	<xsl:output method="xml" indent="yes"/>
	
    <xsl:template match="/rdf:RDF">
<metadata xml:lang="en">
    <Esri>
        <ArcGISFormat>1.0</ArcGISFormat>
        <SyncOnce>TRUE</SyncOnce>
        <ArcGISProfile>FGDC</ArcGISProfile>
    </Esri>
    <mdFileID><xsl:value-of select="/rdf:RDF/rdf:Description/dc:identifier"/></mdFileID>
    <dataIdInfo>
        <idCitation xmlns="">
            <resTitle><xsl:value-of select="/rdf:RDF/rdf:Description/dc:title"/></resTitle>
            <date>
                <pubDate></pubDate> <!-- 2021-05-20T00:00:00 -->
            </date>
            <citRespParty xmlns="">
                <rpIndName><xsl:value-of select="/rdf:RDF/rdf:Description/dc:creator"/></rpIndName>
                <role>
                    <RoleCd value="006"/>
                </role>
            </citRespParty>
        </idCitation>
        <searchKeys>
            <!-- <keyword>keyword</keyword> -->
            <xsl:for-each select="/rdf:RDF/rdf:Description/dc:subject">
                <keyword><xsl:value-of select="."/></keyword>
            </xsl:for-each>
        </searchKeys> 
        <idPurp/>
        <idAbs><xsl:value-of select="/rdf:RDF/rdf:Description/dc:description"/></idAbs>
        <idCredit>TVA</idCredit>
        <resConst>
            <Consts>
                <useLimit></useLimit>
            </Consts>
        </resConst>
        <dataExt xmlns="">
            <geoEle xmlns="">
                <GeoBndBox esriExtentType="search">
                    <westBL></westBL>
                    <eastBL></eastBL>
                    <northBL></northBL>
                    <southBL></southBL>
                    <exTypeCode>1</exTypeCode>
                </GeoBndBox>
            </geoEle>
            <exDesc></exDesc>
            <tempEle>
                <TempExtent>
                    <exTemp>
                        <TM_Period xmlns="">
                            <tmBegin></tmBegin> <!-- 2021-02-01T00:00:00 -->
                        </TM_Period>
                    </exTemp>
                </TempExtent>
            </tempEle>
        </dataExt>
        <dataChar>
            <CharSetCd value="004"/>
        </dataChar>
        <spatRpType>
            <SpatRepTypCd value=""/>
        </spatRpType>
        <idStatus>
            <ProgCd value=""/>
        </idStatus>
        <resMaint xmlns="">
            <maintFreq>
                <MaintFreqCd value=""/>
            </maintFreq>
        </resMaint>
        <tpCat>
            <TopicCatCd value=""/>
        </tpCat>
    </dataIdInfo>
    <mdHrLv>
        <ScopeCd value=""/>
    </mdHrLv>
    <mdDateSt Sync="TRUE"></mdDateSt>
    <mdContact xmlns="">
        <rpIndName><xsl:value-of select="/rdf:RDF/rdf:Description/dc:creator"/></rpIndName>
        <rpOrgName><xsl:value-of select="/rdf:RDF/rdf:Description/dc:contributor"/></rpOrgName>
        <role>
            <RoleCd value="006"/>
        </role>
        <displayName><xsl:value-of select="/rdf:RDF/rdf:Description/dc:creator"/></displayName>
        <rpCntInfo xmlns="">
            <cntAddress addressType="both">
                <city></city>
                <adminArea></adminArea>
                <postCode></postCode>
                <country></country>
            </cntAddress>
            <cntPhone>
                <voiceNum tddtty=""></voiceNum>
            </cntPhone>
        </rpCntInfo>
    </mdContact>
    <dqInfo xmlns="">
        <dataLineage>
            <statement></statement>
            <dataSource xmlns="" type="">
                <srcDesc></srcDesc>
                <srcMedName>
                    <MedNameCd value="015"/>
                </srcMedName>
                <srcCitatn xmlns="">
                    <resTitle></resTitle>
                    <citOnlineRes xmlns="">
                        <linkage></linkage>
                    </citOnlineRes>
                </srcCitatn>
            </dataSource>
        </dataLineage>
        <dqScope>
            <scpLvl>
                <ScopeCd value="005"/>
            </scpLvl>
        </dqScope>
    </dqInfo>
    <xsl:for-each select="/rdf:RDF/rdf:Description/dct:references">
        <distInfo xmlns="">
            <distTranOps xmlns="">
                <onLineSrc xmlns="">
                    <linkage><xsl:value-of select="."/></linkage>
                    <orFunct>
                        <OnFunctCd value="001"/>
                    </orFunct>
                </onLineSrc>
            </distTranOps>
        </distInfo>        
    </xsl:for-each>
</metadata>
	</xsl:template>    
	
</xsl:stylesheet>
