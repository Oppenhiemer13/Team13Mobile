package com.example.team13

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    private val RESULT_LOAD_IMG = 1000

    companion object{
        lateinit var transmissionImg : String
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        loadImgBtn.isEnabled = false
        actionBtn.isEnabled = true
        isEnabledCheck()

        loadImgBtn.setOnClickListener {
            val photoPicker = Intent(Intent.ACTION_GET_CONTENT);
            photoPicker.type = "image/*"
            startActivityForResult(photoPicker, RESULT_LOAD_IMG)
        }

        greenFilterBtn.setOnClickListener{
            val intent = Intent(this, ScaleActivity::class.java)
            intent.putExtra("URI", transmissionImg)
            startActivity(intent)
        }

        blurBtn.setOnClickListener{
            val intent = Intent(this, FiltersActivity::class.java)
            intent.putExtra("URI", transmissionImg)
            startActivity(intent)
        }
    }

    private fun isEnabledCheck() {

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {  //запрос на доступ к данным
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 111)
            } else {

                loadImgBtn.isEnabled = true
            }
        }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) { //доступ к данным
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)

            if (requestCode == 111 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                loadImgBtn.isEnabled = true
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
        }
}


