package org.tensorflow.lite.examples.objectdetection.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Recycler
import kotlinx.coroutines.flow.Flow
import org.tensorflow.lite.examples.objectdetection.R
import org.tensorflow.lite.examples.objectdetection.databinding.ItemHistoryBinding

class HistoryAdapter : RecyclerView.Adapter<HistoryAdapter.HistoryItemViewHolder>() {
//class HistoryAdapter : ListAdapter<History, HistoryAdapter.HistoryItemViewHolder> (HistoryComparator) {

//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryItemViewHolder {
//        return HistoryItemViewHolder.create(parent)
//    }
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryItemViewHolder {
        val view = ItemHistoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return HistoryItemViewHolder(view)
    }
    override fun onBindViewHolder(holder: HistoryItemViewHolder, position: Int) {
        // Associate view holder with data
        //val current = getItem(position)
        holder.bind(historyList[position])
    }


    private var historyList: List<History> = listOf()

    /*
    Bind History entry to TextView
     */
    class HistoryItemViewHolder(private val binding: ItemHistoryBinding) : RecyclerView.ViewHolder(binding.root) {
    //inner class HistoryItemViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        private val itemDateTv: TextView = itemView.findViewById(R.id.itemDateTv)
        private val itemTypeTv: TextView = itemView.findViewById(R.id.itemTypeTv)
        private val itemResultTv: TextView = itemView.findViewById(R.id.itemResultTv)

        fun bind(data: History) {
            // bind to Text views
            itemDateTv.text = data.date
            itemTypeTv.text = data.product
            itemResultTv.text = data.density
            //itemImageNameTv.text = data.imgName
        }

        //RecyclerView.ViewHolder(binding.root) {
//        companion object {
//            fun create(parent: ViewGroup): HistoryItemViewHolder{
//                val view: View = LayoutInflater.from(parent.context)
//                    .inflate(R.layout.item_history, parent, false)
//                return HistoryItemViewHolder(view)
//            }
//        }
    }

    companion object {
        private val HistoryComparator = object : DiffUtil.ItemCallback<History>()
        {
            override fun areItemsTheSame(oldItem: History, newItem: History): Boolean {
                return oldItem === newItem
            }

            override fun areContentsTheSame(oldItem: History, newItem: History): Boolean {
                return (oldItem.product == newItem.product &&
                        oldItem.lot == newItem.lot &&
                        oldItem.date == newItem.date)
            }
        }
    }

    override fun getItemCount(): Int = historyList.size

    // Use DiffUtil instead.
    fun setHistoryList(historyList: List<History>) {
        this.historyList = historyList.reversed()
        notifyDataSetChanged()
    }

    fun addHistoryList(newHistory: History) {
        this.historyList.plusElement(newHistory)
        notifyDataSetChanged()
    }

}