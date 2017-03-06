<?xml version="1.0" encoding="UTF-8"?>
<!--
  adjust-next-cont-error.xsl

  This stylesheet is responsible for adjusting next step links so that
  content error type steps are visited in order of priority, from
  least to greatest.

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
                exclude-result-prefixes="xs"
                version="2.0">

    <xsl:import href="util/funs.xsl"/>

    <!-- Adjust only connected steps -->
    <xsl:template match="chk:step[@next]">
        <xsl:copy>
            <xsl:apply-templates select="@*[local-name() != 'next']"/>
            <xsl:call-template name="sortedNextSteps"/>
            <xsl:apply-templates select="node()"/>
        </xsl:copy>
    </xsl:template>

    <xsl:template name="sortedNextSteps">
        <xsl:variable name="nexts" as="node()*" select="for $n in chk:next(.) return key('checker-by-id',$n)"/>
        <xsl:choose>
            <!--
                Only bother sorting if we have more than one
                cont-error-type in the list.
            -->
            <xsl:when test="$nexts[@type=$cont-error-types]">
                <xsl:variable name="other" as="xs:string*" select="$nexts[not(@type = $cont-error-types) and not(@type = $sink-types)]/@id"/>
                <xsl:variable name="sink" as="xs:string*" select="$nexts[@type=$sink-types]/@id"/>
                <!--
                    New next sorted as follows:
                    1. Non Content Error types
                    2. Content Error Types sorted by priority
                    3. Sink Types
                -->
                <xsl:variable name="newNexts" as="xs:string*">
                    <xsl:sequence select="$other"/>
                    <xsl:for-each select="$nexts[@type=$cont-error-types]">
                        <xsl:sort select="@priority" data-type="number"/>
                        <xsl:value-of select="@id"/>
                    </xsl:for-each>
                    <xsl:sequence select="$sink"/>
                </xsl:variable>
                <xsl:attribute name="next">
                    <xsl:value-of select="$newNexts" separator=" "/>
                </xsl:attribute>
            </xsl:when>
            <xsl:otherwise>
                <xsl:apply-templates select="@next"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <!-- Copy everything else -->
    <xsl:template match="@* | node()">
        <xsl:copy>
            <xsl:apply-templates select="@* | node()"/>
        </xsl:copy>
    </xsl:template>

</xsl:stylesheet>
