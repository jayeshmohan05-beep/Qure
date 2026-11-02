package com.example.qure.activities;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.qure.R;
import com.example.qure.database.FirebaseHelper;
import com.example.qure.models.Appointment;

import java.util.ArrayList;
import java.util.List;

public class AllAppointmentsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private FirebaseHelper firebaseHelper;
    private AdminAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_appointments);

        firebaseHelper = new FirebaseHelper();
        
        progressBar = findViewById(R.id.progressBar);
        recyclerView = findViewById(R.id.recyclerViewAllAppointments);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new AdminAdapter(new ArrayList<>());
        recyclerView.setAdapter(adapter);

        loadDatabase();
    }

    private void loadDatabase() {
        progressBar.setVisibility(View.VISIBLE);
        firebaseHelper.getAllAppointments(new FirebaseHelper.QueueCallback() {
            @Override
            public void onQueueLoaded(List<Appointment> queue) {
                progressBar.setVisibility(View.GONE);
                adapter.setList(queue);
            }

            @Override
            public void onFailure(String errorMessage) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(AllAppointmentsActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    // =====================================================
    // INTERNAL ADAPTER
    // =====================================================
    class AdminAdapter extends RecyclerView.Adapter<AdminAdapter.AdminViewHolder> {
        private List<Appointment> list;

        public AdminAdapter(List<Appointment> list) {
            this.list = list;
        }

        public void setList(List<Appointment> newList) {
            this.list = newList;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public AdminViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_appointment, parent, false);
            return new AdminViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull AdminViewHolder holder, int position) {
            Appointment appt = list.get(position);
            holder.textPatientName.setText("Patient: " + appt.getPatientName());
            String doc = appt.getDoctorName();
            if (doc != null && !doc.startsWith("Dr. ") && !doc.startsWith("Dr.")) {
                doc = "Dr. " + doc;
            }
            holder.textDoctorName.setText((doc != null ? doc : "Doctor") + " (" + appt.getDepartment() + ")");
            holder.textDate.setText(appt.getAppointmentDate());
            holder.textStatus.setText("Status: " + appt.getStatus());
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        class AdminViewHolder extends RecyclerView.ViewHolder {
            TextView textPatientName, textDoctorName, textDate, textStatus;
            public AdminViewHolder(@NonNull View itemView) {
                super(itemView);
                textPatientName = itemView.findViewById(R.id.textAdminPatientName);
                textDoctorName = itemView.findViewById(R.id.textAdminDoctorName);
                textDate = itemView.findViewById(R.id.textAdminDate);
                textStatus = itemView.findViewById(R.id.textAdminStatus);
            }
        }
    }
}
