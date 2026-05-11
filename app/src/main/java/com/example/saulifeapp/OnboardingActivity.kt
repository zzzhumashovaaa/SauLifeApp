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
            "ONE-STOP HEALTHCARE\nSOLUTION",
            "Clinical excellence must be the priority for any health care service provider"
        ),
        OnboardingItem(
            R.drawable.onboard_img2,
            "Helping humans become\nhappier & healthier!",
            "An easy-to-use and reliable app that helps you remember to take your meds at the right time."
        ),
        OnboardingItem(
            R.drawable.onboard_img3,
            "Be in control of your\nmeds",
            "An easy-to-use and reliable app that helps you remember to take your meds at the right time."
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
                        if (position == onboardingItems.lastIndex) "Continue"
                        else "Get Started"
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