package com.gf.common.entity.activity

import android.graphics.Bitmap

data class ActivityModelSimple(
    var points: List<List<RegistryPoint>>,
    var time: List<Int> ,
    var distance: Int,
    var snapshot : Bitmap
)