package com.example.saulifeapp.ui.notification

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.saulifeapp.databinding.ActivityNotificationsBinding

class NotificationsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNotificationsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityNotificationsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupBackButton()
        setupButtons()
        setupFilters()
    }

    private fun setupBackButton() {
        binding.btnBack.setOnClickListener {
            finish()
        }
    }

    private fun setupButtons() {

        binding.layoutTakeMedicine.btnTaken.setOnClickListener {
            Toast.makeText(this, "Приём подтверждён ✅", Toast.LENGTH_SHORT).show()
        }

        binding.layoutTakeMedicine.btnDelay.setOnClickListener {
            Toast.makeText(this, "Напоминание отложено ⏰", Toast.LENGTH_SHORT).show()
        }

        binding.layoutTakeMedicine.btnSkip.setOnClickListener {
            Toast.makeText(this, "Приём пропущен ❌", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupFilters() {

        binding.chipAll.setOnClickListener {
            showAll()
        }

        binding.chipPills.setOnClickListener {

            hideAll()

            binding.layoutExpiry.root.visibility = View.VISIBLE
            binding.layoutExpired.root.visibility = View.VISIBLE
            binding.layoutStock.root.visibility = View.VISIBLE
        }

        binding.chipReminders.setOnClickListener {

            hideAll()

            binding.layoutTakeMedicine.root.visibility = View.VISIBLE
            binding.layoutMissed.root.visibility = View.VISIBLE
        }

        binding.chipHealth.setOnClickListener {

            hideAll()

            binding.layoutAiWarning.root.visibility = View.VISIBLE
            binding.layoutHealthCheck.root.visibility = View.VISIBLE
        }

        binding.chipImportant.setOnClickListener {

            hideAll()

            binding.layoutCritical.root.visibility = View.VISIBLE
        }
    }

    private fun hideAll() {

        binding.layoutExpiry.root.visibility = View.GONE
        binding.layoutExpired.root.visibility = View.GONE
        binding.layoutStock.root.visibility = View.GONE

        binding.layoutTakeMedicine.root.visibility = View.GONE
        binding.layoutMissed.root.visibility = View.GONE

        binding.layoutAiWarning.root.visibility = View.GONE
        binding.layoutHealthCheck.root.visibility = View.GONE

        binding.layoutCritical.root.visibility = View.GONE

        binding.layoutAiChat.root.visibility = View.GONE
    }

    private fun showAll() {

        binding.layoutExpiry.root.visibility = View.VISIBLE
        binding.layoutExpired.root.visibility = View.VISIBLE
        binding.layoutStock.root.visibility = View.VISIBLE

        binding.layoutTakeMedicine.root.visibility = View.VISIBLE
        binding.layoutMissed.root.visibility = View.VISIBLE

        binding.layoutAiWarning.root.visibility = View.VISIBLE
        binding.layoutHealthCheck.root.visibility = View.VISIBLE

        binding.layoutCritical.root.visibility = View.VISIBLE

        binding.layoutAiChat.root.visibility = View.VISIBLE
    }
}