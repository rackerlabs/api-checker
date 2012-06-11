package com.rackspace.com.papi.components.checker.step

import javax.xml.namespace.QName

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

import org.w3c.dom.Document
import org.xml.sax.SAXParseException

import com.rackspace.com.papi.components.checker.util.XMLParserPool

@RunWith(classOf[JUnitRunner])
class StepSuite extends BaseStepSuite {

  test("Regardless of input, Accept step should always return AcceptResult") {
    val accept = new Accept("a","a")
    val res2 = accept.check(request("GET", "/a/b"), response,chain, 2)
    assert(res2.isDefined)
    assert(res2.get.isInstanceOf[AcceptResult])
    val res3 = accept.check(request("XGET", "/a/b"), response,chain, 0)
    assert(res3.isDefined)
    assert(res3.get.isInstanceOf[AcceptResult])
    val res = accept.check (null, null, chain, -1)
    assert(res.isDefined)
    assert(res.get.isInstanceOf[AcceptResult])
  }

  test("Start should not change URI level") {
    val start = new Start("s", "s", Array[Step]())
    assert(start.checkStep(null, null, null, -1) == -1)
    assert(start.checkStep(request("GET", "/a/b"), response,chain, 2) == 2)
    assert(start.checkStep(request("",""), response,chain, 1000) == 1000)
  }

  test("MethodFail should return method fail result if the URI level has been exceeded") {
    val mf  = new MethodFail("mf", "mf")
    val res = mf.check (request("GET", "/a/b"), response,chain, 2)
    assert (res.isDefined)
    assert (res.get.isInstanceOf[MethodFailResult])
    val res2 = mf.check (request("GET", "/a/b"), response,chain, 3)
    assert (res2.isDefined)
    assert (res2.get.isInstanceOf[MethodFailResult])
  }

  test("MethodFail should return None when URI level is not exceeded") {
    val mf  = new MethodFail("mf", "mf")
    val res = mf.check (request("GET", "/a/b"), response,chain, 0)
    assert (res == None)
    val res2 = mf.check (request("GET", "/a/b"), response,chain, 1)
    assert (res2 == None)
  }

  test("MethodFailMatch should return method fail result if the URI level has been exceeded and the method regex does not match") {
    val mf  = new MethodFailMatch("mf", "mf", "POST".r)
    val res = mf.check (request("GET", "/a/b"), response,chain, 2)
    assert (res.isDefined)
    assert (res.get.isInstanceOf[MethodFailResult])
    val res2 = mf.check (request("GET", "/a/b"), response,chain, 3)
    assert (res2.isDefined)
    assert (res2.get.isInstanceOf[MethodFailResult])
  }

  test("MethodFailMatch should return None if the URI level has been exceeded and the method regex matches") {
    val mf  = new MethodFailMatch("mf", "mf", "GET".r)
    val res = mf.check (request("GET", "/a/b"), response,chain, 2)
    assert (res == None)
    val res2 = mf.check (request("GET", "/a/b"), response,chain, 3)
    assert (res2 == None)
  }

  test("MethodFailMatch should return None when URI level is not exceeded") {
    val mf  = new MethodFailMatch("mf", "mf", "GET".r)
    val res = mf.check (request("GET", "/a/b"), response,chain, 0)
    assert (res == None)
    val res2 = mf.check (request("GET", "/a/b"), response,chain, 1)
    assert (res2 == None)
  }

  test("URLFail should return URL fail result if URI level has not been exceeded") {
    val uf = new URLFail("uf", "uf")
    val res = uf.check (request("GET", "/a/b"), response,chain, 0)
    assert (res.isDefined)
    assert (res.get.isInstanceOf[URLFailResult])
    val res2 = uf.check (request("GET", "/a/b"), response,chain, 1)
    assert (res2.isDefined)
    assert (res2.get.isInstanceOf[URLFailResult])
  }


  test("URLFailXSD should return None if URI level has been exceeded : StepType, uuid, evenIntType") {
    val ufx = new URLFailXSD("ufx", "ufx", Array[QName](stepType, uuidType, evenIntType), testSchema)
    val res = ufx.check (request("GET", "/START/b"), response,chain, 2)
    assert (res == None)
    val res2 = ufx.check (request("GET", "/ACCEPT/b"), response,chain, 3)
    assert (res2 == None)
  }

