package com.example.otchet_work;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.otchet_work.Models.ReportModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ReportAdapter adapter;
    private DatabaseReference databaseReference;
    private String userId;
    private List<ReportModel> reportsFull; // Сохранение полного списка отчетов для поиска
    private SearchView searchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
            return;
        }

        userId = currentUser.getUid();

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        databaseReference = FirebaseDatabase.getInstance().getReference().child("reports").child(userId);

        adapter = new ReportAdapter(this, new ArrayList<>());
        recyclerView.setAdapter(adapter);

        fetchReports();

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(this::showPopupMenu);



        searchView = findViewById(R.id.search_view);
        searchView.setIconifiedByDefault(false);
        setupSearchView();
    }

    private void fetchReports() {
        DatabaseReference reportsRef = FirebaseDatabase.getInstance().getReference().child("reports").child(userId);

        reportsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<ReportModel> reports = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    ReportModel report = snapshot.getValue(ReportModel.class);
                    if (report != null) {
                        reports.add(report);
                    }
                }
                adapter.setData(reports);
                reportsFull = new ArrayList<>(reports); // Сохраняем полный список отчетов для поиска
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(MainActivity.this, "Failed to fetch reports: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showPopupMenu(View view) {
        PopupMenu popupMenu = new PopupMenu(this, view);
        popupMenu.getMenuInflater().inflate(R.menu.popup_menu, popupMenu.getMenu());

        popupMenu.setOnMenuItemClickListener(item -> {
            Intent intent = new Intent(MainActivity.this, Report.class);
            intent.putExtra("menuItem", item.getItemId());
            intent.putExtra("userId", userId);
            startActivity(intent);
            return true;
        });

        popupMenu.show();
    }

    private void setupSearchView() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (TextUtils.isEmpty(newText)) {
                    recyclerView.setVisibility(View.VISIBLE); // Показываем RecyclerView при пустом запросе
                    adapter.setData(reportsFull); // Восстанавливаем полный список отчетов
                } else {
                    recyclerView.setVisibility(View.VISIBLE); // Показываем RecyclerView при вводе запроса
                    adapter.filter(newText.trim()); // Фильтруем по введенному тексту
                }
                return true;
            }
        });

        searchView.setOnClickListener(v -> searchView.setIconified(false));
    }
}