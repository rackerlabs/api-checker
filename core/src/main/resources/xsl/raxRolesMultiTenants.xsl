<?xml version="1.0" encoding="UTF-8"?>
<!--
   raxRolesMultiTenants.xsl

   This transform is responsible for making sure that tenant checks
   that handle multiple tenant values function correctly. It performs
   the following tasks if a role tenant check is used (ex
   admin/{tenant}) on a parameter type that can accept multiple values
   ($param-types-multi).

   1. If a multi-value param type is performed before the method
   check, it moves the check until after the method check. This
   ensures that there is a separate check per method.

   2. Multi-value param types contain an matchingRoles attribute which
   list the possible roles to match, this transforms fills these with
   appropriate values.

   If role tenant checks are not used, or they don't rely on
   multi-value params, then the transform just passes the checker
   through unchanged.

   It is a requirement that the raxRolesTenants transform occurs
   before this transform is executed.

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

    <xsl:import href="raxRolesCommon.xsl"/>

    <xsl:output method="xml"/>

    <!-- The input checker -->
    <xsl:variable name="checker" as="node()" select="/"/>

    <!--
        Possible Checker Changes:

        SET  : We must set matching roles attributes on the param, but
               leave role check in place.
        MOVE : We must move the param to occur after a method. Move
               always implies SET, for the sake of simplicity.
    -->
    <xsl:variable name="SET" as="xs:string" select="'set'"/>
    <xsl:variable name="MOVE" as="xs:string" select="'move'"/>


    <xsl:template match="/">
        <xsl:choose>
            <!--
                We check to make sure that this transform is even
                applicable. If it is we processMultiTemplateChecks.
            -->
            <xsl:when test="check:containsMultiTenantRoleCheck()">
                <xsl:call-template name="check:processMultiTemplateChecks"/>
            </xsl:when>
            <!--
                If the transform is not applicable then we simply pass
                the checker through unchanged.
            -->
            <xsl:otherwise>
                <xsl:copy-of select="."/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <!--
        Scans the document for appropriate multiTemplate changes and
        actives if changes need to be made.
    -->
    <xsl:template name="check:processMultiTemplateChecks">
        <!--
            Look for changes related to multi-tenant checks.
        -->
        <xsl:variable name="multiTenantChanges" as="map(xs:string, item())"
                      select="check:findMultiTenantChanges()"/>

        <xsl:variable name="tenantMethodMap" as="map(xs:string, xs:string*)"
                      select="$multiTenantChanges($MOVE)"/>

        <xsl:variable name="tenantRoleMap" as="map(xs:string, xs:string*)"
                      select="$multiTenantChanges($SET)"/>
        <xsl:choose>
            <!--
                In this case we have tenant map changes (MOVE) or
                tenant role changes (SET), so we activete the
                processTemplateRoles templates to perform the changes.
            -->
            <xsl:when test="(map:size($tenantMethodMap) &gt; 0) or (map:size($tenantRoleMap) &gt; 0)">
                <xsl:variable name="multiTenantChecker" as="node()">
                    <xsl:copy>
                        <xsl:apply-templates select="$checker" mode="processTemplateRoles">
                            <xsl:with-param name="tenantMethodMap" select="$tenantMethodMap" tunnel="yes"/>
                            <xsl:with-param name="methodTenantMap" select="check:inverseMap($tenantMethodMap)" tunnel="yes"/>
                            <xsl:with-param name="tenantRoleMap" select="$tenantRoleMap" tunnel="yes"/>
                        </xsl:apply-templates>
                    </xsl:copy>
                </xsl:variable>
                <xsl:sequence select="util:pruneSteps($multiTenantChecker)"/>
            </xsl:when>
            <!--
                No relevant actions were nessesary, so we copy through
                the checker unchanged.
            -->
            <xsl:otherwise>
                <xsl:copy-of select="."/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template match="check:step[@next]" mode="processTemplateRoles" priority="2">
        <xsl:param name="tenantMethodMap" as="map(xs:string, xs:string*)" tunnel="yes"/>
        <xsl:param name="methodTenantMap" as="map(xs:string, xs:string*)" tunnel="yes"/>
        <xsl:param name="tenantRoleMap"   as="map(xs:string, xs:string*)" tunnel="yes"/>

        <xsl:variable name="this" as="node()" select="."/>
        <xsl:variable name="id" as="xs:string" select="@id"/>
        <xsl:variable name="next" as="xs:string*" select="check:next(.)"/>
        <xsl:variable name="movedTenant" as="xs:boolean" select="some $n in $next satisfies map:contains($tenantMethodMap, $n)"/>

        <!--
            Cull out tenant params that come before METHOD checks.
        -->
        <xsl:variable name="newNexts" as="xs:string*">
            <xsl:choose>
                <xsl:when test="$movedTenant">
                    <xsl:sequence select="check:cullFromNext($next, $tenantMethodMap, $id, $checker)"/>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:sequence select="$next"/>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        <xsl:sequence select="if ($movedTenant) then check:generateErrorStatesFromNext($newNexts, $id, $checker) else ()"/>

        <xsl:choose>
            <!--
                If we are at an appropriate methed, rewrite it so that
                the tenant check occurs right after the method check.
            -->
            <xsl:when test="map:contains($methodTenantMap,$id)">
                <xsl:variable name="cfid" as="xs:string" select="$id || 'CFID'"/>
                <xsl:variable name="newIds" as="xs:string*"
                              select="for $oid in $methodTenantMap($id) return $id || $oid"/>
                <xsl:sequence select="check:copyStep($this, $id, ($newIds, $cfid), false(), ())"/>
                <xsl:for-each select="$methodTenantMap($id)">
                    <xsl:variable name="oldId" as="xs:string" select="."/>
                    <xsl:variable name="newId" as="xs:string" select="$id || $oldId"/>
                    <xsl:variable name="oldTenant" as="node()" select="key('checker-by-id', $oldId, $checker)"/>
                    <xsl:sequence select="check:copyStep($oldTenant, $newId, $newNexts, false(), check:roleCheckIdsToMatchStrings($tenantRoleMap($id)))"/>
                </xsl:for-each>
                <check:step type="CONTENT_FAIL" id="{$cfid}"/>
            </xsl:when>
            <!--
                If we need to add matchingRoles attribute to a tenant
                check, then rewrite the tenant check so that it
                includes the appropriate matching roles, here...
            -->
            <xsl:when test="map:contains($tenantRoleMap, $id)">
                <xsl:sequence select="check:copyStep($this, $id, $newNexts, false(), check:roleCheckIdsToMatchStrings($tenantRoleMap($id)))"/>
            </xsl:when>
            <!--
                Otherwise simply copy the step over, adjusting next as
                nessessary.
            -->
            <xsl:otherwise>
                <xsl:sequence select="check:copyStep($this, $id, $newNexts,if (exists($this/@isTenant)) then xs:boolean($this/@isTenant) else false(), ())"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <!--
        Given a list of IDs for role checks, return the appropriate
        matching role names.
    -->
    <xsl:function name="check:roleCheckIdsToMatchStrings" as="xs:string*">
        <xsl:param name="roleCheckIds" as="xs:string*"/>
        <xsl:sequence select="distinct-values(for $s in check:stepsByIds($checker, $roleCheckIds) return check:roleFromMatch($s/@match))"/>
    </xsl:function>

    <!--
        Scan the current checker and find changes for this template to
        make.  There are two possible changes.

        1. We have multi-tenant params before method params and we need to
        move them after a method. (MOVE)

        2. We have multi-tenat params (after a method) and we need to
        set an appropriate list of values for the matchingRoles
        attribute.

        The return value is a map in this format:

        {
        $MOVE : {
              'paramID'  : ['methodID','methodID2'],
              'paramID2' : ['methodID3','methodID4']
             },
        $SET : {
               'paramID3' : ['raxRoleID1', 'raxRoleID2'],
               'paramID4' : ['raxRoleID3'],
               'methedID' : ['raxRoleID4']
             }
       }

       $MOVE : contains a map of param IDs with the list of method IDs
       that are shared with this param.

       $SET : contains a map of param, and methodIDs with the list of
       apporpriate matching roles for these.  Note that you see both
       param and method ids to account for the fact that some methods
       need to move, before setting matching roles.
    -->
    <xsl:function name="check:findMultiTenantChanges" as="map(xs:string, item())">
        <xsl:sequence select="check:findMultiTenantChanges(
                              $checker/check:checker/check:step[@type=$param-types-multi and xs:boolean(@isTenant)],
                              map{}, map{})"/>
    </xsl:function>

    <xsl:function name="check:findMultiTenantChanges" as="map(xs:string, item())">
        <xsl:param name="tenantChecks" as="node()*"/>
        <xsl:param name="moveMap" as="map(xs:string, xs:string*)"/>
        <xsl:param name="setMap" as="map(xs:string, xs:string*)"/>
        <xsl:choose>
            <xsl:when test="empty($tenantChecks)">
                <xsl:sequence select="map {
                                      $SET : $setMap,
                                      $MOVE : $moveMap
                                      }"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:variable name="currentStep" as="node()"  select="$tenantChecks[1]"/>
                <xsl:variable name="methodSteps" as="node()*" select="check:findMethodSteps($currentStep)"/>
                <xsl:variable name="methodMoveItems" as="map(xs:string, xs:string*)*"
                              select="for $m in $methodSteps return map { string($currentStep/@id) : string($m/@id) }"/>
                <xsl:variable name="methodSetItems" as="map(xs:string, xs:string*)*"
                              select="for $m in $methodSteps return
                                      for $r in check:findRoleSteps($m) return map{ string($m/@id) : string($r/@id) }
                                      "/>
                <xsl:variable name="roleSetItems" as="map(xs:string, xs:string*)*"
                              select="
                                      if (empty($methodMoveItems)) then
                                        for $r in check:findRoleSteps($currentStep) return map{ string($currentStep/@id) : string($r/@id) }
                                      else ()
                                      "/>
                <xsl:sequence select="check:findMultiTenantChanges($tenantChecks[position() != 1],
                                      map:merge(($moveMap, $methodMoveItems), map{'duplicates' : 'combine'}),
                                      map:merge(($setMap, $methodSetItems, $roleSetItems), map{'duplicates' : 'combine'}))"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:function>

    <!--
        Given a step returns all Method steps that are children of
        this step.
    -->
    <xsl:function name="check:findMethodSteps" as="node()*">
        <xsl:param name="step" as="node()"/>
        <xsl:choose>
            <xsl:when test="empty($step/@next)">
                <!--
                    Made it to accept or a non-connected step, return
                    nothing.
                -->
                <xsl:sequence select="()"/>
            </xsl:when>
            <xsl:when test="$step/@type='METHOD'">
                <!--
                    Found a method step, return it!
                -->
                <xsl:sequence select="$step"/>
            </xsl:when>
            <xsl:otherwise>
                <!--
                    Keep looking!!
                -->
                <xsl:sequence select="for $s in check:stepsByIds($checker, check:next($step)) return check:findMethodSteps($s)"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:function>

    <!--
        Given a step returns all ROLE Checkes that are children of
        this step.
    -->
    <xsl:function name="check:findRoleSteps" as="node()*">
        <xsl:param name="step" as="node()"/>
        <xsl:choose>
            <xsl:when test="empty($step/@next)">
                <!--
                    Made it to accept or non-connected step, return
                    nothing.
                -->
                <xsl:sequence select="()"/>
            </xsl:when>
            <xsl:when test="($step/@name=$ROLES_HEADER) and ($step/@type=$ROLES_TYPE)">
                <!--
                    Found a role header!
                -->
                <xsl:sequence select="$step"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:sequence select="for $s in check:stepsByIds($checker, check:next($step)) return check:findRoleSteps($s)"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:function>

    <!--
        Returns true if the checker contains at least one tenant check
        of type $param-types-multi
    -->
    <xsl:function name="check:containsMultiTenantRoleCheck"
                  as="xs:boolean">
        <xsl:sequence
            select="exists($checker/check:checker/check:step[@type=$param-types-multi
                    and xs:boolean(@isTenant)][1])"/>
    </xsl:function>

</xsl:transform>
