package com.example.team13

import android.annotation.SuppressLint
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MotionEvent
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.Toast
import androidx.core.graphics.set
import androidx.core.net.toUri
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_retouching.*
import kotlinx.android.synthetic.main.activity_retouching.imageView
import kotlinx.android.synthetic.main.activity_retouching.seekBar
import kotlinx.android.synthetic.main.activity_rotate.*

class RetouchingActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_retouching)

        getImage()

        retouchBtn.setOnClickListener {
            retouching()
        }

        seekBar.setOnSeekBarChangeListener(
            object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    string1.text = "radius: $progress"
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {
                }

                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                }
            }
        )
        seekBar2.setOnSeekBarChangeListener(
            object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    string2.text = "ratio: $progress"
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {
                }

                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                }
            }
        )

    }

    private fun getImage(){
        val selectedImageURI = intent.getStringExtra("ImageUri")!!.toUri()
        val source = ImageDecoder.createSource(this.contentResolver, selectedImageURI)
        val bmpImage = ImageDecoder.decodeBitmap(source).copy(Bitmap.Config.RGBA_F16, true)

        imageView.setImageBitmap(bmpImage)
    }



    @SuppressLint("ClickableViewAccessibility")
    private fun retouching() {
        val imageView: ImageView = findViewById(R.id.imageView)
        val bitmap = (imageView.getDrawable() as BitmapDrawable).bitmap
        //val canvas = Canvas(bitmap)

        imageView.setOnTouchListener { arg0, event ->
            var matrix = Matrix()
            imageView.getImageMatrix().invert(matrix)
            val arr = floatArrayOf(event.x, event.y)
            matrix.mapPoints(arr)
            var x = arr[0].toDouble()
            var y = arr[1].toDouble()
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    var alpha = 0
                    var red = 0
                    var green = 0
                    var blue = 0
                    var kP = 0
                    for (i in x.toInt()-seekBar.progress until x.toInt()+seekBar.progress) {
                        for (j in y.toInt()-seekBar.progress until y.toInt()+seekBar.progress) {
                            val pixel = bitmap.getPixel(i, j)
                            red = (red* kP + Color.red(pixel))/(kP + 1)
                            green = (green * kP + Color.green(pixel))/(kP + 1)
                            blue = (blue * kP + Color.blue(pixel))/(kP + 1)
                            alpha = (alpha * kP + Color.alpha(pixel))/(kP + 1)
                            kP++
                        }
                    }
                    alpha = (alpha*(1- seekBar2.progress * 0.1)).toInt()
                    val middleColor = Color.argb(alpha, red, green, blue)
                    //val paint = Paint()
                    // paint.setColor(middleColor)
                    //paint.setStyle(Paint.Style.FILL)
                    //canvas.drawCircle(x.toFloat(), y.toFloat(), seekBar.progress.toFloat(), paint)
                    //var k = 0
                    for (i in x.toInt()-seekBar.progress until x.toInt()+seekBar.progress) {
                        for (j in y.toInt()-seekBar.progress until y.toInt()+seekBar.progress) {
                            //val pixel = bitmap.getPixel(i, j)
                            // k = ((i-x)*(i-x)+(j-y)*(j-y)).toInt()
                            //val newColor = ((seekBar.progress-k)*middleColor + k*pixel)/(seekBar.progress)
                            bitmap.setPixel(i,j,middleColor)
                        }
                    }
                    arg0.postInvalidate()
                    arg0.performClick()
                }
                MotionEvent.ACTION_UP -> arg0.performClick()
                else -> touchUp()
            }
        }

    }

    fun touchUp(): Boolean {
        return true
    }
}