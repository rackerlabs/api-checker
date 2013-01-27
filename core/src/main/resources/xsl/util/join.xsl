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

   How the process identifies candidates for the join (C, D), and what
   attributes the newly created states have (E) is left for a
   customization layer to implement.  This stylesheet does all of the
   common work around the process of joining.

   There are two templates that a customization layer should fill
   in.

   1.  A template which matches a check:step and produces joins, in
       the example above it would idetitfy steps C and D.
   2.  A template which takes a join and producess the attribues
       needed for the replacement step. So in the example above it
       would state what attributes are needed by step E.

   See the bottom of this file for details.

   The @id of state E, and the @next attributes are filled in by this
   stylesheet.
-->
<xsl:stylesheet
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xsd="http://www.w3.org/2001/XMLSchema"
    xmlns:check="http://www.rackspace.com/repose/wadl/checker"
    xmlns:util="http://www.rackspace.com/repose/wadl/checker/util"
    xmlns="http://www.rackspace.com/repose/wadl/checker"
    exclude-result-prefixes="xsd check"
    version="2.0">

    <xsl:import href="pruneSteps.xsl"/>

    <xsl:output indent="yes" method="xml"/>

    <xsl:template match="check:checker" name="joinDups">
        <xsl:param name="checker" select="." as="node()"/>
        <xsl:variable name="joins" as="node()*">
               <xsl:apply-templates mode="getJoins" select="$checker//check:step">
                <xsl:with-param name="checker" select="$checker"/>
               </xsl:apply-templates>
        </xsl:variable>
        <xsl:choose>
            <xsl:when test="empty($joins)">
                <checker>
                    <xsl:copy-of select="/check:checker/namespace::*"/>
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
                        </xsl:call-template>
                    </xsl:with-param>
                </xsl:call-template>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template name="doJoin">
        <xsl:param name="checker" as="node()"/>
        <xsl:param name="joins" as="node()*"/>

        <checker>
            <xsl:apply-templates select="$checker" mode="copy">
                <xsl:with-param name="joins" select="$joins"/>
            </xsl:apply-templates>
            <xsl:apply-templates select="$joins" mode="join">
                <xsl:with-param name="checker" select="$checker"/>
                <xsl:with-param name="joins" select="$joins"/>
            </xsl:apply-templates>
        </checker>
    </xsl:template>

    <xsl:template match="check:join" mode="join">
        <xsl:param name="checker" as="node()"/>
        <xsl:param name="joins" as="node()*"/>
        <xsl:variable name="joinSteps" as="xsd:string*" select="tokenize(@steps,' ')"/>
        <xsl:variable name="steps" as="node()*" select="$checker//check:step[@id = $joinSteps]"/>

        <step id="{generate-id(.)}">
            <xsl:call-template name="stepAttributes">
                <xsl:with-param name="checker" select="$checker"/>
                <xsl:with-param name="joins" select="$joins"/>
                <xsl:with-param name="currentJoin" select="."/>
                <xsl:with-param name="joinSteps" select="$steps"/>
            </xsl:call-template>
            <xsl:call-template name="joinNext">
                <xsl:with-param name="joins" select="$joins"/>
                <xsl:with-param name="steps" select="$steps"/>
                <xsl:with-param name="nexts" select="()"/>
             </xsl:call-template>
        </step>
    </xsl:template>

    <xsl:template name="joinNext">
        <xsl:param name="joins" as="node()*"/>
        <xsl:param name="steps" as="node()*"/>
        <xsl:param name="nexts" as="xsd:string*"/>
        <xsl:choose>
            <xsl:when test="empty($steps)">
                <xsl:attribute name="next">
                    <xsl:value-of separator=" ">
                        <xsl:sequence select="$nexts"/>
                    </xsl:value-of>
                </xsl:attribute>
            </xsl:when>
            <xsl:otherwise>
                <xsl:variable name="snexts" as="xsd:string*" select="check:getNexts($joins,tokenize($steps[1]/@next,' '))"/>
                <xsl:call-template name="joinNext">
                    <xsl:with-param name="steps" select="$steps[position() != 1]"/>
                    <xsl:with-param name="nexts"
                                    select="(for $s in $snexts
                                             return if (not($s = $nexts)) then $s else (), $nexts)"/>
                </xsl:call-template>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template match="check:step" mode="copy">
        <xsl:param name="joins" as="node()*"/>
        <step>
            <xsl:apply-templates select="@*[name() != 'next']" mode="copy"/>
            <xsl:apply-templates select="@next" mode="copy">
                <xsl:with-param name="joins" select="$joins"/>
            </xsl:apply-templates>
        </step>
    </xsl:template>

    <xsl:template match="@*" mode="copy">
        <xsl:copy/>
    </xsl:template>

    <xsl:template match="@next" name="updateNext" mode="copy">
        <xsl:param name="joins" as="node()*"/>
        <xsl:param name="nexts" as="xsd:string*" select="tokenize(string(.),' ')"/>
        <xsl:attribute name="next"  separator=" " select="check:getNexts($joins,$nexts)"/>
    </xsl:template>

    <xsl:function name="check:getNexts" as="xsd:string*">
        <xsl:param name="joins" as="node()*"/>
        <xsl:param name="nexts" as="xsd:string*"/>
        <xsl:choose>
            <xsl:when test="empty($joins)">
                <xsl:sequence select="$nexts"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:variable name="join" select="$joins[1]" as="node()"/>
                <xsl:variable name="steps" select="tokenize($join/@steps,' ')"/>
                <xsl:variable name="next_out" as="xsd:string*">
                    <xsl:choose>
                        <xsl:when test="every $s in $steps satisfies $s=$nexts">
                            <xsl:for-each-group select="$nexts" group-by=". = $steps">
                                <xsl:choose>
                                    <xsl:when test=". = $steps">
                                        <xsl:value-of select="generate-id($join)"/>
                                    </xsl:when>
                                    <xsl:otherwise>
                                        <xsl:sequence select="current-group()"/>
                                    </xsl:otherwise>
                                </xsl:choose>
                            </xsl:for-each-group>
                        </xsl:when>
                        <xsl:otherwise>
                           <xsl:sequence select="$nexts"/>
                        </xsl:otherwise>
                    </xsl:choose>
                </xsl:variable>
                <xsl:sequence select="check:getNexts($joins[position() != 1],$next_out)"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:function>

    <xsl:template match="check:step[@next]" mode="getJoins">
        <xsl:param name="checker" as="node()"/>
        <!--
            This should be implemented by a customization layer. The
            template should take as input an entire checker document,
            and it should match on a single connected checker step.

            The template should return a <join /> element with an
            attribute @steps that contains the steps that may be
            joined. To this step.


            For example, if ID1, ID2, and ID3 may be joined, then the
            following should be emmited.

            <join xmlns="http://www.rackspace.com/repose/wadl/checker"
                  steps="ID1 ID2 ID3"/>
        -->
    </xsl:template>

    <!--
        A new step is created for every join. The step already
        contains an id and next attributes.  The purpose of this
        template is to generate all other attributes for the new step.

        The following parameters are passed in:

        checker     :  The entire checker document
        joins       :  The entire list of joins
        currentJoin :  The join that is associated with this step
        joinSteps   :  The list of steps associated with the currentJoin
    -->
    <xsl:template name="stepAttributes">
        <xsl:param name="checker" as="node()"/>
        <xsl:param name="joins" as="node()*"/>
        <xsl:param name="currentJoin" as="node()"/>
        <xsl:param name="joinSteps" as="node()*"/>
        <!--
           This should be implemented by a customization layer.
        -->
    </xsl:template>
</xsl:stylesheet>
