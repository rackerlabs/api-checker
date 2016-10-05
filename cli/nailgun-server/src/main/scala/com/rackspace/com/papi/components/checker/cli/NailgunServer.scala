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

import java.net.InetAddress

import java.util.Date
import java.util.Timer
import java.util.TimerTask

import java.io.InputStream
import java.io.PrintStream
import java.io.IOException

import java.nio.file.FileSystems
import java.nio.file.Paths
import java.nio.file.Path
import java.nio.file.Files
import java.nio.file.FileVisitResult
import java.nio.file.FileVisitResult._
import java.nio.file.SimpleFileVisitor
import java.nio.file.WatchService
import java.nio.file.StandardWatchEventKinds._

import java.nio.file.attribute.BasicFileAttributes

import javax.xml.datatype.DatatypeFactory
import javax.xml.datatype.Duration

import org.clapper.argot.ArgotConverters._
import org.clapper.argot.{ArgotParser, ArgotUsageException}

import com.rackspace.com.papi.components.checker.wadl.WADLCheckerBuilder

import com.martiansoftware.nailgun.NGServer

object NailgunServer {
  //
  //  Defaults
  //
  val HOST_DEFAULT : String = "127.0.0.1"
  val PORT_DEFAULT : Int = 2113
  val DURATION_DEFAULT : String = "PT2H"
  val DONT_EXIT_ON_SHUTDOWN  : Boolean = false

  val title = getClass.getPackage.getImplementationTitle
  val version = getClass.getPackage.getImplementationVersion

  def parseArgs (args : Array[String]) : Option[(String, Int, Long, Boolean)] = {

    val parser = new ArgotParser("checker-nailgun-server", preUsage=Some(s"$title v$version"))

    val duration = parser.option[String] (List("d", "duration"), "duration",
                                          s"The duration of time (in ISO 8601 format) before the server shuts down. Default: $DURATION_DEFAULT")

    val port = parser.option[Int] (List("p", "port"), "port",
                                   s"The port for the server to listen to. Default: $PORT_DEFAULT")

    val host = parser.option[String] (List("H", "host"), "host",
                                      s"The host address for the server to listen to. Default: $HOST_DEFAULT")

    val dontExitOnShutdown = parser.flag[Boolean] (List("e", "dont-exit-on-shutdown"),
                                                   s"Dont exit the JVM when the server shutsdown.  Default: $DONT_EXIT_ON_SHUTDOWN")

    val printVersion = parser.flag[Boolean] (List("v", "version"),
                                             "Display version.")

    val help = parser.flag[Boolean] (List("h", "help"),
                                     "Display usage.")

    try {
      parser.parse(args)

      if (help.value.getOrElse(false)) {
        parser.usage() // throws ArgotUsageException
      }

      if (printVersion.value.getOrElse(false)) {
        System.err.println(s"$title v$version")
        None
      } else {
        //
        // Figure out the duration time
        //
        val durationInMillis : Long = DatatypeFactory.newInstance.newDurationDayTime(duration.value.getOrElse(DURATION_DEFAULT)).getTimeInMillis(new Date)

        Some((host.value.getOrElse(HOST_DEFAULT),
              port.value.getOrElse(PORT_DEFAULT),
              durationInMillis,
              !dontExitOnShutdown.value.getOrElse(DONT_EXIT_ON_SHUTDOWN)))
      }
    } catch {
      case e: ArgotUsageException => System.err.println(e.message)
                                     None
      case ia : IllegalArgumentException => System.err.println(ia.getMessage)
                                            None
    }
  }

  private def setupWatcher(server : NGServer, exitOnShutdown : Boolean) : Unit = {
    try {
      val fs = FileSystems.getDefault()
      val ws = fs.newWatchService()
      val jarPath = {
        val jar = Paths.get(classOf[NailgunServer].getProtectionDomain.getCodeSource.getLocation.getPath)
        if (Files.isDirectory(jar)) {
          //
          //  If it's a directory, find the first file in the
          //  directory, then get the file's parent.  This will get to
          //  the actual classes of the NailgunServer.
          //
          var firstFile : Path = null
          Files.walkFileTree(jar, new SimpleFileVisitor[Path]() {
            override def visitFile (file : Path, attrs : BasicFileAttributes) : FileVisitResult = {
              if (attrs.isRegularFile) {
                firstFile = file
                TERMINATE
              } else {
                CONTINUE
              }
            }
          })
          if (firstFile == null) throw new IOException(s"No files in watch directory $jar")
          firstFile.getParent()
        } else {
          jar.getParent()
        }
      }
      val wk = jarPath.register (ws, ENTRY_MODIFY)

      val listenThread = new Thread (new Runnable() {
        override def run : Unit = {
          //  Retrieving any watch event should trigger a server shutdown
          ws.take()
          System.err.println(s"$jarPath changed nailgun server shutting down...")
          server.shutdown(exitOnShutdown)
        }}, s"Change Listener ($title v$version)")
      listenThread.setDaemon(true)
      listenThread.start()
    } catch {
      case unsupportedException : UnsupportedOperationException =>
        System.err.println ("WARNING: file watching is not supported. You'll need to manually shut down the server if the code changes.")
      case ioe : IOException =>
        val msg = ioe.getMessage()
      System.err.println (s"I/O Exception ($msg) : You'll need to manually shut down the server if the code changes.")
      ioe.printStackTrace()
    }
  }

  private def setupTimer(server : NGServer, durationInMillis : Long, exitOnShutdown : Boolean) : Unit = {
    val timer = new Timer(s"Timer ($title v$version)", true)
    timer.schedule(new TimerTask() {
      override def run : Unit = {
        server.shutdown(exitOnShutdown)
      }
    }, durationInMillis)
  }

  def main(args : Array[String]) = {
    parseArgs (args) match {
      case Some((host : String, port : Int, duration : Long, exitOnShutdown : Boolean)) => {
        //
        //  Setup the server
        //
        val server = new NGServer(InetAddress.getByName(host), port)

        //
        // Setup a watcher to listen for changes in the jar directory.  If
        // any file changes, then shutdown the server.
        //
        setupWatcher (server, exitOnShutdown)

        //
        //  Setup the stop timer after the duration, the server will
        //  silently shut down
        //
        setupTimer (server, duration, exitOnShutdown)

        //
        // Now run the actual server
        //
        server.run
      }
      case None => /* Ignore, bad args */
    }
  }
}

//
//  Private class, used to detect the JAR directory.
//
private class NailgunServer {}
