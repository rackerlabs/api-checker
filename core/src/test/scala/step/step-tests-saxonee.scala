package com.rackspace.com.papi.components.checker.step

import javax.xml.namespace.QName

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

import org.xml.sax.SAXParseException

@RunWith(classOf[JUnitRunner])
class StepSuiteSaxonEE extends BaseStepSuiteSaxonEE {

  test("URLFailXSD should return None if URI level has been exceeded : StepType, uuid, evenIntType") {
    val ufx = new URLFailXSD("ufx", "ufx", Array[QName](stepType, uuidType, evenIntType), testSchemaSaxon)
    val res = ufx.check (request("GET", "/START/b"), response,chain, 2)
    assert (res == None)
    val res2 = ufx.check (request("GET", "/ACCEPT/b"), response,chain, 3)
    assert (res2 == None)
  }

  test("URLFailXSD should return URL fail result if URI level has not been exceeded and the uri type does not match : StepType, uuid, evenIntType") {
    val ufx = new URLFailXSD ("ufmx", "ufmx", Array[QName](stepType, uuidType, evenIntType), testSchemaSaxon)
    val res = ufx.check (request("GET", "/a/b"), response,chain, 0)
    assert (res.isDefined)
    assert (res.get.isInstanceOf[URLFailResult])
    val res2 = ufx.check (request("GET", "/a/b"), response,chain, 1)
    assert (res2.isDefined)
    assert (res2.get.isInstanceOf[URLFailResult])
  }


  test("URLFailXSD should return URL None if URI level has not been exceeded and the uri type matches : StepType, uuid, evenIntType") {
    val ufx = new URLFailXSD ("ufmx", "ufmx", Array[QName](stepType, uuidType, evenIntType), testSchemaSaxon)
    val res = ufx.check (request("GET", "/START/b"), response,chain, 0)
    assert (res == None)
    val res2 = ufx.check (request("GET", "/eb507026-6463-11e1-b7aa-8b7b918a1623/b"), response,chain, 0)
    assert (res2 == None)
    val res3 = ufx.check (request("GET", "/90/b"), response,chain, 0)
    assert (res3 == None)
  }

  test("URLFailXSDMatch should return None if URI level has been exceeded : StepType, uuid, evenIntType") {
    val ufx = new URLFailXSDMatch("ufx", "ufx", "c".r, Array[QName](stepType, uuidType, evenIntType), testSchemaSaxon)
    val res = ufx.check (request("GET", "/START/b"), response,chain, 2)
    assert (res == None)
    val res2 = ufx.check (request("GET", "/ACCEPT/b"), response,chain, 3)
    assert (res2 == None)
    val res3 = ufx.check (request("GET", "/c/b"), response,chain, 4)
    assert (res3 == None)
  }

  test("URLFailXSDMatch should return URL fail result if URI level has not been exceeded and the uri type does not match : StepType, uuid, evenIntType") {
    val ufx = new URLFailXSDMatch ("ufmx", "ufmx", "c".r, Array[QName](stepType, uuidType, evenIntType), testSchemaSaxon)
    val res = ufx.check (request("GET", "/a/b"), response,chain, 0)
    assert (res.isDefined)
    assert (res.get.isInstanceOf[URLFailResult])
    val res2 = ufx.check (request("GET", "/a/b"), response,chain, 1)
    assert (res2.isDefined)
    assert (res2.get.isInstanceOf[URLFailResult])
  }

  test("URLFailXSDMatch should return URL None if URI level has not been exceeded and the uri type matches : StepType, uuid, evenIntType") {
    val ufx = new URLFailXSDMatch ("ufmx", "ufmx", "c".r, Array[QName](stepType, uuidType, evenIntType), testSchemaSaxon)
    val res = ufx.check (request("GET", "/START/b"), response,chain, 0)
    assert (res == None)
    val res2 = ufx.check (request("GET", "/eb507026-6463-11e1-b7aa-8b7b918a1623/b"), response,chain, 0)
    assert (res2 == None)
    val res3 = ufx.check (request("GET", "/90/b"), response,chain, 0)
    assert (res3 == None)
    val res4 = ufx.check (request("GET", "/c/b"), response,chain, 0)
    assert (res4 == None)
  }

