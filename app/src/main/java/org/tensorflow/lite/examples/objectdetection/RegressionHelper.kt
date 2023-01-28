package org.tensorflow.lite.examples.objectdetection

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.util.Log
import androidx.core.content.ContentProviderCompat.requireContext
import org.tensorflow.lite.DataType
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.gpu.CompatibilityList
import org.tensorflow.lite.gpu.GpuDelegate
import org.tensorflow.lite.nnapi.NnApiDelegate
import org.tensorflow.lite.support.common.FileUtil
import org.tensorflow.lite.support.common.ops.NormalizeOp
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
//import org.tensorflow.lite.support.image.ops.ResizeWithCropOrPadOp
//import org.tensorflow.lite.support.image.ops.Rot90Op
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.lang.Integer.min
import java.nio.ByteBuffer
import java.nio.ByteOrder
import org.tensorflow.lite.examples.objectdetection.MainActivity.Companion.getOutputDirectory
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class RegressionHelper (
    var numThreads: Int = 2,
    var currentDelegate: Int = 0,
    val context: Context,
    //val regressionListener: RegressionListener
){
    private var interpreterPredict: Interpreter? = null
    private var inputPredictTargetWidth = 0
    private var inputPredictTargetHeight = 0
    private var outputPredictShape = intArrayOf()

    // Custom detector
    private var outputProbabilityBuffer = TensorBuffer.createDynamic(DataType.FLOAT32)
    init {
        if (setupRegression()) {
            inputPredictTargetHeight = interpreterPredict!!.getInputTensor(0).shape()[1]
            inputPredictTargetWidth = interpreterPredict!!.getInputTensor(0).shape()[2]
            outputPredictShape = interpreterPredict!!.getOutputTensor(0).shape()

        // Custom detector
            // 이 자체는 문제 없음. 실제 output과 모양이 안 맞을 뿐?
            // Regression 모델도 [1,1] 모양이 있는데 왜 그거랑은 안 맞을까?
            outputProbabilityBuffer = TensorBuffer.createFixedSize(
                interpreterPredict!!.getOutputTensor(0).shape(),
                interpreterPredict!!.getOutputTensor(0).dataType())
        } else {
            //regressionListener.onError("TFLite failed to init.")
        }
    }

    fun setupRegression():Boolean {
        val tfliteOption = Interpreter.Options()
        tfliteOption.numThreads = numThreads
        println("SJKDJFLDSKJFDSLJF")
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
        val modelPredict = "230117_regression_wb.tflite"//"230126_detection_ver2.tflite"

        try {
            interpreterPredict = Interpreter(
                FileUtil.loadMappedFile(
                    context,
                    modelPredict,
                ), tfliteOption
            )
            return true

        } catch (e: Exception) {
//            regressionListener.onError(
//                "Regression failed to initialize. See error logs for " +
//                        "details"
//            )
            Log.e(TAG, "TFLite failed to load model with error: " + e.message)
            return false
        }
    }

    fun predict(image: Bitmap, imageRotation: Int): Float {
        //val imageProcessor = ImageProcessor.Builder().add(Rot90Op(-imageRotation / 90)).build()

        // Preprocess the image and convert it into a TensorImage for detection.

        val bufferSize = java.lang.Float.SIZE / java.lang.Byte.SIZE
        val modelOutput = ByteBuffer.allocateDirect(bufferSize).order(ByteOrder.nativeOrder())

        val absolutePath = imageSaver(image)
        val input = processInputImage(image,
            inputPredictTargetWidth,
            inputPredictTargetHeight,
            imageRotation)

        println("INFERENCE DONE~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~")
        // custom detector
        //interpreterPredict?.run(input?.buffer, outputProbabilityBuffer))
        interpreterPredict?.run(input?.buffer, modelOutput)
        println("INFERENCE DONE~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~")

        modelOutput.rewind()
        val answer = modelOutput.asFloatBuffer().get()

        // custom detector
//        val answer = outputProbabilityBuffer.getFloatValue(1)
        println("XXXXXXXXXXXXXXXXX DONE ${answer}")

        return answer
    }

    fun clearRegressionHelper() {
        interpreterPredict = null
    }

    private fun processInputImage(
        image: Bitmap,
        targetWidth: Int,
        targetHeight: Int,
        imageRotation: Int
    ): TensorImage? {
        val height = image.height
        val width = image.width
        val imageProcessor = ImageProcessor.Builder()
            //.add(Rot90Op(-imageRotation / 90))
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

        println("TENSOR IMAGE READYYYYYYYYYYYYYYYYYYYYYYYYY")
        return imageProcessor.process(tensorImage)
    }

    fun imageSaver(bitmap: Bitmap): String{
        val photoFile = File(
            getOutputDirectory(context =context),
            SimpleDateFormat(
                "yyyy-MM-dd-HH-mm-ss-SSS", Locale.KOREA
            ).format(System.currentTimeMillis()) + ".png"
        )

        val fileOutputStream = FileOutputStream(photoFile) //location of the image
        val uri = Uri.fromFile(photoFile)

        bitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream)
        //bitmap.recycle() //
        return photoFile.absolutePath
    }

    interface RegressionListener {
        fun onError(error: String)
        fun onResult(results: Float)
        fun onInitialized()
    }

    companion object {
        const val DELEGATE_CPU = 0
        const val DELEGATE_GPU = 1
        const val DELEGATE_NNAPI = 2

        private const val TAG = "Regression Helper"
    }
}

