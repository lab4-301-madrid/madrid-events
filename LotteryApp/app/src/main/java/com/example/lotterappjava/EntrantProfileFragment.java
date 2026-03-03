package com.example.lotterappjava;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.bumptech.glide.Glide;
import com.example.lotterappjava.databinding.FragmentEntrantProfileBinding;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.UUID;

public class EntrantProfileFragment extends Fragment {

    private FragmentEntrantProfileBinding binding;
    private UserController userController;
    private String deviceId;
    private User currentUser;
    private Uri imageUri;
    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private StorageReference storageRef;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentEntrantProfileBinding.inflate(inflater, container, false);
        userController = new UserController();
        deviceId = DeviceIdManager.getDeviceId(requireContext());
        storageRef = FirebaseStorage.getInstance().getReference();

        imagePickerLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        imageUri = result.getData().getData();
                        uploadProfileImage();
                    }
                });

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.btnBack.setOnClickListener(v -> NavHostFragment.findNavController(this).navigateUp());
        
        loadUserProfile();

        binding.btnEditImage.setOnClickListener(v -> openImagePicker());

        binding.btnSaveChanges.setOnClickListener(v -> saveChanges());

        binding.btnDeleteProfile.setOnClickListener(v -> {
            new AlertDialog.Builder(getContext())
                .setTitle("Delete Profile")
                .setMessage("Are you sure you want to delete your profile? This action cannot be undone.")
                .setPositiveButton("Yes", (dialog, which) -> deleteProfile())
                .setNegativeButton("No", null)
                .show();
        });
    }

    private void saveChanges() {
        if (currentUser == null) return;

        String name = binding.editFullName.getText().toString().trim();
        String email = binding.editEmail.getText().toString().trim();
        String phone = binding.editPhone.getText().toString().trim();
        boolean notifications = binding.switchNotifications.isChecked();

        if (name.isEmpty()) {
            Toast.makeText(getContext(), "Name cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        currentUser.setName(name);
        currentUser.setEmail(email);
        currentUser.setPhoneNumber(phone);
        currentUser.setNotificationsEnabled(notifications);

        userController.updateUser(currentUser, aVoid -> {
            Toast.makeText(getContext(), "Changes saved successfully", Toast.LENGTH_SHORT).show();
        });
    }

    private void deleteProfile() {
        if (currentUser != null && currentUser.getProfileImageUrl() != null && !currentUser.getProfileImageUrl().isEmpty()) {
            try {
                StorageReference photoRef = FirebaseStorage.getInstance().getReferenceFromUrl(currentUser.getProfileImageUrl());
                photoRef.delete();
            } catch (Exception e) {
                // Ignore if the file doesn't exist or URL is malformed
            }
        }

        userController.deleteUser(deviceId, aVoid -> {
            Toast.makeText(getContext(), "Profile deleted successfully", Toast.LENGTH_SHORT).show();
            NavHostFragment.findNavController(this).navigate(R.id.FirstFragment);
        });
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        imagePickerLauncher.launch(intent);
    }

    private void uploadProfileImage() {
        if (imageUri != null) {
            final StorageReference profileImageRef = storageRef.child("profile-images/" + deviceId + "/" + UUID.randomUUID().toString());
            profileImageRef.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> profileImageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        String imageUrl = uri.toString();
                        
                        if (currentUser != null && currentUser.getProfileImageUrl() != null && !currentUser.getProfileImageUrl().isEmpty()) {
                            try {
                                StorageReference oldRef = FirebaseStorage.getInstance().getReferenceFromUrl(currentUser.getProfileImageUrl());
                                oldRef.delete();
                            } catch (Exception ignored) {}
                        }

                        currentUser.setProfileImageUrl(imageUrl);
                        userController.updateUser(currentUser, aVoid -> {
                            Toast.makeText(getContext(), "Profile picture updated", Toast.LENGTH_SHORT).show();
                            loadUserProfile();
                        });
                    }))
                    .addOnFailureListener(e -> Toast.makeText(getContext(), "Image upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        }
    }

    private void loadUserProfile() {
        userController.getUser(deviceId, user -> {
            currentUser = user;
            binding.textDeviceId.setText("Device ID: " + user.getDeviceId());
            binding.editFullName.setText(user.getName());
            binding.editEmail.setText(user.getEmail());
            binding.editPhone.setText(user.getPhoneNumber());
            binding.switchNotifications.setChecked(user.isNotificationsEnabled());

            if (user.getProfileImageUrl() != null && !user.getProfileImageUrl().isEmpty()) {
                Glide.with(this)
                        .load(user.getProfileImageUrl())
                        .circleCrop()
                        .into(binding.imageProfile);
            } else {
                Glide.with(this)
                        .load(android.R.drawable.ic_menu_myplaces)
                        .circleCrop()
                        .into(binding.imageProfile);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
