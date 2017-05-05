<?xml version="1.0" encoding="UTF-8"?>
<!--
   meta-transform.xsl

   This stylesheet rax:metadata into metadata resources with
   appropriate rax:roles attributes.

   Copyright 2015 Rackspace US, Inc.

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
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    xmlns:wadl="http://wadl.dev.java.net/2009/02"
    xmlns:rax="http://docs.rackspace.com/api"
    xmlns:meta="http://docs.rackspace.com/metadata/api"
    xmlns:map="http://www.w3.org/2005/xpath-functions/map"
    xmlns="http://wadl.dev.java.net/2009/02"
    exclude-result-prefixes="xsl"
    version="2.0">
    
    <xsl:output indent="yes"/>

    <xsl:variable name="metadata" select="/wadl:application/rax:metadata" as="node()*"/>
    <!--
        Error check first, bail if some of these assertions don't succeed.
    -->
    <xsl:template match="/">
        <xsl:choose>
            <!-- 
                Looks like we have metadata definition, check for errors, if none, then processes normally.
            -->
            <xsl:when test="wadl:application/rax:metadata">
                <xsl:call-template name="checkMetadata"/>
                <xsl:apply-templates select="node()"/>
            </xsl:when>
            <!--
                Looks like we have rax:useMetadata, but there are no rax:metadata definitions so fail..
             -->
            <xsl:when test="wadl:application/wadl:resources//wadl:resource[@rax:useMetadata]">
                <xsl:message terminate="yes">[ERROR] @rax:useMetadata found but missing &lt;rax:metadata&gt; definition.</xsl:message>
            </xsl:when>
            <!--
                Looks like we don't have any metadata assignments, so just copy things over as a noop.
             -->
            <xsl:otherwise>
                <xsl:apply-templates select="node()" mode="copy"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    <!--
        Some simple assertions, we check before we launch the transform - this will keep us
        from creating an invalid WADL and allow us to catch errors upfront.
    -->
    <xsl:template name="checkMetadata">
        <xsl:variable name="resources" as="node()*" select="wadl:application/wadl:resources//wadl:resource[@rax:useMetadata]"/>
        <xsl:for-each select="$metadata">
            <xsl:variable name="mid" as="xs:string?" select="@id"/>
            <xsl:if test="not($mid)">
                <xsl:message terminate="yes">[ERROR] rax:useMetaData is missing an id</xsl:message>
            </xsl:if>
            <xsl:if test="empty($resources[@rax:useMetadata= $mid])">
                <xsl:message>[WARNING] A resource that uses metadata '<xsl:value-of select="$mid"/>' is not found.</xsl:message>
            </xsl:if>
            <xsl:if test="not(every $mr in ./rax:metaRole satisfies $mr/@name)">
                <xsl:message terminate="yes">[ERROR] Every metaRole in '<xsl:value-of select="$mid"/>' must contain a name</xsl:message>
            </xsl:if>
            <xsl:if test="not(some $mr in ./rax:metaRole satisfies $mr/@pattern = '*')">
                <xsl:message terminate="yes">[ERROR] There should be at least one metaRole in '<xsl:value-of select="$mid"/>' that contains a pattern of '*'. This will be the admin role.</xsl:message>
            </xsl:if>
            <xsl:if test="not(every $mr in ./rax:metaRole satisfies if ($mr/@pattern) then $mr/@pattern = '*' or ends-with($mr/@pattern,':') else true())">
                <xsl:message terminate="yes">[ERROR] Error in metadata '<xsl:value-of select="$mid"/>': every pattern must either be '*' or end with ':'.</xsl:message>
            </xsl:if>
            <xsl:if test="count($metadata[@id=$mid]) &gt; 1">
                <xsl:message terminate="yes">[ERROR] Multiple metadata with id '<xsl:value-of select="$mid"/>' defined.</xsl:message>
            </xsl:if>
        </xsl:for-each>
        <xsl:for-each select="$resources">
            <xsl:variable name="mid" as="xs:string" select="@rax:useMetadata"/>
            <xsl:if test="empty($metadata[@id=$mid])">
                <xsl:message terminate="yes">[ERROR] A rax:metadata item with an id of '<xsl:value-of select="$mid"/>' is not defined.</xsl:message>
            </xsl:if>
        </xsl:for-each>
    </xsl:template>
    <xsl:template match="@* | node()" mode="copy #default">
        <xsl:copy>
            <xsl:apply-templates select="@* | node()" mode="#current"/>
        </xsl:copy>
    </xsl:template>
    <xsl:template match="wadl:application">
        <xsl:copy>
            <xsl:apply-templates select="@*"/>
            <xsl:if test="not(wadl:grammars)">
                <wadl:grammars>
                    <xsl:call-template name="addMetadataSchema"/>
                </wadl:grammars>
            </xsl:if>
            <xsl:apply-templates select="node()"/>
        </xsl:copy>
    </xsl:template>
    <xsl:template match="wadl:grammars">
        <xsl:copy>
            <xsl:apply-templates select="@* | node()"/>
            <xsl:call-template name="addMetadataSchema"/>
        </xsl:copy>
    </xsl:template>
    <xsl:template match="wadl:resource[@rax:useMetadata]">
        <xsl:copy>
            <xsl:apply-templates select="@*[not(name() = 'rax:useMetadata')]"/>
            <xsl:apply-templates select="node()"/>
            <xsl:call-template name="addMetadataAPI">
                <xsl:with-param name="useMetadata" select="@rax:useMetadata"/>
            </xsl:call-template>
        </xsl:copy>
    </xsl:template>
    <xsl:template match="rax:metadata"/>
    <xsl:template name="addMetadataSchema">
        <schema
            elementFormDefault="qualified"
            attributeFormDefault="unqualified"
            xmlns="http://www.w3.org/2001/XMLSchema"
            targetNamespace="http://docs.rackspace.com/metadata/api">
            <xsl:for-each-group select="$metadata//rax:metaRole[@pattern != '*']" group-by="@pattern">
                <xsl:variable name="patternId" as="xs:string" select="generate-id(current-group()[1])"/>
                <xsl:variable name="patternUnEsc" as="xs:string" select="current-group()[1]/@pattern"/>
                <xsl:comment>Type for pattern <xsl:value-of select="$patternUnEsc"/></xsl:comment>
                <xs:simpleType name="{$patternId}">
                    <xs:restriction base="xs:string">
                        <xs:pattern value="{rax:toRegExEscaped($patternUnEsc)}.*"/>
                    </xs:restriction>
                </xs:simpleType>
            </xsl:for-each-group>
            <xsl:for-each-group select="$metadata//rax:metaRole[not(@pattern)]" group-by="@name">
                <xsl:variable name="patternId" as="xs:string" select="generate-id(current-group()[1])"/>
                <xsl:variable name="patternUnEsc" as="xs:string" select="rax:pattern(current-group()[1])"/>
                <xsl:comment>Type for pattern <xsl:value-of select="$patternUnEsc"/></xsl:comment>
                <xs:simpleType name="{$patternId}">
                    <xs:restriction base="xs:string">
                        <xs:pattern value="{rax:toRegExEscaped($patternUnEsc)}.*"/>
                    </xs:restriction>
                </xs:simpleType>
            </xsl:for-each-group>
        </schema>
    </xsl:template>
    <!-- Inserts Metadata API with specifics for a particular Metadata group -->
    <xsl:template name="addMetadataAPI">
        <xsl:param name="useMetadata" as="xs:string"/>
        <xsl:variable name="uniqueName" as="xs:string" select="rax:metaResourceTypeName($useMetadata)"/>
        <xsl:variable name="metadata" as="node()" select="/wadl:application/rax:metadata[@id=$useMetadata]"/>
        <xsl:variable name="adminTypes" as="node()*" select="$metadata/rax:metaRole[@pattern='*']"/>
        <xsl:variable name="nonAdmins" as="node()*" select="$metadata/rax:metaRole[not(@pattern) or @pattern !='*']"/>
        <xsl:variable name="adminRaxRoles" as="xs:string"><xsl:value-of select="distinct-values($adminTypes/@name)" separator=" "/></xsl:variable>
        <xsl:variable name="nonAdminRaxRoles" as="xs:string"><xsl:value-of select="distinct-values($nonAdmins/@name)" separator=" "/></xsl:variable>
        <resource id="{$uniqueName}" path="metadata">
            <method name="GET" id="getResourceMetadata_{$uniqueName}">
                <wadl:doc xml:lang="EN" xmlns="http://docbook.org/ns/docbook"
                    title="Show resource metadata for {$uniqueName}"/>
                <response status="200 203"/>
            </method>
            <method name="PUT" id="setResourceMetadata_{$uniqueName}" rax:roles="{$adminRaxRoles}">
                <wadl:doc xml:lang="EN" xmlns="http://docbook.org/ns/docbook"
                    title="Create or update resource metadata for {$uniqueName}"/>
                <request>
                    <representation mediaType="application/json"/>
                    <representation mediaType="application/xml" element="meta:metadata"/>
                </request>
                <response status="200">
                    <representation mediaType="application/json"/>
                    <representation mediaType="application/xml" element="meta:metadata"/>
                </response>
            </method>
            <method name="PUT" id="setResourceMetadata_{$uniqueName}_NA" rax:roles="{$nonAdminRaxRoles}">
                <wadl:doc xml:lang="EN" xmlns="http://docbook.org/ns/docbook"
                    title="Create or update resource metadata for {$uniqueName}"/>
                <request>
                    <representation mediaType="application/json">
                        <xsl:call-template name="rax:metaAsserts">
                            <xsl:with-param name="nonAdmins" select="$nonAdmins"/>
                            <xsl:with-param name="keySelector" select="'map:keys($_?metadata)'"/>
                        </xsl:call-template>
                    </representation>
                    <representation mediaType="application/xml" element="meta:metadata">
                        <xsl:call-template name="rax:metaAsserts">
                            <xsl:with-param name="nonAdmins" select="$nonAdmins"/>
                            <xsl:with-param name="keySelector" select="'/meta:metadata/meta:meta/@key'"/>
                        </xsl:call-template>
                    </representation>
                </request>
                <response status="200">
                    <representation mediaType="application/json"/>
                    <representation mediaType="application/xml" element="meta:metadata"/>
                </response>
            </method>
            <method name="DELETE" id="deleteResourceMetadata_{$uniqueName}" rax:roles="{$adminRaxRoles}">
                <wadl:doc xmlns="http://docbook.org/ns/docbook" xml:lang="EN"
                    title="Delete resource metadata for {$uniqueName}"/>
                <response status="204"/>
            </method>
            <xsl:call-template name="metaKey">
                <xsl:with-param name="isAdmin" select="true()"/>
                <xsl:with-param name="roles" select="$adminRaxRoles"/>
                <xsl:with-param name="uniqueName" select="$uniqueName"/>
            </xsl:call-template>
            <xsl:for-each-group select="$nonAdmins[@pattern]" group-by="@pattern">
                <xsl:variable name="nonAdminRoles" as="xs:string*" select="current-group()/@name"/>
                <xsl:call-template name="metaKey">
                    <xsl:with-param name="isAdmin" select="false()"/>
                    <xsl:with-param name="roles" select="$nonAdminRoles"/>
                    <xsl:with-param name="uniqueName" select="rax:metaResourceXSDType((current-group()/@pattern)[1])"/>
                </xsl:call-template>
            </xsl:for-each-group>
            <xsl:for-each-group select="$nonAdmins[not(@pattern)]" group-by="@name">
                <xsl:variable name="nonAdminRoles" as="xs:string*" select="current-group()/@name"/>
                <xsl:call-template name="metaKey">
                    <xsl:with-param name="isAdmin" select="false()"/>
                    <xsl:with-param name="roles" select="$nonAdminRoles"/>
                    <xsl:with-param name="uniqueName" select="rax:metaResourceXSDTypeFromName((current-group()/@name)[1])"/>
                </xsl:call-template>
            </xsl:for-each-group>
        </resource>
    </xsl:template>
    <xsl:template name="metaKey">
        <xsl:param name="isAdmin" as="xs:boolean" select="false()"/>
        <xsl:param name="roles" as="xs:string*"/>
        <xsl:param name="uniqueName" as="xs:string"/>
        <xsl:variable name="raxRoles" as="xs:string"><xsl:value-of select="$roles" separator=" "/></xsl:variable>
        <xsl:variable name="uniqueId" as="xs:string" select="concat($uniqueName,'_',generate-id())"/>
        <resource id="key_{$uniqueId}">
            <xsl:attribute name="path">{key_<xsl:value-of select="$uniqueId"/>}</xsl:attribute>
            <param name="key_{$uniqueId}" style="template">
                <xsl:attribute name="type">
                    <xsl:choose>
                        <xsl:when test="$isAdmin">xs:string</xsl:when>
                        <xsl:otherwise><xsl:value-of select="concat('meta:',$uniqueName)"/></xsl:otherwise>
                    </xsl:choose>
                </xsl:attribute>
                <wadl:doc xmlns="http://docbook.org/ns/docbook" xml:lang="EN">
                    <para>A unique identifier for metadata item for <xsl:value-of select="$uniqueId"/></para>
                </wadl:doc>
            </param>
            <xsl:if test="$isAdmin">
                <method name="GET" id="getResourceMetadataItem_{$uniqueId}">
                    <wadl:doc xml:lang="EN" xmlns="http://docbook.org/ns/docbook"
                        title="Show resource metadata item details for {$uniqueId}"/>
                    <response status="200 203">
                        <representation mediaType="application/json"/>
                        <representation mediaType="application/xml" element="meta:meta"/>
                    </response>
                </method>
            </xsl:if>
            <method name="PUT" id="setResourceMetadataItem_{$uniqueId}" rax:roles="{$raxRoles}">
                <wadl:doc xml:lang="EN" xmlns="http://docbook.org/ns/docbook"
                    title="Create or update resource metadata item for {$uniqueId}"/>
                <request>
                    <representation mediaType="application/json"/>
                    <representation mediaType="application/xml" element="meta:meta"/>
                </request>
                <response status="200">
                    <representation mediaType="application/json"/>
                    <representation mediaType="application/xml" element="meta:meta"/>
                </response>
            </method>
            <method name="DELETE" id="deleteResourceMetadataItem_{$uniqueId}" rax:roles="{$raxRoles}">
                <wadl:doc xmlns="http://docbook.org/ns/docbook" xml:lang="EN"
                    title="Delete resource metadata item for {$uniqueId}"/>
                <response status="204"/>
            </method>
        </resource>
    </xsl:template>
    <xsl:template name="rax:metaAsserts">
        <xsl:param name="nonAdmins" as="node()*"/>
        <xsl:param name="keySelector" as="xs:string"/>
        <rax:assert message="The message must contain metadata items" code="400" test="not(empty({$keySelector}))"/>
        <rax:assert message="You are not allowed to set metadata items of this type" code="403">
            <xsl:variable name="test" as="xs:string*">
                let $roleToPattern := map {
                <xsl:for-each-group select="$nonAdmins" group-by="@name">
                    <xsl:value-of select="rax:quote(@name)"/> : (<xsl:value-of select="for $c in current-group() return
                            rax:quote(rax:pattern($c))"
                        separator=","/>)
                    <xsl:if test="position() != last()">,</xsl:if>
                </xsl:for-each-group>
                },
                $allowedPatterns := distinct-values(for $role in req:headers('x-roles', true()) return $roleToPattern($role)),
                $metaItems  := for $k in <xsl:value-of select="$keySelector"/> return string($k),
                $matchItems := distinct-values(for $meta in $metaItems return
                for $pattern in $allowedPatterns return
                if (starts-with($meta, $pattern)) then $meta else ())
                return count($matchItems) = count($metaItems)
            </xsl:variable>
            <xsl:attribute name="test" select="normalize-space(string-join($test,''))"/>
            <xsl:comment>
                                <xsl:copy-of select="$test"/>
                            </xsl:comment>
        </rax:assert>
    </xsl:template>
    <xsl:function name="rax:metaResourceTypeName" as="xs:string">
        <xsl:param name="metaDataName" as="xs:string"/>
        <xsl:value-of select="concat($metaDataName,'_RAX_META_TYPE')"/>
    </xsl:function>
    <xsl:function name="rax:metaResourceXSDType" as="xs:string">
        <xsl:param name="pattern" as="xs:string"/>
        <xsl:value-of select="generate-id(($metadata//rax:metaRole[@pattern = $pattern])[1])"/>
    </xsl:function>
    <xsl:function name="rax:metaResourceXSDTypeFromName" as="xs:string">
        <xsl:param name="name" as="xs:string"/>
        <xsl:value-of select="generate-id(($metadata//rax:metaRole[not(@pattern) and @name=$name])[1])"/>
    </xsl:function>
    <xsl:function name="rax:toRegExEscaped" as="xs:string">
        <xsl:param name="in" as="xs:string"/>
        <xsl:value-of select="replace($in,'\.|\\|\(|\)|\{|\}|\[|\]|\?|\+|\-|\^|\$|#|\*|\|','\\$0')"/>
    </xsl:function>
    <xsl:function name="rax:pattern" as="xs:string">
        <xsl:param name="metaRole" as="element()"/>
        <xsl:choose>
            <xsl:when test="$metaRole/@pattern"><xsl:value-of select="$metaRole/@pattern"/></xsl:when>
            <xsl:otherwise><xsl:value-of select="concat($metaRole/@name,':')"/></xsl:otherwise>
        </xsl:choose>
    </xsl:function>
    <xsl:function name="rax:quote" as="xs:string">
        <xsl:param name="in" as="xs:string"/>
        <xsl:variable name="q" as="xs:string">'</xsl:variable>
        <xsl:variable name="noquotes" as="xs:string" select='replace($in,$q,concat($q,$q))'/>
        <xsl:value-of select="concat($q,$noquotes,$q)"/>
    </xsl:function>
</xsl:stylesheet>
