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

//import android.media.MediaScannerConnection
//import android.os.Handler
//import android.os.Looper
//import androidx.camera.core.ImageCapture.CAPTURE_MODE_ZERO_SHUTTER_LAG
//import org.tensorflow.lite.examples.objectdetection.new.LoginActivity
//import org.tensorflow.lite.examples.objectdetection.new.Main2Activity
//import java.io.FileNotFoundException
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.graphics.RectF
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.util.Size
import android.view.LayoutInflater
import android.view.Surface.ROTATION_0
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.camera.core.*
import androidx.camera.core.ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888
import androidx.camera.core.ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import org.tensorflow.lite.examples.objectdetection.HistoryActivity
import org.tensorflow.lite.examples.objectdetection.ObjectDetectorHelper
//import org.tensorflow.lite.examples.objectdetection.RegressionHelper
import org.tensorflow.lite.examples.objectdetection.R
import org.tensorflow.lite.examples.objectdetection.SelectActivity
import org.tensorflow.lite.examples.objectdetection.databinding.FragmentCameraBinding
import org.tensorflow.lite.examples.objectdetection.new.MemberActivity
import org.tensorflow.lite.examples.objectdetection.new.ResultActivity
import org.tensorflow.lite.task.gms.vision.detector.Detection
import org.tensorflow.lite.task.gms.vision.detector.ObjectDetector
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
    private var preview: Preview? = null
    private var imageAnalyzer: ImageAnalysis? = null
    private var camera: Camera? = null
    private var cameraProvider: ProcessCameraProvider? = null

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

        // ImageAnalysis. Using RGBA 8888 to match how our models work
        imageAnalyzer =
            ImageAnalysis.Builder()
                //.setTargetAspectRatio(AspectRatio.RATIO_4_3)
                .setTargetResolution(Size(2400,1800)) // 1440 1080 max?
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
                        detectObjects(image)
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
    }

    private var cap = true

    override fun onStart() {
        super.onStart()
        cap = true
    }

    private fun savePictureToMemory() {
        // 2
        //var cx: Int
        //var cy: Int
        var width: Float
        var height: Float
        var result: Detection?
        var bbox: RectF

        if (!::imageCapture.isInitialized) return
        imageCapture.takePicture(cameraExecutor,
            object :  ImageCapture.OnImageCapturedCallback() {
                override fun onCaptureSuccess(image: ImageProxy) {
                    //get bitmap from image
                    var bitmap = imageProxyToBitmap(image)
                    bitmap = rotate(bitmap, 90f)
//                    objectDetectorHelper.detect(bitmap, 0)
//
//                    result = objectDetectorHelper.detect2(bitmap, 0)
//                    if(result != null){
//                        bbox = result?.boundingBox!!
//                        width = bbox.width()
//                        height = bbox.height()
//                        println("WIDTH and HEIGHT ${width} ${height}")
//                        println("bcx, bcy, ${bbox.centerX()} ${bbox.centerY()}")
//                        bitmap = Bitmap.createBitmap(bitmap,
//                            (ceil(bbox.centerX() - 0.525 * width)*6.3f).toInt(),
//                            (ceil(bbox.centerY() - 0.525 * height)*6.3f).toInt(),
//                            ceil(6.3*1.05*width).toInt(),
//                            ceil(6.3*height).toInt())
                    //}
                    //contentUri = imageSaver(bitmap)
                    println("FILE SAVED ${contentUri?.path}")

                    super.onCaptureSuccess(image)
                    image.close()

                    // Move to Result screen
                    contentUri?.let {
                        val resultIntent = Intent(requireContext(), ResultActivity::class.java)
                        resultIntent.putExtra("imageUri", it)
                        startActivity(resultIntent)
                    }
                }
                override fun onError(exception: ImageCaptureException) {
                    super.onError(exception)
                }
            }
        )
    }

    private fun cutBbox(bitmap: Bitmap, bbox: RectF): Bitmap {
        val width = bbox.width()
        val height = bbox.height()
        println("WIDTH and HEIGHT ${width} ${height}")
        println("bcx, bcy, ${bbox.centerX()} ${bbox.centerY()}")
        var bitmap = Bitmap.createBitmap(bitmap,
            ceil(bbox.centerX() - 0.525 * width).toInt(),
            ceil(bbox.centerY() - 0.525 * height).toInt(),
            ceil(1.05*width).toInt(),
            height.toInt())
        return bitmap
    }

    private fun rotate(bitmap: Bitmap, degrees: Float): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(degrees)
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    private fun imageSaver(bitmap: Bitmap): String{
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
        //contentUri = imageSaver(cropped)
        val absolutePath = imageSaver(cropped)
        absolutePath.let {
            val resultIntent = Intent(requireContext(), ResultActivity::class.java)
            resultIntent.putExtra("imagePath", it)
            startActivity(resultIntent)
        }
    }

    private fun imageProxyToBitmap(image: ImageProxy): Bitmap {
        val planeProxy = image.planes[0]
        val buffer: ByteBuffer = planeProxy.buffer
        val bytes = ByteArray(buffer.remaining())
        buffer.get(bytes)
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }

    private var contentUri: Uri? = null

    private fun captureCamera() {
        if (!::imageCapture.isInitialized) return
        val photoFile = File(
            getOutputDirectory(requireActivity()),
            SimpleDateFormat(
                FILENAME_FORMAT, Locale.KOREA
            ).format(System.currentTimeMillis()) + ".jpg"
        )

        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()
        imageCapture.takePicture(
            outputOptions,
            cameraExecutor,
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    val savedUri = outputFileResults.savedUri ?: Uri.fromFile(photoFile)
                    //val rotation = binding!!.viewFinder.display.rotation // 회전 값 설정
                    contentUri = savedUri
                    println("@@@@@ $savedUri $contentUri")
                    contentUri?.let {
                        val resultIntent = Intent(requireContext(), ResultActivity::class.java)
                        resultIntent.putExtra("imageUri", it)
                        startActivity(resultIntent)
                    }
                }

                override fun onError(e: ImageCaptureException) {
                    e.printStackTrace()
                }
            }
        )
    }

    private fun getOutputDirectory(activity: Activity): File = with(activity) {
        val mediaDir = externalMediaDirs.firstOrNull()?.let {
            File(it, getString(R.string.app_name)).apply { mkdirs() }
        }
        return if (mediaDir != null && mediaDir.exists())
            mediaDir else filesDir
    }


    private fun detectObjects(image: ImageProxy) {
        // Copy out RGB bits to the shared bitmap buffer
        image.use { bitmapBuffer.copyPixelsFromBuffer(image.planes[0].buffer) }

        val imageRotation = image.imageInfo.rotationDegrees
        // Pass Bitmap and rotation to the object detector helper for processing and detection
        objectDetectorHelper.detect(bitmapBuffer, imageRotation)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        imageAnalyzer?.targetRotation = fragmentCameraBinding.viewFinder.display.rotation
    }

    // Update UI after objects have been detected. Extracts original image height/width
    // to scale and place bounding boxes properly through OverlayView
    override fun onResults(
        image: Bitmap,
        results: MutableList<Detection>?,
        inferenceTime: Long,
        imageHeight: Int,
        imageWidth: Int
    ) {
        //var uri: Uri
        activity?.runOnUiThread {
            fragmentCameraBinding.bottomSheetLayout.inferenceTimeVal.text =
                String.format("%d ms", inferenceTime)

            // Pass necessary information to OverlayView for drawing on the canvas
            fragmentCameraBinding.overlay.setResults(
                results ?: LinkedList<Detection>(),
                imageHeight,
                imageWidth
            )

            if (results != null) {
                for (i in results) {
//                    cx = .centerX()
//                    cy = i.boundingBox.centerY()
//                    width = i.boundingBox.width()
//                    height = i.boundingBox.height()
                    println(i.categories[0].score)
                    if (i.categories[0].score > 0.98) {
                        if (cap) {
                            Toast.makeText(requireContext(), "Wait...", Toast.LENGTH_SHORT).show()
                            //captureCamera()
                            //savePictureToMemory()
                            //ObjectDetectorHelper.clearObjectDetector()
                            carryOn(image, i.boundingBox!!)
                        }
                        cap = false
                    }
                }
            }
            fragmentCameraBinding.overlay.invalidate()
        }
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

        fragmentCameraBinding.progressCircular.visibility = View.GONE
    }
}
