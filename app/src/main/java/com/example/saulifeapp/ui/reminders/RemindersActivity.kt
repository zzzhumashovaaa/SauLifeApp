package com.example.saulifeapp.ui.reminders

import android.Manifest
import android.app.AlarmManager
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.saulifeapp.databinding.ActivityRemindersBinding
import java.util.Calendar

class RemindersActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRemindersBinding
    private lateinit var storage: ReminderStorage
    private lateinit var adapter: ReminderAdapter

    private val reminders = mutableListOf<ReminderItem>()

    private var selectedHour = -1
    private var selectedMinute = -1

    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRemindersBinding.inflate(layoutInflater)
        setContentView(binding.root)

        storage = ReminderStorage(this)
        reminders.addAll(storage.load())

        setupRecycler()
        setupButtons()
        renderEmptyState()
        requestNotificationPermissionIfNeeded()
    }

    private fun setupRecycler() {
        adapter = ReminderAdapter(reminders) { reminder ->
            deleteReminder(reminder)
        }

        binding.recyclerReminders.layoutManager = LinearLayoutManager(this)
        binding.recyclerReminders.adapter = adapter
    }

    private fun setupButtons() {
        binding.btnBack.setOnClickListener { finish() }

        binding.editTime.setOnClickListener {
            showTimePicker()
        }

        binding.btnAddReminder.setOnClickListener {
            addReminder()
        }
    }

    private fun showTimePicker() {
        val now = Calendar.getInstance()

        TimePickerDialog(
            this,
            { _, hourOfDay, minute ->
                selectedHour = hourOfDay
                selectedMinute = minute
                binding.editTime.setText(String.format("%02d:%02d", hourOfDay, minute))
            },
            now.get(Calendar.HOUR_OF_DAY),
            now.get(Calendar.MINUTE),
            true
        ).show()
    }

    private fun addReminder() {
        val medicineName = binding.editMedicineName.text.toString().trim()
        val dosage = binding.editDosage.text.toString().trim()

        if (medicineName.isBlank()) {
            binding.layoutMedicineName.error = "Введите название лекарства"
            return
        } else {
            binding.layoutMedicineName.error = null
        }

        if (selectedHour == -1 || selectedMinute == -1) {
            binding.layoutTime.error = "Выберите время"
            return
        } else {
            binding.layoutTime.error = null
        }

        val reminder = ReminderItem(
            id = (System.currentTimeMillis() % Int.MAX_VALUE).toInt(),
            medicineName = medicineName,
            dosage = dosage,
            hour = selectedHour,
            minute = selectedMinute,
            enabled = true
        )

        reminders.add(0, reminder)
        adapter.notifyItemInserted(0)
        storage.save(reminders)
        scheduleReminder(reminder)
        renderEmptyState()

        binding.editMedicineName.text?.clear()
        binding.editDosage.text?.clear()
        binding.editTime.text?.clear()
        selectedHour = -1
        selectedMinute = -1

        Toast.makeText(this, "Напоминание добавлено", Toast.LENGTH_SHORT).show()
    }

    private fun deleteReminder(reminder: ReminderItem) {
        cancelReminder(reminder)
        val index = reminders.indexOfFirst { it.id == reminder.id }
        if (index != -1) {
            reminders.removeAt(index)
            adapter.notifyItemRemoved(index)
            storage.save(reminders)
            renderEmptyState()
        }
    }

    private fun renderEmptyState() {
        binding.tvEmpty.visibility =
            if (reminders.isEmpty()) android.view.View.VISIBLE else android.view.View.GONE
    }

    private fun scheduleReminder(reminder: ReminderItem) {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(this, ReminderReceiver::class.java).apply {
            putExtra("medicineName", reminder.medicineName)
            putExtra("dosage", reminder.dosage)
            putExtra("reminderId", reminder.id)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            this,
            reminder.id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, reminder.hour)
            set(Calendar.MINUTE, reminder.minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)

            if (before(Calendar.getInstance())) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            pendingIntent
        )
    }

    private fun cancelReminder(reminder: ReminderItem) {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(this, ReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            this,
            reminder.id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.cancel(pendingIntent)
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        ) return

        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
    }
}