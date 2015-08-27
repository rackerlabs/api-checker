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
package com.rackspace.com.papi.components.checker.step

import javax.xml.namespace.QName

import com.rackspace.com.papi.components.checker.step.base.Step
import com.rackspace.com.papi.components.checker.step.results.URLFailResult
import com.rackspace.com.papi.components.checker.step.startend.{URLFailXSD, URLFailXSDMatch}
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.xml.sax.SAXParseException

import scala.xml._

@RunWith(classOf[JUnitRunner])
class StepSuiteSaxonEE extends BaseStepSuiteSaxonEE {

  test("URLFailXSD should return None if URI level has been exceeded : StepType, uuid, evenIntType") {
    val ufx = new URLFailXSD("ufx", "ufx", Array[QName](stepType, uuidType, evenIntType), testSchemaSaxon, 10)
    val res = ufx.check (request("GET", "/START/b"), response,chain, 2)
    assert (res.isEmpty)
    val res2 = ufx.check (request("GET", "/ACCEPT/b"), response,chain, 3)
    assert (res2.isEmpty)
  }

  test("URLFailXSD should return URL fail result if URI level has not been exceeded and the uri type does not match : StepType, uuid, evenIntType") {
    val ufx = new URLFailXSD ("ufmx", "ufmx", Array[QName](stepType, uuidType, evenIntType), testSchemaSaxon, 10)
    val res = ufx.check (request("GET", "/a/b"), response,chain, 0)
    assert (res.isDefined)
    assert (res.get.isInstanceOf[URLFailResult])
    val res2 = ufx.check (request("GET", "/a/b"), response,chain, 1)
    assert (res2.isDefined)
    assert (res2.get.isInstanceOf[URLFailResult])
  }


  test("URLFailXSD should return URL None if URI level has not been exceeded and the uri type matches : StepType, uuid, evenIntType") {
    val ufx = new URLFailXSD ("ufmx", "ufmx", Array[QName](stepType, uuidType, evenIntType), testSchemaSaxon, 10)
    val res = ufx.check (request("GET", "/START/b"), response,chain, 0)
    assert (res.isEmpty)
    val res2 = ufx.check (request("GET", "/eb507026-6463-11e1-b7aa-8b7b918a1623/b"), response,chain, 0)
    assert (res2.isEmpty)
    val res3 = ufx.check (request("GET", "/90/b"), response,chain, 0)
    assert (res3.isEmpty)
  }

  test("URLFailXSDMatch should return None if URI level has been exceeded : StepType, uuid, evenIntType") {
    val ufx = new URLFailXSDMatch("ufx", "ufx", "c".r, Array[QName](stepType, uuidType, evenIntType), testSchemaSaxon, 10)
    val res = ufx.check (request("GET", "/START/b"), response,chain, 2)
    assert (res.isEmpty)
    val res2 = ufx.check (request("GET", "/ACCEPT/b"), response,chain, 3)
    assert (res2.isEmpty)
    val res3 = ufx.check (request("GET", "/c/b"), response,chain, 4)
    assert (res3.isEmpty)
  }

  test("URLFailXSDMatch should return URL fail result if URI level has not been exceeded and the uri type does not match : StepType, uuid, evenIntType") {
    val ufx = new URLFailXSDMatch ("ufmx", "ufmx", "c".r, Array[QName](stepType, uuidType, evenIntType), testSchemaSaxon, 10)
    val res = ufx.check (request("GET", "/a/b"), response,chain, 0)
    assert (res.isDefined)
    assert (res.get.isInstanceOf[URLFailResult])
    val res2 = ufx.check (request("GET", "/a/b"), response,chain, 1)
    assert (res2.isDefined)
    assert (res2.get.isInstanceOf[URLFailResult])
  }

  test("URLFailXSDMatch should return URL None if URI level has not been exceeded and the uri type matches : StepType, uuid, evenIntType") {
    val ufx = new URLFailXSDMatch ("ufmx", "ufmx", "c".r, Array[QName](stepType, uuidType, evenIntType), testSchemaSaxon, 10)
    val res = ufx.check (request("GET", "/START/b"), response,chain, 0)
    assert (res.isEmpty)
    val res2 = ufx.check (request("GET", "/eb507026-6463-11e1-b7aa-8b7b918a1623/b"), response,chain, 0)
    assert (res2.isEmpty)
    val res3 = ufx.check (request("GET", "/90/b"), response,chain, 0)
    assert (res3.isEmpty)
    val res4 = ufx.check (request("GET", "/c/b"), response,chain, 0)
    assert (res4.isEmpty)
  }

  test("URIXSD mismatch message should be the same as the QName") {
    val urixsd = new URIXSD("uxd", "uxd", stepType, testSchemaSaxon, Array[Step]())
    assert (urixsd.mismatchMessage == stepType.toString)
  }

  test("In a URIXSD step, if there is a URI match, the step should proceed to the next step : StepType") {
    val urixsd = new URIXSD("uxd", "uxd", stepType, testSchemaSaxon, Array[Step]())
    assert (urixsd.check (request("GET", "/START/b"), response,chain, 0).isEmpty)
    assert (urixsd.check (request("GET", "/URL_FAIL/b"), response,chain, 0).isEmpty)
    assert (urixsd.check (request("GET", "/METHOD_FAIL/b"), response,chain, 0).isEmpty)
    assert (urixsd.check (request("GET", "/ACCEPT/b"), response,chain, 0).isEmpty)
    assert (urixsd.check (request("GET", "/URL/b"), response,chain, 0).isEmpty)
    assert (urixsd.check (request("GET", "/METHOD/b"), response,chain, 0).isEmpty)
    assert (urixsd.check (request("GET", "/URLXSD/b"), response,chain, 0).isEmpty)
  }

  test("In a URIXSD step, if there is a mismatch, a MismatchResult should be returned: StepType") {
    val urixsd = new URIXSD("uxd", "uxd", stepType, testSchemaSaxon, Array[Step]())
    assertMismatchResult (urixsd.check (request("GET", "/ATART/b"), response,chain, 0))
    assertMismatchResult (urixsd.check (request("GET", "/URL_FAI2/b"), response,chain, 0))
    assertMismatchResult (urixsd.check (request("GET", "/METHO4_FAIL/b"), response,chain, 0))
    assertMismatchResult (urixsd.check (request("GET", "/ACCCPT/b"), response,chain, 0))
    assertMismatchResult (urixsd.check (request("GET", "/URLL/b"), response,chain, 0))
    assertMismatchResult (urixsd.check (request("GET", "/METH0D/b"), response,chain, 0))
    assertMismatchResult (urixsd.check (request("GET", "/UR7XSD/b"), response,chain, 0))
  }

  test("In a URIXSD step, if there is a URI match, but the level has been exceeded a MismatchResult should be returned: StepType") {
    val urixsd = new URIXSD("uxd", "uxd", stepType, testSchemaSaxon, Array[Step]())
    assertMismatchResult (urixsd.check (request("GET", "/START/b"), response,chain, 2))
    assertMismatchResult (urixsd.check (request("GET", "/URL_FAIL/b"), response,chain, 2))
    assertMismatchResult (urixsd.check (request("GET", "/METHOD_FAIL/b"), response,chain, 2))
    assertMismatchResult (urixsd.check (request("GET", "/ACCEPT/b"), response,chain, 2))
    assertMismatchResult (urixsd.check (request("GET", "/URL/b"), response,chain, 2))
    assertMismatchResult (urixsd.check (request("GET", "/METHOD/b"), response,chain, 2))
    assertMismatchResult (urixsd.check (request("GET", "/URLXSD/b"), response,chain, 2))
  }

