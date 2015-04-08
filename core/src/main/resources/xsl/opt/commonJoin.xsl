<?xml version="1.0" encoding="UTF-8"?>
<!--
   join.xsl

   This stylesheet take a document in checker format and safely joins
   states in different branches of execution. The operation works like
   removeDups.xsl except that output of a state need not be the same
   in each of the replaced states.

   For Example:

              +===+    +===+
            /=| B +====+ X |
   +===+ /==  +===+    +===+
   | A +=
   +===+ \==  +===+    +===+
            \=| B +====+ Y |
              +===+    +===+

   Becomes:
                     +===+
                    /+ X |
   +===+    +===+ /= +===+
   | A +====+ B +=
   +===+    +===+ \= +===+
                    \+ Y |
                     +===+

   The process is executed recursively.  It is assumed that the input
   file has already gone through the removeDups.xsl

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
        xmlns="http://www.rackspace.com/repose/wadl/checker"
        exclude-result-prefixes="xsd check"
        version="2.0">

    <!--
        Most of the work of joining is done by the following util. The
        purpose of this template is to identify the joins and to
        create new join steps.
    -->
    <xsl:import href="../util/join.xsl"/>

    <!--
        Select join candidates with auto-generated code.
    -->
    <xsl:include href="removeDups-rules.common.xsl"/>

    <!--
        Convert joins into a checker step.
    -->
    <xsl:template match="check:join" mode="join">
        <xsl:param name="checker" as="node()"/>
        <xsl:param name="joins" as="node()*"/>
        <xsl:variable name="steps" select="@steps"/>
        <xsl:variable name="joinSteps" as="node()*" select="check:stepsByIds($checker, tokenize($steps, ' '))"/>

        <step id="{generate-id(.)}">
            <!--
                Since the steps are exactly alike, we need to simply copy
                over the attributes from the first join step.
            -->
            <xsl:apply-templates select="$joinSteps[1]/@*[not(name() = ('next','label','id'))]"
                                 mode="copy"/>
            <xsl:call-template name="joinNext">
                <xsl:with-param name="checker" select="$checker"/>
                <xsl:with-param name="joins" select="$joins"/>
            </xsl:call-template>
        </step>
    </xsl:template>
</xsl:stylesheet>
