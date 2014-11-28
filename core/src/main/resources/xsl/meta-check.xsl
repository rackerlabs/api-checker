<?xml version="1.0" encoding="UTF-8"?>
<!--
   meta-check.xsl

   This stylesheet simply copies over a checker and displays a warning
   if config options differ the ones that are passed via parameter.  In
   particular, it complains if the version and config options differ.

   Copyright 2014 Rackspace US, Inc.

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
<xsl:stylesheet
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    xmlns:chk="http://www.rackspace.com/repose/wadl/checker"
    xmlns="http://www.rackspace.com/repose/wadl/checker"
    exclude-result-prefixes="xs chk"
    version="2.0">

    <xsl:import href="util/funs.xsl"/>

    <xsl:param name="configMetadata" as="node()"/>
    <xsl:param name="creator" as="xs:string" select="'unknown'"/>

    <xsl:template name="chk:copy" match="@* | node()">
        <xsl:copy>
            <xsl:apply-templates select="@* | node()"/>
        </xsl:copy>
    </xsl:template>

    <xsl:template match="/">
        <xsl:variable name="checkConfig" as="node()*">
            <xsl:apply-templates select="$configMetadata//chk:config">
                <xsl:with-param name="otherConfig" select="/chk:checker" tunnel="yes"/>
                <xsl:with-param name="otherConfigName" select="'Compiled'" tunnel="yes"/>
                <xsl:with-param name="thisConfigName" select="'Current'" tunnel="yes"/>
                <xsl:with-param name="reportMismatch" select="false()" tunnel="yes"/>
            </xsl:apply-templates>
        </xsl:variable>
        <xsl:message>[DEBUG] <xsl:copy-of select="$checkConfig"/></xsl:message>
        <xsl:call-template name="chk:copy"/>
        <!--
            WARN if there is no created by in the loaded checker document.
        -->
        <xsl:if test="empty(/chk:checker/chk:meta/chk:created-by)">
            <xsl:call-template name="chk:versionWarn">
                <xsl:with-param name="loadVersion" select="'Unknown'"/>
            </xsl:call-template>
        </xsl:if>
    </xsl:template>

    <xsl:template match="chk:created-by">
        <xsl:variable name="loadVersion" select="." as="xs:string"/>
        <xsl:if test="chk:getVersion($creator) != chk:getVersion($loadVersion)">
            <xsl:call-template name="chk:versionWarn">
                <xsl:with-param name="loadVersion" select="$loadVersion"/>
            </xsl:call-template>
        </xsl:if>
        <xsl:call-template name="chk:copy"/>
    </xsl:template>

    <xsl:template match="chk:config">
        <xsl:param name="otherConfig" as="node()" select="$configMetadata" tunnel="yes"/>
        <xsl:param name="otherConfigName" as="xs:string" select="'Current'" tunnel="yes"/>
        <xsl:param name="thisConfigName" as="xs:string" select="'Compiled'" tunnel="yes"/>
        <xsl:param name="reportMismatch" as="xs:boolean" select="true()" tunnel="yes"/>

        <xsl:variable name="thisValue" as="xs:string" select="@value"/>
        <xsl:variable name="otherValue" as="xs:string" select="chk:optionValue($otherConfig,@option)"/>

        <xsl:message>[DEBUG] <xsl:value-of select="@option"/> : <xsl:value-of select="concat($thisConfigName,':',$thisValue)"/> ⬌  <xsl:value-of select="concat($otherConfigName,':',$otherValue)"/></xsl:message>

        <xsl:if test="$thisValue != $otherValue">
            <xsl:variable name="msg" as="xs:string*">
                <xsl:text>The </xsl:text><xsl:value-of select="$thisConfigName"/>
                <xsl:text> checker has option </xsl:text><xsl:value-of select="@option"/>
                <xsl:text> set to '</xsl:text><xsl:value-of select="@value"/><xsl:text>'.</xsl:text>
                <xsl:choose>
                    <xsl:when test="$otherValue = ''">
                        <xsl:text> The </xsl:text><xsl:value-of select="$otherConfigName"/>
                        <xsl:text> checker does not have a value for </xsl:text>
                        <xsl:value-of select="@option"/><xsl:text> set.</xsl:text>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:if test="$reportMismatch">
                            <xsl:text> The </xsl:text><xsl:value-of select="concat($otherConfigName,' checker has ',@option)"/>
                            <xsl:text> set to '</xsl:text><xsl:value-of select="$otherValue"/><xsl:text>'.</xsl:text>
                            <xsl:text> The compiled option will take effect, the other will be ignored.</xsl:text>
                        </xsl:if>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:variable>
            <xsl:message>[WARNING] <xsl:value-of select="string-join($msg,'')"/></xsl:message>
        </xsl:if>
        <xsl:call-template name="chk:copy"/>
    </xsl:template>

    <xsl:function name="chk:getVersion" as="xs:string">
        <xsl:param name="versionString" as="xs:string"/>
        <xsl:analyze-string select="$versionString" regex="^.*\((.*)\)$">
            <xsl:matching-substring>
                <xsl:value-of select="regex-group(1)"/>
            </xsl:matching-substring>
            <xsl:non-matching-substring>
                <xsl:message>[WARNING] Strange could not extract version number from: <xsl:value-of select="$versionString"/></xsl:message>
                <xsl:value-of select="$versionString"/>
            </xsl:non-matching-substring>
        </xsl:analyze-string>
    </xsl:function>

    <xsl:template name="chk:versionWarn">
        <xsl:param name="loadVersion" as="xs:string"/>
        <xsl:message>[WARNING] This checker was compiled with a different version of api-checker.</xsl:message>
        <xsl:message>[WARNING] Versions should match to avoid incompatibilities.</xsl:message>
        <xsl:message>[WARNING] The checker was compiled with: <xsl:value-of select="$loadVersion"/></xsl:message>
        <xsl:message>[WARNING] The current version is: <xsl:value-of select="$creator"/></xsl:message>
    </xsl:template>
</xsl:stylesheet>
