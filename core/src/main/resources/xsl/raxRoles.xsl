<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:wadl="http://wadl.dev.java.net/2009/02"
                xmlns:xsd="http://www.w3.org/2001/XMLSchema"
                xmlns:rax="http://docs.rackspace.com/api">
    <xsl:template match="node() | @*">
        <xsl:copy>
            <xsl:apply-templates select="@* | node()"/>
        </xsl:copy>
    </xsl:template>

    <xsl:template match="wadl:resource">
        <xsl:param name="roles" as="xsd:string*" select="()"/>

        <xsl:variable name="allRoles" as="xsd:string*">
            <xsl:sequence select="$roles"/>
            <xsl:if test="@rax:roles">
                <xsl:sequence select="tokenize(@rax:roles,' ')"/>
            </xsl:if>
        </xsl:variable>

        <xsl:copy>
            <xsl:apply-templates select="@* | node()">
                <xsl:with-param name="roles" select="$allRoles"/>
            </xsl:apply-templates>
        </xsl:copy>
    </xsl:template>

    <xsl:template match="wadl:method">
        <xsl:param name="roles" as="xsd:string*" select="()"/>

        <xsl:variable name="allRoles" as="xsd:string*">
          <xsl:sequence select="$roles"/>
          <xsl:if test="@rax:roles">
              <xsl:sequence select="tokenize(@rax:roles,' ')"/>
          </xsl:if>
        </xsl:variable>
        <xsl:copy>
          <xsl:apply-templates select="@*"/>
          <xsl:if test="not(wadl:request)">
              <wadl:request>
                  <xsl:call-template name="generateRoles">
                      <xsl:with-param name="roles" select="$allRoles"/>
                  </xsl:call-template>
              </wadl:request>
          </xsl:if>
          <xsl:apply-templates select="node()">
              <xsl:with-param name="roles" select="$allRoles"/>
          </xsl:apply-templates>
        </xsl:copy>
    </xsl:template>

    <xsl:template match="wadl:request">
        <xsl:param name="roles" as="xsd:string*" select="()"/>
        <xsl:copy>
            <xsl:apply-templates select="@*"/>
            <xsl:if test="count($roles) != 0">
               <xsl:call-template name="generateRoles">
                  <xsl:with-param name="roles" select="$roles"/>
               </xsl:call-template>
            </xsl:if>
           <xsl:apply-templates select="node()">
              <xsl:with-param name="roles" select="$roles"/>
           </xsl:apply-templates>
        </xsl:copy>
    </xsl:template>

    <xsl:template name="generateRoles">
        <xsl:param name="roles" as="xsd:string*" select="()"/>
        <xsl:for-each select="$roles">
            <wadl:param name="X-ROLES" style="header" rax:code="403" rax:message="You do not have access to this resource" type="xsd:string" required="true">
                <xsl:attribute name="fixed" select="."/>
            </wadl:param>
        </xsl:for-each>
    </xsl:template>
</xsl:stylesheet>
