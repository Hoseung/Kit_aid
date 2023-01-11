package org.tensorflow.lite.examples.objectdetection

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import org.tensorflow.lite.examples.objectdetection.databinding.ActivitySelectBinding

class SelectActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySelectBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySelectBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initView()

    }

    private fun initView() = with(binding) {
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
                MyEntryPoint.prefs.setString("prodName", "Bovine IgG")
                MyEntryPoint.prefs.setString("lotNum", "220001")
            }
            2 -> {
                kit1Check.isChecked = false
                kit2Check.isChecked = true
                kit3Check.isChecked = false
                MyEntryPoint.prefs.setString("prodName", "ImmuneCheck IgE")
                MyEntryPoint.prefs.setString("lotNum", "220002")

            }
            3 -> {
                kit1Check.isChecked = false
                kit2Check.isChecked = false
                kit3Check.isChecked = true
                MyEntryPoint.prefs.setString("prodName", "ImmuneCheck IgG")
                MyEntryPoint.prefs.setString("lotNum", "220003")

            }
        }
    }
}