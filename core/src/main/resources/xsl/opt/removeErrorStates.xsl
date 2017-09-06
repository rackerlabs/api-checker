<?xml version="1.0" encoding="UTF-8"?>
<!--
   removeErrorStates.xsl

   This stylesheet removes all error states and leaves a marker
   in states that reference error states so that we can add the
   error states again later.

   We remove error states as part of optimization.  Since error
   states can be easily added after the fact if we leave clues
   in the machine as to where they go, be removing them we allow
   optimization stages to execute more efficently and have less
   edge cases to worry about.

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
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xsd="http://www.w3.org/2001/XMLSchema"
    xmlns:check="http://www.rackspace.com/repose/wadl/checker"
    xmlns:map="http://www.w3.org/2005/xpath-functions/map"
    xmlns="http://www.rackspace.com/repose/wadl/checker" exclude-result-prefixes="xsd check map"
    version="3.0">

    <xsl:import href="../util/funs.xsl"/>

    <xsl:output method="xml"/>

    <xsl:variable name="checker" as="node()" select="/check:checker"/>

    <xsl:template match="@* | node()">
        <xsl:copy>
            <xsl:apply-templates select="@* | node()"/>
        </xsl:copy>
    </xsl:template>

    <xsl:template match="check:step[@type = $error-sink-types]"/>
    <xsl:template match="check:step[@next]">
        <xsl:copy>
            <xsl:variable name="nxts" as="node()*" select="check:stepsByIds($checker, check:next(.))"/>
            <xsl:variable name="newNext" as="xsd:string*" select="$nxts[not(@type = $error-sink-types)]/@id"/>
            <xsl:attribute name="next">
                <xsl:value-of select="$newNext" separator=" "/>
            </xsl:attribute>
            <xsl:apply-templates select="@*[name() != 'next']"/>
            <xsl:for-each select="$error-sink-types">
                <xsl:variable name="errorType" as="xsd:string" select="."/>
                <xsl:attribute name="{$errorType}" select="boolean($nxts[@type = $errorType])"/>
            </xsl:for-each>
            <xsl:apply-templates select="node()"/>
        </xsl:copy>
    </xsl:template>

</xsl:stylesheet>
