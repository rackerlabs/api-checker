<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet 
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xsd="http://www.w3.org/2001/XMLSchema"
    xmlns:wadl="http://wadl.dev.java.net/2009/02"
    xmlns:check="http://www.rackspace.com/repose/wadl/checker"
    xmlns:rax="http://docs.rackspace.com/api"
    xmlns="http://www.rackspace.com/repose/wadl/checker"
    exclude-result-prefixes="xsd wadl"
    version="2.0">
    
    <xsl:output indent="yes" method="xml"/>
    
    <!-- Defaults Steps -->
    <xsl:variable name="START"       select="'S0'"/>
    <xsl:variable name="URL_FAIL"    select="'SE0'"/>
    <xsl:variable name="METHOD_FAIL" select="'SE1'"/>
    <xsl:variable name="ACCEPT"      select="'SA'"/>
    
    <xsl:template match="wadl:application">
        <checker>
            <step id="{$START}" type="START">
                <xsl:attribute name="next">
                    <xsl:value-of select="check:getNextURLLinks(wadl:resources)" separator=" "/>
                </xsl:attribute>
            </step>
            <xsl:apply-templates/>
            <step id="{$URL_FAIL}"    type="URL_FAIL"/>
            <step id="{$METHOD_FAIL}" type="METHOD_FAIL"/>
            <step id="{$ACCEPT}" type="ACCEPT"/>
        </checker>
    </xsl:template>
        
    <xsl:template match="wadl:resource">
        <xsl:variable name="links" as="xsd:string*">
            <xsl:sequence select="check:getNextURLLinks(.)"/>
        </xsl:variable>
        <step type="URL">
            <xsl:attribute name="id" select="generate-id()"/>
            <xsl:attribute name="match">
                <xsl:choose>
                    <xsl:when test="starts-with(@path,'{')">
                        <!-- Handle Templates -->
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:value-of select="check:toRegExEscaped(@path)"/>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:attribute>
            <xsl:attribute name="next" select="$links" separator=" "/>
        </step>
        <xsl:apply-templates/>
    </xsl:template>
    
    <xsl:template match="text()"/>
    
    <xsl:function name="check:toRegExEscaped" as="xsd:string">
        <xsl:param name="in" as="xsd:string"/>
        <xsl:value-of select="replace($in,'\.|\\|\(|\)|\{|\}|\[|\]|\?|\+|\^|\$|#','\\$0')"/>
    </xsl:function>
        
    <xsl:function name="check:getNextURLLinks" as="xsd:string*">
        <xsl:param name="from" as="node()"/>
        <xsl:sequence select="(check:nextURLLinks($from), $URL_FAIL)"/>
    </xsl:function>
    
    <xsl:function name="check:nextURLLinks" as="xsd:string*">
        <xsl:param name="from" as="node()"/>
        <xsl:sequence select="for $r in $from/wadl:resource return 
            if (xsd:boolean($r/@rax:invisible)) then
            (generate-id($r), check:nextURLLinks($r))
            else generate-id($r)"/>
    </xsl:function>
</xsl:stylesheet>