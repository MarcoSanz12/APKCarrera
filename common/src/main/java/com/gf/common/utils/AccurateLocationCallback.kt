package com.gf.common.utils

import android.location.Location
import android.util.Log
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult

class AccurateLocationCallback(val accuracy : Int, val handleAccurate : (location:Location) -> Unit) : LocationCallback() {

    override fun onLocationResult(location: LocationResult) {
        super.onLocationResult(location)

        Log.d("PROCESO","${location.lastLocation!!.accuracy}")
        if (location.lastLocation!!.accuracy < accuracy)
            handleAccurate.invoke(location.lastLocation!!)

    }
}