package com
package liangfan
package chapter03

import java.util.concurrent._
import java.util.concurrent.atomic._

import scala.collection.convert.decorateAsScala._
import java.io.File

import org.apache.commons.io.FileUtils

import scala.collection.concurrent

object FileSystemTest extends App {
    val fileSystem = new FileSystem(".")
    fileSystem.logMessage("testing log!")
    fileSystem.deleteFile("test.txt")
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

    val rootDir = new File(root)
    val files: concurrent.Map[String, Entry] =
        //new ConcurrentHashMap().asScala
        new concurrent.TrieMap()
    for (f <- FileUtils.iterateFiles(rootDir, null, false).asScala) {
        files.put(f.getName, new Entry(false))
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

    def deleteFile(fileName: String): Unit = {
        files.get(fileName) match {
            case None => logMessage(s"path '$fileName' does not exist!")
            case Some(entry) if entry.isDir => logMessage(s"path '$fileName' is a directory!")
            case Some(entry) => execute {
                if (prepareForDelete(entry)) {
                    if (FileUtils.deleteQuietly(new File(fileName))) files.remove(fileName)
                }
            }
        }
    }


    private val messages = new LinkedBlockingQueue[String]()

    val logger: Thread = new Thread {
        setDaemon(true)
        override def run(): Unit = while (true) log(messages.take())
    }
    logger.start()

    def logMessage(msg: String): Unit = messages.add(msg)
}
