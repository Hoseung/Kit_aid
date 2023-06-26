package org.tensorflow.lite.examples.objectdetection

import android.app.ProgressDialog.show
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat.startActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import org.tensorflow.lite.examples.objectdetection.MyEntryPoint.Companion.email
import org.tensorflow.lite.examples.objectdetection.databinding.ActivityForgotPasswordBinding
import org.tensorflow.lite.examples.objectdetection.databinding.ActivityLoginBinding
import org.tensorflow.lite.examples.objectdetection.databinding.ActivitySignInBinding
import java.io.BufferedReader
import java.io.File
import java.io.OutputStreamWriter

class LoginActivity : AppCompatActivity() {
    private var email: String = ""
    private var password: String = ""
    private var rememberMe: Boolean = false

    private fun login(email:String, password:String, binding: ActivityLoginBinding) {
        if (email.length?:0 >0 || password.length?:0 >0){
            MyEntryPoint.myFirebase.auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this){task ->
                    if (task.isSuccessful) {
                        if (MyEntryPoint.myFirebase.checkAuth()) {
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

        // 로그인 체크
        val diUser = File(applicationContext.filesDir, "diUser.dat")
        if (diUser.exists()) {
            val readDiFile: BufferedReader = diUser.reader().buffered()
            var cnt = 0
            readDiFile.forEachLine {
                if (cnt == 0) {
                    email = it
                } else if (cnt == 1) {
                    password = it
                } else {
                    rememberMe = it.toBoolean()
                }
                cnt += 1
            }

            binding.inputEmail.setText(email)
            binding.inputPassword.setText(password)
            binding.rememberMe.isChecked = true

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

        // 비밀번호 재설정
        binding.forgotPassword.setOnClickListener {
            AlertDialog.Builder(this).run {
                setTitle("Please enter your email address")
                setIcon(android.R.drawable.ic_dialog_info)
                val forgotPasswordBinding = ActivityForgotPasswordBinding.inflate(layoutInflater)
                setView(forgotPasswordBinding.root)
                setPositiveButton("OK", DialogInterface.OnClickListener { dialog, which ->
                    val lostEmail = forgotPasswordBinding.inputEmail.text.toString()
                    Firebase.auth.sendPasswordResetEmail(lostEmail)
                        .addOnSuccessListener { task ->
                            AlertDialog.Builder(context).run {
                                setMessage("Email sent successfully. Please check your email.")
                                setPositiveButton("OK", null)
                                show()
                            }
                        }.addOnFailureListener {
                            AlertDialog.Builder(context).run {
                                setMessage("Email sending failure. Please check your email format")
                                setPositiveButton("OK", null)
                                show()
                            }
                        }
                    }
                )
                show()
                setNegativeButton("Cancel", null)
            }

        }

    }

    private fun changeMode(mode: String, binding:ActivityLoginBinding){
        if(mode === "login"){
            val diUser = File(applicationContext.filesDir, "diUser.dat")
            val writeStream: OutputStreamWriter = diUser.writer()
            val rememberMe = binding.rememberMe.isChecked
            Log.d("remem", "${rememberMe}")

            if (rememberMe) {
                writeStream.write(binding.inputEmail.text.toString())
                writeStream.write("\n")
                writeStream.write(binding.inputPassword.text.toString())
                writeStream.write("\n")
                writeStream.write("true")
                writeStream.flush()
            } else {
                diUser.delete()
            }

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