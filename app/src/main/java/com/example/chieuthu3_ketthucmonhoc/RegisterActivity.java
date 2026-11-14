package com.example.chieuthu3_ketthucmonhoc;

import android.os.Bundle;
import android.view.inputmethod.EditorInfo;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.chieuthu3_ketthucmonhoc.databinding.ActivityRegisterBinding;
import com.example.chieuthu3_ketthucmonhoc.di.AppServiceLocator;
import com.example.chieuthu3_ketthucmonhoc.domain.models.Customer;
import com.example.chieuthu3_ketthucmonhoc.domain.services.AuthResult;
import com.example.chieuthu3_ketthucmonhoc.domain.services.IAuthenticationService;
import com.example.chieuthu3_ketthucmonhoc.utils.VietnameseTelexHelper;

/**
 * Trang đăng ký tài khoản khách hàng mới, điều hướng từ nút "Tài khoản mới" ở {@link LoginActivity}.
 */
public class RegisterActivity extends AppCompatActivity {

    private ActivityRegisterBinding binding;
    private IAuthenticationService authService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        authService = AppServiceLocator.getAuthService();

        ViewCompat.setOnApplyWindowInsetsListener(binding.registerRoot, (v, insets) -> {
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

        VietnameseTelexHelper.attachToEditText(binding.etRegFullName, null);
        setupListeners();
    }

    private void setupListeners() {
        binding.btnBackFromRegister.setOnClickListener(v -> finish());
        binding.btnGoToLogin.setOnClickListener(v -> finish());
        binding.btnRegisterSubmit.setOnClickListener(v -> attemptRegister());

        binding.etRegConfirmPassword.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                attemptRegister();
                return true;
            }
            return false;
        });
    }

    private void attemptRegister() {
        binding.tilRegFullName.setError(null);
        binding.tilRegPhone.setError(null);
        binding.tilRegEmail.setError(null);
        binding.tilRegPassword.setError(null);
        binding.tilRegConfirmPassword.setError(null);

        String fullName = binding.etRegFullName.getText() != null
                ? binding.etRegFullName.getText().toString().trim()
                : "";
        String phone = binding.etRegPhone.getText() != null
                ? binding.etRegPhone.getText().toString().trim()
                : "";
        String email = binding.etRegEmail.getText() != null
                ? binding.etRegEmail.getText().toString().trim()
                : "";
        String password = binding.etRegPassword.getText() != null
                ? binding.etRegPassword.getText().toString()
                : "";
        String confirmPassword = binding.etRegConfirmPassword.getText() != null
                ? binding.etRegConfirmPassword.getText().toString()
                : "";

        if (fullName.isEmpty()) {
            binding.tilRegFullName.setError("Vui lòng nhập họ và tên");
            binding.etRegFullName.requestFocus();
            return;
        }
        if (phone.length() < 9) {
            binding.tilRegPhone.setError("Số điện thoại không hợp lệ (tối thiểu 9 chữ số)");
            binding.etRegPhone.requestFocus();
            return;
        }
        if (!email.isEmpty() && !email.contains("@")) {
            binding.tilRegEmail.setError("Địa chỉ email không đúng định dạng");
            binding.etRegEmail.requestFocus();
            return;
        }
        if (password.length() < 6) {
            binding.tilRegPassword.setError("Mật khẩu phải chứa ít nhất 6 ký tự");
            binding.etRegPassword.requestFocus();
            return;
        }
        if (!password.equals(confirmPassword)) {
            binding.tilRegConfirmPassword.setError("Mật khẩu nhập lại chưa khớp");
            binding.etRegConfirmPassword.requestFocus();
            return;
        }

        AuthResult<Customer> result = authService.register(fullName, email, phone, password);
        if (result.isSuccess() && result.getData() != null) {
            Toast.makeText(
                    this,
                    "Đăng ký thành công! Chào " + result.getData().getFullName(),
                    Toast.LENGTH_SHORT
            ).show();
            setResult(RESULT_OK);
            finish();
        } else {
            Toast.makeText(this, result.getMessage(), Toast.LENGTH_SHORT).show();
            String msg = result.getMessage() != null ? result.getMessage().toLowerCase() : "";
            if (msg.contains("điện thoại")) {
                binding.tilRegPhone.setError(result.getMessage());
            } else if (msg.contains("email")) {
                binding.tilRegEmail.setError(result.getMessage());
            }
        }
    }
}
