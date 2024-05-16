package com.dsa.pocketmart

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.dsa.pocketmart.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        binding.hook.setOnClickListener {
            navigateToSignup()
        }

        binding.button.setOnClickListener {
            loginAction()
        }
    }

    private fun loginAction() {
        val email = binding.email.text.toString()
        val password = binding.password.text.toString()

        if (email.isNotEmpty() && password.isNotEmpty()) {
            firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener {
                if (it.isSuccessful) {
                    navigateToMainActivity()
                } else {
                    Toast.makeText(this@LoginActivity, it.exception.toString(), Toast.LENGTH_SHORT)
                        .show()
                }
            }
        } else {
            Toast.makeText(
                this@LoginActivity, getString(R.string.llenar_campos), Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun navigateToSignup() {
        val intent = Intent(this@LoginActivity, SignupActivity::class.java)
        startActivity(intent)
    }

    private fun navigateToMainActivity() {
        val intent = Intent(this@LoginActivity, MainActivity::class.java)
        startActivity(intent)
    }

    override fun onStart() {
        super.onStart()
        if (firebaseAuth.currentUser != null) {
            navigateToMainActivity()
        }
    }
}