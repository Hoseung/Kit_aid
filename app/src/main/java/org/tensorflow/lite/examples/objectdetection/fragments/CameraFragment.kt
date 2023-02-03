/*
 * Copyright 2022 The TensorFlow Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.tensorflow.lite.examples.objectdetection.fragments

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.res.Configuration
import android.graphics.*
import android.media.Image
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.util.Size
import android.view.LayoutInflater
import android.view.Surface.ROTATION_0
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.camera.core.*
import androidx.camera.core.Camera
import androidx.camera.core.ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888
import androidx.camera.core.ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import org.tensorflow.lite.examples.objectdetection.*
import org.tensorflow.lite.examples.objectdetection.R
import org.tensorflow.lite.examples.objectdetection.databinding.FragmentCameraBinding
import org.tensorflow.lite.examples.objectdetection.new.MemberActivity
import org.tensorflow.lite.examples.objectdetection.new.ResultActivity
import org.tensorflow.lite.examples.objectdetection.new.pathToBitmap
import org.tensorflow.lite.task.gms.vision.detector.Detection
import org.tensorflow.lite.task.gms.vision.detector.ObjectDetector
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.lang.Math.ceil
import java.nio.ByteBuffer
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CameraFragment : Fragment(), ObjectDetectorHelper.DetectorListener {

    private val TAG = "CameraFragment"

    private var binding: FragmentCameraBinding? = null

    private val fragmentCameraBinding
        get() = binding!!

    private lateinit var objectDetectorHelper: ObjectDetectorHelper
    //private lateinit var regressionHelper: RegressionHelper
    private lateinit var bitmapBuffer: Bitmap
    private lateinit var bitmapBufferCapture: Bitmap
    private var preview: Preview? = null
    private var imageAnalyzer: ImageAnalysis? = null
    private var camera: Camera? = null
    private var cameraProvider: ProcessCameraProvider? = null
    //private val range = camera?.cameraInfo?.exposureState?.exposureCompensationRange

    private lateinit var imageCapture: ImageCapture

    private val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"

    /** Blocking camera operations are performed using this executor */
    private lateinit var cameraExecutor: ExecutorService

    override fun onResume() {
        super.onResume()
        // Make sure that all permissions are still present, since the
        // user could have removed them while the app was in paused state.
        if (!PermissionsFragment.hasPermissions(requireContext())) {
            Navigation.findNavController(requireActivity(), R.id.fragment_container)
                .navigate(CameraFragmentDirections.actionCameraToPermissions())
        }

        // update Product name
        val productName = view?.findViewById<TextView>(R.id.productNameInfo)
        val myPrdName = String.format("Product: %s", MyEntryPoint.prefs.getString("prodName", "Bovine IgG"))
        productName?.text = myPrdName

        val lotNum = view?.findViewById<TextView>(R.id.lotNumber)
        lotNum?.text = String.format("LOT #: %s", MyEntryPoint.prefs.getString("lotNum", "220003"))
    }

    override fun onDestroyView() {
        binding = null
        super.onDestroyView()

        // Shut down our background executor
        cameraExecutor.shutdown()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCameraBinding.inflate(inflater, container, false)

        binding!!.examineHistoryButton.setOnClickListener {
            startActivity(Intent(requireContext(), HistoryActivity::class.java))
        }

        binding!!.kitListButton.setOnClickListener {
            startActivity(Intent(requireContext(), SelectActivity::class.java))
        }

        binding!!.profileImageView.setOnClickListener {
            startActivity(Intent(requireContext(), MemberActivity::class.java))
        }

        return fragmentCameraBinding.root
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }

    @SuppressLint("MissingPermission")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        objectDetectorHelper = ObjectDetectorHelper(
            context = requireContext(),
            objectDetectorListener = this
        )

        // Attach listeners to UI control widgets
        //initBottomSheetControls()
    }

    // Initialize CameraX, and prepare to bind the camera use cases
    private fun setUpCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener(
            {
                // CameraProvider
                cameraProvider = cameraProviderFuture.get()

                // Build and bind the camera use cases
                bindCameraUseCases()
            },
            ContextCompat.getMainExecutor(requireContext())
        )
    }

    // Declare and bind preview, capture and analysis use cases
    @SuppressLint("UnsafeOptInUsageError")
    private fun bindCameraUseCases() {

        // CameraProvider
        val cameraProvider =
            cameraProvider ?: throw IllegalStateException("Camera initialization failed.")

        // CameraSelector - makes assumption that we're only using the back camera
        val cameraSelector =
            CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build()

        // Preview. Only using the 4:3 ratio because this is the closest to our models
        preview =
            Preview.Builder()
                .setTargetAspectRatio(AspectRatio.RATIO_4_3)
                .setTargetRotation(fragmentCameraBinding.viewFinder.display.rotation)
                .build()

        val builder = ImageCapture.Builder().setCaptureMode(CAPTURE_MODE_MAXIMIZE_QUALITY)//CAPTURE_MODE_ZERO_SHUTTER_LAG)
            .setTargetAspectRatio(AspectRatio.RATIO_4_3)
            .setTargetRotation(ROTATION_0)

        imageCapture = builder.build()
//              .also{
//            }

        // ImageAnalysis. Using RGBA 8888 to match how our models work
        imageAnalyzer =
            ImageAnalysis.Builder()
                .setTargetAspectRatio(AspectRatio.RATIO_4_3)
                //.setTargetResolution(Size(1440, 1080)) // 1440 1080 max?
                .setTargetRotation(fragmentCameraBinding.viewFinder.display.rotation)
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .setOutputImageFormat(OUTPUT_IMAGE_FORMAT_RGBA_8888)
                .build()
                // The analyzer can then be assigned to the instance
                .also {
                    it.setAnalyzer(cameraExecutor) { image ->
                        if (!::bitmapBuffer.isInitialized) {
                            // The image rotation and RGB image buffer are initialized only once
                            // the analyzer has started running
                            bitmapBuffer = Bitmap.createBitmap(
                                image.width,
                                image.height,
                                Bitmap.Config.ARGB_8888
                            )
                        }
                        detectObjects(image, false) // Not for capture
                    }
                }

        // Must unbind the use-cases before rebinding them
        cameraProvider.unbindAll()

        try {
            // A variable number of use-cases can be passed here -
            // camera provides access to CameraControl & CameraInfo
            camera = cameraProvider.bindToLifecycle(
                this,
                cameraSelector,
                preview,
                imageAnalyzer,
                imageCapture
            )
            //bindCaptureListener() // No Capture button
            // Attach the viewfinder's surface provider to preview use case
            preview?.setSurfaceProvider(fragmentCameraBinding.viewFinder.surfaceProvider)
        } catch (exc: Exception) {
            Log.e(TAG, "Use case binding failed", exc)
        }
        MyEntryPoint.prefs.setCnt(0)
    }

    private var cap = true

    override fun onStart() {
        super.onStart()
        cap = true
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        imageAnalyzer?.targetRotation = fragmentCameraBinding.viewFinder.display.rotation
    }

    override fun onError(error: String) {
        activity?.runOnUiThread {
            Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onInitialized() {
        objectDetectorHelper.setupObjectDetector()
        // Initialize our background executor
        cameraExecutor = Executors.newSingleThreadExecutor()

        // Wait for the views to be properly laid out
        fragmentCameraBinding.viewFinder.post {
            // Set up the camera and its use cases
            setUpCamera()
        }
    }

    /*
    Capture image into memory and perform downstream tasks
     */
    private fun captureAndDetect() {
        if (!::imageCapture.isInitialized) return

        println("!%%%%%%%%%%%%%%%%%%%%% In captureAndDetect")
        val cameraController = camera?.cameraControl
        val autoExposurePoint = SurfaceOrientedMeteringPointFactory(0.2f, 0.5f).createPoint(.5f, .51f)
//        val autoExposurePoint2 = SurfaceOrientedMeteringPointFactory(0.1f, 0.3f).createPoint(.49f, .52f)
//        val autoExposurePoint3 = SurfaceOrientedMeteringPointFactory(0.1f, 0.3f).createPoint(.5f, .49f)
//        val autoExposurePoint4 = SurfaceOrientedMeteringPointFactory(0.1f, 0.3f).createPoint(.51f, .48f)
//        val autoExposurePoint5 = SurfaceOrientedMeteringPointFactory(0.1f, 0.3f).createPoint(.5f, .47f)
        val action = FocusMeteringAction.Builder(autoExposurePoint)
//            .addPoint(autoExposurePoint2)
//            .addPoint(autoExposurePoint3)
//            .addPoint(autoExposurePoint4)
//            .addPoint(autoExposurePoint5)
            .build()
        cameraController?.startFocusAndMetering(action)
        val cameraInfo = camera?.cameraInfo

        //cameraController?.setExposureCompensationIndex(-5)
        imageCapture.takePicture(cameraExecutor,
            object :  ImageCapture.OnImageCapturedCallback() {
                override fun onCaptureSuccess(imageC: ImageProxy) {
                    println("CCCCCCCCAAAAAAAAAMMMMMMMMMMEEEEEEEERRRRRRRRAAAAAAAAA")

                    println(cameraInfo?.exposureState?.exposureCompensationIndex)

                    val imageRotation = imageC.imageInfo.rotationDegrees
                    // Pass Bitmap and rotation to the object detector helper for processing and detection
                    println("###################### Image rotation $imageRotation")

                    val bitmap = imageProxyToBitmap(imageC)
                    imageC.close()
                    objectDetectorHelper.detectSecond(bitmap!!, imageRotation)
//                    detectObjects(image, true
                    println("!%%%%%%%%%%%%%%%%%%%%% OBJECT DETECTED")
                }
            }
        )
    }

    fun imageProxyToBitmap(imageProxy: ImageProxy): Bitmap? {
        //https://developer.android.com/reference/android/media/Image.html#getFormat()
        //https://developer.android.com/reference/android/graphics/ImageFormat#JPEG
        //https://developer.android.com/reference/android/graphics/ImageFormat#YUV_420_888
        if (imageProxy.format == ImageFormat.JPEG) {
            val buffer = imageProxy.planes[0].buffer
            buffer.rewind()
            val bytes = ByteArray(buffer.remaining())
            buffer.get(bytes)
            val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)

            return bitmap
        }
        else if (imageProxy.format == ImageFormat.YUV_420_888) {
            val yBuffer = imageProxy.planes[0].buffer // Y
            val uBuffer = imageProxy.planes[1].buffer // U
            val vBuffer = imageProxy.planes[2].buffer // V

            val ySize = yBuffer.remaining()
            val uSize = uBuffer.remaining()
            val vSize = vBuffer.remaining()

            val nv21 = ByteArray(ySize + uSize + vSize)

            yBuffer.get(nv21, 0, ySize)
            vBuffer.get(nv21, ySize, vSize)
            uBuffer.get(nv21, ySize + vSize, uSize)

            val yuvImage = YuvImage(nv21, ImageFormat.NV21, imageProxy.width, imageProxy.height, null)
            val out = ByteArrayOutputStream()
            yuvImage.compressToJpeg(Rect(0, 0, yuvImage.width, yuvImage.height), 100, out)
            val imageBytes = out.toByteArray()
            val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)

            return bitmap
        }
        return null
    }
