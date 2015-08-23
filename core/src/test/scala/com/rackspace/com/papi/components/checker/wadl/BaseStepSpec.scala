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
package com.rackspace.com.papi.components.checker.wadl

import javax.xml.namespace.QName

import com.rackspace.cloud.api.wadl.test.BaseWADLSpec
import com.rackspace.com.papi.components.checker.step._
import com.rackspace.com.papi.components.checker.step.base.{ConnectedStep, Step}
import com.rackspace.com.papi.components.checker.step.startend._
import org.scalatest.exceptions.TestFailedException

import scala.collection.mutable.MutableList

class BaseStepSpec extends BaseWADLSpec {
  var builder = new StepBuilder(wadl)

  def withStart(a : Array[Step]) : Array[Step]   = a.filter (f => f.isInstanceOf[Start])
  def withAccept(a : Array[Step]) : Array[Step]  = a.filter (f => f.isInstanceOf[Accept])
  def withURLFail(a : Array[Step]) : Array[Step] = a.filter (f => f.isInstanceOf[URLFail])
  def withMethodFail(a : Array[Step]) : Array[Step] = a.filter (f => f.isInstanceOf[MethodFail])
  def withContentFail (a : Array[Step]) : Array[Step] = a.filter(f => f.isInstanceOf[ContentFail])
  def withWellXML(a : Array[Step]) : Array[Step] = a.filter (f => f.isInstanceOf[WellFormedXML])
  def withWellJSON(a : Array[Step]) : Array[Step] = a.filter (f => f.isInstanceOf[WellFormedJSON])
  def withXSD(a : Array[Step]) : Array[Step] = a.filter (f => f.isInstanceOf[XSD])
  def withURLFailMatch(a : Array[Step], mat : String) : Array[Step] =
    a.filter (f => f.isInstanceOf[URLFailMatch]).filter(f => f.asInstanceOf[URLFailMatch].uri.toString == mat)
  def withMethodFailMatch(a : Array[Step], mat : String) : Array[Step] =
    a.filter (f => f.isInstanceOf[MethodFailMatch]).filter(f => f.asInstanceOf[MethodFailMatch].method.toString == mat)
  def withURI(a : Array[Step], uri : String) : Array[Step] =
    a.filter (f => f.isInstanceOf[URI]).filter(f => f.asInstanceOf[URI].uri.toString == uri)
  def withReqType(a : Array[Step], reqType : String) : Array[Step] =
    a.filter (f => f.isInstanceOf[ReqType]).filter(f => f.asInstanceOf[ReqType].rtype.toString == reqType)
  def withXPath(a : Array[Step], exp : String) : Array[Step] =
    a.filter (f => f.isInstanceOf[XPath]).filter(f => f.asInstanceOf[XPath].expression == exp)
  def withReqTypeFail(a : Array[Step], types : String) : Array[Step] =
    a.filter (f => f.isInstanceOf[ReqTypeFail]).filter(f => f.asInstanceOf[ReqTypeFail].types.toString == types)
  def withURIXSD(a : Array[Step], qname : QName) : Array[Step] =
    a.filter (f => f.isInstanceOf[URIXSD]).filter(f => f.asInstanceOf[URIXSD].xsd.simpleType == qname)
  def withMethod(a : Array[Step], method : String) : Array[Step] =
    a.filter (f => f.isInstanceOf[Method]).filter(f => f.asInstanceOf[Method]. method.toString == method)
  def withLabel(a : Array[Step], label : String) : Array[Step] = a.filter (f => f.label == label)

  def Start   : (Array[Step]) => Array[Step] = withStart
  def Accept  : (Array[Step]) => Array[Step] = withAccept
  def URLFail : (Array[Step]) => Array[Step] = withURLFail
  def MethodFail : (Array[Step]) => Array[Step] = withMethodFail
  def ContentFail : (Array[Step]) => Array[Step] = withContentFail
  def WellFormedXML : (Array[Step]) => Array[Step] = withWellXML
  def WellFormedJSON : (Array[Step]) => Array[Step] = withWellJSON
  def XSD : (Array[Step]) => Array[Step] = withXSD
  def URLFailMatch(m : String) : (Array[Step]) => Array[Step] = withURLFailMatch(_, m)
  def MethodFailMatch(m : String) : (Array[Step]) => Array[Step] = withMethodFailMatch(_, m)
  def URI(m : String) : (Array[Step]) => Array[Step] = withURI(_, m)
  def URIXSD(m : QName) : (Array[Step]) => Array[Step] = withURIXSD(_, m)
  def Method(m : String) : (Array[Step]) => Array[Step] = withMethod(_, m)
  def Label(m : String) : (Array[Step]) => Array[Step] = withLabel(_, m)
  def ReqType(m : String) : (Array[Step]) => Array[Step] = withReqType(_, m)
  def ReqTypeFail(m : String) : (Array[Step]) => Array[Step] = withReqTypeFail(_, m)
  def XPath(m : String) : (Array[Step]) => Array[Step] = withXPath(_, m)

  def assert(s : Step, step_funs : ((Array[Step]) => Array[Step])*) : Unit = {
    if (step_funs.isEmpty) throw new TestFailedException("Path assertion should contain at least one step!",4)

    var next : Array[Step] = Array(s)
    for (a <- 0 to step_funs.length - 1) {
      val result : Array[Step] = step_funs(a)(next)
      if (result.length == 0) {
        throw new TestFailedException("Could not complete path",4)
      }
      var list : MutableList[Step] = new MutableList[Step]
      result.filter(f => f.isInstanceOf[ConnectedStep]).foreach(r => list ++= r.asInstanceOf[ConnectedStep].next)
      next = list.toArray
    }
  }
}
