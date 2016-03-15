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

import java.util.UUID

import org.apache.logging.log4j.{Level, LogManager}
import org.apache.logging.log4j.core.{LogEvent, LoggerContext}
import org.apache.logging.log4j.core.appender.AbstractAppender
import org.scalatest.exceptions.TestFailedException

import scala.collection.mutable.{HashMap, Queue, SynchronizedMap, SynchronizedQueue}

trait LogAssertions {

  type LogName = String
  type Log = Queue[LogEvent]

  private val logEntries = new HashMap[LogName, Log]() with SynchronizedMap[LogName, Log]

  def log(logName: LogName, level: Level)(f : => Any) : Unit = {
    val loggerConfig = LogManager.getContext(false).asInstanceOf[LoggerContext].getConfiguration.getLoggerConfig("root")
    val appenderName = "LogAssertionAppender_"+UUID.randomUUID.toString

    loggerConfig.addAppender(new AbstractAppender(appenderName, null, null) {
      override def isStarted : Boolean = true
      override def isStopped : Boolean = false

      override protected def append (event : LogEvent) : Unit = {
        logEntries.getOrElseUpdate(logName, new SynchronizedQueue[LogEvent]()) += event
      }
    }, level, null)
    f
    loggerConfig.removeAppender (appenderName)
  }

  def log (level : Level)(f : => Any) : LogName = {
    val logName = UUID.randomUUID.toString
    log(logName, level)(f)
    logName
  }

  /**
   *  Assert that at least one log message returns true for f
   */
  def assert(logName: LogName, message : String, f : (LogEvent) => Boolean) : Unit = {
    logEntries.get(logName) match {
      case None =>
        throw new TestFailedException(Some(s"Log $logName does not exist: $message"), None, 4)
      case Some(q) =>  q.find(f) match {
        case None =>
          throw new TestFailedException(Some(s"Log Assertion failed: $message"), None, 4)
        case Some(le) =>
          /* Assertion passed, ignore */
      }
    }
  }

  /**
   *  Asserts that there is a log event that contains the string message.
   */
  def assert(logName: LogName, assertMessage : String) : Unit = {
    assert(logName, s"No log message found containing '$assertMessage'",
           _.getMessage.getFormattedMessage.contains(assertMessage))
  }

  /**
   * Asserts that the log is empty (or does not exist)
   */
  def assertEmpty(logName : LogName) : Unit = {
    logEntries.get(logName) match {
      case None =>
        /* No Log == Empty Log so ignore...*/
      case Some(q) =>
        if (!q.isEmpty) {
          throw new TestFailedException(Some(s"Log $logName is not empty"), None, 4)
        }
    }
  }

  def printLog(logName: LogName) : Unit = {
    logEntries.get(logName).foreach(_.foreach(le => println(le.getMessage.getFormattedMessage)))
  }

  def clearLog(logName : LogName) : Unit = {
    logEntries.get(logName) match {
      case Some(q) => q.clear()
      case None => /* Ignore */
    }
  }

  def clearAllLogs() : Unit = {
    logEntries.clear
  }
}
