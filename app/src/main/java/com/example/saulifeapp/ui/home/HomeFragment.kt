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
import com.example.saulifeapp.cart.CartRepository
import com.example.saulifeapp.databinding.FragmentHomeBinding
import com.example.saulifeapp.news.NewsAdapter
import com.example.saulifeapp.news.NewsRepository
import com.example.saulifeapp.pharmacy.PharmacyWebActivity
import com.example.saulifeapp.ui.reminders.RemindersActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.coroutines.cancellation.CancellationException

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var quickActionAdapter: HomeQuickActionAdapter
    private lateinit var newsAdapter: NewsAdapter
    private lateinit var healthNewsAdapter: HealthNewsAdapter
    private lateinit var cartAdapter: ProductAdapter
    private lateinit var saleAdapter: ProductAdapter

    private val cartRepository = CartRepository()

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

        setupHeader()
        setupClicks()
        setupQuickActions()
        setupHealthInsights()
        setupNews()
        setupPopularProducts()
        setupSaleProducts()
        loadNews()
    }

    private fun setupHeader() {
        binding.textGreeting.text = "Қайырлы кеш, Айжан"
        binding.textGreetingSubtitle.text = "Бүгін өзіңізді қалай сезініп тұрсыз?"

        // Кейін бұл мәндерді Firestore-дан treatment/reminders деректері арқылы есептейміз.
        binding.textActiveMedicines.text = "3"
        binding.textNextMedicine.text = "Парацетамол — 21:00"
        binding.textCourseProgress.text = "5/10 күн"
        binding.textAdherence.text = "86%"
    }

    private fun setupQuickActions() {
        val actions = listOf(
            HomeQuickAction("Сканерлеу", "Дәріні тану", R.drawable.ic_scan, HomeQuickActionType.SCAN),
            HomeQuickAction("Аптека", "Бағаны табу", R.drawable.ic_pharmacy, HomeQuickActionType.PHARMACY),
            HomeQuickAction("AI көмекші", "Кеңес алу", R.drawable.ic_ai, HomeQuickActionType.AI),
            HomeQuickAction("Еске салу", "Қабылдау уақыты", R.drawable.ic_reminder, HomeQuickActionType.REMINDER),
            HomeQuickAction("Рецепт", "Фото жүктеу", R.drawable.ic_prescription, HomeQuickActionType.PRESCRIPTION)
        )

        quickActionAdapter = HomeQuickActionAdapter(actions) { action ->
            when (action.type) {
                HomeQuickActionType.SCAN -> startActivity(Intent(requireContext(), CameraActivity::class.java))
                HomeQuickActionType.PHARMACY -> openPharmacySearch()
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
        val popularList = listOf(
            Product("Panadol", "20pcs", 15.99, null, R.drawable.panadol),
            Product("Bodrex Herbal", "100ml", 7.99, null, R.drawable.bodrex_herbal)
        )

        cartAdapter = ProductAdapter(popularList) { product ->
            cartRepository.addToCart(product) { success ->
                if (!isAdded) return@addToCart

                Toast.makeText(
                    requireContext(),
                    if (success) "Себетке қосылды" else "Қате шықты",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        binding.recyclerCart.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = cartAdapter
            setHasFixedSize(true)
        }
    }

    private fun setupSaleProducts() {
        val saleList = listOf(
            Product("OBH Combi", "75ml", 9.99, 10.99, R.drawable.logo_icon),
            Product("Betadine", "50ml", 6.99, 8.99, R.drawable.bodrex_herbal)
        )

        saleAdapter = ProductAdapter(saleList) { product ->
            cartRepository.addToCart(product) { success ->
                if (!isAdded) return@addToCart

                Toast.makeText(
                    requireContext(),
                    if (success) "Себетке қосылды" else "Қате шықты",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        binding.recyclerSale.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = saleAdapter
            setHasFixedSize(true)
        }
    }

    private fun setupClicks() {
        binding.layoutSearch.setOnClickListener {
            openPharmacySearch()
        }

        binding.btnShopNow.setOnClickListener {
            openPharmacySearch()
        }

        binding.btnSos.setOnClickListener {
            Toast.makeText(
                requireContext(),
                "SOS: қан тобы, аллергия және жедел контакт кейін Profile арқылы толтырылады",
                Toast.LENGTH_LONG
            ).show()
        }

        binding.textSeeAllCart.setOnClickListener {
            Toast.makeText(requireContext(), "Толық себет кейін", Toast.LENGTH_SHORT).show()
        }

        binding.textSeeAllSale.setOnClickListener {
            Toast.makeText(requireContext(), "Барлық жеңілдіктер", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openPharmacySearch() {
        val intent = Intent(requireContext(), PharmacyWebActivity::class.java)
        intent.putExtra("url", "https://www.poisklekarstv.kz/search?lat=43.273564&lng=76.914851&q=%D0%BF%D0%B0%D1%80%D0%B0%D1%86%D0%B5%D1%82%D0%B0%D0%BC%D0%BE%D0%BB&location=%D0%9A%D0%B0%D0%B7%D0%B0%D1%85%D1%81%D1%82%D0%B0%D0%BD%2C+%D0%90%D0%BB%D0%BC%D0%B0%D1%82%D1%8B")
        startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
