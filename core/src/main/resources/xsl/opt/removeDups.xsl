<?xml version="1.0" encoding="UTF-8"?>
<!--
   removeDups.xsl

   This stylesheet takes a document in checker format and removes
   duplicate states. The stylesheet considers a state a duplicate if
   it matches on the same kind of input AND it attaches to the same
   states on output. It then replaces those states with a single
   state.

   For example:

              +===+
             /+ B +=
   +===+ /=== +===+ \    +===+
   | A +=            +===+ C |
   +===+ \=== +===+ /    +===+
             \+ B +=
              +===+

    Becomes:

    +===+    +===+     +===+
    | A +====+ B +=====+ C |
    +===+    +===+     +===+

   The process operates in a recursive manner until all duplicates are
   replaced.

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
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xsd="http://www.w3.org/2001/XMLSchema"
    xmlns:check="http://www.rackspace.com/repose/wadl/checker"
    xmlns:map="http://www.w3.org/2005/xpath-functions/map"
    xmlns:util="http://www.rackspace.com/repose/wadl/checker/util"
    xmlns="http://www.rackspace.com/repose/wadl/checker" exclude-result-prefixes="xsd check"
    version="3.0">

    <xsl:import href="../util/funs.xsl"/>
    <xsl:import href="../util/pruneSteps.xsl"/>

    <xsl:include href="removeDups-rules.xsl"/>


    <xsl:output indent="yes" method="xml"/>

    <xsl:param name="configMetadata" as="node()">
        <params>
          <meta>
            <config option="preserveMethodLabels"
                    value="false"/>
          </meta>
        </params>
    </xsl:param>

    <xsl:variable name="preserveMethodLabels" as="xsd:boolean" select="xsd:boolean(check:optionValue($configMetadata, 'preserveMethodLabels'))"/>

    <xsl:template match="check:checker" name="replaceAllDups">
        <xsl:param name="checker" select="." as="node()"/>
        <xsl:variable name="dups" as="map(xsd:string, xsd:string*)" select="check:getDups($checker)"/>
        <xsl:choose>
            <xsl:when test="map:size($dups) = 0">
                <!-- No duplicats found, tidy up empty epsillon cases -->
                <checker>
                    <xsl:copy-of select="/check:checker/namespace::*"/>
                    <xsl:apply-templates select="/check:checker/check:meta" mode="copyMeta"/>
                    <xsl:copy-of select="/check:checker/check:grammar"/>
                    <xsl:apply-templates select="$checker" mode="epsilonRemove"/>
                </checker>
            </xsl:when>
            <xsl:otherwise>
                <xsl:call-template name="replaceAllDups">
                    <xsl:with-param name="checker">
                        <xsl:call-template name="replaceDups">
                            <xsl:with-param name="checker" select="util:pruneSteps($checker)"/>
                            <xsl:with-param name="dups" select="$dups"/>
                        </xsl:call-template>
                    </xsl:with-param>
                </xsl:call-template>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template match="@* | node()" mode="copyMeta" priority="10">
        <xsl:copy>
            <xsl:apply-templates select="@* | node()" mode="copyMeta"/>
        </xsl:copy>
    </xsl:template>

    <xsl:template match="check:checker" name="replaceEpsilons" mode="epsilonRemove">
        <xsl:param name="checker" select="." as="node()"/>
        <xsl:variable name="dups" as="map(xsd:string, xsd:string*)" select="check:getEpsilonDups($checker)"/>
        <xsl:choose>
            <xsl:when test="map:size($dups) = 0">
                <xsl:for-each select="$checker//check:step">
                    <xsl:copy-of select="."/>
                </xsl:for-each>
            </xsl:when>
            <xsl:otherwise>
                <xsl:call-template name="replaceEpsilons">
                    <xsl:with-param name="checker">
                        <xsl:call-template name="replaceDups">
                            <xsl:with-param name="checker" select="util:pruneSteps($checker)"/>
                            <xsl:with-param name="dups" select="$dups"/>
                        </xsl:call-template>
                    </xsl:with-param>
                </xsl:call-template>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template name="replaceDups">
        <xsl:param name="checker" as="node()"/>
        <xsl:param name="dups" as="map(xsd:string, xsd:string*)"/>
        <xsl:param name="excluded" as="map(xsd:string, xsd:string*)"
                   select="check:getExcludeMap($dups)"/>
        <checker>
            <xsl:apply-templates select="$checker" mode="unDup">
                <xsl:with-param name="checker" select="$checker"/>
                <xsl:with-param name="dups" select="$dups"/>
                <xsl:with-param name="excluded" select="$excluded"/>
            </xsl:apply-templates>
        </checker>
    </xsl:template>

    <xsl:function name="check:getExcludeMap" as="map(xsd:string, xsd:string*)">
        <xsl:param name="dups" as="map(xsd:string, xsd:string*)"/>
        <xsl:sequence select="map:merge(for $k in map:keys($dups) return
                                          for $d in $dups($k) return map{$d :  $k},map{'duplicates' : 'combine'})"/>
    </xsl:function>

    <xsl:template match="check:step" mode="unDup">
        <xsl:param name="checker" as="node()" />
        <xsl:param name="dups" as="map(xsd:string, xsd:string*)"/>
        <xsl:param name="excluded" as="map(xsd:string, xsd:string*)"/>
        <step>
            <xsl:apply-templates select="@*" mode="unDup">
                <xsl:with-param name="checker" select="$checker"/>
                <xsl:with-param name="dups" select="$dups"/>
                <xsl:with-param name="id" select="@id"/>
                <xsl:with-param name="excluded" select="$excluded"/>
            </xsl:apply-templates>
            <xsl:copy-of select="element()"/>
        </step>
    </xsl:template>

    <xsl:template match="@*" mode="unDup">
        <xsl:param name="checker" as="node()" />
        <xsl:param name="dups" as="map(xsd:string, xsd:string*)"/>
        <xsl:param name="excluded" as="map(xsd:string, xsd:string*)"/>
        <xsl:param name="id" as="xsd:string"/>
        <xsl:choose>
            <!-- Substitude excludes from nexts -->
            <xsl:when test="name() = 'next'">
                <xsl:variable name="nexts" as="xsd:string*" select="tokenize(., ' ')"/>
                <xsl:variable name="newNext" as="xsd:string*" select="distinct-values(for $n in $nexts return
                    if (map:contains($dups, $n)) then $dups($n) else $n)"/>
                <xsl:attribute name="next">
                    <xsl:value-of select="$newNext" separator=" "/>
                </xsl:attribute>
            </xsl:when>
            <!-- Copy labels if it is appropriate  to do so -->
            <xsl:when test="name() = 'label'">
                <xsl:choose>
                    <!--
                        Copy the label if there are no dups of this
                        step.
                    -->
                    <xsl:when test="not(map:contains($dups, $id))">
                        <xsl:copy/>
                    </xsl:when>
                    <!--
                        Don't copy the label if this step is being
                        replaced by multiple steps, likely the step
                        will be replaced anyway
                    -->
                    <xsl:when test="count($dups($id)) &gt; 1"/>
                    <!--
                        If this step is being replaced by a single
                        step, and it's replacing itself then copy the
                        label only if all other steps this step is
                        replacing have the same label.

                        This step is a target step, possibly replacing
                        a number of other steps, possibly with
                        different labels so keep the label only if
                        other labels match to avoid confusion.
                    -->
                    <xsl:when test="$dups($id) = $id">
                        <xsl:variable name="possibleLabel" as="xsd:string" select="string(.)"/>
                        <xsl:variable name="excludedLabels" as="xsd:string*" select="for $estep in $checker//check:step[@id=$excluded($id)] return if ($estep/@label) then string($estep/@label) else ()"/>
                        <xsl:if test="every $l in $excludedLabels satisfies $l = $possibleLabel">
                            <xsl:copy />
                        </xsl:if>
                    </xsl:when>
                    <!--
                        Otherwise don't bother copying the lable, this
                        step is being replaced anyway.
                    -->
                    <xsl:otherwise/>
                </xsl:choose>
            </xsl:when>
            <xsl:otherwise>
                <xsl:copy/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:function name="check:getEpsilonDups" as="map(xsd:string, xsd:string*)">
        <xsl:param name="checker" as="node()"/>
        <xsl:map>
            <!-- Treat epsilon methods as dups -->
            <xsl:for-each select="$checker//check:step[@type = 'METHOD']">
                <xsl:variable name="nexts" as="xsd:string*" select="distinct-values(tokenize(@next, ' '))"/>
                <xsl:variable name="nextStep" as="node()*"
                    select="$checker//check:step[@id = $nexts]"/>
                <xsl:variable name="nextStepsNonSink" as="node()*" select="$nextStep[not(@type=$sink-types)]"/>
                <xsl:if test="(not(empty($nextStepsNonSink))) and (every $s in $nextStepsNonSink
                    satisfies $s/@type = 'METHOD')">
                     <xsl:map-entry key="string(@id)" select="$nexts"/>
                </xsl:if>
            </xsl:for-each>
        </xsl:map>
    </xsl:function>


    <xsl:template match="text()" mode="#all"/>
</xsl:stylesheet>
