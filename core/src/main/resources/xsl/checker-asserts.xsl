<?xml version="1.0" encoding="UTF-8"?>
<!--
   checker-asserts.xsl

   This stylesheet simply copies over a checker and displays an error
   if complex assertions fail. It is run only when the checker needs
   to be validated.

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
    xmlns:chk="http://www.rackspace.com/repose/wadl/checker"
    xmlns="http://www.rackspace.com/repose/wadl/checker"
    exclude-result-prefixes="xs chk"
    version="2.0">

    <xsl:import href="util/funs.xsl"/>

    <xsl:variable name="checker" select="/chk:checker" as="node()"/>

    <xsl:template match="/">
        <xsl:apply-templates />
        <xsl:apply-templates mode="copy"/>
    </xsl:template>

    <xsl:template match="node() | @*" mode="copy">
        <xsl:copy>
            <xsl:apply-templates select="@* | node()" mode="copy"/>
        </xsl:copy>
    </xsl:template>

    <xsl:template match="text()"/>

    <!--
        *** Check Start Step ***
    -->

    <xsl:template match="chk:step[@type='START']">
        <!--
            Start should proceed with URL and METHODs.
         -->
        <xsl:call-template name="chk:urlDescendantAsserts"/>
    </xsl:template>

    <!--
       *** Check URL and METHOD Steps ***
    -->

    <xsl:template match="chk:step[@type=('URL', 'METHOD') and @match != $matchAll]">
        <xsl:variable as="xs:string" name="myType" select="@type"/>
        <xsl:variable name="parents" as="node()*" select="key('checker-by-ref',@id, $checker)"/>
        <xsl:choose>
            <xsl:when test="chk:stepsByIds($checker, chk:siblings($checker,.))[@type=$myType and @match=$matchAll]">
                <!--
                    If this step has siblings that match .*, then that's the same
                    as if this step matches .* from an error condition point of view.
                -->
                <xsl:call-template name="chk:allMatchURLMethod"/>
            </xsl:when>
            <xsl:when test="(@type='METHOD') and (every $parent in $parents satisfies $parent/@type='METHOD')">
                <!--
                    A METHOD step can contain another METHOD as a parent to allow chaining multiple
                    METHODs of the same type. These chained METHODs do not require an error state.
                -->
            </xsl:when>
            <xsl:otherwise>
                <xsl:call-template name="chk:TypeNotMatchRefErrorAssert"/>
                <xsl:if test="@type='URL'">
                    <xsl:call-template name="chk:urlDescendantAsserts"/>
                </xsl:if>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template name="chk:allMatchURLMethod" match="chk:step[@type=('URL', 'METHOD') and @match = $matchAll]">
        <!--
            This is essestially a no-op.  If you match .* then you are not required to have a matching
            error step, since essentially this step will always accept. In other words, the error step
            will never hit.

            It is okay for the error step to exist though, and if it does it must be correctly formed,
            but that's validated in another template.

            TODO: We could potentially have an optimization that removes unneeded error steps.
                  As it works now, builder.xsl will not add error steps for .* steps, but some
                  later phases of the pipeline add them in anyway so there's no consistancy there.
                  The clean approach will be to not treat .* as a special case and have a very specific
                  optimization phase that just removes unneeded error states.
         -->
        <xsl:if test="@type='URL'">
            <xsl:call-template name="chk:urlDescendantAsserts"/>
        </xsl:if>
    </xsl:template>

    <xsl:template match="chk:step[@type=('URL_FAIL', 'METHOD_FAIL', 'REQ_TYPE_FAIL') and @notMatch]">
        <xsl:variable name="errorStepId" as="xs:string" select="@id"/>
        <xsl:variable name="notMatches" as="xs:string*" select="for $m in tokenize(chk:escapeBar(@notMatch),'\|')
                                                                return chk:unescapeBar($m)"/>
        <xsl:variable name="sibs" as="node()*" select="chk:stepsByIds($checker, chk:siblings($checker,.))"/>
        <xsl:variable name="refType" as="xs:string"
            select="if (@type='REQ_TYPE_FAIL') then 'REQ_TYPE' else substring-before(@type,'_')"/>
        <xsl:call-template name="chk:mustReferenceByType">
            <xsl:with-param name="refTypes" select="$refType"/>
        </xsl:call-template>
        <!-- Every notMatch is referenced by a refType state -->
        <xsl:for-each select="$notMatches">
            <xsl:variable name="match" as="xs:string" select="."/>
            <xsl:if test="empty($sibs[@type=$refType and @match=$match])">
                <xsl:message terminate="yes">
                    Step <xsl:value-of select="$errorStepId"/> suggests that there should be a sibling
                    step of type <xsl:value-of select="$refType"/> with a match of <xsl:value-of select="$match"/>.
                </xsl:message>
            </xsl:if>
        </xsl:for-each>
        <!-- Every refType state is referenced by a notMatch -->
        <xsl:for-each select="$sibs[@type=$refType]">
            <xsl:if test="not(@match = $notMatches)">
                <xsl:message terminate="yes">
                    Step <xsl:value-of select="@id"/> of type <xsl:value-of select="$refType"/> is not
                    referenced by error state <xsl:value-of select="$errorStepId"/>
                </xsl:message>
            </xsl:if>
        </xsl:for-each>
        <xsl:if test="@notTypes">
            <xsl:call-template name="chk:notTypesAsserts"/>
        </xsl:if>
    </xsl:template>

    <xsl:template match="chk:step[@type=('URL_FAIL', 'METHOD_FAIL') and not(@notMatch)]">
        <xsl:call-template name="chk:mustNotReferenceByType">
            <xsl:with-param name="refTypes" select="substring-before(@type,'_')"/>
        </xsl:call-template>
        <xsl:if test="@notTypes">
            <xsl:call-template name="chk:notTypesAsserts"/>
        </xsl:if>
    </xsl:template>

    <xsl:template match="chk:step[@type='URLXSD']">
        <xsl:variable name="thisStep" as="node()" select="."/>
        <xsl:variable name="sibs" as="node()*" select="chk:stepsByIds($checker, chk:siblings($checker,.))"/>
        <xsl:variable name="qnameMatch" as="xs:QName" select="resolve-QName(@match, .)"/>
        <xsl:choose>
            <xsl:when test="$sibs[@type='URL' and @match=$matchAll]">
                <!--
                    If this step has siblings that match .*, then that's the same
                    as if this step matches .* from an error condition point of view.
                -->
                <xsl:call-template name="chk:allMatchURLMethod"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:if test="not($sibs[@type='URL_FAIL' and chk:matchesnotTypes(.,$qnameMatch)])">
                    <xsl:message terminate="yes">
                Checker error:  There are steps which reference <xsl:value-of select="@id"/>
                but do not also reference an URL_FAIL state with a
                @notType attribute value of <xsl:value-of select="@match"/>
                    </xsl:message>
                </xsl:if>
                <xsl:call-template name="chk:urlDescendantAsserts"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template name="chk:urlDescendantAsserts">
        <xsl:variable name="nextSteps" as="node()*" select="chk:stepsByIds($checker, chk:next(.))"/>
        <xsl:if test="empty($nextSteps[@type=('METHOD','METHOD_FAIL')])">
            <xsl:message terminate="yes">
                The state <xsl:value-of select="@id"/> must be proceeded by
                a METHOD or METHOD_FAIL state.
            </xsl:message>
        </xsl:if>
        <xsl:if test="not(chk:descendantTypes(.,('URL', 'URLXSD','URL_FAIL')))">
            <xsl:message terminate="yes">
                Step <xsl:value-of select="@id"/> must be followed by a descendant
                that is either of type URL, URLXSD, or URL_FAIL.
            </xsl:message>
        </xsl:if>
    </xsl:template>

    <xsl:template name="chk:notTypesAsserts">
        <xsl:variable name="errorStepId" as="xs:string" select="@id"/>
        <xsl:variable name="sibs" as="node()*" select="chk:stepsByIds($checker, chk:siblings($checker,.))"/>
        <xsl:variable name="notTypes" as="xs:QName*" select="for $nt in tokenize(@notTypes,' ') return resolve-QName($nt, .)"/>
        <xsl:for-each select="$notTypes">
            <xsl:variable name="match" as="xs:QName" select="."/>
            <xsl:if test="empty($sibs[@type='URLXSD' and resolve-QName(@match, .)=$match])">
                <xsl:message terminate="yes">
                  Step <xsl:value-of select="$errorStepId"/> suggests that there should be a sibling
                  step of type URLXSD with a match of <xsl:value-of select="$match"/>.
                </xsl:message>
            </xsl:if>
        </xsl:for-each>
    </xsl:template>


    <!--
       *** Check REQ_TYPE asserts ***
    -->

    <xsl:template match="chk:step[@type='REQ_TYPE']">
        <xsl:call-template name="chk:TypeNotMatchRefErrorAssert"/>
    </xsl:template>

    <!--
       *** Check content asserts ***
    -->
    <xsl:template match="chk:step[@type = $cont-error-types]">
        <xsl:variable name="parents" as="node()*" select="key('checker-by-ref',@id, $checker)"/>
        <!--
            Either all content step parents MUST be content steps themselves OR
            the content step must have a sibling that is a CONTENT_FAIL.

            If a step is of type HEADER_ANY then its parent is allowed to be START.
            This weird edge case allows the implementation of rax:roles MASK.
         -->
        <xsl:choose>
            <xsl:when test="every $parent in $parents satisfies $parent/@type = $cont-error-types"/>
            <xsl:when test="(@type='HEADER_ANY') and (every $parent in $parents satisfies $parent/@type = 'START')"/>
            <xsl:otherwise>
                <xsl:call-template name="chk:mustReferenceByType">
                    <xsl:with-param name="refTypes" select="'CONTENT_FAIL'"/>
                </xsl:call-template>
            </xsl:otherwise>
        </xsl:choose>
        <!--
            Content dependecies:  XSD and XPATH require parent to be XSL, WELL_XML, XPATH
                                  JSON_SCHEMA requires parent to be WELL_JSON
         -->
        <xsl:choose>
            <xsl:when test="@type=('XSD', 'XPATH')">
                <xsl:call-template name="chk:requireParentType">
                    <xsl:with-param name="parentTypes" select="('WELL_XML', 'XSL', 'XPATH')"/>
                </xsl:call-template>
            </xsl:when>
            <xsl:when test="@type='JSON_SCHEMA'">
                <xsl:call-template name="chk:requireParentType">
                    <xsl:with-param name="parentTypes" select="'WELL_JSON'"/>
                </xsl:call-template>
            </xsl:when>
        </xsl:choose>
    </xsl:template>

    <xsl:template match="chk:step[@type='CONTENT_FAIL']">
        <xsl:call-template name="chk:mustReferenceByType">
            <xsl:with-param name="refTypes" select="$cont-error-types"/>
        </xsl:call-template>
    </xsl:template>

    <!--
        Assertion for the following pattern:
        Anything that references this state, must NOT also reference a state of type
        $refType.
     -->
    <xsl:template name="chk:mustNotReferenceByType">
        <xsl:param name="refTypes" as="xs:string*"/>
        <xsl:if test="not(every $t in
            (: $n is a step that refrences this step :)
            for $n in key('checker-by-ref',@id, $checker) return
            (:
             Anything $n references must not also reference a step
             of type $refType
            :)
            every $s in chk:stepsByIds($checker, chk:next($n)) satisfies not($s/@type=$refTypes)
            (:
            This must be satisfied for every reference!
            :)
            satisfies $t = true())">
            <xsl:message terminate="yes">
                Checker error:  There are steps which reference <xsl:value-of select="@id"/>
                but also reference a step of type(s) <xsl:value-of select="$refTypes" separator=" or "/>.
                This is not allowed.
            </xsl:message>
        </xsl:if>
    </xsl:template>

    <!--
        Assertion for the following pattern:
        Anything that references this state, must also reference a state of type
        $refType.
     -->
    <xsl:template name="chk:mustReferenceByType">
        <xsl:param name="refTypes" as="xs:string*"/>
        <xsl:if test="not(every $t in
            (: $n is a step that refrences this step :)
            for $n in key('checker-by-ref',@id, $checker) return
            (:
            $n must also reference a step that is of types $refTypes
            :)
            some $s in chk:stepsByIds($checker, chk:next($n)) satisfies $s/@type=$refTypes
            (:
            This must be satisfied for every reference!
            :)
            satisfies $t = true())">
            <xsl:message terminate="yes">
                Checker error:  There are steps which reference <xsl:value-of select="@id"/>
                but do not also reference a <xsl:value-of select="$refTypes" separator=" or "/> state.
            </xsl:message>
        </xsl:if>
    </xsl:template>

    <!--
        Assertion for the following pattern:
        If your are state type X then states that reference you via @next must also
        refernece a state of type X_FAIL which contains a @notMatch attribute.
     -->
    <xsl:template name="chk:TypeNotMatchRefErrorAssert">
        <xsl:variable name="errorStateType" as="xs:string" select="concat(@type,'_FAIL')"/>
        <xsl:variable name="errorMessage" as="node()*">
            Checker error:  There are steps which reference <xsl:value-of select="@id"/>
            but do not also reference an <xsl:value-of select="$errorStateType"/> state with a
            @notMatch attribute<xsl:if test="@type='METHOD'"> or a METHOD state</xsl:if>.
        </xsl:variable>
        <xsl:choose>
            <!--
                METHOD is a special case because it's legit for a method to have a parent
                which is also a METHOD.
            -->
            <xsl:when test="@type = 'METHOD'">
                <xsl:if test="not(every $t in
                    (: $n is a step that refrences this step :)
                    for $n in key('checker-by-ref',@id, $checker) return
                    (:
                    $n must also reference a step that an METHOD_FAIL and contains
                    a @notMatch or $n should be of type METHOD
                    :)
                    (some $s in chk:stepsByIds($checker, chk:next($n)) satisfies ($s/@type=$errorStateType and $s/@notMatch)) or $n/@type='METHOD'
                    (:
                    This must be satisfied for every reference!
                    :)
                    satisfies $t = true())">
                    <xsl:message terminate="yes" select="$errorMessage"/>
                </xsl:if>
            </xsl:when>
            <xsl:otherwise>
                <xsl:if test="not(every $t in
                    (: $n is a step that refrences this step :)
                    for $n in key('checker-by-ref',@id, $checker) return
                    (:
                    $n must also reference a step that an URL_FAIL and contains
                    a @notMatch
                    :)
                    some $s in chk:stepsByIds($checker, chk:next($n)) satisfies $s/@type=$errorStateType and $s/@notMatch
                    (:
                    This must be satisfied for every reference!
                    :)
                    satisfies $t = true())">
                    <xsl:message terminate="yes" select="$errorMessage"/>
                </xsl:if>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <!--
        Assert that the parent of this step is one of the $parentType types.
     -->
    <xsl:template name="chk:requireParentType">
        <xsl:param name="parentTypes" as="xs:string*"/>
        <xsl:variable name="parents" as="node()*" select="key('checker-by-ref',@id, $checker)"/>
        <xsl:if test="not(every $parent in $parents satisfies $parent/@type=$parentTypes)">
            <xsl:message terminate="yes">
                Step <xsl:value-of select="@id"/> requires all parent steps to be of type <xsl:value-of select="$parentTypes" separator=" or "/>
            </xsl:message>
        </xsl:if>
    </xsl:template>

    <!--
        Returns true, if there are descendants of decendent types
        given the current node.
     -->
    <xsl:function name="chk:descendantTypes" as="xs:boolean">
        <xsl:param name="step" as="node()"/>
        <xsl:param name="descendantTypes" as="xs:string*"/>
        <xsl:variable name="nextSteps" as="node()*" select="chk:stepsByIds($checker, chk:next($step))"/>
        <xsl:choose>
            <xsl:when test="some $s in $nextSteps satisfies $s/@type=$descendantTypes">
                <xsl:value-of select="true()"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="some $s in $nextSteps satisfies chk:descendantTypes($s, $descendantTypes)"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:function>

    <xsl:function name="chk:matchesnotTypes" as="xs:boolean">
        <xsl:param name="step" as="node()"/>
        <xsl:param name="match" as="xs:QName"/>
        <xsl:variable name="notTypes" as="xs:QName*"
            select="for $qn in tokenize($step/@notTypes,' ') return resolve-QName($qn, $step)"/>
        <xsl:value-of select="some $qn in $notTypes satisfies $qn = $match"/>
    </xsl:function>

    <!--
        The escape and unescape functions below are kinda hacky, still, I'm
        betting that it's very unlikely that the pattern [((☞☜))] will
        exist in a real WADL
     -->
    <xsl:function name="chk:escapeBar" as="xs:string">
        <xsl:param name="barString"/>
        <xsl:value-of select="replace($barString,'\\\|','[((☞☜))]')"/>
    </xsl:function>

    <xsl:function name="chk:unescapeBar" as="xs:string">
        <xsl:param name="barString"/>
        <xsl:value-of select="replace($barString,'\[\(\(☞☜\)\)\]','\\|')"/>
    </xsl:function>
</xsl:stylesheet>
