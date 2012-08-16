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

    <!-- Paramenters -->
    <xsl:param name="enableXSDContentCheck" as="xsd:boolean" select="false()"/>
    <xsl:param name="enableXSDTransform" as="xsd:boolean" select="false()"/>
    <xsl:param name="enableWellFormCheck" as="xsd:boolean" select="false()"/>
    <xsl:param name="enableElementCheck" as="xsd:boolean" select="false()"/>
    <xsl:param name="enablePlainParamCheck" as="xsd:boolean" select="false()"/>
    <xsl:param name="enablePreProcessExtension" as="xsd:boolean" select="false()"/>
    <xsl:param name="enableHeaderCheck" as="xsd:boolean" select="false()"/>

    <!-- Do we have an XSD? -->
    <xsl:variable name="WADLhasXSD" as="xsd:boolean"
                  select="
                          if (//wadl:grammars/xsd:schema) then true() else
                            if (//wadl:grammars/wadl:include[doc-available(@href) and doc(@href)/xsd:schema]) then true() else
                              false()
                          " />

    <!-- Actual Config Flags -->
    <xsl:variable name="useXSDContentCheck" as="xsd:boolean"
                  select="($enableXSDContentCheck or $enableXSDTransform) and $WADLhasXSD"/>
    <xsl:variable name="useXSDTransform" as="xsd:boolean"
                  select="$enableXSDTransform and $useXSDContentCheck"/>
    <xsl:variable name="usePreProcessExtension" as="xsd:boolean"
                  select="$enablePreProcessExtension"/>
    <xsl:variable name="useElementCheck" as="xsd:boolean"
                  select="$enableElementCheck"/>
    <xsl:variable name="usePlainParamCheck" as="xsd:boolean"
                  select="$enablePlainParamCheck"/>
    <xsl:variable name="useWellFormCheck" as="xsd:boolean"
                  select="$enableWellFormCheck or $useXSDContentCheck or $enableElementCheck or
                          $enablePlainParamCheck or $enablePreProcessExtension"/>
    <xsl:variable name="useHeaderCheck" as="xsd:boolean"
                  select="$enableHeaderCheck"/>

    <!-- Defaults Steps -->
    <xsl:variable name="START"       select="'S0'"/>
    <xsl:variable name="URL_FAIL"    select="'SE0'"/>
    <xsl:variable name="METHOD_FAIL" select="'SE1'"/>
    <xsl:variable name="ACCEPT"      select="'SA'"/>
    
    <!-- Useful namespaces -->
    <xsl:variable name="schemaNS" select="'http://www.w3.org/2001/XMLSchema'"/>
    
    <!-- Useful matches -->
    <xsl:variable name="matchAll" select="'.*'"/>

    <!-- Default prefix -->
    <xsl:variable name="defaultPrefix" select="generate-id(/element()[1])"/>

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
            Return only unique ones, if don't prune is false and usePlainParamCheck is false, 
            otherwise return all.
        -->
        <namespaces>
            <xsl:choose>
                <xsl:when test="$ns//check:dont_prune">
                    <xsl:copy-of select="$ns//check:ns"/>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:for-each-group select="$ns//check:ns" group-by="@uri">
                        <xsl:copy-of select="current-group()[1]"/>
                    </xsl:for-each-group>
                </xsl:otherwise>
            </xsl:choose>
        </namespaces>
    </xsl:template>

    <xsl:template name="check:addNamespaceNodes">
        <xsl:for-each select="$namespaces//check:ns">
            <xsl:namespace name="{@prefix}" select="@uri"/>
        </xsl:for-each>
    </xsl:template>

    <xsl:template match="wadl:param[@type]" priority="1" mode="ns">
        <xsl:variable name="qname" select="resolve-QName(@type,.)" as="xsd:QName"/>
        <xsl:variable name="pfix" select="prefix-from-QName($qname)"/>
        <xsl:call-template name="check:printns">
            <xsl:with-param name="pfix" select="if (empty($pfix)) then '' else $pfix"/>
            <xsl:with-param name="uri" select="namespace-uri-from-QName($qname)"/>
        </xsl:call-template>
    </xsl:template>

    <xsl:template match="wadl:representation[@element]" mode="ns">
        <xsl:variable name="qname" select="resolve-QName(@element,.)" as="xsd:QName"/>
        <xsl:variable name="pfix" select="prefix-from-QName($qname)"/>
        <xsl:call-template name="check:printns">
            <xsl:with-param name="pfix" select="if (empty($pfix)) then '' else $pfix"/>
            <xsl:with-param name="uri" select="namespace-uri-from-QName($qname)"/>
        </xsl:call-template>
        <xsl:apply-templates mode="ns"/>
    </xsl:template>

    <xsl:template match="wadl:representation[check:isXML(@mediaType)]/wadl:param[(@style = 'plain') and @path]" priority="2" mode="ns">
        <!--
            If we have an XPath param in an XML representation, then
            copy all namespace nodes in that param. And enure that we
            don't prune the namespaces.

            A nicer thing to do would be to parse out the XPath,
            include only those namespaces referenced and allow for
            pruning...
        -->
        <xsl:if test="$usePlainParamCheck">
            <xsl:variable name="this" as="node()" select="."/>
            <xsl:for-each select="in-scope-prefixes($this)">
                <xsl:call-template name="check:printns">
                    <xsl:with-param name="pfix" select="."/>
                    <xsl:with-param name="uri" select="namespace-uri-for-prefix(.,$this)"/>
                    <xsl:with-param name="node" select="$this"/>
                </xsl:call-template>
            </xsl:for-each>
            <dont_prune/>
        </xsl:if>
    </xsl:template>

    <xsl:template name="check:printns">
        <xsl:param name="pfix" as="xsd:string" />
        <xsl:param name="uri" as="xsd:string"/>
        <xsl:param name="node" as="node()" select="."/>
        <ns>
            <xsl:attribute name="prefix">
                <xsl:variable name="prefix" select="$pfix"/>
                <xsl:choose>
                    <xsl:when test="not($prefix)">
                        <xsl:value-of select="$defaultPrefix"/>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:value-of select="$prefix"/>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:attribute>
            <xsl:attribute name="uri">
                <xsl:value-of select="$uri"/>
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
        <step>
            <!-- Remove inRequest attributes -->
            <xsl:copy-of select="@*[name() != 'inRequest'] | node()"/>
        </step>
    </xsl:template>

    <!-- Only the following methods need error states added -->
    <xsl:template match="check:step[@type=('URL','URLXSD','START','HEADER','HEADERXSD') and not(@inRequest)]" mode="addErrorStates">
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
    </xsl:template>
    
    <xsl:template match="wadl:resource">
        <xsl:variable name="haveHeaders" as="xsd:boolean"
                      select="check:haveHeaders(.)"/>
        <xsl:variable name="nextSteps" as="xsd:string*">
            <xsl:sequence select="check:getNextURLLinks(.)"/>
            <xsl:sequence select="check:getNextMethodLinks(.)"/>
        </xsl:variable>
        <xsl:variable name="links" as="xsd:string*">
            <xsl:choose>
                <xsl:when test="$haveHeaders">
                    <xsl:sequence select="check:getNextHeaderLinks(.)"/>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:sequence select="$nextSteps"/>
                </xsl:otherwise>
            </xsl:choose>
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
        <xsl:if test="$haveHeaders">
            <xsl:call-template name="check:addHeaderSteps">
                <xsl:with-param name="next" select="$nextSteps"/>
            </xsl:call-template>
        </xsl:if>
        <xsl:apply-templates/>
        <xsl:call-template name="check:addMethodSets"/>
    </xsl:template>
    <xsl:function name="check:isXSDURL" as="xsd:boolean">
        <xsl:param name="path" as="node()"/>
        <xsl:variable name="param" select="check:paramForTemplatePath($path)"/>
        <xsl:value-of select="check:isXSDParam($param)"/>
    </xsl:function>
    <xsl:function name="check:isXSDParam" as="xsd:boolean">
        <xsl:param name="param" as="node()"/>
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

    <xsl:template name="check:addHeaderSteps">
        <xsl:param name="next" as="xsd:string*"/>
        <xsl:param name="from" as="node()" select="."/>
        <xsl:param name="inRequest" as="xsd:boolean" select="false()"/>
        <xsl:variable name="headers" select="check:getHeaders($from)" as="node()*"/>
        <xsl:for-each select="$headers">
            <xsl:variable name="isXSD" select="check:isXSDParam(.)"/>
            <xsl:variable name="pos" select="position()"/>
            <step id="{check:HeaderID(.)}" name="{@name}">
                <xsl:attribute name="type">
                    <xsl:choose>
                        <xsl:when test="$isXSD">HEADERXSD</xsl:when>
                        <xsl:otherwise>HEADER</xsl:otherwise>
                    </xsl:choose>
                </xsl:attribute>
                <xsl:attribute name="match">
                    <xsl:value-of select="check:getMatchForPlainXSDType(resolve-QName(@type,.))"/>
                </xsl:attribute>
                <xsl:attribute name="next">
                    <xsl:choose>
                        <xsl:when test="$pos = last()">
                            <xsl:value-of separator=" " select="$next"/>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:value-of separator=" "
                                          select="(check:HeaderID($headers[position() = ($pos+1)]), check:HeaderFailID($headers[position() = ($pos+1)]))"/>
                        </xsl:otherwise>
                    </xsl:choose>
                </xsl:attribute>
                <xsl:if test="$inRequest">
                    <xsl:attribute name="inRequest">true</xsl:attribute>
                </xsl:if>
            </step>
            <step type="CONTENT_FAIL" id="{check:HeaderFailID(.)}"/>
        </xsl:for-each>
    </xsl:template>
    
    <xsl:template match="wadl:method">
        <xsl:variable name="haveHeaders" as="xsd:boolean"
                      select="wadl:request and check:haveHeaders(wadl:request)"/>
        <xsl:variable name="nextSteps" as="xsd:string*">
            <xsl:sequence select="check:getNextReqTypeLinks(.)"/>
        </xsl:variable>
        <xsl:variable name="links" as="xsd:string*">
            <xsl:choose>
                <xsl:when test="$haveHeaders">
                    <xsl:sequence select="check:getNextHeaderLinks(wadl:request)"/>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:sequence select="$nextSteps"/>
                </xsl:otherwise>
            </xsl:choose>
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
            <xsl:if test="count($nextSteps) &gt; 0">
                <xsl:call-template name="check:addReqTypeFail"/>
            </xsl:if>
            <xsl:if test="$haveHeaders">
                <xsl:call-template name="check:addHeaderSteps">
                    <xsl:with-param name="next" select="$nextSteps"/>
                    <xsl:with-param name="from" select="wadl:request"/>
                    <xsl:with-param name="inRequest" select="true()"/>
                </xsl:call-template>
            </xsl:if>
        </xsl:if>
        <xsl:apply-templates/>
    </xsl:template>

    <xsl:template match="wadl:request/wadl:representation[@mediaType]">
        <xsl:variable name="defaultNext" select="$ACCEPT"/>
        <step type="REQ_TYPE">
            <xsl:attribute name="id" select="generate-id()"/>
            <!-- Note that matches on the media type are always case insensitive -->
            <xsl:attribute name="match" select="concat('(?i)',check:toRegExEscaped(@mediaType))"/>
            <xsl:choose>
                <xsl:when test="$useWellFormCheck">
                    <xsl:choose>
                        <xsl:when test="check:isXML(@mediaType) or check:isJSON(@mediaType)">
                            <xsl:call-template name="check:addWellFormNext"/>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:attribute name="next" select="$defaultNext"/>
                        </xsl:otherwise>
                    </xsl:choose>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:attribute name="next" select="$defaultNext"/>
                </xsl:otherwise>
            </xsl:choose>
            <xsl:call-template name="check:addLabel"/>
        </step>
        <xsl:if test="$useWellFormCheck">
            <xsl:choose>
                <xsl:when test="check:isXML(@mediaType)">
                    <xsl:call-template name="check:addWellForm">
                        <xsl:with-param name="type" select="'WELL_XML'"/>
                    </xsl:call-template>
                </xsl:when>
                <xsl:when test="check:isJSON(@mediaType)">
                    <xsl:call-template name="check:addWellForm">
                        <xsl:with-param name="type" select="'WELL_JSON'"/>
                    </xsl:call-template>
                </xsl:when>
            </xsl:choose>
        </xsl:if>
    </xsl:template>

    <xsl:function name="check:isXML" as="xsd:boolean">
        <xsl:param name="in" as="xsd:string"/>
        <xsl:value-of select="$in = 'application/xml' or ends-with($in,'+xml')"/>
    </xsl:function>

    <xsl:function name="check:isJSON" as="xsd:boolean">
        <xsl:param name="in" as="xsd:string"/>
        <xsl:value-of select="$in = 'application/json' or ends-with($in,'+json')"/>
    </xsl:function>

    <xsl:function name="check:WellFormID" as="xsd:string">
        <xsl:param name="context" as="node()"/>
        <xsl:value-of select="concat(generate-id($context),'W')"/>
    </xsl:function>

    <xsl:function name="check:WellFormFailID" as="xsd:string">
        <xsl:param name="context" as="node()"/>
        <xsl:value-of select="concat(generate-id($context),'WF')"/>
    </xsl:function>

    <xsl:function name="check:HeaderID" as="xsd:string">
        <xsl:param name="context" as="node()"/>
        <xsl:value-of select="generate-id($context)"/>
    </xsl:function>

    <xsl:function name="check:HeaderFailID" as="xsd:string">
        <xsl:param name="context" as="node()"/>
        <xsl:value-of select="concat(check:HeaderID($context), 'HF')"/>
    </xsl:function>

    <xsl:function name="check:XSDID" as="xsd:string">
        <xsl:param name="context" as="node()"/>
        <xsl:value-of select="concat(generate-id($context),'XSD')"/>
    </xsl:function>

    <xsl:function name="check:XPathID" as="xsd:string">
        <xsl:param name="context" as="node()"/>
        <xsl:param name="number" as="xsd:integer"/>
        <xsl:value-of select="concat(generate-id($context),$number,'XPTH')"/>
    </xsl:function>

    <xsl:function name="check:PreProcID" as="xsd:string">
        <xsl:param name="context" as="node()"/>
        <xsl:param name="number" as="xsd:integer"/>
        <xsl:value-of select="concat(generate-id($context),$number,'PPROC')"/>
    </xsl:function>

    <xsl:function name="check:getXSLVersion" as="xsd:integer">
        <xsl:param name="root" as="node()"/>
        <xsl:value-of select="xsd:integer(substring($root/(xsl:transform | xsl:stylesheet)/@version,1,1))"/>
    </xsl:function>

    <xsl:template name="check:addWellFormNext">
        <xsl:attribute name="next" select="(check:WellFormID(.), check:WellFormFailID(.))" separator=" "/>
    </xsl:template>

    <xsl:template name="check:addWellForm">
        <xsl:param name="type" />
        <xsl:variable name="this" as="node()" select="."/>
        <xsl:variable name="defaultPlainParams" as="node()*"
                      select="wadl:param[xsd:boolean(@required) and @path and (@style='plain')]"/>
        <xsl:variable name="doXSD" as="xsd:boolean"
                      select="($type = 'WELL_XML') and $useXSDContentCheck"/>
        <xsl:variable name="doPreProcess" as="xsd:boolean"
                      select="($type = 'WELL_XML') and $usePreProcessExtension and exists(rax:preprocess)"/>
        <xsl:variable name="doElement" as="xsd:boolean"
                      select="($type = 'WELL_XML') and $useElementCheck and @element"/>
        <xsl:variable name="doReqPlainParam" as="xsd:boolean"
                      select="($type = 'WELL_XML') and $usePlainParamCheck and exists($defaultPlainParams)"/>
        <xsl:variable name="XSDID" as="xsd:string"
                      select="check:XSDID(.)"/>
        <xsl:variable name="XPathID" as="xsd:string"
                      select="check:XPathID(.,0)"/>
        <xsl:variable name="FAILID" as="xsd:string"
                      select="check:WellFormFailID(.)"/>
        <step type="{$type}" id="{check:WellFormID(.)}">
            <xsl:choose>
                <xsl:when test="$doElement">
                    <xsl:attribute name="next"
                                   select="($XPathID, $FAILID)"
                                   separator=" "/>
                </xsl:when>
                <xsl:when test="$doReqPlainParam">
                    <xsl:attribute name="next"
                                   select="(check:XPathID(.,1), $FAILID)"
                                   separator=" "/>
                </xsl:when>
                <xsl:when test="$doPreProcess">
                    <xsl:attribute name="next"
                                   select="check:PreProcID(.,1)"/>
                </xsl:when>
                <xsl:when test="$doXSD">
                    <xsl:attribute name="next"
                                   select="($XSDID, $FAILID)"
                                   separator=" "/>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:attribute name="next" select="$ACCEPT"/>
                </xsl:otherwise>
            </xsl:choose>
        </step>
        <xsl:if test="$doElement">
            <step type="XPATH" id="{$XPathID}">
                <xsl:choose>
                    <xsl:when test="$doReqPlainParam">
                        <xsl:attribute name="next"
                                       select="(check:XPathID(.,1), $FAILID)"
                                       separator=" "/>
                    </xsl:when>
                    <xsl:when test="$doPreProcess">
                        <xsl:attribute name="next"
                                       select="check:PreProcID($this, 1)"/>
                    </xsl:when>
                    <xsl:when test="$doXSD">
                        <xsl:attribute name="next"
                                       select="($XSDID, $FAILID)"
                                       separator=" "/>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:attribute name="next" select="$ACCEPT"/>
                    </xsl:otherwise>
                </xsl:choose>
                <xsl:attribute name="match" select="concat('/',check:normType(resolve-QName(@element,.)))"/>
            </step>
        </xsl:if>
        <xsl:if test="$doReqPlainParam">
            <xsl:for-each select="$defaultPlainParams">
                <step type="XPATH" id="{check:XPathID($this,position())}" match="{@path}">
                    <xsl:choose>
                        <xsl:when test="position() = last()">
                            <xsl:choose>
                                <xsl:when test="$doPreProcess">
                                    <xsl:attribute name="next"
                                                   select="check:PreProcID($this, 1)"/>
                                </xsl:when>
                                <xsl:when test="$doXSD">
                                    <xsl:attribute name="next"
                                                   select="($XSDID, $FAILID)"
                                                   separator=" "/>
                                </xsl:when>
                                <xsl:otherwise>
                                    <xsl:attribute name="next" select="$ACCEPT"/>
                                </xsl:otherwise>
                            </xsl:choose>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:attribute name="next"
                                           select="(check:XPathID($this,position()+1), $FAILID)"
                                           separator=" "/>
                        </xsl:otherwise>
                    </xsl:choose>
                </step>
            </xsl:for-each>
        </xsl:if>
        <xsl:if test="$doPreProcess">
            <xsl:for-each select="rax:preprocess">
                <step type="XSL" id="{check:PreProcID($this, position())}">
                    <xsl:if test="@href">
                        <xsl:choose>
                            <xsl:when test="not(doc-available(@href))">
                                <xsl:message terminate="yes">[ERROR] Couldn't access transform <xsl:value-of select="@href"/></xsl:message>
                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:attribute name="href" select="@href"/>
                            </xsl:otherwise>
                        </xsl:choose>
                    </xsl:if>
                    <xsl:attribute name="version">
                        <xsl:choose>
                            <xsl:when test="@href">
                                <xsl:value-of select="check:getXSLVersion(doc(@href))"/>
                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:value-of select="check:getXSLVersion(.)"/>
                            </xsl:otherwise>
                        </xsl:choose>
                    </xsl:attribute>
                    <xsl:choose>
                        <xsl:when test="position() = last()">
                            <xsl:choose>
                                <xsl:when test="$doXSD">
                                    <xsl:attribute name="next" select="($XSDID, $FAILID)"
                                                   separator=" "/>
                                </xsl:when>
                                <xsl:otherwise>
                                    <xsl:attribute name="next" select="$ACCEPT"/>
                                </xsl:otherwise>
                            </xsl:choose>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:attribute name="next"
                                           select="check:PreProcID($this, position()+1)"/>
                        </xsl:otherwise>
                    </xsl:choose>
                    <xsl:copy-of select="child::*"/>
                </step>
            </xsl:for-each>
        </xsl:if>
        <xsl:if test="$doXSD">
            <step type="XSD" id="{$XSDID}" next="{$ACCEPT}"/>
        </xsl:if>
        <step type="CONTENT_FAIL" id="{$FAILID}"/>
    </xsl:template>

    <xsl:template name="check:addReqTypeFail">
        <step type="REQ_TYPE_FAIL">
            <xsl:attribute name="id" select="check:ReqTypeFailID(.)"/>
            <xsl:attribute name="notMatch">
                <xsl:value-of select="distinct-values(for $r in wadl:request/wadl:representation[@mediaType]
                                      return concat('(?i)',check:toRegExEscaped($r/@mediaType)))" separator="|"/>
            </xsl:attribute>
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

    <xsl:function name="check:ReqTypeFailID">
        <xsl:param name="from" as="node()"/>
        <xsl:value-of select="concat(generate-id($from),'rqt')"/>
    </xsl:function>

    <xsl:function name="check:getNextReqTypeLinks" as="xsd:string*">
        <xsl:param name="from" as="node()"/>
        <xsl:sequence select="if ($from/wadl:request/wadl:representation[@mediaType]) then
                              (for $r in $from/wadl:request/wadl:representation[@mediaType]
                              return generate-id($r), check:ReqTypeFailID($from)) else ()"/>
    </xsl:function>
        
    <xsl:function name="check:getNextURLLinks" as="xsd:string*">
        <xsl:param name="from" as="node()"/>
        <xsl:sequence select="for $r in $from/wadl:resource return 
            if (xsd:boolean($r/@rax:invisible)) then
            (generate-id($r), check:getNextURLLinks($r))
            else generate-id($r)"/>
    </xsl:function>

    <xsl:function name="check:getNextHeaderLinks" as="xsd:string*">
        <xsl:param name="from" as="node()"/>
        <xsl:choose>
            <xsl:when test="$useHeaderCheck">
                <xsl:variable name="firstHeader" select="check:getHeaders($from)[1]"/>
                <xsl:value-of select="(check:HeaderID($firstHeader), check:HeaderFailID($firstHeader))"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="()"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:function>

    <xsl:function name="check:haveHeaders" as="xsd:boolean">
        <xsl:param name="from" as="node()"/>
        <xsl:value-of select="$useHeaderCheck and check:getHeaders($from)"/>
    </xsl:function>

    <xsl:function name="check:getHeaders" as="node()*">
        <xsl:param name="from" as="node()"/>
        <xsl:sequence select="$from/wadl:param[@style='header' and @required='true']"/>
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
        <xsl:value-of select="concat($namespaces//check:ns[@uri = namespace-uri-from-QName($type)][1]/@prefix,':',local-name-from-QName($type))"/>
    </xsl:function>

    <xsl:function name="check:sort" as="xsd:string*">
        <xsl:param name="in" as="xsd:string*"/>
        <xsl:for-each select="$in">
            <xsl:sort select="."/>
            <xsl:value-of select="."/>
        </xsl:for-each>
    </xsl:function>
</xsl:stylesheet>
