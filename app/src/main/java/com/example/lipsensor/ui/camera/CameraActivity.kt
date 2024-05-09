package com.example.lipsensor.ui.camera

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.lipsensor.R
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.core.Preview
import java.lang.Exception
import com.example.lipsensor.databinding.ActivityCameraBinding
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

import com.example.lipsensor.network.MarsApi
import java.io.FileInputStream

sealed interface MarsUiState {
    data class Success(val photos: String) : MarsUiState
    object Error : MarsUiState
    object Loading : MarsUiState
}

class CameraActivity : AppCompatActivity() {
    private lateinit var _binding: ActivityCameraBinding
    private var imageCapture: ImageCapture? = null
    private lateinit var outputDirectory: File

    private lateinit var cameraExecutor: ExecutorService

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private var savedImageUri: Uri? =
        null // Variable to store the URI of the most recently saved image

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_camera)
        _binding = ActivityCameraBinding.inflate(layoutInflater)
        setContentView(binding.root)
        outputDirectory = getOutputDirectory()
        cameraExecutor = Executors.newSingleThreadExecutor()

        if (allPermissionGranted()) {
            Toast.makeText(
                this,
                "We Have Permission",
                Toast.LENGTH_SHORT
            ).show()
            startCamera()
        } else {
            ActivityCompat.requestPermissions(
                this, Constants.REQUIRED_PERMISSIONS,
                Constants.REQUEST_CODE_PERMISSIONS
            )
        }
        binding.button.setOnClickListener {
            Toast.makeText(
                this,
                "Clicked",
                Toast.LENGTH_SHORT
            ).show()
            takePhoto()
        }
        binding.send.setOnClickListener {
            Toast.makeText(
                this,
                "Sent",
                Toast.LENGTH_SHORT
            ).show()
            //sendPhoto()
        }
    }

//    private fun sendPhoto() {
//        Log.d(
//            Constants.TAG,
//            "Start sendPicture"
//        )
//        savedImageUri?.let { uri ->
//            val filePath = uri.path
//            if (filePath != null) {
//                sendImageFile(File(filePath))
//            } else {
//                // Handle the case where the URI doesn't have a valid file path
//                Toast.makeText(this, "Failed to get file path from URI", Toast.LENGTH_SHORT).show()
//            }
//        } ?: run {
//            // Handle the case where savedImageUri is null
//            Toast.makeText(this, "No image taken yet.", Toast.LENGTH_SHORT).show()
//        }
//    }

//    private fun sendImageFile(file: File) {
//        // Read the bytes from the image file
//        val fileBytes = FileInputStream(file).use { it.readBytes() }
//
//        // Send the bytes to the server
//        try {
//            val response = MarsApi.retrofitService.sendPhotos(fileBytes)
//            if (response.isSuccessful) {
//                println("Photo sent successfully")
//            } else {
//                println("Failed to send photo: ${response.code()}")
//            }
//        } catch (e: Exception) {
//            println("Error sending photo: ${e.message}")
//        }
//    }

    private fun getOutputDirectory(): File {
        val mediaDir = externalMediaDirs.firstOrNull()?.let { mFile ->
            File(mFile, resources.getString(R.string.app_name)).apply {
                mkdirs()
            }
        }
        return if (mediaDir != null && mediaDir.exists())
            mediaDir else filesDir
    }

    private fun takePhoto() {
        Log.d(
            Constants.TAG,
            "Start takePicture"
        )
        val imageCapture = imageCapture ?: return
        val photoFile = File(
            outputDirectory,
            SimpleDateFormat(
                Constants.FILE_NAME_FORMAT,
                Locale.getDefault()
            ).format(
                System
                    .currentTimeMillis()
            ) + ".jpg"
        )

        val outputOption = ImageCapture
            .OutputFileOptions
            .Builder(photoFile)
            .build()

        imageCapture.takePicture(
            outputOption, ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {

                    savedImageUri = Uri.fromFile(photoFile)
                    val msg = "Photo Saved"
                    Log.d(
                        Constants.TAG,
                        "Photo Saved"
                    )
                    Toast.makeText(
                        this@CameraActivity,
                        "$msg $savedImageUri",
                        Toast.LENGTH_LONG
                    ).show()

                }

                override fun onError(exception: ImageCaptureException) {
                    Log.e(
                        Constants.TAG,
                        "onError: ${exception.message}",
                        exception
                    )
                }
            }
        )
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener(Runnable {
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build()

            imageCapture = ImageCapture.Builder()
                .setTargetRotation(binding.viewFinder.display.rotation)
                .build()

            val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture)

                preview.setSurfaceProvider(binding.viewFinder.surfaceProvider)
                Log.d(Constants.TAG, "Start Camera")
            } catch (e: Exception) {
                Log.e(Constants.TAG, "Failed to bind camera to lifecycle", e)
            }
        }, ContextCompat.getMainExecutor(this))
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == Constants.REQUEST_CODE_PERMISSIONS) {
            if (allPermissionGranted()) {
                startCamera()
            } else {
                Toast.makeText(
                    this, "Permissions not granted by the user.",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }
        }
    }

    private fun allPermissionGranted() =
        Constants.REQUIRED_PERMISSIONS.all {
            ContextCompat.checkSelfPermission(
                baseContext, it
            ) == PackageManager.PERMISSION_GRANTED
        }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
}
