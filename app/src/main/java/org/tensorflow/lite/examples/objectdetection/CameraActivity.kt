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
package org.tensorflow.lite.examples.objectdetection

//import android.media.Image
//import android.net.Uri
//import android.util.Size
//import org.tensorflow.lite.examples.objectdetection.databinding.viewBinding
//import org.tensorflow.lite.examples.objectdetection.new.MemberActivity

import android.annotation.SuppressLint
import android.app.Activity
import android.app.DownloadManager
import android.content.Intent
import android.content.res.Configuration
import android.graphics.*
import android.os.Bundle
import android.util.Log
import android.view.Surface.ROTATION_0
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.core.Camera
import androidx.camera.core.ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888
import androidx.camera.core.ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import org.tensorflow.lite.examples.objectdetection.*
import org.tensorflow.lite.examples.objectdetection.MyEntryPoint.Companion.auth
import org.tensorflow.lite.examples.objectdetection.MyEntryPoint.Companion.storage
import org.tensorflow.lite.examples.objectdetection.adapter.Models
import org.tensorflow.lite.examples.objectdetection.adapter.ModelsViewModel
import org.tensorflow.lite.examples.objectdetection.adapter.ModelsViewModelFactory
import org.tensorflow.lite.examples.objectdetection.databinding.ActivityCameraBinding
import org.tensorflow.lite.examples.objectdetection.new.ResultActivity
import org.tensorflow.lite.task.gms.vision.detector.Detection
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


class CameraActivity : AppCompatActivity(), ObjectDetectorHelper.DetectorListener {
    private val TAG = "cameraFragment"

    private lateinit var viewBinding: ActivityCameraBinding

    private val modelsViewModel: ModelsViewModel by viewModels {
        ModelsViewModelFactory((application as MyEntryPoint).database.modelsDao())
    }
    //private val viewBinding
    //get() = viewBinding!!

    private var imageCaptureDetectionFail = 0

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
    //private val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"

    /** Blocking camera operations are performed using this executor */

    private lateinit var cameraExecutor: ExecutorService
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //Firebase storage setting
        auth = FirebaseAuth.getInstance()
//        db = FirebaseFirestore.getInstance()
        storage = Firebase.storage("gs://kitaid.appspot.com/")
        Log.d("FirebaseCompare", "!!!storage:$storage")

        val storageRef = storage.reference
        Log.d("FirebaseCompare2", "$storageRef")

        viewBinding = ActivityCameraBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        viewBinding.examineHistoryButton.setOnClickListener {
            startActivity(Intent(this, HistoryActivity::class.java))
        }

        viewBinding.kitListButton.setOnClickListener {
            startActivity(Intent(this, SelectActivity::class.java))
        }

        loadLocalModels(storageRef)

//        viewBinding.profileImageView.setOnClickListener {
//            startActivity(Intent(this, MemberActivity::class.java))
//        }

        objectDetectorHelper = ObjectDetectorHelper(
            context = this,
            objectDetectorListener = this
        )
        cameraExecutor = Executors.newSingleThreadExecutor()

    }

    private fun loadLocalModels(storageRef:StorageReference) {
        // import saved preferences
        val prodName = MyEntryPoint.prefs.getString("prodName", "AniCheck-bIgG")
        var lotNum = MyEntryPoint.prefs.getString("lotNum", "BIG22003")
        val date = MyEntryPoint.prefs.getString("date", "20231225")
        val hash = MyEntryPoint.prefs.getString("hash", "abcdefg123")
        val modelCalibration = "${prodName}_${lotNum}.dat"
        var downloadedURI: Nothing? = null

        // setting file path
        val fileRef: StorageReference = storageRef.child(modelCalibration)
        val downloadedFile = File(applicationContext.getExternalFilesDir("Calibration_file"), modelCalibration)
        Log.d("FirebaseCompare3", "$fileRef.")

        // if file is exists, replace it with new one
        if (downloadedFile.exists()) {
            downloadedFile.delete()
        }

        // file download with download manager
        fileRef.downloadUrl.addOnSuccessListener {
            Log.d("FirebaseCompare4", "url down comp!!! $it")

            val request = DownloadManager.Request(it)
                .setTitle("File")
                .setDescription("Downloading...")
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED) // 나중에 주석처리 할 것
                .setAllowedOverMetered(true)
                .setDestinationInExternalFilesDir(
                    applicationContext,
                    "Calibration_file",
                    modelCalibration
                )

            val dm = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
            dm.enqueue(request)

        }.addOnFailureListener {
            Log.d("DownFail", "Url download fails... Use defalut calibration file")
        }

        /*
        Todo
        1. RegressionHelper에서 모델 로드할 때, 파일 없으면 try / catch로 에러 발생
        2. 다운로드가 끝날때까지 하염없이 기다릴 것인가?
        3. timeout을 걸어서, 그 동안에 안 되면 멈추고 메세지를 낼 것인가?
          -- "다운로드가 실패했습니다. 안정적인 인터넷을... 확인..."
          timeout은 다운로드 메니저에 기능이 있을 듯.. callback이든 처음에 옵션이든..
        */

        Log.d("downsuccess", "")

        println(applicationContext.getExternalFilesDir("Calibration_file"))
        println(downloadedFile)
        println(downloadedFile.exists())

        modelsViewModel.insert(
            Models(null, prodName, lotNum, date, hash, downloadedURI.toString())
        )

