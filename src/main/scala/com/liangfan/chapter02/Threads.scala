package com.liangfan
package chapter02


object ThreadMain extends App {

    val t = Thread.currentThread()
    val name = t.getName
    println(s"I am the thread $name")

}


object ThreadsCreation extends App {
    class MyThread extends Thread {
        override def run(): Unit = {
            println("New thread running.")
        } }
    val t = new MyThread
    t.start()
    t.join()
    println("New thread joined.")
}


object ThreadsSleep extends App {

    val t = thread {
        Thread.sleep(1000)
        log("new thread running...")
        Thread.sleep(1000)
        log("still running...")
        Thread.sleep(1000)
        log("completed...")
    }
    t.join()
    log("new thread joined...")

}


object ThreadsNondeterminism extends App {
    val t = thread { log("New thread running.") }
    log("...")
    log("...")
    t.join()
    log("New thread joined.")
}


object ThreadsCommunicate extends App {
    var result: String = null
    val t = thread { result = "\nTitle\n" + "=" * 5 }
    t.join()
    log(result)
}


object ThreadsUnprotectedUid extends App {
    var uidCount = 0L

    def getUniqueId(): Long = {
        val freshUid = uidCount + 1
        uidCount = freshUid
        freshUid
    }

    def printUniqueIds(n: Int): Unit = {
        val uids = for (i<- 0 until n) yield getUniqueId()
        log(s"Generated uids: $uids")
    }

    val t = thread { printUniqueIds(5) }
    printUniqueIds(5)
    t.join()
}

