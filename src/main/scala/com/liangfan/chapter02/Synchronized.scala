package com
package liangfan
package chapter02

object SynchronizedNesting extends App {

    import scala.collection._

    private val transfers = mutable.ArrayBuffer[String]()

    def logTransfer(name: String, n: Int) =
        transfers.synchronized {
            transfers += s"transfer to account '$name' = $n"
        }

    class Account(val name: String, var money: Int)

    def add(account: Account, n: Int) =
        account.synchronized {
            account.money += n
            if (n > 10) logTransfer(account.name, n)
        }

    val jane = new Account("Jane", 100)
    val john = new Account("John", 200)
    val t1 = thread { add(jane, 5) }
    val t2 = thread { add(john, 50) }
    val t3 = thread { add(jane, 70) }

    t1.join()
    t2.join()
    t3.join()

    println(jane.money)

    log(s"--- transfers ---\n$transfers")

}



object SynchronizedDeadlock extends App {

    import SynchronizedNesting.Account

    def send(a: Account, b: Account, n: Int) =
        a.synchronized {
            b.synchronized {
                a.money -= n
                b.money += n
            }
        }

    val a = new Account("Jack", 1000)
    val b = new Account("Jill", 2000)
    val t1 = thread { for (i<- 0 until 100) send(a, b, 1) }
    val t2 = thread { for (i<- 0 until 100) send(b, a, 1) }
    t1.join(); t2.join()
    log(s"a = ${a.money}, b = ${b.money}")
}



//避免死锁的最好方式就是让线程有序的获得资源锁
object SynchronizedNotDeadlock extends App {

    //import SynchronizedNesting.Account
    import com.liangfan.chapter02.ThreadsProtectedUid.getUniqueId

    class Account(val name: String, var money: Int) {
        val uid = getUniqueId()
    }

    def send(a1: Account, a2: Account, n: Int): Unit = {
        def adjust(): Unit = {
            a1.money -= n
            a2.money += n
        }

        if (a1.uid < a2.uid) a1.synchronized {
            a2.synchronized {
                adjust()
            }
        } else a2.synchronized {
            a1.synchronized {
                adjust()
            }
        }
    }

    val a = new Account("Jack", 1000)
    val b = new Account("Jill", 2000)
    val t1 = thread { for (i<- 0 until 100) send(a, b, 1) }
    val t2 = thread { for (i<- 0 until 100) send(b, a, 2) }
    t1.join(); t2.join()
    log(s"a = ${a.money}, b = ${b.money}")

}


import scala.collection._
object SynchronizedBadPool extends App {

    private val tasks = mutable.Queue[() => Unit]()

    val worker = new Thread {
        def poll(): Option[() => Unit] = tasks.synchronized {
            if (tasks.nonEmpty) Some(tasks.dequeue()) else None
        }

        override def run(): Unit = while (true) poll() match {
            case Some(task) => task()
            case None =>
        }
    }

    worker.setName("Worker")
    worker.setDaemon(true)
    worker.start()

    def asynchronous(body: => Unit): Unit = tasks.synchronized {
        tasks.enqueue(() => body)
    }

    asynchronous(log("hello"))
    asynchronous(log("world!"))

    Thread.sleep(10000)

}



object SynchronizedGuardedBlocks extends App {
    val lock = new AnyRef
    var message: Option[String] = None
    val greeter = thread {
        lock.synchronized {
            //guarded block
            while (message.isEmpty) lock.wait() //等待并释放获得的锁
            log(message.get)
        }
    }
    lock.synchronized {
        message = Some("hello")
        lock.notify() //唤醒greeter，greeter开始执行while部分检查
    }
    greeter.join()
}


object SynchronizedPool extends App {
    private val tasks = mutable.Queue[() => Unit]()

    object Worker extends Thread {
        setDaemon(true)

        def poll(): () => Unit = tasks.synchronized {
            //guarded block
            //a synchronized statement which some condition is repetitively checked before calling wait
            while (tasks.isEmpty) tasks.wait()
            tasks.dequeue()
        }

        override def run(): Unit = while (true) {
            val task = poll()
            task()
        }
    }

    Worker.start()

    def asynchronous(body: => Unit): Unit = tasks.synchronized {
        tasks.enqueue(() => body)
        tasks.notify()
    }

    asynchronous(log("hollo"))
    asynchronous(log("world"))

    Thread.sleep(1000)
}


object SynchronizedPool2 extends App {
    private val tasks = mutable.Queue[() => Unit]()

    object Worker extends Thread {
        var terminated = false

        def poll(): Option[() => Unit] = tasks.synchronized {
            while (tasks.isEmpty && !terminated) tasks.wait()
            if (!terminated) Some(tasks.dequeue()) else None
        }

        import scala.annotation.tailrec

        @tailrec
        override def run(): Unit = poll() match {
            case Some(task) => task(); run()
            case None =>
        }

        def shutdown(): Unit = tasks.synchronized {
            terminated = true
            tasks.notify()
        }
    }

    Worker.start()

    def asynchronous(body: => Unit): Unit = tasks.synchronized {
        tasks.enqueue(() => body)
        tasks.notify()
    }

    asynchronous(log("hollo"))
    asynchronous(log("world"))

    Thread.sleep(1000)

    Worker.shutdown()
}






































