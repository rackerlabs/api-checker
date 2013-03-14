package com.rackspace.com.papi.components.checker.step
import java.util.Map
import java.util.HashMap
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

class ErrorResult(message : String, val code : Int, uriLevel : Int, stepId : String,
                  val headers : Map[String,String] = new HashMap()) extends Result(message, false, true, uriLevel, stepId) {
  override def toString : String = path+" "+code+" : "+message
  override def cmpString : String = super.cmpString+" "+code
}

class AcceptResult(message: String, uriLevel : Int, stepId : String)  extends Result(message, true, true, uriLevel, stepId)
class BadContentResult(message : String, uriLevel : Int, stepId : String) extends ErrorResult(message, 400, uriLevel, stepId)
class URLFailResult(message : String, uriLevel : Int, stepId : String) extends ErrorResult(message, 404, uriLevel, stepId)
class MethodFailResult(message: String, uriLevel : Int, stepId : String, headers : Map[String,String])
      extends ErrorResult(message, 405, uriLevel, stepId, headers)
class BadMediaTypeResult(message: String, uriLevel : Int, stepId : String) extends ErrorResult(message, 415, uriLevel, stepId)
class MismatchResult(message: String, uriLevel : Int, stepId : String) extends Result(message, false, false, uriLevel, stepId) {
  override protected val startPath = "("
  override protected val endPath = ")"
}

class MultiFailResult(val fails : Array[Result], uriLevel : Int, stepId : String)
      extends Result ("Multiple possible errors", false, false, uriLevel, stepId)
{
  override protected val startPath = "{"
  override protected val endPath = "}"

  override def path : String = startPath+stepIDs.reduceLeft(_+" "+_)+" "+(for {f <- fails} yield f.path).reduceLeft(_+" "+_)+endPath

  override def toString : String = {
    reduce match {
      case Some(e : ErrorResult) => path+" "+e.code+" : "+e.message
      case Some(r : Result) => path
      case None => path
    }
  }

  override def cmpString : String = {
    val cmpStrings : Array[String]= for {f <- fails} yield f.cmpString
    cmpStrings.reduceLeft(_+" "+_)+" "+uriLevel+" "+stepId
  }

  //
  //  Pick a single fail result out of the possible set of failers.
  //
  def reduce : Option[Result] = {
    fails.foreach (res =>
      {
        res match {
          case f : MultiFailResult =>
            val red = f.reduce
            if (red != None) { return red }
          case other : Result =>
            if (other.terminal) { return Some(other) }
        }
      })
    None
  }
}
