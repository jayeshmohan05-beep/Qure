package com.example.qure.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.qure.R;
import com.example.qure.models.DoctorProfile;

import java.util.List;

public class DoctorProfileAdapter extends RecyclerView.Adapter<DoctorProfileAdapter.DoctorViewHolder> {

    public interface OnDoctorClickListener {
        void onDoctorClick(DoctorProfile profile);
    }

    private List<DoctorProfile> doctorList;
    private final OnDoctorClickListener listener;

    public DoctorProfileAdapter(List<DoctorProfile> doctorList, OnDoctorClickListener listener) {
        this.doctorList = doctorList;
        this.listener = listener;
    }

    public void setDoctorList(List<DoctorProfile> newList) {
        this.doctorList = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public DoctorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_doctor_profile, parent, false);
        return new DoctorViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DoctorViewHolder holder, int position) {
        DoctorProfile doc = doctorList.get(position);

        holder.textDoctorName.setText(doc.getName());

        if (doc.getEmail() != null && !doc.getEmail().trim().isEmpty()) {
            holder.textDoctorEmail.setText("✉ " + doc.getEmail().trim());
            holder.textDoctorEmail.setVisibility(View.VISIBLE);
        } else {
            holder.textDoctorEmail.setVisibility(View.GONE);
        }

        holder.textDoctorDepartment.setText(doc.getDepartment());

        int count = doc.getActiveQueueCount();
        if (count == 0) {
            holder.textDoctorQueueCount.setText("🟢 Queue: Free (0 waiting)");
            holder.textDoctorQueueCount.setTextColor(Color.parseColor("#2E7D32")); // Material Green
        } else {
            String suffix = (count == 1) ? " patient waiting" : " patients waiting";
            holder.textDoctorQueueCount.setText("🟡 " + count + suffix);
            holder.textDoctorQueueCount.setTextColor(Color.parseColor("#E65100")); // Material Amber/Orange
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDoctorClick(doc);
            }
        });
    }

    @Override
    public int getItemCount() {
        return doctorList != null ? doctorList.size() : 0;
    }

    static class DoctorViewHolder extends RecyclerView.ViewHolder {
        TextView textDoctorName;
        TextView textDoctorEmail;
        TextView textDoctorDepartment;
        TextView textDoctorQueueCount;

        public DoctorViewHolder(@NonNull View itemView) {
            super(itemView);
            textDoctorName = itemView.findViewById(R.id.textDoctorName);
            textDoctorEmail = itemView.findViewById(R.id.textDoctorEmail);
            textDoctorDepartment = itemView.findViewById(R.id.textDoctorDepartment);
            textDoctorQueueCount = itemView.findViewById(R.id.textDoctorQueueCount);
        }
    }
}
