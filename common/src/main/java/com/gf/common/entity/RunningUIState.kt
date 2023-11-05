package com.gf.common.entity

import com.gf.common.entity.activity.RegistryPoint

data class RunningUIState(
    val time : Int,
    val distance : Int,
    val speedLastKm : Float,
    val timeList : List<Int>,
    val points : List<List<RegistryPoint>>,
    val status : ActivityStatus
)
