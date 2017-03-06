/***
 *   Copyright 2017 Rackspace US, Inc.
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
package com.rackspace.com.papi.components.checker.step

import javax.servlet.FilterChain

import javax.xml.transform.Source
import javax.xml.transform.dom.DOMSource
import org.xml.sax.SAXParseException

import com.rackspace.com.papi.components.checker.Config
import com.rackspace.com.papi.components.checker.servlet._
import com.rackspace.com.papi.components.checker.step.base.{ConnectedStep, Step, StepContext}
import com.rackspace.com.papi.components.checker.util.ImmutableNamespaceContext
import com.rackspace.com.papi.components.checker.util.XQueryEvaluatorPool._
import com.rackspace.com.papi.components.checker.util.XMLParserPool._

import net.sf.saxon.expr.StaticProperty
import net.sf.saxon.expr.instruct.UserFunctionParameter
import net.sf.saxon.expr.parser.XPathParser

import net.sf.saxon.sxpath.IndependentContext
import net.sf.saxon.sxpath.XPathStaticContext

import net.sf.saxon.om.StructuredQName
import net.sf.saxon.om.GroundedValue
import net.sf.saxon.om.Sequence

import net.sf.saxon.s9api.Processor
import net.sf.saxon.s9api.XQueryEvaluator
import net.sf.saxon.s9api.QName
import net.sf.saxon.s9api.XdmValue
import net.sf.saxon.s9api.XdmEmptySequence

import net.sf.saxon.query.XQueryFunctionLibrary
import net.sf.saxon.query.XQueryFunction


import net.sf.saxon.value.SequenceType
import net.sf.saxon.functions.FunctionLibraryList
import net.sf.saxon.`type`.BuiltInAtomicType

import com.typesafe.scalalogging.slf4j.LazyLogging

object Assert {
  //
  //  The saxon processor, xquery compiler
  //
  protected val processor = new Processor(true)
  protected val saxonConfig = {
    val conf = processor.getUnderlyingConfiguration
    conf.getDynamicLoader.setClassLoader(getClass.getClassLoader)
    conf
  }
  protected val compiler = {
    val c = processor.newXQueryCompiler
    c.setLanguageVersion(Config.RAX_ASSERT_XPATH_VERSION_STRING)
    c
  }

  //
  //  Our functions are always defined in the following namespace.
  //
  private val prefix = "req"
  private val uri = "http://www.rackspace.com/repose/wadl/checker/request"

  //
  //  Functions take a list of sequences as input
  //  and return a sequence as output.
  //
  type Fun = (String /* Function Name */,
              List[SequenceType] /* Argument Types */,
              SequenceType /* Return Type */)

  //
  //  Predefined Functions
  //
  private val functions : List[Fun] =
    List(("header", List(SequenceType.SINGLE_STRING),
          SequenceType.SINGLE_STRING),
         ("header", List(SequenceType.SINGLE_STRING,
                         SequenceType.SINGLE_BOOLEAN),
          SequenceType.SINGLE_STRING),
         ("headers", List(SequenceType.SINGLE_STRING),
          SequenceType.makeSequenceType(BuiltInAtomicType.STRING,
                                        StaticProperty.ALLOWS_ZERO_OR_MORE)),
         ("headers", List(SequenceType.SINGLE_STRING,
                          SequenceType.SINGLE_BOOLEAN),
          SequenceType.makeSequenceType(BuiltInAtomicType.STRING,
                                        StaticProperty.ALLOWS_ZERO_OR_MORE)))

  //
  //  Create an XQueryLibarary where we can declare our custom
  //  functions.
  //
  private val xqlib = new XQueryFunctionLibrary(saxonConfig)

  //
  //  Declare functions
  //
  functions.foreach(f => {
    val fname   = f._1
    val argTypes = f._2
    val retType = f._3

    val fun = new XQueryFunction
    fun.setFunctionName(new StructuredQName(prefix, uri, fname))
    fun.setResultType(retType)

    argTypes.foreach(argType => {
      val param = new UserFunctionParameter
      param.setVariableQName(new StructuredQName(prefix, uri, "arg"))
      param.setRequiredType(argType)
      fun.addArgument(param)
    })

    xqlib.declareFunction(fun)
  })

  //
  // Predefined Variables
  //
  private val variables : List[String] =
    List("_","body","method","uri","headerNames","uriLevel")
  private val localVariables : List[String] =
    List("_","body")


  private def getXPathContext(nsc : ImmutableNamespaceContext) : XPathStaticContext = {
    //
    //  Context used when parsing XPaths
    //
    val ic = new IndependentContext

    ic.setXPathLanguageLevel(Config.RAX_ASSERT_XPATH_VERSION)
    ic.declareNamespace(prefix, uri)

    nsc.getAllPrefixes.filter(p => {
      (p != "xmlns") &&  (p != "xml")
    }).foreach(p => {
      ic.declareNamespace(p, nsc.getNamespaceURI(p))
    })

    //
    //  Declare module and local variables, and functions
    //
    variables.foreach(ic.declareVariable(uri,_))
    localVariables.foreach(ic.declareVariable("",_))

    ic.getFunctionLibrary.asInstanceOf[FunctionLibraryList].addFunctionLibrary(xqlib)
    ic
  }

  //
  //  We are using an XQuery evaluator to execute our XPath. We do
  //  this because it's easy to create the extensions needed for our
  //  assertions req:headers, for example. (we define these in
  //  xq/assert.xq.
  //
  //  XQuery is a superset of XPath and we don't want folks extending
  //  beyond the capabilites of XPath, we use an XPath parser to
  //  confirm we have valid XPath before anything else -- this will
  //  throw an XPathException if we use capabilities in XQuery that
  //  are not in XPath such as FLWOR expressions.
  //
  //  The parser is made aware of the variables / functions available
  //  to asserts, so these are also checked.
  //
  def parseXPath (expression : String, nc : ImmutableNamespaceContext, version : Int) : Unit = {
    val xpathParser = new XPathParser
    xpathParser.setLanguage(XPathParser.XPATH, version)
    xpathParser.parse(expression, 0, 0, getXPathContext(nc))
  }


  //
  // Location of the shared XQuery module
  //
  private val moduleName : String = "/xq/assert.xq";
  private val moduleLoc : String = getClass.getResource(moduleName).toString()

  //
  // XQuery prolog used in front of all XPath expressions.
  //
  protected val XQUERY_PROLOG = s"""
     xquery version \"3.1\" encoding \"UTF-8\";

     import module \"$uri\"
            at \"$moduleLoc\";

     declare namespace $prefix = \"$uri\";


  """

  protected val EMPTY_MAP = XdmEmptySequence.getInstance
  protected val EMPTY_DOC = {
    val parser = borrowParser
    try {
      new DOMSource(parser.newDocument)
    } finally {
      returnParser(parser)
    }
  }
}

