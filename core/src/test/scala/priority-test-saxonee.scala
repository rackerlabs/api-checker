package com.rackspace.com.papi.components.checker

import com.rackspace.cloud.api.wadl.test.SchemaAsserter

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

import scala.xml._

import org.scalatest.FunSuite

@RunWith(classOf[JUnitRunner])
class PrioritySuiteSaxonEE extends FunSuite {

  val priorityMapAsserter = new SchemaAsserter(getClass().getClassLoader().getResource("xsd/priority-map.xsd"), true)

  test("Make sure priorites validate...") {
    priorityMapAsserter.assert(XML.load(getClass().getClassLoader().getResource("xsl/priority-map.xml")))
  }

}
