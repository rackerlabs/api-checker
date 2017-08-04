<?xml version="1.0" encoding="UTF-8"?>
<!--
  raxGlobalExtn.xsl

  The builder.xsl expects certain global extension elements
  (rax:assert, rax:captureHeader) to live at the method
  request or representation level.  But these elements are allowed to
  appear at the resources and resource level.

  Additionally there is a @applyToChildren attribute that is applicable
  to these elements at the resource level that specifies that the feature
  should apply to all subresources.

  This stylesheet is responsible for moving extension elements found in
  resources and resource level and pushing those assertions down into
  the method level. It is also responsible for implementing the
  @applyToChildren functionality.

  Finally, the stylesheet performs error checking and
  provides appropriate warnings if an element is misplaced.

  Copyright 2017 Rackspace US, Inc.

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
<!DOCTYPE stylesheet [
  <!ENTITY extnsMatch "rax:assert|rax:captureHeader">
  <!ENTITY extns "(&extnsMatch;)">
  <!ENTITY extnsNames "rax:assert and rax:captureHader">
]>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    xmlns:rax="http://docs.rackspace.com/api"
    xmlns="http://wadl.dev.java.net/2009/02"
    xmlns:wadl="http://wadl.dev.java.net/2009/02"
    exclude-result-prefixes="wadl"
    version="2.0">

    <xsl:output method="xml" indent="yes"/>

    <xsl:template name="copy" match="@* | node()">
        <xsl:copy>
            <xsl:apply-templates select="@* | node()"/>
        </xsl:copy>
    </xsl:template>

    <!--
        The following templates gather local and global asserts.  The
        are the source of asserts.
    -->
    <xsl:template match="wadl:resources[&extns;]">
        <xsl:copy>
            <xsl:apply-templates select="@* | node()">
                <xsl:with-param name="globalAsserts" as="element()*" select="&extns;" tunnel="yes"/>
            </xsl:apply-templates>
        </xsl:copy>
    </xsl:template>

    <xsl:template match="wadl:resource">
        <xsl:param name="globalAsserts" as="element()*" tunnel="yes"/>
        <xsl:copy>
            <xsl:apply-templates select="@* | node()">
                <xsl:with-param name="globalAsserts" as="element()*" select="($globalAsserts, &extns;[xs:boolean(@applyToChildren)])" tunnel="yes"/>
                <xsl:with-param name="localAsserts" as="element()*" select="&extns;[not(@applyToChildren) or not(xs:boolean(@applyToChildren))]" tunnel="yes"/>
            </xsl:apply-templates>
        </xsl:copy>
    </xsl:template>

    <xsl:template match="wadl:request[&extns; and wadl:representation]">
        <xsl:param name="localAsserts" as="element()*" tunnel="yes"/>
        <xsl:copy>
            <xsl:apply-templates select="@* | node()">
                <xsl:with-param name="localAsserts" as="element()*" select="($localAsserts, &extns;)" tunnel="yes"/>
            </xsl:apply-templates>
        </xsl:copy>
    </xsl:template>

    <!--
        The following templates select the target for global and local asserts.
        They are the destination of asserts.
    -->
    <xsl:template match="wadl:request[not(wadl:representation)] |
        wadl:representation[(local-name(..) = 'request') and (namespace-uri(..) = 'http://wadl.dev.java.net/2009/02') and @mediaType]">
        <xsl:param name="globalAsserts" as="element()*" tunnel="yes"/>
        <xsl:param name="localAsserts" as="element()*" tunnel="yes"/>
        <xsl:copy>
            <xsl:apply-templates select="@* | node()"/>
            <xsl:copy-of select="$globalAsserts | $localAsserts"/>
            <xsl:if test="self::wadl:representation and ../wadl:representation[not(@mediaType)]">
                <xsl:copy-of select="../wadl:representation[not(@mediaType)]/&extns;"/>
            </xsl:if>
        </xsl:copy>
    </xsl:template>

    <xsl:template match="wadl:method[not(wadl:request)]">
        <xsl:param name="globalAsserts" as="element()*" tunnel="yes"/>
        <xsl:param name="localAsserts" as="element()*" tunnel="yes"/>
        <xsl:copy>
            <xsl:apply-templates select="@*"/>
            <xsl:apply-templates select="wadl:doc"/>
            <request>
                <xsl:copy-of select="$globalAsserts | $localAsserts"/>
            </request>
            <xsl:apply-templates select="wadl:response"/>
            <xsl:apply-templates select="node()[not(namespace-uri(.)='http://wadl.dev.java.net/2009/02')]"/>
        </xsl:copy>
    </xsl:template>

    <!--
        asserts on a representation that has no mediaType should be moved
        to the request level, if there are no requests with mediaType
    -->
    <xsl:template match="wadl:request[wadl:representation[not(@mediaType)]]">
        <xsl:param name="globalAsserts" as="element()*" tunnel="yes"/>
        <xsl:param name="localAsserts" as="element()*" tunnel="yes"/>
        <xsl:copy>
            <xsl:apply-templates select="@* | node()"/>
            <xsl:if test="every $r in wadl:representation satisfies not($r/@mediaType)">
                <xsl:copy-of select="$globalAsserts | $localAsserts | wadl:representation[not(@mediaType)]/&extns;"/>
            </xsl:if>
        </xsl:copy>
    </xsl:template>

    <!--
        Handle copying/removing of asserts and flag warnings if an
        assert is in the wrong place.
    -->
    <xsl:template match="&extnsMatch;">
        <xsl:variable name="parent" as="element()" select=".."/>
        <xsl:variable name="grandparent" as="element()" select="../.."/>
        <xsl:choose>
            <xsl:when test="namespace-uri($parent) = 'http://wadl.dev.java.net/2009/02'">
                <xsl:choose>
                    <!--
                        In these instances we are moving assertions, so don't copy
                        the originals.
                    -->
                    <xsl:when test="local-name($parent) = ('resources', 'resource')"/>
                    <xsl:when test="(local-name($parent) = 'request') and $parent/wadl:representation[@mediaType]"/>
                    <xsl:when test="(local-name($parent) = 'representation') and not($parent/@mediaType)"/>
                    <!--
                        These assertions should stay in place, so we copy them
                    -->
                    <xsl:when test="local-name($parent) = 'request'">
                        <xsl:call-template name="copy"/>
                    </xsl:when>
                    <xsl:when test="(local-name($parent) = 'representation') and (local-name($grandparent) = 'request')
                                    and (namespace-uri($grandparent) = 'http://wadl.dev.java.net/2009/02')">
                        <xsl:call-template name="copy"/>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:call-template name="badPlacement"/>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:when>
            <xsl:otherwise>
                <xsl:call-template name="badPlacement"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template name="badPlacement">
        <xsl:message>[WARNING] bad placement for <xsl:copy-of select="."  copy-namespaces="no"/> parent is <xsl:value-of select="name(..)"/> but</xsl:message>
        <xsl:message>[WARNING] only allowed parents for the &extnsNames; extensions are</xsl:message>
        <xsl:message>[WARNING] wadl:resources, wadl:resource, wadl:request, wadl:representation (of a method request)</xsl:message>
        <xsl:message>[WARNING] ignoring this assertion!</xsl:message>
    </xsl:template>
</xsl:stylesheet>
