package com.example.team13

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.ImageDecoder
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.SeekBar
import androidx.core.graphics.drawable.toBitmap
import androidx.core.net.toUri
import kotlinx.android.synthetic.main.activity_rotate.*
import kotlin.math.cos
import kotlin.math.sin

class RotateActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rotate)

        imageView.setImageBitmap(getImage())

        rotateButton90.setOnClickListener {
            val bitmap = imageView.drawable.toBitmap()
            imageView.setImageBitmap(rotate90(bitmap))
        }

        rotateButton180.setOnClickListener {
            val bitmap = imageView.drawable.toBitmap()
            imageView.setImageBitmap(rotate180(bitmap))
        }

        rotateButton270.setOnClickListener {
            val bitmap = imageView.drawable.toBitmap()
            imageView.setImageBitmap(rotate270(bitmap))
        }

        seekBar.setOnSeekBarChangeListener(
            object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    degreesString.text = "Degrees: $progress"
                    val bitmap = getImage()
                    var pr = progress.toFloat()
                    if (pr > 270) {
                        pr -= 270
                        imageView.setImageBitmap(bitmapRotate(pr, rotate270(bitmap)))
                    }
                    else if (pr > 180) {
                        pr -= 180
                        imageView.setImageBitmap(bitmapRotate(pr, rotate180(bitmap)))
                    }
                    else if (pr > 90) {
                        pr -= 90
                        imageView.setImageBitmap(bitmapRotate(pr, rotate90(bitmap)))
                    }
                    else {
                        imageView.setImageBitmap(bitmapRotate(progress.toFloat(), bitmap))
                    }
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {
                }

                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                }
            }
        )
    }

    private fun getImage(): Bitmap {
        val selectedImageURI = intent.getStringExtra("ImageUri")!!.toUri()
        val source = ImageDecoder.createSource(this.contentResolver, selectedImageURI)
        val bmpImage = ImageDecoder.decodeBitmap(source).copy(Bitmap.Config.RGBA_F16, true)

        return bmpImage
    }

    private fun rotate90(bitmap: Bitmap): Bitmap {
        val pictureWidth: Int = bitmap.getWidth()
        val pictureHeight: Int = bitmap.getHeight()
        val newBitmap = Bitmap.createBitmap(pictureHeight, pictureWidth, bitmap.config)

        for (x in 0 until pictureWidth) {
            for (y in 0 until pictureHeight) {
                val pixel: Int = bitmap.getPixel(x, y)
                newBitmap.setPixel(y, pictureWidth-x-1, pixel)
            }
        }
        return newBitmap
    }

    private fun rotate180(bitmap: Bitmap): Bitmap {
        val pictureWidth: Int = bitmap.getWidth()
        val pictureHeight: Int = bitmap.getHeight()
        val newBitmap = Bitmap.createBitmap(pictureWidth, pictureHeight, bitmap.config)

        for (x in 0 until pictureWidth) {
            for (y in 0 until pictureHeight) {
                val pixel: Int = bitmap.getPixel(x, y)
                newBitmap.setPixel(pictureWidth-x-1, pictureHeight-y-1, pixel)
            }
        }
        return newBitmap
    }

    private fun rotate270(bitmap: Bitmap): Bitmap {
        val pictureWidth: Int = bitmap.getWidth()
        val pictureHeight: Int = bitmap.getHeight()
        val newBitmap = Bitmap.createBitmap(pictureHeight, pictureWidth, bitmap.config)

        for (x in 0 until pictureWidth) {
            for (y in 0 until pictureHeight) {
                val pixel: Int = bitmap.getPixel(x, y)
                newBitmap.setPixel(pictureHeight-y-1, x, pixel)
            }
        }
        return newBitmap
    }

    private fun bitmapRotate (degrees: Float, bitmap: Bitmap): Bitmap? {
        val angle = (3.14 * degrees) / 180
        val pictureWidth: Int = bitmap.getWidth()
        val pictureHeight: Int = bitmap.getHeight()

        val newWidth = (pictureWidth * cos(angle) + pictureHeight * sin(angle)).toInt()
        val newHeight = (pictureHeight * cos(angle) + pictureWidth * sin(angle)).toInt()
        val newBitmap = Bitmap.createBitmap(newWidth, newHeight, bitmap.config)
        newBitmap.eraseColor(Color.WHITE)

        for (x in 0 until newWidth) {
            for (y in 0 until newHeight) {
                val xn = (x-newWidth/2)* cos(angle) - (y-newHeight/2)* sin(angle) + pictureWidth/2
                val yn = (x-newWidth/2)* sin(angle) + (y-newHeight/2)* cos(angle) + pictureHeight/2
                if (xn >= 0 && xn <= pictureWidth && yn >= 0 && yn <= pictureHeight) {
                    val pixel: Int = bitmap.getPixel(xn.toInt(), yn.toInt())
                    newBitmap.setPixel(x, y, pixel)
                }
            }
        }
        return newBitmap
    }


}