package com.rackspace.com.papi.components.checker.step

import scala.collection.immutable.List

//
//  Base class for all checker results
//
abstract class Result(val message : String,   // A message describing the result
                      val valid : Boolean,    // Was the http req/res valid?
                      val terminal : Boolean, // Are we at a terminal node in the machine
                      val uriLevel : Int,     // The URI level at the error
                      stepId : String) {      // The ID of the machine at the error

  var stepIDs : List[String] = List[String]() // ID of the linear steps it took to get
                                              // to this result

  addStepId(stepId)

  def addStepId(stepId : String) : Unit = {
    stepIDs = stepId +: stepIDs
  }
}
abstract class ErrorResult(message : String, val code : Int, uriLevel : Int, stepId : String) extends Result(message, false, true, uriLevel, stepId)

class AcceptResult(message: String, uriLevel : Int, stepId : String)  extends Result(message, true, true, uriLevel, stepId)
class URLFailResult(message : String, uriLevel : Int, stepId : String) extends ErrorResult(message, 404, uriLevel, stepId)
class MethodFailResult(message: String, uriLevel : Int, stepId : String) extends ErrorResult(message, 405, uriLevel, stepId)
class MismatchResult(message: String, uriLevel : Int, stepId : String) extends Result(message, false, false, uriLevel, stepId)

class MultiFailResult(val fails : Array[Result], uriLevel : Int, stepId : String)
      extends Result ("Multiple possible errors", false, false, uriLevel, stepId);
