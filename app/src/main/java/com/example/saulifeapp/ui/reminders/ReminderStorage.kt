package com.example.saulifeapp.ui.reminders

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

class ReminderStorage(context: Context) {

    private val prefs = context.getSharedPreferences("saulife_reminders", Context.MODE_PRIVATE)

    fun save(reminders: List<ReminderItem>) {
        val array = JSONArray()
        reminders.forEach { reminder ->
            val obj = JSONObject()
            obj.put("id", reminder.id)
            obj.put("medicineName", reminder.medicineName)
            obj.put("dosage", reminder.dosage)
            obj.put("hour", reminder.hour)
            obj.put("minute", reminder.minute)
            obj.put("enabled", reminder.enabled)
            array.put(obj)
        }
        prefs.edit().putString("items", array.toString()).apply()
    }

    fun load(): MutableList<ReminderItem> {
        val raw = prefs.getString("items", null) ?: return mutableListOf()
        val result = mutableListOf<ReminderItem>()
        val array = JSONArray(raw)

        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            result.add(
                ReminderItem(
                    id = obj.getInt("id"),
                    medicineName = obj.getString("medicineName"),
                    dosage = obj.getString("dosage"),
                    hour = obj.getInt("hour"),
                    minute = obj.getInt("minute"),
                    enabled = obj.getBoolean("enabled")
                )
            )
        }
        return result
    }
}