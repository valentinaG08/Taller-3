package com.example.taller3.utils.schemas

import org.json.JSONObject

class Location(var latitude: Double, var longitude: Double, var name: String) {

    fun toJSON() : JSONObject {
        val obj = JSONObject()
        obj.put("latitud", latitude)
        obj.put("longitug", longitude)
        obj.put("nombre", name)
        return obj
    }

}