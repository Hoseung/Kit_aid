package org.tensorflow.lite.examples.objectdetection.new

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
//import android.graphics.Path
//import android.media.Image
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.viewModels
import kotlinx.coroutines.*
import org.tensorflow.lite.examples.objectdetection.HistoryRoomDatabase
import org.tensorflow.lite.examples.objectdetection.RegressionHelper
import org.tensorflow.lite.examples.objectdetection.MyEntryPoint
import org.tensorflow.lite.examples.objectdetection.R
import org.tensorflow.lite.examples.objectdetection.adapter.*
import org.tensorflow.lite.examples.objectdetection.databinding.ActivityResultBinding
import java.io.File
import java.io.FileInputStream
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.pow
import kotlin.random.Random

class ResultActivity : AppCompatActivity() {

    private lateinit var binding: ActivityResultBinding
    private lateinit var regressionHelper: RegressionHelper
    // ToDo: History

    //val applicationScope = CoroutineScope(SupervisorJob())
//    private val repository by lazy { HistoryRepository(database.historyDao()) }
//    private val historyViewModel = HistoryViewModel(repository)
    // 이 historyViewModel이 HistoryActivity의 historyViewModel과 같은 instance일까?
    private val historyViewModel: HistoryViewModel by viewModels {
        HistoryViewModelFactory((application as MyEntryPoint).database.historyDao())
    }

    //private val modelsDao = (application as MyEntryPoint).database.modelsDao()
    //private val modelsRepo = ModelsRepository((application as MyEntryPoint).database.modelsDao())

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

        val suri = MyEntryPoint.prefs.getString("CalibUri", "badbadbad")
        println("SURI $suri")

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
        var answer = answers.sorted().let {
            it[2]
        }



        /*
        Todo:
         Calibration

         Also Todo: 혈청 / 전혈 선택에 따른 보정
         */
        Log.d("answer", "$answer")
        answer = calibrateLot(answer)

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
        // setHistoryList

//        historyViewModel.insert(
//            History(null, "Bovine", 2022003, "30mg/ml", "2022-10-10")
//        )

        val history = History(null,
            MyEntryPoint.prefs.getString("prodName", "NoProduct"),
            MyEntryPoint.prefs.getString("lotNum","0"),
            answerStr,
            "Today!!"
        )

//        GlobalScope.launch(Dispatchers.IO){
//            database.historyDao().insert(history)
//        }
        historyViewModel.insert(history)

        //historyAdapter.addHistoryList(History(null, "2022-10-10", 20222002, "20mg/ml", "img1.png"))
    }

    private fun calibrateLot(answer: Float) : Float {
        val dAnswer = answer.toDouble()
        val suri = MyEntryPoint.prefs.getString("CalibUri", "badbadbad")
        print("SURI $suri \n")

        val uri = Uri.parse(suri)
        print("URI $uri")
        val file = File(uri.path!!)

        val coefficients = file.readLines() //File(uri.path!!).useLines { it.toList() }
        var sum = 0.0
        for(i in coefficients.indices){
            println("$i ZZZZZZZ $coefficients[i]")
            sum += coefficients[i].toDouble() * dAnswer.pow(i)
        }
        return sum.toFloat()
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
            //println("prediction: ${answers[i]}")
        }
        var answer = answers.sorted().let {
            it[2]
        }
        answer = max(0f, answer)
        return answer
    }

    private fun initView() = with(binding) {

        // todo calibration file check!
//        if File()
        Log.d("calicheck", "${MyEntryPoint.prefs.getString("CalibUri", "00000")}")
        Log.d("calibcheck2", "${MyEntryPoint.prefs.getString("prodName", "---")}")
        Log.d("calibcheck3", "${MyEntryPoint.prefs.getString("lotNum", "---")}")

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