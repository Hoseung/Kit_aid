package org.tensorflow.lite.examples.objectdetection

//import android.app.Activity
//import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import kotlinx.coroutines.*
import org.tensorflow.lite.examples.objectdetection.adapter.History
import org.tensorflow.lite.examples.objectdetection.adapter.HistoryAdapter
//import org.tensorflow.lite.examples.objectdetection.HistoryRoomDatabase
//import org.tensorflow.lite.examples.objectdetection.adapter.HistoryViewModelFactory
import org.tensorflow.lite.examples.objectdetection.databinding.ActivityHistoryBinding
import java.io.File
import java.io.FileOutputStream

class HistoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHistoryBinding
    // Adapter = 데이터 표현용
    // DAO = DB access용

    private var historyAdapter = HistoryAdapter()
    //private var historyDB: HistoryRoomDatabase?= null

    lateinit var database: HistoryRoomDatabase

    //val applicationScope = CoroutineScope(SupervisorJob())
    //val database by lazy { HistoryRoomDatabase.getDatabase(this, applicationScope)}
    //private val repository by lazy { HistoryRepository(database.historyDao()) }
    //private val historyViewModel = HistoryViewModel(repository)

/*
    private val historyViewModel: HistoryViewModel by viewModels {
        HistoryViewModelFactory((application as MyEntryPoint).repository)
    }

*/

//    val hlist =listOf(
//        History(null,"2022-10-10", 2022003, "20mg/ml", "img1.png"),
//        History(null,"2022-10-10", 2022003, "30mg/ml", "img2.png")
//    )
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initView()
        database = HistoryRoomDatabase.getDatabase(this)
    }

    // Init에 할까? onResume에 할까?
    private fun initView() = with(binding) {
        historyBackButton.setOnClickListener { finish() }

        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_STREAM, getContentUri())
            //type = "text/plain"
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, null)
        shareButton.setOnClickListener {
            startActivity(shareIntent)
        }
        kitHistoryRecyclerView.adapter = historyAdapter

        // Todo: Update historyViewModel
        //historyAdapter.setHistoryList(historyViewModel.allHistorys.value!! )

    //        historyAdapter.setHistoryList(
//            hlist
//        )

    }
    override fun onResume() {
        super.onResume()

        var sampleStr = ""


//        for (i in hlist.indices) {
//            sampleStr += "${hlist[i].date},  ${hlist[i].product},  ${hlist[i].density},  ${hlist[i].date}  \n"
//        }

        val extRoot = getExternalFilesDir(null)!!
        val outFile = FileOutputStream("$extRoot/history.csv")
        outFile.write(sampleStr.toByteArray())
        outFile.close()

        val history = History(
            null, "abs", 123, "234.56 mg/ml", "good day"
        )
        GlobalScope.launch(Dispatchers.IO){
            database.historyDao().insert(history)
        }
    }
    private fun getContentUri(): Uri {
        val extRoot = getExternalFilesDir(null)!!
        return FileProvider.getUriForFile(
            this@HistoryActivity,
            applicationContext.packageName + ".provider",
            //filePath
            File("$extRoot/history.csv")
        )
    }
}