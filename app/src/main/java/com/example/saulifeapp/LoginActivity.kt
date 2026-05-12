package com.example.saulifeapp

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.saulifeapp.databinding.ActivityLoginBinding
import com.example.saulifeapp.ui.profile.EditProfileActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

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
            checkUserProfile()
        }
    }

    private fun loginUser() {
        val email = binding.editEmail.text.toString().trim()
        val password = binding.editPassword.text.toString().trim()

        if (!validateInput(email, password)) return

        showLoading(true)

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->

                if (task.isSuccessful) {
                    Toast.makeText(this, "Вход выполнен", Toast.LENGTH_SHORT).show()
                    checkUserProfile()
                } else {
                    showLoading(false)
                    Toast.makeText(
                        this,
                        task.exception?.localizedMessage ?: "Ошибка входа",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }

    private fun checkUserProfile() {
        val uid = auth.currentUser?.uid

        if (uid == null) {
            showLoading(false)
            return
        }

        firestore.collection("users")
            .document(uid)
            .get()
            .addOnSuccessListener { document ->
                showLoading(false)

                val profileCompleted = document.getBoolean("profileCompleted") == true

                if (document.exists() && profileCompleted) {
                    goToHome()
                } else {
                    goToProfileSetup()
                }
            }
            .addOnFailureListener { e ->
                showLoading(false)
                Toast.makeText(
                    this,
                    "Ошибка проверки профиля: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
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
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun goToProfileSetup() {
        val intent = Intent(this, EditProfileActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}