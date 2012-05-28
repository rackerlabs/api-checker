<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet 
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xsd="http://www.w3.org/2001/XMLSchema"
    xmlns:wadl="http://wadl.dev.java.net/2009/02"
    xmlns:check="http://www.rackspace.com/repose/wadl/checker"
    xmlns:rax="http://docs.rackspace.com/api"
    xmlns="http://www.rackspace.com/repose/wadl/checker"
    exclude-result-prefixes="xsd wadl rax check"
    version="2.0">
    
    <xsl:output indent="yes" method="xml"/>
    
    <!-- Defaults Steps -->
    <xsl:variable name="START"       select="'S0'"/>
    <xsl:variable name="URL_FAIL"    select="'SE0'"/>
    <xsl:variable name="METHOD_FAIL" select="'SE1'"/>
    <xsl:variable name="ACCEPT"      select="'SA'"/>
    
    <!-- Useful namespaces -->
    <xsl:variable name="schemaNS" select="'http://www.w3.org/2001/XMLSchema'"/>
    
    <!-- Useful matches -->
    <xsl:variable name="matchAll" select="'.*'"/>

    <!-- A list of namespaces -->
    <xsl:variable name="namespaces">
        <xsl:call-template name="check:getNamespaces"/>
    </xsl:variable>
    
    <xsl:template match="wadl:application">
        <!--
            The first pass processes the WADL
            and connects all of the states.
         -->
        <xsl:variable name="pass1">
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
        </xsl:variable>
        <!--
            In the second pass, we connect the error
            states in the machine.
        -->
        <xsl:variable name="pass2">
            <checker>
                <xsl:apply-templates select="$pass1" mode="addErrorStates"/>
            </checker>
        </xsl:variable>
        <!--
            Finally add namespaces and remove unconnected nodes.
        -->
        <checker>
            <xsl:call-template name="check:addNamespaceNodes"/>
            <xsl:apply-templates mode="grammar"/>
            <xsl:call-template name="check:pruneStates">
                <xsl:with-param name="checker" select="$pass2"/>
            </xsl:call-template>
        </checker>
    </xsl:template>

    <xsl:template match="wadl:grammars/wadl:include" mode="grammar">
        <xsl:choose>
            <xsl:when test="doc-available(@href)">
                <xsl:variable name="ns" select="doc(@href)/xsd:schema/@targetNamespace"/>
                <xsl:choose>
                    <xsl:when test="$ns">
                        <grammar ns="{$ns}" href="{@href}"/>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:message>[WARNING] Don't understand XML grammar of <xsl:value-of select="@href"/> ignoring...</xsl:message>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:when>
            <xsl:when test="unparsed-text-available(@href)">
                <xsl:message>[WARNING] Don't understand unparsed grammar of <xsl:value-of select="@href"/> ignoring...</xsl:message>
            </xsl:when>
            <xsl:otherwise>
                <xsl:message terminate="yes">[ERROR] Couldn't access grammar <xsl:value-of select="@href"/></xsl:message>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template match="wadl:grammars/xsd:schema" mode="grammar">
        <grammar>
            <xsl:if test="@targetNamespace">
                <xsl:attribute name="ns" select="@targetNamespace"/>
            </xsl:if>
            <xsl:copy-of select="."/>
        </grammar>
    </xsl:template>

    <xsl:template name="check:pruneStates">
        <xsl:param name="checker" as="node()"/>
        <xsl:variable name="nexts" as="xsd:string*" select="tokenize(string-join($checker//check:step/@next,' '),' ')"/>
        <xsl:variable name="connected" as="xsd:integer" select="count($checker//check:step[$nexts = @id])"/>
        <xsl:variable name="all" as="xsd:integer" select="count($checker//check:step[@type != 'START'])"/>
        <xsl:choose>
            <xsl:when test="$connected = $all">
                <xsl:for-each select="$checker//check:step">
                    <xsl:copy-of select="."/>
                </xsl:for-each>
            </xsl:when>
            <xsl:otherwise>
                <xsl:call-template name="check:pruneStates">
                    <xsl:with-param name="checker">
                        <checker>
                            <xsl:apply-templates select="$checker" mode="pruneStates">
                                <xsl:with-param name="nexts" select="$nexts"/>
                            </xsl:apply-templates>
                        </checker>
                    </xsl:with-param>
                </xsl:call-template>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template name="check:getNamespaces">
        <!--
            Retrieve all required namespaces
        -->
        <xsl:variable name="ns">
            <namespaces>
                <xsl:apply-templates mode="ns"/>
            </namespaces>
        </xsl:variable>

        <!--
            Return only unique ones.
        -->
        <namespaces>
            <xsl:for-each-group select="$ns//check:ns" group-by="@uri">
                <xsl:copy-of select="current-group()[1]"/>
            </xsl:for-each-group>
        </namespaces>
    </xsl:template>

    <xsl:template name="check:addNamespaceNodes">
        <xsl:for-each select="$namespaces//check:ns">
            <xsl:namespace name="{@prefix}" select="@uri"/>
        </xsl:for-each>
    </xsl:template>

    <xsl:template match="wadl:param[@type]" mode="ns">
        <xsl:variable name="qname" select="resolve-QName(@type,.)" as="xsd:QName"/>
        <xsl:call-template name="check:printns">
            <xsl:with-param name="qname" select="$qname"/>
        </xsl:call-template>
    </xsl:template>

    <xsl:template match="wadl:representation[@element]" mode="ns">
        <xsl:variable name="qname" select="resolve-QName(@element,.)" as="xsd:QName"/>
        <xsl:call-template name="check:printns">
            <xsl:with-param name="qname" select="$qname"/>
        </xsl:call-template>
    </xsl:template>

    <xsl:template name="check:printns">
        <xsl:param name="qname" as="xsd:QName"/>
        <ns>
            <xsl:attribute name="prefix">
                <xsl:variable name="prefix" select="prefix-from-QName($qname)"/>
                <xsl:choose>
                    <xsl:when test="not($prefix)">
                        <xsl:value-of select="generate-id()"/>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:value-of select="$prefix"/>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:attribute>
            <xsl:attribute name="uri">
                <xsl:value-of select="namespace-uri-from-QName($qname)"/>
            </xsl:attribute>
        </ns>
    </xsl:template>

    <xsl:template match="check:step" mode="pruneStates">
        <xsl:param name="nexts" as="xsd:string*"/>
        <xsl:choose>
            <xsl:when test="@type='START'">
                <xsl:copy-of select="."/>
            </xsl:when>
            <xsl:when test="@id = $nexts">
                <xsl:copy-of select="."/>
            </xsl:when>
            <xsl:otherwise>
                <!-- pruned -->
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template match="check:step" mode="addErrorStates">
        <xsl:choose>
            <xsl:when test="@type=('URL','URLXSD','START')">
                <xsl:variable name="nexts" as="xsd:string*" select="tokenize(@next,' ')"/>
                <xsl:variable name="doConnect" as="xsd:boolean" 
                    select="not((for $n in $nexts return 
                              if (..//check:step[@id = $n and @match=$matchAll])
                              then false() else true()) = false())"/>
                <!-- Vars for processing method fail -->
                <xsl:variable name="nextMethodMatch" as="xsd:string*"
                              select="check:sort(for $n in $nexts return ..//check:step[@id=$n and @type='METHOD']/@match)"/>
                <xsl:variable name="haveMethodMatch" as="xsd:boolean" select="count($nextMethodMatch) &gt; 0"/>
                <xsl:variable name="MethodMatchID" as="xsd:string" select="concat(generate-id(),'m')"/>
                <!-- Vars for processing url fail -->
                <xsl:variable name="nextURLMatch" as="xsd:string*"
                              select="check:sort(for $n in $nexts return ..//check:step[@id=$n and @type='URL']/@match)"/>
                <xsl:variable name="nextURLXSDMatch" as="xsd:string*"
                              select="check:sort(for $n in $nexts return ..//check:step[@id=$n and @type='URLXSD']/@match)"/>
                <xsl:variable name="haveURLMatch" as="xsd:boolean" select="(count($nextURLMatch) &gt; 0) or (count($nextURLXSDMatch) &gt; 0)"/>
                <xsl:variable name="URLMatchID" as="xsd:string" select="concat(generate-id(),'u')"/>
                <!-- Next variables -->
                <xsl:variable name="newNexts" as="xsd:string*">
                    <xsl:sequence select="$nexts"/>
                    <xsl:choose>
                        <xsl:when test="$haveMethodMatch">
                            <xsl:sequence select="$MethodMatchID"/>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:sequence select="$METHOD_FAIL"/>
                        </xsl:otherwise>
                    </xsl:choose>
                    <xsl:if test="$doConnect">
                        <xsl:choose>
                            <xsl:when test="$haveURLMatch">
                                <xsl:sequence select="$URLMatchID"/>
                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:sequence select="$URL_FAIL"/>
                            </xsl:otherwise>
                        </xsl:choose>
                    </xsl:if>
                </xsl:variable>
                <step>
                    <xsl:copy-of select="@*[name() != 'next']"/>
                    <xsl:attribute name="next">
                        <xsl:value-of select="$newNexts" separator=" "/>
                    </xsl:attribute>
                </step>
                <xsl:if test="$haveMethodMatch">
                    <step type="METHOD_FAIL">
                        <xsl:attribute name="id" select="$MethodMatchID"/>
                        <xsl:attribute name="notMatch">
                            <xsl:value-of select="$nextMethodMatch" separator="|"/>
                        </xsl:attribute>
                    </step>
                </xsl:if>
                <xsl:if test="$doConnect and $haveURLMatch">
                    <step type="URL_FAIL">
                        <xsl:attribute name="id" select="$URLMatchID"/>
                        <xsl:if test="count($nextURLMatch) &gt; 0">
                            <xsl:attribute name="notMatch">
                                <xsl:value-of select="$nextURLMatch" separator="|"/>
                            </xsl:attribute>
                        </xsl:if>
                        <xsl:if test="count($nextURLXSDMatch) &gt; 0">
                            <xsl:attribute name="notTypes">
                                <xsl:value-of select="$nextURLXSDMatch" separator=" "/>
                            </xsl:attribute>
                        </xsl:if>
                    </step>
                </xsl:if>
            </xsl:when>
            <xsl:otherwise>
                <xsl:copy-of select="."/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    
    <xsl:template match="wadl:resource">
        <xsl:variable name="links" as="xsd:string*">
            <xsl:sequence select="check:getNextURLLinks(.)"/>
            <xsl:sequence select="check:getNextMethodLinks(.)"/>
        </xsl:variable>
        <xsl:variable name="templatePath" select="starts-with(@path,'{')" as="xsd:boolean"/>
        <step>
            <xsl:attribute name="type">
                <xsl:choose>
                    <xsl:when test="$templatePath and check:isXSDURL(.)">
                        <xsl:text>URLXSD</xsl:text>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:text>URL</xsl:text>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:attribute>
            <xsl:attribute name="id" select="generate-id()"/>
            <xsl:attribute name="match">
                <xsl:choose>
                    <xsl:when test="$templatePath">
                        <xsl:call-template name="check:getTemplateMatch"/>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:value-of select="check:toRegExEscaped(@path)"/>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:attribute>
            <xsl:attribute name="next" select="$links" separator=" "/>
            <xsl:if test="$templatePath">
                <xsl:attribute name="label">
                    <xsl:value-of select="check:paramForTemplatePath(.)/@name"/>
                </xsl:attribute>
            </xsl:if>
        </step>
        <xsl:apply-templates/>
        <xsl:call-template name="check:addMethodSets"/>
    </xsl:template>
    <xsl:function name="check:isXSDURL" as="xsd:boolean">
        <xsl:param name="path" as="node()"/>
        <xsl:variable name="param" select="check:paramForTemplatePath($path)"/>
        <xsl:variable name="type" select="resolve-QName($param/@type,$param)" as="xsd:QName"/>
        <xsl:choose>
            <xsl:when test="(namespace-uri-from-QName($type) = $schemaNS) and (local-name-from-QName($type) = 'string')">
                <xsl:value-of select="false()"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="true()"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:function>
    <xsl:function name="check:paramForTemplatePath" as="node()">
        <xsl:param name="path" as="node()"/>
        <xsl:variable name="paramName" select="replace($path/@path,'(\{|\})','')"
            as="xsd:string"/>
        <xsl:if test="not($path/wadl:param[@style='template' and @name=$paramName])">
            <xsl:message terminate="yes">
                <xsl:text>[ERROR] The WADL contains a resource with {</xsl:text>
                <xsl:value-of select="$paramName"/>
                <xsl:text>} but a template param of name '</xsl:text>
                <xsl:value-of select="$paramName"/>
                <xsl:text>' is not found.</xsl:text>
            </xsl:message>
        </xsl:if>
        <xsl:copy-of select="$path/wadl:param[@style='template' and @name=$paramName]"></xsl:copy-of>
    </xsl:function>
    <xsl:template name="check:getTemplateMatch">
        <xsl:variable name="param" select="check:paramForTemplatePath(.)"/>
        <xsl:value-of select="check:getMatch(resolve-QName($param/@type,$param))"/>
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
        <xsl:variable name="links" as="xsd:string*">
            <xsl:sequence select="check:getNextReqTypeLinks(.)"/>
        </xsl:variable>
        <!--
            Only work with source methods, not copies if possible.  
            If the source isn't available then use the first copy
            as the source.
        -->
        <xsl:variable name="raxid" select="@rax:id"/>
        <xsl:if test="(not($raxid) or 
                      (not(//wadl:method[@id=$raxid]) and (generate-id((//wadl:method[@rax:id = $raxid])[1]) = generate-id())))">
            <step type="METHOD">
                <xsl:attribute name="id" select="generate-id()"/>
                <xsl:attribute name="match" select="check:toRegExEscaped(@name)"/>
                <xsl:choose>
                    <xsl:when test="count($links) &gt; 0">
                        <xsl:attribute name="next" select="$links" separator=" "/>
                    </xsl:when>
                    <xsl:otherwise>
                        <!-- for now, once we get to the method we accept -->
                        <xsl:attribute name="next" select="$ACCEPT"/>
                    </xsl:otherwise>
                </xsl:choose>
                <xsl:call-template name="check:addLabel"/>
            </step>
        </xsl:if>
        <xsl:apply-templates/>
    </xsl:template>

    <xsl:template match="wadl:request/wadl:representation[@mediaType]">
        <step type="REQ_TYPE">
            <xsl:attribute name="id" select="generate-id()"/>
            <xsl:attribute name="match" select="check:toRegExEscaped(@mediaType)"/>
            <!-- for now, once we get here we accept -->
            <xsl:attribute name="next" select="$ACCEPT"/>
            <xsl:call-template name="check:addLabel"/>
        </step>
    </xsl:template>

    <xsl:template match="text()" mode="#all"/>
    
    <xsl:template name="check:addLabel">
        <!--
            If an id or doc title exists, use it as the label.
        -->
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
    </xsl:template>

    <xsl:function name="check:toRegExEscaped" as="xsd:string">
        <xsl:param name="in" as="xsd:string"/>
        <xsl:value-of select="replace($in,'\.|\\|\(|\)|\{|\}|\[|\]|\?|\+|\-|\^|\$|#|\*|\|','\\$0')"/>
    </xsl:function>

    <xsl:function name="check:getNextReqTypeLinks" as="xsd:string*">
        <xsl:param name="from" as="node()"/>
        <xsl:sequence select="for $r in $from/wadl:request/wadl:representation[@mediaType]
                              return generate-id($r)"/>
    </xsl:function>
        
    <xsl:function name="check:getNextURLLinks" as="xsd:string*">
        <xsl:param name="from" as="node()"/>
        <xsl:sequence select="for $r in $from/wadl:resource return 
            if (xsd:boolean($r/@rax:invisible)) then
            (generate-id($r), check:getNextURLLinks($r))
            else generate-id($r)"/>
    </xsl:function>
    
    <xsl:function name="check:getNextMethodLinks" as="xsd:string*">
        <xsl:param name="from" as="node()"/>
        <xsl:for-each-group select="$from/wadl:method" group-by="@name">
            <xsl:choose>
                <xsl:when test="count(current-group()) &gt; 1">
                    <xsl:sequence select="concat(current-grouping-key(),'_',generate-id($from))"/>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:sequence select="for $m in current-group() return
                                          if ($m/@rax:id) then
                                            if ($from/ancestor::*/wadl:method[@id=$m/@rax:id]) then
                                                generate-id($from/ancestor::*/wadl:method[@id=$m/@rax:id])
                                            else generate-id(($from/ancestor::*//wadl:method[@rax:id = $m/@rax:id])[1])
                                          else generate-id($m)"/>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:for-each-group>
        <xsl:if test="$from/wadl:resource[@rax:invisible]">
            <xsl:for-each select="$from/wadl:resource[@rax:invisible]">
                <xsl:sequence select="check:getNextMethodLinks(.)"/>
            </xsl:for-each>
        </xsl:if>
    </xsl:function>
    
    <xsl:function name="check:getMatch" as="xsd:string">
        <xsl:param name="type" as="xsd:QName"/>
        <xsl:choose>
            <xsl:when test="namespace-uri-from-QName($type) = $schemaNS">
                <xsl:value-of select="check:getMatchForPlainXSDType($type)"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="check:normType($type)"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:function>

    <xsl:function name="check:getMatchForPlainXSDType" as="xsd:string">
        <xsl:param name="type" as="xsd:QName"/>
        <xsl:variable name="name" as="xsd:string"
            select="local-name-from-QName($type)"/>
        <xsl:choose>
            <xsl:when test="$name = 'string'">
                <xsl:value-of select="'.*'"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="check:normType($type)"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:function>

    <xsl:function name="check:normType" as="xsd:string">
        <xsl:param name="type" as="xsd:QName"/>
        <xsl:value-of select="concat($namespaces//check:ns[@uri = namespace-uri-from-QName($type)]/@prefix,':',local-name-from-QName($type))"/>
    </xsl:function>

    <xsl:function name="check:sort" as="xsd:string*">
        <xsl:param name="in" as="xsd:string*"/>
        <xsl:for-each select="$in">
            <xsl:sort select="."/>
            <xsl:value-of select="."/>
        </xsl:for-each>
    </xsl:function>
</xsl:stylesheet>
