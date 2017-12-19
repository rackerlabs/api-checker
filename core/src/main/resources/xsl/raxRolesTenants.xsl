<?xml version="1.0" encoding="UTF-8"?>
<!--
   raxRolesTenants.xsl

   This transform is responsible for rewriting a checker to make sure
   that role tenant checks work properly.  It performs the following
   tasks if a role tenant check is used (ex admin/{tenant}).

   1. It ensures that maskRaxRoles is not enabled, as role tenant
   checks are currently not supported with that option. If maskRaxRoles is
   enabled this transform fails with an error message.

   2. It ensures that the {tenant} exists, if not this transform fails
   with an appropriate error message.

   3. It ensures that the {tenant} has isTenant set to true, if not it
   rewrites the tenant so it is set to true.

   4. In ensures that rax roles checks occur *after* the {tenant}
   param has been processed. If not, it moves role checks to
   appropriate location.

   If role tenant checks are not used, this transform just passes the
   checker through unchanged.

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

    <!-- config -->
    <xsl:param name="configMetadata" as="node()">
        <check:params>
          <check:meta>
            <check:config option="maskRaxRoles403" value="false"/>
          </check:meta>
        </check:params>
    </xsl:param>

    <!-- Are we masking roles? -->
    <xsl:variable name="maskRaxRoles" as="xs:boolean"
                  select="xs:boolean(check:optionValue($configMetadata, 'maskRaxRoles403'))"/>

    <!-- The input checker -->
    <xsl:variable name="checker" as="node()" select="/"/>

    <!-- Errors -->
    <xsl:variable name="MISSING_TENANT" as="xs:QName" static="yes" select="xs:QName('check:MissingTenant')"/>
    <xsl:variable name="MISSING_REL_ROLE" as="xs:QName" static="yes" select="xs:QName('check:MissingRelRole')"/>
    <xsl:variable name="BAD_REL_ROLE" as="xs:QName" static="yes" select="xs:QName('check:BadRelRole')"/>

    <!--
        SET isTenantID : A magic ID that shouldn't conflict with
        regular step IDs, The purpose of which is to avoiding making
        multiple passes at the checker to look for $MOVEs and $SETs.
    -->
    <xsl:variable name="SET_IS_TENANT_ID" as="xs:string" select="'0000IS_TENANT9999'"/>

    <!--
        Possible Checker Changes:

        SET  : We must set isTenant=true on the param, but leave role check in place.
        MOVE : We must move the role check to occur after a param is set.
               Move always implies SET, for the sake of simplicity.
    -->
    <xsl:variable name="SET" as="xs:string" select="'set'"/>
    <xsl:variable name="MOVE" as="xs:string" select="'move'"/>

    <xsl:template match="/">
        <xsl:choose>
            <!--
                First thing we want to do is check to make sure this
                template is even applicable.  We look for RAX-ROLES
                checks that reference a tenant param.

                If we find the right kind of role we check to make
                sure maskRaxRoles isn't set then we
                processTemplateRoles.
            -->
            <xsl:when test="check:containsTenantRoleCheck()">
                <xsl:call-template name="check:checkMaskRaxRoles"/>
                <xsl:call-template name="check:processTemplateRoles"/>
            </xsl:when>
            <!--
                If this transform is not applicable, we simply pass
                the checker through unchanged.
            -->
            <xsl:otherwise>
                <xsl:copy-of select="."/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <!--
        This is a stop-gap to enable delivery of the feature before
        MaskRaxRoles support is implemented.  The template simply
        checks to see if the mask rax roles feature is enabled and if
        it is, it fails with a not implement message.
    -->
    <xsl:template name="check:checkMaskRaxRoles">
        <xsl:if test="$maskRaxRoles">
            <xsl:message terminate="yes">[ERROR] Support for per-tenant roles is not implemented when the maskRaxRoles403 feature is enabled!</xsl:message>
        </xsl:if>
    </xsl:template>

    <!--
        Scans the checker document for instances where capturing the
        tenant occurs *after* the role check.
    -->
    <xsl:template name="check:processTemplateRoles">
        <!--
            Look for changes related to tenant roles
        -->
        <xsl:variable name="tenantChanges" as="map(xs:string, item())"
                      select="check:findTenantChanges()"/>
        <!--
            A map from X-ROLES header step id to the IDs of locations
            that capture the tenant.
        -->
        <xsl:variable name="roleTenantMap" as="map(xs:string, xs:string*)"
                      select="$tenantChanges($MOVE)"/>

        <xsl:variable name="setTenantMap" as="map(xs:string, xs:boolean)"
                      select="$tenantChanges($SET)"/>

        <xsl:choose>
            <xsl:when test="(map:size($roleTenantMap) &gt; 0) or (map:size($setTenantMap) &gt; 0)">
                <xsl:variable name="tenantRoleMap" as="map(xs:string, xs:string*)"
                              select="check:inverseMap($roleTenantMap)"/>
                <xsl:variable name="roleRelRoleMap" as="map(xs:string, xs:string)"
                              select="check:createRelRoleMap($roleTenantMap)"/>
                <xsl:variable name="relRoleRoleMap" as="map(xs:string, xs:string*)"
                              select="check:inverseMap($roleRelRoleMap)"/>
                <xsl:variable name="movedRoleChecker" as="node()">
                    <xsl:copy>
                        <xsl:apply-templates select="$checker" mode="processTemplateRoles">
                            <xsl:with-param name="roleTenantMap" select="$roleTenantMap" tunnel="yes"/>
                            <xsl:with-param name="tenantRoleMap" select="$tenantRoleMap" tunnel="yes"/>
                            <xsl:with-param name="roleRelRoleMap" select="$roleRelRoleMap" tunnel="yes"/>
                            <xsl:with-param name="relRoleRoleMap" select="$relRoleRoleMap" tunnel="yes"/>
                            <xsl:with-param name="setTenantMap" select="$setTenantMap" tunnel="yes"/>
                        </xsl:apply-templates>
                    </xsl:copy>
                </xsl:variable>
                <xsl:sequence select="util:pruneSteps($movedRoleChecker)"/>
            </xsl:when>
            <!--
                In this case we found no reason to make changes to the
                checker so we just pass the checker through unchanged.
            -->
            <xsl:otherwise>
                <xsl:copy-of select="."/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>


    <xsl:template match="check:step[@next]" mode="processTemplateRoles" priority="2">
        <xsl:param name="roleTenantMap" as="map(xs:string, xs:string*)" tunnel="yes"/>
        <xsl:param name="tenantRoleMap" as="map(xs:string, xs:string*)" tunnel="yes"/>
        <xsl:param name="roleRelRoleMap" as="map(xs:string, xs:string)" tunnel="yes"/>
        <xsl:param name="relRoleRoleMap" as="map(xs:string, xs:string*)" tunnel="yes"/>
        <xsl:param name="setTenantMap" as="map(xs:string, xs:boolean)" tunnel="yes"/>

        <xsl:variable name="id" as="xs:string" select="@id"/>
        <xsl:variable name="next" as="xs:string*" select="check:next(.)"/>
        <xsl:variable name="cullFromNext" as="xs:boolean" select="some $n in $next satisfies
                                                                  (map:contains($roleTenantMap, $n) or
                                                                  map:contains($relRoleRoleMap, $n))"/>


        <!--
            Cull out the X-ROLES checks and X-Relevent-Role captures
            from next, if we have to.
        -->
        <xsl:variable name="newNexts" as="xs:string*">
            <xsl:variable name="next" as="xs:string*" select="check:next(.)"/>
            <xsl:choose>
                <xsl:when test="$cullFromNext">
                    <xsl:sequence select="check:cullFromNext($next,
                                          map:merge(($roleTenantMap, $relRoleRoleMap),
                                          map { 'duplicates' :
                                          'use-any'}), $id, $checker)"/>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:sequence select="$next"/>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>

        <!-- Generate errors that resulted from the culling -->
        <xsl:sequence select="if ($cullFromNext) then check:generateErrorStatesFromNext($newNexts, $id, $checker) else ()"/>

        <!--
            If we are a tenant step, we must create new role checks, with their appropriate
            X-RELEVENT-ROLE sets.

            If not simply copy over adjusting next and isTenant as nessesary.
        -->
        <xsl:try>
            <xsl:choose>
                <xsl:when test="map:contains($tenantRoleMap, $id)">
                    <xsl:variable name="xroles" as="xs:string*"
                                  select="distinct-values($tenantRoleMap($id))"/>
                    <xsl:variable name="cfid" as="xs:string" select="$id || 'CFID'"/>
                    <xsl:variable name="xroleNexts" as="xs:string*"
                                  select="(for $r in $xroles return concat($id,$r), $cfid)"/>
                    <xsl:variable name="relRoleSteps" as="node()"
                                  select="check:stepsByIds($checker, distinct-values(for $r in $xroles return $roleRelRoleMap($r)))"/>
                    <!--
                        We're making an assumption here that there is a single X-RELEVANT-ROLE step
                        that captures the logic for all of these related role steps, because
                        they are all siblings of each other.

                        I can't think of a reason why this wouldn't be true. Just to be on the extream
                        safe side, we throw an error here if my assumption is off.

                        How would we fix if this were not the case? Well, we'd need to create a new
                        relevant role step here that covered the logic
                        for this set of roles.
                    -->
                    <xsl:sequence select="if (count($relRoleSteps) != 1) then
                                          error($BAD_REL_ROLE,'No REL ROLE to cover this case',$xroles) else ()
                                          "/>
                    <xsl:sequence select="check:copyStep(., xs:string(@id), $xroleNexts, true(), ())"/>
                    <xsl:for-each select="check:stepsByIds($checker, $xroles)">
                        <xsl:sequence select="check:copyStep(., concat($id,xs:string(@id)), concat($id,xs:string($relRoleSteps[1]/@id)), false(), ())"/>
                    </xsl:for-each>
                    <xsl:sequence select="check:copyStep($relRoleSteps[1], concat($id,xs:string($relRoleSteps[1]/@id)), $newNexts, false(), ())"/>
                    <check:step type="CONTENT_FAIL" id="{$cfid}"/>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:variable name="doTenantMap" as="xs:boolean"
                                  select="(map:contains($setTenantMap, $id) or
                                          (if (exists(./@isTenant)) then xs:boolean(./@isTenant) else false()))"/>
                    <xsl:sequence select="check:copyStep(.,xs:string(@id), $newNexts, $doTenantMap, ())"/>
                </xsl:otherwise>
            </xsl:choose>
            <xsl:catch _errors="{$BAD_REL_ROLE}">
                <!-- This Should Never Happen ® -->
                <xsl:variable name="roles" as="node()*" select="check:stepsByIds($checker, xs:string*($err:value))"/>
                <xsl:message terminate="yes">[ERROR] That's odd, I made a bad assumption, I can't find a X-RELEVENT-ROLE step for these set of roles:
                <xsl:for-each select="$roles">
                    <xsl:variable name="role" as="node()" select="."/>
                    <xsl:text expand-text="yes">{$role/@match} ({$role/@id}) </xsl:text>
                </xsl:for-each>
                </xsl:message>
            </xsl:catch>
        </xsl:try>
    </xsl:template>

    <!--
        Given the tenat role map, create a map from X-ROLE check to
        the releveant role capture header, that must also be moved.
    -->
    <xsl:function name="check:createRelRoleMap" as="map(xs:string, xs:string)">
        <xsl:param name="roleTenantMap" as="map(xs:string, xs:string*)"/>
        <xsl:try>
            <xsl:sequence select="map:merge(
                                  for $k in map:keys($roleTenantMap) return
                                  map { $k : for $rr in check:findRelRole(key('checker-by-id', $k, $checker), $k)
                                             return if (exists($rr)) then $rr else error($MISSING_REL_ROLE, '', $k) },
                                  map { 'duplicates' : 'use-any'})
                                  "/>
            <xsl:catch _errors="{$MISSING_REL_ROLE}">
                <!-- This Should Never Happen ® -->
                <xsl:variable name="roleStep" as="node()" select="key('checker-by-id', xs:string($err:value), $checker)"/>
                <xsl:message terminate="yes" expand-text="yes">[ERROR] That's odd, I'm in a bad state: role {$roleStep/@match} with id {$err:value} has no X-RELEVANT-ROLE set!  Please report this error!</xsl:message>
            </xsl:catch>
        </xsl:try>
    </xsl:function>


    <!--
        Given a map from role check id to one or more decendant
        params, extends the map to include sibling role checks as
        siblings must stay together to properly secure the call.
    -->
    <xsl:function name="check:addSiblingMapRoles" as="map(xs:string, xs:string*)">
        <xsl:param name="initMap" as="map(xs:string, xs:string*)"/>
        <xsl:sequence select="map:merge((
                              $initMap,
                              for $k in map:keys($initMap) return
                              for $s in check:stepsByIds($checker, check:siblings($checker, key('checker-by-id', $k, $checker))) return
                                if ($s/@name=$ROLES_HEADER and $s/@type=$ROLES_TYPE) then map{xs:string($s/@id) : $initMap($k)} else ()
                              ), map{'duplicates' : 'combine'})"/>
    </xsl:function>

    <!--
        Finds tetant changes that must be made to the checker. There
        are two passible changes that we can process.

        1.  We have a valid tenant param, but isTenant attribute is
        not set to true.  In this case, we simply want to set the
        value to true.

        2. We have a tenant param that is set *after* the rax role
        header check is performed (rax role check is performed before
        the tenat param is even set.) In this case we want to set
        things up so that we move the rax role check to occur after
        the tenant param.

        The return value is a map, with this format:

        {
        $SET : {
             'paramID' : true,
             'paramID2' : true,
             },
        $MOVE : {
             'raxroleId' : 'paramId3',
             'raxroleId2' : 'paramId4'
         }
       }

       $SET  : contains a *set* of paramIDs where we simply must set
       isTenat to true.

       $MOVE : contains a map between a role check and and
       tenanID. In this case we want to move the role check to to move
       after param check.
    -->
    <xsl:function name="check:findTenantChanges" as="map(xs:string, item())">
        <xsl:variable name="results" as="map(xs:string, xs:string*)"
                      select="map:merge(check:findTenantChanges($checker), map{'duplicates':'combine'})"/>
        <xsl:variable name="set" as="map(xs:string, xs:boolean)"
            select="map:merge((for $r in $results($SET_IS_TENANT_ID) return map{$r : true()}),
                               map{'duplicates' : 'use-any'})"/>
        <xsl:variable name="move" as="map(xs:string, xs:string*)"
                      select="map:remove($results,$SET_IS_TENANT_ID) => check:addSiblingMapRoles()"/>
        <xsl:sequence select="map {
                              $SET : $set,
                              $MOVE : $move
                              }"/>
    </xsl:function>
    <xsl:function name="check:findTenantChanges"
                  as="map(xs:string, xs:string*)*">
        <xsl:param name="checker" as="node()"/>
        <xsl:apply-templates mode="findTenantChanges" select="$checker"/>
    </xsl:function>

    <xsl:template match="check:step[@name=$ROLES_HEADER and @type=$ROLES_TYPE and
                         matches(@match,$tenantRoleRegex)]"
                  mode="findTenantChanges">
        <xsl:variable name="match" as="xs:string" select="check:roleFromMatch(@match)"/>
        <xsl:variable name="param" as="xs:string">
            <xsl:variable name="after" as="xs:string" select="substring-after($match,'{')"/>
            <xsl:value-of select="substring($after,1,string-length($after)-1)"/>
        </xsl:variable>

        <xsl:try>
            <xsl:variable name="fixParentParams" as="xs:string*"
                          select="distinct-values(check:findParentTenantParams(., $param))"/>
            <xsl:choose>
                <!--
                    There are no missing parent params to fix. This
                    means that everything is in order, we are good,
                    for now.
                -->
                <xsl:when test="empty($fixParentParams)" />
                <!--
                    We have parent params where isTenant isn't set to
                    true, so we should denote that we need to modify
                    these params.
                -->
                <xsl:otherwise>
                    <xsl:map-entry key="$SET_IS_TENANT_ID" select="$fixParentParams"/>
                </xsl:otherwise>
            </xsl:choose>
            <xsl:try>
                <!--
                    Because it is possible (and useful) to have multiple
                    params of the same name, it could happen that there
                    are additional params among our descendants, and if so
                    this step needs to move.  Let's take a look to be safe.
                -->
                <xsl:map-entry key="xs:string(@id)" select="distinct-values(check:findChildTenantParams(.,$param))"/>
                <xsl:catch _errors="{$MISSING_TENANT}">
                    <!--
                        Ignore : if we don't find anything here, this
                        isn't an error because we found a parameter in
                        our parent steps.
                    -->
                </xsl:catch>
            </xsl:try>
            <xsl:catch _errors="{$MISSING_TENANT}">
                <!--
                    We couldn't find the tenant among our parents. We
                    can recover from this by looking at our
                    decendents. If the tenant param is there we need
                    to denote that this step must be moved.
                -->
                <xsl:try>
                    <xsl:map-entry key="xs:string(@id)" select="distinct-values(check:findChildTenantParams(.,$param))"/>
                    <xsl:catch _errors="{$MISSING_TENANT}">
                        <!--
                            Okay we still couldn't find it, this is truely
                            an error. Log an error message and call it
                            quits.
                        -->
                        <xsl:message terminate="yes" expand-text="yes">[ERROR] rax:roles match for role '{$match}', but no defined param named '{$param}' in this case.</xsl:message>
                    </xsl:catch>
                </xsl:try>
            </xsl:catch>
        </xsl:try>
    </xsl:template>
    <xsl:template match="text()" mode="findTenantChanges"/>

    <!--
        For the given $step, returns the ID if the step is a tenant
        param of name $name. Or return the IDs of all children of
        $step that are params $name of names.

        If $step is of type ACCEPT, or there is a path to ACCEPT that
        does not contain an approprite tenant param, then fail with a
        $MISSING_TENANT error.
    -->
    <xsl:function name="check:findChildTenantParams" as="xs:string*">
        <xsl:param name="step" as="node()"/>
        <xsl:param name="name" as="xs:string"/>

        <xsl:choose>
            <xsl:when test="$step/@type='ACCEPT'">
                <!--
                    Made it to ACCEPT without a param name, this is an
                    error!
                -->
                <xsl:sequence select="error($MISSING_TENANT)"/>
            </xsl:when>
            <xsl:when test="not($step/@next)">
                <!--
                    We are at a terminal step that is not accept (an
                    error state).  Ignore this path, return nothing.
                -->
                <xsl:sequence select="()"/>
            </xsl:when>
            <xsl:when test="($step/@name=$name) and ($step/@type = $param-types)">
                <!--
                    Found the tenant so return the ID.
                -->
                <xsl:sequence select="xs:string($step/@id)"/>
            </xsl:when>
            <xsl:otherwise>
                <!--
                    Keep looking.
                -->
                <xsl:sequence select="for $s in check:stepsByIds($checker, check:next($step)) return check:findChildTenantParams($s, $name)"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:function>

    <!--
        For the given $step, returns the id of a parent step that
        contains param of name $name, but has isTenant set to
        false. It returns an empty sequence if isTenant is set to
        true. It also fails with MISSING_TENANT error if the param is
        not found at all.
    -->
    <xsl:function name="check:findParentTenantParams" as="xs:string*">
        <xsl:param name="step" as="node()"/>
        <xsl:param name="name" as="xs:string"/>

        <xsl:choose>
            <xsl:when test="$step/@type='START'">
                <!--
                    We walked back to start so, nope.
                -->
                <xsl:sequence select="error($MISSING_TENANT)"/>
            </xsl:when>
            <xsl:when test="($step/@name=$name) and ($step/@type = $param-types) and xs:boolean($step/@isTenant)">
                <!--
                    Found it and it requires no changes...so return nothing.
                -->
                <xsl:sequence select="()"/>
            </xsl:when>
            <xsl:when test="($step/@name=$name) and ($step/@type = $param-types) and not(xs:boolean($step/@isTenant))">
                <!--
                    Found it but isTenant is false, return the ID
                -->
                <xsl:sequence select="xs:string($step/@id)"/>
            </xsl:when>
            <xsl:otherwise>
                <!--
                    Keep looking.
                -->
                <xsl:variable name="parents" as="node()*" select="key('checker-by-ref',$step/@id, $checker)"/>
                <xsl:sequence select="for $p in $parents return check:findParentTenantParams($p,$name)"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:function>

    <!--
        Given a current $step, ruterns the ID the step if it is a
        X-RELEVENAT-ROLE capture header. Continues to check to child
        nodes until an X-RELEVENT-ROLE check is found.  If none is
        found raisis an error $MISSING_REL_ROLE.
    -->
    <xsl:function name="check:findRelRole" as="xs:string?">
        <xsl:param name="step" as="node()"/>
        <xsl:param name="forRole" as="xs:string"/>
        <xsl:choose>
            <xsl:when test="$step/@type='ACCEPT'">
                <!--
                    Maide it to ACCEPT without setting an
                    X-RELEVENT-ROLE, something is wrong here raise an
                    error!
                -->
                <xsl:sequence
                    select="error($MISSING_REL_ROLE, 'Missing Relevant Role Check for step ' || $forRole, $forRole)"/>
            </xsl:when>
            <xsl:when test="not($step/@next)">
                <!--
                    We are at a non-ACCEPT terminal step.
                    Ignore this path.
                -->
                <xsl:sequence select="()"/>
            </xsl:when>
            <xsl:when test="($step/@type='CAPTURE_HEADER') and ($step/@name='X-RELEVANT-ROLES')">
                <!--
                    Found it, return the id
                -->
                <xsl:sequence select="xs:string($step/@id)"/>
            </xsl:when>
            <xsl:otherwise>
                <!--
                    Keep looking, return the first match.
                -->
                <xsl:sequence select="(for $s in check:stepsByIds($checker, check:next($step)) return check:findRelRole($s, $forRole))[1]"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:function>

    <!--
        Returns true if the checker contains at least one X-ROLE check
        which references a tenant param.
    -->
    <xsl:function name="check:containsTenantRoleCheck"
                  as="xs:boolean">
        <xsl:sequence
            select="exists($checker/check:checker/check:step[@name=$ROLES_HEADER
                    and @type=$ROLES_TYPE and matches(@match,$tenantRoleRegex)][1])"/>
    </xsl:function>
</xsl:transform>
