package org.tensorflow.lite.examples.objectdetection.new

import android.app.AlertDialog
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
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
        loginButton.setOnClickListener {
            if (idEditText.text.toString() == "user" && pwEditText.text.toString() == "1234") {
//                startActivity(Intent(this@LoginActivity, Main2Activity::class.java))
//                finishAffinity()
                finish()
            } else {
                Toast.makeText(this@LoginActivity, "아이디 또는 비밀번호를 확인해주세요.", Toast.LENGTH_SHORT)
                    .show()
            }

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