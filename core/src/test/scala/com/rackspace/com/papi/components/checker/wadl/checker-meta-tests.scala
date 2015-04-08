/***
 *   Copyright 2014 Rackspace US, Inc.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package com.rackspace.com.papi.components.checker.wadl

import scala.xml._

import java.io.File
import java.io.ByteArrayOutputStream

import javax.xml.transform.stream.StreamSource
import javax.xml.transform.stream.StreamResult

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.matchers.ShouldMatchers._

import scala.reflect.runtime.universe

import com.rackspace.com.papi.components.checker.Config

@RunWith(classOf[JUnitRunner])
class WADLCheckerMetaSpec extends BaseCheckerSpec {

  //
  //  Register some common prefixes, you'll need the for XPath
  //  assertions.
  //
  register ("xsd", "http://www.w3.org/2001/XMLSchema")
  register ("wadl","http://wadl.dev.java.net/2009/02")
  register ("chk","http://www.rackspace.com/repose/wadl/checker")


  feature ("The WADLCheckerBuilder can correctly set metadata") {

    type Dep  = (String, NodeSeq) /* (Base URL, XML Source */
    type WADL = (String, Dep)    /* Description (Base URL, WADL Source) */
    type Deps = List[Dep] /* A list of dependecies */

    val testWADLs : Map[WADL, Deps] = Map(
        ("WADL with no deps",
        ("test://path/to/test/mywadl.wadl",
         <application xmlns="http://wadl.dev.java.net/2009/02" xmlns:rax="http://docs.rackspace.com/api"
                   xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns:tst="test://schema/a">
        <grammars>
           <schema elementFormDefault="qualified"
                   attributeFormDefault="unqualified"
                   xmlns="http://www.w3.org/2001/XMLSchema"
                   xmlns:xsd="http://www.w3.org/2001/XMLSchema"
                   targetNamespace="test://schema/a">
              <simpleType name="yesno">
                 <restriction base="xsd:string">
                     <enumeration value="yes"/>
                     <enumeration value="no"/>
                 </restriction>
             </simpleType>
           </schema>
        </grammars>
        <resources base="https://test.api.openstack.com">
          <resource path="/a" rax:roles="a:admin">
            <method name="PUT" rax:roles="a:observer"/>
            <resource path="/b" rax:roles="b:creator">
              <method name="POST"/>
              <method name="PUT" rax:roles="b:observer"/>
              <method name="DELETE" rax:roles="b:observer b:admin"/>
              <method name="GET"  rax:roles="#all"/>
            </resource>
            <resource path="{yn}">
              <param name="yn" style="template" type="tst:yesno"/>
              <method name="POST"/>
              <method name="PUT" rax:roles="b:observer"/>
            </resource>
          </resource>
        </resources>
      </application>
      )) -> List(),
      ("WADL with schema reference in grammar",
      ("test://path/to/test/mywadl.wadl",
         <application xmlns="http://wadl.dev.java.net/2009/02" xmlns:rax="http://docs.rackspace.com/api"
                   xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns:tst="test://schema/a">
        <grammars>
           <include href="xsd/mytest.xsd"/>
        </grammars>
        <resources base="https://test.api.openstack.com">
          <resource path="/a" rax:roles="a:admin">
            <method name="PUT" rax:roles="a:observer"/>
            <resource path="/b" rax:roles="b:creator">
              <method name="POST"/>
              <method name="PUT" rax:roles="b:observer"/>
              <method name="DELETE" rax:roles="b:observer b:admin"/>
              <method name="GET"  rax:roles="#all"/>
            </resource>
            <resource path="{yn}">
              <param name="yn" style="template" type="tst:yesno"/>
              <method name="POST"/>
              <method name="PUT" rax:roles="b:observer"/>
            </resource>
          </resource>
        </resources>
      </application>
      )) -> List(("test://path/to/test/xsd/mytest.xsd",
                  <schema elementFormDefault="qualified"
                   attributeFormDefault="unqualified"
                   xmlns="http://www.w3.org/2001/XMLSchema"
                   xmlns:xsd="http://www.w3.org/2001/XMLSchema"
                   targetNamespace="test://schema/a">
              <simpleType name="yesno">
                 <restriction base="xsd:string">
                     <enumeration value="yes"/>
                     <enumeration value="no"/>
                 </restriction>
             </simpleType>
           </schema>)),
      ("WADL with schema reference in grammar. The schema itself includes a reference.",
      ("test://path/to/test/mywadl.wadl",
         <application xmlns="http://wadl.dev.java.net/2009/02" xmlns:rax="http://docs.rackspace.com/api"
                   xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns:tst="test://schema/a">
        <grammars>
           <include href="xsd/mytest.xsd"/>
        </grammars>
        <resources base="https://test.api.openstack.com">
          <resource path="/a" rax:roles="a:admin">
            <method name="PUT" rax:roles="a:observer"/>
            <resource path="/b" rax:roles="b:creator">
              <method name="POST"/>
              <method name="PUT" rax:roles="b:observer">
                 <request>
                   <representation mediaType="application/xml">
                       <rax:preprocess href="foo.xsl"/>
                   </representation>
                 </request>
              </method>
              <method name="DELETE" rax:roles="b:observer b:admin"/>
              <method name="GET"  rax:roles="#all"/>
            </resource>
            <resource path="{yn}">
              <param name="yn" style="template" type="tst:yesno"/>
              <method name="POST">
                 <request>
                   <representation mediaType="application/xml">
                       <rax:preprocess href="bar.xsl"/>
                   </representation>
                 </request>
              </method>
              <method name="PUT" rax:roles="b:observer"/>
            </resource>
          </resource>
        </resources>
      </application>
      )) -> List(("test://path/to/test/xsd/mytest.xsd",
                  <schema elementFormDefault="qualified"
                   attributeFormDefault="unqualified"
                   xmlns="http://www.w3.org/2001/XMLSchema"
                   xmlns:xsd="http://www.w3.org/2001/XMLSchema"
                   targetNamespace="test://schema/a">
                  <include schemaLocation="mytest2.xsd"/>
                  </schema>),
                 ("test://path/to/test/util.xsl",
                  <xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                  xpath-default-namespace="http://www.datypic.com/books/ns">
                  <xsl:output method="html"/>
                  </xsl:stylesheet>),
                 ("test://path/to/test/foo.xsl",
                  <xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                  xpath-default-namespace="http://www.datypic.com/books/ns">
                  <xsl:output method="html"/>
                  <xsl:import href="util.xsl"/>
                  </xsl:stylesheet>),
                 ("test://path/to/test/bar.xsl",
                  <xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                  xpath-default-namespace="http://www.datypic.com/books/ns">
                  <xsl:output method="html"/>
                  <xsl:import href="util.xsl"/>
                  <xsl:import-schema namespace="test://schema/a"
                     schema-location="xsd/mytest2.xsd"/>
                  </xsl:stylesheet>),
               ("test://path/to/test/xsd/mytest2.xsd",
                  <schema elementFormDefault="qualified"
                   attributeFormDefault="unqualified"
                   xmlns="http://www.w3.org/2001/XMLSchema"
                   xmlns:xsd="http://www.w3.org/2001/XMLSchema"
                   targetNamespace="test://schema/a">
              <simpleType name="yesno">
                 <restriction base="xsd:string">
                     <enumeration value="yes"/>
                     <enumeration value="no"/>
                 </restriction>
             </simpleType>
           </schema>
              )),
      ("WADL with inline XSD grammar. The schema includes a reference.",
      ("test://path/to/test/mywadl.wadl",
         <application xmlns="http://wadl.dev.java.net/2009/02" xmlns:rax="http://docs.rackspace.com/api"
                   xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns:tst="test://schema/a">
        <grammars>
             <schema elementFormDefault="qualified"
                   attributeFormDefault="unqualified"
                   xmlns="http://www.w3.org/2001/XMLSchema"
                   xmlns:xsd="http://www.w3.org/2001/XMLSchema"
                   targetNamespace="test://schema/a">
                  <include schemaLocation="xsd/mytest2.xsd"/>
             </schema>
        </grammars>
        <resources base="https://test.api.openstack.com">
          <resource path="/a" rax:roles="a:admin">
            <method name="PUT" rax:roles="a:observer"/>
            <resource path="/b" rax:roles="b:creator">
              <method name="POST"/>
              <method name="PUT" rax:roles="b:observer"/>
              <method name="DELETE" rax:roles="b:observer b:admin"/>
              <method name="GET"  rax:roles="#all"/>
            </resource>
            <resource path="{yn}">
              <param name="yn" style="template" type="tst:yesno"/>
              <method name="POST"/>
              <method name="PUT" rax:roles="b:observer"/>
            </resource>
          </resource>
        </resources>
      </application>
      )) -> List(("test://path/to/test/xsd/mytest2.xsd",
                  <schema elementFormDefault="qualified"
                   attributeFormDefault="unqualified"
                   xmlns="http://www.w3.org/2001/XMLSchema"
                   xmlns:xsd="http://www.w3.org/2001/XMLSchema"
                   targetNamespace="test://schema/a">
              <simpleType name="yesno">
                 <restriction base="xsd:string">
                     <enumeration value="yes"/>
                     <enumeration value="no"/>
                 </restriction>
             </simpleType>
           </schema>
              )),
      ("WADL with schema reference in grammar. The schema itself includes a reference, the other schema imports one.",
      ("test://path/to/test/mywadl.wadl",
         <application xmlns="http://wadl.dev.java.net/2009/02" xmlns:rax="http://docs.rackspace.com/api"
                   xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns:tst="test://schema/a">
        <grammars>
           <include href="xsd/mytest.xsd"/>
        </grammars>
        <resources base="https://test.api.openstack.com">
          <resource path="/a" rax:roles="a:admin">
            <method name="PUT" rax:roles="a:observer"/>
            <resource path="/b" rax:roles="b:creator">
              <method name="POST"/>
              <method name="PUT" rax:roles="b:observer"/>
              <method name="DELETE" rax:roles="b:observer b:admin"/>
              <method name="GET"  rax:roles="#all"/>
            </resource>
            <resource path="{yn}">
              <param name="yn" style="template" type="tst:yesno"/>
              <method name="POST"/>
              <method name="PUT" rax:roles="b:observer"/>
            </resource>
          </resource>
        </resources>
      </application>
      )) -> List(("test://path/to/test/xsd/mytest.xsd",
                  <schema elementFormDefault="qualified"
                   attributeFormDefault="unqualified"
                   xmlns="http://www.w3.org/2001/XMLSchema"
                   xmlns:xsd="http://www.w3.org/2001/XMLSchema"
                   targetNamespace="test://schema/a">
                  <include schemaLocation="mytest2.xsd"/>
                  </schema>),
               ("test://path/to/test/xsd/mytest2.xsd",
                  <schema elementFormDefault="qualified"
                   attributeFormDefault="unqualified"
                   xmlns="http://www.w3.org/2001/XMLSchema"
                   xmlns:xsd="http://www.w3.org/2001/XMLSchema"
                   targetNamespace="test://schema/a">
                 <import namespace="test://schema/b" schemaLocation="mytest3.xsd"/>
              <simpleType name="yesno">
                 <restriction base="xsd:string">
                     <enumeration value="yes"/>
                     <enumeration value="no"/>
                 </restriction>
             </simpleType>
           </schema>
              ),
             ("test://path/to/test/xsd/mytest3.xsd",
                  <schema elementFormDefault="qualified"
                   attributeFormDefault="unqualified"
                   xmlns="http://www.w3.org/2001/XMLSchema"
                   xmlns:xsd="http://www.w3.org/2001/XMLSchema"
                   targetNamespace="test://schema/b">
              <simpleType name="yes">
                 <restriction base="xsd:string">
                     <enumeration value="yes"/>
                 </restriction>
             </simpleType>
           </schema>
              )),
        ("WADL referencing resource type in another WADL",
        ("test://path/to/test/mywadl.wadl",
         <application xmlns="http://wadl.dev.java.net/2009/02" xmlns:rax="http://docs.rackspace.com/api"
                   xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns:tst="test://schema/a">
        <grammars>
           <schema elementFormDefault="qualified"
                   attributeFormDefault="unqualified"
                   xmlns="http://www.w3.org/2001/XMLSchema"
                   xmlns:xsd="http://www.w3.org/2001/XMLSchema"
                   targetNamespace="test://schema/a">
              <simpleType name="yesno">
                 <restriction base="xsd:string">
                     <enumeration value="yes"/>
                     <enumeration value="no"/>
                 </restriction>
             </simpleType>
           </schema>
        </grammars>
        <resources base="https://test.api.openstack.com">
          <resource path="/a" type="other.wadl#methods" rax:roles="a:admin">
            <resource path="/b" rax:roles="b:creator">
              <method name="POST"/>
              <method name="PUT" rax:roles="b:observer"/>
              <method name="DELETE" rax:roles="b:observer b:admin"/>
              <method name="GET"  rax:roles="#all"/>
            </resource>
            <resource path="{yn}">
              <param name="yn" style="template" type="tst:yesno"/>
              <method name="POST"/>
              <method name="PUT" rax:roles="b:observer"/>
            </resource>
          </resource>
        </resources>
      </application>
      )) -> List(("test://path/to/test/other.wadl",
         <application xmlns="http://wadl.dev.java.net/2009/02" xmlns:rax="http://docs.rackspace.com/api"
                   xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns:tst="test://schema/a">
        <resource_type id="methods">
            <method name="PUT" rax:roles="a:observer"/>
        </resource_type>
      </application>)),
      ("WADL referencing resource type in another WADL, which references a method in another",
        ("test://path/to/test/mywadl.wadl",
         <application xmlns="http://wadl.dev.java.net/2009/02" xmlns:rax="http://docs.rackspace.com/api"
                   xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns:tst="test://schema/a">
        <grammars>
           <schema elementFormDefault="qualified"
                   attributeFormDefault="unqualified"
                   xmlns="http://www.w3.org/2001/XMLSchema"
                   xmlns:xsd="http://www.w3.org/2001/XMLSchema"
                   targetNamespace="test://schema/a">
              <simpleType name="yesno">
                 <restriction base="xsd:string">
                     <enumeration value="yes"/>
                     <enumeration value="no"/>
                 </restriction>
             </simpleType>
           </schema>
        </grammars>
        <resources base="https://test.api.openstack.com">
          <resource path="/a" type="other.wadl#methods" rax:roles="a:admin">
            <resource path="/b" rax:roles="b:creator">
              <method name="POST"/>
              <method name="PUT" rax:roles="b:observer"/>
              <method name="DELETE" rax:roles="b:observer b:admin"/>
              <method name="GET"  rax:roles="#all"/>
            </resource>
            <resource path="{yn}">
              <param name="yn" style="template" type="tst:yesno"/>
              <method name="POST"/>
              <method name="PUT" rax:roles="b:observer"/>
            </resource>
          </resource>
        </resources>
      </application>
      )) -> List(("test://path/to/test/other.wadl",
         <application xmlns="http://wadl.dev.java.net/2009/02" xmlns:rax="http://docs.rackspace.com/api"
                   xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns:tst="test://schema/a">
        <resource_type id="methods">
            <method href="other2.wadl#PUTMethod"/>
        </resource_type>
      </application>),
                 ("test://path/to/test/other2.wadl",
         <application xmlns="http://wadl.dev.java.net/2009/02" xmlns:rax="http://docs.rackspace.com/api"
                   xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns:tst="test://schema/a">
            <method id="PUTMethod" name="PUT" rax:roles="a:observer"/>
      </application>)),
      ("WADL referencing an XSLT via rax:preprocess",
      ("test://path/to/test/mywadl.wadl",
        <application xmlns="http://wadl.dev.java.net/2009/02"
                     xmlns:xsd="http://www.w3.org/2001/XMLSchema"
                     xmlns:rax="http://docs.rackspace.com/api">
             <resources base="https://test.api.openstack.com">
                 <resource path="a/b">
                     <resource path="c">
                        <method name="POST">
                            <request>
                                <representation mediaType="application/xml">
                                    <rax:preprocess href="xsl/beginStart.xsl"/>
                                </representation>
                            </request>
                        </method>
                     </resource>
                 </resource>
             </resources>
        </application>)) -> List(("test://path/to/test/xsl/beginStart.xsl",
               <xsl:stylesheet
               xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
               xmlns:tst="http://www.rackspace.com/repose/wadl/checker/step/test"
               xmlns="http://www.rackspace.com/repose/wadl/checker/step/test"
               version="1.0">

               <xsl:template match="node() | @*">
                 <xsl:copy>
                   <xsl:apply-templates select="@* | node()"/>
                 </xsl:copy>
               </xsl:template>

               <xsl:template match="tst:stepType">
                 <xsl:choose>
                   <xsl:when test=". = 'BEGIN'">
                     <stepType>START</stepType>
                   </xsl:when>
                   <xsl:otherwise>
                     <stepType><xsl:value-of select="."/></stepType>
                   </xsl:otherwise>
                 </xsl:choose>
               </xsl:template>

               </xsl:stylesheet>))
    )

    type WADLFile = (String, String, List[String]) /* (Description, File path, list of dependecies */

    val baseSamples = "src/test/test-samples"
    val testFiles : List[WADLFile] = List(
        ("WADL with a common entity", s"$baseSamples/entity.wadl.xml", List(s"$baseSamples/common.ent")),
        ("WADL which links to a wadl with a common entity", s"$baseSamples/entity2.wadl.xml", List(s"$baseSamples/entity2-method.wadl.xml",
                                                                                                   s"$baseSamples/common.ent")),
        ("WADL with samples and common entity", s"$baseSamples/entity-withsamples.wadl.xml", List(s"$baseSamples/samples/metadata_item.xml",
                                                                                                  s"$baseSamples/samples/metadata_item.json",
                                                                                                  s"$baseSamples/common.ent")),
        ("WADL with a common entity and JSON Schema", s"$baseSamples/entity-json-grammar.wadl.xml", List(s"$baseSamples/common.ent",
                                                                                                         s"$baseSamples/test-schema.json")),
        ("WADL with an Xinclude that points to common entity", s"$baseSamples/xinclude-entity.wadl.xml", List(s"$baseSamples/xinclude-method.wadl.xml",
                                                                                                               s"$baseSamples/common.ent")),
        ("A WADL with an xinclude, that includes an xinclude, that points to a common entity",
             s"$baseSamples/xinclude-entity2.wadl.xml", List(s"$baseSamples/xinclude-method2.wadl.xml", s"$baseSamples/xinclude-method.wadl.xml",
                                                             s"$baseSamples/common.ent")))

    def clearConfig (c : Config) : Config = {
      c.removeDups = false
      c.validateChecker = true /* We should always validate */
      c.checkWellFormed = false
      c.checkXSDGrammar = false
      c.doXSDGrammarTransform = false
      c.checkJSONGrammar = false
      c.checkElements = false
      c.checkPlainParams = false
      c.enablePreProcessExtension = false
      c.enableIgnoreXSDExtension = false
      c.enableIgnoreJSONSchemaExtension = false
      c.enableMessageExtension = false
      c.enableRaxRolesExtension = false
      c.maskRaxRoles403 = false
      c.joinXPathChecks = false
      c.checkHeaders = false
      c.preserveRequestBody = false

      c
    }

    type MetaTest = (String, Config, String) /* Desc, Config, XPath assertion */
    type MetaTests = List[MetaTest]
    type Assertion = (String, String) /* Desc, XPath assertion */
    type Assertions = List[Assertion]

    def setConfig (setter : (Config) => Unit) : Config = {
      val conf = clearConfig(new Config)
      setter(conf)
      conf
    }

    def setBoolConfig(name : String, value : Boolean) : Config = {
      val conf = clearConfig(new Config)
      val conf_ref = universe.runtimeMirror(conf.getClass.getClassLoader).reflect(conf)
      val cType = universe.typeOf[Config]
      conf_ref.reflectField(cType.member(universe.newTermName(name)).asTerm).set(value)
      conf
    }

    def checkConfigOptionMatch(option : String, value: String, setter : (Config) => Unit) : MetaTest = {
      (s"Metadata $option  $value", setConfig(setter),
       s"/chk:checker/chk:meta/chk:config[@option='$option' and @value='$value']")
    }

    //
    //  We can generate tests for boolean options.
    //
    val boolOptionNames : List[String] = (new Config).checkerMetaMap.filter(c => c._2.isInstanceOf[Boolean]).map(c => c._1).toList

    val trueTests : MetaTests = boolOptionNames.map(option => (s"Metadata $option check should be true if set", setBoolConfig(option, true),
                                                               s"/chk:checker/chk:meta/chk:config[@option='$option' and @value='true']"))
    val falseTests : MetaTests = boolOptionNames.map(option => (s"Metadata $option check should be false if not set", setBoolConfig(option, false),
                                                               s"not(/chk:checker/chk:meta/chk:config[@option='$option']) or /chk:checker/chk:meta/chk:config[@option='$option' and @value='false']"))

    //
    // Test for non-boolean config flagsconfiguration flags
    //
    val nonBoolTests :  MetaTests =
      List(
           checkConfigOptionMatch("xpathVersion","1",(c : Config) => {c.joinXPathChecks=true; c.xpathVersion=1}),
           checkConfigOptionMatch("xpathVersion","2",(c : Config) => {c.joinXPathChecks=true; c.xpathVersion=2})
         )

    val optionTests : MetaTests = trueTests ++ falseTests ++ nonBoolTests

    //
    //  These assertions should hold true, regardress of configuration.
    //
    val standardAssertions : Assertions =
      List(("Metadata should contain a user","not(empty(/chk:checker/chk:meta/chk:built-by))"),
           ("Metadata should cantain a creator","not(empty(/chk:checker/chk:meta/chk:created-by))"),
           ("Metadata should cantain a created on date","not(empty(/chk:checker/chk:meta/chk:created-on))"))

    val standardTests : MetaTests = for {t <- optionTests
                                         sa <- standardAssertions}
                                    yield (sa._1+" with "+t._1, t._2, sa._2)

    val allConfigTests = optionTests ++ standardTests

    //
    // Let's make sure all configs are accounted for in our tests
    //
    val allOptions = (new Config).checkerMetaMap.keys
    allOptions.foreach (o => assert(allConfigTests.exists (t => t._1.contains(o)),s"No meta test found for $o option!"))

    //
    // Generate the tests...
    //
    for ( t <- allConfigTests ) {
      for ((w, deps) <- testWADLs) {
        scenario ("The checker for "+w._1+" contains Metadata and "+t._1) {
          for (dep <- deps) {
            register(dep)
          }
          val checker = builder.build (w._2, t._2)
          assert(checker, t._3)
          for (dep <- w._2 +: deps) {
            val durl = dep._1
            assert(checker, s"exactly-one(/chk:checker/chk:meta/chk:created-from[. = '$durl'])")
          }
        }
      }

      for (fw <- testFiles) {
        scenario ("The checker for "+fw._1+" contains Metadata and "+t._1) {
          val checkerOutBytes = new ByteArrayOutputStream()
          builder.build (new StreamSource(new File(fw._2)), new StreamResult(checkerOutBytes), t._2)
          val checker = XML.loadString(checkerOutBytes.toString())
          for (dep <- fw._2 +: fw._3) {
            assert(checker, s"exactly-one(/chk:checker/chk:meta/chk:created-from[contains(.,'$dep')])")
          }
        }
      }
    }
  }
}
