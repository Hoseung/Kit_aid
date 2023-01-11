package org.tensorflow.lite.examples.objectdetection

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.util.Log
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
import org.tensorflow.lite.support.image.ops.ResizeWithCropOrPadOp
import org.tensorflow.lite.support.image.ops.Rot90Op
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.lang.Integer.min
import java.nio.ByteBuffer
import java.nio.ByteOrder

class RegressionHelper (
    var numThreads: Int = 2,
    var currentDelegate: Int = 0,
    //var currentModel: Int = 0,
    val context: Context,
    //val regressionListener: RegressionListener
){
    private var interpreterPredict: Interpreter? = null
    private var inputPredictTargetWidth = 0
    private var inputPredictTargetHeight = 0
    private var outputPredictShape = intArrayOf()

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
//    fun setupRegression(){
//        println("SETUP")
//    }

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
        val modelTransfer: String
        val modelPredict = "regression_bovine.tflite"

        try {
            interpreterPredict = Interpreter(
                FileUtil.loadMappedFile(
                    context,
                    modelPredict,
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

    fun predict(image: Bitmap, imageRotation: Int): Float {
        //interpreterPredict.run { bitmap }

        //val imageProcessor = ImageProcessor.Builder().add(Rot90Op(-imageRotation / 90)).build()

        // Preprocess the image and convert it into a TensorImage for detection.

        /*
        val modelOutput2 = TensorBuffer.createFixedSize(
            outputPredictShape, DataType.FLOAT32
        )
        */

        println("RUNNING REGRESSION@@@@!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!")

        val bufferSize = java.lang.Float.SIZE / java.lang.Byte.SIZE
        val modelOutput = ByteBuffer.allocateDirect(bufferSize).order(ByteOrder.nativeOrder())
        val input = processInputImage(image,
            inputPredictTargetWidth,
            inputPredictTargetHeight,
            imageRotation)

        println("GOGOGOGOGOGOGOGOGOGOGOOGOOGOG")

        interpreterPredict?.run(input?.buffer, modelOutput)
        println("INFERENCE DONE~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~")
        //val answer = modelOutput.floatArray.toString()

        modelOutput.rewind()
        val answer = modelOutput.asFloatBuffer().get()

        println("DONE ${answer}")
        println("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX")

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
        val cropSize = min(height, width)
        val imageProcessor = ImageProcessor.Builder()
            .add(Rot90Op(-imageRotation / 90))
            .add(ResizeWithCropOrPadOp(cropSize, cropSize))
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

    interface RegressionListener {
        fun onError(error: String)
        fun onResult(results: Float)
        fun onInitialized()
    }

    companion object {
        const val DELEGATE_CPU = 0
        const val DELEGATE_GPU = 1
        const val DELEGATE_NNAPI = 2
        const val MODEL_INT8 = 0

        private const val TAG = "Regression Helper"
    }
}