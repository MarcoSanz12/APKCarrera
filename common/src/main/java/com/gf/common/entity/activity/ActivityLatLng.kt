package com.gf.common.entity.activity

import com.google.android.gms.maps.model.LatLng

data class ActivityLatLng (
    var x : Double,
    var y: Double
) {
    constructor(o: LatLng) : this(o.latitude,o.longitude)

}