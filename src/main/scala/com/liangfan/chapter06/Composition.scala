package com
package liangfan
package chapter06


import rx.lang.scala._

import scala.concurrent.duration._
import org.apache.commons.io.monitor._
import scala.concurrent.ExecutionContext
import ExecutionContext.Implicits.global
import scala.concurrent.{Future, blocking}
import scala.io.Source


object CompositionMapAndFilter extends  App {

    val odds = Observable.interval(0.5.seconds)
        .filter(_ % 2 == 1)
        .map(n => s"mum $n")
        .take(5)

    val evens = Observable.interval(0.5.seconds)
        .filter(_ % 2 == 0)
        .map(n => s"mum $n")
        .take(5)

    odds.subscribe(log _)
    evens.subscribe(log _)

    Thread.sleep(1000*10)

}


object CompositionConcatAndFlatten extends App {

    def fetchQuote: Future[String] = Future {
        blocking {
            val url = "http://quotes.stormconsultancy.co.uk/random.json"
            Source.fromURL(url).getLines.mkString
        }
    }

    def fetchQuoteObservable: Observable[String] = Observable.from(fetchQuote)

    def quotes: Observable[Observable[String]] = Observable.interval(0.5.seconds)
        .take(5)
        .map {
            n => fetchQuoteObservable.map(t => s"$n) $t")
        }

    log("using concat")
    quotes.concat.subscribe(log _)
    Thread.sleep(6000)

    log("using flatten")
    quotes.flatten.subscribe(log _)
    Thread.sleep(6000)

    log("using flatmap")
    Observable.interval(0.5.seconds).take(5).flatMap({
        n => fetchQuoteObservable.map(txt => s"$n) $txt")
    }).subscribe(log _)
    Thread.sleep(6000)

    log("now using good ol' for-comprehensions")
    val qs = for {
        n <- Observable.interval(0.5.seconds).take(5)
        txt <- fetchQuoteObservable
    } yield s"$n) $txt"
    qs.subscribe(log _)
    Thread.sleep(6000)

}


object CompositionRetry extends App {

    def randomQuote = Observable.apply[String] { obs =>
        val url = "http://www.iheartquotes.com/api/v1/random?" +
            "show_permalink=false&show_source=false"
        obs.onNext(Source.fromURL(url).getLines().mkString)
        obs.onCompleted()
        Subscription
    }

    def errorMessage = Observable.items("Retrying...") ++ Observable.error(new Exception)

    def shortQuote = for {
        txt <- randomQuote
        msg <- if (txt.length < 100) Observable.items(txt) else errorMessage
    } yield msg

    shortQuote.retry(5).subscribe(log _, e => log(s"too long - $e"), () => log("done!"))

}


object CompositionScan extends App {

    CompositionRetry.shortQuote.retry.repeat.take(100).scan(0) {
        (n, q) => if (q == "Retrying...") n+1 else n
    }.subscribe( n => log(s"$n / 100 ") )

}





























