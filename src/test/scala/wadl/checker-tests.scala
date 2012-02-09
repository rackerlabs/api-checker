package com.rackspace.com.papi.components.checker.wadl

import scala.xml._
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.matchers.ShouldMatchers._


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
      val checker = builder.build (inWADL)
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
    //  /path/to/my/resource with a GET and a DELETE method. They are
    //  equivalent but they are written in slightly different WADL
    //  form the assertions below must apply to all of them.
    //

    def singlePathAssertions (checker : NodeSeq) : Unit = {
      then("The checker should contain an URL node for each path step")
      assert (checker, "count(/chk:checker/chk:step[@type='URL']) = 4")
      and ("The checker should contain a GET and a DELETE method")
      assert (checker, "/chk:checker/chk:step[@type='METHOD' and @match='GET']")
      assert (checker, "/chk:checker/chk:step[@type='METHOD' and @match='DELETE']")
      and ("The path from the start should contain all URL nodes in order")
      and ("it should end in the GET and a DELETE method node")
      assert (checker, Start, URL("path"), URL("to"), URL("my"), URL("resource"), Method("GET"))
      assert (checker, Start, URL("path"), URL("to"), URL("my"), URL("resource"), Method("DELETE"))
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
    }

    scenario("The WADL contains a single multi-path resource") {
      given("a WADL that contains a single multi-path resource with a GET and DELETE method")
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
           </resources>
        </application>
      when("the wadl is translated")
      val checker = builder.build (inWADL)
      singlePathAssertions(checker)
    }

    scenario("The WADL contains a single multi-path resource in tree form") {
      given("a WADL that contains a single multi-path resource in tree form with a GET and DELETE method")
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
           </resources>
        </application>
      when("the wadl is translated")
      val checker = builder.build (inWADL)
      singlePathAssertions(checker)
    }

    scenario("The WADL contains a single multi-path resource in mixed form") {
      given("a WADL that contains a single multi-path resource in mixed form with a GET and DELETE method")
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
           </resources>
        </application>
      when("the wadl is translated")
      val checker = builder.build (inWADL)
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
              </resource>
           </resources>
           <method id="getMethod" name="GET">
               <response status="200 203"/>
           </method>
        </application>
      when("the wadl is translated")
      val checker = builder.build (inWADL)
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
           </resource_type>
        </application>
      when("the wadl is translated")
      val checker = builder.build (inWADL)
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
           </resource_type>
           <method id="getMethod" name="GET">
               <response status="200 203"/>
           </method>
        </application>
      when("the wadl is translated")
      val checker = builder.build (inWADL)
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
      val checker = builder.build (inWADL)
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
      val checker = builder.build (inWADL)
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
      val checker = builder.build (inWADL)
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
      val checker = builder.build (inWADL)
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
      val checker = builder.build (inWADL)
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
      val checker = builder.build (inWADL)
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
      val checker = builder.build (inWADL)
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
      val checker = builder.build (inWADL)
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
      val checker = builder.build (inWADL)
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
      val checker = builder.build (inWADL)
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
      val checker = builder.build (inWADL)
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
      val checker = builder.build (inWADL)
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
        val checker = builder.build (inWADL)
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
        val checker = builder.build (inWADL)
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
        val checker = builder.build (inWADL)
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
        val checker = builder.build (inWADL)
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
      val checker = builder.build (inWADL)
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
      val checker = builder.build (inWADL)
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
      val checker = builder.build (inWADL)
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
    }

    scenario("The WADL contains a template parameter of a custom type at the end of the path") {
      given("A WADL with a template parameter of a custom type at the end of the path")
      val inWADL =
        <application xmlns="http://wadl.dev.java.net/2009/02"
                     xmlns:tst="test://schema/a">
           <grammars>
              <include href="test://simple.xsd"/>
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
      val checker = builder.build (inWADL)
      customTemplateAtEndAssertions(checker)
    }

    scenario("The WADL in tree format contains a template parameter of a custom type at the end of the path") {
      given("A WADL in tree format with a template parameter of a custom type at the end of the path")
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
      val checker = builder.build (inWADL)
      customTemplateAtEndAssertions(checker)
    }

    scenario("The WADL in mix format contains a template parameter of a custom type at the end of the path") {
      given("A WADL in mix format with a template parameter of a custom type at the end of the path")
      val inWADL =
        <application xmlns="http://wadl.dev.java.net/2009/02"
                     xmlns:tst="test://schema/a">
           <grammars>
              <include href="test://simple.xsd"/>
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
      val checker = builder.build (inWADL)
      customTemplateAtEndAssertions(checker)
    }

    scenario("The WADL contains a template parameter of a custom type at the end of the path, the type is in the default namespace") {
      given("A WADL with a template parameter of a custom type at the end of the path, with the type in a default namespace")
      val inWADL =
        <wadl:application xmlns:wadl="http://wadl.dev.java.net/2009/02"
                          xmlns="test://schema/a">
           <wadl:grammars>
              <wadl:include href="test://simple.xsd"/>
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
      val checker = builder.build (inWADL)
      customTemplateAtEndAssertions(checker)
    }

    scenario("The WADL in tree format contains a template parameter of a custom type at the end of the path, the type is in the default namespace") {
      given("A WADL in tree format with a template parameter of a custom type at the end of the path, the type is in the default namespace")
      val inWADL =
        <wadl:application xmlns:wadl="http://wadl.dev.java.net/2009/02"
                          xmlns="test://schema/a">
           <wadl:grammars>
              <wadl:include href="test://simple.xsd"/>
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
      val checker = builder.build (inWADL)
      customTemplateAtEndAssertions(checker)
    }

    scenario("The WADL in mix format contains a template parameter of a custom type at the end of the path, the type is in the default namespace") {
      given("A WADL in mix format with a template parameter of a custom type at the end of the path, the type is in the default namespace")
      val inWADL =
        <wadl:application xmlns:wadl="http://wadl.dev.java.net/2009/02"
                         xmlns="test://schema/a">
           <wadl:grammars>
              <wadl:include href="test://simple.xsd"/>
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
      val checker = builder.build (inWADL)
      customTemplateAtEndAssertions(checker)
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
      val checker = builder.build (inWADL)
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
      val checker = builder.build (inWADL)
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
      val checker = builder.build (inWADL)
      customTemplateInMiddleAssertions(checker)
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
      val checker = builder.build (inWADL)
      assert (checker, Start, URL("\\\\\\^\\-\\.\\$\\{\\}\\*\\+\\|\\#\\(\\)\\[\\]"), Method("\\.\\-"))
      assert (checker, Start, URL("\\^ABC\\[D\\]EFG\\#"), Method("\\-GET\\.\\.IT\\-"))
    }
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
    assert (checker, Start, URL("path"), URL("to"), URL("another"), URL("res"), Method("GET"))
    assert (checker, "count(//chk:step[@type='URL' and @match='path']) = 1")
    assert (checker, "count(//chk:step[@type='URL' and @match='to']) = 1")
    assert (checker, "count(//chk:step[@type='URL' and @match='my']) = 1")
    assert (checker, "count(//chk:step[@type='URL' and @match='resource']) = 1")
    assert (checker, "count(//chk:step[@type='URL' and @match='another']) = 1")
    assert (checker, "count(//chk:step[@type='URL' and @match='res']) = 1")
    assert (checker, "count(//chk:step[@type='METHOD' and @match='GET']) = 2")
  }

  scenario("The WADL does not contain any duplicate nodes, but remove dup optimization is on") {
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
              <resource path="path/to/another/res">
                     <method name="GET">
                        <response status="200 203"/>
                     </method>
              </resource>
           </resources>
        </application>
    when("the WADL is translated, with dup remove on")
    val checker_dupon = builder.build(inWADL, true)
    and ("the WADL is translated, with dup remove off")
    val checker_dupoff = builder.build(inWADL)

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
                 <resource path="another">
                   <resource path="res">
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
    val checker_dupon = builder.build(inWADL, true)
    and ("the WADL is translated, with dup remove off")
    val checker_dupoff = builder.build(inWADL)

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
              <resource path="path/to/another">
                  <resource path="res">
                     <method name="GET">
                        <response status="200 203"/>
                     </method>
                  </resource>
              </resource>
           </resources>
        </application>
    when("the WADL is translated, with dup remove on")
    val checker_dupon = builder.build(inWADL, true)
    and ("the WADL is translated, with dup remove off")
    val checker_dupoff = builder.build(inWADL)

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
    val checker_dupon = builder.build(inWADL, true)
    and ("the WADL is translated, with dup remove off")
    val checker_dupoff = builder.build(inWADL)

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
    val checker_dupon = builder.build(inWADL, true)
    and ("the WADL is translated, with dup remove off")
    val checker_dupoff = builder.build(inWADL)

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
    val checker_dupon = builder.build(inWADL, true)
    and ("the WADL is translated, with dup remove off")
    val checker_dupoff = builder.build(inWADL)

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
    assert (checker_dupon, "count(//chk:step[@type='METHOD' and @match='GET']) = 2")
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
    val checker_dupon = builder.build(inWADL, true)
    and ("the WADL is translated, with dup remove off")
    val checker_dupoff = builder.build(inWADL)

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
    val checker_dupon = builder.build(inWADL, true)
    and ("the WADL is translated, with dup remove off")
    val checker_dupoff = builder.build(inWADL)

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
    val checker_dupon = builder.build(inWADL, true)
    and ("the WADL is translated, with dup remove off")
    val checker_dupoff = builder.build(inWADL)

    multipleDupAsserts(checker_dupon, checker_dupoff)
  }

}
