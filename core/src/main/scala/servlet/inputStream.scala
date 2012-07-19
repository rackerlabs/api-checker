package com.rackspace.com.papi.components.checker.servlet

import java.io.ByteArrayInputStream
import javax.servlet.ServletInputStream

class ByteArrayServletInputStream (private val byteArray : Array[Byte]) extends ServletInputStream {
  val bin = new ByteArrayInputStream(byteArray)

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
