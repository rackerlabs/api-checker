<?xml version="1.0" encoding="UTF-8"?>
<!--
   funs.xsl

   This stylesheet contains common utility functions used by other
   checker stylesheets.

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
    exclude-result-prefixes="xs chk"
    version="2.0">

    <!--
        The following key is useful for retrivivg a checker step by an
        ID.
    -->
    <xsl:key name="checker-by-id" match="chk:step" use="@id"/>

    <!--
        The following key is useful for retriving a list of checker steps that
        refernce a particular step ID.
    -->
    <xsl:key name="checker-by-ref" match="chk:step" use="tokenize(@next,' ')"/>

    <!--
       The following key is useful for retriving a list of checker steps
       by their type.
    -->
    <xsl:key name="checker-by-type" match="chk:step" use="@type"/>

    <!--
        Sink types return an message error or accept.
    -->
    <xsl:variable name="sink-types" as="xs:string*" select="('URL_FAIL', 'METHOD_FAIL', 'CONTENT_FAIL',
                                                             'REQ_TYPE_FAIL', 'ACCEPT')"/>


    <!--
       These types set a content error.
    -->
    <xsl:variable name="cont-error-types" as="xs:string*" select="('WELL_XML','WELL_JSON', 'XSD',
                                                                   'XPATH', 'XSL', 'HEADER',
                                                                   'HEADERXSD', 'HEADER_ANY', 'HEADERXSD_ANY',
                                                                   'HEADER_SINGLE', 'HEADERXSD_SINGLE', 'HEADER_ALL',
                                                                   'JSON_SCHEMA')"/>

    <!--
        Given a step, returns a collection of ids to
        the connected steps.
    -->
    <xsl:function as="xs:string*" name="chk:next">
        <xsl:param name="step" as="node()"/>
        <xsl:sequence select="tokenize($step/@next, ' ')"/>
    </xsl:function>

    <!--
        Given a step, returns ids a the step's siblings:
        Steps that have the same parent step.
     -->
    <xsl:function name="chk:siblings" as="xs:string*">
        <xsl:param name="checker" as="node()"/>
        <xsl:param name="step" as="node()"/>
        <xsl:sequence select="for $parent in key('checker-by-ref',$step/@id, $checker)
                              return chk:next($parent)"/>
    </xsl:function>

    <!--
        Returns a copy of steps given a list of ids
     -->
    <xsl:function name="chk:stepsByIds" as="node()*">
        <xsl:param name="checker" as="node()"/>
        <xsl:param name="ids" as="xs:string*"/>
        <xsl:sequence select="for $id in $ids return key('checker-by-id', $id, $checker)"/>
    </xsl:function>

    <!--
        Given check:meta return a configuration option value.
    -->
    <xsl:function name="chk:optionValue" as="xs:string?">
        <xsl:param name="configMetadata" as="node()"/>
        <xsl:param name="option" as="xs:string"/>
        <xsl:value-of select="$configMetadata/chk:meta/chk:config[@option=$option]/@value"/>
    </xsl:function>

    <!--
        Given a string, convert it to a valid ID.
    -->
    <xsl:function as="xs:string" name="chk:string-to-id">
        <xsl:param name="in" as="xs:string"/>
        <xsl:choose>
            <xsl:when test="chk:is-valid-id($in)" >
                <!-- If the value is castable as an ID don't mess with it -->
                <xsl:sequence select="$in"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:sequence select="concat('_',chk:string-to-hex($in))"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:function>

    <!--
       Returns true if the string is a valid ID
    -->
    <xsl:function as="xs:boolean" name="chk:is-valid-id">
        <xsl:param name="in" as="xs:string"/>
        <xsl:choose>
            <xsl:when test="matches($in,'^[\i-[:]][\c-[:]]*$')" use-when="system-property('xsl:is-schema-aware') eq 'no'">
                <xsl:sequence select="true()"/>
            </xsl:when>
            <xsl:when test="$in castable as xs:ID" use-when="system-property('xsl:is-schema-aware') eq 'yes'">
                <xsl:sequence select="true()"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:sequence select="false()"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:function>

    <!--
        Given a unique id and a list of uri states (which may be empty)
        returns an appropriate URI_FAIL step
     -->
    <xsl:function name="chk:createURIErrorStep" as="node()">
        <xsl:param name="id" as="xs:string"/>
        <xsl:param name="uriSteps" as="node()*"/>
        <step id="{$id}" type="URL_FAIL">
            <xsl:if test="not(empty($uriSteps))">
                <xsl:variable name="notMatches" as="xs:string*">
                    <xsl:perform-sort select="$uriSteps[@type='URL']/@match">
                        <xsl:sort select="."/>
                    </xsl:perform-sort>
                </xsl:variable>
                <xsl:variable name="notXSDMatches" as="xs:QName*">
                    <xsl:perform-sort select="for $s in $uriSteps[@type='URLXSD'] return resolve-QName($s/@match, $s)">
                        <xsl:sort select="concat(local-name-from-QName(.),'_',namespace-uri-from-QName(.))"/>
                    </xsl:perform-sort>
                </xsl:variable>
                <xsl:if test="not(empty($notMatches))">
                    <xsl:attribute name="notMatch">
                        <xsl:value-of select="$notMatches" separator="|"/>
                    </xsl:attribute>
                </xsl:if>
                <xsl:if test="not(empty($notXSDMatches))">
                    <xsl:attribute name="notTypes">
                        <xsl:value-of select="$notXSDMatches" separator=" "/>
                    </xsl:attribute>
                </xsl:if>
            </xsl:if>
        </step>
    </xsl:function>

    <!--
        Given a unique id and a list of method states (which may be empty)
        returns an appropriate METHOD_FAIL step
     -->
    <xsl:function name="chk:createMethodErrorStep" as="node()">
        <xsl:param name="id" as="xs:string"/>
        <xsl:param name="methodSteps" as="node()*"/>
        <step id="{$id}" type="METHOD_FAIL">
            <xsl:if test="not(empty($methodSteps))">
                <xsl:variable name="notMatchValues" as="xs:string*">
                    <xsl:perform-sort select="$methodSteps/@match">
                        <xsl:sort select="."/>
                    </xsl:perform-sort>
                </xsl:variable>
                <xsl:attribute name="notMatch">
                    <xsl:value-of select="$notMatchValues" separator="|"/>
                </xsl:attribute>
            </xsl:if>
        </step>
    </xsl:function>

    <!--
        Given a unique ida and a list of reqtype states
        returns an appropriate REQ_TYPE_FAIL step
     -->
    <xsl:function name="chk:createReqTypeErrorStep" as="node()">
        <xsl:param name="id" as="xs:string"/>
        <xsl:param name="reqTypeSteps" as="node()+"/>
        <step id="{$id}" type="REQ_TYPE_FAIL">
            <xsl:variable name="notMatchValues" as="xs:string*" select="$reqTypeSteps/@match"/>
            <xsl:attribute name="notMatch">
                <xsl:value-of select="$notMatchValues" separator="|"/>
            </xsl:attribute>
        </step>
    </xsl:function>

    <!--
       Hexadecimal encode a string.
    -->
    <xsl:function as="xs:string" name="chk:string-to-hex">
        <xsl:param name="in" as="xs:string"/>
        <xsl:sequence select="string-join(for $i in string-to-codepoints($in)
                                          return chk:int-to-hex($i), '')"/>
    </xsl:function>

    <!--
      Converts an integer to a hex string.

      This clever function was borrowed from Yves Forkl and Michael
      Kay:
      http://www.oxygenxml.com/archives/xsl-list/200902/msg00215.html
    -->
    <xsl:function name="chk:int-to-hex" as="xs:string">
        <xsl:param name="in" as="xs:integer"/>
        <xsl:sequence
        select="if ($in eq 0)
                then '0'
                else
                concat(if ($in gt 16)
                then chk:int-to-hex($in idiv 16)
                else '',
                substring('0123456789ABCDEF',
                ($in mod 16) + 1, 1))"/>
    </xsl:function>

</xsl:stylesheet>
