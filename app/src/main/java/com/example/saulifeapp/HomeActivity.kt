
package com.example.saulifeapp

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.saulifeapp.databinding.ActivityHomeBinding
import com.google.firebase.auth.FirebaseAuth

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private lateinit var auth: FirebaseAuth

    private lateinit var popularAdapter: ProductAdapter
    private lateinit var saleAdapter: ProductAdapter

    private val newsTitle = "Актуальные новости здоровья в Казахстане"
    private val newsShort =
        "Новые рекомендации, аптечные сервисы, профилактика заболеваний и полезные обновления для жителей Казахстана."
    private val newsFull =
        """
        В этом разделе SauLife будет показывать актуальные новости медицины и здоровья по Казахстану.

        Здесь можно будет размещать:
        • новости Министерства здравоохранения РК;
        • обновления по лекарствам и аптекам;
        • сезонные рекомендации врачей;
        • информацию о профилактике заболеваний;
        • полезные статьи о здоровье.

        Позже этот блок можно подключить к API или к собственной базе новостей.
        """.trimIndent()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        checkUser()
        setupNews()
        setupPopularProducts()
        setupSaleProducts()
        setupClicks()
    }

    private fun checkUser() {
        val user = auth.currentUser

        if (user == null) {
            startActivity(Intent(this, LoginRegisterActivity::class.java))
            finish()
        }
    }

    private fun setupNews() {
        binding.textNewsTitle.text = newsTitle
        binding.textNewsDesc.text = newsShort
    }

    private fun setupPopularProducts() {
        val popularList = listOf(
            Product("Panadol", "20pcs", 15.99, null, R.drawable.panadol),
            Product("Bodrex Herbal", "100ml", 7.99, null, R.drawable.bodrex_herbal),
            Product("Konidin", "3pcs", 5.99, null, R.drawable.logo_icon)
        )

        popularAdapter = ProductAdapter(popularList) { product ->
            Toast.makeText(this, "${product.name} added", Toast.LENGTH_SHORT).show()
        }

        binding.recyclerPopular.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        binding.recyclerPopular.adapter = popularAdapter
    }

    private fun setupSaleProducts() {
        val saleList = listOf(
            Product("OBH Combi", "75ml", 9.99, 10.99, R.drawable.logo_icon),
            Product("Betadine", "50ml", 6.99, 8.99, R.drawable.bodrex_herbal),
            Product("Bodrexin", "75ml", 7.99, 8.99, R.drawable.logo_icon)
        )

        saleAdapter = ProductAdapter(saleList) { product ->
            Toast.makeText(this, "${product.name} added", Toast.LENGTH_SHORT).show()
        }

        binding.recyclerSale.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        binding.recyclerSale.adapter = saleAdapter
    }

    private fun setupClicks() {
        binding.layoutSearch.setOnClickListener {
            Toast.makeText(this, "Search will be added later", Toast.LENGTH_SHORT).show()
        }

        binding.btnReadNews.setOnClickListener {
            val intent = Intent(this, NewsArticleActivity::class.java)
            intent.putExtra("title", newsTitle)
            intent.putExtra("short", newsShort)
            intent.putExtra("content", newsFull)
            startActivity(intent)
        }

        binding.btnShopNow.setOnClickListener {
            Toast.makeText(this, "Sale products", Toast.LENGTH_SHORT).show()
        }

        binding.textSeeAllPopular.setOnClickListener {
            Toast.makeText(this, "See all popular products", Toast.LENGTH_SHORT).show()
        }

        binding.textSeeAllSale.setOnClickListener {
            Toast.makeText(this, "See all sale products", Toast.LENGTH_SHORT).show()
        }

        binding.fabScan.setOnClickListener {
            startActivity(Intent(this, CameraActivity::class.java))
        }

        binding.navHome.setOnClickListener {
            Toast.makeText(this, "Home", Toast.LENGTH_SHORT).show()
        }

        binding.navPrescription.setOnClickListener {
            Toast.makeText(this, "Treatment page later", Toast.LENGTH_SHORT).show()
        }

        binding.navDelivery.setOnClickListener {
            Toast.makeText(this, "Pharmacies page later", Toast.LENGTH_SHORT).show()
        }

        binding.navMore.setOnClickListener {
            Toast.makeText(this, "Profile page later", Toast.LENGTH_SHORT).show()
        }
    }
}