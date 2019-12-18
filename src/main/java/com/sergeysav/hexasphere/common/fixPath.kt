package com.sergeysav.hexasphere.common

fun String.fixPath(): String {
    val stack = mutableListOf<String>()
    for (part in this.split('/')) {
        when (part) {
            "", "." -> { }
            ".." -> {
                stack.removeAt(stack.lastIndex)
            }
            else -> {
                stack.add(part)
            }
        }
    }
    return stack.joinToString("/", "/")
}