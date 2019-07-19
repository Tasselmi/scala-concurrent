package com
package liangfan
package chapter04


import scala.concurrent._
import ExecutionContext.Implicits.global

object FuturesCreate extends App {

    Future { log("the future is here") }
    log("the future is coming")
    Thread.sleep(1000)

}


import scala.io.Source
object FuturesDataType extends App {

    val buildFile = Future {
        val f = Source.fromFile("build.sbt")
        try { f.getLines().mkString("\n") } finally f.close()
    }
    log(s"started reading the build file asynchronously")
    log(s"status: ${buildFile.isCompleted}")

    Thread.sleep(250)
    log(s"status: ${buildFile.isCompleted}")
    log(s"value: ${buildFile.value}")

}


import scala.util.{Try, Success, Failure}
object FuturesCallbacks extends App {

    def getUrlSpec: Future[List[String]] = Future {
        val url = "http://www.w3.org/Addressing/URL/url-spec.txt"
        val f = Source.fromURL(url)
        try f.getLines().toList finally f.close()
    }

    val urlSpec: Future[List[String]] = getUrlSpec

    def find(lines: List[String], keyword: String) = {
        lines.zipWithIndex collect {
            case (line, n) if line.contains(keyword) => (n, line)
        } mkString "\n"
    }

    urlSpec foreach {
        case lines => log(find(lines, "telnet"))
    }

    log("callback registered, continuing with other work")
    Thread.sleep(2000)
    println("\n")

    urlSpec foreach {
        case lines => log(find(lines, "password"))
    }

    Thread.sleep(1000)

    urlSpec.onComplete {
        case Success(value) => log(find(value, "txt"))
        case Failure(err) => log(s"exception occurred - $err")
    }

}


object FuturesFailure extends App {

    val urlSpec = Future {
        val invalidUrl = "http://www.w3.org/non-existent-url-spec.txt"
        Source.fromURL(invalidUrl).mkString
    }

    urlSpec.failed foreach {
        case t => log(s"exception occurred - $t")
    }

    Thread.sleep(1000)
}


object FuturesTry extends App {

    val threadName: Try[String] = Try(Thread.currentThread().getName)
    val someText: Try[String] = Try("Try objects are synchronous")

    val message: Try[String] = for {
        tn <- threadName
        st <- someText
    } yield s"Message $st was created on t = $tn"

    def handleMessage(t: Try[String]): Unit = t match {
        case Success(msg) => log(msg)
        case Failure(e) => log(s"unexpected failure - $e")
    }

    handleMessage(message)

}

import scala.util.control.NonFatal
object FuturesNonFatal extends App {

    val f = Future { throw new InterruptedException }
    val g = Future { throw new IllegalArgumentException }

    f.failed foreach { case NonFatal(t) => log(s"$t is non-fatal!") }

    f.failed foreach { case t => log(s"error - $t") }
    g.failed foreach { case t => log(s"error - $t") }

}


import java.io._
import org.apache.commons.io.FileUtils._
import scala.collection.convert.decorateAsScala._
object FuturesClumsyCallback extends App {

    def blacklistFile(name: String): Future[List[String]] = Future {
        val lines = Source.fromFile(name).getLines()
        lines.filter(x => !x.startsWith("#") && x.nonEmpty).toList
    }

    def findFiles(patterns: List[String]): List[String] = {
        val root = new File(".")

        for {
            f <- iterateFiles(root, null ,true).asScala.toList
            pat <- patterns
            abspat = root.getCanonicalPath + File.separator + pat
            if f.getCanonicalPath.contains(abspat)
        } yield f.getCanonicalPath
    }

    def blacklisted(name: String) =
        blacklistFile(name).map(p => findFiles(p))

    blacklistFile(".gitignore") foreach {
        case lines => {
            val files = findFiles(lines)
            log(s"matches: ${files.mkString("\n")}")
        }
    }

    Thread.sleep(1000)

}










































