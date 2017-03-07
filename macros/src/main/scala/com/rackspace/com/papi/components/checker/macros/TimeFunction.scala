/***
 *   Copyright 2017 Rackspace US, Inc.
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
package com.rackspace.com.papi.components.checker.macros

import scala.language.experimental.macros
import scala.reflect.macros.blackbox

object TimeFunction {
  val TIME_PROP = "checker.timeFunctions"
  val TIME_WARN = 650
  def timeFunction[T](desc : String, f : => T) : T = macro timeFunctionImpl[T]

  def timeFunctionImpl[T] (c : blackbox.Context)(desc : c.Tree, f : c.Tree) : c.Expr[T] = {
    import c.universe._

    if (!doTimeFunction) {
      c.Expr(f)
    } else {
      val start = c.freshName("start")
      val end   = c.freshName("end")
      val tt    = c.freshName("tt")
      val ret   = c.freshName("ret")

      c.Expr(q"""
             Console.err.print("["+$desc+" : ")
                                 val $$start = System.currentTimeMillis
                                 val $$ret = $f
                                 val $$end = System.currentTimeMillis
                                 val $$tt  = $$end - $$start
                                 if ($$tt > $TIME_WARN) Console.err.print(Console.RED)
                                 Console.err.print($$tt)
                                 if ($$tt > $TIME_WARN) Console.err.print(Console.RESET)
                                 Console.err.println(" Millis]")
             $$ret
             """)
    }
  }

  def doTimeFunction : Boolean = Option(System.getProperty(TIME_PROP)).isDefined
}
