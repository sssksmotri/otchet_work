package com.example.otchet_work;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.otchet_work.Models.ReportModel;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ReportUpdate extends AppCompatActivity {

    private EditText editObjectName, editComments;
    private ImageView imageViewPhoto;
    private Button buttonUpdateReport, buttonDeleteReport;

    private DatabaseReference databaseReference;
    private FirebaseAuth firebaseAuth;

    private String reportId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_update);

        // Initialize Firebase
        databaseReference = FirebaseDatabase.getInstance().getReference("reports");
        firebaseAuth = FirebaseAuth.getInstance();

        // Initialize views
        editObjectName = findViewById(R.id.editObjectName);
        editComments = findViewById(R.id.editComments);
        imageViewPhoto = findViewById(R.id.imageViewPhoto);
        buttonUpdateReport = findViewById(R.id.buttonUpdateReport);
        buttonDeleteReport = findViewById(R.id.buttonDeleteReport);

        // Get reportId from intent extras
        reportId = getIntent().getStringExtra("reportId");
        if (reportId == null) {
            // Handle error: reportId not found
            finish();
        } else {
            // Load report details for editing
            loadReportDetails();
        }

        // Setup onClickListeners
        buttonUpdateReport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateReport();
            }
        });

        buttonDeleteReport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmDeleteReport();
            }
        });
    }

    private void loadReportDetails() {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            DatabaseReference reportRef = databaseReference.child(userId).child(reportId);
            reportRef.get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
                @Override
                public void onSuccess(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        ReportModel report = dataSnapshot.getValue(ReportModel.class);
                        if (report != null) {
                            editObjectName.setText(report.getObjectName());
                            editComments.setText(report.getComments());

                            // Load image using Glide or any other image loading library
                            Glide.with(ReportUpdate.this)
                                    .load(report.getPhotoUri())
                                    .into(imageViewPhoto);
                        }
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(ReportUpdate.this, "Failed to load report details: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            // Handle error: User is not authenticated
            Toast.makeText(ReportUpdate.this, "Пользователь не аутентифицирован", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateReport() {
        String updatedObjectName = editObjectName.getText().toString().trim();
        String updatedComments = editComments.getText().toString().trim();

        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            DatabaseReference reportRef = databaseReference.child(userId).child(reportId);
            reportRef.child("objectName").setValue(updatedObjectName);
            reportRef.child("comments").setValue(updatedComments)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(ReportUpdate.this, "Отчет обновлен", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(ReportUpdate.this, "Failed to update report: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            // Handle error: User is not authenticated
            Toast.makeText(ReportUpdate.this, "Пользователь не аутентифицирован", Toast.LENGTH_SHORT).show();
        }
    }

    private void confirmDeleteReport() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Удаление отчета");
        builder.setMessage("Вы уверены, что хотите удалить этот отчет?");
        builder.setPositiveButton("Да", (dialog, which) -> deleteReport());
        builder.setNegativeButton("Отмена", null);
        builder.create().show();
    }

    private void deleteReport() {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            DatabaseReference reportRef = databaseReference.child(userId).child(reportId);
            reportRef.removeValue()
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(ReportUpdate.this, "Отчет удален", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(ReportUpdate.this, "Failed to delete report: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            // Handle error: User is not authenticated
            Toast.makeText(ReportUpdate.this, "Пользователь не аутентифицирован", Toast.LENGTH_SHORT).show();
        }
    }
}