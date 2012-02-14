package com.rackspace.com.papi.components.checker.step

//
//  Base class for all checker results
//
abstract class CheckerResult(val message : String, val valid : Boolean, val code : Int)

object AcceptResult extends CheckerResult("Valid", true, 200)
class URLFailResult(message : String) extends CheckerResult(message, false, 404)
class MethodFailResult(message: String) extends CheckerResult(message, false, 405)
class ExpectingResult(message: String, code : Int) extends CheckerResult(message, false, code)
class MultiFailResult(val fails : Array[CheckerResult]) extends CheckerResult ("Multiple possible errors",false,400)