//        val modelPredict = "230213_new_regression_float16.tflite"
//        modelsViewModel.insert(
//            Models(null, "Bovine_IgG", 20230009, "20230226","a3dcex2745", Uri.fromFile(File(modelPredict)).toString())
//        )

        // ToDo: modelsViewModel.updateCalibUri(hash) 하기 전엔 modelViewMdoel은 빈 string.
        //  -> 다른 곳에서 파일 체크하고 업데이트 해보자...
//        modelsViewModel.updateCalibUri(hash)
    }

    override fun onResume() {
        super.onResume()
        // Make sure that all permissions are still present, since the
        // user could have removed them while the app was in paused state.
//        if (!PermissionsFragment.hasPermissions(this)) {
//            Navigation.findNavController(requireActivity(), R.id.fragment_container)
//                .navigate(CameraFragmentDirections.actionCameraToPermissions())
//        }
//        if (!PermissionsFragment.hasPermissions(this)) {
//            CameraFragmentDirections.actionCameraToPermissions()
//        }

        // update Product name
        val productName = viewBinding.productNameInfo //viewBinding?.findViewById<TextView>(R.id.productNameInfo)
        val myPrdName = String.format("Product Name  %s", MyEntryPoint.prefs.getString("prodName", "AniCheck-bIgG"))
        productName.text = myPrdName

        val lotNum = viewBinding.lotNumber
        lotNum.text = String.format("Lot No.  %s", MyEntryPoint.prefs.getString("lotNum", "00000"))
    }

    override fun onDestroy() {
        super.onDestroy()
        // Shut down our background executor
        cameraExecutor.shutdown()
    }

    //override fun onCreate(savedInstanceState: Bundle?){
//    (
//        inflater: LayoutInflater,
//        container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View {
//        viewBinding = viewBinding.inflate(inflater, container, false)
//
//        binding!!.examineHistoryButton.setOnClickListener {
//            startActivity(Intent(this, HistoryActivity::class.java))
//        }
//
//        binding!!.kitListButton.setOnClickListener {
//            startActivity(Intent(this, SelectActivity::class.java))
//        }
//
//        binding!!.profileImageView.setOnClickListener {
//            startActivity(Intent(this, MemberActivity::class.java))
//        }
//
//        return viewBinding.root
//    }

//    override fun onDestroy() {
//        super.onDestroy()
//        binding = null
//    }