import Assert._

class Assert(id : String, label : String, val expression : String, val message : Option[String],
             val code : Option[Int], val nc : ImmutableNamespaceContext, val version : Int,
             val priority : Long, next : Array[Step]) extends ConnectedStep(id, label, next) with LazyLogging {

  override val mismatchMessage : String = message.getOrElse (s"Expecting $expression")
  val mismatchCode : Int = code.getOrElse(400)

  private val query : String = {
    XQUERY_PROLOG + nc.getAllPrefixes.filter(p => {
      //
      // These are predefined prefixes, we can't overwrite them
      //
      (p != "xmlns") &&  (p != "xml") && (p != "req") && (p != "")
    }).map(p => {
      val uri = nc.getNamespaceURI(p);
      s"""declare namespace $p = \"$uri\";\n"""
    }).fold("")(_ + _)+s"""
          declare variable $$_    := $$$prefix:_;    (: For compatibility with plain params :)
          declare variable $$body := $$$prefix:body; (: For compatibility with plain params :)

    """+expression
  }

  private val exec = {
    try {
      logger.trace (s"compiling :\n$query")
      val e = compiler.compile(query)
      logger.trace ("compilation done.")
      e
    } catch {
      case ex : Exception => logger.error(s"Caught exception while compiling $query",ex)
                             throw ex
    }
  }

  override def checkStep(req : CheckerServletRequest, resp : CheckerServletResponse, chain : FilterChain, context : StepContext) : Option[StepContext] = {
    var ret : Option[StepContext] = None
    var eval : XQueryEvaluator = null

    val json : XdmValue = req.parsedJSONXdmValue match {
      case null => EMPTY_MAP
      case jv : XdmValue => jv
    }

    val xml : Source = req.parsedXMLSource match {
      case null => EMPTY_DOC
      case src : Source => src
    }

    try {
      eval = borrowEvaluator(expression, exec)
      eval.setSource(xml)
      eval.setExternalVariable (new QName(prefix,uri,"__JSON__"), json)
      eval.setExternalVariable (new QName(prefix,uri,"__REQUEST__"), req.asXdmValue(saxonConfig))
      eval.setExternalVariable (new QName(prefix,uri,"__CONTEXT__"), context.asXdmValue(saxonConfig))
      val res : Boolean = eval.evaluate.getUnderlyingValue match {
        case groundedValue : GroundedValue => groundedValue.effectiveBooleanValue
        case s : Sequence => s.head != null
      }
      if (res) {
        ret = Some(context)
      } else {
        req.contentError(new SAXParseException (mismatchMessage, null), mismatchCode, priority)
      }
    } catch {
      case e : Exception => req.contentError(new SAXParseException(mismatchMessage+" : "+e.getMessage, null, e), mismatchCode, priority)
    }finally {
      returnEvaluator (expression, eval)
    }
    ret
  }
}
