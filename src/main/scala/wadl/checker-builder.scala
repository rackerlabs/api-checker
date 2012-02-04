package com.rackspace.com.papi.components.checker.wadl

import scala.xml._

import java.io.ByteArrayOutputStream

import javax.xml.transform._
import javax.xml.transform.sax._
import javax.xml.transform.stream._

import com.rackspace.cloud.api.wadl.WADLNormalizer
import com.rackspace.cloud.api.wadl.WADLFormat._
import com.rackspace.cloud.api.wadl.RType._
import com.rackspace.cloud.api.wadl.XSDVersion._
import com.rackspace.cloud.api.wadl.Converters._


/**
 * An exception when transating the WADL into a checker.
 */
class WADLException(private val msg : String, private val cause : Throwable) extends Throwable(msg, cause) {}

class WADLCheckerBuilder(private var wadl : WADLNormalizer) {

  if (wadl == null) {
    wadl = new WADLNormalizer
  }

  def this() = this(null)

  val buildTemplates : Templates = wadl.saxTransformerFactory.newTemplates(new StreamSource(getClass().getResourceAsStream("/xsl/builder.xsl")))

  def build (in : Source, out: Result) : Unit = {
    try {
      val transformer = wadl.newTransformer(TREE, XSD11, false, KEEP)
      val buildHandler = wadl.saxTransformerFactory.newTransformerHandler(buildTemplates)

      buildHandler.setResult (out)
      transformer.transform (in, new SAXResult(buildHandler))
    } catch {
      case e => throw new WADLException ("WADL Processing Error", e)
    }
  }

  def build (in : (String, NodeSeq)) : NodeSeq = {
    val bytesOut = new ByteArrayOutputStream()
    build (in, new StreamResult(bytesOut))
    XML.loadString (bytesOut.toString())
  }

  def build (in: NodeSeq) : NodeSeq = {
    build (("",in))
  }
}

