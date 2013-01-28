<?xml version="1.0" encoding="UTF-8"?>
<!--
   join.xsl

   This stylesheet take a document in checker format and safely joins
   states in different branches of execution. The operation works like
   removeDups.xsl except that output of a state need not be the same
   in each of the replaced states.

   For Example:

              +===+    +===+
            /=| B +====+ X |
   +===+ /==  +===+    +===+
   | A +=
   +===+ \==  +===+    +===+
            \=| B +====+ Y |
              +===+    +===+

   Becomes:
                     +===+
                    /+ X |
   +===+    +===+ /= +===+
   | A +====+ B +=
   +===+    +===+ \= +===+
                    \+ Y |
                     +===+

   The process is executed recursively.  It is assumed that the input
   file has already gone through the removeDups.xsl
-->
<xsl:stylesheet
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xsd="http://www.w3.org/2001/XMLSchema"
    xmlns:check="http://www.rackspace.com/repose/wadl/checker"
    xmlns:util="http://www.rackspace.com/repose/wadl/checker/util"
    xmlns="http://www.rackspace.com/repose/wadl/checker"
    exclude-result-prefixes="xsd check"
    version="2.0">

    <!--
        Most of the work of joining is done by the following util. The
        purpose of this template is to identify the joins and to
        create new join steps.
    -->
    <xsl:import href="../util/join.xsl"/>

    <!--
        Select join candidates.
    -->
    <xsl:template match="check:step[@next]" mode="getJoins">
        <xsl:param name="checker" as="node()"/>
        <xsl:variable name="nexts" as="xsd:string*" select="tokenize(@next,' ')"/>
        <xsl:variable name="nextStep" as="node()*" select="$checker//check:step[@id = $nexts]"/>
        <!-- Steps with the same @match, not versioned -->
        <xsl:for-each-group select="$nextStep[not(@version) and not(@name) and @match]" group-by="@type">
            <xsl:for-each-group select="current-group()" group-by="@match">
                <xsl:if test="count(current-group()) &gt; 1">
                    <join>
                        <xsl:attribute name="steps">
                            <xsl:value-of separator=" ">
                                <xsl:sequence select="current-group()/@id"/>
                            </xsl:value-of>
                        </xsl:attribute>
                    </join>
                </xsl:if>
            </xsl:for-each-group>
        </xsl:for-each-group>
        <!-- Versioned groups -->
        <xsl:for-each-group select="$nextStep[@version and @match]" group-by="@type">
            <xsl:for-each-group select="current-group()" group-by="@match">
                <xsl:for-each-group select="current-group()" group-by="@version">
                    <xsl:if test="count(current-group()) &gt; 1">
                        <join>
                            <xsl:attribute name="steps">
                                <xsl:value-of separator=" ">
                                    <xsl:sequence select="current-group()/@id"/>
                                </xsl:value-of>
                            </xsl:attribute>
                        </join>
                    </xsl:if>
                </xsl:for-each-group>
            </xsl:for-each-group>
        </xsl:for-each-group>
        <!-- Name groups -->
        <xsl:for-each-group select="$nextStep[@name and @match]" group-by="@type">
            <xsl:for-each-group select="current-group()" group-by="@match">
                <xsl:for-each-group select="current-group()" group-by="@name">
                    <xsl:if test="count(current-group()) &gt; 1">
                        <join>
                            <xsl:attribute name="steps">
                                <xsl:value-of separator=" ">
                                    <xsl:sequence select="current-group()/@id"/>
                                </xsl:value-of>
                            </xsl:attribute>
                        </join>
                    </xsl:if>
                </xsl:for-each-group>
            </xsl:for-each-group>
        </xsl:for-each-group>
        <!-- XSD Steps with transform -->
        <xsl:for-each-group select="$nextStep[@transform]" group-by="@type">
            <xsl:for-each-group select="current-group()" group-by="@transform">
                <xsl:if test="count(current-group()) &gt; 1">
                    <join>
                        <xsl:attribute name="steps">
                            <xsl:value-of separator=" ">
                                <xsl:sequence select="current-group()/@id"/>
                            </xsl:value-of>
                        </xsl:attribute>
                    </join>
                </xsl:if>
            </xsl:for-each-group>
        </xsl:for-each-group>
        <!-- XSL Steps, steps with href by type -->
        <xsl:for-each-group select="$nextStep[@href and not(@version)]" group-by="@type">
            <xsl:for-each-group select="current-group()" group-by="@href">
                <xsl:if test="count(current-group()) &gt; 1">
                    <join>
                        <xsl:attribute name="steps">
                            <xsl:value-of separator=" ">
                                <xsl:sequence select="current-group()/@id"/>
                            </xsl:value-of>
                        </xsl:attribute>
                    </join>
                </xsl:if>
            </xsl:for-each-group>
        </xsl:for-each-group>
        <xsl:for-each-group select="$nextStep[@href and @version]" group-by="@type">
            <xsl:for-each-group select="current-group()" group-by="@version">
                <xsl:for-each-group select="current-group()" group-by="@href">
                    <xsl:if test="count(current-group()) &gt; 1">
                        <join>
                            <xsl:attribute name="steps">
                                <xsl:value-of separator=" ">
                                    <xsl:sequence select="current-group()/@id"/>
                                </xsl:value-of>
                            </xsl:attribute>
                        </join>
                    </xsl:if>
                </xsl:for-each-group>
            </xsl:for-each-group>
        </xsl:for-each-group>
        <!-- Steps with no @match and no @transform-->
        <xsl:for-each-group select="$nextStep[not(@match) and not(@transform)]" group-by="@type">
            <xsl:if test="count(current-group()) &gt; 1">
                <join>
                    <xsl:attribute name="steps">
                        <xsl:value-of separator=" ">
                            <xsl:sequence select="current-group()/@id"/>
                        </xsl:value-of>
                    </xsl:attribute>
                </join>
            </xsl:if>
        </xsl:for-each-group>
    </xsl:template>

    <!--
        Convert joins into a checker step.
    -->
    <xsl:template match="check:join" mode="join">
        <xsl:param name="checker" as="node()"/>
        <xsl:param name="joins" as="node()*"/>
        <xsl:variable name="steps" select="@steps"/>
        <xsl:variable name="joinSteps" as="node()*" select="$checker//check:step[@id = tokenize($steps,' ')]"/>

        <step id="{generate-id(.)}">
            <!--
                Since the steps are exactly alike, we need to simply copy
                over the attributes from the first join step.
            -->
            <xsl:apply-templates select="$joinSteps[1]/@*[not(name() = ('next','label','id'))]"
                                 mode="copy"/>
            <xsl:call-template name="joinNext">
                <xsl:with-param name="checker" select="$checker"/>
                <xsl:with-param name="joins" select="$joins"/>
            </xsl:call-template>
        </step>
    </xsl:template>
</xsl:stylesheet>
