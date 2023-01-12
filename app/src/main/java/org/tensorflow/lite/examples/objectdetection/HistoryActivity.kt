package org.tensorflow.lite.examples.objectdetection

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import org.tensorflow.lite.examples.objectdetection.adapter.History
import org.tensorflow.lite.examples.objectdetection.adapter.HistoryAdapter
import org.tensorflow.lite.examples.objectdetection.databinding.ActivityHistoryBinding

class HistoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHistoryBinding
    private var historyAdapter = HistoryAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initView()

    }

    private fun initView() = with(binding) {
        historyBackButton.setOnClickListener { finish() }
        kitHistoryRecyclerView.adapter = historyAdapter
        historyAdapter.setHistoryList(
            listOf(
                History("2022-10-10", "AniCheck", "20mg/ml", "img1.png"),
                History("2022-10-10", "AniCheck", "30mg/ml", "img2.png")
            )
        )
    }
}