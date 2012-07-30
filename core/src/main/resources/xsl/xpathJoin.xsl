<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xsd="http://www.w3.org/2001/XMLSchema"
    xmlns:check="http://www.rackspace.com/repose/wadl/checker"
    xmlns:xslout="http://www.rackspace.com/repose/wadl/checker/Transform"
    xmlns="http://www.rackspace.com/repose/wadl/checker"
    exclude-result-prefixes="xsd check"
    version="2.0">

    <xsl:namespace-alias stylesheet-prefix="xslout" result-prefix="xsl"/>

    <xsl:output indent="yes" method="xml"/>

    <xsl:template match="check:checker" name="joinXPath">
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
                    <xsl:copy-of select="$checker//check:step"/>
                </checker>
            </xsl:when>
            <xsl:otherwise>
                <xsl:call-template name="joinXPath">
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
        <xsl:variable name="excludes" as="xsd:string*">
            <xsl:sequence select="$joins/@id" />
            <xsl:sequence select="tokenize(string-join($joins//@steps, ' '), ' ')"/>
        </xsl:variable>

        <checker>
            <xsl:apply-templates select="$checker" mode="copy">
                <xsl:with-param name="joins" select="$joins"/>
                <xsl:with-param name="excludes" select="$excludes"/>
            </xsl:apply-templates>
            <xsl:apply-templates select="$joins" mode="join">
                <xsl:with-param name="checker" select="$checker"/>
            </xsl:apply-templates>
        </checker>
    </xsl:template>

    <xsl:template match="check:step" mode="copy">
        <xsl:param name="joins" as="node()*"/>
        <xsl:param name="excludes" as="xsd:string*"/>
        <xsl:choose>
            <xsl:when test="$excludes = @id"/>
            <xsl:otherwise>
                <step>
                    <xsl:apply-templates select="@*[name() != 'next']" mode="copy"/>
                    <xsl:apply-templates select="@next" mode="copy">
                        <xsl:with-param name="joins" select="$joins"/>
                    </xsl:apply-templates>
                    <xsl:copy-of select="./*"/>
                </step>
            </xsl:otherwise>
        </xsl:choose>
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
                <xsl:variable name="steps" select="(tokenize($join/@steps,' '), $join/@id)"/>
                <xsl:variable name="next_out" as="xsd:string*">
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
                </xsl:variable>
                <xsl:sequence select="check:getNexts($joins[position() != 1],$next_out)"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:function>

    <xsl:template match="check:join" mode="join">
        <xsl:param name="checker" as="node()"/>
        <xsl:variable name="rootID" as="xsd:string" select="@id"/>
        <xsl:variable name="joinSteps" as="xsd:string*" select="tokenize(@steps,' ')"/>
        <xsl:variable name="steps" as="node()*" select="$checker//check:step[@id = $joinSteps]"/>
        <xsl:variable name="root" as="node()" select="$checker//check:step[@id = $rootID]"/>

        <step id="{generate-id(.)}" type="XSL" version="1">
            <xsl:attribute name="next" select="check:joinNext($root, $steps)" separator=" "/>
            <xslout:transform version="1.0" check:mergable="true">
                <xslout:template match="/">
                    <xsl:if test="$root/@type = 'XSL'">
                        <xsl:copy-of select="$root/xsl:transform/xsl:template[@match='/']/xsl:choose"/>
                    </xsl:if>
                    <xslout:choose>
                        <xsl:apply-templates select="$steps" mode="join"/>
                        <xslout:otherwise>
                            <xslout:message terminate="yes">
                                <xsl:text>Expecting: </xsl:text>
                                <xsl:value-of select="$steps[@type='XPATH']/@match" separator=" or "/>
                            </xslout:message>
                        </xslout:otherwise>
                    </xslout:choose>

                    <xslout:copy>
                        <xslout:apply-templates select="@* | node()"/>
                    </xslout:copy>
                </xslout:template>

                <xslout:template match="node() | @*">
                    <xslout:copy>
                        <xslout:apply-templates select="@* | node()"/>
                    </xslout:copy>
                </xslout:template>
            </xslout:transform>
        </step>
    </xsl:template>

    <xsl:template match="check:step[@type='XPATH']" mode="join">
        <xslout:when test="{@match}"/>
    </xsl:template>

    <xsl:function name="check:joinNext" as="xsd:string*">
        <xsl:param name="root" as="node()"/>
        <xsl:param name="steps" as="node()*"/>
        <xsl:variable name="rnexts" as="xsd:string*" select="tokenize($root/@next, ' ')"/>
        <xsl:variable name="uniqueNexts" as="xsd:string*"
                      select="for $n in $rnexts return if ($steps[@id =$n]) then () else $n"/>
        <xsl:value-of select="check:joinMerge($steps,$uniqueNexts)"/>
    </xsl:function>

    <xsl:function name="check:joinMerge" as="xsd:string*">
        <xsl:param name="steps" as="node()*"/>
        <xsl:param name="nexts" as="xsd:string*"/>

        <xsl:choose>
            <xsl:when test="empty($steps)">
                <xsl:value-of select="$nexts"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:variable name="snexts" as="xsd:string*" select="tokenize($steps[1]/@next, ' ')"/>
                <xsl:value-of select="check:joinMerge ($steps[position() != 1], (for $s in $snexts
                                       return if (not ($s = $nexts)) then $s else (), $nexts))"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:function>

    <xsl:template match="check:step[@type=('XSL', 'WELL_XML')]" mode="getJoins">
        <xsl:param name="checker" as="node()"/>
        <xsl:variable name="nexts" as="xsd:string*" select="tokenize(@next,' ')"/>
        <xsl:variable name="nextStep" as="node()*" select="$checker//check:step[@id = $nexts and @type='XPATH']"/>

        <xsl:if test="not(empty($nextStep))">
            <xsl:choose>
                <xsl:when test="@type='XSL' and not(xsl:transform/@check:mergable)"/>
                <xsl:otherwise>
                    <join id="{@id}" type="{@type}">
                        <xsl:attribute name="steps">
                            <xsl:value-of select="$nextStep/@id" separator=' '/>
                        </xsl:attribute>
                    </join>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:if>
    </xsl:template>

</xsl:stylesheet>
