package com.example.saulifeapp.ui.profile

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.example.saulifeapp.LoginActivity
import com.example.saulifeapp.R
import com.example.saulifeapp.databinding.FragmentProfileBinding
import com.example.saulifeapp.ui.treatment.TreatmentActivity
import com.example.saulifeapp.ui.treatment.TreatmentFragment
import com.example.saulifeapp.utils.AppPreferences
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.card.MaterialCardView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ProfileFragment : Fragment(R.layout.fragment_profile) {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private val auth by lazy { FirebaseAuth.getInstance() }
    private val firestore by lazy { FirebaseFirestore.getInstance() }

    private var cachedName: String = ""
    private var cachedEmail: String = ""
    private var cachedBloodType: String = ""
    private var cachedAllergies: String = ""
    private var cachedChronic: String = ""
    private var cachedCurrentMeds: String = ""
    private var cachedAge: Int = 0
    private var cachedGender: String = ""
    private var cachedCity: String = ""

    private val notifPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        val msg = if (isGranted) "✅ Хабарламаларға рұқсат берілді"
        else "❌ Хабарламаларға рұқсат берілмеді"
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentProfileBinding.bind(view)
        AppPreferences.applySavedTheme(requireContext())
        setupClicks()
    }

    override fun onResume() {
        super.onResume()
        loadProfile()
    }

    private fun setupClicks() {
        binding.cardEditProfile.setOnClickListener {
            startActivity(Intent(requireContext(), EditProfileActivity::class.java))
        }

        // ✅ Менің емделуім → меню бөліміндегі TreatmentActivity-ге ауысады
        binding.cardTreatment.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.container, TreatmentFragment())
                .addToBackStack(null)
                .commit()
        }
        // ✅ Денсаулық тарихы → денсаулық ақпараты (қан тобы, аллергия, т.б.) Extra арқылы беріледі
        binding.cardHistory.setOnClickListener {
            val intent = Intent(requireContext(), HealthHistoryActivity::class.java).apply {
                putExtra(HealthHistoryActivity.EXTRA_BLOOD_TYPE,   cachedBloodType)
                putExtra(HealthHistoryActivity.EXTRA_ALLERGIES,    cachedAllergies)
                putExtra(HealthHistoryActivity.EXTRA_CHRONIC,      cachedChronic)
                putExtra(HealthHistoryActivity.EXTRA_CURRENT_MEDS, cachedCurrentMeds)
                putExtra(HealthHistoryActivity.EXTRA_NAME,         cachedName)
                putExtra(HealthHistoryActivity.EXTRA_AGE,          cachedAge)
                putExtra(HealthHistoryActivity.EXTRA_GENDER,       cachedGender)
            }
            startActivity(intent)
        }

        // ❌ cardOrderHistory (Тарих) — жойылды, click handler жоқ

        binding.cardNotifications.setOnClickListener { showNotificationsSheet() }
        binding.cardSettings.setOnClickListener { showSettingsSheet() }

        binding.btnLogout.setOnClickListener {
            auth.signOut()
            startActivity(Intent(requireContext(), LoginActivity::class.java))
            requireActivity().finishAffinity()
        }

        binding.btnExportPdf.setOnClickListener { exportAsPdf() }
        binding.btnExportCsv.setOnClickListener { exportAsCsv() }
    }

    // ══════════════════════════════════════════════════════
    //  ХАБАРЛАМАЛАР BOTTOM SHEET
    // ══════════════════════════════════════════════════════

    private fun showNotificationsSheet() {
        val sheet = BottomSheetDialog(requireContext(), R.style.BottomSheetDialogTheme)
        val view = LayoutInflater.from(requireContext())
            .inflate(R.layout.bottom_sheet_notifications, null)
        sheet.setContentView(view)

        val switchPush     = view.findViewById<android.widget.Switch>(R.id.switchPushNotif)
        val switchReminder = view.findViewById<android.widget.Switch>(R.id.switchMedReminder)
        val switchAppoint  = view.findViewById<android.widget.Switch>(R.id.switchAppointments)
        val switchNews     = view.findViewById<android.widget.Switch>(R.id.switchHealthNews)
        val btnSave        = view.findViewById<android.widget.Button>(R.id.btnSaveNotif)

        val hasPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                requireContext(), Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else true

        switchPush.isChecked = hasPermission
        switchPush.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) requestNotificationPermission()
        }

        val prefs = requireContext().getSharedPreferences("notif_prefs", 0)
        switchReminder.isChecked = prefs.getBoolean("med_reminder", true)
        switchAppoint.isChecked  = prefs.getBoolean("appointments", true)
        switchNews.isChecked     = prefs.getBoolean("health_news", false)

        btnSave.setOnClickListener {
            prefs.edit()
                .putBoolean("med_reminder", switchReminder.isChecked)
                .putBoolean("appointments", switchAppoint.isChecked)
                .putBoolean("health_news", switchNews.isChecked)
                .apply()
            Toast.makeText(requireContext(), "✅ Сақталды", Toast.LENGTH_SHORT).show()
            sheet.dismiss()
        }
        sheet.show()
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    requireContext(), Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    Toast.makeText(requireContext(), "✅ Рұқсат бұрыннан берілген", Toast.LENGTH_SHORT).show()
                }
                shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) -> {
                    AlertDialog.Builder(requireContext())
                        .setTitle("Хабарламаларға рұқсат")
                        .setMessage("Дәрі еске салуларын алу үшін хабарламаларға рұқсат беру қажет.")
                        .setPositiveButton("Рұқсат беру") { _, _ ->
                            notifPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                        .setNegativeButton("Болдырмау", null)
                        .show()
                }
                else -> notifPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    // ══════════════════════════════════════════════════════
    //  БАПТАУЛАР BOTTOM SHEET
    // ══════════════════════════════════════════════════════

    private fun showSettingsSheet() {
        val sheet = BottomSheetDialog(requireContext(), R.style.BottomSheetDialogTheme)
        val view = LayoutInflater.from(requireContext())
            .inflate(R.layout.bottom_sheet_settings, null)
        sheet.setContentView(view)

        val cardKk = view.findViewById<MaterialCardView>(R.id.cardLangKk)
        val cardRu = view.findViewById<MaterialCardView>(R.id.cardLangRu)
        val cardEn = view.findViewById<MaterialCardView>(R.id.cardLangEn)

        val langPairs = listOf(
            cardKk to AppPreferences.LANGUAGE_KK,
            cardRu to AppPreferences.LANGUAGE_RU,
            cardEn to AppPreferences.LANGUAGE_EN
        )
        applyCardSelection(langPairs, AppPreferences.getLanguage(requireContext()))
        langPairs.forEach { (card, code) ->
            card.setOnClickListener {
                applyCardSelection(langPairs, code)
                AppPreferences.setLanguage(requireContext(), code)
                sheet.dismiss()
                AlertDialog.Builder(requireContext())
                    .setTitle(getString(R.string.language_saved_title))
                    .setMessage(getString(R.string.language_saved_message))
                    .setPositiveButton(getString(R.string.ok)) { _, _ -> requireActivity().recreate() }
                    .show()
            }
        }

        val cardSystem = view.findViewById<MaterialCardView>(R.id.cardThemeSystem)
        val cardLight  = view.findViewById<MaterialCardView>(R.id.cardThemeLight)
        val cardDark   = view.findViewById<MaterialCardView>(R.id.cardThemeDark)

        val themePairs = listOf(
            cardSystem to AppPreferences.THEME_SYSTEM,
            cardLight  to AppPreferences.THEME_LIGHT,
            cardDark   to AppPreferences.THEME_DARK
        )
        applyCardSelection(themePairs, AppPreferences.getTheme(requireContext()))
        themePairs.forEach { (card, code) ->
            card.setOnClickListener {
                applyCardSelection(themePairs, code)
                AppPreferences.setTheme(requireContext(), code)
            }
        }

        view.findViewById<MaterialCardView>(R.id.cardGoNotifications)?.setOnClickListener {
            sheet.dismiss()
            showNotificationsSheet()
        }
        sheet.show()
    }

    private fun applyCardSelection(pairs: List<Pair<MaterialCardView, String>>, selected: String) {
        val primaryColor = resources.getColor(R.color.teal_dark, null)
        pairs.forEach { (card, code) ->
            if (code == selected) {
                card.strokeColor = primaryColor
                card.strokeWidth = 3
                card.cardElevation = 6f
            } else {
                card.strokeColor = Color.parseColor("#E8ECF2")
                card.strokeWidth = 2
                card.cardElevation = 0f
            }
        }
    }

    // ══════════════════════════════════════════════════════
    //  PROFILE LOAD
    // ══════════════════════════════════════════════════════

    private fun loadProfile() {
        val user = auth.currentUser ?: return
        binding.progressBar.visibility = View.VISIBLE

        firestore.collection("users").document(user.uid).get()
            .addOnSuccessListener { document ->
                binding.progressBar.visibility = View.GONE

                val finalName = document.getString("fullName")
                    .takeIf { !it.isNullOrBlank() }
                    ?: user.displayName
                    ?: getString(R.string.default_user)

                val finalEmail = document.getString("email")
                    .takeIf { !it.isNullOrBlank() }
                    ?: user.email
                    ?: getString(R.string.email_not_set)

                binding.tvName.text     = finalName
                binding.tvEmail.text    = finalEmail
                binding.tvInitials.text = getInitials(finalName)

                val completed = document.getBoolean("profileCompleted") ?: false
                binding.tvProfileStatus.text =
                    if (completed) getString(R.string.profile_completed)
                    else getString(R.string.profile_not_completed)

                val bloodType   = document.getString("bloodType").orEmpty()
                val allergies   = document.getString("allergies").orEmpty()
                val chronic     = document.getString("chronicDiseases").orEmpty()
                val currentMeds = document.getString("currentMedications").orEmpty()

                // Cache — cardHistory Intent үшін де қажет
                cachedName        = finalName
                cachedEmail       = finalEmail
                cachedBloodType   = bloodType
                cachedAllergies   = allergies
                cachedChronic     = chronic
                cachedCurrentMeds = currentMeds
                cachedAge         = document.getLong("age")?.toInt() ?: 0
                cachedGender      = document.getString("gender").orEmpty()
                cachedCity        = document.getString("city").orEmpty()

                bindHealthCard(bloodType, allergies, chronic, currentMeds)
            }
            .addOnFailureListener {
                binding.progressBar.visibility = View.GONE
                binding.tvName.text     = getString(R.string.default_user)
                binding.tvEmail.text    = user.email ?: getString(R.string.email_not_set)
                binding.tvInitials.text = getString(R.string.default_initial)
            }
    }

    private fun bindHealthCard(
        bloodType: String,
        allergies: String,
        chronic: String,
        currentMeds: String
    ) {
        binding.tvHealthBloodType.text   = bloodType.ifBlank { "—" }
        binding.tvHealthAllergies.text   = allergies.ifBlank { "—" }
        binding.tvHealthChronic.text     = chronic.ifBlank { "—" }
        binding.tvHealthCurrentMeds.text = currentMeds.ifBlank { "—" }
        binding.cardHealthInfo.visibility = View.VISIBLE
    }

    // ══════════════════════════════════════════════════════
    //  PDF / CSV EXPORT
    // ══════════════════════════════════════════════════════

    private fun exportAsPdf() {
        try {
            val document = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
            val page = document.startPage(pageInfo)
            drawPdfContent(page.canvas)
            document.finishPage(page)
            val file = createOutputFile("health_report_${timestamp()}.pdf")
            FileOutputStream(file).use { document.writeTo(it) }
            document.close()
            shareFile(file, "application/pdf")
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "PDF қатесі: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun drawPdfContent(canvas: Canvas) {
        val primaryColor = Color.parseColor("#0F9B8E")
        val darkColor    = Color.parseColor("#0D1B2A")
        val grayColor    = Color.parseColor("#9AAAB8")
        val bgColor      = Color.parseColor("#F0FAFA")

        canvas.drawRect(0f, 0f, 595f, 110f, Paint().apply { color = primaryColor })
        canvas.drawText("SauLife", 36f, 52f, Paint().apply {
            color = Color.WHITE; textSize = 26f; isFakeBoldText = true; isAntiAlias = true })
        canvas.drawText("Денсаулық туралы медициналық есеп", 36f, 76f, Paint().apply {
            color = Color.parseColor("#B2E8E4"); textSize = 13f; isAntiAlias = true })
        canvas.drawText("Жасалған күні: ${formattedDate()}", 36f, 98f, Paint().apply {
            color = Color.parseColor("#B2E8E4"); textSize = 11f; isAntiAlias = true })

        canvas.drawRoundRect(36f, 126f, 559f, 212f, 12f, 12f, Paint().apply { color = bgColor })
        val lp = Paint().apply { color = grayColor; textSize = 11f; isAntiAlias = true }
        val vp = Paint().apply { color = darkColor; textSize = 14f; isFakeBoldText = true; isAntiAlias = true }

        canvas.drawText("АТЫ-ЖӨНІ", 52f, 148f, lp)
        canvas.drawText(cachedName.ifBlank { "—" }, 52f, 168f, vp)
        canvas.drawText("EMAIL", 300f, 148f, lp)
        canvas.drawText(cachedEmail.ifBlank { "—" }, 300f, 168f, vp)
        canvas.drawText("ЖАСЫ", 52f, 192f, lp)
        canvas.drawText(if (cachedAge > 0) "${cachedAge} жас" else "—", 52f, 206f, vp)
        canvas.drawText("ЖЫНЫСЫ", 300f, 192f, lp)
        canvas.drawText(cachedGender.ifBlank { "—" }, 300f, 206f, vp)

        var y = 238f
        fun drawSection(emoji: String, title: String, value: String) {
            val h = if (value.length > 60) 100f else 72f
            canvas.drawRoundRect(36f, y, 559f, y + h, 10f, 10f, Paint().apply { color = Color.WHITE })
            canvas.drawRoundRect(36f, y, 559f, y + h, 10f, 10f, Paint().apply {
                color = Color.parseColor("#E8ECF2"); style = Paint.Style.STROKE; strokeWidth = 1f })
            canvas.drawRoundRect(36f, y, 44f, y + h, 4f, 4f, Paint().apply { color = primaryColor })
            canvas.drawText(emoji, 56f, y + 26f, Paint().apply { textSize = 20f; isAntiAlias = true })
            canvas.drawText(title, 88f, y + 22f, lp)
            val sv = Paint().apply { color = darkColor; textSize = 14f; isAntiAlias = true }
            if (value.length > 60) {
                canvas.drawText(value.take(60), 88f, y + 44f, sv)
                canvas.drawText(value.drop(60).take(60), 88f, y + 62f, sv)
            } else canvas.drawText(value.ifBlank { "—" }, 88f, y + 44f, sv)
            y += h + 10f
        }

        canvas.drawRoundRect(36f, y, 559f, y + 60f, 10f, 10f, Paint().apply { color = Color.WHITE })
        canvas.drawRoundRect(36f, y, 559f, y + 60f, 10f, 10f, Paint().apply {
            color = Color.parseColor("#E8ECF2"); style = Paint.Style.STROKE; strokeWidth = 1f })
        canvas.drawRoundRect(36f, y, 44f, y + 60f, 4f, 4f, Paint().apply { color = primaryColor })
        canvas.drawText("🩸", 56f, y + 26f, Paint().apply { textSize = 20f; isAntiAlias = true })
        canvas.drawText("ҚАН ТОБЫ", 88f, y + 22f, lp)
        val btText = cachedBloodType.ifBlank { "—" }
        canvas.drawRoundRect(88f, y + 30f, 88f + btText.length * 11f + 20f, y + 54f, 8f, 8f,
            Paint().apply { color = primaryColor })
        canvas.drawText(btText, 98f, y + 48f, Paint().apply {
            color = Color.WHITE; textSize = 15f; isFakeBoldText = true; isAntiAlias = true })
        y += 70f

        drawSection("⚠️", "АЛЛЕРГИЯЛАР", cachedAllergies)
        drawSection("🫀", "СОЗЫЛМАЛЫ АУРУЛАР", cachedChronic)
        drawSection("💊", "АҒЫМДАҒЫ ДӘРІЛЕР", cachedCurrentMeds)

        canvas.drawText("* Бұл есеп SauLife қолданбасы арқылы автоматты түрде жасалды",
            36f, 820f, Paint().apply { color = grayColor; textSize = 10f; isAntiAlias = true })
    }

    private fun exportAsCsv() {
        try {
            val file = createOutputFile("health_report_${timestamp()}.csv")
            val sb = StringBuilder()
            sb.appendLine("Өріс,Мәні")
            sb.appendLine("Аты-жөні,\"${escapeCsv(cachedName)}\"")
            sb.appendLine("Email,\"${escapeCsv(cachedEmail)}\"")
            sb.appendLine("Жасы,${cachedAge}")
            sb.appendLine("Жынысы,\"${escapeCsv(cachedGender)}\"")
            sb.appendLine("Қала,\"${escapeCsv(cachedCity)}\"")
            sb.appendLine("Қан тобы,\"${escapeCsv(cachedBloodType)}\"")
            sb.appendLine("Аллергиялар,\"${escapeCsv(cachedAllergies)}\"")
            sb.appendLine("Созылмалы аурулар,\"${escapeCsv(cachedChronic)}\"")
            sb.appendLine("Ағымдағы дәрілер,\"${escapeCsv(cachedCurrentMeds)}\"")
            sb.appendLine("Жасалған күні,\"${formattedDate()}\"")
            file.writeText(sb.toString(), Charsets.UTF_8)
            shareFile(file, "text/csv")
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "CSV қатесі: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun escapeCsv(v: String) = v.replace("\"", "\"\"")

    private fun createOutputFile(fileName: String): File {
        val dir = requireContext().getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
            ?: requireContext().filesDir
        dir.mkdirs()
        return File(dir, fileName)
    }

    private fun shareFile(file: File, mimeType: String) {
        val uri: Uri = FileProvider.getUriForFile(
            requireContext(), "${requireContext().packageName}.fileprovider", file)
        startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }, "Файлды ашу / сақтау"))
    }

    private fun timestamp() = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    private fun formattedDate() = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).format(Date())

    private fun getInitials(fullName: String): String {
        val parts = fullName.trim().split(" ").filter { it.isNotBlank() }
        return when {
            parts.isEmpty() -> getString(R.string.default_initial)
            parts.size == 1 -> parts[0].take(1).uppercase()
            else -> (parts[0].take(1) + parts[1].take(1)).uppercase()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}