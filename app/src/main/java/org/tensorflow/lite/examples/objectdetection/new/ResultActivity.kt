package org.tensorflow.lite.examples.objectdetection.new

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import org.tensorflow.lite.examples.objectdetection.*
import org.tensorflow.lite.examples.objectdetection.databinding.ActivityResultBinding
import org.tensorflow.lite.examples.objectdetection.adapter.History
import java.io.File
import java.io.FileInputStream
import kotlin.random.Random

class ResultActivity : AppCompatActivity() {

    private lateinit var binding: ActivityResultBinding
    private lateinit var regressionHelper: RegressionHelper
/*
    private val historyViewModel: HistoryViewModel by viewModels {
        HistoryViewModelFactory((application as MyEntryPoint).repository)
    }*/
    /*
    val applicationScope = CoroutineScope(SupervisorJob())
    val database by lazy { HistoryRoomDatabase.getDatabase(this, applicationScope) }
    private val repository by lazy { HistoryRepository(database.historyDao()) }
    private val historyViewModel = HistoryViewModel(repository)
    */

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResultBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //regressionListener =
        regressionHelper = RegressionHelper(
            context = this, // provider 지정해주어야함
            //regressionListener = regressionListener
        )
        initView()
        regressionHelper.setupRegression()

        val imgPath = intent.getStringExtra("imagePath")!!
        //binding.resultImageView.setImageURI(uri)
        setImageFromPath(imgPath, binding.resultImageView)

        //
        val img = pathToBitmap(imgPath)!!
        var cropped: Bitmap
        val answers = mutableListOf<Float>()
//        for (i in 0..11) {
//            cropped = Bitmap.createBitmap(img,
//                Math.ceil(img.width*0.065).toInt() + Random.nextInt(20)-10,
//                Math.ceil(img.height *0.36).toInt() + Random.nextInt(20)-10,
//                Math.ceil(img.width*0.52).toInt() + Random.nextInt(20)-10,
//                Math.ceil(img.height * 0.25).toInt() + Random.nextInt(20)-10
//            )
//            answers.add(regressionHelper.predict(cropped, 0))
//            println("_______@*Q(&(*^(*&)(OLJGLIJG_++++")
//            println(answers[i])
//        }
        //answers.add(regressionHelper.predict(img, 0))
        val answer = regressionHelper.predict(img, 0)
        println("_______@*Q(&(*^(*&)(OLJGLIJG_++++")

//        val answer = answers.sorted().let {
//            it[3]
//        }

        val answerStr = String.format("%.2fmg/ml", answer*180f)
        println(".....................++++++++ ${answer}###########")

        val myTextView = findViewById<TextView>(R.id.resultText)
        myTextView.text = answerStr
        // saveResult()
        //setHistoryList
        val history = History("2023-01-13", "ANIANI", answerStr, imgPath)
        //historyViewModel.insert(history)
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