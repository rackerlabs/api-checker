<?xml version="1.0" encoding="UTF-8"?>
<!--
   raxRolesMask.xsl

   By default when the rax roles extension is enabled the checker
   document has header checks steps after each method which perform a
   check on the X-Roles header for an appropriate role:


                                              +===+
                                             /|GET|        +=====+
                                            / +===+       /+Admin|
                                          /=           /== +=====+
                                         /    +====+ /=    +====+
  +=====+    +====+    +==+    +========/  /==|POST+=======|User|
  |Start|====+path+====+to+====+resource\==   +====+       +====+
  +=====+    +====+    +==+    +========+\=
                                           \= +======+     +=====+
                                             \|DELETE+=====+Admin|
                                              +======+     +=====+



   This transform moves the header checks up front and repeats
   approprate part of the machine to allow returning 404s and 405s
   errors instead of the default 403:

                                                        +===+
                                                        |GET|
                                                        /===+
                                                       |
                   +=====+   +====+  +==+    +========+/   +======+
                   /Admin|===|path|==|to|====|resource|====|DELETE|
                 / +=====+   +====+  +==+    +========+    +======+
                /                                      \
              /                                         +====+
             /                                          +POST|
   +=====+  /                                           +====+
   |Start|/       +====+  +====+ +==+ +========+
   +=====\========+User|==|path|=|to|=|resource+======+===+
          \       +====+  +====+ +==+ +========+      |GET|
           \                                   \      +===+
            \                                   \
             \                                  | +====+
              \                                  \|POST|
              |                                   +====+
               \
                \
                 \+====+ +==+  +========+   +===+
                  \path|=|to|==|resource|===|GET|
                  +====+ +==+  +========+   +===+

   Copyright 2014 Rackspace US, Inc.

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
    xmlns:chk="http://www.rackspace.com/repose/wadl/checker"
    xmlns="http://www.rackspace.com/repose/wadl/checker"
    xmlns:rol="http://www.rackspace.com/repose/wadl/checker/rax-roles"
    xmlns:util="http://www.rackspace.com/repose/wadl/checker/util"
    exclude-result-prefixes="xs rol chk util"
    version="2.0">

    <xsl:import href="util/pruneSteps.xsl"/>

    <xsl:variable name="checker" select="/chk:checker" as="node()"/>
    <xsl:variable name="roles" select="distinct-values(/chk:checker/chk:step[@type='HEADER_ANY' and
                                                             @name='X-ROLES' and @code='403']/@match)"
                               as="xs:string*"/>
    <!--
        searchStates:

        We continue the search for roles checks along a path so long
        as we are traversing one of these states...
    -->
    <xsl:variable name="searchStates"
                  select="('URL',
                          'URLXSD','METHOD','HEADER','HEADER_ANY',
                          'HEADERXSD','HEADERXSD_ANY', 'HEADER_ALL',
                          'HEADER_SINGLE', 'HEADERXSD_SINGLE',
                          'SET_HEADER','SET_HEADER_ALWAYS')"
                  as="xs:string*"/>

    <xsl:template match="/">
        <xsl:choose>
            <xsl:when test="empty($roles)">
                <xsl:apply-templates mode="copy"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:variable name="pass1">
                    <xsl:apply-templates mode="processRoles"/>
                </xsl:variable>
                <xsl:copy-of select="util:pruneSteps($pass1)"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    <xsl:template match="@* | node()" mode="processRoles copy">
        <xsl:copy>
            <xsl:apply-templates select="@* | node()" mode="#current"/>
        </xsl:copy>
    </xsl:template>
    <xsl:template match="chk:step[@type='START']" mode="processRoles">
        <xsl:variable name="start" as="node()" select="."/>
        <xsl:variable name="headerChecks" as="node()*">
            <xsl:for-each select="$roles">
                <step id="{$start/@id}" next="{$start/@next}" type='HEADER_ANY' priority="5" name="X-ROLES" match="{.}" />
            </xsl:for-each>
        </xsl:variable>
        <xsl:variable name="noRoleStart" as="node()*">
            <xsl:call-template name="copyPathsForRole">
                 <xsl:with-param name="role" select="''"/>
            </xsl:call-template>
        </xsl:variable>
        <xsl:variable name="headerCheckIds" as="xs:string*" select="for $r in $roles return rol:id($start/@id, $r)"/>
        <xsl:for-each select="$headerChecks">
            <xsl:call-template name="copyPathsForRole">
                <xsl:with-param name="role" select="@match"/>
            </xsl:call-template>
        </xsl:for-each>
        <step id="{$start/@id}" type="START">
            <xsl:attribute name="next">
                <xsl:variable name="next" as="xs:string*">
                    <xsl:sequence select="$headerCheckIds"/>
                    <xsl:sequence select="chk:next($noRoleStart[@type='START'][1])"/>
                </xsl:variable>
                <xsl:value-of select="$next" separator=" "/>
            </xsl:attribute>
        </step>
        <xsl:copy-of select="$noRoleStart[@type != 'START']"/>
    </xsl:template>
    <xsl:template name="copyPathsForRole">
        <xsl:param name="role" as="xs:string"/>
        <xsl:variable name="nexts" as="node()*" select="for $n in chk:next(.) return key('checker-by-id',$n,$checker)"/>
        <xsl:variable name="nextPathSteps" as="node()*"
            select="for $n in $nexts[@type=$searchStates] return if (rol:stepInRole($n, $role)) then $n else ()"/>
        <xsl:variable name="URLFailId" as="xs:string" select="rol:id(concat(@id,'UF'),$role)"/>
        <xsl:variable name="MethodFailId" as="xs:string" select="rol:id(concat(@id,'MF'),$role)"/>
        <xsl:variable name="finalCheck" as="node()?" select="$nexts[@type='HEADER_ANY' and @name='X-ROLES'
                                                                  and @code='403' and @match=$role][1]"/>
        <xsl:copy>
            <xsl:attribute name="id" select="rol:id(@id,$role)"/>
            <xsl:attribute name="next">
                <xsl:choose>
                    <xsl:when test="$finalCheck">
                        <xsl:value-of select="$finalCheck/@next"/>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:variable name="next" as="xs:string*">
                            <xsl:sequence select="for $n in $nextPathSteps return rol:id($n/@id, $role)"/>
                            <xsl:sequence select="$nexts[not(@type=('URL_FAIL','METHOD_FAIL', $searchStates))]/@id"/>
                            <xsl:sequence select="($URLFailId, $MethodFailId)"/>
                        </xsl:variable>
                        <xsl:value-of select="$next" separator=" "/>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:attribute>
            <xsl:apply-templates mode="copy" select="@*[not(local-name() = ('id', 'next'))] | node()"/>
        </xsl:copy>
        <xsl:if test="not($finalCheck)">
            <xsl:call-template name="addURIErrorStates">
                <xsl:with-param name="id" select="$URLFailId"/>
                <xsl:with-param name="uriStates" select="$nextPathSteps[@type=('URL', 'URLXSD')]"/>
            </xsl:call-template>
            <xsl:call-template name="addMethodErrorStates">
                <xsl:with-param name="id" select="$MethodFailId"/>
                <xsl:with-param name="methodStates" select="$nextPathSteps[@type='METHOD']"/>
            </xsl:call-template>
            <xsl:for-each select="$nextPathSteps">
                <xsl:call-template name="copyPathsForRole">
                    <xsl:with-param name="role" select="$role"/>
                </xsl:call-template>
            </xsl:for-each>
        </xsl:if>
    </xsl:template>
    <xsl:template name="addURIErrorStates">
        <xsl:param name="id" as="xs:string"/>
        <xsl:param name="uriStates" as="node()*"/>
        <step id="{$id}" type="URL_FAIL">
            <xsl:if test="not(empty($uriStates))">
                <xsl:variable name="notMatches" as="xs:string*">
                    <xsl:perform-sort select="$uriStates[@type='URL']/@match">
                        <xsl:sort select="."/>
                    </xsl:perform-sort>
                </xsl:variable>
                <xsl:variable name="notXSDMatches" as="xs:QName*">
                    <xsl:perform-sort select="for $s in $uriStates[@type='URLXSD'] return resolve-QName($s/@match, $s)">
                        <xsl:sort select="concat(local-name-from-QName(.),'_',namespace-uri-from-QName(.))"/>
                    </xsl:perform-sort>
                </xsl:variable>
                <xsl:if test="not(empty($notMatches))">
                    <xsl:attribute name="notMatch">
                        <xsl:value-of select="$notMatches" separator="|"/>
                    </xsl:attribute>
                </xsl:if>
                <xsl:if test="not(empty($notXSDMatches))">
                    <xsl:attribute name="notTypes">
                        <xsl:value-of select="$notXSDMatches" separator=" "/>
                    </xsl:attribute>
                </xsl:if>
            </xsl:if>
        </step>
    </xsl:template>
    <xsl:template name="addMethodErrorStates">
        <xsl:param name="id" as="xs:string"/>
        <xsl:param name="methodStates" as="node()*"/>
        <step id="{$id}" type="METHOD_FAIL">
            <xsl:if test="not(empty($methodStates))">
                <xsl:variable name="notMatchValues" as="xs:string*">
                    <xsl:perform-sort select="$methodStates/@match">
                        <xsl:sort select="."/>
                    </xsl:perform-sort>
                </xsl:variable>
                <xsl:attribute name="notMatch">
                    <xsl:value-of select="$notMatchValues" separator="|"/>
                </xsl:attribute>
            </xsl:if>
        </step>
    </xsl:template>
    <xsl:function name="rol:stepInRole" as="xs:boolean">
        <xsl:param name="step" as="node()"/>
        <xsl:param name="role" as="xs:string"/>
        <xsl:choose>
            <xsl:when test="$step/@type='HEADER_ANY' and $step/@name='X-ROLES' and
                            $step/@code='403' and not($step/@match = $role)">
                <xsl:sequence select="false()"/>
            </xsl:when>
            <xsl:when test="$step/@type = $searchStates">
                <xsl:sequence select="true() = (for $n in chk:next($step) return rol:stepInRole(key('checker-by-id',$n,$checker),$role))"/>
            </xsl:when>
            <xsl:when test="contains($step/@type,'_FAIL')">
                <xsl:sequence select="false()"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:sequence select="true()"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:function>
    <xsl:function as="xs:string" name="rol:id">
        <xsl:param name="oldId" as="xs:string"/>
        <xsl:param name="role" as="xs:string"/>
        <xsl:value-of select="chk:string-to-id(replace(concat($oldId,'_',$role),':','_'))"/>
    </xsl:function>
</xsl:stylesheet>
