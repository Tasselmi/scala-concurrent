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


//出现了竞争条件，并发执行的结果取决于执行的调度结果
//比如下面的例子，每次执行的结果可能都不一样，因为uidCount作为共享变量可能被其他线程修改
//Two concurrent invocations of the getUniqueId method
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


//通过synchronized关键字来实现原子性执行（atomic execution）
object ThreadsProtectedUid extends App {
    var uidCount = 0L

    //We can also call synchronized and omit the this part, in which case the compiler will infer what the surrounding
    // object is, but we strongly discourage you from doing so.
    //Synchronizing on incorrect objects results in concurrency errors that are not easily identified.
    def getUniqueId(): Long = this.synchronized {
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


object ThreadSharedStateAccessReordering extends App {

    for (i <- 0 until 10000) {
        var a = false
        var b = false
        var x = -1
        var y = -1

        val t1 = thread {
            a = true
            y = if (b) 0 else 1
        }

        val t2 = thread {
            b = true
            x = if (a) 0 else 1
        }

        t1.join()
        t2.join()

        assert(!(x == 1 && y == 1), s"x = $x, y = $y")
    }

}










