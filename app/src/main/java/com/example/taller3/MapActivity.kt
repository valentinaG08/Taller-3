package com.example.taller3

import android.Manifest
import android.annotation.SuppressLint
import android.app.UiModeManager
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.taller3.databinding.ActivityMapBinding
import com.example.taller3.utils.JSONReader.JSONReader
import com.example.taller3.utils.schemas.Location
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.gson.Gson
import org.json.JSONArray
import org.osmdroid.api.IMapController
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.TilesOverlay

class MapActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMapBinding
    private lateinit var mapController: IMapController
    private val locationsArray: ArrayList<Location> = ArrayList()
    private lateinit var mFusedLocationClient: FusedLocationProviderClient

    private var userLocation: GeoPoint? = null
    override fun onResume() {
        super.onResume()
        binding.osmMapPlace.onResume()

        val uiManager = getSystemService(android.content.Context.UI_MODE_SERVICE) as UiModeManager
        if (uiManager.nightMode == UiModeManager.MODE_NIGHT_YES) {
            binding.osmMapPlace.overlayManager.tilesOverlay.setColorFilter(TilesOverlay.INVERT_COLORS)
        }
    }

    override fun onPause() {
        super.onPause()
        binding.osmMapPlace.onPause()
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Esta linea es importante O.o
        Configuration.getInstance().userAgentValue = applicationContext.packageName

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        binding = ActivityMapBinding.inflate(layoutInflater)

        setContentView(binding.root)

        binding.osmMapPlace.setTileSource(TileSourceFactory.MAPNIK)
        binding.osmMapPlace.setMultiTouchControls(true)
        mapController = binding.osmMapPlace.controller

        val json = JSONReader.readJSONFile(null, null, assets.open("locations.json"))
        val locationsJSON = json.get("locationsArray") as JSONArray

        for (i in 0..<locationsJSON.length()) {
            val location = Gson().fromJson(locationsJSON[i].toString(), Location::class.java)
            locationsArray.add(location)
            Log.i("LUGARES", locationsJSON[i].toString())
            addMarker(location.latitude, location.longitude, location.name)
        }

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                1
            )
        }

        getUserLocation()
    }

    private fun addMarker(lat: Double, lng: Double, title: String) {
        val markerPoint = GeoPoint(lat, lng)
        val marker = Marker(binding.osmMapPlace)
        marker.title = title
        marker.position = markerPoint
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        binding.osmMapPlace.overlays.add(marker)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getUserLocation()
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun getUserLocation() {
        mFusedLocationClient.lastLocation.addOnSuccessListener(this) { location->
            Log.i("LOCATION", "Se obtuvo la localización")
            if (location != null) {
                userLocation = GeoPoint(location.latitude, location.longitude)
                addMarker(userLocation!!.latitude, userLocation!!.longitude, "My location")
                mapController.setZoom(12.5)
                mapController.setCenter(userLocation)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater : MenuInflater = menuInflater
        inflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle item selection.
        return when (item.itemId) {
            R.id.menu_logout -> {
                Firebase.auth.signOut()
                val intent = Intent(baseContext, MainActivity::class.java)
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity( intent )
                true
            }
            R.id.menu_set_available -> {

                true
            }
            R.id.menu_set_unavailable -> {
                true
            }

            R.id.menu_view_users -> {
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}