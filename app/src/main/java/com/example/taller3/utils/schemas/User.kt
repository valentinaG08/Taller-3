package com.example.taller3.utils.schemas

data class User (
    val id: String = "", //
    val firstName: String = "", //
    val identificationNumber: Long = 0, //
    val lastName: String = "", //
    val latitude: Double = 0.0, //
    val longitude: Double = 0.0, //
    val imagenId: String? = "", //
    val available: Boolean = false //
) {

}
