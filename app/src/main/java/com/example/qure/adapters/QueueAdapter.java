package com.example.qure.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.qure.R;
import com.example.qure.models.Appointment;

import java.util.List;

public class QueueAdapter extends RecyclerView.Adapter<QueueAdapter.QueueViewHolder> {

    private List<Appointment> queueList;

    public QueueAdapter(List<Appointment> queueList) {
        this.queueList = queueList;
    }

    // Method to update the list when the database changes
    public void setQueueList(List<Appointment> newList) {
        this.queueList = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public QueueViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_queue_patient, parent, false);
        return new QueueViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull QueueViewHolder holder, int position) {
        Appointment appointment = queueList.get(position);

        holder.textToken.setText(String.valueOf(appointment.getTokenNumber()));
        holder.textName.setText(appointment.getPatientName());
        holder.textSymptoms.setText("Symptoms: " + appointment.getSymptoms());
        
        boolean isEmergency = com.example.qure.queue.QueueManager.isEmergency(appointment);
        if (isEmergency) {
            holder.textTime.setText("EMERGENCY");
            holder.textTime.setTextColor(android.graphics.Color.parseColor("#C62828"));
        } else {
            holder.textTime.setText("FCFS #" + (position + 1));
            holder.textTime.setTextColor(android.graphics.Color.parseColor("#00838F"));
        }
    }

    @Override
    public int getItemCount() {
        return queueList == null ? 0 : queueList.size();
    }

    static class QueueViewHolder extends RecyclerView.ViewHolder {
        TextView textToken, textName, textSymptoms, textTime;

        public QueueViewHolder(@NonNull View itemView) {
            super(itemView);
            textToken = itemView.findViewById(R.id.textItemToken);
            textName = itemView.findViewById(R.id.textItemName);
            textSymptoms = itemView.findViewById(R.id.textItemSymptoms);
            textTime = itemView.findViewById(R.id.textItemTime);
        }
    }
}
