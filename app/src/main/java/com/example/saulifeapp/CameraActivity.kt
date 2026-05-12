package com.example.saulifeapp

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.saulifeapp.data.remote.GeminiVisionService
import com.example.saulifeapp.databinding.ActivityCameraBinding
import com.example.saulifeapp.ui.scanresult.ScanResultActivity
import kotlinx.coroutines.launch
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CameraActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCameraBinding
    private var imageCapture: ImageCapture? = null
    private lateinit var cameraExecutor: ExecutorService

    private val geminiVisionService = GeminiVisionService()

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) startCamera()
            else {
                Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show()
                finish()
            }
        }

    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            if (uri != null) {
                val bitmap = uriToBitmap(uri)
                if (bitmap != null) {
                    analyzeImageWithGemini(bitmap)
                } else {
                    Toast.makeText(this, "Не удалось открыть фото", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Файл не выбран", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCameraBinding.inflate(layoutInflater)
        setContentView(binding.root)

        cameraExecutor = Executors.newSingleThreadExecutor()

        setScanButtonEnabled(false)

        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.btnCapture.setOnClickListener {
            takePhotoAndAnalyze()
        }

        binding.btnUpload.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_GRANTED
        ) {
            startCamera()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            val preview = androidx.camera.core.Preview.Builder().build().also {
                it.surfaceProvider = binding.previewView.surfaceProvider
            }

            imageCapture = ImageCapture.Builder().build()

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this,
                    cameraSelector,
                    preview,
                    imageCapture
                )

                setScanButtonEnabled(true)

            } catch (e: Exception) {
                setScanButtonEnabled(false)
                Toast.makeText(this, "Camera start failed: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun takePhotoAndAnalyze() {
        val localImageCapture = imageCapture ?: return

        setLoadingState(true)

        val photoFile = File(
            cacheDir,
            "saulife_scan_${System.currentTimeMillis()}.jpg"
        )

        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        localImageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {

                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    val bitmap = BitmapFactory.decodeFile(photoFile.absolutePath)
                    if (bitmap != null) {
                        analyzeImageWithGemini(bitmap)
                    } else {
                        setLoadingState(false)
                        Toast.makeText(
                            this@CameraActivity,
                            "Не удалось обработать фото",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onError(exception: ImageCaptureException) {
                    setLoadingState(false)
                    Toast.makeText(
                        this@CameraActivity,
                        "Ошибка камеры: ${exception.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        )
    }

    private fun analyzeImageWithGemini(bitmap: Bitmap) {
        setLoadingState(true)

        lifecycleScope.launch {
            val result = geminiVisionService.extractMedicineName(bitmap)

            val medicineName = result.getOrElse {
                setLoadingState(false)
                Toast.makeText(
                    this@CameraActivity,
                    "Не удалось распознать лекарство: ${it.message}",
                    Toast.LENGTH_LONG
                ).show()
                return@launch
            }

            setLoadingState(false)

            val intent = Intent(this@CameraActivity, ScanResultActivity::class.java)
            intent.putExtra("ocr_text", medicineName)
            startActivity(intent)
        }
    }

    private fun setLoadingState(isLoading: Boolean) {
        binding.btnCapture.isEnabled = !isLoading && imageCapture != null
        binding.btnUpload.isEnabled = !isLoading

        binding.btnCapture.text = if (isLoading) {
            "Анализируем..."
        } else {
            "Сканировать"
        }

        binding.btnCapture.alpha = if (binding.btnCapture.isEnabled) 1f else 0.5f
        binding.btnUpload.alpha = if (binding.btnUpload.isEnabled) 1f else 0.5f
    }

    private fun setScanButtonEnabled(enabled: Boolean) {
        binding.btnCapture.isEnabled = enabled
        binding.btnCapture.alpha = if (enabled) 1f else 0.5f
    }

    private fun uriToBitmap(uri: Uri): Bitmap? {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val source = android.graphics.ImageDecoder.createSource(contentResolver, uri)
                android.graphics.ImageDecoder.decodeBitmap(source)
            } else {
                @Suppress("DEPRECATION")
                MediaStore.Images.Media.getBitmap(contentResolver, uri)
            }
        } catch (e: Exception) {
            null
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
}