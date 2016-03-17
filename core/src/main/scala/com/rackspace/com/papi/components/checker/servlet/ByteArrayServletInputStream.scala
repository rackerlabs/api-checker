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
package com.rackspace.com.papi.components.checker.servlet

import java.io.ByteArrayInputStream
import javax.servlet.ServletInputStream

import scala.xml.NodeSeq

class ByteArrayServletInputStream (private val byteArray : Array[Byte]) extends ServletInputStream {
  val bin = new ByteArrayInputStream(byteArray)

  def this(content : String) {
    this(content.getBytes)
  }

  def this(n : NodeSeq) {
    this(n.toString)
  }

  override def available : Int = bin.available()
  override def close : Unit = bin.close()
  override def mark (readLimit : Int) : Unit = bin.mark(readLimit)
  override def markSupported : Boolean = bin.markSupported()
  override def read : Int = bin.read
  override def read(b : Array[Byte]) : Int = bin.read(b)
  override def read(b : Array[Byte], off : Int, len : Int) : Int = bin.read (b, off, len)
  override def reset : Unit = bin.reset
  override def skip(n : Long) : Long = bin.skip(n)
}
