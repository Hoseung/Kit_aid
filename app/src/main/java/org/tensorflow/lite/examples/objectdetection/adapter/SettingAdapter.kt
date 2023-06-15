package org.tensorflow.lite.examples.objectdetection.adapter

import android.app.Activity
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.tensorflow.lite.examples.objectdetection.MyEntryPoint
import org.tensorflow.lite.examples.objectdetection.databinding.ItemSettingBinding

class SettingViewHolder(val binding: ItemSettingBinding) : RecyclerView.ViewHolder(binding.root)

class SettingAdapter(val data: MutableList<String>): RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    override fun getItemCount(): Int {
        return data.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {



        return SettingViewHolder(ItemSettingBinding.inflate(
            LayoutInflater.from(parent.context), parent,
            false
        ))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val binding = (holder as SettingViewHolder).binding
        binding.settingText.text = data[position]

        // setting init value of switch
        val calibOn = MyEntryPoint.prefs.getString("calibOn", "true")
        binding.calibrationSwitch.isChecked =calibOn.toBoolean()

        // click event handler
        binding.calibrationSwitch.setOnClickListener {
            MyEntryPoint.prefs.setString(
                "calibOn",
                (!MyEntryPoint.prefs.getString("calibOn", "error")
                    .toBoolean()).toString()
            )
        }
        println("calobon: ${MyEntryPoint.prefs.getString("calibOn", "error")}")
    }
}