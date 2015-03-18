<!--
   pruneSteps.xsl

   This stylesheet exposes a template named util:pruneSteps.  The
   template takes a checker document as input and outputs only those
   steps that are not connected to other steps.

   The process is executed recursively until all disconnected states
   are pruned.

   For example, assuming that S is the start state, this document:

              +===+
   +===+   /==+ Y |
   | X +===   +===+
   +===+             +===+
                    /+ B |
   +===+    +===+ /= +===+
   | S +====+ A +=
   +===+    +===+ \= +===+
                    \+ C |
                     +===+

          +===+
          | Z |
          +===+

   Will simply become:

                     +===+
                    /+ B |
   +===+    +===+ /= +===+
   | S +====+ A +=
   +===+    +===+ \= +===+
                    \+ C |
                     +===+

   Note that this is not an optimization, because technically a
   checker document with disconnected states is an illegal
   document. That said, the prune states method may be called by other
   optimization to tidy things up.

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
    xmlns:wadl="http://wadl.dev.java.net/2009/02"
    xmlns:check="http://www.rackspace.com/repose/wadl/checker"
    xmlns:rax="http://docs.rackspace.com/api"
    xmlns:util="http://www.rackspace.com/repose/wadl/checker/util"
    xmlns="http://www.rackspace.com/repose/wadl/checker"
    exclude-result-prefixes="xsd wadl rax check util"
    version="2.0">

    <xsl:import href="funs.xsl"/>

    <xsl:template name="util:pruneSteps">
        <xsl:param name="checker" as="node()"/>
        <xsl:variable name="nexts" as="xsd:string*" select="distinct-values(tokenize(string-join($checker//check:step/@next,' '),' '))"/>
        <xsl:variable name="connected" as="xsd:integer" select="count($checker//check:step[$nexts = @id])"/>
        <xsl:variable name="all" as="xsd:integer" select="count($checker//check:step[@type != 'START'])"/>
        <xsl:choose>
            <xsl:when test="$connected = $all">
                <xsl:copy-of select="$checker"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:apply-templates select="$checker" mode="util:pruneSteps"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template match="@* | node()" mode="util:pruneSteps">
        <xsl:copy>
            <xsl:apply-templates select="@* | node()" mode="util:pruneSteps"/>
        </xsl:copy>
    </xsl:template>
    <xsl:template match="text()" priority="2" mode="util:pruneSteps"/>
    <xsl:template match="check:meta | check:grammar" mode="util:pruneSteps">
        <xsl:copy-of select="."/>
    </xsl:template>
    <xsl:template match="check:step" mode="util:pruneSteps"/>
    <xsl:template match="check:checker" mode="util:pruneSteps">
        <xsl:variable name="connectedIDs" as="xsd:string*"
            select="util:getConnectedIds(., check:step[@type='START'][1])"/>
        <xsl:copy>
            <xsl:apply-templates select="@* | node()" mode="util:pruneSteps"/>
            <xsl:copy-of select="check:stepsByIds(., distinct-values($connectedIDs))"/>
        </xsl:copy>
    </xsl:template>

    <xsl:function name="util:getConnectedIds" as="xsd:string*">
        <xsl:param name="checker" as="node()"/>
        <xsl:param name="step" as="node()*"/>
        <xsl:for-each select="$step">
            <xsl:variable name="nextSteps" as="xsd:string*" select="check:next(.)"/>
            <xsl:choose>
                <xsl:when test="not(@next)">
                    <xsl:sequence select="@id"/>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:sequence select="(@id, util:getConnectedIds($checker, check:stepsByIds($checker, $nextSteps)))"/>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:for-each>
    </xsl:function>

    <xsl:function name="util:pruneSteps" as="node()">
        <xsl:param name="checker" as="node()"/>
        <xsl:call-template name="util:pruneSteps">
            <xsl:with-param name="checker" select="$checker"/>
        </xsl:call-template>
    </xsl:function>

</xsl:stylesheet>
