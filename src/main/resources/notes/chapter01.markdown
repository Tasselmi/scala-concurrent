# 1. 并发编程介绍

## 1.1 简述并发编程

- 什么是并发编程
    - 并发编程就是让在一段时间内有一组计算的程序以
    某个方式让这些计算协调运行。

- 为什么要写并发程序，而不是直接写顺序执行的程序呢
    - 能提升程序运行速度和性能
    - 提升IO吞吐量，提升程序与环境进行交互时的响应效率（比如键盘、网络接口、其他设备）
    - 简化程序的实现和可维护性
    
- 并发编程不同于分布式编程
    - 并发编程遵循使用共享内存的管理，在单个机器上执行
    - 分布式编程，程序在多个机器上执行，各机器使用自己的内存；分布式编程
    必须考虑每台机器在每个节点都有执行失败的可能性（更复杂，机器间的通信、可靠性保证等）
    
## 1.2 传统并发编程概览

- 并发性有多个层次
    - 计算机硬件层次
    - 操作系统层次
    - 编程层次
    - 并发编程讨论的是编程语言层次，也即第3个层次
    
- synchronization
    - 在并发系统中协调多个执行（线程）被称为同步，它是成功实现并发的关键所在
    - synchronization包含在一段时间内让各个线程有序执行的机制
    - synchronization指定了并发执行的线程信息交换的方式
    - 在并发程序中，不同线程会修改计算中的共享内存并进行交互，这被称为**共享内存通信**
    - 在分布式程序中，各executions通过消息交换来通信，这种同步机制称为**消息传递通信**
    
- 进程、线程、锁和监视器
    - 在计算机底层，并发的executions由进程实体（processes）和线程实体（threads）来代表
    - 进程和线程使用锁、监视器来保证executions的有序性
    - 通过线程和锁来表达并发编程其实是非常笨重的方法
    - 更加复杂的并发组件，比如——communication channels、concurrent collections、
    barriers、countdown latches、thread pools等，都被开发出来了；这些组件的设计目的
    就是让表达并发编程模式更加简易
    - 这些传统的并发模式相对来说比较low-level，而且容易出错，比如deadlocks、starvations、
    data races、race conditions等
    - 在scala中编写并发程序不必或很少写low-level的并发原语，但是拥有low-level的
    并发编程的基本知识，对于理解high-level的并发概念和思想是至关重要的。
    
 
 ## 1.3 现代并发编程范式
 
- 现代范式与传统范式的区别   
    - 一句很重要的话：The crucial defference lies in the fact that a high-level concurrency
    framework expresses **which goal** to achieve, rather than **how to achieve**
    that goal
    - 在具体实践中，现代与传统的分野并不特别明显
    - 并发编程如今仍然对declarative and functional programming存有偏见
    - 异步编程asynchronous programming using futures to pretend as if we have already get the result，
    and not to wait for it
    - 反应式编程reactive programming using event streams事件流 aims to declaratively express concurrent
    computationas that produce many values
    - 可并行的集合框架data-parallel collections framework available in Scala, which is designed to seamlessly accelerate 
    collection operations using multiple processors
    - 内存事务Another trend seen in high-level concurrency frameworks is specialization towards specific tasks. 
    A memory transaction is a sequence of memory operations that appear as if they either execute all at once 
    or do not execute at all. The advantage of using memory transactions is that this avoids a lot of errors typically associated with low-level concurrency.
 
    
## 1.4 scala的优势
 
 - 句法灵活性，能很好的和并发框架结合
 - 安全的语言：自动垃圾回收、自动界限检查、无指针算法，能够避免很多问题——如内存泄漏、缓存溢出；静态类型语言
 - 可与java互操作：scala代码被编译为java字节码，能够在jvm虚拟机上运行；因此能够使用java丰富的生态资源和类库；可在各平台上移植
 
    
## 1.5 预热与准备  
    
    
    
    
    