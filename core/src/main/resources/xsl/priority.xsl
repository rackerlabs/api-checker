<?xml version="1.0" encoding="UTF-8"?>
<!--
    priority.xsl

    This stylesheet is responsible for setting priorities in steps
    that need them (sink types and content error types). Priorites are
    used to determine what error message to display when more then one
    error message may apply. The priority is simply the sum of the
    priority value specified in the priority-map.xml, the @multValue
    of any attribute multipliers, and the largest distance in steps
    from start, it is computed in the chkp:priority function.

    This transform should be run at the end of the pipeline, after all
    optimization stages, but before validation.

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
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    xmlns:chk="http://www.rackspace.com/repose/wadl/checker"
    xmlns="http://www.rackspace.com/repose/wadl/checker"
    xmlns:chkp="http://www.rackspace.com/repose/wadl/checker/priority"
    exclude-result-prefixes="xs"
    version="2.0">

    <xsl:import href="util/funs.xsl"/>

    <xsl:key name="type-to-priority" match="chkp:map" use="@type"/>

    <xsl:variable name="priority-types" as="xs:string*" select="($sink-types, $cont-error-types)"/>
    <xsl:variable name="priority-map" select="document('priority-map.xml')/chkp:priority-map" as="node()"/>

    <xsl:template match="chk:step[@type='START']">
        <xsl:variable name="prioritySteps" as="node()*">
            <xsl:call-template name="copyPrioritySteps">
                <xsl:with-param name="priority" select="0"/>
            </xsl:call-template>
        </xsl:variable>
        <xsl:copy>
            <xsl:apply-templates select="@* | node()"/>
        </xsl:copy>
        <xsl:for-each-group select="$prioritySteps" group-by="@id">
            <xsl:variable name="maxPriority" select="max(for $p in current-group()/@priority return xs:integer($p))"/>
            <xsl:copy-of select="current-group()[@priority = $maxPriority][1]"/>
        </xsl:for-each-group>
    </xsl:template>

    <!--
        Copies priority steps where the priority is the distance from
        the start node.  NOTE: Assumes the graph is acyclic, if we
        ever add cycles we'll need to adjust.  Keeping things simple
        for now.
    -->
    <xsl:template name="copyPrioritySteps">
        <xsl:param name="priority" as="xs:integer"/>
        <xsl:variable name="currentStep" as="node()" select="."/>
        <xsl:variable name="nexts" as="node()*" select="for $n in chk:next(.) return key('checker-by-id',$n)"/>
        <xsl:variable name="currentType" as="xs:string" select="@type"/>
        <xsl:if test="$currentStep[$currentType=$priority-types]">
            <xsl:copy>
                <xsl:apply-templates select="@*[not(local-name() = 'priority')]"/>
                <xsl:attribute name="priority" select="chkp:priority($currentStep, $priority)"/>
                <xsl:apply-templates select="node()"/>
            </xsl:copy>
        </xsl:if>
        <xsl:for-each select="$nexts">
            <xsl:call-template name="copyPrioritySteps">
                <xsl:with-param name="priority" select="$priority+1"/>
            </xsl:call-template>
        </xsl:for-each>
    </xsl:template>

    <!-- Don't copy sink types directly -->
    <xsl:template match="chk:step[@type=$priority-types]"/>

    <!-- Copy everything else -->
    <xsl:template match="@* | node()">
        <xsl:copy>
            <xsl:apply-templates select="@* | node()"/>
        </xsl:copy>
    </xsl:template>

    <!--
        Computes actual attribute value given:

        1. The current step
        2. Current priority
        3. Priority values in priority-map.xml
    -->
    <xsl:function name="chkp:priority" as="xs:integer">
        <xsl:param as="node()" name="step"/>
        <xsl:param as="xs:integer" name="inPriority"/>
        <xsl:variable name="map" as="node()?" select="key('type-to-priority',$step/@type, $priority-map)"/>
        <xsl:if test="not($map)">
            <xsl:message  terminate="yes">[ERROR] Cannot find priority information for step type <xsl:value-of select="$step/@type"/> that's very strange</xsl:message>
        </xsl:if>
        <xsl:choose>
            <xsl:when test="$step/@priority and not($step/@priority = 0)">
                <xsl:value-of select="$step/@priority"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:variable name="offsets" as="xs:integer*">
                    <xsl:choose>
                        <xsl:when test="$map/@attributeMultipliers and $map/@multValue">
                            <xsl:variable name="mults" as="xs:string*" select="tokenize($map/@attributeMultipliers,' ')"/>
                            <xsl:for-each select="$mults">
                                <xsl:variable name="attr" as="xs:string" select="."/>
                                <xsl:if test="$step[@*[local-name() = $attr]]">
                                    <xsl:for-each select="chkp:matchList($step/@*[local-name() = $attr])">
                                        <xsl:value-of select="xs:integer($map/@multValue)"/>
                                    </xsl:for-each>
                                </xsl:if>
                            </xsl:for-each>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:value-of select="0"/>
                        </xsl:otherwise>
                    </xsl:choose>
                </xsl:variable>
                <xsl:message>[DEBUG] ID=<xsl:value-of select="$step/@id"/> inPriority=<xsl:value-of select="$inPriority"/> offsets=<xsl:value-of select="$offsets"/> priority=<xsl:value-of select="$map/@priority"/> total: <xsl:value-of select="sum(($inPriority,$offsets,$map/@priority))"/></xsl:message>
                <xsl:value-of select="sum(($inPriority,$offsets,$map/@priority))"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:function>

    <!--
        Split matches by ' ' and '|' since this is how matches
        are normally grouped.
    -->
    <xsl:function name="chkp:matchList" as="xs:string*">
        <xsl:param as="xs:string" name="match"/>
        <xsl:sequence select="for $t in tokenize($match,' ') return tokenize($t,'\|')"/>
    </xsl:function>
</xsl:stylesheet>
