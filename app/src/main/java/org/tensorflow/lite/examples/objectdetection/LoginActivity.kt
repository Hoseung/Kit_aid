package org.tensorflow.lite.examples.objectdetection

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat.startActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.GoogleAuthProvider
import org.tensorflow.lite.examples.objectdetection.MyEntryPoint.Companion.email
import org.tensorflow.lite.examples.objectdetection.databinding.ActivityLoginBinding
import org.tensorflow.lite.examples.objectdetection.databinding.ActivitySignInBinding
import java.io.BufferedReader
import java.io.File
import java.io.OutputStreamWriter

class LoginActivity : AppCompatActivity() {
    private var email: String = ""
    private var password: String = ""

    private fun login(email:String, password:String, binding: ActivityLoginBinding) {
        if (email.length?:0 >0 || password.length?:0 >0){
            MyEntryPoint.auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this){task ->
                    if (task.isSuccessful) {
                        if (MyEntryPoint.checkAuth()) {
                            // 로그인 성공
                            MyEntryPoint.email = email
                            Toast.makeText(baseContext,
                                "Login Success",
                                Toast.LENGTH_SHORT).show()
                            Log.d("login", "login success!")
                            changeMode("login", binding)
                        } else {
                            // 발송된 메일로 인증 확인을 안 한 경우
                            Toast.makeText(baseContext,
                                "Please verify your email first",
                                Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(baseContext,
                            "Fail to login", Toast.LENGTH_SHORT).show()
                    }
                }
        } else {
            Toast.makeText(baseContext,
                "Please enter email and password correctly",
                Toast.LENGTH_SHORT).show()
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 자동 로그인
        val diUser = File(applicationContext.filesDir, "diUser.dat")
        if (diUser.exists()) {
            val readDiFile: BufferedReader = diUser.reader().buffered()
            readDiFile.forEachLine {
                if (it != null && "@" in it) {
                    email = it
                } else if (it != null && email != null) {
                    password = it
                }
            }

            // 자동 로그인 시도
            if (email?.length ?: 0 > 0 && password?.length ?: 0 > 6) {
                binding.inputEmail.setText(email)
                binding.inputPassword.setText(password)
                login(email, password, binding)
            }
        }

        // 로그인
        binding.loginButton.setOnClickListener {
            email = binding.inputEmail.text.toString()
            password = binding.inputPassword.text.toString()
            login(email, password, binding)
            }

        // 회원가입
        binding.signinText.setOnClickListener{
            val switchSignIn = Intent(this@LoginActivity, SignInActivity::class.java)
            startActivity(switchSignIn)
        }
}
    private fun changeMode(mode: String, binding:ActivityLoginBinding){
        if(mode === "login"){
            val diUser = File(applicationContext.filesDir, "diUser.dat")
            val writeStream: OutputStreamWriter = diUser.writer()
            writeStream.write(binding.inputEmail.text.toString())
            writeStream.write("\n")
            writeStream.write(binding.inputPassword.text.toString())
            writeStream.flush()

            binding.run {
                val switchMain = Intent(this@LoginActivity, MainActivity::class.java)
                startActivity(switchMain)
            }

        }else if(mode === "logout"){
            binding.run {
                println("로그인 하거나 회원가입 해주세요.")
            }

        }else if(mode === "signin"){
            binding.run {
                println("사인인!")
            }
        }
    }
}