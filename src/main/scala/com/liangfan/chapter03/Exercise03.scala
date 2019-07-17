package com
package liangfan
package chapter03


import scala.concurrent._
import scala.util.{Try, Success, Failure}
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




















