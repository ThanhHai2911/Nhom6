package com.example.xemphim.activity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.xemphim.R;

public class DangNhapActivity extends AppCompatActivity {
    private EditText edtSDT, edtMk, forgotPasswordTextView, tvTaoTaiKhoan;
    private Button btnDangNhap;
    private CheckBox rememberMeCheckBox;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_dang_nhap);

        setControl();
    }
    private void setControl() {
        edtSDT = findViewById(R.id.edtSDT);
        edtMk = findViewById(R.id.edtMk);
        btnDangNhap = findViewById(R.id.loginButton);
        rememberMeCheckBox = findViewById(R.id.rememberMeCheckBox);
    }
    private void setEvent() {

    }
}