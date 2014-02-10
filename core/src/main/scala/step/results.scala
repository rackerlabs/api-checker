package com.rackspace.com.papi.components.checker.step
import java.util.Map
import java.util.HashMap
import scala.collection.immutable.List
import scala.collection.mutable.PriorityQueue

//
//  Base class for all checker results
//
abstract class Result(private val messageP : String,   // A message describing the result
                      private val validP : Boolean,    // Was the http req/res valid?
                      private val terminalP : Boolean, // Are we at a terminal node in the machine
                      private val uriLevelP : Int,     // The URI level at the error
                      val stepId : String,
                      private val stepCountP : Int) extends Ordered[Result] {      // The ID of the machine at the error

  private var stepIDsP : List[String] = List[String]() // ID of the linear steps it took to get
  // to this result

  def addStepId(stepId : String) : Unit = {
    stepIDsP = stepId +: stepIDsP
  }

  addStepId(stepId)

  //
  // In MultiErrorResult, we forward the following calls to another object.  You cannot override val with a def call
  // since val implies a stable & immutable guarantee which def does not contain.
  //
  // We declare private vals and manually create accessors
  //
  def message = messageP
  def valid = validP
  def terminal = terminalP
  def uriLevel = uriLevelP
  def stepCount = stepCountP
  def stepIDs = stepIDsP

  def allResults : Traversable[Result] = List( this )

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

class ErrorResult(private val messageP : String,
                  private val codeP : Int,
                  uriLevel : Int,
                  stepId : String,
                  stepCount : Int,
                  private val headersP : Map[String,String] = new HashMap()) extends Result(messageP, false, true, uriLevel,
                                                                                   stepId, stepCount) {

  //
  // In MultiErrorResult, we forward the following calls to another object.  You cannot override val with a def call
  // since val implies a stable & immutable guarentee which def does not contain.
  //
  // We declare private vals and manually create accessors
  //
  def code = codeP
  def headers = headersP

  override def toString : String = path+" "+code+" : "+message
  override def cmpString : String = super.cmpString+" "+code
}

class AcceptResult(message: String,
                   uriLevel : Int,
                   stepId : String,
                   stepCount : Int)  extends Result(message, true, true, uriLevel, stepId, stepCount)

class BadContentResult(message : String,
                       codeP : Int = 400,
                       uriLevel : Int,
                       stepId : String,
                       stepCount : Int) extends ErrorResult(message, codeP, uriLevel, stepId, stepCount)

class URLFailResult(message : String,
                    uriLevel : Int,
                    stepId : String,
                    stepCount: Int) extends ErrorResult(message, 404, uriLevel, stepId, stepCount)

class MethodFailResult(message: String,
                       uriLevel : Int,
                       stepId : String,
                       stepCount : Int,
                       headers : Map[String,String]) extends ErrorResult(message, 405, uriLevel, stepId,
                                                                         stepCount, headers )

class BadMediaTypeResult(message: String,
                         uriLevel : Int,
                         stepId : String,
                         stepCount : Int) extends ErrorResult(message, 415, uriLevel, stepId, stepCount)

class MismatchResult(message: String,
                     uriLevel : Int,
                     stepId : String,
                     stepCount : Int) extends Result(message, false, false, uriLevel, stepId, stepCount) {
  override protected val startPath = "("
  override protected val endPath = ")"
}


class NoResultsException( val message : String ) extends Exception( message )

//
// This class is a wrapper around priorityqueue of several results, delegating the Result methods to the Result
// at the head of the data structure.
//
class MultiFailResult(val fails : Array[Result], stepId : String) extends ErrorResult ( "Multiple possible errors", -1, -1, stepId, -1 ) {

  if ( fails.isEmpty ) throw new NoResultsException( "Input array must be non-empty." )

  private val results = new PriorityQueue[Result]()

  results ++= fails

  //
  // delegate all method calls to the Result with the highest priority
  //
  override def message = results.head.message
  override def uriLevel = results.head.uriLevel
  override def valid = results.head.valid
  override def terminal = results.head.terminal
  override def stepCount = results.head.stepCount

  override def code = results.head match {
    case e : ErrorResult => e.code
  }

  override def headers = results.head match {
    case e : ErrorResult => e.headers
  }

  //
  // This order value is the value of the result in the priorityqueue
  //
  override def orderValue() = results.head.orderValue

  //
  // Define new Traversable instance which calls foreach on each MultiFailResult
  // and the function on each non-MultiFailResult (i.e., each leaf node in the
  // Result-tree).  The main result is encountered first in this traversal.
  //
  override def allResults : Traversable[Result] = {

    new Traversable[Result] {

      def foreach[U]( f: Result => U ) = {
        results.foreach(
          ( r : Result) => {
            r match {
              case m : MultiFailResult => r.allResults.foreach( f )
              case r : Result => f( r )
            }
          }
        )
      }
    }
  }

  override protected val startPath = "{"
  override protected val endPath = "}"

  private def selectId (r : Result) : String = {
    r match {
      case m : MultiFailResult => selectId(m.results.head)
      case r : Result => r.stepId
    }
  }

  override def path : String = startPath+stepIDs.reduceLeft(_+" "+_)+" " + fails.map( _.path ).reduceLeft( _ + " " + _ ) + endPath+"*"+selectId(this)+"*"
}
