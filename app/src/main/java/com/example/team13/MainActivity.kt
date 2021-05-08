package com.example.team13

import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.graphics.drawable.toBitmap
import kotlinx.android.synthetic.main.activity_main.*
import java.io.OutputStream
import java.net.URI
import java.util.*


class MainActivity : AppCompatActivity() {

    private val RESULT_LOAD_IMG = 1000
    private val RESULT_CALL_CAMERA = 1001

    companion object{
        lateinit var transmissionImg : String
        var cameraUri : Uri? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        loadImgBtn.isEnabled = false
        cameraBtn.isEnabled = false
        isEnabledCheck()

        actionBtn.setOnClickListener {
            saveToGallery()
        }

        loadImgBtn.setOnClickListener {
            val photoPicker = Intent(Intent.ACTION_GET_CONTENT);
            photoPicker.type = "image/*"
            startActivityForResult(photoPicker, RESULT_LOAD_IMG)
        }

        cameraBtn.setOnClickListener{
            val values = ContentValues()
            values.put(MediaStore.Images.Media.TITLE, "New Picture")
            values.put(MediaStore.Images.Media.DESCRIPTION, "From the Camera")
            cameraUri =  contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)

            val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, cameraUri)
            startActivityForResult(cameraIntent, RESULT_CALL_CAMERA)
        }

        scaleBtn.setOnClickListener{
            val intent = Intent(this, ScaleActivity::class.java)
            intent.putExtra("ImageUri", transmissionImg)
            startActivity(intent)
        }

        filtersBtn.setOnClickListener{
            val intent = Intent(this, FiltersActivity::class.java)
            intent.putExtra("ImageUri", transmissionImg)
            startActivity(intent)
        }

        unsharpMaksBtn.setOnClickListener{
            val intent = Intent(this, UnsharpMaskActivity::class.java)
            intent.putExtra("BitmapImage", transmissionImg)
            startActivity(intent)
        }

        button.setOnClickListener{
            val intent = Intent(this, TestActivity::class.java)
            startActivity(intent)
        }
    }

    private fun isEnabledCheck() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {  //запрос на доступ к данным
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 111)
        } else {
            loadImgBtn.isEnabled = true
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 112)
        } else {
            cameraBtn.isEnabled = true
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 113)
        } else {
            actionBtn.isEnabled = true
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) { //доступ к данным
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == 111 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            loadImgBtn.isEnabled = true
        }

        if (requestCode == 112 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            cameraBtn.isEnabled = true
        }

        if (requestCode == 113 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            actionBtn.isEnabled = true
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {   //вывод картинки на ImageView
            super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK && requestCode == RESULT_LOAD_IMG) {

            val selectedImageURI = data?.data

            if (selectedImageURI != null) {

                val source = ImageDecoder.createSource(this.contentResolver, selectedImageURI)
                val bmpImage = ImageDecoder.decodeBitmap(source).copy(Bitmap.Config.RGBA_F16, true)

                transmissionImg = selectedImageURI.toString()
                imageView.setImageBitmap(bmpImage)
            }
        }
        else if(resultCode == Activity.RESULT_OK && requestCode == RESULT_CALL_CAMERA){

            if (cameraUri != null) {

                val source = ImageDecoder.createSource(this.contentResolver, cameraUri!!)
                val bmpImage = ImageDecoder.decodeBitmap(source).copy(Bitmap.Config.RGBA_F16, true)

                transmissionImg = cameraUri!!.toString()
                imageView.setImageBitmap(bmpImage)
            }
        }
    }

    private fun saveToGallery() {
        val bitmap = imageView.drawable.toBitmap()
        val fos : OutputStream
        val resolver = contentResolver
        val contentValues = ContentValues()
        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, "test")
        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpg")
        contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
        val imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
        fos = resolver.openOutputStream(Objects.requireNonNull(imageUri)!!)!!
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100,fos)
        Objects.requireNonNull<OutputStream?>(fos)
        Toast.makeText(this, "Image saved", Toast.LENGTH_SHORT).show()
    }
}


