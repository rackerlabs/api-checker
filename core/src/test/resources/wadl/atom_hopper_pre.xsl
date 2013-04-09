<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:event="http://docs.rackspace.com/core/event"
    xmlns:atom="http://www.w3.org/2005/Atom"
    exclude-result-prefixes="event"
    version="1.0">

    <!-- Import utility templates -->
    <xsl:import href="util.xsl" />

    <xsl:output method="xml" encoding="UTF-8"/>

    <xsl:template match="atom:entry[atom:content/event:event]">
        <xsl:variable name="event" select="atom:content/event:event"/>
        <xsl:copy>
            <atom:id><xsl:value-of select="concat('urn:uuid:',$event/@id)"/></atom:id>
            <xsl:call-template name="addCategory">
                <xsl:with-param name="term" select="$event/@tenantId"/>
                <xsl:with-param name="prefix" select="'tid:'"/>
            </xsl:call-template>
            <xsl:call-template name="addCategory">
                <xsl:with-param name="term" select="$event/@region"/>
                <xsl:with-param name="prefix" select="'rgn:'"/>
                <xsl:with-param name="default" select="'GLOBAL'"/>
            </xsl:call-template>
            <xsl:call-template name="addCategory">
                <xsl:with-param name="term" select="$event/@dataCenter"/>
                <xsl:with-param name="prefix" select="'dc:'"/>
                <xsl:with-param name="default" select="'GLOBAL'"/>
            </xsl:call-template>
            <xsl:call-template name="addCategory">
                <xsl:with-param name="term" select="$event/@resourceId"/>
                <xsl:with-param name="prefix" select="'rid:'"/>
            </xsl:call-template>
            <xsl:call-template name="addIdCategory">
                <xsl:with-param name="event" select="$event"/>
            </xsl:call-template>
            <xsl:apply-templates select="@* | node()"/>
        </xsl:copy>
    </xsl:template>

    <xsl:template match="atom:entry[atom:content/event:event]/atom:id">
        <!-- Ignore passed in ID -->
    </xsl:template>

    <!--
        STOP GAP: If the category is 'monitoring.check.usage' and the
        tenantId starts with hybrid: , then remove the category.
        Otherwise copy it.
    -->
    <xsl:template match="atom:category[@term='monitoring.check.usage']">
        <xsl:variable name="event" select="../atom:content/event:event"/>
        <xsl:choose>
            <xsl:when test="starts-with($event/@tenantId,'hybrid:')"/>
            <xsl:otherwise>
                <xsl:copy>
                    <xsl:apply-templates select="@* | node()"/>
                </xsl:copy>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
</xsl:stylesheet>
