package com.example.saulifeapp

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.saulifeapp.databinding.ActivityRegisterBinding
import com.example.saulifeapp.ui.profile.EditProfileActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        binding.btnRegister.setOnClickListener {
            registerUser()
        }

        binding.textGoLogin.setOnClickListener {
            finish()
        }
    }

    override fun onStart() {
        super.onStart()

        if (auth.currentUser != null) {
            checkUserProfile()
        }
    }

    private fun registerUser() {
        val email = binding.editEmail.text.toString().trim()
        val password = binding.editPassword.text.toString().trim()

        if (!validateInput(email, password)) return

        showLoading(true)

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                showLoading(false)

                if (task.isSuccessful) {
                    Toast.makeText(this, "Регистрация успешна", Toast.LENGTH_SHORT).show()
                    goToProfileSetup()
                } else {
                    Toast.makeText(
                        this,
                        task.exception?.localizedMessage ?: "Ошибка регистрации",
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
        binding.btnRegister.isEnabled = !isLoading
    }

    private fun goToHome() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
    private fun goToProfileSetup() {
        startActivity(Intent(this, EditProfileActivity::class.java))
        finish()
    }

    private fun checkUserProfile() {

        val uid = auth.currentUser?.uid ?: return

        FirebaseFirestore.getInstance()
            .collection("users")
            .document(uid)
            .get()
            .addOnSuccessListener { document ->

                if (document.exists() && document.getBoolean("profileCompleted") == true) {
                    goToHome()
                } else {
                    goToProfileSetup()
                }
            }
    }
}