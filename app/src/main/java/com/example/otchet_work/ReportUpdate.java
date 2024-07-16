package com.example.otchet_work;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.otchet_work.Models.ReportModel;
import com.example.otchet_work.Models.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;

public class ReportUpdate extends AppCompatActivity {

    private DatabaseReference databaseReference;
    private FirebaseAuth firebaseAuth;
    private StorageReference storageReference;

    private EditText editObjectName, editComments;
    private ImageView imageViewPhoto;
    private Spinner spinnerApartments, spinnerActions;
    private Button buttonUpdateReport, buttonDeleteReport, buttonUploadPhoto;
    private String reportId, photoUri;
    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_update);

        // Initialize Firebase
        databaseReference = FirebaseDatabase.getInstance().getReference("reports");
        firebaseAuth = FirebaseAuth.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();

        // Initialize views
        editObjectName = findViewById(R.id.editObjectName);
        editComments = findViewById(R.id.editComments);
        imageViewPhoto = findViewById(R.id.imageViewPhoto);
        buttonUpdateReport = findViewById(R.id.buttonUpdateReport);
        buttonDeleteReport = findViewById(R.id.buttonDeleteReport);
        buttonUploadPhoto = findViewById(R.id.buttonUploadPhoto);
        spinnerApartments = findViewById(R.id.spinnerApartments_up);
        spinnerActions = findViewById(R.id.spinnerActions_up);

        // Get reportId from intent extras
        reportId = getIntent().getStringExtra("reportId");
        if (reportId == null) {
            // Handle error: reportId not found
            Toast.makeText(this, "Report ID not found", Toast.LENGTH_SHORT).show();
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

        buttonUploadPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFileChooser();
            }
        });

        // Setup Spinner for apartments
        ArrayAdapter<CharSequence> apartmentsAdapter = ArrayAdapter.createFromResource(this,
                R.array.apartments_array, android.R.layout.simple_spinner_item);
        apartmentsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerApartments.setAdapter(apartmentsAdapter);

        ImageButton back_btn = findViewById(R.id.buttonBack_up);
        back_btn.setOnClickListener(v -> goBack());
    }
    public void goBack() {
        finish();
    }

    // Method to load report details from Firebase
    private void loadReportDetails() {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            DatabaseReference reportRef = databaseReference.child(userId).child(reportId);
            reportRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        ReportModel report = dataSnapshot.getValue(ReportModel.class);
                        if (report != null) {
                            // Set retrieved data to views
                            editObjectName.setText(report.getObjectName());
                            editComments.setText(report.getComments());
                            Glide.with(ReportUpdate.this)
                                    .load(report.getPhotoUri())
                                    .into(imageViewPhoto);
                            photoUri = report.getPhotoUri();
                            loadActionSpinner(report.getActionType());
                            // Select correct apartment from spinner
                            int pos = ((ArrayAdapter<String>) spinnerApartments.getAdapter()).getPosition(report.getApartment());
                            spinnerApartments.setSelection(pos);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(ReportUpdate.this, "Failed to load report details: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(ReportUpdate.this, "User is not authenticated", Toast.LENGTH_SHORT).show();
        }
    }

    // Method to load appropriate actions array in spinner
    private void loadActionSpinner(String actionType) {
        int arrayId = R.array.actions_array;
        switch (actionType) {
            case "ventilation":
                arrayId = R.array.ventilation_array;
                break;
            case "brickwork":
                arrayId = R.array.brickwork_array;
                break;
            case "plumbing":
                arrayId = R.array.plumbing_array;
                break;
            case "drywall":
                arrayId = R.array.drywall_array;
                break;
            case "floor_heating":
                arrayId = R.array.floor_heating_array;
                break;
            case "switches_sockets":
                arrayId = R.array.switches_sockets_array;
                break;
            case "lighting":
                arrayId = R.array.lighting_array;
                break;
            case "doors":
                arrayId = R.array.doors_array;
                break;
        }

        ArrayAdapter<CharSequence> actionsAdapter = ArrayAdapter.createFromResource(this,
                arrayId, android.R.layout.simple_spinner_item);
        actionsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerActions.setAdapter(actionsAdapter);
    }

    // Method to update report details in Firebase
    private void updateReport() {
        String updatedObjectName = editObjectName.getText().toString().trim();
        String updatedComments = editComments.getText().toString().trim();
        String selectedApartment = spinnerApartments.getSelectedItem().toString();
        String selectedAction = spinnerActions.getSelectedItem().toString();

        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            DatabaseReference reportRef = databaseReference.child(userId).child(reportId);

            // Update report data in Firebase
            reportRef.child("objectName").setValue(updatedObjectName);
            reportRef.child("comments").setValue(updatedComments);
            reportRef.child("apartment").setValue(selectedApartment);
            reportRef.child("action").setValue(selectedAction);

            // Update report data in Excel
            updateInExcel(reportId, updatedObjectName, updatedComments, selectedApartment, selectedAction);

            Toast.makeText(ReportUpdate.this, "Report updated", Toast.LENGTH_SHORT).show();

            // Finish the activity
            finish();
        } else {
            Toast.makeText(ReportUpdate.this, "User is not authenticated", Toast.LENGTH_SHORT).show();
        }
    }

    // Method to update data in Excel file
    private void updateInExcel(String reportId, String objectName, String comments, String apartment, String action) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(userId);
            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        User user = dataSnapshot.getValue(User.class);
                        if (user != null) {
                            String firstName = user.getFirstName();
                            String lastName = user.getLastName();
                            String userFullName = firstName + "_" + lastName;

                            // Current date for finding the corresponding Excel file
                            String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

                            // Path to Excel file to update
                            File directory = new File(getExternalFilesDir(null), "WORK/REPORTS/" + currentDate);
                            if (!directory.exists()) {
                                if (!directory.mkdirs()) {
                                    Log.e("Directory Creation", "Failed to create directory: " + directory.getAbsolutePath());
                                    Toast.makeText(ReportUpdate.this, "Failed to create directory", Toast.LENGTH_SHORT).show();
                                    return;
                                }
                            }

                            File file = new File(directory, userFullName + ".xlsx");

                            if (file.exists()) {
                                Log.d("File Path", "Excel file exists: " + file.getAbsolutePath());
                                try (FileInputStream fis = new FileInputStream(file);
                                     XSSFWorkbook workbook = new XSSFWorkbook(fis)) {

                                    XSSFSheet sheet = workbook.getSheetAt(0); // Assuming there's only one sheet

                                    // Find and update the row (for example, by reportId)
                                    Iterator<Row> iterator = sheet.iterator();
                                    boolean isUpdated = false;
                                    while (iterator.hasNext()) {
                                        Row currentRow = iterator.next();
                                        Cell cell = currentRow.getCell(8); // Assuming reportId is in the 9th column
                                        if (cell != null && cell.getStringCellValue().equals(reportId)) {
                                            // Update the row with new data
                                            currentRow.getCell(0).setCellValue(objectName);
                                            currentRow.getCell(1).setCellValue(apartment);
                                            currentRow.getCell(2).setCellValue(action);
                                            currentRow.getCell(3).setCellValue(comments);
                                            isUpdated = true;
                                            break;
                                        }
                                    }

                                    if (isUpdated) {
                                        try (FileOutputStream fos = new FileOutputStream(file)) {
                                            workbook.write(fos);
                                            Log.d("Excel Update", "Excel file updated successfully");
                                            uploadExcelReport(file);
                                        }
                                    } else {
                                        Log.e("Excel Update", "Report ID not found in the Excel file");
                                        Toast.makeText(ReportUpdate.this, "Report ID not found in the Excel file", Toast.LENGTH_SHORT).show();
                                    }
                                } catch (IOException e) {
                                    e.printStackTrace();
                                    Toast.makeText(ReportUpdate.this, "Failed to update Excel file: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Log.e("File Path", "Excel file does not exist: " + file.getAbsolutePath());
                                Toast.makeText(ReportUpdate.this, "Excel file does not exist", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(ReportUpdate.this, "Failed to load user data: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(ReportUpdate.this, "User is not authenticated", Toast.LENGTH_SHORT).show();
        }
    }

    // Method to confirm report deletion
    private void confirmDeleteReport() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirm Deletion");
        builder.setMessage("Are you sure you want to delete this report?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteReport();
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

    // Method to delete report from Firebase and Excel
    private void deleteReport() {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            DatabaseReference reportRef = databaseReference.child(userId).child(reportId);

            // Delete report from Firebase
            reportRef.removeValue()
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(ReportUpdate.this, "Report deleted from Firebase", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(ReportUpdate.this, "Failed to delete report from Firebase: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

            // Delete photo from Firebase Storage
            if (photoUri != null && !photoUri.isEmpty()) {
                StorageReference photoRef = storageReference.getStorage().getReferenceFromUrl(photoUri);
                photoRef.delete()
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(ReportUpdate.this, "Photo deleted from Firebase Storage", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(ReportUpdate.this, "Failed to delete photo from Firebase Storage: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
            }

            // Delete report from Excel
            deleteFromExcel(reportId);

            // Finish the activity
            finish();
        } else {
            Toast.makeText(ReportUpdate.this, "User is not authenticated", Toast.LENGTH_SHORT).show();
        }
    }

    // Method to delete report data from Excel file
    private void deleteFromExcel(String reportId) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(userId);
            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        User user = dataSnapshot.getValue(User.class);
                        if (user != null) {
                            String firstName = user.getFirstName();
                            String lastName = user.getLastName();
                            String userFullName = firstName + "_" + lastName;

                            // Current date for finding the corresponding Excel file
                            String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

                            // Path to Excel file to update
                            File directory = new File(getExternalFilesDir(null), "WORK/REPORTS/" + currentDate);
                            File file = new File(directory, userFullName + ".xlsx");

                            if (file.exists()) {
                                try {
                                    FileInputStream fis = new FileInputStream(file);
                                    XSSFWorkbook workbook = new XSSFWorkbook(fis);
                                    XSSFSheet sheet = workbook.getSheetAt(0); // Assuming there's only one sheet

                                    // Find and delete the row based on the reportId
                                    Iterator<Row> iterator = sheet.iterator();
                                    while (iterator.hasNext()) {
                                        Row currentRow = iterator.next();
                                        Cell cell = currentRow.getCell(8); // Assuming reportId is in the 9th column
                                        if (cell != null && cell.getStringCellValue().equals(reportId)) {
                                            // Delete the row from sheet
                                            sheet.removeRow(currentRow);
                                            fis.close();

                                            // Rewrite the file without deleted row
                                            FileOutputStream fos = new FileOutputStream(file);
                                            workbook.write(fos);
                                            fos.close();
                                            workbook.close();
                                            uploadExcelReport(file); // Upload the file after deletion
                                            break; // Exit while loop once row is found and deleted
                                        }
                                    }
                                } catch (IOException e) {
                                    e.printStackTrace();
                                    Toast.makeText(ReportUpdate.this, "Failed to delete from Excel file: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toast.makeText(ReportUpdate.this, "Excel file does not exist", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(ReportUpdate.this, "Failed to delete from Excel file: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(ReportUpdate.this, "User is not authenticated", Toast.LENGTH_SHORT).show();
        }
    }

    // Method to upload Excel report to Firebase Storage
    private void uploadExcelReport(File file) {
        Uri fileUri = Uri.fromFile(file);
        String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        String userFullName = file.getName().substring(0, file.getName().lastIndexOf('.'));

        // Folder structure for Excel reports
        StorageReference reportsFolderRef = storageReference.child("WORK/REPORTS/" + currentDate + "/" + userFullName + "/" + file.getName());

        reportsFolderRef.putFile(fileUri).addOnSuccessListener(taskSnapshot -> {
            Toast.makeText(ReportUpdate.this, "Excel отчет загружен", Toast.LENGTH_SHORT).show();
        }).addOnFailureListener(e -> {
            Toast.makeText(ReportUpdate.this, "Ошибка при загрузке Excel отчета", Toast.LENGTH_SHORT).show();
        });
    }

    // Method to open gallery and select photo
    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    // Method to handle selected image from gallery
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            imageViewPhoto.setImageURI(imageUri);

            // Upload selected image to Firebase Storage
            uploadPhoto(imageUri);
        }
    }

    // Method to upload photo to Firebase Storage
    private void uploadPhoto(Uri imageUri) {
        if (imageUri != null) {
            // Define the folder path based on objectName, apartment, and current date
            String currentDateTime = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault()).format(new Date());
            String photoFolder = "WORK/PHOTO/" + currentDateTime.substring(0, 10) + "/" + editObjectName.getText().toString().trim() + "/" + spinnerApartments.getSelectedItem().toString() + "/";
            StorageReference objectFolderRef = storageReference.child(photoFolder);

            // Upload the photo to Firebase Storage
            StorageReference photoRef = objectFolderRef.child(reportId); // reportId is used as the filename
            photoRef.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> {
                        // Get the download URL of the uploaded photo
                        photoRef.getDownloadUrl().addOnSuccessListener(uri -> {
                            photoUri = uri.toString();
                            Toast.makeText(ReportUpdate.this, "Photo uploaded to Firebase Storage", Toast.LENGTH_SHORT).show();

                            // Update photoUri in Firebase Realtime Database
                            FirebaseUser currentUser = firebaseAuth.getCurrentUser();
                            if (currentUser != null) {
                                String userId = currentUser.getUid();
                                DatabaseReference reportRef = databaseReference.child(userId).child(reportId);
                                reportRef.child("photoUri").setValue(photoUri);

                                // Update photoUri in Excel file
                                updatePhotoInExcel(reportId, photoUri);
                            }
                        });
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(ReportUpdate.this, "Failed to upload photo to Firebase Storage: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } else {
            Toast.makeText(ReportUpdate.this, "No image selected", Toast.LENGTH_SHORT).show();
        }
    }
    private void updatePhotoInExcel(String reportId, String photoUri) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(userId);
            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        User user = dataSnapshot.getValue(User.class);
                        if (user != null) {
                            String firstName = user.getFirstName();
                            String lastName = user.getLastName();
                            String userFullName = firstName + "_" + lastName;

                            // Current date for finding the corresponding Excel file
                            String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

                            // Path to Excel file to update
                            File directory = new File(getExternalFilesDir(null), "WORK/REPORTS/" + currentDate);
                            File file = new File(directory, userFullName + ".xlsx");

                            if (file.exists()) {
                                try {
                                    FileInputStream fis = new FileInputStream(file);
                                    XSSFWorkbook workbook = new XSSFWorkbook(fis);
                                    XSSFSheet sheet = workbook.getSheetAt(0); // Assuming there's only one sheet

                                    // Find and update the row (for example, by reportId)
                                    Iterator<Row> iterator = sheet.iterator();
                                    boolean isUpdated = false;
                                    while (iterator.hasNext()) {
                                        Row currentRow = iterator.next();
                                        Cell cell = currentRow.getCell(8); // Assuming reportId is in the 9th column
                                        if (cell != null && cell.getStringCellValue().equals(reportId)) {
                                            // Update the row with new photoUri
                                            currentRow.getCell(4).setCellValue(photoUri); // Assuming photoUri is in the 5th column
                                            isUpdated = true;
                                            break;
                                        }
                                    }

                                    if (isUpdated) {
                                        try (FileOutputStream fos = new FileOutputStream(file)) {
                                            workbook.write(fos);
                                            Log.d("Excel Update", "Excel file updated with photoUri successfully");
                                            uploadExcelReport(file); // Upload the file after photoUri update
                                        }
                                    } else {
                                        Log.e("Excel Update", "Report ID not found in the Excel file");
                                        Toast.makeText(ReportUpdate.this, "Report ID not found in the Excel file", Toast.LENGTH_SHORT).show();
                                    }
                                } catch (IOException e) {
                                    e.printStackTrace();
                                    Toast.makeText(ReportUpdate.this, "Failed to update Excel file with photoUri: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Log.e("File Path", "Excel file does not exist: " + file.getAbsolutePath());
                                Toast.makeText(ReportUpdate.this, "Excel file does not exist", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(ReportUpdate.this, "Failed to load user data: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(ReportUpdate.this, "User is not authenticated", Toast.LENGTH_SHORT).show();
        }
    }
}
