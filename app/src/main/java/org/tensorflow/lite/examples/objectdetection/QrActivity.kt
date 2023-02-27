package org.tensorflow.lite.examples.objectdetection

import android.Manifest
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.camera.mlkit.vision.MlKitAnalyzer
import androidx.camera.view.CameraController.COORDINATE_SYSTEM_VIEW_REFERENCED
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import org.tensorflow.lite.examples.objectdetection.adapter.Models
import org.tensorflow.lite.examples.objectdetection.adapter.ModelsViewModel
import org.tensorflow.lite.examples.objectdetection.adapter.ModelsViewModelFactory
import org.tensorflow.lite.examples.objectdetection.databinding.ActivityQrBinding
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class QrActivity : AppCompatActivity() {
    private lateinit var viewBinding: ActivityQrBinding
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var barcodeScanner: BarcodeScanner

    private val modelsViewModel: ModelsViewModel by viewModels {
        ModelsViewModelFactory((application as MyEntryPoint).database.modelsDao())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityQrBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)


//        // Request camera permissions
//        if (!PermissionsFragment.hasPermissions(this)) {
//            CameraFragmentDirections.actionCameraToPermissions()
//        }
        // Request camera permissions
        if (allPermissionsGranted()) {
            Toast.makeText(this, "Touch the QR on the screen", Toast.LENGTH_LONG).show()
            startCamera()
        } else {
            ActivityCompat.requestPermissions(
                this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS
            )
        }

        cameraExecutor = Executors.newSingleThreadExecutor()
    }

    private fun startCamera() {
        var cameraController = LifecycleCameraController(baseContext)
        val previewView: PreviewView = viewBinding.viewFinder

        val options = BarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
            .build()
        barcodeScanner = BarcodeScanning.getClient(options)

        cameraController.setImageAnalysisAnalyzer(
            ContextCompat.getMainExecutor(this),
            MlKitAnalyzer(
                listOf(barcodeScanner),
                COORDINATE_SYSTEM_VIEW_REFERENCED,
                ContextCompat.getMainExecutor(this)
            ) { result: MlKitAnalyzer.Result? ->
                val barcodeResults = result?.getValue(barcodeScanner)
                if ((barcodeResults == null) ||
                    (barcodeResults.size == 0) ||
                    (barcodeResults.first() == null)
                ) {
                    previewView.overlay.clear()
                    previewView.setOnTouchListener { _, _ -> false } //no-op
                    return@MlKitAnalyzer
                }

                val qrCodeViewModel = QrCodeViewModel(barcodeResults[0])
                val qrCodeDrawable = QrCodeDrawable(qrCodeViewModel)

                previewView.setOnTouchListener(qrCodeViewModel.qrCodeTouchCallback)
                previewView.overlay.clear()
                previewView.overlay.add(qrCodeDrawable)
            }
        )

        cameraController.bindToLifecycle(this)
        previewView.controller = cameraController
    }

    // Todo: save file to 'app internal' storage.
    // https://developer.android.com/training/data-storage/app-specific
    var downloadID : Long = 0
    private fun downloadMissingCalibration(){
        val missingCalib = "20230003"
        val missingCalibModel = "BovineIgG"

        var request = DownloadManager.Request(
            Uri.parse("https://프로테옴텍/서버/주소/정해진/위치/${missingCalibModel}_${missingCalib}.dat"))
            .setDescription("calibration model downloading")
            .setAllowedOverMetered(true)

        var dm = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        downloadID = dm.enqueue(request)

        var br = object:BroadcastReceiver(){
            override fun onReceive(p0: Context?, p1: Intent?) {
                var id = p1?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
                if(id==downloadID){
                    Toast.makeText(applicationContext, "calibration model is ready!", Toast.LENGTH_LONG).show()
                }
            }
        }
        registerReceiver(br, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))

    }


    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
        barcodeScanner.close()
    }

    companion object {
        private const val TAG = "CameraX-MLKit"
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS =
            mutableListOf (
                Manifest.permission.CAMERA
            ).toTypedArray()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults:
        IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                Toast.makeText(this,
                    "Permissions not granted by the user.",
                    Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }
}