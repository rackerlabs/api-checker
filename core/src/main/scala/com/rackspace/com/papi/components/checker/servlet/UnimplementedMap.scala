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


package com.rackspace.com.papi.components.checker.servlet

import java.util.Map

/**
 * This helper class is used as a base class for all other map clases.
 * it is simply an unimplemented map.
 */

class UnimplementedMap[K, V] extends Map[K, V] {
  def clear(): Unit = ???
  def containsKey(x$1: Any): Boolean = ???
  def containsValue(x$1: Any): Boolean = ???
  def entrySet(): java.util.Set[java.util.Map.Entry[K,V]] = ???
  def get(x$1: Any): V = ???
  def isEmpty(): Boolean = ???
  def keySet(): java.util.Set[K] = ???
  def put(x$1: K,x$2: V): V = ???
  def putAll(x$1: java.util.Map[_ <: K, _ <: V]): Unit = ???
  def remove(x$1: Any): V = ???
  def size(): Int = ???
  def values(): java.util.Collection[V] = ???
}