  test("In a URIXSD step, if there is a URI match, the step should proceed to the next step : UUID") {
    val urixsd = new URIXSD("uxd", "uxd", uuidType, testSchemaSaxon, Array[Step]())
    assert (urixsd.check (request("GET", "/55b76e92-6450-11e1-9012-37afadb5ff61/b"), response,chain, 0).isEmpty)
    assert (urixsd.check (request("GET", "/56d7a1fc-6450-11e1-b360-8fe15f519bf2/b"), response,chain, 0).isEmpty)
    assert (urixsd.check (request("GET", "/5731bb7e-6450-11e1-9b88-6ff2691237cd/b"), response,chain, 0).isEmpty)
    assert (urixsd.check (request("GET", "/578952c6-6450-11e1-892b-8bae86031338/b"), response,chain, 0).isEmpty)
    assert (urixsd.check (request("GET", "/57e75268-6450-11e1-892e-abc2baf50960/b"), response,chain, 0).isEmpty)
    assert (urixsd.check (request("GET", "/58415556-6450-11e1-96f9-17b1db29daf7/b"), response,chain, 0).isEmpty)
    assert (urixsd.check (request("GET", "/58a0ff60-6450-11e1-95bd-77590a8a0a53/b"), response,chain, 0).isEmpty)
  }

  test("In a URIXSD step, if there is a mismatch, a MismatchResult should be returned: UUID") {
    val urixsd = new URIXSD("uxd", "uxd", uuidType, testSchemaSaxon, Array[Step]())
    assertMismatchResult (urixsd.check (request("GET", "/55b76e92-6450-11e1-9012-37afadbgff61/b"), response,chain, 0))
    assertMismatchResult (urixsd.check (request("GET", "/55/b"), response,chain, 0))
    assertMismatchResult (urixsd.check (request("GET", "/aoeee..x/b"), response,chain, 0))
    assertMismatchResult (urixsd.check (request("GET", "/09cgff.dehbj/b"), response,chain, 0))
    assertMismatchResult (urixsd.check (request("GET", "/55b76e92-6450-11e1-901237afadb5ff61/b"), response,chain, 0))
    assertMismatchResult (urixsd.check (request("GET", "/58415556-6450-11e1-96f9:17b1db29daf7/b"), response,chain, 0))
    assertMismatchResult (urixsd.check (request("GET", "/+58a0ff60-6450-11e1-95bd-77590a8a0a53/b"), response,chain, 0))
  }

  test("In a URIXSD step, if there is a URI match, but the level has been exceeded a MismatchResult should be returned: UUID") {
    val urixsd = new URIXSD("uxd", "uxd", uuidType, testSchemaSaxon, Array[Step]())
    assertMismatchResult (urixsd.check (request("GET", "/55b76e92-6450-11e1-9012-37afadb5ff61/b"), response,chain, 2))
    assertMismatchResult (urixsd.check (request("GET", "/56d7a1fc-6450-11e1-b360-8fe15f519bf2/b"), response,chain, 2))
    assertMismatchResult (urixsd.check (request("GET", "/5731bb7e-6450-11e1-9b88-6ff2691237cd/b"), response,chain, 2))
    assertMismatchResult (urixsd.check (request("GET", "/578952c6-6450-11e1-892b-8bae86031338/b"), response,chain, 2))
    assertMismatchResult (urixsd.check (request("GET", "/57e75268-6450-11e1-892e-abc2baf50960/b"), response,chain, 2))
    assertMismatchResult (urixsd.check (request("GET", "/58415556-6450-11e1-96f9-17b1db29daf7/b"), response,chain, 2))
    assertMismatchResult (urixsd.check (request("GET", "/58a0ff60-6450-11e1-95bd-77590a8a0a53/b"), response,chain, 2))
  }

  test("In a URIXSD step, if there is a URI match, the step should proceed to the next step : EvenInt100") {
    val urixsd = new URIXSD("uxd", "uxd", evenIntType, testSchemaSaxon, Array[Step]())
    assert (urixsd.check (request("GET", "/54/b"), response,chain, 0).isEmpty)
    assert (urixsd.check (request("GET", "/0/b"), response,chain, 0).isEmpty)
    assert (urixsd.check (request("GET", "/32/b"), response,chain, 0).isEmpty)
    assert (urixsd.check (request("GET", "/2/b"), response,chain, 0).isEmpty)
    assert (urixsd.check (request("GET", "/12/b"), response,chain, 0).isEmpty)
    assert (urixsd.check (request("GET", "/100/b"), response,chain, 0).isEmpty)
    assert (urixsd.check (request("GET", "/84/b"), response,chain, 0).isEmpty)
  }

  test("In a URIXSD step, if there is a mismatch, a MismatchResult should be returned: EvenInt100, assert") {
    val urixsd = new URIXSD("uxd", "uxd", evenIntType, testSchemaSaxon, Array[Step]())
    assertMismatchResult (urixsd.check (request("GET", "/55/b"), response,chain, 0))
    assertMismatchResult (urixsd.check (request("GET", "/1/b"), response,chain, 0))
    assertMismatchResult (urixsd.check (request("GET", "/33/b"), response,chain, 0))
    assertMismatchResult (urixsd.check (request("GET", "/3/b"), response,chain, 0))
    assertMismatchResult (urixsd.check (request("GET", "/15/b"), response,chain, 0))
    assertMismatchResult (urixsd.check (request("GET", "/101/b"), response,chain, 0))
    assertMismatchResult (urixsd.check (request("GET", "/85/b"), response,chain, 0))
  }

  test("In a URIXSD step, if there is a mismatch, a MismatchResult should be returned: EvenInt100") {
    val urixsd = new URIXSD("uxd", "uxd", evenIntType, testSchemaSaxon, Array[Step]())
    assertMismatchResult (urixsd.check (request("GET", "/101/b"), response,chain, 0))
    assertMismatchResult (urixsd.check (request("GET", "/555/b"), response,chain, 0))
    assertMismatchResult (urixsd.check (request("GET", "/hello/b"), response,chain, 0))
    assertMismatchResult (urixsd.check (request("GET", "/09cgff.dehbj/b"), response,chain, 0))
    assertMismatchResult (urixsd.check (request("GET", "/-99/b"), response,chain, 0))
    assertMismatchResult (urixsd.check (request("GET", "/3tecr/b"), response,chain, 0))
    assertMismatchResult (urixsd.check (request("GET", "/58a0ff60-6450-11e1-95bd-77590a8a0a53/b"), response,chain, 0))
  }

  test("In a URIXSD step, if there is a URI match, but the level has been exceeded a MismatchResult should be returned: EvenInt100") {
    val urixsd = new URIXSD("uxd", "uxd", evenIntType, testSchemaSaxon, Array[Step]())
    assertMismatchResult (urixsd.check (request("GET", "/54/b"), response,chain, 2))
    assertMismatchResult (urixsd.check (request("GET", "/0/b"), response,chain, 2))
    assertMismatchResult (urixsd.check (request("GET", "/32/b"), response,chain, 2))
    assertMismatchResult (urixsd.check (request("GET", "/2/b"), response,chain, 2))
    assertMismatchResult (urixsd.check (request("GET", "/12/b"), response,chain, 2))
    assertMismatchResult (urixsd.check (request("GET", "/100/b"), response,chain, 2))
    assertMismatchResult (urixsd.check (request("GET", "/84/b"), response,chain, 2))
  }

  test ("In an XSD test, if the content contains valid XML, the uriLevel should stay the same") {
    val xsd = new XSD("XSD", "XSD", testSchemaSaxon, false, 10, Array[Step]())
    val req1 = request ("PUT", "/a/b", "application/xml",
                        <e xmlns="http://www.rackspace.com/repose/wadl/checker/step/test">
                          <id>f76d5638-bb4f-11e1-abb0-539c4b93e64a</id>
                          <stepType>START</stepType>
                          <even>22</even>
                        </e>, true)
    val req2 = request ("PUT", "/a/b", "application/xml",
                        <a xmlns="http://www.rackspace.com/repose/wadl/checker/step/test"
                           id="f76d5638-bb4f-11e1-abb0-539c4b93e64a"
                           stepType="START"
                           even="22"/>, true)

    assert (xsd.checkStep (req1, response, chain, 0) == 0)
    assert (xsd.checkStep (req2, response, chain, 1) == 1)
  }

