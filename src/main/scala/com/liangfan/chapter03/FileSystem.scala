package com
package liangfan
package chapter03


import java.util.concurrent._
import java.util.concurrent.atomic._

import scala.collection.convert.decorateAsScala._
import java.io.File

import org.apache.commons.io.FileUtils

import scala.annotation.tailrec
import scala.collection.concurrent


object FileSystemTest extends App {
    val fileSystem = new FileSystem(".")

    fileSystem.logMessage("testing log!")
    fileSystem.copyFile("build.sbt", "text.txt")
    fileSystem.deleteFile("text.txt")
    fileSystem.deleteFile("build.sbt.backup")

    val rootFiles = fileSystem.filesInDir("") //注意这方法是会造成阻塞的，如果先运行这个，会和deleteFile冲突
    log("All files in the root dir: " + rootFiles.mkString(", "))
}


class FileSystem(val root: String) {

    private val messages = new LinkedBlockingQueue[String]()

    val logger = new Thread {
        setDaemon(true)
        override def run(): Unit = while (true) log(messages.take())
    }
    logger.start()

    def logMessage(msg: String): Unit = messages.add(msg)


    sealed trait State
    class Idle extends State
    class Creating extends State
    class Copying(val n: Int) extends State
    class Deleting extends State

    class Entry(val isDir: Boolean) {
        val state = new AtomicReference[State](new Idle)
    }

    val files: concurrent.Map[String, Entry] = new concurrent.TrieMap()
        //new ConcurrentHashMap().asScala

    for (f <- FileUtils.iterateFiles(new File(root), null, false).asScala) {
        files.put(f.getName, new Entry(false))
    }

    @tailrec
    private def prepareForDelete(entry: Entry): Boolean = {
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
            case Some(ent) => execute {
                if (prepareForDelete(ent)) {
                    if (FileUtils.deleteQuietly(new File(fileName))) {
                        files.remove(fileName)
                        println(s"File $fileName has been deleted !")
                    }
                }
            }
        }
    }

    @tailrec
    private def acquire(entry: Entry): Boolean = {
        val s0 = entry.state.get()
        s0 match {
            case _: Creating | _: Deleting => {
                logMessage("file inaccessible, cannot copy.")
                false
            }
            case i: Idle => { //如果是空闲状态，那么将其变为Copying状态
                if (entry.state.compareAndSet(s0, new Copying(1))) true
                else acquire(entry)
            }
            case c: Copying => { //如果已经是Copying状态，那么n代表其第n次copy，将子状态n加1
                if (entry.state.compareAndSet(s0, new Copying(c.n + 1))) true
                else acquire(entry)
            }
        }
    }

    @tailrec
    private def release(entry: Entry): Unit = {
        val s0 = entry.state.get()
        s0 match {
            case c: Creating => { if (!entry.state.compareAndSet(s0, new Idle)) release(entry) }
            case cp: Copying => {
                val nstate = if (cp.n == 1) new Idle else new Copying(cp.n - 1)
                if (!entry.state.compareAndSet(s0, nstate)) release(entry)
            }
            case i: Idle => sys.error("Error - released more times than acquired.")
            case d: Deleting => sys.error("Error - releasing a file that is being deleted!")
        }
    }

    def copyFile(src: String, dest: String): Unit = {
        files.get(src) match {
            case Some(srcEntry) if !srcEntry.isDir => execute {
                if (acquire(srcEntry)) try {
                    val destEntry = new Entry(isDir = false)
                    destEntry.state.set(new Creating)
                    if (files.putIfAbsent(dest, destEntry).isEmpty) try {
                        FileUtils.copyFile(new File(src), new File(dest))
                    } finally release(destEntry)
                } finally release(srcEntry)
            }
        }
    }

//    def filesInDir(dir: String): Iterable[String] = {
//        for ((name, state) <- files; if name.startsWith(dir)) yield name
//    }

    def filesInDir(dir: String): Iterable[String] = {
        // trie map snapshots
        for ((name, state) <- files; if name.startsWith(dir)) yield name
    }
}
