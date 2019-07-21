package com
package liangfan
package chapter05


import scala.collection
import scala.collection.parallel.{SeqSplitter, immutable}


class ParString(val str: String) extends immutable.ParSeq[Char] {
    def apply(i: Int): Char = str.charAt(i)

    def length: Int = str.length

    def splitter: SeqSplitter[Char] = new ParStringSplitter(str, 0, str.length)

    def seq = new collection.immutable.WrappedString(str)
}

class ParStringSplitter(val s: String, var i: Int, val limit: Int)
extends SeqSplitter[Char] {
    final def hasNext: Boolean = i < limit

    final def next: Char = {
        val r = s.charAt(i)
        i += 1
        r
    }
    def dup = new ParStringSplitter(s, i, limit)

    def remaining: Int = limit - i

    def psplit(sizes: Int*): Seq[ParStringSplitter] = {
        val ss = for (sz <- sizes) yield {
            val nlimit = (i + sz) min limit
            val ps = new ParStringSplitter(s, i, nlimit)
            i = nlimit
            ps
        }
        if (i == limit) ss
        else ss :+ new ParStringSplitter(s, i, limit)
    }

    def split: Seq[ParStringSplitter] = {
        val rem = remaining
        if (rem >= 2) psplit(rem / 2, rem - rem / 2)
        else Seq(this)
    }
}


import scala.collection.parallel._
import scala.collection.mutable.ArrayBuffer
class ParStringCombiner extends Combiner[Char, ParString] {
    private var sz = 0
    private val chunks = new ArrayBuffer += new StringBuilder
    private var lastc = chunks.last

    def size: Int = sz

    def +=(elem: Char): this.type = {
        lastc += elem
        sz += 1
        this
    }

    def clear = {
        chunks.clear()
        chunks += new StringBuilder
        lastc = chunks.last
        sz = 0
    }

    def result: ParString = {
        val rsb = new StringBuilder
        for (sb <- chunks) rsb.append(sb)
        new ParString(rsb.toString())
    }

    def combine[U <: Char, NewTo >: ParString](that: Combiner[U, NewTo]): ParStringCombiner = {
        if (this eq that) this else that match {
            case t: ParStringCombiner =>
                sz += t.sz
                chunks ++= t.chunks
                lastc = chunks.last
                this
        }
    }
}


object CustomCharCount extends App {

    val txt = "A custom text " * 250000
    val partxt = new ParString(txt)

    val seqtime = warmedTimed(50) {
        txt.foldLeft(0) {
            (n, c) => if (Character.isUpperCase(c)) n+1 else n
        }
    }
    log(s"sequential time - $seqtime ms")

    val partime = warmedTimed(50) {
        partxt.aggregate(0)((n, c) =>
            if (Character.isUpperCase(c)) n+1 else n, _+_
        )
    }
    log(s"parallel time - $partime ms")

}


object CustomCharFilter extends App {
    val txt = "A custom txt" * 25000
    val partxt = new ParString(txt)

    val seqtime = warmedTimed(250) {
        txt.filter(_ != ' ')
    }

    log(s"Sequential time - $seqtime ms")

    val partime = warmedTimed(250) {
        partxt.filter(_ != ' ')
    }

    log(s"Parallel time   - $partime ms")

}