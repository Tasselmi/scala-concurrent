import java.util.regex.Pattern
import java.io._

import com.liangfan.chapter03.EvaluationApp

import scala.sys.process.Process

val className: String = EvaluationApp.getClass().getName().split((Pattern.quote("$")))(0)

val tmp = File.createTempFile("concurrent-programming-in-scala", null)

tmp.getCanonicalPath

System.getProperty("java.class.path")

Process(s"java -cp .:$className ${tmp.getCanonicalPath}").!
//
Process(s"java -cp ${System.getProperty("java.class.path")} $className ${tmp.getCanonicalPath}").!