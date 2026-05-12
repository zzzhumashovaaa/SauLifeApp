package com.example.saulifeapp.ui.profile

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.saulifeapp.R

class HealthHistoryActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_BLOOD_TYPE = "extra_blood_type"
        const val EXTRA_ALLERGIES = "extra_allergies"
        const val EXTRA_CHRONIC = "extra_chronic"
        const val EXTRA_CURRENT_MEDS = "extra_current_meds"
        const val EXTRA_NAME = "extra_name"
        const val EXTRA_AGE = "extra_age"
        const val EXTRA_GENDER = "extra_gender"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_health_history)

        val tvName = findViewById<TextView>(R.id.tvName)
        val tvAge = findViewById<TextView>(R.id.tvAge)
        val tvGender = findViewById<TextView>(R.id.tvGender)

        val tvBlood = findViewById<TextView>(R.id.tvBloodType)
        val tvAllergies = findViewById<TextView>(R.id.tvAllergies)
        val tvChronic = findViewById<TextView>(R.id.tvChronic)
        val tvMeds = findViewById<TextView>(R.id.tvCurrentMeds)

        // Intent data
        val name = intent.getStringExtra(EXTRA_NAME) ?: "—"
        val age = intent.getIntExtra(EXTRA_AGE, 0)
        val gender = intent.getStringExtra(EXTRA_GENDER) ?: "—"

        val blood = intent.getStringExtra(EXTRA_BLOOD_TYPE) ?: "—"
        val allergies = intent.getStringExtra(EXTRA_ALLERGIES) ?: "—"
        val chronic = intent.getStringExtra(EXTRA_CHRONIC) ?: "—"
        val meds = intent.getStringExtra(EXTRA_CURRENT_MEDS) ?: "—"

        // Bind UI
        tvName.text = name
        tvAge.text = if (age > 0) "$age жас" else "—"
        tvGender.text = gender

        tvBlood.text = blood
        tvAllergies.text = allergies
        tvChronic.text = chronic
        tvMeds.text = meds
    }
}