package com.example.team13

import android.graphics.Bitmap
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.graphics.drawable.toBitmap
import kotlinx.android.synthetic.main.scale_activity.*

class ScaleActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.scale_activity)

        getImage()

        actionBtn.setOnClickListener {
           interpolation()
        }
    }

    private fun getImage(){
        val selectedImage = intent.getParcelableExtra<Bitmap>("BitmapImage")
        imageView.setImageBitmap(selectedImage)
    }

    private fun interpolation(){

        val bmpImage = imageView.drawable.toBitmap()
        val scale = scaleEditText.text.toString().toDouble()


        val oldw = bmpImage.width
        val oldh = bmpImage.height
        val neww = (bmpImage.width * scale).toInt()
        val newh = (bmpImage.height * scale).toInt()

        val newBmp = Bitmap.createBitmap(neww, newh, Bitmap.Config.RGBA_F16)

        for(x in 0 until neww){
            for(y in 0 until newh){

                val gx = x.toFloat() / neww * (oldw - 1)
                val gy = y.toFloat() / newh * (oldh - 1)

                val gxi = gx.toInt()
                val gyi = gy.toInt()

                val c00  = bmpImage.getColor(gxi, gyi)
                val c10 = bmpImage.getColor(gxi + 1,gyi)
                val c01 = bmpImage.getColor(gxi,gyi + 1)
                val c11 = bmpImage.getColor(gxi + 1,gyi + 1)

                val red = Blerp(c00.red(), c10.red(), c01.red(), c11.red(), gx - gxi, gy- gyi)
                val green = Blerp(c00.green(), c10.green(), c01.green(), c11.green(), gx - gxi, gy- gyi)
                val blue = Blerp(c00.blue(), c10.blue(), c01.blue(), c11.blue(), gx - gxi, gy- gyi)

                newBmp.setPixel(x,y, Color.rgb(red,green,blue))
            }
        }

        imageView.setImageBitmap(newBmp)
    }

    private fun Lerp (s : Float, e : Float, t : Float): Float {
        return s + (e - s) * t
    }

    private fun Blerp (c00 : Float, c10 : Float, c01 : Float, c11 : Float, tx : Float, ty : Float): Float {
        return Lerp(Lerp(c00,c10,tx), Lerp(c01,c11,tx),ty)
    }
}