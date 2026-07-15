package com.salahguard.app.domain.model

data class Reciter(
    val id: String,
    val name: String,
    val subFolder: String // Used in everyayah.com URLs
)

val DefaultReciter = Reciter(
    id = "alafasy",
    name = "Mishary Rashid Alafasy",
    subFolder = "Alafasy_128kbps"
)
