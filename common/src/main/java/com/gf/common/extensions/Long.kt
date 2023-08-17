package com.gf.common.extensions

import java.text.SimpleDateFormat
import java.util.*

fun Long.toTimeFormated(format: String): String {
    val format = SimpleDateFormat(format, Locale.getDefault())
    return format.format(Date(this))
}
fun Int.toTimeFormated(format: String): String {
    val format = SimpleDateFormat(format, Locale.getDefault())
    return format.format(Date(this.toLong()))
}