package com.example.otchet_work;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.otchet_work.Models.ReportModel;

import java.util.ArrayList;
import java.util.List;

public class ReportAdapter extends RecyclerView.Adapter<ReportAdapter.ReportViewHolder> {

    private List<ReportModel> reports;
    private List<ReportModel> reportsFull; // Сохранение полного списка отчетов для поиска
    private Context context;

    public ReportAdapter(Context context, List<ReportModel> reports) {
        this.context = context;
        this.reports = reports != null ? reports : new ArrayList<>(); // Ensure reports is not null
        this.reportsFull = new ArrayList<>(this.reports); // Инициализация полного списка отчетов
    }

    public void setData(List<ReportModel> reports) {
        if (reports == null) {
            reports = new ArrayList<>();
        }
        this.reports.clear();
        this.reports.addAll(reports); // Обновляем данные адаптера
        this.reportsFull.clear();
        this.reportsFull.addAll(reports); // Обновляем полный список отчетов
        notifyDataSetChanged();
    }

    public void filter(String searchText) {
        List<ReportModel> filteredList = new ArrayList<>();

        if (TextUtils.isEmpty(searchText)) {
            setData(reportsFull); // Показываем все отчеты, если строка поиска пуста
        } else {
            for (ReportModel report : reportsFull) {
                if (report.getObjectName().toLowerCase().contains(searchText.toLowerCase())
                        || report.getApartment().toLowerCase().contains(searchText.toLowerCase())) {
                    filteredList.add(report);
                }
            }
            setData(filteredList); // Обновляем данные адаптера после фильтрации
        }
    }

    @NonNull
    @Override
    public ReportViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_report, parent, false);
        return new ReportViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReportViewHolder holder, int position) {
        ReportModel report = reports.get(position);

        // Установка данных в элементы интерфейса
        holder.nameTextView.setText(report.getObjectName());



        holder.komnataTextView.setText(report.getRoomName());

        holder.timeTextView.setText(report.getDatetime()); // Установка времени

        // Загрузка изображения с помощью Glide
        if (report.getPhotoUri() != null && !report.getPhotoUri().isEmpty()) {
            Glide.with(context)
                    .load(report.getPhotoUri())
                    .centerCrop()
                    .into(holder.imageView);
        } else {
            // Обработка случая отсутствия изображения
            holder.imageView.setImageResource(R.drawable.ic_basic_kluch);
        }

        // Установка OnClickListener для перехода к активности обновления отчета
        holder.itemView.setOnClickListener(v -> {
            Log.d("ReportAdapter", "Item clicked: " + report.getReportID());
            Intent intent = new Intent(context, ReportUpdate.class);
            intent.putExtra("reportId", report.getReportID()); // Pass the report ID
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return reports.size();
    }

    public static class ReportViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView nameTextView;
        TextView komnataTextView;
        TextView timeTextView; // Добавлено поле для времени

        public ReportViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView_reports);
            nameTextView = itemView.findViewById(R.id.nameTextView);
            komnataTextView = itemView.findViewById(R.id.komnataTextView);
            timeTextView = itemView.findViewById(R.id.timeTextView); // Инициализация TextView времени


        }
    }
}