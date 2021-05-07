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
import kotlinx.android.synthetic.main.activity_main.blurBtn
import kotlinx.android.synthetic.main.activity_main.greenFilterBtn
import kotlinx.android.synthetic.main.activity_main.imageView
import kotlin.math.PI
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sqrt

class FiltersActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_filters)

        getImage()

        greenFilterBtn.setOnClickListener{
            greenFilter()
        }

        blurBtn.setOnClickListener{
            gaussianBlur()
        }

        medianBtn.setOnClickListener{
            medianFilter()
        }

        contrastBtn.setOnClickListener{
            contrastFilter()
        }

        sharpnessBtn.setOnClickListener{
            increaseSharpness()
        }
    }

    private fun getImage(){
        val selectedImageURI = intent.getStringExtra("URI")?.toUri()

        if(selectedImageURI != null){

            val source = ImageDecoder.createSource(this.contentResolver, selectedImageURI)
            val bmpImage = ImageDecoder.decodeBitmap(source).copy(Bitmap.Config.RGBA_F16, true)
            imageView.setImageBitmap(bmpImage)
        }
    }

    private fun greenFilter() {

        val imageView1: ImageView = findViewById(R.id.imageView)
        val bitmap1 = (imageView1.getDrawable() as BitmapDrawable).bitmap
        val pictureWidth: Int = bitmap1.getWidth()
        val pictureHeight: Int = bitmap1.getHeight()
        val newPicture = Bitmap.createBitmap(pictureWidth, pictureHeight, bitmap1.config)

        val valueSeekBar: SeekBar = findViewById(R.id.seekBar);
        val currentNumberOfSeekBar: Int = valueSeekBar.progress
        val percentGreen: Int = currentNumberOfSeekBar

        for (x in 0 until pictureWidth) {
            for (y in 0 until pictureHeight) {
                val pixelColor: Int = bitmap1.getPixel(x, y)
                val pixelAlpha = Color.alpha(pixelColor)
                val pixelRed: Int = Color.red(pixelColor)
                var pixelGreen: Int = Color.green(pixelColor) + percentGreen * 128 / 100
                if (pixelGreen > 255) {
                    pixelGreen = 255
                }
                val pixelBlue: Int = Color.blue(pixelColor)
                val newPixel: Int = Color.argb(pixelAlpha, pixelRed, pixelGreen, pixelBlue)
                newPicture.setPixel(x, y, newPixel)
            }
        }

        imageView1.setImageBitmap(newPicture)
    }

    private fun gaussianMatrix(sizeArr: Int, sigmaValue: Double): DoubleArray {

        fun gaussianValue(r: Int, sigma: Double): Double {
            val exponent: Double = 2.71828182845904523536
            val forExponent: Double = -1.0 * (r * r) / (2 * sigma * sigma)
            val answer = exponent.pow(forExponent) / sqrt(2 * PI * sigma * sigma)
            return answer
        }

        var gaussianDistribution = DoubleArray(sizeArr, { 0.0 })

        var sum = 0.0
        for (i in 0 until sizeArr) {
            gaussianDistribution[i] = gaussianValue(i - (sizeArr - 1) / 2, sigmaValue)
            sum += gaussianDistribution[i]
        }
        for (i in 0 until sizeArr) {
            gaussianDistribution[i] += (1 - sum) / sizeArr
        }

        return gaussianDistribution
    }

    private fun gaussianMove(x: Int,
                             y: Int,
                             gaussianDistribution: DoubleArray,
                             orientationArr: Boolean,
                             sizeArr: Int,
                             bitmap1: Bitmap,
                             width: Int,
                             height: Int): Int {

        val leftNum: Int = -(sizeArr - 1) / 2
        val rightNum: Int = (sizeArr - 1) / 2

        val currentPixel: Int = bitmap1.getPixel(x, y)

        var newPixel: Int = Color.argb(0, 0, 0, 0)
        var newPixelAlpha = Color.alpha(currentPixel)
        //println("alpha " + newPixelAlpha)
        var newPixelRed = Color.red(newPixel)
        var newPixelGreen = Color.green(newPixel)
        var newPixelBlue = Color.blue(newPixel)

        if (orientationArr) {

            for (i in leftNum..rightNum) {
                if (x + i >= 0 && x + i < width){

                    val pixelColor: Int = bitmap1.getPixel(x + i, y)
                    //val pixelAlpha = Color.alpha(pixelColor)
                    val pixelRed = Color.red(pixelColor)
                    val pixelGreen = Color.green(pixelColor)
                    val pixelBlue = Color.blue(pixelColor)

                    var newIndex: Int = i + (sizeArr - 1) / 2
                    //newPixelAlpha += (Math.round(pixelAlpha * gaussianDistribution[newIndex])).toInt()
                    //println("kjhlk" + pixelRed + " " + gaussianDistribution[newIndex])
                    newPixelRed += ((pixelRed * gaussianDistribution[newIndex]).roundToInt())
                    newPixelGreen += ((pixelGreen * gaussianDistribution[newIndex]).roundToInt())
                    newPixelBlue += ((pixelBlue * gaussianDistribution[newIndex]).roundToInt())

                    if (newPixelAlpha > 255) {
                        newPixelAlpha = 255
                    }
                    if (newPixelRed > 255) {
                        newPixelRed = 255
                    }
                    if (newPixelGreen > 255) {
                        newPixelGreen = 255
                    }
                    if (newPixelBlue > 255) {
                        newPixelBlue = 255
                    }
                }
            }

            newPixel = Color.argb(newPixelAlpha, newPixelRed, newPixelGreen, newPixelBlue)

        }
        else {
            for (i in leftNum..rightNum) {
                if (y + i >= 0 && y + i < height){

                    val pixelColor: Int = bitmap1.getPixel(x, y + i)
                    //val pixelAlpha = Color.alpha(pixelColor)
                    val pixelRed = Color.red(pixelColor)
                    val pixelGreen = Color.green(pixelColor)
                    val pixelBlue = Color.blue(pixelColor)

                    var newIndex: Int = i + (sizeArr - 1) / 2
                    //newPixelAlpha += (Math.round(pixelAlpha * gaussianDistribution[newIndex])).toInt()
                    newPixelRed += ((pixelRed * gaussianDistribution[newIndex]).roundToInt())
                    newPixelGreen += ((pixelGreen * gaussianDistribution[newIndex]).roundToInt())
                    newPixelBlue += ((pixelBlue * gaussianDistribution[newIndex]).roundToInt())

                    if (newPixelAlpha > 255) {
                        newPixelAlpha = 255
                    }
                    if (newPixelRed > 255) {
                        newPixelRed = 255
                    }
                    if (newPixelGreen > 255) {
                        newPixelGreen = 255
                    }
                    if (newPixelBlue > 255) {
                        newPixelBlue = 255
                    }
                }
            }

            newPixel = Color.argb(newPixelAlpha, newPixelRed, newPixelGreen, newPixelBlue)
        }

        return newPixel
    }

    fun makeGaussianBlur(originalPicture: Bitmap, radius: Int, sigma: Double): Bitmap {

        var gaussionDistribution = gaussianMatrix(radius, sigma)

        val pictureWidth: Int = originalPicture.getWidth()
        val pictureHeight: Int = originalPicture.getHeight()
        val newPicture = Bitmap.createBitmap(pictureWidth, pictureHeight, originalPicture.config)
        val newPicture2 = Bitmap.createBitmap(pictureWidth, pictureHeight, originalPicture.config)

        for (l in 0..1) {

            var orientationMatrix: Boolean = true
            if (l == 1){
                orientationMatrix = false
            }

            for (x in 0 until pictureWidth) {
                for (y in 0 until pictureHeight) {
                    if (orientationMatrix) {
                        var newPixel = gaussianMove(x, y, gaussionDistribution, orientationMatrix, radius, originalPicture, pictureWidth, pictureHeight)
                        newPicture.setPixel(x, y, newPixel)
                    }
                    else {
                        var newPixel = gaussianMove(x, y, gaussionDistribution, orientationMatrix, radius, newPicture, pictureWidth, pictureHeight)
                        newPicture2.setPixel(x, y, newPixel)
                    }
                }
            }
        }

        return newPicture2
    }

    private fun gaussianBlur( ) {

        val valueSeekBar: SeekBar = findViewById(R.id.seekBar);
        val currentNumberOfSeekBar: Int = valueSeekBar.progress
        val sigmaValue: Double = currentNumberOfSeekBar / 15.0
        var sizeArr: Int = (sigmaValue * 3).toInt()
        if (sizeArr % 2 == 0) {
            sizeArr += 1
        }
        if (sizeArr < 3) {
            sizeArr = 3
        }

        val imageView1: ImageView = findViewById(R.id.imageView)
        val bitmap1 = (imageView1.getDrawable() as BitmapDrawable).bitmap

        val newPicture = makeGaussianBlur(bitmap1, sizeArr, sigmaValue)

        imageView1.setImageBitmap(newPicture)
    }

    private fun getNewMedianPixel(x: Int,
                                  y: Int,
                                  sizeMedian: Int,
                                  bitmap1: Bitmap,
                                  downSide: Int,
                                  rightSide: Int): Int {

        val leftValue = -(sizeMedian - 1) / 2
        val rightValue = (sizeMedian - 1) / 2

        var alphaMedian = Array(sizeMedian * sizeMedian, { 0 })
        var redMedian = Array(sizeMedian * sizeMedian, { 0 })
        var greenMedian = Array(sizeMedian * sizeMedian, { 0 })
        var blueMedian = Array(sizeMedian * sizeMedian, { 0 })

        var num = 0
        for (i in leftValue..rightValue) {
            for (j in leftValue..rightValue) {
                if (i + x >= 0 && i + x < rightSide && i + y >= 0 && i + y < downSide) {
                    var currentPixel = bitmap1.getPixel(x + i, y + i)
                    var currentPixelAlpha: Int = Color.alpha(currentPixel)
                    var currentPixelRed: Int = Color.red(currentPixel)
                    var currentPixelGreen: Int = Color.green(currentPixel)
                    var currentPixelBlue: Int = Color.blue(currentPixel)

                    alphaMedian[num] = currentPixelAlpha
                    redMedian[num] = currentPixelRed
                    greenMedian[num] = currentPixelGreen
                    blueMedian[num] = currentPixelBlue
                }
                num += 1
            }
        }

        alphaMedian.sort()
        redMedian.sort()
        greenMedian.sort()
        blueMedian.sort()

        val medianNumber: Int = (sizeMedian * sizeMedian - 1) / 2

        val pixelAlpha: Int = alphaMedian[medianNumber]
        val pixelRed: Int = redMedian[medianNumber]
        val pixelGreen: Int = greenMedian[medianNumber]
        val pixelBlue: Int = blueMedian[medianNumber]

        val pixelColor: Int = Color.argb(pixelAlpha, pixelRed, pixelGreen, pixelBlue)

        return pixelColor
    }

    private fun medianFilter() {
        val imageView1: ImageView = findViewById(R.id.imageView)
        val bitmap1 = (imageView1.getDrawable() as BitmapDrawable).bitmap
        val pictureWidth: Int = bitmap1.getWidth()
        val pictureHeight: Int = bitmap1.getHeight()
        val newPicture = bitmap1

        var sizeArr = 3

        for (x in 0 until pictureWidth) {
            //if (x % 2 == 1) {
                for (y in 0 until pictureHeight) {
                    val newPixel = getNewMedianPixel(x, y, sizeArr, bitmap1, pictureHeight, pictureWidth)
                    newPicture.setPixel(x, y, newPixel)
                }
            //}
            //else {
                //for (y in (pictureHeight)..0) {
                    //val newPixel = getNewMedianPixel(x, y, sizeArr, newPicture, pictureHeight, pictureWidth)
                    //newPicture.setPixel(x, y, newPixel)
                //}
            //}
        }

        imageView1.setImageBitmap(newPicture)
    }

    private fun getCommonContrast(bitmap1: Bitmap): LongArray {

        val commonPixelColor = LongArray(4, { 0 })

        val pictureWidth: Int = bitmap1.getWidth()
        val pictureHeight: Int = bitmap1.getHeight()

        for (x in 0 until pictureWidth) {
            for (y in 0 until pictureHeight) {
                val pixelColor: Int = bitmap1.getPixel(x, y)
                val pixelAlpha: Int = Color.alpha(pixelColor)
                val pixelRed: Int = Color.red(pixelColor)
                val pixelGreen: Int = Color.green(pixelColor)
                val pixelBlue: Int = Color.blue(pixelColor)

                commonPixelColor[0] += pixelAlpha.toLong()
                commonPixelColor[1] += pixelRed.toLong()
                commonPixelColor[2] += pixelGreen.toLong()
                commonPixelColor[3] += pixelBlue.toLong()
            }
        }

        return commonPixelColor
    }

    private fun contrastFilter() {
        val imageView1: ImageView = findViewById(R.id.imageView)
        val bitmap1 = (imageView1.getDrawable() as BitmapDrawable).bitmap
        val pictureWidth: Int = bitmap1.getWidth()
        val pictureHeight: Int = bitmap1.getHeight()
        val newPicture = Bitmap.createBitmap(pictureWidth, pictureHeight, bitmap1.config)

        val valueSeekBar: SeekBar = findViewById(R.id.seekBar);
        val currentNumberOfSeekBar: Int = valueSeekBar.progress
        val contrastValue: Double = currentNumberOfSeekBar / 50.0;

        val commonPixelColor = getCommonContrast(bitmap1)
        for (i in 0..3) {
            commonPixelColor[i] = (commonPixelColor[i] / (pictureHeight * pictureWidth)).toLong()
        }

        for (x in 0 until pictureWidth) {
            for (y in 0 until pictureHeight) {
                val pixelColor: Int = bitmap1.getPixel(x, y)
                var pixelAlpha: Int = (contrastValue * (Color.alpha(pixelColor) - commonPixelColor[0]) + commonPixelColor[0]).toInt()
                if (pixelAlpha > 255) {
                    pixelAlpha = 255
                } else {
                    if (pixelAlpha < 0) {
                        pixelAlpha = 0
                    }
                }

                var pixelRed: Int = (contrastValue * (Color.red(pixelColor) - commonPixelColor[1]) + commonPixelColor[1]).toInt()
                if (pixelRed > 255) {
                    pixelRed = 255
                } else {
                    if (pixelRed < 0) {
                        pixelRed = 0
                    }
                }

                var pixelGreen: Int = (contrastValue * (Color.green(pixelColor) - commonPixelColor[2]) + commonPixelColor[2]).toInt()
                if (pixelGreen > 255) {
                    pixelGreen = 255
                } else {
                    if (pixelGreen < 0) {
                        pixelGreen = 0
                    }
                }

                var pixelBlue: Int = (contrastValue * (Color.blue(pixelColor) - commonPixelColor[3]) + commonPixelColor[3]).toInt()
                if (pixelBlue > 255) {
                    pixelBlue = 255
                } else {
                    if (pixelBlue < 0) {
                        pixelBlue = 0
                    }
                }

                val newPixel: Int = Color.argb(pixelAlpha, pixelRed, pixelGreen, pixelBlue)
                newPicture.setPixel(x, y, newPixel)
            }
        }

        imageView1.setImageBitmap(newPicture)
    }

    private fun increaseSharpness() {

        val foldingMatrix = Array(3, { Array(3, {0}) })
        foldingMatrix[0] = arrayOf(-1, -1, -1)
        foldingMatrix[1] = arrayOf(-1, 9, -1)
        foldingMatrix[2] = arrayOf(-1, -1, -1)

        val imageView1: ImageView = findViewById(R.id.imageView)
        val bitmap1 = (imageView1.getDrawable() as BitmapDrawable).bitmap
        val pictureWidth: Int = bitmap1.getWidth()
        val pictureHeight: Int = bitmap1.getHeight()
        val newPicture = Bitmap.createBitmap(pictureWidth, pictureHeight, bitmap1.config)

        for (i in 1 until pictureWidth - 1) {
            for (j in 1 until pictureHeight - 1) {

                val pixelColor: Int = bitmap1.getPixel(i, j)
                var pixelAlpha: Int = 0
                var pixelRed: Int = 0
                var pixelGreen: Int = 0
                var pixelBlue: Int = 0

                for (k in (i - 1)..(i + 1)) {
                    for (l in (j - 1)..(j + 1)) {

                        val currentPixelColor: Int = bitmap1.getPixel(k, l)
                        val currentPixelAlpha: Int = Color.alpha(currentPixelColor)
                        val currentPixelRed: Int = Color.red(currentPixelColor)
                        val currentPixelGreen: Int = Color.green(currentPixelColor)
                        val currentPixelBlue: Int = Color.blue(currentPixelColor)

                        pixelAlpha += foldingMatrix[k - i + 1][l - j + 1] * currentPixelAlpha
                        pixelRed += foldingMatrix[k - i + 1][l - j + 1] * currentPixelRed
                        pixelGreen += foldingMatrix[k - i + 1][l - j + 1] * currentPixelGreen
                        pixelBlue += foldingMatrix[k - i + 1][l - j + 1] * currentPixelBlue
                    }
                }

                if (pixelAlpha > 255) {
                    pixelAlpha = 255
                } else {
                    if (pixelAlpha < 0) {
                        pixelAlpha = 0
                    }
                }
                if (pixelRed > 255) {
                    pixelRed = 255
                } else {
                    if (pixelRed < 0) {
                        pixelRed = 0
                    }
                }
                if (pixelGreen > 255) {
                    pixelGreen = 255
                } else {
                    if (pixelGreen < 0) {
                        pixelGreen = 0
                    }
                }
                if (pixelBlue > 255) {
                    pixelBlue = 255
                } else {
                    if (pixelBlue < 0) {
                        pixelBlue = 0
                    }
                }

                val newPixel: Int = Color.argb(pixelAlpha, pixelRed, pixelGreen, pixelBlue)
                newPicture.setPixel(i, j, newPixel)

            }
        }

        imageView1.setImageBitmap(newPicture)
    }
}