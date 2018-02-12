/***
 *   Copyright 2017 Rackspace US, Inc.
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
package com.rackspace.com.papi.components.checker.util

import com.rackspace.com.papi.components.checker.servlet.CheckerServletRequest
import com.rackspace.com.papi.components.checker.servlet.CheckerServletRequest.ROLES_HEADER
import com.rackspace.com.papi.components.checker.servlet.CheckerServletRequest.MAP_ROLES_HEADER
import com.rackspace.com.papi.components.checker.step.base.StepContext

import com.rackspace.com.papi.components.checker.BaseValidatorSuite

import org.junit.runner.RunWith
import org.mockito.Mockito.when
import org.scalatest.junit.JUnitRunner


@RunWith(classOf[JUnitRunner])
class TenantUtilSuite extends BaseValidatorSuite {

  type HappyPathAssertType = Map[(String /* Tenant Name */, String /*Tenant Value*/), List[String] /*Expected Roles*/]
  type SadPathAssertType = Map[String /*  Desc*/, (String /* Tenant Name */, String /* Tenant Value */, Option[String] /* Map Header Value */)]


  type HappyPathListAssertType = Map[(String /* Tenant Name */, List[String] /*Tenant Values*/), List[String] /*Expected Roles*/]
  type SadPathListAssertType = Map[String /*  Desc*/, (String /* Tenant Name */, List[String] /* Tenant Values */, Option[String] /* Map Header Value */)]

  val mapHeaderValue = b64Encode("""
      {
         "tenant1" : ["admin","foo","bar"],
         "tenant2" : ["admin", "foo"],
         "tenant3" : ["foo", "bar", "biz", "booz"],
         "tenant4" : ["booga"]
      }
    """)

  //
  //  Happy asserts with no preexisting headers
  //

  val happyAsserts : HappyPathAssertType = Map(
    ("happyTenant", "tenant1") -> List("admin/{happyTenant}", "foo/{happyTenant}", "bar/{happyTenant}"),
    ("happyTenant", "tenant2") -> List("admin/{happyTenant}", "foo/{happyTenant}"),
    ("happyTenant", "tenant3") -> List("foo/{happyTenant}", "bar/{happyTenant}", "biz/{happyTenant}", "booz/{happyTenant}"),
    ("fooTenant", "tenant1") -> List("admin/{fooTenant}", "foo/{fooTenant}", "bar/{fooTenant}")
  )

  val emptyContext = StepContext()

  for ((tenantInfo, expectedRoles) <- happyAsserts) {
    val tenantName  = tenantInfo._1
    val tenantValue = tenantInfo._2

    test(s"Assert correct headers given $tenantName and $tenantValue and an empty context") {
      val req = new CheckerServletRequest(request("GET","/foo", "application/XML", "", false, Map(MAP_ROLES_HEADER->List(mapHeaderValue))))
      val newContext = TenantUtil.addTenantRoles(emptyContext, req, tenantName, tenantValue)
      assert (newContext.requestHeaders(ROLES_HEADER) == expectedRoles)
    }

    test(s"Assert correct headers given $tenantName and $tenantValue and an empty context (list call)") {
      val req = new CheckerServletRequest(request("GET","/foo", "application/XML", "", false, Map(MAP_ROLES_HEADER->List(mapHeaderValue))))
      val newContext = TenantUtil.addTenantRoles(emptyContext, req, tenantName, List(tenantValue), None)
      assert (newContext.requestHeaders(ROLES_HEADER) == expectedRoles)
    }

  }

  //
  //  Happy asserts with existing request headers.
  //

  val happyNonEmptyAsserts : HappyPathAssertType = Map(
    ("happyTenant", "tenant1") -> List("foo", "admin/{happyTenant}", "admin/{happyTenant}", "foo/{happyTenant}", "bar/{happyTenant}"),
    ("happyTenant", "tenant2") -> List("foo", "admin/{happyTenant}", "admin/{happyTenant}", "foo/{happyTenant}"),
    ("happyTenant", "tenant3") -> List("foo", "admin/{happyTenant}", "foo/{happyTenant}", "bar/{happyTenant}", "biz/{happyTenant}", "booz/{happyTenant}"),
    ("fooTenant", "tenant1") -> List("foo", "admin/{happyTenant}", "admin/{fooTenant}", "foo/{fooTenant}", "bar/{fooTenant}")
  )

  val notEmptyContext = new StepContext(0, (new HeaderMap).addHeaders(ROLES_HEADER, List("foo","admin/{happyTenant}")))

  for ((tenantInfo, expectedRoles) <- happyNonEmptyAsserts) {
    val tenantName  = tenantInfo._1
    val tenantValue = tenantInfo._2

    test(s"Assert correct headers given $tenantName and $tenantValue and an non-empty context") {
      val req = new CheckerServletRequest(request("GET","/foo", "application/XML", "", false, Map(MAP_ROLES_HEADER->List(mapHeaderValue))))
      val newContext = TenantUtil.addTenantRoles(notEmptyContext, req, tenantName, tenantValue)
      assert (newContext.requestHeaders(ROLES_HEADER) == expectedRoles)
    }

    test(s"Assert correct headers given $tenantName and $tenantValue and an non-empty context (list call)") {
      val req = new CheckerServletRequest(request("GET","/foo", "application/XML", "", false, Map(MAP_ROLES_HEADER->List(mapHeaderValue))))
      val newContext = TenantUtil.addTenantRoles(notEmptyContext, req, tenantName, List(tenantValue), None)
      assert (newContext.requestHeaders(ROLES_HEADER) == expectedRoles)
    }
  }

  //
  //  Sad asserts, missed tenants.
  //

  val sadAsserts : SadPathAssertType = Map(
    "If tenant does not match"->("happyTenant", "tenant5", Some(mapHeaderValue)),
    "If the map header is null"->("happyTenant", "tenant1", Some(b64Encode("null"))),
    "If the map header is valid json, with wrong format (boolean) "->("happyTenant", "tenant1", Some(b64Encode("false"))),
    "If the map header is valid json, with wrong format (no list)"->("happyTenant", "tenant1", Some(b64Encode("""{ "tenant1" : "foo" }"""))),
    "If the map header contains unparsadble json"->("happyTenant", "tenant1", Some(b64Encode("{ booga ]"))),
    "If there is no map header"->("happyTenant", "tenant1", None),
    "If we have bad base64 data"->("happyTenant", "tenant1", Some("""{ "tenant1" : ["foo"]} """))
  )

  for ((desc, assertInf) <- sadAsserts) {
    val tenantName  = assertInf._1
    val tenantValue = assertInf._2
    val mapHeader   = assertInf._3

    test(s"$desc do not modify the context") {
      val req = new CheckerServletRequest(request("GET","/foo", "application/XML", "", false, mapHeader match {
        case Some(s : String) => Map(MAP_ROLES_HEADER->List(s))
        case None => Map[String, List[String]]()
      }))

      val newEmptyContext = TenantUtil.addTenantRoles(emptyContext, req, tenantName, tenantValue)
      assert (newEmptyContext == emptyContext)

      val newNotEmptyContext = TenantUtil.addTenantRoles(notEmptyContext, req, tenantName, tenantValue)
      assert (newNotEmptyContext == notEmptyContext)
    }

    test(s"$desc do not modify the context (list call)") {
      val req = new CheckerServletRequest(request("GET","/foo", "application/XML", "", false, mapHeader match {
        case Some(s : String) => Map(MAP_ROLES_HEADER->List(s))
        case None => Map[String, List[String]]()
      }))

      val newEmptyContext = TenantUtil.addTenantRoles(emptyContext, req, tenantName, List(tenantValue), None)
      assert (newEmptyContext == emptyContext)

      val newNotEmptyContext = TenantUtil.addTenantRoles(notEmptyContext, req, tenantName, List(tenantValue), None)
      assert (newNotEmptyContext == notEmptyContext)
    }

  }

  //
  //  Happy asserts on a list with multiple tenant values.
  //
  val happyListAsserts : HappyPathListAssertType = Map(
    ("happyTenant", List("tenant1", "tenant2")) -> List("foo/{happyTenant}", "admin/{happyTenant}", "bar/{happyTenant}"),
    ("happyTenant", List("tenant2", "tenant1")) -> List("foo/{happyTenant}", "admin/{happyTenant}", "bar/{happyTenant}"),
    ("happyTenant", List("tenant1", "tenant3")) -> List("bar/{happyTenant}", "foo/{happyTenant}", "admin/{happyTenant}"),
    ("happyTenant", List("tenant3", "tenant1")) -> List("bar/{happyTenant}", "foo/{happyTenant}", "admin/{happyTenant}"),
    ("happyTenant", List("tenant2", "tenant3")) -> List("foo/{happyTenant}", "bar/{happyTenant}", "admin/{happyTenant}"),
    ("happyTenant", List("tenant1", "tenant2", "tenant3")) -> List("foo/{happyTenant}", "bar/{happyTenant}", "admin/{happyTenant}"),
    ("happyTenant", List("tenant3", "tenant2", "tenant1")) -> List("foo/{happyTenant}", "bar/{happyTenant}", "admin/{happyTenant}"),
    ("happyTenant", List("tenant2", "tenant3", "tenant1")) -> List("foo/{happyTenant}", "bar/{happyTenant}", "admin/{happyTenant}"),
    ("fooTenant", List("tenant2", "tenant3", "tenant1")) -> List("foo/{fooTenant}", "bar/{fooTenant}", "admin/{fooTenant}")
  )

  for ((tenantInfo, expectedRoles) <- happyListAsserts) {
    val tenantName = tenantInfo._1
    val tenantValues = tenantInfo._2

    test(s"Correctly Handle multiple roles with tenant name $tenantName on values : $tenantValues") {
      val req = new CheckerServletRequest(request("GET","/foo", "application/XML", "", false, Map(MAP_ROLES_HEADER->List(mapHeaderValue))))
      val newContext = TenantUtil.addTenantRoles(emptyContext, req, tenantName, tenantValues, Some(Set("admin/{happyTenant}", "foo/{happyTenant}", "bar/{happyTenant}",
                                                                                                       "admin/{fooTenant}", "foo/{fooTenant}", "bar/{fooTenant}")))
      assert ((Set[String]() ++ newContext.requestHeaders(ROLES_HEADER)) == (Set[String]() ++ expectedRoles))
    }
  }

  //
  //  Happy asserts with existing request headers
  //
  val happyNonEmptyListAsserts : HappyPathListAssertType = Map(
    ("happyTenant", List("tenant1", "tenant2")) -> List("foo", "admin/{happyTenant}", "foo/{happyTenant}", "bar/{happyTenant}"),
    ("happyTenant", List("tenant2", "tenant1")) -> List("foo", "admin/{happyTenant}", "foo/{happyTenant}", "bar/{happyTenant}"),
    ("happyTenant", List("tenant1", "tenant3")) -> List("foo", "admin/{happyTenant}", "bar/{happyTenant}", "foo/{happyTenant}"),
    ("happyTenant", List("tenant3", "tenant1")) -> List("foo", "admin/{happyTenant}", "bar/{happyTenant}", "foo/{happyTenant}"),
    ("happyTenant", List("tenant2", "tenant3")) -> List("foo", "admin/{happyTenant}", "foo/{happyTenant}", "bar/{happyTenant}"),
    ("happyTenant", List("tenant1", "tenant2", "tenant3")) -> List("foo", "admin/{happyTenant}", "foo/{happyTenant}", "bar/{happyTenant}"),
    ("happyTenant", List("tenant3", "tenant2", "tenant1")) -> List("foo", "admin/{happyTenant}", "foo/{happyTenant}", "bar/{happyTenant}"),
    ("happyTenant", List("tenant2", "tenant3", "tenant1")) -> List("foo", "admin/{happyTenant}", "foo/{happyTenant}", "bar/{happyTenant}"),
    ("fooTenant", List("tenant2", "tenant3", "tenant1")) -> List("foo", "admin/{happyTenant}", "foo/{fooTenant}", "bar/{fooTenant}", "admin/{fooTenant}")
  )

  for ((tenantInfo, expectedRoles) <- happyNonEmptyListAsserts) {
    val tenantName = tenantInfo._1
    val tenantValues = tenantInfo._2

    test(s"Correctly Handle multiple roles with tenant name $tenantName on values : $tenantValues on a non-empty context") {
      val req = new CheckerServletRequest(request("GET","/foo", "application/XML", "", false, Map(MAP_ROLES_HEADER->List(mapHeaderValue))))
      val newContext = TenantUtil.addTenantRoles(notEmptyContext, req, tenantName, tenantValues, Some(Set("admin/{happyTenant}", "foo/{happyTenant}", "bar/{happyTenant}",
                                                                                                          "admin/{fooTenant}", "foo/{fooTenant}", "bar/{fooTenant}")))
      assert ((Set[String]() ++ newContext.requestHeaders(ROLES_HEADER)) == (Set[String]() ++ expectedRoles))
    }
  }


  //
  //  Sad asserts, missed tenants with multiple tenant values.
  //

  val sadListAsserts : SadPathListAssertType = Map(
    "If tenant does not match (does not exist)"->("happyTenant", List("tenant1","tenant5"), Some(mapHeaderValue)),
    "If tenant does not match (no roles match)"->("happyTenant", List("tenant1","tenant2","tenant4"), Some(mapHeaderValue)),
    "If tenant does not match (no roles match 2)"->("happyTenant", List("tenant4","tenant1","tenant3"), Some(mapHeaderValue)),
    "If tenant does not match (no roles match 3)"->("happyTenant", List("tenant4","tenant2"), Some(mapHeaderValue)),
    "If the map header is null"->("happyTenant", List("tenant1","tenant2"), Some(b64Encode("null"))),
    "If the map header is valid json, with wrong format (boolean) "->("happyTenant", List("tenant1", "tenant2"), Some(b64Encode("false"))),
    "If the map header is valid json, with wrong format (no list)"->("happyTenant", List("tenant1","tenant2"), Some(b64Encode("""{ "tenant1" : "foo" }"""))),
    "If the map header contains unparsadble json"->("happyTenant", List("tenant1","tenant2"), Some(b64Encode("{ booga ]"))),
    "If there is no map header"->("happyTenant", List("tenant1","tenant2"), None),
    "If the map header has invalid base64 encoding"->("happyTenant", List("tenant1","tenant2"), Some("""{ "tenant1" : "foo" }"""))
  )

  for ((desc, assertInf) <- sadListAsserts) {
    val tenantName  = assertInf._1
    val tenantValues = assertInf._2
    val mapHeader   = assertInf._3

    test(s"$desc do not modify the context (multi-value)") {
      val req = new CheckerServletRequest(request("GET","/foo", "application/XML", "", false, mapHeader match {
        case Some(s : String) => Map(MAP_ROLES_HEADER->List(s))
        case None => Map[String, List[String]]()
      }))

      val newEmptyContext = TenantUtil.addTenantRoles(emptyContext, req, tenantName, tenantValues, Some(Set("admin/{happyTenant}", "booga/{fooTenant}")))
      assert (newEmptyContext == emptyContext)

      val newNotEmptyContext = TenantUtil.addTenantRoles(notEmptyContext, req, tenantName, tenantValues, Some(Set("admin/{happyTenant}", "booga/{fooTenant}")))
      assert (newNotEmptyContext == notEmptyContext)
    }
  }

}
