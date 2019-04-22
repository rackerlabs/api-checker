package com.rackspace.com.papi.components.checker.util

import com.codahale.metrics.{Gauge, Metric, MetricFilter, MetricRegistry, JmxReporter}
import com.typesafe.scalalogging.LazyLogging

import scala.language.existentials

object Instrumented {
  val metricRegistry = new MetricRegistry()
  val metricDomain = "com.rackspace.com.papi.components.checker"
  val reporter = JmxReporter
    .forRegistry(metricRegistry)
    .inDomain(metricDomain)
    .createsObjectNamesWith(new JmxObjectNameFactory())
    .build()
  reporter.start()
}

import Instrumented._

/**
  * @see nl.grons.metrics.scala.InstrumentedBuilder
  */
trait Instrumented extends LazyLogging {

  def getRegistryClassName(clazz: Class[_]): String = {
    clazz.getName
      .replace(metricDomain,"")
      // @see nl.grons.metrics.scala.MetricName
      .replaceAllLiterally("$$anonfun", ".")
      .replaceAllLiterally("$apply", ".")
      .replaceAll("""\$\d*""", ".")
      .replaceAllLiterally(".package.", ".")
      .replaceAll("""^\.""", "")
      .replaceAll("""\.$""", "")
  }

  def gaugeOrAdd[T](name: String)(f: => T): Gauge[T] = {
    gaugeOrAdd(name, new Gauge[T] {
      def getValue: T = f
    }).asInstanceOf[Gauge[T]]
  }

  /**
    * @see com.codahale.metrics.MetricRegistry.getOrAdd
    */
  def gaugeOrAdd(name: String, gauge: Gauge[_]): Gauge[_] = {
    val addedGauge = try {
      Some(metricRegistry.register(name, gauge))
    } catch {
      case e: IllegalArgumentException =>
        logger.warn("Failed to add new gauge. Reason: {}", e.getMessage)
        logger.debug("", e)
        None
    }
    if (addedGauge.isDefined) {
      addedGauge.get
    } else {
      logger.debug("Retrieving gauge named: {}", name)
      metricRegistry.getGauges(new MetricFilter() {
        override def matches(filterName: String, metric: Metric): Boolean = {
          filterName.equals(name) &&
            metric.isInstanceOf[Gauge[_]]
        }
      }).get(name)
    }
  }
}
