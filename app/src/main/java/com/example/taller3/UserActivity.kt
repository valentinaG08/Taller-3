package com.example.taller3

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user)

        recyclerView = findViewById(R.id.recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = UserAdapter(emptyList()) { usuario ->
            // Implementar ver ubicación del usuario en tiempo real aqui
            showToast("Clic en usuario: ${usuario.nombre}")
        }
        recyclerView.adapter = adapter

        firestore = FirebaseFirestore.getInstance()
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

                val usuarios = mutableListOf<User>()
                snapshot?.let { documentSnapshot ->
                    for (change in documentSnapshot.documentChanges) {
                        val usuario = change.document.toObject(User::class.java)
                        when (change.type) {
                            DocumentChange.Type.ADDED -> usuarios.add(usuario)
                            DocumentChange.Type.MODIFIED -> {
                                val index = usuarios.indexOfFirst { it.id == usuario.id }
                                if (index != -1) {
                                    usuarios[index] = usuario
                                }
                            }
                            DocumentChange.Type.REMOVED -> usuarios.remove(usuario)
                        }
                    }
                }
                adapter.setUsers(usuarios)
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