  test ("In an XSD test, if the content contains valid XML, the contentErrorPriority should be -1") {
    val xsd = new XSD("XSD", "XSD", testSchemaSaxon, false, 10, Array[Step]())
    val req1 = request ("PUT", "/a/b", "application/xml",
                        <e xmlns="http://www.rackspace.com/repose/wadl/checker/step/test">
                          <id>f76d5638-bb4f-11e1-abb0-539c4b93e64a</id>
                          <stepType>START</stepType>
                          <even>22</even>
                        </e>, true)
    val req2 = request ("PUT", "/a/b", "application/xml",
                        <a xmlns="http://www.rackspace.com/repose/wadl/checker/step/test"
                           id="f76d5638-bb4f-11e1-abb0-539c4b93e64a"
                           stepType="START"
                           even="22"/>, true)

    xsd.checkStep (req1, response, chain, 0)
    xsd.checkStep (req2, response, chain, 1)
    assert (req1.contentErrorPriority == -1)
    assert (req2.contentErrorPriority == -1)
  }

  test ("In an XSD test, if the content contains invalid XML, the uriLevel should be -1") {
    val xsd = new XSD("XSD", "XSD", testSchemaSaxon, false, 10, Array[Step]())
    val req1 = request ("PUT", "/a/b", "application/xml",
                        <e xmlns="http://www.rackspace.com/repose/wadl/checker/step/test">
                          <id>f76d5638-bb4f-11e1-abb0-539c4b93e64aaa</id>
                          <stepType>START</stepType>
                          <even>22</even>
                        </e>, true)
    val req2 = request ("PUT", "/a/b", "application/xml",
                        <a xmlns="http://www.rackspace.com/repose/wadl/checker/step/test"
                           id="f76d5638-bb4f-11e1-abb0-539c4b93e64aaaa"
                           stepType="START"
                           even="22"/>, true)

    assert (xsd.checkStep (req1, response, chain, 0) == -1)
    assert (xsd.checkStep (req2, response, chain, 1) == -1)
  }


  test ("In an XSD test, if the content contains invalid XML, the contentErrorPriority should be set") {
    val xsd = new XSD("XSD", "XSD", testSchemaSaxon, false, 10, Array[Step]())
    val req1 = request ("PUT", "/a/b", "application/xml",
                        <e xmlns="http://www.rackspace.com/repose/wadl/checker/step/test">
                          <id>f76d5638-bb4f-11e1-abb0-539c4b93e64aaa</id>
                          <stepType>START</stepType>
                          <even>22</even>
                        </e>, true)
    val req2 = request ("PUT", "/a/b", "application/xml",
                        <a xmlns="http://www.rackspace.com/repose/wadl/checker/step/test"
                           id="f76d5638-bb4f-11e1-abb0-539c4b93e64aaaa"
                           stepType="START"
                           even="22"/>, true)

    xsd.checkStep (req1, response, chain, 0)
    xsd.checkStep (req2, response, chain, 1)
    assert (req1.contentErrorPriority == 10)
    assert (req2.contentErrorPriority == 10)
  }

  test ("In an XSD test, if the content contains invalid XML, the request should contain a SAXException") {
    val xsd = new XSD("XSD", "XSD", testSchemaSaxon, false, 10, Array[Step]())
    val req1 = request ("PUT", "/a/b", "application/xml",
                        <e xmlns="http://www.rackspace.com/repose/wadl/checker/step/test">
                          <id>f76d5638-bb4f-11e1-abb0-539c4b93e64aaa</id>
                          <stepType>START</stepType>
                          <even>22</even>
                        </e>, true)
    val req2 = request ("PUT", "/a/b", "application/xml",
                        <a xmlns="http://www.rackspace.com/repose/wadl/checker/step/test"
                           id="f76d5638-bb4f-11e1-abb0-539c4b93e64aaaa"
                           stepType="START"
                           even="22"/>, true)

    xsd.checkStep (req1, response, chain, 0)
    assert (req1.contentError != null)
    assert (req1.contentError.isInstanceOf[SAXParseException])

    xsd.checkStep (req2, response, chain, 1)
    assert (req2.contentError != null)
    assert (req2.contentError.isInstanceOf[SAXParseException])
  }

  test ("In an XSD test, if the content contains invalid XML, the uriLevel should be -1 (XSD 1.1 assert)") {
    val xsd = new XSD("XSD", "XSD", testSchemaSaxon, false, 10, Array[Step]())
    val req1 = request ("PUT", "/a/b", "application/xml",
                        <e xmlns="http://www.rackspace.com/repose/wadl/checker/step/test">
                          <id>309a8f1e-bb52-11e1-b9d9-b7652ca2118a</id>
                          <stepType>START</stepType>
                          <even>23</even>
                        </e>, true)
    val req2 = request ("PUT", "/a/b", "application/xml",
                        <a xmlns="http://www.rackspace.com/repose/wadl/checker/step/test"
                           id="309a8f1e-bb52-11e1-b9d9-b7652ca2118a"
                           stepType="START"
                           even="23"/>, true)

    assert (xsd.checkStep (req1, response, chain, 0) == -1)
    assert (xsd.checkStep (req2, response, chain, 1) == -1)
  }

  test ("In an XSD test, if the content contains invalid XML, the contentErrorPrioirty should be set (XSD 1.1 assert)") {
    val xsd = new XSD("XSD", "XSD", testSchemaSaxon, false, 1000, Array[Step]())
    val req1 = request ("PUT", "/a/b", "application/xml",
                        <e xmlns="http://www.rackspace.com/repose/wadl/checker/step/test">
                          <id>309a8f1e-bb52-11e1-b9d9-b7652ca2118a</id>
                          <stepType>START</stepType>
                          <even>23</even>
                        </e>, true)
    val req2 = request ("PUT", "/a/b", "application/xml",
                        <a xmlns="http://www.rackspace.com/repose/wadl/checker/step/test"
                           id="309a8f1e-bb52-11e1-b9d9-b7652ca2118a"
                           stepType="START"
                           even="23"/>, true)

    xsd.checkStep (req1, response, chain, 0)
    xsd.checkStep (req2, response, chain, 1)
    assert (req1.contentErrorPriority == 1000)
    assert (req2.contentErrorPriority == 1000)
  }


  test ("In an XSD test, if the content contains invalid XML, the request should contain a SAXException (XSD 1.1 assert)") {
    val xsd = new XSD("XSD", "XSD", testSchemaSaxon, false, 10, Array[Step]())
    val req1 = request ("PUT", "/a/b", "application/xml",
                        <e xmlns="http://www.rackspace.com/repose/wadl/checker/step/test">
                          <id>309a8f1e-bb52-11e1-b9d9-b7652ca2118a</id>
                          <stepType>START</stepType>
                          <even>23</even>
                        </e>, true)
    val req2 = request ("PUT", "/a/b", "application/xml",
                        <a xmlns="http://www.rackspace.com/repose/wadl/checker/step/test"
                           id="309a8f1e-bb52-11e1-b9d9-b7652ca2118a"
                           stepType="START"
                           even="23"/>, true)

    xsd.checkStep (req1, response, chain, 0)
    assert (req1.contentError != null)
    assert (req1.contentError.isInstanceOf[SAXParseException])

    xsd.checkStep (req2, response, chain, 1)
    assert (req2.contentError != null)
    assert (req2.contentError.isInstanceOf[SAXParseException])
  }

  test ("In an XSD test, if the content contains valid XML, the uriLevel should stay the same (transform == true)") {
    val xsd = new XSD("XSD", "XSD", testSchemaSaxon, true, 10, Array[Step]())
    val req1 = request ("PUT", "/a/b", "application/xml",
                        <e xmlns="http://www.rackspace.com/repose/wadl/checker/step/test">
                          <id>f76d5638-bb4f-11e1-abb0-539c4b93e64a</id>
                          <stepType>START</stepType>
                          <even>22</even>
                        </e>, true)
    val req2 = request ("PUT", "/a/b", "application/xml",
                        <a xmlns="http://www.rackspace.com/repose/wadl/checker/step/test"
                           id="f76d5638-bb4f-11e1-abb0-539c4b93e64a"
                           stepType="START"
                           even="22"/>, true)

    assert (xsd.checkStep (req1, response, chain, 0) == 0)
    assert (xsd.checkStep (req2, response, chain, 1) == 1)
  }

