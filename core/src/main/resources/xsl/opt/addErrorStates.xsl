<?xml version="1.0" encoding="UTF-8"?>
<!--
   addErrorStates.xsl

   This stylesheet adds error states after they've been removed. We consolidate
   error states and use the minimum possible number of error states.

   Copyright 2017 Rackspace US, Inc.

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
-->
<!DOCTYPE stylesheet [
  <!ENTITY matchId "$matchSteps($match)[1] || $errorType">
]>
<xsl:stylesheet
        xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
        xmlns:xsd="http://www.w3.org/2001/XMLSchema"
        xmlns:check="http://www.rackspace.com/repose/wadl/checker"
        xmlns:map="http://www.w3.org/2005/xpath-functions/map"
        xmlns="http://www.rackspace.com/repose/wadl/checker"
        exclude-result-prefixes="xsd check map"
        expand-text="true"
        version="3.0">

    <xsl:import href="../util/funs.xsl"/>

    <xsl:output method="xml"/>

    <xsl:variable name="urlSeparator" as="xsd:string" select="'!!--~~--~~!!'"/>

    <xsl:template match="check:checker">
        <xsl:variable name="checker" select="." as="node()"/>
        <!--
            error_type  ->  match  -> (stepId1, stepId2, stepId3)
        -->
        <xsl:variable name="typeToMatchToSteps" as="map(xsd:string, map(xsd:string, xsd:string*))">
            <xsl:map>
                <xsl:for-each select="$error-sink-types">
                    <xsl:map-entry key="." select="check:getErrorMatchSteps(., $checker)"/>
                </xsl:for-each>
            </xsl:map>
        </xsl:variable>

        <!--
            stepId -> matchId, matchId2, matchId3
        -->
        <xsl:variable name="stepsToMatchIds" as="map(xsd:string, xsd:string*)">
            <xsl:variable name="maps" as="map(xsd:string, xsd:string)*">
                <xsl:for-each select="map:keys($typeToMatchToSteps)">
                    <xsl:variable name="errorType" as="xsd:string" select="."/>
                    <xsl:variable name="matchSteps" as="map(xsd:string, xsd:string*)" select="$typeToMatchToSteps($errorType)"/>
                    <xsl:for-each select="map:keys($matchSteps)">
                        <xsl:variable name="match" as="xsd:string" select="."/>
                        <xsl:variable name="matchId" as="xsd:string" select="&matchId;"/>
                        <xsl:for-each select="$matchSteps($match)">
                            <xsl:map>
                                <xsl:map-entry key="." select="$matchId"/>
                            </xsl:map>
                        </xsl:for-each>
                    </xsl:for-each>
                </xsl:for-each>
            </xsl:variable>
            <xsl:sequence select="map:merge($maps,map{'duplicates' : 'combine'})"/>
        </xsl:variable>

        <!-- Copy replacing old error states -->
        <xsl:copy>
            <xsl:apply-templates select="@* | node()">
                <xsl:with-param name="checker" select="$checker"/>
                <xsl:with-param name="stepsToMatchIds" select="$stepsToMatchIds" tunnel="yes"/>
            </xsl:apply-templates>
            <!-- Gerenate match steps -->
            <xsl:call-template name="generateErrorStates">
                <xsl:with-param name="typeToMatchToSteps" select="$typeToMatchToSteps"/>
            </xsl:call-template>
        </xsl:copy>
    </xsl:template>

    <!--
        Generate new steps
    -->
    <xsl:template name="generateErrorStates">
        <xsl:param name="typeToMatchToSteps" as="map(xsd:string, map(xsd:string, xsd:string*))"/>
        <xsl:for-each select="$error-sink-types[. != 'URL_FAIL']">
            <xsl:call-template name="generateSimpleErrorStates">
                <xsl:with-param name="typeToMatchToSteps" select="$typeToMatchToSteps"/>
                <xsl:with-param name="errorType" select="."/>
            </xsl:call-template>
        </xsl:for-each>
        <xsl:call-template name="generateURLErrorStates">
            <xsl:with-param name="typeToMatchToSteps" select="$typeToMatchToSteps"/>
        </xsl:call-template>
    </xsl:template>

    <xsl:template name="generateSimpleErrorStates">
        <xsl:param name="typeToMatchToSteps" as="map(xsd:string, map(xsd:string, xsd:string*))"/>
        <xsl:param name="errorType" as="xsd:string" />
        <xsl:variable name="matchSteps" as="map(xsd:string, xsd:string*)" select="$typeToMatchToSteps($errorType)"/>
        <xsl:for-each select="map:keys($matchSteps)">
            <xsl:variable name="match" as="xsd:string" select="."/>
            <step id="{&matchId;}" type="{$errorType}">
                <xsl:if test=". != ''">
                    <xsl:attribute name="notMatch" select="."/>
                </xsl:if>
            </step>
        </xsl:for-each>
    </xsl:template>

    <xsl:template name="generateURLErrorStates">
        <xsl:param name="typeToMatchToSteps" as="map(xsd:string, map(xsd:string, xsd:string*))"/>
        <xsl:variable name="errorType" as="xsd:string" select="'URL_FAIL'"/>
        <xsl:variable name="matchSteps" as="map(xsd:string, xsd:string*)" select="$typeToMatchToSteps($errorType)"/>
        <xsl:for-each select="map:keys($matchSteps)">
            <xsl:variable name="match" as="xsd:string" select="."/>
            <xsl:variable name="xsdMatch" as="xsd:string" select="substring-before(.,$urlSeparator)"/>
            <xsl:variable name="umatch" as="xsd:string" select="substring-after(.,$urlSeparator)"/>
            <step id="{&matchId;}" type="{$errorType}">
                <xsl:if test="$umatch != ''">
                    <xsl:attribute name="notMatch" select="$umatch"/>
                </xsl:if>
                <xsl:if test="$xsdMatch != ''">
                    <xsl:attribute name="notTypes" select="$xsdMatch"/>
                </xsl:if>
            </step>
        </xsl:for-each>
    </xsl:template>

    <!--
        Copy templates here we remove old error steps and replace links to new ones.
    -->
    <xsl:template match="@* | node()">
        <xsl:copy>
            <xsl:apply-templates select="@* | node()"/>
        </xsl:copy>
    </xsl:template>

    <xsl:template match="check:step[@next]">
        <xsl:param name="checker" as="node()" />
        <xsl:param name="stepsToMatchIds" as="map(xsd:string, xsd:string*)" tunnel="yes"/>
        <xsl:copy>
            <xsl:choose>
                <xsl:when test="map:contains($stepsToMatchIds,string(@id))">
                    <xsl:variable name="nxts" as="xsd:string*" select="check:next(.)"/>
                    <xsl:variable name="newNext" as="xsd:string*" select="($nxts, $stepsToMatchIds(string(@id)))"/>
                    <xsl:apply-templates select="@*[not(name()='next')]"/>
                    <xsl:attribute name="next"><xsl:value-of select="$newNext" separator=" "/></xsl:attribute>
                    <xsl:apply-templates select="node()"/>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:apply-templates select="@* | node()"/>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:copy>
    </xsl:template>

    <xsl:template match="@*[name() = $error-sink-types]"/>

    <!--
        Matching error states.
     -->
    <xsl:function name="check:getErrorMatchSteps" as="map(xsd:string, xsd:string*)">
        <xsl:param name="type" as="xsd:string"/>
        <xsl:param name="checker" as="node()"/>
        <xsl:variable name="foundMatches" as="map(xsd:string, xsd:string*)*">
            <xsl:choose>
                <xsl:when test="$type='METHOD_FAIL'">
                    <xsl:apply-templates select="$checker" mode="METHOD_FAIL">
                        <xsl:with-param name="checker" select="$checker"/>
                    </xsl:apply-templates>
                </xsl:when>
                <xsl:when test="$type='REQ_TYPE_FAIL'">
                    <xsl:apply-templates select="$checker" mode="REQ_TYPE_FAIL">
                        <xsl:with-param name="checker" select="$checker"/>
                    </xsl:apply-templates>
                </xsl:when>
                <xsl:when test="$type='URL_FAIL'">
                    <xsl:apply-templates select="$checker" mode="URL_FAIL">
                        <xsl:with-param name="checker" select="$checker"/>
                    </xsl:apply-templates>
                </xsl:when>
                <xsl:when test="$type='CONTENT_FAIL'">
                    <xsl:apply-templates select="$checker" mode="CONTENT_FAIL">
                        <xsl:with-param name="checker" select="$checker"/>
                    </xsl:apply-templates>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:message terminate="yes">Unknown clean error failed type {$type}.</xsl:message>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        <xsl:sequence select="map:merge($foundMatches,map{'duplicates' : 'combine'})"/>
    </xsl:function>

    <xsl:template match="check:step[@next and xsd:boolean(@METHOD_FAIL)]" mode="METHOD_FAIL">
        <xsl:param name="checker" as="node()"/>
        <xsl:variable name="nxts" as="node()*" select="check:stepsByIds($checker, check:next(.))"/>
            <xsl:variable name="methodNexts" as="node()*" select="$nxts[@type='METHOD']"/>
            <xsl:map>
                <xsl:choose>
                    <xsl:when test="not(empty($methodNexts))">
                        <xsl:variable name="notMatchValues" as="xsd:string*">
                            <xsl:perform-sort select="$methodNexts/@match">
                                <xsl:sort select="."/>
                            </xsl:perform-sort>
                        </xsl:variable>
                        <xsl:variable name="matchValue" as="xsd:string">
                            <xsl:value-of select="$notMatchValues" separator="|"/>
                        </xsl:variable>
                        <xsl:map-entry key="$matchValue" select="string(@id)"/>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:map-entry key="''" select="string(@id)"/>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:map>
    </xsl:template>


    <xsl:template match="check:step[@next and xsd:boolean(@URL_FAIL)]" mode="URL_FAIL">
        <xsl:param name="checker" as="node()"/>
        <xsl:variable name="nxts" as="node()*" select="check:stepsByIds($checker, check:next(.))"/>
            <xsl:variable name="unexts" as="node()*" select="$nxts[@type=('URL','URLXSD')]"/>
            <xsl:map>
                <xsl:choose>
                    <xsl:when test="not(empty($unexts))">
                        <xsl:variable name="urlNexts" as="node()*" select="$unexts[@type='URL']"/>
                        <xsl:variable name="urlXSDNexts" as="node()*" select="$unexts[@type='URLXSD']"/>
                        <xsl:variable name="notMatchValues" as="xsd:string*">
                            <xsl:perform-sort select="$urlNexts/@match">
                                <xsl:sort select="."/>
                            </xsl:perform-sort>
                        </xsl:variable>
                        <xsl:variable name="matchValue" as="xsd:string">
                            <xsl:value-of select="$notMatchValues" separator="|"/>
                        </xsl:variable>
                        <xsl:variable name="notXSDMatches" as="xsd:QName*">
                            <xsl:perform-sort select="for $s in $urlXSDNexts return resolve-QName($s/@match, $s)">
                                <xsl:sort select="concat(local-name-from-QName(.),'_',namespace-uri-from-QName(.))"/>
                            </xsl:perform-sort>
                        </xsl:variable>
                        <xsl:variable name="matchXSDValue" as="xsd:string">
                            <xsl:value-of select="$notXSDMatches" separator=" "/>
                        </xsl:variable>
                        <xsl:map-entry key="$matchXSDValue || $urlSeparator || $matchValue" select="string(@id)"/>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:map-entry key="''" select="string(@id)"/>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:map>
    </xsl:template>

    <xsl:template match="check:step[@next and xsd:boolean(@REQ_TYPE_FAIL)]" mode="REQ_TYPE_FAIL">
        <xsl:param name="checker" as="node()"/>
        <xsl:variable name="nxts" as="node()*" select="check:stepsByIds($checker, check:next(.))"/>
            <xsl:variable name="reqNexts" as="node()*" select="$nxts[@type='REQ_TYPE']"/>
            <xsl:variable name="notMatchValues" as="xsd:string*">
                <xsl:perform-sort select="$reqNexts/@match">
                    <xsl:sort select="." order="descending"/>
                </xsl:perform-sort>
            </xsl:variable>
            <xsl:variable name="matchValue" as="xsd:string">
                <xsl:value-of select="$notMatchValues" separator="|"/>
            </xsl:variable>
            <xsl:map>
                <xsl:map-entry key="$matchValue" select="string(@id)"/>
            </xsl:map>
    </xsl:template>

    <xsl:template match="check:step[@next and xsd:boolean(@CONTENT_FAIL)]" mode="CONTENT_FAIL">
        <xsl:param name="checker" as="node()"/>
        <xsl:variable name="nxts" as="node()*" select="check:stepsByIds($checker, check:next(.))"/>
            <xsl:map>
                <xsl:map-entry key="''" select="string(@id)"/>
            </xsl:map>
    </xsl:template>

    <xsl:template match="text()" mode="CONTENT_FAIL METHOD_FAIL REQ_TYPE_FAIL URL_FAIL"/>
</xsl:stylesheet>
