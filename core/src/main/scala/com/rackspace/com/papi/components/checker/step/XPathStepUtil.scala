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

import javax.xml.transform.Source
import javax.xml.transform.dom.DOMSource

import com.rackspace.com.papi.components.checker.Config
import com.rackspace.com.papi.components.checker.servlet.CheckerServletRequest
import com.rackspace.com.papi.components.checker.step.base.StepContext

import com.rackspace.com.papi.components.checker.util.ImmutableNamespaceContext
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
import net.sf.saxon.s9api.XQueryExecutable
import net.sf.saxon.s9api.XQueryEvaluator
import net.sf.saxon.s9api.QName
import net.sf.saxon.s9api.XdmValue
import net.sf.saxon.s9api.XdmEmptySequence

import net.sf.saxon.query.XQueryFunctionLibrary
import net.sf.saxon.query.XQueryFunction

import net.sf.saxon.value.SequenceType
import net.sf.saxon.functions.FunctionLibraryList
import net.sf.saxon.`type`.BuiltInAtomicType

import com.typesafe.scalalogging.LazyLogging

//
//  This utility is used to execute XPaths on steps which have access
//  to the current Request and the StepContext. Currently this
//  includes the Assert step which is used to make XPath assertions
//  and the CaptureHeader step which is used to introspect the request
//  to capture one or more header values.
//

object XPathStepUtil extends LazyLogging {
  //
  //  The saxon processor, xquery compiler
  //
  protected val processor = {
    val p = new Processor(true)
    val dynLoader = p.getUnderlyingConfiguration.getDynamicLoader
    dynLoader.setClassLoader(getClass.getClassLoader)
    p
  }

  protected val saxonConfig = {
    val conf = processor.getUnderlyingConfiguration
    conf.getDynamicLoader.setClassLoader(getClass.getClassLoader)
    conf
  }
  protected val compiler = {
    val c = processor.newXQueryCompiler
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
  protected[step] def parseXPath (expression : String, nc : ImmutableNamespaceContext, version : Int) : Unit = {
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

  //
  //  Create a valid XQuery for the given expression given the
  //  namespace context.
  //
  protected def xqueryStringForExpression(expression : String, nc : ImmutableNamespaceContext) : String = {
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

  //
  //  Compile an XQuery Executable for the given XPath expression,
  //  taking into account extended functions, etc.
  //
  protected[step] def xqueryExecutableForExpression(expression : String, nc : ImmutableNamespaceContext) : XQueryExecutable = {
    val query = xqueryStringForExpression(expression, nc)
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

  //
  //  Given a Checker Request and a StepContext, this function sets up
  //  an XQuery evaluator for execution.
  //
  protected[step] def setupXQueryEvaluator (eval : XQueryEvaluator, req : CheckerServletRequest, context : StepContext) : Unit = {
    val json : XdmValue = req.parsedJSONXdmValue match {
      case null => EMPTY_MAP
      case jv : XdmValue => jv
    }

    val xml : Source = req.parsedXMLSource match {
      case null => EMPTY_DOC
      case src : Source => src
    }

    eval.setSource(xml)
    eval.setExternalVariable (new QName(prefix,uri,"__JSON__"), json)
    eval.setExternalVariable (new QName(prefix,uri,"__REQUEST__"), req.asXdmValue)
    eval.setExternalVariable (new QName(prefix,uri,"__CONTEXT__"), context.asXdmValue)
  }
}
