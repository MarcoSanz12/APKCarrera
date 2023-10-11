package com.gf.common.entity

import com.google.android.gms.maps.model.LatLng

data class ActivityUIState(
    val time : Int,
    val distance : Int,
    val speedLastKm : Double,
    val points : List<List<LatLng>>,
    val status : ActivityStatus
)
