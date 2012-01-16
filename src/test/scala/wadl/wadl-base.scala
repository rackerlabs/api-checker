package com.rackspace.com.papi.components.checker.wadl


import com.rackspace.cloud.api.wadl.test.BaseWADLSpec

class BaseCheckerSpec extends BaseWADLSpec {
  val builder = new WADLCheckerBuilder(wadl)
}
