<?xml version="1.0" encoding="UTF-8"?>
<!--
   raxRolesCommon.xsl

   This transfrom simply contains common functions, templates, and
   variables used by rax:roles templates.

   Copyright 2018 Rackspace US, Inc.

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
<xsl:transform  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:check="http://www.rackspace.com/repose/wadl/checker"
                xmlns:util="http://www.rackspace.com/repose/wadl/checker/util"
                xmlns:map="http://www.w3.org/2005/xpath-functions/map"
                xmlns:err="http://www.w3.org/2005/xqt-errors"
                exclude-result-prefixes="xs"
                version="3.0">

    <!--
        Although not explicitly used here, prunning is needed by
        transforms that depend on this transform, so convienent to
        import here.
    -->
    <xsl:import href="util/pruneSteps.xsl"/>

    <!-- Roles header name -->
    <xsl:variable name="ROLES_HEADER" as="xs:string" select="'X-ROLES'"/>

    <!-- Roles type -->
    <xsl:variable name="ROLES_TYPE" as="xs:string" select="'HEADER_ANY'"/>

    <!-- Regex for matching a tenant role -->
    <xsl:variable name="tenantRoleRegex" as="xs:string"
                  select="'^(.+)/\\\{(.+)\\\}$'"/>

    <!-- Tenant Parameter Types -->
    <xsl:variable name="param-types-single" as="xs:string*" select="(
                                                                     'XPATH', 'JSON_XPATH', 'URL', 'URLXSD',
                                                                     'HEADER_SINGLE','HEADERXSD_SINGLE'
                                                                     )"/>

    <xsl:variable name="param-types-multi" as="xs:string*" select="(
                                                                    'HEADER', 'HEADERXSD', 'HEADER_ANY',
                                                                    'HEADERXSD_ANY', 'HEADER_ALL',
                                                                    'CAPTURE_HEADER'
                                                                   )"/>
    <xsl:variable name="param-types" as="xs:string*" select="(
                                                               $param-types-single,
                                                               $param-types-multi
                                                               )"/>

    <!--
        Given a sequence of next steps and a map with steps to cull as
        keys, returns a new sequence with steps culled out if they are
        contained in the map.
    -->
    <xsl:function name="check:cullFromNext" as="xs:string*">
        <xsl:param name="next" as="xs:string*"/>
        <xsl:param name="cullMap" as="map(xs:string, xs:string*)"/>
        <xsl:param name="checker" as="node()"/>
        <xsl:choose>
            <xsl:when test="some $n in $next satisfies map:contains($cullMap,$n)">
                <xsl:sequence select="
                    (for $n in $next return if (map:contains($cullMap, $n)) then
                    check:next(key('checker-by-id', $n, $checker)) else $n) =>
                    distinct-values() =>
                    check:cullFromNext($cullMap, $checker)
                    "/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:sequence select="$next"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:function>

    <!--
        This version of cullFromNext removes existing URL_FAIL,
        METHOD_FAIL and CONTENT_FAIL error states and replaces these
        with new IDs, which will be the combination of the current
        steps id (currentId) with '_u_', '_m_', and '_c_' added.

        You can use check:generateErrorStatesFromNext to generate the
        appropriate error states with those IDs.
    -->
    <xsl:function name="check:cullFromNext" as="xs:string*">
        <xsl:param name="next" as="xs:string*"/>
        <xsl:param name="cullMap" as="map(xs:string, xs:string*)"/>
        <xsl:param name="currentId" as="xs:string"/>
        <xsl:param name="checker" as="node()"/>

        <xsl:variable name="cullNext" as="xs:string*"
            select="check:cullFromNext($next, $cullMap, $checker)"/>

        <xsl:variable name="currentStep" as="node()" select="check:stepsByIds($checker, $currentId)"/>
        <xsl:variable name="nextSteps" as="node()*" select="check:stepsByIds($checker, $cullNext)"/>
        <xsl:variable name="NFIDs" as="xs:string*" select="$nextSteps[not(@type=('CONTENT_FAIL','METHOD_FAIL','URL_FAIL'))]/@id"/>

        <xsl:sequence select="(
                              $NFIDs,
                              if ($nextSteps[@type=$cont-error-types]) then check:contentFailId($currentId) else (),
                              if ($nextSteps[@type='METHOD'] or ($currentStep/@type=('URL','URLXSD'))) then check:methodFailId($currentId) else (),
                              if ($nextSteps[@type=('URL', 'URLXSD')] or ($currentStep/@type=('URL','URLXSD'))) then  check:urlFailId($currentId) else ()
                              )"/>
    </xsl:function>


    <!--
        Generates appropriate error states after next steps have been
        culled.  The function assumes that next is the result of
        cullFromNext(next, cullMap, currentId) and that currentId is the
        same baseID passed to that function.
    -->
    <xsl:function name="check:generateErrorStatesFromNext" as="node()*">
        <xsl:param name="next" as="xs:string*"/>
        <xsl:param name="currentId" as="xs:string"/>
        <xsl:param name="checker" as="node()"/>

        <xsl:variable name="nextSteps" as="node()*" select="check:stepsByIds($checker, $next)"/>
        <xsl:variable name="cfid" as="xs:string" select="check:contentFailId($currentId)"/>
        <xsl:variable name="mfid" as="xs:string" select="check:methodFailId($currentId)"/>
        <xsl:variable name="ufid" as="xs:string" select="check:urlFailId($currentId)"/>

        <!-- Generate Content Fail -->
        <xsl:if test="$cfid = $next">
            <check:step type="CONTENT_FAIL" id="{$cfid}" />
        </xsl:if>

        <!-- Generate Method Fail -->
        <xsl:if test="$mfid = $next">
            <xsl:variable name="nextMethodMatch" as="xs:string*" select="sort($nextSteps[@type='METHOD']/@match)"/>
            <check:step type="METHOD_FAIL" id="{$mfid}">
                <xsl:if test="exists($nextMethodMatch)">
                    <xsl:attribute name="notMatch">
                        <xsl:value-of select="$nextMethodMatch" separator="|"/>
                    </xsl:attribute>
                </xsl:if>
            </check:step>
        </xsl:if>

        <!-- Generate URL Fail -->
        <xsl:if test="$ufid = $next">
            <xsl:variable name="nextURLMatch" as="xs:string*" select="sort($nextSteps[@type='URL']/@match)"/>
            <xsl:variable name="nextURLXSDMatch" as="xs:string*" select="sort($nextSteps[@type='URLXSD']/@match)"/>
            <check:step type="URL_FAIL" id="{$ufid}">
                <xsl:if test="exists($nextURLMatch)">
                    <xsl:attribute name="notMatch">
                        <xsl:value-of select="$nextURLMatch" separator="|"/>
                    </xsl:attribute>
                </xsl:if>
                <xsl:if test="exists($nextURLXSDMatch)">
                    <xsl:attribute name="notTypes">
                        <xsl:value-of select="$nextURLXSDMatch" separator=" "/>
                    </xsl:attribute>
                </xsl:if>
            </check:step>
        </xsl:if>
    </xsl:function>

    <xsl:function name="check:contentFailId" as="xs:string">
        <xsl:param name="baseId" as="xs:string"/>
        <xsl:value-of select="$baseId || '_c_'"/>
    </xsl:function>

    <xsl:function name="check:methodFailId" as="xs:string">
        <xsl:param name="baseId" as="xs:string"/>
        <xsl:value-of select="$baseId || '_m_'"/>
    </xsl:function>

    <xsl:function name="check:urlFailId" as="xs:string">
        <xsl:param name="baseId" as="xs:string"/>
        <xsl:value-of select="$baseId || '_u_'"/>
    </xsl:function>

    <!--
        Converts a match regular expression for a role into a regular
        string.
    -->
    <xsl:function name="check:roleFromMatch" as="xs:string">
        <xsl:param name="match" as="xs:string"/>
        <xsl:value-of select="replace(replace($match,'\\(.)','$1'),' ','&#xA0;')"/>
    </xsl:function>

    <!--
        Inverses a map
    -->
    <xsl:function name="check:inverseMap" as="map(xs:string, xs:string*)">
        <xsl:param name="inMap" as="map(xs:string, xs:string*)"/>
        <xsl:sequence select="map:merge(
                              for $k in map:keys($inMap) return
                              for $t in $inMap($k) return
                                map{ $t : $k }
                              ,map{'duplicates' : 'combine'})"/>
    </xsl:function>

    <!--
        Copies a step, but replaces an the id and next attributes
    -->
    <xsl:function name="check:copyStep" as="node()">
        <xsl:param name="step" as="node()" />
        <xsl:param name="newID" as="xs:string"/>
        <xsl:param name="newNext" as="xs:string*"/>
        <xsl:param name="setIsTenant" as="xs:boolean"/>
        <xsl:param name="matchingRoles" as="xs:string*"/>

        <xsl:variable name="excludeAttribs" as="xs:string*"
                      select="('next', 'id', 'isTenant',
                              if (exists($matchingRoles)) then 'matchingRoles' else ())"/>

        <check:step>
            <xsl:attribute name="id" select="$newID"/>
            <xsl:attribute name="next" select="string-join($newNext,' ')"/>
            <xsl:if test="$setIsTenant or exists($matchingRoles)">
                <xsl:attribute name="isTenant" select="'true'"/>
            </xsl:if>
            <xsl:if test="exists($matchingRoles)">
                <xsl:attribute name="matchingRoles">
                    <xsl:value-of select="$matchingRoles" separator=" "/>
                </xsl:attribute>
            </xsl:if>
            <xsl:apply-templates select="$step/@*[not(name() = $excludeAttribs)] | $step/node()" mode="copy"/>
        </check:step>
    </xsl:function>

    <!-- Copy Template -->
    <xsl:template match="node() | @*" mode="copy processTemplateRoles">
        <xsl:copy>
            <xsl:apply-templates select="@* | node()" mode="#current"/>
        </xsl:copy>
    </xsl:template>


</xsl:transform>
