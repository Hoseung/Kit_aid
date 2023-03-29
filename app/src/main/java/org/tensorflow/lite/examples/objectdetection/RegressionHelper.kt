package org.tensorflow.lite.examples.objectdetection

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
//import android.net.Uri
import android.util.Log
import androidx.activity.viewModels
import org.tensorflow.lite.DataType
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.examples.objectdetection.adapter.Models
import org.tensorflow.lite.examples.objectdetection.adapter.ModelsViewModel
import org.tensorflow.lite.examples.objectdetection.adapter.ModelsViewModelFactory
import org.tensorflow.lite.gpu.CompatibilityList
import org.tensorflow.lite.gpu.GpuDelegate
import org.tensorflow.lite.nnapi.NnApiDelegate
import org.tensorflow.lite.support.common.FileUtil
import org.tensorflow.lite.support.common.ops.NormalizeOp
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder
//import org.tensorflow.lite.examples.objectdetection.MainActivity.Companion.getOutputDirectory
//import java.io.File
//import java.io.FileOutputStream
//import java.text.SimpleDateFormat
//import java.util.*

class RegressionHelper (
    private var numThreads: Int = 2,
    private var currentDelegate: Int = 0,
    val context: Context,
    //val regressionListener: RegressionListener
){
    private var interpreterPredict: Interpreter? = null
    private var inputPredictTargetWidth = 0
    private var inputPredictTargetHeight = 0
    private var outputPredictShape = intArrayOf()

//    private val modelsViewModel: ModelsViewModel by viewModels {
//        ModelsViewModelFactory((this.context as MyEntryPoint).database.modelsDao())
//    }

    init {
        if (setupRegression()) {
            inputPredictTargetHeight = interpreterPredict!!.getInputTensor(0)
                .shape()[1]
            inputPredictTargetWidth = interpreterPredict!!.getInputTensor(0)
                .shape()[2]
            outputPredictShape = interpreterPredict!!.getOutputTensor(0).shape()
        } else {
            //regressionListener.onError("TFLite failed to init.")
        }
    }

    fun setupRegression():Boolean {
        val tfliteOption = Interpreter.Options()
        tfliteOption.numThreads = numThreads
        when (currentDelegate) {
            DELEGATE_CPU -> {
                // Default
            }
            DELEGATE_GPU -> {
                if (CompatibilityList().isDelegateSupportedOnThisDevice) {
                    tfliteOption.addDelegate(GpuDelegate())
                } else {
                    //regressionListener.onError("GPU is not supported on this device")
                }
            }
            DELEGATE_NNAPI -> {
                tfliteOption.addDelegate(NnApiDelegate())
            }
        }
        //val modelPredict =

        try {
            interpreterPredict = Interpreter(
                // load a file from the asset folder.
                FileUtil.loadMappedFile(
                    context,
                    "230329_regression_float16.tflite",
                ), tfliteOption
            )
            return true

        } catch (e: Exception) {
            //regressionListener.onError(
            //    "Regression failed to initialize. See error logs for " +
            //            "details"
            //)
            Log.e(TAG, "TFLite failed to load model with error: " + e.message)
            return false
        }
    }

    fun predict(image: Bitmap): Float {
        // Preprocess the image and convert it into a TensorImage for detection.
        val bufferSize = java.lang.Float.SIZE / java.lang.Byte.SIZE
        val modelOutput = ByteBuffer.allocateDirect(bufferSize).order(ByteOrder.nativeOrder())
        val input = processInputImage(image,
            inputPredictTargetWidth,
            inputPredictTargetHeight)
        interpreterPredict?.run(input?.buffer, modelOutput)
        //println("INFERENCE DONE~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~")
        //val answer = modelOutput.floatArray.toString()

        modelOutput.rewind()
        val answer = modelOutput.asFloatBuffer().get()

        //println("DONE ${answer}")
        //println("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX")

        return answer
    }

    private fun processInputImage(
        image: Bitmap,
        targetWidth: Int,
        targetHeight: Int,
    ): TensorImage? {
        val imageProcessor = ImageProcessor.Builder()
            .add(
                ResizeOp(
                    targetHeight,
                    targetWidth,
                    ResizeOp.ResizeMethod.BILINEAR
                )
            )
            .add(NormalizeOp(0f, 255f))
            .build()
        val tensorImage = TensorImage(DataType.FLOAT32) // DataType Check!
        tensorImage.load(image)

        return imageProcessor.process(tensorImage)
    }

//    fun imageSaver(bitmap: Bitmap): String{
//        val photoFile = File(
//            getOutputDirectory(context =context),
//            SimpleDateFormat(
//                "yyyy-MM-dd-HH-mm-ss-SSS", Locale.KOREA
//            ).format(System.currentTimeMillis()) + ".png"
//        )
//
//        val fileOutputStream = FileOutputStream(photoFile) //location of the image
//        val uri = Uri.fromFile(photoFile)
//
//        bitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream)
//        //bitmap.recycle() //
//        return photoFile.absolutePath
//    }
//
//    interface RegressionListener {
//        fun onError(error: String)
//        fun onResult(results: Float)
//        fun onInitialized()
//    }

    companion object {
        const val DELEGATE_CPU = 0
        const val DELEGATE_GPU = 1
        const val DELEGATE_NNAPI = 2

        private const val TAG = "Regression Helper"
    }
}

