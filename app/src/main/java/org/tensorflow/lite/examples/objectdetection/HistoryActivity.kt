package org.tensorflow.lite.examples.objectdetection

//import android.app.Activity
//import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.*
import org.tensorflow.lite.examples.objectdetection.adapter.History
import org.tensorflow.lite.examples.objectdetection.adapter.HistoryAdapter
import org.tensorflow.lite.examples.objectdetection.adapter.HistoryViewModel
//import org.tensorflow.lite.examples.objectdetection.HistoryRoomDatabase
import org.tensorflow.lite.examples.objectdetection.adapter.HistoryViewModelFactory
import org.tensorflow.lite.examples.objectdetection.databinding.ActivityHistoryBinding
import java.io.File
import java.io.FileOutputStream

class HistoryActivity : AppCompatActivity() {
    val applicationScope = CoroutineScope(SupervisorJob())
    private lateinit var binding: ActivityHistoryBinding
    // Adapter = 데이터 표현용
    // DAO = DB access용

    private var historyAdapter = HistoryAdapter()
    //private var historyDB: HistoryRoomDatabase?= null

    //lateinit var database: HistoryRoomDatabase
    //private val historyViewModel = HistoryViewModel(repository)

    private val historyViewModel: HistoryViewModel by viewModels {
        HistoryViewModelFactory((application as MyEntryPoint).repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 여기가 예제랑 조금 다름.
        /*
        예제는...
        setContentView(R.layout.activity_main)
        */
        binding = ActivityHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val kitHistoryRecyclerView = findViewById<RecyclerView>(R.id.kitHistoryRecyclerView)
        kitHistoryRecyclerView.adapter = historyAdapter

        // FIXME: layoutManager is specified in activity_history.xml So, can I skip this?
        kitHistoryRecyclerView.layoutManager = LinearLayoutManager(this)
        historyViewModel.allHistorys.observe(this) { historys ->
            historys.let {historyAdapter.setHistoryList(it)}// submitList(it)}
        }
        initView()
        //database = HistoryRoomDatabase.getDatabase(this, applicationScope)
    }

    // Init에 할까? onResume에 할까?
    private fun initView() = with(binding) {
        historyBackButton.setOnClickListener { finish() }
//        val sendIntent = Intent().apply {
//            action = Intent.ACTION_SEND
//            putExtra(Intent.EXTRA_STREAM, getContentUri())
//            //type = "text/plain"
//            type = "text/plain"
//        }
//        val shareIntent = Intent.createChooser(sendIntent, null)
//        shareButton.setOnClickListener {
//            startActivity(shareIntent)
//        }

        // Todo: Update historyViewModel
        // historyAdapter.setHistoryList(historyViewModel.allHistorys.value!! )
        val hlist =listOf(
            History(null, "2022-10-10", 20222002, "20mg/ml", "img1.png"),
            History(null, "2022-10-10", 20222002, "30mg/ml", "img2.png")
        )
        // 아래는 에러
        //historyAdapter.setHistoryList(hlist)
        // 어댑터로 넣어봤자 RoomDB로 전달 안 됨.
        //historyAdapter.addHistoryList(History(null, "NEW2", 20222002, "20mg/ml", "img1.png"))
        //
        // RoomDB로 넣으려면 ViewModel에서 insert 해야함.
        // 근데 이렇게 해도 화면으로는 왜 안 나오냐고...
        //historyViewModel.insert(History(null, "TEST", 2022003, "30mg/ml", "2022-10-10"))


    }
//    override fun onResume() {
//        super.onResume()
//
//        var sampleStr = ""
//
//
////        for (i in hlist.indices) {
////            sampleStr += "${hlist[i].date},  ${hlist[i].product},  ${hlist[i].density},  ${hlist[i].date}  \n"
////        }
//
//        val extRoot = getExternalFilesDir(null)!!
//        val outFile = FileOutputStream("$extRoot/history.csv")
//        outFile.write(sampleStr.toByteArray())
//        outFile.close()
//
////        val history = History(
////            null, "abs", 123, "234.56 mg/ml", "good day"
////        )
////        GlobalScope.launch(Dispatchers.IO){
////            database.historyDao().insert(history)
////        }
//        // 새 history는 Result Activity에서 만들어지는데.
//        // observer는 여기 두고, insert는 Result activity에서 해도 되나?
//    }
//    private fun getContentUri(): Uri {
//        val extRoot = getExternalFilesDir(null)!!
//        return FileProvider.getUriForFile(
//            this@HistoryActivity,
//            applicationContext.packageName + ".provider",
//            //filePath
//            File("$extRoot/history.csv")
//        )
//    }
}