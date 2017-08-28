<?xml version="1.0" encoding="UTF-8"?>
<!--
  joinSetupTemplate.xsl

  This template is called at compile time to build
  removeDups-rules.setup.xsl this contains auto-generated code to
  to annotate optional attributes for the purpose of speeding up join
  optimization stages.

  The input to the template is removeDups-rules.xml which contains
  rules for how states can be combined.

  Copyright 2015 Rackspace US, Inc.

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
    xmlns="http://www.rackspace.com/repose/wadl/checker/opt/removeDups/rules"
    xmlns:rules="http://www.rackspace.com/repose/wadl/checker/opt/removeDups/rules"
    xmlns:check="http://www.rackspace.com/repose/wadl/checker"
    xmlns:xslout="http://www.rackspace.com/repose/wadl/checker/Transform"
    exclude-result-prefixes="rules"
    version="2.0">

    <xsl:namespace-alias stylesheet-prefix="xslout" result-prefix="xsl"/>
    <xsl:output method="xml" encoding="UTF-8" indent="yes"/>
    <xsl:variable name="EMPTY" as="xs:string">[{[!EMPTY!]}]</xsl:variable>
    <xsl:template match="rules:rules">
        <xsl:comment>
            **********                                                    **********
            ********** THIS IS A GENERATED STYLESHEET DO NOT EDIT BY HAND **********
            **********                                                    **********
        </xsl:comment>
        <xsl:text>&#xa;</xsl:text>
        <xslout:stylesheet
            xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
            xmlns:xsd="http://www.w3.org/2001/XMLSchema"
            xmlns:check="http://www.rackspace.com/repose/wadl/checker"
            xmlns="http://www.rackspace.com/repose/wadl/checker"
            exclude-result-prefixes="xsd check"
            version="2.0">

            <xslout:template match="@* | node()">
                <xslout:copy>
                    <xslout:apply-templates select="@* | node()"/>
                </xslout:copy>
            </xslout:template>

            <xsl:apply-templates/>

        </xslout:stylesheet>
    </xsl:template>

    <xsl:template match="rules:rule">
        <xsl:variable name="types"      as="xs:string*" select="tokenize(@types,' ')"/>
        <xsl:variable name="optionals"  as="xs:string*" select="tokenize(@optional,' ')"/>
        <xsl:variable name="sq" as="xs:string">'</xsl:variable>
        <xsl:variable name="qtypes" as="xs:string*" select="for $t in $types return concat($sq,$t,$sq)"/>
        <xsl:variable name="typematch" as="xs:string"><xsl:value-of select="$qtypes" separator=", "/></xsl:variable>
        <xslout:template match="check:step[@type=({$typematch})]">
            <xslout:copy>
                <xsl:for-each select="$optionals">
                    <xslout:if test="not(@{.})">
                        <xslout:attribute name="{.}" select="'{$EMPTY}'"/>
                    </xslout:if>
                </xsl:for-each>
                <xslout:apply-templates select="@* | node()"/>
            </xslout:copy>
        </xslout:template>
    </xsl:template>
    <xsl:template match="text()"/>

</xsl:stylesheet>
