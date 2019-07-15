package com
package liangfan
package chapter03

object LazyValsCreate extends App {
    lazy val obj = new AnyRef
    lazy val non = s"made by ${Thread.currentThread.getName}"
    execute {
        log(s"EC sees obj = $obj")
        log(s"EC sees non = $non")
    }
    log(s"Main sees obj = $obj")
    log(s"Main sees non = $non")
    Thread.sleep(500)
}


object LazyValsObject extends App {
    object Lazy { log("Running Lazy constructor.") }
    log("Main thread is about to reference Lazy.")
    Lazy
    log("Main thread completed.")
}


object LazyValsUnderTheHood extends App {
    @volatile private var _bitmap = false
    private var _obj: AnyRef = _
    def obj = if (_bitmap) _obj else this.synchronized {
        if (!_bitmap) {
            _obj = new AnyRef
            _bitmap = true
        }
        _obj }
    log(s"$obj")
    log(s"$obj")
}


//不管是1个线程还是2个线程，都会报错，切记
object LazyValsDeadlock extends App {
    object A { lazy val x: Int = B.y }
    object B { lazy val y: Int = A.x }
    //execute { B.y }
    B.y
    A.x
}


//t线程尝试或得x，但是主线程已经锁住了
object LazyValsAndBlocking extends App {
    lazy val x: Int = {
        val t = chapter02.thread { println(s"Initializing $x.") }
        t.join()
        1
    }
    x
}


object LazyValsAndMonitors extends App {
    lazy val x = 1
    this.synchronized {
        val t = chapter02.thread { x }
        t.join()
    }
}