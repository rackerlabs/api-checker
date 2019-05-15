/** *
  * Copyright 2016 Rackspace US, Inc.
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
package com.rackspace.com.papi.components.checker.cli

import java.io.IOException
import java.net.InetAddress
import java.nio.file.FileVisitResult._
import java.nio.file._
import java.nio.file.StandardWatchEventKinds._
import java.nio.file.attribute.BasicFileAttributes
import java.util.{Date, Timer, TimerTask}

import com.martiansoftware.nailgun.NGServer
import javax.xml.datatype.DatatypeFactory
import scopt.OptionParser

object NailgunServer {
  //
  //  Defaults
  //
  val HOST_DEFAULT: String = "127.0.0.1"
  val PORT_DEFAULT: Int = 2113
  val DURATION_DEFAULT: String = "PT2H"
  val DONT_EXIT_ON_SHUTDOWN: Boolean = false

  val title = getClass.getPackage.getImplementationTitle
  val implVersion = getClass.getPackage.getImplementationVersion

  def parseArgs(args: Array[String]): Option[(String, Int, Long, Boolean)] = {

    var duration = DURATION_DEFAULT
    var port = PORT_DEFAULT
    var host = HOST_DEFAULT
    var dontExitOnShutdown = DONT_EXIT_ON_SHUTDOWN

    val parser = new OptionParser[Unit]("checker-nailgun-server") {
      head(s"$title v$implVersion")

      help('h', "help").text("Display usage.")

      version("version").text("Display version.")

      opt[String]('d', "duration").text(s"The duration of time (in ISO 8601 format) before the server shuts down. Default: $DURATION_DEFAULT")
        .foreach(x => duration = x)

      opt[Int]('p', "port").text(s"The port for the server to listen to. Default: $PORT_DEFAULT")
        .foreach(x => port = x)

      opt[String]('H', "host").text(s"The host address for the server to listen to. Default: $HOST_DEFAULT")
        .foreach(x => host = x)

      opt[Unit]('e', "dont-exit-on-shutdown").text(s"Dont exit the JVM when the server shutsdown.  Default: $DONT_EXIT_ON_SHUTDOWN")
        .foreach(_ => dontExitOnShutdown = true)
    }


    try {
      if (parser.parse(args)) {
        //
        // Figure out the duration time
        //
        val durationInMillis: Long = DatatypeFactory.newInstance.newDurationDayTime(duration).getTimeInMillis(new Date)

        Some((host,
          port,
          durationInMillis,
          !dontExitOnShutdown))

      } else {
        //failed to parse
        None
      }
    } catch {
      case ia: IllegalArgumentException => System.err.println(ia.getMessage)
        None
    }
  }

  private def setupWatcher(server: NGServer, exitOnShutdown: Boolean): Unit = {
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
          var firstFile: Path = null
          Files.walkFileTree(jar, new SimpleFileVisitor[Path]() {
            override def visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult = {
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
      val wk = jarPath.register(ws, ENTRY_MODIFY)

      val listenThread = new Thread(new Runnable() {
        override def run: Unit = {
          //  Retrieving any watch event should trigger a server shutdown
          ws.take()
          System.err.println(s"$jarPath changed nailgun server shutting down...")
          server.shutdown(exitOnShutdown)
        }
      }, s"Change Listener ($title v$implVersion)")
      listenThread.setDaemon(true)
      listenThread.start()
    } catch {
      case unsupportedException: UnsupportedOperationException =>
        System.err.println("WARNING: file watching is not supported. You'll need to manually shut down the server if the code changes.")
      case ioe: IOException =>
        val msg = ioe.getMessage()
        System.err.println(s"I/O Exception ($msg) : You'll need to manually shut down the server if the code changes.")
        ioe.printStackTrace()
    }
  }

  private def setupTimer(server: NGServer, durationInMillis: Long, exitOnShutdown: Boolean): Unit = {
    val timer = new Timer(s"Timer ($title v$implVersion)", true)
    timer.schedule(new TimerTask() {
      override def run: Unit = {
        server.shutdown(exitOnShutdown)
      }
    }, durationInMillis)
  }

  def main(args: Array[String]) = {
    parseArgs(args) match {
      case Some((host: String, port: Int, duration: Long, exitOnShutdown: Boolean)) => {
        //
        //  Setup the server
        //
        val server = new NGServer(InetAddress.getByName(host), port)

        //
        // Setup a watcher to listen for changes in the jar directory.  If
        // any file changes, then shutdown the server.
        //
        setupWatcher(server, exitOnShutdown)

        //
        //  Setup the stop timer after the duration, the server will
        //  silently shut down
        //
        setupTimer(server, duration, exitOnShutdown)

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
