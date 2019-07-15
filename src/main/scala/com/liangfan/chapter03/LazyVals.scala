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