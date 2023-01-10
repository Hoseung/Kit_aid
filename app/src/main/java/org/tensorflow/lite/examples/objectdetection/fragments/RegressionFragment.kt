package org.tensorflow.lite.examples.objectdetection.fragments


import android.graphics.Bitmap
import android.widget.Toast
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.fragment.app.Fragment
import org.tensorflow.lite.examples.objectdetection.RegressionHelper
import org.tensorflow.lite.task.gms.vision.detector.Detection
import java.util.*

class RegressionFragment : Fragment(),
    RegressionHelper.RegressionListener {

    private lateinit var regressionHelper: RegressionHelper

    override fun onError(error: String) {
        activity?.runOnUiThread {
            Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResult(
        result: Float,
    ) {
        //var uri: Uri
        activity?.runOnUiThread {

            if (result != null) {
                println(result)
            }
            //fragmentCameraBinding.overlay.invalidate()
        }
    }
}