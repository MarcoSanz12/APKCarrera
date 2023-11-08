package com.gf.common.extensions

import android.content.Context
import android.content.res.Resources
import androidx.core.content.res.ResourcesCompat
import com.gf.common.entity.activity.RegistryPoint
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.PolylineOptions
fun GoogleMap.paintPolyline(context: Context, points:List<List<RegistryPoint>>){
    for (point in points){
        if (point.isEmpty())
            continue


        val polylineOptions = PolylineOptions().apply {
            color(context.getColor(com.gf.common.R.color.orange_quaternary))// Color de la línea
            visible(true)
            zIndex(7f)
            width(12f) // Grosor de la línea en píxeles
        }
        point.forEach {
            polylineOptions.add(it.latLng)
        }

        addPolyline(polylineOptions)
    }
}
fun GoogleMap.adjustCamera(points:List<List<RegistryPoint>>){
    if (points.flatten().isEmpty())
        return

    val builder = LatLngBounds.Builder()
    points.flatten().forEach {
        builder.include(it.latLng)
    }


    val bounds = builder.build()
    val update = CameraUpdateFactory.newLatLngBounds(bounds,100)

    moveCamera(update)
}