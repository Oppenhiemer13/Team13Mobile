package com.example.team13

import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.graphics.drawable.toBitmap
import kotlinx.android.synthetic.main.activity_main.*
import org.opencv.android.BaseLoaderCallback
import org.opencv.android.LoaderCallbackInterface
import org.opencv.android.OpenCVLoader
import org.opencv.android.Utils
import org.opencv.core.Mat
import org.opencv.core.MatOfRect
import org.opencv.core.Point
import org.opencv.core.Scalar
import org.opencv.imgproc.Imgproc
import org.opencv.imgproc.Imgproc.cvtColor
import org.opencv.objdetect.CascadeClassifier
import java.io.File
import java.io.OutputStream
import java.util.*


class MainActivity : AppCompatActivity() {

    private val resultLoadImg = 1000
    private val resultCallCamera = 1001

    private var transmissionImg: String = ""
    private var cameraUri: Uri? = null

    lateinit var main: Mat
    lateinit var copy: Mat
    var cascFile: File? = null
    var faceDetector: CascadeClassifier? = null

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        disableButtons()
        isEnabledCheck()

        actionBtn.setOnClickListener {
            saveToGallery()
        }

        loadImgBtn.setOnClickListener {
            val photoPicker = Intent(Intent.ACTION_GET_CONTENT)
            photoPicker.type = "image/*"
            startActivityForResult(photoPicker, resultLoadImg)
        }

        cameraBtn.setOnClickListener {
            val values = ContentValues()
            values.put(MediaStore.Images.Media.TITLE, "New Picture")
            values.put(MediaStore.Images.Media.DESCRIPTION, "From the Camera")
            cameraUri = contentResolver.insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                values
            )

            val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, cameraUri)
            startActivityForResult(cameraIntent, resultCallCamera)
        }

        scaleBtn.setOnClickListener {
            startNewIntent(this, ScaleActivity::class.java)
        }

        rotateBtn.setOnClickListener {
            startNewIntent(this, RotateActivity::class.java)
        }

        filtersBtn.setOnClickListener {
            startNewIntent(this, FiltersActivity::class.java)
        }

        unsharpMaskBtn.setOnClickListener {
            startNewIntent(this, UnsharpMaskActivity::class.java)
        }

        faceDetBtn.setOnClickListener {
            imageView.setImageBitmap(faceDetect())
        }

        linearFilterBtn.setOnClickListener {
            startNewIntent(this, LinearFilteringActivity::class.java)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == 111 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            loadImgBtn.isEnabled = true
        }

        if (requestCode == 112 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            cameraBtn.isEnabled = true
        }

        if (requestCode == 113 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            actionBtn.isEnabled = true
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK && requestCode == resultLoadImg) {

            val selectedImageURI = data?.data

            if (selectedImageURI != null) {

                val source = ImageDecoder.createSource(this.contentResolver, selectedImageURI)
                val bmpImage = ImageDecoder.decodeBitmap(source).copy(Bitmap.Config.ARGB_8888, true)

                transmissionImg = selectedImageURI.toString()
                imageView.setImageBitmap(bmpImage)
                enableButtons()
            }
        } else if (resultCode == Activity.RESULT_OK && requestCode == resultCallCamera) {

            if (cameraUri != null) {

                val source = ImageDecoder.createSource(this.contentResolver, cameraUri!!)
                val bmpImage = ImageDecoder.decodeBitmap(source).copy(Bitmap.Config.ARGB_8888, true)

                transmissionImg = cameraUri!!.toString()
                imageView.setImageBitmap(bmpImage)
                enableButtons()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (!OpenCVLoader.initDebug()) {
            Log.d(
                "OpenCV",
                "Internal OpenCV library not found. Using OpenCV Manager for initialization"
            )
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback)
        } else {
            Log.d("OpenCV", "OpenCV library found inside package. Using it!")
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS)
        }
    }

    private val mLoaderCallback: BaseLoaderCallback = object : BaseLoaderCallback(this) {
        override fun onManagerConnected(status: Int) {
            when (status) {
                SUCCESS -> {
                    Log.i("OpenCV", "OpenCV loaded successfully")
                    main = Mat()
                    copy = Mat()

                    val cascadeDir = getDir("cascade", MODE_PRIVATE)
                    cascFile = File(cascadeDir, "haarcascade_frontalface_alt2.xml")

                    faceDetector = CascadeClassifier(cascFile!!.absolutePath)
                }
                else -> {
                    super.onManagerConnected(status)
                }
            }
        }
    }

    private fun isEnabledCheck() {

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {  //запрос на доступ к данным
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                111
            )
        } else {
            loadImgBtn.isEnabled = true
        }

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 112)
        } else {
            cameraBtn.isEnabled = true
        }

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                113
            )
        } else {
            actionBtn.isEnabled = true
        }
    }

    private fun faceDetect(): Bitmap {
        val imageView = findViewById<ImageView>(R.id.imageView)
        val bitmap = (imageView.drawable as BitmapDrawable).bitmap

        Utils.bitmapToMat(bitmap, copy)
        Utils.bitmapToMat(bitmap, main)

        cvtColor(copy, copy, Imgproc.COLOR_RGB2GRAY)

        val faceDetections = MatOfRect()
        faceDetector!!.detectMultiScale(copy, faceDetections)


        for (rect in faceDetections.toArray()) {
            Imgproc.rectangle(
                main, Point(
                    rect.x.toDouble(),
                    rect.y.toDouble()
                ),
                Point(
                    (rect.x + rect.width).toDouble(),
                    (rect.y + rect.height).toDouble()
                ),
                Scalar(0.0, 255.0, 0.0),
                2
            )
        }

        Utils.matToBitmap(main, bitmap)
        return bitmap
    }

    private fun saveToGallery() {
        val bitmap = imageView.drawable.toBitmap()
        val fos: OutputStream
        val resolver = contentResolver
        val contentValues = ContentValues()
        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, "test")
        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpg")
        contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
        val imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
        fos = resolver.openOutputStream(Objects.requireNonNull(imageUri)!!)!!
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos)
        Objects.requireNonNull<OutputStream?>(fos)
        Toast.makeText(this, "Image saved", Toast.LENGTH_SHORT).show()
    }

    private fun <T> startNewIntent(context: Context, destinationClass: Class<T>) {
        val intent = Intent(context, destinationClass)
        intent.putExtra("ImageUri", transmissionImg)
        startActivity(intent)
    }

    private fun disableButtons() {
        loadImgBtn.isEnabled = false
        cameraBtn.isEnabled = false
        actionBtn.isEnabled = false
        faceDetBtn.isEnabled = false
        scaleBtn.isEnabled = false
        filtersBtn.isEnabled = false
        unsharpMaskBtn.isEnabled = false
        rotateBtn.isEnabled = false
        linearFilterBtn.isEnabled = false
    }

    private fun enableButtons() {
        actionBtn.isEnabled = true
        faceDetBtn.isEnabled = true
        scaleBtn.isEnabled = true
        filtersBtn.isEnabled = true
        unsharpMaskBtn.isEnabled = true
        rotateBtn.isEnabled = true
        linearFilterBtn.isEnabled = true
    }
}


