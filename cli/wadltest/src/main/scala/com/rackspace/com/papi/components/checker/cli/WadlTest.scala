/***
 *   Copyright 2015 Rackspace US, Inc.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package com.rackspace.com.papi.components.checker.cli

import java.io.File
import javax.xml.transform._
import javax.xml.transform.stream._

import com.rackspace.com.papi.components.checker.handler._
import com.rackspace.com.papi.components.checker.util.URLResolver
import com.rackspace.com.papi.components.checker.{Config, Validator}
import org.clapper.argot.ArgotConverters._
import org.clapper.argot.{ArgotParser, ArgotUsageException}
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.servlet.{FilterMapping, ServletContextHandler}

object WadlTest {
  val MAX_CONSOLE_WIDTH = 100
  val DEFAULT_NAME = "Test_Validator"
  val DEFAULT_PORT = 9191

  val title = getClass.getPackage.getImplementationTitle

  val version = getClass.getPackage.getImplementationVersion

  val parser = new ArgotParser("wadltest", preUsage=Some(s"$title v$version"))

  val removeDups = parser.flag[Boolean] (List("d", "remove-dups"),
                                         "Remove duplicate nodes. Default: false")

  val raxRoles = parser.flag[Boolean] (List("r", "rax-roles"),
                                         "Enable Rax-Roles extension. Default: false")

  val raxRolesMask403 = parser.flag[Boolean] (List("M", "rax-roles-mask-403s"),
                                              "When Rax-Roles is enable mask 403 errors with 404 or 405s. Default: false")

  val wellFormed = parser.flag[Boolean] (List("w", "well-formed"),
                                         "Add checks to ensure that XML and JSON are well formed. Default: false")

  val joinXPaths = parser.flag[Boolean] (List("j", "join-xpaths"),
                                         "Join multiple XPath and XML well-formed checks into a single check. Default: false")

  val xsdGrammarTransform = parser.flag[Boolean] (List("g", "xsd-grammar-transform"),
                                                  "Transform the XML after validation, to fill in things like default values etc. Default: false")

  val preserveRequestBody = parser.flag[Boolean] (List("b", "preserve-req-body"),
                                              "Ensure that the request body is preserved after validating the request.")

  val xsdCheck = parser.flag[Boolean] (List("x", "xsd"),
                                         "Add checks to ensure that XML validates against XSD grammar Default: false")

  val jsonCheck = parser.flag[Boolean] (List("J", "json"),
                                         "Add checks to ensure that JSON validates against JSON Schema grammar Default: false")

  val element = parser.flag[Boolean] (List("l", "element"),
                                         "Add checks to ensure that XML requests use the correct element : false")

  val header = parser.flag[Boolean] (List("H", "header"),
                                         "Add checks to ensure that required headers are passed in: false")

  val setDefaults = parser.flag[Boolean] (List("s", "setParamDefaults"),
                                          "Fill in required parameters if a default value is specified Default: false")

  val plainParam = parser.flag[Boolean] (List("p", "plain"),
                                         "Add checks for plain parameters : false")

  val preProc  = parser.flag[Boolean] (List("P", "disable-preproc-ext"),
                                       "Disable preprocess extension : false")

  val ignoreXSD  = parser.flag[Boolean] (List("i", "disable-ignore-xsd-ext"),
                                         "Disable Ignore XSD  extension : false")

  val ignoreJSON  = parser.flag[Boolean] (List("I", "disable-ignore-json-ext"),
                                          "Disable Ignore JSON Schema  extension : false")

  val message  = parser.flag[Boolean] (List("m", "disable-message-ext"),
                                         "Disable Message extension : false")

  val captureHeader = parser.flag[Boolean] (List("c", "disable-capture-header-ext"),
                                            "Disable capture header extension : false")

  val anyMatch  = parser.flag[Boolean] (List("a", "disable-any-match"),
                                            "Disable any match extension : false")

  val warnHeaders = parser.flag[Boolean] (List("W", "disable-warn-headers"),
                                          "Disable warn headers : false")

  val warnAgent = parser.option[String] (List("A", "warn-agent"), "agent-name",
                                            "The name of the agent used in WARNING headers. Default: -")

  val xslEngine = parser.option[String] (List("E", "xsl-engine"), "xsl-engine",
                                            "The name of the XSLT engine to use. Possible names are Xalan, XalanC, SaxonHE, SaxonEE.  Default: XalanC")

  val dontValidate = parser.flag[Boolean] (List("D", "dont-validate"),
                                       "Don't validate produced checker Default: false")

  val showErrors = parser.flag[Boolean] (List("e", "show-errors"),
                                          "Show error nodes in the generated dot. Default: false")

  val nfaMode = parser.flag[Boolean] (List("n", "nfa-mode"),
                                          "Display the generated dot in NFA mode. Default: false")

  val consoleLog = parser.flag[Boolean](List("L", "console-log"),
                                        "Display request log in console. Default: false")

  val port = parser.option[Int]("o", "portNumber", s"Port number. Default: $DEFAULT_PORT")

  val name = parser.option[String]("N", "name", s"The validator name. Default: $DEFAULT_NAME")

  val help = parser.flag[Boolean] (List("h", "help"),
                                   "Display usage.")

  val xpathVersion = parser.option[Int](List("t", "xpath-version"), "n",
                                           "XPath version to use. Can be 10, 20, 30, 31 for 1.0, 2.0, 3.0, and 3.1. Default: 10")

  val input = parser.parameter[String]("wadl",
                                       "WADL file/uri to read.  If not specified, stdin will be used.",
                                       true)

  val printVersion = parser.flag[Boolean] (List("version"),
                                            "Display version.")

  def getSource: Source = {
    var source: Source = null
    if (input.value.isEmpty) {
      source = new StreamSource(System.in)
    } else {
      source = new StreamSource(URLResolver.toAbsoluteSystemId(input.value.get))
    }
    source
  }

  def handleArgs(args: Array[String]): Unit = {
    parser.parse(args)

    if (help.value.getOrElse(false)) {
      parser.usage()
    }
  }

  //
  //  Draw a silly console box, cus it's cool...
  //
  def drawBox(title : String, content : String) {
    val allText  = title+"\n"+content
    val currMax = allText.split("\n").map(s=> s.trim).map(s => s.length()).reduceLeft((x,y) => if (x >= y) x else y)

    def padString (s : String, pad  : Int, padString : String = " ") : String = {
      s.padTo(pad,padString).map(a => a.toString).foldRight(""){(a, b) => a+b}
    }
    println()
    println (" ╒"+padString("",currMax,"═")+"╕")
    println (" │ "+padString(title, currMax-1)+"│")
    println (" ╞"+padString("",currMax,"═")+"╡")
    content.split("\n").map(s=>s.trim()).foreach (s => {
      if (s.contains(Console.RESET)) {
        println (" │ "+padString(s,currMax+7)+"│")
      } else {
        println (" │ "+padString(s,currMax-1)+"│")
      }
    })
    println (" ╘"+padString("",currMax,"═")+"╛")
    println()
  }

  def runServer (name : String, port : Int, dot : File, config : Config) : Unit = {
    val source = getSource

    val sourceName = {
      if (source.asInstanceOf[StreamSource].getInputStream != null) {
        "<STDIN>"
      } else {
        source.asInstanceOf[StreamSource].getSystemId
      }
    }

    val echoParam = OkayServlet.ECHO_CONTENT_PARAM
    val respParam = OkayServlet.RESPONSE_TYPE

    //
    //  Initalize the validator, this catches errors early...
    //
    println(s"Loading $sourceName...\n")
    val validator = Validator (name, source, config)


    //
    //  Initialize the server....
    //
    val server = new Server(port)
    val servletContextHandler = new ServletContextHandler(ServletContextHandler.NO_SESSIONS)
    val servletHandler = servletContextHandler.getServletHandler

    servletContextHandler.setContextPath("/")
    servletContextHandler.setResourceBase(System.getProperty("java.io.tmpdir"))
    servletContextHandler.setAttribute(ValidatorFilter.VALIDATOR_ATTRIB, validator)

    server.setHandler(servletContextHandler)

    servletHandler.addServletWithMapping(classOf[OkayServlet], "/*")
    servletHandler.addFilterWithMapping(classOf[ValidatorFilter], "/*", FilterMapping.REQUEST)

    server.start()

    //
    //  Display a nice little text box with important info...
    //
    val B  = Console.BOLD
    val R  = Console.RESET

    drawBox(s"$title $version",
            s"""
             Running validator $B$name$R

             Port: $B$port$R
             WADL Input: $B$sourceName$R
             Dot File: $B$dot$R

             The service should return a 200 response if the request
             validates against the WADL, it will return a 4xx code
             with an appropriate message otherwise.

             You can pass an '$B$echoParam$R' query paramater to the
             request to have the service echo the body of the request
             in the response.

             You can pass a '$B$respParam$R' query paramater to the
             request to set the ContentType of the response to the value
             of that parameter.
            """)

    //
    // Let the current thread join until the server is don executing...
    //
    server.join()
  }

  def main(args: Array[String]) = {
    try {
      handleArgs(args)

      if (printVersion.value.getOrElse(false)) {
        println(s"$title v$version")
      } else {
        val c = new Config

        c.removeDups = removeDups.value.getOrElse(false)
        c.enableRaxRolesExtension = raxRoles.value.getOrElse(false)
        c.maskRaxRoles403 = raxRolesMask403.value.getOrElse(false)
        c.checkWellFormed = wellFormed.value.getOrElse(false)
        c.checkXSDGrammar = xsdCheck.value.getOrElse(false)
        c.checkJSONGrammar = jsonCheck.value.getOrElse(false)
        c.checkElements = element.value.getOrElse(false)
        c.checkPlainParams = plainParam.value.getOrElse(false)
        c.enablePreProcessExtension = !preProc.value.getOrElse(false)
        c.joinXPathChecks = joinXPaths.value.getOrElse(false)
        c.checkHeaders = header.value.getOrElse(false)
        c.setParamDefaults = setDefaults.value.getOrElse(false)
        c.enableIgnoreXSDExtension = !ignoreXSD.value.getOrElse(false)
        c.enableIgnoreJSONSchemaExtension = !ignoreJSON.value.getOrElse(false)
        c.enableMessageExtension = !message.value.getOrElse(false)
        c.enableCaptureHeaderExtension = !captureHeader.value.getOrElse(false)
        c.enableAnyMatchExtension = !anyMatch.value.getOrElse(false)
        c.xpathVersion = xpathVersion.value.getOrElse(10)
        c.preserveRequestBody = preserveRequestBody.value.getOrElse(false)
        c.doXSDGrammarTransform = xsdGrammarTransform.value.getOrElse(false)
        c.validateChecker = !dontValidate.value.getOrElse(false)
        c.enableWarnHeaders = !warnHeaders.value.getOrElse(false)
        c.warnAgent = warnAgent.value.getOrElse("-")
        c.xslEngine = xslEngine.value.getOrElse("XalanC")


        val dot = File.createTempFile("chk", ".dot")
        dot.deleteOnExit()

        val handlerList = {
          val dotHandler = new SaveDotHandler(dot, !showErrors.value.getOrElse(false),
                                              nfaMode.value.getOrElse(false))

          val initList = List[ResultHandler](dotHandler,
                                             new ServletResultHandler(),
                                             new InstrumentedHandler())

          if (consoleLog.value.getOrElse(false)) {
            initList :+ new ConsoleResultHandler()
          } else {
            initList
          }
        }

        c.resultHandler = new DispatchResultHandler(handlerList)

        runServer(name.value.getOrElse(DEFAULT_NAME), port.value.getOrElse(DEFAULT_PORT), dot, c)
      }
    } catch {
      case e: ArgotUsageException => println(e.message)
    }
  }
}
