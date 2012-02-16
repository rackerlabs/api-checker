package com.rackspace.com.papi.components.checker.step

//
//  Base class for all checker results
//
abstract class Result(val message : String, val valid : Boolean, val uriLevel : Int)
abstract class ErrorResult(message : String, val code : Int, uriLevel : Int) extends Result(message, false, uriLevel)

object AcceptResult extends Result("Valid", true, -1)

class URLFailResult(message : String, uriLevel : Int) extends ErrorResult(message, 404, uriLevel)
class MethodFailResult(message: String, uriLevel : Int) extends ErrorResult(message, 405, uriLevel)
class MismatchResult(message: String, uriLevel : Int) extends Result(message, false, uriLevel)
class MultiFailResult(val fails : Array[Result], uriLevel : Int) extends Result ("Multiple possible errors", false, uriLevel)
