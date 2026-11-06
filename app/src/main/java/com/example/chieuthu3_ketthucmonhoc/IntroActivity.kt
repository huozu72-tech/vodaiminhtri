package com.example.chieuthu3_ketthucmonhoc

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AlphaAnimation
import android.view.animation.AnimationSet
import android.view.animation.TranslateAnimation
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.chieuthu3_ketthucmonhoc.databinding.ActivityIntroBinding

/**
 * Màn hình giới thiệu (Intro / Splash Screen) khi khởi động ứng dụng.
 * - Hiển thị logo nhận diện thương hiệu nhà hàng (logo_app.png)
 * - Giới thiệu các tính năng chủ đạo: Thực đơn 16+ món, Trợ lý AI hình ảnh & gợi ý, Thanh toán VietQR
 * - Tự động chuyển tiếp vào MainActivity sau 2.5 giây hoặc khi người dùng nhấn "Bắt Đầu Trải Nghiệm"
 */
class IntroActivity : AppCompatActivity() {

    private lateinit var binding: ActivityIntroBinding
    private val handler = Handler(Looper.getMainLooper())
    private var hasNavigated = false

    private val autoNavigateRunnable = Runnable {
        navigateToMain()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityIntroBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.introRoot) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        startIntroAnimations()

        binding.btnStartNow.setOnClickListener {
            navigateToMain()
        }

        // Tự động chuyển sang màn hình chính sau 2.5s
        handler.postDelayed(autoNavigateRunnable, AUTO_NAVIGATE_DELAY_MS)
    }

    private fun startIntroAnimations() {
        // Animation xuất hiện mượt mà cho Logo & Tiêu đề
        val logoAnim = AnimationSet(true).apply {
            interpolator = AccelerateDecelerateInterpolator()
            duration = 800
            addAnimation(AlphaAnimation(0f, 1f))
            addAnimation(TranslateAnimation(0f, 0f, 40f, 0f))
        }
        binding.cardLogo.startAnimation(logoAnim)
        binding.tvBrandTitle.startAnimation(logoAnim)
        binding.tvTaglineBadge.startAnimation(logoAnim)

        // Animation xuất hiện các tính năng nổi bật
        val featuresAnim = AnimationSet(true).apply {
            interpolator = AccelerateDecelerateInterpolator()
            duration = 1000
            startOffset = 250
            addAnimation(AlphaAnimation(0f, 1f))
            addAnimation(TranslateAnimation(0f, 0f, 60f, 0f))
        }
        binding.cardFeatures.startAnimation(featuresAnim)

        // Animation nút bấm phía dưới
        val bottomAnim = AnimationSet(true).apply {
            interpolator = AccelerateDecelerateInterpolator()
            duration = 1000
            startOffset = 450
            addAnimation(AlphaAnimation(0f, 1f))
        }
        binding.layoutBottomAction.startAnimation(bottomAnim)
    }

    private fun navigateToMain() {
        if (hasNavigated) return
        hasNavigated = true
        handler.removeCallbacks(autoNavigateRunnable)

        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        if (android.os.Build.VERSION.SDK_INT >= 34) {
            overrideActivityTransition(
                OVERRIDE_TRANSITION_OPEN,
                android.R.anim.fade_in,
                android.R.anim.fade_out
            )
        } else {
            @Suppress("DEPRECATION")
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(autoNavigateRunnable)
    }

    companion object {
        private const val AUTO_NAVIGATE_DELAY_MS = 2500L
    }
}
