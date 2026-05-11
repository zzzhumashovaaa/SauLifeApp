package com.example.saulifeapp.ui.profile

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.saulifeapp.LoginRegisterActivity
import com.example.saulifeapp.R
import com.example.saulifeapp.ui.profile.UserProfile
import com.example.saulifeapp.databinding.ActivityProfileBinding
import com.example.saulifeapp.ui.reminders.RemindersActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding

    private val auth by lazy { FirebaseAuth.getInstance() }
    private val firestore by lazy { FirebaseFirestore.getInstance() }

    private var isEditMode = false

    private val cities = listOf(
        "Алматы", "Астана", "Шымкент", "Караганда",
        "Актобе", "Тараз", "Павлодар", "Семей",
        "Усть-Каменогорск", "Атырау", "Актау",
        "Костанай", "Кокшетау"
    )

    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                Toast.makeText(this, "Уведомления включены", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Уведомления не разрешены", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupCityDropdown()
        setupGenderLogic()
        setupClickListeners()
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
        binding.radioGroupGender.setOnCheckedChangeListener { _, checkedId ->
            if (checkedId == R.id.radioFemale) {
                binding.layoutPregnancy.visibility = View.VISIBLE
            } else {
                binding.layoutPregnancy.visibility = View.GONE
                binding.radioGroupPregnancy.clearCheck()
            }
        }
    }

    private fun setupClickListeners() {

        binding.btnEditProfile.setOnClickListener {
            setEditMode(true)
        }

        binding.btnCancelEdit.setOnClickListener {
            setEditMode(false)
            loadProfile()
        }

        binding.btnSaveProfile.setOnClickListener {
            saveProfile()
        }

        binding.cardReminders.setOnClickListener {
            startActivity(Intent(this, RemindersActivity::class.java))
        }

        binding.cardNotifications.setOnClickListener {
            requestNotificationPermissionIfNeeded()
        }

        binding.btnLogout.setOnClickListener {
            auth.signOut()
            startActivity(Intent(this, LoginRegisterActivity::class.java))
            finishAffinity()
        }
    }

    private fun setEditMode(enabled: Boolean) {
        isEditMode = enabled

        val editableVisibility = if (enabled) View.VISIBLE else View.GONE
        val infoVisibility = if (enabled) View.GONE else View.VISIBLE

        binding.groupEditable.visibility = editableVisibility
        binding.groupInfo.visibility = infoVisibility

        binding.btnEditProfile.visibility = if (enabled) View.GONE else View.VISIBLE
        binding.layoutEditActions.visibility = if (enabled) View.VISIBLE else View.GONE
    }

    private fun loadProfile() {
        val uid = auth.currentUser?.uid ?: return

        binding.progressBar.visibility = View.VISIBLE

        firestore.collection("users")
            .document(uid)
            .get()
            .addOnSuccessListener { document ->
                binding.progressBar.visibility = View.GONE

                if (!document.exists()) {
                    Toast.makeText(this, "Профиль не найден", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }

                val profile = document.toObject(UserProfile::class.java) ?: return@addOnSuccessListener
                bindProfile(profile)
            }
            .addOnFailureListener { e ->
                binding.progressBar.visibility = View.GONE
                Toast.makeText(this, "Ошибка загрузки: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun bindProfile(profile: UserProfile) {
        val displayName = if (profile.fullName.isNotBlank()) profile.fullName else "Пользователь"
        val displayEmail = if (profile.email.isNotBlank()) profile.email else (auth.currentUser?.email ?: "—")

        binding.tvName.text = displayName
        binding.tvEmail.text = displayEmail

        binding.tvCityValue.text = profile.city.ifBlank { "—" }
        binding.tvAgeValue.text = if (profile.age > 0) profile.age.toString() else "—"
        binding.tvGenderValue.text = profile.gender.ifBlank { "—" }
        binding.tvPregnancyValue.text = when (profile.isPregnant) {
            true -> "Да"
            false -> "Нет"
            null -> "—"
        }
        binding.tvAllergiesValue.text = profile.allergies.ifBlank { "Не указано" }
        binding.tvChronicValue.text = profile.chronicDiseases.ifBlank { "Не указано" }
        binding.tvCurrentMedsValue.text = profile.currentMedications.ifBlank { "Не указано" }

        binding.editFullName.setText(profile.fullName)
        binding.editEmail.setText(displayEmail)
        binding.autoCity.setText(profile.city, false)
        binding.editAge.setText(if (profile.age > 0) profile.age.toString() else "")
        binding.editAllergies.setText(profile.allergies)
        binding.editChronic.setText(profile.chronicDiseases)
        binding.editCurrentMeds.setText(profile.currentMedications)

        when (profile.gender) {
            "Мужской" -> binding.radioMale.isChecked = true
            "Женский" -> binding.radioFemale.isChecked = true
            "Другое" -> binding.radioOther.isChecked = true
        }

        if (profile.gender == "Женский") {
            binding.layoutPregnancy.visibility = View.VISIBLE
            when (profile.isPregnant) {
                true -> binding.radioPregnantYes.isChecked = true
                false -> binding.radioPregnantNo.isChecked = true
                null -> binding.radioGroupPregnancy.clearCheck()
            }
        } else {
            binding.layoutPregnancy.visibility = View.GONE
            binding.radioGroupPregnancy.clearCheck()
        }
    }

    private fun saveProfile() {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            Toast.makeText(this, "Ошибка авторизации", Toast.LENGTH_SHORT).show()
            return
        }

        val fullName = binding.editFullName.text.toString().trim()
        val email = binding.editEmail.text.toString().trim()
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
            Toast.makeText(this, "Выберите пол", Toast.LENGTH_SHORT).show()
            return
        }

        if (gender == "Женский" && isPregnant == null) {
            Toast.makeText(this, "Укажите беременность", Toast.LENGTH_SHORT).show()
            return
        }

        binding.progressBar.visibility = View.VISIBLE
        binding.btnSaveProfile.isEnabled = false

        val profile = UserProfile(
            uid = uid,
            fullName = fullName,
            email = email,
            city = city,
            age = age,
            gender = gender,
            isPregnant = if (gender == "Женский") isPregnant else null,
            allergies = allergies,
            chronicDiseases = chronic,
            currentMedications = currentMeds,
            profileCompleted = true
        )

        firestore.collection("users")
            .document(uid)
            .set(profile)
            .addOnSuccessListener {
                binding.progressBar.visibility = View.GONE
                binding.btnSaveProfile.isEnabled = true
                Toast.makeText(this, "Профиль обновлён", Toast.LENGTH_SHORT).show()
                setEditMode(false)
                bindProfile(profile)
            }
            .addOnFailureListener { e ->
                binding.progressBar.visibility = View.GONE
                binding.btnSaveProfile.isEnabled = true
                Toast.makeText(this, "Ошибка: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            Toast.makeText(this, "Уведомления доступны", Toast.LENGTH_SHORT).show()
            return
        }

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            Toast.makeText(this, "Уведомления уже разрешены", Toast.LENGTH_SHORT).show()
            return
        }

        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
    }
}