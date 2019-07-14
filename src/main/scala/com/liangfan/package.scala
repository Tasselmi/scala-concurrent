package com

package object liangfan {

    def log(msg: String) {
        println(s"${Thread.currentThread.getName}: $msg")
    }


}
