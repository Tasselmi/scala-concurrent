package com
package liangfan
package chapter03


import java.io.{FileOutputStream, ObjectOutputStream}
import java.util.regex.Pattern

import scala.concurrent._
import scala.util.{Failure, Success, Try}
object Ex01 extends App {

    class PiggybackContext extends ExecutionContext {
        override def execute(runnable: Runnable): Unit = Try(runnable.run()) match {
            case Success(s) => log("result: OK")
            case Failure(f) => reportFailure(f)
        }

        override def reportFailure(cause: Throwable): Unit = {
            log(s"error: ${cause.getMessage}")
        }
    }

    val p = new PiggybackContext

    p.execute(new Runnable {
        override def run(): Unit = {
            log("run(exception)")
            throw new Exception("test exception")
        }
    })

    p.execute(new Runnable {
        override def run(): Unit = log("run")
    })
}


import java.util.concurrent.atomic.AtomicReference
import scala.annotation.tailrec
object Ex02 extends App {

    class TreiberStack[T] {  //先进后出
        var r = new AtomicReference[List[T]](List.empty[T])

        @tailrec
        final def push(x: T): Unit = {
            val ol = r.get()
            val nl = x :: ol //x压到栈顶
            if (!r.compareAndSet(ol, nl)) push(x)
        }

        @tailrec
        final def pop: T = {
            val ol = r.get()
            val nl = ol.tail
            if (r.compareAndSet(ol, nl)) ol.head //拿到栈顶
            else pop
        }
    }

    import com.liangfan.chapter02.thread

    val s = new TreiberStack[Int]

    val t1 = thread {
        for (i <- 1 to 10) { s.push(i); Thread.sleep(10) }
    }

    val t2 = thread {
        for (i <- 1 to 10) { s.push(i * 10); Thread.sleep(10) }
    }

    t1.join()
    t2.join()

    for (i <- 1 to 20)
        log(s"s[$i] = ${s.pop}")

}


import com.liangfan.chapter02.thread
object Ex0304 extends App {
    class ConcurrentSortedList[T](implicit val ord: Ordering[T]) {

        case class Node(
            head: T,
            tail: AtomicReference[Option[Node]] = new AtomicReference[Option[Node]](None)
        )

        val root = new AtomicReference[Option[Node]](None)

        @tailrec
        private def add(r: AtomicReference[Option[Node]], x: T): Unit = {
            val optNode = r.get()

            optNode match {
                //如果optNode为None那么直接将x封装后替代optNode即可，如果不成功，那么递归重试
                case None => { if (!r.compareAndSet(optNode, Some(Node(x)))) add(r, x) }
                case Some(Node(h, t)) => {
                    if (ord.compare(x, h) <= 0) { // x <= head
                        //先创建一个newNode，更新好数据后，再去整体替换optNode
                        val newNode = Node(x)
                        newNode.tail.set(optNode) //这里注意了，方法比较巧妙，Node对象有set方法
                        if (!r.compareAndSet(optNode, Some(newNode))) add(r, x)
                    } else {
                        //如果 x > h，那么比较麻烦了，需要递归的去一个一个和下一个元素比较，直到满足退出条件case None
                        add(t, x)
                    }
                }
            }
        }

        def add(x: T): Unit = add(root, x) //方法重载，对外暴露的接口

        def iterator: Iterator[T] = new Iterator[T] {

            var rt: Option[Node] = root.get()

            override def hasNext: Boolean = rt.isDefined

            override def next(): T = rt match {
                case Some(node) => { rt = node.tail.get(); node.head } //调用一次next去掉一次head
                case None => throw new NoSuchElementException("next on empty iterator")
            }
        }
    }

    val csl = new ConcurrentSortedList[Int]()

    (1 to 100).map(
        i => thread {
            Thread.sleep((Math.random() * 100).toInt) //0-100ms
            for (j <- 1 to 1000) {
                Thread.sleep((Math.random() * 10).toInt)
                csl.add((math.random * 100 + i).toInt)
            }
        }
    ).foreach(_.join()) //等待100个线程都运行完毕

    log(s"length = ${csl.iterator.length}")

    var prev = 0
    var length = 0
    for (a <- csl.iterator) {
        log(a.toString)
        if (prev > a) throw new Exception(s"$prev > $a")
        prev = a
        length += 1
    }

    if (csl.iterator.length != length) throw new Exception(s"${csl.iterator.length} != $length")

    log(s"length = ${csl.iterator.length} ($length)")
}


object Ex05 extends App {

    class LazyCellWithLazy[T](initialization: => T) {
        lazy val l = initialization
    }

    class LazyCell[T](initialization: =>T) {
        var r: Option[T] = None

        @volatile
        def apply: T = r match {
            case Some(v) => v
            case None => this.synchronized {
                r = Some(initialization)
                r.get
            }
        }
    }

