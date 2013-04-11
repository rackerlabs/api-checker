package com.rackspace.com.papi.components.checker.step

import javax.xml.namespace.QName
import javax.xml.validation.Schema

import com.rackspace.com.papi.components.checker.servlet._
import javax.servlet.FilterChain
import collection.mutable.ListBuffer

class URIXSD(id : String, label : String, simpleType : QName, schema : Schema, next : Array[Step])  extends ConnectedStep(id, label, next) {

     override val mismatchMessage : String = simpleType.toString
     val xsd = new XSDStringValidator(simpleType, schema, id)

     override def check(req : CheckerServletRequest,
                        resp : CheckerServletResponse,
                        chain : FilterChain,
                        uriLevel : Int,
                        stepCount : Int ) : ListBuffer[Result] = {

       var buffer = new ListBuffer[Result]

       if (uriLevel < req.URISegment.size) {

         val error = xsd.validate(req.URISegment(uriLevel))

         if (error != None) {

           buffer += new MismatchResult(error.get.getMessage(), uriLevel, id, stepCount)

         } else {

           buffer = nextStep (req, resp, chain, uriLevel + 1, stepCount )

           if (!buffer.isEmpty) {

             buffer( 0 ).addStepId( id )
           }
         }
       } else {

         buffer += new MismatchResult(mismatchMessage, uriLevel, id, stepCount )
       }

       buffer
     }
}

