package com.example.saulifeapp.ui.profile

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.saulifeapp.HomeActivity
import com.example.saulifeapp.R
import com.example.saulifeapp.databinding.ActivityProfileSetupBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class ProfileSetupActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileSetupBinding

    private val auth by lazy { FirebaseAuth.getInstance() }
    private val firestore by lazy { FirebaseFirestore.getInstance() }

    private val cities = listOf(
        "Алматы", "Астана", "Шымкент", "Караганда",
        "Актобе", "Тараз", "Павлодар", "Семей",
        "Усть-Каменогорск", "Атырау", "Актау",
        "Костанай", "Кокшетау"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileSetupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupCityDropdown()
        setupGenderLogic()
        setupButtons()
    }

    private fun setupCityDropdown() {
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_dropdown_item_1line,
            cities
        )
        binding.autoCity.setAdapter(adapter)
    }

    private fun setupGenderLogic() {
        binding.radioGroupGender.setOnCheckedChangeListener { _, checkedId ->
            if (checkedId == R.id.radioFemale) {
                binding.layoutPregnancy.visibility = View.VISIBLE
            } else {
                binding.layoutPregnancy.visibility = View.GONE
                binding.radioGroupPregnancy.clearCheck()
            }
        }
    }

    private fun setupButtons() {
        binding.btnContinue.setOnClickListener {
            saveProfile()
        }

        binding.btnSkip.setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
        }
    }

    private fun saveProfile() {
        val user = auth.currentUser
        val uid = user?.uid

        if (uid == null) {
            Toast.makeText(this, "Ошибка авторизации", Toast.LENGTH_SHORT).show()
            return
        }

        val city = binding.autoCity.text.toString().trim()
        val ageText = binding.editAge.text.toString().trim()
        val allergies = binding.editAllergies.text.toString().trim()
        val chronic = binding.editChronic.text.toString().trim()
        val currentMeds = binding.editCurrentMeds.text.toString().trim()

        val gender = when (binding.radioGroupGender.checkedRadioButtonId) {
            R.id.radioMale -> "Мужской"
            R.id.radioFemale -> "Женский"
            R.id.radioOther -> "Другое"
            else -> ""
        }

        val isPregnant = when (binding.radioGroupPregnancy.checkedRadioButtonId) {
            R.id.radioPregnantYes -> true
            R.id.radioPregnantNo -> false
            else -> null
        }

        if (city.isEmpty()) {
            binding.layoutCity.error = "Выберите город"
            return
        } else {
            binding.layoutCity.error = null
        }

        if (ageText.isEmpty()) {
            binding.layoutAge.error = "Введите возраст"
            return
        } else {
            binding.layoutAge.error = null
        }

        val age = ageText.toIntOrNull()
        if (age == null || age <= 0 || age > 120) {
            binding.layoutAge.error = "Некорректный возраст"
            return
        }

        if (gender.isEmpty()) {
            Toast.makeText(this, "Выберите пол", Toast.LENGTH_SHORT).show()
            return
        }

        if (gender == "Женский" && isPregnant == null) {
            Toast.makeText(this, "Укажите беременность", Toast.LENGTH_SHORT).show()
            return
        }

        if (!binding.checkboxConsent.isChecked) {
            Toast.makeText(this, "Нужно согласие на сохранение данных", Toast.LENGTH_SHORT).show()
            return
        }

        binding.progressBar.visibility = View.VISIBLE
        binding.btnContinue.isEnabled = false

        val profileUpdates = hashMapOf<String, Any?>(
            "uid" to uid,
            "email" to (user.email ?: ""),
            "city" to city,
            "age" to age,
            "gender" to gender,
            "isPregnant" to if (gender == "Женский") isPregnant else null,
            "allergies" to allergies,
            "chronicDiseases" to chronic,
            "currentMedications" to currentMeds,
            "profileCompleted" to true
        )

        firestore.collection("users")
            .document(uid)
            .set(profileUpdates, SetOptions.merge())
            .addOnSuccessListener {
                binding.progressBar.visibility = View.GONE
                binding.btnContinue.isEnabled = true

                Toast.makeText(this, "Профиль сохранён", Toast.LENGTH_SHORT).show()

                startActivity(Intent(this, HomeActivity::class.java))
                finish()
            }
            .addOnFailureListener { e ->
                binding.progressBar.visibility = View.GONE
                binding.btnContinue.isEnabled = true

                Toast.makeText(this, "Ошибка: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }
}