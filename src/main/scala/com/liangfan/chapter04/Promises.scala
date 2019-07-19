package com
package liangfan
package chapter04


import java.io.File

import scala.concurrent.{Future, Promise}
import scala.concurrent._
import ExecutionContext.Implicits.global
import scala.util.control.NonFatal
import org.apache.commons.io.monitor._
import java.util._

object PromisesCreate extends App {

    val p = Promise[String]
    val q = Promise[String]

    p.future foreach { case x => log(s"p succeeded with '$x'") }
    Thread.sleep(1000)
    p success "assigned"
    q failure new Exception("not kept")
    q.future.failed foreach { case t => log(s"q failed with $t") }
    Thread.sleep(1000)

}


object PromisesCustomAsync extends App {

    def myFuture[T](b: => T): Future[T] = {
        val p = Promise[T]
        global.execute(new Runnable {
            override def run(): Unit = try {
                p.success(b)
            } catch {
                case NonFatal(e) => p.failure(e)
            }
        })
        p.future
    }

    val f = myFuture { "naa" + "na"*8 + " Katamari Damacy!" }
    f foreach { case t => log(t) }

    Thread.sleep(200)

}


object PromisesAndCallbacks extends App {

    def fileCreated(dir: String): Future[String] = {
        val p = Promise[String]
        val fileMonitor = new FileAlterationMonitor(1000)
        val observer = new FileAlterationObserver(dir)

        val listener = new FileAlterationListenerAdaptor {
            override def onFileCreate(file: File): Unit =
                try p.trySuccess(file.getName)
                finally fileMonitor.stop()
        }

        observer.addListener(listener)
        fileMonitor.addObserver(observer)
        fileMonitor.start()
        p.future
    }

    fileCreated(".") foreach {
        case fn => log(s"detected new file '$fn'")
    }

    private val timer = new Timer(true)

    def timeout(t: Long): Future[Unit] = {
        val p = Promise[Unit]
        timer.schedule(new TimerTask {
            override def run(): Unit = {
                p success ()
                timer.cancel()
            }
        }, t)
        p.future
    }

    timeout(1000) foreach { case _ => log("Time out!") }

    Thread.sleep(2000)
}


object PromisesAndCustomOperations extends App {

    implicit class FutureOps[T](val self: Future[T]) {
        def or(that: Future[T]): Future[T] = {
            val p = Promise[T]
            self onComplete { case x => p tryComplete(x) }
            that onComplete { case y => p tryComplete(y) }
            p.future
        }
    }

    val f = Future("now") or Future("later")

    f foreach {
        case when => log(s"The future is $when")
    }

    Thread.sleep(100)

}


object PromisesAndTimers extends App {

    import PromisesAndCustomOperations._

    private val timer = new Timer(true)

    def timeout(millis: Long): Future[Unit] = {
        val p = Promise[Unit]
        timer.schedule(new TimerTask {
            override def run(): Unit = p.success(())
        }, millis)
        p.future
    }

    val f = timeout(100).map(t => "timeout") or Future {
        Thread.sleep(500)
        "work completed!"
    }

    f foreach(t => log(t))

    Thread.sleep(2000)

}


object PromisesCancellation extends App {

    type Cancelable[T] = (Promise[Unit], Future[T])

    def cancellable[T](b: Future[Unit] => T): Cancelable[T] = {
        val cancel = Promise[Unit]
        val f = Future {
            val r = b(cancel.future)
            if (!cancel.tryFailure(new Exception))
                throw new CancellationException
            r
        }
        (cancel, f)
    }

    val (cancel, value) = cancellable {
        cancel =>
            var i = 0
            while (i < 5) {
                if (cancel.isCompleted) throw new CancellationException
                Thread.sleep(500)
                log(s"$i: working")
                i += 1
            }
            "resulting value"
    }

    Thread.sleep(1500)
    cancel.trySuccess(())
    log("computation cancelled!")
    Thread.sleep(2000)

}









































