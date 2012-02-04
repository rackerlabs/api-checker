<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet 
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xsd="http://www.w3.org/2001/XMLSchema"
    xmlns:wadl="http://wadl.dev.java.net/2009/02"
    xmlns:chk="http://www.rackspace.com/repose/wadl/checker"
    xmlns:rax="http://docs.rackspace.com/api"
    xmlns:saxon="http://saxon.sf.net/"
    xmlns="http://www.rackspace.com/repose/wadl/checker"
    exclude-result-prefixes="xsd wadl rax chk"
    extension-element-prefixes="saxon"
    version="2.0">

    <xsl:param name="sid" as="xsd:string" select="'S0'"/>
    <xsl:param name="max" as="xsd:integer" select="2147483647"/>
    <xsl:variable name="step" as="node()" select="/chk:checker/chk:step[@id=$sid]"/>
    <xsl:variable name="d" as="node()" select="/chk:checker"/>

    <xsl:output indent="yes" method="xml"/>

    <xsl:template match="chk:checker">
        <xsl:variable name="path" as="node()">
            <checker>
                <xsl:call-template name="chk:followPaths">
                    <xsl:with-param name="step" select="$step"/>
                    <xsl:with-param name="max" select="$max"/>
                </xsl:call-template>
            </checker>
        </xsl:variable>
        <checker>
            <xsl:for-each-group select="$path/chk:step" group-by="@id">
                <xsl:copy-of select="current-group()[1]"/>
            </xsl:for-each-group>
        </checker>
    </xsl:template>

    <xsl:template name="chk:followPaths">
        <xsl:param name="step" as="node()"/>
        <xsl:param name="max" as="xsd:integer"/>

        <xsl:if test="$max &gt; 0">
            <xsl:copy-of select="$step"/>
            <xsl:variable name="nexts" as="xsd:string*" select="tokenize($step/@next,' ')"/>
            <xsl:variable name="id" as="xsd:string" select="$step/@id"/>
            <xsl:for-each select="$nexts">
                <xsl:variable name="next" select="."/>
                <xsl:call-template name="chk:followPaths">
                    <xsl:with-param name="step" select="$d/chk:step[@id=$next]"/>
                    <xsl:with-param name="max" select="$max - 1"/>
                </xsl:call-template>
            </xsl:for-each>
        </xsl:if>
    </xsl:template>

</xsl:stylesheet>
