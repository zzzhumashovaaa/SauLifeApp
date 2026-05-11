package com.example.saulifeapp.ui.profile

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.example.saulifeapp.LoginRegisterActivity
import com.example.saulifeapp.R
import com.example.saulifeapp.databinding.FragmentProfileBinding
import com.example.saulifeapp.utils.AppPreferences
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProfileFragment : Fragment(R.layout.fragment_profile) {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private val auth by lazy { FirebaseAuth.getInstance() }
    private val firestore by lazy { FirebaseFirestore.getInstance() }

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

        binding.cardTreatment.setOnClickListener {
            // later: open Treatment screen
        }

        binding.cardNotifications.setOnClickListener {
            // later: open Notifications screen
        }

        binding.cardHistory.setOnClickListener {
            // later: open Scan / order history screen
        }

        binding.cardSettings.setOnClickListener {
            showSettingsDialog()
        }

        binding.btnLogout.setOnClickListener {
            auth.signOut()
            startActivity(Intent(requireContext(), LoginRegisterActivity::class.java))
            requireActivity().finishAffinity()
        }
    }

    private fun showSettingsDialog() {
        val options = arrayOf(
            getString(R.string.settings_language),
            getString(R.string.settings_theme)
        )

        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.settings_title))
            .setItems(options) { _, which ->
                when (which) {
                    0 -> showLanguageDialog()
                    1 -> showThemeDialog()
                }
            }
            .show()
    }

    private fun showLanguageDialog() {
        val languages = arrayOf(
            getString(R.string.language_kazakh),
            getString(R.string.language_russian),
            getString(R.string.language_english)
        )
        val codes = arrayOf(
            AppPreferences.LANGUAGE_KK,
            AppPreferences.LANGUAGE_RU,
            AppPreferences.LANGUAGE_EN
        )
        val current = AppPreferences.getLanguage(requireContext())
        val checkedIndex = codes.indexOf(current).takeIf { it >= 0 } ?: 0

        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.choose_language))
            .setSingleChoiceItems(languages, checkedIndex) { dialog, which ->
                AppPreferences.setLanguage(requireContext(), codes[which])
                dialog.dismiss()
                AlertDialog.Builder(requireContext())
                    .setTitle(getString(R.string.language_saved_title))
                    .setMessage(getString(R.string.language_saved_message))
                    .setPositiveButton(getString(R.string.ok)) { _, _ -> requireActivity().recreate() }
                    .show()
            }
            .show()
    }

    private fun showThemeDialog() {
        val themes = arrayOf(
            getString(R.string.theme_system),
            getString(R.string.theme_light),
            getString(R.string.theme_dark)
        )
        val codes = arrayOf(
            AppPreferences.THEME_SYSTEM,
            AppPreferences.THEME_LIGHT,
            AppPreferences.THEME_DARK
        )
        val current = AppPreferences.getTheme(requireContext())
        val checkedIndex = codes.indexOf(current).takeIf { it >= 0 } ?: 0

        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.choose_theme))
            .setSingleChoiceItems(themes, checkedIndex) { dialog, which ->
                AppPreferences.setTheme(requireContext(), codes[which])
                dialog.dismiss()
            }
            .show()
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

                val firestoreName = document.getString("fullName").orEmpty()
                val firestoreEmail = document.getString("email").orEmpty()

                val finalName = when {
                    firestoreName.isNotBlank() -> firestoreName
                    !user.displayName.isNullOrBlank() -> user.displayName!!
                    else -> getString(R.string.default_user)
                }

                val finalEmail = when {
                    firestoreEmail.isNotBlank() -> firestoreEmail
                    !user.email.isNullOrBlank() -> user.email!!
                    else -> getString(R.string.email_not_set)
                }

                binding.tvName.text = finalName
                binding.tvEmail.text = finalEmail
                binding.tvInitials.text = getInitials(finalName)

                val completed = document.getBoolean("profileCompleted") ?: false
                binding.tvProfileStatus.text =
                    if (completed) getString(R.string.profile_completed) else getString(R.string.profile_not_completed)
            }
            .addOnFailureListener {
                binding.progressBar.visibility = View.GONE
                binding.tvName.text = getString(R.string.default_user)
                binding.tvEmail.text = user.email ?: getString(R.string.email_not_set)
                binding.tvInitials.text = getString(R.string.default_initial)
            }
    }

    private fun getInitials(fullName: String): String {
        val parts = fullName.trim().split(" ").filter { it.isNotBlank() }

        return when {
            parts.isEmpty() -> getString(R.string.default_initial)
            parts.size == 1 -> parts.first().take(1).uppercase()
            else -> (parts[0].take(1) + parts[1].take(1)).uppercase()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
