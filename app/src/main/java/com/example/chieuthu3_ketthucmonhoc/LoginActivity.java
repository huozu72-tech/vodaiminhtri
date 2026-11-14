package com.example.chieuthu3_ketthucmonhoc;

import android.content.Intent;
import android.os.Bundle;
import android.view.inputmethod.EditorInfo;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.chieuthu3_ketthucmonhoc.databinding.ActivityLoginBinding;
import com.example.chieuthu3_ketthucmonhoc.di.AppServiceLocator;
import com.example.chieuthu3_ketthucmonhoc.domain.services.AuthResult;
import com.example.chieuthu3_ketthucmonhoc.domain.services.IAuthenticationService;
import com.example.chieuthu3_ketthucmonhoc.domain.services.UnifiedLoginResult;

/**
 * Trang đăng nhập dùng chung cho cả Khách hàng và Quản trị viên.
 * Hệ thống tự phân biệt vai trò theo tài khoản và điều hướng về MainActivity hoặc AdminActivity.
 * Khi người dùng bấm "Tài khoản mới", chuyển sang {@link RegisterActivity}.
 */
public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private IAuthenticationService authService;

    private final ActivityResultLauncher<Intent> registerLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && authService != null && authService.getCurrentCustomer() != null) {
                    finish();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        authService = AppServiceLocator.getAuthService();

        ViewCompat.setOnApplyWindowInsetsListener(binding.loginRoot, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            int basePaddingPx = Math.round(20 * getResources().getDisplayMetrics().density);
            v.setPadding(
                    systemBars.left + basePaddingPx,
                    systemBars.top + basePaddingPx,
                    systemBars.right + basePaddingPx,
                    systemBars.bottom + basePaddingPx
            );
            return insets;
        });

        setupListeners();
    }

    private void setupListeners() {
        binding.btnBackFromLogin.setOnClickListener(v -> finish());

        binding.btnLoginSubmit.setOnClickListener(v -> attemptUnifiedLogin());

        binding.etLoginPassword.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                attemptUnifiedLogin();
                return true;
            }
            return false;
        });

        binding.btnNewAccount.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            registerLauncher.launch(intent);
        });

        binding.btnQuickCustomer.setOnClickListener(v -> {
            binding.tilLoginIdentifier.setError(null);
            binding.tilLoginPassword.setError(null);
            binding.etLoginIdentifier.setText("0901234567");
            binding.etLoginPassword.setText("123456");
        });

        binding.btnQuickAdmin.setOnClickListener(v -> {
            binding.tilLoginIdentifier.setError(null);
            binding.tilLoginPassword.setError(null);
            binding.etLoginIdentifier.setText("admin");
            binding.etLoginPassword.setText("admin123");
        });
    }

    private void attemptUnifiedLogin() {
        binding.tilLoginIdentifier.setError(null);
        binding.tilLoginPassword.setError(null);

        String identifier = binding.etLoginIdentifier.getText() != null
                ? binding.etLoginIdentifier.getText().toString().trim()
                : "";
        String password = binding.etLoginPassword.getText() != null
                ? binding.etLoginPassword.getText().toString()
                : "";

        if (identifier.isEmpty()) {
            binding.tilLoginIdentifier.setError("Vui lòng nhập SĐT, Email hoặc tên đăng nhập");
            binding.etLoginIdentifier.requestFocus();
            return;
        }
        if (password.isEmpty()) {
            binding.tilLoginPassword.setError("Vui lòng nhập mật khẩu");
            binding.etLoginPassword.requestFocus();
            return;
        }

        AuthResult<UnifiedLoginResult> result = authService.login(identifier, password);
        if (!result.isSuccess() || result.getData() == null) {
            Toast.makeText(this, result.getMessage(), Toast.LENGTH_SHORT).show();
            if (result.getMessage() != null && result.getMessage().toLowerCase().contains("mật khẩu")) {
                binding.tilLoginPassword.setError(result.getMessage());
            } else {
                binding.tilLoginIdentifier.setError(result.getMessage());
            }
            return;
        }

        UnifiedLoginResult loginData = result.getData();
        if (loginData.isAdmin()) {
            Toast.makeText(
                    this,
                    "Đăng nhập quản trị thành công! Xin chào " + loginData.getAdminAccount().getFullName(),
                    Toast.LENGTH_SHORT
            ).show();
            startActivity(new Intent(this, AdminActivity.class));
            finish();
        } else {
            Toast.makeText(
                    this,
                    "Đăng nhập thành công! Chào " + loginData.getCustomer().getFullName(),
                    Toast.LENGTH_SHORT
            ).show();
            finish();
        }
    }
}
