/** *
  * Copyright 2015 Rackspace US, Inc.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */

package com.rackspace.com.papi.components.checker.handler

import javax.xml.transform.stream.StreamSource

import com.rackspace.com.papi.components.checker._
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.core.LoggerContext
import org.apache.logging.log4j.test.appender.ListAppender
import org.junit.runner.RunWith
import org.scalatest.BeforeAndAfter
import org.scalatestplus.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class ApiCoverageHandlerTest extends BaseValidatorSuite with BeforeAndAfter {
  // This logger is being used instead of the one provided with LogAssertions
  // since this is part of the functionality under test. SEE: log4j2-test.xml
  var apiCoverageLogger: ListAppender = _
  val checkerXml = new StreamSource(getClass.getResource("/xml/sharedXPath.checker").toString)
  val handlerConfig = {
    val assertHandler = new DispatchResultHandler(List[ResultHandler](
      new ConsoleResultHandler(),
      new ApiCoverageHandler(),
      new AssertResultHandler())
    )
    assertConfig.resultHandler = assertHandler
    assertConfig.checkPlainParams = true  // These allow the X-Path
    assertConfig.removeDups = false       // tests to be performed.
    assertConfig
  }
  val validator = Validator("TestValidator", checkerXml, handlerConfig)

  val good_usage16 =
    <atom:entry xmlns:atom="http://www.w3.org/2005/Atom" xmlns="http://docs.rackspace.com/usage/nova/ips" only_usage_up_down="true">
      <usage>
        <up>
          <down/>
        </up>
      </usage>
    </atom:entry>

  val good_usage17 =
    <atom:entry xmlns:atom="http://www.w3.org/2005/Atom" xmlns="http://docs.rackspace.com/event/nova/host" only_usage_up_down="true">
      <usage>
        <up>
          <down/>
        </up>
      </usage>
    </atom:entry>

  val good_rhel =
    <atom:entry xmlns:atom="http://www.w3.org/2005/Atom" xmlns="http://docs.rackspace.com/event/RHEL" only_usage="true">
      <usage/>
    </atom:entry>

  before {
    val ctx = LogManager.getContext(false).asInstanceOf[LoggerContext]
    apiCoverageLogger = ctx.getConfiguration.getAppender("api-coverage-listAppender").asInstanceOf[ListAppender].clear
  }

  List(
    ("POST",  "/nova/entries",          "application/atom+xml", good_usage16, """{"steps":["S0","d59e4","d59e5","POST_d59e5","d59e133","d59e137","d59e137W","d59e1371XPTH","d59e1372XPTH","d59e1373XPTH","d59e1374XPTH","SA"]}"""),
    ("POST",  "/nova/entries",          "application/atom+xml", good_rhel,    """{"steps":["S0","d59e4","d59e5","POST_d59e5","d59e107","d59e111","d59e111W","d59e1111XPTH","d59e1112XPTH","SA"]}"""),
    ("POST",  "/servers/entries",       "application/atom+xml", good_rhel,    """{"steps":["S0","d59e54","d59e55","POST_d59e55","d59e107","d59e111","d59e111W","d59e1111XPTH","d59e1112XPTH","SA"]}"""),
    ("POST",  "/servers/entries",       "application/atom+xml", good_usage17, """{"steps":["S0","d59e54","d59e55","POST_d59e55","d59e163","d59e167","d59e167W","d59e1671XPTH","d59e1672XPTH","d59e1673XPTH","d59e1674XPTH","SA"]}""")
  ).foreach { case (method, path, content, body, logged) =>
    test(s"For method: $method, path: $path, and content type: $content, the validation should pass and $logged should be logged to api-coverage-logger") {
      assert(validator.validate(request(method,path,content,body),response,chain).valid)

      val logEvents = apiCoverageLogger.getEvents
      assertResult(1)(logEvents.size)
      assert(logged == logEvents.get(0).getMessage.getFormattedMessage)
    }
  }

  import javax.servlet.http.HttpServletResponse.{SC_METHOD_NOT_ALLOWED, SC_NOT_FOUND, SC_UNSUPPORTED_MEDIA_TYPE} // 405, 404, 415

  List(
    ("GET",   "/nova/entries",          "application/atom+xml", good_usage16, SC_METHOD_NOT_ALLOWED,      """{"steps":["S0","d59e4","d59e5","d67e4m"]}"""),
    ("POST",  "/nova/entries",          "application/json",     good_usage16, SC_UNSUPPORTED_MEDIA_TYPE,  """{"steps":["S0","d59e4","d59e5","POST_d59e5","d59e133","d59e133rqt"]}"""),
    ("POST",  "/nova/entries/fail",     "application/atom+xml", good_usage16, SC_NOT_FOUND,               """{"steps":["S0","d59e4","d59e5","SE0"]}"""),
    ("POST",  "/nova/fail",             "application/atom+xml", good_usage16, SC_NOT_FOUND,               """{"steps":["S0","d59e4","d67e3u"]}"""),
    ("GET",   "/nova",                  "application/atom+xml", good_usage16, SC_METHOD_NOT_ALLOWED,      """{"steps":["S0","d59e4","SE1"]}"""),
    ("GET",   "/",                      "application/atom+xml", good_usage16, SC_METHOD_NOT_ALLOWED,      """{"steps":["S0","SE1"]}"""),
    ("GET",   "/servers",               "application/atom+xml", good_usage17, SC_METHOD_NOT_ALLOWED,      """{"steps":["S0","d59e54","SE1"]}"""),
    ("POST",  "/servers/fail",          "application/atom+xml", good_usage17, SC_NOT_FOUND,               """{"steps":["S0","d59e54","d67e19u"]}"""),
    ("POST",  "/servers/entries/fail",  "application/atom+xml", good_usage17, SC_NOT_FOUND,               """{"steps":["S0","d59e54","d59e55","SE0"]}"""),
    ("GET",   "/servers/entries",       "application/atom+xml", good_usage17, SC_METHOD_NOT_ALLOWED,      """{"steps":["S0","d59e54","d59e55","d67e20m"]}"""),
    ("POST",  "/servers/entries",       "application/json",     good_usage17, SC_UNSUPPORTED_MEDIA_TYPE, """{"steps":["S0","d59e54","d59e55","POST_d59e55","d59e163","d59e163rqt"]}"""),
    ("POST",  "/fail",                  "application/atom+xml", good_usage17, SC_NOT_FOUND, """{"steps":["S0","d67e2u"]}""")
  ).foreach { case (method, path, content, body, status, logged) =>
    test(s"For method: $method, path: $path, and content type: $content, the validation should fail, status should be: $status, and $logged should be logged to api-coverage-logger") {
      assertResultFailed(validator.validate(request(method,path,content,body),response,chain), status)

      val logEvents = apiCoverageLogger.getEvents
      assertResult(1)(logEvents.size)
      assert(logged == logEvents.get(0).getMessage.getFormattedMessage)
    }
  }

  List(
    ("POST",  "/nova/entries",          "application/json",     good_rhel,    SC_UNSUPPORTED_MEDIA_TYPE,  """{"steps":["S0","d59e4","d59e5","POST_d59e5","d59e133","d59e133rqt"]}"""),
    ("POST",  "/servers/entries",       "application/json",     good_rhel,    SC_UNSUPPORTED_MEDIA_TYPE, """{"steps":["S0","d59e54","d59e55","POST_d59e55","d59e163","d59e163rqt"]}""")
  ).foreach { case (method, path, content, body, status, logged) =>
    test(s"For method: $method, path: $path, and content type: $content, the validation should fail, status should be: $status, and $logged, the first failure of that priority, should be logged to api-coverage-logger") {
      assertResultFailed(validator.validate(request(method,path,content,body),response,chain), status)

      val logEvents = apiCoverageLogger.getEvents
      assertResult(1)(logEvents.size)
      assert(logged == logEvents.get(0).getMessage.getFormattedMessage)
    }
  }
}
