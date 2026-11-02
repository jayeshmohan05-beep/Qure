package com.example.qure.activities;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.qure.R;
import com.example.qure.adapters.AdminUserAdapter;
import com.example.qure.database.FirebaseHelper;
import com.example.qure.models.UserAccount;
import com.example.qure.utils.Constants;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

/**
 * AdminManageUsersActivity.java — Admin User Accounts Management & Deletion
 * ------------------------------------------------------------------------
 * Gives the Administrator complete authority to:
 *   1. View all registered accounts (Patients, Doctors, Admin).
 *   2. Search users in real-time by Name or registered Gmail/Email.
 *   3. Filter users by role (All, Patients, Doctors).
 *   4. Permanently delete user accounts with full safety confirmation.
 */
public class AdminManageUsersActivity extends AppCompatActivity implements AdminUserAdapter.OnUserDeleteListener {

    private ImageButton buttonBack;
    private TextView textAccountCount;
    private EditText editSearchUser;
    private MaterialButton filterAll, filterPatients, filterDoctors;
    private RecyclerView recyclerViewUsers;
    private LinearLayout layoutEmptyUsers;
    private ProgressBar progressBar;

    private FirebaseHelper firebaseHelper;
    private AdminUserAdapter adapter;

    private List<UserAccount> masterUserList = new ArrayList<>();
    private List<UserAccount> filteredUserList = new ArrayList<>();

    private String currentRoleFilter = "ALL";
    private String currentSearchQuery = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_manage_users);

        firebaseHelper = new FirebaseHelper();

        initUI();
        loadUsers();
    }

    private void initUI() {
        buttonBack = findViewById(R.id.buttonBack);
        textAccountCount = findViewById(R.id.textAccountCount);
        editSearchUser = findViewById(R.id.editSearchUser);

        filterAll = findViewById(R.id.filterAll);
        filterPatients = findViewById(R.id.filterPatients);
        filterDoctors = findViewById(R.id.filterDoctors);

        recyclerViewUsers = findViewById(R.id.recyclerViewUsers);
        layoutEmptyUsers = findViewById(R.id.layoutEmptyUsers);
        progressBar = findViewById(R.id.progressBar);

        buttonBack.setOnClickListener(v -> finish());

        // Setup RecyclerView
        recyclerViewUsers.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AdminUserAdapter(filteredUserList, this);
        recyclerViewUsers.setAdapter(adapter);

        // Filter button listeners
        filterAll.setOnClickListener(v -> setFilter("ALL"));
        filterPatients.setOnClickListener(v -> setFilter(Constants.ROLE_PATIENT));
        filterDoctors.setOnClickListener(v -> setFilter(Constants.ROLE_DOCTOR));

        // Real-time search watcher
        editSearchUser.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentSearchQuery = s.toString().trim().toLowerCase();
                applyFilterAndSearch();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void setFilter(String role) {
        currentRoleFilter = role;

        // Update button visual styles
        updateFilterButtonStyle(filterAll, "ALL".equals(role));
        updateFilterButtonStyle(filterPatients, Constants.ROLE_PATIENT.equals(role));
        updateFilterButtonStyle(filterDoctors, Constants.ROLE_DOCTOR.equals(role));

        applyFilterAndSearch();
    }

    private void updateFilterButtonStyle(MaterialButton btn, boolean isSelected) {
        if (isSelected) {
            btn.setBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimary));
            btn.setTextColor(ContextCompat.getColor(this, R.color.white));
            btn.setStrokeWidth(0);
        } else {
            btn.setBackgroundColor(ContextCompat.getColor(this, android.R.color.transparent));
            btn.setTextColor(ContextCompat.getColor(this, R.color.colorPrimary));
            btn.setStrokeColorResource(R.color.colorPrimary);
            btn.setStrokeWidth(2);
        }
    }

    private void loadUsers() {
        progressBar.setVisibility(View.VISIBLE);
        firebaseHelper.getAllUsers(new FirebaseHelper.UsersListCallback() {
            @Override
            public void onUsersLoaded(List<UserAccount> users) {
                progressBar.setVisibility(View.GONE);
                masterUserList = users;
                applyFilterAndSearch();
            }

            @Override
            public void onFailure(String errorMessage) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(AdminManageUsersActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void applyFilterAndSearch() {
        filteredUserList.clear();

        for (UserAccount user : masterUserList) {
            String role = (user.getRole() != null) ? user.getRole().trim() : Constants.ROLE_PATIENT;
            String name = (user.getName() != null) ? user.getName().toLowerCase() : "";
            String email = (user.getEmail() != null) ? user.getEmail().toLowerCase() : "";

            // Check role filter
            boolean matchesRole = true;
            if (Constants.ROLE_PATIENT.equalsIgnoreCase(currentRoleFilter)) {
                matchesRole = Constants.ROLE_PATIENT.equalsIgnoreCase(role);
            } else if (Constants.ROLE_DOCTOR.equalsIgnoreCase(currentRoleFilter)) {
                matchesRole = Constants.ROLE_DOCTOR.equalsIgnoreCase(role) || name.startsWith("dr");
            }

            // Check search query
            boolean matchesSearch = currentSearchQuery.isEmpty() ||
                    name.contains(currentSearchQuery) ||
                    email.contains(currentSearchQuery);

            if (matchesRole && matchesSearch) {
                filteredUserList.add(user);
            }
        }

        adapter.setUserList(filteredUserList);

        int totalCount = masterUserList.size();
        textAccountCount.setText(totalCount + " total registered accounts (" + filteredUserList.size() + " shown)");

        if (filteredUserList.isEmpty()) {
            layoutEmptyUsers.setVisibility(View.VISIBLE);
            recyclerViewUsers.setVisibility(View.GONE);
        } else {
            layoutEmptyUsers.setVisibility(View.GONE);
            recyclerViewUsers.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onDeleteUser(UserAccount user) {
        String role = (user.getRole() != null) ? user.getRole() : "User";
        String email = (!user.getEmail().isEmpty()) ? " (" + user.getEmail() + ")" : "";

        new AlertDialog.Builder(this)
                .setTitle("Delete " + role + " Account")
                .setMessage("Are you sure you want to permanently delete " + user.getName() + "'s account" + email +
                        "?\n\nThis will remove their profile and all associated appointments from the system.")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton("Delete Forever", (dialog, which) -> {
                    progressBar.setVisibility(View.VISIBLE);
                    firebaseHelper.deleteUserByAdmin(user, new FirebaseHelper.AuthCallback() {
                        @Override
                        public void onSuccess() {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(AdminManageUsersActivity.this,
                                    user.getName() + "'s account has been deleted.",
                                    Toast.LENGTH_SHORT).show();

                            // Remove from local lists
                            masterUserList.remove(user);
                            applyFilterAndSearch();
                        }

                        @Override
                        public void onFailure(String errorMessage) {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(AdminManageUsersActivity.this,
                                    "Failed to delete account: " + errorMessage,
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
