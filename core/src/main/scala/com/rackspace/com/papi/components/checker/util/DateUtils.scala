/*
 * ====================================================================
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 *
 * Apache HttpComponents Client
 * Copyright 1999-2014 The Apache Software Foundation
 *
 * This product includes software developed at
 * The Apache Software Foundation (http://www.apache.org/).
 *
 *
 * Modified by Damien Johnson <damien.johnson@rackspace.com>
 *  - Removed the usage of org.apache.http.annotation.Immutable
 *  - Removed the usage of org.apache.http.util.Args
 *  + Added null checks where Args.notNull was used previously, and an IllegalArgumentException for when the check fails
 *
 * Modified by David Kowis <dkowis@shlrm.org>
 *  - Converted to scala code using http://javatoscala.com
 */
package com.rackspace.com.papi.components.checker.util

import java.lang.ref.SoftReference
import java.text.ParsePosition
import java.text.SimpleDateFormat
import java.util._
/**
 * A utility class for parsing and formatting HTTP dates as used in cookies and
 * other headers.  This class handles dates as defined by RFC 2616 section
 * 3.3.1 as well as some other common non-standard formats.
 *
 * Converted using javatoscala.com from the original apache source
 */

object DateUtils {

  val calendar = Calendar.getInstance
  val GMT = TimeZone.getTimeZone("GMT")
  calendar.setTimeZone(GMT)
  calendar.set(2000, Calendar.JANUARY, 1, 0, 0, 0)
  calendar.set(Calendar.MILLISECOND, 0)

  val PATTERN_RFC1123 = "EEE, dd MMM yyyy HH:mm:ss zzz"

  val PATTERN_RFC1036 = "EEE, dd-MMM-yy HH:mm:ss zzz"

  val PATTERN_ASCTIME = "EEE MMM d HH:mm:ss yyyy"

  private val DEFAULT_PATTERNS = Array(PATTERN_RFC1123, PATTERN_RFC1036, PATTERN_ASCTIME)

  private val DEFAULT_TWO_DIGIT_YEAR_START = calendar.getTime

  def parseDate(dateValue: String): Date = parseDate(dateValue, null, null)

  def parseDate(dateValue: String, dateFormats: Array[String]): Date = parseDate(dateValue, dateFormats, null)

  def parseDate(dateValue: String, dateFormats: Array[String], startDate: Date): Date = {
    if (dateValue == null) {
      throw new IllegalArgumentException("Date value cannot be null")
    }
    val localDateFormats = if (dateFormats != null) dateFormats else DEFAULT_PATTERNS
    val localStartDate = if (startDate != null) startDate else DEFAULT_TWO_DIGIT_YEAR_START
    var v = dateValue
    if (v.length > 1 && v.startsWith("'") && v.endsWith("'")) {
      v = v.substring(1, v.length - 1)
    }
    for (dateFormat <- localDateFormats) {
      val dateParser = DateFormatHolder.formatFor(dateFormat)
      dateParser.set2DigitYearStart(localStartDate)
      val pos = new ParsePosition(0)
      val result = dateParser.parse(v, pos)
      if (pos.getIndex != 0) {
        return result
      }
    }
    null
  }

  def formatDate(date: Date): String = formatDate(date, PATTERN_RFC1123)

  def formatDate(date: Date, pattern: String): String = {
    if (date == null || pattern == null) {
      throw new IllegalArgumentException("Date value or pattern cannot be null")
    }
    val formatter = DateFormatHolder.formatFor(pattern)
    formatter.format(date)
  }

  def clearThreadLocal() {
    DateFormatHolder.clearThreadLocal()
  }

  object DateFormatHolder {

    private val THREADLOCAL_FORMATS = new ThreadLocal[SoftReference[Map[String, SimpleDateFormat]]]() {

      protected override def initialValue(): SoftReference[Map[String, SimpleDateFormat]] = {
        new SoftReference[Map[String, SimpleDateFormat]](new HashMap[String, SimpleDateFormat]())
      }
    }

    def formatFor(pattern: String): SimpleDateFormat = {
      val ref = THREADLOCAL_FORMATS.get
      var formats = ref.get
      if (formats == null) {
        formats = new HashMap[String, SimpleDateFormat]()
        THREADLOCAL_FORMATS.set(new SoftReference[Map[String, SimpleDateFormat]](formats))
      }
      var format = formats.get(pattern)
      if (format == null) {
        format = new SimpleDateFormat(pattern, Locale.US)
        format.setTimeZone(TimeZone.getTimeZone("GMT"))
        formats.put(pattern, format)
      }
      format
    }

    def clearThreadLocal() {
      THREADLOCAL_FORMATS.remove()
    }
  }
}
