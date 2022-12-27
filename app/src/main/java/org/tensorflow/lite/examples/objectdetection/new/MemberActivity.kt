package org.tensorflow.lite.examples.objectdetection.new

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.appcompat.app.AppCompatActivity
import org.tensorflow.lite.examples.objectdetection.databinding.ActivityMemberBinding

class MemberActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMemberBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMemberBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initView()

    }

    private fun initView() = with(binding) {
        memberBackButton.setOnClickListener { finish() }
        nameEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                nameCountTextView.text = p0.toString()
            }

            override fun afterTextChanged(p0: Editable?) {}

        })
    }
}