  test ("In an XSD test, if the content contains valid XML, the contentErrorPrioirty should be -1 (transform == true)") {
    val xsd = new XSD("XSD", "XSD", testSchemaSaxon, true, 10, Array[Step]())
    val req1 = request ("PUT", "/a/b", "application/xml",
                        <e xmlns="http://www.rackspace.com/repose/wadl/checker/step/test">
                          <id>f76d5638-bb4f-11e1-abb0-539c4b93e64a</id>
                          <stepType>START</stepType>
                          <even>22</even>
                        </e>, true)
    val req2 = request ("PUT", "/a/b", "application/xml",
                        <a xmlns="http://www.rackspace.com/repose/wadl/checker/step/test"
                           id="f76d5638-bb4f-11e1-abb0-539c4b93e64a"
                           stepType="START"
                           even="22"/>, true)

    xsd.checkStep (req1, response, chain, 0)
    xsd.checkStep (req2, response, chain, 1)
    assert (req1.contentErrorPriority == -1)
    assert (req2.contentErrorPriority == -1)
  }


  test ("In an XSD test, if the content contains valid XML1, with transform == true, then default values should be filled in") {
    val xsd = new XSD("XSD", "XSD", testSchemaSaxon, true, 10, Array[Step]())
    val req1 = request ("PUT", "/a/b", "application/xml",
                        <e xmlns="http://www.rackspace.com/repose/wadl/checker/step/test">
                         <id>21f1fcf6-bf38-11e1-878e-133ab65fcec3</id>
                        <stepType/>
                        <even/>
                       </e>, true)

    xsd.checkStep (req1, response, chain, 0)

    val updatedRequest = XML.load(req1.getInputStream())
    assert ((updatedRequest \ "stepType").text == "START")
    assert ((updatedRequest \ "even").text == "50")
  }

  test ("In an XSD test, if the content contains valid XML2, with transform == true, then default values should be filled in") {
    val xsd = new XSD("XSD", "XSD", testSchemaSaxon, true, 10, Array[Step]())
    val req1 = request ("PUT", "/a/b", "application/xml",
                        <a xmlns="http://www.rackspace.com/repose/wadl/checker/step/test"
                           id="f76d5638-bb4f-11e1-abb0-539c4b93e64a"/>, true)

    xsd.checkStep (req1, response, chain, 0)

    val updatedRequest = XML.load(req1.getInputStream())
    assert ((updatedRequest \ "@stepType").text == "START")
    assert ((updatedRequest \ "@even").text == "50")
  }

  test ("In an XSD test, if the content contains invalid XML, the uriLevel should be -1 (transform == true)") {
    val xsd = new XSD("XSD", "XSD", testSchemaSaxon, true, 10, Array[Step]())
    val req1 = request ("PUT", "/a/b", "application/xml",
                        <e xmlns="http://www.rackspace.com/repose/wadl/checker/step/test">
                          <id>f76d5638-bb4f-11e1-abb0-539c4b93e64aaa</id>
                          <stepType>START</stepType>
                          <even>22</even>
                        </e>, true)
    val req2 = request ("PUT", "/a/b", "application/xml",
                        <a xmlns="http://www.rackspace.com/repose/wadl/checker/step/test"
                           id="f76d5638-bb4f-11e1-abb0-539c4b93e64aaaa"
                           stepType="START"
                           even="22"/>, true)

    assert (xsd.checkStep (req1, response, chain, 0) == -1)
    assert (xsd.checkStep (req2, response, chain, 1) == -1)
  }

  test ("In an XSD test, if the content contains invalid XML, the contentErrorPriority should be set (transform == true)") {
    val xsd = new XSD("XSD", "XSD", testSchemaSaxon, true, 10, Array[Step]())
    val req1 = request ("PUT", "/a/b", "application/xml",
                        <e xmlns="http://www.rackspace.com/repose/wadl/checker/step/test">
                          <id>f76d5638-bb4f-11e1-abb0-539c4b93e64aaa</id>
                          <stepType>START</stepType>
                          <even>22</even>
                        </e>, true)
    val req2 = request ("PUT", "/a/b", "application/xml",
                        <a xmlns="http://www.rackspace.com/repose/wadl/checker/step/test"
                           id="f76d5638-bb4f-11e1-abb0-539c4b93e64aaaa"
                           stepType="START"
                           even="22"/>, true)

    xsd.checkStep (req1, response, chain, 0)
    xsd.checkStep (req2, response, chain, 1)
    assert (req1.contentErrorPriority == 10)
    assert (req2.contentErrorPriority == 10)
  }


  test ("In an XSD test, if the content contains invalid XML, the request should contain a SAXException (transform == true)") {
    val xsd = new XSD("XSD", "XSD", testSchemaSaxon, true, 10, Array[Step]())
    val req1 = request ("PUT", "/a/b", "application/xml",
                        <e xmlns="http://www.rackspace.com/repose/wadl/checker/step/test">
                          <id>f76d5638-bb4f-11e1-abb0-539c4b93e64aaa</id>
                          <stepType>START</stepType>
                          <even>22</even>
                        </e>, true)
    val req2 = request ("PUT", "/a/b", "application/xml",
                        <a xmlns="http://www.rackspace.com/repose/wadl/checker/step/test"
                           id="f76d5638-bb4f-11e1-abb0-539c4b93e64aaaa"
                           stepType="START"
                           even="22"/>, true)

    xsd.checkStep (req1, response, chain, 0)
    assert (req1.contentError != null)
    assert (req1.contentError.isInstanceOf[SAXParseException])

    xsd.checkStep (req2, response, chain, 1)
    assert (req2.contentError != null)
    assert (req2.contentError.isInstanceOf[SAXParseException])
  }

  test ("In an XSD test, if the content contains invalid XML, the uriLevel should be -1 (XSD 1.1 assert, transform == true)") {
    val xsd = new XSD("XSD", "XSD", testSchemaSaxon, true, 10, Array[Step]())
    val req1 = request ("PUT", "/a/b", "application/xml",
                        <e xmlns="http://www.rackspace.com/repose/wadl/checker/step/test">
                          <id>309a8f1e-bb52-11e1-b9d9-b7652ca2118a</id>
                          <stepType>START</stepType>
                          <even>23</even>
                        </e>, true)
    val req2 = request ("PUT", "/a/b", "application/xml",
                        <a xmlns="http://www.rackspace.com/repose/wadl/checker/step/test"
                           id="309a8f1e-bb52-11e1-b9d9-b7652ca2118a"
                           stepType="START"
                           even="23"/>, true)

    assert (xsd.checkStep (req1, response, chain, 0) == -1)
    assert (xsd.checkStep (req2, response, chain, 1) == -1)
  }

  test ("In an XSD test, if the content contains invalid XML, the contentErrorPrioirty should be set (XSD 1.1 assert, transform == true)") {
    val xsd = new XSD("XSD", "XSD", testSchemaSaxon, true, 10, Array[Step]())
    val req1 = request ("PUT", "/a/b", "application/xml",
                        <e xmlns="http://www.rackspace.com/repose/wadl/checker/step/test">
                          <id>309a8f1e-bb52-11e1-b9d9-b7652ca2118a</id>
                          <stepType>START</stepType>
                          <even>23</even>
                        </e>, true)
    val req2 = request ("PUT", "/a/b", "application/xml",
                        <a xmlns="http://www.rackspace.com/repose/wadl/checker/step/test"
                           id="309a8f1e-bb52-11e1-b9d9-b7652ca2118a"
                           stepType="START"
                           even="23"/>, true)

    xsd.checkStep (req1, response, chain, 0)
    xsd.checkStep (req2, response, chain, 1)
    assert (req1.contentErrorPriority == 10)
    assert (req2.contentErrorPriority == 10)
  }


