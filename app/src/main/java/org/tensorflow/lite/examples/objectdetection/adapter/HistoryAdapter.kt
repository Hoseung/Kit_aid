package org.tensorflow.lite.examples.objectdetection.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.tensorflow.lite.examples.objectdetection.databinding.ItemHistoryBinding

class HistoryAdapter : RecyclerView.Adapter<HistoryAdapter.HistoryItemViewHolder>() {

    private var historyList: List<History> = listOf()

    inner class HistoryItemViewHolder(private val binding: ItemHistoryBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(data: History) = with(binding) {
            // bind to Text views
            itemDateTv.text = data.date
            itemTypeTv.text = data.product
            itemResultTv.text = data.density
            //itemImageNameTv.text = data.imgName
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryItemViewHolder {
        val view = ItemHistoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return HistoryItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: HistoryItemViewHolder, position: Int) {
        holder.bind(historyList[position])
    }

    override fun getItemCount(): Int = historyList.size

    //
    fun setHistoryList(historyList: List<History>) {
        this.historyList = historyList
        notifyDataSetChanged()
    }
}