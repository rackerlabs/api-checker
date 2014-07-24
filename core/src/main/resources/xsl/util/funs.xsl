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
    exclude-result-prefixes="xs"
    version="2.0">

    <!--
        The following key is useful for retrivivg a checker step by an
        ID.
    -->
    <xsl:key name="checker-by-id" match="chk:step" use="@id"/>

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