//    @SuppressLint("MissingPermission")
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//
//        objectDetectorHelper = ObjectDetectorHelper(
//            context = this,
//            objectDetectorListener = this
//        )
//
//        // Attach listeners to UI control widgets
//        //initBottomSheetControls()
//    }

    // Initialize CameraX, and prepare to bind the camera use cases
    private fun setUpCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener(
            {
                // CameraProvider
                cameraProvider = cameraProviderFuture.get()

                // Build and bind the camera use cases
                bindCameraUseCases()
            },
            ContextCompat.getMainExecutor(this)
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
                .setTargetRotation(viewBinding.viewFinder.display.rotation)
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
                .setTargetRotation(viewBinding.viewFinder.display.rotation)
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .setOutputImageFormat(OUTPUT_IMAGE_FORMAT_RGBA_8888)
                // Only one image at a time
                //.setBackpressureStrategy(ImageAnalysis.STRATEGY_BLOCK_PRODUCER).setImageQueueDepth(8)
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
                        detectObjects(image)//, false) // Not for capture
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
            preview?.setSurfaceProvider(viewBinding.viewFinder.surfaceProvider)
        } catch (exc: Exception) {
            Log.e(TAG, "Use case binding failed", exc)
        }
        MyEntryPoint.prefs.setCnt("count1", 0)
        MyEntryPoint.prefs.setCnt("count2", 0)
    }

    private var detect1Ready = false
    private var cap2 = false

    override fun onStart() {
        super.onStart()
        detect1Ready = false
        cap2 = false

    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        imageAnalyzer?.targetRotation = viewBinding.viewFinder.display.rotation
    }

    override fun onError(error: String) {
        Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
    }

    override fun onInitialized() {
        objectDetectorHelper.setupObjectDetector()
        // Initialize our background executor


        // Wait for the views to be properly laid out
        viewBinding.viewFinder.post {
            // Set up the camera and its use cases
            setUpCamera()
        }
    }

    /*
    Capture image into memory and perform downstream tasks
     */
    private fun captureAndDetect() {
        if (!::imageCapture.isInitialized) {
            println("not init capture...")
            return
        }

        println("!%%%%%%%%%%%%%%%%%%%%% In captureAndDetect")
        val cameraController = camera?.cameraControl
        val autoExposurePoint = SurfaceOrientedMeteringPointFactory(0.2f, 0.5f).createPoint(.5f, .51f)
//        val autoExposurePoint2 = SurfaceOrientedMeteringPointFactory(0.1f, 0.3f).createPoint(.49f, .52f)
//        val autoExposurePoint3 = SurfaceOrientedMeteringPointFactory(0.1f, 0.3f).createPoint(.5f, .49f)
//        val autoExposurePoint4 = SurfaceOrientedMeteringPointFactory(0.1f, 0.3f).createPoint(.51f, .48f)
//        val autoExposurePoint5 = SurfaceOrientedMeteringPointFactory(0.1f, 0.3f).createPoint(.5f, .47f)

        val action1 = FocusMeteringAction.Builder(autoExposurePoint).build()
//        val action2 = FocusMeteringAction.Builder(autoExposurePoint2).build()
//        val action3 = FocusMeteringAction.Builder(autoExposurePoint3).build()
//        val action4 = FocusMeteringAction.Builder(autoExposurePoint4).build()
//        val action5 = FocusMeteringAction.Builder(autoExposurePoint5).build()
//            .addPoint(autoExposurePoint2)
        val cameraInfo = camera?.cameraInfo

        //cameraController?.setExposureCompensationIndex(-5)

        for (i in 1..5){
            println("$i - captureAndDetect cnt1: ${MyEntryPoint.prefs.getCnt("count1", 0)}")
            println("   - captureAndDetect cnt2: ${MyEntryPoint.prefs.getCnt("count2", 0)}")
            cameraController?.startFocusAndMetering(action1) // ToDo: Action List로 수정

            imageCapture.takePicture(cameraExecutor,
                object :  ImageCapture.OnImageCapturedCallback() {
                    override fun onCaptureSuccess(imageC: ImageProxy) {
                        println("caminfo: ${cameraInfo?.exposureState?.exposureCompensationIndex}")

                        val imageRotation = imageC.imageInfo.rotationDegrees
                        // Pass Bitmap and rotation to the object detector helper for processing and detection
                        val bitmap = imageProxyToBitmap(imageC)
                        imageC.close()
                        println("bitmap?: $bitmap")

                        objectDetectorHelper.detectSecond(bitmap!!, imageRotation)
                    }
                }
            )
        }
        println("captureAndDetect Loop is Over. Did you see the Result??")
    }

    /*
    objectDetectorHelper.detectSecond ->
        objectDectorListener.onSecondResult()
     */

    override fun onSecondResult(
        image: Bitmap,
        results: MutableList<Detection>?,
        imageHeight: Int,
        imageWidth: Int
    ) {
        //var uri: Uri
        println("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% SECOND RESULTS>>>>>>>>>>>>>>>>")
        runOnUiThread {
            println("results??? -> ${results?.size?:100}") // todo delete it later
            if (results != null && results.size > 0 ) {
                println("get in results!!! $results")
                if (MyEntryPoint.prefs.getCnt("count2", 0) == 0) {
                    Toast.makeText(this, "Hold on for a second", Toast.LENGTH_LONG).show()
                }
                val i = results[0]
                if (i.categories[0].score > 0.96) {
                    MyEntryPoint.prefs.increaseOneCnt("count1")
                    MyEntryPoint.prefs.increaseOneCnt("count2")
                    println("@@@getcnt@@@ ${MyEntryPoint.prefs.getCnt("count2", 0)}")
                    //savePictureToMemory(image, i.boundingBox!!)
                    //savePicture to storage
                    val cropped: Bitmap = cutBbox(rotate(image, 90f), i.boundingBox)
                    val absolutePath = imageSaver(cropped)

                    if (MyEntryPoint.prefs.getCnt("count2", 0) >= 5){
                        println("come in 5 points")

                        absolutePath.let {
                            val resultIntent = Intent(this, ResultActivity::class.java)
                            resultIntent.putExtra("imagePath", it)
                            startActivity(resultIntent)
                        }
                        // Reset everything
                        cap2 = false
                        imageCaptureDetectionFail = 0
                        detect1Ready = false
                        MyEntryPoint.prefs.setCnt("count2", 0)
                        //carryOn(image, i.boundingBox!!)
                        finish()
                    }
                } else {
                    imageCaptureDetectionFail += 1
                }
                if (MyEntryPoint.prefs.getCnt("count2", 0) < 5) cap2 = true
                if (imageCaptureDetectionFail > 5) {
                    Toast.makeText(this, "try again...", Toast.LENGTH_SHORT).show()
                    //refreshFragment(context)
                }// RESET
            }
        }
        println("CCCCCCCCCCCC OnSecondResult Done")
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

            return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
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

            val yuvImage =
                YuvImage(nv21, ImageFormat.NV21, imageProxy.width, imageProxy.height, null)
            val out = ByteArrayOutputStream()
            yuvImage.compressToJpeg(Rect(0, 0, yuvImage.width, yuvImage.height), 100, out)
            val imageBytes = out.toByteArray()

            return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
        }
        return null
    }
