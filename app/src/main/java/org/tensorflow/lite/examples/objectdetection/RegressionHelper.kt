package org.tensorflow.lite.examples.objectdetection

import android.content.Context
import android.graphics.Bitmap
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
        if (setupRegression2()) {
            inputPredictTargetHeight = interpreterPredict!!.getInputTensor(0)
                .shape()[1]
            inputPredictTargetWidth = interpreterPredict!!.getInputTensor(0)
                .shape()[2]
            outputPredictShape = interpreterPredict!!.getOutputTensor(0).shape()
        } else {
            //regressionListener.onError("TFLite failed to init.")
        }
    }
    fun setupRegression(){
        println("SETUP")
    }

    fun setupRegression2():Boolean {
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

    fun predict(image: Bitmap, imageRotation: Int) {
        //interpreterPredict.run { bitmap }

        val imageProcessor = ImageProcessor.Builder().add(Rot90Op(-imageRotation / 90)).build()

        // Preprocess the image and convert it into a TensorImage for detection.
        //val tensorImage = imageProcessor.process(TensorImage.fromBitmap(image))

        val predictOutput = TensorBuffer.createFixedSize(
            outputPredictShape, DataType.FLOAT32
        )
        interpreterPredict?.run(image, predictOutput)//tensorImage)
    }

    fun clearRegressionHelper() {
        interpreterPredict = null
    }

    private fun processInputImage(
        image: Bitmap,
        targetWidth: Int,
        targetHeight: Int
    ): TensorImage? {
        val height = image.height
        val width = image.width
        val cropSize = min(height, width)
        val imageProcessor = ImageProcessor.Builder()
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