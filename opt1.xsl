<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xsd="http://www.w3.org/2001/XMLSchema"
    xmlns:check="http://www.rackspace.com/repose/wadl/checker"
    xmlns="http://www.rackspace.com/repose/wadl/checker"
    exclude-result-prefixes="xsd check"
    version="2.0">
    
    <xsl:output indent="yes" method="xml"/>
    
    <xsl:template match="check:checker" name="replaceAllDups">
        <xsl:param name="checker" select="." as="node()"/>
        <xsl:variable name="dups" as="node()">
            <xsl:call-template name="getDups">
                <xsl:with-param name="checker" select="$checker"/>
            </xsl:call-template>
        </xsl:variable>
        <xsl:choose>
            <xsl:when test="not($dups/check:group)">
                <!-- No duplicats found, output checker -->
                <xsl:copy-of select="$checker"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:call-template name="replaceAllDups">
                    <xsl:with-param name="checker">
                        <xsl:call-template name="replaceDups">
                            <xsl:with-param name="checker" select="$checker"/>
                            <xsl:with-param name="dups" select="$dups"/>
                        </xsl:call-template>
                    </xsl:with-param>
                </xsl:call-template>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    
    <xsl:template name="replaceDups">
        <xsl:param name="checker" as="node()"/>
        <xsl:param name="dups" as="node()"/>
        <xsl:variable name="excludes" as="xsd:string*">
            <xsl:sequence select="tokenize(string-join($dups/check:group/@exclude,' '),' ')"/>
        </xsl:variable>
        <checker>
            <xsl:apply-templates select="$checker" mode="unDup">
                <xsl:with-param name="dups" select="$dups"/>
                <xsl:with-param name="excludes" select="$excludes"/>
            </xsl:apply-templates>
        </checker>
    </xsl:template>
    
    <xsl:template match="check:step" mode="unDup">
        <xsl:param name="dups" as="node()"/>
        <xsl:param name="excludes" as="xsd:string*"/>
        <xsl:choose>
            <xsl:when test="count(index-of($excludes,@id)) != 0"/>
            <xsl:otherwise>
                <step>
                    <xsl:apply-templates select="@*" mode="unDup">
                        <xsl:with-param name="dups" select="$dups"/>
                        <xsl:with-param name="excludes" select="$excludes"/>
                    </xsl:apply-templates>
                </step>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    
    <xsl:template match="@*" mode="unDup">
        <xsl:param name="dups" as="node()"/>
        <xsl:param name="excludes" as="xsd:string*"/>
        <xsl:choose>
            <xsl:when test="name() = 'next'">
                <xsl:variable name="nexts" as="xsd:string*" select="tokenize(.,' ')"/>
                <xsl:attribute name="next">
                  <xsl:value-of select="check:swapExclude($nexts,$excludes,$dups)" separator=" "/>
                </xsl:attribute>
            </xsl:when>
            <xsl:otherwise>
                <xsl:copy/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    
    <xsl:function name="check:swapExclude" as="xsd:string*">
        <xsl:param name="nexts" as="xsd:string*"/>
        <xsl:param name="excludes" as="xsd:string*"/>
        <xsl:param name="dups" as="node()"/>
        <xsl:sequence select="for $n in $nexts return if ($excludes = $n) then 
                              $dups/check:group/@include[contains(../@exclude,$n)] 
                              else $n"/>
    </xsl:function>
    
    <xsl:template name="getDups" as="node()">
        <xsl:param name="checker" as="node()"/>
        <checker>
            <xsl:for-each-group select="$checker//check:step" group-by="@type">
                <xsl:for-each-group select="current-group()" group-by="@next">
                    <xsl:for-each-group select="current-group()" group-by="@match">
                        <xsl:if test="count(current-group()) > 1">
                            <group>
                                <xsl:attribute name="include">
                                    <xsl:value-of select="current-group()[1]/@id"></xsl:value-of>
                                </xsl:attribute>
                                <xsl:attribute name="exclude">
                                    <xsl:value-of separator=" ">
                                        <xsl:sequence select="current-group()[position() != 1]/@id"></xsl:sequence>
                                    </xsl:value-of>
                                </xsl:attribute>
                            </group>
                        </xsl:if>
                    </xsl:for-each-group>
                </xsl:for-each-group>
            </xsl:for-each-group>
        </checker>
    </xsl:template>
    <xsl:template match="text()" mode="#all"/>
</xsl:stylesheet>