package com.rackspace.com.papi.components.checker

import java.util.Date
import java.util.UUID
import java.math.BigInteger
import scala.util.Random

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

import com.rackspace.cloud.api.wadl.Converters._

@RunWith(classOf[JUnitRunner])
class ValidatorWADLSuite extends BaseValidatorSuite {
  //
  // validator_EMPTY does not allow ANY HTTP requests. The validator
  // is used in the following tests.
  //
  val validator_EMPTY = Validator(
        <application xmlns="http://wadl.dev.java.net/2009/02">
           <grammars/>
           <resources base="https://test.api.openstack.com">
              <resource/>
           </resources>
        </application>, false, assertHandler)

  test ("GET on / should fail on validator_EMPTY") {
    assertResultFailed(validator_EMPTY.validate(request("GET","/"),response), 405)
  }

  test ("an empty GET should fail on validator_EMPTY") {
    assertResultFailed(validator_EMPTY.validate(request("GET",""),response), 405)
  }

  test ("GET on /a should fail on validator_EMPTY") {
    assertResultFailed(validator_EMPTY.validate(request("GET","/a"),response), 404)
  }

  test ("POST on /a/b/c/hello/there should fail on validator_EMPTY") {
    assertResultFailed(validator_EMPTY.validate(request("GET","/a/b/c/hello/there"),response), 404)
  }

  test ("a completely empty request should fail on validator_EMPTY") {
    assertResultFailed(validator_EMPTY.validate(request("",""),response), 405)
  }

  //
  // validator_AB allows a GET on /a/b. The validator is used in the
  // following tests.
  //
  val validator_AB = Validator(
    <application xmlns="http://wadl.dev.java.net/2009/02">
         <grammars/>
           <resources base="https://test.api.openstack.com">
              <resource path="/a/b">
                   <method name="GET">
                      <response status="200 203"/>
                   </method>
              </resource>
           </resources>
    </application>
    ,false, assertHandler)

  test ("GET on /a/b should succeed on validator_AB") {
    validator_AB.validate(request("GET","/a/b"),response)
  }

  test ("GET on /a/b/ should succeed on validator_AB") {
    validator_AB.validate(request("GET","/a/b/"),response)
  }

  test ("GET on a/b/ should succeed on validator_AB") {
    validator_AB.validate(request("GET","a/b/"),response)
  }

  test ("GET on / should fail on validator_AB") {
    assertResultFailed(validator_AB.validate(request("GET","/"),response), 405)
  }

  test ("an empty GET should fail on validator_AB") {
    assertResultFailed(validator_AB.validate(request("GET",""),response), 405)
  }

  test ("GET on /a should fail on validator_AB") {
    assertResultFailed(validator_AB.validate(request("GET","/a"),response), 405)
  }

  test ("GET on /a/b/c/d should fail on validator_AB") {
    assertResultFailed(validator_AB.validate(request("GET","/a/b/c/d"),response), 404)
  }

  test ("POST on /a/b should fail on validator_AB") {
    assertResultFailed(validator_AB.validate(request("POST","/a/b"),response), 405)
  }

  test ("GET on /index.html should fail on validator_AB") {
    assertResultFailed(validator_AB.validate(request("GET","/index.html"),response), 404)
  }

  //
  // validator_REG allows a GET on /a/.*/c. That is, the 2nd URI
  // component can be anything. The validator is used in the
  // following tests.
  //
  val validator_REG = Validator(
    <application xmlns="http://wadl.dev.java.net/2009/02"
                     xmlns:xsd="http://www.w3.org/2001/XMLSchema">
           <grammars/>
           <resources base="https://test.api.openstack.com">
              <resource path="a/{b}/c">
                   <param name="b" style="template" type="xsd:string"/>
                   <method href="#getMethod" />
              </resource>
           </resources>
           <method id="getMethod" name="GET">
               <response status="200 203"/>
           </method>
    </application>
    ,false, assertHandler)

  test ("GET on /a/7/c should succeed on validator_REG") {
    validator_REG.validate(request("GET","/a/7/c"),response)
  }

