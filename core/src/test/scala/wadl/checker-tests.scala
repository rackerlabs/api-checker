package com.rackspace.com.papi.components.checker.wadl

import scala.xml._
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.matchers.ShouldMatchers._

import com.rackspace.com.papi.components.checker.TestConfig

@RunWith(classOf[JUnitRunner])
class WADLCheckerSpec extends BaseCheckerSpec {

  //
  //  Register some common prefixes, you'll need the for XPath
  //  assertions.
  //
  register ("xsd", "http://www.w3.org/2001/XMLSchema")
  register ("wadl","http://wadl.dev.java.net/2009/02")
  register ("chk","http://www.rackspace.com/repose/wadl/checker")

  feature ("The WADLCheckerBuilder can correctly transforma a WADL into checker format") {

    info ("As a developer")
    info ("I want to be able to transform a WADL which references multiple XSDs into a ")
    info ("a description of a machine that can validate the API in checker format")
    info ("so that an API validator can process the checker format to validate the API")

    scenario("The WADL does not contain any resources") {
      given("a WADL with no resources")
      val inWADL =
        <application xmlns="http://wadl.dev.java.net/2009/02">
           <grammars/>
           <resources base="https://test.api.openstack.com">
              <resource/>
           </resources>
        </application>
      when("the wadl is translated")
      val checker = builder.build (inWADL, stdConfig)
      then("The checker should contain a single start node")
      assert (checker, "count(//chk:step[@type='START']) = 1")
      and("The only steps accessible from start should be the fail states")
      val path = allStepsFromStart(checker)
      assert (path, "count(//chk:step) = 3")
      assert (path, "/chk:checker/chk:step[@type='START']")
      assert (path, "/chk:checker/chk:step[@type='METHOD_FAIL']")
      assert (path, "/chk:checker/chk:step[@type='URL_FAIL']")
      and("There should exist a direct path from start to each failed state")
      assert (checker, Start, URLFail)
      assert (checker, Start, MethodFail)
    }


    //
    //  The following scenarios test a single resource located at
    //  /path/to/my/resource with a GET, DELETE, and POST method. They are
    //  equivalent but they are written in slightly different WADL
    //  form the assertions below must apply to all of them.
    //  Only application/xml is allowed in the POST operation
    //
    //

    def singlePathAssertions (checker : NodeSeq) : Unit = {
      then("The checker should contain an URL node for each path step")
      assert (checker, "count(/chk:checker/chk:step[@type='URL']) = 4")
      and ("The checker should contain a GET, DELETE, and POST method")
      assert (checker, "/chk:checker/chk:step[@type='METHOD' and @match='GET']")
      assert (checker, "/chk:checker/chk:step[@type='METHOD' and @match='DELETE']")
      assert (checker, "/chk:checker/chk:step[@type='METHOD' and @match='POST']")
      and ("The path from the start should contain all URL nodes in order")
      and ("it should end in the GET and a DELETE method node")
      assert (checker, Start, URL("path"), URL("to"), URL("my"), URL("resource"), Method("GET"))
      assert (checker, Start, URL("path"), URL("to"), URL("my"), URL("resource"), Method("DELETE"))
      assert (checker, Start, URL("path"), URL("to"), URL("my"), URL("resource"), Method("POST"), ReqType("application/xml"))
      and ("The Start state and each URL state should contain a path to MethodFail and URLFail")
      assert (checker, Start, URLFail)
      assert (checker, Start, MethodFail)
      assert (checker, URL("path"), URLFail)
      assert (checker, URL("path"), MethodFail)
      assert (checker, URL("to"), URLFail)
      assert (checker, URL("to"), MethodFail)
      assert (checker, URL("my"), URLFail)
      assert (checker, URL("my"), MethodFail)
      assert (checker, URL("resource"), URLFail)
      assert (checker, URL("resource"), MethodFail)
      and ("The POST method should contain an ReqTypeFail")
      assert (checker, Method("POST"), ReqTypeFail)
    }

    scenario("The WADL contains a single multi-path resource") {
      given("a WADL that contains a single multi-path resource with a GET, DELETE, and POST method")
      val inWADL =
        <application xmlns="http://wadl.dev.java.net/2009/02">
           <grammars/>
           <resources base="https://test.api.openstack.com">
              <resource path="path/to/my/resource">
                   <method name="GET">
                      <response status="200 203"/>
                   </method>
                   <method name="DELETE">
                      <response status="200"/>
                   </method>
                   <method name="POST">
                      <request>
                         <representation mediaType="application/xml"/>
                      </request>
                      <response status="200"/>
                   </method>
              </resource>
           </resources>
        </application>
      when("the wadl is translated")
      val checker = builder.build (inWADL, stdConfig)
      singlePathAssertions(checker)
    }

    ignore("The WADL contains a single multi-path resource ending in /") {
      given("a WADL that contains a single multi-path resource with a GET, DELETE, and POST method and ending in /")
      val inWADL =
        <application xmlns="http://wadl.dev.java.net/2009/02">
           <grammars/>
           <resources base="https://test.api.openstack.com">
              <resource path="path/to/my/resource/">
                   <method name="GET">
                      <response status="200 203"/>
                   </method>
                   <method name="DELETE">
                      <response status="200"/>
                   </method>
                   <method name="POST">
                      <request>
                         <representation mediaType="application/xml"/>
                      </request>
                      <response status="200"/>
                   </method>
              </resource>
           </resources>
        </application>
      when("the wadl is translated")
      val checker = builder.build (inWADL, stdConfig)
      singlePathAssertions(checker)
    }

    scenario("The WADL contains a single multi-path resource in tree form") {
      given("a WADL that contains a single multi-path resource in tree form with a GET, DELETE, and POST method")
      val inWADL =
        <application xmlns="http://wadl.dev.java.net/2009/02">
           <grammars/>
           <resources base="https://test.api.openstack.com">
              <resource path="path">
                <resource path="to">
                  <resource path="my">
                   <resource path="resource">
                     <method name="GET">
                        <response status="200 203"/>
                     </method>
                     <method name="DELETE">
                        <response status="200"/>
                     </method>
                     <method name="POST">
                        <request>
                           <representation mediaType="application/xml"/>
                        </request>
                        <response status="200"/>
                     </method>
                   </resource>
                 </resource>
                </resource>
              </resource>
           </resources>
        </application>
      when("the wadl is translated")
      val checker = builder.build (inWADL, stdConfig)
      singlePathAssertions(checker)
    }

    scenario("The WADL contains a single multi-path resource in mixed form") {
      given("a WADL that contains a single multi-path resource in mixed form with a GET, DELETE, and POST method")
      val inWADL =
        <application xmlns="http://wadl.dev.java.net/2009/02">
           <grammars/>
           <resources base="https://test.api.openstack.com">
              <resource path="path/to/my">
                   <resource path="resource">
                     <method name="GET">
                        <response status="200 203"/>
                     </method>
                     <method name="DELETE">
                        <response status="200"/>
                     </method>
                     <method name="POST">
                        <request>
                           <representation mediaType="application/xml"/>
                        </request>
                        <response status="200"/>
                     </method>
                   </resource>
              </resource>
           </resources>
        </application>
      when("the wadl is translated")
      val checker = builder.build (inWADL, stdConfig)
      singlePathAssertions(checker)
    }

    scenario("The WADL contains a single multi-path resource with a method referece") {
      given("a WADL that contains a single multi-path resource with a method reference")
      val inWADL =
        <application xmlns="http://wadl.dev.java.net/2009/02">
           <grammars/>
           <resources base="https://test.api.openstack.com">
              <resource path="path/to/my/resource">
                   <method href="#getMethod" />
                   <method name="DELETE">
                      <response status="200"/>
                   </method>
                   <method name="POST">
                      <request>
                         <representation mediaType="application/xml"/>
                      </request>
                      <response status="200"/>
                   </method>
              </resource>
           </resources>
           <method id="getMethod" name="GET">
               <response status="200 203"/>
           </method>
        </application>
      when("the wadl is translated")
      val checker = builder.build (inWADL, stdConfig)
      singlePathAssertions(checker)
    }

    scenario("The WADL contains a single multi-path resource with a resource type") {
      given("a WADL that contains a single multi-path resource with a resource type")
      val inWADL =
        <application xmlns="http://wadl.dev.java.net/2009/02">
           <grammars/>
           <resources base="https://test.api.openstack.com">
              <resource path="path/to/my/resource" type="#test"/>
           </resources>
           <resource_type id="test">
              <method id="getMethod" name="GET">
                  <response status="200 203"/>
              </method>
              <method name="DELETE">
                  <response status="200"/>
              </method>
              <method name="POST">
                   <request>
                     <representation mediaType="application/xml"/>
                   </request>
                     <response status="200"/>
             </method>
           </resource_type>
        </application>
      when("the wadl is translated")
      val checker = builder.build (inWADL, stdConfig)
      singlePathAssertions(checker)
    }

    scenario("The WADL contains a single multi-path resource with a resource type with method references") {
      given("a WADL that contains a single multi-path resource with a resource type with method references")
      val inWADL =
        <application xmlns="http://wadl.dev.java.net/2009/02">
           <grammars/>
           <resources base="https://test.api.openstack.com">
              <resource path="path/to/my/resource" type="#test"/>
           </resources>
           <resource_type id="test">
              <method href="#getMethod" />
              <method name="DELETE">
                  <response status="200"/>
              </method>
              <method name="POST">
                 <request>
                    <representation mediaType="application/xml"/>
                 </request>
                 <response status="200"/>
              </method>
           </resource_type>
           <method id="getMethod" name="GET">
               <response status="200 203"/>
           </method>
        </application>
      when("the wadl is translated")
      val checker = builder.build (inWADL, stdConfig)
      singlePathAssertions(checker)
    }

    //
    //  The following scenarios test two resources located at
    //  /path/to/my/resource with a GET and a DELETE method and
    //  /path/to/my/other_resource with a GET and POST method. They
    //  are equivalent but they are written in slightly different WADL
    //  form. The assertions below must apply to all of them.
    //

    def multiplePathAssertions (checker : NodeSeq) : Unit = {
      then("The checker should contain an URL node for each path step")
      assert (checker, "count(/chk:checker/chk:step[@type='URL']) = 5")
      and ("The checker should contain a GET, POST, and DELETE method")
      assert (checker, "/chk:checker/chk:step[@type='METHOD' and @match='GET']")
      assert (checker, "/chk:checker/chk:step[@type='METHOD' and @match='DELETE']")
      assert (checker, "/chk:checker/chk:step[@type='METHOD' and @match='POST']")
      and ("The path from the start should contain all URL nodes in order")
      and ("it should end in the right method")
      assert (checker, Start, URL("path"), URL("to"), URL("my"), URL("resource"), Method("GET"))
      assert (checker, Start, URL("path"), URL("to"), URL("my"), URL("resource"), Method("DELETE"))
      assert (checker, Start, URL("path"), URL("to"), URL("my"), URL("other_resource"), Method("GET"))
      assert (checker, Start, URL("path"), URL("to"), URL("my"), URL("other_resource"), Method("POST"))
      and ("The Start state and each URL state should contain a path to MethodFail and URLFail")
      assert (checker, Start, URLFail)
      assert (checker, Start, MethodFail)
      assert (checker, URL("path"), URLFail)
      assert (checker, URL("path"), MethodFail)
      assert (checker, URL("to"), URLFail)
      assert (checker, URL("to"), MethodFail)
      assert (checker, URL("my"), URLFail)
      assert (checker, URL("my"), MethodFail)
      assert (checker, URL("resource"), URLFail)
      assert (checker, URL("resource"), MethodFail)
      assert (checker, URL("other_resource"), URLFail)
      assert (checker, URL("other_resource"), MethodFail)
    }

    scenario("The WADL contains multiple, related paths") {
      given ("a WADL with multiple related paths")
      val inWADL =
        <application xmlns="http://wadl.dev.java.net/2009/02">
           <grammars/>
           <resources base="https://test.api.openstack.com">
              <resource path="path/to/my/resource">
                   <method name="GET">
                      <response status="200 203"/>
                   </method>
                   <method name="DELETE">
                      <response status="200"/>
                   </method>
              </resource>
              <resource path="path/to/my/other_resource">
                   <method name="GET">
                      <response status="200 203"/>
                   </method>
                   <method name="POST">
                      <response status="200"/>
                   </method>
              </resource>
          </resources>
        </application>
      when("the wadl is translated")
      val checker = builder.build (inWADL, stdConfig)
      multiplePathAssertions(checker)
    }

    scenario("The WADL in tree format contains multiple, related paths") {
      given ("a WADL in tree format with multiple related paths")
      val inWADL =
        <application xmlns="http://wadl.dev.java.net/2009/02">
           <grammars/>
           <resources base="https://test.api.openstack.com">
              <resource path="path">
                <resource path="to">
                  <resource path="my">
                   <resource path="resource">
                     <method name="GET">
                        <response status="200 203"/>
                     </method>
                     <method name="DELETE">
                        <response status="200"/>
                     </method>
                   </resource>
                   <resource path="other_resource">
                     <method name="GET">
                        <response status="200 203"/>
                     </method>
                     <method name="POST">
                        <response status="200"/>
                     </method>
                   </resource>
                 </resource>
                </resource>
              </resource>
           </resources>
        </application>
      when("the wadl is translated")
      val checker = builder.build (inWADL, stdConfig)
      multiplePathAssertions(checker)
    }

    scenario("The WADL in mix format contains multiple, related paths") {
      given ("a WADL in mix format with multiple related paths")
      val inWADL =
        <application xmlns="http://wadl.dev.java.net/2009/02">
           <grammars/>
           <resources base="https://test.api.openstack.com">
              <resource path="path/to/my">
                   <resource path="resource">
                     <method name="GET">
                        <response status="200 203"/>
                     </method>
                     <method name="DELETE">
                        <response status="200"/>
                     </method>
                   </resource>
                   <resource path="other_resource">
                     <method name="GET">
                        <response status="200 203"/>
                     </method>
                     <method name="POST">
                        <response status="200"/>
                     </method>
                   </resource>
              </resource>
           </resources>
        </application>
      when("the wadl is translated")
      val checker = builder.build (inWADL, stdConfig)
      multiplePathAssertions(checker)
    }

    //
    //  The following scenarios test two resources located at
    //  /path/to/my/resource with a GET and a DELETE method and
    //  /path/to/my/other_resource with a GET and POST method. They
    //  are equivalent but they are written in slightly different WADL
    //  form. The assertions below must apply to all of them.
    //

    def multipleUnrelatedPathAssertions (checker : NodeSeq) : Unit = {
      then("The checker should contain an URL node for each path step")
      assert (checker, "count(/chk:checker/chk:step[@type='URL']) = 8")
      and ("The checker should contain a GET, POST, and DELETE method")
      assert (checker, "/chk:checker/chk:step[@type='METHOD' and @match='GET']")
      assert (checker, "/chk:checker/chk:step[@type='METHOD' and @match='DELETE']")
      assert (checker, "/chk:checker/chk:step[@type='METHOD' and @match='POST']")
      and ("The path from the start should contain all URL nodes in order")
      and ("it should end in the right method")
      assert (checker, Start, URL("path"), URL("to"), URL("my"), URL("resource"), Method("GET"))
      assert (checker, Start, URL("path"), URL("to"), URL("my"), URL("resource"), Method("DELETE"))
      assert (checker, Start, URL("this"), URL("is"), URL("my"), URL("other_resource"), Method("GET"))
      assert (checker, Start, URL("this"), URL("is"), URL("my"), URL("other_resource"), Method("POST"))
      and ("The Start state and each URL state should contain a path to MethodFail and URLFail")
      assert (checker, Start, URLFail)
      assert (checker, Start, MethodFail)
      assert (checker, URL("this"), URLFail)
      assert (checker, URL("this"), MethodFail)
      assert (checker, URL("is"), URLFail)
      assert (checker, URL("is"), MethodFail)
      assert (checker, URL("path"), URLFail)
      assert (checker, URL("path"), MethodFail)
      assert (checker, URL("to"), URLFail)
      assert (checker, URL("to"), MethodFail)
      assert (checker, URL("my"), URLFail)
      assert (checker, URL("my"), MethodFail)
      assert (checker, URL("resource"), URLFail)
      assert (checker, URL("resource"), MethodFail)
      assert (checker, URL("other_resource"), URLFail)
      assert (checker, URL("other_resource"), MethodFail)
    }

    scenario("The WADL contains multiple, unrelated paths") {
      given ("a WADL with multiple unrelated paths")
      val inWADL =
        <application xmlns="http://wadl.dev.java.net/2009/02">
           <grammars/>
           <resources base="https://test.api.openstack.com">
              <resource path="path/to/my/resource">
                   <method name="GET">
                      <response status="200 203"/>
                   </method>
                   <method name="DELETE">
                      <response status="200"/>
                   </method>
              </resource>
              <resource path="this/is/my/other_resource">
                   <method name="GET">
                      <response status="200 203"/>
                   </method>
                   <method name="POST">
                      <response status="200"/>
                   </method>
              </resource>
          </resources>
        </application>
      when("the wadl is translated")
      val checker = builder.build (inWADL, stdConfig)
      multipleUnrelatedPathAssertions(checker)
    }

    scenario("The WADL in tree format contains multiple, unrelated paths") {
      given ("a WADL in tree format with multiple unrelated paths")
      val inWADL =
        <application xmlns="http://wadl.dev.java.net/2009/02">
           <grammars/>
           <resources base="https://test.api.openstack.com">
              <resource path="path">
                <resource path="to">
                  <resource path="my">
                   <resource path="resource">
                     <method name="GET">
                        <response status="200 203"/>
                     </method>
                     <method name="DELETE">
                        <response status="200"/>
                     </method>
                   </resource>
                 </resource>
                </resource>
              </resource>
              <resource path="this">
                <resource path="is">
                   <resource path="my">
                     <resource path="other_resource">
                       <method name="GET">
                          <response status="200 203"/>
                       </method>
                       <method name="POST">
                          <response status="200"/>
                       </method>
                     </resource>
                   </resource>
                </resource>
              </resource>
           </resources>
        </application>
      when("the wadl is translated")
      val checker = builder.build (inWADL, stdConfig)
      multipleUnrelatedPathAssertions(checker)
    }

    scenario("The WADL in mix format contains multiple, unrelated paths") {
      given ("a WADL in mix format with multiple unrelated paths")
      val inWADL =
        <application xmlns="http://wadl.dev.java.net/2009/02">
           <grammars/>
           <resources base="https://test.api.openstack.com">
              <resource path="path/to/my">
                   <resource path="resource">
                     <method name="GET">
                        <response status="200 203"/>
                     </method>
                     <method name="DELETE">
                        <response status="200"/>
                     </method>
                   </resource>
              </resource>
              <resource path="this/is/my">
                   <resource path="other_resource">
                     <method name="GET">
                        <response status="200 203"/>
                     </method>
                     <method name="POST">
                        <response status="200"/>
                     </method>
                   </resource>
              </resource>
           </resources>
        </application>
      when("the wadl is translated")
      val checker = builder.build (inWADL, stdConfig)
      multipleUnrelatedPathAssertions(checker)
    }

    scenario("The WADL contains method ids") {
      given ("a WADL with method IDs")
      val inWADL =
        <application xmlns="http://wadl.dev.java.net/2009/02">
           <grammars/>
           <resources base="https://test.api.openstack.com">
              <resource path="path/to/my/resource">
                   <method id="getResource" name="GET">
                      <response status="200 203"/>
                   </method>
                   <method id="deleteResource" name="DELETE">
                      <response status="200"/>
                   </method>
              </resource>
          </resources>
        </application>
      when("the wadl is translated")
      val checker = builder.build (inWADL, stdConfig)
      then("The method nodes should contain a resource label with the id")
      assert (checker, "count(/chk:checker/chk:step[@type='METHOD']) = 2")
      assert (checker, "/chk:checker/chk:step[@type='METHOD' and @match='GET' and @label='getResource']")
      assert (checker, "/chk:checker/chk:step[@type='METHOD' and @match='DELETE' and @label='deleteResource']")
    }

    scenario("The WADL contains an initial invisible node") {
      given ("a WADL with an initial invisble node")
      val inWADL =
        <application xmlns="http://wadl.dev.java.net/2009/02"
                     xmlns:rax="http://docs.rackspace.com/api">
           <grammars/>
           <resources base="https://test.api.openstack.com">
              <resource rax:invisible="true" path="path">
               <method name="GET">
                    <response status="200 203"/>
                </method>
                <resource path="to">
                  <resource path="my">
                   <resource path="resource">
                     <method name="GET">
                        <response status="200 203"/>
                     </method>
                     <method name="DELETE">
                        <response status="200"/>
                     </method>
                   </resource>
                 </resource>
                </resource>
              </resource>
           </resources>
        </application>
      when ("the wadl is translated")
      val checker = builder.build (inWADL, stdConfig)
      then("All paths should be available as defined in the WADL...")
      assert (checker, Start, URL("path"), Method("GET"))
      assert (checker, Start, URL("path"), URL("to"), URL("my"), URL("resource"), Method("GET"))
      assert (checker, Start, URL("path"), URL("to"), URL("my"), URL("resource"), Method("DELETE"))
      and("Paths should also be accessible directly from start")
      assert (checker, Start, Method("GET"))
      assert (checker, Start, URL("to"), URL("my"), URL("resource"), Method("GET"))
      assert (checker, Start, URL("to"), URL("my"), URL("resource"), Method("DELETE"))
    }

    //
    //  The following scenarios test a string template parameter at the 
    //  of a resource path (/path/to/my/resource/{id}. They are
    //  equivalent but they are written in slightly different WADL
    //  form the assertions below must apply to all of them.
    //

    def stringTemplateAtEndAssertions (checker : NodeSeq) : Unit = {
      then("The checker should contain an URL node for each path step")
      assert (checker, "count(/chk:checker/chk:step[@type='URL']) = 5")
      and ("The checker should contain a GET method")
      assert (checker, "/chk:checker/chk:step[@type='METHOD' and @match='GET']")
      and ("The path from the start should contain all URL nodes including a .*")
      and ("it should end in the GET method node")
      assert (checker, Start, URL("path"), URL("to"), URL("my"), URL("resource"), URL(".*"), Method("GET"))
      and ("The Start state and each URL state should contain a path to MethodFail and URLFail")
      assert (checker, Start, URLFail)
      assert (checker, Start, MethodFail)
      assert (checker, URL("path"), URLFail)
      assert (checker, URL("path"), MethodFail)
      assert (checker, URL("to"), URLFail)
      assert (checker, URL("to"), MethodFail)
      assert (checker, URL("my"), URLFail)
      assert (checker, URL("my"), MethodFail)
      assert (checker, URL("resource"), MethodFail)
      assert (checker, URL(".*"), URLFail)
      assert (checker, URL(".*"), MethodFail)
      and ("the URL('resource') will not have an URL fail because all URLs are accepted")
      val stepsFromResource = allStepsFromStep (checker, URL("resource"), 2)
      assert (stepsFromResource, "not(//chk:step[@type='URL_FAIL'])")
    }


    scenario("The WADL contains a template parameter of type string at the end of a path") {
      given("a WADL with a single template string at the end of the path")
      val inWADL=
        <application xmlns="http://wadl.dev.java.net/2009/02"
                     xmlns:xsd="http://www.w3.org/2001/XMLSchema">
           <grammars/>
           <resources base="https://test.api.openstack.com">
              <resource path="path/to/my/resource/{id}">
                   <param name="id" style="template" type="xsd:string"/>
                   <method href="#getMethod" />
              </resource>
           </resources>
           <method id="getMethod" name="GET">
               <response status="200 203"/>
           </method>
        </application>
      when ("the wadl is translated")
      val checker = builder.build (inWADL, stdConfig)
      stringTemplateAtEndAssertions(checker)
    }

    scenario("The WADL in tree format contains a template parameter of type string at the end of a path") {
      given("a WADL in tree format with a single template string at the end of the path")
      val inWADL=
        <application xmlns="http://wadl.dev.java.net/2009/02"
                     xmlns:xsd="http://www.w3.org/2001/XMLSchema">
           <grammars/>
           <resources base="https://test.api.openstack.com">
              <resource path="path">
                <resource path="to">
                  <resource path="my">
                   <resource path="resource">
                    <resource path="{id}">
                       <param name="id" style="template" type="xsd:string"/>
                       <method href="#getMethod" />
                    </resource>
                  </resource>
                </resource>
               </resource>
             </resource>
           </resources>
           <method id="getMethod" name="GET">
               <response status="200 203"/>
           </method>
        </application>
      when ("the wadl is translated")
      val checker = builder.build (inWADL, stdConfig)
      stringTemplateAtEndAssertions(checker)
    }

    scenario("The WADL in mix format contains a template parameter of type string at the end of a path") {
      given("a WADL in mix format with a single template string at the end of the path")
      val inWADL=
        <application xmlns="http://wadl.dev.java.net/2009/02"
                     xmlns:xsd="http://www.w3.org/2001/XMLSchema">
           <grammars/>
           <resources base="https://test.api.openstack.com">
              <resource path="path/to/my">
                   <resource path="resource">
                    <resource path="{id}">
                       <param name="id" style="template" type="xsd:string"/>
                       <method href="#getMethod" />
                    </resource>
               </resource>
             </resource>
           </resources>
           <method id="getMethod" name="GET">
               <response status="200 203"/>
           </method>
        </application>
      when ("the wadl is translated")
      val checker = builder.build (inWADL, stdConfig)
      stringTemplateAtEndAssertions(checker)
    }

    scenario("The WADL contains a template parameter of type string at the end of a path, the prefix used is not xsd, but the qname is valid") {
      given("a WADL with a single template string at the end of the path, the prefix used is not xsd, but the qname is valid")
      val inWADL=
        <application xmlns="http://wadl.dev.java.net/2009/02"
                     xmlns:x="http://www.w3.org/2001/XMLSchema">
           <grammars/>
           <resources base="https://test.api.openstack.com">
              <resource path="path/to/my/resource/{id}">
                   <param name="id" style="template" type="x:string"/>
                   <method href="#getMethod" />
              </resource>
           </resources>
           <method id="getMethod" name="GET">
               <response status="200 203"/>
           </method>
        </application>
      when ("the wadl is translated")
      val checker = builder.build (inWADL, stdConfig)
      stringTemplateAtEndAssertions(checker)
    }

    scenario("Error Condition: The WADL contains a template parameter of type string, but the param element has a name mismatch") {
      given("a WADL with a template parameter, with a mismatch in the param name")
      val inWADL=
        <application xmlns="http://wadl.dev.java.net/2009/02"
                     xmlns:xsd="http://www.w3.org/2001/XMLSchema">
           <grammars/>
           <resources base="https://test.api.openstack.com">
              <resource path="path/to/my/resource/{id}">
                   <param name="other" style="template" type="xsd:string"/>
                   <method href="#getMethod" />
              </resource>
           </resources>
           <method id="getMethod" name="GET">
               <response status="200 203"/>
           </method>
        </application>
      when ("the wadl is translated")
      then ("A WADLException should be thrown")
      intercept[WADLException] {
        val checker = builder.build (inWADL, stdConfig)
      }
    }

    scenario("Error Condition: The WADL contains a template parameter of type string, but is missing a  param element") {
      given("a WADL with a template parameter but no param element")
      val inWADL=
        <application xmlns="http://wadl.dev.java.net/2009/02"
                     xmlns:xsd="http://www.w3.org/2001/XMLSchema">
           <grammars/>
           <resources base="https://test.api.openstack.com">
              <resource path="path/to/my/resource/{id}">
                   <method href="#getMethod" />
              </resource>
           </resources>
           <method id="getMethod" name="GET">
               <response status="200 203"/>
           </method>
        </application>
      when ("the wadl is translated")
      then ("A WADLException should be thrown")
      intercept[WADLException] {
        val checker = builder.build (inWADL, stdConfig)
      }
    }

    scenario("Error Condition: The WADL contains a template parameter of type string, but the param element has a type mismatch") {
      given("a WADL with a template parameter, with a mismatch in the param type")
      val inWADL=
        <application xmlns="http://wadl.dev.java.net/2009/02"
                     xmlns:xsd="http://www.w3.org/2001/XMLSchema">
           <grammars/>
           <resources base="https://test.api.openstack.com">
              <resource path="path/to/my/resource/{id}">
                   <param name="id" style="header" type="xsd:string"/>
                   <method href="#getMethod" />
              </resource>
           </resources>
           <method id="getMethod" name="GET">
               <response status="200 203"/>
           </method>
        </application>
      when ("the wadl is translated")
      then ("A WADLException should be thrown")
      intercept[WADLException] {
        val checker = builder.build (inWADL, stdConfig)
      }
    }

    scenario("Error Condition: The WADL contains a template parameter of a type with a bad qname") {
      given("a WADL with a template parameter of a type with a bad qname")
      val inWADL=
        <application xmlns="http://wadl.dev.java.net/2009/02">
           <grammars/>
           <resources base="https://test.api.openstack.com">
              <resource path="path/to/my/resource/{id}">
                   <param name="id" style="template" type="xsd:string"/>
                   <method href="#getMethod" />
              </resource>
           </resources>
           <method id="getMethod" name="GET">
               <response status="200 203"/>
           </method>
        </application>
      when ("the wadl is translated")
      then ("A WADLException should be thrown")
      intercept[WADLException] {
        val checker = builder.build (inWADL, stdConfig)
      }
    }

    //
    //  The following scenarios test a string template parameter in the
    //  middle of the resource path (/path/to/my/{id}/resource. They are
    //  equivalent but they are written in slightly different WADL
    //  form the assertions below must apply to all of them.
    //

    def stringTemplateInMiddleAssertions (checker : NodeSeq) : Unit = {
      then("The checker should contain an URL node for each path step")
      assert (checker, "count(/chk:checker/chk:step[@type='URL']) = 5")
      and ("The checker should contain a GET method")
      assert (checker, "/chk:checker/chk:step[@type='METHOD' and @match='GET']")
      and ("The path from the start should contain all URL nodes including a .*")
      and ("it should end in the GET method node")
      assert (checker, Start, URL("path"), URL("to"), URL("my"), URL(".*"), URL("resource"), Method("GET"))
      and ("The Start state and each URL state should contain a path to MethodFail and URLFail")
      assert (checker, Start, URLFail)
      assert (checker, Start, MethodFail)
      assert (checker, URL("path"), URLFail)
      assert (checker, URL("path"), MethodFail)
      assert (checker, URL("to"), URLFail)
      assert (checker, URL("to"), MethodFail)
      assert (checker, URL("my"), MethodFail)
      assert (checker, URL("resource"), MethodFail)
      assert (checker, URL("resource"), URLFail)
      assert (checker, URL(".*"), URLFail)
      assert (checker, URL(".*"), MethodFail)
      and ("the URL('my') will not have an URL fail because all URLs are accepted")
      val stepsFromResource = allStepsFromStep (checker, URL("my"), 2)
      assert (stepsFromResource, "not(//chk:step[@type='URL_FAIL'])")
    }

    scenario("The WADL contains a template parameter of type string in the middle of the path") {
      given("a WADL with a single template string in the middle of the path")
      val inWADL=
        <application xmlns="http://wadl.dev.java.net/2009/02"
                     xmlns:xsd="http://www.w3.org/2001/XMLSchema">
           <grammars/>
           <resources base="https://test.api.openstack.com">
              <resource path="path/to/my/{id}/resource">
                   <param name="id" style="template" type="xsd:string"/>
                   <method href="#getMethod" />
              </resource>
           </resources>
           <method id="getMethod" name="GET">
               <response status="200 203"/>
           </method>
        </application>
      when ("the wadl is translated")
      val checker = builder.build (inWADL, stdConfig)
      stringTemplateInMiddleAssertions(checker)
    }

    scenario("The WADL in tree format contains a template parameter of type string in the middle of the path") {
      given("a WADL in tree format with a single template string in the middle of the path")
      val inWADL=
        <application xmlns="http://wadl.dev.java.net/2009/02"
                     xmlns:xsd="http://www.w3.org/2001/XMLSchema">
           <grammars/>
           <resources base="https://test.api.openstack.com">
              <resource path="path">
                <resource path="to">
                  <resource path="my">
                    <resource path="{id}">
                       <param name="id" style="template" type="xsd:string"/>
                      <resource path="resource">
                       <method href="#getMethod" />
                     </resource>
                  </resource>
                </resource>
               </resource>
             </resource>
           </resources>
           <method id="getMethod" name="GET">
               <response status="200 203"/>
           </method>
        </application>
      when ("the wadl is translated")
      val checker = builder.build (inWADL, stdConfig)
      stringTemplateInMiddleAssertions(checker)
    }

    scenario("The WADL in mix format contains a template parameter of type string in the middle of the path") {
      given("a WADL in mix format with a single template string in the middle of the path")
      val inWADL=
        <application xmlns="http://wadl.dev.java.net/2009/02"
                     xmlns:xsd="http://www.w3.org/2001/XMLSchema">
           <grammars/>
           <resources base="https://test.api.openstack.com">
              <resource path="path/to/my">
                   <resource path="{id}">
                      <param name="id" style="template" type="xsd:string"/>
                    <resource path="resource">
                       <method href="#getMethod" />
                    </resource>
               </resource>
             </resource>
           </resources>
           <method id="getMethod" name="GET">
               <response status="200 203"/>
           </method>
        </application>
      when ("the wadl is translated")
      val checker = builder.build (inWADL, stdConfig)
      stringTemplateInMiddleAssertions(checker)
    }

    //
    //  The following scenarios test a custom type template parameter at the
    //  of a resource path (/path/to/my/resource/{yn}. They are
    //  equivalent but they are written in slightly different WADL
    //  form the assertions below must apply to all of them.
    //

    def customTemplateAtEndAssertions (checker : NodeSeq) : Unit = {
      then("The checker should contain an URL node for each path step")
      assert (checker, "count(/chk:checker/chk:step[@type='URL']) = 4")
      and("A single URLXSD node")
      assert (checker, "count(/chk:checker/chk:step[@type='URLXSD']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='URLXSD' and @label='yn']) = 1")
      and ("The checker should contain a GET method")
      assert (checker, "/chk:checker/chk:step[@type='METHOD' and @match='GET']")
      and ("The path from the start should contain all URL and URLXSD nodes")
      and ("it should end in the GET method node")
      assert (checker, Start, URL("path"), URL("to"), URL("my"), URL("resource"), Label("yn"), Method("GET"))
      and ("The URLXSD should match a valid QName")
      assert (checker, "namespace-uri-from-QName(resolve-QName(//chk:step[@label='yn'][1]/@match, //chk:step[@label='yn'][1])) "+
                                           "= 'test://schema/a'")
      assert (checker, "local-name-from-QName(resolve-QName(//chk:step[@label='yn'][1]/@match, //chk:step[@label='yn'][1])) "+
                                           "= 'yesno'")
      and ("The Start state and each URL state should contain a path to MethodFail and URLFail")
      assert (checker, Start, URLFail)
      assert (checker, Start, MethodFail)
      assert (checker, URL("path"), URLFail)
      assert (checker, URL("path"), MethodFail)
      assert (checker, URL("to"), URLFail)
      assert (checker, URL("to"), MethodFail)
      assert (checker, URL("my"), URLFail)
      assert (checker, URL("my"), MethodFail)
      assert (checker, URL("resource"), URLFail)
      assert (checker, URL("resource"), MethodFail)
      assert (checker, Label("yn"), URLFail)
      assert (checker, Label("yn"), MethodFail)
      and("The grammar nodes are added to the checker")
      assert (checker, "/chk:checker/chk:grammar[@ns='test://schema/a' and  @type='W3C_XML']")
      assert (checker, "if (/chk:checker/chk:grammar/@href) then not(/chk:checker/chk:grammar/xsd:schema) else /chk:checker/chk:grammar/xsd:schema")
      assert (checker, "if (/chk:checker/chk:grammar/@href) then /chk:checker/chk:grammar/@href='test://app/xsd/simple.xsd' else true()")
    }

    scenario("The WADL contains a template parameter of a custom type at the end of the path") {
      given("A WADL with a template parameter of a custom type at the end of the path")
      val inWADL =
        <application xmlns="http://wadl.dev.java.net/2009/02"
                     xmlns:tst="test://schema/a">
           <grammars>
              <include href="test://app/xsd/simple.xsd"/>
           </grammars>
           <resources base="https://test.api.openstack.com">
              <resource id="yn" path="path/to/my/resource/{yn}">
                   <param name="yn" style="template" type="tst:yesno"/>
                   <method href="#getMethod" />
              </resource>
           </resources>
           <method id="getMethod" name="GET">
               <response status="200 203"/>
           </method>
        </application>
      register("test://app/xsd/simple.xsd",
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
                </schema>)
      when("the wadl is translated")
      val checker = builder.build (inWADL, stdConfig)
      customTemplateAtEndAssertions(checker)
    }

    scenario("The WADL contains a template parameter of a custom type at the end of the path, the XSD is embedded") {
      given("A WADL with a template parameter of a custom type at the end of the path, the XSD is embedded")
      val inWADL =
        <application xmlns="http://wadl.dev.java.net/2009/02"
                     xmlns:tst="test://schema/a">
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
              <resource id="yn" path="path/to/my/resource/{yn}">
                   <param name="yn" style="template" type="tst:yesno"/>
                   <method href="#getMethod" />
              </resource>
           </resources>
           <method id="getMethod" name="GET">
               <response status="200 203"/>
           </method>
        </application>
      when("the wadl is translated")
      val checker = builder.build (inWADL, stdConfig)
      customTemplateAtEndAssertions(checker)
    }

    scenario("The WADL contains a template parameter of a custom type at the end of the path, the schema is in a relative path") {
      given("A WADL with a template parameter of a custom type at the end of the path, the schema is in a relative path")
      val inWADL =
        <application xmlns="http://wadl.dev.java.net/2009/02"
                     xmlns:tst="test://schema/a">
           <grammars>
              <include href="xsd/simple.xsd"/>
           </grammars>
           <resources base="https://test.api.openstack.com">
              <resource id="yn" path="path/to/my/resource/{yn}">
                   <param name="yn" style="template" type="tst:yesno"/>
                   <method href="#getMethod" />
              </resource>
           </resources>
           <method id="getMethod" name="GET">
               <response status="200 203"/>
           </method>
        </application>
      register("test://app/xsd/simple.xsd",
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
                </schema>)
      when("the wadl is translated")
      val checker = builder.build (inWADL, stdConfig)
      customTemplateAtEndAssertions(checker)
    }

    scenario("The WADL in tree format contains a template parameter of a custom type at the end of the path") {
      given("A WADL in tree format with a template parameter of a custom type at the end of the path")
      val inWADL =
        <application xmlns="http://wadl.dev.java.net/2009/02"
                     xmlns:tst="test://schema/a">
           <grammars>
              <include href="test://app/xsd/simple.xsd"/>
           </grammars>
           <resources base="https://test.api.openstack.com">
              <resource path="path">
                <resource path="to">
                  <resource path="my">
                   <resource path="resource">
                    <resource path="{yn}">
                       <param name="yn" style="template" type="tst:yesno"/>
                       <method href="#getMethod" />
                    </resource>
                  </resource>
                </resource>
               </resource>
             </resource>
           </resources>
           <method id="getMethod" name="GET">
               <response status="200 203"/>
           </method>
        </application>
      register("test://app/xsd/simple.xsd",
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
                </schema>)
      when("the wadl is translated")
      val checker = builder.build (inWADL, stdConfig)
      customTemplateAtEndAssertions(checker)
    }

    scenario("The WADL in mix format contains a template parameter of a custom type at the end of the path") {
      given("A WADL in mix format with a template parameter of a custom type at the end of the path")
      val inWADL =
        <application xmlns="http://wadl.dev.java.net/2009/02"
                     xmlns:tst="test://schema/a">
           <grammars>
              <include href="test://app/xsd/simple.xsd"/>
           </grammars>
           <resources base="https://test.api.openstack.com">
              <resource path="path/to/my">
                   <resource path="resource">
                    <resource id="yn" path="{yn}">
                       <param name="yn" style="template" type="tst:yesno"/>
                       <method href="#getMethod" />
                    </resource>
                    </resource>
               </resource>
           </resources>
           <method id="getMethod" name="GET">
               <response status="200 203"/>
           </method>
        </application>
      register("test://app/xsd/simple.xsd",
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
                </schema>)
      when("the wadl is translated")
      val checker = builder.build (inWADL, stdConfig)
      customTemplateAtEndAssertions(checker)
    }

    scenario("The WADL contains a template parameter of a custom type at the end of the path, the type is in the default namespace") {
      given("A WADL with a template parameter of a custom type at the end of the path, with the type in a default namespace")
      val inWADL =
        <wadl:application xmlns:wadl="http://wadl.dev.java.net/2009/02"
                          xmlns="test://schema/a">
           <wadl:grammars>
              <wadl:include href="test://app/xsd/simple.xsd"/>
           </wadl:grammars>
           <wadl:resources base="https://test.api.openstack.com">
              <wadl:resource id="yn" path="path/to/my/resource/{yn}">
                   <wadl:param name="yn" style="template" type="yesno"/>
                   <wadl:method href="#getMethod" />
              </wadl:resource>
           </wadl:resources>
           <wadl:method id="getMethod" name="GET">
               <wadl:response status="200 203"/>
           </wadl:method>
        </wadl:application>
      register("test://app/xsd/simple.xsd",
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
                </schema>)
      when("the wadl is translated")
      val checker = builder.build (inWADL, stdConfig)
      customTemplateAtEndAssertions(checker)
    }

    scenario("The WADL in tree format contains a template parameter of a custom type at the end of the path, the type is in the default namespace") {
      given("A WADL in tree format with a template parameter of a custom type at the end of the path, the type is in the default namespace")
      val inWADL =
        <wadl:application xmlns:wadl="http://wadl.dev.java.net/2009/02"
                          xmlns="test://schema/a">
           <wadl:grammars>
              <wadl:include href="test://app/xsd/simple.xsd"/>
           </wadl:grammars>
           <wadl:resources base="https://test.api.openstack.com">
              <wadl:resource path="path">
                <wadl:resource path="to">
                  <wadl:resource path="my">
                   <wadl:resource path="resource">
                    <wadl:resource path="{yn}">
                       <wadl:param name="yn" style="template" type="yesno"/>
                       <wadl:method href="#getMethod" />
                    </wadl:resource>
                  </wadl:resource>
                </wadl:resource>
               </wadl:resource>
             </wadl:resource>
           </wadl:resources>
           <wadl:method id="getMethod" name="GET">
               <wadl:response status="200 203"/>
           </wadl:method>
        </wadl:application>
      register("test://app/xsd/simple.xsd",
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
                </schema>)
      when("the wadl is translated")
      val checker = builder.build (inWADL, stdConfig)
      customTemplateAtEndAssertions(checker)
    }

    scenario("The WADL in mix format contains a template parameter of a custom type at the end of the path, the type is in the default namespace") {
      given("A WADL in mix format with a template parameter of a custom type at the end of the path, the type is in the default namespace")
      val inWADL =
        <wadl:application xmlns:wadl="http://wadl.dev.java.net/2009/02"
                         xmlns="test://schema/a">
           <wadl:grammars>
              <wadl:include href="test://app/xsd/simple.xsd"/>
           </wadl:grammars>
           <wadl:resources base="https://test.api.openstack.com">
              <wadl:resource path="path/to/my">
                   <wadl:resource path="resource">
                    <wadl:resource id="yn" path="{yn}">
                       <wadl:param name="yn" style="template" type="yesno"/>
                       <wadl:method href="#getMethod" />
                    </wadl:resource>
                    </wadl:resource>
               </wadl:resource>
           </wadl:resources>
           <wadl:method id="getMethod" name="GET">
               <wadl:response status="200 203"/>
           </wadl:method>
        </wadl:application>
      register("test://app/xsd/simple.xsd",
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
                </schema>)
      when("the wadl is translated")
      val checker = builder.build (inWADL, stdConfig)
      customTemplateAtEndAssertions(checker)
    }

    scenario("The WADL contains a template parameter of a custom type at the end of the path, with remove dup on") {
      given("A WADL with a template parameter of a custom type at the end of the path")
      val inWADL =
        <application xmlns="http://wadl.dev.java.net/2009/02"
                     xmlns:tst="test://schema/a">
           <grammars>
              <include href="test://app/xsd/simple.xsd"/>
           </grammars>
           <resources base="https://test.api.openstack.com">
              <resource id="yn" path="path/to/my/resource/{yn}">
                   <param name="yn" style="template" type="tst:yesno"/>
                   <method href="#getMethod" />
              </resource>
              <resource path="1/dup">
                   <method href="#getMethod" />
              </resource>
              <resource path="2/dup">
                   <method href="#getMethod" />
              </resource>
           </resources>
           <method id="getMethod" name="GET">
               <response status="200 203"/>
           </method>
        </application>
      register("test://app/xsd/simple.xsd",
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
                </schema>)
      when("the wadl is translated")
      val checker = builder.build (inWADL, dupConfig)
      then("The checker should contain an URL node for each path step")
      assert (checker, "count(/chk:checker/chk:step[@type='URL']) = 7")
      and("A single URLXSD node")
      assert (checker, "count(/chk:checker/chk:step[@type='URLXSD']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='URLXSD' and @label='yn']) = 1")
      and ("The checker should contain a GET method")
      assert (checker, "/chk:checker/chk:step[@type='METHOD' and @match='GET']")
      and ("The path from the start should contain all URL and URLXSD nodes")
      and ("it should end in the GET method node")
      assert (checker, Start, URL("path"), URL("to"), URL("my"), URL("resource"), Label("yn"), Method("GET"))
      and ("The URLXSD should match a valid QName")
      assert (checker, "namespace-uri-from-QName(resolve-QName(//chk:step[@label='yn'][1]/@match, //chk:step[@label='yn'][1])) "+
                                           "= 'test://schema/a'")
      assert (checker, "local-name-from-QName(resolve-QName(//chk:step[@label='yn'][1]/@match, //chk:step[@label='yn'][1])) "+
                                           "= 'yesno'")
      and ("There should not be a duplicate dup node")
      assert (checker, "count(//chk:step[@type='URL' and @match='dup']) = 1")
      and ("The dup paths should be valid")
      assert (checker, Start, URL("1"), URL("dup"), Method("GET"))
      assert (checker, Start, URL("2"), URL("dup"), Method("GET"))
      and ("The Start state and each URL state should contain a path to MethodFail and URLFail")
      assert (checker, Start, URLFail)
      assert (checker, Start, MethodFail)
      assert (checker, URL("1"), URLFail)
      assert (checker, URL("1"), MethodFail)
      assert (checker, URL("2"), URLFail)
      assert (checker, URL("2"), MethodFail)
      assert (checker, URL("dup"), URLFail)
      assert (checker, URL("dup"), MethodFail)
      assert (checker, URL("path"), URLFail)
      assert (checker, URL("path"), MethodFail)
      assert (checker, URL("to"), URLFail)
      assert (checker, URL("to"), MethodFail)
      assert (checker, URL("my"), URLFail)
      assert (checker, URL("my"), MethodFail)
      assert (checker, URL("resource"), URLFail)
      assert (checker, URL("resource"), MethodFail)
      assert (checker, Label("yn"), URLFail)
      assert (checker, Label("yn"), MethodFail)
      and("The grammar nodes are added to the checker")
      assert (checker, "/chk:checker/chk:grammar[@ns='test://schema/a' and @href='test://app/xsd/simple.xsd' and @type='W3C_XML']")
    }

    scenario("The WADL contains a template parameter of a custom type, but the grammar is missing") {
      given("A WADL with a template parameter of a custom type, but the grammar is missing")
      val inWADL =
        <application xmlns="http://wadl.dev.java.net/2009/02"
                     xmlns:tst="test://schema/a">
           <grammars>
              <include href="test://app/xsd/simple.xsd"/>
           </grammars>
           <resources base="https://test.api.openstack.com">
              <resource id="yn" path="path/to/my/resource/{yn}">
                   <param name="yn" style="template" type="tst:yesno"/>
                   <method href="#getMethod" />
              </resource>
           </resources>
           <method id="getMethod" name="GET">
               <response status="200 203"/>
           </method>
        </application>
      when("the wadl is translated")
      then ("A WADLException should be thrown")
      intercept[WADLException] {
        val checker = builder.build (inWADL, dupConfig)
      }
    }

    scenario("The WADL contains a template parameter of a custom type, but the grammar is not XSD") {
      given("A WADL with a template parameter of a custom type, but the grammar is not XSD")
      val inWADL =
        <application xmlns="http://wadl.dev.java.net/2009/02"
                     xmlns:tst="test://schema/a">
           <grammars>
              <include href="test://app/xsd/simple.rng"/>
           </grammars>
           <resources base="https://test.api.openstack.com">
              <resource id="int" path="path/to/my/resource/{int}">
                   <param name="int" style="template" type="tst:int"/>
                   <method href="#getMethod" />
              </resource>
           </resources>
           <method id="getMethod" name="GET">
               <response status="200 203"/>
           </method>
        </application>
      when("the wadl is translated")
      register("test://app/xsd/simple.rng",
               <grammar datatypeLibrary="http://www.w3.org/2001/XMLSchema-datatypes"
               xmlns="http://relaxng.org/ns/structure/1.0" >
                 <define name="int">
                   <data type="integer"/>
                 </define>
               </grammar>
             )
      then ("The grammar should be ignored")
      val checker = builder.build (inWADL, dupConfig)
      assert (checker, "not(/chk:checker/chk:grammar)")
    }

    //
    //  We ignore until we handle adding an unparsed text reslover for
    //  testing.
    //
    ignore("The WADL contains a template parameter of a custom type, but the grammar is not XML") {
      given("A WADL with a template parameter of a custom type, but the grammar is not XML")
      val inWADL =
        <application xmlns="http://wadl.dev.java.net/2009/02"
                     xmlns:tst="test://schema/a">
           <grammars>
              <include href="test://app/xsd/simple.json"/>
           </grammars>
           <resources base="https://test.api.openstack.com">
              <resource id="int" path="path/to/my/resource/{int}">
                   <param name="int" style="template" type="tst:int"/>
                   <method href="#getMethod" />
              </resource>
           </resources>
           <method id="getMethod" name="GET">
               <response status="200 203"/>
           </method>
        </application>
      when("the wadl is translated")
      // register_unparsed("test://app/xsd/simple.json",
      //          """
      //            {
      //              "type" : "int"
      //            }
      //          """
      //        )
      then ("The grammar should be ignored")
      val checker = builder.build (inWADL, stdConfig)
      assert (checker, "not(/chk:checker/chk:grammar)")
    }


    //
    //  The following scenarios test a custom template parameter in the
    //  middle of the resource path (/path/to/my/{yn}/resource. They are
    //  equivalent but they are written in slightly different WADL
    //  form the assertions below must apply to all of them.
    //

    def customTemplateInMiddleAssertions (checker : NodeSeq) : Unit = {
      then("The checker should contain an URL node for each path step")
      assert (checker, "count(/chk:checker/chk:step[@type='URL']) = 4")
      and("A single URLXSD node")
      assert (checker, "count(/chk:checker/chk:step[@type='URLXSD']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='URLXSD' and @label='yn']) = 1")
      and ("The checker should contain a GET method")
      assert (checker, "/chk:checker/chk:step[@type='METHOD' and @match='GET']")
      and ("The path from the start should contain all URL and URLXSD nodes")
      and ("it should end in the GET method node")
      assert (checker, Start, URL("path"), URL("to"), URL("my"), Label("yn"), URL("resource"), Method("GET"))
      and ("The URLXSD should match a valid QName")
      assert (checker, "namespace-uri-from-QName(resolve-QName(//chk:step[@label='yn'][1]/@match, //chk:step[@label='yn'][1])) "+
                                           "= 'test://schema/a'")
      assert (checker, "local-name-from-QName(resolve-QName(//chk:step[@label='yn'][1]/@match, //chk:step[@label='yn'][1])) "+
                                           "= 'yesno'")
      and ("The Start state and each URL state should contain a path to MethodFail and URLFail")
      assert (checker, Start, URLFail)
      assert (checker, Start, MethodFail)
      assert (checker, URL("path"), URLFail)
      assert (checker, URL("path"), MethodFail)
      assert (checker, URL("to"), URLFail)
      assert (checker, URL("to"), MethodFail)
      assert (checker, URL("my"), MethodFail)
      assert (checker, URL("my"), URLFail)
      assert (checker, URL("resource"), MethodFail)
      assert (checker, URL("resource"), URLFail)
      assert (checker, Label("yn"), URLFail)
      assert (checker, Label("yn"), MethodFail)
      and("The grammar nodes are added to the checker")
      assert (checker, "/chk:checker/chk:grammar[@ns='test://schema/a' and @href='test://simple.xsd' and @type='W3C_XML']")
    }

    scenario("The WADL contains a template parameter of a custom type in the middle of the path") {
      given("A WADL with a template parameter of a custom type in the middle of  the path")
      val inWADL =
        <application xmlns="http://wadl.dev.java.net/2009/02"
                     xmlns:tst="test://schema/a">
           <grammars>
              <include href="test://simple.xsd"/>
           </grammars>
           <resources base="https://test.api.openstack.com">
              <resource id="yn" path="path/to/my/{yn}/resource">
                   <param name="yn" style="template" type="tst:yesno"/>
                   <method href="#getMethod" />
              </resource>
           </resources>
           <method id="getMethod" name="GET">
               <response status="200 203"/>
           </method>
        </application>
      register("test://simple.xsd",
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
                </schema>)
      when("the wadl is translated")
      val checker = builder.build (inWADL, stdConfig)
      customTemplateInMiddleAssertions(checker)
    }

    scenario("The WADL in tree format contains a template parameter of a custom type in the middle of the path") {
      given("A WADL in tree format with a template parameter of a custom type in the middle of  the path")
      val inWADL =
        <application xmlns="http://wadl.dev.java.net/2009/02"
                     xmlns:tst="test://schema/a">
           <grammars>
              <include href="test://simple.xsd"/>
           </grammars>
           <resources base="https://test.api.openstack.com">
              <resource path="path">
                <resource path="to">
                  <resource path="my">
                    <resource id="yn" path="{yn}">
                       <param name="yn" style="template" type="tst:yesno"/>
                      <resource path="resource">
                       <method href="#getMethod" />
                     </resource>
                  </resource>
                </resource>
               </resource>
             </resource>
           </resources>
           <method id="getMethod" name="GET">
               <response status="200 203"/>
           </method>
        </application>
      register("test://simple.xsd",
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
                </schema>)
      when("the wadl is translated")
      val checker = builder.build (inWADL, stdConfig)
      customTemplateInMiddleAssertions(checker)
    }

    scenario("The WADL in mix format contains a template parameter of a custom type in the middle of the path") {
      given("A WADL in mix format with a template parameter of a custom type in the middle of  the path")
      val inWADL =
        <application xmlns="http://wadl.dev.java.net/2009/02"
                     xmlns:tst="test://schema/a">
           <grammars>
              <include href="test://simple.xsd"/>
           </grammars>
           <resources base="https://test.api.openstack.com">
              <resource path="path/to/my">
                 <resource id="yn" path="{yn}">
                   <param name="yn" style="template" type="tst:yesno"/>
                   <resource path="resource">
                     <method href="#getMethod" />
                   </resource>
                 </resource>
              </resource>
           </resources>
           <method id="getMethod" name="GET">
               <response status="200 203"/>
           </method>
        </application>
      register("test://simple.xsd",
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
                </schema>)
      when("the wadl is translated")
      val checker = builder.build (inWADL, stdConfig)
      customTemplateInMiddleAssertions(checker)
    }

    //
    //  The following scenarios test multiple template parameters in
    //  the middle of the resource path (/path/to/my/{yn}/resource,
    //  /path/to/my/{tf}/resource. They are equivalent but they are
    //  written in slightly different WADL form the assertions below
    //  must apply to all of them.
    //

    def customTemplatesInMiddleAssertions (checker : NodeSeq) : Unit = {
      then("The checker should contain an URL node for each path step")
      assert (checker, "count(/chk:checker/chk:step[@type='URL']) = 5")
      and("A single URLXSD node")
      assert (checker, "count(/chk:checker/chk:step[@type='URLXSD']) = 2")
      assert (checker, "count(/chk:checker/chk:step[@type='URLXSD' and @label='yn']) = 1")
      assert (checker, "count(/chk:checker/chk:step[@type='URLXSD' and @label='tf']) = 1")
      and ("The checker should contain two GET methods")
      assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='GET']) = 2")
      and ("The path from the start should contain all URL and URLXSD nodes")
      and ("it should end in the GET method node")
      assert (checker, Start, URL("path"), URL("to"), URL("my"), Label("yn"), URL("resource"), Method("GET"))
      assert (checker, Start, URL("path"), URL("to"), URL("my"), Label("tf"), URL("resource"), Method("GET"))
      and ("The URLXSD should match a valid QName")
      assert (checker, "namespace-uri-from-QName(resolve-QName(//chk:step[@label='yn'][1]/@match, //chk:step[@label='yn'][1])) "+
                                           "= 'test://schema/a'")
      assert (checker, "local-name-from-QName(resolve-QName(//chk:step[@label='yn'][1]/@match, //chk:step[@label='yn'][1])) "+
                                           "= 'yesno'")
      assert (checker, "namespace-uri-from-QName(resolve-QName(//chk:step[@label='tf'][1]/@match, //chk:step[@label='tf'][1])) "+
                                           "= 'test://schema/b'")
      assert (checker, "local-name-from-QName(resolve-QName(//chk:step[@label='tf'][1]/@match, //chk:step[@label='tf'][1])) "+
                                           "= 'truefalse'")
      and ("The Start state and each URL state should contain a path to MethodFail and URLFail")
      assert (checker, Start, URLFail)
      assert (checker, Start, MethodFail)
      assert (checker, URL("path"), URLFail)
      assert (checker, URL("path"), MethodFail)
      assert (checker, URL("to"), URLFail)
      assert (checker, URL("to"), MethodFail)
      assert (checker, URL("my"), MethodFail)
      assert (checker, URL("my"), URLFail)
      assert (checker, URL("resource"), MethodFail)
      assert (checker, URL("resource"), URLFail)
      assert (checker, Label("yn"), URLFail)
      assert (checker, Label("yn"), MethodFail)
      assert (checker, Label("tf"), URLFail)
      assert (checker, Label("tf"), MethodFail)
      and("The grammar nodes are added to the checker")
      assert (checker, "/chk:checker/chk:grammar[@ns='test://schema/a' and @href='test://simple.xsd' and @type='W3C_XML']")
      assert (checker, "/chk:checker/chk:grammar[@ns='test://schema/b' and @href='test://simple-too.xsd' and @type='W3C_XML']")
    }

    scenario("The WADL contains multiple template parameters of a custom type in the middle of the path") {
      given("A WADL with multiple template parameters of a custom type in the middle of  the path")
      val inWADL =
        <application xmlns="http://wadl.dev.java.net/2009/02"
                     xmlns:tst="test://schema/a"
                     xmlns:tst2="test://schema/b"
                     >
           <grammars>
              <include href="test://simple.xsd"/>
              <include href="test://simple-too.xsd"/>
           </grammars>
           <resources base="https://test.api.openstack.com">
              <resource id="yn" path="path/to/my/{yn}/resource">
                   <param name="yn" style="template" type="tst:yesno"/>
                   <method href="#getMethod" />
              </resource>
              <resource id="tf" path="path/to/my/{tf}/resource">
                   <param name="tf" style="template" type="tst2:truefalse"/>
                   <method href="#otherGetMethod" />
              </resource>
           </resources>
           <method id="getMethod" name="GET">
               <response status="200 203"/>
           </method>
           <method id="otherGetMethod" name="GET">
               <response status="200 203"/>
           </method>
        </application>
      register("test://simple.xsd",
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
                </schema>)
      register("test://simple-too.xsd",
               <schema elementFormDefault="qualified"
                        attributeFormDefault="unqualified"
                        xmlns="http://www.w3.org/2001/XMLSchema"
                        xmlns:xsd="http://www.w3.org/2001/XMLSchema"
                        targetNamespace="test://schema/b">
                   <simpleType name="truefalse">
                       <restriction base="xsd:string">
                           <enumeration value="true"/>
                           <enumeration value="false"/>
                       </restriction>
                   </simpleType>
                </schema>)
      when("the wadl is translated")
      val checker = builder.build (inWADL, stdConfig)
      customTemplatesInMiddleAssertions(checker)
    }

    scenario("The WADL in tree format contains multiple template parameter of a custom type in the middle of the path") {
      given("A WADL in tree format with multiple template parameter of a custom type in the middle of  the path")
      val inWADL =
        <application xmlns="http://wadl.dev.java.net/2009/02"
                     xmlns:tst="test://schema/a"
                     xmlns:tst2="test://schema/b"
                     >
           <grammars>
              <include href="test://simple.xsd"/>
              <include href="test://simple-too.xsd"/>
           </grammars>
           <resources base="https://test.api.openstack.com">
              <resource path="path">
                <resource path="to">
                  <resource path="my">
                    <resource id="yn" path="{yn}">
                       <param name="yn" style="template" type="tst:yesno"/>
                      <resource path="resource">
                       <method href="#getMethod" />
                     </resource>
                  </resource>
                   <resource id="tf" path="{tf}">
                       <param name="tf" style="template" type="tst2:truefalse"/>
                      <resource path="resource">
                       <method href="#otherGetMethod" />
                     </resource>
                  </resource>
                </resource>
               </resource>
             </resource>
           </resources>
           <method id="getMethod" name="GET">
               <response status="200 203"/>
           </method>
           <method id="otherGetMethod" name="GET">
               <response status="200 203"/>
           </method>
        </application>
      register("test://simple.xsd",
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
                </schema>)
      register("test://simple-too.xsd",
               <schema elementFormDefault="qualified"
                        attributeFormDefault="unqualified"
                        xmlns="http://www.w3.org/2001/XMLSchema"
                        xmlns:xsd="http://www.w3.org/2001/XMLSchema"
                        targetNamespace="test://schema/b">
                   <simpleType name="truefalse">
                       <restriction base="xsd:string">
                           <enumeration value="true"/>
                           <enumeration value="false"/>
                       </restriction>
                   </simpleType>
                </schema>)
      when("the wadl is translated")
      val checker = builder.build (inWADL, stdConfig)
      customTemplatesInMiddleAssertions(checker)
    }

    scenario("The WADL in mix format contains multiple template parameters of a custom type in the middle of the path") {
      given("A WADL in mix format with multiple template parameters of a custom type in the middle of  the path")
      val inWADL =
        <application xmlns="http://wadl.dev.java.net/2009/02"
                     xmlns:tst="test://schema/a"
                     xmlns:tst2="test://schema/b"
                     >
           <grammars>
              <include href="test://simple.xsd"/>
              <include href="test://simple-too.xsd"/>
           </grammars>
           <resources base="https://test.api.openstack.com">
              <resource path="path/to/my">
                 <resource id="yn" path="{yn}">
                   <param name="yn" style="template" type="tst:yesno"/>
                   <resource path="resource">
                     <method href="#getMethod" />
                   </resource>
                 </resource>
                   <resource id="tf" path="{tf}">
                       <param name="tf" style="template" type="tst2:truefalse"/>
                      <resource path="resource">
                       <method href="#otherGetMethod" />
                     </resource>
                  </resource>
              </resource>
           </resources>
           <method id="getMethod" name="GET">
               <response status="200 203"/>
           </method>
           <method id="otherGetMethod" name="GET">
               <response status="200 203"/>
           </method>
        </application>
      register("test://simple.xsd",
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
                </schema>)
      register("test://simple-too.xsd",
               <schema elementFormDefault="qualified"
                        attributeFormDefault="unqualified"
                        xmlns="http://www.w3.org/2001/XMLSchema"
                        xmlns:xsd="http://www.w3.org/2001/XMLSchema"
                        targetNamespace="test://schema/b">
                   <simpleType name="truefalse">
                       <restriction base="xsd:string">
                           <enumeration value="true"/>
                           <enumeration value="false"/>
                       </restriction>
                   </simpleType>
                </schema>)
      when("the wadl is translated")
      val checker = builder.build (inWADL, stdConfig)
      customTemplatesInMiddleAssertions(checker)
    }

    scenario("The WADL contains a URL with special regex symbols") {
      given("a WADL that contains an URL winh special regex symbols")
      val inWADL =
        <application xmlns="http://wadl.dev.java.net/2009/02">
           <grammars/>
           <resources base="https://test.api.openstack.com">
              <resource path="\^-.${}*+|#()[]">
                  <method name=".-"/>
              </resource>
              <resource path="^ABC[D]EFG#">
                  <method name="-GET..IT-"/>
              </resource>
           </resources>
        </application>
      when("the wadl is translated")
      val checker = builder.build (inWADL, stdConfig)
      assert (checker, Start, URL("\\\\\\^\\-\\.\\$\\{\\}\\*\\+\\|\\#\\(\\)\\[\\]"), Method("\\.\\-"))
      assert (checker, Start, URL("\\^ABC\\[D\\]EFG\\#"), Method("\\-GET\\.\\.IT\\-"))
    }
  }

  //
  //  The following assertions are used to test ReqType and
  //  ReqTypeFail nodes, they are used in the next couple of tests.
  //
  def reqTypeAssertions(checker : NodeSeq) : Unit = {
    then("The machine should contain paths to all ReqTypes")
    assert (checker, Start, URL("a"), URL("b"), Method("PUT"), ReqType("application/xml"))
    assert (checker, Start, URL("a"), URL("b"), Method("PUT"), ReqType("application/json"))
    assert (checker, Start, URL("a"), URL("b"), Method("POST"), ReqType("application/xml"))
    assert (checker, Start, URL("c"), Method("POST"), ReqType("application/json"))
    assert (checker, Start, URL("c"), Method("GET"))
    and("ReqTypeFail states should be after PUT and POST states")
    assert (checker, Start, URL("a"), URL("b"), Method("PUT"), ReqTypeFail)
    assert (checker, Start, URL("a"), URL("b"), Method("PUT"), ReqTypeFail)
    assert (checker, Start, URL("a"), URL("b"), Method("POST"), ReqTypeFail)
    assert (checker, Start, URL("c"), Method("POST"), ReqTypeFail)
  }

  scenario("The WADL contains PUT and POST operations accepting various media types") {
    given ("a WADL that contains multiple PUT and POST operation with various media types")
    val inWADL =
      <application xmlns="http://wadl.dev.java.net/2009/02">
        <grammars/>
        <resources base="https://test.api.openstack.com">
           <resource path="/a/b">
               <method name="PUT">
                  <request>
                      <representation mediaType="application/xml"/>
                      <representation mediaType="application/json"/>
                  </request>
               </method>
               <method name="POST">
                  <request>
                      <representation mediaType="application/xml"/>
                  </request>
               </method>
           </resource>
           <resource path="/c">
               <method name="POST">
                  <request>
                      <representation mediaType="application/json"/>
                  </request>
               </method>
               <method name="GET"/>
           </resource>
        </resources>
    </application>
    when("the wadl is translated")
    val checker = builder.build (inWADL, stdConfig)
    reqTypeAssertions(checker)
    and("The following assertions should also hold:")
    assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='POST']) = 2")
    assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='PUT']) = 1")
    assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='GET']) = 1")
    assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE' and @match='(?i)application/xml']) = 2")
    assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE' and @match='(?i)application/json']) = 2")
    assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)application/json']) = 1")
    assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)application/xml']) = 1")
    assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)application/xml|(?i)application/json']) = 1")
  }

  scenario("The WADL contains PUT and POST operations accepting various media types, with dups optimization on") {
    given ("a WADL that contains multiple PUT and POST operation with various media types")
    val inWADL =
      <application xmlns="http://wadl.dev.java.net/2009/02">
        <grammars/>
        <resources base="https://test.api.openstack.com">
           <resource path="/a/b">
               <method name="PUT">
                  <request>
                      <representation mediaType="application/xml"/>
                      <representation mediaType="application/json"/>
                  </request>
               </method>
               <method name="POST">
                  <request>
                      <representation mediaType="application/xml"/>
                  </request>
               </method>
           </resource>
           <resource path="/c">
               <method name="POST">
                  <request>
                      <representation mediaType="application/json"/>
                  </request>
               </method>
               <method name="GET"/>
           </resource>
        </resources>
    </application>
    when("the wadl is translated")
    val checker = builder.build (inWADL, dupConfig)
    reqTypeAssertions(checker)
    and("The following assertions should also hold:")
    assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='POST']) = 2")
    assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='PUT']) = 1")
    assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='GET']) = 1")
    assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE' and @match='(?i)application/xml']) = 1")
    assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE' and @match='(?i)application/json']) = 1")
    assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)application/json']) = 1")
    assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)application/xml']) = 1")
    assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)application/xml|(?i)application/json']) = 1")
  }

  //
  //  The following scenarios test the remove duplicate optimization
  //  when there are no duplicates. They are equivalent but they are
  //  written in slightly different WADL form the assertions below
  //  must apply to all of them.
  //

  def noDupAsserts(checker : NodeSeq) : Unit = {
    then("The paths in both resources should match")
    assert (checker, Start, URL("path"), URL("to"), URL("my"), URL("resource"), Method("GET"))
    assert (checker, "count(//chk:step[@type='URL' and @match='path']) = 1")
    assert (checker, "count(//chk:step[@type='URL' and @match='to']) = 1")
    assert (checker, "count(//chk:step[@type='URL' and @match='my']) = 1")
    assert (checker, "count(//chk:step[@type='URL' and @match='resource']) = 1")
    assert (checker, "count(//chk:step[@type='METHOD' and @match='GET']) = 1")
  }

  scenario("The WADL does not contain any duplicate resources, but remove dup optimization is on") {
    given("a WADL that contains no duplicate nodes")
    val inWADL =
        <application xmlns="http://wadl.dev.java.net/2009/02">
           <grammars/>
           <resources base="https://test.api.openstack.com">
              <resource path="path/to/my/resource">
                     <method name="GET">
                        <response status="200 203"/>
                     </method>
              </resource>
           </resources>
        </application>
    when("the WADL is translated, with dup remove on")
    val checker_dupon = builder.build(inWADL, dupConfig)
    and ("the WADL is translated, with dup remove off")
    val checker_dupoff = builder.build(inWADL, stdConfig)

    noDupAsserts(checker_dupon)
    noDupAsserts(checker_dupoff)
  }

  scenario("The WADL, in tree format, does not contain any duplicate nodes, but remove dup optimization is on") {
    given("a WADL in tree format that contains no duplicate nodes")
    val inWADL =
        <application xmlns="http://wadl.dev.java.net/2009/02">
           <grammars/>
           <resources base="https://test.api.openstack.com">
              <resource path="path">
                <resource path="to">
                  <resource path="my">
                   <resource path="resource">
                     <method name="GET">
                        <response status="200 203"/>
                     </method>
                   </resource>
                 </resource>
                </resource>
              </resource>
           </resources>
        </application>
    when("the WADL is translated, with dup remove on")
    val checker_dupon = builder.build(inWADL, dupConfig)
    and ("the WADL is translated, with dup remove off")
    val checker_dupoff = builder.build(inWADL, stdConfig)

    noDupAsserts(checker_dupon)
    noDupAsserts(checker_dupoff)
  }

  scenario("The WADL, in mix format, does not contain any duplicate nodes, but remove dup optimization is on") {
    given("a WADL in mix format that contains no duplicate nodes")
    val inWADL =
        <application xmlns="http://wadl.dev.java.net/2009/02">
           <grammars/>
           <resources base="https://test.api.openstack.com">
              <resource path="path/to/my">
                  <resource path="resource">
                     <method name="GET">
                        <response status="200 203"/>
                     </method>
                  </resource>
              </resource>
           </resources>
        </application>
    when("the WADL is translated, with dup remove on")
    val checker_dupon = builder.build(inWADL, dupConfig)
    and ("the WADL is translated, with dup remove off")
    val checker_dupoff = builder.build(inWADL, stdConfig)

    noDupAsserts(checker_dupon)
    noDupAsserts(checker_dupoff)
  }

  //
  //  The following scenarios test the remove duplicate optimization
  //  when there is a single duplicate. They are equivalent but they are
  //  written in slightly different WADL form the assertions below
  //  must apply to all of them.
  //

  def singleDupAsserts(checker_dupon : NodeSeq, checker_dupoff : NodeSeq) : Unit = {
    then("Paths should exist in both checkers")
    assert (checker_dupon, Start, URL("path"), URL("to"), URL("my"), URL("resource"), Method("GET"))
    assert (checker_dupon, Start, URL("path"), URL("to"), URL("another"), URL("resource"), Method("GET"))
    assert (checker_dupoff, Start, URL("path"), URL("to"), URL("my"), URL("resource"), Method("GET"))
    assert (checker_dupoff, Start, URL("path"), URL("to"), URL("another"), URL("resource"), Method("GET"))

    and("there should be a duplicate resource node when dup is off")
    assert (checker_dupoff, "count(//chk:step[@type='URL' and @match='path']) = 1")
    assert (checker_dupoff, "count(//chk:step[@type='URL' and @match='to']) = 1")
    assert (checker_dupoff, "count(//chk:step[@type='URL' and @match='my']) = 1")
    assert (checker_dupoff, "count(//chk:step[@type='URL' and @match='resource']) = 2")
    assert (checker_dupoff, "count(//chk:step[@type='URL' and @match='another']) = 1")
    assert (checker_dupoff, "count(//chk:step[@type='METHOD' and @match='GET']) = 1")

    and("there should *not* be a duplicate resource node when dup is on")
    assert (checker_dupon, "count(//chk:step[@type='URL' and @match='path']) = 1")
    assert (checker_dupon, "count(//chk:step[@type='URL' and @match='to']) = 1")
    assert (checker_dupon, "count(//chk:step[@type='URL' and @match='my']) = 1")
    assert (checker_dupon, "count(//chk:step[@type='URL' and @match='resource']) = 1")
    assert (checker_dupon, "count(//chk:step[@type='URL' and @match='another']) = 1")
    assert (checker_dupon, "count(//chk:step[@type='METHOD' and @match='GET']) = 1")
  }

  scenario("The WADL contain a single duplicate and remove dup optimization is on") {
    given("a WADL that contains a single duplicate")
    val inWADL =
        <application xmlns="http://wadl.dev.java.net/2009/02">
           <grammars/>
           <resources base="https://test.api.openstack.com">
              <resource path="path/to/my/resource">
                     <method href="#res"/>
              </resource>
              <resource path="path/to/another/resource">
                     <method href="#res"/>
              </resource>
           </resources>
           <method id="res" name="GET">
                 <response status="200 203"/>
           </method>
        </application>
    when("the WADL is translated, with dup remove on")
    val checker_dupon = builder.build(inWADL, dupConfig)
    and ("the WADL is translated, with dup remove off")
    val checker_dupoff = builder.build(inWADL, stdConfig)

    singleDupAsserts(checker_dupon, checker_dupoff)
  }

  scenario("The WADL, in tree format, contains a single duplicate node and remove dup optimization is on") {
    given("a WADL in tree format contains a single duplicate")
    val inWADL =
        <application xmlns="http://wadl.dev.java.net/2009/02">
           <grammars/>
           <resources base="https://test.api.openstack.com">
              <resource path="path">
                <resource path="to">
                  <resource path="my">
                   <resource path="resource">
                     <method href="#res"/>
                   </resource>
                 </resource>
                 <resource path="another">
                   <resource path="resource">
                     <method href="#res"/>
                   </resource>
                 </resource>
                </resource>
              </resource>
           </resources>
           <method id="res" name="GET">
                 <response status="200 203"/>
           </method>
        </application>
    when("the WADL is translated, with dup remove on")
    val checker_dupon = builder.build(inWADL, dupConfig)
    and ("the WADL is translated, with dup remove off")
    val checker_dupoff = builder.build(inWADL, stdConfig)

    singleDupAsserts(checker_dupon, checker_dupoff)
  }

  scenario("The WADL, in mix format,  contain a single duplicate and remove dup optimization is on") {
    given("a WADL, in mix format,  that contains a single duplicate")
    val inWADL =
        <application xmlns="http://wadl.dev.java.net/2009/02">
           <grammars/>
           <resources base="https://test.api.openstack.com">
              <resource path="path/to/my">
                 <resource path="resource">
                     <method href="#res"/>
                 </resource>
              </resource>
              <resource path="path/to/another">
                 <resource path="resource">
                     <method href="#res"/>
                 </resource>
              </resource>
           </resources>
           <method id="res" name="GET">
                 <response status="200 203"/>
           </method>
        </application>
    when("the WADL is translated, with dup remove on")
    val checker_dupon = builder.build(inWADL, dupConfig)
    and ("the WADL is translated, with dup remove off")
    val checker_dupoff = builder.build(inWADL, stdConfig)

    singleDupAsserts(checker_dupon, checker_dupoff)
  }


  //
  //  The following scenarios test the remove duplicate optimization
  //  when there is a single duplicate. They are equivalent but they are
  //  written in slightly different WADL form the assertions below
  //  must apply to all of them.
  //

  def multipleDupAsserts(checker_dupon : NodeSeq, checker_dupoff : NodeSeq) : Unit = {
    then("Paths should exist in both checkers")
    assert (checker_dupon, Start, URL("path"), URL("to"), URL("my"), URL("resource"), Method("GET"))
    assert (checker_dupon, Start, URL("path"), URL("to"), URL("another"), URL("resource"), Method("GET"))
    assert (checker_dupon, Start, URL("what"), URL("about"), URL("this"), URL("resource"), Method("GET"))
    assert (checker_dupon, Start, URL("what"), URL("about"), URL("this"), URL("resource"), URL("resource2"), Method("GET"))
    assert (checker_dupon, Start, URL("and"), URL("another"), URL("resource"), Method("GET"))
    assert (checker_dupon, Start, URL("and"), URL("another"), URL("resource2"), Method("GET"))
    assert (checker_dupon, Start, URL("resource2"), Method("GET"))
    assert (checker_dupoff, Start, URL("path"), URL("to"), URL("my"), URL("resource"), Method("GET"))
    assert (checker_dupoff, Start, URL("path"), URL("to"), URL("another"), URL("resource"), Method("GET"))
    assert (checker_dupoff, Start, URL("what"), URL("about"), URL("this"), URL("resource"), Method("GET"))
    assert (checker_dupoff, Start, URL("what"), URL("about"), URL("this"), URL("resource"), URL("resource2"), Method("GET"))
    assert (checker_dupoff, Start, URL("and"), URL("another"), URL("resource"), Method("GET"))
    assert (checker_dupoff, Start, URL("and"), URL("another"), URL("resource2"), Method("GET"))
    assert (checker_dupoff, Start, URL("resource2"), Method("GET"))


    and("there should be a number of duplicate resource node when dup is off")
    assert (checker_dupoff, "count(//chk:step[@type='URL' and @match='resource']) = 4")
    assert (checker_dupoff, "count(//chk:step[@type='URL' and @match='resource2']) = 3")

    and("there should *not* be many duplicate resources dup is on, resource will be duplicate 'cus it's used in 2 different ways")
    assert (checker_dupon, "count(//chk:step[@type='URL' and @match='resource']) = 2")
    assert (checker_dupon, "count(//chk:step[@type='URL' and @match='resource2']) = 1")

    and("there should be the same number of methods in each")
    assert (checker_dupon, "count(//chk:step[@type='METHOD' and @match='GET']) = 1")
    assert (checker_dupoff, "count(//chk:step[@type='METHOD' and @match='GET']) = 2")
  }

  scenario("The WADL contain multiple duplicates and remove dup optimization is on") {
    given("a WADL that contains multiple duplicates")
    val inWADL =
        <application xmlns="http://wadl.dev.java.net/2009/02">
           <grammars/>
           <resources base="https://test.api.openstack.com">
              <resource path="path/to/my/resource">
                     <method href="#res"/>
              </resource>
              <resource path="path/to/another/resource">
                     <method href="#res"/>
              </resource>
              <resource path="and/another/resource">
                     <method href="#res"/>
              </resource>
              <resource path="what/about/this/resource">
                     <method href="#res"/>
              </resource>
              <resource path="what/about/this/resource/resource2">
                     <method href="#res2"/>
              </resource>
              <resource path="and/another/resource2">
                     <method href="#res2"/>
              </resource>
              <resource path="resource2">
                     <method href="#res2"/>
              </resource>
           </resources>
           <method id="res" name="GET">
                 <response status="200 203"/>
           </method>
           <method id="res2" name="GET">
                 <response status="200 203"/>
           </method>
        </application>
    when("the WADL is translated, with dup remove on")
    val checker_dupon = builder.build(inWADL, dupConfig)
    and ("the WADL is translated, with dup remove off")
    val checker_dupoff = builder.build(inWADL, stdConfig)

    multipleDupAsserts(checker_dupon, checker_dupoff)
  }

  scenario("The WADL, in tree format, contains multiple duplicates and remove dup optimization is on") {
    given("a WADL in tree format contains multiple duplicates")
    val inWADL =
        <application xmlns="http://wadl.dev.java.net/2009/02">
           <grammars/>
           <resources base="https://test.api.openstack.com">
              <resource path="resource2">
                 <method href="#res2"/>
              </resource>
              <resource path="and">
                  <resource path="another">
                   <resource path="resource">
                     <method href="#res"/>
                   </resource>
                   <resource path="resource2">
                      <method href="#res2"/>
                   </resource>
                 </resource>
              </resource>
              <resource path="what">
                <resource path="about">
                  <resource path="this">
                    <resource path="resource">
                      <method href="#res"/>
                      <resource path="resource2">
                        <method href="#res2"/>
                      </resource>
                    </resource>
                  </resource>
                </resource>
              </resource>
              <resource path="path">
                <resource path="to">
                  <resource path="another">
                   <resource path="resource">
                     <method href="#res"/>
                   </resource>
                  </resource>
                  <resource path="my">
                   <resource path="resource">
                     <method href="#res"/>
                   </resource>
                 </resource>
                 <resource path="another">
                   <resource path="resource">
                     <method href="#res"/>
                   </resource>
                 </resource>
                </resource>
              </resource>
           </resources>
           <method id="res" name="GET">
                 <response status="200 203"/>
           </method>
           <method id="res2" name="GET">
                 <response status="200 203"/>
           </method>
        </application>
    when("the WADL is translated, with dup remove on")
    val checker_dupon = builder.build(inWADL, dupConfig)
    and ("the WADL is translated, with dup remove off")
    val checker_dupoff = builder.build(inWADL, stdConfig)

    multipleDupAsserts(checker_dupon, checker_dupoff)
  }

  scenario("The WADL, in mixed format, contains multiple duplicates and remove dup optimization is on") {
    given("a WADL, in mixed format, that contains multiple duplicates")
    val inWADL =
        <application xmlns="http://wadl.dev.java.net/2009/02">
           <grammars/>
           <resources base="https://test.api.openstack.com">
              <resource path="path/to">
                  <resource path="my/resource">
                     <method href="#res"/>
                  </resource>
                  <resource path="another/resource">
                     <method href="#res"/>
                  </resource>
              </resource>
              <resource path="and/another/resource">
                     <method href="#res"/>
              </resource>
              <resource path="what/about/this/resource">
                     <method href="#res"/>
                     <resource path="resource2">
                        <method href="#res2"/>
                     </resource>
              </resource>
              <resource path="and/another/resource2">
                     <method href="#res2"/>
              </resource>
              <resource path="resource2">
                     <method href="#res2"/>
              </resource>
           </resources>
           <method id="res" name="GET">
                 <response status="200 203"/>
           </method>
           <method id="res2" name="GET">
                 <response status="200 203"/>
           </method>
        </application>
    when("the WADL is translated, with dup remove on")
    val checker_dupon = builder.build(inWADL, dupConfig)
    and ("the WADL is translated, with dup remove off")
    val checker_dupoff = builder.build(inWADL, stdConfig)

    multipleDupAsserts(checker_dupon, checker_dupoff)
  }

  scenario("The WADL contains a single resource with multiple methods of the same type") {
      given("a WADL that contains a single resource with multiple methods of the same type")
      val inWADL =
        <application xmlns="http://wadl.dev.java.net/2009/02">
           <grammars/>
           <resources base="https://test.api.openstack.com">
              <resource path="path/to/my/resource">
                   <method id="action1" name="POST">
                      <response status="201"/>
                   </method>
                   <method id="action2" name="POST">
                      <response status="201"/>
                   </method>
                   <method id="action3" name="POST">
                      <response status="201"/>
                   </method>
                   <method id="action4" name="POST">
                      <response status="201"/>
                   </method>
              </resource>
           </resources>
        </application>
      when("the wadl is translated")
      val checker = builder.build (inWADL, stdConfig)
      then("There should be a total of 5 POST method steps...4 specified plus 1 collecting them")
      assert(checker, "count(//chk:step[@type='METHOD']) = 5")
      and ("All paths should go through POST step.")
      assert(checker, Start, URL("path"), URL("to"), URL("my"), URL("resource"), Method("POST"), Method("POST"))
      assert(checker, Start, URL("path"), URL("to"), URL("my"), URL("resource"), Method("POST"), Label("action1"))
      assert(checker, Start, URL("path"), URL("to"), URL("my"), URL("resource"), Method("POST"), Label("action2"))
      assert(checker, Start, URL("path"), URL("to"), URL("my"), URL("resource"), Method("POST"), Label("action3"))
      assert(checker, Start, URL("path"), URL("to"), URL("my"), URL("resource"), Method("POST"), Label("action4"))
      and ("One of the post steps is labeled ε, the rest are labeled according to thier ID")
      assert(checker, "//chk:step[@type='METHOD' and @label='action1']")
      assert(checker, "//chk:step[@type='METHOD' and @label='action2']")
      assert(checker, "//chk:step[@type='METHOD' and @label='action3']")
      assert(checker, "//chk:step[@type='METHOD' and @label='action4']")
      assert(checker, "//chk:step[@type='METHOD' and @label='ε']")
    }

  //
  //  The following assertions are used to test WellFormXML and
  //  ContentError nodes.  They are used in the next couple of tests.
  //
  def wellFormedAssertions(checker : NodeSeq) : Unit = {
    and("The machine should contain paths to WellXML and WELLJSON types")
    assert (checker, Start, URL("a"), URL("b"), Method("PUT"), ReqType("application/xml"), WellXML)
    assert (checker, Start, URL("a"), URL("b"), Method("PUT"), ReqType("application/json"), WellJSON)
    assert (checker, Start, URL("a"), URL("b"), Method("POST"), ReqType("application/xml"), WellXML)
    assert (checker, Start, URL("c"), Method("POST"), ReqType("application/json"), WellJSON)
    and("There should be content failed states")
    assert (checker, Start, URL("a"), URL("b"), Method("PUT"), ReqType("application/xml"), ContentFail)
    assert (checker, Start, URL("a"), URL("b"), Method("PUT"), ReqType("application/json"), ContentFail)
    assert (checker, Start, URL("a"), URL("b"), Method("POST"), ReqType("application/xml"), ContentFail)
    assert (checker, Start, URL("c"), Method("POST"), ReqType("application/json"), ContentFail)
  }

  scenario("The WADL contains PUT and POST operations accepting various media types where well formness is checked") {
    given ("a WADL that contains multiple PUT and POST operation with various media types")
    val inWADL =
      <application xmlns="http://wadl.dev.java.net/2009/02">
        <grammars/>
        <resources base="https://test.api.openstack.com">
           <resource path="/a/b">
               <method name="PUT">
                  <request>
                      <representation mediaType="application/xml"/>
                      <representation mediaType="application/json"/>
                  </request>
               </method>
               <method name="POST">
                  <request>
                      <representation mediaType="application/xml"/>
                  </request>
               </method>
           </resource>
           <resource path="/c">
               <method name="POST">
                  <request>
                      <representation mediaType="application/json"/>
                  </request>
               </method>
               <method name="GET"/>
           </resource>
        </resources>
    </application>
    when("the wadl is translated")
    val checker = builder.build (inWADL, TestConfig(false, false, true))
    reqTypeAssertions(checker)
    wellFormedAssertions(checker)
    and("The following assertions should also hold:")
    assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='POST']) = 2")
    assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='PUT']) = 1")
    assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='GET']) = 1")
    assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE' and @match='(?i)application/xml']) = 2")
    assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE' and @match='(?i)application/json']) = 2")
    assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)application/json']) = 1")
    assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)application/xml']) = 1")
    assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)application/xml|(?i)application/json']) = 1")
    assert (checker, "count(/chk:checker/chk:step[@type='WELL_XML']) = 2")
    assert (checker, "count(/chk:checker/chk:step[@type='WELL_JSON']) = 2")
    assert (checker, "count(/chk:checker/chk:step[@type='CONTENT_FAIL']) = 4")
  }

  scenario("The WADL contains PUT and POST operations accepting various media types where well formness is checked, with dupson") {
    given ("a WADL that contains multiple PUT and POST operation with various media types")
    val inWADL =
      <application xmlns="http://wadl.dev.java.net/2009/02">
        <grammars/>
        <resources base="https://test.api.openstack.com">
           <resource path="/a/b">
               <method name="PUT">
                  <request>
                      <representation mediaType="application/xml"/>
                      <representation mediaType="application/json"/>
                  </request>
               </method>
               <method name="POST">
                  <request>
                      <representation mediaType="application/xml"/>
                  </request>
               </method>
           </resource>
           <resource path="/c">
               <method name="POST">
                  <request>
                      <representation mediaType="application/json"/>
                  </request>
               </method>
               <method name="GET"/>
           </resource>
        </resources>
    </application>
    when("the wadl is translated")
    val checker = builder.build (inWADL, TestConfig(true, false, true))
    reqTypeAssertions(checker)
    wellFormedAssertions(checker)
    and("The following assertions should also hold:")
    assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='POST']) = 2")
    assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='PUT']) = 1")
    assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='GET']) = 1")
    assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE' and @match='(?i)application/xml']) = 1")
    assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE' and @match='(?i)application/json']) = 1")
    assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)application/json']) = 1")
    assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)application/xml']) = 1")
    assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)application/xml|(?i)application/json']) = 1")
    assert (checker, "count(/chk:checker/chk:step[@type='WELL_XML']) = 1")
    assert (checker, "count(/chk:checker/chk:step[@type='WELL_JSON']) = 1")
    assert (checker, "count(/chk:checker/chk:step[@type='CONTENT_FAIL']) = 1")
  }

  //
  // The following assertions are used to test XSD and ContentError
  // nodes. They are used in the next couple of tests.
  //
  def xsdAssertions(checker : NodeSeq) : Unit = {
    and("The machine should cantain paths to XSD types")
    assert (checker, Start, URL("a"), URL("b"), Method("PUT"), ReqType("application/xml"), WellXML, XSD, Accept)
    assert (checker, Start, URL("a"), URL("b"), Method("POST"), ReqType("application/xml"), WellXML, XSD, Accept)
    assert (checker, Start, URL("a"), URL("b"), Method("PUT"), ReqType("application/xml"), WellXML, ContentFail)
    assert (checker, Start, URL("a"), URL("b"), Method("POST"), ReqType("application/xml"), WellXML, ContentFail)
  }

  scenario("The WADL contains PUT and POST operations accepting xml which must validate against an XSD") {
    given ("a WADL that contains multiple PUT and POST operation with XML that must validate against an XSD")
    val inWADL =
      <application xmlns="http://wadl.dev.java.net/2009/02">
        <grammars>
            <include href="src/test/resources/xsd/test-urlxsd.xsd"/>
        </grammars>
        <resources base="https://test.api.openstack.com">
           <resource path="/a/b">
               <method name="PUT">
                  <request>
                      <representation mediaType="application/xml"/>
                      <representation mediaType="application/json"/>
                  </request>
               </method>
               <method name="POST">
                  <request>
                      <representation mediaType="application/xml"/>
                  </request>
               </method>
           </resource>
           <resource path="/c">
               <method name="POST">
                  <request>
                      <representation mediaType="application/json"/>
                  </request>
               </method>
               <method name="GET"/>
           </resource>
        </resources>
    </application>
    register("test://app/src/test/resources/xsd/test-urlxsd.xsd",
             XML.loadFile("src/test/resources/xsd/test-urlxsd.xsd"))
    when("the wadl is translated")
    val checker = builder.build (inWADL, TestConfig(false, false, true, true))
    reqTypeAssertions(checker)
    wellFormedAssertions(checker)
    xsdAssertions(checker)
    and("The following assertions should also hold:")
    assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='POST']) = 2")
    assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='PUT']) = 1")
    assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='GET']) = 1")
    assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE' and @match='(?i)application/xml']) = 2")
    assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE' and @match='(?i)application/json']) = 2")
    assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)application/json']) = 1")
    assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)application/xml']) = 1")
    assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)application/xml|(?i)application/json']) = 1")
    assert (checker, "count(/chk:checker/chk:step[@type='WELL_XML']) = 2")
    assert (checker, "count(/chk:checker/chk:step[@type='WELL_JSON']) = 2")
    assert (checker, "count(/chk:checker/chk:step[@type='XSD']) = 2")
    assert (checker, "count(/chk:checker/chk:step[@type='CONTENT_FAIL']) = 4")
  }

  scenario("The WADL contains PUT and POST operations accepting xml which must validate against an XSD, but no grammar is actually specified") {
    given ("a WADL that contains multiple PUT and POST operation with XML that must validate against an XSD")
    val inWADL =
      <application xmlns="http://wadl.dev.java.net/2009/02">
        <grammars>
        </grammars>
        <resources base="https://test.api.openstack.com">
           <resource path="/a/b">
               <method name="PUT">
                  <request>
                      <representation mediaType="application/xml"/>
                      <representation mediaType="application/json"/>
                  </request>
               </method>
               <method name="POST">
                  <request>
                      <representation mediaType="application/xml"/>
                  </request>
               </method>
           </resource>
           <resource path="/c">
               <method name="POST">
                  <request>
                      <representation mediaType="application/json"/>
                  </request>
               </method>
               <method name="GET"/>
           </resource>
        </resources>
    </application>
    when("the wadl is translated")
    val checker = builder.build (inWADL, TestConfig(false, false, true, true))
    reqTypeAssertions(checker)
    wellFormedAssertions(checker)
    and("The following assertions should also hold:")
    assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='POST']) = 2")
    assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='PUT']) = 1")
    assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='GET']) = 1")
    assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE' and @match='(?i)application/xml']) = 2")
    assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE' and @match='(?i)application/json']) = 2")
    assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)application/json']) = 1")
    assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)application/xml']) = 1")
    assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)application/xml|(?i)application/json']) = 1")
    assert (checker, "count(/chk:checker/chk:step[@type='WELL_XML']) = 2")
    assert (checker, "count(/chk:checker/chk:step[@type='WELL_JSON']) = 2")
    assert (checker, "count(/chk:checker/chk:step[@type='CONTENT_FAIL']) = 4")
    and("There should be no XSD steps, since no grammar was specified")
    assert (checker, "count(/chk:checker/chk:step[@type='XSD']) = 0")
  }



  scenario("The WADL contains PUT and POST operations accepting xml which must validate against an XSD, with dupson") {
    given ("a WADL that contains multiple PUT and POST operation with XML that must validate against an XSD, with dupson")
    val inWADL =
      <application xmlns="http://wadl.dev.java.net/2009/02">
        <grammars>
            <include href="src/test/resources/xsd/test-urlxsd.xsd"/>
        </grammars>
        <resources base="https://test.api.openstack.com">
           <resource path="/a/b">
               <method name="PUT">
                  <request>
                      <representation mediaType="application/xml"/>
                      <representation mediaType="application/json"/>
                  </request>
               </method>
               <method name="POST">
                  <request>
                      <representation mediaType="application/xml"/>
                  </request>
               </method>
           </resource>
           <resource path="/c">
               <method name="POST">
                  <request>
                      <representation mediaType="application/json"/>
                  </request>
               </method>
               <method name="GET"/>
           </resource>
        </resources>
    </application>
    register("test://app/src/test/resources/xsd/test-urlxsd.xsd",
             XML.loadFile("src/test/resources/xsd/test-urlxsd.xsd"))
    when("the wadl is translated")
    val checker = builder.build (inWADL, TestConfig(true, false, true, true))
    reqTypeAssertions(checker)
    wellFormedAssertions(checker)
    xsdAssertions(checker)
    and("The following assertions should also hold:")
    assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='POST']) = 2")
    assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='PUT']) = 1")
    assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='GET']) = 1")
    assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE' and @match='(?i)application/xml']) = 1")
    assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE' and @match='(?i)application/json']) = 1")
    assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)application/json']) = 1")
    assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)application/xml']) = 1")
    assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)application/xml|(?i)application/json']) = 1")
    assert (checker, "count(/chk:checker/chk:step[@type='WELL_XML']) = 1")
    assert (checker, "count(/chk:checker/chk:step[@type='WELL_JSON']) = 1")
    assert (checker, "count(/chk:checker/chk:step[@type='XSD']) = 1")
    assert (checker, "count(/chk:checker/chk:step[@type='CONTENT_FAIL']) = 1")
  }

  //
  // The following assertions are used to test XSD, XPath and ContentError
  // nodes. They are used in the next couple of tests.
  //
  def xsdElementAssertions(checker : NodeSeq) : Unit = {
    and("The machine should cantain paths to XSD types")
    assert (checker, Start, URL("a"), URL("b"), Method("PUT"), ReqType("application/xml"), WellXML, XPath("/tst:e"), XSD, Accept)
    assert (checker, Start, URL("a"), URL("b"), Method("POST"), ReqType("application/xml"), WellXML, XPath("/tst:a"), XSD, Accept)
    assert (checker, Start, URL("a"), URL("b"), Method("PUT"), ReqType("application/xml"), WellXML, ContentFail)
    assert (checker, Start, URL("a"), URL("b"), Method("POST"), ReqType("application/xml"), WellXML, ContentFail)
    assert (checker, Start, URL("a"), URL("b"), Method("PUT"), ReqType("application/xml"), WellXML, XPath("/tst:e"), ContentFail)
    assert (checker, Start, URL("a"), URL("b"), Method("POST"), ReqType("application/xml"), WellXML, XPath("/tst:a"), ContentFail)
  }

  //
  //  Like the assertions above but only one XPath type is specified
  //
  def xsdElementAssertions2(checker : NodeSeq) : Unit = {
    and("The machine should cantain paths to XSD types")
    assert (checker, Start, URL("a"), URL("b"), Method("PUT"), ReqType("application/xml"), WellXML, XPath("/tst:e"), XSD, Accept)
    assert (checker, Start, URL("a"), URL("b"), Method("POST"), ReqType("application/xml"), WellXML, XPath("/tst:e"), XSD, Accept)
    assert (checker, Start, URL("a"), URL("b"), Method("PUT"), ReqType("application/xml"), WellXML, ContentFail)
    assert (checker, Start, URL("a"), URL("b"), Method("POST"), ReqType("application/xml"), WellXML, ContentFail)
    assert (checker, Start, URL("a"), URL("b"), Method("PUT"), ReqType("application/xml"), WellXML, XPath("/tst:e"), ContentFail)
    assert (checker, Start, URL("a"), URL("b"), Method("POST"), ReqType("application/xml"), WellXML, XPath("/tst:e"), ContentFail)
  }

  //
  //  Here, were not doing XSD validation
  //
  def elementAssertions(checker : NodeSeq) : Unit = {
    and("The machine should cantain paths to XSD types")
    assert (checker, Start, URL("a"), URL("b"), Method("PUT"), ReqType("application/xml"), WellXML, XPath("/tst:e"), Accept)
    assert (checker, Start, URL("a"), URL("b"), Method("POST"), ReqType("application/xml"), WellXML, XPath("/tst:a"), Accept)
    assert (checker, Start, URL("a"), URL("b"), Method("PUT"), ReqType("application/xml"), WellXML, ContentFail)
    assert (checker, Start, URL("a"), URL("b"), Method("POST"), ReqType("application/xml"), WellXML, ContentFail)
  }

  scenario("The WADL contains PUT and POST operations accepting xml which must validate against an XSD with elements specified and checked") {
    given ("a WADL that contains multiple PUT and POST operation with XML that must validate against an XSD with elemenst specified")
    val inWADL =
      <application xmlns="http://wadl.dev.java.net/2009/02"
                   xmlns:tst="http://www.rackspace.com/repose/wadl/checker/step/test">
        <grammars>
            <include href="src/test/resources/xsd/test-urlxsd.xsd"/>
        </grammars>
        <resources base="https://test.api.openstack.com">
           <resource path="/a/b">
               <method name="PUT">
                  <request>
                      <representation mediaType="application/xml" element="tst:e"/>
                      <representation mediaType="application/json"/>
                  </request>
               </method>
               <method name="POST">
                  <request>
                      <representation mediaType="application/xml" element="tst:a"/>
                  </request>
               </method>
           </resource>
           <resource path="/c">
               <method name="POST">
                  <request>
                      <representation mediaType="application/json"/>
                  </request>
               </method>
               <method name="GET"/>
           </resource>
        </resources>
    </application>
    register("test://app/src/test/resources/xsd/test-urlxsd.xsd",
             XML.loadFile("src/test/resources/xsd/test-urlxsd.xsd"))
    when("the wadl is translated")
    val checker = builder.build (inWADL, TestConfig(false, false, true, true, true))
    reqTypeAssertions(checker)
    wellFormedAssertions(checker)
    xsdElementAssertions(checker)
    and("The following assertions should also hold:")
    assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='POST']) = 2")
    assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='PUT']) = 1")
    assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='GET']) = 1")
    assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE' and @match='(?i)application/xml']) = 2")
    assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE' and @match='(?i)application/json']) = 2")
    assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)application/json']) = 1")
    assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)application/xml']) = 1")
    assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)application/xml|(?i)application/json']) = 1")
    assert (checker, "count(/chk:checker/chk:step[@type='WELL_XML']) = 2")
    assert (checker, "count(/chk:checker/chk:step[@type='WELL_JSON']) = 2")
    assert (checker, "count(/chk:checker/chk:step[@type='XSD']) = 2")
    assert (checker, "count(/chk:checker/chk:step[@type='XPATH']) = 2")
    assert (checker, "count(/chk:checker/chk:step[@type='XPATH' and @match='/tst:e']) = 1")
    assert (checker, "count(/chk:checker/chk:step[@type='XPATH' and @match='/tst:a']) = 1")
    assert (checker, "count(/chk:checker/chk:step[@type='CONTENT_FAIL']) = 4")
  }

  scenario("The WADL contains PUT and POST operations accepting xml which must validate against an XSD with elements specified and checked. The same element is used for POST and PUT") {
    given ("a WADL that contains multiple PUT and POST operation with XML that must validate against an XSD with elemenst specified")
    val inWADL =
      <application xmlns="http://wadl.dev.java.net/2009/02"
                   xmlns:tst="http://www.rackspace.com/repose/wadl/checker/step/test">
        <grammars>
            <include href="src/test/resources/xsd/test-urlxsd.xsd"/>
        </grammars>
        <resources base="https://test.api.openstack.com">
           <resource path="/a/b">
               <method name="PUT">
                  <request>
                      <representation mediaType="application/xml" element="tst:e"/>
                      <representation mediaType="application/json"/>
                  </request>
               </method>
               <method name="POST">
                  <request>
                      <representation mediaType="application/xml" element="tst:e"/>
                  </request>
               </method>
           </resource>
           <resource path="/c">
               <method name="POST">
                  <request>
                      <representation mediaType="application/json"/>
                  </request>
               </method>
               <method name="GET"/>
           </resource>
        </resources>
    </application>
    register("test://app/src/test/resources/xsd/test-urlxsd.xsd",
             XML.loadFile("src/test/resources/xsd/test-urlxsd.xsd"))
    when("the wadl is translated")
    val checker = builder.build (inWADL, TestConfig(false, false, true, true, true))
    reqTypeAssertions(checker)
    wellFormedAssertions(checker)
    xsdElementAssertions2(checker)
    and("The following assertions should also hold:")
    assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='POST']) = 2")
    assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='PUT']) = 1")
    assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='GET']) = 1")
    assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE' and @match='(?i)application/xml']) = 2")
    assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE' and @match='(?i)application/json']) = 2")
    assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)application/json']) = 1")
    assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)application/xml']) = 1")
    assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)application/xml|(?i)application/json']) = 1")
    assert (checker, "count(/chk:checker/chk:step[@type='WELL_XML']) = 2")
    assert (checker, "count(/chk:checker/chk:step[@type='WELL_JSON']) = 2")
    assert (checker, "count(/chk:checker/chk:step[@type='XSD']) = 2")
    assert (checker, "count(/chk:checker/chk:step[@type='XPATH']) = 2")
    assert (checker, "count(/chk:checker/chk:step[@type='XPATH' and @match='/tst:e']) = 2")
    assert (checker, "count(/chk:checker/chk:step[@type='XPATH' and @match='/tst:a']) = 0")
    assert (checker, "count(/chk:checker/chk:step[@type='CONTENT_FAIL']) = 4")
  }

  scenario("The WADL contains PUT and POST operations accepting xml which must validate against an XSD with elements specified and checked. The same element is used for POST and PUT, dups on") {
    given ("a WADL that contains multiple PUT and POST operation with XML that must validate against an XSD with elemenst specified")
    val inWADL =
      <application xmlns="http://wadl.dev.java.net/2009/02"
                   xmlns:tst="http://www.rackspace.com/repose/wadl/checker/step/test">
        <grammars>
            <include href="src/test/resources/xsd/test-urlxsd.xsd"/>
        </grammars>
        <resources base="https://test.api.openstack.com">
           <resource path="/a/b">
               <method name="PUT">
                  <request>
                      <representation mediaType="application/xml" element="tst:e"/>
                      <representation mediaType="application/json"/>
                  </request>
               </method>
               <method name="POST">
                  <request>
                      <representation mediaType="application/xml" element="tst:e"/>
                  </request>
               </method>
           </resource>
           <resource path="/c">
               <method name="POST">
                  <request>
                      <representation mediaType="application/json"/>
                  </request>
               </method>
               <method name="GET"/>
           </resource>
        </resources>
    </application>
    register("test://app/src/test/resources/xsd/test-urlxsd.xsd",
             XML.loadFile("src/test/resources/xsd/test-urlxsd.xsd"))
    when("the wadl is translated")
    val checker = builder.build (inWADL, TestConfig(true, false, true, true, true))
    reqTypeAssertions(checker)
    wellFormedAssertions(checker)
    xsdElementAssertions2(checker)
    and("The following assertions should also hold:")
    assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='POST']) = 2")
    assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='PUT']) = 1")
    assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='GET']) = 1")
    assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE' and @match='(?i)application/xml']) = 1")
    assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE' and @match='(?i)application/json']) = 1")
    assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)application/json']) = 1")
    assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)application/xml']) = 1")
    assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)application/xml|(?i)application/json']) = 1")
    assert (checker, "count(/chk:checker/chk:step[@type='WELL_XML']) = 1")
    assert (checker, "count(/chk:checker/chk:step[@type='WELL_JSON']) = 1")
    assert (checker, "count(/chk:checker/chk:step[@type='XSD']) = 1")
    assert (checker, "count(/chk:checker/chk:step[@type='XPATH']) = 1")
    assert (checker, "count(/chk:checker/chk:step[@type='XPATH' and @match='/tst:e']) = 1")
    assert (checker, "count(/chk:checker/chk:step[@type='XPATH' and @match='/tst:a']) = 0")
    assert (checker, "count(/chk:checker/chk:step[@type='CONTENT_FAIL']) = 1")
  }

  scenario("The WADL contains PUT and POST operations accepting xml no XSD validation with elements specified and checked") {
    given ("a WADL that contains multiple PUT and POST operation with XML elements specified")
    val inWADL =
      <application xmlns="http://wadl.dev.java.net/2009/02"
                   xmlns:tst="http://www.rackspace.com/repose/wadl/checker/step/test">
        <grammars>
            <include href="src/test/resources/xsd/test-urlxsd.xsd"/>
        </grammars>
        <resources base="https://test.api.openstack.com">
           <resource path="/a/b">
               <method name="PUT">
                  <request>
                      <representation mediaType="application/xml" element="tst:e"/>
                      <representation mediaType="application/json"/>
                  </request>
               </method>
               <method name="POST">
                  <request>
                      <representation mediaType="application/xml" element="tst:a"/>
                  </request>
               </method>
           </resource>
           <resource path="/c">
               <method name="POST">
                  <request>
                      <representation mediaType="application/json"/>
                  </request>
               </method>
               <method name="GET"/>
           </resource>
        </resources>
    </application>
    register("test://app/src/test/resources/xsd/test-urlxsd.xsd",
             XML.loadFile("src/test/resources/xsd/test-urlxsd.xsd"))
    when("the wadl is translated")
    val checker = builder.build (inWADL, TestConfig(false, false, true, false, true))
    reqTypeAssertions(checker)
    wellFormedAssertions(checker)
    elementAssertions(checker)
    and("The following assertions should also hold:")
    assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='POST']) = 2")
    assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='PUT']) = 1")
    assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='GET']) = 1")
    assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE' and @match='(?i)application/xml']) = 2")
    assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE' and @match='(?i)application/json']) = 2")
    assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)application/json']) = 1")
    assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)application/xml']) = 1")
    assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)application/xml|(?i)application/json']) = 1")
    assert (checker, "count(/chk:checker/chk:step[@type='WELL_XML']) = 2")
    assert (checker, "count(/chk:checker/chk:step[@type='WELL_JSON']) = 2")
    assert (checker, "count(/chk:checker/chk:step[@type='XSD']) = 0")
    assert (checker, "count(/chk:checker/chk:step[@type='XPATH']) = 2")
    assert (checker, "count(/chk:checker/chk:step[@type='XPATH' and @match='/tst:e']) = 1")
    assert (checker, "count(/chk:checker/chk:step[@type='XPATH' and @match='/tst:a']) = 1")
    assert (checker, "count(/chk:checker/chk:step[@type='CONTENT_FAIL']) = 4")
  }

  scenario("The WADL contains PUT and POST operations accepting xml no XSD validation, no WELL Formed  with elements specified and checked") {
    given ("a WADL that contains multiple PUT and POST operation with XML elements specified")
    val inWADL =
      <application xmlns="http://wadl.dev.java.net/2009/02"
                   xmlns:tst="http://www.rackspace.com/repose/wadl/checker/step/test">
        <grammars>
            <include href="src/test/resources/xsd/test-urlxsd.xsd"/>
        </grammars>
        <resources base="https://test.api.openstack.com">
           <resource path="/a/b">
               <method name="PUT">
                  <request>
                      <representation mediaType="application/xml" element="tst:e"/>
                      <representation mediaType="application/json"/>
                  </request>
               </method>
               <method name="POST">
                  <request>
                      <representation mediaType="application/xml" element="tst:a"/>
                  </request>
               </method>
           </resource>
           <resource path="/c">
               <method name="POST">
                  <request>
                      <representation mediaType="application/json"/>
                  </request>
               </method>
               <method name="GET"/>
           </resource>
        </resources>
    </application>
    register("test://app/src/test/resources/xsd/test-urlxsd.xsd",
             XML.loadFile("src/test/resources/xsd/test-urlxsd.xsd"))
    when("the wadl is translated")
    val checker = builder.build (inWADL, TestConfig(false, false, false, false, true))
    reqTypeAssertions(checker)
    wellFormedAssertions(checker)
    elementAssertions(checker)
    and("The following assertions should also hold:")
    assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='POST']) = 2")
    assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='PUT']) = 1")
    assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='GET']) = 1")
    assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE' and @match='(?i)application/xml']) = 2")
    assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE' and @match='(?i)application/json']) = 2")
    assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)application/json']) = 1")
    assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)application/xml']) = 1")
    assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)application/xml|(?i)application/json']) = 1")
    assert (checker, "count(/chk:checker/chk:step[@type='WELL_XML']) = 2")
    assert (checker, "count(/chk:checker/chk:step[@type='WELL_JSON']) = 2")
    assert (checker, "count(/chk:checker/chk:step[@type='XSD']) = 0")
    assert (checker, "count(/chk:checker/chk:step[@type='XPATH']) = 2")
    assert (checker, "count(/chk:checker/chk:step[@type='XPATH' and @match='/tst:e']) = 1")
    assert (checker, "count(/chk:checker/chk:step[@type='XPATH' and @match='/tst:a']) = 1")
    assert (checker, "count(/chk:checker/chk:step[@type='CONTENT_FAIL']) = 4")
  }

  scenario("The WADL contains PUT and POST operations accepting xml which must validate against an XSD with elements checked, but none specified") {
    given ("a WADL that contains multiple PUT and POST operation with XML that must validate against an XSD")
    val inWADL =
      <application xmlns="http://wadl.dev.java.net/2009/02"
                   xmlns:tst="http://www.rackspace.com/repose/wadl/checker/step/test">
        <grammars>
            <include href="src/test/resources/xsd/test-urlxsd.xsd"/>
        </grammars>
        <resources base="https://test.api.openstack.com">
           <resource path="/a/b">
               <method name="PUT">
                  <request>
                      <representation mediaType="application/xml" />
                      <representation mediaType="application/json"/>
                  </request>
               </method>
               <method name="POST">
                  <request>
                      <representation mediaType="application/xml" />
                  </request>
               </method>
           </resource>
           <resource path="/c">
               <method name="POST">
                  <request>
                      <representation mediaType="application/json"/>
                  </request>
               </method>
               <method name="GET"/>
           </resource>
        </resources>
    </application>
    register("test://app/src/test/resources/xsd/test-urlxsd.xsd",
             XML.loadFile("src/test/resources/xsd/test-urlxsd.xsd"))
    when("the wadl is translated")
    val checker = builder.build (inWADL, TestConfig(false, false, true, true, true))
    reqTypeAssertions(checker)
    wellFormedAssertions(checker)
    xsdAssertions(checker)
    and("The following assertions should also hold:")
    assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='POST']) = 2")
    assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='PUT']) = 1")
    assert (checker, "count(/chk:checker/chk:step[@type='METHOD' and @match='GET']) = 1")
    assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE' and @match='(?i)application/xml']) = 2")
    assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE' and @match='(?i)application/json']) = 2")
    assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)application/json']) = 1")
    assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)application/xml']) = 1")
    assert (checker, "count(/chk:checker/chk:step[@type='REQ_TYPE_FAIL' and @notMatch='(?i)application/xml|(?i)application/json']) = 1")
    assert (checker, "count(/chk:checker/chk:step[@type='WELL_XML']) = 2")
    assert (checker, "count(/chk:checker/chk:step[@type='WELL_JSON']) = 2")
    assert (checker, "count(/chk:checker/chk:step[@type='XSD']) = 2")
    assert (checker, "count(/chk:checker/chk:step[@type='XPATH']) = 0")
    assert (checker, "count(/chk:checker/chk:step[@type='CONTENT_FAIL']) = 4")
  }

  scenario("The WADL contains a POST  operation accepting xml which must validate against an XSD with elements specified and multiple required plain params") {
    given ("a WADL that contains a POST operation with XML that must validate against an XSD with elemenst specified and multiple plain params")
    val inWADL =
      <application xmlns="http://wadl.dev.java.net/2009/02"
                   xmlns:tst="http://www.rackspace.com/repose/wadl/checker/step/test">
        <grammars>
            <include href="src/test/resources/xsd/test-urlxsd.xsd"/>
        </grammars>
        <resources base="https://test.api.openstack.com">
           <resource path="/a/b">
               <method name="POST">
                  <request>
                      <representation mediaType="application/xml" element="tst:a">
                         <param name="id" style="plain" path="/tst:a/@id" required="true"/>
                         <param name="stepType" style="plain" path="/tst:a/@stepType" required="true"/>
                      </representation>
                  </request>
               </method>
           </resource>
        </resources>
    </application>
    register("test://app/src/test/resources/xsd/test-urlxsd.xsd",
             XML.loadFile("src/test/resources/xsd/test-urlxsd.xsd"))
    val checker = builder.build (inWADL, TestConfig(false, false, true, true, true, 1, true))
    assert(checker, "count(/chk:checker/chk:step[@type='XPATH']) = 3")
    assert(checker, "count(/chk:checker/chk:step[@type='XPATH' and @match='/tst:a']) = 1")
    assert(checker, "count(/chk:checker/chk:step[@type='XPATH' and @match='/tst:a/@id']) = 1")
    assert(checker, "count(/chk:checker/chk:step[@type='XPATH' and @match='/tst:a/@stepType']) = 1")
    assert (checker, Start, URL("a"), URL("b"), Method("POST"), ReqType("application/xml"), WellXML, XPath("/tst:a"),
            XPath("/tst:a/@id"), XPath("/tst:a/@stepType"), XSD, Accept)
    assert (checker, Start, URL("a"), URL("b"), Method("POST"), ReqType("application/xml"), ContentFail)
    assert (checker, Start, URL("a"), URL("b"), Method("POST"), ReqType("application/xml"), WellXML, ContentFail)
    assert (checker, Start, URL("a"), URL("b"), Method("POST"), ReqType("application/xml"), WellXML, XPath("/tst:a"), ContentFail)
    assert (checker, Start, URL("a"), URL("b"), Method("POST"), ReqType("application/xml"), WellXML, XPath("/tst:a"),
            XPath("/tst:a/@id"), ContentFail)
    assert (checker, Start, URL("a"), URL("b"), Method("POST"), ReqType("application/xml"), WellXML, XPath("/tst:a"),
            XPath("/tst:a/@id"), XPath("/tst:a/@stepType"), ContentFail)
  }

  scenario("The WADL contains a POST  operation accepting xml which must validate against an XSD with elements specified and multiple required plain params (different reps)") {
    given ("a WADL that contains a POST operation with XML that must validate against an XSD with elemenst specified and multiple plain params")
    val inWADL =
      <application xmlns="http://wadl.dev.java.net/2009/02"
                   xmlns:tst="http://www.rackspace.com/repose/wadl/checker/step/test">
        <grammars>
            <include href="src/test/resources/xsd/test-urlxsd.xsd"/>
        </grammars>
        <resources base="https://test.api.openstack.com">
           <resource path="/a/b">
               <method name="POST">
                  <request>
                      <representation mediaType="application/xml" element="tst:a">
                         <param name="id" style="plain" path="/tst:a/@id" required="true"/>
                      </representation>
                      <representation mediaType="application/xml" element="tst:e">
                         <param name="id" style="plain" path="/tst:e/tst:id" required="true"/>
                      </representation>
                  </request>
               </method>
           </resource>
        </resources>
    </application>
    register("test://app/src/test/resources/xsd/test-urlxsd.xsd",
             XML.loadFile("src/test/resources/xsd/test-urlxsd.xsd"))
    val checker = builder.build (inWADL, TestConfig(false, false, true, true, true, 1, true))
    assert(checker, "count(/chk:checker/chk:step[@type='XPATH']) = 4")
    assert(checker, "count(/chk:checker/chk:step[@type='XPATH' and @match='/tst:a']) = 1")
    assert(checker, "count(/chk:checker/chk:step[@type='XPATH' and @match='/tst:a/@id']) = 1")
    assert(checker, "count(/chk:checker/chk:step[@type='XPATH' and @match='/tst:e']) = 1")
    assert(checker, "count(/chk:checker/chk:step[@type='XPATH' and @match='/tst:e/tst:id']) = 1")
    assert (checker, Start, URL("a"), URL("b"), Method("POST"), ReqType("application/xml"), WellXML, XPath("/tst:a"),
            XPath("/tst:a/@id"), XSD, Accept)
    assert (checker, Start, URL("a"), URL("b"), Method("POST"), ReqType("application/xml"), ContentFail)
    assert (checker, Start, URL("a"), URL("b"), Method("POST"), ReqType("application/xml"), WellXML, ContentFail)
    assert (checker, Start, URL("a"), URL("b"), Method("POST"), ReqType("application/xml"), WellXML, XPath("/tst:a"), ContentFail)
    assert (checker, Start, URL("a"), URL("b"), Method("POST"), ReqType("application/xml"), WellXML, XPath("/tst:a"),
            XPath("/tst:a/@id"), ContentFail)
    assert (checker, Start, URL("a"), URL("b"), Method("POST"), ReqType("application/xml"), WellXML, XPath("/tst:e"),
            XPath("/tst:e/tst:id"), XSD, Accept)
    assert (checker, Start, URL("a"), URL("b"), Method("POST"), ReqType("application/xml"), WellXML, XPath("/tst:e"), ContentFail)
    assert (checker, Start, URL("a"), URL("b"), Method("POST"), ReqType("application/xml"), WellXML, XPath("/tst:e"),
            XPath("/tst:e/tst:id"), ContentFail)
  }
}