  test ("In an XSD test, if the content contains invalid XML, the request should contain a SAXException (XSD 1.1 assert, transform == true)") {
    val xsd = new XSD("XSD", "XSD", testSchemaSaxon, true, 10, Array[Step]())
    val req1 = request ("PUT", "/a/b", "application/xml",
                        <e xmlns="http://www.rackspace.com/repose/wadl/checker/step/test">
                          <id>309a8f1e-bb52-11e1-b9d9-b7652ca2118a</id>
                          <stepType>START</stepType>
                          <even>23</even>
                        </e>, true)
    val req2 = request ("PUT", "/a/b", "application/xml",
                        <a xmlns="http://www.rackspace.com/repose/wadl/checker/step/test"
                           id="309a8f1e-bb52-11e1-b9d9-b7652ca2118a"
                           stepType="START"
                           even="23"/>, true)

    xsd.checkStep (req1, response, chain, 0)
    assert (req1.contentError != null)
    assert (req1.contentError.isInstanceOf[SAXParseException])

    xsd.checkStep (req2, response, chain, 1)
    assert (req2.contentError != null)
    assert (req2.contentError.isInstanceOf[SAXParseException])
  }

  test ("In an XSD header step, if the header is available then the uri level should stay the same.") {
    val header = new HeaderXSD("HEADER", "HEADER", "X-ID", uuidType, testSchemaSaxon, 10, Array[Step]())
    val req1 = request("GET", "/path/to/resource", "", "", false, Map("X-ID"->List("28d42e00-e25a-11e1-9897-efbf2fa68353")))
    val req2 = request("GET", "/path/to/resource", "", "", false, Map("X-ID"->List("2fbf4592-e25a-11e1-bae1-93374682bd20")))

    assert (header.checkStep (req1, response, chain, 0) == 0)
    assert (header.checkStep (req2, response, chain, 1) == 1)
  }

  test ("In an XSD header step, if the header is available then the contentErrorPriority should be -1.") {
    val header = new HeaderXSD("HEADER", "HEADER", "X-ID", uuidType, testSchemaSaxon, 10, Array[Step]())
    val req1 = request("GET", "/path/to/resource", "", "", false, Map("X-ID"->List("28d42e00-e25a-11e1-9897-efbf2fa68353")))
    val req2 = request("GET", "/path/to/resource", "", "", false, Map("X-ID"->List("2fbf4592-e25a-11e1-bae1-93374682bd20")))

    header.checkStep (req1, response, chain, 0)
    header.checkStep (req2, response, chain, 1)
    assert (req1.contentErrorPriority == -1)
    assert (req2.contentErrorPriority == -1)
  }

  test ("In an XSD header step, if the header is available then the uri level should stay the same. (Multiple Headers)") {
    val header = new HeaderXSD("HEADER", "HEADER", "X-ID", uuidType, testSchemaSaxon, 10, Array[Step]())
    val req1 = request("GET", "/path/to/resource", "", "", false, Map("X-ID"->List("28d42e00-e25a-11e1-9897-efbf2fa68353", "c8678844-3288-11e2-835c-c71ff3985a57")))
    val req2 = request("GET", "/path/to/resource", "", "", false, Map("X-ID"->List("2fbf4592-e25a-11e1-bae1-93374682bd20", "d389f680-3288-11e2-b58a-638ddd4222be")))

    assert (header.checkStep (req1, response, chain, 0) == 0)
    assert (header.checkStep (req2, response, chain, 1) == 1)
  }

  test ("In an XSD header step, if the header is available then the contentErrorPriority should be -1 (Multiple Headers)") {
    val header = new HeaderXSD("HEADER", "HEADER", "X-ID", uuidType, testSchemaSaxon, 10, Array[Step]())
    val req1 = request("GET", "/path/to/resource", "", "", false, Map("X-ID"->List("28d42e00-e25a-11e1-9897-efbf2fa68353", "c8678844-3288-11e2-835c-c71ff3985a57")))
    val req2 = request("GET", "/path/to/resource", "", "", false, Map("X-ID"->List("2fbf4592-e25a-11e1-bae1-93374682bd20", "d389f680-3288-11e2-b58a-638ddd4222be")))

    header.checkStep (req1, response, chain, 0)
    header.checkStep (req2, response, chain, 1)
    assert (req1.contentErrorPriority == -1)
    assert (req2.contentErrorPriority == -1)
  }


  test ("In an XSD header step, if the header is available then the uri level should stay the same. (Multiple Items in a single header)") {
    val header = new HeaderXSD("HEADER", "HEADER", "X-ID", uuidType, testSchemaSaxon, 10, Array[Step]())
    val req1 = request("GET", "/path/to/resource", "", "", false, Map("X-ID"->List("28d42e00-e25a-11e1-9897-efbf2fa68353, c8678844-3288-11e2-835c-c71ff3985a57")))
    val req2 = request("GET", "/path/to/resource", "", "", false, Map("X-ID"->List("2fbf4592-e25a-11e1-bae1-93374682bd20, d389f680-3288-11e2-b58a-638ddd4222be")))

    assert (header.checkStep (req1, response, chain, 0) == 0)
    assert (header.checkStep (req2, response, chain, 1) == 1)
  }

  test ("In an XSD header step, if the header is available then the contentErrorPriority should be -1. (Multiple Items in a single header)") {
    val header = new HeaderXSD("HEADER", "HEADER", "X-ID", uuidType, testSchemaSaxon, 10, Array[Step]())
    val req1 = request("GET", "/path/to/resource", "", "", false, Map("X-ID"->List("28d42e00-e25a-11e1-9897-efbf2fa68353, c8678844-3288-11e2-835c-c71ff3985a57")))
    val req2 = request("GET", "/path/to/resource", "", "", false, Map("X-ID"->List("2fbf4592-e25a-11e1-bae1-93374682bd20, d389f680-3288-11e2-b58a-638ddd4222be")))

    header.checkStep (req1, response, chain, 0)
    header.checkStep (req2, response, chain, 1)
    assert (req1.contentErrorPriority == -1)
    assert (req2.contentErrorPriority == -1)
  }

  test ("In an XSD header step, if the header is available, but the content is not correct, the uri level should be -1") {
    val header = new HeaderXSD("HEADER", "HEADER", "X-ID", uuidType, testSchemaSaxon, 10, Array[Step]())
    val req1 = request("GET", "/path/to/resource", "", "", false, Map("X-ID"->List("28d42e00-e25a-11e1-9897-efbf2Za68353")))
    val req2 = request("GET", "/path/to/resource", "", "", false, Map("X-ID"->List("2fbf4592-e25a-11e1bae1-93374682bd20")))

    assert (header.checkStep (req1, response, chain, 0) == -1)
    assert (header.checkStep (req2, response, chain, 1) == -1)
  }

  test ("In an XSD header step, if the header is available, but the content is not correct, the contentErrorPriority should be set") {
    val header = new HeaderXSD("HEADER", "HEADER", "X-ID", uuidType, testSchemaSaxon, 10, Array[Step]())
    val req1 = request("GET", "/path/to/resource", "", "", false, Map("X-ID"->List("28d42e00-e25a-11e1-9897-efbf2Za68353")))
    val req2 = request("GET", "/path/to/resource", "", "", false, Map("X-ID"->List("2fbf4592-e25a-11e1bae1-93374682bd20")))

    header.checkStep (req1, response, chain, 0)
    header.checkStep (req2, response, chain, 1)
    assert (req1.contentErrorPriority == 10)
    assert (req2.contentErrorPriority == 10)
  }

  test ("In an XSD header step, if the header is available, but the content is not correct, the uri level should be -1 (Multiple Headers)") {
    val header = new HeaderXSD("HEADER", "HEADER", "X-ID", uuidType, testSchemaSaxon, 10, Array[Step]())
    val req1 = request("GET", "/path/to/resource", "", "", false, Map("X-ID"->List("c8678844-3288-11e2-835c-c71ff3985a57", "28d42e00-e25a-11e1-9897-efbf2Za68353")))
    val req2 = request("GET", "/path/to/resource", "", "", false, Map("X-ID"->List("d389f680-3288-11e2-b58a-638ddd4222be", "2fbf4592-e25a-11e1bae1-93374682bd20")))

    assert (header.checkStep (req1, response, chain, 0) == -1)
    assert (header.checkStep (req2, response, chain, 1) == -1)
  }

