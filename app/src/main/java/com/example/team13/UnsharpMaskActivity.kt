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
import kotlinx.android.synthetic.main.activity_main.imageView
import kotlinx.android.synthetic.main.activity_unsharpmask.*
import kotlin.math.abs

class UnsharpMaskActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_unsharpmask)

        getImage()

        actionUnsharpBtn.setOnClickListener {
            unsharpMasking()
        }
    }

    private fun getImage() {
        val selectedImageURI = intent.getStringExtra("ImageUri")!!.toUri()
        val source = ImageDecoder.createSource(this.contentResolver, selectedImageURI)
        val bmpImage = ImageDecoder.decodeBitmap(source).copy(Bitmap.Config.RGBA_F16, true)

        imageView.setImageBitmap(bmpImage)
    }


    private fun createUnsharpPicture(
        originalBitmap: Bitmap,
        bluredBitmap: Bitmap,
        amountValue: Double,
        thresholdValue: Int
    ): Bitmap {
        val pictureWidth: Int = originalBitmap.width
        val pictureHeight: Int = originalBitmap.height
        val newPicture = Bitmap.createBitmap(pictureWidth, pictureHeight, originalBitmap.config)

        for (i in 0 until pictureWidth) {
            for (j in 0 until pictureHeight) {
                var newPixel: Int = originalBitmap.getPixel(i, j)
                var newPixelAlpha = Color.alpha(newPixel)
                var newPixelRed = Color.red(newPixel)
                var newPixelGreen = Color.green(newPixel)
                var newPixelBlue = Color.blue(newPixel)

                val originalPixelColor: Int = originalBitmap.getPixel(i, j)
                val originalPixelAlpha = Color.alpha(originalPixelColor)
                val originalPixelRed = Color.red(originalPixelColor)
                val originalPixelGreen = Color.green(originalPixelColor)
                val originalPixelBlue = Color.blue(originalPixelColor)

                val bluredPixelColor: Int = bluredBitmap.getPixel(i, j)
                val bluredPixelAlpha = Color.alpha(bluredPixelColor)
                val bluredPixelRed = Color.red(bluredPixelColor)
                val bluredPixelGreen = Color.green(bluredPixelColor)
                val bluredPixelBlue = Color.blue(bluredPixelColor)

                if (abs(originalPixelAlpha - bluredPixelAlpha) > thresholdValue) {
                    newPixelAlpha =
                        (originalPixelAlpha + (originalPixelAlpha - bluredPixelAlpha) * amountValue).toInt()
                    if (newPixelAlpha < 0) {
                        newPixelAlpha = 0
                    } else {
                        if (newPixelAlpha > 255) {
                            newPixelAlpha = 255
                        }
                    }
                }

                if (abs(originalPixelRed - bluredPixelRed) > thresholdValue) {
                    newPixelRed =
                        (originalPixelRed + (originalPixelRed - bluredPixelRed) * amountValue).toInt()
                    if (newPixelRed < 0) {
                        newPixelRed = 0
                    } else {
                        if (newPixelRed > 255) {
                            newPixelRed = 255
                        }
                    }
                }

                if (abs(originalPixelGreen - bluredPixelGreen) > thresholdValue) {
                    newPixelGreen =
                        (originalPixelGreen + (originalPixelGreen - bluredPixelGreen) * amountValue).toInt()
                    if (newPixelGreen < 0) {
                        newPixelGreen = 0
                    } else {
                        if (newPixelGreen > 255) {
                            newPixelGreen = 255
                        }
                    }
                }

                if (abs(originalPixelBlue - bluredPixelBlue) > thresholdValue) {
                    newPixelBlue =
                        (originalPixelBlue + (originalPixelBlue - bluredPixelBlue) * amountValue).toInt()

                    if (newPixelBlue < 0) {
                        newPixelBlue = 0
                    } else {
                        if (newPixelBlue > 255) {
                            newPixelBlue = 255
                        }
                    }
                }

                newPixel = Color.argb(newPixelAlpha, newPixelRed, newPixelGreen, newPixelBlue)
                newPicture.setPixel(i, j, newPixel)
            }
        }

        return newPicture
    }

    private fun unsharpMasking() {

        val imageView1: ImageView = findViewById(R.id.imageView)
        val bitmap1 = (imageView1.drawable as BitmapDrawable).bitmap
        val newPicture: Bitmap

        val amountValue: SeekBar = findViewById(R.id.amountSeekBar)
        val currentNumberAmount: Double = amountValue.progress * 0.005

        val radiusValue: SeekBar = findViewById(R.id.radiusSeekBar)
        val sigma: Double = radiusValue.progress / 20.0
        val currentNumberRadius: Int = (3 * sigma).toInt()

        val thresholdValue: SeekBar = findViewById(R.id.thresholdSeekBar)
        val currentNumberThreshold: Int = thresholdValue.progress

        val bluredPicture = FiltersActivity()
        val newBluredPicture = bluredPicture.makeGaussianBlur(bitmap1, currentNumberRadius, sigma)

        newPicture = createUnsharpPicture(
            bitmap1,
            newBluredPicture,
            currentNumberAmount,
            currentNumberThreshold
        )

        imageView1.setImageBitmap(newPicture)
    }
}