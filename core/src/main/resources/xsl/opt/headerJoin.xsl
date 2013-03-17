<?xml version="1.0" encoding="UTF-8"?>
<!--
   headerJoin.xsl

   This stylesheet takes a document in checker format and safely joins
   header states in different branches of execution. The operation
   works like removeDups.xsl, but this takes into account the fact
   that the same HTTP header may have multiple values, which are
   checked by different states, and it creates a state that check
   those values simultaneously.

   For Example:

              +===+
            /=| C +=
   +===+ /==  +===+ \== +===+
   | A +=               + Y |
   +===+ \==  +===+ /== +===+
            \=| D +=
              +===+

   Becomes:


   +===+    +===+    +===+
   | A +====+ B +====+ Y |
   +===+    +===+    +===+


   Here, C and D are states that check that the content of a header
   contians a value. The new state B, checks that the content of the
   header contain be any one of those values.

   The process is executed recursively.  It is assumed that the input
   file has already gone through removeDups.xsl and commonDups.xsl
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

        <xsl:for-each-group select="$nextStep[@type = ('HEADER','HEADER_ANY') and not(@code) and not(@message)]" group-by="@type">
            <xsl:for-each-group select="current-group()" group-by="@name">
                <xsl:if test="count(current-group()) &gt; 1">
                    <join type="{current-group()[1]/@type}" name="{current-group()[1]/@name}">
                        <xsl:attribute name="steps">
                            <xsl:value-of separator=" ">
                                <xsl:sequence select="current-group()/@id"/>
                            </xsl:value-of>
                        </xsl:attribute>
                    </join>
                </xsl:if>
            </xsl:for-each-group>
        </xsl:for-each-group>
        <xsl:for-each-group select="$nextStep[@type = ('HEADER','HEADER_ANY') and @code and not(@message)]" group-by="@type">
            <xsl:for-each-group select="current-group()" group-by="@name">
                <xsl:for-each-group select="current-group()" group-by="@code">
                    <xsl:if test="count(current-group()) &gt; 1">
                        <join type="{current-group()[1]/@type}" name="{current-group()[1]/@name}">
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
        <xsl:for-each-group select="$nextStep[@type = ('HEADER','HEADER_ANY') and not(@code) and @message]" group-by="@type">
            <xsl:for-each-group select="current-group()" group-by="@name">
                <xsl:for-each-group select="current-group()" group-by="@message">
                    <xsl:if test="count(current-group()) &gt; 1">
                        <join type="{current-group()[1]/@type}" name="{current-group()[1]/@name}">
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
        <xsl:for-each-group select="$nextStep[@type = ('HEADER','HEADER_ANY') and @code and @message]" group-by="@type">
            <xsl:for-each-group select="current-group()" group-by="@name">
                <xsl:for-each-group select="current-group()" group-by="@code">
                    <xsl:for-each-group select="current-group()" group-by="@message">
                        <xsl:if test="count(current-group()) &gt; 1">
                            <join type="{current-group()[1]/@type}" name="{current-group()[1]/@name}">
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
        <xsl:variable name="distinctMatches" as="xsd:string*" select="distinct-values($joinSteps/@match)"/>

        <step id="{generate-id(.)}" type="{@type}" name="{@name}">
            <xsl:call-template name="joinNext">
                <xsl:with-param name="checker" select="$checker"/>
                <xsl:with-param name="joins" select="$joins"/>
            </xsl:call-template>
            <xsl:attribute name="match">
                <xsl:value-of select="$distinctMatches" separator="|" />
            </xsl:attribute>
            <xsl:if test="$joinSteps[1]/@code">
                <xsl:attribute name="code" select="$joinSteps[1]/@code"/>
            </xsl:if>
            <xsl:if test="$joinSteps[1]/@message">
                <xsl:attribute name="code" select="$joinSteps[1]/@message"/>
            </xsl:if>
        </step>
    </xsl:template>
</xsl:stylesheet>
