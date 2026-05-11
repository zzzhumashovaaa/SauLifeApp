package com.example.saulifeapp.ui.treatment

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.saulifeapp.R
import com.example.saulifeapp.data.remote.RetrofitClient
import com.example.saulifeapp.databinding.FragmentTreatmentBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

class TreatmentFragment : Fragment() {

    private var _binding: FragmentTreatmentBinding? = null
    private val binding get() = _binding!!

    private lateinit var homeAdapter: TreatmentAdapter
    private lateinit var currentAdapter: CurrentMedicineAdapter

    private val medicines = mutableListOf<TreatmentMedicine>()

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private var selectedFilter: TreatmentFilter = TreatmentFilter.ALL

    enum class TreatmentFilter {
        ALL,
        ACTIVE,
        FINISHED,
        EXPIRED
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTreatmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupRecyclerViews()
        setupClicks()
        setupFilterChips()
        loadMedicinesFromFirestore()
    }

    private fun setupRecyclerViews() {
        currentAdapter = CurrentMedicineAdapter(mutableListOf())
        homeAdapter = TreatmentAdapter(mutableListOf())

        binding.recyclerCurrentTreatment.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.recyclerCurrentTreatment.adapter = currentAdapter

        binding.recyclerHomeMedicine.layoutManager =
            LinearLayoutManager(requireContext())
        binding.recyclerHomeMedicine.adapter = homeAdapter
    }

    private fun setupClicks() {
        binding.btnAddMedicine.setOnClickListener {
            showAddMedicineBottomSheet()
        }
    }

    private fun setupFilterChips() {
        binding.chipAll.setOnClickListener {
            selectedFilter = TreatmentFilter.ALL
            updateUI()
        }

        binding.chipActive.setOnClickListener {
            selectedFilter = TreatmentFilter.ACTIVE
            updateUI()
        }

        binding.chipFinished.setOnClickListener {
            selectedFilter = TreatmentFilter.FINISHED
            updateUI()
        }

        binding.chipExpired.setOnClickListener {
            selectedFilter = TreatmentFilter.EXPIRED
            updateUI()
        }
    }

