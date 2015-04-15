<?xml version="1.0" encoding="UTF-8"?>
<!--
  raxDevice.xsl

  This stylesheet is responsible for transforming rax:device attributes found in
  param elements in a WADL into rax:captureHeader attributes.

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
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:rax="http://docs.rackspace.com/api">

    <xsl:template match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>

    <xsl:template match="@rax:device[. = 'true' or . = '1']">
        <xsl:attribute name="rax:captureHeader">
            <xsl:value-of select="'X-DEVICE-ID'"/>
        </xsl:attribute>
    </xsl:template>
</xsl:stylesheet>