package com
package liangfan
package chapter03


import scala.concurrent._
import scala.annotation.tailrec
import java.util.concurrent._
import com.liangfan.chapter02.thread

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
