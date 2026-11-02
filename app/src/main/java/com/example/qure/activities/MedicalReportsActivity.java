package com.example.qure.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.qure.R;
import com.example.qure.models.Report;
import com.example.qure.utils.Constants;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MedicalReportsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private FloatingActionButton fabAddReport;
    private ProgressBar progressBar;
    private TextView textEmpty;
    private ProgressDialog uploadDialog;

    private FirebaseFirestore mFirestore;
    private FirebaseStorage mStorage;
    private String patientId;

    private ReportAdapter adapter;
    private List<Report> reportList = new ArrayList<>();

    // File Picker Launcher
    private final ActivityResultLauncher<Intent> filePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri fileUri = result.getData().getData();
                    if (fileUri != null) {
                        uploadFileToFirebase(fileUri);
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medical_reports);

        mFirestore = FirebaseFirestore.getInstance();
        mStorage = FirebaseStorage.getInstance();
        SharedPreferences prefs = getSharedPreferences(Constants.PREF_FILE_NAME, MODE_PRIVATE);
        patientId = prefs.getString(Constants.PREF_USER_ID, null);
        if (patientId == null || patientId.isEmpty()) {
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            patientId = (currentUser != null) ? currentUser.getUid() : "local_patient";
        }

        recyclerView = findViewById(R.id.recyclerViewReports);
        fabAddReport = findViewById(R.id.fabAddReport);
        progressBar = findViewById(R.id.progressBar);
        textEmpty = findViewById(R.id.textEmpty);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ReportAdapter(reportList);
        recyclerView.setAdapter(adapter);

        uploadDialog = new ProgressDialog(this);
        uploadDialog.setMessage("Uploading report...");
        uploadDialog.setCancelable(false);

        fabAddReport.setOnClickListener(v -> openFilePicker());

        loadReports();
    }

    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        // Allow PDFs and Images
        String[] mimeTypes = {"application/pdf", "image/jpeg", "image/png"};
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
        filePickerLauncher.launch(intent);
    }

    private void uploadFileToFirebase(Uri fileUri) {
        uploadDialog.show();
        
        String timestamp = String.valueOf(System.currentTimeMillis());
        String fileName = "Report_" + timestamp; // Simplified naming
        
        // Path in Firebase Storage: medical_reports/patientId/fileName
        StorageReference storageRef = mStorage.getReference()
                .child(Constants.STORAGE_REPORTS)
                .child(patientId)
                .child(fileName);

        // 1. Upload to Storage
        storageRef.putFile(fileUri)
                .addOnSuccessListener(taskSnapshot -> {
                    // 2. Get the Download URL
                    storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        String downloadUrl = uri.toString();
                        saveReportMetadataToFirestore(fileName, downloadUrl);
                    });
                })
                .addOnFailureListener(e -> {
                    uploadDialog.dismiss();
                    Toast.makeText(this, "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void saveReportMetadataToFirestore(String fileName, String downloadUrl) {
        String reportId = mFirestore.collection(Constants.COLLECTION_REPORTS).document().getId();
        
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String currentDate = sdf.format(new Date());

        Report report = new Report(reportId, patientId, fileName, downloadUrl, currentDate);

        // Save to local SQLite
        new com.example.qure.database.DatabaseHelper(this).insertReport(report);

        // 3. Save details to Firestore Database
        mFirestore.collection(Constants.COLLECTION_REPORTS)
                .document(reportId)
                .set(report)
                .addOnSuccessListener(unused -> {
                    uploadDialog.dismiss();
                    Toast.makeText(this, "Report uploaded successfully!", Toast.LENGTH_SHORT).show();
                    loadReports(); // Refresh the list
                })
                .addOnFailureListener(e -> {
                    uploadDialog.dismiss();
                    Toast.makeText(this, "Report saved locally!", Toast.LENGTH_SHORT).show();
                    loadReports();
                });
    }

    private void loadReports() {
        progressBar.setVisibility(View.VISIBLE);
        
        mFirestore.collection(Constants.COLLECTION_REPORTS)
                .whereEqualTo(Constants.FIELD_PATIENT_ID, patientId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    progressBar.setVisibility(View.GONE);
                    reportList.clear();
                    
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        Report report = doc.toObject(Report.class);
                        if (report != null) {
                            reportList.add(report);
                            new com.example.qure.database.DatabaseHelper(this).insertReport(report);
                        }
                    }

                    if (reportList.isEmpty()) {
                        reportList.addAll(new com.example.qure.database.DatabaseHelper(this).getReportsByPatient(patientId));
                    }
                    
                    adapter.notifyDataSetChanged();
                    
                    if (reportList.isEmpty()) {
                        textEmpty.setVisibility(View.VISIBLE);
                    } else {
                        textEmpty.setVisibility(View.GONE);
                    }
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    reportList.clear();
                    reportList.addAll(new com.example.qure.database.DatabaseHelper(this).getReportsByPatient(patientId));
                    adapter.notifyDataSetChanged();
                    if (reportList.isEmpty()) {
                        textEmpty.setVisibility(View.VISIBLE);
                    } else {
                        textEmpty.setVisibility(View.GONE);
                    }
                });
    }

    // =====================================================
    // INTERNAL ADAPTER (To keep things in one file)
    // =====================================================
    class ReportAdapter extends RecyclerView.Adapter<ReportAdapter.ReportViewHolder> {

        private List<Report> list;

        public ReportAdapter(List<Report> list) {
            this.list = list;
        }

        @NonNull
        @Override
        public ReportViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_report, parent, false);
            return new ReportViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ReportViewHolder holder, int position) {
            Report report = list.get(position);
            holder.textFileName.setText(report.getFileName());
            holder.textUploadDate.setText("Uploaded: " + report.getUploadDate());
            
            // When clicked, open the file URL in the phone's browser or PDF viewer
            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(report.getFileUrl()));
                startActivity(intent);
            });
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        class ReportViewHolder extends RecyclerView.ViewHolder {
            TextView textFileName, textUploadDate;
            public ReportViewHolder(@NonNull View itemView) {
                super(itemView);
                textFileName = itemView.findViewById(R.id.textFileName);
                textUploadDate = itemView.findViewById(R.id.textUploadDate);
            }
        }
    }
}
