<?xml version="1.0" encoding="UTF-8"?>
<!--
   xpathJoin.xsl

   This stylesheet takes a document in checker format and replaces
   adjacent XPath states with a single XSLT that executes a
   combination of the XPath steps and prints an appropriate error.
   The stylesheet also combines the XML WellForm check with the XSL if
   there is an adjacent XPath state.
-->
<xsl:stylesheet
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xsd="http://www.w3.org/2001/XMLSchema"
    xmlns:check="http://www.rackspace.com/repose/wadl/checker"
    xmlns:xslout="http://www.rackspace.com/repose/wadl/checker/Transform"
    xmlns="http://www.rackspace.com/repose/wadl/checker"
    exclude-result-prefixes="xsd check"
    version="2.0">

    <!--
        Most of the work of joining is done by the following util. The
        purpose of this template is to identify the joins and produce
        their replacement steps.
    -->
    <xsl:import href="../util/join.xsl"/>

    <xsl:param name="defaultXPathVersion" as="xsd:integer" select="1"/>
    <xsl:param name="preserveRequestBody" as="xsd:boolean" select="false()"/>

    <xsl:namespace-alias stylesheet-prefix="xslout" result-prefix="xsl"/>

    <!--
        Identify the steps to join.
    -->
    <xsl:template match="check:step[@next]" mode="getJoins">
        <xsl:param name="checker" as="node()"/>
        <xsl:variable name="nexts" as="xsd:string*" select="tokenize(@next,' ')"/>
        <xsl:variable name="nextStep" as="node()*" select="$checker//check:step[@id = $nexts]"/>
        <xsl:apply-templates select="$nextStep" mode="targetJoins">
            <xsl:with-param name="checker" select="$checker"/>
        </xsl:apply-templates>
    </xsl:template>

    <xsl:template match="check:step[@type=('XSL', 'WELL_XML')]" mode="targetJoins">
        <xsl:param name="checker" as="node()"/>
        <xsl:variable name="nexts" as="xsd:string*" select="tokenize(@next,' ')"/>
        <xsl:variable name="nextStep" as="node()*" select="$checker//check:step[@id = $nexts and @type='XPATH']"/>

        <xsl:if test="count($nextStep) = 1">
            <xsl:choose>
                <xsl:when test="@type='XSL' and not(xsl:transform/@check:mergable)"/>
                <xsl:otherwise>
                    <join type="{@type}" steps="{@id}">
                        <xsl:attribute name="mergeSteps">
                            <xsl:value-of select="$nextStep/@id" separator=' '/>
                        </xsl:attribute>
                    </join>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:if>
    </xsl:template>

    <!--
        Produce joined steps
    -->
    <xsl:template match="check:join" mode="join">
        <xsl:param name="checker" as="node()"/>
        <xsl:param name="joins" as="node()*"/>

        <xsl:variable name="rootID" as="xsd:string" select="@steps"/>
        <xsl:variable name="mergeSteps" as="xsd:string*" select="tokenize(@mergeSteps,' ')"/>
        <xsl:variable name="steps" as="node()*" select="$checker//check:step[@id = $mergeSteps]"/>
        <xsl:variable name="root" as="node()" select="$checker//check:step[@id = $rootID]"/>
        <xsl:variable name="version" as="xsd:integer">
            <xsl:choose>
                <xsl:when test="$root/@type = 'XSL' and $root/@version='2'">2</xsl:when>
                <xsl:when test="$steps[@version='2']">2</xsl:when>
                <xsl:when test="$steps[not(@version)] and $defaultXPathVersion=2">2</xsl:when>
                <xsl:otherwise>1</xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        <xsl:variable name="preserveWellFormedStep" as="xsd:boolean" select="$preserveRequestBody and ($root/@type = 'WELL_XML')"/>
        <xsl:variable name="idBase" as="xsd:string" select="generate-id(.)"/>
        <xsl:variable name="newStepID" as="xsd:string">
            <xsl:choose>
                <xsl:when test="$preserveWellFormedStep">
                    <xsl:value-of select="concat($idBase, 'NS')"/>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:value-of select="$idBase"/>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>

        <!--
            If we are interested in preserving the request body, we
            must leave the WELL_XML check in tact.
        -->
        <xsl:if test="$preserveWellFormedStep">
            <step id="{$idBase}" type="WELL_XML" next="{$newStepID}"/>
        </xsl:if>

        <step id="{$newStepID}" type="XSL" version="{$version}">
            <xsl:call-template name="joinNext">
                <xsl:with-param name="checker" select="$checker"/>
                <xsl:with-param name="joins" select="$joins"/>
             </xsl:call-template>

             <xslout:transform version="{$version}.0" check:mergable="true">
                 <xslout:template match="/">
                     <xsl:if test="$root/@type = 'XSL'">
                         <xsl:copy-of select="$root/xsl:transform/xsl:template[@match='/']/xsl:choose"/>
                     </xsl:if>
                     <xslout:choose>
                         <xsl:apply-templates select="$steps" mode="joinXPath"/>
                         <xslout:otherwise>
                             <xslout:message terminate="yes">
                                 <xsl:value-of select="for $s in $steps[@type='XPATH'] return
                                                       if ($s/@message) then $s/@message
                                                       else concat('Expecting ',$s/@match)" separator=" or "/>
                                 <!--
                                     Since XPath or operations are not
                                     joined at the moment, then only
                                     the first error code need apply.
                                 -->
                                 <xsl:if test="$steps[@type='XPATH' and exists(@code)]">
                                     <xsl:value-of select="concat('C:',$steps[@type='XPATH']/@code[1],':C')"/>
                                 </xsl:if>
                             </xslout:message>
                         </xslout:otherwise>
                     </xslout:choose>

                     <xslout:copy>
                         <xslout:apply-templates select="node()"/>
                     </xslout:copy>
                 </xslout:template>

                 <xslout:template match="node() | @*">
                     <xslout:copy>
                         <xslout:apply-templates select="@* | node()"/>
                     </xslout:copy>
                 </xslout:template>
             </xslout:transform>
        </step>
    </xsl:template>

    <xsl:template match="check:step[@type='XPATH']" mode="joinXPath">
        <xslout:when test="{@match}"/>
    </xsl:template>

    <!--
        joinNext needs to operate a bit different because we are
        merging nodes.
    -->
    <xsl:template name="joinNext">
        <xsl:param name="checker" as="node()*"/>
        <xsl:param name="joins" as="node()*"/>
        <xsl:param name="join" as="node()" select="."/>
        <xsl:param name="steps" as="node()*" select="$checker//check:step[@id = tokenize($join/@steps,' ')]"/>
        <xsl:param name="nexts" as="xsd:string*" select="()"/>

        <xsl:variable name="mergeSteps" as="node()*">
            <!--
                If there are no current nexts, then merge in the merge
                steps
            -->
            <xsl:choose>
                <xsl:when test="empty($nexts)">
                    <xsl:variable name="MSteps" as="xsd:string*" select="tokenize(@mergeSteps,' ')"/>
                    <xsl:variable name="stepNodes" as="node()*" select="$checker//check:step[@id = $MSteps]"/>
                    <xsl:copy-of select="$stepNodes"/>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:copy-of select="$steps"/>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        <xsl:choose>
            <xsl:when test="empty($mergeSteps)">
                <xsl:attribute name="next">
                    <xsl:value-of separator=" ">
                        <xsl:sequence select="$nexts"/>
                    </xsl:value-of>
                </xsl:attribute>
            </xsl:when>
            <xsl:otherwise>
                <xsl:variable name="snexts" as="xsd:string*" select="check:getNexts($joins,tokenize($mergeSteps[1]/@next,' '))"/>
                <xsl:call-template name="joinNext">
                    <xsl:with-param name="checker" select="$checker"/>
                    <xsl:with-param name="joins" select="$joins"/>
                    <xsl:with-param name="steps" select="$mergeSteps[position() != 1]"/>
                    <xsl:with-param name="nexts"
                                    select="(for $s in $snexts
                                             return if (not($s = $nexts)) then $s else (), $nexts)"/>
                </xsl:call-template>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
</xsl:stylesheet>
