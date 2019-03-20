package com.sergeysav.hexasphere

import kotlin.random.Random

fun main(args: Array<String>) {
    Hexasphere(if (args.isNotEmpty()) args[0].toLong() else Random.nextLong()).run()
}
