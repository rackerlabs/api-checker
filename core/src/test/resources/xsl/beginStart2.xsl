<?xml version="1.0" encoding="UTF-8"?>
<!--
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
    xmlns:tst="http://www.rackspace.com/repose/wadl/checker/step/test"
    xmlns="http://www.rackspace.com/repose/wadl/checker/step/test"
    version="2.0" exclude-result-prefixes="tst">

    <xsl:template match="node() | @*">
        <xsl:copy>
            <xsl:apply-templates select="@* | node()"/>
        </xsl:copy>
    </xsl:template>

    <xsl:template match="tst:stepType">
        <xsl:choose>
            <xsl:when test=". = 'BEGIN'">
                <stepType>START</stepType>
            </xsl:when>
            <xsl:otherwise>
                <stepType><xsl:value-of select="."/></stepType>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template match="@stepType">
        <xsl:choose>
            <xsl:when test=". = 'BEGIN'">
                <xsl:attribute name="stepType">START</xsl:attribute>
            </xsl:when>
            <xsl:otherwise>
                <xsl:attribute name="stepType"><xsl:value-of select="."/></xsl:attribute>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

</xsl:stylesheet>
