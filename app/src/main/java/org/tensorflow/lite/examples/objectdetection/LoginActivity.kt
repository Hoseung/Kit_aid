package org.tensorflow.lite.examples.objectdetection

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.widget.Toast
import androidx.core.content.ContextCompat.startActivity
import org.tensorflow.lite.examples.objectdetection.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {
    lateinit var binding: ActivityLoginBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 로그인
        binding.loginButton.setOnClickListener {
            val email: String = binding.inputEmail.text.toString()
            val password: String = binding.inputPassword.text.toString()

            if (email.length?:0 >0 || password.length?:0 >0){

                MyEntryPoint.auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this){task ->
                        binding.inputEmail.text.clear()
                        binding.inputPassword.text.clear()
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

        // 회원가입
//        binding.loginButton.setOnClickListener {
//            val email: String = binding.inputEmail.text.toString()
//            val password: String = binding.inputPassword.text.toString()
//            MyEntryPoint.auth.createUserWithEmailAndPassword(email, password)
//                .addOnCompleteListener(this) { task ->
//                    binding.inputEmail.text.clear()
//                    binding.inputPassword.text.clear()
//
//                    if (task.isSuccessful) {
//                        // password must be at least 6
//                        // send verifying email
//                        MyEntryPoint.auth.currentUser?.sendEmailVerification()
//                            ?.addOnCompleteListener { sendTask ->
//                                if (sendTask.isSuccessful) {
//                                Toast.makeText(
//                                    baseContext,
//                                    "Sign in successful. Please confirm your Email",
//                                    Toast.LENGTH_SHORT).show()
//                                } else {
//                                    Toast.makeText(
//                                        baseContext, "메일 전송 실패",
//                                        Toast.LENGTH_SHORT
//                                    ).show()
//                                }
//                            }
//                        } else {
//                            Toast.makeText(
//                            baseContext, "회원가입 실패",
//                            Toast.LENGTH_SHORT).show()
//                        }
//                    }
    }

            }

}
    fun changeMode(mode: String, binding:ActivityLoginBinding){
        if(mode === "login"){
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