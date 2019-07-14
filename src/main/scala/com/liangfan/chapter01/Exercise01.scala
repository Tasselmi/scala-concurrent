package com.liangfan.chapter01

object Exercise01 extends App {

    //ex01
    def compose[A, B, C](g: B => C, f: A => B): A => C = f andThen g
    def compose2[A, B, C](g: B => C, f: A => B): A => C = g compose f
    def compose3[A, B, C](g: B => C, f: A => B): A => C = x => g(f(x))

    //ex02
    def fuse[A, B](a: Option[A], b: Option[B]): Option[(A, B)] = for {
        x <- a
        y <- b
    } yield (x, y)
    def fuse2[A, B](a: Option[A], b: Option[B]): Option[(A, B)] = a.flatMap(
        x => b.map(y => (x, y))
    )

    //ex03
    //def check[T](xs: Seq[T])(pred: T => Boolean): Boolean = xs.forall(pred)
    def check[T](xs: Seq[T])(pred: T => Boolean): Boolean = xs.forall {
        x => try {
            pred(x)
        } catch {
            case e: Exception => false
        }
    }
    val s = check(0 until 10)(40 / _ > 0) //发现报错后修改为能够捕捉错误的版本
    println(s)


    def permutations(x: String): Seq[String] = x.permutations.toList

}
