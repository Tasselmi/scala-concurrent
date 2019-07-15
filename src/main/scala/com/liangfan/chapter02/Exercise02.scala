package com
package liangfan
package chapter02

//Exercise02
object Ex01 {
    /**
      * a和b并行计算
      * @param a 返回类型为A的延迟计算代码块
      * @param b 返回类型为B的延迟计算代码块
      * @tparam A
      * @tparam B
      * @return 返回A/B类型组成的元组
      */
    def parallel[A, B](a: => A, b: => B): (A, B) = {
        var aVal: A = null.asInstanceOf[A]
        var bVal: B = null.asInstanceOf[B]

        val t1 = thread {
            aVal = a
            log(aVal.toString)
        }

        val t2 = thread {
            bVal = b
            log(bVal.toString)
        }

        t1.join()
        t2.join()

        (aVal, bVal)
    }
}


object Ex02 extends App {
    /**
      * 每隔duration时间段，执行一次f
      * @param duration 毫秒为单位的时间
      * @param f 返回类型为unit的操作
      */
    def periodically(duration: Long)(f: () => Unit): Unit = {
        val worker = new Thread {
            while (true) {f(); Thread.sleep(duration)}
        }

        worker.setName("Worker")
        worker.setDaemon(true)
        worker.start()
    }
    periodically(2000)(() => log("123"))
}


object Ex03 {
    class SyncVar[T] {
        private var empty = true
        private var x = null.asInstanceOf[T]

        def get(): T = this.synchronized {
            if (empty) throw new Exception("must be nonEmpty")
            else {
                empty = true
                val v = x
                x = null.asInstanceOf[T]
                v
            }
        }
        def put(x: T): Unit = this.synchronized {
            if (!empty) throw new Exception("must not be empty")
            else {
                empty = false
                this.x = x
            }
        }
    }
}


object Ex04 extends App {
    class SyncVar2[T] {
        private var empty = true
        private var x = null.asInstanceOf[T]

        def get(): T = this.synchronized {
            if (empty) throw new Exception("must be non-empty")
            else {
                empty = true
                x
            }
        }

        def put(x: T): Unit = this.synchronized {
            if (!empty) throw new Exception("must be empty")
            else {
                empty = false
                this.x = x
            }
        }

        def isEmpty: Boolean = synchronized { empty }

        def nonEmpty: Boolean = synchronized { !empty }
    }
    val syncVar = new SyncVar2[Int]
    val producer = thread {
        var x = 0
        while (x < 15) {
            if (syncVar.isEmpty) {
                syncVar.put(x)
                x += 1
            }
        }
    }
    val consumer = thread {
        var x = 0
        while (x != 15) {
            if (syncVar.nonEmpty) {
                log(s"get = ${syncVar.get()}")
                x += 1
            }
        }
    }
    producer.join()
    consumer.join()
}


object Ex05 extends App {
    class SyncVar3[T] {
        private var empty = true

        private var x = null.asInstanceOf[T]

        def isEmpty: Boolean = synchronized { empty }

        def nonEmpty: Boolean = synchronized { !empty }

        def getWait: T = this.synchronized {
            while (empty) this.wait()
            empty = true
            this.notify()
            x
        }

        def putWait(a: T): Unit = this.synchronized {
            while (!empty) this.wait()
            empty = false
            this.x = a
            this.notify()
        }
    }

    val syncVar3 = new SyncVar3[Int]
    val producer3 = thread {
        var x = 0
        while (x < 15) {
            syncVar3.putWait(x)
            x += 1
        }
    }
    val consumer3 = thread {
        var x = 0
        while (x < 14) {
            x = syncVar3.getWait
            log(s"new get: $x")
        }
    }
    producer3.join()
    consumer3.join()
}


object Ex06 extends App {
    import scala.collection.mutable.Queue
    class SyncQueue[T](val n: Int) {
        private var syncQueue = Queue[T]()

        def getWait: T = this.synchronized {
            while (syncQueue.isEmpty) this.wait()
            val x = syncQueue.dequeue() //出列
            this.notify() //释放锁
            x
        }

        def putWait(a: T): Unit = this.synchronized {
            while (syncQueue.length == n) this.wait()
            syncQueue += a
            this.notify()
        }
    }

    val syncQueue1 = new SyncQueue[Int](10)
    val producer4 = thread {
        var x = 0
        while (x < 15) {
            syncQueue1.putWait(x)
            x += 1
        }
    }
    val consumer4 = thread {
        var x = 0
        while (x < 15) {
            x = syncQueue1.getWait
            log(s"queue get: $x")
        }
    }
    producer4.join()
    consumer4.join()
}


object Ex07 extends App {
    import ThreadsProtectedUid.getUniqueId

    class Account(val name: String, var money: Int) {
        val uid = getUniqueId()
    }

    /**
      * 一对一账户转账
      * @param a1
      * @param a2
      * @param n
      */
    def send(a1: Account, a2: Account, n: Int): Unit = {
        def adjust(): Unit = {
            a1.money -= n
            a2.money += n
        }

        if (a1.uid < a2.uid) {
            a1.synchronized {
                a2.synchronized { adjust() }
            }
        } else {
            a2.synchronized {
                a1.synchronized { adjust() }
            }
        }
    }

