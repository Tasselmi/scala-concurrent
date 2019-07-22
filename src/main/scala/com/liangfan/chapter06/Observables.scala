package com
package liangfan
package chapter06


import java.io.File

import rx.lang.scala._

import scala.concurrent.duration._
import org.apache.commons.io.monitor._

object ObservablesItems extends App {

    val o = Observable.items("Pascal", "Java", "Scala")
    o.subscribe(name => log(s"learned the $name language"))
    o.subscribe(name => log(s"forgot the $name language"))

}


object ObservablesTimer extends  App {

    val o = Observable.timer(1.second)
    o.subscribe(_ => log(s"Timeout!"))
    o.subscribe(_ => log(s"Another timeout!"))
    Thread.sleep(2000)

}


object ObservablesExceptions extends App {

    val exc = new RuntimeException
    val o = Observable.items(1, 2 ) ++ Observable.error(exc)
    o.subscribe(
        x => log(s"number $x"),
        t => log(s"an error occurred: $t")
    )

}


object ObservablesLifetime extends App {

    val classics = List("Il buono, il brutto, il cattivo.", "Back to the future", "Die Hard")
    val o = Observable.from(classics)

    o.subscribe(new Observer[String] {
        override def onNext(value: String): Unit = log(s"movie watchlist - $value")

        override def onError(error: Throwable): Unit = log(s"ooops - $error")

        override def onCompleted(): Unit = log("no more movies.")
    })

}


object ObservablesCreate extends App {

    val vms = Observable.apply[String] {
        obs => {
            obs.onNext("JVM")
            obs.onNext(".NET")
            obs.onNext("DartVM")
            obs.onCompleted()
            Subscription
        }
    }

    log(s"About to subscribe")
    vms.subscribe(log _, e => log(s"oops - $e"), () => log("Done!"))
    log(s"About to subscribe")

}


object ObservablesSubscriptions extends  App {

    def modefiedFiles(dir: String): Observable[String] = {
        Observable.apply { obs =>
            val fileMonitor = new FileAlterationMonitor(1000)
            val fileObs = new FileAlterationObserver(dir)
            val fileLis = new FileAlterationListenerAdaptor {
                override def onFileChange(file: java.io.File): Unit = {
                    obs.onNext(file.getName)
                }
            }
            fileObs.addListener(fileLis)
            fileMonitor.addObserver(fileObs)
            fileMonitor.start()

            Subscription { fileMonitor.stop() }
        }
    }

    val subscription = modefiedFiles(".").subscribe {
        fn => log(s"$fn modified!")
    }

    Thread.sleep(10000)

    subscription.unsubscribe()
    log(s"monitoring done")

}


object ObservablesHot extends App {

    val fileMonitor = new FileAlterationMonitor(1000)
    fileMonitor.start()

    def modifiedFiles(dir: String): Observable[String] = {
        val fileObs = new FileAlterationObserver(dir)
        fileMonitor.addObserver(fileObs)
        Observable.apply { obs =>
            val fileLis = new FileAlterationListenerAdaptor {
                override def onFileChange(file: java.io.File): Unit = {
                    obs.onNext(file.getName)
                }
            }
            fileObs.addListener(fileLis)
            Subscription { fileObs.removeListener(fileLis) }
        }
    }

    log(s"first subscribe call")
    val subscription1 = modifiedFiles(".").subscribe(filename => log(s"$filename modified!"))

    Thread.sleep(6000)

    log(s"another subscribe call")
    val subscription2 = modifiedFiles(".").subscribe(filename => log(s"$filename changed!"))

    Thread.sleep(6000)

    log(s"unsubscribed second call")
    subscription2.unsubscribe()

    Thread.sleep(6000)

    fileMonitor.stop()

}


















