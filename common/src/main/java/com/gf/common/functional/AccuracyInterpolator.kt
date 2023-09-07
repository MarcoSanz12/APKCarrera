package com.gf.common.functional

import android.util.Log
import com.google.android.gms.maps.model.LatLng

interface AccuracyInterpolator {

    fun interpolate(fraction: Float, a: Float, b: Float): Float

    class Linear : AccuracyInterpolator {
        override fun interpolate(fraction: Float, a: Float, b: Float): Float =
            (b - a) * fraction + a


    }

}