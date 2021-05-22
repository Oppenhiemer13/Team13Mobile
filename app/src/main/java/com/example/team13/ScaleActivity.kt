package com.example.team13

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.ImageDecoder
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.graphics.drawable.toBitmap
import androidx.core.net.toUri
import androidx.core.view.isVisible
import kotlinx.android.synthetic.main.scale_activity.*
import kotlinx.android.synthetic.main.scale_activity.actionBtn
import kotlinx.android.synthetic.main.scale_activity.imageView
import kotlin.math.ceil
import kotlin.math.floor

class ScaleActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.scale_activity)

        getImage()

        actionBtn.setOnClickListener {

            if(scaleEditText.text.toString().toDouble() > 0.0) {
                interpolation()
            }
            else{
                Toast.makeText(this, "Enter a coefficient greater than 0", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getImage() {
        val selectedImageURI = intent.getStringExtra("ImageUri")!!.toUri()
        val source = ImageDecoder.createSource(this.contentResolver, selectedImageURI)
        val bmpImage = ImageDecoder.decodeBitmap(source).copy(Bitmap.Config.RGBA_F16, true)

        imageView.setImageBitmap(bmpImage)
    }

    private fun interpolation() {

        val bmpImage = imageView.drawable.toBitmap()
        val scale = scaleEditText.text.toString().toDouble()


        val oldWidth = bmpImage.width
        val oldHeight = bmpImage.height
        val newWidth = (bmpImage.width * scale).toInt()
        val newHeight = (bmpImage.height * scale).toInt()

        val newBmp = Bitmap.createBitmap(newWidth, newHeight, Bitmap.Config.RGBA_F16)


        val xRatio: Float = (oldWidth - 1).toFloat() / (newWidth - 1)
        val yRatio: Float = (oldHeight - 1).toFloat() / (newHeight - 1)

        for (x in 0 until newWidth) {
            for (y in 0 until newHeight) {

                val xTop = floor(xRatio * x).toInt()
                val yLeft = floor(yRatio * y).toInt()

                val xBot = ceil(xRatio * x).toInt()
                val yRight = ceil(yRatio * y).toInt()

                val xDelta = (xRatio * x) - xTop
                val yDelta = (yRatio * y) - yLeft

                val f00 = bmpImage.getColor(xTop, yLeft)
                val f10 = bmpImage.getColor(xBot, yLeft)
                val f01 = bmpImage.getColor(xTop, yRight)
                val f11 = bmpImage.getColor(xBot, yRight)

                val newPixelRed =
                    f00.red() * (1 - xDelta) * (1 - yDelta) + f10.red() * xDelta * (1 - yDelta) +
                            f01.red() * yDelta * (1 - xDelta) + f11.red() * xDelta * yDelta

                val newPixelGreen =
                    f00.green() * (1 - xDelta) * (1 - yDelta) + f10.green() * xDelta * (1 - yDelta) +
                            f01.green() * yDelta * (1 - xDelta) + f11.green() * xDelta * yDelta

                val newPixelBlue =
                    f00.blue() * (1 - xDelta) * (1 - yDelta) + f10.blue() * xDelta * (1 - yDelta) +
                            f01.blue() * yDelta * (1 - xDelta) + f11.blue() * xDelta * yDelta

                newBmp.setPixel(x, y, Color.rgb(newPixelRed, newPixelGreen, newPixelBlue))
            }
        }

        imageView.setImageBitmap(newBmp)
    }
}