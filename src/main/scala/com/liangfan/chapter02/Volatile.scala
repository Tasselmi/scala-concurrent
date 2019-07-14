package com
package liangfan
package chapter02


//The JVM offers a more lightweight form of synchronization than the synchronized block, called volatile variables.
//writes to and reads from volatile variables cannot be reordered in a single thread
//writing to a volatile variable is immediately visible to all the other threads

class Page(val txt: String, var position: Int)

object Volatile extends App {
    val pages = for {
        i <- 1 to 5
    } yield new Page("Na" * (100 - 20 * i) + " Batman!", -1)

    @volatile
    var found = false

    for {
        p <- pages
    } yield  thread {
        var i = 0
        while (i < p.txt.length && !found) {
            if (p.txt(i) == '!') {
                p.position = i
                found = true
            } else i += 1
        }
    }

    while (!found) { }
    log(s"results: ${pages.map(_.position)}")
}




