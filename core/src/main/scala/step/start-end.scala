package com.rackspace.com.papi.components.checker.step

import java.util.HashMap

import scala.util.control.Breaks._

import javax.xml.namespace.QName
import javax.xml.validation.Schema

import scala.util.matching.Regex

import com.rackspace.com.papi.components.checker.servlet._
import javax.servlet.FilterChain

//
//  The start step
//
class Start(id : String, label : String, next : Array[Step]) extends ConnectedStep(id, label, next) {
  override def checkStep(req : CheckerServletRequest, resp : CheckerServletResponse, chain : FilterChain, uriLevel : Int) : Int = uriLevel
  override val mismatchMessage : String = "Bad Start Node?"
}

//
//  The accept state, send the request over
//
class Accept(id : String, label : String) extends Step(id, label) {
  override def check(req : CheckerServletRequest,
                     resp : CheckerServletResponse,
                     chain : FilterChain, uriLevel : Int,
                     stepCount : Int ) : Option[Result] = {
    //
    //  For now, accept always send out to the chain
    //
    chain.doFilter(req, resp)

    //
    //  Send request...
    //
    return Some(new AcceptResult("", uriLevel, id, stepCount ))
  }
}

//
//  The URLFail state, return a 404
//
class URLFail(id : String, label : String) extends Step(id, label) {
  override def check(req : CheckerServletRequest,
                     resp : CheckerServletResponse,
                     chain : FilterChain, uriLevel : Int,
                     stepCount : Int) : Option[Result] = {
    //
    //  If there is stuff in the path, then this error is
    //  applicable. Generate the error, commit the message. No URI
    //  stuff, then return None.
    //
    var result : Option[URLFailResult] = None

    if (uriLevel < req.URISegment.size) {
      val path = (for (i <- 0 until (uriLevel)) yield req.URISegment(i)).foldLeft("")(_ + "/" + _)+"/{"+req.URISegment(uriLevel)+"}"
      val ufr = new URLFailResult("Resource not found: "+path, uriLevel, id, stepCount )
      result = Some(ufr)
    }

    return result
  }
}

//
//  Like URLFail, but fails only if the current uri path is not matched
//  against the uri regex
//
class URLFailMatch(id : String, label : String, val uri : Regex) extends URLFail(id, label) {
  override def check(req : CheckerServletRequest,
                     resp : CheckerServletResponse,
                     chain : FilterChain,
                     uriLevel : Int,
                     stepCount : Int ) : Option[Result] = {
    var result : Option[Result] = super.check (req, resp, chain, uriLevel, stepCount)
    if (result != None) {
      req.URISegment(uriLevel) match {
        case uri() => result = None
        case _ => result = Some(new URLFailResult (result.get.message+". The URI segment does not match the pattern: '"+uri+"'", uriLevel, id, stepCount )) // Augment our parents result with match info
      }
    }
    result
  }
}

//
//  Like URLFailMatch, but fails only if the current uri path is not
//  matched against any of a number of simple XSD types
//
class URLFailXSDMatch(id : String, label : String, uri : Regex, types : Array[QName], schema : Schema) extends URLFailMatch(id, label, uri) {
  //
  //  XSD validators
  //
  val validators : Array[XSDStringValidator] = types.map (t => new XSDStringValidator(t, schema, id))

  override def check(req : CheckerServletRequest,
                     resp : CheckerServletResponse,
                     chain : FilterChain,
                     uriLevel : Int,
                     stepCount : Int) : Option[Result] = {
    var result : Option[Result] = super.check (req, resp, chain, uriLevel, stepCount)
    if (result != None) {
      val in = req.URISegment(uriLevel)
      val errors = for (validator <- validators) yield {
        val e = validator.validate(in)
        if (e == None) return None
        e.get.getMessage()
      }

      val message = errors.foldLeft(result.get.message)(_ + " and "+_)
      result = Some(new URLFailResult (message, uriLevel, id, stepCount))
    }
    result
  }
}

