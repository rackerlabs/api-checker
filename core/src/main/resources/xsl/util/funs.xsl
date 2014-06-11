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


</xsl:stylesheet>
