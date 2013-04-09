<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:event="http://docs.rackspace.com/core/event"
    xmlns:lbaas="http://docs.rackspace.com/usage/lbaas"
    xmlns:atom="http://www.w3.org/2005/Atom"
    exclude-result-prefixes="event"
    version="1.0">

    <!-- Import utility templates -->
    <xsl:import href="util.xsl" />

    <xsl:output method="xml" encoding="UTF-8"/>

    <xsl:template match="lbaas:product[@sslMode='OFF']">
        <xsl:copy>
            <xsl:attribute name="bandWidthInSsl">0</xsl:attribute>
            <xsl:attribute name="bandWidthOutSsl">0</xsl:attribute>
            <xsl:attribute name="avgConcurrentConnectionsSsl">0</xsl:attribute>
            <xsl:apply-templates
                select="@*[(local-name() != 'bandWidthInSsl') and
                           (local-name() != 'bandWidthOutSsl') and
                           (local-name() != 'avgConcurrentConnectionsSsl')] | node()"/>
        </xsl:copy>
    </xsl:template>
    
    <xsl:template match="lbaas:product[@sslMode='ON']">
        <xsl:copy>
            <xsl:attribute name="bandWidthIn">0</xsl:attribute>
            <xsl:attribute name="bandWidthOut">0</xsl:attribute>
            <xsl:attribute name="avgConcurrentConnections">0</xsl:attribute>
            <xsl:apply-templates
                select="@*[(local-name() != 'bandWidthIn') and
                           (local-name() != 'bandWidthOut') and
                           (local-name() != 'avgConcurrentConnections')] | node()"/>
        </xsl:copy>
    </xsl:template>
</xsl:stylesheet>
