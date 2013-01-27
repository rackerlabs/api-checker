<!--
   pruneSteps.xsl

   This stylesheet exposes a template named util:pruneSteps.  The
   template takes a checker document as input and outputs only those
   steps that are not connected to other steps.

   The process is executed recursively until all disconnected states
   are pruned.

   For example, assuming that S is the start state, this document:

              +===+
   +===+   /==+ Y |
   | X +===   +===+
   +===+             +===+
                    /+ B |
   +===+    +===+ /= +===+
   | S +====+ A +=
   +===+    +===+ \= +===+
                    \+ C |
                     +===+

          +===+
          | Z |
          +===+

   Will simply become:

                     +===+
                    /+ B |
   +===+    +===+ /= +===+
   | S +====+ A +=
   +===+    +===+ \= +===+
                    \+ C |
                     +===+

   Note that this is not an optimization, because technically a
   checker document with disconnected states is an illegal
   document. That said, the prune states method may be called by other
   optimization to tidy things up.
-->
<xsl:stylesheet
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xsd="http://www.w3.org/2001/XMLSchema"
    xmlns:wadl="http://wadl.dev.java.net/2009/02"
    xmlns:check="http://www.rackspace.com/repose/wadl/checker"
    xmlns:rax="http://docs.rackspace.com/api"
    xmlns:util="http://www.rackspace.com/repose/wadl/checker/util"
    xmlns="http://www.rackspace.com/repose/wadl/checker"
    exclude-result-prefixes="xsd wadl rax check util"
    version="2.0">

    <xsl:template name="util:pruneSteps">
        <xsl:param name="checker" as="node()"/>
        <xsl:variable name="nexts" as="xsd:string*" select="tokenize(string-join($checker//check:step/@next,' '),' ')"/>
        <xsl:variable name="connected" as="xsd:integer" select="count($checker//check:step[$nexts = @id])"/>
        <xsl:variable name="all" as="xsd:integer" select="count($checker//check:step[@type != 'START'])"/>
        <xsl:choose>
            <xsl:when test="$connected = $all">
                <checker>
                    <xsl:copy-of select="$checker//check:step"/>
                </checker>
            </xsl:when>
            <xsl:otherwise>
                <xsl:call-template name="util:pruneSteps">
                    <xsl:with-param name="checker">
                        <checker>
                            <xsl:apply-templates select="$checker" mode="util:pruneSteps">
                                <xsl:with-param name="nexts" select="$nexts"/>
                            </xsl:apply-templates>
                        </checker>
                    </xsl:with-param>
                </xsl:call-template>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template match="check:step" mode="util:pruneSteps">
        <xsl:param name="nexts" as="xsd:string*"/>
        <xsl:choose>
            <xsl:when test="(@id = $nexts) or (@type='START')">
                <xsl:copy-of select="."/>
            </xsl:when>
            <xsl:otherwise>
                <!-- pruned -->
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

</xsl:stylesheet>
