package com.salahguard.app.util

fun String.capitalizeWords(): String {
    return this.lowercase().split(" ").joinToString(" ") { it.replaceFirstChar { char -> char.uppercase() } }
}

fun String.capitalizeFirst(): String {
    return this.lowercase().replaceFirstChar { it.uppercase() }
}
