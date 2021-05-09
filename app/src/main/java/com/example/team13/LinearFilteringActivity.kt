package com.example.team13

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.ImageDecoder
import android.graphics.drawable.BitmapDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import android.widget.SeekBar
import androidx.core.net.toUri
import kotlinx.android.synthetic.main.activity_filters.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main.imageView
import kotlinx.android.synthetic.main.activity_unsharpmask.*
import kotlin.math.PI
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sqrt

class LinearFilteringActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_linearfiltering)

        getImage()

    }

    private fun getImage(){
        val selectedImageURI = intent.getStringExtra("ImageUri")!!.toUri()
        val source = ImageDecoder.createSource(this.contentResolver, selectedImageURI)
        val bmpImage = ImageDecoder.decodeBitmap(source).copy(Bitmap.Config.RGBA_F16, true)

        imageView.setImageBitmap(bmpImage)
    }
}