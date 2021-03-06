package com.example.team13

import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.view.MotionEvent
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import kotlinx.android.synthetic.main.activity_linearfiltering.*
import kotlinx.android.synthetic.main.activity_main.imageView
import kotlin.math.*


class LinearFilteringActivity: AppCompatActivity(){

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_linearfiltering)

        getImage()

        startThreePointsBtn.setOnClickListener{
            getFirstThreePoints()
        }

        finishThreePointsBtn.setOnClickListener{
            getSecondThreePoints()
        }

        bilinearBtn.setOnClickListener{
            bilinearFiltering()
        }

        affineBtn.setOnClickListener{
            affineFiltering()
        }

        trilinearBtn.setOnClickListener{
            trilinearFiltering()
        }
    }

    private var pointsCoordinates: Array<Array<Double>> = Array(2) { Array(6) { 0.0 } }
    private var stepNum = 0
    private var reversePointsCoordinates: Array<Array<Double>> = Array(2) { Array(6) { 0.0 } }
    private lateinit var firstCanvas: Canvas

    private fun getImage(){
        val selectedImageURI = intent.getStringExtra("ImageUri")!!.toUri()
        val source = ImageDecoder.createSource(this.contentResolver, selectedImageURI)
        val bmpImage = ImageDecoder.decodeBitmap(source).copy(Bitmap.Config.RGBA_F16, true)

        imageView.setImageBitmap(bmpImage)
    }

    private fun getDeterminantSize3(matrix: Array<Array<Double>>): Double {
        val sum1 = matrix[0][0] * matrix[1][1] * matrix[2][2]
        val sum2 = matrix[1][0] * matrix[0][2] * matrix[2][1]
        val sum3 = matrix[0][1] * matrix[1][2] * matrix[2][0]
        val sum4 = -1 * matrix[0][2] * matrix[1][1] * matrix[2][0]
        val sum5 = -1 * matrix[0][1] * matrix[1][0] * matrix[2][2]
        val sum6 = -1 * matrix[0][0] * matrix[1][2] * matrix[2][1]

        return sum1 + sum2 + sum3 + sum4 + sum5 + sum6
    }

    private fun getDeterminantSize2(matrix: Array<Array<Double>>): Double {
        val sum1 = matrix[0][0] * matrix[1][1]
        val sum2 = -1 * matrix[1][0] * matrix[0][1]

        return sum1 + sum2
    }

    private fun getTransposedMatrix(matrix: Array<Array<Double>>): Array<Array<Double>> {
        val transposedMatrix: Array<Array<Double>> = Array(3) { Array(3) { 0.0 } }

        for (i in 0..2) {
            for (j in 0..2) {
                transposedMatrix[j][i] = matrix[i][j]
            }
        }

        return transposedMatrix
    }

    private fun getMatrixAlgebraicComplements(matrix: Array<Array<Double>>): Array<Array<Double>> {

        val matrixOfMinors: Array<Array<Double>> = Array(3) { Array(3) { 0.0 } }

        for (i in 0..2) {
            for (j in 0..2) {

                val oneMinor: Array<Array<Double>> = Array(2) { Array(2) { 0.0 } }
                var positionForNewNumber = 0
                for (x in 0..2) {
                    for (y in 0..2) {
                        if (x != i && y != j) {
                            oneMinor[positionForNewNumber / 2][positionForNewNumber % 2] = matrix[x][y]
                            positionForNewNumber += 1
                        }
                    }
                }
                matrixOfMinors[i][j] = getDeterminantSize2(oneMinor)
            }
        }

        matrixOfMinors[0][1] = -1 * matrixOfMinors[0][1]
        matrixOfMinors[1][0] = -1 * matrixOfMinors[1][0]
        matrixOfMinors[1][2] = -1 * matrixOfMinors[1][2]
        matrixOfMinors[2][1] = -1 * matrixOfMinors[2][1]

        return matrixOfMinors
    }

    private fun getReverseOfMatrix(matrix: Array<Array<Double>>): Array<Array<Double>> {

        val matrixOfMinors = getMatrixAlgebraicComplements(matrix)
        val reverseMatrix = getTransposedMatrix(matrixOfMinors)
        val determinantOfCurrentMatrix = getDeterminantSize3(matrix)

        for (i in 0..2) {
            for (j in 0..2) {
                reverseMatrix[i][j] = reverseMatrix[i][j] / determinantOfCurrentMatrix
            }
        }

        return reverseMatrix
    }

    private fun matrixMultiplication(firstMatrix: Array<Array<Double>>, secondMatrix: Array<Double>): Array<Double> {
        val resultMatrix: Array<Double> = Array(3) { 0.0 }

        for (i in 0..2) {
            var sum = 0.0
            for (j in 0..2) {
                sum += firstMatrix[i][j] * secondMatrix[j]
            }
            resultMatrix[i] = sum
        }

        return resultMatrix
    }

    private fun getAffineTransformationMatrix(coordinates: Array<Array<Double>>): Array<Array<Double>> {
        val affineTransformationMatrix: Array<Array<Double>> = Array(3) { Array(3) { 0.0 } }
        affineTransformationMatrix[2][0] = 0.0
        affineTransformationMatrix[2][1] = 0.0
        affineTransformationMatrix[2][2] = 1.0

        val matrixFirstComponents: Array<Array<Double>> = Array(3) { Array(3) { 0.0 } }
        matrixFirstComponents[0] = arrayOf(1.0, coordinates[0][0], coordinates[1][0])
        matrixFirstComponents[1] = arrayOf(1.0, coordinates[0][1], coordinates[1][1])
        matrixFirstComponents[2] = arrayOf(1.0, coordinates[0][2], coordinates[1][2])

        val matrixSecondComponentX = arrayOf(
            coordinates[0][3],
            coordinates[0][4],
            coordinates[0][5]
        )
        val matrixSecondComponentY = arrayOf(
            coordinates[1][3],
            coordinates[1][4],
            coordinates[1][5]
        )
        val matrixComponentsX = matrixMultiplication(
            getReverseOfMatrix(matrixFirstComponents),
            matrixSecondComponentX
        )
        val matrixComponentsY = matrixMultiplication(
            getReverseOfMatrix(matrixFirstComponents),
            matrixSecondComponentY
        )

        for (i in 0..2) {
            affineTransformationMatrix[0][i] = matrixComponentsX[i]
            affineTransformationMatrix[1][i] = matrixComponentsY[i]
        }
        return affineTransformationMatrix
    }

    private fun getInterpolation(
        firstI: Int,
        secondI: Int,
        thirdI: Int,
        fourthI: Int,
        ceilX: Int,
        floorX: Int,
        ceilY: Int,
        floorY: Int,
        x: Double,
        y: Double
    ): Int {
        return ((firstI * (ceilX - x) + secondI * (x - floorX)) * (ceilY - y) +
                (thirdI * (ceilX - x) + fourthI * (x - floorX)) * (y - floorY)).toInt()
    }

    private fun getNewBilinearPixel(
        affineTransformation: Array<Array<Double>>,
        x: Int,
        y: Int,
        currentBitmap: Bitmap
    ): Int {

        val x1 =
            affineTransformation[0][0] + x * affineTransformation[0][1] + y * affineTransformation[0][2]
        val y1 =
            affineTransformation[1][0] + x * affineTransformation[1][1] + y * affineTransformation[1][2]
        val coordinates: Array<Array<Int>> = Array(2) { Array(4) { 0 } }
        coordinates[0] = arrayOf(
            floor(x1).toInt(), ceil(x1).toInt(), floor(x1).toInt(), ceil(x1).toInt())
        coordinates[1] = arrayOf(
            floor(y1).toInt(), floor(y1).toInt(), ceil(y1).toInt(), ceil(y1).toInt())
        val pictureWidth: Int = currentBitmap.width
        val pictureHeight: Int = currentBitmap.height

        for (i in 0..3) {
            coordinates[0][i] = normalizeValue(0, pictureWidth - 1, coordinates[0][i])
            coordinates[1][i] = normalizeValue(0, pictureHeight - 1, coordinates[1][i])
        }

        val firstPixel: Int = currentBitmap.getPixel(coordinates[0][0], coordinates[1][0])
        val secondPixel: Int = currentBitmap.getPixel(coordinates[0][1], coordinates[1][1])
        val thirdPixel: Int = currentBitmap.getPixel(coordinates[0][2], coordinates[1][2])
        val fourthPixel: Int = currentBitmap.getPixel(coordinates[0][3], coordinates[1][3])

        return makeInterpolation(coordinates, firstPixel, secondPixel, thirdPixel, fourthPixel, x1, y1)
    }

    private fun makeInterpolation(
        coordinates: Array<Array<Int>>,
        firstPixel: Int,
        secondPixel: Int,
        thirdPixel: Int,
        fourthPixel: Int,
        x1: Double,
        y1: Double): Int {

        val newAlpha = getInterpolation(
            Color.alpha(firstPixel), Color.alpha(secondPixel), Color.alpha(
                thirdPixel
            ), Color.alpha(fourthPixel),
            coordinates[0][1], coordinates[0][0], coordinates[1][2], coordinates[1][0], x1, y1
        )
        val newRed = getInterpolation(
            Color.red(firstPixel), Color.red(secondPixel), Color.red(thirdPixel), Color.red(
                fourthPixel
            ),
            coordinates[0][1], coordinates[0][0], coordinates[1][2], coordinates[1][0], x1, y1
        )
        val newGreen = getInterpolation(
            Color.green(firstPixel), Color.green(secondPixel), Color.green(thirdPixel), Color.green(
                fourthPixel
            ),
            coordinates[0][1], coordinates[0][0], coordinates[1][2], coordinates[1][0], x1, y1
        )
        val newBlue = getInterpolation(
            Color.blue(firstPixel), Color.blue(secondPixel), Color.blue(thirdPixel), Color.blue(
                fourthPixel
            ),
            coordinates[0][1], coordinates[0][0], coordinates[1][2], coordinates[1][0], x1, y1
        )

        return Color.argb(newAlpha, newRed, newGreen, newBlue)
    }

    private fun getImageWithDecrease(widthBitmap: Int, heightBitmap: Int, originalBitmap: Bitmap): Bitmap {
        var x = widthBitmap
        var y = heightBitmap
        if (widthBitmap % 2 == 1 && widthBitmap > 1) x = widthBitmap - 1
        if (heightBitmap % 2 == 1 && heightBitmap > 1) y = heightBitmap - 1
        val newBitmap = Bitmap.createBitmap(x / 2, y / 2, Bitmap.Config.ARGB_8888)

        for (i in 0 until x / 2) {
            for (j in 0 until y / 2) {
                val colors = arrayOf(0, 0, 0, 0)
                for (k in 0..1) {
                    for (l in 0..1) {
                        val pixel = originalBitmap.getPixel(i * 2 + k, j * 2 + l)
                        val currentColors = arrayOf(
                            Color.alpha(pixel), Color.red(pixel), Color.green(
                                pixel
                            ), Color.blue(pixel)
                        )

                        for (q in 0..3) {
                            colors[q] = colors[q] + currentColors[q]
                        }
                    }
                }
                val newPixel = Color.argb(
                    colors[0] / 4,
                    colors[1] / 4,
                    colors[2] / 4,
                    colors[3] / 4
                )
                newBitmap.setPixel(i, j, newPixel)
            }
        }
        return newBitmap
    }

    private fun getMipMapping(bitmapStart: Bitmap): Array<Bitmap> {

        var imageSizes: Array<Bitmap> = Array(1) { bitmapStart }

        var sizeWidth = bitmapStart.width
        var sizeHeight = bitmapStart.height

        var i = 0
        while (min(sizeWidth / 2, sizeHeight / 2) > 0) {
            imageSizes += getImageWithDecrease(sizeWidth, sizeHeight, imageSizes[i])
            sizeWidth /= 2
            sizeHeight /= 2
            i += 1
        }

        return imageSizes
    }

    private fun getKDecrease(
        affineTransformation: Array<Array<Double>>,
        x: Int,
        y: Int,
        currentBitmap: Bitmap
    ): Int {

        val pictureWidth: Int = currentBitmap.width
        val pictureHeight: Int = currentBitmap.height
        val x1 = affineTransformation[0][0] + x * affineTransformation[0][1] + y * affineTransformation[0][2]
        val y1 = affineTransformation[1][0] + x * affineTransformation[1][1] + y * affineTransformation[1][2]

        val newX: Int = if (x + 1 >= pictureWidth) x - 1
        else x + 1
        val newY: Int = if (y + 1 >= pictureHeight) y - 1
        else y + 1

        val x2 = affineTransformation[0][0] + newX * affineTransformation[0][1] + newY * affineTransformation[0][2]
        val y2 = affineTransformation[1][0] + newX * affineTransformation[1][1] + newY * affineTransformation[1][2]

        val kx = abs(x1 - x2)
        val ky = abs(y1 - y2)

        return ((kx + ky) / 2).roundToInt()
    }

    private fun getNewPixel(
        affineTransformation: Array<Array<Double>>,
        x: Int,
        y: Int,
        currentBitmap: Bitmap
    ): Int {

        val x1 = affineTransformation[0][0] + x * affineTransformation[0][1] + y * affineTransformation[0][2]
        val y1 = affineTransformation[1][0] + x * affineTransformation[1][1] + y * affineTransformation[1][2]
        var coordinatesX = (x1.roundToInt())
        var coordinatesY = (y1.roundToInt())
        val pictureWidth: Int = currentBitmap.width
        val pictureHeight: Int = currentBitmap.height

        if (coordinatesX < 0) coordinatesX = 0
        else {
            if (coordinatesX >= pictureWidth) coordinatesX = pictureWidth - 1
        }

        if (coordinatesY < 0) coordinatesY = 0
        else {
            if (coordinatesY >= pictureHeight) coordinatesY = pictureHeight - 1
        }

        val pixel = currentBitmap.getPixel(coordinatesX, coordinatesY)
        val newAlpha = Color.alpha(pixel)
        val newRed = Color.red(pixel)
        val newGreen = Color.green(pixel)
        val newBlue = Color.blue(pixel)

        return Color.argb(newAlpha, newRed, newGreen, newBlue)
    }

    private fun bilinearFiltering() {
        reversePointsCoordinates = reverseCoordinates(pointsCoordinates)
        val affineTransformation = getAffineTransformationMatrix(reversePointsCoordinates)

        val imageViewStart: ImageView = findViewById(R.id.imageView)
        val bitmapStart = (imageViewStart.drawable as BitmapDrawable).bitmap
        val pictureWidth: Int = bitmapStart.width
        val pictureHeight: Int = bitmapStart.height
        val newPicture = Bitmap.createBitmap(pictureWidth, pictureHeight, bitmapStart.config)

        for (i in 0 until pictureWidth) {
            for (j in 0  until pictureHeight) {
                val newPixel = getNewBilinearPixel(affineTransformation, i, j, bitmapStart)
                newPicture.setPixel(i, j, newPixel)
            }
        }

        imageView.setImageBitmap(newPicture)
        stepNum = 0
    }

    private fun affineFiltering() {
        getImage()
        val affineTransformation = getAffineTransformationMatrix(pointsCoordinates)

        val imageViewStart: ImageView = findViewById(R.id.imageView)
        val bitmapStart = (imageViewStart.drawable as BitmapDrawable).bitmap
        val pictureWidth: Int = bitmapStart.width
        val pictureHeight: Int = bitmapStart.height
        val newPicture = Bitmap.createBitmap(pictureWidth, pictureHeight, bitmapStart.config)

        for (i in 0 until pictureWidth) {
            for (j in 0 until pictureHeight) {
                val newPixel = getNewPixel(affineTransformation, i, j, bitmapStart)
                newPicture.setPixel(i, j, newPixel)
            }
        }

        imageView.setImageBitmap(newPicture)
        stepNum = 0
    }

    private fun getTrilinearInterpolation(firstPixel: Int, secondPixel: Int, k: Int, m: Int): Int {

        val newPixelAlpha = (Color.alpha(firstPixel) * (2 * m - k) + Color.alpha(secondPixel) * (k - m)) / m
        val newPixelRed = (Color.red(firstPixel) * (2 * m - k) + Color.red(secondPixel) * (k - m)) / m
        val newPixelGreen = (Color.green(firstPixel) * (2 * m - k) + Color.green(secondPixel) * (k - m)) / m
        val newPixelBlue = (Color.blue(firstPixel) * (2 * m - k) + Color.blue(secondPixel) * (k - m)) / m

        return Color.argb(newPixelAlpha, newPixelRed, newPixelGreen, newPixelBlue)
    }

    private fun getNewTrilinearPixel(
        x: Int,
        y: Int,
        affineTransformation: Array<Array<Double>>,
        pictureWidth: Int,
        pictureHeight: Int
    ): Array<Int> {
        val coordinates: Array<Int> = arrayOf(0, 0)

        var x1 = (affineTransformation[0][0] + x * affineTransformation[0][1] + y * affineTransformation[0][2]).roundToInt()
        var y1 = (affineTransformation[1][0] + x * affineTransformation[1][1] + y * affineTransformation[1][2]).roundToInt()

        x1 = normalizeValue(0, pictureWidth - 1, x1)
        y1 = normalizeValue(0, pictureHeight - 1, y1)

        coordinates[0] = x1
        coordinates[1] = y1

        return coordinates
    }

    private fun normalizeValue(min: Int, max: Int, value: Int): Int {
        var newValue = value

        if (value > max) {
            newValue = max
        }
        else {
            if (value < min) {
                newValue = min
            }
        }

        return newValue
    }

    private fun getTwoLayersPixels(decrease: Int, x: Int, y: Int, pixels: Array<Bitmap>): Array<Int> {
        val twoPixels: Array<Int> = arrayOf(0, 0, 0)
        var degree = 1
        var valueOfdegree = 0

        while (decrease > degree) {
            degree *= 2
            valueOfdegree += 1
        }

        val degree2: Int
        if (valueOfdegree == 0) {
            valueOfdegree += 1
            degree2 = degree
            degree *= 2
        }
        else {
            degree2 = degree / 2
        }

        val firstLayerX = normalizeValue(0, pixels[valueOfdegree].width - 1, x / degree)
        val firstLayerY = normalizeValue(0, pixels[valueOfdegree].height - 1, y / degree)
        val secondLayerX = normalizeValue(0, pixels[valueOfdegree - 1].width - 1, x / degree2)
        val secondLayerY = normalizeValue(0, pixels[valueOfdegree - 1].height - 1, y / degree2)

        twoPixels[0] = pixels[valueOfdegree].getPixel(firstLayerX, firstLayerY)
        twoPixels[1] = pixels[valueOfdegree - 1].getPixel(secondLayerX, secondLayerY)
        twoPixels[2] = degree2

        return twoPixels
    }

    private fun printAllPictures(pictures: Array<Bitmap>) {
        val imageViewStart: ImageView = findViewById(R.id.imageView)
        val bitmapStart = (imageViewStart.drawable as BitmapDrawable).bitmap
        val pictureWidth: Int = bitmapStart.width
        val pictureHeight: Int = bitmapStart.height
        val newPicture = Bitmap.createBitmap(pictureWidth, pictureHeight, bitmapStart.config)

        var sumWidths = 0
        val numPictures = pictures.size

        for (i in 1 until numPictures) {
            val currentWidth: Int = pictures[i].width
            val currentHeight: Int = pictures[i].height

            for (j in 0 until currentWidth) {
                for (k in 0 until currentHeight) {
                    val currentPixel = pictures[i].getPixel(j, k)
                    newPicture.setPixel(sumWidths + j, k, currentPixel)
                }
            }
            sumWidths += (currentWidth - 1)
        }
        imageView.setImageBitmap(newPicture)
    }

    private fun trilinearFiltering() {
        getImage()
        reversePointsCoordinates = reverseCoordinates(pointsCoordinates)
        val affineTransformation = getAffineTransformationMatrix(reversePointsCoordinates)

        val imageViewStart: ImageView = findViewById(R.id.imageView)
        val bitmapStart = (imageViewStart.drawable as BitmapDrawable).bitmap
        val pictureWidth: Int = bitmapStart.width
        val pictureHeight: Int = bitmapStart.height
        val newPicture = Bitmap.createBitmap(pictureWidth, pictureHeight, bitmapStart.config)
        val imageSizes = getMipMapping(bitmapStart)

        for (i in 0 until pictureWidth) {
            for (j in 0 until pictureHeight) {
                val coordinates = getNewTrilinearPixel(
                    i,
                    j,
                    affineTransformation,
                    pictureWidth,
                    pictureHeight
                )
                val x = coordinates[0]
                val y = coordinates[1]
                val decrease = getKDecrease(affineTransformation, i, j, bitmapStart)
                val pixels = getTwoLayersPixels(decrease, x, y, imageSizes)
                val newPixel = getTrilinearInterpolation(
                    pixels[0],
                    pixels[1],
                    decrease,
                    pixels[2] + 1
                )
                newPicture.setPixel(i, j, newPixel)
            }
        }
        imageView.setImageBitmap(newPicture)
        stepNum = 0
    }

    private fun touchUp(): Boolean {
        return true
    }

    private fun reverseCoordinates(coordinates: Array<Array<Double>>): Array<Array<Double>> {
        val reverseCoord: Array<Array<Double>> = Array(2) { Array(6) { 0.0 } }

        for (i in 0..5) {
            reverseCoord[0][i] = coordinates[0][(i + 3) % 6]
            reverseCoord[1][i] = coordinates[1][(i + 3) % 6]
        }

        return reverseCoord
    }

    private fun newCoordinatesFirst(x: Double, y: Double, firstCanvas: Canvas) {
        if (stepNum < 3 && x >= 0 && y >= 0) {
            pointsCoordinates[0][stepNum] = x
            pointsCoordinates[1][stepNum] = y

            val paintRed = Paint()
            paintRed.color = Color.RED
            paintRed.style = Paint.Style.FILL

            val paintGreen = Paint()
            paintGreen.color = Color.GREEN
            paintGreen.style = Paint.Style.FILL

            val paintBlue = Paint()
            paintBlue.color = Color.BLUE
            paintBlue.style = Paint.Style.FILL

            when (stepNum) {
                0 -> firstCanvas.drawCircle(x.toFloat(), y.toFloat(), 25.0F, paintRed)
                1 -> firstCanvas.drawCircle(x.toFloat(), y.toFloat(), 25.0F, paintGreen)
                2 -> firstCanvas.drawCircle(x.toFloat(), y.toFloat(), 25.0F, paintBlue)
            }
            stepNum += 1
        }
    }

    private fun newCoordinatesSecond(x: Double, y: Double, firstCanvas: Canvas) {
        if (stepNum in 3..5 && x >= 0 && y >= 0) {
            pointsCoordinates[0][stepNum] = x
            pointsCoordinates[1][stepNum] = y

            val paintRed = Paint()
            paintRed.color = Color.RED
            paintRed.style = Paint.Style.FILL

            val paintGreen = Paint()
            paintGreen.color = Color.GREEN
            paintGreen.style = Paint.Style.FILL

            val paintBlue = Paint()
            paintBlue.color = Color.BLUE
            paintBlue.style = Paint.Style.FILL

            when (stepNum) {
                3 -> firstCanvas.drawCircle(x.toFloat(), y.toFloat(), 25.0F, paintRed)
                4 -> firstCanvas.drawCircle(x.toFloat(), y.toFloat(), 25.0F, paintGreen)
                5 -> firstCanvas.drawCircle(x.toFloat(), y.toFloat(), 25.0F, paintBlue)
            }
            stepNum += 1
        }
    }

    fun newCoordinates(x: Double, y: Double, firstCanvas: Canvas): Boolean {
        if (stepNum < 3 && x >= 0 && y >= 0) {
            newCoordinatesFirst(x, y, firstCanvas)
        }
        else {
            newCoordinatesSecond(x, y, firstCanvas)
        }
        return true
    }


    private fun getFirstThreePoints() {
        val imageViewStart: ImageView = findViewById(R.id.imageView)
        val bitmapStart = (imageViewStart.getDrawable() as BitmapDrawable).bitmap
        firstCanvas = Canvas(bitmapStart)

        imageViewStart.setOnTouchListener { arg0, event ->
            var inverse = Matrix()
            imageViewStart.getImageMatrix().invert(inverse)
            val pts = floatArrayOf(event.x, event.y)
            inverse.mapPoints(pts)
            var x = pts[0].toDouble()
            var y = pts[1].toDouble()
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    newCoordinates(x, y, firstCanvas)
                    arg0.postInvalidate()
                    arg0.performClick()
                }
                MotionEvent.ACTION_UP -> arg0.performClick()
                else -> touchUp()
            }
        }
    }

    private fun getSecondThreePoints() {
        getImage()
        val imageViewStart: ImageView = findViewById(R.id.imageView)
        val bitmapStart = (imageViewStart.drawable as BitmapDrawable).bitmap
        firstCanvas = Canvas(bitmapStart)
    }
}