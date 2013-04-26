package com.rackspace.com.papi.components.checker.step

import javax.xml.namespace.QName
import javax.xml.validation.Schema

import com.rackspace.com.papi.components.checker.servlet._
import javax.servlet.FilterChain

class URIXSD(id : String, label : String, simpleType : QName, schema : Schema, next : Array[Step])  extends ConnectedStep(id, label, next) {

     override val mismatchMessage : String = simpleType.toString
     val xsd = new XSDStringValidator(simpleType, schema, id)

     override def check(req : CheckerServletRequest,
                        resp : CheckerServletResponse,
                        chain : FilterChain,
                        uriLevel : Int,
                        stepCount : Int ) : Option[Result] = {
       var result : Option[Result] = None
       if (uriLevel < req.URISegment.size) {
         val error = xsd.validate(req.URISegment(uriLevel))
         if (error != None) {
           result = Some(new MismatchResult(error.get.getMessage(), uriLevel, id, stepCount))
         } else {
           val results : Array[Result] = nextStep (req, resp, chain, uriLevel + 1, stepCount )
           results.size match {
             case 0 =>
               result = None
             case 1 =>
               results(0).addStepId(id)
               result = Some(results(0))
             case _ =>
               result = Some(new MultiFailResult (results))
           }
         }
       } else {
         result = Some( new MismatchResult( mismatchMessage, uriLevel, id, stepCount ) )
       }
       result
     }
}