  test("URLFailXSD should return URL fail result if URI level has not been exceeded and the uri type does not match : StepType, uuid, evenIntType") {
    val ufx = new URLFailXSD ("ufmx", "ufmx", Array[QName](stepType, uuidType, evenIntType), testSchema)
    val res = ufx.check (request("GET", "/a/b"), response,chain, 0)
    assert (res.isDefined)
    assert (res.get.isInstanceOf[URLFailResult])
    val res2 = ufx.check (request("GET", "/a/b"), response,chain, 1)
    assert (res2.isDefined)
    assert (res2.get.isInstanceOf[URLFailResult])
  }


  test("URLFailXSD should return URL None if URI level has not been exceeded and the uri type matches : StepType, uuid, evenIntType") {
    val ufx = new URLFailXSD ("ufmx", "ufmx", Array[QName](stepType, uuidType, evenIntType), testSchema)
    val res = ufx.check (request("GET", "/START/b"), response,chain, 0)
    assert (res == None)
    val res2 = ufx.check (request("GET", "/eb507026-6463-11e1-b7aa-8b7b918a1623/b"), response,chain, 0)
    assert (res2 == None)
    val res3 = ufx.check (request("GET", "/90/b"), response,chain, 0)
    assert (res3 == None)
  }

  test("URLFailXSDMatch should return None if URI level has been exceeded : StepType, uuid, evenIntType") {
    val ufx = new URLFailXSDMatch("ufx", "ufx", "c".r, Array[QName](stepType, uuidType, evenIntType), testSchema)
    val res = ufx.check (request("GET", "/START/b"), response,chain, 2)
    assert (res == None)
    val res2 = ufx.check (request("GET", "/ACCEPT/b"), response,chain, 3)
    assert (res2 == None)
    val res3 = ufx.check (request("GET", "/c/b"), response,chain, 4)
    assert (res3 == None)
  }

  test("URLFailXSDMatch should return URL fail result if URI level has not been exceeded and the uri type does not match : StepType, uuid, evenIntType") {
    val ufx = new URLFailXSDMatch ("ufmx", "ufmx", "c".r, Array[QName](stepType, uuidType, evenIntType), testSchema)
    val res = ufx.check (request("GET", "/a/b"), response,chain, 0)
    assert (res.isDefined)
    assert (res.get.isInstanceOf[URLFailResult])
    val res2 = ufx.check (request("GET", "/a/b"), response,chain, 1)
    assert (res2.isDefined)
    assert (res2.get.isInstanceOf[URLFailResult])
  }

  test("URLFailXSDMatch should return URL None if URI level has not been exceeded and the uri type matches : StepType, uuid, evenIntType") {
    val ufx = new URLFailXSDMatch ("ufmx", "ufmx", "c".r, Array[QName](stepType, uuidType, evenIntType), testSchema)
    val res = ufx.check (request("GET", "/START/b"), response,chain, 0)
    assert (res == None)
    val res2 = ufx.check (request("GET", "/eb507026-6463-11e1-b7aa-8b7b918a1623/b"), response,chain, 0)
    assert (res2 == None)
    val res3 = ufx.check (request("GET", "/90/b"), response,chain, 0)
    assert (res3 == None)
    val res4 = ufx.check (request("GET", "/c/b"), response,chain, 0)
    assert (res4 == None)
  }

  test("URLFail should return None if URI level has been exceeded") {
    val uf = new URLFail("uf", "uf")
    val res = uf.check (request("GET", "/a/b"), response,chain, 2)
    assert (res == None)
    val res2 = uf.check (request("GET", "/a/b"), response,chain, 3)
    assert (res2 == None)
  }

  test("URLFailMatch should return URL fail result if URI level has not been exceeded and the uri regex does not match") {
    val uf = new URLFailMatch ("ufm", "ufm", "c".r)
    val res = uf.check (request("GET", "/a/b"), response,chain, 0)
    assert (res.isDefined)
    assert (res.get.isInstanceOf[URLFailResult])
    val res2 = uf.check (request("GET", "/a/b"), response,chain, 1)
    assert (res2.isDefined)
    assert (res2.get.isInstanceOf[URLFailResult])
  }

  test("URLFailMatch should return None if URI level has not been exceeded and the uri regex matches") {
    val uf = new URLFailMatch ("ufm", "ufm", "a".r)
    val res = uf.check (request("GET", "/a/b"), response,chain, 0)
    assert (res == None)
  }

