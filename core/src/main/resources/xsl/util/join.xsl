<?xml version="1.0" encoding="UTF-8"?>
<!--
   join.xsl

   This stylesheet takes a document in checker format and safely joins
   states in different branches of execution.

   For Example:

              +===+    +===+
            /=| C +====+ X |
   +===+ /==  +===+    +===+
   | A +=
   +===+ \==  +===+    +===+
            \=| D +====+ Y |
              +===+    +===+

   Becomes:
                     +===+
                    /+ X |
   +===+    +===+ /= +===+
   | A +====+ E +=
   +===+    +===+ \= +===+
                    \+ Y |
                     +===+

   A new state E is used to replace states C and D. The process is
   executed recursively.

   How the process identifies candidates for the join (C, D), and
   produces the new state (E) is left for a customization layer to
   implement.  This stylesheet does all of the common work around the
   process of joining.

   The customization layer should fill in:

   1.  A template which matches a check:step and produces joins, in
       the example above it would idetitfy steps C and D.

   2.  Templates in 'join' mode, wich match on joins and create the
       replacement checker steps. So in the example above it would
       match on the join produced in step 1 and generate step E.

   3.  A template named 'addMetadata' which produces metadata for
      the output checker, this is optional.

   See the bottom of this file for details.

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
<xsl:stylesheet
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xsd="http://www.w3.org/2001/XMLSchema"
    xmlns:check="http://www.rackspace.com/repose/wadl/checker"
    xmlns:util="http://www.rackspace.com/repose/wadl/checker/util"
    xmlns:map="http://www.w3.org/2005/xpath-functions/map"
    xmlns="http://www.rackspace.com/repose/wadl/checker"
    exclude-result-prefixes="xsd check"
    version="3.0">

    <xsl:import href="pruneSteps.xsl"/>

    <xsl:output indent="yes" method="xml"/>

    <xsl:template match="check:checker" name="joinDups">
        <xsl:param name="checker" select="." as="node()"/>
        <xsl:variable name="joins" as="map(xsd:string, xsd:string*)">
            <xsl:map>
                <xsl:apply-templates mode="getJoins" select="$checker//check:step">
                    <xsl:with-param name="checker" select="$checker"/>
                </xsl:apply-templates>
            </xsl:map>
        </xsl:variable>
        <xsl:choose>
            <xsl:when test="empty(map:keys($joins))">
                <checker>
                    <xsl:copy-of select="/check:checker/namespace::*"/>
                    <xsl:apply-templates select="/check:checker/check:meta" mode="copyMeta"/>
                    <xsl:copy-of select="/check:checker/check:grammar"/>
                    <xsl:variable name="pruned">
                        <xsl:call-template name="util:pruneSteps">
                            <xsl:with-param name="checker" select="$checker"/>
                        </xsl:call-template>
                    </xsl:variable>
                    <xsl:copy-of select="$pruned//check:step"/>
                </checker>
            </xsl:when>
            <xsl:otherwise>
                <xsl:call-template name="joinDups">
                    <xsl:with-param name="checker">
                        <xsl:call-template name="doJoin">
                            <xsl:with-param name="checker" select="$checker"/>
                            <xsl:with-param name="joins" select="$joins"/>
                            <xsl:with-param name="stepsToJoin" select="check:stepsToJoin(map:keys($joins), $joins, map{})"/>
                        </xsl:call-template>
                    </xsl:with-param>
                </xsl:call-template>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:function name="check:stepsToJoin" as="map(xsd:string, xsd:string)">
        <xsl:param name="joins" as="xsd:string*"/>
        <xsl:param name="joinStepIndex" as="map(xsd:string, xsd:string*)"/>
        <xsl:param name="map" as="map(xsd:string, xsd:string)"/>
        <xsl:choose>
            <xsl:when test="not(empty($joins))">
                <xsl:variable name="id" as="xsd:string" select="$joins[1]"/>
                <xsl:variable name="newMap" as="map(xsd:string, xsd:string)">
                    <xsl:map>
                        <xsl:for-each select="$joinStepIndex($id)">
                            <xsl:if test="not(map:contains($map, .))">
                                <xsl:map-entry key="." select="$id"/>
                            </xsl:if>
                        </xsl:for-each>
                    </xsl:map>
                </xsl:variable>
                <xsl:sequence select="check:stepsToJoin(subsequence($joins, 2), $joinStepIndex, map:merge(($map, $newMap)))"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:sequence select="$map"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:function>

    <xsl:template match="@* | node()" mode="copyMeta" priority="10">
        <xsl:copy>
            <xsl:apply-templates select="@* | node()" mode="copyMeta"/>
        </xsl:copy>
    </xsl:template>

    <xsl:template match="check:meta" mode="copyMeta" priority="11">
        <xsl:copy>
            <xsl:apply-templates select="@* | node()" mode="copyMeta"/>
            <xsl:call-template name="addMetadata"/>
        </xsl:copy>
    </xsl:template>

    <xsl:template name="doJoin">
        <xsl:param name="checker" as="node()"/>
        <xsl:param name="joins" as="map(xsd:string, xsd:string*)"/>
        <xsl:param name="stepsToJoin" as="map(xsd:string, xsd:string)"/>

        <checker>
            <xsl:apply-templates select="$checker" mode="copy">
                <xsl:with-param name="joins" select="$joins"/>
                <xsl:with-param name="stepsToJoin" select="$stepsToJoin"/>
            </xsl:apply-templates>
            <xsl:for-each select="map:keys($joins)">
                <xsl:call-template name="createJoinStep">
                    <xsl:with-param name="checker" select="$checker"/>
                    <xsl:with-param name="joins" select="$joins"/>
                    <xsl:with-param name="stepsToJoin" select="$stepsToJoin"/>
                </xsl:call-template>
            </xsl:for-each>
        </checker>
    </xsl:template>

    <xsl:template name="joinNext">
        <xsl:param name="checker" as="node()*"/>
        <xsl:param name="joins" as="map(xsd:string, xsd:string*)"/>
        <xsl:param name="stepsToJoin" as="map(xsd:string, xsd:string)"/>
        <xsl:param name="join" as="xsd:string" select="."/>
        <xsl:param name="steps" as="node()*" select="check:stepsByIds($checker, $joins(.))"/>
        <xsl:param name="nexts" as="xsd:string*" select="()"/>
        <xsl:choose>
            <xsl:when test="empty($steps)">
                <xsl:attribute name="next">
                    <xsl:value-of separator=" ">
                        <xsl:sequence select="$nexts"/>
                    </xsl:value-of>
                </xsl:attribute>
            </xsl:when>
            <xsl:otherwise>
                <xsl:variable name="snexts" as="xsd:string*" select="check:getNexts($joins, $stepsToJoin, tokenize($steps[1]/@next,' '))"/>
                <xsl:call-template name="joinNext">
                    <xsl:with-param name="checker" select="$checker"/>
                    <xsl:with-param name="joins" select="$joins"/>
                    <xsl:with-param name="stepsToJoin" select="$stepsToJoin"/>
                    <xsl:with-param name="steps" select="subsequence($steps, 2)"/>
                    <xsl:with-param name="nexts"
                                    select="(for $s in $snexts
                                             return if (not($s = $nexts)) then $s else (), $nexts)"/>
                </xsl:call-template>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template match="check:step" mode="copy">
        <xsl:param name="joins" as="map(xsd:string, xsd:string*)"/>
        <xsl:param name="stepsToJoin" as="map(xsd:string, xsd:string)"/>
        <step>
            <xsl:apply-templates select="@*[name() != 'next']" mode="copy"/>
            <xsl:apply-templates select="@next" mode="copy">
                <xsl:with-param name="joins" select="$joins"/>
                <xsl:with-param name="stepsToJoin" select="$stepsToJoin"/>
            </xsl:apply-templates>
            <xsl:copy-of select="element()"/>
        </step>
    </xsl:template>

    <xsl:template match="@*" mode="copy">
        <xsl:copy/>
    </xsl:template>

    <xsl:template match="@next" name="updateNext" mode="copy">
        <xsl:param name="joins" as="map(xsd:string, xsd:string*)"/>
        <xsl:param name="stepsToJoin" as="map(xsd:string, xsd:string)"/>
        <xsl:param name="nexts" as="xsd:string*" select="tokenize(string(.),' ')"/>
        <xsl:attribute name="next"  separator=" " select="check:getNexts($joins,$stepsToJoin,$nexts)"/>
    </xsl:template>

    <xsl:function name="check:getNexts" as="xsd:string*">
        <xsl:param name="joins" as="map(xsd:string, xsd:string*)"/>
        <xsl:param name="stepsToJoin" as="map(xsd:string, xsd:string)"/>
        <xsl:param name="nexts" as="xsd:string*"/>
        <xsl:variable name="candidateJoins" as="xsd:string*" select="distinct-values(for $n in $nexts return map:get($stepsToJoin, $n))"/>
        <xsl:choose>
            <xsl:when test="empty($candidateJoins)">
                <xsl:sequence select="$nexts"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:sequence select="check:replaceJoins($joins, $candidateJoins, $nexts)"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:function>

    <xsl:function name="check:replaceJoins" as="xsd:string*">
        <xsl:param name="joinSteps" as="map(xsd:string, xsd:string*)"/>
        <xsl:param name="candidateJoins" as="xsd:string*"/>
        <xsl:param name="nexts" as="xsd:string*"/>
        <xsl:choose>
            <xsl:when test="empty($candidateJoins)">
                <xsl:sequence select="$nexts"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:variable name="candidateJoin" as="xsd:string" select="$candidateJoins[1]"/>
                <xsl:variable name="steps" as="xsd:string*" select="$joinSteps($candidateJoin)"/>
                <xsl:variable name="nextOut" as="xsd:string*">
                    <xsl:choose>
                        <xsl:when test="every $s in $steps satisfies $s=$nexts">
                            <xsl:sequence select="($candidateJoin, for $n in $nexts return
                                                  if (not($n = $steps)) then $n else ())"/>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:sequence select="$nexts"/>
                        </xsl:otherwise>
                    </xsl:choose>
                </xsl:variable>
                <xsl:sequence select="check:replaceJoins($joinSteps, subsequence($candidateJoins, 2), $nextOut)"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:function>

    <xsl:template match="text()" mode="#all"/>
    <xsl:template match="check:step[@next]" mode="getJoins">
        <xsl:param name="checker" as="node()"/>
        <!--
            This should be implemented by a customization layer. The
            template should take as input an entire checker document,
            and it should match on a single connected checker step.

            The template should return a map that maps a new join id
            with the ids of the state that it joins.

            For example, if ID1, ID2, and ID3 may be joined, then the
            following should be emmited.

            map {
             'JID1' : ('ID1', 'ID2', 'ID3')
             }

            Where JID1 is the suggested ID of the new join state.
        -->
    </xsl:template>

    <!--
        A new step is created for every join. The context is the id of
        the new step. There is a default template for doing this
        called joinNext.

        The following parameters are passed to apply templates:

        checker     :  The entire checker document
        joins       :  A map from join to all of it's state
        stepsToJoin :  A map from a state to it's join state

    -->
    <xsl:template name="createJoinStep">
        <xsl:param name="checker" as="node()"/>
        <xsl:param name="joins" as="map(xsd:string, xsd:string*)"/>
        <xsl:param name="stepsToJoin" as="map(xsd:string, xsd:string)"/>

        <step id="{.}">
            <xsl:call-template name="joinNext">
                <xsl:with-param name="checker" select="$checker"/>
                <xsl:with-param name="joins" select="$joins"/>
                <xsl:with-param name="stepsToJoin" select="$stepsToJoin"/>
             </xsl:call-template>
        </step>
    </xsl:template>

    <!--
        Provide content to be output in metadata section of the
        checker file. You do not need to implement this template.
    -->
    <xsl:template name="addMetadata"/>
</xsl:stylesheet>
