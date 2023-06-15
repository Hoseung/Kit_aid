package org.tensorflow.lite.examples.objectdetection

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import org.tensorflow.lite.examples.objectdetection.adapter.SettingAdapter
import org.tensorflow.lite.examples.objectdetection.databinding.ActivitySettingBinding
import org.tensorflow.lite.examples.objectdetection.databinding.ItemSettingBinding

class SettingActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivitySettingBinding.inflate(layoutInflater)
        val itemSettingBinding = ItemSettingBinding.inflate(layoutInflater)

        // setting data
        val listData = mutableListOf<String>()
        listData.add("Calibration")

        binding.settingView.layoutManager = LinearLayoutManager(this)
        binding.settingView.adapter = SettingAdapter(listData)
        binding.settingView.addItemDecoration(DividerItemDecoration(this, LinearLayoutManager.VERTICAL))

        // click event handler
        itemSettingBinding.calibrationSwitch.setOnClickListener {
        }

        // update recycler view
//        setContentView(itemSettingBinding.root)
        setContentView(binding.root)
    }
}