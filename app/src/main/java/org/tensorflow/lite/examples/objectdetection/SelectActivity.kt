package org.tensorflow.lite.examples.objectdetection

import android.content.DialogInterface
import android.opengl.Visibility
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AlertDialog
import com.google.firebase.events.EventHandler
import com.google.firebase.storage.ListResult
import org.tensorflow.lite.examples.objectdetection.databinding.ActivitySelectBinding
import com.google.firebase.storage.ktx.component1
import com.google.firebase.storage.ktx.component2
import org.tensorflow.lite.examples.objectdetection.myFirebase.MyFirebase

class SelectActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySelectBinding
    private var loading: Boolean = true
    private var lotIndex = 0
    private lateinit var lotNumList: Array<String>
    private var lotNum :String? =  null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySelectBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // get lotNum
        binding.loadingLayout.visibility = View.VISIBLE
        if (MyEntryPoint.myFirebase.totalFileList == null) {
            MyEntryPoint.myFirebase.getTotalFileList()
            loadingFileCheck()
        } else {
            println("Already got the file list!")
            binding.loadingLayout.visibility = View.GONE
            MyEntryPoint.myFirebase.totalFileList!!.forEach {
                println(it)
            }
        }
        initView()
    }

    private fun initView() = with(binding) {
        // todo 키트 선택 못하면 빠져나가지 못하게 막아야해.
        selectKitButton.setOnClickListener { finish() }
        selectBackButton.setOnClickListener { finish() }

        kit1.setOnClickListener { check(1) }
        kit1Tv.setOnClickListener { check(1) }
        kit1Check.setOnClickListener { check(1) }

        kit2.setOnClickListener { check(2) }
        kit2Tv.setOnClickListener { check(2) }
        kit2Check.setOnClickListener { check(2) }

        kit3.setOnClickListener { check(3) }
        kit3Tv.setOnClickListener { check(3) }
        kit3Check.setOnClickListener { check(3) }

        //val myTextView = findViewById<TextView>(R.id.productNameInfo)
        //val myTextView = findViewById<TextView>(R.id.resultText)
        //myTextView.text = R.string.product_1.toString()
    }

    private fun check(number: Int) = with(binding) {

        when (number) {
            1 -> {
                kit1Check.isChecked = true
                kit2Check.isChecked = false
                kit3Check.isChecked = false

                val prodName = getString(R.string.product_1)
                MyEntryPoint.prefs.setString("prodName", prodName)
                setLotNum(prodName)
            }
            2 -> {
                kit1Check.isChecked = false
                kit2Check.isChecked = true
                kit3Check.isChecked = false

                val prodName = getString(R.string.product_2)
                MyEntryPoint.prefs.setString("prodName", prodName)
                setLotNum(prodName)

            }
            3 -> {
                kit1Check.isChecked = false
                kit2Check.isChecked = false
                kit3Check.isChecked = true

                val prodName = getString(R.string.product_3)
                MyEntryPoint.prefs.setString("prodName", prodName)
                setLotNum(prodName)
            }
        }
    }

    private fun loadingFileCheck() {
        Thread {
            while (loading) {
                println("go on and on...")
                if (MyEntryPoint.myFirebase.totalFileList != null){
                    loading = false
                    MyEntryPoint!!.myFirebase.totalFileList?.forEach {
                        println(it)
                    }

                    // disable progressbar layout
                    runOnUiThread {
                        binding.loadingLayout.visibility = View.GONE
                    }
                    break
                }
                Thread.sleep(1000)
            }
        }.start()
    }

    private fun setLotNum(prodName:String) {
        MyEntryPoint.myFirebase.arrangeFileList()
        when (prodName) {
            "AniCheck-bIgG" -> {
                lotNumList = MyEntryPoint.myFirebase.datListBIgG.toTypedArray()
            }
            "ImmuneCheck-IgE" -> {
                lotNumList = MyEntryPoint.myFirebase.datListIgE.toTypedArray()
            }
            "ImmuneCheck-IgG" -> {
                lotNumList = MyEntryPoint.myFirebase.datListIgG.toTypedArray()
            }
        }

        AlertDialog.Builder(this).run{
            setTitle("Choose lot number")

            if (lotNumList.isEmpty()) {
                setMessage("There is no Calibration file on server. Please contact app developer.")
                lotIndex = 0
            }

            setSingleChoiceItems(lotNumList, lotIndex, object: DialogInterface.OnClickListener{
                override fun onClick(dialog: DialogInterface?, which: Int) {
                    if (which != -1) {
                        lotIndex = which
                    }
                }
            })

            val eventHandler = object : DialogInterface.OnClickListener {

                override fun onClick(dialog: DialogInterface?, which: Int) {
                    if (!lotNumList.isEmpty()) {
                        println(lotNumList[lotIndex])
                        lotNum = lotNumList[lotIndex]
                        MyEntryPoint.prefs.setString("lotNum", lotNum!!)
                        binding.lotNumText.text = "※ Selected Lot: $lotNum"
                    }
                }
            }
            setPositiveButton("OK", eventHandler)
            show()
        }
    }
}