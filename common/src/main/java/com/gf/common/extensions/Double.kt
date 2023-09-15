package com.cotesa.common.extensions

import android.util.Log
import java.math.RoundingMode
import java.text.DecimalFormat

fun Double.roundTo(decimals : Int) : Double {
    var decimalsToRound = decimals
    if (decimalsToRound < 1)
        decimalsToRound = 1

    var pattern = "#."
    for (i in 1..decimalsToRound){
        pattern += "#"
        Log.d("PATTERN",pattern)
    }
    val df = DecimalFormat(pattern)
    df.roundingMode = RoundingMode.DOWN
    return  df.format(this).toDouble()
}

fun Double.roundToString(decimals : Int) : String {
    var decimalsToRound = decimals
    if (decimalsToRound < 1)
        decimalsToRound = 1

    var pattern = "#."
    for (i in 1..decimalsToRound){
        pattern += "#"
        Log.d("PATTERN",pattern)
    }
    val df = DecimalFormat(pattern)
    df.roundingMode = RoundingMode.DOWN
    return  df.format(this).replace(",",".")
}