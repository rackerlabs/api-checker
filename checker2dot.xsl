<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns="http://www.rackspace.com/repose/wadl/checker"
    xmlns:check="http://www.rackspace.com/repose/wadl/checker"
    xmlns:xsd="http://www.w3.org/2001/XMLSchema"
    version="2.0">
     
   <xsl:output method="text"/>
   <xsl:variable name="source_types" select="('START')" as="xsd:string*"/>
   <xsl:variable name="sink_types" select="('URL_FAIL', 'METHOD_FAIL', 'ACCEPT')" as="xsd:string*"/>
   <xsl:variable name="indent" select='"           "'/>
   <xsl:template match="check:checker">
       <xsl:text>digraph Checker { rankdir=LR; fontname="Helvetica"; labelloc=b;
           node [fontname="Helvetica", shape=rect, style=filled,fillcolor="#EEEEEE"]
           {
           rank=source&#x0a;</xsl:text><xsl:apply-templates mode="source"/>
       <xsl:value-of select="$indent"/>
       <xsl:text>}&#x0a;</xsl:text>
       <xsl:value-of select="$indent"/>
       <xsl:text>{&#x0a;</xsl:text>
       <xsl:apply-templates mode="connections"/>
       <xsl:value-of select="$indent"/>
       <xsl:text>}&#x0a;</xsl:text>
       <xsl:text>
           {
           rank=sink&#x0a;</xsl:text><xsl:apply-templates mode="sink"/>
       <xsl:value-of select="$indent"/>
       <xsl:text>}&#x0a;//Nodes&#x0a;</xsl:text>
       <xsl:apply-templates/>
       <xsl:text>}</xsl:text>
   </xsl:template>
    
   <xsl:template match="check:step[count(index-of($source_types,@type)) != 0]" mode="source">
        <xsl:value-of select="concat($indent,@id,'&#x0a;')"/>
   </xsl:template>
   <xsl:template match="check:step[count(index-of($sink_types,@type)) != 0]" mode="sink">
       <xsl:value-of select="concat($indent,@id,'&#x0a;')"/>
   </xsl:template>
   <xsl:template match="check:step" mode="connections">
       <xsl:variable name="id" select="@id"/>
       <xsl:variable name="nexts" select="tokenize(normalize-space(@next),' ')" as="xsd:string*"/>
       <xsl:for-each select="$nexts">
           <xsl:value-of select="concat($indent,$id,'-&gt;',.,'&#x0a;')"></xsl:value-of>
       </xsl:for-each>
   </xsl:template>
   <xsl:template match="check:step">
      <xsl:value-of select="concat(@id,'[')"/>
      <xsl:value-of select="'label=&quot;'"/>
      <xsl:choose>
          <xsl:when test="@label">
              <xsl:value-of select="concat(check:escapeRegex(@match),' \n(',@label,')')"/>
          </xsl:when>
          <xsl:when test="@match">
              <xsl:value-of select="check:escapeRegex(@match)"/>
          </xsl:when>
          <xsl:otherwise>
              <xsl:value-of select="@type"/>
          </xsl:otherwise>
      </xsl:choose>
      <xsl:value-of select="'&quot;'"/>
      <xsl:choose>
          <xsl:when test="(count(index-of($source_types,@type)) != 0) or @type = 'ACCEPT'">
              <xsl:text>, shape=doublecircle, fillcolor="white"</xsl:text>
          </xsl:when>
          <xsl:when test="count(index-of($sink_types,@type)) != 0">
              <xsl:text>, shape=doublecircle, fillcolor="crimson"</xsl:text>
          </xsl:when>
          <xsl:when test="@type = 'URL'">
              <xsl:text>, fillcolor="yellowgreen"</xsl:text>
          </xsl:when>
      </xsl:choose>
       <xsl:value-of select="']&#x0a;'"/>
   </xsl:template>
   <xsl:template match="text()" mode="#all"/>
   <xsl:function name="check:escapeRegex" as="xsd:string">
      <xsl:param name="in" as="xsd:string"/>
      <xsl:value-of select="replace($in,'\\','\\\\')"/> 
   </xsl:function>
</xsl:stylesheet>