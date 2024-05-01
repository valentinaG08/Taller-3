package com.example.taller3.utils.Firebase

import android.annotation.SuppressLint
import android.util.Log
import com.example.taller3.utils.schemas.User
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.getValue


class RealtimeCRUD(database: FirebaseDatabase) {

    private val databaseReference : DatabaseReference = database.reference

    fun getUserById(id: String, callback : (String?, User?) -> Unit ) {
        databaseReference.child("users").child(id).get().addOnSuccessListener {
            Log.i("USER RECUPERDO", it.value.toString())
            val returnUser = it.getValue<User>()
            callback(null, returnUser)
        }.addOnFailureListener {
            callback(it.message, null)
        }
    }

    fun writeUserLocation(id: String, lat: Double, lng: Double, callback: (String?, User?) -> Unit) {
        val userReference = databaseReference.child("users").child(id)
        userReference.child("latitude").setValue(lat)
        userReference.child("longitude").setValue(lng)
        getUserById(id, callback)
    }

    fun addListenerToUser(id: String, listener: ValueEventListener) {
        databaseReference.child("users").child(id).addValueEventListener(listener)
    }

    fun writeUser(id: String, user: User, callback: (String?) -> Unit) {
        databaseReference.child("users").child(id).setValue(user).addOnSuccessListener {
            callback(null)
        }.addOnFailureListener {
            callback(it.message)
        }
    }

    fun listenForUsers(listener: ValueEventListener) {
        databaseReference.child("users").addValueEventListener(listener)
    }

}