  test ("In an XSD header step, if the header is available, but the content is not correct, the contentErrorPrioroty should be set (Multiple Headers)") {
    val header = new HeaderXSD("HEADER", "HEADER", "X-ID", uuidType, testSchemaSaxon, 10, Array[Step]())
    val req1 = request("GET", "/path/to/resource", "", "", false, Map("X-ID"->List("c8678844-3288-11e2-835c-c71ff3985a57", "28d42e00-e25a-11e1-9897-efbf2Za68353")))
    val req2 = request("GET", "/path/to/resource", "", "", false, Map("X-ID"->List("d389f680-3288-11e2-b58a-638ddd4222be", "2fbf4592-e25a-11e1bae1-93374682bd20")))

    header.checkStep (req1, response, chain, 0)
    header.checkStep (req2, response, chain, 1)
    assert (req1.contentErrorPriority == 10)
    assert (req2.contentErrorPriority == 10)
  }

  test ("In an XSD header step, if the header is available, but the content is not correct, the uri level should be -1 (Multiple Items in a single header)") {
    val header = new HeaderXSD("HEADER", "HEADER", "X-ID", uuidType, testSchemaSaxon, 10, Array[Step]())
    val req1 = request("GET", "/path/to/resource", "", "", false, Map("X-ID"->List("c8678844-3288-11e2-835c-c71ff3985a57", "28d42e00-e25a-11e1-9897-efbf2Za68353")))
    val req2 = request("GET", "/path/to/resource", "", "", false, Map("X-ID"->List("d389f680-3288-11e2-b58a-638ddd4222be", "2fbf4592-e25a-11e1bae1-93374682bd20")))

    assert (header.checkStep (req1, response, chain, 0) == -1)
    assert (header.checkStep (req2, response, chain, 1) == -1)
  }

  test ("In an XSD header step, if the header is available, but the content is not correct, the contentErrorPriority should be set (Multiple Items in a single header)") {
    val header = new HeaderXSD("HEADER", "HEADER", "X-ID", uuidType, testSchemaSaxon, 10, Array[Step]())
    val req1 = request("GET", "/path/to/resource", "", "", false, Map("X-ID"->List("c8678844-3288-11e2-835c-c71ff3985a57", "28d42e00-e25a-11e1-9897-efbf2Za68353")))
    val req2 = request("GET", "/path/to/resource", "", "", false, Map("X-ID"->List("d389f680-3288-11e2-b58a-638ddd4222be", "2fbf4592-e25a-11e1bae1-93374682bd20")))

    header.checkStep (req1, response, chain, 0)
    header.checkStep (req2, response, chain, 1)
    assert (req1.contentErrorPriority == 10)
    assert (req2.contentErrorPriority == 10)
  }

  test ("In an XSD header step, if the header is not available then the uri level should be -1") {
    val header = new HeaderXSD("HEADER", "HEADER", "X-ID", uuidType, testSchemaSaxon, 10, Array[Step]())
    val req1 = request("GET", "/path/to/resource", "", "", false, Map("ID"->List("28d42e00-e25a-11e1-9897-efbf2fa68353")))
    val req2 = request("GET", "/path/to/resource", "", "", false, Map("ID"->List("2fbf4592-e25a-11e1-bae1-93374682bd20")))

    assert (header.checkStep (req1, response, chain, 0) == -1)
    assert (header.checkStep (req2, response, chain, 1) == -1)
  }

  test ("In an XSD header step, if the header is not available then the contentErrorPriority should be set") {
    val header = new HeaderXSD("HEADER", "HEADER", "X-ID", uuidType, testSchemaSaxon, 10, Array[Step]())
    val req1 = request("GET", "/path/to/resource", "", "", false, Map("ID"->List("28d42e00-e25a-11e1-9897-efbf2fa68353")))
    val req2 = request("GET", "/path/to/resource", "", "", false, Map("ID"->List("2fbf4592-e25a-11e1-bae1-93374682bd20")))

    header.checkStep (req1, response, chain, 0)
    header.checkStep (req2, response, chain, 1)
    assert (req1.contentErrorPriority == 10)
    assert (req2.contentErrorPriority == 10)
  }

  test ("In an XSD header step, if the header is available, but the content is not correct, the request should conatin an Exception") {
    val header = new HeaderXSD("HEADER", "HEADER", "X-ID", uuidType, testSchemaSaxon, 10, Array[Step]())
    val req1 = request("GET", "/path/to/resource", "", "", false, Map("X-ID"->List("28d42e00-e25a-11e1-9897-efbf2Za68353")))
    val req2 = request("GET", "/path/to/resource", "", "", false, Map("X-ID"->List("2fbf4592-e25a-11e1bae1-93374682bd20")))

    header.checkStep (req1, response, chain, 0)
    assert (req1.contentError != null)
    assert (req1.contentError.isInstanceOf[Exception])
    header.checkStep (req2, response, chain, 1)
    assert (req2.contentError != null)
    assert (req2.contentError.isInstanceOf[Exception])
  }

  test ("In an XSD header step, if the header is available, but the content is not correct, the request should conatin an Exception (Multiple Headers)") {
    val header = new HeaderXSD("HEADER", "HEADER", "X-ID", uuidType, testSchemaSaxon, 10, Array[Step]())
    val req1 = request("GET", "/path/to/resource", "", "", false, Map("X-ID"->List("c8678844-3288-11e2-835c-c71ff3985a57", "28d42e00-e25a-11e1-9897-efbf2Za68353")))
    val req2 = request("GET", "/path/to/resource", "", "", false, Map("X-ID"->List("d389f680-3288-11e2-b58a-638ddd4222be", "2fbf4592-e25a-11e1bae1-93374682bd20")))

    header.checkStep (req1, response, chain, 0)
    assert (req1.contentError != null)
    assert (req1.contentError.isInstanceOf[Exception])
    header.checkStep (req2, response, chain, 1)
    assert (req2.contentError != null)
    assert (req2.contentError.isInstanceOf[Exception])
  }

  test ("In an XSD header step, if the header is available, but the content is not correct, the request should conatin an Exception (Multiple Items in a single header)") {
    val header = new HeaderXSD("HEADER", "HEADER", "X-ID", uuidType, testSchemaSaxon, 10, Array[Step]())
    val req1 = request("GET", "/path/to/resource", "", "", false, Map("X-ID"->List("ed2127f6-327b-11e2-abc3ebcd8ddbb97, 28d42e00-e25a-11e1-9897-efbf2Za68353")))
    val req2 = request("GET", "/path/to/resource", "", "", false, Map("X-ID"->List("f5bc95d0327b11e29f31e79a818b84c8, 2fbf4592-e25a-11e1bae1-93374682bd20")))

    header.checkStep (req1, response, chain, 0)
    assert (req1.contentError != null)
    assert (req1.contentError.isInstanceOf[Exception])
    header.checkStep (req2, response, chain, 1)
    assert (req2.contentError != null)
    assert (req2.contentError.isInstanceOf[Exception])
  }

  test ("In an XSD header step, if the header is not available then the request should contain an Exception") {
    val header = new HeaderXSD("HEADER", "HEADER", "X-ID", uuidType, testSchemaSaxon, 10, Array[Step]())
    val req1 = request("GET", "/path/to/resource", "", "", false, Map("ID"->List("28d42e00-e25a-11e1-9897-efbf2fa68353")))
    val req2 = request("GET", "/path/to/resource", "", "", false, Map("ID"->List("2fbf4592-e25a-11e1-bae1-93374682bd20")))

    header.checkStep (req1, response, chain, 0)
    assert (req1.contentError != null)
    assert (req1.contentError.isInstanceOf[Exception])
    header.checkStep (req2, response, chain, 1)
    assert (req2.contentError != null)
    assert (req2.contentError.isInstanceOf[Exception])
  }

