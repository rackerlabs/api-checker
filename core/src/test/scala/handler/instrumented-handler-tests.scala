package com.rackspace.com.papi.components.checker.handler

import java.util.Date
import java.util.UUID
import java.math.BigInteger
import scala.util.Random

import java.lang.management.ManagementFactory
import javax.management.MBeanServer
import javax.management.ObjectName

import com.rackspace.com.papi.components.checker.Validator
import com.rackspace.com.papi.components.checker.BaseValidatorSuite
import com.rackspace.com.papi.components.checker.TestConfig
import com.rackspace.com.papi.components.checker.AssertResultHandler
import com.rackspace.com.papi.components.checker.step._
import com.rackspace.com.papi.components.checker.Converters._
import com.rackspace.cloud.api.wadl.Converters._
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.FunSuite


@RunWith(classOf[JUnitRunner])
class InstrumentedHandlerSuite extends BaseValidatorSuite {

  //
  // validator a simple machine used to test the insturmentation
  // handler.
  //

  val instrumentedHandler = new InstrumentedHandler()

  val handlerConfig = {
    val cnfg = TestConfig()
    val handler = new DispatchResultHandler(List[ResultHandler](new ConsoleResultHandler(),
                                                                instrumentedHandler,
                                                                new AssertResultHandler(),
                                                                new ServletResultHandler()))
    cnfg.resultHandler = handler
    cnfg
  }

  //
  //  This is silly, we use the xml checker format as an intermediate
  //  representation so we have no way to input it directly. As a
  //  result, I manually build the format here and the corresponding
  //  states. Eventually need to fix this.
  //

  val xmlChecker =
    <checker xmlns="http://www.rackspace.com/repose/wadl/checker">
      <step id="S0" type="START" next="a badAURL badMethod"/>
      <step id="a" type="URL" match="a" next="b badBURL badMethod"/>
      <step id="b" type="URL" match="b" next="GET badURL badMethodGet"/>
      <step id="GET" type="METHOD" match="GET" next="Accept"/>
      <step id="Accept" type="ACCEPT"/>
      <step id="badAURL" type="URL_FAIL" notMatch="a"/>
      <step id="badBURL" type="URL_FAIL" notMatch="b"/>
      <step id="badMethodGet" type="METHOD_FAIL" notMatch="GET"/>
      <step id="badMethod" type="METHOD_FAIL"/>
      <step id="badURL" type="URL_FAIL"/>
    </checker>

  val validator = Validator("MyInstTestValidator",{
    val badURL = new URLFail("badURL", "URLFail")
    val badMethod = new MethodFail("badMethod", "MethodFail")
    val badMethodGet = new MethodFailMatch("badMethodGet","MethodFail", "GET".r)
    val badBURL = new URLFailMatch("badBURL", "URLFail", "b".r)
    val badAURL = new URLFailMatch("badAURL", "URLFail", "a".r)
    val accept = new Accept("Accept", "Accept")
    val get = new Method("GET", "GET", "GET".r, Array(accept))
    val b = new URI("b","b","b".r, Array(get, badURL, badMethodGet))
    val a = new URI("a","a","a".r, Array(b, badBURL, badMethod))
    val start = new Start("S0", "Start", Array(a, badAURL, badMethod))
    start
  }, handlerConfig)

  //
  //  Reinitialize the handlers so that we can connect the state
  //  machine with the checker format manually.
  //

  handlerConfig.resultHandler.destroy
  handlerConfig.resultHandler.init(validator, Some(xmlChecker))

  val allSteps = List("S0", "a", "b", "GET", "Accept", "badAURL", "badBURL", "badMethodGet", "badMethod", "badURL")

  val platformMBeanServer = ManagementFactory.getPlatformMBeanServer()

  def getStepObjectName (stepId : String) : ObjectName = {
    new ObjectName("\"com.rackspace.com.papi.components.checker.handler\":type=\"InstrumentedHandler\",scope=\"MyInstTestValidator\",name=\""+stepId+"\"")
  }

  def getStepCount (stepId : String) : Long = {
    platformMBeanServer.getAttribute(getStepObjectName(stepId), "Count").asInstanceOf[Long]
  }

  def getStepCounts (stepIds : List[String]) : List[(String, Long)] = {
    stepIds.map ( s => (s, getStepCount(s)))
  }

  def assertCountsIncremented (initCounts : List[(String, Long)], newCounts : List[(String, Long)]) : Unit = {
   initCounts match {
     case List() => assert(newCounts.isEmpty)
     case count :: counts => newCounts match {
       case List() => assert(false, "Count sizes don't match")
       case ncount :: ncounts => {
           assert (ncount._1 == count._1, s"Count list mismatch $ncount != $count")
           assert (ncount._2 > count._2, s"Count did not increment of state $ncount !> $count")
           assertCountsIncremented (counts, ncounts)
       }
     }
   }
  }

  def assertCountsIncremented (path : List[String], method : String, url : String, expectedError : Option[Int] = None) {
    val initCounts = getStepCounts(path)
    expectedError match {
      case Some(e) => assertResultFailed(validator.validate(request(method, url),response, chain), e)
      case None => validator.validate(request(method, url),response, chain)
    }
    val newCounts = getStepCounts(path)
    assertCountsIncremented (initCounts, newCounts)
    println (getStepCounts(allSteps))
  }

  println(getStepCounts(allSteps))

  test("GET on /a/b should trigger incremented paths on S0, a, b, GET, Accept") {
    assertCountsIncremented (List("S0", "a", "b", "GET", "Accept"), "GET", "/a/b")
  }

  test ("GET on /foo should trigger incremented paths on S0, badAURL") {
    assertCountsIncremented (List("S0", "badAURL"), "GET", "/foo", Some(404))
  }


  test ("GET on / should trigger incremented paths on S0, badMethod") {
    assertCountsIncremented (List("S0", "badMethod"), "GET", "/", Some(405))
  }

  test ("POST on /a should trigger incremented paths on S0, a, badMethod") {
    assertCountsIncremented (List("S0", "a", "badMethod"), "POST", "/a", Some(405))
  }

  test ("GET on /a/foo should trigger incremented paths on S0, a, badBURL") {
    assertCountsIncremented (List("S0", "a", "badBURL"), "GET", "/a/foo", Some(404))
  }

  test ("POST on /a/b should trigger incremented paths on S0, a, b, badMethodGet") {
    assertCountsIncremented (List("S0", "a", "b", "badMethodGet"), "POST", "/a/b", Some(405))
  }

  test ("GET on /a/b/c should trigger incremented paths on S0, a, b, badURL") {
    assertCountsIncremented (List("S0", "a", "b", "badURL"), "GET", "/a/b/c", Some(404))
  }

}
