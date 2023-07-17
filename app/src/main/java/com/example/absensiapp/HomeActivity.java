package com.example.absensiapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import com.example.absensiapp.databinding.ActivityHomeBinding;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class HomeActivity extends AppCompatActivity {

    private ActivityHomeBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        binding.tvName.setText("Hi! "+ user.getDisplayName());
        binding.btnAbsen.setOnClickListener(view -> {
            startActivity(new Intent(HomeActivity.this,AbsenActivity.class));
        });
//        binding.btnHistory.setOnClickListener(view -> startActivity(new Intent(HomeActivity.this,HistoryAbsensiActivity.class)));
    }
}