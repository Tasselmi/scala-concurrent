package com
package liangfan
package chapter04


import scala.concurrent.duration._
import scala.concurrent.{Future, Promise, Await, blocking}
import scala.concurrent.ExecutionContext
import ExecutionContext.Implicits.global
import scala.io.Source


object BlockingAwait extends App {

    val urlSpecSizeFuture: Future[Int] = Future {
        val specUrl = "http://www.w3.org/Addressing/URL/url-spec.txt"
        Source.fromURL(specUrl).size
    }
    val urlSpecSize = Await.result(urlSpecSizeFuture, 10.seconds)
    log(s"url spec contains $urlSpecSize characters")

}


object BlockingSleepBad extends App {

    val startTime = System.nanoTime()
    val futures = for {
        t <- 0 until 32 //my computer has 8 cores, lol
    } yield Future { Thread.sleep(1000) }
    for (f <- futures) Await.ready(f, Duration.Inf)
    val endTime = System.nanoTime()

    //1s = 10^9 ns
    log(s"total time = ${(endTime - startTime) / 1000000} ms")
    log(s"total cpus = ${Runtime.getRuntime.availableProcessors()}")

}


//这里的一个核心观点是，线程数/任务数远大于CPU核心数且并行执行时，容易造成thread starvation
//因为每个线程是分时段调用cpu核心资源的
object BlockingSleepOk extends App {

    val startTime = System.nanoTime()
    val futures = for (t <- 0 until 16) yield Future {
        blocking { Thread.sleep(1000) }
    }

    for (f <- futures) Await.ready(f, Duration.Inf)
    val endTime = System.nanoTime()
    log(s"Total execution time of the program = ${(endTime - startTime) / 1000000} ms")
}





















