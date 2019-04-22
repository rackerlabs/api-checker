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
package com.rackspace.com.papi.components.checker.util

import com.rackspace.com.papi.components.checker.servlet.CheckerServletRequest
import com.rackspace.com.papi.components.checker.servlet.CheckerServletRequest.ROLES_HEADER
import com.rackspace.com.papi.components.checker.step.base.StepContext

import com.typesafe.scalalogging.LazyLogging

import scala.annotation.tailrec

object TenantUtil extends LazyLogging {
  /**
   * Adds tenanted roles to the StepContext given the current checker
   * request, the name of a tenant paramater, and the current value of
   * the tenant parameter.
   */
  def addTenantRoles(context : StepContext, request : CheckerServletRequest,
                     tenantName : String, tenantValue : String) : StepContext = try {
    getPossibleRoles(context, request, tenantName, tenantValue)  match {
      case Nil => context
      case roles : List[String] => context.copy(requestHeaders = context.requestHeaders.addHeaders(ROLES_HEADER, roles))
    }
  } catch {
    case e : Exception =>
      logger.error(s"Strange error while computing tenant roles. Ignoring map roles!", e)
      context
  }

  /**
   * Adds tenanted roles to the StepContext given the current
   * checker request, the name of a tenant parameter, and the current
   * values of the tenant parameter.
   *
   * All values must be accepted for the given role for it to match.
   */
  def addTenantRoles(context : StepContext, request : CheckerServletRequest,
                     tenantName : String, tenantValues : List[String],
                     matchingRoles : Option[Set[String]]) : StepContext = try {
    tenantValues match {
      case Nil => context
      case value :: Nil  => addTenantRoles(context, request, tenantName, value)
      case firstValue :: otherValues => matchingRoles match {
        case None => context
        case Some(mroles) => reduceRoles(context, request, tenantName, firstValue :: otherValues, mroles, Nil) match {
          case Nil => context
          case roles : List[String] => context.copy(requestHeaders = context.requestHeaders.addHeaders(ROLES_HEADER, roles))
        }
      }
    }
  } catch {
    case e : Exception =>
      logger.error(s"Strange error while computing tenant roles. Ignoring map roles!", e)
      context
  }

  @tailrec
  private[this] def reduceRoles (context: StepContext, request : CheckerServletRequest,
                                 tenantName : String, tenantValues : List[String],
                                 matchingRoles : Set[String], retRoles : List[String]) : List[String] = tenantValues match {
    case Nil => retRoles
    case value :: nextTenantValues =>
      getPossibleRoles(context, request, tenantName, value).filter(matchingRoles.contains(_)) match {
        case Nil => Nil // We have a tenant value that doesn't match any roles, We can't let the request through!
        case roles : List[String] => reduceRoles(context, request, tenantName, nextTenantValues, matchingRoles, roles ++ retRoles)
      }
  }


  private[this] def getPossibleRoles(context : StepContext, request : CheckerServletRequest,
                                     tenantName : String, tenantValue : String) : List[String] =
    request.mappedRoles.getOrElse(tenantValue,Nil).map(_+"/{"+tenantName+"}")
}
