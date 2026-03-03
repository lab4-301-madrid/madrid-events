package com.example.lotterappjava;

import android.app.Activity;
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

import com.example.lotterappjava.databinding.FragmentOrganizerCreateEventBinding;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

public class OrganizerCreateEventFragment extends Fragment {

    private FragmentOrganizerCreateEventBinding binding;
    private EventController eventController;
    private String organizerId;
    private Uri imageUri;
    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private StorageReference storageRef;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentOrganizerCreateEventBinding.inflate(inflater, container, false);
        eventController = new EventController();
        organizerId = DeviceIdManager.getDeviceId(requireContext());
        storageRef = FirebaseStorage.getInstance().getReference();

        imagePickerLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        imageUri = result.getData().getData();
                        binding.btnUploadImage.setText(R.string.image_selected);
                    }
                });

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.btnCreateEvent.setOnClickListener(v -> createEvent());
        binding.btnUploadImage.setOnClickListener(v -> openImagePicker());
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        imagePickerLauncher.launch(intent);
    }

    private void createEvent() {
        if (imageUri == null) {
            Toast.makeText(getContext(), R.string.please_select_an_image, Toast.LENGTH_SHORT).show();
            return;
        }

        uploadImageAndCreateEvent();
    }

    private void uploadImageAndCreateEvent() {
        final StorageReference imageRef = storageRef.child("event-posters/" + UUID.randomUUID().toString());
        imageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    String imageUrl = uri.toString();
                    saveEventToFirestore(imageUrl);
                }))
                .addOnFailureListener(e -> {
                    String message = getString(R.string.image_upload_failed, e.getMessage());
                    Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                });
    }

    private void saveEventToFirestore(String imageUrl) {
        String eventName = binding.editEventName.getText().toString().trim();
        String location = binding.editEventLocation.getText().toString().trim();
        String description = binding.editEventDescription.getText().toString().trim();
        String startDateStr = binding.editStartDate.getText().toString().trim();
        String startTimeStr = binding.editStartTime.getText().toString().trim();
        String endDateStr = binding.editEndDate.getText().toString().trim();
        String endTimeStr = binding.editEndTime.getText().toString().trim();

        if (eventName.isEmpty() || location.isEmpty() || description.isEmpty() || startDateStr.isEmpty() || endDateStr.isEmpty()) {
            Toast.makeText(getContext(), R.string.please_fill_in_all_fields, Toast.LENGTH_SHORT).show();
            return;
        }

        Date startDate = parseDateTime(startDateStr, startTimeStr);
        Date endDate = parseDateTime(endDateStr, endTimeStr);

        if (startDate == null || endDate == null) {
            Toast.makeText(getContext(), R.string.invalid_date_time_format, Toast.LENGTH_SHORT).show();
            return;
        }

        Event newEvent = new Event();
        newEvent.setTitle(eventName);
        newEvent.setLocation(location);
        newEvent.setDescription(description);
        newEvent.setRegistrationStart(startDate);
        newEvent.setRegistrationEnd(endDate);
        newEvent.setPosterUrl(imageUrl);

        eventController.createEvent(newEvent, organizerId, success -> {
            if (success) {
                Toast.makeText(getContext(), R.string.event_created_successfully, Toast.LENGTH_SHORT).show();
                NavHostFragment.findNavController(this).popBackStack();
            } else {
                Toast.makeText(getContext(), R.string.failed_to_create_event, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private Date parseDateTime(String dateStr, String timeStr) {
        SimpleDateFormat dateTimeFormat = new SimpleDateFormat("MM/dd/yyyy hh:mm a", Locale.US);
        try {
            return dateTimeFormat.parse(dateStr + " " + timeStr);
        } catch (ParseException e) {
            return null;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}