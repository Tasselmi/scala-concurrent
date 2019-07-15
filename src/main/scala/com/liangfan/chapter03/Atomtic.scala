package com
package liangfan
package chapter03


import java.util.concurrent.atomic._
object AtomaticUid extends App {
    private val uid = new AtomicLong(0L)
    def getUniqueId() = uid.incrementAndGet()

    execute( log(s"uid asynchronously: ${getUniqueId()}") )
    log(s"got a unique id: ${getUniqueId()}")
}


import scala.annotation.tailrec
object AtomaticUidCAS extends App {
    private val uid = new AtomicLong(0L)

    @tailrec
    def getUniqueId: Long = {
        val oldUid = uid.get()
        val newUid = oldUid + 1

        if (uid.compareAndSet(oldUid, newUid)) newUid
        else getUniqueId
    }

    execute {
        log(s"Got a unique id asynchronously: $getUniqueId")
    }

    log(s"Got a unique id: $getUniqueId")
}


object AtomaticLock extends App {
    private val lock = new AtomicBoolean(false)
    def mySynchronized(body: => Unit): Unit = {
        //This example shows you that we need to define the lock-freedom more carefully
        while (!lock.compareAndSet(false, true)) { }
        try body finally lock.set(false)
    }
    var count = 0
    for (i <- 0 until 10) execute(mySynchronized(count += 1))
    Thread.sleep(1000)
    log(s"count is $count")
}



