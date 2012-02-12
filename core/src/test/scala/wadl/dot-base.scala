package com.rackspace.com.papi.components.checker.wadl

import com.rackspace.cloud.api.wadl.Converters._
import com.rackspace.cloud.api.wadl.test.BaseWADLSpec

class BaseDotSpec extends BaseWADLSpec {
  val builder = new WADLDotBuilder(wadl)
}
