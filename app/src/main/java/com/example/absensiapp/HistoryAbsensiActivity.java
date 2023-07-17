package com.example.absensiapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.os.Bundle;

import com.example.absensiapp.databinding.ActivityHistoryAbsensiBinding;
import com.example.absensiapp.service.DbHandler;
import com.example.absensiapp.service.ListAbsensiAdapter;

public class HistoryAbsensiActivity extends AppCompatActivity {

    private ActivityHistoryAbsensiBinding binding;
    private ListAbsensiAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHistoryAbsensiBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.toolbar.setNavigationIcon(R.drawable.baseline_arrow_back_24);
        binding.toolbar.setNavigationOnClickListener(view -> finish());

        binding.rvAbsensi.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ListAbsensiAdapter(this, DbHandler.getInstance(this).getDatabase().absensiDAO().getAllAbsensi());
        binding.rvAbsensi.setAdapter(adapter);
    }
}