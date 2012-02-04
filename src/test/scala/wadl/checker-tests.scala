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
           <resources base="https://test.api.openstack.com"/>
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
      printf ("%s\n",checker)
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
      printf ("%s\n",checker)
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
      printf ("%s\n",checker)
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
      printf ("%s\n", checker)
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
      printf ("%s\n", checker)
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
      printf ("%s\n",checker)
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

    //
    //  The following scenarios test a string template parameter in the
    //  middle of the resource path (/path/to/my/{id}/resource. They are
    //  equivalent but they are written in slightly different WADL
    //  form the assertions below must apply to all of them.
    //

    def stringTemplateInMiddleAssertions (checker : NodeSeq) : Unit = {
      printf ("%s\n",checker)
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
  }
}