    private fun showAddMedicineBottomSheet() {
        val bottomSheetDialog = BottomSheetDialog(requireContext())

        val view = LayoutInflater.from(requireContext())
            .inflate(R.layout.bottom_sheet_add_medicine, null)

        val etName = view.findViewById<TextInputEditText>(R.id.etMedicineName)
        val etDosage = view.findViewById<TextInputEditText>(R.id.etDosage)
        val etQuantity = view.findViewById<TextInputEditText>(R.id.etQuantity)
        val etExpiry = view.findViewById<TextInputEditText>(R.id.etExpiryDate)
        val etCategory = view.findViewById<TextInputEditText>(R.id.etCategory)
        val etTime = view.findViewById<TextInputEditText>(R.id.etTime)
        val etTimesPerDay = view.findViewById<TextInputEditText>(R.id.etTimesPerDay)
        val cbCurrent = view.findViewById<CheckBox>(R.id.cbCurrent)
        val layoutCurrentFields = view.findViewById<View>(R.id.layoutCurrentTreatmentFields)
        val btnSave = view.findViewById<MaterialButton>(R.id.btnSaveMedicine)
        val rvSuggestions = view.findViewById<RecyclerView>(R.id.rvSuggestions)

        var dosageOptions = listOf<String>()
        var suggestionMode = "name"

        val suggestionAdapter = MedicineSuggestionAdapter { selected ->
            if (suggestionMode == "name") {
                etName.setText(selected)
                etName.setSelection(etName.text?.length ?: 0)
                etDosage.requestFocus()
            } else {
                etDosage.setText(selected)
                etDosage.setSelection(etDosage.text?.length ?: 0)
            }

            rvSuggestions.visibility = View.GONE
        }

        rvSuggestions.layoutManager = LinearLayoutManager(requireContext())
        rvSuggestions.adapter = suggestionAdapter
        rvSuggestions.visibility = View.GONE

        layoutCurrentFields.visibility = View.GONE

        cbCurrent.setOnCheckedChangeListener { _, isChecked ->
            layoutCurrentFields.visibility = if (isChecked) View.VISIBLE else View.GONE
        }

        etName.addTextChangedListener(object : TextWatcher {

            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {}

            override fun onTextChanged(
                s: CharSequence?,
                start: Int,
                before: Int,
                count: Int
            ) {
                val userInput = s.toString().trim()

                if (userInput.length < 3) {
                    rvSuggestions.visibility = View.GONE
                    return
                }

                val query = normalizeMedicineName(userInput)

                lifecycleScope.launch {
                    try {
                        val searchQuery = "generic_name:$query*"

                        Log.d("OPENFDA_TEST", "User typed: $userInput")
                        Log.d("OPENFDA_TEST", "Search query: $searchQuery")

                        val response = RetrofitClient.api.searchMedicines(searchQuery)

                        val medicineNames = response.results
                            ?.mapNotNull { result ->
                                result.generic_name ?: result.brand_name
                            }
                            ?.map { it.lowercase().replaceFirstChar { char -> char.uppercase() } }
                            ?.distinct()
                            ?: emptyList()

                        dosageOptions = response.results
                            ?.mapNotNull { result ->
                                val strength = result.active_ingredients
                                    ?.firstOrNull()
                                    ?.strength
                                    .orEmpty()

                                val form = result.dosage_form.orEmpty()

                                val dosageText = "$strength $form".trim()

                                if (dosageText.isEmpty()) null else dosageText
                            }
                            ?.distinct()
                            ?: emptyList()

                        suggestionMode = "name"
                        suggestionAdapter.updateData(medicineNames)

                        rvSuggestions.visibility =
                            if (medicineNames.isNotEmpty()) View.VISIBLE else View.GONE

                    } catch (e: Exception) {
                        rvSuggestions.visibility = View.GONE
                        Log.e("OPENFDA_TEST", "OpenFDA error", e)
                    }
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        etDosage.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus && dosageOptions.isNotEmpty()) {
                suggestionMode = "dosage"
                suggestionAdapter.updateData(dosageOptions)
                rvSuggestions.visibility = View.VISIBLE
            }
        }

        etDosage.setOnClickListener {
            if (dosageOptions.isNotEmpty()) {
                suggestionMode = "dosage"
                suggestionAdapter.updateData(dosageOptions)
                rvSuggestions.visibility = View.VISIBLE
            }
        }

        btnSave.setOnClickListener {
            val name = etName.text.toString().trim()
            val dosage = etDosage.text.toString().trim()
            val quantityText = etQuantity.text.toString().trim()
            val expiry = etExpiry.text.toString().trim()
            val category = etCategory.text.toString().trim()
            val time = etTime.text.toString().trim()
            val timesPerDayText = etTimesPerDay.text.toString().trim()
            val isCurrent = cbCurrent.isChecked

            if (name.isEmpty()) {
                etName.error = "Дәрі атауын енгізіңіз"
                return@setOnClickListener
            }

            if (quantityText.isEmpty()) {
                etQuantity.error = "Санын енгізіңіз"
                return@setOnClickListener
            }

            if (isCurrent && timesPerDayText.isEmpty()) {
                etTimesPerDay.error = "Күніне неше рет қабылдайтынын енгізіңіз"
                return@setOnClickListener
            }

            if (isCurrent && time.isEmpty()) {
                etTime.error = "Қабылдау уақытын енгізіңіз"
                return@setOnClickListener
            }

            val medicine = TreatmentMedicine(
                name = name,
                dosage = dosage,
                quantity = quantityText.toIntOrNull() ?: 0,
                expiryDate = expiry,
                category = if (category.isEmpty()) "Medicine" else category,
                isCurrent = isCurrent,
                time = if (isCurrent) time else "",
                timesPerDay = if (isCurrent) timesPerDayText.toIntOrNull() ?: 0 else 0
            )

            saveMedicineToFirestore(medicine)
            bottomSheetDialog.dismiss()
        }

        bottomSheetDialog.setContentView(view)
        bottomSheetDialog.show()
    }

    private fun normalizeMedicineName(input: String): String {
        val value = input.lowercase().trim()

        return when {
            value.startsWith("пара") -> "acetaminophen"
            value.startsWith("ибу") -> "ibuprofen"
            value.startsWith("нуро") -> "ibuprofen"
            value.startsWith("асп") -> "aspirin"
            value.startsWith("цитр") -> "aspirin"
            value.startsWith("амок") -> "amoxicillin"
            value.startsWith("лора") -> "loratadine"
            value.startsWith("цетир") -> "cetirizine"
            value.startsWith("омеп") -> "omeprazole"
            value.startsWith("азит") -> "azithromycin"
            value.startsWith("пант") -> "pantoprazole"
            value.startsWith("дикло") -> "diclofenac"
            value.startsWith("кет") -> "ketoprofen"
            value.startsWith("метф") -> "metformin"
            value.startsWith("лево") -> "levofloxacin"
            else -> input
        }
    }

    private fun saveMedicineToFirestore(medicine: TreatmentMedicine) {
        val userId = auth.currentUser?.uid

        if (userId == null) {
            Toast.makeText(requireContext(), "Пайдаланушы табылмады", Toast.LENGTH_SHORT).show()
            return
        }

        db.collection("users")
            .document(userId)
            .collection("medicines")
            .add(medicine)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Дәрі қосылды", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Сақтау қатесі", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadMedicinesFromFirestore() {
        val userId = auth.currentUser?.uid

        if (userId == null) {
            Toast.makeText(requireContext(), "Пайдаланушы табылмады", Toast.LENGTH_SHORT).show()
            return
        }

        db.collection("users")
            .document(userId)
            .collection("medicines")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Toast.makeText(requireContext(), "Жүктеу қатесі", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                medicines.clear()

                snapshot?.documents?.forEach { document ->
                    val medicine = document.toObject(TreatmentMedicine::class.java)
                    if (medicine != null) {
                        medicine.id = document.id
                        medicines.add(medicine)
                    }
                }

                updateUI()
            }
    }

    private fun updateUI() {
        val currentTreatment = medicines.filter { it.isCurrent }

        val filteredMedicines = when (selectedFilter) {
            TreatmentFilter.ALL -> medicines
            TreatmentFilter.ACTIVE -> medicines.filter { it.isCurrent }
            TreatmentFilter.FINISHED -> medicines.filter { !it.isCurrent && it.quantity == 0 }
            TreatmentFilter.EXPIRED -> medicines.filter { isExpiredMedicine(it) }
        }

        currentAdapter.updateData(currentTreatment)
        homeAdapter.updateData(filteredMedicines)

        binding.textActiveCount.text = currentTreatment.size.toString()
        binding.textNextDose.text = getNextDoseTime(currentTreatment)
        binding.textAdherence.text = calculateAdherence(currentTreatment)

        binding.textCurrentCount.text = "${currentTreatment.size} белсенді дәрі"
        binding.textHomeCount.text = "${filteredMedicines.size} дәрі"

        updateChipVisuals()
    }

    private fun getNextDoseTime(currentTreatment: List<TreatmentMedicine>): String {
        return currentTreatment
            .mapNotNull { it.time.takeIf { time -> time.isNotBlank() } }
            .firstOrNull()
            ?: "--:--"
    }

    private fun calculateAdherence(currentTreatment: List<TreatmentMedicine>): String {
        if (currentTreatment.isEmpty()) return "0%"
        return "92%"
    }

    private fun isExpiredMedicine(medicine: TreatmentMedicine): Boolean {
        return false
        // Кейін expiryDate форматын нақтылап, мерзімі өткен дәрілерді автоматты есептейміз.
    }

    private fun updateChipVisuals() {
        binding.chipAll.alpha = if (selectedFilter == TreatmentFilter.ALL) 1f else 0.55f
        binding.chipActive.alpha = if (selectedFilter == TreatmentFilter.ACTIVE) 1f else 0.55f
        binding.chipFinished.alpha = if (selectedFilter == TreatmentFilter.FINISHED) 1f else 0.55f
        binding.chipExpired.alpha = if (selectedFilter == TreatmentFilter.EXPIRED) 1f else 0.55f
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}