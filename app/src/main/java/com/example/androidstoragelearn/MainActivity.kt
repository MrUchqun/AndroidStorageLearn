package com.example.androidstoragelearn

import android.Manifest
import android.app.Activity
import android.content.ComponentName
import android.content.ContentUris
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.opengl.Visibility
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.Settings
import android.text.TextUtils
import android.util.Log
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import java.io.*
import java.nio.charset.Charset
import java.util.*
import kotlin.collections.ArrayList


class MainActivity : AppCompatActivity() {

    private var readPermissionGranted = false
    private var writePermissionGranted = false
    private var cameraPermissionGranted = false
    private var isPersistent = false
    private var isInternal = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        requestPermission()
        initViews()
    }

    override fun onResume() {
        super.onResume()
        checkPermissionBtnStatus()
    }

    private fun initViews() {
        val btnSaveInternal: Button = findViewById(R.id.btn_save_internal)
        val btnReadInternal: Button = findViewById(R.id.btn_read_internal)
        val btnDeleteInternal: Button = findViewById(R.id.btn_delete_internal)
        val btnSaveExternal: Button = findViewById(R.id.btn_save_external)
        val btnReadExternal: Button = findViewById(R.id.btn_read_external)
        val btnDeleteExternal: Button = findViewById(R.id.btn_delete_external)
        val btnTakePhoto: Button = findViewById(R.id.btn_take_photo)
        val btnGetInternal: Button = findViewById(R.id.btn_get_internal)
        val btnGetExternal: Button = findViewById(R.id.btn_get_external)


        btnSaveInternal.setOnClickListener {
            saveInternalFile("Welcome to PDP Academy!")
        }

        btnReadInternal.setOnClickListener {
            readInternalFile()
        }

        btnDeleteInternal.setOnClickListener {
            deleteInternalFile()
        }

        btnSaveExternal.setOnClickListener {
            saveExternalFile("Goodbye from PDP Academy!")
        }

        btnReadExternal.setOnClickListener {
            readExternalFile()
        }

        btnDeleteExternal.setOnClickListener {
            deleteExternalFile()
        }

        btnTakePhoto.setOnClickListener {
            takePhoto.launch()
        }

        btnGetInternal.setOnClickListener {
            startActivity(Intent(this, DetailsActivity::class.java).apply {
                putExtra("isInternal", true)
            })
        }

        btnGetExternal.setOnClickListener {
            startActivity(Intent(this, DetailsActivity::class.java).apply {
                putExtra("isInternal", false)
            })
        }
    }

    private fun checkStoragePaths() {
        val internal_m1 = getDir("custom", 0)
        val internal_m2 = filesDir

        val exteral_m1 = getExternalFilesDir(null)
        val exteral_m2 = externalCacheDir
        val exteral_m3 = getExternalFilesDir(Environment.DIRECTORY_PICTURES)

        Log.d("checkStoragePaths", internal_m1.absolutePath)
        Log.d("checkStoragePaths", internal_m2.absolutePath)
        Log.d("checkStoragePaths", exteral_m1!!.absolutePath)
        Log.d("checkStoragePaths", exteral_m2!!.absolutePath)
        Log.d("checkStoragePaths", exteral_m3!!.absolutePath)
    }

    private fun createInternalFile() {
        val fileName = "pdp_internal.txt"
        val file: File = if (isPersistent) File(filesDir, fileName) else File(cacheDir, fileName)
        if (!file.exists()) {
            try {
                file.createNewFile()
                Toast.makeText(
                    this,
                    String.format("File %s has been created", fileName),
                    Toast.LENGTH_SHORT
                ).show()
            } catch (e: IOException) {
                Toast.makeText(
                    this,
                    String.format("File %s creation failed", fileName),
                    Toast.LENGTH_SHORT
                ).show()
            }
        } else {
            Toast.makeText(
                this,
                String.format("File %s already exists", fileName),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    // PERMISSIONS

    private fun requestPermission() {

        val hasReadPermission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED

        val hasWritePermission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED

        val hasCameraPermission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

        val minSdk29 = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q

        readPermissionGranted = hasReadPermission
        writePermissionGranted = hasWritePermission || minSdk29
        cameraPermissionGranted = hasCameraPermission

        val permissionToRequest = mutableListOf<String>()
        if (!readPermissionGranted)
            permissionToRequest.add(Manifest.permission.READ_EXTERNAL_STORAGE)

        if (!writePermissionGranted)
            permissionToRequest.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)

        if (!cameraPermissionGranted)
            permissionToRequest.add(Manifest.permission.CAMERA)

        permissionToRequest.add(Manifest.permission.ACCESS_FINE_LOCATION)
        permissionToRequest.add(Manifest.permission.ACCESS_COARSE_LOCATION)

        if (permissionToRequest.isNotEmpty())
            permissionLauncher.launch(permissionToRequest.toTypedArray())
    }

    private fun checkPermission(): Boolean {
        val hasReadPermission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_DENIED

        val hasWritePermission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_DENIED

        val hasCameraPermission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_DENIED

        return hasCameraPermission || (hasReadPermission && hasWritePermission)
    }

    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            readPermissionGranted =
                permissions[Manifest.permission.READ_EXTERNAL_STORAGE] ?: readPermissionGranted
            writePermissionGranted =
                permissions[Manifest.permission.WRITE_EXTERNAL_STORAGE] ?: writePermissionGranted
            cameraPermissionGranted =
                permissions[Manifest.permission.CAMERA] ?: cameraPermissionGranted

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                when {
                    permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                        // Precise location access granted.
                    }
                    permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                        // Only approximate location access granted.
                    }
                    else -> {
                        // No location access granted.
                    }
                }
            }

            if (readPermissionGranted)
                Toast.makeText(
                    this,
                    String.format("READ_EXTERNAL_STORAGE"),
                    Toast.LENGTH_SHORT
                ).show()

            if (writePermissionGranted)
                Toast.makeText(
                    this,
                    String.format("WRITE_EXTERNAL_STORAGE"),
                    Toast.LENGTH_SHORT
                ).show()

            if (cameraPermissionGranted)
                Toast.makeText(
                    this,
                    String.format("CAMERA"),
                    Toast.LENGTH_SHORT
                ).show()
        }

    private fun checkPermissionBtnStatus() {
        val btnEnabledPermission: Button = findViewById(R.id.btn_enabled_permission)
        btnEnabledPermission.apply {
            setOnClickListener {
                startActivity(Intent().apply {
                    action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                    data = Uri.fromParts("package", packageName, null)
                })
            }
            visibility = if (checkPermission()) VISIBLE else GONE
        }
    }

    // SCOPED STORAGE

    private val takePhoto =
        registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->

            val filename = UUID.randomUUID().toString()

            val isPhotoSaved = try {

                if (isInternal) {
                    savePhotoToInternalStorage(filename, bitmap!!)
                } else {
                    if (writePermissionGranted) {
                        savePhotoToExternalStorage(filename, bitmap!!)
                    } else {
                        false
                    }
                }

            } catch (e: Exception) {
                false
            }

            if (isPhotoSaved) {
                Toast.makeText(
                    this,
                    String.format("Save photo"),
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                Toast.makeText(
                    this,
                    String.format("Couldn't save photo"),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    private fun savePhotoToInternalStorage(filename: String, bmp: Bitmap): Boolean {
        return try {
            openFileOutput("$filename.jpg", MODE_PRIVATE).use { stream ->
                if (!bmp.compress(Bitmap.CompressFormat.JPEG, 95, stream)) {
                    throw IOException("Couldn't save bitmap.")
                }
            }
            true
        } catch (e: IOException) {
            e.printStackTrace()
            false
        }
    }

    private fun savePhotoToExternalStorage(filename: String, bmp: Bitmap): Boolean {
        val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        }

        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "$filename.jpg")
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            put(MediaStore.Images.Media.WIDTH, bmp.width)
            put(MediaStore.Images.Media.HEIGHT, bmp.height)
        }
        return try {
            contentResolver.insert(collection, contentValues)?.also { uri ->
                contentResolver.openOutputStream(uri).use { outputStream ->
                    if (!bmp.compress(Bitmap.CompressFormat.JPEG, 95, outputStream)) {
                        throw IOException("Couldn't save bitmap")
                    }
                }
            } ?: throw IOException("Couldn't create MediaStore entry")
            true
        } catch (e: IOException) {
            e.printStackTrace()
            false
        }
    }

    // INTERNAL STORAGE

    private fun saveInternalFile(data: String) {
        val fileName = "pdp_internal.txt"

        try {
            val fileOutputStream: FileOutputStream =
                if (isPersistent) openFileOutput(fileName, MODE_PRIVATE) else {
                    val file = File(cacheDir, fileName)
                    FileOutputStream(file)
                }
            fileOutputStream.write(data.toByteArray(Charset.forName("UTF-8")))

            Toast.makeText(
                this,
                String.format("Write to %s successful", fileName),
                Toast.LENGTH_SHORT
            ).show()

        } catch (e: Exception) {
            e.printStackTrace()

            Toast.makeText(
                this,
                String.format("Write to file %s failed", fileName),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun readInternalFile() {
        val fileName = "pdp_internal.txt"

        try {

            val fileInputStream: FileInputStream =
                if (isPersistent) openFileInput(fileName) else {
                    val file = File(cacheDir, fileName)
                    FileInputStream(file)
                }

            val inputStreamReader = InputStreamReader(fileInputStream, Charset.forName("UTF-8"))

            val lines: MutableList<String?> = ArrayList()
            val reader = BufferedReader(inputStreamReader)
            var line = reader.readLine()
            while (line != null) {
                lines.add(line)
                line = reader.readLine()
            }

            val readText = TextUtils.join("\n", lines)

            Toast.makeText(
                this,
                String.format("Read from file %s successful", fileName),
                Toast.LENGTH_SHORT
            ).show()

            Log.d("@@@readInternalFile", readText)

        } catch (e: Exception) {
            e.printStackTrace()

            Toast.makeText(
                this,
                String.format("Read from file %s failed", fileName),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun deleteInternalFile() {
        val fileName = "pdp_internal.txt"
        val file: File = if (isPersistent) {
            File(filesDir, fileName)
        } else {
            File(cacheDir, fileName)
        }

        if (file.exists()) {
            file.delete()
            Toast.makeText(
                this,
                String.format("File %s has been deleted", fileName),
                Toast.LENGTH_SHORT
            ).show()
        } else {
            Toast.makeText(
                this,
                String.format("File %s doesn't exist", fileName),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    // EXTERNAL STORAGE

    private fun saveExternalFile(data: String) {
        val fileName = "pdp_external.txt"
        val file: File = if (isPersistent) File(getExternalFilesDir(null), fileName) else File(
            externalCacheDir,
            fileName
        )
        try {
            val fileOutputStream = FileOutputStream(file)
            fileOutputStream.write(data.toByteArray(Charset.forName("UTF-8")))

            Toast.makeText(
                this,
                String.format("Write to %s successful", fileName),
                Toast.LENGTH_SHORT
            ).show()

        } catch (e: Exception) {
            e.printStackTrace()

            Toast.makeText(
                this,
                String.format("Write to file %s failed", fileName),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun readExternalFile() {
        val fileName = "pdp_external.txt"
        val file: File = if (isPersistent) File(getExternalFilesDir(null), fileName)
        else File(externalCacheDir, fileName)
        try {
            val fileInputStream = FileInputStream(file)
            val inputStreamReader = InputStreamReader(fileInputStream, Charset.forName("UTF-8"))
            val lines: MutableList<String?> = ArrayList()
            val reader = BufferedReader(inputStreamReader)
            var line = reader.readLine()
            while (line != null) {
                lines.add(line)
                line = reader.readLine()
            }
            val readText = TextUtils.join("\n", lines)
            Toast.makeText(
                this,
                String.format("Read from file %s successful", fileName),
                Toast.LENGTH_SHORT
            ).show()

            Log.d("@@@readExternalFile", readText)

        } catch (e: Exception) {
            e.printStackTrace()

            Toast.makeText(
                this,
                String.format("Read from file %s failed", fileName),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun deleteExternalFile() {
        val fileName = "pdp_external.txt"
        val file: File = if (isPersistent) {
            File(getExternalFilesDir(null), fileName)
        } else {
            File(externalCacheDir, fileName)
        }
        if (file.exists()) {
            file.delete()
            Toast.makeText(
                this,
                String.format("File %s has been deleted", fileName),
                Toast.LENGTH_SHORT
            ).show()
        } else {
            Toast.makeText(
                this,
                String.format("File %s doesn't exist", fileName),
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}
