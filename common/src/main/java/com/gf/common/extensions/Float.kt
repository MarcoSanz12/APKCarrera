package com.gf.common.extensions

fun Float.format(digits: Int) = "%.${digits}f".format(this)