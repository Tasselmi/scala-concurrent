package com
package liangfan
package chapter05


import scala.collection._
import scala.util.Random
import java.util.concurrent.atomic._

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
