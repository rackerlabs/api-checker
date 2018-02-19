<?xml version="1.0" encoding="UTF-8"?>
<!--
   wadl-asserts.xsl

   This stylesheet simply copies over a normalized wadl and displays
   an error if assertions fail. It is always run - even when
   validation is disabled because stages later in the pipeline expect
   a valid WADL.

   Technically the assertions checks here may be better placed in the
   wadl-tools project, but they are here because:

   1. It is convenient.
   2. It is a good placeholder for api-checker specific checks in the
   future.

   Copyright 2018 Rackspace US, Inc.

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
<xsl:transform xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
               xmlns:wadl="http://wadl.dev.java.net/2009/02"
               version="3.0">

    <xsl:mode on-no-match="shallow-copy"/>

    <xsl:template match="wadl:method[not(@href) and not(@name)]">
        <xsl:message terminate="yes">[ERROR] The method <xsl:copy-of select="."/> requires a name.</xsl:message>
    </xsl:template>
</xsl:transform>
