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
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.taller3.databinding.ActivityUserLocationBinding
import com.example.taller3.utils.Firebase.RealtimeCRUD
import com.example.taller3.utils.JSONReader.JSONReader
import com.example.taller3.utils.schemas.Location
import com.example.taller3.utils.schemas.User
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import com.google.firebase.database.getValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import org.json.JSONArray
import org.osmdroid.api.IMapController
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.TilesOverlay
import kotlin.math.ln

class UserLocationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUserLocationBinding
    private lateinit var mapController: IMapController
    private lateinit var userId: String // ID del usuario a quien se le hace el seguimiento
    private lateinit var mFusedLocationClient: FusedLocationProviderClient
    private lateinit var databaseCRUD :RealtimeCRUD

    private lateinit var mLocationCallback: LocationCallback
    private lateinit var mLocationRequest: LocationRequest

    private var userLocation: GeoPoint? = null
    private var myUserMarker: Marker? = null
    private var trackedUserMarker: Marker? = null

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
        stopLocationUpdates()
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Configuration.getInstance().userAgentValue = applicationContext.packageName
        databaseCRUD = RealtimeCRUD(Firebase.database)
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        binding = ActivityUserLocationBinding.inflate(layoutInflater)

        setContentView(binding.root)

        binding.osmMapPlace.setTileSource(TileSourceFactory.MAPNIK)
        binding.osmMapPlace.setMultiTouchControls(true)
        mapController = binding.osmMapPlace.controller

        userId = intent.getStringExtra("userId") ?: ""

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

        databaseCRUD.addListenerToUser(userId, object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue<User>()
                addTrackedMarker(user?.latitude, user?.longitude)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.i("ERROR", "Esto se jodio")
            }

        })

        // Location updates

        mLocationRequest = createLocationRequest()

        mLocationCallback = object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult) {
                val location = p0.lastLocation
                Log.i("LOCATION UPDATE", "Location update $location")
                if (location != null) {
                    databaseCRUD.writeUserLocation(
                        Firebase.auth.currentUser!!.uid,
                        location.latitude,
                        location.longitude,
                    ) { error, user ->
                        if (error != null) Log.i("ERROR EN LA ESCRITURA", error)
                        else createUserMarker(user!!.latitude, user.longitude)
                    }
                }
            }
        }

        startLocationUpdates()
    }

    private fun stopLocationUpdates() {
        mFusedLocationClient.removeLocationUpdates(mLocationCallback)
    }

    private fun createLocationRequest() : LocationRequest {
        return LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000).apply {
            setMinUpdateIntervalMillis(10000)
        }.build()
    }
    private fun startLocationUpdates() {
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
            return
        }
        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, null)
    }

    private fun addTrackedMarker(lat: Double?, lng: Double?) {
        if (lat == null || lng == null) {
            Log.i("LONGITUDES", "Las longitudes son null")
            return
        }
        trackedUserMarker.let { binding.osmMapPlace.overlays.remove(it) }

        trackedUserMarker = addMarker(lat, lng, "Tracked user", true)
    }

    private fun createUserMarker(lat: Double, lng: Double) {
        myUserMarker.let { binding.osmMapPlace.overlays.remove(it) }
        myUserMarker = addMarker(lat, lng, "You", false)
        mapController.setZoom(12.5)
        mapController.setCenter(myUserMarker!!.position)
    }

    private fun addMarker(lat: Double, lng: Double, title: String, isTracked: Boolean) : Marker {
        val markerPoint = GeoPoint(lat, lng)
        val marker = Marker(binding.osmMapPlace)
        marker.title = title
        marker.position = markerPoint
        if (isTracked) marker.subDescription = "${
            markerPoint.distanceToAsDouble(myUserMarker?.position ?: GeoPoint(0.0, 0.0))
        } metros"
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        binding.osmMapPlace.overlays.add(marker)
        return marker
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startLocationUpdates()
            }
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
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