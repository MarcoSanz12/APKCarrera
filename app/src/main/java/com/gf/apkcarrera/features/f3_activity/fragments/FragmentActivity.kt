package com.gf.apkcarrera.features.f3_activity.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.cotesa.common.extensions.distanceTo
import com.gf.apkcarrera.R
import com.gf.apkcarrera.databinding.Frg03ActivityBinding
import com.gf.common.platform.BaseFragment
import com.gf.common.utils.Constants.Permissions.LOCATION_PERMISSION_CODE
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PolylineOptions

class FragmentActivity : BaseFragment<Frg03ActivityBinding>(), OnMapReadyCallback {

    private lateinit var mapFragment: SupportMapFragment
    private lateinit var map: GoogleMap
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private val points : MutableList<LatLng> = mutableListOf()
    private var STATUS = STATUS_LOCATING

    companion object{
        private const val STATUS_LOCATING = 0
        private const val STATUS_READY = 1
        private const val STATUS_RUNNING = 2
        private const val STATUS_PAUSE = 3
        private const val STATUS_DONE = 4
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)
        setOnBackPressed(R.id.fragmentFeed)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initializeView()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }


    private fun initializeView(){
        if (areLocationPermissionsGranted())
            createMap()
        else
            requestLocationPermissions()
    }

    private fun createMap(){
        mapFragment = SupportMapFragment.newInstance()
        childFragmentManager
            .beginTransaction()
            .add(R.id.ly_map_container, mapFragment)
            .commit()

        mapFragment.getMapAsync(this)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == LOCATION_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                createMap()
            } else {
              handleNoGps()
            }
        }
        else {
            handleNoGps()
        }
    }
    private fun requestLocationPermissions() {
        requestPermissions( //Method of Fragment
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ),
            LOCATION_PERMISSION_CODE
        )
    }
    
    private fun handleNoGps(){
        Toast.makeText(requireContext(), getString(com.gf.common.R.string.error_no_location), Toast.LENGTH_SHORT).show()
        onBackPressed()
    }

    private fun areLocationPermissionsGranted() : Boolean  =
        (ContextCompat.checkSelfPermission(requireContext(),Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                &&
                ContextCompat.checkSelfPermission(requireContext(),Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)


    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(41.6315, -4.7220),9f))



        val locationRequest = LocationRequest.Builder( Priority.PRIORITY_HIGH_ACCURACY,3000).build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)

                // Si la localización es null, no coger punto
                val location = locationResult.lastLocation ?: return

                when (STATUS){
                    STATUS_LOCATING ->
                        locateGps(location)
                    STATUS_READY ->{

                    }
                    STATUS_PAUSE ->{

                    }
                    STATUS_RUNNING ->
                        addPoint(location)
                    STATUS_DONE ->{

                    }
                }
                addPoint(location)
            }
        }

        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null)

    }
    private fun locateGps(location: Location){
        if (location.accuracy > 10)
            return


        STATUS = STATUS_READY
    }

    @SuppressLint("MissingPermission")
    private fun addPoint(location: Location) {
        Log.d("GPS_Update", "Accuracy: ${location.accuracy} meters")
        // Si detecta a más de 15 metros, no coger
        if (location.accuracy > 15)
            return

        val locLatLng = LatLng(location.latitude, location.longitude)
        if (!map.isMyLocationEnabled)
            map.isMyLocationEnabled = true

        if (points.size > 0) {
            if (points.last().distanceTo(locLatLng) < 15) {
                points.add(locLatLng)
                updatePolyline(map)
            }

        } else {
            points.add(locLatLng)
            updatePolyline(map)
        }



        Log.d("GPS_Update", "Lat:${locLatLng.latitude}, Lon: ${locLatLng.longitude}")
        if (map.myLocation != null)
            Log.d(
                "GPS_Update",
                "Maps diff ${
                    locLatLng.distanceTo(
                        LatLng(
                            map.myLocation.latitude,
                            map.myLocation.longitude
                        )
                    )
                } meters"
            )

        Log.d("GPS_Update", "Points: ${points.size}")
    }

    private fun updatePolyline(map:GoogleMap){
        map.clear()

        val polylineOptions = PolylineOptions().apply {
            color(requireContext().getColor(com.gf.common.R.color.orange_quaternary)) // Color de la línea
            visible(true)
            zIndex(7f)
            width(8f) // Grosor de la línea en píxeles
        }
        for (point in points)
            polylineOptions.add(point)

       map.addPolyline(polylineOptions)
    }

    fun simplifyDouglasPeucker(points: List<LatLng>, epsilon: Double): List<LatLng> {
        if (points.size < 3) {
            return points
        }

        val firstPoint = points.first()
        val lastPoint = points.last()

        var maxDistance = 0.0
        var index = 0

        for (i in 1 until points.size - 1) {
            val distance = perpendicularDistance(points[i], firstPoint, lastPoint)
            if (distance > maxDistance) {
                maxDistance = distance
                index = i
            }
        }

        return if (maxDistance > epsilon) {
            val simplified1 = simplifyDouglasPeucker(points.subList(0, index + 1), epsilon)
            val simplified2 = simplifyDouglasPeucker(points.subList(index, points.size), epsilon)
            simplified1.dropLast(1) + simplified2
        } else {
            listOf(firstPoint, lastPoint)
        }
    }

    fun perpendicularDistance(p: LatLng, p1: LatLng, p2: LatLng): Double {
        val area = Math.abs(0.5 * (p1.longitude * p2.latitude + p2.longitude * p.latitude + p.latitude * p1.longitude - p2.longitude * p1.latitude - p.latitude * p2.longitude - p1.longitude * p.latitude))
        val bottom = Math.sqrt(Math.pow(p1.longitude - p2.longitude, 2.0) + Math.pow(p1.latitude - p2.latitude, 2.0))
        return area / bottom
    }



    override fun onPause() {
        super.onPause()
        if (::locationCallback.isInitialized)
            fusedLocationProviderClient.removeLocationUpdates(locationCallback)
    }


}