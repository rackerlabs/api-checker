package com.rackspace.com.papi.components.checker.step

//
//  Base class for all checker results
//
abstract class CheckerResult(val message : String, val valid : Boolean)
abstract class ErrorResult(message : String, val code : Int) extends CheckerResult(message, false)

object AcceptResult extends CheckerResult("Valid", true)

class URLFailResult(message : String) extends ErrorResult(message, 404)
class MethodFailResult(message: String) extends ErrorResult(message, 405)
class MismatchResult(message: String) extends CheckerResult(message, false)
class MultiFailResult(val fails : Array[CheckerResult]) extends CheckerResult ("Multiple possible errors", false)
