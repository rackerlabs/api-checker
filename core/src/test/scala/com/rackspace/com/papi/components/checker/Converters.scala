/***
 *   Copyright 2014 Rackspace US, Inc.
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
package com.rackspace.com.papi.components.checker

import java.io.{ByteArrayInputStream, StringWriter}
import javax.xml.transform.Transformer
import javax.xml.transform.dom.{DOMResult, DOMSource}
import javax.xml.transform.stream.{StreamResult, StreamSource}

import com.rackspace.com.papi.components.checker.util.IdentityTransformPool
import org.w3c.dom.Document

import scala.language.implicitConversions
import scala.xml._

object Converters {
  //
  //  Convert a W3C dom node to a node seq.
  //
  implicit def doc2NodeSeq (doc : Document) : NodeSeq = {
    var transf : Transformer = null
    val swriter = new StringWriter()
    try {
      transf = IdentityTransformPool.borrowTransformer
      transf.transform(new DOMSource(doc), new StreamResult(swriter))
      XML.loadString (swriter.toString)
    } finally {
      if (transf != null) IdentityTransformPool.returnTransformer(transf)
    }
  }

  implicit def nodeSeq2Doc (n : NodeSeq) : Document = {
    var transf : Transformer = null
    try {
      val result = new DOMResult()
      transf = IdentityTransformPool.borrowTransformer
      transf.transform(new StreamSource(new ByteArrayInputStream(n.toString.getBytes)),
                       result)
      result.getNode.asInstanceOf[Document]
    } finally {
      if (transf != null) IdentityTransformPool.returnTransformer(transf)
    }
  }
}
