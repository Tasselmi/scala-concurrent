package com
package liangfan
package chapter02

object Exercise02 extends App {

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

    def periodically(duration: Long)(f: () => Unit): Unit = {
        val worker = new Thread {
            while (true) {f(); Thread.sleep(duration)}
        }

        worker.setName("Worker")
        worker.setDaemon(true)
        worker.start()
    }
    periodically(2000)(() => log("123"))

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
