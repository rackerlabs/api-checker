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

import com.rackspace.cloud.api.wadl.Converters._
import org.junit.runner.RunWith
import org.scalatest.FlatSpec
import org.scalatest.junit.JUnitRunner

abstract class GivenAWadlWithMetaTransformsBase extends FlatSpec with RaxRolesBehaviors {
  val description = "WADL With Meta-Transforms"
  val descriptionWithoutRaxRoles = description + " and Without RAX-Roles"
  val descriptionWithRaxRoles = description + " and RAX-Roles"
  val descriptionWithRaxRolesMasked = descriptionWithRaxRoles + " Masked"

  val metadataWadl = <application xmlns="http://wadl.dev.java.net/2009/02" xmlns:rax="http://docs.rackspace.com/api">
    <resources base="https://resource.api.rackspace.com">
      <resource id="standardResource" path="standard" rax:useMetadata="standardMeta"/>
      <resource id="customResource" path="custom" rax:useMetadata="customMeta"/>
    </resources>
    <rax:metadata id="standardMeta">
      <!-- I propose we support very simple patterns * and sameThing: (or anyOther string which will be prepended) -->
      <rax:metaRole name="admin" pattern="*"/>
      <rax:metaRole name="billing:role" pattern="billing:"/>
      <rax:metaRole name="service:role" pattern="service:"/>
    </rax:metadata>
    <rax:metadata id="customMeta">
      <!-- at least one pattern with '*' is required, this is the admin role,
               multiple admins are allowed...-->
      <rax:metaRole name="admin" pattern="*"/>
      <rax:metaRole name="superAdmin" pattern="*"/>
      <!-- pattern is optional, if it is left off then the roleName followed by a colon is assumed.
               In this case the pattern is 'customer:role:' -->
      <rax:metaRole name="customer:role"/>
      <!-- multiple roles are allowed as well with the same pattern.
               One role per metaRole, since this is an edge case...
           -->
      <rax:metaRole name="service:role" pattern="service:"/>
      <rax:metaRole name="another_role" pattern="service:"/>
      <!-- Another edge case regex stuff in the pattern should not mess things up.
               This silly combination of role name and pattern should work.
               The pattern is not a regEx |||: is simply treated as a string.
          -->
      <rax:metaRole name="???" pattern="|||:"/>
    </rax:metadata>
  </application>

  val methodGet = "GET"
  val methodPut = "PUT"
  val methodDel = "DELETE"
  val methodPst = "POST"

  val targetsStandard = List(
    "",
    "billing:",
    "service:"
  )

  val targetsCustom = List(
    "",
    "customer:role:",
    "service:",
    "%7C%7C%7C:" //java.net.URLEncoder.encode("|||:", "UTF-8")
  )

  // http://stackoverflow.com/questions/13109720/how-to-create-all-possible-combinations-from-the-elements-of-a-list
  val rolesSet = Set("admin", "billing:role", "service:role", "superAdmin", "customer:role", "another_role", "???")
  val rolesList = rolesSet.subsets.map(_.toList).toList

