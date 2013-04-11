package com.rackspace.com.papi.components.checker.step

import scala.collection.mutable.ListBuffer

import com.rackspace.com.papi.components.checker.servlet._
import javax.servlet.FilterChain

//
//  Base class for all other steps
//
abstract class Step(val id : String, val label : String) {

  //
  //  Checks the step at the given URI level.
  //
  // stepCount : default value is 1 is convenience for first step
  def check(req : CheckerServletRequest, resp : CheckerServletResponse, chain : FilterChain, uriLevel : Int, stepCount : Int = 1 ) : ListBuffer[Result]

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
  def nextStep (req : CheckerServletRequest, resp : CheckerServletResponse, chain : FilterChain, uriLevel : Int, stepCount : Int ) : ListBuffer[Result] = {

    val buffer = new ListBuffer[Result]

    for (i <- 0 to next.length-1) {

      val moreRes = next(i).check(req, resp, chain, uriLevel, stepCount + 1 )
      if (!moreRes.isEmpty && moreRes( 0 ).valid ) {

        buffer.clear()
        buffer += moreRes( 0 )
        return buffer

      } else {

        buffer ++= moreRes
      }
    }

    buffer
  }

  //
  //  Check this step, if successful, check next relevant steps.
  //
  override def check(req : CheckerServletRequest, resp : CheckerServletResponse, chain : FilterChain, uriLevel : Int, stepCount : Int ) : ListBuffer[Result] = {

    val nextURILevel = checkStep(req, resp, chain, uriLevel )

    if (nextURILevel != -1) {

      val buffer = nextStep (req, resp, chain, nextURILevel, stepCount )

      if ( !buffer.isEmpty ) {

        buffer(0).addStepId( id )
      }

      return buffer
    } else {

      val buffer = new ListBuffer[Result]
      buffer += new MismatchResult(mismatchMessage, uriLevel, id, stepCount)
      return buffer
    }
  }
}
