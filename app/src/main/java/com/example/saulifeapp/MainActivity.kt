package com.example.saulifeapp

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.saulifeapp.databinding.ActivityMainBinding
import com.example.saulifeapp.ui.chat.ChatFragment
import com.example.saulifeapp.ui.home.HomeFragment
import com.example.saulifeapp.ui.profile.ProfileFragment
import com.example.saulifeapp.ui.treatment.TreatmentFragment
import com.google.android.material.bottomsheet.BottomSheetDialog

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (savedInstanceState == null) {
            loadFragment(HomeFragment())
            binding.bottomNav.selectedItemId = R.id.nav_home
        }

        binding.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> loadFragment(HomeFragment())
                R.id.nav_treatment -> loadFragment(TreatmentFragment())
                R.id.nav_chat -> loadFragment(ChatFragment())
                R.id.nav_profile -> loadFragment(ProfileFragment())
            }
            true
        }

        binding.fabScan.setOnClickListener {
            startActivity(Intent(this, CameraActivity::class.java))
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }
}