    def func: String = {
        log("start...")
        Thread.sleep(1000)
        s"Calculation by ${Thread.currentThread().getName}"
    }

    val a = new LazyCell[String](func)

    log("Start")

    val b = new LazyCellWithLazy[String](func)

    (0 to 50).map(
        i => thread {
            Thread.sleep((Math.random * 10).toInt)
            println(a.apply)
        }
    ).foreach(_.join())

}


object Ex06 extends App {

    class PureLazyCell[T](initialization: => T) {

        val r = new AtomicReference[Option[T]](None)

        final def apply(): T = r.get() match {
            case Some(v) => v
            case None => {
                val v = initialization
                if (!r.compareAndSet(None, Some(v))) apply()
                else v
            }
        }

    }

    def initialization = {
        log("calculation ...")
        Thread.sleep(1000)
        s"result (calculate by ${Thread.currentThread().getName})"
    }

    val p = new PureLazyCell[String](initialization)

    log("start")

    val t = (1 to 10).map(
        i => thread {
            val sleep = (math.random * 2000).toInt
            Thread.sleep(sleep)
            (1 to 3).foreach { i => log(s"v$i = ${p.apply()}") }
        }
    )

    t.foreach(_.join())
}


import scala.collection.mutable
object Ex07 extends App {

    class SyncConcurrentMap[A, B] extends scala.collection.concurrent.Map[A, B] {

        private val m = mutable.Map.empty[A, B]

        override def putIfAbsent(k: A, v: B): Option[B] = m synchronized { //拿到m的监视器锁
            m.get(k) match {
                case opt@Some(_) => opt
                case None => m.put(k, v)
            }
        }

        def replace(k: A, oldvalue: B, newvalue: B): Boolean = m synchronized {
            m.get(k) match {
                //v不为null且v等于老值 || v和老值军等于null
                //这里之所以要分开写是因为对null值使用equals方法会报空指针错误 => null.equals(null)
                case Some(v) if (v != null && v.equals(oldvalue)) || (v == null && oldvalue == null) => {
                    m.put(k, newvalue)
                    true
                }
                case _ => true
            }
        }

        override def replace(k: A, v: B): Option[B] = m synchronized {
            m.get(k) match {
                case ol@Some(x) => { m.put(k, v); ol } //put方法会覆盖
                case None => None
            }
        }

        def remove(k: A, v: B): Boolean = m synchronized {
            m.get(k) match {
                case Some(ov) if (ov != null && ov.equals(v)) || (ov == null && v == null) => {
                    m.remove(k)
                    true
                }
                case _ => false
            }
        }

        override def +=(kv: (A, B)): SyncConcurrentMap.this.type = m synchronized {
            m.put(kv._1, kv._2)
            this
        }

        override def -=(key: A): SyncConcurrentMap.this.type = m synchronized {
            m.remove(key)
            this
        }

        override def get(key: A): Option[B] = m synchronized {
            m.get(key)
        }

        override def iterator: Iterator[(A, B)] = m synchronized {
            m.iterator
        }

    }

    val m = new SyncConcurrentMap[Int, String]()

    val t = (1 to 100).map(
        i => thread {
            (1 to 100).foreach {
                k => {
                    val v = s"${Thread.currentThread().getName}"
                    m.put(k, v)
                }
            }
        }
    )

    Thread.sleep(100)

    for ((k, v) <- m) log(s"<- ($k, $v)")

    t.foreach(_.join)

}


import java.io._
import java.util.regex.Pattern
import scala.sys.process._

object Ex08 extends App {

    def spawn[T](block: => T): T = {

        val className: String = EvaluationApp.getClass().getName().split((Pattern.quote("$")))(0)
        val tmp = File.createTempFile("concurrent-programming-in-scala", null)
        tmp.deleteOnExit()

        val out = new ObjectOutputStream(new FileOutputStream(tmp))
        try {
            out.writeObject(() => block)
        } finally {
            out.close()
        }

        val ret = Process(s"java -cp ${System.getProperty("java.class.path")} $className ${tmp.getCanonicalPath}").!
        if (ret != 0) throw new RuntimeException("fails to evaluate block in a new JVM process")

        val in = new ObjectInputStream(new FileInputStream(tmp))
        try {
            in.readObject() match {
                case e: Throwable => throw e
                case x => x.asInstanceOf[T]
            }
        } finally {
            in.close()
            tmp.delete()
        }

    }

    val s1 = spawn({ 1 + 1 })
    assert(s1 == 2)

    try {
        spawn({ "test".toInt })
    } catch {
        case e: NumberFormatException =>
        case _: Throwable => assert(false)
    }

    try {
        spawn({ System.exit(0) })
    } catch  {
        case e: SecurityException =>
        case _: Throwable => assert(false)
    }

}

























