  test ("GET on /a/-7/c should succeed on validator_REG") {
    validator_REG.validate(request("GET","/a/-7/c"),response)
  }

  test ("GET on /a/<randomLong>/c should succeed on validator_REG") {
    val rl = new Random(new Date().getTime()).nextLong()
    validator_REG.validate(request("GET","/a/"+rl+"/c"),response)
  }

  test ("GET on /a/<bigInt>/c should succeed on validator_REG") {
    val bi = new BigInteger(1024, new Random(new Date().getTime()).self)
    validator_REG.validate(request("GET","/a/"+bi+"/c"),response)
  }

  test ("GET on /a/<randomDouble>/c should succeed validator_REG") {
    val rf = new Random(new Date().getTime()).nextDouble()
    validator_REG.validate(request("GET","/a/"+rf+"/c"),response)
  }

  test ("GET on /a/<uuid>/c should succeed validator_REG") {
    val uuid = UUID.randomUUID().toString()
    validator_REG.validate(request("GET","/a/"+uuid+"/c"),response)
  }

  test ("GET on /a/<katakana>/c should succeed validator_REG") {
    validator_REG.validate(request("GET","/a/%E3%83%84%E3%83%85%E3%83%8C%E3%82%A4/c"),response)
  }

  test ("GET on /a/<arrows>/c should succeed validator_REG") {
    validator_REG.validate(request("GET","/a/%E2%86%90%E2%86%91%E2%86%92%E2%86%93/c"),response)
  }

  test ("GET on /a/<snowman>/c should succeed validator_REG") {
    validator_REG.validate(request("GET","/a/%E2%98%83/c"),response)
  }

  test ("GET on /a/b/c should succeed validator_REG") {
    validator_REG.validate(request("GET","/a/b/c"),response)
  }

  test ("GET on /a/ 7/c should succeed validator_REG") {
    validator_REG.validate(request("GET","/a/+7/c"),response)
  }

  test ("GET on /a/+7/c should succeed validator_REG") {
    validator_REG.validate(request("GET","/a/%2B7/c"),response)
  }

  test ("GET on /a/    /c should succeed validator_REG") {
    validator_REG.validate(request("GET","/a/++++/c"),response)
  }

  test ("GET on /a/  hi  /c should succeed validator_REG") {
    validator_REG.validate(request("GET","/a/++hi++/c"),response)
  }

  test ("GET on /a//c should fail validator_REG") {
    assertResultFailed(validator_REG.validate(request("GET","/a//c"),response), 405)
  }

  test ("GET on /a should fail validator_REG") {
    assertResultFailed(validator_REG.validate(request("GET","/a"),response), 405)
  }

  test ("GET on /a/b/d should fail validator_REG") {
    assertResultFailed(validator_REG.validate(request("GET","/a/b/d"),response), 404)
  }

  //
  // validator_AM allows:
  //
  // GET /.*/b
  // PUT /a/b
  //
  // The of course means that a GET on /a/b is allowed.
  //
  // The validator is used in the following tests.
  //
  val validator_AM = Validator(
    <application xmlns="http://wadl.dev.java.net/2009/02"
                     xmlns:xsd="http://www.w3.org/2001/XMLSchema">
           <grammars/>
           <resources base="https://test.api.openstack.com">
              <resource path="{any}/b">
                   <param name="any" style="template" type="xsd:string"/>
                   <method href="#getMethod" />
              </resource>
              <resource path="a/b">
                   <method href="#putMethod" />
              </resource>
           </resources>
           <method id="getMethod" name="GET">
               <response status="200 203"/>
           </method>
           <method id="putMethod" name="PUT">
               <response status="200"/>
           </method>
    </application>
    ,false, assertHandler)

  test ("GET on /a/b should succeed on validator_AM") {
    validator_AM.validate(request("GET","/a/b"),response)
  }

  test ("PUT on /a/b should succeed on validator_AM") {
    validator_AM.validate(request("PUT","/a/b"),response)
  }

  test ("GET on /b/b should succeed on validator_AM") {
    validator_AM.validate(request("GET","/b/b"),response)
  }

