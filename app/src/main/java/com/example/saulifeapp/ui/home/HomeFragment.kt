package com.example.saulifeapp.ui.home

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.saulifeapp.CameraActivity
import com.example.saulifeapp.Product
import com.example.saulifeapp.ProductAdapter
import com.example.saulifeapp.R
import com.example.saulifeapp.databinding.FragmentHomeBinding
import com.example.saulifeapp.news.NewsAdapter
import com.example.saulifeapp.news.NewsRepository
import com.example.saulifeapp.ui.notification.NotificationsActivity
import com.example.saulifeapp.pharmacy.PharmacyWebActivity
import com.example.saulifeapp.ui.reminders.RemindersActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.coroutines.cancellation.CancellationException
import com.example.saulifeapp.ui.medicine.MedicineDetailActivity
import com.example.saulifeapp.ui.medicine.MedicineLocalRepository
import com.example.saulifeapp.ui.medicine.MedicineSearchAdapter
import com.example.saulifeapp.ui.treatment.TreatmentFragment

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var medicineSearchAdapter: MedicineSearchAdapter

    private lateinit var quickActionAdapter: HomeQuickActionAdapter
    private lateinit var newsAdapter: NewsAdapter
    private lateinit var healthNewsAdapter: HealthNewsAdapter
    private lateinit var cartAdapter: ProductAdapter


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupMedicineSearch()
        setupHeader()
        setupClicks()
        setupQuickActions()
        setupHealthInsights()
        setupNews()
        setupPopularProducts()
        loadNews()
    }

    private fun setupHeader() {
        binding.textGreeting.text = "Қайырлы кеш"
        binding.textGreetingSubtitle.text = "Бүгін өзіңізді қалай сезініп тұрсыз?"

        loadUserName()

        binding.textActiveMedicines.text = "3"
        binding.textNextMedicine.text = "Парацетамол — 21:00"
        binding.textCourseProgress.text = "5/10 күн"
        binding.textAdherence.text = "86%"
    }
    private fun loadUserName() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        FirebaseFirestore.getInstance()
            .collection("users")
            .document(uid)
            .get()
            .addOnSuccessListener { document ->
                if (!isAdded || _binding == null) return@addOnSuccessListener

                val fullName = document.getString("fullName").orEmpty()
                val firstName = fullName.split(" ").firstOrNull().orEmpty()

                binding.textGreeting.text = if (firstName.isNotBlank()) {
                    "Қайырлы кеш, $firstName"
                } else {
                    "Қайырлы кеш"
                }
            }
    }
    private fun setupMedicineSearch() {
        binding.btnSearchIcon.setOnClickListener {

            val query = binding.editSearch.text.toString().trim()

            val result =
                MedicineLocalRepository.search(query)

            if (query.isNotEmpty() && result.isNotEmpty()) {

                medicineSearchAdapter.updateList(result)

                binding.recyclerMedicineSearchResults.visibility =
                    View.VISIBLE

            } else {

                binding.recyclerMedicineSearchResults.visibility =
                    View.GONE
            }
        }
        medicineSearchAdapter = MedicineSearchAdapter(emptyList()) { medicine ->
            val intent = Intent(requireContext(), MedicineDetailActivity::class.java)
            intent.putExtra("medicine_id", medicine.id)
            startActivity(intent)

            binding.recyclerMedicineSearchResults.visibility = View.GONE
            binding.editSearch.text.clear()
        }

        binding.recyclerMedicineSearchResults.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = medicineSearchAdapter
        }

        binding.editSearch.setOnEditorActionListener { _, _, _ ->
            val query = binding.editSearch.text.toString().trim()
            val result = MedicineLocalRepository.search(query)

            if (query.isNotEmpty() && result.isNotEmpty()) {
                medicineSearchAdapter.updateList(result)
                binding.recyclerMedicineSearchResults.visibility = View.VISIBLE
            } else {
                binding.recyclerMedicineSearchResults.visibility = View.GONE
            }

            true
        }
    }

    private fun setupQuickActions() {
        val actions = listOf(
            HomeQuickAction("Сканерлеу", "Дәріні тану", R.drawable.ic_scan, HomeQuickActionType.SCAN),
            HomeQuickAction("Аптека", "Үй аптечкасы", R.drawable.ic_pharmacy, HomeQuickActionType.PHARMACY),
            HomeQuickAction("AI көмекші", "Кеңес алу", R.drawable.ic_ai, HomeQuickActionType.AI),
            HomeQuickAction("Еске салу", "Қабылдау уақыты", R.drawable.ic_reminder, HomeQuickActionType.REMINDER),
            HomeQuickAction("Рецепт", "Фото жүктеу", R.drawable.ic_prescription, HomeQuickActionType.PRESCRIPTION)
        )

        quickActionAdapter = HomeQuickActionAdapter(actions) { action ->
            when (action.type) {
                HomeQuickActionType.SCAN -> startActivity(Intent(requireContext(), CameraActivity::class.java))
                HomeQuickActionType.PHARMACY -> startActivity(Intent(requireContext(),
                    TreatmentFragment::class.java))
                HomeQuickActionType.AI -> Toast.makeText(requireContext(), "AI чат төменгі мәзірде ашылады", Toast.LENGTH_SHORT).show()
                HomeQuickActionType.REMINDER -> startActivity(Intent(requireContext(), RemindersActivity::class.java))
                HomeQuickActionType.PRESCRIPTION -> Toast.makeText(requireContext(), "Рецепт сканері келесі этапта қосылады", Toast.LENGTH_SHORT).show()
            }
        }

        binding.recyclerQuickActions.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = quickActionAdapter
            setHasFixedSize(true)
        }
    }

    private fun setupHealthInsights() {
        val items = listOf(
            HealthNewsItem(
                title = "2 дәрінің әсері ұқсас болуы мүмкін",
                description = "AI кейін сіздің treatment тізіміңізге қарап нақты ескерту береді.",
                tag = "AI"
            ),
            HealthNewsItem(
                title = "D витаминін қабылдауды ұмытпаңыз",
                description = "Еске салғыш қоссаңыз, қабылдау уақыты Home бетінде көрінеді.",
                tag = "Reminder"
            ),
            HealthNewsItem(
                title = "Ибупрофен кей аптекада арзанырақ болуы мүмкін",
                description = "Келесі этапта бағаларды poisklekarstv.kz арқылы салыстырамыз.",
                tag = "Pharmacy"
            )
        )

        healthNewsAdapter = HealthNewsAdapter(items) { item ->
            Toast.makeText(requireContext(), item.title, Toast.LENGTH_SHORT).show()
        }

        binding.recyclerHealthInsights.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = healthNewsAdapter
            setHasFixedSize(true)
        }
    }

    private fun setupNews() {
        newsAdapter = NewsAdapter(emptyList())

        binding.recyclerNews.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = newsAdapter
            setHasFixedSize(true)
        }
    }

    private fun loadNews() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val news = withContext(Dispatchers.IO) {
                    NewsRepository.getHealthNews()
                }

                if (!isAdded || _binding == null) return@launch

                binding.progressNews.visibility = View.GONE

                if (news.isNotEmpty()) {
                    newsAdapter.updateNews(news.shuffled().take(10))
                    binding.recyclerNews.visibility = View.VISIBLE
                } else {
                    binding.recyclerNews.visibility = View.GONE
                }

            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e("NEWS_ERROR", "Ошибка загрузки новостей", e)

                if (!isAdded || _binding == null) return@launch

                binding.progressNews.visibility = View.GONE
                binding.recyclerNews.visibility = View.GONE
            }
        }
    }

    private fun setupPopularProducts() {

        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        FirebaseFirestore.getInstance()
            .collection("users")
            .document(uid)
            .collection("cart")
            .get()
            .addOnSuccessListener { documents ->

                val productList = mutableListOf<Product>()

                for (doc in documents) {

                    val name = doc.getString("name") ?: ""
                    val type = doc.getString("type") ?: ""
                    val dosage = doc.getString("dosage") ?: ""
                    val price = doc.getDouble("price") ?: 0.0

                    productList.add(
                        Product(
                            name = "$name - $type",
                            volume = dosage,
                            price = price,
                            oldPrice = null,
                            imageRes = R.drawable.logo_icon
                        )
                    )
                }

                cartAdapter = ProductAdapter(productList) { product ->

                    Toast.makeText(
                        requireContext(),
                        product.name,
                        Toast.LENGTH_SHORT
                    ).show()
                }

                binding.recyclerCart.apply {

                    layoutManager = LinearLayoutManager(
                        requireContext(),
                        LinearLayoutManager.HORIZONTAL,
                        false
                    )

                    adapter = cartAdapter
                    setHasFixedSize(true)
                }
            }
    }


    private fun setupClicks() {
        binding.btnNotifications.setOnClickListener {
            startActivity(Intent(requireContext(), NotificationsActivity::class.java))
        }

        binding.layoutSearch.setOnClickListener {
            binding.editSearch.requestFocus()
        }

        binding.btnShopNow.setOnClickListener {
        }


        binding.textSeeAllCart.setOnClickListener {
            Toast.makeText(requireContext(), "Толық себет кейін", Toast.LENGTH_SHORT).show()
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
