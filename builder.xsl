<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet 
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xsd="http://www.w3.org/2001/XMLSchema"
    xmlns:wadl="http://wadl.dev.java.net/2009/02"
    xmlns:check="http://www.rackspace.com/repose/wadl/checker"
    xmlns:rax="http://docs.rackspace.com/api"
    xmlns="http://www.rackspace.com/repose/wadl/checker"
    exclude-result-prefixes="xsd wadl rax"
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
                    <xsl:value-of select="(check:getNextURLLinks(wadl:resources), check:getNextMethodLinks(wadl:resources))" separator=" "/>
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
            <xsl:sequence select="check:getNextMethodLinks(.)"/>
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
        <xsl:call-template name="check:addMethodSets"/>
    </xsl:template>
    
    <xsl:template name="check:addMethodSets">
        <xsl:variable name="from" select="." as="node()"/>
        <xsl:variable name="baseId" select="generate-id()"/>
        <xsl:for-each-group select="wadl:method" group-by="@name">
            <xsl:if test="count(current-group()) &gt; 1">
                <step type="METHOD" id="{current-grouping-key()}_{$baseId}"
                     match="{current-grouping-key()}" label="ε">
                    <xsl:attribute name="next">
                        <xsl:value-of separator=" ">
                            <xsl:sequence select="for $m in current-group() return
                                                   if ($m/@rax:id) then
                                                     generate-id($from/ancestor::*/wadl:method[@id=$m/@rax:id])
                                                     else generate-id($m)"></xsl:sequence>
                        </xsl:value-of>
                    </xsl:attribute>
                </step>
            </xsl:if>
        </xsl:for-each-group>
    </xsl:template>
    
    
    <xsl:template match="wadl:method">
        <!-- Only work with source methods, not copies -->
        <xsl:if test="(not(@rax:id))">
            <step type="METHOD">
                <xsl:attribute name="id" select="generate-id()"/>
                <xsl:attribute name="match" select="check:toRegExEscaped(@name)"/>
                <xsl:if test="@id or wadl:doc/@title">
                    <xsl:attribute name="label">
                        <xsl:choose>
                            <xsl:when test="wadl:doc/@title">
                                <xsl:value-of select="normalize-space(wadl:doc/@title)"/>
                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:value-of select="normalize-space(@id)"/>
                            </xsl:otherwise>
                        </xsl:choose>
                    </xsl:attribute>
                </xsl:if>
            </step>
        </xsl:if>
    </xsl:template>
    
    <xsl:template match="text()" mode="#all"/>
    
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
    
    <xsl:function name="check:nextMethodLinks" as="xsd:string*">
        <xsl:param name="from" as="node()"/>
        <xsl:for-each-group select="$from/wadl:method" group-by="@name">
            <xsl:choose>
                <xsl:when test="count(current-group()) &gt; 1">
                    <xsl:sequence select="concat(current-grouping-key(),'_',generate-id($from))"/>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:sequence select="for $m in current-group() return
                                          if ($m/@rax:id) then
                                           generate-id($from/ancestor::*/wadl:method[@id=$m/@rax:id])
                                          else generate-id($m)"/>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:for-each-group>
        <xsl:if test="$from/wadl:resource[@rax:invisible]">
            <xsl:for-each select="$from/wadl:resource[@rax:invisible]">
                <xsl:sequence select="check:nextMethodLinks(.)"/>
            </xsl:for-each>
        </xsl:if>
    </xsl:function>
    
    <xsl:function name="check:getNextMethodLinks">
        <xsl:param name="from" as="node()"/>
        <xsl:sequence select="check:nextMethodLinks($from)"/>
        <xsl:sequence select="$METHOD_FAIL"/>
    </xsl:function>
</xsl:stylesheet>