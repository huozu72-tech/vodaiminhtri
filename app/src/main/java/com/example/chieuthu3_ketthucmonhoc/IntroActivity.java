package com.example.chieuthu3_ketthucmonhoc;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.chieuthu3_ketthucmonhoc.databinding.ActivityIntroBinding;

/**
 * Màn hình giới thiệu (Intro / Splash Screen) khi khởi động ứng dụng.
 * - Hiển thị logo nhận diện thương hiệu nhà hàng (logo_app.png)
 * - Giới thiệu các tính năng chủ đạo: Thực đơn 16+ món, Trợ lý gợi ý & nhận diện hình ảnh, Thanh toán VietQR
 * - Tự động chuyển tiếp vào MainActivity sau 2.5 giây hoặc khi người dùng nhấn "Bắt Đầu Trải Nghiệm"
 */
public class IntroActivity extends AppCompatActivity {

    private static final long AUTO_NAVIGATE_DELAY_MS = 2500L;

    private ActivityIntroBinding binding;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private boolean hasNavigated = false;

    private final Runnable autoNavigateRunnable = this::navigateToMain;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityIntroBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.introRoot, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        startIntroAnimations();

        binding.btnStartNow.setOnClickListener(v -> navigateToMain());

        // Tự động chuyển sang màn hình chính sau 2.5s
        handler.postDelayed(autoNavigateRunnable, AUTO_NAVIGATE_DELAY_MS);
    }

    private void startIntroAnimations() {
        // Animation xuất hiện mượt mà cho Logo & Tiêu đề
        AnimationSet logoAnim = new AnimationSet(true);
        logoAnim.setInterpolator(new AccelerateDecelerateInterpolator());
        logoAnim.setDuration(800);
        logoAnim.addAnimation(new AlphaAnimation(0f, 1f));
        logoAnim.addAnimation(new TranslateAnimation(0f, 0f, 40f, 0f));

        binding.cardLogo.startAnimation(logoAnim);
        binding.tvBrandTitle.startAnimation(logoAnim);
        binding.tvTaglineBadge.startAnimation(logoAnim);

        // Animation xuất hiện các tính năng nổi bật
        AnimationSet featuresAnim = new AnimationSet(true);
        featuresAnim.setInterpolator(new AccelerateDecelerateInterpolator());
        featuresAnim.setDuration(1000);
        featuresAnim.setStartOffset(250);
        featuresAnim.addAnimation(new AlphaAnimation(0f, 1f));
        featuresAnim.addAnimation(new TranslateAnimation(0f, 0f, 60f, 0f));

        binding.cardFeatures.startAnimation(featuresAnim);

        // Animation nút bấm phía dưới
        AnimationSet bottomAnim = new AnimationSet(true);
        bottomAnim.setInterpolator(new AccelerateDecelerateInterpolator());
        bottomAnim.setDuration(1000);
        bottomAnim.setStartOffset(450);
        bottomAnim.addAnimation(new AlphaAnimation(0f, 1f));

        binding.layoutBottomAction.startAnimation(bottomAnim);
    }

    private void navigateToMain() {
        if (hasNavigated) {
            return;
        }
        hasNavigated = true;
        handler.removeCallbacks(autoNavigateRunnable);

        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        if (Build.VERSION.SDK_INT >= 34) {
            overrideActivityTransition(
                    OVERRIDE_TRANSITION_OPEN,
                    android.R.anim.fade_in,
                    android.R.anim.fade_out
            );
        } else {
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        }
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(autoNavigateRunnable);
    }
}
