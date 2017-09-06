<?xml version="1.0" encoding="UTF-8"?>
<!--
   headerJoin.xsl

   This stylesheet takes a document in checker format and safely joins
   header states in different branches of execution. The operation
   works like removeDups.xsl, but this takes into account the fact
   that the same HTTP header may have multiple values, which are
   checked by different states, and it creates a state that check
   those values simultaneously.

   For Example:

              +===+
            /=| C +=
   +===+ /==  +===+ \== +===+
   | A +=               + Y |
   +===+ \==  +===+ /== +===+
            \=| D +=
              +===+

   Becomes:


   +===+    +===+    +===+
   | A +====+ B +====+ Y |
   +===+    +===+    +===+


   Here, C and D are states that check that the content of a header
   contians a value. The new state B, checks that the content of the
   header contain be any one of those values.

   The process is executed recursively.  It is assumed that the input
   file has already gone through removeDups.xsl and commonJoin.xsl

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
        xmlns:xsd="http://www.w3.org/2001/XMLSchema"
        xmlns:check="http://www.rackspace.com/repose/wadl/checker"
        xmlns="http://www.rackspace.com/repose/wadl/checker"
        exclude-result-prefixes="xsd check"
        version="3.0">

    <!--
        Most of the work of joining is done by the following util. The
        purpose of this template is to identify the joins and to
        create new join steps.
    -->
    <xsl:import href="../util/join.xsl"/>

    <!--
        Select join candidates with auto-generated code.
    -->
    <xsl:include href="removeDups-rules.header.xsl"/>

    <!--
        Convert joins into a checker step.
    -->
    <xsl:template name="createJoinStep">
        <xsl:param name="checker" as="node()"/>
        <xsl:param name="joins" as="map(xsd:string, xsd:string*)"/>
        <xsl:param name="stepsToJoin" as="map(xsd:string, xsd:string)"/>
        <xsl:variable name="joinSteps" as="node()*" select="check:stepsByIds($checker, $joins(.))"/>
        <xsl:variable name="distinctMatches" as="xsd:string*" select="distinct-values($joinSteps/@match)"/>

        <step id="{.}" type="{$joinSteps[1]/@type}" name="{$joinSteps[1]/@name}">
            <xsl:call-template name="joinNext">
                <xsl:with-param name="checker" select="$checker"/>
                <xsl:with-param name="joins" select="$joins"/>
                <xsl:with-param name="stepsToJoin" select="$stepsToJoin"/>
            </xsl:call-template>
            <xsl:attribute name="match">
                <xsl:value-of select="$distinctMatches" separator="|" />
            </xsl:attribute>
            <xsl:if test="$joinSteps[1]/@code">
                <xsl:attribute name="code" select="$joinSteps[1]/@code"/>
            </xsl:if>
            <xsl:if test="$joinSteps[1]/@message">
                <xsl:attribute name="message" select="$joinSteps[1]/@message"/>
            </xsl:if>
            <xsl:if test="$joinSteps[1]/@captureHeader">
                <xsl:attribute name="captureHeader" select="$joinSteps[1]/@captureHeader"/>
            </xsl:if>
            <xsl:copy-of select="$joinSteps[1]/@*[name() = $error-sink-types]"/>
        </step>
    </xsl:template>
</xsl:stylesheet>