  test("URIXSD mismatch message should be the same as the QName") {
    val urixsd = new URIXSD("uxd", "uxd", stepType, testSchemaSaxon, Array[Step]())
    assert (urixsd.mismatchMessage == stepType.toString)
  }

  test("In a URIXSD step, if there is a URI match, the step should proceed to the next step : StepType") {
    val urixsd = new URIXSD("uxd", "uxd", stepType, testSchemaSaxon, Array[Step]())
    assert (urixsd.check (request("GET", "/START/b"), response,chain, 0) == None)
    assert (urixsd.check (request("GET", "/URL_FAIL/b"), response,chain, 0) == None)
    assert (urixsd.check (request("GET", "/METHOD_FAIL/b"), response,chain, 0) == None)
    assert (urixsd.check (request("GET", "/ACCEPT/b"), response,chain, 0) == None)
    assert (urixsd.check (request("GET", "/URL/b"), response,chain, 0) == None)
    assert (urixsd.check (request("GET", "/METHOD/b"), response,chain, 0) == None)
    assert (urixsd.check (request("GET", "/URLXSD/b"), response,chain, 0) == None)
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
    assert (urixsd.check (request("GET", "/55b76e92-6450-11e1-9012-37afadb5ff61/b"), response,chain, 0) == None)
    assert (urixsd.check (request("GET", "/56d7a1fc-6450-11e1-b360-8fe15f519bf2/b"), response,chain, 0) == None)
    assert (urixsd.check (request("GET", "/5731bb7e-6450-11e1-9b88-6ff2691237cd/b"), response,chain, 0) == None)
    assert (urixsd.check (request("GET", "/578952c6-6450-11e1-892b-8bae86031338/b"), response,chain, 0) == None)
    assert (urixsd.check (request("GET", "/57e75268-6450-11e1-892e-abc2baf50960/b"), response,chain, 0) == None)
    assert (urixsd.check (request("GET", "/58415556-6450-11e1-96f9-17b1db29daf7/b"), response,chain, 0) == None)
    assert (urixsd.check (request("GET", "/58a0ff60-6450-11e1-95bd-77590a8a0a53/b"), response,chain, 0) == None)
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
    assert (urixsd.check (request("GET", "/54/b"), response,chain, 0) == None)
    assert (urixsd.check (request("GET", "/0/b"), response,chain, 0) == None)
    assert (urixsd.check (request("GET", "/32/b"), response,chain, 0) == None)
    assert (urixsd.check (request("GET", "/2/b"), response,chain, 0) == None)
    assert (urixsd.check (request("GET", "/12/b"), response,chain, 0) == None)
    assert (urixsd.check (request("GET", "/100/b"), response,chain, 0) == None)
    assert (urixsd.check (request("GET", "/84/b"), response,chain, 0) == None)
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
    val xsd = new XSD("XSD", "XSD", testSchemaSaxon, Array[Step]())
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

  test ("In an XSD test, if the content contains invalid XML, the uriLevel should be -1") {
    val xsd = new XSD("XSD", "XSD", testSchemaSaxon, Array[Step]())
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

  test ("In an XSD test, if the content contains invalid XML, the request should contain a SAXException") {
    val xsd = new XSD("XSD", "XSD", testSchemaSaxon, Array[Step]())
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
    val xsd = new XSD("XSD", "XSD", testSchemaSaxon, Array[Step]())
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

  test ("In an XSD test, if the content contains invalid XML, the request should contain a SAXException (XSD 1.1 assert)") {
    val xsd = new XSD("XSD", "XSD", testSchemaSaxon, Array[Step]())
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
}