  test("URLFailMatch should return None if URI level has been exceeded") {
    val uf = new URLFailMatch("uf", "uf", "a".r)
    val res = uf.check (request("GET", "/a/b"), response,chain, 2)
    assert (res == None)
    val res2 = uf.check (request("GET", "/a/b"), response,chain, 3)
    assert (res2 == None)
  }

  test("URIXSD mismatch message should be the same as the QName") {
    val urixsd = new URIXSD("uxd", "uxd", stepType, testSchema, Array[Step]())
    assert (urixsd.mismatchMessage == stepType.toString)
  }

  test("In a URIXSD step, if there is a URI match, the step should proceed to the next step : StepType") {
    val urixsd = new URIXSD("uxd", "uxd", stepType, testSchema, Array[Step]())
    assert (urixsd.check (request("GET", "/START/b"), response,chain, 0) == None)
    assert (urixsd.check (request("GET", "/URL_FAIL/b"), response,chain, 0) == None)
    assert (urixsd.check (request("GET", "/METHOD_FAIL/b"), response,chain, 0) == None)
    assert (urixsd.check (request("GET", "/ACCEPT/b"), response,chain, 0) == None)
    assert (urixsd.check (request("GET", "/URL/b"), response,chain, 0) == None)
    assert (urixsd.check (request("GET", "/METHOD/b"), response,chain, 0) == None)
    assert (urixsd.check (request("GET", "/URLXSD/b"), response,chain, 0) == None)
  }

  test("In a URIXSD step, if there is a mismatch, a MismatchResult should be returned: StepType") {
    val urixsd = new URIXSD("uxd", "uxd", stepType, testSchema, Array[Step]())
    assertMismatchResult (urixsd.check (request("GET", "/ATART/b"), response,chain, 0))
    assertMismatchResult (urixsd.check (request("GET", "/URL_FAI2/b"), response,chain, 0))
    assertMismatchResult (urixsd.check (request("GET", "/METHO4_FAIL/b"), response,chain, 0))
    assertMismatchResult (urixsd.check (request("GET", "/ACCCPT/b"), response,chain, 0))
    assertMismatchResult (urixsd.check (request("GET", "/URLL/b"), response,chain, 0))
    assertMismatchResult (urixsd.check (request("GET", "/METH0D/b"), response,chain, 0))
    assertMismatchResult (urixsd.check (request("GET", "/UR7XSD/b"), response,chain, 0))
  }

  test("In a URIXSD step, if there is a URI match, but the level has been exceeded a MismatchResult should be returned: StepType") {
    val urixsd = new URIXSD("uxd", "uxd", stepType, testSchema, Array[Step]())
    assertMismatchResult (urixsd.check (request("GET", "/START/b"), response,chain, 2))
    assertMismatchResult (urixsd.check (request("GET", "/URL_FAIL/b"), response,chain, 2))
    assertMismatchResult (urixsd.check (request("GET", "/METHOD_FAIL/b"), response,chain, 2))
    assertMismatchResult (urixsd.check (request("GET", "/ACCEPT/b"), response,chain, 2))
    assertMismatchResult (urixsd.check (request("GET", "/URL/b"), response,chain, 2))
    assertMismatchResult (urixsd.check (request("GET", "/METHOD/b"), response,chain, 2))
    assertMismatchResult (urixsd.check (request("GET", "/URLXSD/b"), response,chain, 2))
  }

  test("In a URIXSD step, if there is a URI match, the step should proceed to the next step : UUID") {
    val urixsd = new URIXSD("uxd", "uxd", uuidType, testSchema, Array[Step]())
    assert (urixsd.check (request("GET", "/55b76e92-6450-11e1-9012-37afadb5ff61/b"), response,chain, 0) == None)
    assert (urixsd.check (request("GET", "/56d7a1fc-6450-11e1-b360-8fe15f519bf2/b"), response,chain, 0) == None)
    assert (urixsd.check (request("GET", "/5731bb7e-6450-11e1-9b88-6ff2691237cd/b"), response,chain, 0) == None)
    assert (urixsd.check (request("GET", "/578952c6-6450-11e1-892b-8bae86031338/b"), response,chain, 0) == None)
    assert (urixsd.check (request("GET", "/57e75268-6450-11e1-892e-abc2baf50960/b"), response,chain, 0) == None)
    assert (urixsd.check (request("GET", "/58415556-6450-11e1-96f9-17b1db29daf7/b"), response,chain, 0) == None)
    assert (urixsd.check (request("GET", "/58a0ff60-6450-11e1-95bd-77590a8a0a53/b"), response,chain, 0) == None)
  }

