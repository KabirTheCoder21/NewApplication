package com.example.application

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.application.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val REQUEST_IMAGE_PICK = 100
    private lateinit var permissionLauncher : ActivityResultLauncher<String>
    private var isReadPermissionGranted = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        pickImageFromGallery()
    }

    private fun pickImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, REQUEST_IMAGE_PICK)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_IMAGE_PICK && resultCode == Activity.RESULT_OK && data != null) {
            // Handle the picked image here
            val selectedImageUri = data.data
            // Now you can do whatever you want with the selected image URI
            // For example, display it in an ImageView
            // imageView.setImageURI(selectedImageUri)
            binding.showImage.setImageURI(selectedImageUri)
        } else {
            // Image picking was canceled or unsuccessful
            Toast.makeText(
                this,
                "Image picking was canceled or unsuccessful",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
 private fun hasPermission(permission: String) : Boolean
 {
     return ContextCompat.checkSelfPermission(
         this,
         permission
     )==PackageManager.PERMISSION_GRANTED
 }
    fun readPermission()
    {
        var permission = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
        {
                android.Manifest.permission.READ_MEDIA_IMAGES

        } else {
            android.Manifest.permission.READ_EXTERNAL_STORAGE
        }
      //  if(!hasPermission(permission))
    }
}