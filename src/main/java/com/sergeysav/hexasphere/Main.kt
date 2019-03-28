package com.sergeysav.hexasphere

import kotlin.random.Random

fun main(args: Array<String>) {
    //    ClasspathScanner.scanForAnnotation(EventHandler::class.java).forEach(System.out::println)
    Hexasphere(if (args.isNotEmpty()) args[0].toLong() else Random.nextLong()).run()
}
