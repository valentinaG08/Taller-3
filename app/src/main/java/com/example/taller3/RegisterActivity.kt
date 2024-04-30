package com.example.taller3

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var storage: FirebaseStorage
    val btnRegister = findViewById<Button>(R.id.btn_register)
    val btnSelectImage = findViewById<Button>(R.id.btnSelectImage)
    val etEmail = findViewById<EditText>(R.id.etEmail)
    val etPassword = findViewById<EditText>(R.id.etPassword)
    val etFirstName = findViewById<EditText>(R.id.etFirstName)
    val etLastName = findViewById<EditText>(R.id.etLastName)
    val etIdentificationNumber = findViewById<EditText>(R.id.etIdentificationNumber)
    val etLatitude = findViewById<EditText>(R.id.etLatitude)
    val etLongitude = findViewById<EditText>(R.id.etLongitude)
    val ivSelectedImage = findViewById<ImageView>(R.id.ivSelectedImage)


    private var selectedImageUri: Uri? = null

    companion object {
        private const val TAG = "RegisterActivity"
        private const val PICK_IMAGE_REQUEST = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        storage = FirebaseStorage.getInstance()

        btnRegister.setOnClickListener {
            performRegister()
        }

        btnSelectImage.setOnClickListener {
            selectImage()
        }
    }

    private fun performRegister() {
        val email = etEmail.text.toString()
        val password = etPassword.text.toString()

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Por favor, ingrese correo electrónico y contraseña", Toast.LENGTH_SHORT).show()
            return
        }

        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Registro exitoso
                        Log.d(TAG, "createUserWithEmail:success")
                        val user = auth.currentUser
                        saveUserDataToDatabase(user?.uid)
                    } else {
                        // Fallo en el registro
                        Log.w(TAG, "createUserWithEmail:failure", task.exception)
                        Toast.makeText(baseContext, "Fallo en la autenticación.",
                                Toast.LENGTH_SHORT).show()
                    }
                }
    }

    private fun saveUserDataToDatabase(userId: String?) {
        userId?.let {
            val ref = database.getReference("users").child(it)
            val firstName = etFirstName.text.toString()
            val lastName = etLastName.text.toString()
            val identificationNumber = etIdentificationNumber.text.toString()
            val latitude = etLatitude.text.toString().toDoubleOrNull()
            val longitude = etLongitude.text.toString().toDoubleOrNull()

            val userData = HashMap<String, Any>()
            userData["firstName"] = firstName
            userData["lastName"] = lastName
            userData["identificationNumber"] = identificationNumber
            userData["latitude"] = latitude ?: 0.0
            userData["longitude"] = longitude ?: 0.0

            ref.setValue(userData)
                    .addOnSuccessListener {
                        Log.d(TAG, "User data saved to database")
                        uploadImageToStorage(userId)
                    }
                    .addOnFailureListener { e ->
                        Log.e(TAG, "Error saving user data to database", e)
                        Toast.makeText(this, "Error al guardar datos de usuario", Toast.LENGTH_SHORT).show()
                    }
        }
    }

    private fun selectImage() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            selectedImageUri = data.data
            selectedImageUri?.let {
                ivSelectedImage.setImageURI(it)
            }
        }
    }

    private fun uploadImageToStorage(userId: String?) {
        userId?.let {
            val ref = storage.reference.child("images/$userId")

            selectedImageUri?.let { uri ->
                ref.putFile(uri)
                        .addOnSuccessListener {
                            Log.d(TAG, "Image uploaded successfully")
                            Toast.makeText(this, "Registro completado", Toast.LENGTH_SHORT).show()
                            finish() // Regresa a la actividad anterior
                        }
                        .addOnFailureListener { e ->
                            Log.e(TAG, "Error uploading image", e)
                            Toast.makeText(this, "Error al subir imagen", Toast.LENGTH_SHORT).show()
                        }
            }
        }
    }
}