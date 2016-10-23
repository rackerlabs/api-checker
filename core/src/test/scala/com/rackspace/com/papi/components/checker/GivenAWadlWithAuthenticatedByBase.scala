/** *
  * Copyright 2014 Rackspace US, Inc.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
package com.rackspace.com.papi.components.checker

import java.io.File
import javax.servlet.FilterChain
import javax.servlet.http.HttpServletResponse

import com.rackspace.cloud.api.wadl.Converters._
import com.rackspace.com.papi.components.checker.servlet.{CheckerServletRequest, CheckerServletResponse}
import com.rackspace.com.papi.components.checker.step.results.Result
import com.rackspace.com.papi.components.checker.RunAssertionsHandler.ASSERT_FUNCTION
import org.mockito.Mockito._
import org.scalatest.FlatSpec

import scala.collection.JavaConversions._

abstract class GivenAWadlWithAuthenticatedByBase extends FlatSpec {

  import GivenAWadlWithAuthenticatedByBase._

  val wadl =
    <application xmlns="http://wadl.dev.java.net/2009/02"
                 xmlns:rax="http://docs.rackspace.com/api"
                 xmlns:xs="http://www.w3.org/2001/XMLSchema"
                 xmlns:tst="http://www.rackspace.com/repose/wadl/checker/step/test">
      <resources base="https://test.api.openstack.com">
        <resource path="/noAuthenticatedBy">
          <method name="GET"/>
          <method name="POST">{ DefaultRequest }</method>
          <method name="PUT">{ DefaultRequest }</method>
          <method name="DELETE">{ DefaultRequest }</method>
        </resource>
        <resource path="/resourceLevel" rax:authenticatedBy="APIKEY">
          <method name="GET"/>
          <method name="POST">{ DefaultRequest }</method>
          <method name="PUT">{ DefaultRequest }</method>
          <method name="DELETE">{ DefaultRequest }</method>
        </resource>
        <resource path="/methodLevel">
          <method name="GET" rax:authenticatedBy="PASSCODE"/>
          <method name="POST" rax:authenticatedBy="APIKEY">{ DefaultRequest }</method>
          <method name="PUT" rax:authenticatedBy="PASSWORD">{ DefaultRequest }</method>
          <method name="DELETE" rax:authenticatedBy="FEDERATED">{ DefaultRequest }</method>
        </resource>
        <resource path="/multipleValues" rax:authenticatedBy="APIKEY PASSWORD OTPPASSCODE">
          <method name="GET"/>
          <method name="POST">{ DefaultRequest }</method>
          <method name="PUT">{ DefaultRequest }</method>
          <method name="DELETE">{ DefaultRequest }</method>
        </resource>
        <resource path="/multipleLevels" rax:authenticatedBy="RSAKEY">
          <method name="GET" rax:authenticatedBy="RSAKEY"/>
          <method name="POST" rax:authenticatedBy="APIKEY">{ DefaultRequest }</method>
          <method name="PUT" rax:authenticatedBy="PASSWORD">{ DefaultRequest }</method>
          <method name="DELETE" rax:authenticatedBy="FEDERATED">{ DefaultRequest }</method>
        </resource>
        <resource path="/parentResourceLevel" rax:authenticatedBy="PASSCODE">
          <resource path="/noAdditionalAuthBy">
            <method name="GET"/>
            <method name="POST">{ DefaultRequest }</method>
            <method name="PUT">{ DefaultRequest }</method>
            <method name="DELETE">{ DefaultRequest }</method>
          </resource>
          <resource path="/allAuthByAllowed" rax:authenticatedBy="#all">
            <method name="GET"/>
            <method name="POST">{ DefaultRequest }</method>
            <method name="PUT">{ DefaultRequest }</method>
            <method name="DELETE">{ DefaultRequest }</method>
          </resource>
        </resource>
        <resource path="/resourceWithHeaderPresent" rax:authenticatedBy="RSAKEY">
          <method name="GET">
            <request>
              <param name={ AuthByHeader } style="header" required="true" fixed="PASSWORD"/>
            </request>
          </method>
        </resource>
        <resource path="/methodWithHeaderPresent">
          <method name="GET" rax:authenticatedBy="RSAKEY">
            <request>
              <param name={ AuthByHeader } style="header" required="true" fixed="PASSWORD"/>
            </request>
          </method>
        </resource>
        <resource path="/methodId" rax:authenticatedBy="RSAKEY">
          <method href="#getOnMethodId" rax:authenticatedBy="APIKEY"/>
          <resource path="/more" rax:authenticatedBy="PASSWORD">
            <method href="#getOnMore" rax:authenticatedBy="PASSCODE"/>
            <method href="#postOnMore"/>
            <method href="#putOnMore" rax:authenticatedBy="IMPERSONATION"/>
          </resource>
        </resource>
        <resource path="/regression">
          <resource path="/captureHeader" rax:roles="user" rax:authenticatedBy="RSAKEY">
            <method name="POST">
              <request>
                <representation mediaType={ DefaultMediaType }>
                  <param id="captureHeader" path="/tst:a/@stepType" rax:captureHeader={ DeviceIdHeader } required="true" style="plain"/>
                </representation>
              </request>
            </method>
            <resource path="{subResource}">
              <param name="subResource" rax:captureHeader={ DeviceIdHeader } required="true" style="template" type="xs:int"/>
              <method name="POST">{ DefaultRequest }</method>
              <method name="PUT" rax:roles="putrole">
                <request>
                  <param type="xs:int" name="InputHeader" rax:captureHeader="OutputHeaderPut" repeating="false" required="true" style="header"/>
                  <representation mediaType={ DefaultMediaType }/>
                </request>
              </method>
              <method name="PATCH" rax:authenticatedBy="APIKEY">
                <request>
                  <param fixed="FOO" name="InputHeader" rax:anyMatch="true" rax:captureHeader="OutputHeaderPatch" repeating="true" required="true" style="header"/>
                  <param fixed="FAR" name="InputHeader" rax:anyMatch="true" rax:captureHeader="OutputHeaderPatch" repeating="true" required="true" style="header"/>
                  <param fixed="OUT" name="InputHeader" rax:anyMatch="true" rax:captureHeader="OutputHeaderPatch" repeating="true" required="true" style="header"/>
                  <representation mediaType={ DefaultMediaType }/>
                </request>
              </method>
            </resource>
          </resource>
          <resource path="/raxDevice" rax:roles="user" rax:authenticatedBy="RSAKEY">
            <method name="POST">
              <request>
                <representation mediaType={ DefaultMediaType }>
                  <param id="raxDevice" path="/tst:a/@stepType" rax:device="true" required="true" style="plain"/>
                </representation>
              </request>
            </method>
            <resource path="{subResource}">
              <param name="subResource" rax:device="true" required="true" style="template" type="xs:int"/>
              <method name="POST">{ DefaultRequest }</method>
            </resource>
          </resource>
          <resource path="/raxRoles" rax:roles="admin" rax:authenticatedBy="PASSCODE">
            <method name="GET"/>
          </resource>
        </resource>
      </resources>
      <method id="getOnMethodId" name="GET"/>
      <method id="getOnMore" name="GET"/>
      <method id="postOnMore" name="POST" rax:authenticatedBy="FEDERATED">{ DefaultRequest }</method>
      <method id="putOnMore" name="PUT" rax:authenticatedBy="OTPPASSCODE">{ DefaultRequest }</method>
    </application>

  val requestBody = base.goodXML_XSD2
  val xmlPathValue = "ACCEPT"

  def createTests(useSaxon: Boolean): Unit = {
    val authByCombinations = getAllCombinations(ValidAuthenticatedByValues)
    var desc: String = ""

    // run the tests for every different set of configuration options we're interested in
    ConfigOptionCombinations.foreach { configOptions: List[ConfigEnabledOption] =>
      val config = createConfig(useSaxon)
      configOptions.foreach(_.apply(config))
      val validator = Validator((localWADLURI, wadl), config)

      def accessIsAllowed(method: String, path: String, authBy: List[String], desc: String,
                          headers: Map[String, List[String]] = Map.empty): Unit =
        it should s"succeed when $method on $path with $AuthByHeader values $authBy and headers $headers for a $desc" in {
          validator.validate(createRequest(method, path, authBy, headers), response, chain)
        }

      def accessIsAllowedAndRequestIsUpdated(method: String, path: String, authBy: List[String], desc: String,
                                             headers: Map[String, List[String]], asserts: CheckerServletRequest => Unit): Unit = {
        it should s"succeed when $method on $path with $AuthByHeader values $authBy and headers $headers for a $desc" in {
          val request = createRequest(method, path, authBy, headers)
          request.setAttribute(ASSERT_FUNCTION, (csReq: CheckerServletRequest, csResp: CheckerServletResponse, res: Result) => {
            asserts(csReq)
          })
          validator.validate(request, response, chain)
        }
      }

      def accessIsUnauthorized(method: String, path: String, authBy: List[String],
                               desc: String, headers: Map[String, List[String]] = Map.empty): Unit =
        verifyNotValid(HttpCodeUnauthorized, "Authentication method not allowed for this operation")(method, path, authBy, desc, headers)

      def accessIsForbidden = verifyNotValid(HttpCodeForbidden, "You are forbidden to perform the operation") _

      def resourceNotFound = verifyNotValid(HttpCodeNotFound, "Resource not found") _

      def requestIsBad = verifyNotValid(HttpCodeBadRequest, "Bad Content") _

      def verifyNotValid(httpCode: Int, message: String)(method: String, path: String, authBy: List[String],
                         desc: String, headers: Map[String, List[String]]): Unit =
        it should s"fail with a $httpCode when $method on $path with $AuthByHeader values $authBy and headers $headers for a $desc" in {
          base.assertResultFailed(validator.validate(
            createRequest(method, path, authBy, headers), response, chain),
            httpCode,
            List(message))
        }

      behavior of s"Validator with config ${configToString(config)}"

      // everything should be allowed when no rax:authenticatedBy is configured on the resource or method
      desc = "resource and method with no rax:authenticatedBy"
      for {
        authBy <- authByCombinations
        method <- WadlMethods
      } {
        it should behave like accessIsAllowed(method, "/noAuthenticatedBy", authBy, desc)
      }

      // rax:authenticatedBy set at resource level applies to all of the resource's methods
      desc = "resource with rax:authenticatedBy of APIKEY"
      WadlMethods.foreach { method =>
        it should behave like accessIsAllowed(method, "/resourceLevel", List("APIKEY"), desc)
        it should behave like accessIsAllowed(method, "/resourceLevel", List("APIKEY", "PASSCODE", "RSAKEY"), desc)

        authByCombinations.filterNot(_.contains("APIKEY")).foreach { authBy =>
          it should behave like accessIsUnauthorized(method, "/resourceLevel", authBy, desc)
        }
      }

      // rax:authenticatedBy set at the method level applies to only that method
      desc = "method with rax:authenticatedBy of"
      WadlMethods.zip(List("PASSCODE", "APIKEY", "PASSWORD", "FEDERATED")).foreach { case (method, authBy) =>
        it should behave like accessIsAllowed(method, "/methodLevel", List(authBy), s"$desc $authBy")
        it should behave like accessIsAllowed(method, "/methodLevel", List(authBy, "BACON"), s"$desc $authBy")
        it should behave like accessIsUnauthorized(method, "/methodLevel", InvalidAuthBy, s"$desc $authBy")
        it should behave like accessIsUnauthorized(method, "/methodLevel", NoAuthBy, s"$desc $authBy")
      }

      // rax:authenticatedBy set at resource level with multiple values
      desc = "resource with rax:authenticatedBy of APIKEY PASSWORD OTPPASSCODE"
      val multipleAuthByCombos = getAllCombinations(Set("APIKEY", "PASSWORD", "OTPPASSCODE")).filterNot(_.isEmpty)
      WadlMethods.foreach { method =>
        multipleAuthByCombos.foreach { authBy =>
          it should behave like accessIsAllowed(method, "/multipleValues", authBy, desc)
        }
        it should behave like accessIsUnauthorized(method, "/multipleValues", InvalidAuthBy, desc)
        it should behave like accessIsUnauthorized(method, "/multipleValues", NoAuthBy, desc)
      }

      // rax:authenticatedBy set at both resource and method level, values should be OR'd
      desc = "resource with rax:authenticatedBy of RSAKEY and method with "
      WadlMethods.zip(List("RSAKEY", "APIKEY", "PASSWORD", "FEDERATED")).foreach { case (method, authBy) =>
        it should behave like accessIsAllowed(method, "/multipleLevels", List(authBy), s"$desc $authBy")
        it should behave like accessIsAllowed(method, "/multipleLevels", List("RSAKEY", "BACON"), s"$desc $authBy")
        it should behave like accessIsUnauthorized(method, "/multipleLevels", InvalidAuthBy, s"$desc $authBy")
        it should behave like accessIsUnauthorized(method, "/multipleLevels", NoAuthBy, s"$desc $authBy")
      }

      // rax:authenticatedBy set at parent resource level should apply to all of the methods
      desc = "parent resource with rax:authenticatedBy of PASSCODE"
      WadlMethods.foreach { method =>
        it should behave like accessIsAllowed(method, "/parentResourceLevel/noAdditionalAuthBy", List("PASSCODE"), desc)
        it should behave like accessIsUnauthorized(method, "/parentResourceLevel/noAdditionalAuthBy", InvalidAuthBy, desc)
        it should behave like accessIsUnauthorized(method, "/parentResourceLevel/noAdditionalAuthBy", NoAuthBy, desc)
      }

      // rax:authenticatedBy set at resource level to #all should allow everything
      desc = "resource with rax:authenticatedBy of #all and parent resource with PASSCODE"
      for {
        authBy <- authByCombinations
        method <- WadlMethods
      } {
        it should behave like accessIsAllowed(method, "/parentResourceLevel/allAuthByAllowed", authBy, desc)
      }

      // rax:authenticatedBy set and an X-Authenticated-By header param that should also be allowed
      desc = "with rax:authenticatedBy set to RSAKEY and header param set to PASSWORD"
      List(("resource", "/resourceWithHeaderPresent"), ("method", "/methodWithHeaderPresent")).foreach { case (level, url) =>
        it should behave like accessIsAllowed("GET", url, List("RSAKEY"), "$level $desc")
        it should behave like accessIsAllowed("GET", url, List("PASSWORD"), "$level $desc")
        it should behave like accessIsUnauthorized("GET", url, InvalidAuthBy, "$level $desc")
        it should behave like accessIsUnauthorized("GET", url, NoAuthBy, "$level $desc")
      }

      // reference a method with rax:authenticatedBy on resource and referencing method
      desc = "method with a rax:authenticatedBy and referencing another method, resource rax:authenticatedBy"
      Set("RSAKEY", "APIKEY").foreach { authBy =>
        it should behave like accessIsAllowed("GET", "/methodId", List(authBy), desc)
      }
      Set("PASSWORD", "PASSCODE", "OTPPASSCODE", "IMPERSONATION", "FEDERATED").foreach { authBy =>
        it should behave like accessIsUnauthorized("GET", "/methodId", List(authBy), desc)
      }
      it should behave like accessIsUnauthorized("GET", "/methodId", InvalidAuthBy, desc)
      it should behave like accessIsUnauthorized("GET", "/methodId", NoAuthBy, desc)

      // reference a method with rax:authenticatedBy on parent+child resource and referencing method
      desc = "method with a rax:authenticatedBy and referencing another method, parent+child resource rax:authenticatedBy"
      Set("RSAKEY", "PASSWORD", "PASSCODE").foreach { authBy =>
        it should behave like accessIsAllowed("GET", "/methodId/more", List(authBy), desc)
      }
      Set("APIKEY", "OTPPASSCODE", "IMPERSONATION", "FEDERATED").foreach { authBy =>
        it should behave like accessIsUnauthorized("GET", "/methodId/more", List(authBy), desc)
      }
      it should behave like accessIsUnauthorized("GET", "/methodId/more", InvalidAuthBy, desc)
      it should behave like accessIsUnauthorized("GET", "/methodId/more", NoAuthBy, desc)

      // reference a method with rax:authenticatedBy on parent+child resource and referenced method
      desc = "method and referencing another method with a rax:authenticatedBy, parent+child resource rax:authenticatedBy"
      Set("RSAKEY", "PASSWORD", "FEDERATED").foreach { authBy =>
        it should behave like accessIsAllowed("POST", "/methodId/more", List(authBy), desc)
      }
      Set("APIKEY", "PASSCODE", "OTPPASSCODE", "IMPERSONATION").foreach { authBy =>
        it should behave like accessIsUnauthorized("POST", "/methodId/more", List(authBy), desc)
      }
      it should behave like accessIsUnauthorized("POST", "/methodId/more", InvalidAuthBy, desc)
      it should behave like accessIsUnauthorized("POST", "/methodId/more", NoAuthBy, desc)

      // reference a method with rax:authenticatedBy on parent+child resource and referencing+referenced method
      desc = "method and referencing another method, both with a rax:authenticatedBy, and parent+child resource rax:authenticatedBy"
      Set("RSAKEY", "PASSWORD", "IMPERSONATION").foreach { authBy =>
        it should behave like accessIsAllowed("PUT", "/methodId/more", List(authBy), desc)
      }
      // interesting note: when both methods have a rax:authenticatedBy, only the one on the referencing method will be used
      // so on the "#postOnMore" method, the "FEDERATED" value is used, but for "#putOnMore", "OTPPASSCODE" is not used
      Set("APIKEY", "PASSCODE", "FEDERATED", "OTPPASSCODE").foreach { authBy =>
        it should behave like accessIsUnauthorized("PUT", "/methodId/more", List(authBy), desc)
      }
      it should behave like accessIsUnauthorized("PUT", "/methodId/more", InvalidAuthBy, desc)
      it should behave like accessIsUnauthorized("PUT", "/methodId/more", NoAuthBy, desc)

      // regression tests
      // behavior of some asserts depend on the current state of configuration

      // rax:roles enforcement
      // when rax:roles is turned off, all roles are allowed
      // when rax:roles is turned on, insufficient roles result in a 403
      // when rax:roles and maskRaxRoles are turned on, insufficient roles result in a 404 (for our test setup)
      val raxRoleExpectation: AccessExpectation = (config.enableRaxRolesExtension, config.maskRaxRoles403) match {
        case (true, true) => resourceNotFound
        case (true, false) => accessIsForbidden
        case (false, _) => accessIsAllowed
      }

      // rax:roles 'user', when rax:roles is enabled, ensure it's being enforced
      desc = "resource with rax:roles 'user'"
      List("/regression/captureHeader", "/regression/captureHeader/100", "/regression/raxDevice", "/regression/raxDevice/100").foreach { url =>
        it should behave like accessIsAllowed("POST", url, List("RSAKEY"), desc, roles("user"))
        it should behave like accessIsAllowed("POST", url, List("RSAKEY"), desc, roles("user", "admin"))
        it should behave like accessIsUnauthorized("POST", url, InvalidAuthBy, desc, roles("user"))
        it should behave like accessIsUnauthorized("POST", url, NoAuthBy, desc, roles("user"))
        it should behave like raxRoleExpectation("POST", url, List("RSAKEY"), desc, InvalidRaxRoles)
        it should behave like raxRoleExpectation("POST", url, List("RSAKEY"), desc, roles("admin"))
        it should behave like raxRoleExpectation("POST", url, List("RSAKEY"), desc, NoRaxRoles)
      }

      // rax:roles 'admin', when rax:roles is enabled, ensure it's being enforced
      desc = "resource with rax:roles 'admin'"
      it should behave like accessIsAllowed("GET", "/regression/raxRoles", List("PASSCODE"), desc, roles("admin"))
      it should behave like accessIsAllowed("GET", "/regression/raxRoles", List("PASSCODE"), desc, roles("admin", "user"))
      it should behave like accessIsUnauthorized("GET", "/regression/raxRoles", InvalidAuthBy, desc, roles("admin"))
      it should behave like accessIsUnauthorized("GET", "/regression/raxRoles", NoAuthBy, desc, roles("admin"))
      it should behave like raxRoleExpectation("GET", "/regression/raxRoles", List("PASSCODE"), desc, InvalidRaxRoles)
      it should behave like raxRoleExpectation("GET", "/regression/raxRoles", List("PASSCODE"), desc, roles("user"))
      it should behave like raxRoleExpectation("GET", "/regression/raxRoles", List("PASSCODE"), desc, NoRaxRoles)

      // capture headers and rax:device
      // the contents of the specified header depends on whether or not the captureHeaders feature is enabled
      val headerShouldContain = if (config.enableCaptureHeaderExtension) {
        (csReq: CheckerServletRequest, headerName: String, expectedValues: List[String]) => {
          val actualValues = csReq.getHeaders(headerName).toList
          assert(actualValues.intersect(expectedValues).nonEmpty,
            s"Header $headerName did not have any of the expected values: $expectedValues, actual values: $actualValues")
        }
      } else {
        (csReq: CheckerServletRequest, headerName: String, expectedValues: List[String]) =>
          assert(csReq.getHeaders(headerName) == null || !csReq.getHeaders(headerName).hasMoreElements,
            s"Header $headerName contained a value when it should not have.")
      }

      // capture headers and rax:device - from request body using xpath
      desc = "capture header/rax:device with required param referencing an xpath in the request body"
      List("/regression/captureHeader", "/regression/raxDevice").foreach { url =>
        it should behave like accessIsAllowedAndRequestIsUpdated("POST", url, List("RSAKEY"), desc, roles("user"),
          req => headerShouldContain(req, DeviceIdHeader, List(xmlPathValue)))
      }

      // capture headers and rax:device - from resource path, valid content
      desc = "capture header/rax:device with required param referencing a resource in the URI with valid content"
      for {
        resource <- List(-100, 0, 42).map(_.toString)
        baseUrl <- List("/regression/captureHeader/", "/regression/raxDevice/")
      } {
        it should behave like accessIsAllowedAndRequestIsUpdated("POST", baseUrl + resource, List("RSAKEY"), desc, roles("user"),
          req => headerShouldContain(req, DeviceIdHeader, List(resource)))
      }

      // capture headers and rax:device - from resource path, malformed content
      desc = "capture header/rax:device with required param referencing a resource in the URI with malformed content"
      List("/regression/captureHeader/notAnInteger", "/regression/raxDevice/notAnInteger").foreach { url =>
        it should behave like resourceNotFound("POST", url, List("RSAKEY"), desc, roles("user"))
      }

      // capture headers - from header, valid content
      desc = "capture header with required param referencing header 'InputHeader' with valid content"
      List(List("user"), List("putrole"), List("user", "putrole")).map(roles(_:_*)).foreach { roleHeaders =>
        it should behave like accessIsAllowedAndRequestIsUpdated("PUT", "/regression/captureHeader/100", List("RSAKEY"), desc,
          roleHeaders ++ Map("InputHeader" -> List("99")), req => headerShouldContain(req, "OutputHeaderPut", List("99")))
      }

      // capture headers - from header, malformed content
      desc = "capture header with required param referencing header 'InputHeader' with malformed content"
      it should behave like requestIsBad("PUT", "/regression/captureHeader/100", List("RSAKEY"), s"$desc - too many values",
        roles("user") ++ Map("InputHeader" -> List("99", "44")))
      it should behave like requestIsBad("PUT", "/regression/captureHeader/100", List("RSAKEY"), s"$desc - missing value", roles("user"))
      it should behave like requestIsBad("PUT", "/regression/captureHeader/100", List("RSAKEY"), s"$desc - not an integer",
        roles("user") ++ Map("InputHeader" -> List("notAnInteger")))
      it should behave like accessIsUnauthorized("PUT", "/regression/captureHeader/100", InvalidAuthBy, s"$desc - invalid authBy",
        roles("user") ++ Map("InputHeader" -> List("99")))

      // capture headers - from header (fixed, repeating, three valid values), valid content
      desc = "capture header with required param referencing header 'InputHeader' that should be FOO, FAR, or OUT"
      for {
        inputHeaderValues <- getAllCombinations(Set("FOO", "FAR", "OUT")).filterNot(_.isEmpty)
        authenticatedBy <- List(List("RSAKEY"), List("APIKEY"), List("RSAKEY", "APIKEY"))
      } {
        it should behave like accessIsAllowedAndRequestIsUpdated("PATCH", "/regression/captureHeader/100", authenticatedBy, desc,
          roles("user") ++ Map("InputHeader" -> inputHeaderValues), req => headerShouldContain(req, "OutputHeaderPatch", inputHeaderValues))
      }

      // capture headers - from header (fixed, repeating, three valid values), malformed content
      List(List("BACON"), List("BACON", "POTATO")).foreach { invalidInputHeaderValues =>
        it should behave like requestIsBad("PATCH", "/regression/captureHeader/100", List("RSAKEY"), desc,
          roles("user") ++ Map("InputHeader" -> invalidInputHeaderValues))
      }
      it should behave like requestIsBad("PATCH", "/regression/captureHeader/100", List("RSAKEY"), desc, roles("user"))
      it should behave like accessIsUnauthorized("PATCH", "/regression/captureHeader/100", InvalidAuthBy, desc,
        roles("user") ++ Map("InputHeader" -> List("FOO")))
      it should behave like raxRoleExpectation("PATCH", "/regression/captureHeader/100", List("RSAKEY"), desc,
        InvalidRaxRoles ++ Map("InputHeader" -> List("FOO")))
    }
  }

  def createRequest(method: String, path: String, authBy: List[String], headers: Map[String, List[String]] = Map.empty) =
    if (method == "GET") {
      base.request(method, path, "", "", parseContent = false, Map(AuthByHeader -> authBy) ++ headers)
    } else {
      base.request(method, path, DefaultMediaType, requestBody, parseContent = false, Map(AuthByHeader -> authBy) ++ headers)
    }

  def createConfig(useSaxon: Boolean): Config = {
    val config = new Config
    config.resultHandler = TestConfig.assertHandler

    if (useSaxon) {
      config.xsdEngine                    = "SaxonEE"
    }

    // these are always true
    config.checkWellFormed                = true
    config.checkElements                  = true
    config.checkPlainParams               = true
    config.checkHeaders                   = true
    config.enableAuthenticatedByExtension = true
    config.enableAnyMatchExtension        = true

    // these will be turned on for individual tests
    config.enableRaxRolesExtension        = false
    config.maskRaxRoles403                = false
    config.removeDups                     = false
    config.joinXPathChecks                = false
    config.enableCaptureHeaderExtension   = false

    config
  }

  def base: BaseValidatorSuite = new BaseValidatorSuite

  def localWADLURI = new File(System.getProperty("user.dir"), "mywadl.wadl").toURI.toString

  def response: HttpServletResponse = mock(classOf[HttpServletResponse])

  def chain: FilterChain = mock(classOf[FilterChain])
}

object GivenAWadlWithAuthenticatedByBase {
  val DefaultMediaType = "application/xml"
  val DefaultRequest =
    <request>
      <representation mediaType={ DefaultMediaType }/>
    </request>
  val AuthByHeader = "X-Authenticated-By"
  val RaxRolesHeader = "X-Roles"
  val DeviceIdHeader = "X-Device-Id"
  val ValidAuthenticatedByValues = Set("PASSWORD", "APIKEY", "PASSCODE", "OTPPASSCODE", "IMPERSONATION", "RSAKEY", "FEDERATED")
  val InvalidAuthBy = List("POTATO")
  val NoAuthBy = List()
  val InvalidRaxRoles = roles("POTATO")
  val NoRaxRoles = Map.empty[String, List[String]]
  val WadlMethods = Set("GET", "POST", "PUT", "DELETE")
  val HttpCodeBadRequest = 400
  val HttpCodeUnauthorized = 401
  val HttpCodeForbidden = 403
  val HttpCodeNotFound = 404

  type AccessExpectation = (String, String, List[String], String, Map[String, List[String]]) => Unit

  type ConfigEnabledOption = Config => Unit

  val EnableRaxRoles: ConfigEnabledOption = _.enableRaxRolesExtension = true
  val EnableMaskRaxRoles403: ConfigEnabledOption = _.maskRaxRoles403 = true
  val EnableRemoveDups: ConfigEnabledOption = _.removeDups = true
  val EnableJoinXPathChecks: ConfigEnabledOption = _.joinXPathChecks = true
  val EnableCaptureHeadersExt: ConfigEnabledOption = _.enableCaptureHeaderExtension = true

  // all of the config option combinations we want to test with
  val ConfigOptionCombinations: List[List[ConfigEnabledOption]] = List(
    List(),
    List(EnableRaxRoles),
    List(EnableCaptureHeadersExt),
    List(EnableRaxRoles, EnableCaptureHeadersExt),
    List(EnableRaxRoles, EnableMaskRaxRoles403, EnableRemoveDups, EnableCaptureHeadersExt),
    List(EnableRaxRoles, EnableMaskRaxRoles403, EnableRemoveDups, EnableJoinXPathChecks, EnableCaptureHeadersExt))

  // get all of the possible combinations, e.g. Set(a, b) => List(List(), List(a), List(b), List(a, b))
  // http://stackoverflow.com/questions/13109720/how-to-create-all-possible-combinations-from-the-elements-of-a-list
  def getAllCombinations[A](values: Set[A]): List[List[A]] = values.subsets.map(_.toList).toList

  def roles(roles: String*): Map[String, List[String]] = Map(RaxRolesHeader -> roles.toList)

  // our test names use the values in the config, so let's make it something meaningful
  def configToString(config: Config): String =
    s"Config(raxRoles=${config.enableRaxRolesExtension}, maskRaxRoles=${config.maskRaxRoles403}, " +
      s"removeDups=${config.removeDups}, joinXPathChecks=${config.joinXPathChecks}, " +
      s"captureHeaderExtension=${config.enableCaptureHeaderExtension})"
}
