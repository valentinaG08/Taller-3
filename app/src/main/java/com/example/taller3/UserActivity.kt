package com.example.taller3

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.taller3.utils.Firebase.RealtimeCRUD
import com.example.taller3.utils.schemas.User
import com.google.firebase.Firebase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.storage
import org.json.JSONObject

class UserActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: UserAdapter
    private lateinit var firestore: FirebaseStorage
    private lateinit var database: FirebaseDatabase
    var users = mutableListOf<User>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user)

        firestore = Firebase.storage
        database = Firebase.database
        recyclerView = findViewById(R.id.recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)
        startListeningForUsers()
        adapter = UserAdapter(users, this) { user ->
            showToast("Clic en usuario: ${user.firstName}")
            viewUserLocation(user)
        }
        recyclerView.adapter = adapter
    }

    //Implementar en el mapa
    private fun viewUserLocation(user: User) {
        val intent = Intent(this, UserLocationActivity::class.java)
        intent.putExtra("userId", user.id)
        startActivity(intent)
    }

    override fun onStart() {
        super.onStart()
        startListeningForUsers()
    }

    override fun onStop() {
        super.onStop()
    }

    private fun startListeningForUsers() {
        RealtimeCRUD(database).listenForUsers(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val usersMap :HashMap<String, Any> =  (snapshot.value as HashMap<String, Any>)
                Log.i("LIST USERS",usersMap.toString())
                val listUsers = mutableListOf<User>()
                for ((key, value) in usersMap) {
                    Log.i("USER EACH", value.toString())
                    val eachUser = User(
                        firstName = (value as HashMap<*, *>)["firstName"] as String,
                        lastName = (value as HashMap<*, *>)["lastName"] as String,
                        latitude = (value as HashMap<*, *>)["latitude"] as Double,
                        available = (value as HashMap<*, *>)["available"] as Boolean,
                        identificationNumber = (value as HashMap<*, *>)["identificationNumber"] as Long,
                        id = (value as HashMap<*, *>)["id"] as String,
                        imagenId = (value as HashMap<*, *>)["imagenId"] as String,
                        longitude = (value as HashMap<*, *>)["longitude"] as Double,
                    )
                    if (eachUser.available) listUsers.add(eachUser)
                }
                users = listUsers

                adapter = UserAdapter(users, baseContext) { user ->
                    showToast("Clic en usuario: ${user.firstName}")
                    viewUserLocation(user)
                }
                recyclerView.adapter = adapter
            }

            override fun onCancelled(error: DatabaseError) {
                Log.i("CANCELLED", "Esto se cancelo")
            }

        })
    }


    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}

