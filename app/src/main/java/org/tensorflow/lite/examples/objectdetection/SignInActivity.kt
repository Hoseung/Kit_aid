package org.tensorflow.lite.examples.objectdetection

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build.VERSION_CODES.M
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.preference.Preference
import androidx.preference.PreferenceManager
import org.tensorflow.lite.examples.objectdetection.MyEntryPoint.Companion.email
import org.tensorflow.lite.examples.objectdetection.databinding.ActivityLoginBinding
import org.tensorflow.lite.examples.objectdetection.databinding.ActivitySignInBinding

class SignInActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        val binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.signInButton.setOnClickListener {
            val email = binding.signInEmail.text.toString()
            val pw1 = binding.signInPw1.text.toString()
            val pw2 = binding.signInPw2.text.toString()

            if ("@" !in email) {
                Toast.makeText(baseContext,
                "Invalid email format",
                Toast.LENGTH_SHORT).show()

                binding.signInEmail.text.clear()

            } else if (pw1?.length?:0 < 6 || pw2?.length?:0 < 6) {
                Toast.makeText(baseContext,
                    "Password must be more than 6 letters.",
                    Toast.LENGTH_SHORT).show()
                binding.signInPw1.text.clear()
                binding.signInPw2.text.clear()

            } else if (pw1 != pw2) {
                Toast.makeText(baseContext,
                    "Two password are different.",
                    Toast.LENGTH_SHORT).show()
                binding.signInPw1.text.clear()
                binding.signInPw2.text.clear()
            } else {
                MyEntryPoint.auth.createUserWithEmailAndPassword(email, pw1)
                    .addOnCompleteListener(this) { task ->
                        binding.signInEmail.text.clear()
                        binding.signInPw1.text.clear()
                        binding.signInPw2.text.clear()

                        if (task.isSuccessful) {
                            // password must be at least 6
                            // send verifying email
                            MyEntryPoint.auth.currentUser?.sendEmailVerification()
                                ?.addOnCompleteListener { sendTask ->
                                    if (sendTask.isSuccessful) {
                                        Toast.makeText(
                                            baseContext,
                                            "Sign in successful. Please verify your Email",
                                            Toast.LENGTH_SHORT
                                        ).show()

                                        // save user id and password
                                        MyEntryPoint.prefs.setString("email", email)
                                        MyEntryPoint.prefs.setString("password", pw1)

                                        // switch activity to login page
                                        val switchLogin = Intent(this@SignInActivity, LoginActivity::class.java)
                                        startActivity(switchLogin)

                                    } else {
                                        Toast.makeText(
                                            baseContext, "Fail to send mail...",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                        } else {
                            Toast.makeText(
                                baseContext, "회원가입 실패",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
            }
        }
    }
}