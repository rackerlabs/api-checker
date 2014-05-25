<?xml version="1.0" encoding="UTF-8"?>
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