  def createTests(useSaxon: Boolean) {

    // Standard/Custom GET's, PUT's, DELETE's, and POST's are forbidden when RAX-Roles are not enabled.
    val validator = Validator((localWADLURI, metadataWadl), createConfigWithRaxRoles(false, false, useSaxon))
    List(("standard", targetsStandard), ("custom", targetsCustom)).foreach { case (style, targets) =>
      List(methodGet, methodPut, methodDel, methodPst).foreach { method =>
        targets.foreach { target =>
          rolesList.foreach { roles =>
            // Access should be forbidden.
            it should behave like resourceNotFound(validator, method, s"/$style/metadata/${target}foo", roles, descriptionWithoutRaxRoles)
          }
          it should behave like resourceNotFoundWhenNoXRoles(validator, method, s"/$style/metadata/${target}foo", descriptionWithoutRaxRoles)
        }
      }
    }

    List((true, false), (true, true)).foreach { case (enabled, masked) =>
      val validator = Validator((localWADLURI, metadataWadl), createConfigWithRaxRoles(enabled, masked, useSaxon))
      val desc = if (masked) {
        descriptionWithRaxRolesMasked
      } else {
        descriptionWithRaxRoles
      }
      List(("standard", targetsStandard), ("custom", targetsCustom)).foreach { case (style, targets) =>
        // Standard/Custom GET's are allowed regardless of roles.
        targets.foreach { target =>
          rolesList.foreach { roles =>
            it should behave like accessIsAllowed(validator, methodGet, s"/$style/metadata/${target}foo", roles, desc)
          }
          it should behave like accessIsAllowedWhenNoXRoles(validator, methodGet, s"/$style/metadata/${target}foo", desc)
        }

        // Standard/Custom POST's are not allowed regardless of roles.
        targets.foreach { target =>
          rolesList.foreach { roles =>
            it should behave like methodNotAllowed(validator, methodPst, s"/$style/metadata/${target}foo", roles, desc)
          }
          it should behave like methodNotAllowedWhenNoXRoles(validator, methodPst, s"/$style/metadata/${target}foo", desc)
        }
      }

      // Standard PUT's and DELETE's are allowed or not allowed/forbidden based on roles.
      List(methodPut, methodDel).foreach { method =>
        targetsStandard.foreach { target =>
          rolesList.foreach { roles =>
            // IF the list of roles contains the target
            // OR the list of roles contains admin,
            // THEN access should be allowed;
            // ELSE access should be not allowed/forbidden.
            if (targetInRoles(target, roles, List("admin"))) {
              it should behave like accessIsAllowed(validator, method, s"/standard/metadata/${target}foo", roles, desc)
            } else if (masked) {
              it should behave like methodNotAllowed(validator, method, s"/standard/metadata/${target}foo", roles, desc)
            } else {
              it should behave like accessIsForbidden(validator, method, s"/standard/metadata/${target}foo", roles, desc)
            }
          }
          if (masked) {
            it should behave like methodNotAllowedWhenNoXRoles(validator, method, s"/standard/metadata/${target}foo", desc)
          } else {
            it should behave like accessIsForbiddenWhenNoXRoles(validator, method, s"/standard/metadata/${target}foo", desc)
          }
        }
      }

      // Custom PUT's and DELETE's are allowed or not allowed/forbidden based on roles.
      List(methodPut, methodDel).foreach { method =>
        targetsCustom.foreach { target =>
          rolesList.foreach { roles =>
            // IF the list of roles contains the target
            // OR the list of roles contains admin OR superAdmin,
            // - OR -
            // IF the target is the overloaded target
            // AND the list of roles contains the overloaded role,
            // - OR -
            // IF the target is the URL Encoded regex target
            // AND the list of roles contains the question marks role,
            // THEN access should be allowed;
            // ELSE access should be not allowed/forbidden.
            if (targetInRoles(target, roles, List("admin", "superAdmin"))
              || (target.equals("service:") && targetInRoles("another_role", roles, List.empty))
              || (target.equals("%7C%7C%7C:") && targetInRoles("???", roles, List.empty))
            ) {
              it should behave like accessIsAllowed(validator, method, s"/custom/metadata/${target}foo", roles, desc)
            } else if (masked) {
              it should behave like methodNotAllowed(validator, method, s"/custom/metadata/${target}foo", roles, desc)
            } else {
              it should behave like accessIsForbidden(validator, method, s"/custom/metadata/${target}foo", roles, desc)
            }
          }
          if (masked) {
            it should behave like methodNotAllowedWhenNoXRoles(validator, method, s"/custom/metadata/${target}foo", desc)
          } else {
            it should behave like accessIsForbiddenWhenNoXRoles(validator, method, s"/custom/metadata/${target}foo", desc)
          }
        }
      }
    }
  }

  def createConfigWithRaxRoles(enabled: Boolean, masked: Boolean, useSaxon: Boolean): Config = {
    val config = new Config

    if(useSaxon) {
      config.xsdEngine              = "SaxonEE"
    }
    config.removeDups               = true    // -d Wadl2Checker default is different from Config default.
    config.checkWellFormed          = true    // -w
    config.checkPlainParams         = true    // -p
    config.joinXPathChecks          = true    // -j
    config.checkHeaders             = true    // -H
    config.enableRaxRolesExtension  = enabled // -r
    config.maskRaxRoles403          = masked  // -M
    config.validateChecker          = true    // !-D

    config.resultHandler = TestConfig.assertHandler

    config
  }

  def targetInRoles(target: String, roles: List[String], admins: List[String]): Boolean = {
    if (target.length > 0) {
      roles.exists { role => role.startsWith(target) || target.startsWith(role) } ||
        roles.toSet.intersect(admins.toSet).nonEmpty
    } else {
      roles.toSet.intersect(admins.toSet).nonEmpty
    }
  }
}
