package com.example.taller3

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class UserActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: UserAdapter
    private lateinit var firestore: FirebaseFirestore
    private lateinit var userListener: ListenerRegistration
    val users = mutableListOf<User>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user)

        recyclerView = findViewById(R.id.recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = UserAdapter(users, this) { user ->
            showToast("Clic en usuario: ${user.nombre}")
            viewUserLocation(user)
        }
        recyclerView.adapter = adapter

        firestore = FirebaseFirestore.getInstance()
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
        stopListeningForUsers()
    }

    private fun startListeningForUsers() {
        userListener = firestore.collection("usuarios")
            .addSnapshotListener { snapshot, exception ->
                if (exception != null) {
                    showToast("Error al obtener usuarios: ${exception.message}")
                    return@addSnapshotListener
                }
                snapshot?.let { documentSnapshot ->
                    for (change in documentSnapshot.documentChanges) {
                        val user = change.document.toObject(User::class.java)
                        when (change.type) {
                            DocumentChange.Type.ADDED -> users.add(user)
                            DocumentChange.Type.MODIFIED -> {
                                val index = users.indexOfFirst { it.id == user.id }
                                if (index != -1) {
                                    users[index] = user
                                }
                            }
                            DocumentChange.Type.REMOVED -> users.remove(user)
                        }
                    }
                }
                adapter.setUsers(users)
            }
    }

    private fun stopListeningForUsers() {
        userListener.remove()
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
data class User(val id: String, val nombre: String, val imagenId: Int)

