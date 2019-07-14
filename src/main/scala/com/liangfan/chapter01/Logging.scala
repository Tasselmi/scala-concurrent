package com.liangfan.chapter01

trait Logging {

    def log(s: String): Unit

    def warn(s: String): Unit = log("warn:" + s)

    def error(s: String): Unit = log("error:" + s)

}

class PrintLogging extends Logging {
    override def log(s: String): Unit = println(s)
}
