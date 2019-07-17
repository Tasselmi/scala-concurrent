package com
package liangfan
package chapter03

import scala.sys.process._
object ProcessRun extends App {
    val command = "ls"
    val exitcode = command.!
    log(s"command exited with status $exitcode")

    def lineCount(filename: String) = {
        val output = s"wc $filename".!!
        output.trim.split(" ").head.toInt
    }

    log(lineCount("./sbt.build").toString)
}


object ProcessAsync extends App {
    val lsProcess = "ls -R /".run()
    Thread.sleep(1000)
    log("Timeout - killing ls !")
    lsProcess.destroy()
}