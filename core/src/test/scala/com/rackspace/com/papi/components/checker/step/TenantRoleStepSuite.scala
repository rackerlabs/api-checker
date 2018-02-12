/***
 *   Copyright 2018 Rackspace US, Inc.
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
package com.rackspace.com.papi.components.checker.step

import javax.xml.namespace.QName

import javax.servlet.FilterChain

import com.rackspace.com.papi.components.checker.step.base.Step
import com.rackspace.com.papi.components.checker.step.base.StepContext

import com.rackspace.com.papi.components.checker.step.results.Result

import com.rackspace.com.papi.components.checker.servlet.CheckerServletResponse
import com.rackspace.com.papi.components.checker.servlet.CheckerServletRequest
import com.rackspace.com.papi.components.checker.servlet.CheckerServletRequest.MAP_ROLES_HEADER
import com.rackspace.com.papi.components.checker.servlet.CheckerServletRequest.ROLES_HEADER

import com.rackspace.com.papi.components.checker.util.ImmutableNamespaceContext
import com.rackspace.com.papi.components.checker.util.HeaderMap

import com.rackspace.com.papi.components.checker.LogAssertions

import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.fasterxml.jackson.databind.ObjectMapper

import org.junit.runner.RunWith

import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class TenantRoleStepSuite extends BaseStepSuite with LogAssertions {

  val mapHeaderValue = b64Encode("""
      {
         "tenant1" : ["admin","foo","bar"],
         "tenant2" : ["admin", "foo"],
         "tenant3" : ["foo", "bar", "biz", "booz"],
         "tenant4" : ["booga"]
      }
    """)
  val CAPTURE_HEADER = "X-TENANT-HEADER"
  val XSD_STRING = new QName("http://www.w3.org/2001/XMLSchema", "string", "xsd")
  val initContext = new StepContext(0, (new HeaderMap).addHeaders(ROLES_HEADER, List("foo")))

  val privateMapper = {
    val om = new ObjectMapper

    om.registerModule(DefaultScalaModule)
    om
  }

  type ProcessStepType = (String /* Tenant Param Name */,
                          Boolean /* enable tenant */,
                          Option[List[String]] /* matchTenants */,
                          Option[Set[String]]  /* matchRoles */,
                          Option[String] /* Capture Header */,
                          StepContext /* existing context */) => StepContext

  type TenantRoleSteps = Map[String /*step name*/, ProcessStepType]


  //
  //  These are functions of type ProcessStep that create a step and
  //  do a check based on parameters.
  //
  def xpathProcessStep(tenantName : String, enableTenant : Boolean,
                       matchTenants : Option[List[String]], matchRoles : Option[Set[String]],
                       captureHeader : Option[String],
                       context : StepContext) : StepContext = {
    val nsContext = ImmutableNamespaceContext(Map("tst"->"http://test.org/test"))
    val xpath = new XPath("XPath", "XPath", Some(tenantName), "/tst:tenants/tst:tenant[1]", None, None, nsContext, 20, captureHeader, enableTenant, 10, Array[Step]())
    val xml = <tenants xmlns="http://test.org/test">
    {
      matchTenants match {
        case Some(matches) => matches.map { t => <tenant>{t}</tenant> }
        case None => <tenant/>
      }
    }
    </tenants>
    val req = request("PUT", "/a/b", "application/xml", xml, true, Map(MAP_ROLES_HEADER->List(mapHeaderValue)))

    xpath.checkStep (req, response, chain, context).get
  }

  def jsonXPathProcessStep(tenantName : String, enableTenant : Boolean,
                           matchTenants : Option[List[String]], matchRoles : Option[Set[String]],
                           captureHeader : Option[String],
                           context : StepContext) : StepContext = {
    val nsContext = ImmutableNamespaceContext(Map[String,String]())
    val xpath = new JSONXPath("JSONXPath", "JSONXPath", Some(tenantName), "$_?tenants(1)", None, None, nsContext, 31, captureHeader, enableTenant, 10, Array[Step]())
    val json : Map[String, List[String]] = Map[String, List[String]]( "tenants" -> { matchTenants match {
      case Some(mts : List[String]) => mts
      case None => List[String]("")
    }})
    val req = request("PUT", "/a/b", "application/json",privateMapper.writeValueAsString(json), true, Map(MAP_ROLES_HEADER->List(mapHeaderValue)))

    xpath.checkStep (req, response, chain, context).get
  }

  def uriProcessStep(tenantName : String, enableTenant : Boolean,
                     matchTenants : Option[List[String]], matchRoles : Option[Set[String]],
                     captureHeader : Option[String],
                     context : StepContext) : StepContext = {

    val uri = new URI("URI", "URI", Some(tenantName), ".*".r, captureHeader, enableTenant, Array[Step]())
    //
    //  This step requires a single match tenant, No match and
    //  multi-match don't make sense in a URI param.
    //
    //  So if you're here 'cus of a None.get or NoSuchElementException,
    //  you're using this function in the wrong test!
    //
    val uriMatch = matchTenants.get.head

    uri.checkStep (request("GET", s"/$uriMatch/b","","", false, Map(MAP_ROLES_HEADER->List(mapHeaderValue))), response, chain, context).get
  }


  def uriXSDProcessStep(tenantName : String, enableTenant : Boolean,
                        matchTenants : Option[List[String]],
                        matchRoles : Option[Set[String]], captureHeader : Option[String],
                        context : StepContext) : StepContext = {
    //
    //  Because of the way error-messaging works with URIXSD, we can't
    //  just call checkStep directly. We must call check and intercept
    //  the context from there. Kinda nasty, this got somewhat fixed
    //  with content error types, but not in URLXSD.
    //
    var retContext : Option[StepContext] = None
    val capture = new Step("capture", "CaptureContext") {
      override def check(req : CheckerServletRequest,
                         resp : CheckerServletResponse,
                         chain : FilterChain,
                         captureContext : StepContext) : Option[Result] = {
        retContext = Some(captureContext)
        None
      }
    }
    val urixsd = new URIXSD("URIXSD", "URIXSD", Some(tenantName), XSD_STRING, xsdSchema, captureHeader, enableTenant, Array[Step](capture))

    //
    //  This step requires a single match tenant, No match and
    //  multi-match don't make sense in a URI param.
    //
    //  So if you're here 'cus of a None.get or NoSuchElementException,
    //  you're using this function in the wrong test!
    //
    val uriMatch = matchTenants.get.head

    urixsd.check (request("GET", s"/$uriMatch/b","","", false, Map(MAP_ROLES_HEADER->List(mapHeaderValue))), response, chain, context)
    retContext.get
  }

  def headerSingleProcessStep(tenantName : String, enableTenant : Boolean,
                              matchTenants : Option[List[String]],
                              matchRoles : Option[Set[String]], captureHeader : Option[String],
                              context : StepContext) : StepContext = {
    val header = new HeaderSingle("HEADER_SINGLE", "Header Single", tenantName, ".*".r, None, None, captureHeader, enableTenant, 12345, Array[Step]())

    //
    //  This step requires a single match tenant, multi-match does not
    //  make sense in a HeaderSingle step.
    //
    //
    val headerMatch = matchTenants.get.head

    header.checkStep (request("GET", s"/a/b","","", false, Map(MAP_ROLES_HEADER->List(mapHeaderValue), tenantName->List(headerMatch))),
                      response, chain, context).get
  }


  def headerXSDSingleProcessStep(tenantName : String, enableTenant : Boolean,
                                 matchTenants : Option[List[String]], matchRoles : Option[Set[String]],
                                 captureHeader : Option[String],
                                 context : StepContext) : StepContext = {
    val header = new HeaderXSDSingle("HEADER_SINGLE", "Header Single", tenantName, XSD_STRING, xsdSchema, None, None, captureHeader, enableTenant, 12345, Array[Step]())

    //
    //  This step requires a single match tenant, multi-match does not
    //  make sense in a HeaderSingle step.
    //
    //
    val headerMatch = matchTenants.get.head

    header.checkStep (request("GET", s"/a/b","","", false, Map(MAP_ROLES_HEADER->List(mapHeaderValue), tenantName->List(headerMatch))),
                      response, chain, context).get
  }

  def headerProcessStep(tenantName : String, enableTenant : Boolean,
                        matchTenants : Option[List[String]], matchRoles : Option[Set[String]],
                        captureHeader : Option[String],
                        context : StepContext) : StepContext = {
    val header = new Header("HEADER", "Header", tenantName, ".*".r, None, None, captureHeader, matchRoles, enableTenant, 12345, Array[Step]())

    header.checkStep (request("GET", s"/a/b","","", false, Map(MAP_ROLES_HEADER->List(mapHeaderValue), tenantName->matchTenants.get)),
                      response, chain, context).get
  }


  def headerXSDProcessStep(tenantName : String, enableTenant : Boolean,
                           matchTenants : Option[List[String]], matchRoles : Option[Set[String]],
                           captureHeader : Option[String],
                           context : StepContext) : StepContext = {
    val header = new HeaderXSD("HEADERXSD", "Header XSD", tenantName, XSD_STRING, xsdSchema, None, None, captureHeader, matchRoles, enableTenant, 12345, Array[Step]())

    header.checkStep (request("GET", s"/a/b","","", false, Map(MAP_ROLES_HEADER->List(mapHeaderValue), tenantName->matchTenants.get)),
                      response, chain, context).get
  }


  def headerAnyProcessStep(tenantName : String, enableTenant : Boolean,
                           matchTenants : Option[List[String]], matchRoles : Option[Set[String]],
                           captureHeader : Option[String],
                           context : StepContext) : StepContext = {

    val header = new HeaderAny("HEADER_ANY", "Header Any", tenantName, ".*".r, None, None, captureHeader, matchRoles, enableTenant, 12345, Array[Step]())

    header.checkStep (request("GET", s"/a/b","","", false, Map(MAP_ROLES_HEADER->List(mapHeaderValue), tenantName->matchTenants.get)),
                      response, chain, context).get
  }


  def headerXSDAnyProcessStep(tenantName : String, enableTenant : Boolean,
                              matchTenants : Option[List[String]],
                              matchRoles : Option[Set[String]], captureHeader : Option[String],
                              context : StepContext) : StepContext = {

    val testHeader = "X-TEST-HEADER"
    val header = new HeaderXSDAny("HEADERXSD_Any", "HeaderXSD Any", tenantName, XSD_STRING, xsdSchema, None, None, captureHeader, matchRoles, enableTenant, 12345, Array[Step]())

    header.checkStep (request("GET", s"/a/b","","", false, Map(MAP_ROLES_HEADER->List(mapHeaderValue), tenantName->matchTenants.get)),
                      response, chain, context).get
  }


  def headerAllProcessStep(tenantName : String, enableTenant : Boolean,
                           matchTenants : Option[List[String]], matchRoles : Option[Set[String]],
                           captureHeader : Option[String],
                           context : StepContext) : StepContext = {

    val header = new HeaderAll("HEADER_ALL", "Header All", tenantName, None, None, Some(".*".r), None, None, captureHeader, matchRoles, enableTenant, 12345, Array[Step]())

    header.checkStep (request("GET", s"/a/b","","", false, Map(MAP_ROLES_HEADER->List(mapHeaderValue), tenantName->matchTenants.get)),
                      response, chain, context).get
  }


  def captureHeaderProcessStep(tenantName : String, enableTenant : Boolean,
                               matchTenants : Option[List[String]], matchRoles : Option[Set[String]],
                               captureHeader : Option[String],
                               context : StepContext) : StepContext = {
    val nsContext = ImmutableNamespaceContext(Map[String,String]())
    val captureHeaderStep = new CaptureHeader("CaptureHeader", "Capture Header", tenantName,  "$_?tenants?*", nsContext, 31, matchRoles, enableTenant, Array[Step]())
    val json : Map[String, List[String]] = Map[String, List[String]]( "tenants" -> { matchTenants match {
      case Some(mts : List[String]) => mts
      case None => List[String]("")
    }})
    val req = request("PUT", "/a/b", "application/json",privateMapper.writeValueAsString(json), true, Map(MAP_ROLES_HEADER->List(mapHeaderValue)))

    //
    //  Capture header is a weird case because the parameter name and
    //  the capture header name are always the same. We split these up
    //  to play nice with the test faramework.
    //
    val contextWithRoles = captureHeaderStep.checkStep (req, response, chain, context).get
    captureHeader match {
      case Some(header) =>
        contextWithRoles.copy(requestHeaders = contextWithRoles.requestHeaders.addHeaders(header, contextWithRoles.requestHeaders(tenantName)))
      case None => contextWithRoles
    }
  }

  //
  //  Steps that can processes only a single tenant value.
  //
  val tenantRoleStepsSingle : TenantRoleSteps = Map(
    "XPATH" -> xpathProcessStep,
    "JSON_XPATH" -> jsonXPathProcessStep,
    "URI" -> uriProcessStep,
    "URIXSD" -> uriXSDProcessStep,
    "HEADER_SINGLE" -> headerSingleProcessStep,
    "HEADERXSD_SINGLE" -> headerXSDSingleProcessStep
  )

  //
  //  Steps that can process multiple tenant values
  //
  val tenantRoleStepsMulti : TenantRoleSteps = Map(
    "HEADER" -> headerProcessStep,
    "HEADERXSD" -> headerXSDProcessStep,
    "HEADER_ANY" -> headerAnyProcessStep,
    "HEADERXSD_ANY" -> headerXSDAnyProcessStep,
    "HEADER_ALL" -> headerAllProcessStep,
    "CAPTURE_HEADER" -> captureHeaderProcessStep
  )

  //
  // These tests cover single tenant value, note that we also run
  // these tests on steps that support multiple tenant values as well.
  //
  for ((stepName, processStep) <- tenantRoleStepsSingle ++ tenantRoleStepsMulti) {
    test(s"If isTenant is enabled in a(n) $stepName step should set correct roles on a match") {
      val tstContext  = processStep("happyTenant",true, Some(List("tenant1")), None, None, initContext)
      val tstContext2 = processStep("happyTenant",true, Some(List("tenant2")), None, None, initContext)
      val tstContext3 = processStep("happyTenant",true, Some(List("tenant4")), None, None, initContext)

      assert (tstContext.requestHeaders(ROLES_HEADER) == List("foo","admin/{happyTenant}", "foo/{happyTenant}", "bar/{happyTenant}"))
      assert (!tstContext.requestHeaders.contains(CAPTURE_HEADER))

      assert (tstContext2.requestHeaders(ROLES_HEADER) == List("foo","admin/{happyTenant}", "foo/{happyTenant}"))
      assert (!tstContext2.requestHeaders.contains(CAPTURE_HEADER))

      assert (tstContext3.requestHeaders(ROLES_HEADER) == List("foo","booga/{happyTenant}"))
      assert (!tstContext3.requestHeaders.contains(CAPTURE_HEADER))
    }

    test(s"If isTenant is enabled in a(n) $stepName step should set correct roles on a match (capture header)") {
      val tstContext  = processStep("happyTenant",true, Some(List("tenant1")), None, Some(CAPTURE_HEADER), initContext)
      val tstContext2 = processStep("happyTenant",true, Some(List("tenant2")), None, Some(CAPTURE_HEADER), initContext)
      val tstContext3 = processStep("happyTenant",true, Some(List("tenant4")), None, Some(CAPTURE_HEADER), initContext)

      assert (tstContext.requestHeaders(ROLES_HEADER) == List("foo","admin/{happyTenant}", "foo/{happyTenant}", "bar/{happyTenant}"))
      assert (tstContext.requestHeaders(CAPTURE_HEADER) == List("tenant1"))

      assert (tstContext2.requestHeaders(ROLES_HEADER) == List("foo","admin/{happyTenant}", "foo/{happyTenant}"))
      assert (tstContext2.requestHeaders(CAPTURE_HEADER) == List("tenant2"))

      assert (tstContext3.requestHeaders(ROLES_HEADER) == List("foo","booga/{happyTenant}"))
      assert (tstContext3.requestHeaders(CAPTURE_HEADER) == List("tenant4"))
    }


    test(s"If isTenant is enabled in a(n) $stepName, but there is no tenant match there should be no change in the content") {
      val tstContext  = processStep("happyTenant", true, Some(List("t1")), None, None, initContext)
      val tstContext2 = processStep("happyTenant", true, Some(List("t2")), None, None, initContext)
      val tstContext3 = processStep("happyTenant", true, Some(List("t4")), None, None, initContext)

      assert (tstContext.requestHeaders(ROLES_HEADER) == List("foo"))
      assert (!tstContext.requestHeaders.contains(CAPTURE_HEADER))

      assert (tstContext2.requestHeaders(ROLES_HEADER) == List("foo"))
      assert (!tstContext2.requestHeaders.contains(CAPTURE_HEADER))

      assert (tstContext3.requestHeaders(ROLES_HEADER) == List("foo"))
      assert (!tstContext3.requestHeaders.contains(CAPTURE_HEADER))
    }

    test(s"If isTenant is enabled in a(n) $stepName, but there is no tenant match there should be no change in the content (capture header)") {
      val tstContext  = processStep("happyTenant", true, Some(List("t1")), None, Some(CAPTURE_HEADER), initContext)
      val tstContext2 = processStep("happyTenant", true, Some(List("t2")), None, Some(CAPTURE_HEADER), initContext)
      val tstContext3 = processStep("happyTenant", true, Some(List("t4")), None, Some(CAPTURE_HEADER), initContext)

      assert (tstContext.requestHeaders(ROLES_HEADER) == List("foo"))
      assert (tstContext.requestHeaders(CAPTURE_HEADER) == List("t1"))

      assert (tstContext2.requestHeaders(ROLES_HEADER) == List("foo"))
      assert (tstContext2.requestHeaders(CAPTURE_HEADER) == List("t2"))

      assert (tstContext3.requestHeaders(ROLES_HEADER) == List("foo"))
      assert (tstContext3.requestHeaders(CAPTURE_HEADER) == List("t4"))
    }

    test(s"If isTenant is disabled in a(n) $stepName there should be no change in the content") {
      val tstContext  = processStep("happyTenant",false, Some(List("tenant1")), None, None, initContext)
      val tstContext2 = processStep("happyTenant",false, Some(List("tenant2")), None, None, initContext)
      val tstContext3 = processStep("happyTenant",false, Some(List("tenant4")), None, None, initContext)

      assert (tstContext.requestHeaders(ROLES_HEADER) == List("foo"))
      assert (!tstContext.requestHeaders.contains(CAPTURE_HEADER))

      assert (tstContext2.requestHeaders(ROLES_HEADER) == List("foo"))
      assert (!tstContext2.requestHeaders.contains(CAPTURE_HEADER))

      assert (tstContext3.requestHeaders(ROLES_HEADER) == List("foo"))
      assert (!tstContext3.requestHeaders.contains(CAPTURE_HEADER))
    }

    test(s"If isTenant is disabled in a(n) $stepName there should be no change in the content (capture header)") {
      val tstContext  = processStep("happyTenant",false, Some(List("tenant1")), None, Some(CAPTURE_HEADER), initContext)
      val tstContext2 = processStep("happyTenant",false, Some(List("tenant2")), None, Some(CAPTURE_HEADER), initContext)
      val tstContext3 = processStep("happyTenant",false, Some(List("tenant4")), None, Some(CAPTURE_HEADER), initContext)

      assert (tstContext.requestHeaders(ROLES_HEADER) == List("foo"))
      assert (tstContext.requestHeaders(CAPTURE_HEADER) == List("tenant1"))

      assert (tstContext2.requestHeaders(ROLES_HEADER) == List("foo"))
      assert (tstContext2.requestHeaders(CAPTURE_HEADER) == List("tenant2"))

      assert (tstContext3.requestHeaders(ROLES_HEADER) == List("foo"))
      assert (tstContext3.requestHeaders(CAPTURE_HEADER) == List("tenant4"))
    }
  }

  //
  // These tests cover multi tenant value checks
  //
  for ((stepName, processStep) <- tenantRoleStepsMulti) {

    test(s"If isTenant is enabled in a(n) $stepName step should set correct roles on a match (multi-tenant)") {
      val tstContext  = processStep("happyTenant",true, Some(List("tenant1", "tenant2", "tenant3")), Some(Set("foo/{happyTenant}")), None, initContext)
      val tstContext2 = processStep("happyTenant",true, Some(List("tenant1", "tenant3")), Some(Set("bar/{happyTenant}","foo/{happyTenant}")), None, initContext)
      val tstContext3 = processStep("happyTenant",true, Some(List("tenant1", "tenant2")), Some(Set("admin/{happyTenant}")), None, initContext)

      assert ((Set[String]() ++ tstContext.requestHeaders(ROLES_HEADER)) == Set("foo", "foo/{happyTenant}"))
      assert (!tstContext.requestHeaders.contains(CAPTURE_HEADER))

      assert ((Set[String]() ++ tstContext2.requestHeaders(ROLES_HEADER)) == Set("foo","bar/{happyTenant}", "foo/{happyTenant}"))
      assert (!tstContext2.requestHeaders.contains(CAPTURE_HEADER))

      assert ((Set[String]() ++ tstContext3.requestHeaders(ROLES_HEADER)) == Set("foo","admin/{happyTenant}"))
      assert (!tstContext3.requestHeaders.contains(CAPTURE_HEADER))
    }

    test(s"If isTenant is enabled in a(n) $stepName step should set correct roles on a match (multi-tenant, capture header)") {
      val tstContext  = processStep("happyTenant",true, Some(List("tenant1", "tenant2", "tenant3")), Some(Set("foo/{happyTenant}")), Some(CAPTURE_HEADER), initContext)
      val tstContext2 = processStep("happyTenant",true, Some(List("tenant1", "tenant3")), Some(Set("bar/{happyTenant}","foo/{happyTenant}")), Some(CAPTURE_HEADER), initContext)
      val tstContext3 = processStep("happyTenant",true, Some(List("tenant1", "tenant2")), Some(Set("admin/{happyTenant}")), Some(CAPTURE_HEADER), initContext)

      assert ((Set[String]() ++ tstContext.requestHeaders(ROLES_HEADER)) == Set("foo", "foo/{happyTenant}"))
      assert (tstContext.requestHeaders(CAPTURE_HEADER) == List("tenant1", "tenant2", "tenant3"))

      assert ((Set[String]() ++ tstContext2.requestHeaders(ROLES_HEADER)) == Set("foo","bar/{happyTenant}", "foo/{happyTenant}"))
      assert (tstContext2.requestHeaders(CAPTURE_HEADER) == List("tenant1", "tenant3"))

      assert ((Set[String]() ++ tstContext3.requestHeaders(ROLES_HEADER)) == Set("foo","admin/{happyTenant}"))
      assert (tstContext3.requestHeaders(CAPTURE_HEADER) == List("tenant1", "tenant2"))
    }

    test(s"If isTenant is enabled in a(n) $stepName, but there is no tenant match there should be no change in the content (multi-tenant)") {
      val tstContext  = processStep("happyTenant", true, Some(List("t1", "tenant1")), Some(Set("foo/{happyTenant}")), None, initContext)
      val tstContext2 = processStep("happyTenant", true, Some(List("tenant1", "t2")), Some(Set("foo/{happyTenant}")), None, initContext)
      val tstContext3 = processStep("happyTenant", true, Some(List("tenant3", "tenant1", "tenant4")), Some(Set("foo/{happyTenant}")), None, initContext)
      val tstContext4 = processStep("happyTenant", true, Some(List("tenant4", "tenant2")), Some(Set("foo/{happyTenant}")), None, initContext)

      assert (tstContext.requestHeaders(ROLES_HEADER) == List("foo"))
      assert (!tstContext.requestHeaders.contains(CAPTURE_HEADER))

      assert (tstContext2.requestHeaders(ROLES_HEADER) == List("foo"))
      assert (!tstContext2.requestHeaders.contains(CAPTURE_HEADER))

      assert (tstContext3.requestHeaders(ROLES_HEADER) == List("foo"))
      assert (!tstContext3.requestHeaders.contains(CAPTURE_HEADER))

      assert (tstContext4.requestHeaders(ROLES_HEADER) == List("foo"))
      assert (!tstContext4.requestHeaders.contains(CAPTURE_HEADER))
    }

    test(s"If isTenant is enabled in a(n) $stepName, but there is no tenant match there should be no change in the content (multi-tenant, capture header)") {
      val tstContext  = processStep("happyTenant", true, Some(List("t1", "tenant1")), Some(Set("foo/{happyTenant}")), Some(CAPTURE_HEADER), initContext)
      val tstContext2 = processStep("happyTenant", true, Some(List("tenant1", "t2")), Some(Set("foo/{happyTenant}")), Some(CAPTURE_HEADER), initContext)
      val tstContext3 = processStep("happyTenant", true, Some(List("tenant3", "tenant1", "tenant4")), Some(Set("foo/{happyTenant}")), Some(CAPTURE_HEADER), initContext)
      val tstContext4 = processStep("happyTenant", true, Some(List("tenant4", "tenant2")), Some(Set("foo/{happyTenant}")), Some(CAPTURE_HEADER), initContext)

      assert (tstContext.requestHeaders(ROLES_HEADER) == List("foo"))
      assert (tstContext.requestHeaders(CAPTURE_HEADER) == List("t1", "tenant1"))

      assert (tstContext2.requestHeaders(ROLES_HEADER) == List("foo"))
      assert (tstContext2.requestHeaders(CAPTURE_HEADER) == List("tenant1", "t2"))

      assert (tstContext3.requestHeaders(ROLES_HEADER) == List("foo"))
      assert (tstContext3.requestHeaders(CAPTURE_HEADER) == List("tenant3", "tenant1", "tenant4"))

      assert (tstContext4.requestHeaders(ROLES_HEADER) == List("foo"))
      assert (tstContext4.requestHeaders(CAPTURE_HEADER) == List("tenant4", "tenant2"))
    }

    test(s"If isTenant is disabled in a(n) $stepName there should be no change in the content (multi-tenant)") {
      val tstContext  = processStep("happyTenant",false, Some(List("tenant1", "tenant2", "tenant3")), Some(Set("foo/{happyTenant}")), None, initContext)
      val tstContext2 = processStep("happyTenant",false, Some(List("tenant1", "tenant3")), Some(Set("foo/{happyTenant}")), None, initContext)
      val tstContext3 = processStep("happyTenant",false, Some(List("tenant1", "tenant2")), Some(Set("foo/{happyTenant}")), None, initContext)

      assert (tstContext.requestHeaders(ROLES_HEADER) == List("foo"))
      assert (!tstContext.requestHeaders.contains(CAPTURE_HEADER))

      assert (tstContext2.requestHeaders(ROLES_HEADER) == List("foo"))
      assert (!tstContext2.requestHeaders.contains(CAPTURE_HEADER))

      assert (tstContext3.requestHeaders(ROLES_HEADER) == List("foo"))
      assert (!tstContext3.requestHeaders.contains(CAPTURE_HEADER))
    }

    test(s"If isTenant is disabled in a(n) $stepName there should be no change in the content (multi-tenant, capture header)") {
      val tstContext  = processStep("happyTenant",false, Some(List("tenant1", "tenant2", "tenant3")), Some(Set("foo/{happyTenant}")), Some(CAPTURE_HEADER), initContext)
      val tstContext2 = processStep("happyTenant",false, Some(List("tenant1", "tenant3")), Some(Set("foo/{happyTenant}")), Some(CAPTURE_HEADER), initContext)
      val tstContext3 = processStep("happyTenant",false, Some(List("tenant1", "tenant2")), Some(Set("foo/{happyTenant}")), Some(CAPTURE_HEADER), initContext)

      assert (tstContext.requestHeaders(ROLES_HEADER) == List("foo"))
      assert (tstContext.requestHeaders(CAPTURE_HEADER) == List("tenant1", "tenant2", "tenant3"))

      assert (tstContext2.requestHeaders(ROLES_HEADER) == List("foo"))
      assert (tstContext2.requestHeaders(CAPTURE_HEADER) == List("tenant1", "tenant3"))

      assert (tstContext3.requestHeaders(ROLES_HEADER) == List("foo"))
      assert (tstContext3.requestHeaders(CAPTURE_HEADER) == List("tenant1", "tenant2"))
    }
  }

  //
  //  XPath on XML has a weird properity where you can select a node
  //  that won't actually resolve to a string.  This is specific to
  //  XML nodes, and doesn't affect JSON. We test the weird case here.
  //
  val tenantRoleXPathStep : TenantRoleSteps = Map(
    "XPATH" -> xpathProcessStep
  )

  for ((stepName, processStep) <- tenantRoleXPathStep) {
    test(s"If isTenant is enabled in a(n) $stepName, but no tenant is selected there should be no change in the content") {
      val tstContext  = processStep("happyTenant", true, None, None, None, initContext)
      val tstContext2 = processStep("happyTenant", true, None, None, None, initContext)
      val tstContext3 = processStep("happyTenant", true, None, None, None, initContext)

      assert (tstContext.requestHeaders(ROLES_HEADER) == List("foo"))
      assert (!tstContext.requestHeaders.contains(CAPTURE_HEADER))

      assert (tstContext2.requestHeaders(ROLES_HEADER) == List("foo"))
      assert (!tstContext2.requestHeaders.contains(CAPTURE_HEADER))

      assert (tstContext3.requestHeaders(ROLES_HEADER) == List("foo"))
      assert (!tstContext3.requestHeaders.contains(CAPTURE_HEADER))
    }

    test(s"If isTenant is enabled in a(n) $stepName, but no tenant is selected there should be no change in the content (capture header)") {
      val tstContext  = processStep("happyTenant", true, None, None, Some(CAPTURE_HEADER), initContext)
      val tstContext2 = processStep("happyTenant", true, None, None, Some(CAPTURE_HEADER), initContext)
      val tstContext3 = processStep("happyTenant", true, None, None, Some(CAPTURE_HEADER), initContext)

      assert (tstContext.requestHeaders(ROLES_HEADER) == List("foo"))
      assert (tstContext.requestHeaders(CAPTURE_HEADER) == List(""))

      assert (tstContext2.requestHeaders(ROLES_HEADER) == List("foo"))
      assert (tstContext2.requestHeaders(CAPTURE_HEADER) == List(""))

      assert (tstContext3.requestHeaders(ROLES_HEADER) == List("foo"))
      assert (tstContext3.requestHeaders(CAPTURE_HEADER) == List(""))
    }
  }
}
