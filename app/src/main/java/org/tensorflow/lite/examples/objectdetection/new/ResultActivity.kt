package org.tensorflow.lite.examples.objectdetection.new

import android.graphics.Bitmap
import android.graphics.BitmapFactory
//import android.graphics.Path
//import android.media.Image
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.viewModels
import kotlinx.coroutines.*
import org.tensorflow.lite.examples.objectdetection.HistoryRoomDatabase
import org.tensorflow.lite.examples.objectdetection.RegressionHelper
import org.tensorflow.lite.examples.objectdetection.MyEntryPoint
import org.tensorflow.lite.examples.objectdetection.R
import org.tensorflow.lite.examples.objectdetection.databinding.ActivityResultBinding
import org.tensorflow.lite.examples.objectdetection.adapter.History
import org.tensorflow.lite.examples.objectdetection.adapter.HistoryDao
import org.tensorflow.lite.examples.objectdetection.adapter.HistoryViewModel
import org.tensorflow.lite.examples.objectdetection.adapter.HistoryViewModelFactory
import java.io.File
import java.io.FileInputStream
import kotlin.math.ceil
import kotlin.math.max
import kotlin.random.Random

class ResultActivity : AppCompatActivity() {

    private lateinit var binding: ActivityResultBinding
    private lateinit var regressionHelper: RegressionHelper
    //private lateinit var database: HistoryRoomDatabase
    // ToDo: History

//    private val historyViewModel: HistoryViewModel by viewModels {
//        HistoryViewModelFactory((application as MyEntryPoint).repository)
//    }
    val applicationScope = CoroutineScope(SupervisorJob())
    //val database by lazy { HistoryRoomDatabase.getDatabase(this, applicationScope) }
//    private val repository by lazy { HistoryRepository(database.historyDao()) }
//    private val historyViewModel = HistoryViewModel(repository)
    // 이 historyViewModel이 HistoryActivity의 historyViewModel과 같은 instance일까?
    private val historyViewModel: HistoryViewModel by viewModels {
        HistoryViewModelFactory((application as MyEntryPoint).repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResultBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //regressionListener =
        regressionHelper = RegressionHelper(
            context = this,
            //regressionListener = regressionListener
        )
        initView()
        regressionHelper.setupRegression()

        //database = HistoryRoomDatabase.getDatabase(this, applicationScope)
    }

    override fun onResume() {
        super.onResume()
        // Run inference onResume, not OnCreate

        val imgDir = intent.getStringExtra("imagePath")!!
        var imgPath : String
        //binding.resultImageView.setImageURI(uri)

        setImageFromPath("$imgDir/img1.png", binding.resultImageView)

        val answers = mutableListOf<Float>()
        for (i in 1..5) {
            imgPath = "$imgDir/img${i}.png"
            val img = pathToBitmap(imgPath)!!
            // 파일 삭제
            answers.add(randomCroppedPredict(img))

            val f = File(imgPath)
            f.delete()
        }
        val answer = answers.sorted().let {
            it[2]
        }

        val answerStr = answer.let { it ->
            if (it > 130) {
                "> 130 mg/ml"
            } else {
                String.format("%.1fmg/ml", it * 180f)
            }
        }

        val myTextView = findViewById<TextView>(R.id.resultText)
        myTextView.text = answerStr

        // ToDo: saveResult()
        //setHistoryList

//        historyViewModel.insert(
//            History(null, "Bovine", 2022003, "30mg/ml", "2022-10-10")
//        )

        val history = History(null,
            MyEntryPoint.prefs.getString("prodName", "NoProduct"),
            MyEntryPoint.prefs.getString("lotNum","0").toInt(),
            answerStr,
            "Today!!"
        )

//        GlobalScope.launch(Dispatchers.IO){
//            database.historyDao().insert(history)
//        }
        historyViewModel.insert(history)
    }

    private fun randomCroppedPredict(image: Bitmap) : Float {
        var cropped: Bitmap
        val answers = mutableListOf<Float>()
        for (i in 0..5) {
            cropped = Bitmap.createBitmap(
                image,
                ceil(image.width * 0.08).toInt() + Random.nextInt(40) - 20,
                ceil(image.height * 0.36).toInt() + Random.nextInt(60) - 30,
                ceil(image.width * 0.54).toInt() + Random.nextInt(40) - 20,
                ceil(image.height * 0.245).toInt() + Random.nextInt(60) - 30
            )
            answers.add(regressionHelper.predict(cropped))
            println("prediction: ${answers[i]}")
        }
        var answer = answers.sorted().let {
            it[2]
        }
        answer = max(0f, answer)
        return answer
    }

    private fun initView() = with(binding) {
        resultBackButton.setOnClickListener { finish() }
    }
}

fun setImageFromPath(imagePath: String, imageView: ImageView){
    val imgFile = File(imagePath)
    if (imgFile.exists()) {
        val bitmap = BitmapFactory.decodeFile(imgFile.absolutePath)
        imageView.setImageBitmap(bitmap)
    }
}

fun pathToBitmap(path: String): Bitmap? {
    return try {
        val f = File(path)
        val options = BitmapFactory.Options()
        options.inPreferredConfig = Bitmap.Config.ARGB_8888
        BitmapFactory.decodeStream(FileInputStream(f), null, options)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}