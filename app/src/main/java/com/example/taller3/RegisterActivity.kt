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
import com.example.taller3.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var storage: FirebaseStorage
    private var selectedImageUri: Uri? = null

    private lateinit var binding: ActivityRegisterBinding;

    companion object {
        private const val TAG = "RegisterActivity"
        private const val PICK_IMAGE_REQUEST = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRegisterBinding.inflate(layoutInflater)

        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        storage = FirebaseStorage.getInstance()

        binding.btnRegister.setOnClickListener {
            performRegister()
        }

        binding.btnSelectImage.setOnClickListener {
            selectImage()
        }
    }

    private fun performRegister() {
        val email = binding.etEmail.text.toString()
        val password = binding.etPassword.text.toString()

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Por favor, ingrese correo electrónico y contraseña", Toast.LENGTH_SHORT).show()
            return
        }

        if (selectedImageUri == null) {
            Toast.makeText(this, "Seleccione una foto porfavor", Toast.LENGTH_SHORT).show()
            return
        }

        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Registro exitoso
                        Log.d(TAG, "createUserWithEmail:success")
                        Toast.makeText(baseContext, "Guardando datos...", Toast.LENGTH_SHORT).show();
                        val user = auth.currentUser
                        saveUserDataToDatabase(user?.uid)
                    } else {
                        // Fallo en el registro
                        Log.w(TAG, "createUserWithEmail:failure", task.exception)
                        Toast.makeText(baseContext, "Falló el registro del usuario.",
                                Toast.LENGTH_SHORT).show()
                    }
                }
    }

    private fun saveUserDataToDatabase(userId: String?) {
        userId?.let {
            val ref = database.getReference("users").child(it)
            val firstName = binding.etFirstName.text.toString()
            val lastName = binding.etLastName.text.toString()
            val identificationNumber = binding.etIdentificationNumber.text.toString()
            val latitude = binding.etLatitude.text.toString().toDoubleOrNull()
            val longitude = binding.etLongitude.text.toString().toDoubleOrNull()

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
                binding.ivSelectedImage.setImageURI(it)
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