//
//  Fail with a 415 if the request content type doesn't match one of
//  the accepted types
//
class ReqTypeFail(id : String, label : String, val types : Regex) extends Step(id, label) {
  override def check(req : CheckerServletRequest,
                     resp : CheckerServletResponse,
                     chain : FilterChain,
                     uriLevel : Int,
                     stepCount : Int ) : Option[Result] = {
    var result : Option[BadMediaTypeResult] = None
    req.getContentType() match {
      case types() => result = None
      case _ => result = Some(new BadMediaTypeResult("The content type did not match the pattern: '" +
                                                      types.toString.replaceAll("\\(\\?i\\)","")+"'",
                                                      uriLevel, id, stepCount))
    }
    result
  }
}

//
//  Like URLFail, but fails only if the current uri path is not
//  matched by any of the simple XSD types.
//
class URLFailXSD(id : String, label : String, types : Array[QName], schema : Schema) extends URLFail(id, label) {
  //
  //  XSD validators
  //
  val validators : Array[XSDStringValidator] = types.map (t => new XSDStringValidator(t, schema, id))


  override def check(req : CheckerServletRequest,
                     resp : CheckerServletResponse,
                     chain : FilterChain,
                     uriLevel : Int,
                     stepCount : Int ) : Option[Result] = {
    var result : Option[Result] = super.check (req, resp, chain, uriLevel, stepCount)
    if (result != None) {
      val in = req.URISegment(uriLevel)
      val errors = for (validator <- validators) yield {
        val e = validator.validate(in)
        if (e == None) return None
        e.get.getMessage()
      }

      val message = errors.foldLeft(result.get.message)(_ + " "+_)
      result = Some(new URLFailResult (message, uriLevel, id, stepCount ))
    }
    result
  }
}

//
// Method fail state
//
class MethodFail(id : String, label : String) extends Step(id, label) {
  private val allowHeaders = new HashMap[String,String](1)
  allowHeaders.put("Allow","")

  override def check(req : CheckerServletRequest,
                     resp : CheckerServletResponse,
                     chain : FilterChain,
                     uriLevel : Int,
                     stepCount : Int) : Option[Result] = {
    //
    //  If there is URL stuff return NONE.  Otherwise generate an
    //  error, commit the message.
    //
    var result : Option[MethodFailResult] = None

    if (uriLevel >= req.URISegment.size) {
      val mfr = new MethodFailResult("Bad method: "+req.getMethod(),
                                     uriLevel,
                                     id,
                                     stepCount,
                                     allowHeaders.clone().asInstanceOf[java.util.Map[String,String]])
      result = Some(mfr)
    }

    return result
  }
}

//
//  Like MethodFail, but fails only if the current method is not
//  matched against the uri regex
//
class MethodFailMatch(id : String, label : String, val method : Regex) extends MethodFail(id, label) {
  private val allowHeaders = new HashMap[String, String](1)
  allowHeaders.put("Allow", method.toString.replaceAll("\\|",", "))

  override def check(req : CheckerServletRequest,
                     resp : CheckerServletResponse,
                     chain : FilterChain,
                     uriLevel : Int,
                     stepCount : Int) : Option[Result] = {
    var result : Option[Result] = super.check(req, resp, chain, uriLevel, stepCount)
    if (result != None) {
      req.getMethod() match {
        case method() => result = None
        case _ => result = Some(new MethodFailResult (result.get.message+". The Method does not match the pattern: '"+method+"'",
                                                      uriLevel,
                                                      id,
                                                      stepCount,
                                                      allowHeaders.clone.asInstanceOf[java.util.Map[String,String]])) // Augment our parents result with match info
      }
    }
    result
  }
}

//
// Content fail state
//
class ContentFail(id : String, label : String) extends Step(id, label) {
  override def check(req : CheckerServletRequest,
                     resp : CheckerServletResponse,
                     chain : FilterChain,
                     uriLevel : Int,
                     stepCount : Int ) : Option[Result] = {
    //
    //  If there is a contentError in the request, return it,
    //  otherwise return NONE.
    //
    var result : Option[Result] = None

    if (req.contentError != null) {
      val msg = {
        var m = req.contentError.getMessage()
        if (m == null) {
          m = req.contentError.toString()
        }
        m
      }

      val prepend = if ( req.contentErrorCode == 400 ) "Bad Content: " else ""

      // TODO:  I'm assuming everything generated within this step is a content error
      result = Some( new BadContentResult ( prepend + msg, req.contentErrorCode, uriLevel, id, stepCount ) )
    }

    return result
  }
}