//    private var contentUri: Uri? = null

    private fun cutBbox(bitmap: Bitmap, bbox: RectF, factorWidth: Float = 1f, factorHeight: Float = 1f): Bitmap {
        val width = bbox.width()
        val height = bbox.height()
        println("WIDTH and HEIGHT ${width} ${height}")
        println("bcx, bcy, ${bbox.centerX()} ${bbox.centerY()}")
        var bitmap = Bitmap.createBitmap(bitmap,
            ceil((bbox.centerX() - 0.525 * width)*factorWidth).toInt(),
            ceil((bbox.centerY() - 0.525 * height)*factorHeight).toInt(),
            ceil(1.05*width*factorWidth).toInt(),
            (height*factorHeight).toInt())
        return bitmap
    }

    private fun rotate(bitmap: Bitmap, degrees: Float): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(degrees)
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    fun imageSaver(bitmap: Bitmap): String{
        val photoFile = File(
            getOutputDirectory(requireActivity()),
            SimpleDateFormat(
                FILENAME_FORMAT, Locale.KOREA
            ).format(System.currentTimeMillis()) + ".png"
        )

        val fileOutputStream = FileOutputStream(photoFile) //location of the image
        val uri = Uri.fromFile(photoFile)

        bitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream)
        //bitmap.recycle() //
        return photoFile.absolutePath
    }

    private fun carryOn(bitmap: Bitmap, bbox: RectF){
        //var bitmap =
        var cropped: Bitmap = cutBbox(rotate(bitmap, 90f), bbox)

//        cropped = Bitmap.createBitmap(cropped,
//            Math.ceil(cropped.width*0.08).toInt(),
//            Math.ceil(cropped.height *0.36).toInt(),
//            Math.ceil(cropped.width*0.54).toInt(),
//            Math.ceil(cropped.height * 0.244).toInt())

        //contentUri = imageSaver(cropped)
        val absolutePath = imageSaver(cropped)
        absolutePath.let {
            val resultIntent = Intent(requireContext(), ResultActivity::class.java)
            resultIntent.putExtra("imagePath", it)
            startActivity(resultIntent)
        }
    }

    private fun getOutputDirectory(activity: Activity): File = with(activity) {
        val mediaDir = externalMediaDirs.firstOrNull()?.let {
            File(it, getString(R.string.app_name)).apply { mkdirs() }
        }
        return if (mediaDir != null && mediaDir.exists())
            mediaDir else filesDir
    }

    private fun detectObjects(image: ImageProxy, cap: Boolean) {
        if (cap){
            // Copy out RGB bits to the shared bitmap buffer
            if (!::bitmapBufferCapture.isInitialized) {
                // The image rotation and RGB image buffer are initialized only once
                // the analyzer has started running
                println("################## INITIALIZING Bitmap Buffer")
                bitmapBufferCapture = Bitmap.createBitmap(
                    image.width,
                    image.height,
                    Bitmap.Config.ARGB_8888
                )
            }
            image.use { bitmapBufferCapture.copyPixelsFromBuffer(image.planes[0].buffer) }

            val imageRotation = image.imageInfo.rotationDegrees
            // Pass Bitmap and rotation to the object detector helper for processing and detection

            println("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% Calling detectSecond()")
            println("###################### Image rotation $imageRotation")
            // SAVE to check
            //val absolutePath = imageSaver(bitmapBufferCapture)
            println("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<< IMAGE SAVED")

            objectDetectorHelper.detectSecond(bitmapBufferCapture, imageRotation)

        } else {
            // Copy out RGB bits to the shared bitmap buffer
            image.use { bitmapBuffer.copyPixelsFromBuffer(image.planes[0].buffer) }

            val imageRotation = image.imageInfo.rotationDegrees
            // Pass Bitmap and rotation to the object detector helper for processing and detection

            objectDetectorHelper.detect(bitmapBuffer, imageRotation)
        }

    }

    // Update UI after objects have been detected. Extracts original image height/width
    // to scale and place bounding boxes properly through OverlayView
    override fun onResults(
        image: Bitmap,
        results: MutableList<Detection>?,
        imageHeight: Int,
        imageWidth: Int
    ) {
        //var uri: Uri
        val cnt = MyEntryPoint.prefs.getCnt()

        activity?.runOnUiThread {
            // Pass necessary information to OverlayView for drawing on the canvas
            fragmentCameraBinding.overlay.setResults(
                results ?: LinkedList<Detection>(),
                imageHeight,
                imageWidth
            )

            if (results != null && results.size > 0 ) {
                val i = results[0]
                    println(i.categories[0].score)
                    if (i.categories[0].score > 0.92) {
                        if (cnt > 7) {
                            if (cap) {
                                MyEntryPoint.prefs.setCnt(0)
                                Toast.makeText(requireContext(), "Ready to Capture", Toast.LENGTH_SHORT).show()
                                cap = false
                                captureAndDetect()
                                //captureCamera()
                                //savePictureToMemory(image, i.boundingBox!!)
                                //ObjectDetectorHelper.clearObjectDetector()
                                //carryOn(image, i.boundingBox!!)
                            }
                        } else {
                            if (i.boundingBox.height() < 0.25*imageHeight){
//                                Toast.makeText(requireContext(), "Too far!", Toast.LENGTH_SHORT).show()
                                MyEntryPoint.prefs.setCnt(0)
                            }
                            else if  (i.boundingBox.height() > 0.6*imageHeight){
//                                Toast.makeText(requireContext(), "Too close!", Toast.LENGTH_SHORT).show()
                                MyEntryPoint.prefs.setCnt(0)
                            }
                            else {
                                MyEntryPoint.prefs.setCnt(cnt+1)
                            }
                        }
                    } else {
                        MyEntryPoint.prefs.setCnt(0)
                    }
                    println("CURRENT CNT $cnt")
                //}
            }
            fragmentCameraBinding.overlay.invalidate()
        }
    }

    /*
    Save Captured image and pass to Result
     */
    override fun onSecondResult(
        image: Bitmap,
        results: MutableList<Detection>?,
        imageHeight: Int,
        imageWidth: Int
    ) {
        //var uri: Uri
        println("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% SECOND RESULTS>>>>>>>>>>>>>>>>")
        println(results)  // RESULT가 비어있음...
        activity?.runOnUiThread {
            if (results != null && results.size > 0 ) {
                val i = results[0]
                if (i.categories[0].score > 0.96) {
                    Toast.makeText(requireContext(), "Capturing...", Toast.LENGTH_SHORT).show()
                    //savePictureToMemory(image, i.boundingBox!!)
                    carryOn(image, i.boundingBox!!)
                } else {
                    Toast.makeText(requireContext(), "try again...", Toast.LENGTH_SHORT).show()
                    cap = true
                }
            }
        }
        println("CCCCCCCCCCCC OnSecondResult Done")
    }
}
