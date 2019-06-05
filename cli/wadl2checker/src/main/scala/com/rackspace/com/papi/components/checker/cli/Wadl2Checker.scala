/** *
  * Copyright 2014 Rackspace US, Inc.
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

import javax.xml.transform._
import javax.xml.transform.stream._

import java.io.File
import java.io.PrintStream
import java.io.InputStream

import com.rackspace.com.papi.components.checker.Config
import com.rackspace.com.papi.components.checker.util.URLResolver
import com.rackspace.com.papi.components.checker.wadl.WADLCheckerBuilder
import com.martiansoftware.nailgun.NGContext

object Wadl2Checker {
  val title = getClass.getPackage.getImplementationTitle
  val implVersion = getClass.getPackage.getImplementationVersion

  def parseArgs(args: Array[String], base: String,
                in: InputStream, out: PrintStream, err: PrintStream): Option[(Source, Result, Option[StreamResult], Config)] = {

    val c = new Config

    // Fix default values for fields with defaults that differ between the API and CLI Config
    c.removeDups = false

    var input: String = ""
    var output: Option[String] = None
    var outputMetadata: Boolean = false

    val parser = new CheckerParser("wadl2checker", c) {
      head(s"$title v$implVersion")

      opt[Unit]('O', "output-metadata").text("Display checker metadata")
        .foreach(_ => outputMetadata = true)

      arg[String]("wadl").text("WADL file/uri to read.")
        .foreach(x => input = x)

      arg[String]("output").text("Output file. If not specified, stdout will be used.")
        .optional()
        .foreach(x => output = Some(x))
    }

    def source: Source = new StreamSource(URLResolver.toAbsoluteSystemId(input, base))

    def result: Result = {
      var r: Result = null
      if (output.isEmpty) {
        r = new StreamResult(out)
      } else {
        r = new StreamResult(URLResolver.toAbsoluteSystemId(output.get, base))
      }
      r
    }

    if (parser.parse(args)) {
      val metaOutResult = {
        if (outputMetadata) {
          Some(new StreamResult(err))
        } else {
          None
        }
      }

      Some((source, result, metaOutResult, c))
    } else {
      //Bad arguements
      None
    }
  }

  private def getBaseFromWorkingDir(workingDir: String): String = {
    (new File(workingDir)).toURI().toString
  }

  //
  //  Local run...
  //
  def main(args: Array[String]) = {
    parseArgs(args, getBaseFromWorkingDir(System.getProperty("user.dir")),
      System.in, System.out, System.err) match {
      case Some((source: Source, result: Result, metaResult: Option[StreamResult], config: Config)) =>
        new WADLCheckerBuilder().build(source, result, metaResult, config)
      case None => /* Bad args, Ignore */
    }
  }

  //
  //  Nailgun run...
  //
  def nailMain(context: NGContext) = {
    parseArgs(context.getArgs, getBaseFromWorkingDir(context.getWorkingDirectory),
      context.in, context.out, context.err) match {
      case Some((source: Source, result: Result, metaResult: Option[StreamResult], config: Config)) =>
        new WADLCheckerBuilder().build(source, result, metaResult, config)
      case None => /* Bad args, Ignore */
    }
  }
}
