package com
package liangfan
package chapter05


import scala.collection._
import scala.util.Random
import java.util.concurrent.atomic._
import scala.concurrent.forkjoin.ForkJoinPool
import scala.io.Source
import scala.concurrent.Future
import scala.concurrent.ExecutionContext
import ExecutionContext.Implicits.global


object ParBasic extends App {

    val numbers = Random.shuffle(Vector.tabulate(50000000)(i => i))
    val seqtime = timed { numbers.max }
    log(s"sequential time $seqtime ms")
    val partime = timed { numbers.par.max }
    log(s"parallel time $partime ms")

}


object ParUid extends App {

    private val uid = new AtomicLong(0L)

    val seqtime = timed {
        for (i <- 0 until 100000000) uid.incrementAndGet()
    }
    log(s"sequential time $seqtime ms")

    val partime = timed {
        for (i <- (0 until 100000000).par) uid.incrementAndGet()
    }
    log(s"parallel time $partime ms")

}


object ParConfig extends App {

    val fjpool = new ForkJoinPool(2)
    val customTaskSupport = new parallel.ForkJoinTaskSupport(fjpool)
    val numbers = Random.shuffle(Vector.tabulate(5000000)(i => i))
    val partime = timed {
        val parnumbers = numbers.par
        parnumbers.tasksupport = customTaskSupport
        val n = parnumbers.max
        println(s"largest number $n")
    }
    log(s"parallel time $partime ms")

}


object ParHtmlSearch extends App {

    def getHtmlSpec = Future {
        val specSrc: Source = Source.fromURL("http://www.w3.org/MarkUp/html-spec/html-spec.txt")
        try specSrc.getLines().toArray finally specSrc.close()
    }

    getHtmlSpec foreach { case specDoc =>
        log(s"Download complete!")

        def search(d: GenSeq[String]) = warmedTimed() {
            d.indexWhere(line => line.matches(".*TEXTAREA.*"))
        }

        val seqtime = search(specDoc)
        log(s"Sequential time $seqtime ms")

        val partime = search(specDoc.par)
        log(s"Parallel time $partime ms")
    }

    Thread.sleep(5000)

}


object ParNonParallelizableCollections extends App {

    val list = List.fill(1000000)("")
    val vector = Vector.fill(1000000)("")
    log(s"list conversion time: ${timed(list.par)} ms")
    log(s"vector conversion time: ${timed(vector.par)} ms")

}


import java.util.concurrent.atomic._
object ParSideEffectsCorrect extends App {

    def intSize(a: GenSet[Int], b: GenSet[Int]) = {
        val count = new AtomicInteger(0)
        for (x <-a) if (b.contains(x)) count.incrementAndGet()
        count.get()
    }

    val seqres = intSize((0 until 1000).toSet, (0 until 1000 by 4).toSet)
    val parres = intSize((0 until 1000).par.toSet, (0 until 1000 by 4).par.toSet)
    log(s"Sequential result - $seqres")
    log(s"Parallel result   - $parres")

}



































