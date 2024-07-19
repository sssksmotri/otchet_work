package com.example.otchet_work;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class ViborOtchet extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vibor_otchet);

        // Установите обработчики кликов для карточек
        findViewById(R.id.cardViewVentilation).setOnClickListener(v -> openReport("Отчет по вентиляции", "ventilation"));
        findViewById(R.id.cardViewFloorWallsCeilings).setOnClickListener(v -> openReport("Отчет по підлога / стіни / потолки", "floor_walls_ceilings"));
        findViewById(R.id.cardViewPlumbing).setOnClickListener(v -> openReport("Отчет по сантехнике", "plumbing"));
        findViewById(R.id.cardViewElectricity).setOnClickListener(v -> openReport("Отчет по електрике", "electricity"));
        findViewById(R.id.cardViewDemolitionWorks).setOnClickListener(v -> openReport("Отчет по демонтажу", "demolition_works"));
        findViewById(R.id.cardViewFurnitureAssembly).setOnClickListener(v -> openReport("Отчет по збирання меблів", "furniture_assembly"));
        findViewById(R.id.cardViewWindowsDoors).setOnClickListener(v -> openReport("Отчет по вікна / двері", "windows_doors"));

        // Инициализируйте BottomNavigationView и установите обработчик
        bottomNavigationView = findViewById(R.id.navigation_bar_create);
        bottomNavigationView.setSelectedItemId(R.id.navigation_create);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                return handleNavigation(item);
            }
        });
    }

    private boolean handleNavigation(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.navigation_home) {
            // Переход на домашний экран, возможно, MainActivity
            startActivity(new Intent(this, MainActivity.class));
            return true;
        } else if (id == R.id.navigation_profile) {
            // Переход на профиль
            startActivity(new Intent(this, ProfileActivity.class));
            return true;
        } else if (id == R.id.navigation_create) {
            // Переход на активность ViborOtchet не требуется, так как это текущая активность
            return true;
        }

        return false;
    }

    private void openReport(String reportTitle, String actionType) {
        Intent intent = new Intent(ViborOtchet.this, Report.class);
        intent.putExtra("REPORT_TITLE", reportTitle);
        intent.putExtra("ACTION_TYPE", actionType);
        startActivity(intent);
    }
}