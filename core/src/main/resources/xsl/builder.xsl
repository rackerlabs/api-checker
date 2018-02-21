<?xml version="1.0" encoding="UTF-8"?>
<!--
   builder.xsl

   This stylesheet is responsible for converting a WADL into the
   checker format that is used natively by the api-checker to validate
   requests.

   The input is a WADL that has been normalized into "tree" format by
   WADL-Tools (https://github.com/rackspace/wadl-tools)

   The parameters below enable features. If no features are specified
   then only URI, Methods, and Media-Types are checked.

   The config flags below the parameters are what are used to actually
   drive the transformation. The reason for this is that enabling
   one feature via a parameter (enableXSDTransform) may depend on
   other features being turned on (enableXSDContentCheck).

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
<xsl:stylesheet 
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xsd="http://www.w3.org/2001/XMLSchema"
    xmlns:wadl="http://wadl.dev.java.net/2009/02"
    xmlns:check="http://www.rackspace.com/repose/wadl/checker"
    xmlns:json="http://json-schema.org/schema#"
    xmlns:rax="http://docs.rackspace.com/api"
    xmlns="http://www.rackspace.com/repose/wadl/checker"
    xmlns:util="http://www.rackspace.com/repose/wadl/checker/util"
    xmlns:svrl="http://purl.oclc.org/dsdl/svrl"
    exclude-result-prefixes="xsd wadl rax check svrl"
    version="3.0">

    <!-- Add prune steps template -->
    <xsl:import href="util/pruneSteps.xsl"/>

    <xsl:output indent="yes" method="xml"/>

    <!-- Paramenters -->
    <xsl:param name="user" as="xsd:string" select="'unknown'" />
    <xsl:param name="creator" as="xsd:string" select="'unknown'"/>
    <xsl:param name="schematronOutput" as="node()"><check:out/></xsl:param>
    <xsl:param name="configMetadata" as="node()">
        <params>
          <meta>
            <config option="enableIgnoreJSONSchemaExtension" value="true"/>
            <config option="setParamDefaults" value="false"/>
            <config option="enableMessageExtension" value="true"/>
            <config option="joinXPathChecks" value="false"/>
            <config option="doXSDGrammarTransform" value="false"/>
            <config option="enablePreProcessExtension" value="true"/>
            <config option="removeDups" value="false"/>
            <config option="checkXSDGrammar" value="false"/>
            <config option="enableCaptureHeaderExtension" value="true"/>
            <config option="xpathVersion" value="1"/>
            <config option="checkPlainParams" value="false"/>
            <config option="checkWellFormed" value="false"/>
            <config option="enableAnyMatchExtension" value="true"/>
            <config option="enableIgnoreXSDExtension" value="true"/>
            <config option="enableAssertExtension" value="true"/>
            <config option="checkJSONGrammar" value="false"/>
            <config option="checkElements" value="false"/>
            <config option="preserveRequestBody" value="false"/>
            <config option="checkHeaders" value="true"/>
            <config option="enableRaxRolesExtension" value="false"/>
            <config option="enableRaxIsTenantExtension" value="false"/>
            <config option="enableRaxRepresentationExtension" value="false"/>
            <config option="maskRaxRoles403" value="false"/>
            <config option="enableAuthenticatedByExtension" value="false"/>
            <config option="enableWarnHeaders" value="true"/>
            <config option="warnAgent" value="-"/>
          </meta>
        </params>
    </xsl:param>

    <xsl:variable name="checkXSDGrammar"  as="xsd:boolean" select="xsd:boolean(check:optionValue($configMetadata, 'checkXSDGrammar'))"/>
    <xsl:variable name="checkJSONGrammar" as="xsd:boolean" select="xsd:boolean(check:optionValue($configMetadata, 'checkJSONGrammar'))"/>
    <xsl:variable name="checkWellFormed"  as="xsd:boolean" select="xsd:boolean(check:optionValue($configMetadata, 'checkWellFormed'))"/>
    <xsl:variable name="checkElements"    as="xsd:boolean" select="xsd:boolean(check:optionValue($configMetadata, 'checkElements'))"/>
    <xsl:variable name="checkPlainParams" as="xsd:boolean" select="xsd:boolean(check:optionValue($configMetadata, 'checkPlainParams'))"/>
    <xsl:variable name="checkHeaders"     as="xsd:boolean" select="xsd:boolean(check:optionValue($configMetadata, 'checkHeaders'))"/>
    <xsl:variable name="setParamDefaults" as="xsd:boolean" select="xsd:boolean(check:optionValue($configMetadata, 'setParamDefaults'))"/>
    <xsl:variable name="doXSDGrammarTransform" as="xsd:boolean" select="xsd:boolean(check:optionValue($configMetadata, 'doXSDGrammarTransform'))"/>
    <xsl:variable name="enablePreProcessExtension" as="xsd:boolean" select="xsd:boolean(check:optionValue($configMetadata, 'enablePreProcessExtension'))"/>
    <xsl:variable name="enableIgnoreXSDExtension" as="xsd:boolean" select="xsd:boolean(check:optionValue($configMetadata, 'enableIgnoreXSDExtension'))"/>
    <xsl:variable name="enableIgnoreJSONSchemaExtension" as="xsd:boolean" select="xsd:boolean(check:optionValue($configMetadata, 'enableIgnoreJSONSchemaExtension'))"/>
    <xsl:variable name="enableMessageExtension" as="xsd:boolean" select="xsd:boolean(check:optionValue($configMetadata, 'enableMessageExtension'))"/>
    <xsl:variable name="enableRaxRolesExtension" as="xsd:boolean" select="xsd:boolean(check:optionValue($configMetadata, 'enableRaxRolesExtension'))"/>
    <xsl:variable name="enableRaxIsTenantExtension" as="xsd:boolean" select="xsd:boolean(check:optionValue($configMetadata, 'enableRaxIsTenantExtension'))"/>
    <xsl:variable name="enableRaxRepresentationExtension" as="xsd:boolean" select="xsd:boolean(check:optionValue($configMetadata, 'enableRaxRepresentationExtension'))"/>
    <xsl:variable name="enableAuthenticatedByExtension" as="xsd:boolean" select="xsd:boolean(check:optionValue($configMetadata, 'enableAuthenticatedByExtension'))"/>
    <xsl:variable name="enableCaptureHeaderExtension" as="xsd:boolean" select="xsd:boolean(check:optionValue($configMetadata, 'enableCaptureHeaderExtension'))"/>
    <xsl:variable name="enableAnyMatchExtension" as="xsd:boolean" select="xsd:boolean(check:optionValue($configMetadata, 'enableAnyMatchExtension'))"/>
    <xsl:variable name="enableWarnHeaders" as="xsd:boolean" select="xsd:boolean(check:optionValue($configMetadata, 'enableWarnHeaders'))"/>
    <xsl:variable name="enableAssertExtension" as="xsd:boolean" select="xsd:boolean(check:optionValue($configMetadata, 'enableAssertExtension'))"/>
    <xsl:variable name="warnAgent" as="xsd:string" select="check:optionValue($configMetadata,'warnAgent')"/>

    <!-- Do we have an XSD? -->
    <xsl:variable name="WADLhasXSD" as="xsd:boolean"
                  select="
                          if (//wadl:grammars/xsd:schema) then true() else
                            if (//wadl:grammars/wadl:include[doc-available(@href) and doc(@href)/xsd:schema]) then true() else
                              false()
                          " />

    <!-- Do we have JSON Schema? -->
    <xsl:variable name="WADLhasJSONSchema" as="xsd:boolean"
                  select="
                          if (//wadl:grammars/json:schema) then true() else
                           if (//wadl:grammars/wadl:include[unparsed-text-available(@href) and check:looksLikeJSONObject(unparsed-text(@href,'UTF-8'))]) then true() else
                             false()
                         "/>

    <!-- Actual Config Flags -->
    <xsl:variable name="useXSDContentCheck" as="xsd:boolean"
                  select="($checkXSDGrammar or $doXSDGrammarTransform) and $WADLhasXSD"/>
    <xsl:variable name="useJSONContentCheck" as="xsd:boolean"
                  select="$checkJSONGrammar and $WADLhasJSONSchema"/>
    <xsl:variable name="useXSDTransform" as="xsd:boolean"
                  select="$doXSDGrammarTransform and $useXSDContentCheck"/>
    <xsl:variable name="usePreProcessExtension" as="xsd:boolean"
                  select="$enablePreProcessExtension"/>
    <xsl:variable name="useIgnoreXSDExtension" as="xsd:boolean"
                  select="$enableIgnoreXSDExtension and $useXSDContentCheck"/>
    <xsl:variable name="useIgnoreJSONSchemaExtension" as="xsd:boolean"
                  select="$enableIgnoreJSONSchemaExtension and $useJSONContentCheck"/>
    <xsl:variable name="useElementCheck" as="xsd:boolean"
                  select="$checkElements"/>
    <xsl:variable name="usePlainParamCheck" as="xsd:boolean"
                  select="$checkPlainParams"/>
    <xsl:variable name="useWellFormCheck" as="xsd:boolean"
                  select="$checkWellFormed or $useXSDContentCheck or $checkElements or
                          $checkPlainParams or $useJSONContentCheck"/>
    <xsl:variable name="useRaxRoles" as="xsd:boolean"
                  select="$enableRaxRolesExtension"/>
    <xsl:variable name="useRaxRepresentation" as="xsd:boolean"
                  select="$enableRaxRepresentationExtension"/>
    <xsl:variable name="useAuthenticatedBy" as="xsd:boolean"
                  select="$enableAuthenticatedByExtension"/>
    <xsl:variable name="useAnyMatchExtension" as="xsd:boolean"
                 select="$enableAnyMatchExtension or $useRaxRoles or $useAuthenticatedBy"/>
    <xsl:variable name="useHeaderCheck" as="xsd:boolean"
                  select="$checkHeaders or $useRaxRoles or $useAuthenticatedBy"/>
    <xsl:variable name="useParamDefaults" as="xsd:boolean"
                  select="$setParamDefaults"/>
    <xsl:variable name="useMessageExtension" as="xsd:boolean"
                  select="$enableMessageExtension or $useRaxRoles or $useAuthenticatedBy"/>
    <xsl:variable name="useCaptureHeaderExtension" as="xsd:boolean"
                  select="$enableCaptureHeaderExtension"/>
    <xsl:variable name="useAssert" as="xsd:boolean" select="$enableAssertExtension or $useRaxRoles"/>
    <xsl:variable name="useWarnHeaders" as="xsd:boolean"
                  select="$enableWarnHeaders and ($useXSDTransform or $usePreProcessExtension)"/>
    <xsl:variable name="useIsTenantExtension" as="xsd:boolean" select="$enableRaxIsTenantExtension or $useRaxRoles"/>

    <!-- Version of XPath that supports JSON -->
    <xsl:variable name="JSON_XPATH_VERSION" select="'31'"/>

    <!-- Defaults Steps -->
    <xsl:variable name="START"       select="'S0'"/>
    <xsl:variable name="URL_FAIL"    select="'SE0'"/>
    <xsl:variable name="METHOD_FAIL" select="'SE1'"/>
    <xsl:variable name="ACCEPT"      select="'SA'"/>
    
    <!-- Useful namespaces -->
    <xsl:variable name="schemaNS" select="'http://www.w3.org/2001/XMLSchema'"/>

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
            Finally add metadata, namespaces, and remove unconnected nodes.
        -->
        <checker>
            <xsl:call-template name="check:addNamespaceNodes"/>
            <xsl:call-template name="check:addMetadata"/>
            <xsl:apply-templates mode="grammar"/>
            <xsl:variable name="pruned">
                <xsl:call-template name="util:pruneSteps">
                    <xsl:with-param name="checker" select="$pass2"/>
                </xsl:call-template>
            </xsl:variable>
            <xsl:copy-of select="$pruned//check:step"/>
        </checker>
    </xsl:template>

    <xsl:template match="wadl:grammars/wadl:include" mode="grammar">
        <xsl:choose>
            <xsl:when test="doc-available(@href)">
                <xsl:variable name="ns" select="doc(@href)/xsd:schema/@targetNamespace"/>
                <xsl:choose>
                    <xsl:when test="$ns">
                        <grammar ns="{$ns}" href="{@href}" type="W3C_XML"/>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:message>[WARNING] Don't understand XML grammar of <xsl:value-of select="@href"/> ignoring...</xsl:message>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:when>
            <xsl:when test="unparsed-text-available(@href)">
                <xsl:variable name="text" select="unparsed-text(@href,'UTF-8')"/>
                <xsl:choose>
                    <xsl:when test="check:looksLikeJSONObject($text)">
                        <grammar type="SCHEMA_JSON" href="{@href}"/>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:message>[WARNING] Don't understand unparsed grammar of <xsl:value-of select="@href"/> ignoring...</xsl:message>
                    </xsl:otherwise>
                </xsl:choose>
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

    <xsl:template match="wadl:grammars/json:schema[text()]" mode="grammar">
        <grammar type="SCHEMA_JSON"><xsl:value-of select="text()"/></grammar>
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

    <xsl:template name="check:addMetadata">
        <xsl:variable name="created-from" as="node()*">
            <xsl:apply-templates mode="addCreatedFrom" select="$schematronOutput"/>
        </xsl:variable>
        <meta>
            <xsl:if test="$user"><built-by><xsl:value-of select="$user"/></built-by></xsl:if>
            <xsl:if test="$creator"><created-by><xsl:value-of select="$creator"/></created-by></xsl:if>
            <created-on><xsl:value-of select="current-dateTime()"/></created-on>
            <xsl:for-each-group select="$created-from" group-by=".">
                <xsl:copy-of select="current-group()[1]"/>
            </xsl:for-each-group>
            <xsl:copy-of select="$configMetadata/check:meta/check:config"/>
        </meta>
    </xsl:template>

    <xsl:template match="svrl:active-pattern[@name='References']" mode="addCreatedFrom">
        <created-from><xsl:value-of select="@document"/></created-from>
    </xsl:template>

    <xsl:template match="svrl:successful-report[@role=('unparsedReference','includeReference')]/svrl:text" mode="addCreatedFrom">
        <created-from><xsl:value-of select="."/></created-from>
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

    <xsl:template match="(wadl:representation|rax:representation[$useRaxRepresentation])[@element]" mode="ns">
        <xsl:variable name="qname" select="resolve-QName(@element,.)" as="xsd:QName"/>
        <xsl:variable name="pfix" select="prefix-from-QName($qname)"/>
        <xsl:call-template name="check:printns">
            <xsl:with-param name="pfix" select="if (empty($pfix)) then '' else $pfix"/>
            <xsl:with-param name="uri" select="namespace-uri-from-QName($qname)"/>
        </xsl:call-template>
        <xsl:apply-templates mode="ns"/>
    </xsl:template>

    <xsl:template match="(wadl:representation|rax:representation[$useRaxRepresentation])[@mediaType and (check:isXML(@mediaType) or check:isJSON(@mediaType))]/wadl:param[(@style = 'plain') and @path]"
                  priority="2" mode="ns">
        <xsl:if test="$usePlainParamCheck">
          <xsl:call-template name="check:handleXPathNameSpaces"/>
        </xsl:if>
    </xsl:template>

    <xsl:template match="rax:captureHeader" mode="ns">
        <xsl:if test="$useCaptureHeaderExtension">
            <xsl:call-template name="check:handleXPathNameSpaces"/>
        </xsl:if>
    </xsl:template>

    <xsl:template match="rax:assert" mode="ns">
        <xsl:if test="$useAssert">
            <xsl:call-template name="check:handleXPathNameSpaces">
                <xsl:with-param name="test" select="@test"/>
            </xsl:call-template>
        </xsl:if>
    </xsl:template>

    <xsl:template name="check:handleXPathNameSpaces">
        <!--
            Copy all namespace nodes in the *this*
            param. And enure that we don't prune the namespaces.

            A nicer thing to do would be to parse out the XPath,
            include only those namespaces referenced and allow for
            pruning...
        -->
        <xsl:param name="this" as="node()" select="."/>
        <xsl:param name="test" as="xsd:string" select="$this/@path"/>
        <xsl:if test="contains($test,':')">
            <xsl:for-each select="in-scope-prefixes($this)">
                <xsl:if test="contains($test,concat(.,':'))">
                    <xsl:call-template name="check:printns">
                        <xsl:with-param name="pfix" select="."/>
                        <xsl:with-param name="uri" select="namespace-uri-for-prefix(.,$this)"/>
                    </xsl:call-template>
                </xsl:if>
            </xsl:for-each>
            <dont_prune/>
        </xsl:if>
    </xsl:template>

    <xsl:template name="check:printns">
        <xsl:param name="pfix" as="xsd:string" />
        <xsl:param name="uri" as="xsd:string"/>
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

    <xsl:template match="check:step" mode="addErrorStates">
        <step>
            <!-- Remove inRequest attributes -->
            <xsl:copy-of select="@*[name() != 'inRequest'] | node()"/>
        </step>
    </xsl:template>

    <!-- Only the following methods need error states added -->
    <xsl:template match="check:step[@type=('URL','URLXSD','START','HEADER','HEADERXSD','HEADER_ANY','HEADERXSD_ANY','HEADER_SINGLE','HEADERXSD_SINGLE','HEADER_ALL') and not(@inRequest)]" mode="addErrorStates">
        <xsl:variable name="myId" as="xsd:string" select="generate-id()"/>
        <xsl:variable name="nexts" as="xsd:string*" select="check:next(.)"/>
        <xsl:variable name="nextSteps" as="node()*" select="check:stepsByIds(..,$nexts)"/>
        <xsl:variable name="doConnect" as="xsd:boolean" select="empty($nextSteps[@match=$matchAll])"/>
        <!-- Vars for processing method fail -->
        <xsl:variable name="nextMethodMatch" as="xsd:string*" select="check:sort($nextSteps[@type='METHOD']/@match)"/>
        <xsl:variable name="haveMethodMatch" as="xsd:boolean" select="count($nextMethodMatch) &gt; 0"/>
        <xsl:variable name="MethodMatchID" as="xsd:string" select="concat($myId,'m')"/>
        <!-- Vars for processing url fail -->
        <xsl:variable name="nextURLMatch" as="xsd:string*" select="check:sort($nextSteps[@type='URL']/@match)"/>
        <xsl:variable name="nextURLXSDMatch" as="xsd:string*" select="check:sort($nextSteps[@type='URLXSD']/@match)"/>
        <xsl:variable name="haveURLMatch" as="xsd:boolean" select="(count($nextURLMatch) &gt; 0) or (count($nextURLXSDMatch) &gt; 0)"/>
        <xsl:variable name="URLMatchID" as="xsd:string" select="concat($myId,'u')"/>
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
    
    <xsl:template match="wadl:resource[@path]">
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
        <xsl:variable name="templateParam" as="node()?"
                      select="if ($templatePath) then check:paramForTemplatePath(.) else ()"/>
        <xsl:variable name="templatePathFixedValue" as="xsd:string?"
                      select="if ($templatePath) then $templateParam/@fixed else ()"/>
        <step>
            <xsl:attribute name="type">
                <xsl:choose>
                    <xsl:when test="$templatePath and not($templatePathFixedValue) and check:isXSDURL(.)">
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
                    <xsl:when test="$templatePathFixedValue">
                        <xsl:value-of select="check:toRegExEscaped($templatePathFixedValue)"/>
                    </xsl:when>
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
                    <xsl:value-of select="$templateParam/@name"/>
                </xsl:attribute>
                <xsl:attribute name="name">
                    <xsl:value-of select="$templateParam/@name"/>
                </xsl:attribute>
                <xsl:call-template name="check:addIsTenantExtension">
                    <xsl:with-param name="param" select="$templateParam"/>
                </xsl:call-template>
                <xsl:call-template name="check:addCaptureHeaderExtension">
                    <xsl:with-param name="context" select="$templateParam"/>
                </xsl:call-template>
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
    <!--
        Very simple test for JSON Schema.  Must start with '{' and end
        with '}'. If the text isn't truely JSON, the schema validator
        will catch it.
    -->
    <xsl:function name="check:looksLikeJSONObject" as="xsd:boolean">
        <xsl:param name="text" as="xsd:string"/>
        <xsl:variable name="normText" select="normalize-space($text)"/>
        <xsl:value-of select="starts-with($normText,'{') and ends-with($normText,'}')"/>
    </xsl:function>
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
                            <xsl:sequence select="for $m in current-group() return check:MethodID($m)"/>
                        </xsl:value-of>
                    </xsl:attribute>
                </step>
            </xsl:if>
        </xsl:for-each-group>
    </xsl:template>
    <xsl:template name="check:addRaxRepSteps">
        <xsl:param name="next" as="xsd:string*"/>
        <xsl:param name="from" as="node()" select="."/>
        <xsl:variable name="popId" as="xsd:string" select="check:PopID($from)"/>
        <xsl:for-each select="$from/rax:representation">
            <xsl:variable name="wfType" as="xsd:string">
                <xsl:choose>
                    <xsl:when test="check:isXML(@mediaType)">WELL_XML</xsl:when>
                    <xsl:when test="check:isJSON(@mediaType)">WELL_JSON</xsl:when>
                    <xsl:otherwise>
                        <xsl:message terminate="yes">[ERROR] Only XML and JSON mediaTypes are allowed with rax:representation</xsl:message>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:variable>
            <xsl:call-template name="check:addWellForm">
                <xsl:with-param name="type" select="$wfType"/>
                <xsl:with-param name="afterStep" select="$popId"/>
            </xsl:call-template>
        </xsl:for-each>
        <step type="POP_REP" id="{$popId}">
            <xsl:attribute name="next"
                           select="$next"
                           separator=" "/>
        </step>
    </xsl:template>
    <xsl:template name="check:addAssertSteps">
        <xsl:param name="next" as="xsd:string*"/>
        <xsl:param name="from" as="node()" select="."/>
        <xsl:variable name="asserts" select="check:getAsserts($from)" as="node()*"/>
        <step type="CONTENT_FAIL" id="{check:assertFailID($asserts[1])}"/>
        <xsl:for-each select="$asserts">
            <xsl:variable name="pos" select="position()"/>
            <step id="{check:assertID(.)}" type="ASSERT" match="{@test}">
                <xsl:copy-of select="(@message, @code)"/>
                <xsl:attribute name="next">
                    <xsl:choose>
                        <xsl:when test="$pos = last()">
                            <xsl:value-of separator=" " select="$next"/>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:value-of select="check:assertID($asserts[position() = ($pos+1)])"/>
                        </xsl:otherwise>
                    </xsl:choose>
                </xsl:attribute>
            </step>
        </xsl:for-each>
    </xsl:template>
    <xsl:template name="check:addCaptureHeaderSteps">
        <xsl:param name="next" as="xsd:string*"/>
        <xsl:param name="from" as="node()" select="."/>
        <xsl:variable name="captureHeaders" select="check:getCaptureHeaders($from)" as="node()*"/>
        <xsl:for-each select="$captureHeaders">
            <xsl:variable name="pos" select="position()"/>
            <step id="{check:captureHeaderID(.)}"
                  type="CAPTURE_HEADER" name="{@name}" path="{@path}">
                <xsl:call-template name="check:addIsTenantExtension">
                    <xsl:with-param name="isTenant" select="@isTenant"/>
                </xsl:call-template>
                <xsl:attribute name="next">
                    <xsl:choose>
                        <xsl:when test="$pos = last()">
                            <xsl:value-of separator=" " select="$next"/>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:value-of select="check:captureHeaderID($captureHeaders[position() = ($pos+1)])"/>
                        </xsl:otherwise>
                    </xsl:choose>
                </xsl:attribute>
            </step>
        </xsl:for-each>
    </xsl:template>
    <xsl:template name="check:addHeaderSteps">
        <xsl:param name="next" as="xsd:string*"/>
        <xsl:param name="from" as="node()" select="."/>
        <xsl:param name="inRequest" as="xsd:boolean" select="false()"/>
        <xsl:variable name="headers" select="check:getHeaders($from)" as="node()*"/>
        <xsl:variable name="names" select="distinct-values(for $h in $headers return upper-case($h/@name))"/>
        <xsl:variable name="default_headers" select="$headers[@default]" as="node()*"/>
        <xsl:variable name="header_next" as="xsd:string*">
            <xsl:choose>
                <xsl:when test="$headers">
                    <xsl:sequence select="check:getNextHeaderLinksByName($headers, $headers[1]/@name)"/>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:sequence select="$next"/>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        <xsl:if test="$useParamDefaults">
          <xsl:for-each-group select="$headers" group-by="upper-case(@name)">
            <xsl:if test="count(current-group()[@default]) gt 1">
              <xsl:message terminate="yes">[ERROR] Multiple headers <xsl:value-of select="current-group()[1]/@name"/> have multiple @default vaules.
              Only one default value is allowed.</xsl:message>
            </xsl:if>
          </xsl:for-each-group>
          <xsl:for-each select="$default_headers">
            <xsl:variable name="pos" select="position()"/>
            <step id="{check:SetHeaderID(.)}" type="SET_HEADER" name="{@name}" value="{@default}">
              <xsl:attribute name="next">
                <xsl:choose>
                  <xsl:when test="$pos = last()">
                    <xsl:value-of separator=" " select="$header_next"/>
                  </xsl:when>
                  <xsl:otherwise>
                    <xsl:value-of select="check:SetHeaderID($default_headers[position() = ($pos+1)])"/>
                  </xsl:otherwise>
                </xsl:choose>
              </xsl:attribute>
            </step>
          </xsl:for-each>
        </xsl:if>
        <xsl:for-each select="$names">
            <xsl:variable name="pos" select="position()"/>
            <xsl:variable name="last" select="last()"/>
            <xsl:variable name="current" select="." as="xsd:string"/>
            <xsl:variable name="multipleHeaders" as="xsd:boolean" select="count($headers[upper-case(@name)=$current]) gt 1" />
            <xsl:variable name="currentHeaders" as="node()*" select="$headers[upper-case(@name)=$current]"/>
            <xsl:for-each select="$currentHeaders">
                <xsl:variable name="isXSD" select="check:isXSDParam(.)"/>
                <xsl:variable name="isFixed" as="xsd:boolean" select="exists(@fixed)"/>
                <xsl:variable name="isRepeating" as="xsd:boolean" select="exists(@repeating) and xsd:boolean(@repeating)"/>
                <xsl:variable name="isHeaderAny" as="xsd:boolean" select="$useAnyMatchExtension and xsd:boolean(@rax:anyMatch)"/>
                <xsl:variable name="isTenant" as="xsd:boolean" select="$useIsTenantExtension and (some $h in $currentHeaders satisfies xsd:boolean($h/@rax:isTenant))"/>
                <xsl:choose>
                    <xsl:when test="$isRepeating and not($isHeaderAny) and $multipleHeaders">
                        <!-- This looks like HEADER_ALL with multiple matches which we process later -->
                    </xsl:when>
                    <xsl:otherwise>
                        <step id="{check:HeaderID(.)}" name="{@name}">
                            <xsl:call-template name="check:addMessageExtension"/>
                            <xsl:call-template name="check:addCaptureHeaderExtension"/>
                            <xsl:attribute name="type">
                                <xsl:choose>
                                    <xsl:when test="$isRepeating">
                                        <xsl:choose>
                                            <xsl:when test="$isFixed">HEADER_ANY</xsl:when>
                                            <xsl:when test="$isXSD and $multipleHeaders">HEADERXSD_ANY</xsl:when>
                                            <xsl:when test="$isXSD">HEADERXSD</xsl:when>
                                            <xsl:when test="$multipleHeaders">HEADER_ANY</xsl:when>
                                            <xsl:otherwise>HEADER</xsl:otherwise>
                                        </xsl:choose>
                                    </xsl:when>
                                    <xsl:when test="$isXSD and not($isFixed)">HEADERXSD_SINGLE</xsl:when>
                                    <xsl:otherwise>HEADER_SINGLE</xsl:otherwise>
                                </xsl:choose>
                            </xsl:attribute>
                            <xsl:attribute name="match">
                                <xsl:choose>
                                    <xsl:when test="$isFixed"><xsl:value-of select="check:toRegExEscaped(@fixed)"/></xsl:when>
                                    <xsl:otherwise><xsl:value-of select="check:getMatch(resolve-QName(@type,.))"/></xsl:otherwise>
                                </xsl:choose>
                            </xsl:attribute>
                            <xsl:if test="$useParamDefaults and @default and $isFixed">
                                <xsl:if test="@fixed != @default">
                                    <xsl:message terminate="yes">[ERROR] In header param <xsl:value-of select="@name"/> @default value "<xsl:value-of select="@default"/>"
                                    does not match @fixed value "<xsl:value-of select="@fixed"/>".</xsl:message>
                                </xsl:if>
                            </xsl:if>
                            <xsl:if test="$isTenant">
                                <xsl:attribute name="isTenant" select="'true'"/>
                            </xsl:if>
                            <xsl:attribute name="next">
                                <xsl:choose>
                                    <xsl:when test="$pos = $last">
                                        <xsl:value-of separator=" " select="$next"/>
                                    </xsl:when>
                                    <xsl:otherwise>
                                        <xsl:value-of separator=" "
                                                      select="check:getNextHeaderLinksByName($headers,$names[$pos+1])"/>
                                    </xsl:otherwise>
                                </xsl:choose>
                            </xsl:attribute>
                            <xsl:if test="$inRequest">
                                <xsl:attribute name="inRequest">true</xsl:attribute>
                            </xsl:if>
                        </step>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:for-each>
            <xsl:variable name="allHeaders" as="node()*" select="if ($multipleHeaders) then
                     if ($useAnyMatchExtension) then $headers[upper-case(@name)=$current and not(xsd:boolean(@rax:anyMatch)) and xsd:boolean(@repeating)]
                     else $headers[upper-case(@name)=$current and xsd:boolean(@repeating)]
                     else ()"/>
            <xsl:if test="not(empty($allHeaders))">
              <xsl:variable name="matches" as="xsd:string*">
                <xsl:for-each select="$allHeaders">
                  <xsl:choose>
                    <xsl:when test="@fixed">
                      <xsl:value-of select="check:toRegExEscaped(@fixed)"/>
                    </xsl:when>
                    <xsl:otherwise>
                      <xsl:variable name="typeQName" as="xsd:QName" select="resolve-QName(@type,.)"/>
                      <xsl:if test="(namespace-uri-from-QName($typeQName) = $schemaNS) and (local-name-from-QName($typeQName) = 'string')">
                        <xsl:value-of select="$matchAll"/>
                      </xsl:if>
                    </xsl:otherwise>
                  </xsl:choose>
                </xsl:for-each>
              </xsl:variable>
              <xsl:variable name="xsdMatches" as="xsd:string*">
                <xsl:for-each select="$allHeaders">
                  <xsl:if test="not(exists(@fixed))">
                    <xsl:variable name="typeQName" as="xsd:QName" select="resolve-QName(@type,.)"/>
                    <xsl:if test="not((namespace-uri-from-QName($typeQName) = $schemaNS) and (local-name-from-QName($typeQName) = 'string'))">
                      <xsl:value-of select="check:normType($typeQName)"/>
                    </xsl:if>
                  </xsl:if>
                </xsl:for-each>
              </xsl:variable>
              <step type="HEADER_ALL" id="{check:HeaderID($allHeaders[1])}" name="{$allHeaders[1]/@name}">
                <xsl:if test="not(empty($matches))">
                  <xsl:attribute name="matchRegEx"><xsl:value-of select="distinct-values($matches)" separator="|"/></xsl:attribute>
                </xsl:if>
                <xsl:if test="not(empty($xsdMatches))">
                  <xsl:attribute name="match"><xsl:value-of select="distinct-values($xsdMatches)" separator=" "/></xsl:attribute>
                </xsl:if>
                <xsl:for-each select="$allHeaders[@rax:message or @rax:code][1]">
                  <!--
                      grab extension form the header with the first message or code.
                  -->
                  <xsl:call-template name="check:addMessageExtension"/>
                </xsl:for-each>
                <xsl:for-each select="$allHeaders[@rax:captureHeader][1]">
                  <!-- grab extension from first header that has it-->
                  <xsl:call-template name="check:addCaptureHeaderExtension"/>
                </xsl:for-each>
                <!-- If any header was denoted as a tenant then this is a tenant -->
                <xsl:if test="$useIsTenantExtension and (some $h in $allHeaders satisfies xsd:boolean($h/@rax:isTenant))">
                    <xsl:attribute name="isTenant" select="'true'"/>
                </xsl:if>
                <xsl:attribute name="next">
                  <xsl:choose>
                    <xsl:when test="$pos = $last">
                      <xsl:value-of separator=" " select="$next"/>
                    </xsl:when>
                    <xsl:otherwise>
                      <xsl:value-of separator=" "
                                    select="check:getNextHeaderLinksByName($headers,$names[$pos+1])"/>
                    </xsl:otherwise>
                  </xsl:choose>
                </xsl:attribute>
                <xsl:if test="$inRequest">
                  <xsl:attribute name="inRequest">true</xsl:attribute>
                </xsl:if>
              </step>
            </xsl:if>
            <step type="CONTENT_FAIL" id="{check:HeaderFailID($headers[upper-case(@name) = $current][1])}"/>
        </xsl:for-each>
    </xsl:template>
    
    <xsl:template match="wadl:method">
        <xsl:variable name="haveHeaders" as="xsd:boolean"
                      select="wadl:request and check:haveHeaders(wadl:request)"/>
        <xsl:variable name="haveAsserts" as="xsd:boolean"
                      select="wadl:request and check:haveAsserts(wadl:request)"/>
        <xsl:variable name="haveCaptureHeaders" as="xsd:boolean"
                      select="wadl:request and check:haveCaptureHeaders(wadl:request)"/>
        <xsl:variable name="haveRaxRep" as="xsd:boolean"
                      select="wadl:request and check:haveRaxRep(wadl:request)"/>
        <xsl:variable name="nextSteps" as="xsd:string*">
            <xsl:sequence select="check:getNextReqTypeLinks(.)"/>
        </xsl:variable>
        <xsl:variable name="nextRaxReps" as="xsd:string*" select="check:getNextRaxRepLinks(wadl:request)"/>
        <xsl:variable name="nextAsserts" as="xsd:string*" select="check:getNextAssertLinks(wadl:request)"/>
        <xsl:variable name="nextCaptureHeaders" as="xsd:string*" select="check:getNextCaptureHeaderLinks(wadl:request)"/>
        <xsl:variable name="links" as="xsd:string*">
            <xsl:choose>
                <xsl:when test="$haveHeaders">
                    <xsl:sequence select="check:getNextHeaderLinks(wadl:request)"/>
                </xsl:when>
                <xsl:when test="$haveRaxRep">
                    <xsl:sequence select="$nextRaxReps"/>
                </xsl:when>
                <xsl:when test="$haveAsserts">
                    <xsl:sequence select="$nextAsserts"/>
                </xsl:when>
                <xsl:when test="$haveCaptureHeaders">
                    <xsl:sequence select="$nextCaptureHeaders"/>
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
        <xsl:if test="(not($raxid) or @id or
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
                    <xsl:with-param name="next">
                        <xsl:choose>
                            <xsl:when test="$haveRaxRep">
                                <xsl:sequence select="$nextRaxReps"/>
                            </xsl:when>
                            <xsl:when test="$haveAsserts">
                                <xsl:value-of select="$nextAsserts"/>
                            </xsl:when>
                            <xsl:when test="$haveCaptureHeaders">
                                <xsl:sequence select="$nextCaptureHeaders"/>
                            </xsl:when>
                            <xsl:when test="count($nextSteps) &gt; 0">
                                <xsl:value-of select="$nextSteps"/>
                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:value-of select="($ACCEPT)"/>
                            </xsl:otherwise>
                        </xsl:choose>
                    </xsl:with-param>
                    <xsl:with-param name="from" select="wadl:request"/>
                    <xsl:with-param name="inRequest" select="true()"/>
                </xsl:call-template>
            </xsl:if>
            <xsl:if test="$haveRaxRep">
                <xsl:call-template name="check:addRaxRepSteps">
                    <xsl:with-param name="next">
                        <xsl:choose>
                            <xsl:when test="$haveAsserts">
                                <xsl:value-of select="$nextAsserts"/>
                            </xsl:when>
                            <xsl:when test="$haveCaptureHeaders">
                                <xsl:sequence select="$nextCaptureHeaders"/>
                            </xsl:when>
                            <xsl:when test="count($nextSteps) &gt; 0">
                                <xsl:value-of select="$nextSteps"/>
                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:value-of select="($ACCEPT)"/>
                            </xsl:otherwise>
                        </xsl:choose>
                    </xsl:with-param>
                    <xsl:with-param name="from" select="wadl:request"/>
                </xsl:call-template>
            </xsl:if>
            <xsl:if test="$haveAsserts">
                <xsl:call-template name="check:addAssertSteps">
                    <xsl:with-param name="next">
                        <xsl:choose>
                            <xsl:when test="$haveCaptureHeaders">
                                <xsl:sequence select="$nextCaptureHeaders"/>
                            </xsl:when>
                            <xsl:when test="count($nextSteps) &gt; 0">
                                <xsl:sequence select="$nextSteps"/>
                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:value-of select="($ACCEPT)"/>
                            </xsl:otherwise>
                        </xsl:choose>
                    </xsl:with-param>
                    <xsl:with-param name="from" select="wadl:request"/>
                </xsl:call-template>
            </xsl:if>
            <xsl:if test="$haveCaptureHeaders">
                <xsl:call-template name="check:addCaptureHeaderSteps">
                    <xsl:with-param name="next">
                        <xsl:choose>
                            <xsl:when test="count($nextSteps) &gt; 0">
                                <xsl:sequence select="$nextSteps"/>
                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:value-of select="($ACCEPT)"/>
                            </xsl:otherwise>
                        </xsl:choose>
                    </xsl:with-param>
                    <xsl:with-param name="from" select="wadl:request"/>
                </xsl:call-template>
            </xsl:if>
        </xsl:if>
        <xsl:apply-templates/>
    </xsl:template>

    <xsl:template match="wadl:request/wadl:representation[@mediaType]">
        <xsl:variable name="defaultNext" select="$ACCEPT"/>
        <xsl:variable name="haveRaxRep" as="xsd:boolean" select="check:haveRaxRep(.)"/>
        <xsl:variable name="nextRaxReps" as="xsd:string*" select="check:getNextRaxRepLinks(.)"/>
        <xsl:variable name="haveAsserts" as="xsd:boolean" select="check:haveAsserts(.)"/>
        <xsl:variable name="nextAsserts" as="xsd:string*" select="check:getNextAssertLinks(.)"/>
        <xsl:variable name="haveCaptureHeaders" as="xsd:boolean" select="check:haveCaptureHeaders(.)"/>
        <xsl:variable name="nextCaptureHeaders" as="xsd:string*" select="check:getNextCaptureHeaderLinks(.)"/>
        <xsl:variable name="doUseWellFormCheck" as="xsd:boolean"
                      select="$useWellFormCheck or ($usePreProcessExtension and exists(rax:preprocess))
                              or $haveAsserts or $haveCaptureHeaders or $haveRaxRep"/>
        <step type="REQ_TYPE">
            <xsl:attribute name="id" select="generate-id()"/>
            <!-- Note that matches on the media type are always case insensitive -->
            <xsl:attribute name="match" select="check:mediaTypeToRegEx(@mediaType)"/>
            <xsl:choose>
                <xsl:when test="$doUseWellFormCheck">
                    <xsl:choose>
                        <xsl:when test="check:isXML(@mediaType) or check:isJSON(@mediaType)">
                            <xsl:call-template name="check:addWellFormNext"/>
                        </xsl:when>
                        <xsl:when test="$haveRaxRep">
                            <xsl:attribute name="next">
                                <xsl:value-of select="$nextRaxReps" separator=" "/>
                            </xsl:attribute>
                        </xsl:when>
                        <xsl:when test="$haveAsserts">
                            <xsl:attribute name="next">
                                <xsl:value-of select="$nextAsserts" separator=" "/>
                            </xsl:attribute>
                        </xsl:when>
                        <xsl:when test="$haveCaptureHeaders">
                            <xsl:attribute name="next">
                                <xsl:value-of select="$nextCaptureHeaders" separator=" "/>
                            </xsl:attribute>
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
        <xsl:if test="$doUseWellFormCheck">
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
                <xsl:otherwise>
                    <xsl:if test="$haveRaxRep">
                        <xsl:call-template name="check:addRaxRepSteps">
                            <xsl:with-param name="next">
                                <xsl:choose>
                                    <xsl:when test="$haveAsserts">
                                        <xsl:sequence select="$nextAsserts"/>
                                    </xsl:when>
                                    <xsl:when test="$haveCaptureHeaders">
                                        <xsl:sequence select="$nextCaptureHeaders"/>
                                    </xsl:when>
                                    <xsl:otherwise>
                                        <xsl:sequence select="$defaultNext"/>
                                    </xsl:otherwise>
                                </xsl:choose>
                            </xsl:with-param>
                            <xsl:with-param name="from" select="."/>
                        </xsl:call-template>
                    </xsl:if>
                    <xsl:if test="$haveAsserts">
                        <xsl:call-template name="check:addAssertSteps">
                            <xsl:with-param name="next">
                                <xsl:choose>
                                    <xsl:when test="$haveCaptureHeaders">
                                        <xsl:sequence select="$nextCaptureHeaders"/>
                                    </xsl:when>
                                    <xsl:otherwise>
                                        <xsl:sequence select="$defaultNext"/>
                                    </xsl:otherwise>
                                </xsl:choose>
                            </xsl:with-param>
                            <xsl:with-param name="from" select="."/>
                        </xsl:call-template>
                    </xsl:if>
                    <xsl:if test="$haveCaptureHeaders">
                        <xsl:call-template name="check:addCaptureHeaderSteps">
                            <xsl:with-param name="next" select="$defaultNext"/>
                            <xsl:with-param name="from" select="."/>
                        </xsl:call-template>
                    </xsl:if>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:if>
    </xsl:template>

    <xsl:function name="check:mediaTypeToRegEx" as="xsd:string">
        <xsl:param name="mediaType" as="xsd:string"/>
        <xsl:variable name="pre" as="xsd:string" select="'(?i)'"/>
        <!--
            Note, all of the regexes that we create need to use two
            capture groups.  Has to do with the way in which scala
            handles groups, which I think is silly.

            -jw
        -->
        <xsl:choose>
            <!-- any media type */* -->
            <xsl:when test="$mediaType = '*/*'">
                <xsl:value-of select="'(.*)()'"/>
            </xsl:when>
            <!-- any subtype type/* -->
            <xsl:when test="matches($mediaType,'^[^/]+/\*$')">
                <xsl:value-of select="concat($pre,'(',check:toRegExEscaped(replace($mediaType,'^([^/]+/)(\*)$','$1')),')(.*)')"/>
            </xsl:when>
            <!-- If we still see an '*' we have a bad media type -->
            <xsl:when test="contains($mediaType, '*')">
                <xsl:message terminate="yes">[ERROR] Bad mediatype '<xsl:value-of select="$mediaType"/>' valid media type ranges are of the form */* and type/*.  See RFC 2616, (14.1).</xsl:message>
            </xsl:when>
            <!-- mediatype (type/subtype) does not contain parameters, in this case we ignore params -->
            <xsl:when test="not(contains($mediaType,';'))">
                <xsl:value-of select="concat($pre,'(',check:toRegExEscaped($mediaType),')(;.*)?')"/>
            </xsl:when>
            <!--
                If we do have parameters we try to match exactly,
                this is a hack we should find a more accurate way
                of dealing with mediaType parameters...extension?
            -->
            <xsl:otherwise>
                <xsl:value-of select="concat($pre,'(',check:toRegExEscaped($mediaType),')()')"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:function>

    <xsl:function name="check:isXML" as="xsd:boolean">
        <xsl:param name="in" as="xsd:string"/>
        <xsl:value-of select="$in = 'application/xml' or ends-with($in,'+xml')"/>
    </xsl:function>

    <xsl:function name="check:isJSON" as="xsd:boolean">
        <xsl:param name="in" as="xsd:string"/>
        <xsl:value-of select="$in = 'application/json' or ends-with($in,'+json')"/>
    </xsl:function>

    <xsl:function name="check:WarnID" as="xsd:string">
        <xsl:param name="context" as="node()"/>
        <xsl:param name="number" as="xsd:integer"/>
        <xsl:value-of select="concat(generate-id($context),'Wrn',$number)"/>
    </xsl:function>

    <xsl:function name="check:WellFormID" as="xsd:string">
        <xsl:param name="context" as="node()"/>
        <xsl:value-of select="concat(generate-id($context),'W')"/>
    </xsl:function>

    <xsl:function name="check:WellFormFailID" as="xsd:string">
        <xsl:param name="context" as="node()"/>
        <xsl:value-of select="concat(generate-id($context),'WF')"/>
    </xsl:function>

    <xsl:function name="check:PopID" as="xsd:string">
        <xsl:param name="context" as="node()"/>
        <xsl:value-of select="concat(generate-id($context),'POP')"/>
    </xsl:function>


    <xsl:function name="check:SetHeaderID" as="xsd:string">
      <xsl:param name="context" as="node()"/>
      <xsl:value-of select="concat(generate-id($context), 'SH')"/>
    </xsl:function>

    <xsl:function name="check:HeaderID" as="xsd:string">
        <xsl:param name="context" as="node()"/>
        <xsl:value-of select="generate-id($context)"/>
    </xsl:function>

    <xsl:function name="check:HeaderFailID" as="xsd:string">
        <xsl:param name="context" as="node()"/>
        <xsl:value-of select="concat(check:HeaderID($context), 'HF')"/>
    </xsl:function>

    <xsl:function name="check:assertID" as="xsd:string">
        <xsl:param name="context" as="node()"/>
        <xsl:value-of select="generate-id($context)"/>
    </xsl:function>

    <xsl:function name="check:assertFailID" as="xsd:string">
        <xsl:param name="context" as="node()"/>
        <xsl:value-of select="concat(check:assertID($context), 'AF')"/>
    </xsl:function>

    <xsl:function name="check:captureHeaderID" as="xsd:string">
        <xsl:param name="context" as="node()"/>
        <xsl:value-of select="generate-id($context)"/>
    </xsl:function>

    <xsl:function name="check:XSDID" as="xsd:string">
        <xsl:param name="context" as="node()"/>
        <xsl:value-of select="concat(generate-id($context),'XSD')"/>
    </xsl:function>

    <xsl:function name="check:JSONID" as="xsd:string">
        <xsl:param name="context" as="node()"/>
        <xsl:value-of select="concat(generate-id($context),'JSON')"/>
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

    <xsl:function name="check:MethodID" as="xsd:string">
        <xsl:param name="m" as="node()"/>
        <xsl:variable name="parent" as="node()" select="$m/.."/>
        <xsl:value-of select="if ($m/@rax:id) then
                                 if ($parent/ancestor::*//wadl:method[@id=$m/@rax:id]) then
                                    generate-id($parent/ancestor::*//wadl:method[@id=$m/@rax:id])
                                 else
                                    generate-id(($parent/ancestor::*//wadl:method[@rax:id = $m/@rax:id])[1])
                              else generate-id($m)"/>
    </xsl:function>

    <xsl:template name="check:addWellFormNext">
        <xsl:attribute name="next" select="(check:WellFormID(.), check:WellFormFailID(.))" separator=" "/>
    </xsl:template>

    <xsl:template name="check:addWellForm">
        <xsl:param name="type" />
        <xsl:param name="afterStep" as="xsd:string*" select="$ACCEPT"/>
        <xsl:variable name="this" as="node()" select="."/>
        <xsl:variable name="defaultPlainParams" as="node()*"
                      select="wadl:param[xsd:boolean(@required) and @path and (@style='plain')]"/>
        <xsl:variable name="ignoreXSDCheck" as="xsd:boolean"
                      select="$useIgnoreXSDExtension and (xsd:boolean(@rax:ignoreXSD) or xsd:boolean(../@rax:ignoreXSD))"/>
        <xsl:variable name="ignoreJSONCheck" as="xsd:boolean"
                      select="$useIgnoreJSONSchemaExtension and (xsd:boolean(@rax:ignoreJSONSchema) or xsd:boolean(../@rax:ignoreJSONSchema))"/>
        <xsl:variable name="doXSD" as="xsd:boolean"
                      select="($type = 'WELL_XML') and $useXSDContentCheck and not($ignoreXSDCheck)"/>
        <xsl:variable name="doJSON" as="xsd:boolean"
                      select="($type = 'WELL_JSON') and $useJSONContentCheck and not($ignoreJSONCheck)"/>
        <xsl:variable name="doPreProcess" as="xsd:boolean"
                      select="($type = 'WELL_XML') and $usePreProcessExtension and exists(rax:preprocess)"/>
        <xsl:variable name="doElement" as="xsd:boolean"
                      select="($type = 'WELL_XML') and $useElementCheck and @element"/>
        <xsl:variable name="doReqPlainParam" as="xsd:boolean"
                      select="($type = ('WELL_XML','WELL_JSON')) and $usePlainParamCheck and exists($defaultPlainParams)"/>
        <xsl:variable name="doRaxRep" as="xsd:boolean"
                      select="check:haveRaxRep(.)"/>
        <xsl:variable name="haveAsserts" as="xsd:boolean" select="check:haveAsserts(.)"/>
        <xsl:variable name="haveCaptureHeaders" as="xsd:boolean" select="check:haveCaptureHeaders(.)"/>
        <xsl:variable name="XSDID" as="xsd:string"
                      select="check:XSDID(.)"/>
        <xsl:variable name="JSONID" as="xsd:string"
                      select="check:JSONID(.)"/>
        <xsl:variable name="XPathID" as="xsd:string"
                      select="check:XPathID(.,0)"/>
        <xsl:variable name="FAILID" as="xsd:string"
                      select="check:WellFormFailID(.)"/>
        <xsl:variable name="TRANSFORM_PREID" as="xsd:string"
                      select="check:WarnID(.,0)"/>
        <xsl:variable name="TRANSFORM_XSDID" as="xsd:string"
                      select="check:WarnID(.,1)"/>
        <xsl:variable name="assertNext" as="xsd:string*"
                      select="check:getNextAssertLinks(.)"/>
        <xsl:variable name="captureHeadersNext" as="xsd:string*"
                      select="check:getNextCaptureHeaderLinks(.)"/>
        <xsl:variable name="raxRepNext" as="xsd:string*"
                      select="check:getNextRaxRepLinks(.)"/>
        <xsl:variable name="typeToRepType" as="map(xsd:string, xsd:string)"
                      select="map { 'WELL_XML'  : 'PUSH_XML_REP',
                                    'WELL_JSON' : 'PUSH_JSON_REP' }"/>
        <step id="{check:WellFormID(.)}">
            <xsl:choose>
                <!--
                    When this is a rax:representation...
                -->
                <xsl:when test="namespace-uri(.) = 'http://docs.rackspace.com/api'">
                    <xsl:attribute name="type" select="$typeToRepType($type)"/>
                    <xsl:attribute name="path" select="@path"/>
                    <xsl:if test="exists(@name)">
                        <xsl:attribute name="name" select="@name"/>
                    </xsl:if>
                    <xsl:if test="exists(@version)">
                        <xsl:attribute name="version" select="@version"/>
                    </xsl:if>
                </xsl:when>
                <!--
                    Otherwise this is a wadl:representation...
                -->
                <xsl:otherwise>
                    <xsl:attribute name="type" select="$type"/>
                </xsl:otherwise>
            </xsl:choose>
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
                <xsl:when test="$doRaxRep">
                    <xsl:attribute name="next"
                                   select="$raxRepNext"
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
                <xsl:when test="$doJSON">
                    <xsl:attribute name="next"
                                   select="($JSONID, $FAILID)"
                                   separator=" "/>
                </xsl:when>
                <xsl:when test="$haveAsserts">
                    <xsl:attribute name="next"
                        select="$assertNext"
                        separator=" "/>
                </xsl:when>
                <xsl:when test="$haveCaptureHeaders">
                    <xsl:attribute name="next"
                        select="$captureHeadersNext"
                        separator=" "/>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:attribute name="next" select="$afterStep"/>
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
                    <xsl:when test="$doRaxRep">
                        <xsl:attribute name="next"
                                       select="$raxRepNext"
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
                    <xsl:when test="$haveAsserts">
                        <xsl:attribute name="next"
                            select="$assertNext"
                            separator=" "/>
                    </xsl:when>
                    <xsl:when test="$haveCaptureHeaders">
                        <xsl:attribute name="next"
                                       select="$captureHeadersNext"
                                       separator=" "/>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:attribute name="next" select="$afterStep"/>
                    </xsl:otherwise>
                </xsl:choose>
                <xsl:attribute name="match" select="concat('/',check:normType(resolve-QName(@element,.)))"/>
                <xsl:attribute name="message" select="concat('Expecting the root element to be: ',check:normType(resolve-QName(@element,.)))"/>
            </step>
        </xsl:if>
        <xsl:if test="$doReqPlainParam">
            <xsl:for-each select="$defaultPlainParams">
                <step id="{check:XPathID($this,position())}" match="{@path}" name="{@name}">
                    <xsl:attribute name="type">
                        <xsl:choose>
                            <xsl:when test="$type='WELL_XML'">XPATH</xsl:when>
                            <xsl:when test="$type='WELL_JSON'">JSON_XPATH</xsl:when>
                        </xsl:choose>
                    </xsl:attribute>
                    <xsl:if test="$type='WELL_JSON'">
                        <xsl:attribute name="version" select="$JSON_XPATH_VERSION"/>
                    </xsl:if>
                    <xsl:call-template name="check:addIsTenantExtension"/>
                    <xsl:call-template name="check:addMessageExtension"/>
                    <xsl:call-template name="check:addCaptureHeaderExtension"/>
                    <xsl:choose>
                        <xsl:when test="position() = last()">
                            <xsl:choose>
                                <xsl:when test="$doRaxRep">
                                    <xsl:attribute name="next"
                                                   select="$raxRepNext"
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
                                <xsl:when test="$doJSON">
                                    <xsl:attribute name="next"
                                                   select="($JSONID, $FAILID)"
                                                   separator=" "/>
                                </xsl:when>
                                <xsl:when test="$haveAsserts">
                                    <xsl:attribute name="next"
                                        select="$assertNext"
                                        separator=" "/>
                                </xsl:when>
                                <xsl:when test="$haveCaptureHeaders">
                                    <xsl:attribute name="next"
                                                   select="$captureHeadersNext"
                                                   separator=" "/>
                                </xsl:when>
                                <xsl:otherwise>
                                    <xsl:attribute name="next" select="$afterStep"/>
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
        <xsl:if test="$doRaxRep">
            <xsl:call-template name="check:addRaxRepSteps">
                <xsl:with-param name="from" select="."/>
                <xsl:with-param name="next">
                    <xsl:choose>
                        <xsl:when test="$doPreProcess">
                            <xsl:sequence select="check:PreProcID($this, 1)"/>
                        </xsl:when>
                        <xsl:when test="$doXSD">
                            <xsl:sequence select="($XSDID, $FAILID)"/>
                        </xsl:when>
                        <xsl:when test="$doJSON">
                            <xsl:sequence select="($JSONID, $FAILID)"/>
                        </xsl:when>
                        <xsl:when test="$haveAsserts">
                            <xsl:sequence select="$assertNext"/>
                        </xsl:when>
                        <xsl:when test="$haveCaptureHeaders">
                            <xsl:sequence select="$captureHeadersNext"/>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:sequence select="$afterStep"/>
                        </xsl:otherwise>
                    </xsl:choose>
                </xsl:with-param>
            </xsl:call-template>
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
                                <xsl:when test="$useWarnHeaders">
                                    <xsl:attribute name="next" select="$TRANSFORM_PREID"/>
                                </xsl:when>
                                <xsl:when test="$haveAsserts">
                                    <xsl:attribute name="next"
                                        select="$assertNext"
                                        separator=" "/>
                                </xsl:when>
                                <xsl:when test="$haveCaptureHeaders">
                                    <xsl:attribute name="next"
                                                   select="$captureHeadersNext"
                                                   separator=" "/>
                                </xsl:when>
                                <xsl:otherwise>
                                    <xsl:attribute name="next" select="$afterStep"/>
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
            <xsl:if test="$useWarnHeaders">
                <step type="SET_HEADER_ALWAYS" id="{$TRANSFORM_PREID}" name="Warning" value='214 {$warnAgent} "Preprocess Transformation Applied"'>
                       <xsl:choose>
                          <xsl:when test="$haveAsserts">
                              <xsl:attribute name="next"
                                  select="$assertNext"
                                  separator=" "/>
                          </xsl:when>
                          <xsl:otherwise>
                              <xsl:attribute name="next" select="$afterStep"/>
                          </xsl:otherwise>
                      </xsl:choose>
                </step>
            </xsl:if>
        </xsl:if>
        <xsl:if test="$doJSON">
            <step type="JSON_SCHEMA" id="{$JSONID}">
                    <xsl:choose>
                        <xsl:when test="$haveAsserts">
                            <xsl:attribute name="next"
                                select="$assertNext"
                                separator=" "/>
                        </xsl:when>
                        <xsl:when test="$haveCaptureHeaders">
                            <xsl:attribute name="next"
                                           select="$captureHeadersNext"
                                           separator=" "/>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:attribute name="next" select="$afterStep"/>
                        </xsl:otherwise>
                    </xsl:choose>
            </step>
        </xsl:if>
        <xsl:if test="$doXSD">
            <step type="XSD" id="{$XSDID}">
                <xsl:choose>
                    <xsl:when test="$useWarnHeaders and $useXSDTransform">
                        <xsl:attribute name="next" select="$TRANSFORM_XSDID"/>
                    </xsl:when>
                    <xsl:when test="$useWarnHeaders and $doPreProcess">
                        <xsl:attribute name="next" select="$TRANSFORM_PREID"/>
                    </xsl:when>
                    <xsl:when test="$haveAsserts">
                        <xsl:attribute name="next"
                            select="$assertNext"
                            separator=" "/>
                    </xsl:when>
                    <xsl:when test="$haveCaptureHeaders">
                        <xsl:attribute name="next"
                                       select="$captureHeadersNext"
                                       separator=" "/>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:attribute name="next" select="$afterStep"/>
                    </xsl:otherwise>
                </xsl:choose>
            </step>
            <xsl:if test="$useWarnHeaders and $useXSDTransform">
                <step type="SET_HEADER_ALWAYS" id="{$TRANSFORM_XSDID}" name="Warning"
                      value='214 {$warnAgent} "Default values may have been filled in by XSD processor"'>
                    <xsl:choose>
                        <xsl:when test="$doPreProcess">
                            <xsl:attribute name="next" select="$TRANSFORM_PREID"/>
                        </xsl:when>
                        <xsl:when test="$haveAsserts">
                            <xsl:attribute name="next"
                                select="$assertNext"
                                separator=" "/>
                        </xsl:when>
                        <xsl:when test="$haveCaptureHeaders">
                            <xsl:attribute name="next"
                                           select="$captureHeadersNext"
                                           separator=" "/>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:attribute name="next" select="$afterStep"/>
                        </xsl:otherwise>
                    </xsl:choose>
                </step>
            </xsl:if>
        </xsl:if>
        <xsl:if test="$haveAsserts">
            <xsl:call-template name="check:addAssertSteps">
                <xsl:with-param name="next">
                    <xsl:choose>
                        <xsl:when test="$haveCaptureHeaders">
                            <xsl:sequence select="$captureHeadersNext"/>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:sequence select="$afterStep"/>
                        </xsl:otherwise>
                    </xsl:choose>
                </xsl:with-param>
                <xsl:with-param name="from" select="."/>
            </xsl:call-template>
        </xsl:if>
        <xsl:if test="$haveCaptureHeaders">
            <xsl:call-template name="check:addCaptureHeaderSteps">
                <xsl:with-param name="next" select="$afterStep"/>
                <xsl:with-param name="from" select="."/>
            </xsl:call-template>
        </xsl:if>
        <step type="CONTENT_FAIL" id="{$FAILID}"/>
    </xsl:template>

    <xsl:template name="check:addReqTypeFail">
        <step type="REQ_TYPE_FAIL">
            <xsl:attribute name="id" select="check:ReqTypeFailID(.)"/>
            <xsl:attribute name="notMatch">
                <xsl:value-of select="distinct-values(for $r in wadl:request/wadl:representation[@mediaType]
                                      return check:mediaTypeToRegEx($r/@mediaType))" separator="|"/>
            </xsl:attribute>
        </step>
    </xsl:template>

    <xsl:template match="text()" mode="#all"/>
    
    <xsl:template name="check:addLabel">
        <!--
            If an id a rax:id or doc title exists, use it as the label.
        -->
        <xsl:if test="@id or @rax:id or wadl:doc/@title">
            <xsl:attribute name="label">
                <xsl:choose>
                    <xsl:when test="wadl:doc/@title">
                        <xsl:value-of select="normalize-space(wadl:doc[1]/@title)"/>
                    </xsl:when>
                    <xsl:when test="@id">
                        <xsl:value-of select="normalize-space(@id)"/>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:value-of select="normalize-space(@rax:id)"/>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:attribute>
        </xsl:if>
    </xsl:template>

    <xsl:template name="check:addIsTenantExtension">
        <!--
            This sholud be called from a parameter that supports the
            isTenant extension.

            The context should be the appropriate wadl:parameter,
            otherwise the wadl:parameter should be passed in.

            The attribute is added if the useIsTenantExtension is
            enabled AND the extension is used for this step.
        -->
        <xsl:param name="param" as="node()" select="."/>
        <xsl:param name="isTenant" as="node()?" select="$param/@rax:isTenant"/>
        <xsl:if test="$useIsTenantExtension and $isTenant and xsd:boolean($isTenant)">
            <xsl:if test="not($param/@name)">
                <xsl:message terminate="yes">[ERROR] The element requires a name: <xsl:copy-of select="$param"/></xsl:message>
            </xsl:if>
            <xsl:attribute name="isTenant" select="'true'"/>
        </xsl:if>
    </xsl:template>

    <xsl:template name="check:addMessageExtension">
        <!--
            This should be called from a state that supports message
            and code attributes.

            The context should be the appropriate wadl:paramater.

            The attributes are added if the useMessageExtension is
            enabled AND the extension is used for this step.
        -->
        <xsl:if test="@rax:message and $useMessageExtension">
            <xsl:attribute name="message" select="@rax:message"/>
        </xsl:if>
        <xsl:if test="@rax:code and $useMessageExtension">
            <xsl:attribute name="code" select="@rax:code"/>
        </xsl:if>
    </xsl:template>

    <xsl:template name="check:addCaptureHeaderExtension">
        <!--
            This should be called from a state that supports capture
            header attributes.

            The context should be the appropriate wadl:paramater, this
            may be passed as a param.

            The attributes are added if the useCaptureHeaderExtension
            is enabled AND the extension is used for this step.
        -->
        <xsl:param name="context" as="node()" select="."/>
        <xsl:if test="$context/@rax:captureHeader and $useCaptureHeaderExtension">
            <xsl:attribute name="captureHeader" select="$context/@rax:captureHeader"/>
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
        <xsl:param name="from" as="node()?"/>
        <xsl:sequence select="if ($from/wadl:request/wadl:representation[@mediaType]) then
                              (for $r in $from/wadl:request/wadl:representation[@mediaType]
                              return generate-id($r), check:ReqTypeFailID($from)) else ()"/>
    </xsl:function>
        
    <xsl:function name="check:getNextURLLinks" as="xsd:string*">
        <xsl:param name="from" as="node()?"/>
        <xsl:sequence select="for $r in $from/wadl:resource[@path] return
            if ($r/@path = '/') then
            (check:getNextURLLinks($r))
            else if (xsd:boolean($r/@rax:invisible)) then
            (generate-id($r), check:getNextURLLinks($r))
            else generate-id($r)"/>
    </xsl:function>

    <xsl:function name="check:getNextHeaderLinks" as="xsd:string*">
      <xsl:param name="from" as="node()?"/>
      <xsl:variable name="headers" select="check:getHeaders($from)"/>
      <xsl:variable name="default" select="$headers[@default]" as="node()*"/>
      <xsl:choose>
        <xsl:when test="$useParamDefaults and $default">
          <xsl:value-of select="check:SetHeaderID($default[1])"/>
        </xsl:when>
        <xsl:when test="$headers">
          <xsl:value-of select="check:getNextHeaderLinksByName($headers,$headers[1]/@name)"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="()"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:function>

    <xsl:function name="check:getNextHeaderLinksByName" as="xsd:string*">
      <xsl:param name="headers" as="node()*"/> <!-- all headers -->
      <xsl:param name="headerName" as="xsd:string"/>
      <xsl:variable name="headersWithName" as="node()*" select="$headers[upper-case(@name)=upper-case($headerName)]"/>
      <xsl:choose>
        <xsl:when test="empty($headersWithName)">
          <xsl:value-of select="()"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:variable name="failID" as="xsd:string" select="check:HeaderFailID($headersWithName[1])"/>
          <xsl:variable name="singleHeaders" as="xsd:string*"
                        select="for $h in $headersWithName[not(xsd:boolean(@repeating))] return check:HeaderID($h)"/>
          <xsl:choose>
            <xsl:when test="$useAnyMatchExtension">
              <xsl:variable name="anyHeaders" select="for $h in $headersWithName[xsd:boolean(@rax:anyMatch) and xsd:boolean(@repeating)] return check:HeaderID($h)"/>
              <xsl:variable name="allHeaders" select="for $h in $headersWithName[not(xsd:boolean(@rax:anyMatch)) and xsd:boolean(@repeating)][1] return check:HeaderID($h)"/>
              <xsl:sequence select="($singleHeaders, $anyHeaders, $allHeaders, $failID)"/>
            </xsl:when>
            <xsl:otherwise>
              <xsl:variable name="allHeaders" select="for $h in $headersWithName[xsd:boolean(@repeating)][1] return check:HeaderID($h)"/>
              <xsl:sequence select="($singleHeaders, $allHeaders, $failID)"/>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:function>

    <xsl:function name="check:haveHeaders" as="xsd:boolean">
        <xsl:param name="from" as="node()?"/>
        <xsl:value-of select="$useHeaderCheck and check:getHeaders($from)"/>
    </xsl:function>

    <xsl:function name="check:getHeaders" as="node()*">
        <xsl:param name="from" as="node()?"/>
        <xsl:variable name="headers" as="node()*" select="$from/wadl:param[@style='header' and @required='true']"/>
        <xsl:variable name="unnamedHeaders" as="node()*" select="$headers[not(@name)]"/>
        <xsl:if test="exists($unnamedHeaders)">
            <!--
                Headers always require a name, if there's a header without
                a name that's an error condition. This used to fail with a
                cryptic error since we added support for header
                checks...we now provide a friendlier error message.

                Technically, this should be checked during WADL
                validation, but the W3C XSD for WADL does not set this
                requirement.

                It is uncleare how you can say anything about a header
                without specifying its name!
            -->
            <xsl:message terminate="yes">[ERROR] Headers always require a name:  <xsl:copy-of select="$unnamedHeaders"/></xsl:message>
        </xsl:if>
        <xsl:sequence select="$headers"/>
    </xsl:function>

    <xsl:function name="check:getNextAssertLinks" as="xsd:string*">
        <xsl:param name="from" as="node()?"/>
        <xsl:choose>
            <xsl:when test="check:haveAsserts($from)">
                <xsl:variable name="firstAssert" as="node()" select="check:getAsserts($from)[1]"/>
                <xsl:sequence select="(check:assertFailID($firstAssert), check:assertID($firstAssert))"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:sequence select="()"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:function>

    <xsl:function name="check:haveAsserts" as="xsd:boolean">
        <xsl:param name="from" as="node()?"/>
        <xsl:value-of select="$useAssert and $from/rax:assert"/>
    </xsl:function>

    <xsl:function name="check:getAsserts" as="node()*">
        <xsl:param name="from" as="node()?"/>
        <xsl:sequence select="$from/rax:assert"/>
    </xsl:function>

    <xsl:function name="check:getNextCaptureHeaderLinks" as="xsd:string*">
        <xsl:param name="from" as="node()?"/>
        <xsl:choose>
            <xsl:when test="check:haveCaptureHeaders($from)">
                <xsl:variable name="firstCaptureHeader" as="node()" select="check:getCaptureHeaders($from)[1]"/>
                <xsl:sequence select="(check:captureHeaderID($firstCaptureHeader))"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:sequence select="()"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:function>

    <xsl:function name="check:haveRaxRep" as="xsd:boolean">
        <xsl:param name="from" as="node()?"/>
        <xsl:value-of select="$useRaxRepresentation and exists($from/rax:representation)"/>
    </xsl:function>

    <xsl:function name="check:getNextRaxRepLinks" as="xsd:string*">
        <xsl:param name="from" as="node()?"/>
        <xsl:choose>
            <xsl:when test="check:haveRaxRep($from)">
                <xsl:sequence select="(for $rep in $from/rax:representation return check:WellFormID($rep),
                                       check:WellFormFailID($from/rax:representation[1]))"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:sequence select="()"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:function>

    <xsl:function name="check:haveCaptureHeaders" as="xsd:boolean">
        <xsl:param name="from" as="node()?"/>
        <xsl:value-of select="$useCaptureHeaderExtension and $from/rax:captureHeader"/>
    </xsl:function>

    <xsl:function name="check:getCaptureHeaders" as="node()*">
        <xsl:param name="from" as="node()?"/>
        <xsl:sequence select="$from/rax:captureHeader"/>
    </xsl:function>
    
    <xsl:function name="check:getNextMethodLinks" as="xsd:string*">
        <xsl:param name="from" as="node()?"/>
        <xsl:for-each-group select="$from/wadl:method" group-by="@name">
            <xsl:choose>
                <xsl:when test="count(current-group()) &gt; 1">
                    <xsl:sequence select="concat(current-grouping-key(),'_',generate-id($from))"/>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:sequence select="for $m in current-group() return check:MethodID($m)"/>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:for-each-group>
        <xsl:if test="$from/wadl:resource[@rax:invisible] or $from/wadl:resource[@path='/']">
            <xsl:for-each select="$from/wadl:resource[@rax:invisible] | $from/wadl:resource[@path='/']">
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
                <xsl:value-of select="$matchAll"/>
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
