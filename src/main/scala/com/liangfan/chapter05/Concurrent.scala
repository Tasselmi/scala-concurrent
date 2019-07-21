package com
package liangfan
package chapter05


import java.util.concurrent.ConcurrentSkipListSet
import scala.collection._
import scala.collection.convert.decorateAsScala._
import scala.concurrent.ExecutionContext
import ExecutionContext.Implicits.global
import ParHtmlSearch.getHtmlSpec
import chapter04.FuturesCallbacks.getUrlSpec


object ConcurrentCollections extends App {

    def intersection(a: GenSet[String], b: GenSet[String]): GenSet[String] = {
        val skiplist = new ConcurrentSkipListSet[String]()
        for (x <- a.par) if (b.contains(x)) skiplist.add(x)
        val result = skiplist.asScala
        result
    }

    val ifut = for {
        htmlSpec <- getHtmlSpec
        urlSpec <- getUrlSpec
    } yield {
        val htmlWords = htmlSpec.mkString.split("\\s+").toSet
        val urlWords = urlSpec.mkString.split("\\s+").toSet
        intersection(htmlWords, urlWords)
    }

    ifut foreach {
        case i => log(s"intersection = $i")
    }

    Thread.sleep(5000)

}


object ConcurrentTrieMap extends App {

    val cache = new concurrent.TrieMap[Int, String]()
    for (i <- 0 until 100) cache(i) = i.toString
    for ((n, s) <- cache.par) cache(-n) = s"-$s"

    log(s"cache - ${cache.keys.toList.sorted}")

}
