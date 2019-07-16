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


import scala.collection.convert.decorateAsScala._
object CollectionsConcurrentMapBulk extends App {  //bad bad 啥也没有
    val names = new ConcurrentHashMap[String, Int]().asScala
    names("Johnny") = 0
    names("Jane") = 0
    names("Jack") = 0
    execute { for (n <- 0 until 10) names(s"John $n") = n }
    execute { for (n <- names) log(s"name: $n") }
    Thread.sleep(1000)
}


//发现是主线程运行的太快了，需要sleep一下下
object CollectionsTrieMapBulk extends App { //bad bad。。。也是啥玩意儿都没有啊
    val names = new concurrent.TrieMap[String, Int]()
    names("Janice") = 0
    names("Jackie") = 0
    names("Jill") = 0
    execute { for (n <- 10 until 100) names(s"John $n") = n }
    execute {
        log("snapshot time !")
        for (n <- names.keys.toSeq.sorted) log(s"name: $n")
    }
    Thread.sleep(1000)
    //names.keys foreach(println)
}


