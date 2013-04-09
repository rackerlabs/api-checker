<?xml version="1.0" encoding="UTF-8"?>
<!--
  util.xsl

  This is a utility stylesheet for XSL transformations.  It
  essentially is simply an identity template (copies input to output)
  with some additional useful templates processing atom entries.
-->
<!DOCTYPE stylesheet[
   <!ENTITY UPPERCASE "ABCDEFGHIJKLMNOPQRSTUVWXYZ">
   <!ENTITY LOWERCASE "abcdefghijklmnopqrstuvwxyz">
   <!ENTITY UPPER_TO_LOWER " '&UPPERCASE;', '&LOWERCASE;'">
]>
<xsl:stylesheet
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:event="http://docs.rackspace.com/core/event"
    xmlns:atom="http://www.w3.org/2005/Atom"
    exclude-result-prefixes="event"
    version="1.0">

    <xsl:output method="xml" encoding="UTF-8"/>

    <!--
        Identity transform.
    -->
    <xsl:template match="node() | @*">
        <xsl:copy>
            <xsl:apply-templates select="@* | node()"/>
        </xsl:copy>
    </xsl:template>

    <!--
        addIDCategory:

        Adds an atom category that uniquely identifies a product
        event. The category has one of two possible formats:

        {serviceCode}.{NSPart}.{resourceType}.{eventType}

        and

        {serviceCode}.{NSPart}.{eventType}

        The product event is passed in the 'event' parameter.
    -->
    <xsl:template name="addIdCategory">
        <xsl:param name="event"/>
        <xsl:variable name="prod" select="$event/child::*[1]"/>
        <xsl:variable name="nsPart">
            <xsl:if test="$prod">
                <xsl:call-template name="getNSPart">
                    <xsl:with-param name="ns" select="namespace-uri($prod)"/>
                </xsl:call-template>
            </xsl:if>
        </xsl:variable>
        <xsl:variable name="Id_1">
            <xsl:choose>
                <!--
                    If there is a resourceType then create a catagory from
                    the message like this:

                    {serviceCode}.{NSPart}.{resourceType}.{eventType}

                    Resource Type and eventType are converted to lowercase.

                    NSPart is the last path segment in the product
                    namespace.
                -->
                <xsl:when test="$prod/@resourceType">
                    <xsl:value-of
                        select="concat(translate($prod/@serviceCode, &UPPER_TO_LOWER;),'.',$nsPart,'.',
                                translate($prod/@resourceType, &UPPER_TO_LOWER;),'.',translate($event/@type, &UPPER_TO_LOWER;))" />
                </xsl:when>
                <!--
                    If there is no resourceType then we create a
                    catagory from the message like this:

                    {serviceCode}.{NSPart}.{eventType}

                    eventType is converted to lowercase

                    NSPart is the last path segment in the product
                    namespace.
                -->
                <xsl:otherwise>
                    <xsl:value-of
                        select="concat(translate($prod/@serviceCode, &UPPER_TO_LOWER;),'.',$nsPart,'.',translate($event/@type, &UPPER_TO_LOWER;))"/>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        <!--
            STOP GAP: Append .hybrid to the id when a tenantId starts with hybrid:
        -->
        <xsl:variable name="Id_2">
            <xsl:choose>
                <xsl:when test="starts-with($event/@tenantId,'hybrid:')">
                    <xsl:value-of select="concat($Id_1,'.hybrid')"/>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:value-of select="$Id_1"/>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        <xsl:call-template name="addCategory">
            <xsl:with-param name="term" select="$Id_2"/>
        </xsl:call-template>
    </xsl:template>

    <!--
        getNSPart:

        Given a namspace URI (passed in the 'ns' parameter) returns
        the last path segment in the URI.  This is known as the NSPart
        and is used when computing a unique category ID for an event.
    -->
    <xsl:template name="getNSPart">
        <xsl:param name="ns"/>
        <xsl:choose>
            <xsl:when test="contains($ns,'/')">
                <xsl:call-template name="getNSPart">
                    <xsl:with-param name="ns" select="substring-after($ns, '/')"/>
                </xsl:call-template>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="$ns"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <!--
        addCategory:

        Adds a new atom:catefory element.  The following parameters
        are used:

        1.  term    : The term used in the category. Typically, the term
                      is derived from an XPath so it may be blank.
        2.  default : The term to use when 'term' is blank.
        3.  prefix  : A prefix to be used in the category term.
    -->
    <xsl:template name="addCategory">
        <xsl:param name="term"/>
        <xsl:param name="default" select="''"/>
        <xsl:param name="prefix" select="''" />
        <xsl:variable name="actualTerm">
            <xsl:choose>
                <xsl:when test="$term != ''">
                    <xsl:value-of select="concat($prefix, $term)"/>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:if test="$default != ''">
                        <xsl:value-of select="concat($prefix, $default)"/>
                    </xsl:if>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        <!--
            If the category term is not empty and a category for that
            term does not already exist, add it.
        -->
        <xsl:if test="$actualTerm != '' and not(atom:category[@term = $actualTerm])">
            <atom:category term="{$actualTerm}"/>
        </xsl:if>
    </xsl:template>
</xsl:stylesheet>
