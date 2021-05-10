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
import kotlinx.android.synthetic.main.activity_linearfiltering.*
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

        startThreePointsBtn.setOnClickListener{
            getFirstThreePoints()
        }

        finishThreePointsBtn.setOnClickListener{
            getSecondThreePoints()
        }

        bilinearBtn.setOnClickListener{
            bilinearFiltering()
        }

    }

    private var pointsCoordinates: Array<Array<Double>> = Array(2, { Array(6, {0.0}) })

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
        var transposedMatrix: Array<Array<Double>> = Array(3, { Array(3, {0.0}) })

        for (i in 0..2) {
            for (j in 0..2) {
                transposedMatrix[j][i] = matrix[i][j]
            }
        }

        return transposedMatrix
    }

    private fun getMatrixAlgebraicComplements(matrix: Array<Array<Double>>): Array<Array<Double>> {

        println("OK8 " + matrix[0][0] + " " + matrix[0][1] + " " + matrix[0][2])
        println("OK8 " + matrix[1][0] + " " + matrix[1][1] + " " + matrix[1][2])
        println("OK8 " + matrix[2][0] + " " + matrix[2][1] + " " + matrix[2][2])
        var matrixOfMinors: Array<Array<Double>> = Array(3, { Array(3, {0.0}) })

        for (i in 0..2) {
            for (j in 0..2) {

                var oneMinor: Array<Array<Double>> = Array(2, { Array(2, {0.0}) })
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
                println("OK7.0 " + matrixOfMinors[i][j])
                println("OK7 " + oneMinor[0][0] + " " + oneMinor[0][1])
                println("OK7 " + oneMinor[1][0] + " " + oneMinor[1][1])
            }
        }

        matrixOfMinors[0][1] = -1 * matrixOfMinors[0][1]
        matrixOfMinors[1][0] = -1 * matrixOfMinors[1][0]
        matrixOfMinors[1][2] = -1 * matrixOfMinors[1][2]
        matrixOfMinors[2][1] = -1 * matrixOfMinors[2][1]

        return matrixOfMinors
    }

    private fun getReverseOfMatrix(matrix: Array<Array<Double>>): Array<Array<Double>> {

        println("OK6 " + matrix[0][0] + " " + matrix[0][1] + " " + matrix[0][2])
        println("OK6 " + matrix[1][0] + " " + matrix[1][1] + " " + matrix[1][2])
        println("OK6 " + matrix[2][0] + " " + matrix[2][1] + " " + matrix[2][2])
        var matrixOfMinors = getMatrixAlgebraicComplements(matrix)
        println("OK4 " + matrixOfMinors[0][0] + " " + matrixOfMinors[0][1] + " " + matrixOfMinors[0][2])
        println("OK4 " + matrixOfMinors[1][0] + " " + matrixOfMinors[1][1] + " " + matrixOfMinors[1][2])
        println("OK4 " + matrixOfMinors[2][0] + " " + matrixOfMinors[2][1] + " " + matrixOfMinors[2][2])

        var reverseMatrix = getTransposedMatrix(matrixOfMinors)

        val determinantOfCurrentMatrix = getDeterminantSize3(matrix)
        println("OK5 " + determinantOfCurrentMatrix)

        for (i in 0..2) {
            for (j in 0..2) {
                reverseMatrix[i][j] = reverseMatrix[i][j] / determinantOfCurrentMatrix
            }
        }

        println("OK15 " + reverseMatrix[0][0] + " " + reverseMatrix[0][1] + " " + reverseMatrix[0][2])
        println("OK15 " + reverseMatrix[1][0] + " " + reverseMatrix[1][1] + " " + reverseMatrix[1][2])
        println("OK15 " + reverseMatrix[2][0] + " " + reverseMatrix[2][1] + " " + reverseMatrix[2][2])
        return reverseMatrix
    }

    private fun matrixMultiplication(firstMatrix: Array<Array<Double>>, secondMatrix: Array<Double>): Array<Double> {

        val resultMatrix: Array<Double> = Array(3, { 0.0 })
        println("OK3 " + firstMatrix[0][0] + " " + firstMatrix[0][1] + " " + firstMatrix[0][2])
        println("OK3 " + firstMatrix[1][0] + " " + firstMatrix[1][1] + " " + firstMatrix[1][2])
        println("OK3 " + firstMatrix[2][0] + " " + firstMatrix[2][1] + " " + firstMatrix[2][2])

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

        var affineTransformationMatrix: Array<Array<Double>> = Array(3, { Array(3, {0.0}) })

        affineTransformationMatrix[2][0] = 0.0
        affineTransformationMatrix[2][1] = 0.0
        affineTransformationMatrix[2][2] = 1.0

        val matrixFirstComponents: Array<Array<Double>> = Array(3, { Array(3, {0.0}) })
        matrixFirstComponents[0] = arrayOf(1.0, coordinates[0][0], coordinates[1][0])
        matrixFirstComponents[1] = arrayOf(1.0, coordinates[0][1], coordinates[1][1])
        matrixFirstComponents[2] = arrayOf(1.0, coordinates[0][2], coordinates[1][2])

        val matrixSecondComponentsX = arrayOf(coordinates[0][3], coordinates[0][4], coordinates[0][5])
        val matrixSecondComponentsY = arrayOf(coordinates[1][3], coordinates[1][4], coordinates[1][5])

        val matrixComponentsX = matrixMultiplication(getReverseOfMatrix(matrixFirstComponents), matrixSecondComponentsX)
        val matrixComponentsY = matrixMultiplication(getReverseOfMatrix(matrixFirstComponents), matrixSecondComponentsY)
        println("OK2.1 " + matrixComponentsX[0] + " " + matrixComponentsX[1] + " " + matrixComponentsX[2])
        println("OK2.2 " + matrixComponentsY[0] + " " + matrixComponentsY[1] + " " + matrixComponentsY[2])

        for (i in 0..2) {
            affineTransformationMatrix[0][i] = matrixComponentsX[i]
            affineTransformationMatrix[1][i] = matrixComponentsY[i]
        }

        return affineTransformationMatrix
    }

    private fun getInterpolation(firstI: Int,
                                 secondI: Int,
                                 thirdI: Int,
                                 fourthI: Int,
                                 ceilX: Int,
                                 floorX: Int,
                                 ceilY: Int,
                                 floorY: Int,
                                 x: Double,
                                 y: Double): Int {
        return ((firstI * (ceilX - x) + secondI * (x - floorX)) * (ceilY - y) +
                (thirdI * (ceilX - x) + fourthI * (x - floorX)) * (y - floorY)).toInt()
    }

    private fun getNewBilinearPixel(affineTransformation: Array<Array<Double>>, x: Int, y: Int, currentBitmap: Bitmap): Int {

        val x1 = affineTransformation[0][0] + x * affineTransformation[0][1] + y * affineTransformation[0][2]
        val y1 = affineTransformation[1][0] + x * affineTransformation[1][1] + y * affineTransformation[1][2]
        //println("OK14 " + x1 + " " + y1)

        var coordinates: Array<Array<Int>> = Array(2, { Array(4, {0}) })
        coordinates[0] = arrayOf(Math.floor(x1).toInt(), Math.ceil(x1).toInt(), Math.floor(x1).toInt(), Math.ceil(x1).toInt())
        coordinates[1] = arrayOf(Math.floor(y1).toInt(), Math.floor(y1).toInt(), Math.ceil(y1).toInt(), Math.ceil(y1).toInt())

        val pictureWidth: Int = currentBitmap.getWidth()
        val pictureHeight: Int = currentBitmap.getHeight()

        for (i in 0..3) {
            if (coordinates[0][i] < 0) {
                coordinates[0][i] = 0
            }
            else {
                if (coordinates[0][i] >= pictureWidth) {
                    coordinates[0][i] = pictureWidth -1
                }
            }

            if (coordinates[1][i] < 0) {
                coordinates[1][i] = 0
            }
            else {
                if (coordinates[1][i] >= pictureHeight) {
                    coordinates[1][i] = pictureHeight - 1
                }
            }
        }

        val firstPixel: Int = currentBitmap.getPixel(coordinates[0][0], coordinates[1][0])
        val secondPixel: Int = currentBitmap.getPixel(coordinates[0][1], coordinates[1][1])
        val thirdPixel: Int = currentBitmap.getPixel(coordinates[0][2], coordinates[1][2])
        val fourthPixel: Int = currentBitmap.getPixel(coordinates[0][3], coordinates[1][3])

        var newAlpha = getInterpolation(Color.alpha(firstPixel), Color.alpha(secondPixel), Color.alpha(thirdPixel), Color.alpha(fourthPixel),
            coordinates[0][1], coordinates[0][0], coordinates[1][2], coordinates[1][0], x1, y1)

        var newRed = getInterpolation(Color.red(firstPixel), Color.red(secondPixel), Color.red(thirdPixel), Color.red(fourthPixel),
            coordinates[0][1], coordinates[0][0], coordinates[1][2], coordinates[1][0], x1, y1)

        var newGreen = getInterpolation(Color.green(firstPixel), Color.green(secondPixel), Color.green(thirdPixel), Color.green(fourthPixel),
            coordinates[0][1], coordinates[0][0], coordinates[1][2], coordinates[1][0], x1, y1)

        var newBlue = getInterpolation(Color.blue(firstPixel), Color.blue(secondPixel), Color.blue(thirdPixel), Color.blue(fourthPixel),
            coordinates[0][1], coordinates[0][0], coordinates[1][2], coordinates[1][0], x1, y1)

        val newPixel = Color.argb(newAlpha, newRed, newGreen, newBlue)
        return newPixel
    }

    private fun getNewPixel(affineTransformation: Array<Array<Double>>, x: Int, y: Int, currentBitmap: Bitmap): Int {

        val x1 = affineTransformation[0][0] + x * affineTransformation[0][1] + y * affineTransformation[0][2]
        val y1 = affineTransformation[1][0] + x * affineTransformation[1][1] + y * affineTransformation[1][2]

        var coordinatesX = (Math.round(x1).toInt())
        var coordinatesY = (Math.round(y1).toInt())

        val pictureWidth: Int = currentBitmap.getWidth()
        val pictureHeight: Int = currentBitmap.getHeight()


        if (coordinatesX < 0) {
            coordinatesX = 0
        }
        else {
            if (coordinatesX >= pictureWidth) {
                coordinatesX= pictureWidth - 1
            }
        }

        if (coordinatesY < 0) {
            coordinatesY = 0
        }
        else {
            if (coordinatesY >= pictureHeight) {
                coordinatesY = pictureHeight - 1
            }
        }

        var pixel = currentBitmap.getPixel(coordinatesX, coordinatesY)
        var newAlpha = Color.alpha(pixel)

        var newRed = Color.red(pixel)

        var newGreen = Color.green(pixel)

        var newBlue = Color.blue(pixel)

        val newPixel = Color.argb(newAlpha, newRed, newGreen, newBlue)
        return newPixel
    }

    private fun bilinearFiltering() {

        pointsCoordinates[0] = arrayOf(67.0, 5.0, 125.0, 67.0, 5.0, 125.0)
        pointsCoordinates[1] = arrayOf(82.0, 56.0, 58.0, 32.0, 6.0, 8.0)

        val affineTransformation = getAffineTransformationMatrix(pointsCoordinates)
        println("OK10 " + affineTransformation[0][0] + " " + affineTransformation[0][1] + " " + affineTransformation[0][2])
        println("OK11 " + affineTransformation[1][0] + " " + affineTransformation[0][1] + " " + affineTransformation[0][2])
        println("OK12 " + affineTransformation[2][0] + " " + affineTransformation[2][1] + " " + affineTransformation[2][2])

        val imageViewStart: ImageView = findViewById(R.id.imageView)
        val bitmapStart = (imageViewStart.getDrawable() as BitmapDrawable).bitmap
        val pictureWidth: Int = bitmapStart.getWidth()
        val pictureHeight: Int = bitmapStart.getHeight()
        var newPicture = Bitmap.createBitmap(pictureWidth, pictureHeight, bitmapStart.config)

        for (i in 0 until pictureWidth) {
            for (j in 0  until pictureHeight) {
                val newPixel = getNewPixel(affineTransformation, i, j, bitmapStart)
                newPicture.setPixel(i, j, newPixel)
            }
        }

        imageView.setImageBitmap(newPicture)
    }

    private fun getFirstThreePoints() {

    }

    private fun getSecondThreePoints() {

    }
}