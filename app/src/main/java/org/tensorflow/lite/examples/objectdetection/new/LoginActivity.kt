package org.tensorflow.lite.examples.objectdetection.new

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import org.tensorflow.lite.examples.objectdetection.MyEntryPoint
import org.tensorflow.lite.examples.objectdetection.QrActivity
import org.tensorflow.lite.examples.objectdetection.R
import org.tensorflow.lite.examples.objectdetection.databinding.ActivityLoginBinding


class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initView()
    }

    private fun initView() = with(binding) {
        val productTextView = findViewById<TextView>(R.id.loginProductName)
        val lotTextView = findViewById<TextView>(R.id.lotNumber)
        // Todo: retain last sessions' choice
//        productTextView.text = MyEntryPoint.prefs.getString("prodName", "PRODUCT NAME")
//        lotTextView.text = MyEntryPoint.prefs.getString("lotNum", "LOT NUMBER")

        loginButton.setOnClickListener {
//            if (idEditText.text.toString() == "user" && pwEditText.text.toString() == "1234") {
                finish()
//            } else {
//                Toast.makeText(this@LoginActivity, "아이디 또는 비밀번호를 확인해주세요.", Toast.LENGTH_SHORT)
//                    .show()
//            }
        }
        scanQRButton.setOnClickListener {
            finish()
            //startActivity(Intent(this, QrActivity::class.java))
        }
    }

    override fun onBackPressed() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Exit").setMessage("Do you want to exit?")
            .setPositiveButton("Yes") { _, _ ->
                finishAffinity()
            }.setNegativeButton("No") { _, _ ->
//                alertDialog.dismiss()
            }
        val alertDialog = builder.create()
        alertDialog.show()

    }
}