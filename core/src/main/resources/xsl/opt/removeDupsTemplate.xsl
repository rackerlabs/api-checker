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
           <xslout:template name="getDups" as="node()">
               <xslout:param name="checker" as="node()"/>
               <check:checker>
                   <xsl:for-each select="$types">
                       <xsl:variable name="rule" as="node()" select="key('rule-by-type', ., $rules)"/>
                       <xsl:variable name="required" as="xs:string*" select="tokenize($rule/@required,' ')"/>
                       <xsl:variable name="optional" as="xs:string*" select="tokenize($rule/@optional,' ')"/>
                       <xsl:variable name="match" as="xs:string?" select="$rule/@match"/>
                       <xsl:choose>
                           <xsl:when test="empty($optional) and empty($required)">
                               <xslout:variable name="{.}" as="node()*"
                                   select="key('checker-by-type','{.}', $checker){if ($match) then concat('[',$match,']') else ()}"/>
                               <xslout:if test="count(${.}) > 1">
                                   <check:group>
                                       <xslout:attribute name="include">
                                           <xslout:value-of select="${.}[1]/@id"></xslout:value-of>
                                       </xslout:attribute>
                                       <xslout:attribute name="exclude">
                                           <xslout:value-of separator=" ">
                                               <xslout:sequence select="subsequence(${.},2)/@id"></xslout:sequence>
                                           </xslout:value-of>
                                       </xslout:attribute>
                                   </check:group>
                               </xslout:if>
                           </xsl:when>
                           <xsl:otherwise>
                               <xsl:call-template name="matchTemplates">
                                   <xsl:with-param name="type" select="."/>
                                   <xsl:with-param name="required" select="if (empty($required)) then 'type' else $required"/>
                                   <xsl:with-param name="optional" select="$optional"/>
                                   <xsl:with-param name="currentMatch" select="()"/>
                                   <xsl:with-param name="match" select="$match"/>
                               </xsl:call-template>
                           </xsl:otherwise>
                       </xsl:choose>
                   </xsl:for-each>
               </check:checker>
           </xslout:template>
        </xslout:stylesheet>
    </xsl:template>

    <xsl:template name="matchTemplates">
        <xsl:param name="type" as="xs:string"/>
        <xsl:param name="required" as="xs:string*"/>
        <xsl:param name="optional" as="xs:string*"/>
        <xsl:param name="currentMatch" as="xs:string*"/>
        <xsl:param name="match" as="xs:string?"/>
        <xsl:choose>
            <xsl:when test="empty($optional)">
                <xslout:for-each-group select="key('checker-by-type','{$type}', $checker){if ($match) then concat('[',$match,']') else ()}"
                        group-by="@{$required[1]}">
                    <xsl:call-template name="forEachTemplate">
                        <xsl:with-param name="attribs" select="subsequence($required,2)"/>
                    </xsl:call-template>
                </xslout:for-each-group>
            </xsl:when>
            <xsl:when test="count($optional) = count($currentMatch)">
                <xsl:variable name="allAttribs" as="xs:string*" select="($required, for $o in $optional return
                                                                         if (concat('@',$o) = $currentMatch) then $o else ())"/>
                <xsl:variable name="matchString" as="xs:string" select="string-join(($currentMatch, $match), ' and ')"/>
                <xslout:for-each-group select="key('checker-by-type','{$type}', $checker)[{$matchString}]" group-by="@{$allAttribs[1]}">
                    <xsl:call-template name="forEachTemplate">
                        <xsl:with-param name="attribs" select="subsequence($allAttribs,2)"/>
                    </xsl:call-template>
                </xslout:for-each-group>
            </xsl:when>
            <xsl:otherwise>
                <xsl:variable name="newMatch" as="xs:string" select="$optional[count($currentMatch)+1]"/>
                <xsl:call-template name="matchTemplates">
                    <xsl:with-param name="type" select="$type"/>
                    <xsl:with-param name="required" select="$required"/>
                    <xsl:with-param name="optional" select="$optional"/>
                    <xsl:with-param name="currentMatch" select="($currentMatch, concat('@',$newMatch))"/>
                    <xsl:with-param name="match" select="$match"/>
                </xsl:call-template>
                <xsl:call-template name="matchTemplates">
                    <xsl:with-param name="type" select="$type"/>
                    <xsl:with-param name="required" select="$required"/>
                    <xsl:with-param name="optional" select="$optional"/>
                    <xsl:with-param name="currentMatch" select="($currentMatch, concat('not(@',$newMatch,')'))"/>
                    <xsl:with-param name="match" select="$match"/>
                </xsl:call-template>
            </xsl:otherwise>
        </xsl:choose>
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
                    <check:group>
                        <xslout:attribute name="include">
                            <xslout:value-of select="current-group()[1]/@id"/>
                        </xslout:attribute>
                        <xslout:attribute name="exclude">
                            <xslout:value-of separator=" ">
                                <xslout:sequence select="subsequence(current-group(), 2)/@id"/>
                            </xslout:value-of>
                        </xslout:attribute>
                    </check:group>
                </xsl:otherwise>
            </xsl:choose>
        </xslout:if>
    </xsl:template>
</xsl:stylesheet>
