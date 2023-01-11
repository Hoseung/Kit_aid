package org.tensorflow.lite.examples.objectdetection

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
//import android.widget.CheckBox
//import android.widget.ImageView
//import android.widget.TextView
//import org.tensorflow.lite.examples.objectdetection.R
//import org.tensorflow.lite.examples.objectdetection.databinding.ActivityLoginBinding
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

    }

    private fun check(number: Int) = with(binding) {
        when (number) {
            1 -> {
                kit1Check.isChecked = true
                kit2Check.isChecked = false
                kit3Check.isChecked = false
            }
            2 -> {
                kit1Check.isChecked = false
                kit2Check.isChecked = true
                kit3Check.isChecked = false
            }
            3 -> {
                kit1Check.isChecked = false
                kit2Check.isChecked = false
                kit3Check.isChecked = true
            }
        }
    }
}