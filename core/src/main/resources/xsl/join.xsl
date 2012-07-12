<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xsd="http://www.w3.org/2001/XMLSchema"
    xmlns:check="http://www.rackspace.com/repose/wadl/checker"
    xmlns="http://www.rackspace.com/repose/wadl/checker"
    exclude-result-prefixes="xsd check"
    version="2.0">

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
                    <xsl:copy-of select="$checker//check:step"/>
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
        <xsl:variable name="excludes" as="xsd:string*">
            <xsl:sequence select="tokenize(string-join($joins//@steps, ' '), ' ')"/>
        </xsl:variable>

        <checker>
            <xsl:apply-templates select="$checker" mode="copy">
                <xsl:with-param name="joins" select="$joins"/>
                <xsl:with-param name="excludes" select="$excludes"/>
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
            <xsl:apply-templates select="$steps[1]/@*[not(name() = ('next','label','id'))]" mode="copy"/>
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
        <xsl:param name="excludes" as="xsd:string*"/>
        <xsl:choose>
            <xsl:when test="$excludes = @id"/>
            <xsl:otherwise>
                <step>
                    <xsl:apply-templates select="@*[name() != 'next']" mode="copy"/>
                    <xsl:apply-templates select="@next" mode="copy">
                        <xsl:with-param name="joins" select="$joins"/>
                    </xsl:apply-templates>
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
                <xsl:variable name="steps" select="tokenize($join/@steps,' ')"/>
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

    <xsl:template match="check:step[@next]" mode="getJoins">
        <xsl:param name="checker" as="node()"/>
        <xsl:variable name="nexts" as="xsd:string*" select="tokenize(@next,' ')"/>
        <xsl:variable name="nextStep" as="node()*" select="$checker//check:step[@id = $nexts]"/>
        <!-- Steps with the same @match, not versioned -->
        <xsl:for-each-group select="$nextStep[not(@version) and @match]" group-by="@type">
            <xsl:for-each-group select="current-group()" group-by="@match">
                <xsl:if test="count(current-group()) &gt; 1">
                    <join>
                        <xsl:attribute name="steps">
                            <xsl:value-of separator=" ">
                                <xsl:sequence select="current-group()/@id"/>
                            </xsl:value-of>
                        </xsl:attribute>
                    </join>
                </xsl:if>
            </xsl:for-each-group>
        </xsl:for-each-group>
        <!-- Versioned groups -->
        <xsl:for-each-group select="$nextStep[@version]" group-by="@type">
            <xsl:for-each-group select="current-group()" group-by="@match">
                <xsl:for-each-group select="current-group()" group-by="@version">
                    <xsl:if test="count(current-group()) &gt; 1">
                        <join>
                            <xsl:attribute name="steps">
                                <xsl:value-of separator=" ">
                                    <xsl:sequence select="current-group()/@id"/>
                                </xsl:value-of>
                            </xsl:attribute>
                        </join>
                    </xsl:if>
                </xsl:for-each-group>
            </xsl:for-each-group>
        </xsl:for-each-group>
        <!-- Steps with no @match -->
        <xsl:for-each-group select="$nextStep[not(@match)]" group-by="@type">
            <xsl:if test="count(current-group()) &gt; 1">
                <join>
                    <xsl:attribute name="steps">
                        <xsl:value-of separator=" ">
                            <xsl:sequence select="current-group()/@id"/>
                        </xsl:value-of>
                    </xsl:attribute>
                </join>
            </xsl:if>
        </xsl:for-each-group>
    </xsl:template>
</xsl:stylesheet>
