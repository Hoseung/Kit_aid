package org.tensorflow.lite.examples.objectdetection

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import org.tensorflow.lite.examples.objectdetection.adapter.History
import org.tensorflow.lite.examples.objectdetection.adapter.HistoryAdapter
import org.tensorflow.lite.examples.objectdetection.adapter.HistoryViewModel
//import org.tensorflow.lite.examples.objectdetection.HistoryRoomDataBase
//import org.tensorflow.lite.examples.objectdetection.adapter.HistoryViewModelFactory
import org.tensorflow.lite.examples.objectdetection.databinding.ActivityHistoryBinding

class HistoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHistoryBinding
    // Adapter = 데이터 표현용
    // DAO = DB access용

    private var historyAdapter = HistoryAdapter()
    private var historyDB: HistoryRoomDatabase?= null

    //historyDB = HistoryRoomDataBase.getDatabase(this,)

    val applicationScope = CoroutineScope(SupervisorJob())
    //val database by lazy { HistoryRoomDatabase.getDatabase(this, applicationScope)}
    //private val repository by lazy { HistoryRepository(database.historyDao()) }
    //private val historyViewModel = HistoryViewModel(repository)

/*
    private val historyViewModel: HistoryViewModel by viewModels {
        HistoryViewModelFactory((application as MyEntryPoint).repository)
    }

*/
    val hlist =listOf(
        History("2022-10-10", "AniCheck", "20mg/ml", "img1.png"),
        History("2022-10-10", "AniCheck", "30mg/ml", "img2.png")
    )
    var out_str=""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initView()

    }

    private fun initView() = with(binding) {
        historyBackButton.setOnClickListener { finish() }
        shareButton.setOnClickListener { startActivity(shareIntent)
        }
        kitHistoryRecyclerView.adapter = historyAdapter

        //historyAdapter.setHistoryList(historyViewModel.allHistorys.value!! )


        historyAdapter.setHistoryList(
            hlist
        )

    }


    val sendIntent: Intent = Intent().apply {
        action = Intent.ACTION_SEND

        for (i in hlist.indices){
            out_str = out_str + "${hlist[i].date},  ${hlist[i].productName},  ${hlist[i].result},  ${hlist[i].imgName}  \n"
        }
        putExtra(Intent.EXTRA_TEXT, out_str)
        type = "text/plain"

    }

    val shareIntent = Intent.createChooser(sendIntent, null)

}