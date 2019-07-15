package com
package liangfan
package chapter03

import java.util.concurrent._
import java.util.concurrent.atomic._

object FileSystemTest extends App {

}


class FileSystem(val root: String) {
    sealed trait State
    class Idle extends State
    class Creating extends State
    class Copying(val n: Int) extends State
    class Deleting extends State

    class Entry(val isDir: Boolean) {
        val state = new AtomicReference[State](new Idle)
    }

    def prepareForDelete(entry: Entry): Boolean = {
        val s0 = entry.state.get()
        s0 match {
            case i: Idle => {  //空闲的文件才能被删除，如果空闲，那么试着改变其状态为Deleting
                if (entry.state.compareAndSet(s0, new Deleting)) true //改变状态成功（有任何一个线程改变成功即可），返回true
                else prepareForDelete(entry) //否则继续尝试改变其状态
            }
            case c: Creating => {
                log("file currently created, cannot delete.")
                false
            }
            case cp: Copying => {
                log("file currently copied, cannot delete.")
                false
            }
            case d: Deleting => false //说明文件在删除中
        }
    }
}
