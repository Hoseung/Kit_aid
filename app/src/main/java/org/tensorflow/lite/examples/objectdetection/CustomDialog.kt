package org.tensorflow.lite.examples.objectdetection

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import org.tensorflow.lite.examples.objectdetection.databinding.DialogBinding

class CustomDialog(context: Context): Dialog(context) {

    private lateinit var binding: DialogBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DialogBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.dialogYesBtn.setOnClickListener {

        }

    }
}