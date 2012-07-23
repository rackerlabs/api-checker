<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:tst="http://www.rackspace.com/repose/wadl/checker/step/test"
    xmlns="http://www.rackspace.com/repose/wadl/checker/step/test"
    version="2.0" exclude-result-prefixes="tst">

    <xsl:template match="node() | @*">
        <xsl:copy>
            <xsl:apply-templates select="@* | node()"/>
        </xsl:copy>
    </xsl:template>

    <xsl:template match="tst:stepType">
        <xsl:choose>
            <xsl:when test=". = 'BEGIN'">
                <stepType>START</stepType>
            </xsl:when>
            <xsl:otherwise>
                <stepType><xsl:value-of select="."/></stepType>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template match="@stepType">
        <xsl:choose>
            <xsl:when test=". = 'BEGIN'">
                <xsl:attribute name="stepType">START</xsl:attribute>
            </xsl:when>
            <xsl:otherwise>
                <xsl:attribute name="stepType"><xsl:value-of select="."/></xsl:attribute>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

</xsl:stylesheet>