//    private var contentUri: Uri? = null

    private fun cutBbox(bitmap: Bitmap, bbox: RectF, factorWidth: Float = 1f, factorHeight: Float = 1f): Bitmap {
        val width = bbox.width()
        val height = bbox.height()
        println("WIDTH and HEIGHT $width $height")
        println("bcx, bcy, ${bbox.centerX()} ${bbox.centerY()}")
        return Bitmap.createBitmap(bitmap,
            kotlin.math.ceil((bbox.centerX() - 0.525 * width) * factorWidth).toInt(),
            kotlin.math.ceil((bbox.centerY() - 0.525 * height)*factorHeight).toInt(),
            kotlin.math.ceil(1.05*width*factorWidth).toInt(),
            (height*factorHeight).toInt())
    }

    private fun rotate(bitmap: Bitmap, degrees: Float): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(degrees)
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }
     private fun imageSaver(bitmap: Bitmap): String{
        val photoFile = File(
            getOutputDirectory(this),
            "img${MyEntryPoint.prefs.getCnt("count2", 0)}.png"
//            SimpleDateFormat(
//                FILENAME_FORMAT, Locale.KOREA
//            ).format(System.currentTimeMillis()) + ".png"
        )

        val fileOutputStream = FileOutputStream(photoFile) //location of the image
        //val uri = Uri.fromFile(photoFile)

        bitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream)
        //bitmap.recycle() //
        return photoFile.parent!!
    }

    private fun carryOn(bitmap: Bitmap, bbox: RectF){
        //var bitmap =
        val cropped: Bitmap = cutBbox(rotate(bitmap, 90f), bbox)
        //contentUri = imageSaver(cropped)
        val absolutePath = imageSaver(cropped)
        absolutePath.let {
            val resultIntent = Intent(this, ResultActivity::class.java)
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

    private fun detectObjects(image: ImageProxy) {
        // Copy out RGB bits to the shared bitmap buffer
        if (detect1Ready){
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
            objectDetectorHelper.detect(bitmapBufferCapture, imageRotation)
        }
        else
        {
            //image.use { bitmapBuffer.copyPixelsFromBuffer(image.planes[0].buffer) }
            // Not closing the image here (by not using .use) lets me to chose
            // whether to block the next frame or not.
            bitmapBuffer.copyPixelsFromBuffer(image.planes[0].buffer)
            val imageRotation = image.imageInfo.rotationDegrees
            //println("DETECTOR1 ----")
            if (MyEntryPoint.prefs.getCnt("count1", 0) <= 8){
                image.close()
                objectDetectorHelper.detect(bitmapBuffer, imageRotation)
            } else {
                // simply ignore the stream
                image.close()
            }
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
        runOnUiThread {
            // Pass necessary information to OverlayView for drawing on the canvas
            viewBinding.overlay.setResults(
                results ?: LinkedList<Detection>(),
                imageHeight,
                imageWidth
            )

            if (results != null && results.size > 0 ) {
                val i = results[0]
                if (i.categories[0].score > 0.92) {
                    if (MyEntryPoint.prefs.getCnt("count1", 0) >= 7) {
                        Toast.makeText(this, "Ready to Capture", Toast.LENGTH_SHORT).show()
                        detect1Ready = true
                        captureAndDetect()
                        // No more captureAndDetect!
                        MyEntryPoint.prefs.setCnt("count1", 0)
// thread inside captureAndDetect
// and this Main thread are not synced.
// Below setCnt(0) will be effective before captureAndDetect() is over.
                        //ObjectDetectorHelper.clearObjectDetector()
                    } else if (MyEntryPoint.prefs.getCnt("count1", 0) < 7){
                        if (i.boundingBox.height() < 0.25*imageHeight){
                            MyEntryPoint.prefs.setCnt("count1", 0)
                            println("RESET CNT  1 ... ${MyEntryPoint.prefs.getCnt("count1", 0)}")
                        }
                        else if  (i.boundingBox.height() > 0.7*imageHeight){
                            MyEntryPoint.prefs.setCnt("count1", 0)
                            println("RESET CNT  2 ... ${MyEntryPoint.prefs.getCnt("count1", 0)}")
                        }
                        else {
                            MyEntryPoint.prefs.increaseOneCnt("count1")
                            println("INCREMENT CNT ... ${MyEntryPoint.prefs.getCnt("count1", 0)}")
                        }
                    } else {
                        /* Some frames will stream in
                        even after cnt == 7 is reached. ?
                        I still have to close the image, or ...?
                        */

                    }
                } else {
                    MyEntryPoint.prefs.setCnt("count1", 0)
                }
                println("CURRENT CNT ${MyEntryPoint.prefs.getCnt("count1", 0)}")
            }
            viewBinding.overlay.invalidate()
        }
    }

//    private fun refreshFragment(context: Context?){
//        context?.let{
//            val fragmentManager = ( context as? AppCompatActivity)?.supportFragmentManager
//            fragmentManager?. let {
//                // Todo: fragment ID 확인
//                val currentFragment = fragmentManager.findFragmentByTag("cameraFragment")//  findFragmentById(this)
//                currentFragment?.let{
//                    // Reset everything
//                    MyEntryPoint.prefs.setCnt(0)
//                    cap2 = false
//                    imageCaptureDetectionFail = 0
//                    detetc1Ready = false
//
//                    val fragmentTransaction = fragmentManager.beginTransaction()
//                    fragmentTransaction.detach(it)
//                    fragmentTransaction.attach(it)
//                    fragmentTransaction.commit()
//                }
//            }
//        }
//    }
}