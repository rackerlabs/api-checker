package com.rackspace.com.papi.components.checker.util

import java.util.Enumeration
import javax.servlet.http.HttpServletRequest
import scala.collection.JavaConversions._

object HeaderUtil {

  def getHeader (request : HttpServletRequest, name : String) = {
    val value = request.getHeader(name)
    value match {
      case null => null
      case _ =>  name.split(",")(0).trim
    }
  }

  def getHeaders (request : HttpServletRequest, name : String) : Enumeration[String] = {
    val headers = request.getHeaders(name)
    headers match {
      case null => List[String]().iterator
      case _ => var list : List[String] = List()
        headers.foreach(i => list = list ++ i.split(",").map(j => j.trim))
        list.iterator
    }
  }
}
