package com.rackspace.com.papi.components.checker.step

//
//  Base class for all checker results
//
abstract class CheckerResult(val message : String, val valid : Boolean, val code : Int)

class AcceptResult extends CheckerResult("Valid", true, 200)

object AcceptResult {
  val acceptSingleton = new AcceptResult
  def apply = acceptSingleton
}

class URLFailResult(message : String) extends CheckerResult(message, false, 404)
class MethodFailResult(message: String) extends CheckerResult(message, false, 405)
