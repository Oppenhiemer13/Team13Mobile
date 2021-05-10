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

    private fun getDeterminantSize3(matrix: Array<Array<Double>>): Double {
        val sum1 = matrix[0][0] * matrix[1][1] * matrix[2][2]
        val sum2 = matrix[1][0] * matrix[0][2] * matrix[2][1]
        val sum3 = matrix[0][1] * matrix[2][1] * matrix[2][0]
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

        var matrixOfMinors: Array<Array<Double>> = Array(3, { Array(3, {0.0}) })

        for (i in 0..2) {
            for (j in 0..2) {

                var oneMinor: Array<Array<Double>> = Array(2, { Array(2, {0.0}) })
                var positionForNewNumber = 0
                for (x in 0..2) {
                    for (y in 0..2) {
                        if (x != i && y != j) {
                            oneMinor[positionForNewNumber / 2][positionForNewNumber % 2] = matrix[i][j]
                            positionForNewNumber += 1
                        }
                    }
                }

                matrixOfMinors[i][j] = getDeterminantSize2(oneMinor)
                matrixOfMinors[0][1] = -1 * matrixOfMinors[0][1]
                matrixOfMinors[1][0] = -1 * matrixOfMinors[1][0]
                matrixOfMinors[1][2] = -1 * matrixOfMinors[1][2]
                matrixOfMinors[2][1] = -1 * matrixOfMinors[2][1]
            }
        }

        return matrixOfMinors
    }

    private fun getReverseOfMatrix(matrix: Array<Array<Double>>): Array<Array<Double>> {

        var matrixOfMinors = getMatrixAlgebraicComplements(matrix)

        var reverseMatrix = getTransposedMatrix(matrixOfMinors)

        val determinantOfCurrentMatrix = getDeterminantSize3(matrix)

        for (i in 0..2) {
            for (j in 0..2) {
                reverseMatrix[i][j] = reverseMatrix[i][j] / determinantOfCurrentMatrix
            }
        }

        return reverseMatrix
    }

    private fun matrixMultiplication(firstMatrix: Array<Array<Double>>, secondMatrix: Array<Double>): Array<Double> {

        val resultMatrix: Array<Double> = Array(3, { 0.0 })

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
        matrixFirstComponents[0] = arrayOf(1.0, coordinates[0][1], coordinates[1][1])
        matrixFirstComponents[0] = arrayOf(1.0, coordinates[0][2], coordinates[1][2])

        val matrixSecondComponentsX = arrayOf(coordinates[0][3], coordinates[0][4], coordinates[0][5])
        val matrixSecondComponentsY = arrayOf(coordinates[1][3], coordinates[1][4], coordinates[1][5])

        val matrixComponentsX = matrixMultiplication(getReverseOfMatrix(matrixFirstComponents), matrixSecondComponentsX)
        val matrixComponentsY = matrixMultiplication(getReverseOfMatrix(matrixFirstComponents), matrixSecondComponentsY)

        for (i in 0..2) {
            affineTransformationMatrix[0][i] = matrixComponentsX[i]
            affineTransformationMatrix[1][i] = matrixComponentsY[i]
        }

        return affineTransformationMatrix
    }

    private fun bilinearFiltering() {
        
    }

}