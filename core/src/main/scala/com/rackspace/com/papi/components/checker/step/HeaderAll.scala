/***
 *   Copyright 2016 Rackspace US, Inc.
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

import javax.servlet.FilterChain
import javax.xml.namespace.QName
import javax.xml.validation.Schema

import com.rackspace.com.papi.components.checker.servlet._
import com.rackspace.com.papi.components.checker.step.base.{ConnectedStep, Step, StepContext}
import com.rackspace.com.papi.components.checker.util.HeaderUtil._

import com.rackspace.com.papi.components.checker.util.TenantUtil._

import org.xml.sax.SAXParseException

import scala.util.matching.Regex
import scala.annotation.tailrec


class HeaderAll(id : String, label : String, val name : String, val valueTypes : Option[List[QName]],
                schema : Option[Schema], val value : Option[Regex], val message : Option[String],
                val code : Option[Int], val captureHeader : Option[String], val matchingRoles : Option[Set[String]],
                val isTenant : Boolean, val priority : Long, next : Array[Step]) extends ConnectedStep(id, label, next) {


  def this(id : String, label : String,  name : String,  ueTypes : Option[List[QName]],
           schema : Option[Schema],  ue : Option[Regex],  message : Option[String],
           code : Option[Int],  captureHeader : Option[String],
           priority : Long, next : Array[Step]) = this(id, label, name, ueTypes, schema, ue, message, code,
                                                       captureHeader, None, false, priority, next)

  override val mismatchMessage : String = message.getOrElse({
    val matchString = {
      val v1 = valueTypes.getOrElse(List()).map(vt => vt.toString)
      val v2 = { if (value.isEmpty) List() else List(value.get.toString) }
      (v1 ++ v2).mkString(" ")
    }
    s"Expecting an HTTP header $name to have a value matching $matchString"
  })

  val mismatchCode : Int = code.getOrElse(400)

  val xsds : Option[List[XSDStringValidator]] = valueTypes.map(vts => vts.map ( vt=> new XSDStringValidator(vt, schema.get, id)))

  @tailrec
  private[this] def filterHeaders (headers : List[String], xsds : List[XSDStringValidator]) : List[String] = xsds match {
    case Nil => headers
    case xsd :: tail if (headers.isEmpty) => headers
    case xsd :: tail => filterHeaders(headers.filterNot(h => xsd.validate(h).isEmpty), tail)
  }

  override def checkStep(req : CheckerServletRequest, resp : CheckerServletResponse, chain : FilterChain, context : StepContext) : Option[StepContext] = {
    val headers : List[String] = getHeaders(context, req, name)

    //
    //  If there are no headers matching the name, then we set an
    //  error and return None.
    //
    if (headers.isEmpty) {
      req.contentError(new Exception(mismatchMessage), mismatchCode, priority)
      None
    } else {
      //
      //  We filter out the values that match the valueRegEx if we
      //  have one.
      //
      val headersSansValue = {
        if (value.isEmpty) {
          headers
        } else {
          val vr = value.get
          headers.filterNot(v => v match { case vr() => true ; case _ => false})
        }
      }

      //
      // Next we filter out values that match value types.
      //
      val mismatchHeaders = filterHeaders(headersSansValue, xsds.getOrElse(Nil))

      //
      // If we have don't have mismatch header values then we return a
      // valid context, otherwise we set an error.
      //
      if (mismatchHeaders.isEmpty) {
        val contextWithCaptureHeaders = captureHeader match {
          case None => context
          case Some(h) => context.copy(requestHeaders = context.requestHeaders.addHeaders(h, headers))
        }
        val contextWithTenantRoles = isTenant match {
          case false => contextWithCaptureHeaders
          case true => addTenantRoles(contextWithCaptureHeaders, req, name, headers, matchingRoles)
        }
        Some(contextWithTenantRoles)
      } else {
        val mhlist = mismatchHeaders.mkString(" ")
        req.contentError(new Exception(s"$mismatchMessage. The following header values did not match: $mhlist."),
          mismatchCode, priority)
        None
      }
    }
  }
}
