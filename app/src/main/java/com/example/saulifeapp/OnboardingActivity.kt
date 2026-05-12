package com.example.saulifeapp

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.saulifeapp.databinding.ActivityOnboardingBinding

class OnboardingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOnboardingBinding
    private lateinit var onboardingAdapter: OnboardingAdapter

    private val onboardingItems = listOf(
        OnboardingItem(
            R.drawable.onboard_img1,
            "БІРЫҢҒАЙ МЕДИЦИНАЛЫҚ\nШАРАЛАР ЖҮЙЕСІ",
            "Медициналық қызметтің басты мақсаты — сапалы және сенімді көмек көрсету"
        ),
        OnboardingItem(
            R.drawable.onboard_img2,
            "Адамдарды бақытты және\nдені сау етуге көмектесу!",
            "Дәрілерді уақытында қабылдауды еске салатын қарапайым әрі сенімді қосымша."
        ),
        OnboardingItem(
            R.drawable.onboard_img3,
            "Дәрілерді толық бақылауда\nұстаңыз",
            "Дәрілерді уақытында қабылдауды еске салатын қарапайым әрі сенімді қосымша."
        )
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnboardingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupViewPager()
        updateDots(0)

        binding.btnGetStarted.setOnClickListener {
            val currentItem = binding.viewPagerOnboarding.currentItem
            if (currentItem < onboardingItems.lastIndex) {
                binding.viewPagerOnboarding.currentItem = currentItem + 1
            } else {
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
        }

        binding.viewPagerOnboarding.registerOnPageChangeCallback(
            object : androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    updateDots(position)

                    binding.btnGetStarted.text =
                        if (position == onboardingItems.lastIndex) "Жалғастыру"
                        else "Бастау"
                }
            }
        )
    }

    private fun setupViewPager() {
        onboardingAdapter = OnboardingAdapter(onboardingItems)
        binding.viewPagerOnboarding.adapter = onboardingAdapter
    }

    private fun updateDots(position: Int) {
        val active = R.drawable.dot_active
        val inactive = R.drawable.dot_inactive

        binding.dot1.setBackgroundResource(if (position == 0) active else inactive)
        binding.dot2.setBackgroundResource(if (position == 1) active else inactive)
        binding.dot3.setBackgroundResource(if (position == 2) active else inactive)
    }
}