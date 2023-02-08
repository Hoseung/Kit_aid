package org.tensorflow.lite.examples.objectdetection

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import org.tensorflow.lite.examples.objectdetection.adapter.History
import org.tensorflow.lite.examples.objectdetection.adapter.HistoryAdapter
//import org.tensorflow.lite.examples.objectdetection.HistoryRoomDataBase
//import org.tensorflow.lite.examples.objectdetection.adapter.HistoryViewModelFactory
import org.tensorflow.lite.examples.objectdetection.databinding.ActivityHistoryBinding
import java.io.File
import java.io.FileOutputStream

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
    private var filePath: String = ""

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

        //historyAdapter.setHistoryList(historyViewModel.allHistorys.value!! )
        historyAdapter.setHistoryList(
            hlist
        )


    }
    override fun onResume() {
        super.onResume()

        var sample_str = ""

        for (i in hlist.indices) {
            sample_str += "${hlist[i].date},  ${hlist[i].productName},  ${hlist[i].result},  ${hlist[i].imgName}  \n"
        }

//        openFileOutput("history.csv", Context.MODE_PRIVATE)
//            .use {
//
//                it.write(sample_str.toByteArray())
//            }

        val extRoot = getExternalFilesDir(null)!!
        val outFile = FileOutputStream("$extRoot/history.csv")
        outFile.write(sample_str.toByteArray())
        outFile.close()
        //        FileOutputStream(historyFile)
//            .use{
//                for (i in hlist.indices){
//                    sample_str = sample_str + "${hlist[i].date},  ${hlist[i].productName},  ${hlist[i].result},  ${hlist[i].imgName}  \n"
//                }
//                it.write(sample_str.toByteArray())
//            }
        //val historyFile = File(filePath)
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