package com.rackspace.com.papi.components.checker.step

import javax.xml.namespace.QName
import javax.xml.validation.Schema

import com.rackspace.com.papi.components.checker.servlet._

class URIXSD(id : String, label : String, sType : QName, sch : Schema, next : Array[Step])  extends ConnectedStep(id, label, next)
   with XSDStringValidator {

     override val mismatchMessage : String = sType.toString;
     override val elementName : String = id
     override val simpleType : QName = sType
     override val schema : Schema = sch

     override def checkStep(req : CheckerServletRequest, resp : CheckerServletResponse, uriLevel : Int) : Int = {
       var ret = -1
       if ((uriLevel < req.URISegment.size) && validate(req.URISegment(uriLevel))) {
         ret = uriLevel+1
       }
       ret
     }
}
