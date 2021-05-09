package com.example.team13

import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
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


class MainActivity : AppCompatActivity() {

    private val RESULT_LOAD_IMG = 1000
    private val RESULT_CALL_CAMERA = 1001

    companion object{
        lateinit var transmissionImg : String
        var cameraUri : Uri? = null

        lateinit var main : Mat
        lateinit var copy : Mat

        var cascFile: File? = null
        var faceDetector: CascadeClassifier? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        loadImgBtn.isEnabled = false
        cameraBtn.isEnabled = false
        isEnabledCheck()

        actionBtn.setOnClickListener {

        }

        loadImgBtn.setOnClickListener {
            val photoPicker = Intent(Intent.ACTION_GET_CONTENT);
            photoPicker.type = "image/*"
            startActivityForResult(photoPicker, RESULT_LOAD_IMG)
        }

        cameraBtn.setOnClickListener{
            val values = ContentValues()
            values.put(MediaStore.Images.Media.TITLE, "New Picture")
            values.put(MediaStore.Images.Media.DESCRIPTION, "From the Camera")
            cameraUri =  contentResolver.insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                values
            )

            val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, cameraUri)
            startActivityForResult(cameraIntent, RESULT_CALL_CAMERA)
        }

        scaleBtn.setOnClickListener{
            val intent = Intent(this, ScaleActivity::class.java)
            intent.putExtra("ImageUri", transmissionImg)
            startActivity(intent)
        }

        filtersBtn.setOnClickListener{
            val intent = Intent(this, FiltersActivity::class.java)
            intent.putExtra("ImageUri", transmissionImg)
            startActivity(intent)
        }

        unsharpMaksBtn.setOnClickListener{
            val intent = Intent(this, UnsharpMaskActivity::class.java)
            intent.putExtra("BitmapImage", transmissionImg)
            startActivity(intent)
        }

        button.setOnClickListener{
            imageView.setImageBitmap(peopleDetect())
        }
    }

    private fun isEnabledCheck() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {  //запрос на доступ к данным
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                111
            )
        } else {
            loadImgBtn.isEnabled = true
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 112)
        } else {
            cameraBtn.isEnabled = true
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                113
            )
        } else {
            actionBtn.isEnabled = true
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) { //доступ к данным
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {   //вывод картинки на ImageView
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK && requestCode == RESULT_LOAD_IMG) {

            val selectedImageURI = data?.data

            if (selectedImageURI != null) {

                val source = ImageDecoder.createSource(this.contentResolver, selectedImageURI)
                val bmpImage = ImageDecoder.decodeBitmap(source).copy(Bitmap.Config.ARGB_8888, true)

                transmissionImg = selectedImageURI.toString()
                imageView.setImageBitmap(bmpImage)
            }
        }
        else if(resultCode == Activity.RESULT_OK && requestCode == RESULT_CALL_CAMERA){

            if (cameraUri != null) {

                val source = ImageDecoder.createSource(this.contentResolver, cameraUri!!)
                val bmpImage = ImageDecoder.decodeBitmap(source).copy(Bitmap.Config.ARGB_8888, true)

                transmissionImg = cameraUri!!.toString()
                imageView.setImageBitmap(bmpImage)
            }
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

    fun peopleDetect(): Bitmap? {
        val imageView = findViewById<ImageView>(R.id.imageView)
        val bitmap = (imageView.drawable as BitmapDrawable).bitmap

        Utils.bitmapToMat(bitmap, copy)
        Utils.bitmapToMat(bitmap, main)

        cvtColor(copy, copy, Imgproc.COLOR_RGB2GRAY)
        
        val faceDetections = MatOfRect()
        faceDetector!!.detectMultiScale(copy, faceDetections)


        for (rect in faceDetections.toArray()) {
            Imgproc.rectangle(main, Point(rect.x.toDouble(),
                rect.y.toDouble()),
                Point((rect.x + rect.width).toDouble(),
                    (rect.y + rect.height).toDouble()),
                Scalar(0.0, 255.0, 0.0),
                2)
        }

        Utils.matToBitmap(main, bitmap)
        return bitmap
    }
}