  test("In a URIXSD step, if there is a mismatch, a MismatchResult should be returned: UUID") {
    val urixsd = new URIXSD("uxd", "uxd", uuidType, testSchema, Array[Step]())
    assertMismatchResult (urixsd.check (request("GET", "/55b76e92-6450-11e1-9012-37afadbgff61/b"), response,chain, 0))
    assertMismatchResult (urixsd.check (request("GET", "/55/b"), response,chain, 0))
    assertMismatchResult (urixsd.check (request("GET", "/aoeee..x/b"), response,chain, 0))
    assertMismatchResult (urixsd.check (request("GET", "/09cgff.dehbj/b"), response,chain, 0))
    assertMismatchResult (urixsd.check (request("GET", "/55b76e92-6450-11e1-901237afadb5ff61/b"), response,chain, 0))
    assertMismatchResult (urixsd.check (request("GET", "/58415556-6450-11e1-96f9:17b1db29daf7/b"), response,chain, 0))
    assertMismatchResult (urixsd.check (request("GET", "/+58a0ff60-6450-11e1-95bd-77590a8a0a53/b"), response,chain, 0))
  }

  test("In a URIXSD step, if there is a URI match, but the level has been exceeded a MismatchResult should be returned: UUID") {
    val urixsd = new URIXSD("uxd", "uxd", uuidType, testSchema, Array[Step]())
    assertMismatchResult (urixsd.check (request("GET", "/55b76e92-6450-11e1-9012-37afadb5ff61/b"), response,chain, 2))
    assertMismatchResult (urixsd.check (request("GET", "/56d7a1fc-6450-11e1-b360-8fe15f519bf2/b"), response,chain, 2))
    assertMismatchResult (urixsd.check (request("GET", "/5731bb7e-6450-11e1-9b88-6ff2691237cd/b"), response,chain, 2))
    assertMismatchResult (urixsd.check (request("GET", "/578952c6-6450-11e1-892b-8bae86031338/b"), response,chain, 2))
    assertMismatchResult (urixsd.check (request("GET", "/57e75268-6450-11e1-892e-abc2baf50960/b"), response,chain, 2))
    assertMismatchResult (urixsd.check (request("GET", "/58415556-6450-11e1-96f9-17b1db29daf7/b"), response,chain, 2))
    assertMismatchResult (urixsd.check (request("GET", "/58a0ff60-6450-11e1-95bd-77590a8a0a53/b"), response,chain, 2))
  }

  test("In a URIXSD step, if there is a URI match, the step should proceed to the next step : EvenInt100") {
    val urixsd = new URIXSD("uxd", "uxd", evenIntType, testSchema, Array[Step]())
    assert (urixsd.check (request("GET", "/54/b"), response,chain, 0) == None)
    assert (urixsd.check (request("GET", "/0/b"), response,chain, 0) == None)
    assert (urixsd.check (request("GET", "/32/b"), response,chain, 0) == None)
    assert (urixsd.check (request("GET", "/2/b"), response,chain, 0) == None)
    assert (urixsd.check (request("GET", "/12/b"), response,chain, 0) == None)
    assert (urixsd.check (request("GET", "/100/b"), response,chain, 0) == None)
    assert (urixsd.check (request("GET", "/84/b"), response,chain, 0) == None)
  }

  //
  //  Pending bug fix on xerces-j
  //
  ignore("In a URIXSD step, if there is a mismatch, a MismatchResult should be returned: EvenInt100, assert") {
    val urixsd = new URIXSD("uxd", "uxd", evenIntType, testSchema, Array[Step]())
    assertMismatchResult (urixsd.check (request("GET", "/55/b"), response,chain, 0))
    assertMismatchResult (urixsd.check (request("GET", "/1/b"), response,chain, 0))
    assertMismatchResult (urixsd.check (request("GET", "/33/b"), response,chain, 0))
    assertMismatchResult (urixsd.check (request("GET", "/3/b"), response,chain, 0))
    assertMismatchResult (urixsd.check (request("GET", "/15/b"), response,chain, 0))
    assertMismatchResult (urixsd.check (request("GET", "/101/b"), response,chain, 0))
    assertMismatchResult (urixsd.check (request("GET", "/85/b"), response,chain, 0))
  }