  test ("In an XSD any header step, if the header is available then the uri level should stay the same.") {
    val header = new HeaderXSDAny("HEADER", "HEADER", "X-ID", uuidType, testSchemaSaxon, 10, Array[Step]())
    val req1 = request("GET", "/path/to/resource", "", "", false, Map("X-ID"->List("28d42e00-e25a-11e1-9897-efbf2fa68353")))
    val req2 = request("GET", "/path/to/resource", "", "", false, Map("X-ID"->List("2fbf4592-e25a-11e1-bae1-93374682bd20")))

    assert (header.checkStep (req1, response, chain, 0) == 0)
    assert (header.checkStep (req2, response, chain, 1) == 1)
  }

  test ("In an XSD any header step, if the header is available then the contentErrorPriority should be -1.") {
    val header = new HeaderXSDAny("HEADER", "HEADER", "X-ID", uuidType, testSchemaSaxon, 10, Array[Step]())
    val req1 = request("GET", "/path/to/resource", "", "", false, Map("X-ID"->List("28d42e00-e25a-11e1-9897-efbf2fa68353")))
    val req2 = request("GET", "/path/to/resource", "", "", false, Map("X-ID"->List("2fbf4592-e25a-11e1-bae1-93374682bd20")))

    header.checkStep (req1, response, chain, 0)
    header.checkStep (req2, response, chain, 1)
    assert (req1.contentErrorPriority == -1)
    assert (req2.contentErrorPriority == -1)
  }


  test ("In an XSD any header step, if the header is available then the uri level should stay the same. (Multiple Headers)") {
    val header = new HeaderXSDAny("HEADER", "HEADER", "X-ID", uuidType, testSchemaSaxon, 10, Array[Step]())
    val req1 = request("GET", "/path/to/resource", "", "", false, Map("X-ID"->List("28d42e00-e25a-11e1-9897-efbf2fa68353","9457bff8-56d5-11e2-8033-a39a52706e3e")))
    val req2 = request("GET", "/path/to/resource", "", "", false, Map("X-ID"->List("2fbf4592-e25a-11e1-bae1-93374682bd20","9ce63532-56d5-11e2-a5a1-b3291df67c83")))

    assert (header.checkStep (req1, response, chain, 0) == 0)
    assert (header.checkStep (req2, response, chain, 1) == 1)
  }

  test ("In an XSD any header step, if the header is available then the contentErrorPriority should be -1 (Multiple Headers)") {
    val header = new HeaderXSDAny("HEADER", "HEADER", "X-ID", uuidType, testSchemaSaxon, 10, Array[Step]())
    val req1 = request("GET", "/path/to/resource", "", "", false, Map("X-ID"->List("28d42e00-e25a-11e1-9897-efbf2fa68353","9457bff8-56d5-11e2-8033-a39a52706e3e")))
    val req2 = request("GET", "/path/to/resource", "", "", false, Map("X-ID"->List("2fbf4592-e25a-11e1-bae1-93374682bd20","9ce63532-56d5-11e2-a5a1-b3291df67c83")))

    header.checkStep (req1, response, chain, 0)
    header.checkStep (req2, response, chain, 1)
    assert (req1.contentErrorPriority == -1)
    assert (req2.contentErrorPriority == -1)
  }


  test ("In an XSD any header step, if the header is available then the uri level should stay the same. (Multiple Items single header)") {
    val header = new HeaderXSDAny("HEADER", "HEADER", "X-ID", uuidType, testSchemaSaxon, 10, Array[Step]())
    val req1 = request("GET", "/path/to/resource", "", "", false, Map("X-ID"->List("28d42e00-e25a-11e1-9897-efbf2fa68353, 9457bff8-56d5-11e2-8033-a39a52706e3e")))
    val req2 = request("GET", "/path/to/resource", "", "", false, Map("X-ID"->List("2fbf4592-e25a-11e1-bae1-93374682bd20, 9ce63532-56d5-11e2-a5a1-b3291df67c83")))

    assert (header.checkStep (req1, response, chain, 0) == 0)
    assert (header.checkStep (req2, response, chain, 1) == 1)
  }

  test ("In an XSD any header step, if the header is available then the contentErrorPriority should be -1. (Multiple Items single header)") {
    val header = new HeaderXSDAny("HEADER", "HEADER", "X-ID", uuidType, testSchemaSaxon, 10, Array[Step]())
    val req1 = request("GET", "/path/to/resource", "", "", false, Map("X-ID"->List("28d42e00-e25a-11e1-9897-efbf2fa68353, 9457bff8-56d5-11e2-8033-a39a52706e3e")))
    val req2 = request("GET", "/path/to/resource", "", "", false, Map("X-ID"->List("2fbf4592-e25a-11e1-bae1-93374682bd20, 9ce63532-56d5-11e2-a5a1-b3291df67c83")))

    header.checkStep (req1, response, chain, 0)
    header.checkStep (req2, response, chain, 1)
    assert (req1.contentErrorPriority == -1)
    assert (req2.contentErrorPriority == -1)
  }

  test ("In an XSD any header step, if the header is available then the uri level should stay the same, even if other headers don't match") {
    val header = new HeaderXSDAny("HEADER", "HEADER", "X-ID", uuidType, testSchemaSaxon, 10, Array[Step]())
    val req1 = request("GET", "/path/to/resource", "", "", false, Map("X-ID"->List("28d42e00-e25a-11e1-9897-efbf2fa68353","9457bff856d5-11e2-8033-a39a52706e3e")))
    val req2 = request("GET", "/path/to/resource", "", "", false, Map("X-ID"->List("2fbf4592-e25a-11e1-bae1-93374682bd20","9ce6353256d5-11e2-a5a1b3291df67c83")))

    assert (header.checkStep (req1, response, chain, 0) == 0)
    assert (header.checkStep (req2, response, chain, 1) == 1)
  }

  test ("In an XSD any header step, if the header is available then the contentErrorPriority should be -1, even if other headers don't match") {
    val header = new HeaderXSDAny("HEADER", "HEADER", "X-ID", uuidType, testSchemaSaxon, 10, Array[Step]())
    val req1 = request("GET", "/path/to/resource", "", "", false, Map("X-ID"->List("28d42e00-e25a-11e1-9897-efbf2fa68353","9457bff856d5-11e2-8033-a39a52706e3e")))
    val req2 = request("GET", "/path/to/resource", "", "", false, Map("X-ID"->List("2fbf4592-e25a-11e1-bae1-93374682bd20","9ce6353256d5-11e2-a5a1b3291df67c83")))

    header.checkStep (req1, response, chain, 0)
    header.checkStep (req2, response, chain, 1)
    assert (req1.contentErrorPriority == -1)
    assert (req2.contentErrorPriority == -1)
  }

  test ("In an XSD any header step, if the header is available, but the content is not correct, the uri level should be -1") {
    val header = new HeaderXSDAny("HEADER", "HEADER", "X-ID", uuidType, testSchemaSaxon, 10, Array[Step]())
    val req1 = request("GET", "/path/to/resource", "", "", false, Map("X-ID"->List("28d42e00-e25a-11e1-9897-efbf2Za68353")))
    val req2 = request("GET", "/path/to/resource", "", "", false, Map("X-ID"->List("2fbf4592-e25a-11e1bae1-93374682bd20")))

    assert (header.checkStep (req1, response, chain, 0) == -1)
    assert (header.checkStep (req2, response, chain, 1) == -1)
  }

  test ("In an XSD any header step, if the header is available, but the content is not correct, the contentErrorPriority should be set") {
    val header = new HeaderXSDAny("HEADER", "HEADER", "X-ID", uuidType, testSchemaSaxon, 10, Array[Step]())
    val req1 = request("GET", "/path/to/resource", "", "", false, Map("X-ID"->List("28d42e00-e25a-11e1-9897-efbf2Za68353")))
    val req2 = request("GET", "/path/to/resource", "", "", false, Map("X-ID"->List("2fbf4592-e25a-11e1bae1-93374682bd20")))

    header.checkStep (req1, response, chain, 0)
    header.checkStep (req2, response, chain, 1)
    assert (req1.contentErrorPriority == 10)
    assert (req2.contentErrorPriority == 10)
  }

