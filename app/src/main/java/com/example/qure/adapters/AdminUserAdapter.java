package com.example.qure.adapters;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.qure.R;
import com.example.qure.models.UserAccount;
import com.example.qure.utils.Constants;

import java.util.ArrayList;
import java.util.List;

/**
 * AdminUserAdapter.java — RecyclerView Adapter for Admin User Accounts Management
 * -------------------------------------------------------------------------------
 * Binds user accounts, applies role badges and avatar initials, and handles account
 * deletion triggers.
 */
public class AdminUserAdapter extends RecyclerView.Adapter<AdminUserAdapter.UserViewHolder> {

    public interface OnUserDeleteListener {
        void onDeleteUser(UserAccount user);
    }

    private List<UserAccount> userList;
    private final OnUserDeleteListener deleteListener;

    public AdminUserAdapter(List<UserAccount> userList, OnUserDeleteListener deleteListener) {
        this.userList = userList;
        this.deleteListener = deleteListener;
    }

    public void setUserList(List<UserAccount> list) {
        this.userList = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_user_account, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        UserAccount user = userList.get(position);

        holder.textUserName.setText(user.getName());
        holder.textUserEmail.setText(!user.getEmail().isEmpty() ? "✉ " + user.getEmail() : "✉ No email registered");

        String role = (user.getRole() != null) ? user.getRole().trim() : Constants.ROLE_PATIENT;
        holder.textUserRole.setText(role);

        // Customize role styling & avatar
        if (Constants.ROLE_DOCTOR.equalsIgnoreCase(role) || user.getName().toLowerCase().startsWith("dr")) {
            holder.textUserRole.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#E0F2F1"))); // Teal light
            holder.textUserRole.setTextColor(Color.parseColor("#00796B"));
            holder.layoutAvatar.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#E0F2F1")));

            String dept = user.getDepartment();
            if (dept != null && !dept.isEmpty()) {
                holder.textUserDetail.setText("Department: " + dept);
                holder.textUserDetail.setVisibility(View.VISIBLE);
            } else {
                holder.textUserDetail.setVisibility(View.GONE);
            }

            String clean = user.getName().replace("Dr. ", "").replace("Dr.", "").trim();
            holder.textUserInitial.setText(!clean.isEmpty() ? String.valueOf(clean.charAt(0)).toUpperCase() : "🩺");
            holder.buttonDeleteUser.setVisibility(View.VISIBLE);
        } else if (Constants.ROLE_ADMIN.equalsIgnoreCase(role)) {
            holder.textUserRole.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#EDE7F6"))); // Purple light
            holder.textUserRole.setTextColor(Color.parseColor("#512DA8"));
            holder.layoutAvatar.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#EDE7F6")));
            holder.textUserDetail.setText("System Administrator");
            holder.textUserDetail.setVisibility(View.VISIBLE);
            holder.textUserInitial.setText("🛡");

            // Protect admin from accidental self-deletion
            holder.buttonDeleteUser.setVisibility(View.GONE);
        } else {
            // Patient
            holder.textUserRole.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#E3F2FD"))); // Blue light
            holder.textUserRole.setTextColor(Color.parseColor("#1976D2"));
            holder.layoutAvatar.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#E3F2FD")));

            String phone = user.getPhone();
            if (phone != null && !phone.isEmpty()) {
                holder.textUserDetail.setText("Phone: " + phone);
                holder.textUserDetail.setVisibility(View.VISIBLE);
            } else {
                holder.textUserDetail.setVisibility(View.GONE);
            }

            String name = user.getName().trim();
            holder.textUserInitial.setText(!name.isEmpty() ? String.valueOf(name.charAt(0)).toUpperCase() : "👤");
            holder.buttonDeleteUser.setVisibility(View.VISIBLE);
        }

        holder.buttonDeleteUser.setOnClickListener(v -> {
            if (deleteListener != null) {
                deleteListener.onDeleteUser(user);
            }
        });
    }

    @Override
    public int getItemCount() {
        return userList != null ? userList.size() : 0;
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        FrameLayout layoutAvatar;
        TextView textUserInitial, textUserName, textUserRole, textUserEmail, textUserDetail;
        ImageButton buttonDeleteUser;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            layoutAvatar = itemView.findViewById(R.id.layoutAvatar);
            textUserInitial = itemView.findViewById(R.id.textUserInitial);
            textUserName = itemView.findViewById(R.id.textUserName);
            textUserRole = itemView.findViewById(R.id.textUserRole);
            textUserEmail = itemView.findViewById(R.id.textUserEmail);
            textUserDetail = itemView.findViewById(R.id.textUserDetail);
            buttonDeleteUser = itemView.findViewById(R.id.buttonDeleteUser);
        }
    }
}
