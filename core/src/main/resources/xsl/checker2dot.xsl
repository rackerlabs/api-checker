<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns="http://www.rackspace.com/repose/wadl/checker"
    xmlns:check="http://www.rackspace.com/repose/wadl/checker"
    xmlns:xsd="http://www.w3.org/2001/XMLSchema"
    version="2.0">
     
   <xsl:output method="text"/>
   <xsl:param name="ignoreSinks" select="true()" as="xsd:boolean"/>
   <xsl:param name="nfaMode" select="true()" as="xsd:boolean"/>
   <xsl:variable name="source_types" select="('START')" as="xsd:string*"/>
   <xsl:variable name="sink_types" as="xsd:string*">
       <xsl:choose>
           <xsl:when test="$nfaMode">
               <xsl:sequence select="('URL_FAIL', 'METHOD_FAIL')"/>
           </xsl:when>
           <xsl:otherwise>
               <xsl:sequence select="('URL_FAIL', 'METHOD_FAIL', 'ACCEPT')"/>
           </xsl:otherwise>
       </xsl:choose>
   </xsl:variable>
   <xsl:variable name="real_start" select="'REAL_START'" as="xsd:string"/>
   <xsl:variable name="indent" select='"           "'/>
   <xsl:variable name="methodFail" select="concat(generate-id(),'M')"/>
   <xsl:variable name="urlFail" select="concat(generate-id(),'U')"/>
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
       <xsl:choose>
           <xsl:when test="$nfaMode">
               <xsl:text>
           {
           rank=source&#x0a;</xsl:text>
           <xsl:value-of select="concat($indent,$real_start,'&#x0a;')"/>
           <xsl:text>}
               {
               rank=same&#x0a;</xsl:text><xsl:apply-templates mode="source"/>
               <xsl:value-of select="$indent"/>
               <xsl:text>}&#x0a;</xsl:text>
           </xsl:when>
           <xsl:otherwise>
               <xsl:text>
           {
           rank=source&#x0a;</xsl:text><xsl:apply-templates mode="source"/>
               <xsl:value-of select="$indent"/>
               <xsl:text>}&#x0a;</xsl:text>
           </xsl:otherwise>
       </xsl:choose>
       <xsl:value-of select="$indent"/>
       <xsl:text>{&#x0a;</xsl:text>
       <xsl:if test="$nfaMode">
           <xsl:apply-templates mode="nfa_connections"/>
       </xsl:if>
       <xsl:apply-templates mode="connections"/>
       <xsl:value-of select="$indent"/>
       <xsl:text>}&#x0a;</xsl:text>
       <xsl:if test="not($ignoreSinks) and not($nfaMode)">
          <xsl:text>
           {
            &#x0a;</xsl:text><xsl:apply-templates mode="sink"/>
          <xsl:value-of select="concat($indent,'}')"/>
       </xsl:if>
       <xsl:text>&#x0a;//Nodes&#x0a;
       </xsl:text>
       <xsl:value-of select="concat($real_start,'[style=invis]&#x0a;')"/>
       <xsl:apply-templates/>
       <xsl:if test="not($ignoreSinks) and $nfaMode">
           <xsl:value-of select="check:nodeLabel($methodFail,'salmon','doublecircle')"/>
           <xsl:value-of select="check:nodeLabel($urlFail,'salmon3', 'doublecircle')"/>
       </xsl:if>
       <xsl:text>}</xsl:text>
   </xsl:template>
    
   <xsl:template match="check:step[count(index-of($source_types,@type)) != 0]" mode="source">
        <xsl:value-of select="concat($indent,@id,'&#x0a;')"/>
   </xsl:template>
   <xsl:template match="check:step[count(index-of($sink_types,@type)) != 0]" mode="sink">
       <xsl:value-of select="concat($indent,@id,'&#x0a;')"/>
   </xsl:template>
   <xsl:template match="check:step[$source_types = @type]" mode="nfa_connections">
       <xsl:value-of select="concat($indent,$real_start,'-&gt;',@id,'&#x0a;')"/>
   </xsl:template>
   <xsl:template match="check:step" mode="connections">
       <xsl:variable name="step" select="."/>
       <xsl:choose>
           <xsl:when test="($ignoreSinks or $nfaMode) and count(index-of($sink_types,@type)) != 0"/>
           <xsl:otherwise>
               <xsl:variable name="label" select="@label"/>
               <xsl:variable name="id" select="@id"/>
               <xsl:variable name="nexts" select="tokenize(normalize-space(@next),' ')" as="xsd:string*"/>
               <xsl:for-each select="$nexts">
                   <xsl:variable name="next" select="." as="xsd:string"/>
                   <xsl:variable name="nextStep" select="$step/../check:step[@id = $next]" as="node()"/>
                   <xsl:variable name="dest" as="xsd:string" select="check:nextDest($nextStep)"/>
                   <xsl:choose>
                       <xsl:when test="$ignoreSinks and count(index-of($sink_types,$nextStep/@type)) != 0"/>
                       <xsl:otherwise>
                           <xsl:value-of select="check:connect($id, $dest)"/>
                           <xsl:choose>
                               <xsl:when test="$nfaMode">
                                   <xsl:text> [label=&quot;</xsl:text>
                                   <xsl:choose>
                                       <xsl:when test="$label = 'ε'">
                                           <xsl:value-of select="$label"/>
                                       </xsl:when>
                                       <xsl:when test="$nextStep/@type = 'ACCEPT'">
                                           <xsl:value-of select="'ε'"/>
                                       </xsl:when>
                                       <xsl:otherwise>
                                           <xsl:value-of select="substring($nextStep/@type,1,1)"/>
                                           <xsl:choose>
                                               <xsl:when test="$nextStep/@match">
                                                 <xsl:text> (</xsl:text>
                                                 <xsl:value-of select="check:escapeRegex($nextStep/@match)"/>
                                                 <xsl:text>)</xsl:text>
                                               </xsl:when>
                                               <xsl:otherwise>
                                                   <xsl:value-of select="check:getErrorRegex($step, $nextStep,$nexts)"/>
                                               </xsl:otherwise>
                                           </xsl:choose>
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
               <!-- Add Bad URL connections --> 
               <xsl:if test="$nfaMode and $step/@type = 'URL_FAIL'">
                   <xsl:value-of select="concat($indent,$id,'-&gt;',$id,' [label=&quot;U(.*)&quot;, tailport=n, headport=ne]&#x0a;')"/>
                   <xsl:value-of select="concat($indent,$id,'-&gt;',$id,' [label=&quot;M(.*)&quot;, tailport=s, headport=sw]&#x0a;')"/>
               </xsl:if>
           </xsl:otherwise>
       </xsl:choose>
   </xsl:template>
     <xsl:template match="check:step">
         <xsl:choose>
             <xsl:when test="($ignoreSinks or $nfaMode) and count(index-of($sink_types,@type)) != 0"/>
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
                     <xsl:when test="@notMatch and @notTypes">
                         <xsl:value-of select="check:notMatchRegex((tokenize(@notMatch,' '), tokenize(@notTypes,' ')))"/>
                     </xsl:when>
                     <xsl:when test="@notMatch">
                         <xsl:value-of select="check:notMatchRegex(tokenize(@notMatch,' '))"/>
                     </xsl:when>
                     <xsl:when test="@notTypes">
                         <xsl:value-of select="check:notMatchRegex(tokenize(@notTypes,' '))"/>
                     </xsl:when>
                     <xsl:otherwise>
                         <xsl:value-of select="@type"/>
                     </xsl:otherwise>
                 </xsl:choose>
                 <xsl:value-of select="'&quot;'"/>
                 <xsl:choose>
                     <xsl:when test="(count(index-of($source_types,@type)) != 0)">
                         <xsl:choose>
                             <xsl:when test="$nfaMode">
                                 <xsl:text>, fillcolor="white"</xsl:text>
                             </xsl:when>
                             <xsl:otherwise>
                                 <xsl:text>, shape=none, fillcolor="white"</xsl:text>
                             </xsl:otherwise>
                         </xsl:choose>
                     </xsl:when>
                     <xsl:when test="@type = 'ACCEPT'">
                         <xsl:text>, shape=doublecircle, fillcolor="white"</xsl:text>
                     </xsl:when>
                     <xsl:when test="@type = 'METHOD_FAIL'">
                         <xsl:text>, shape=ellipse, fillcolor="salmon"</xsl:text>
                     </xsl:when>
                     <xsl:when test="@type= 'URL_FAIL'">
                         <xsl:text>, shape=ellipse, fillcolor="salmon3"</xsl:text>
                     </xsl:when>
                     <xsl:when test="@type = 'URL'">
                         <xsl:text>, fillcolor="yellowgreen"</xsl:text>
                     </xsl:when>
                     <xsl:when test="@type = 'URLXSD'">
                         <xsl:text>, fillcolor="yellowgreen"</xsl:text>
                     </xsl:when>
                 </xsl:choose>
                 <xsl:value-of select="']&#x0a;'"/>
             </xsl:otherwise>
         </xsl:choose>
     </xsl:template>
   <xsl:template match="text()" mode="#all"/>
   <xsl:function name="check:getErrorRegex" as="xsd:string">
       <xsl:param name="step" as="node()"/>
       <xsl:param name="nextStep" as="node()"/>
       <xsl:param name="nexts" as="xsd:string*"/>
          <xsl:variable name="matches" as="xsd:string*" 
                        select="distinct-values(for $n in $nexts 
                                                return $step/../check:step[@id=$n and @type=substring-before($nextStep/@type,'_')]/@match)"/>
          <xsl:choose>
              <xsl:when test="count($matches) != 0">
                  <xsl:value-of>
                   <xsl:text>(!~(</xsl:text>
                   <xsl:value-of select="$matches" separator=" | "/>
                   <xsl:text>))</xsl:text>
                  </xsl:value-of>
              </xsl:when>
              <xsl:otherwise>
                  <xsl:text>(.*)</xsl:text>
              </xsl:otherwise>
          </xsl:choose>
   </xsl:function>
   <xsl:function name="check:escapeRegex" as="xsd:string">
      <xsl:param name="in" as="xsd:string"/>
      <xsl:value-of select="replace($in,'\\','\\\\')"/> 
   </xsl:function>
   <xsl:function name="check:notMatchRegex" as="xsd:string">
       <xsl:param name="in" as="xsd:string*"/>
       <xsl:variable name="inEsc"
                     select="for $i in $in return check:escapeRegex($i)"
                     as="xsd:string*"/>
       <xsl:value-of select="concat('!(',string-join($in,' | '),')')"/>
   </xsl:function>
   <xsl:function name="check:connect" as="xsd:string">
       <xsl:param name="src" as="xsd:string"/>
       <xsl:param name="dst" as="xsd:string"/>
       <xsl:value-of select="concat($indent,$src,'-&gt;',$dst)"/>
   </xsl:function>
   <xsl:function name="check:nextDest" as="xsd:string">
       <xsl:param name="nextStep" as="node()"/>
       <xsl:choose>
           <!--
               In NFA Mode, we have a single METHOD_FAIL and URL_FAIL
               node.
           -->
           <xsl:when test="$nfaMode">
               <xsl:choose>
                   <xsl:when test="$nextStep/@type = 'METHOD_FAIL'">
                       <xsl:value-of select="$methodFail"/>
                   </xsl:when>
                   <xsl:when test="$nextStep/@type = 'URL_FAIL'">
                       <xsl:value-of select="$urlFail"/>
                   </xsl:when>
                   <xsl:otherwise>
                       <xsl:value-of select="$nextStep/@id"/>
                   </xsl:otherwise>
               </xsl:choose>
           </xsl:when>
           <xsl:otherwise>
               <xsl:value-of select="$nextStep/@id"/>
           </xsl:otherwise>
       </xsl:choose>
   </xsl:function>
   <xsl:function name="check:nodeLabel" as="xsd:string">
       <xsl:param name="id" as="xsd:string"/>
       <xsl:param name="color" as="xsd:string"/>
       <xsl:param name="shape" as="xsd:string"/>
       <xsl:value-of>
           <xsl:value-of select="$id"/>
           <xsl:text>[label="</xsl:text>
           <xsl:value-of select="$id"/>
           <xsl:text>", fillcolor="</xsl:text>
           <xsl:value-of select="$color"/>
           <xsl:text>", shape="</xsl:text>
           <xsl:value-of select="$shape"/>
           <xsl:text>"]&#x0a;</xsl:text>
       </xsl:value-of>
   </xsl:function>
</xsl:stylesheet>
