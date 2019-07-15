package com
package liangfan
package chapter03

import scala.collection._

object CollectionsBad extends App {
    val buffer = mutable.ArrayBuffer[Int]()
    def asyncAddBad(numbers: Seq[Int]): Unit = execute {
        buffer ++= numbers
        log(s"buffer = $buffer")
    }

    def asyncAdd(numbers: Seq[Int]): Unit = execute {
        buffer.synchronized {
            buffer ++= numbers
            log(s"buffer = $buffer")
        }
    }

    asyncAdd(0 until 10)
    asyncAdd(10 until 20)

//    asyncAdd(0 until 10)
//    asyncAdd(10 until 20)
    Thread.sleep(500)
}


import java.util.concurrent._
object CollectionsIterators extends App {
    val queue = new LinkedBlockingDeque[String]()
    for (i <- 1 to 5500) queue.offer(i.toString)
    execute {
        // We say that the iterator is not consistent
        val it = queue.iterator()
        while (it.hasNext) log(it.next())
    }
    for (i <- 1 to 5500) queue.poll()
    Thread.sleep(1000)
}