  test ("GET on /<katakana>/b should succeed validator_AM") {
    validator_AM.validate(request("GET","/%E3%83%84%E3%83%85%E3%83%8C%E3%82%A4/b"),response)
  }

  test ("GET on /<arrows>/b should succeed validator_AM") {
    validator_AM.validate(request("GET","/%E2%86%90%E2%86%91%E2%86%92%E2%86%93/b"),response)
  }

  test ("POST on /a/b should fail on validator_AM") {
    assertResultFailed(validator_AM.validate(request("POST","/a/b"),response), 405)
  }

  test ("DELETE on /z/b should fail on validator_AM") {
    assertResultFailed(validator_AM.validate(request("DELETE","/z/b"),response), 405)
  }

  test ("GET on /a/c should fail on validator_AM") {
    assertResultFailed(validator_AM.validate(request("GET","/a/c"),response), 404)
  }

  test ("GET on /z/c should fail on validator_AM") {
    assertResultFailed(validator_AM.validate(request("GET","/z/c"),response), 404)
  }

  //
  // validator_UUID allows:
  //
  // The validator is used in the following tests.
  //
  val validator_UUID = Validator(
    <application xmlns="http://wadl.dev.java.net/2009/02"
                 xmlns:csapi="http://docs.openstack.org/compute/api/v1.1">
           <grammars>
              <include href="https://raw.github.com/openstack/compute-api/master/openstack-compute-api-2/src/xsd/common.xsd"/>
           </grammars>
           <resources base="https://test.api.openstack.com">
              <resource id="uuid" path="path/to/my/resource/{uuid}">
                   <param name="uuid" style="template" type="csapi:UUID"/>
                   <method href="#getMethod" />
              </resource>
              <resource id="progress" path="path/to/{progress}">
                   <param name="progress" style="template" type="csapi:Progress"/>
                   <method href="#getMethod" />
              </resource>
           </resources>
           <method id="getMethod" name="GET">
               <response status="200 203"/>
           </method>
        </application>
    ,false, assertHandler)

  test ("GET on /path/to/my/resource/bbe10c88-6477-11e1-84cf-979e24b1498f should succeed on validator_UUID") {
    validator_UUID.validate(request("GET","/path/to/my/resource/bbe10c88-6477-11e1-84cf-979e24b1498f"),response)
  }

  test ("GET on /path/to/my/resource/e5b13268-6477-11e1-8e8a-ff0ea421704f should succeed on validator_UUID") {
    validator_UUID.validate(request("GET","/path/to/my/resource/e5b13268-6477-11e1-8e8a-ff0ea421704f"),response)
  }

  test ("GET on /path/to/my/resource/16dfce76-6478-11e1-9e38-97e6e1882c28 should succeed on validator_UUID") {
    validator_UUID.validate(request("GET","/path/to/my/resource/16dfce76-6478-11e1-9e38-97e6e1882c28"),response)
  }

  test ("DELETE on /path/to/my/resource/bbe10c88-6477-11e1-84cf-979e24b1498f should fail on validator_UUID") {
    assertResultFailed(validator_UUID.validate(request("DELETE","/path/to/my/resource/bbe10c88-6477-11e1-84cf-979e24b1498f"),response), 405)
  }

  test ("GET on /path/to/my/resource/bbe10c88-6477-11e1-84cf-979e24b1498z should fail on validator_UUID") {
    assertResultFailed(validator_UUID.validate(request("GET","/path/to/my/resource/bbe10c88-6477-11e1-84cf-979e24b1498z"),response), 404)
  }

  test ("GET on /path/to/100 should succeed on validator_UUID") {
    validator_UUID.validate(request("GET","/path/to/100"),response)
  }

  test ("GET on /path/to/20 should succeed on validator_UUID") {
    validator_UUID.validate(request("GET","/path/to/20"),response)
  }

  test ("GET on /path/to/hello should fail on validator_UUID") {
    assertResultFailed(validator_UUID.validate(request("GET","/path/to/hello"),response), 404)
  }

  test ("GET on /path/to/101 should fail on validator_UUID") {
    assertResultFailed(validator_UUID.validate(request("GET","/path/to/101"),response), 404)
  }

}
