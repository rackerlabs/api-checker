package com.rackspace.com.papi.components.checker.step

//
//  Base class for all checker results
//
abstract class Result(val message : String, val valid : Boolean)
abstract class ErrorResult(message : String, val code : Int) extends Result(message, false)

object AcceptResult extends Result("Valid", true)

class URLFailResult(message : String) extends ErrorResult(message, 404)
class MethodFailResult(message: String) extends ErrorResult(message, 405)
class MismatchResult(message: String) extends Result(message, false)
class MultiFailResult(val fails : Array[Result]) extends Result ("Multiple possible errors", false)
