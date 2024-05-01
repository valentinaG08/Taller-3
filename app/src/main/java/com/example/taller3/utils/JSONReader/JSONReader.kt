package com.example.taller3.utils.JSONReader

import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.io.InputStream
import java.io.Writer

class JSONReader {

    companion object {
        /**
         * For external storage: parent = baseContext.getExternalFilesDir(null)
         */
        fun writeJSONObject(filename: String, parent: String, data: String) {
            val output: Writer?
            try {
                val file = File(parent, filename)
                output = BufferedWriter(FileWriter(file))
                output.write(data)
                output.close()
            } catch (e: Exception) {
                //Log error
            }
        }

        /**
         * For external storage: parent = baseContext.getExternalFilesDir(null)
         */
        fun readJSONFile(parent: String?, filename: String?, file: InputStream?) : JSONObject {
            val finalFile = file ?: File(parent, filename!!).inputStream()

            val size = finalFile.available()
            val buffer = ByteArray(size)
            finalFile.read(buffer)

            val JSONString = String(buffer)
            return JSONObject(JSONString)
        }
    }

}