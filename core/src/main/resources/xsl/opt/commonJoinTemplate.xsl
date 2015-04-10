<?xml version="1.0" encoding="UTF-8"?>
<!--
  commonJoinTemplate.xsl

  This template is called at compile time to build
  removeDups-rules.common.xsl this contains auto-generated code to
  select common join candidates for optimization.

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

    <xsl:variable name="rules" as="node()" select="/rules:rules"/>
    <xsl:variable name="types" as="xs:string*" select="distinct-values(tokenize(string-join($rules/rules:rule/@types, ' '), ' '))"/>

    <xsl:key name="rule-by-type" match="rules:rule" use="tokenize(@types,' ')"/>

    <xsl:template match="/">
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
            <xslout:template match="check:step[@next]" mode="getJoins">
                <xslout:param name="checker" as="node()"/>
                <xslout:variable name="nextStep" as="node()*" select="check:stepsByIds($checker, check:next(.))"/>
                <xsl:for-each select="$types">
                    <xsl:variable name="rule" as="node()" select="key('rule-by-type', ., $rules)"/>
                    <xsl:variable name="required" as="xs:string*" select="for $r in tokenize($rule/@required,' ') return
                                                                          (: Don't treat next as a required attribute, we are joining by it :)
                                                                          if ($r = 'next') then () else $r"/>
                    <xsl:variable name="optional" as="xs:string*" select="tokenize($rule/@optional,' ')"/>
                    <xsl:variable name="match" as="xs:string?" select="$rule/@match"/>
                    <xsl:call-template name="matchJoinTemplates">
                        <xsl:with-param name="type" select="."/>
                        <xsl:with-param name="required" select="if (empty($required)) then 'type' else $required"/>
                        <xsl:with-param name="optional" select="$optional"/>
                        <xsl:with-param name="currentMatch" select="()"/>
                        <xsl:with-param name="match" select="$match"/>
                    </xsl:call-template>
                </xsl:for-each>
            </xslout:template>
        </xslout:stylesheet>
    </xsl:template>
    <xsl:template name="matchJoinTemplates">
        <xsl:param name="type" as="xs:string"/>
        <xsl:param name="required" as="xs:string*"/>
        <xsl:param name="optional" as="xs:string*"/>
        <xsl:param name="currentMatch" as="xs:string*"/>
        <xsl:param name="match" as="xs:string?"/>
        <xsl:choose>
            <xsl:when test="empty($optional)">
                <xslout:for-each-group group-by="@{$required[1]}">
                    <xsl:attribute name="select">
                        <xsl:text>$nextStep[@type='</xsl:text>
                        <xsl:value-of select="$type"/>
                        <xsl:text>' </xsl:text>
                        <xsl:if test="$match">
                            <xsl:text>and </xsl:text>
                            <xsl:value-of select="$match"/>
                        </xsl:if>
                        <xsl:text>]</xsl:text>
                    </xsl:attribute>
                    <xsl:call-template name="forEachJoinTemplate">
                        <xsl:with-param name="attribs" select="subsequence($required,2)"/>
                    </xsl:call-template>
                </xslout:for-each-group>
            </xsl:when>
            <xsl:when test="count($optional) = count($currentMatch)">
                <xsl:variable name="allAttribs" as="xs:string*" select="($required, for $o in $optional return
                                                                         if (concat('@',$o) = $currentMatch) then $o else ())"/>
                <xsl:variable name="matchString" as="xs:string" select="string-join(($currentMatch, $match), ' and ')"/>
                <xslout:for-each-group group-by="@{$allAttribs[1]}">
                    <xsl:attribute name="select">
                        <xsl:text>$nextStep[@type ='</xsl:text>
                        <xsl:value-of select="$type"/>
                        <xsl:text>' </xsl:text>
                        <xsl:text>and </xsl:text>
                        <xsl:value-of select="$matchString"/>
                        <xsl:text>]</xsl:text>
                    </xsl:attribute>
                    <xsl:call-template name="forEachJoinTemplate">
                        <xsl:with-param name="attribs" select="subsequence($allAttribs,2)"/>
                    </xsl:call-template>
                </xslout:for-each-group>
            </xsl:when>
            <xsl:otherwise>
                <xsl:variable name="newMatch" as="xs:string" select="$optional[count($currentMatch)+1]"/>
                <xsl:call-template name="matchJoinTemplates">
                    <xsl:with-param name="type" select="$type"/>
                    <xsl:with-param name="required" select="$required"/>
                    <xsl:with-param name="optional" select="$optional"/>
                    <xsl:with-param name="currentMatch" select="($currentMatch, concat('@',$newMatch))"/>
                    <xsl:with-param name="match" select="$match"/>
                </xsl:call-template>
                <xsl:call-template name="matchJoinTemplates">
                    <xsl:with-param name="type" select="$type"/>
                    <xsl:with-param name="required" select="$required"/>
                    <xsl:with-param name="optional" select="$optional"/>
                    <xsl:with-param name="currentMatch" select="($currentMatch, concat('not(@',$newMatch,')'))"/>
                    <xsl:with-param name="match" select="$match"/>
                </xsl:call-template>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template name="forEachJoinTemplate">
        <xsl:param name="attribs" as="xs:string*"/>
        <xslout:if test="count(current-group()) > 1">
            <xsl:choose>
                <xsl:when test="count($attribs) != 0">
                    <xsl:variable name="current" as="xs:string" select="$attribs[1]"/>
                    <xsl:variable name="next" as="xs:string*" select="subsequence($attribs, 2)"/>
                    <xslout:for-each-group select="current-group()" group-by="@{$current}">
                        <xsl:call-template name="forEachJoinTemplate">
                            <xsl:with-param name="attribs" select="$next"/>
                        </xsl:call-template>
                    </xslout:for-each-group>
                </xsl:when>
                <xsl:otherwise>
                    <check:join>
                        <xslout:attribute name="steps">
                            <xslout:value-of separator=" ">
                                <xslout:sequence select="current-group()/@id"/>
                            </xslout:value-of>
                        </xslout:attribute>
                    </check:join>
                </xsl:otherwise>
            </xsl:choose>
        </xslout:if>
    </xsl:template>
</xsl:stylesheet>
