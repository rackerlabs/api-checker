<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns="http://www.rackspace.com/repose/wadl/checker"
    xmlns:check="http://www.rackspace.com/repose/wadl/checker"
    xmlns:xsd="http://www.w3.org/2001/XMLSchema"
    version="2.0">
     
   <xsl:output method="text"/>
   <xsl:param name="ignoreSinks" select="false()" as="xsd:boolean"/>
   <xsl:param name="nfaMode" select="true()" as="xsd:boolean"/>
   <xsl:variable name="source_types" select="('START')" as="xsd:string*"/>
   <xsl:variable name="sink_types" select="('URL_FAIL', 'METHOD_FAIL', 'ACCEPT')" as="xsd:string*"/>
   <xsl:variable name="indent" select='"           "'/>
   <xsl:template match="check:checker">
       <xsl:text>digraph Checker { rankdir=LR; fontname="Helvetica"; labelloc=b;
       </xsl:text>
       <xsl:choose>
           <xsl:when test="$nfaMode">
               <xsl:text>
                    node [fontname="Helvetica", shape=ellipse, style=filled,fillcolor="#EEEEEE"]
               </xsl:text>
           </xsl:when>
           <xsl:otherwise>
               <xsl:text>
                    node [fontname="Helvetica", shape=rect, style=filled,fillcolor="#EEEEEE"]
               </xsl:text>
           </xsl:otherwise>
       </xsl:choose>
       <xsl:text>
           {
           rank=source&#x0a;</xsl:text><xsl:apply-templates mode="source"/>
       <xsl:value-of select="$indent"/>
       <xsl:text>}&#x0a;</xsl:text>
       <xsl:value-of select="$indent"/>
       <xsl:text>{&#x0a;</xsl:text>
       <xsl:apply-templates mode="connections"/>
       <xsl:value-of select="$indent"/>
       <xsl:text>}&#x0a;</xsl:text>
       <xsl:if test="not($ignoreSinks)">
          <xsl:text>
           {
           rank=sink&#x0a;</xsl:text><xsl:apply-templates mode="sink"/>
          <xsl:value-of select="concat($indent,'}')"/>
       </xsl:if>
       <xsl:text>&#x0a;//Nodes&#x0a;</xsl:text>
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
       <xsl:variable name="step" select="."/>
       <xsl:choose>
           <xsl:when test="$ignoreSinks and count(index-of($sink_types,@type)) != 0"/>
           <xsl:otherwise>
               <xsl:variable name="label" select="@label"/>
               <xsl:variable name="id" select="@id"/>
               <xsl:variable name="nexts" select="tokenize(normalize-space(@next),' ')" as="xsd:string*"/>
               <xsl:for-each select="$nexts">
                   <xsl:variable name="next" select="." as="xsd:string"/>
                   <xsl:variable name="nextStep" select="$step/../check:step[@id = $next]" as="node()"/>
                   <xsl:choose>
                       <xsl:when test="$ignoreSinks and count(index-of($sink_types,$nextStep/@type)) != 0"/>
                       <xsl:otherwise>
                           <xsl:value-of select="concat($indent,$id,'-&gt;',.)"/>
                           <xsl:choose>
                               <xsl:when test="$nfaMode">
                                   <xsl:text> [label=&quot;</xsl:text>
                                   <xsl:choose>
                                       <xsl:when test="$label = 'ε'">
                                           <xsl:value-of select="$label"/>
                                       </xsl:when>
                                       <xsl:otherwise>
                                           <xsl:value-of select="if (contains($nextStep/@type,'FAIL')) then $nextStep/@type 
                                                                 else substring($nextStep/@type,1,1)"/>
                                           <xsl:if test="$nextStep/@match">
                                             <xsl:text> (</xsl:text>
                                             <xsl:value-of select="check:escapeRegex($nextStep/@match)"/>
                                             <xsl:text>)</xsl:text>
                                           </xsl:if>
                                       </xsl:otherwise>
                                   </xsl:choose>
                                   <xsl:text>&quot;];&#x0a;</xsl:text>
                               </xsl:when>
                               <xsl:otherwise>
                                   <xsl:text>&#x0a;</xsl:text>
                               </xsl:otherwise>
                           </xsl:choose>
                       </xsl:otherwise>
                   </xsl:choose>
               </xsl:for-each>
           </xsl:otherwise>
       </xsl:choose>
   </xsl:template>
     <xsl:template match="check:step">
         <xsl:choose>
             <xsl:when test="$ignoreSinks and count(index-of($sink_types,@type)) != 0"/>
             <xsl:otherwise>
              <xsl:value-of select="concat(@id,'[')"/>
              <xsl:value-of select="'label=&quot;'"/>
              <xsl:choose>
                  <xsl:when test="$nfaMode">
                      <xsl:value-of select="@id"/>
                  </xsl:when>
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
            <xsl:when test="(count(index-of($source_types,@type)) != 0)">
                <xsl:text>, shape=none, fillcolor="white"</xsl:text>
            </xsl:when>
            <xsl:when test="@type = 'ACCEPT'">
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
             </xsl:otherwise>
         </xsl:choose>
     </xsl:template>
   <xsl:template match="text()" mode="#all"/>
   <xsl:function name="check:escapeRegex" as="xsd:string">
      <xsl:param name="in" as="xsd:string"/>
      <xsl:value-of select="replace($in,'\\','\\\\')"/> 
   </xsl:function>
</xsl:stylesheet>