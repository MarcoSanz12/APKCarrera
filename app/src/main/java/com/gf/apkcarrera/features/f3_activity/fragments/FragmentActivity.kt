package com.gf.apkcarrera.features.f3_activity.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.cotesa.common.extensions.distanceTo
import com.gf.apkcarrera.R
import com.gf.apkcarrera.databinding.Frg03ActivityBinding
import com.gf.common.extensions.invisible
import com.gf.common.extensions.visible
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
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.PolylineOptions
import kotlinx.coroutines.launch
import java.util.Timer
import kotlin.concurrent.timerTask

class FragmentActivity : BaseFragment<Frg03ActivityBinding>(), OnMapReadyCallback {

    private lateinit var mapFragment: SupportMapFragment
    private lateinit var map: GoogleMap
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private val points : MutableList<LatLng> = mutableListOf()
    private var STATUS = STATUS_LOCATING
    private lateinit var lastLocation : LatLng
    private var isLocated = false
    private var time : Int = 0
    private var distance : Int = 0
    private lateinit var timer : Timer

    companion object{
        private const val STATUS_LOCATING = 0
        private const val STATUS_READY = 1
        private const val STATUS_RUNNING = 2
        private const val STATUS_PAUSE = 3
        private const val STATUS_DONE = 4
        private const val CAM_ZOOM = 18f
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
        map.mapType = GoogleMap.MAP_TYPE_SATELLITE
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(41.6315, -4.7220),9f))



        val locationRequest = LocationRequest.Builder( Priority.PRIORITY_HIGH_ACCURACY,3000).build()

        // Timer para poner 3 puntitos *importante* al buscar gps
       timer = Timer().apply {
            scheduleAtFixedRate(timerTask {
                MAIN.launch {
                    if (binding.tvLocating.text.contains("..."))
                        binding.tvLocating.text = getString(com.gf.common.R.string.obtaining_gps)
                    else
                        binding.tvLocating.text = binding.tvLocating.text.toString() + "."
                }

            }
                ,0,1000)
        }

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)

                // Si la localización es null, no coger punto
                val location = locationResult.lastLocation ?: return

                when (STATUS){
                    STATUS_LOCATING ->
                        action0LocateGps(location)

                    STATUS_RUNNING ->
                        action1AddPoint(location)
                    STATUS_PAUSE ->{

                    }
                    STATUS_DONE ->{

                    }
                }


            }
        }

        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null)

    }
    @SuppressLint("MissingPermission")
    private fun action0LocateGps(location: Location){
        if (location.accuracy > 30)
            return

        // LOCALIZACIÓN ENCONTRADA
        map.isMyLocationEnabled = true

        map.animateCamera(CameraUpdateFactory.newLatLngZoom(
            LatLng(location.latitude,location.longitude),
            CAM_ZOOM
        ))

        STATUS = STATUS_READY
        timer.cancel()
        binding.tvLocating.text = getString(com.gf.common.R.string.obtaining_gps_done)
        binding.lyLocating.setBackgroundResource(com.gf.common.R.color.green)
        binding.pbLocating.invisible()

        timer = Timer().apply{
            schedule(timerTask {
                MAIN.launch { binding.lyLocating.collapse() }

            },2000)
        }


        binding.btMapButton.apply {
            visible()
            setOnClickListener {
                binding.lyInfoPanel.expand()
                text = getString(com.gf.common.R.string.btmap_detener)
                setTextColor(resources.getColor(com.gf.common.R.color.white))
                backgroundTintList = ColorStateList.valueOf(resources.getColor(com.gf.common.R.color.purple_secondary))
                STATUS = STATUS_RUNNING
            }
        }


    }

    @SuppressLint("MissingPermission")
    private fun action1AddPoint(location: Location) {
        Log.d("GPS_Update", "Accuracy: ${location.accuracy} meters")
        val locLatLng = LatLng(location.latitude, location.longitude)

        if (map.cameraPosition.zoom < CAM_ZOOM)
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(locLatLng,CAM_ZOOM))
        else
            map.animateCamera(CameraUpdateFactory.newLatLng(locLatLng))


        // Si detecta a más de 30 metros de precisión, no coger
        if (location.accuracy > 30)
            return

        if (points.size > 0) {
            val distance = points.last().distanceTo(locLatLng)
            if (distance > 10 && distance < 1000) {
                points.add(locLatLng)
                updatePolyline(map)
                Log.d("GPS_Update", "Points: ${points.size}")
            }

        } else {
            points.add(locLatLng)
            updatePolyline(map)
            Log.d("GPS_Update", "Points: ${points.size}")
        }
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