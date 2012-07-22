<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:tst="http://www.rackspace.com/repose/wadl/checker/step/test"
    version="2.0">
    <!--
       Very simple XSL 2.0 template. Simply translates any message to
       a <tst:success ../> element.
    -->
    <xsl:template match="/">
        <tst:success didIt="true" currentTime="{current-time()}">Yup, that worked.</tst:success>
    </xsl:template>
</xsl:stylesheet>
