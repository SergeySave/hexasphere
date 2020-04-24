package com.sergeysav.hexasphere

import com.sergeysav.hexasphere.common.LoggingUtils
import org.slf4j.event.Level
import kotlin.random.Random

fun main(args: Array<String>) {
    //    ClasspathScanner.scanForAnnotation(EventHandler::class.java).forEach(System.out::println)
    LoggingUtils.setLogLevel(Level.TRACE)
    Hexasphere(if (args.isNotEmpty()) args[0].toLong() else Random.nextLong()).run()
}
