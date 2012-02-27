package com.rackspace.com.papi.components.checker.wadl

import com.rackspace.cloud.api.wadl.Converters._
import com.rackspace.cloud.api.wadl.test.BaseWADLSpec

class BaseStepSpec extends BaseWADLSpec {
  var builder = new StepBuilder(wadl)
}
