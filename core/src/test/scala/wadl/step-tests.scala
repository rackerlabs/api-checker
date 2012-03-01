package com.rackspace.com.papi.components.checker.wadl

import scala.xml._
import org.junit.runner.RunWith
import org.scalatest.TestFailedException
import org.scalatest.junit.JUnitRunner
import org.scalatest.matchers.ShouldMatchers._

import com.rackspace.com.papi.components.checker.step._

@RunWith(classOf[JUnitRunner])
class WADLStepSpec extends BaseStepSpec {
  feature ("The WADLStepBuilder can correctly transforma a WADL into a Step") {
    info ("As a developer")
    info ("I want to be able to transform a WADL which references multiple XSDs into a ")
    info ("a description of a machine that can validate the API")
    info ("so that an API validator can process the machine to validate the API")

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
      val step = builder.build (inWADL).asInstanceOf[Start]
      then("the start step should only be connected with an URLFail and MethodFail steps")
      assert (step.next.length == 2)
      assert (step.next.filter(a => a.isInstanceOf[URLFail]).length == 1)
      assert (step.next.filter(a => a.isInstanceOf[MethodFail]).length == 1)
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
      val step = builder.build (inWADL).asInstanceOf[Start]
      assert(step, Start, URI("path"), URI("to"), URI("my"), URI("resource"), Method("GET"), Accept)
      assert(step, Start, URI("path"), URI("to"), URI("my"), URI("resource"), Method("DELETE"), Accept)
      assert(step, Start, URI("path"), URI("to"), URI("my"), URI("resource"), URLFail)
      assert(step, Start, URI("path"), URLFailMatch("to"))
      assert(step, Start, URI("path"), URI("to"), URI("my"), URI("resource"), MethodFailMatch("DELETE|GET"))
      assert(step, Start, URI("path"), MethodFail)
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
      val step = builder.build (inWADL)
      assert(step, Start, URI("path"), URI("to"), URI("my"), URI("resource"), Method("GET"), Accept)
      assert(step, Start, URI("path"), URI("to"), URI("my"), URI("resource"), Method("DELETE"), Accept)
      assert(step, Start, URI("path"), URI("to"), URI("my"), URI("resource"), URLFail)
      assert(step, Start, URI("path"), URLFailMatch("to"))
      assert(step, Start, URI("path"), URI("to"), URI("my"), URI("resource"), MethodFailMatch("DELETE|GET"))
      assert(step, Start, URI("path"), MethodFail)
      assert(step, Start, URI("path"), URI("to"), URI("my"), URI("other_resource"), Method("GET"), Accept)
      assert(step, Start, URI("path"), URI("to"), URI("my"), URI("other_resource"), Method("POST"), Accept)
      assert(step, Start, URI("path"), URI("to"), URI("my"), URI("other_resource"), URLFail)
      assert(step, Start, URI("path"), URI("to"), URI("my"), URI("other_resource"), MethodFailMatch("GET|POST"))
      assert(step, Start, URI("path"), URI("to"), URI("my"), URLFailMatch("other_resource|resource"))
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
      val step = builder.build (inWADL)
      assert(step, Start, URI("path"), URI("to"), URI("my"), URI("resource"), Method("GET"), Accept)
      assert(step, Start, URI("path"), URI("to"), URI("my"), URI("resource"), Method("DELETE"), Accept)
      assert(step, Start, URI("path"), URI("to"), URI("my"), URI("resource"), URLFail)
      assert(step, Start, URI("path"), URLFailMatch("to"))
      assert(step, Start, URI("path"), URI("to"), URI("my"), URI("resource"), MethodFailMatch("DELETE|GET"))
      assert(step, Start, URI("path"), MethodFail)
      assert(step, Start, URI("this"), URI("is"), URI("my"), URI("other_resource"), Method("GET"), Accept)
      assert(step, Start, URI("this"), URI("is"), URI("my"), URI("other_resource"), Method("POST"), Accept)
      assert(step, Start, URI("this"), URI("is"), URI("my"), URI("other_resource"), URLFail)
      assert(step, Start, URI("this"), URLFailMatch("is"))
      assert(step, Start, URI("this"), URI("is"), URI("my"), URI("other_resource"), MethodFailMatch("GET|POST"))
      assert(step, Start, URI("this"), URI("is"), MethodFail)
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
      val step = builder.build (inWADL)
      assert(step, Start, URI("path"), URI("to"), URI("my"), URI("resource"), Method("GET"), Accept)
      assert(step, Start, URI("path"), URI("to"), URI("my"), URI("resource"), Method("DELETE"), Accept)
      assert(step, Start, URI("path"), URI("to"), URI("my"), URI("resource"), URLFail)
      assert(step, Start, URI("path"), URLFailMatch("to"))
      assert(step, Start, URI("path"), URI("to"), URI("my"), URI("resource"), MethodFailMatch("DELETE|GET"))
      assert(step, Start, URI("path"), MethodFail)
      assert(step, Start, URI("path"), URI("to"), URI("my"), URI("resource"), Label("getResource"), Accept)
      assert(step, Start, URI("path"), URI("to"), URI("my"), URI("resource"), Label("deleteResource"), Accept)
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
      val step = builder.build (inWADL)
      assert(step, Start, URI("path"), URI("to"), URI("my"), URI("resource"), Method("GET"), Accept)
      assert(step, Start, URI("path"), URI("to"), URI("my"), URI("resource"), Method("DELETE"), Accept)
      assert(step, Start, URI("path"), URI("to"), URI("my"), URI("resource"), URLFail)
      assert(step, Start, URI("path"), URLFailMatch("to"))
      assert(step, Start, URI("path"), URI("to"), URI("my"), URI("resource"), MethodFailMatch("DELETE|GET"))
      assert(step, Start, URI("path"), MethodFail)
      assert(step, Start, URI("to"), URI("my"), URI("resource"), Method("GET"), Accept)
      assert(step, Start, URI("to"), URI("my"), URI("resource"), Method("DELETE"), Accept)
      assert(step, Start, URI("to"), URI("my"), URI("resource"), URLFail)
      assert(step, Start, URI("to"), URLFailMatch("my"))
      assert(step, Start, URI("to"), URI("my"), URI("resource"), MethodFailMatch("DELETE|GET"))
      assert(step, Start, URLFailMatch("path|to"))
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
      val step = builder.build (inWADL)
      assert(step, Start, URI("path"), URI("to"), URI("my"), URI("resource"), URI(".*"), Method("GET"), Accept)
      assert(step, Start, URI("path"), URI("to"), URI("my"), URI("resource"), URI(".*"), MethodFailMatch("GET"))
      assert(step, Start, URI("path"), URI("to"), URI("my"), URI("resource"), URI(".*"), URLFail)
      assert(step, Start, URI("path"), URLFailMatch("to"))
      and("There should not be an URLFail node right before a catch any URI...")
      intercept[TestFailedException] {
        assert(step, Start, URI("path"), URI("to"), URI("my"), URI("resource"), URLFail)
      }
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
      val step = builder.build (inWADL)
      assert(step, Start, URI("path"), URI("to"), URI("my"), URI(".*"), URI("resource"), Method("GET"), Accept)
      assert(step, Start, URI("path"), URI("to"), URI("my"), URI(".*"), URI("resource"), MethodFailMatch("GET"))
      assert(step, Start, URI("path"), URI("to"), URI("my"), URI(".*"), URI("resource"), URLFail)
      assert(step, Start, URI("path"), URLFailMatch("to"))
      intercept[TestFailedException] {
        assert(step, Start, URI("path"), URI("to"), URI("my"), URLFail)
      }
    }

  }
}
