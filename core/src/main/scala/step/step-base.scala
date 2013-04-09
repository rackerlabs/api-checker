package com.rackspace.com.papi.components.checker.step

import scala.collection.mutable.ListBuffer

import com.rackspace.com.papi.components.checker.servlet._
import javax.servlet.FilterChain
import collection.mutable.PriorityQueue

//
//  Base class for all other steps
//
abstract class Step(val id : String, val label : String) {

  //
  //  Checks the step at the given URI level.
  //
  // stepCount : default value is 1 is convenience for first step
  def check(req : CheckerServletRequest, resp : CheckerServletResponse, chain : FilterChain, uriLevel : Int, stepCount : Int = 1 ) : Option[Result]

  //
  //  Checks the step at the beginnig of the PATH
  //
  // TODO:  is this actually called?  Wouldn't this recursive call never end?
  //
  def check(req : CheckerServletRequest, resp : CheckerServletResponse, chain : FilterChain) : Option[Result] = {
    return check(req,resp,chain)
  }
}

//
//  Steps that ore connected to other steps
//
abstract class ConnectedStep(id : String, label : String, val next : Array[Step]) extends Step (id, label) {

  //
  //  Check the currest step.  If the step is a match return the next
  //  uriLevel.  If not return -1.
  //
  def checkStep(req : CheckerServletRequest, resp : CheckerServletResponse, chain : FilterChain, uriLevel : Int) : Int = -1

  //
  //  The error message when there is a step mismatch.
  //
  val mismatchMessage : String = "Step Mismatch"

  //
  //  Go to the next step.
  //
  def nextStep (req : CheckerServletRequest, resp : CheckerServletResponse, chain : FilterChain, uriLevel : Int, stepCount : Int ) : Array[Result] = {
    val resultBuffer = new ListBuffer[Result]
    for (i <- 0 to next.length-1) {
      val oresult = next(i).check(req, resp, chain, uriLevel, stepCount + 1 )
      if (oresult.isDefined) {
        val result = oresult.get
        if (result.valid) {
          return Array(result)
        } else {
          resultBuffer += result
        }
      }
    }
    return resultBuffer.toArray
  }

  //
  //  Check this step, if successful, check next relevant steps.
  //
  override def check(req : CheckerServletRequest, resp : CheckerServletResponse, chain : FilterChain, uriLevel : Int, stepCount : Int ) : Option[Result] = {
    var result : Option[Result] = None
    val nextURILevel = checkStep(req, resp, chain, uriLevel )

    if (nextURILevel != -1) {

      var results = nextStep (req, resp, chain, nextURILevel, stepCount )

      results.size match {
        case 0 =>
          result = None

        case 1 =>
          results(0).addStepId(id)
          result = Some(results(0))

        case _ =>

          // get the highest priority result
          val pQueue = new PriorityQueue[Result]
          pQueue.enqueue( results: _* )

          result = Some( pQueue.dequeue )
      }

    } else {
      result = Some(new MismatchResult(mismatchMessage, uriLevel, id, stepCount))
    }

    return result
  }
}
