package com.liangfan

package object chapter02 {

    def thread(body: => Unit): Thread = {
        val t = new Thread {
            override def run(): Unit = body
        }
        t.start()
        t
    }

}
