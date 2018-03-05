<?xml version="1.0" encoding="UTF-8"?>
<!--
   checker2info.xsl

   This stylesheet is simply responsible for displaying the metadata
   associated with a checker as plain text.  It is meant for
   logging/informational purposes.

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
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    xmlns:chk="http://www.rackspace.com/repose/wadl/checker"
    version="3.0" expand-text="yes">

    <xsl:mode on-no-match="shallow-skip"/>
    <xsl:output method="text" encoding="UTF-8"/>

    <xsl:variable name="createdFrom" as="xs:string*" select="sort(/chk:checker/chk:meta/chk:created-from)"/>
    <xsl:variable name="options" as="xs:string*" select="sort(/chk:checker/chk:meta/chk:config/@option)"/>

    <xsl:template match="chk:meta">
Checker Metadata:

Built By:     {chk:built-by}
Created By:   {chk:created-by}
Created On:   {chk:created-on}
Created From: {$createdFrom[1]}
 {for $cf in $createdFrom[position() != 1] return '             ' || $cf || '&#xa;'}
Config Settings: {for $o in $options[1] return $o || ' = ' || chk:config[@option=$o]/@value}
 {for $o in $options[position() != 1] return '                ' || $o || ' = ' || chk:config[@option=$o]/@value ||'&#xa;'}
</xsl:template>
</xsl:stylesheet>
