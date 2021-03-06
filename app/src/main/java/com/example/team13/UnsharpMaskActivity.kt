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
import kotlinx.android.synthetic.main.scale_activity.*
import kotlin.math.PI
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sqrt

class UnsharpMaskActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_unsharpmask)

        getImage()

        actionUnsharpBtn.setOnClickListener{
            unsharpMasking()
        }
    }

    private fun getImage(){
        val selectedImageURI = intent.getStringExtra("ImageUri")!!.toUri()
        val source = ImageDecoder.createSource(this.contentResolver, selectedImageURI)
        val bmpImage = ImageDecoder.decodeBitmap(source).copy(Bitmap.Config.RGBA_F16, true)

        imageView.setImageBitmap(bmpImage)
    }

    private fun normalizePixel(original: Int, blured:Int, threshold: Int, amount: Double, newPixel: Int): Int {

        var newPixelCopy = newPixel

        if (Math.abs(original - blured) > threshold) {
            newPixelCopy = (original + (original - blured) * amount).toInt()
            if (newPixelCopy < 0) {
                newPixelCopy = 0
            } else {
                if (newPixelCopy > 255) {
                    newPixelCopy = 255
                }
            }
        }

        return newPixelCopy
    }

    private fun createUnsharpPicture(originalBitmap: Bitmap, bluredBitmap: Bitmap, amountValue: Double, thresholdValue: Int): Bitmap {
        val pictureWidth: Int = originalBitmap.getWidth()
        val pictureHeight: Int = originalBitmap.getHeight()
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

                newPixelAlpha = normalizePixel(originalPixelAlpha, bluredPixelAlpha, thresholdValue, amountValue, newPixelAlpha)
                newPixelRed = normalizePixel(originalPixelRed, bluredPixelRed, thresholdValue, amountValue, newPixelRed)
                newPixelGreen = normalizePixel(originalPixelGreen, bluredPixelGreen, thresholdValue, amountValue, newPixelGreen)
                newPixelBlue = normalizePixel(originalPixelBlue, bluredPixelBlue, thresholdValue, amountValue, newPixelBlue)

                newPixel = Color.argb(newPixelAlpha, newPixelRed, newPixelGreen, newPixelBlue)
                newPicture.setPixel(i, j, newPixel)
            }
        }
        return newPicture
    }

    private fun unsharpMasking() {

        val imageView1: ImageView = findViewById(R.id.imageView)
        val bitmap1 = (imageView1.getDrawable() as BitmapDrawable).bitmap
        val pictureWidth: Int = bitmap1.getWidth()
        val pictureHeight: Int = bitmap1.getHeight()
        var newPicture = Bitmap.createBitmap(pictureWidth, pictureHeight, bitmap1.config)

        val amountValue: SeekBar = findViewById(R.id.amountSeekBar);
        var currentNumberAmount: Double = amountValue.progress * 0.005

        val radiusValue: SeekBar = findViewById(R.id.radiusSeekBar);
        val sigma: Double = radiusValue.progress / 20.0
        val currentNumberRadius: Int = (3 * sigma).toInt()

        val thresholdValue: SeekBar = findViewById(R.id.thresholdSeekBar);
        val currentNumberThreshold: Int = thresholdValue.progress

        val bluredPicture: FiltersActivity = FiltersActivity()
        val newBluredPicture = bluredPicture.makeGaussianBlur(bitmap1, currentNumberRadius, sigma)

        newPicture = createUnsharpPicture(bitmap1, newBluredPicture, currentNumberAmount, currentNumberThreshold)

        imageView1.setImageBitmap(newPicture)
    }
}