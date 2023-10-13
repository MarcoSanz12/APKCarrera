package com.gf.common.entity

import com.google.android.gms.maps.model.LatLng

data class RunningUIState(
    val time : Int,
    val distance : Int,
    val speedLastKm : Int,
    val points : List<List<LatLng>>,
    val status : ActivityStatus
)
