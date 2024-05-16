package com.dsa.pocketmart

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.dsa.pocketmart.databinding.ActivitySignupBinding
import com.google.firebase.auth.FirebaseAuth

class SignupActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignupBinding
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        binding.hook.setOnClickListener {
            navigateToLogin()
        }

        binding.button.setOnClickListener {
            signupAction()
        }
    }

    private fun signupAction() {
        val email = binding.email.text.toString()
        val password = binding.password.text.toString()
        val passwordConfirmation = binding.passwordConfirmation.text.toString()

        if (email.isNotEmpty() && password.isNotEmpty() && passwordConfirmation.isNotEmpty()) {
            if (password == passwordConfirmation) {
                firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener {
                    if (it.isSuccessful) {
                        navigateToLogin()
                    } else {
                        Toast.makeText(
                            this@SignupActivity, it.exception.toString(), Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } else {
                Toast.makeText(
                    this@SignupActivity,
                    getString(R.string.contrasenas_deben_coincidir),
                    Toast.LENGTH_SHORT
                ).show()
            }
        } else {
            Toast.makeText(
                this@SignupActivity, getString(R.string.llenar_campos), Toast.LENGTH_SHORT
            ).show()
        }

    }

    private fun navigateToLogin() {
        val intent = Intent(this@SignupActivity, LoginActivity::class.java)
        startActivity(intent)
    }
}