    /**
      * 多对一账户转账
      * @param accounts
      * @param target
      */
    def sendAll(accounts: Set[Account], target: Account): Unit = {
        //调整转账账户和接收账户的额度值
        def adjust(): Unit = {
            //注意这里的签名： foldLeft(b)(op: (b, Account)=>b )
            target.money = accounts.foldLeft(0) {
                (b, acc) => {
                    val m = acc.money //获取账户额度，用中间变量置换出来
                    acc.money = 0 //转账完毕后，原账户剩余额度重置为0
                    b + m //目标账户的钱需要把发送的账户的钱都加起来
                }
            }
        }

        //使用递归进行每个账户的转账操作
        def sendAllWithSynchronize(la: List[Account]): Unit = la match {
            case h :: t => h.synchronized { sendAllWithSynchronize(t) }
            case _ => adjust()
        }

        //为了防止死锁，需要对资源的有序性进行安排
        sendAllWithSynchronize((target :: accounts.toList).sortBy(_.uid))
    }

    val accounts = (0 to 100).map(i => new Account(s"Account: $i", i * 10)).toSet
    val target = new Account("target account", 0)

    sendAll(accounts, target)

    accounts.foreach { a => log(s"${a.name}, money = ${a.money}") }
    log(s"${target.name} - money = ${target.money}")
}


import scala.collection.mutable
object Ex08 extends App {
    class PriorityTaskPool {
        implicit val ord: Ordering[(Int, () => Unit)] = Ordering.by(_._1)

        private val tasks = mutable.PriorityQueue[(Int, () => Unit)]()

        def asynchronous(priority: Int)(task: => Unit): Unit = tasks synchronized {
            tasks.enqueue((priority, () => task))
            tasks.notify()
        }

        object Worker extends Thread {
            setDaemon(true)

            def poll: (Int, () => Unit) = tasks synchronized { //轮询
                while (tasks.isEmpty) tasks.wait()
                log("queue + " + tasks.foldLeft("")((i, t) => s"$i|${t._1}, "))
                tasks.dequeue()
            }

            override def run(): Unit = {
                while (true) {
                    poll match {
                        case (_, task) => task()
                    }
                }
            }
        }
        Worker.start()
    }

    val ts = new PriorityTaskPool

    0 to 10 foreach {
        i => {
            val a = (Math.random()*1000).toInt
            ts.asynchronous(a)(log(s"<- $a"))
        }
    }

    Thread.sleep(10000)
}


object Ex09 extends App {

    class PriorityTaskPool(val p: Int) {
        implicit val ord: Ordering[(Int, () => Unit)] = Ordering.by(_._1)

        private val tasks = mutable.PriorityQueue[(Int, () => Unit)]()

        def asynchronous(priority: Int)(task: => Unit): Unit = tasks synchronized {
            tasks.enqueue((priority, () => task))
            tasks.notify()
        }

        class Worker extends Thread {
            setDaemon(true)

            def poll: (Int, () => Unit) = tasks synchronized { //轮询
                while (tasks.isEmpty) tasks.wait()
                log("queue + " + tasks.foldLeft("")((i, t) => s"$i|${t._1}, "))
                tasks.dequeue()
            }

            override def run(): Unit = {
                while (true) {
                    poll match {
                        case (_, task) => task()
                    }
                }
            }
        }

        (1 to p).map(i => new Worker()).map(_.start())
    }

    val ts = new PriorityTaskPool(10)

    0 to 100 foreach {
        i => {
            val a = (Math.random()*1000).toInt
            ts.asynchronous(a)(log(s"<- $a"))
        }
    }

    Thread.sleep(10000)
}


object Ex10 extends App {

    class PriorityTaskPool(val p: Int, val importance: Int) {
        implicit val ord: Ordering[(Int, () => Unit)] = Ordering.by(_._1)

        private val tasks = mutable.PriorityQueue[(Int, () => Unit)]()

        @volatile
        private var terminated = false

        def asynchronous(priority: Int)(task: => Unit): Unit = tasks synchronized {
            tasks.enqueue((priority, () => task))
            tasks.notify()
        }

        class Worker extends Thread {
            //setDaemon(true)  //如果setDaemon的话，那么主线程（用户线程）执行完毕的时候会被关闭

            def poll: (Int, () => Unit) = tasks synchronized { //轮询
                while (tasks.isEmpty) tasks.wait()
                log("queue + " + tasks.foldLeft("")((i, t) => s"$i|${t._1}, "))
                tasks.dequeue()
            }

            override def run(): Unit = {
                while (true) {
                    poll match {
                        //比较重要，或者没有接收到停止消息的，那么继续运行
                        //如果没有接收到停止消息，那么所有都运行
                        //如果接收到停止消息，那么不重要的停止运行（只有重要的运行）
                        case (p, task) if (p > importance) || (!terminated) => task()
                        case _ =>
                    }
                }
            }
        }

        def shutdown: Unit = tasks.synchronized {
            terminated = true
            tasks.notify()
        }

        (1 to p).map(i => new Worker()).map(_.start())
    }

    val ts = new PriorityTaskPool(10, 300)

    0 to 1000 foreach {
        i => {
            val a = (Math.random()*1000).toInt
            ts.asynchronous(a)(log(s"<- $a"))
        }
    }

    Thread.sleep(1)
    ts.shutdown

}














































