package com
package liangfan
package chapter05


import scala.collection
import scala.collection.parallel.{SeqSplitter, immutable}


class ParString(val str: String) extends immutable.ParSeq[Char] {
    def apply(i: Int): Char = str.charAt(i)

    def length: Int = str.length

//    def splitter: SeqSplitter[Char] = new ParStringSplitter(str, 0, str.length)

    def seq = new collection.immutable.WrappedString(str)
}