package com
package liangfan
package chapter03


import scala.concurrent._
import scala.concurrent.forkjoin.ForkJoinPool
object ExecutorsCreate extends App {

    val executor = new forkjoin.ForkJoinPool()
    executor.execute(new Runnable {
        override def run(): Unit = log("this task is run asynchronously.")
    })

//    import java.util.concurrent.TimeUnit
//    executor.shutdown()
//    executor.awaitTermination(3, TimeUnit.SECONDS)

    Thread.sleep(1000)

}


object ExecutionContextGlobal extends App {
    val ectx = ExecutionContext.global
    ectx.execute(new Runnable {
        override def run(): Unit = log("running on the execution context.")
    })
    Thread.sleep(1000)
}


object ExecutionContextCreate extends App {
    val pool = new forkjoin.ForkJoinPool(2)
    val ectx = ExecutionContext.fromExecutorService(pool)
    ectx.execute(new Runnable {
        override def run(): Unit = log("running on the execution context again.")
    })
    Thread.sleep(1000)
}


object ExecutionContextSleep extends App {
    for (i <- 0 until 32) execute {
        Thread.sleep(2000)
        log(s"task $i completed.")
    }
    Thread.sleep(10000)
}