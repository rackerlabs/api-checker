<?xml version="1.0" encoding="UTF-8"?>
<!--
   meta-transform.xsl

   This stylesheet ...

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
    xmlns="http://wadl.dev.java.net/2009/02"
    exclude-result-prefixes="xsl"
    version="2.0">
    
    <xsl:output indent="yes"/>
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
        <xsl:variable name="metadata" as="node()*" select="wadl:application/rax:metadata"/>
        <xsl:variable name="resources" as="node()*" select="wadl:application/wadl:resources//wadl:resource[@rax:useMetadata]"/>
        <xsl:for-each select="$metadata">
            <xsl:variable name="id" as="xs:string?" select="@id"/>
            <xsl:if test="not($id)">
                <xsl:message terminate="yes">[ERROR] rax:useMetaData is missing an id</xsl:message>
            </xsl:if>
            <xsl:if test="empty($resources[@rax:useMetadata= $id])">
                <xsl:message>[WARNING] A resource that uses metadata '<xsl:value-of select="$id"/>' is not found.</xsl:message>
            </xsl:if>
            <xsl:if test="not(every $mr in ./rax:metaRole satisfies $mr/@name)">
                <xsl:message terminate="yes">[ERROR] Every metaRole in '<xsl:value-of select="$id"/>' must contain a name</xsl:message>
            </xsl:if>
            <xsl:if test="not(some $mr in ./rax:metaRole satisfies $mr/@pattern = '*')">
                <xsl:message terminate="yes">[ERROR] There should be at least one metaRole in '<xsl:value-of select="$id"/>' that contains a pattern of '*'. This will be the admin role.</xsl:message>
            </xsl:if>
            <xsl:if test="count($metadata[@id=$id]) &gt; 1">
                <xsl:message terminate="yes">[ERROR] Multiple metadata with id '<xsl:value-of select="$id"/>' defined.</xsl:message>
            </xsl:if>
        </xsl:for-each>
        <xsl:for-each select="$resources">
            <xsl:variable name="id" as="xs:string" select="@rax:useMetadata"/>
            <xsl:if test="empty($metadata[@id=$id])">
                <xsl:message terminate="yes">[ERROR] A rax:metadata item with an id of '<xsl:value-of select="$id"/>' is not defined.</xsl:message>
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
                    <xsl:call-template name="addMetadataSchema">
                        <xsl:with-param name="metadata" select="rax:metadata"/>
                    </xsl:call-template>
                </wadl:grammars>
            </xsl:if>
            <xsl:apply-templates select="node()"/>
        </xsl:copy>
    </xsl:template>
    <xsl:template match="wadl:grammars">
        <xsl:copy>
            <xsl:apply-templates select="@*"/>
            <xsl:call-template name="addMetadataSchema">
                <xsl:with-param name="metadata" select="../rax:metadata"/>
            </xsl:call-template>
            <xsl:apply-templates/>
        </xsl:copy>
    </xsl:template>
    <xsl:template match="wadl:resource[@rax:useMetadata]">
        <xsl:copy>
            <xsl:apply-templates select="@*[not(name() = 'rax:useMetadata')]"/>
            <xsl:call-template name="addMetadataAPI">
                <xsl:with-param name="useMetadata" select="@rax:useMetadata"/>
            </xsl:call-template>
            <xsl:apply-templates select="node()"/>
        </xsl:copy>
    </xsl:template>
    <xsl:template match="rax:metadata"/>
    <xsl:template match="rax:metadata" mode="pattern">
        <xsl:variable name="metaRoles" as="node()*">
            <xsl:apply-templates select="rax:metaRole[not(@pattern) or @pattern !='*']" mode="metaCopy"/>
        </xsl:variable>
        <xsl:variable name="id" select="@id" as="xs:string"/>

        <xsl:for-each-group select="$metaRoles" group-by="@pattern">
            <xsl:variable name="name" as="xs:string" select="current-group()[1]/@name"/>
            <xs:simpleType name="{rax:metaResourceXSDType($id,rax:encodeRole($name))}">
                <xs:restriction base="xs:string">
                    <xs:pattern value="{rax:toRegExEscaped(current-group()[1]/@pattern)}.*"/>
                </xs:restriction>
            </xs:simpleType>
        </xsl:for-each-group>
    </xsl:template>
    <xsl:template name="addMetadataSchema">
        <xsl:param name="metadata" as="node()*"/>
        <schema
            elementFormDefault="qualified"
            attributeFormDefault="unqualified"
            xmlns="http://www.w3.org/2001/XMLSchema"
            targetNamespace="http://docs.rackspace.com/metadata/api">
            <xsl:apply-templates mode="pattern" select="$metadata"/>
        </schema>
    </xsl:template>
    <!-- Inserts Metadata API with specifics for a particular Metadata group -->
    <xsl:template name="addMetadataAPI">
        <xsl:param name="useMetadata" as="xs:string"/>
        <xsl:variable name="uniqueName" as="xs:string" select="rax:metaResourceTypeName($useMetadata)"/>
        <xsl:variable name="metadata" as="node()" select="/wadl:application/rax:metadata[@id=$useMetadata]"/>
        <xsl:variable name="adminTypes" as="node()*" select="$metadata/rax:metaRole[@pattern='*']"/>
        <xsl:variable name="nonAdmins" as="node()*">
            <xsl:apply-templates select="$metadata/rax:metaRole[not(@pattern) or @pattern !='*']" mode="metaCopy"/>
        </xsl:variable>
        <xsl:variable name="adminRaxRoles" as="xs:string"><xsl:value-of select="$adminTypes/@name" separator=" "/></xsl:variable>
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
            <xsl:for-each-group select="$nonAdmins" group-by="@pattern">
                <xsl:variable name="nonAdminRoles" as="xs:string*" select="current-group()/@name"/>
                <xsl:call-template name="metaKey">
                    <xsl:with-param name="isAdmin" select="false()"/>
                    <xsl:with-param name="roles" select="$nonAdminRoles"/>
                    <xsl:with-param name="uniqueName" select="rax:metaResourceXSDType($useMetadata,rax:encodeRole(@name))"/>
                </xsl:call-template>
            </xsl:for-each-group>
        </resource>
    </xsl:template>
    <xsl:template name="metaKey">
        <xsl:param name="isAdmin" as="xs:boolean" select="false()"/>
        <xsl:param name="roles" as="xs:string*"/>
        <xsl:param name="uniqueName" as="xs:string"/>
        <xsl:variable name="raxRoles" as="xs:string"><xsl:value-of select="$roles" separator=" "/></xsl:variable>
        <resource id="key_{$uniqueName}">
            <xsl:attribute name="path">{key_<xsl:value-of select="$uniqueName"/>}</xsl:attribute>
            <param name="key_{$uniqueName}" style="template">
                <xsl:attribute name="type">
                    <xsl:choose>
                        <xsl:when test="$isAdmin">xs:string</xsl:when>
                        <xsl:otherwise><xsl:value-of select="concat('meta:',$uniqueName)"/></xsl:otherwise>
                    </xsl:choose>
                </xsl:attribute>
                <wadl:doc xmlns="http://docbook.org/ns/docbook" xml:lang="EN">
                    <para>A unique identifier for metadata item for <xsl:value-of select="$uniqueName"/></para>
                </wadl:doc>
            </param>
            <xsl:if test="$isAdmin">
                <method name="GET" id="getResourceMetadataItem_{$uniqueName}">
                    <wadl:doc xml:lang="EN" xmlns="http://docbook.org/ns/docbook"
                        title="Show resource metadata item details for {$uniqueName}"/>
                    <response status="200 203">
                        <representation mediaType="application/json"/>
                        <representation mediaType="application/xml" element="meta:meta"/>
                    </response>
                </method>
            </xsl:if>
            <method name="PUT" id="setResourceMetadataItem_{$uniqueName}" rax:roles="{$raxRoles}">
                <wadl:doc xml:lang="EN" xmlns="http://docbook.org/ns/docbook"
                    title="Create or update resource metadata item for {$uniqueName}"/>
                <request>
                    <representation mediaType="application/json"/>
                    <representation mediaType="application/xml" element="meta:meta"/>
                </request>
                <response status="200">
                    <representation mediaType="application/json"/>
                    <representation mediaType="application/xml" element="meta:meta"/>
                </response>
            </method>
            <method name="DELETE" id="deleteResourceMetadataItem_{$uniqueName}" rax:roles="{$raxRoles}">
                <wadl:doc xmlns="http://docbook.org/ns/docbook" xml:lang="EN"
                    title="Delete resource metadata item for {$uniqueName}"/>
                <response status="204"/>
            </method>
        </resource>
    </xsl:template>
    <xsl:template match="rax:metaRole" mode="metaCopy">
        <xsl:copy>
            <xsl:if test="not(@pattern)">
                <xsl:attribute name="pattern" select="concat(@name,':')"/>
            </xsl:if>
            <xsl:apply-templates mode="copy" select="@* | node()"/>
        </xsl:copy>
    </xsl:template>
    <xsl:function name="rax:metaResourceTypeName" as="xs:string">
        <xsl:param name="metaDataName" as="xs:string"/>
        <xsl:value-of select="concat($metaDataName,'_RAX_META_TYPE')"/>
    </xsl:function>
    <xsl:function name="rax:metaResourceXSDType" as="xs:string">
        <xsl:param name="metaDataName" as="xs:string"/>
        <xsl:param name="roleName" as="xs:string"/>
        <xsl:value-of select="concat(rax:metaResourceTypeName($metaDataName),replace($roleName,':','_'))"/>
    </xsl:function>
    <xsl:function name="rax:toRegExEscaped" as="xs:string">
        <xsl:param name="in" as="xs:string"/>
        <xsl:value-of select="replace($in,'\.|\\|\(|\)|\{|\}|\[|\]|\?|\+|\-|\^|\$|#|\*|\|','\\$0')"/>
    </xsl:function>
    <xsl:function name="rax:encodeRole" as="xs:string">
        <xsl:param name="in" as="xs:string"/>
        <xsl:value-of select="string-join(for $i in string-to-codepoints($in) return rax:int-to-hex($i),'')"/>
    </xsl:function>
    <xsl:function name="rax:int-to-hex" as="xs:string">
        <xsl:param name="in" as="xs:integer"/>
        <xsl:sequence
            select="if ($in eq 0)
            then '0'
            else
            concat(if ($in gt 16)
            then rax:int-to-hex($in idiv 16)
            else '',
            substring('0123456789ABCDEF',
            ($in mod 16) + 1, 1))"/>
    </xsl:function>
</xsl:stylesheet>