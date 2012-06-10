package com.rackspace.com.papi.components.checker.step

import com.rackspace.com.papi.components.checker.servlet._
import javax.servlet.FilterChain

class WellFormedJSON(id : String, label : String, next : Array[Step]) extends ConnectedStep(id, label, next) {
  override val mismatchMessage : String = "The JSON is not well formed!"

  override def checkStep(req : CheckerServletRequest, resp : CheckerServletResponse, chain : FilterChain, uriLevel : Int) : Int = {
    //
    //  TODO: Finish this implementation.  In the meantime we always
    //  assume good JSON.
    //
    uriLevel
  }
}
