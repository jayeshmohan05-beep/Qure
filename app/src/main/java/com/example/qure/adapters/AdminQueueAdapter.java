package com.example.qure.adapters;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.qure.R;
import com.example.qure.models.Appointment;
import com.example.qure.queue.QueueManager;
import com.example.qure.utils.Constants;
import com.google.android.material.button.MaterialButton;

import java.util.List;

public class AdminQueueAdapter extends RecyclerView.Adapter<AdminQueueAdapter.QueueViewHolder> {

    public interface OnEmergencyToggleListener {
        void onToggleEmergency(Appointment appointment, boolean makeEmergency);
    }

    private List<Appointment> appointmentList;
    private final OnEmergencyToggleListener emergencyListener;

    public AdminQueueAdapter(List<Appointment> appointmentList, OnEmergencyToggleListener emergencyListener) {
        this.appointmentList = appointmentList;
        this.emergencyListener = emergencyListener;
    }

    public void setAppointmentList(List<Appointment> newList) {
        this.appointmentList = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public QueueViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_queue_patient, parent, false);
        return new QueueViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull QueueViewHolder holder, int position) {
        Appointment appt = appointmentList.get(position);

        holder.textTokenNumber.setText(String.valueOf(appt.getTokenNumber()));
        holder.textPatientName.setText(appt.getPatientName());
        holder.textSymptoms.setText("Symptoms: " + appt.getSymptoms());
        holder.textAppointmentDate.setText("Date: " + appt.getAppointmentDate());

        boolean isEmergency = QueueManager.isEmergency(appt);
        String status = appt.getStatus();

        if (isEmergency) {
            holder.textEmergencyNotice.setVisibility(View.VISIBLE);
            holder.textOrderLabel.setText("🚨 NEXT UP (Emergency Priority)");
            holder.textOrderLabel.setTextColor(Color.parseColor("#C62828"));
            holder.layoutTokenBadge.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#C62828")));

            holder.textStatusBadge.setText("EMERGENCY");
            holder.textStatusBadge.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#C62828")));

            holder.buttonToggleEmergency.setText("Remove Emergency");
            holder.buttonToggleEmergency.setTextColor(Color.parseColor("#555555"));
            holder.buttonToggleEmergency.setStrokeColor(ColorStateList.valueOf(Color.parseColor("#888888")));

            holder.buttonToggleEmergency.setOnClickListener(v -> {
                if (emergencyListener != null) {
                    emergencyListener.onToggleEmergency(appt, false);
                }
            });
        } else {
            holder.textEmergencyNotice.setVisibility(View.GONE);
            holder.textOrderLabel.setText("Queue Order: FCFS (#" + (position + 1) + ")");
            holder.textOrderLabel.setTextColor(Color.parseColor("#666666"));
            holder.layoutTokenBadge.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#00838F")));

            holder.textStatusBadge.setText(status != null ? status : "Waiting");
            if (Constants.STATUS_IN_CONSULTATION.equals(status)) {
                holder.textStatusBadge.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#2E7D32")));
            } else {
                holder.textStatusBadge.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#00838F")));
            }

            holder.buttonToggleEmergency.setText("🚨 Flag Emergency");
            holder.buttonToggleEmergency.setTextColor(Color.parseColor("#C62828"));
            holder.buttonToggleEmergency.setStrokeColor(ColorStateList.valueOf(Color.parseColor("#C62828")));

            holder.buttonToggleEmergency.setOnClickListener(v -> {
                if (emergencyListener != null) {
                    emergencyListener.onToggleEmergency(appt, true);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return appointmentList != null ? appointmentList.size() : 0;
    }

    static class QueueViewHolder extends RecyclerView.ViewHolder {
        FrameLayout layoutTokenBadge;
        TextView textTokenNumber;
        TextView textPatientName;
        TextView textSymptoms;
        TextView textAppointmentDate;
        TextView textStatusBadge;
        TextView textEmergencyNotice;
        TextView textOrderLabel;
        MaterialButton buttonToggleEmergency;

        public QueueViewHolder(@NonNull View itemView) {
            super(itemView);
            layoutTokenBadge = itemView.findViewById(R.id.layoutTokenBadge);
            textTokenNumber = itemView.findViewById(R.id.textTokenNumber);
            textPatientName = itemView.findViewById(R.id.textPatientName);
            textSymptoms = itemView.findViewById(R.id.textSymptoms);
            textAppointmentDate = itemView.findViewById(R.id.textAppointmentDate);
            textStatusBadge = itemView.findViewById(R.id.textStatusBadge);
            textEmergencyNotice = itemView.findViewById(R.id.textEmergencyNotice);
            textOrderLabel = itemView.findViewById(R.id.textOrderLabel);
            buttonToggleEmergency = itemView.findViewById(R.id.buttonToggleEmergency);
        }
    }
}
