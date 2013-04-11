package com.rackspace.com.papi.components.checker.step

import java.util.Map
import java.util.HashMap
import scala.collection.immutable.List
import collection.mutable.ListBuffer
//
//  Base class for all checker results
//
abstract class Result(val message : String,   // A message describing the result
                      val valid : Boolean,    // Was the http req/res valid?
                      val terminal : Boolean, // Are we at a terminal node in the machine
                      val uriLevel : Int,     // The URI level at the error
                      stepId : String,        // The ID of the machine at the error
                      val stepCount : Int,    // count of of many steps were checked for this result
                      val code : Int = -1,
                      val headers : Map[String,String] = new HashMap() ) extends Ordered[Result] {

  //
  // Results with less priority than the finally selected Result
  //
  val otherResults = new ListBuffer[Result]

  //
  // Orders Results by the most appropriate Result value to the least.  The ordering is
  // dictated as per the match statement in orderValue().
  //
  // Within each type, the most appropriate is assumed to be the result whose path
  // is the longest in the automaton.
  //
  def compare( o : Result ) = {

    val order = orderValue() - o.orderValue()

     if ( order != 0 ) {

       order
     }
     else {

       stepCount - o.stepCount
     }
  }


  //
  // Declares the ordering for the different Result types.  The bigger number
  // has the highest priority.  If Result type not found in match statement,
  // gives it least priority.
  //
  def orderValue() = {

    this match {

      case _ : AcceptResult => 100
      case _ : BadContentResult => 90
      case _ : BadMediaTypeResult => 80
      case _ : MethodFailResult => 70
      case _ : URLFailResult => 60
      case _ : MismatchResult => 50
      case _ => 0
    }
  }

  var stepIDs : List[String] = List[String]() // ID of the linear steps it took to get
                                              // to this result

  addStepId(stepId)

  def addStepId(stepId : String) : Unit = {
    stepIDs = stepId +: stepIDs
  }

  protected val startPath = "["
  protected val endPath = "]"

  def path : String = startPath+stepIDs.reduceLeft(_+" "+_)+endPath
  def cmpString : String = message+" "+path+" "+terminal+" "+uriLevel
  override def toString : String = path
  override def hashCode : Int = cmpString.hashCode
  override def equals (any : Any) : Boolean = {
    any match {
      case r : Result => this.hashCode() == r.hashCode()
      case _ => false
    }
  }
}

class ErrorResult(message : String,
                  val codeP : Int,
                  uriLevel : Int,
                  stepId : String,
                  stepCount : Int,
                  val headersP : Map[String,String] = new HashMap()) extends Result(message, false, true, uriLevel,
                                                                                    stepId, stepCount, codeP, headersP) {

  override def toString : String = path+" "+code+" : "+message
  override def cmpString : String = super.cmpString+" "+code
}

class AcceptResult(message: String,
                   uriLevel : Int,
                   stepId : String,
                   stepCount : Int )  extends Result(message, true, true, uriLevel, stepId, stepCount)

class BadContentResult(message : String,
                       codeP : Int = 400 : Int,
                       uriLevel : Int,
                       stepId : String,
                       stepCount : Int ) extends ErrorResult(message, codeP, uriLevel, stepId, stepCount )

class URLFailResult(message : String,
                    uriLevel : Int,
                    stepId : String,
                    stepCount : Int ) extends ErrorResult(message, 404, uriLevel, stepId, stepCount )

class MethodFailResult(message: String,
                       uriLevel : Int,
                       stepId : String,
                       stepCount : Int,
                       headers : Map[String,String]) extends ErrorResult(message, 405, uriLevel, stepId, stepCount, headers)

class BadMediaTypeResult(message: String,
                         uriLevel : Int,
                         stepId : String,
                         stepCount : Int ) extends ErrorResult(message, 415, uriLevel, stepId, stepCount )

class MismatchResult(message: String,
                     uriLevel : Int,
                     stepId : String,
                     stepCount : Int ) extends Result(message, false, false, uriLevel, stepId, stepCount ) {

  override protected val startPath = "("
  override protected val endPath = ")"
}
