<?xml version="1.0" encoding="UTF-8"?>
<!--
  removeDupsTemplate.xsl

  This template is called at compile time to build
  removeDups-rules.xsl this contains auto-generated code to
  select merge candidates for optimization.

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
    exclude-result-prefixes="rules" version="3.0">

    <xsl:namespace-alias stylesheet-prefix="xslout" result-prefix="xsl"/>
    <xsl:output method="xml" encoding="UTF-8" indent="yes"/>

    <xsl:variable name="pfx" as="xs:string" select="'TMP-'"/>
    <xsl:variable name="rules" as="node()" select="/rules:rules"/>
    <xsl:variable name="types" as="xs:string*"
        select="distinct-values(tokenize(string-join($rules/rules:rule/@types, ' '), ' '))"/>

    <xsl:key name="rule-by-type" match="rules:rule" use="tokenize(@types, ' ')"/>

    <xsl:template match="/">
        <xsl:comment>
            **********                                                    **********
            ********** THIS IS A GENERATED STYLESHEET DO NOT EDIT BY HAND **********
            **********                                                    **********
</xsl:comment>
        <xsl:text>&#xa;</xsl:text>
        <xslout:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
            xmlns:xsd="http://www.w3.org/2001/XMLSchema"
            xmlns:check="http://www.rackspace.com/repose/wadl/checker"
            xmlns:map="http://www.w3.org/2005/xpath-functions/map"
            xmlns="http://www.rackspace.com/repose/wadl/checker" exclude-result-prefixes="xsd check"
            version="3.0">
            <xslout:function name="check:getDups" as="map(xs:string, xs:string*)">
                <xslout:param name="checker" as="node()"/>
                <xslout:sequence>
                    <xsl:attribute name="select">
                        <xsl:text>map:merge((</xsl:text>
                        <xsl:for-each select="$types">
                            <xsl:text>check:</xsl:text>
                            <xsl:value-of select="$pfx"/>
                            <xsl:value-of select="."/>
                            <xsl:text>($checker)</xsl:text>
                            <xsl:if test="position() != last()">
                                <xsl:text>, </xsl:text>
                            </xsl:if>
                        </xsl:for-each>
                        <xsl:text>))</xsl:text>
                    </xsl:attribute>
                </xslout:sequence>
            </xslout:function>
            <xsl:for-each select="$types">
                <xsl:variable name="rule" as="node()" select="key('rule-by-type', ., $rules)"/>
                <xsl:variable name="required" as="xs:string*"
                    select="
                        (if (empty($rule/@required)) then
                            'type'
                        else
                            tokenize($rule/@required, ' '),
                        tokenize($rule/@optional, ' '))"/>
                <xsl:variable name="match" as="xs:string?" select="$rule/@match"/>
                <xslout:function name="check:{$pfx}{.}" as="map(xs:string, xs:string*)">
                    <xslout:param name="checker" as="node()"/>
                    <xslout:map>
                        <xsl:choose>
                            <xsl:when test="empty($required)">
                                <xslout:variable name="{.}" as="node()*"
                                    select="key('checker-by-type','{.}', $checker){if ($match) then concat('[',$match,']') else ()}"/>
                                <xslout:if test="count(${.}) > 1">
                                    <xslout:variable name="include" as="xs:string"
                                        select="${.}[1]/@id"/>
                                    <xslout:for-each select="${.}/@id">
                                        <xslout:map-entry key="string(.)" select="$include"/>
                                    </xslout:for-each>
                                </xslout:if>
                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:call-template name="matchTemplates">
                                    <xsl:with-param name="type" select="."/>
                                    <xsl:with-param name="required" select="$required"/>
                                    <xsl:with-param name="currentMatch" select="()"/>
                                    <xsl:with-param name="match" select="$match"/>
                                </xsl:call-template>
                            </xsl:otherwise>
                        </xsl:choose>
                    </xslout:map>
                </xslout:function>
            </xsl:for-each>
        </xslout:stylesheet>
    </xsl:template>

    <xsl:template name="matchTemplates">
        <xsl:param name="type" as="xs:string"/>
        <xsl:param name="required" as="xs:string*"/>
        <xsl:param name="currentMatch" as="xs:string*"/>
        <xsl:param name="match" as="xs:string?"/>

        <xslout:for-each-group
            select="key('checker-by-type','{$type}', $checker){if ($match) then concat('[',$match,']') else ()}"
            group-by="@{$required[1]}">
            <xsl:call-template name="forEachTemplate">
                <xsl:with-param name="attribs" select="subsequence($required, 2)"/>
            </xsl:call-template>
        </xslout:for-each-group>
    </xsl:template>

    <xsl:template name="forEachTemplate">
        <xsl:param name="attribs" as="xs:string*"/>
        <xslout:if test="count(current-group()) > 1">
            <xsl:choose>
                <xsl:when test="count($attribs) != 0">
                    <xsl:variable name="current" as="xs:string" select="$attribs[1]"/>
                    <xsl:variable name="next" as="xs:string*" select="subsequence($attribs, 2)"/>
                    <xslout:for-each-group select="current-group()" group-by="@{$current}">
                        <xsl:call-template name="forEachTemplate">
                            <xsl:with-param name="attribs" select="$next"/>
                        </xsl:call-template>
                    </xslout:for-each-group>
                </xsl:when>
                <xsl:otherwise>
                    <xslout:variable name="include" as="xs:string" select="current-group()[1]/@id"/>
                    <xslout:for-each select="current-group()/@id">
                        <xslout:map-entry key="string(.)" select="$include"/>
                    </xslout:for-each>
                </xsl:otherwise>
            </xsl:choose>
        </xslout:if>
    </xsl:template>
</xsl:stylesheet>