  test("In a URIXSD step, if there is a mismatch, a MismatchResult should be returned: EvenInt100") {
    val urixsd = new URIXSD("uxd", "uxd", evenIntType, testSchema, Array[Step]())
    assertMismatchResult (urixsd.check (request("GET", "/101/b"), response,chain, 0))
    assertMismatchResult (urixsd.check (request("GET", "/555/b"), response,chain, 0))
    assertMismatchResult (urixsd.check (request("GET", "/hello/b"), response,chain, 0))
    assertMismatchResult (urixsd.check (request("GET", "/09cgff.dehbj/b"), response,chain, 0))
    assertMismatchResult (urixsd.check (request("GET", "/-99/b"), response,chain, 0))
    assertMismatchResult (urixsd.check (request("GET", "/3tecr/b"), response,chain, 0))
    assertMismatchResult (urixsd.check (request("GET", "/58a0ff60-6450-11e1-95bd-77590a8a0a53/b"), response,chain, 0))
  }

  test("In a URIXSD step, if there is a URI match, but the level has been exceeded a MismatchResult should be returned: EvenInt100") {
    val urixsd = new URIXSD("uxd", "uxd", evenIntType, testSchema, Array[Step]())
    assertMismatchResult (urixsd.check (request("GET", "/54/b"), response,chain, 2))
    assertMismatchResult (urixsd.check (request("GET", "/0/b"), response,chain, 2))
    assertMismatchResult (urixsd.check (request("GET", "/32/b"), response,chain, 2))
    assertMismatchResult (urixsd.check (request("GET", "/2/b"), response,chain, 2))
    assertMismatchResult (urixsd.check (request("GET", "/12/b"), response,chain, 2))
    assertMismatchResult (urixsd.check (request("GET", "/100/b"), response,chain, 2))
    assertMismatchResult (urixsd.check (request("GET", "/84/b"), response,chain, 2))
  }

  test("URI mismatch message should be the same of the uri regex") {
    val uri = new URI("u", "u", "u".r, Array[Step]())
    assert (uri.mismatchMessage == "u".r.toString)
  }

  test("In a URI step, if there is a URI match, the uriLevel should increase by 1") {
    val uri = new URI("a", "a", "a".r, Array[Step]())
    assert (uri.checkStep (request("GET", "/a/b"), response,chain, 0) == 1)

    val uri2 = new URI("ab", "a or b", "[a-b]".r, Array[Step]())
    assert (uri2.checkStep (request("GET", "/a/b"), response,chain, 0) == 1)
    assert (uri2.checkStep (request("GET", "/a/b"), response,chain, 1) == 2)
  }

  test("In a URI step, if there is a URI mismatch, the uriLevel should be -1") {
    val uri = new URI("a", "a", "a".r, Array[Step]())
    assert (uri.checkStep (request("GET", "/c/b"), response,chain, 0) == -1)

    val uri2 = new URI("ab", "a or b", "[a-b]".r, Array[Step]())
    assert (uri2.checkStep (request("GET", "/c/d"), response,chain, 0) == -1)
    assert (uri2.checkStep (request("GET", "/c/d"), response,chain, 1) == -1)
  }

  test("In a URI step, if there is a URI match, but the URI level has been exceeded the new URI level should be -1") {
    val uri = new URI("a", "a", "a".r, Array[Step]())
    assert (uri.checkStep (request("GET", "/a/b"), response,chain, 2) == -1)

    val uri2 = new URI("ab", "a or b", "[a-b]".r, Array[Step]())
    assert (uri2.checkStep (request("GET", "/a/b"), response,chain, 3) == -1)
    assert (uri2.checkStep (request("GET", "/a/b"), response,chain, 4) == -1)
  }

  test("Method mismatch message should be the same of the uri regex") {
    val method = new Method("GET", "GET", "GET".r, Array[Step]())
    assert (method.mismatchMessage == "GET".r.toString)
  }

  test("In a Method step, if the uriLevel has not been exceeded, the returned URI level should be -1") {
    val method = new Method("GET", "GET", "GET".r, Array[Step]())
    assert (method.checkStep (request("GET", "/a/b"), response,chain, 0) == -1)
    assert (method.checkStep (request("GET", "/a/b"), response,chain, 1) == -1)
  }

