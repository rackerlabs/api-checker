package com.rackspace.com.papi.components.checker.wadl

import scala.collection.mutable.LinkedList

import org.scalatest.TestFailedException

import com.rackspace.cloud.api.wadl.Converters._
import com.rackspace.cloud.api.wadl.test.BaseWADLSpec

import javax.xml.namespace.QName

import com.rackspace.com.papi.components.checker.step._

class BaseStepSpec extends BaseWADLSpec {
  var builder = new StepBuilder(wadl)

  def withStart(a : Array[Step]) : Array[Step]   = a.filter (f => f.isInstanceOf[Start])
  def withAccept(a : Array[Step]) : Array[Step]  = a.filter (f => f.isInstanceOf[Accept])
  def withURLFail(a : Array[Step]) : Array[Step] = a.filter (f => f.isInstanceOf[URLFail])
  def withMethodFail(a : Array[Step]) : Array[Step] = a.filter (f => f.isInstanceOf[MethodFail])
  def withURLFailMatch(a : Array[Step], mat : String) : Array[Step] =
    a.filter (f => f.isInstanceOf[URLFailMatch]).filter(f => f.asInstanceOf[URLFailMatch].uri.toString == mat)
  def withMethodFailMatch(a : Array[Step], mat : String) : Array[Step] =
    a.filter (f => f.isInstanceOf[MethodFailMatch]).filter(f => f.asInstanceOf[MethodFailMatch].method.toString == mat)
  def withURI(a : Array[Step], uri : String) : Array[Step] =
    a.filter (f => f.isInstanceOf[URI]).filter(f => f.asInstanceOf[URI].uri.toString == uri)
  def withURIXSD(a : Array[Step], qname : QName) : Array[Step] =
    a.filter (f => f.isInstanceOf[URIXSD]).filter(f => f.asInstanceOf[URIXSD].xsd.simpleType == qname)
  def withMethod(a : Array[Step], method : String) : Array[Step] =
    a.filter (f => f.isInstanceOf[Method]).filter(f => f.asInstanceOf[Method]. method.toString == method)
  def withLabel(a : Array[Step], label : String) : Array[Step] = a.filter (f => f.label == label)

  def Start   : (Array[Step]) => Array[Step] = withStart
  def Accept  : (Array[Step]) => Array[Step] = withAccept
  def URLFail : (Array[Step]) => Array[Step] = withURLFail
  def MethodFail : (Array[Step]) => Array[Step] = withMethodFail
  def URLFailMatch(m : String) : (Array[Step]) => Array[Step] = withURLFailMatch(_, m)
  def MethodFailMatch(m : String) : (Array[Step]) => Array[Step] = withMethodFailMatch(_, m)
  def URI(m : String) : (Array[Step]) => Array[Step] = withURI(_, m)
  def URIXSD(m : QName) : (Array[Step]) => Array[Step] = withURIXSD(_, m)
  def Method(m : String) : (Array[Step]) => Array[Step] = withMethod(_, m)
  def Label(m : String) : (Array[Step]) => Array[Step] = withLabel(_, m)

  def assert(s : Step, step_funs : ((Array[Step]) => Array[Step])*) : Unit = {
    if (step_funs.length == 0) throw new TestFailedException("Path assertion should contain at least one step!",4)

    var next : Array[Step] = Array(s)
    for (a <- 0 to step_funs.length - 1) {
      val result : Array[Step] = step_funs(a)(next)
      if (result.length == 0) {
        throw new TestFailedException("Could not complete path",4)
      }
      var list : LinkedList[Step] = new LinkedList[Step]
      result.filter(f => f.isInstanceOf[ConnectedStep]).foreach(r => list ++= r.asInstanceOf[ConnectedStep].next)
      next = list.toArray
    }
  }
}
