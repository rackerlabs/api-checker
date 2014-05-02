package com.rackspace.com.papi.components.checker.handler

import java.lang.management.ManagementFactory
import javax.management.MBeanServer
import javax.management.ObjectName

import javax.xml.transform.Transformer
import javax.xml.transform.stream.StreamSource
import javax.xml.transform.sax.SAXResult

import com.rackspace.com.papi.components.checker.step.StepHandler
import com.rackspace.com.papi.components.checker.util.IdentityTransformPool

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

  val xmlChecker =
    <checker xmlns="http://www.rackspace.com/repose/wadl/checker">
      <step id="S0" type="START" next="a badAURL badMethod"/>
      <step id="a" type="URL" match="a" next="b badBURL badMethod"/>
      <step id="b" type="URL" match="b" next="GET badURL badMethodGet"/>
      <step id="GET" type="METHOD" match="GET" next="Accept"/>
      <step id="Accept" type="ACCEPT" priority="100000"/>
      <step id="badAURL" type="URL_FAIL" notMatch="a" priority="1000"/>
      <step id="badBURL" type="URL_FAIL" notMatch="b" priority="1000"/>
      <step id="badMethodGet" type="METHOD_FAIL" notMatch="GET" priority="2000"/>
      <step id="badMethod" type="METHOD_FAIL" priority="2000"/>
      <step id="badURL" type="URL_FAIL" priority="1000"/>
    </checker>

  val steps = {
    var transf : Transformer = null
    val stepHandler = new StepHandler (null, handlerConfig)
    try {
      transf = IdentityTransformPool.borrowTransformer
      transf.transform (new StreamSource(xmlChecker), new SAXResult(stepHandler))
      stepHandler.step
    } finally {
      if (transf != null) IdentityTransformPool.returnTransformer(transf)
    }
  }

  val validator = Validator("MyInstTestValidator", steps, handlerConfig)

  //
  //  Reinitialize the handlers so that we can connect the state
  //  machine with the checker format manually.
  //

  handlerConfig.resultHandler.destroy
  handlerConfig.resultHandler.init(validator, Some(xmlChecker))

  val allSteps = (xmlChecker \\ "step" \\ "@id").map (n => n.text).toList

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
