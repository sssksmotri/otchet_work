package com.example.otchet_work;

import android.app.ProgressDialog;
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
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class Report extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri imageUri;
    private DatabaseReference databaseReference;
    private StorageReference storageReference;
    private String currentActionType;
    private EditText editObjectName, editComments, editApartment, editRoomName, editQuantity;
    private Spinner spinnerActions, spinnerExistingObjects, spinnerExistingApartments;
    private TextView reportTitleTextView;
    private DatabaseReference existingObjectsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);

        // Инициализация Firebase
        databaseReference = FirebaseDatabase.getInstance().getReference("reports");
        storageReference = FirebaseStorage.getInstance().getReference();
        existingObjectsRef = FirebaseDatabase.getInstance().getReference("existing_objects");

        // Инициализация элементов интерфейса
        editObjectName = findViewById(R.id.editObjectName);
        editComments = findViewById(R.id.editComments);
        editApartment = findViewById(R.id.editApartment);
        editRoomName = findViewById(R.id.editRoomName);
        editQuantity = findViewById(R.id.editQuantity);
        spinnerActions = findViewById(R.id.spinnerActions);
        spinnerExistingObjects = findViewById(R.id.spinnerExistingObjects);
        spinnerExistingApartments = findViewById(R.id.spinnerExistingApartments);
        reportTitleTextView = findViewById(R.id.textTitle);

        // Получение данных из Intent
        Intent intent = getIntent();
        String reportTitle = intent.getStringExtra("REPORT_TITLE");
        currentActionType = intent.getStringExtra("ACTION_TYPE");

        // Установка заголовка отчета
        reportTitleTextView.setText(reportTitle);

        // Настройка адаптера для Spinner в зависимости от текущего типа действия
        ArrayAdapter<CharSequence> actionsAdapter = null;
        if (currentActionType != null) {
            switch (currentActionType) {
                case "ventilation":
                    actionsAdapter = ArrayAdapter.createFromResource(this, R.array.ventilation, android.R.layout.simple_spinner_item);
                    break;
                case "floor_walls_ceilings":
                    actionsAdapter = ArrayAdapter.createFromResource(this, R.array.floor_walls_ceilings, android.R.layout.simple_spinner_item);
                    break;
                case "plumbing":
                    actionsAdapter = ArrayAdapter.createFromResource(this, R.array.plumbing, android.R.layout.simple_spinner_item);
                    break;
                case "electricity":
                    actionsAdapter = ArrayAdapter.createFromResource(this, R.array.electricity, android.R.layout.simple_spinner_item);
                    break;
                case "demolition_works":
                    actionsAdapter = ArrayAdapter.createFromResource(this, R.array.demolition_works, android.R.layout.simple_spinner_item);
                    break;
                case "furniture_assembly":
                    actionsAdapter = ArrayAdapter.createFromResource(this, R.array.furniture_assembly, android.R.layout.simple_spinner_item);
                    break;
                case "windows_doors":
                    actionsAdapter = ArrayAdapter.createFromResource(this, R.array.windows_doors, android.R.layout.simple_spinner_item);
                    break;
            }
        }

        if (actionsAdapter != null) {
            actionsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerActions.setAdapter(actionsAdapter);
        }

        // Инициализация существующих объектов и установка обработчиков кнопок
        loadExistingObjects();
        loadExistingApartments();

        Button buttonUploadPhoto = findViewById(R.id.buttonUploadPhoto);
        buttonUploadPhoto.setOnClickListener(v -> openFileChooser());

        Button buttonSaveReport = findViewById(R.id.buttonSaveReport);
        buttonSaveReport.setOnClickListener(v -> saveReport());

        ImageButton backButton = findViewById(R.id.buttonBack);
        backButton.setOnClickListener(v -> goBack());
    }

    public void goBack() {
        finish();
    }

    private void loadExistingApartments() {
        DatabaseReference existingApartmentsRef = FirebaseDatabase.getInstance().getReference("existing_apartments");
        existingApartmentsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ArrayList<String> apartmentsList = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String apartmentName = snapshot.getValue(String.class);
                    apartmentsList.add(apartmentName);
                }
                ArrayAdapter<String> apartmentsAdapter = new ArrayAdapter<>(Report.this,
                        android.R.layout.simple_spinner_item, apartmentsList);
                apartmentsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerExistingApartments.setAdapter(apartmentsAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(Report.this, "Ошибка загрузки квартир", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void loadExistingObjects() {
        existingObjectsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ArrayList<String> objectsList = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String objectName = snapshot.getValue(String.class);
                    objectsList.add(objectName);
                }
                ArrayAdapter<String> objectsAdapter = new ArrayAdapter<>(Report.this,
                        android.R.layout.simple_spinner_item, objectsList);
                objectsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerExistingObjects.setAdapter(objectsAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(Report.this, "Ошибка загрузки объектов", Toast.LENGTH_SHORT).show();
            }
        });
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
        }
    }

    private void saveReport() {
        final String inputObjectName = editObjectName.getText().toString().trim();
        final String selectedObjectName = spinnerExistingObjects.getSelectedItem() != null ? spinnerExistingObjects.getSelectedItem().toString() : "";
        final String inputApartmentName = editApartment.getText().toString().trim();
        final String selectedApartmentName = spinnerExistingApartments.getSelectedItem() != null ? spinnerExistingApartments.getSelectedItem().toString() : "";
        final String actionWithUnit = spinnerActions.getSelectedItem().toString().trim();
        final String comments = editComments.getText().toString().trim();
        String roomName = editRoomName.getText().toString().trim();
        String quantity = editQuantity.getText().toString().trim();
        final String actionType = this.currentActionType;

        if (inputObjectName.isEmpty() && selectedObjectName.isEmpty()) {
            Toast.makeText(this, "Выберите или введите имя объекта", Toast.LENGTH_SHORT).show();
            return;
        }

        final String objectName = !inputObjectName.isEmpty() ? inputObjectName : selectedObjectName;
        if (!inputObjectName.isEmpty()) {
            addNewObject(inputObjectName);
        }

        if (inputApartmentName.isEmpty() && selectedApartmentName.isEmpty()) {
            Toast.makeText(this, "Выберите или введите квартиру", Toast.LENGTH_SHORT).show();
            return;
        }

        final String apartmentName = !inputApartmentName.isEmpty() ? inputApartmentName : selectedApartmentName;
        if (!inputApartmentName.isEmpty()) {
            addNewApartment(inputApartmentName);
        }

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Ошибка аутентификации пользователя", Toast.LENGTH_SHORT).show();
            return;
        }

        final String userId = currentUser.getUid();
        DatabaseReference userReportsRef = databaseReference.child(userId);

        String reportId = userReportsRef.push().getKey();
        String datetime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

        if (reportId == null) {
            Toast.makeText(this, "Ошибка создания отчета", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users").child(userId);
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    User user = dataSnapshot.getValue(User.class);
                    if (user != null) {
                        String firstName = user.getFirstName();
                        String lastName = user.getLastName();

                        if (objectName.isEmpty() || apartmentName.isEmpty() || actionWithUnit.isEmpty()) {
                            Toast.makeText(Report.this, "Введите данные", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        ProgressDialog progressDialog = new ProgressDialog(Report.this);
                        progressDialog.setTitle("Сохранение отчета...");
                        progressDialog.show();

                        String[] actionParts = actionWithUnit.split(",");
                        String action = actionParts[0].trim();
                        String unit = actionParts.length > 1 ? actionParts[1].trim() : "";

                        if (imageUri != null) {
                            // Folder structure for photos
                            String photoFolder = "WORK/PHOTO/" + datetime.substring(0, 10) + "/" + objectName + "/" + apartmentName + "/";
                            StorageReference objectFolderRef = storageReference.child(photoFolder);

                            // File naming for photos
                            StorageReference fileReference = objectFolderRef.child(objectName + "_" + apartmentName + "_" + System.currentTimeMillis() + "_" + firstName + "_" + lastName + ".jpg");

                            fileReference.putFile(imageUri).addOnSuccessListener(taskSnapshot -> {
                                fileReference.getDownloadUrl().addOnSuccessListener(uri -> {
                                    String photoUrl = uri.toString();
                                    saveReportToDatabase(progressDialog, userReportsRef, reportId, action, apartmentName, comments, datetime, firstName, lastName, objectName, photoUrl, userId, actionType, unit,roomName,quantity);
                                }).addOnFailureListener(e -> {
                                    progressDialog.dismiss();
                                    Log.e("PhotoUpload", "Ошибка при получении URL фото: " + e.getMessage());
                                    Toast.makeText(Report.this, "Ошибка при получении URL фото", Toast.LENGTH_SHORT).show();
                                });
                            }).addOnFailureListener(e -> {
                                progressDialog.dismiss();
                                Log.e("PhotoUpload", "Ошибка при загрузке фото: " + e.getMessage());
                                Toast.makeText(Report.this, "Ошибка при загрузке фото", Toast.LENGTH_SHORT).show();
                            });
                        } else {
                            // Если фотография не выбрана, сохраняем отчет без фото
                            saveReportToDatabase(progressDialog, userReportsRef, reportId, action, apartmentName, comments, datetime, firstName, lastName, objectName, null, userId, actionType, unit,roomName,quantity);
                        }
                    }
                } else {
                    Log.e("UserFetch", "Пользователь не найден в базе данных");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("UserFetch", "Ошибка чтения данных пользователя: " + databaseError.getMessage());
                Toast.makeText(Report.this, "Ошибка чтения данных пользователя", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveReportToDatabase(ProgressDialog progressDialog, DatabaseReference userReportsRef, String reportId, String action, String apartmentName, String comments, String datetime, String firstName, String lastName, String objectName, String photoUrl, String userId, String actionType, String unit,String roomName, String quantity) {
        ReportModel reportModel = new ReportModel(action, apartmentName, comments, datetime, firstName, lastName, objectName, photoUrl, userId, actionType, reportId, unit,roomName,quantity);

        userReportsRef.child(reportId).setValue(reportModel).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                try {
                    generateExcelReport(objectName, apartmentName, action, comments, photoUrl, datetime, firstName, lastName, reportId, unit,roomName,quantity);
                    progressDialog.dismiss();
                    Toast.makeText(Report.this, "Отчет сохранен", Toast.LENGTH_SHORT).show();
                } catch (IOException e) {
                    progressDialog.dismiss();
                    Toast.makeText(Report.this, "Ошибка при сохранении отчета", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            } else {
                progressDialog.dismiss();
                Log.e("ReportSave", "Ошибка при сохранении отчета: " + task.getException().getMessage());
                Toast.makeText(Report.this, "Ошибка при сохранении отчета", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addNewObject(final String objectName) {
        DatabaseReference existingObjectsRef = FirebaseDatabase.getInstance().getReference("existing_objects");
        existingObjectsRef.child(objectName).setValue(objectName).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(Report.this, "Новый объект добавлен", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(Report.this, "Ошибка добавления нового объекта", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void addNewApartment(final String apartmentName) {
        DatabaseReference existingApartmentsRef = FirebaseDatabase.getInstance().getReference("existing_apartments");
        existingApartmentsRef.child(apartmentName).setValue(apartmentName).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(Report.this, "Новая квартира добавлена", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(Report.this, "Ошибка добавления новой квартиры", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void generateExcelReport(String objectName, String apartment, String action, String comments, String photoUrl,
                                    String dateTime, String firstName, String lastName, String reportId, String unit,
                                    String roomName, String quantity) throws IOException {

        String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        String userFullName = firstName + "_" + lastName;

        // Директория, где будут храниться отчеты
        File directory = new File(getExternalFilesDir(null), "WORK/REPORTS/" + currentDate);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        // Файл для записи отчета
        File file = new File(directory, userFullName + ".xlsx");

        XSSFWorkbook workbook;
        XSSFSheet sheet;
        Row dataRow;
        Cell dataCell;

        int newRowNumber;
        if (file.exists()) {
            // Если файл уже существует, открываем его для добавления данных
            FileInputStream fis = new FileInputStream(file);
            workbook = new XSSFWorkbook(fis);
            sheet = workbook.getSheetAt(0);
            fis.close();

            newRowNumber = sheet.getLastRowNum() + 1;
            dataRow = sheet.createRow(newRowNumber);
        } else {
            // Если файл не существует, создаем новую рабочую книгу
            workbook = new XSSFWorkbook();
            sheet = workbook.createSheet("Отчет");

            // Создание заголовков для листа отчета
            Row headerRow = sheet.createRow(0);
            String[] headers = {"№ п/п", "Дата", "Время", "Звіт виконаних робіт", "од.вим", "к-ть", "Ціна", "Сума",
                    "Кімната", "Об'єкт", "квартира №","Коментар", "Фотографія", "П.І.Б.", "ReportId"};
            for (int i = 0; i < headers.length; i++) {
                Cell headerCell = headerRow.createCell(i);
                headerCell.setCellValue(headers[i]);
            }

            newRowNumber = 1;
            dataRow = sheet.createRow(newRowNumber);
        }

        // Заполнение данными на листе отчета
        dataCell = dataRow.createCell(0);
        dataCell.setCellValue(newRowNumber);

        // Разделение даты и времени
        String[] dateTimeParts = dateTime.split(" ");
        String date = dateTimeParts[0];
        String time = dateTimeParts[1];

        dataCell = dataRow.createCell(1);
        dataCell.setCellValue(date); // Дата

        dataCell = dataRow.createCell(2);
        dataCell.setCellValue(time); // Время

        dataCell = dataRow.createCell(3);
        dataCell.setCellValue(action);

        dataCell = dataRow.createCell(4);
        dataCell.setCellValue(unit);

        dataCell = dataRow.createCell(5);
        dataCell.setCellValue(quantity);

        // Добавление пустых ячеек для "Цена" и "Сума"
        dataCell = dataRow.createCell(6); // Цена (пусто)
        dataCell.setCellValue("");

        dataCell = dataRow.createCell(7); // Сума (пусто)
        dataCell.setCellValue("");

        dataCell = dataRow.createCell(8);
        dataCell.setCellValue(roomName);

        dataCell = dataRow.createCell(9);
        dataCell.setCellValue(objectName);

        dataCell = dataRow.createCell(10);
        dataCell.setCellValue(apartment);

        dataCell = dataRow.createCell(11);
        dataCell.setCellValue(comments);

        dataCell = dataRow.createCell(12);
        dataCell.setCellValue(photoUrl);

        dataCell = dataRow.createCell(13);
        dataCell.setCellValue(firstName + " " + lastName);

        dataCell = dataRow.createCell(14);
        dataCell.setCellValue(reportId);

        // Создание листа для названия комнаты, если он не существует
        XSSFSheet roomSheet = workbook.getSheet(roomName);
        if (roomSheet == null) {
            roomSheet = workbook.createSheet(roomName);

            // Создание заголовков для листа комнаты
            Row roomHeaderRow = roomSheet.createRow(0);
            String[] roomHeaders = {"Дія", "од.вим", "к-ть", "Ціна", "Сума", "ReportId"};
            for (int i = 0; i < roomHeaders.length; i++) {
                Cell roomHeaderCell = roomHeaderRow.createCell(i);
                roomHeaderCell.setCellValue(roomHeaders[i]);
            }
        }

        // Добавление данных на лист комнаты
        int roomNewRowNumber = roomSheet.getLastRowNum() + 1;
        Row roomDataRow = roomSheet.createRow(roomNewRowNumber);

        Cell roomDataCell = roomDataRow.createCell(0);
        roomDataCell.setCellValue(action);

        roomDataCell = roomDataRow.createCell(1);
        roomDataCell.setCellValue(unit);

        roomDataCell = roomDataRow.createCell(2);
        roomDataCell.setCellValue(quantity);

        // Добавление пустых ячеек для "Цена" и "Сума" на листе комнаты
        roomDataCell = roomDataRow.createCell(3); // Цена (пусто)
        roomDataCell.setCellValue("");

        roomDataCell = roomDataRow.createCell(4); // Сума (пусто)
        roomDataCell.setCellValue("");

        roomDataCell = roomDataRow.createCell(5); // ReportId
        roomDataCell.setCellValue(reportId);

        // Запись рабочей книги в файл
        FileOutputStream fileOut = new FileOutputStream(file);
        workbook.write(fileOut);
        fileOut.close();
        workbook.close();

        // Логирование успешной записи и инициация выгрузки
        Log.d("ExcelReport", "Рабочая книга успешно записана в файл: " + file.getAbsolutePath());
        uploadExcelReport(file);
        Log.d("ExcelReport", "Инициирована выгрузка отчета Excel.");
    }



    private void uploadExcelReport(File file) {
        Uri fileUri = Uri.fromFile(file);
        String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        String userFullName = file.getName().substring(0, file.getName().lastIndexOf('.'));

        StorageReference reportsFolderRef = storageReference.child("WORK/REPORTS/" + currentDate + "/" + userFullName + "/" + file.getName());

        reportsFolderRef.putFile(fileUri).addOnSuccessListener(taskSnapshot -> {
            Toast.makeText(Report.this, "Excel отчет загружен", Toast.LENGTH_SHORT).show();
        }).addOnFailureListener(e -> {
            Toast.makeText(Report.this, "Ошибка при загрузке Excel отчета", Toast.LENGTH_SHORT).show();
        });
    }
}
