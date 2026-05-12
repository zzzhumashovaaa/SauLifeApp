package com.example.saulifeapp.ui.profile

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.saulifeapp.MainActivity
import com.example.saulifeapp.R
import com.example.saulifeapp.databinding.ActivityEditProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class EditProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditProfileBinding

    private val auth by lazy { FirebaseAuth.getInstance() }
    private val firestore by lazy { FirebaseFirestore.getInstance() }

    private var originalProfileSnapshot: String = ""
    private var profileAlreadyCompleted: Boolean = false

    private val cities = listOf(
        "Алматы", "Астана", "Шымкент", "Караганда",
        "Актобе", "Тараз", "Павлодар", "Семей",
        "Усть-Каменогорск", "Атырау", "Актау",
        "Костанай", "Кокшетау"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupCityDropdown()
        setupGenderLogic()
        setupButtons()
        loadProfile()
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
        binding.chipGroupGender.setOnCheckedStateChangeListener { _, checkedIds ->
            if (checkedIds.contains(R.id.chipFemale)) {
                binding.layoutPregnancy.visibility = View.VISIBLE
            } else {
                binding.layoutPregnancy.visibility = View.GONE
                binding.chipGroupPregnancy.clearCheck()
            }
        }
    }

    private fun setupButtons() {

        binding.btnBack.setOnClickListener {
            handleBack()
        }

        binding.btnSaveProfile.setOnClickListener {
            saveProfile()
        }
    }

    private fun loadProfile() {

        val user = auth.currentUser ?: return
        val uid = user.uid

        binding.progressBar.visibility = View.VISIBLE

        firestore.collection("users")
            .document(uid)
            .get()
            .addOnSuccessListener { document ->

                binding.progressBar.visibility = View.GONE

                val fullName = document.getString("fullName").orEmpty()
                val email = document.getString("email").orEmpty()
                val authEmail = user.email.orEmpty()

                binding.editFullName.setText(fullName)

                binding.editEmail.setText(
                    if (email.isNotBlank()) email else authEmail
                )

                binding.autoCity.setText(
                    document.getString("city").orEmpty(),
                    false
                )

                val age = document.getLong("age")?.toInt() ?: 0

                if (age > 0) {
                    binding.editAge.setText(age.toString())
                }


                binding.editAllergies.setText(
                    document.getString("allergies").orEmpty()
                )

                binding.editChronic.setText(
                    document.getString("chronicDiseases").orEmpty()
                )

                binding.editCurrentMeds.setText(
                    document.getString("currentMedications").orEmpty()
                )

                val gender = document.getString("gender").orEmpty()

                val isPregnant = document.getBoolean("isPregnant")
                    ?: document.getBoolean("pregnant")

                // ChipGroup арқылы жыныс орнату
                when (gender) {
                    "Мужской" -> binding.chipMale.isChecked = true
                    "Женский" -> binding.chipFemale.isChecked = true
                    "Другое"  -> binding.chipOther.isChecked = true
                }

                if (gender == "Женский") {

                    binding.layoutPregnancy.visibility = View.VISIBLE

                    when (isPregnant) {
                        true  -> binding.chipPregnantYes.isChecked = true
                        false -> binding.chipPregnantNo.isChecked = true
                        null  -> binding.chipGroupPregnancy.clearCheck()
                    }

                } else {

                    binding.layoutPregnancy.visibility = View.GONE
                }

                profileAlreadyCompleted =
                    document.getBoolean("profileCompleted") == true

                originalProfileSnapshot = getCurrentProfileSnapshot()
            }
            .addOnFailureListener { e ->

                binding.progressBar.visibility = View.GONE

                Toast.makeText(
                    this,
                    "Ошибка загрузки: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    private fun saveProfile() {

        val user = auth.currentUser
        val uid = user?.uid

        if (uid == null) {
            Toast.makeText(
                this,
                "Ошибка авторизации",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val fullName = binding.editFullName.text.toString().trim()
        val email = binding.editEmail.text.toString().trim()
        val city = binding.autoCity.text.toString().trim()
        val ageText = binding.editAge.text.toString().trim()

        val allergies = binding.editAllergies.text.toString().trim()

        val chronic = binding.editChronic.text.toString().trim()

        val currentMeds = binding.editCurrentMeds.text.toString().trim()

        // ChipGroup арқылы жыныс анықтау
        val gender = when {
            binding.chipMale.isChecked   -> "Мужской"
            binding.chipFemale.isChecked -> "Женский"
            binding.chipOther.isChecked  -> "Другое"
            else -> ""
        }

        // ChipGroup арқылы жүктілік анықтау
        val isPregnant = when {
            binding.chipPregnantYes.isChecked -> true
            binding.chipPregnantNo.isChecked  -> false
            else -> null
        }

        // Валидация
        if (fullName.isBlank()) {
            binding.layoutFullName.error = "Введите имя"
            return
        } else {
            binding.layoutFullName.error = null
        }

        if (email.isBlank()) {
            binding.layoutEmail.error = "Введите email"
            return
        } else {
            binding.layoutEmail.error = null
        }

        if (city.isBlank()) {
            binding.layoutCity.error = "Выберите город"
            return
        } else {
            binding.layoutCity.error = null
        }

        val age = ageText.toIntOrNull()

        if (age == null || age <= 0 || age > 120) {
            binding.layoutAge.error = "Некорректный возраст"
            return
        } else {
            binding.layoutAge.error = null
        }

        if (gender.isBlank()) {
            Toast.makeText(
                this,
                "Выберите пол",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        if (gender == "Женский" && isPregnant == null) {
            Toast.makeText(
                this,
                "Укажите беременность",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        binding.progressBar.visibility = View.VISIBLE
        binding.btnSaveProfile.isEnabled = false

        val wasCompletedBefore = profileAlreadyCompleted

        val profile = UserProfile(
            uid                = uid,
            fullName           = fullName,
            email              = email,
            city               = city,
            age                = age,
            gender             = gender,
            isPregnant         = if (gender == "Женский") isPregnant else null,
            allergies          = allergies,
            chronicDiseases    = chronic,
            currentMedications = currentMeds,
            profileCompleted   = true
        )

        firestore.collection("users")
            .document(uid)
            .set(profile, SetOptions.merge())
            .addOnSuccessListener {

                binding.progressBar.visibility = View.GONE
                binding.btnSaveProfile.isEnabled = true

                profileAlreadyCompleted = true
                originalProfileSnapshot = getCurrentProfileSnapshot()

                Toast.makeText(
                    this,
                    "Профиль сохранён",
                    Toast.LENGTH_SHORT
                ).show()

                if (wasCompletedBefore) {

                    finish()

                } else {

                    val intent = Intent(this, MainActivity::class.java)

                    intent.flags =
                        Intent.FLAG_ACTIVITY_NEW_TASK or
                                Intent.FLAG_ACTIVITY_CLEAR_TASK

                    startActivity(intent)
                    finish()
                }
            }
            .addOnFailureListener { e ->

                binding.progressBar.visibility = View.GONE
                binding.btnSaveProfile.isEnabled = true

                Toast.makeText(
                    this,
                    "Ошибка: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    private fun getCurrentProfileSnapshot(): String {

        val gender = when {
            binding.chipMale.isChecked -> "Мужской"
            binding.chipFemale.isChecked -> "Женский"
            binding.chipOther.isChecked -> "Другое"
            else -> ""
        }

        val pregnancy = when {
            binding.chipPregnantYes.isChecked -> "true"
            binding.chipPregnantNo.isChecked -> "false"
            else -> ""
        }

        return listOf(
            binding.editFullName.text.toString().trim(),
            binding.editEmail.text.toString().trim(),
            binding.autoCity.text.toString().trim(),
            binding.editAge.text.toString().trim(),
            gender,
            pregnancy,
            binding.editAllergies.text.toString().trim(),
            binding.editChronic.text.toString().trim(),
            binding.editCurrentMeds.text.toString().trim()
        ).joinToString("|")
    }

    private fun hasUnsavedChanges(): Boolean {
        return getCurrentProfileSnapshot() != originalProfileSnapshot
    }

    private fun handleBack() {

        if (!profileAlreadyCompleted) {

            Toast.makeText(
                this,
                "Сначала заполните профиль",
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        if (hasUnsavedChanges()) {

            Toast.makeText(
                this,
                "Сначала сохраните изменения",
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        finish()
    }

}