  test("In a Method step, if the uriLevel has been exceeded, and the method matches, the URI level should stay the same") {
    val method = new Method("GET", "GET", "GET".r, Array[Step]())
    assert (method.checkStep (request("GET", "/a/b"), response,chain, 2) == 2)
    assert (method.checkStep (request("GET", "/a/b"), response,chain, 3) == 3)
    val method2 = new Method("GETPOST", "GET or POST", "GET|POST".r, Array[Step]())
    assert (method2.checkStep (request("GET", "/a/b"), response,chain, 2) == 2)
    assert (method2.checkStep (request("POST", "/a/b"), response,chain, 3) == 3)
  }

  test("In a Method step, if the uriLevel has been exceeded, and the method does not match, the URI level should be set to -1") {
    val method = new Method("GET", "GET", "GET".r, Array[Step]())
    assert (method.checkStep (request("POST", "/a/b"), response,chain, 2) == -1)
    assert (method.checkStep (request("GTB", "/a/b"), response,chain, 3) == -1)
    val method2 = new Method("GETPOST", "GET or POST", "GET|POST".r, Array[Step]())
    assert (method2.checkStep (request("PUT", "/a/b"), response,chain, 2) == -1)
    assert (method2.checkStep (request("DELETE", "/a/b"), response,chain, 3) == -1)
  }

  test("A ReqTestFail step should fail if the content type does not match with a BadMediaTypeResult") {
    val rtf = new ReqTypeFail ("XML", "XML", "application/xml|application/json".r)
    assertBadMediaType (rtf.check (request("PUT", "/a/b", "*.*"), response, chain, 1))
    assertBadMediaType (rtf.check (request("POST", "/index.html", "application/XMLA"), response, chain, 0))
  }

  test("A ReqTestFail step should return None if the content type matchs") {
    val rtf = new ReqTypeFail ("XML", "XML", "application/xml|application/json".r)
    assert (rtf.check (request("PUT", "/a/b", "application/json"), response, chain, 1) == None)
    assert (rtf.check (request("POST", "/index.html", "application/xml"), response, chain, 0) == None)
  }

  test("ReqType mismatch message should be the same of the type regex") {
    val rt = new ReqType ("XML", "XML", "application/xml|application/json".r, Array[Step]())
    assert (rt.mismatchMessage == "application/xml|application/json".r.toString)
  }

  test("In a ReqType step, if the content type does not match, the returned URI level should be -1") {
    val rt = new ReqType ("XML", "XML", "application/xml|application/json".r, Array[Step]())
    assert (rt.checkStep (request("PUT", "/a/b","*.*"), response,chain, 0) == -1)
    assert (rt.checkStep (request("POST", "/a/b","application/junk"), response,chain, 1) == -1)
    val rt2 = new ReqType ("XML", "XML", "text/html".r, Array[Step]())
    assert (rt2.checkStep (request("PUT", "/a/b","*.*"), response,chain, 0) == -1)
    assert (rt2.checkStep (request("POST", "/a/b","application/junk"), response,chain, 2) == -1)
  }

  test("In a ReqType step, if the content type is null, the returned URI level should be -1") {
    val rt = new ReqType ("XML", "XML", "application/xml|application/json".r, Array[Step]())
    assert (rt.checkStep (request("PUT", "/a/b", null), response,chain, 0) == -1)
  }

  test("In a ReqType step, if the content matches, the URI level should stay the same") {
    val rt = new ReqType ("XML", "XML", "application/xml|application/json".r, Array[Step]())
    assert (rt.checkStep (request("PUT", "/a/b","application/xml"), response,chain, 0) == 0)
    assert (rt.checkStep (request("POST", "/a/b","application/json"), response,chain, 1) == 1)
    val rt2 = new ReqType ("XML", "XML", "text/html".r, Array[Step]())
    assert (rt2.checkStep (request("GET", "/a/b/c","text/html"), response,chain, 2) == 2)
  }

  test("In a WellFormedXML step, if the content contains well formed XML, the uriLevel should stay the same") {
    val wfx = new WellFormedXML("WFXML", "WFXML", Array[Step]())
    assert (wfx.checkStep (request("PUT", "/a/b", "application/xml", <validXML xmlns="http://valid"/>), response, chain, 0) == 0)
    assert (wfx.checkStep (request("PUT", "/a/b", "application/xml", <validXML xmlns="http://valid"><more/></validXML>), response, chain, 1) == 1)
  }

