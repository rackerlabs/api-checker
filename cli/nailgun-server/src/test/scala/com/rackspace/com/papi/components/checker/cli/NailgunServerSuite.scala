/***
 *   Copyright 2016 Rackspace US, Inc.
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

import javax.xml.transform.stream._

import org.junit.runner.RunWith
import org.scalatest.time.Span
import org.scalatest.time.Seconds
import org.scalatest.concurrent.Timeouts
import org.scalatest.concurrent.Timeouts._

import org.scalatest.FunSuite
import org.scalatestplus.junit.JUnitRunner

import java.io.ByteArrayOutputStream
import java.io.PrintStream

import java.net.InetAddress
import java.net.Socket

import java.nio.file.Paths
import java.nio.file.Path
import java.nio.file.Files
import java.nio.file.FileVisitResult
import java.nio.file.FileVisitResult._
import java.nio.file.SimpleFileVisitor

import java.nio.file.attribute.BasicFileAttributes
import java.nio.file.attribute.BasicFileAttributeView
import java.nio.file.attribute.FileTime

import com.rackspace.com.papi.components.checker.wadl.WADLCheckerBuilder

@RunWith(classOf[JUnitRunner])
class NailgunServerSuite extends FunSuite with Timeouts {

  //
  // Runs nailgun server in an interruptable thread, allows running
  // assertions before the thread is joined.
  //
  def runNailgunServer(args : Array[String], asserts : => Unit = { }) : Unit = {
    val nailgunThread = new Thread(new Runnable() {
      override def run : Unit = {
        NailgunServer.main(args)
      }
    }, "Test nailgun server thread")
    nailgunThread.setDaemon(true)
    nailgunThread.start()
    //
    // Run assertions
    //
    asserts
    nailgunThread.join()
  }

  test ("--help should generate usage info, and then quit") {
    val stream = new ByteArrayOutputStream()
    val printStream = new PrintStream(stream)
    val oldErr = System.err
    failAfter(Span(3, Seconds)) {
      System.setErr(printStream)
      runNailgunServer(Array("--help","--duration", "PT10S", "-e"))
      System.setErr (oldErr)
      assert(stream.toString().contains("Usage: checker-nailgun-server"))
    }
  }

  test ("--version should display version, and then quit") {
    val stream = new ByteArrayOutputStream()
    val printStream = new PrintStream(stream)
    val oldErr = System.err
    failAfter(Span(3, Seconds)) {
      System.setErr(printStream)
      runNailgunServer(Array("--version","--duration", "PT10S", "-e"))
      System.setErr (oldErr)
      // Check weird version string, displays correctly during testing!
      assert(stream.toString().contains("null vnull"))
    }
  }

  test ("A bad duration should fail") {
    val stream = new ByteArrayOutputStream()
    val printStream = new PrintStream(stream)
    val oldErr = System.err
    System.setErr(printStream)
    runNailgunServer(Array("--duration","22Seconds"))
    System.setErr (oldErr)
    assert(stream.toString().contains("invalid lexical representation of \"22Seconds\""))
  }

  test ("A 2 second timeout should cause the server to stop running after 2 seconds.") {
    failAfter(Span(3, Seconds)) {
      print ("Running server with a timeout of 2 seconds...")
      runNailgunServer(Array("--duration","PT2S","-e"))
      println ("OKAY")
    }
  }

  test ("A 4 second timeout should cause the server to stop running after 4 seconds. (Check connection)") {
    val testPort = 2114
    failAfter(Span(5, Seconds)) {
      print ("Running server with a timeout of 5 seconds...")
      runNailgunServer(Array("--duration","PT2S","-e","-p",testPort.toString), {
        //
        //  Wait 1 sec for the server to start
        //
        print ("...waiting start...")
        Thread.sleep(1000)

        //
        //  Attempt to open a socket to the default port
        //
        print ("...connecting to port...")
        val socket = new Socket(InetAddress.getByName(NailgunServer.HOST_DEFAULT), testPort)

        //
        //  If something goes wrong, we'd exception so now just close socket
        //
        socket.close
      })
      println ("OKAY")
    }
  }

  test ("A server should shut down if the file on it's jar path changes") {
    val testPort = 2116
    //
    // Jar path, during testing this is actually a directory...
    //
    val classPath = Paths.get(classOf[NailgunServer].getProtectionDomain.getCodeSource.getLocation.getPath)
    //
    //  Get the first regular file we find in that path
    //
    val classPathFile : Path = {
      var cpf : Path = null
      Files.walkFileTree (classPath, new SimpleFileVisitor[Path]() {
        override def visitFile (file : Path, attrs : BasicFileAttributes) : FileVisitResult = {
          if (attrs.isRegularFile) {
            cpf = file
            TERMINATE
          } else {
            CONTINUE
          }
        }
      })
      if (cpf == null) fail(s"Could not find a file to change in $classPath")
      cpf
    }
    failAfter(Span(15, Seconds)) {
      print ("Running server with a timeout of 60 seconds...")
      runNailgunServer(Array("--duration","PT60S","-e","-p", testPort.toString), {
        //
        //  Wait 1 sec for the server to start
        //
        print ("...waiting start...")
        Thread.sleep(1000)

        //
        //  Attempt to open a socket to the default port...this tells us the server is really running
        //
        print ("...connecting to port...")
        val socket = new Socket(InetAddress.getByName(NailgunServer.HOST_DEFAULT), testPort)

        //
        //  If something goes wrong, we'd exception so now just close socket
        //
        socket.close

        //
        //  Touch a file in the classpath directory...this should cause the server to shutdown
        //
        print (s"...Touching $classPathFile...")
        val attrs = Files.getFileAttributeView(classPathFile, classOf[BasicFileAttributeView]).asInstanceOf[BasicFileAttributeView]
        val currentFileTime = FileTime.fromMillis(System.currentTimeMillis)
        attrs.setTimes (currentFileTime, currentFileTime, currentFileTime)
      })
      println ("OKAY")
    }
  }
}
