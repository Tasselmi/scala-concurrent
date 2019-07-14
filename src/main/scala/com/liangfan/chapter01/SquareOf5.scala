package com.liangfan.chapter01

object SquareOf5 extends App {

    def square(x: Int) = x * x

    val s = square(5)

    println(s"result: $s")
    println("\n----------")


    class Printer(val greeting: String) {

        def printMessage() = println(greeting + "!")

        def printNumber(x: Int) =
            println("number: " + x)

    }

    val printy = new Printer("Hi")
    printy.printMessage()
    printy.printNumber(5)
    println("\n----------")


    val twice: Int => Int = _ * 2

    def runTwice(body: => Unit): Unit = {
        body
        body
    }
    runTwice(println("hello"))
    println("\n----------")


    for (i <- 0 until 10) println(i)
    (0 until 10) foreach(i => println(i))
    println("\n----------")


    val negatives = for (i <- 0 until 10) yield -i
    println(negatives)


    /**
      * We can nest an arbitrary number of generator expressions in a for-comprehension.
      * The Scala compiler will transform them into a sequence of nested flatMap calls,
      * followed by a map call at the deepest level.
      */
    val pairs = for {
        x <- 0 until 4
        y <- 0 until 4
    } yield (x, y)

    val pairs2 = (0 until 4).flatMap(
        x => (0 until 4).map(
            y => (x, y)
        )
    )

    class Position(val x: Int, val y: Int) {
        def +(that: Position) = new Position(x + that.x, y + that.y)
    }


}