  test("In a WellFormedXML step, the parsed DOM should be stored in the request") {
    val wfx = new WellFormedXML("WFXML", "WFXML", Array[Step]())
    val req1 = request("PUT", "/a/b", "application/xml", <validXML xmlns="http://valid"/>)
    wfx.checkStep (req1, response, chain, 0)
    assert (req1.parsedXML != null)
    assert (req1.parsedXML.isInstanceOf[Document])

    val req2 = request("PUT", "/a/b", "application/xml", <validXML xmlns="http://valid"><more/></validXML>)
    assert (wfx.checkStep (req2, response, chain, 1) == 1)
    assert (req2.parsedXML != null)
    assert (req2.parsedXML.isInstanceOf[Document])
  }

  test("In a WellFormedXML step, if the content is not well formed XML, the uriLevel should be -1") {
    val wfx = new WellFormedXML("WFXML", "WFXML", Array[Step]())
    assert (wfx.checkStep (request("PUT", "/a/b", "application/xml", """<validXML xmlns='http://valid'>"""), response, chain, 0) == -1)
    assert (wfx.checkStep (request("PUT", "/a/b", "application/xml", """{ \"bla\" : 55 }"""), response, chain, 1) == -1)
  }

  test("In a WellFormedXML step, if the content is not well formed XML, the request should contian a SAXException") {
    val wfx = new WellFormedXML("WFXML", "WFXML", Array[Step]())
    val req1 = request("PUT", "/a/b", "application/xml", """<validXML xmlns='http://valid'>""")

    wfx.checkStep (req1, response, chain, 0)
    assert (req1.contentError != null)
    assert (req1.contentError.isInstanceOf[SAXParseException])

    val req2 = request("PUT", "/a/b", "application/xml", """{ \"bla\" : 55 }""")

    wfx.checkStep (req2, response, chain, 1)
    assert (req2.contentError != null)
    assert (req2.contentError.isInstanceOf[SAXParseException])
  }

  test("In a WellFormedXML step, XML in the same request should not be parsed twice") {
    val wfx = new WellFormedXML("WFXML", "WFXML", Array[Step]())
    val req1 = request("PUT", "/a/b", "application/xml", <validXML xmlns="http://valid"/>)
    wfx.checkStep (req1, response, chain, 0)
    assert (req1.parsedXML != null)
    assert (req1.parsedXML.isInstanceOf[Document])

    val doc = req1.parsedXML
    wfx.checkStep (req1, response, chain, 0)
    //
    //  Assert that the same document is being returned.
    //
    assert (doc == req1.parsedXML)
  }

  test("In a WellFormedXML step, on two completely different requests the XML sholud be parsed each time"){
    val wfx = new WellFormedXML("WFXML", "WFXML", Array[Step]())
    val req1 = request("PUT", "/a/b", "application/xml", <validXML xmlns="http://valid"/>)
    val req2 = request("PUT", "/a/b", "application/xml", <validXML xmlns="http://valid"/>)

    wfx.checkStep (req1, response, chain, 0)
    assert (req1.parsedXML != null)
    assert (req1.parsedXML.isInstanceOf[Document])
    wfx.checkStep (req2, response, chain, 0)

    assert (req1.parsedXML != req2.parsedXML)
  }

  test ("Since WellFormedXML steps are synchornous, the parser pool should contain only a single idle parser") {
    assert (XMLParserPool.numActive == 0)
    assert (XMLParserPool.numIdle == 1)
  }

  test ("If there is no content error ContentFail should return NONE") {
    val cf  = new ContentFail ("CF", "CF")
    val wfx = new WellFormedXML("WFXML", "WFXML", Array[Step](cf))
    val req1 = request("PUT", "/a/b", "application/xml", <validXML xmlns="http://valid"/>)
    wfx.checkStep (req1, response, chain, 0)
    assert (cf.check(req1, response, chain, 0) == None)
  }

  test ("If there is an error ContentFail should return BadContentResult") {
    val cf  = new ContentFail ("CF", "CF")
    val wfx = new WellFormedXML("WFXML", "WFXML", Array[Step](cf))
    val req1 = request("PUT", "/a/b", "application/xml", """<validXML xmlns='http://valid'>""")
    wfx.checkStep (req1, response, chain, 0)
    val result = cf.check(req1, response, chain, 0)
    assert (result.isDefined)
    assert (result.get.isInstanceOf[BadContentResult])
  }
}
