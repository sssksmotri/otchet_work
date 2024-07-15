package com.example.otchet_work;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.otchet_work.Models.ReportModel;
import com.example.otchet_work.Models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
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
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class Report extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri imageUri;
    private DatabaseReference databaseReference;
    private StorageReference storageReference;

    private EditText editObjectName, editComments;
    private Spinner spinnerApartments, spinnerActions;
    private ImageView imageViewPhoto;
    private TextView reportTitleTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);

        // Extract intent extras
        int menuItemId = getIntent().getIntExtra("menuItem", R.id.ventilation); // Default to ventilation if not provided
        String userId = getIntent().getStringExtra("userId");

        databaseReference = FirebaseDatabase.getInstance().getReference("reports");
        storageReference = FirebaseStorage.getInstance().getReference();

        editObjectName = findViewById(R.id.editObjectName);
        editComments = findViewById(R.id.editComments);
        spinnerApartments = findViewById(R.id.spinnerApartments);
        spinnerActions = findViewById(R.id.spinnerActions);
        imageViewPhoto = findViewById(R.id.imageViewPhoto);
        reportTitleTextView = findViewById(R.id.textTitle);

        ArrayAdapter<CharSequence> apartmentsAdapter = ArrayAdapter.createFromResource(this,
                R.array.apartments_array, android.R.layout.simple_spinner_item);
        apartmentsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerApartments.setAdapter(apartmentsAdapter);

        ArrayAdapter<CharSequence> actionsAdapter = null;

        String reportTitle = "";

        if (menuItemId == R.id.ventilation) {
            actionsAdapter = ArrayAdapter.createFromResource(this,
                    R.array.ventilation_array, android.R.layout.simple_spinner_item);
            reportTitle = "Отчет по вентиляции";
        } else if (menuItemId == R.id.brickwork) {
            actionsAdapter = ArrayAdapter.createFromResource(this,
                    R.array.brickwork_array, android.R.layout.simple_spinner_item);
            reportTitle = "Отчет по кирпичным работам";
        } else if (menuItemId == R.id.plumbing) {
            actionsAdapter = ArrayAdapter.createFromResource(this,
                    R.array.plumbing_array, android.R.layout.simple_spinner_item);
            reportTitle = "Отчет по сантехнике";
        } else if (menuItemId == R.id.drywall) {
            actionsAdapter = ArrayAdapter.createFromResource(this,
                    R.array.drywall_array, android.R.layout.simple_spinner_item);
            reportTitle = "Отчет по гипсокартонным работам";
        } else if (menuItemId == R.id.floor_heating) {
            actionsAdapter = ArrayAdapter.createFromResource(this,
                    R.array.floor_heating_array, android.R.layout.simple_spinner_item);
            reportTitle = "Отчет по напольному отоплению";
        } else if (menuItemId == R.id.switches_sockets) {
            actionsAdapter = ArrayAdapter.createFromResource(this,
                    R.array.switches_sockets_array, android.R.layout.simple_spinner_item);
            reportTitle = "Отчет по выключателям и розеткам";
        } else if (menuItemId == R.id.lighting) {
            actionsAdapter = ArrayAdapter.createFromResource(this,
                    R.array.lighting_array, android.R.layout.simple_spinner_item);
            reportTitle = "Отчет по освещению";
        } else if (menuItemId == R.id.doors) {
            actionsAdapter = ArrayAdapter.createFromResource(this,
                    R.array.doors_array, android.R.layout.simple_spinner_item);
            reportTitle = "Отчет по дверям";
        }

        if (actionsAdapter != null) {
            actionsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerActions.setAdapter(actionsAdapter);
        }

        // Set title
        reportTitleTextView.setText(reportTitle);

        Button buttonUploadPhoto = findViewById(R.id.buttonUploadPhoto);
        buttonUploadPhoto.setOnClickListener(v -> openFileChooser());

        Button buttonSaveReport = findViewById(R.id.buttonSaveReport);
        buttonSaveReport.setOnClickListener(v -> saveReport());
    }

    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            imageViewPhoto.setImageURI(imageUri);
        }
    }
    private void saveReport() {
        String objectName = editObjectName.getText().toString().trim();
        String apartment = spinnerApartments.getSelectedItem().toString().trim();
        String action = spinnerActions.getSelectedItem().toString().trim();
        String comments = editComments.getText().toString().trim();

        // Get current user
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Ошибка аутентификации пользователя", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = currentUser.getUid();
        DatabaseReference userReportsRef = FirebaseDatabase.getInstance().getReference("reports").child(userId);

        // Generating reportId
        String reportId = userReportsRef.push().getKey();
        if (reportId == null) {
            Toast.makeText(this, "Ошибка при сохранении отчета", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get user details
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users").child(userId);
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    User user = dataSnapshot.getValue(User.class);
                    if (user != null) {
                        String firstName = user.getFirstName();
                        String lastName = user.getLastName();

                        String currentDateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

                        if (objectName.isEmpty() || apartment.isEmpty() || action.isEmpty() || comments.isEmpty() || imageUri == null) {
                            Toast.makeText(Report.this, "Заполните все поля и загрузите фото", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        ProgressDialog progressDialog = new ProgressDialog(Report.this);
                        progressDialog.setTitle("Сохранение отчета...");
                        progressDialog.show();

                        // Folder structure for photos
                        String photoFolder = "WORK/PHOTO/" + currentDateTime.substring(0, 10) + "/" + objectName + "/" + apartment + "/";
                        StorageReference objectFolderRef = storageReference.child(photoFolder);

                        // File naming for photos
                        StorageReference fileReference = objectFolderRef.child(objectName + "_" + apartment + "_" + System.currentTimeMillis() + "_" + firstName + "_" + lastName + ".jpg");

                        fileReference.putFile(imageUri).addOnSuccessListener(taskSnapshot -> {
                            fileReference.getDownloadUrl().addOnSuccessListener(uri -> {
                                String photoUrl = uri.toString();

                                // Create ReportModel instance
                                ReportModel reportModel = new ReportModel(action, apartment, comments, currentDateTime, firstName, lastName, objectName, photoUrl, userId);

                                // Save report to Firebase Realtime Database under userId/reportId
                                userReportsRef.child(reportId).setValue(reportModel).addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        try {
                                            generateExcelReport(objectName, apartment, action, comments, photoUrl, currentDateTime, firstName, lastName);
                                            progressDialog.dismiss();
                                            Toast.makeText(Report.this, "Отчет сохранен", Toast.LENGTH_SHORT).show();
                                        } catch (IOException e) {
                                            progressDialog.dismiss();
                                            Toast.makeText(Report.this, "Ошибка при сохранении отчета", Toast.LENGTH_SHORT).show();
                                            e.printStackTrace();
                                        }
                                    } else {
                                        progressDialog.dismiss();
                                        Toast.makeText(Report.this, "Ошибка при сохранении отчета", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            });
                        }).addOnFailureListener(e -> {
                            progressDialog.dismiss();
                            Toast.makeText(Report.this, "Ошибка при загрузке фото", Toast.LENGTH_SHORT).show();
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(Report.this, "Ошибка чтения данных пользователя", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void generateExcelReport(String objectName, String apartment, String action, String comments, String photoUrl, String dateTime, String firstName, String lastName) throws IOException {
        String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        String userFullName = firstName + "_" + lastName;

        // Define the directory where the file will be stored for the current day
        File directory = new File(getExternalFilesDir(null), "WORK/REPORTS/" + currentDate);
        if (!directory.exists()) {
            directory.mkdirs(); // Create directories if they don't exist
        }

        // Define the file path for the current day
        File file = new File(directory, userFullName + ".xlsx");

        XSSFWorkbook workbook;
        XSSFSheet sheet;
        Row dataRow;
        Cell dataCell;

        // Check if the file already exists for the current day
        if (file.exists()) {
            // If the file exists, open it and get the existing sheet
            Log.d("ExcelReport", "File already exists. Opening...");
            FileInputStream fis = new FileInputStream(file);
            workbook = new XSSFWorkbook(fis);
            sheet = workbook.getSheetAt(0); // Assuming there's only one sheet
            fis.close();

            // Determine the next row number to append data
            int lastRowNum = sheet.getLastRowNum();
            dataRow = sheet.createRow(lastRowNum + 1); // Create a new row for data
        } else {
            // If the file doesn't exist, create a new workbook and sheet
            Log.d("ExcelReport", "File doesn't exist. Creating new workbook...");
            workbook = new XSSFWorkbook();
            sheet = workbook.createSheet("Report");

            // Create header row
            Row headerRow = sheet.createRow(0);
            Cell headerCell = headerRow.createCell(0);
            headerCell.setCellValue("Имя объекта");

            headerCell = headerRow.createCell(1);
            headerCell.setCellValue("Квартира");

            headerCell = headerRow.createCell(2);
            headerCell.setCellValue("Действие");

            headerCell = headerRow.createCell(3);
            headerCell.setCellValue("Комментарий");

            headerCell = headerRow.createCell(4);
            headerCell.setCellValue("Фотография");

            headerCell = headerRow.createCell(5);
            headerCell.setCellValue("Дата и время");

            headerCell = headerRow.createCell(6);
            headerCell.setCellValue("Имя");

            headerCell = headerRow.createCell(7);
            headerCell.setCellValue("Фамилия");

            // Create first data row
            dataRow = sheet.createRow(1);
        }

        // Populate data into the cells
        dataCell = dataRow.createCell(0);
        dataCell.setCellValue(objectName);

        dataCell = dataRow.createCell(1);
        dataCell.setCellValue(apartment);

        dataCell = dataRow.createCell(2);
        dataCell.setCellValue(action);

        dataCell = dataRow.createCell(3);
        dataCell.setCellValue(comments);

        dataCell = dataRow.createCell(4);
        dataCell.setCellValue(photoUrl);

        dataCell = dataRow.createCell(5);
        dataCell.setCellValue(dateTime);

        dataCell = dataRow.createCell(6);
        dataCell.setCellValue(firstName);

        dataCell = dataRow.createCell(7);
        dataCell.setCellValue(lastName);

        // Write the workbook to the file system
        FileOutputStream fileOut = new FileOutputStream(file);
        workbook.write(fileOut);
        fileOut.close();
        workbook.close();

        Log.d("ExcelReport", "Workbook successfully written to file: " + file.getAbsolutePath());

        // Upload the Excel report to Firebase Storage
        uploadExcelReport(file);
        Log.d("ExcelReport", "Excel report upload initiated.");
    }

    private void uploadExcelReport(File file) {
        Uri fileUri = Uri.fromFile(file);
        String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        String userFullName = file.getName().substring(0, file.getName().lastIndexOf('.'));

        // Folder structure for Excel reports
        StorageReference reportsFolderRef = storageReference.child("WORK/REPORTS/" + currentDate + "/" + userFullName + "/" + file.getName());

        reportsFolderRef.putFile(fileUri).addOnSuccessListener(taskSnapshot -> {
            Toast.makeText(Report.this, "Excel отчет загружен", Toast.LENGTH_SHORT).show();
        }).addOnFailureListener(e -> {
            Toast.makeText(Report.this, "Ошибка при загрузке Excel отчета", Toast.LENGTH_SHORT).show();
        });
    }
}