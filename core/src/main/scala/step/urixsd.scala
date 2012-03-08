package com.rackspace.com.papi.components.checker.step

import javax.xml.namespace.QName
import javax.xml.validation.Schema

import com.rackspace.com.papi.components.checker.servlet._
import javax.servlet.FilterChain

class URIXSD(id : String, label : String, simpleType : QName, schema : Schema, next : Array[Step])  extends ConnectedStep(id, label, next) {

     override val mismatchMessage : String = simpleType.toString
     val xsd = new XSDStringValidator(simpleType, schema, id)

     override def checkStep(req : CheckerServletRequest, resp : CheckerServletResponse, chan : FilterChain, uriLevel : Int) : Int = {
       var ret = -1
       if ((uriLevel < req.URISegment.size) && xsd.validate(req.URISegment(uriLevel))) {
         ret = uriLevel+1
       }
       ret
     }

}

