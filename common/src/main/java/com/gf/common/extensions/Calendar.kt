package com.cotesa.common.extensions

import java.util.*
import java.util.concurrent.TimeUnit

fun Calendar.toStringShort() : String{
    return "${this.get(Calendar.DAY_OF_MONTH)}/${this.get(Calendar.MONTH)}/${this.get(Calendar.YEAR)}"
}
fun Calendar.dayBetween(calendar: Calendar): Long{
    val msDiff: Long = calendar.timeInMillis -this.timeInMillis
    return TimeUnit.MILLISECONDS.toDays(msDiff)
}