  test ("In an XSD any header step, if the header is available, but the content is not correct, the uri level should be -1 (Multiple headers, all incorrect)") {
    val header = new HeaderXSDAny("HEADER", "HEADER", "X-ID", uuidType, testSchemaSaxon, 10, Array[Step]())
    val req1 = request("GET", "/path/to/resource", "", "", false, Map("X-ID"->List("28d42e00-e25a-11e1-9897-efbf2Za68353","foo")))
    val req2 = request("GET", "/path/to/resource", "", "", false, Map("X-ID"->List("2fbf4592-e25a-11e1bae1-93374682bd20","bar")))

    assert (header.checkStep (req1, response, chain, 0) == -1)
    assert (header.checkStep (req2, response, chain, 1) == -1)
  }

  test ("In an XSD any header step, if the header is available, but the content is not correct, the contentErrorPriority should be set (Multiple headers, all incorrect)") {
    val header = new HeaderXSDAny("HEADER", "HEADER", "X-ID", uuidType, testSchemaSaxon, 10, Array[Step]())
    val req1 = request("GET", "/path/to/resource", "", "", false, Map("X-ID"->List("28d42e00-e25a-11e1-9897-efbf2Za68353","foo")))
    val req2 = request("GET", "/path/to/resource", "", "", false, Map("X-ID"->List("2fbf4592-e25a-11e1bae1-93374682bd20","bar")))

    header.checkStep (req1, response, chain, 0)
    header.checkStep (req2, response, chain, 1)
    assert (req1.contentErrorPriority == 10)
    assert (req2.contentErrorPriority == 10)
  }


  test ("In an XSD any header step, if the header is available, but the content is not correct, the uri level should be -1 (Multiple items in single header, all incorrect)") {
    val header = new HeaderXSDAny("HEADER", "HEADER", "X-ID", uuidType, testSchemaSaxon, 10, Array[Step]())
    val req1 = request("GET", "/path/to/resource", "", "", false, Map("X-ID"->List("28d42e00-e25a-11e1-9897-efbf2Za68353, foo")))
    val req2 = request("GET", "/path/to/resource", "", "", false, Map("X-ID"->List("2fbf4592-e25a-11e1bae1-93374682bd20, bar")))

    assert (header.checkStep (req1, response, chain, 0) == -1)
    assert (header.checkStep (req2, response, chain, 1) == -1)
  }


  test ("In an XSD any header step, if the header is available, but the content is not correct, the contentErrorPriority should be set (Multiple items in single header, all incorrect)") {
    val header = new HeaderXSDAny("HEADER", "HEADER", "X-ID", uuidType, testSchemaSaxon, 10, Array[Step]())
    val req1 = request("GET", "/path/to/resource", "", "", false, Map("X-ID"->List("28d42e00-e25a-11e1-9897-efbf2Za68353, foo")))
    val req2 = request("GET", "/path/to/resource", "", "", false, Map("X-ID"->List("2fbf4592-e25a-11e1bae1-93374682bd20, bar")))

    header.checkStep (req1, response, chain, 0)
    header.checkStep (req2, response, chain, 1)
    assert (req1.contentErrorPriority == 10)
    assert (req2.contentErrorPriority == 10)
  }


  test ("In an XSD any header step, if the header is not available, the uri level should be -1") {
    val header = new HeaderXSDAny("HEADER", "HEADER", "X-ID", uuidType, testSchemaSaxon, 10, Array[Step]())
    val req1 = request("GET", "/path/to/resource", "", "", false, Map("ID"->List("509acc44-56d8-11e2-a542-cbb2f2d12c96")))
    val req2 = request("GET", "/path/to/resource", "", "", false, Map("ID"->List("5be28dda-56d8-11e2-9cf0-4728d4210e49")))

    assert (header.checkStep (req1, response, chain, 0) == -1)
    assert (header.checkStep (req2, response, chain, 1) == -1)
  }

  test ("In an XSD any header step, if the header is not available, the contentErrorPriority should be set") {
    val header = new HeaderXSDAny("HEADER", "HEADER", "X-ID", uuidType, testSchemaSaxon, 10, Array[Step]())
    val req1 = request("GET", "/path/to/resource", "", "", false, Map("ID"->List("509acc44-56d8-11e2-a542-cbb2f2d12c96")))
    val req2 = request("GET", "/path/to/resource", "", "", false, Map("ID"->List("5be28dda-56d8-11e2-9cf0-4728d4210e49")))

    header.checkStep (req1, response, chain, 0)
    header.checkStep (req2, response, chain, 1)
    assert (req1.contentErrorPriority == 10)
    assert (req2.contentErrorPriority == 10)
  }


  test ("In an XSD any header step, if the header is available, but the content is not correct, the request should conatin an Exception") {
    val header = new HeaderXSDAny("HEADER", "HEADER", "X-ID", uuidType, testSchemaSaxon, 10, Array[Step]())
    val req1 = request("GET", "/path/to/resource", "", "", false, Map("X-ID"->List("28d42e00-e25a-11e1-9897-efbf2Za68353")))
    val req2 = request("GET", "/path/to/resource", "", "", false, Map("X-ID"->List("2fbf4592-e25a-11e1bae1-93374682bd20")))

    header.checkStep (req1, response, chain, 0)
    assert (req1.contentError != null)
    assert (req1.contentError.isInstanceOf[Exception])
    header.checkStep (req2, response, chain, 1)
    assert (req2.contentError != null)
    assert (req2.contentError.isInstanceOf[Exception])
  }

  test ("In an XSD any header step, if the header is available, but the content is not correct, the request should conatin an Exception (multiple headers all incorrect)") {
    val header = new HeaderXSDAny("HEADER", "HEADER", "X-ID", uuidType, testSchemaSaxon, 10, Array[Step]())
    val req1 = request("GET", "/path/to/resource", "", "", false, Map("X-ID"->List("28d42e00-e25a-11e1-9897-efbf2Za68353", "foo")))
    val req2 = request("GET", "/path/to/resource", "", "", false, Map("X-ID"->List("2fbf4592-e25a-11e1bae1-93374682bd20", "bar")))

    header.checkStep (req1, response, chain, 0)
    assert (req1.contentError != null)
    assert (req1.contentError.isInstanceOf[Exception])
    header.checkStep (req2, response, chain, 1)
    assert (req2.contentError != null)
    assert (req2.contentError.isInstanceOf[Exception])
  }

  test ("In an XSD any header step, if the header is available, but the content is not correct, the request should conatin an Exception (multiple items, all incorrect)") {
    val header = new HeaderXSDAny("HEADER", "HEADER", "X-ID", uuidType, testSchemaSaxon, 10, Array[Step]())
    val req1 = request("GET", "/path/to/resource", "", "", false, Map("X-ID"->List("28d42e00-e25a-11e1-9897-efbf2Za68353, foo")))
    val req2 = request("GET", "/path/to/resource", "", "", false, Map("X-ID"->List("2fbf4592-e25a-11e1bae1-93374682bd20, bar")))

    header.checkStep (req1, response, chain, 0)
    assert (req1.contentError != null)
    assert (req1.contentError.isInstanceOf[Exception])
    header.checkStep (req2, response, chain, 1)
    assert (req2.contentError != null)
    assert (req2.contentError.isInstanceOf[Exception])
  }

  test ("In an XSD any header step, if the header not available, the request should conatin an Exception") {
    val header = new HeaderXSDAny("HEADER", "HEADER", "X-ID", uuidType, testSchemaSaxon, 10, Array[Step]())
    val req1 = request("GET", "/path/to/resource", "", "", false, Map("ID"->List("28d42e00-e25a-11e1-9897-efbf2Za68353")))
    val req2 = request("GET", "/path/to/resource", "", "", false, Map("ID"->List("2fbf4592-e25a-11e1bae1-93374682bd20")))

    header.checkStep (req1, response, chain, 0)
    assert (req1.contentError != null)
    assert (req1.contentError.isInstanceOf[Exception])
    header.checkStep (req2, response, chain, 1)
    assert (req2.contentError != null)
    assert (req2.contentError.isInstanceOf[Exception])
  }

}
