package com.example.taller3

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.MapView
import com.google.firebase.firestore.GeoPoint

class MapActivity : AppCompatActivity() {

    private lateinit var mapView: MapView
    private var lastClickedPosition: GeoPoint? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        // Configuración del mapa

        // Inicializar el mapa etc

    }

}

data class PointOfInterest(val latitude: Double, val longitude: Double, val name: String)