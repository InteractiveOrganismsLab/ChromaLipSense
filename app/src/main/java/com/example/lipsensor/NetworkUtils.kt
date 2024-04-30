package com.example.lipsensor

import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class NetworkUtils {
    companion object {
        fun sendImageToServer(imageFile: File, serverUrl: String): Boolean {
            return try {
                val url = URL(serverUrl)
                val connection = url.openConnection() as HttpURLConnection
                connection.doOutput = true
                connection.requestMethod = "POST"
                connection.setRequestProperty("Content-Type", "multipart/form-data;boundary=${System.currentTimeMillis()}")

                val outputStream = DataOutputStream(connection.outputStream)
                val fileInputStream = FileInputStream(imageFile)
                val buffer = ByteArray(4096)
                var bytesRead: Int

                // Write file data to the output stream
                while (fileInputStream.read(buffer).also { bytesRead = it } != -1) {
                    outputStream.write(buffer, 0, bytesRead)
                }

                outputStream.flush()
                outputStream.close()
                fileInputStream.close()

                // Check response code
                val responseCode = connection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    // Success
                    // Read response from server if needed
                    val inputStream = BufferedReader(InputStreamReader(connection.inputStream))
                    var line: String?
                    val response = StringBuilder()
                    while (inputStream.readLine().also { line = it } != null) {
                        response.append(line)
                    }
                    inputStream.close()
                    val serverResponse = response.toString()
                    println("Server response: $serverResponse")
                } else {
                    // Error handling
                    println("Server returned HTTP response code: $responseCode")
                }

                connection.disconnect()
                true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }
}