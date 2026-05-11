package com.example.saulifeapp

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.saulifeapp.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        binding.btnLogin.setOnClickListener {
            loginUser()
        }

        binding.textGoRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    override fun onStart() {
        super.onStart()

        if (auth.currentUser != null) {
            goToHome()
        }
    }

    private fun loginUser() {
        val email = binding.editEmail.text.toString().trim()
        val password = binding.editPassword.text.toString().trim()

        if (!validateInput(email, password)) return

        showLoading(true)

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                showLoading(false)

                if (task.isSuccessful) {
                    Toast.makeText(this, "Вход выполнен", Toast.LENGTH_SHORT).show()
                    goToHome()
                } else {
                    Toast.makeText(
                        this,
                        task.exception?.localizedMessage ?: "Ошибка входа",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }

    private fun validateInput(email: String, password: String): Boolean {
        if (email.isEmpty()) {
            binding.editEmail.error = "Введите email"
            binding.editEmail.requestFocus()
            return false
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.editEmail.error = "Введите корректный email"
            binding.editEmail.requestFocus()
            return false
        }

        if (password.isEmpty()) {
            binding.editPassword.error = "Введите пароль"
            binding.editPassword.requestFocus()
            return false
        }

        if (password.length < 6) {
            binding.editPassword.error = "Минимум 6 символов"
            binding.editPassword.requestFocus()
            return false
        }

        return true
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.btnLogin.isEnabled = !isLoading
    }

    private fun goToHome() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}