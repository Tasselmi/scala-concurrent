package com
package liangfan

package object chapter03 {
    import scala.concurrent.ExecutionContext

    def execute(body: => Unit): Unit = ExecutionContext.global.execute(
        new Runnable {
            override def run(): Unit = body
        }
    )
}
