package com.example.lotterappjava;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.lotterappjava.databinding.FragmentEntrantEventDetailsBinding;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class EntrantEventDetailsFragment extends Fragment {

    private FragmentEntrantEventDetailsBinding binding;
    private String eventId;
    private EventController eventController;
    private String userId;
    private boolean onWaitlist = false;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            eventId = getArguments().getString("eventId");
        }
        eventController = new EventController();
        userId = DeviceIdManager.getDeviceId(requireContext());
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentEntrantEventDetailsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (eventId != null) {
            fetchEventDetails();
            checkWaitlistStatus();
            fetchWaitlistCount();
        } else {
            Toast.makeText(getContext(), "Error: Event ID missing", Toast.LENGTH_SHORT).show();
        }

        binding.btnJoinWaitlist.setOnClickListener(v -> {
            if (eventId != null) {
                if (onWaitlist) {
                    leaveWaitlist();
                } else {
                    joinWaitlist();
                }
            }
        });
    }

    private void fetchEventDetails() {
        eventController.getAllEvents(events -> {
            for (Event event : events) {
                if (event.getEventId().equals(eventId)) {
                    binding.textEventTitle.setText(event.getTitle());
                    binding.textEventDescription.setText(event.getDescription());
                    
                    if (event.getEventDate() != null) {
                        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
                        binding.textEventDate.setText(sdf.format(event.getEventDate()));
                    } else {
                        binding.textEventDate.setText("Date TBD");
                    }

                    binding.textGeolocked.setText(event.isGeolocationRequired() ? "Geo-locked" : "No Geo-lock");

                    if (event.getPosterUrl() != null && !event.getPosterUrl().isEmpty()) {
                        Glide.with(this)
                                .load(event.getPosterUrl())
                                .into(binding.imageEventPoster);
                    }

                    if (event.getQrCodeUrl() != null && !event.getQrCodeUrl().isEmpty()) {
                        Glide.with(this)
                                .load(event.getQrCodeUrl())
                                .into(binding.imageQrCode);
                    }
                    break;
                }
            }
        });
    }

    private void fetchWaitlistCount() {
        eventController.getEntrantsForEvent(eventId, entrants -> {
            binding.textWaitlistCount.setText(entrants.size() + " waitlisted");
        });
    }

    private void checkWaitlistStatus() {
        eventController.isUserOnWaitlist(eventId, userId, onWaitlist -> {
            this.onWaitlist = onWaitlist;
            updateWaitlistButton();
        });
    }

    private void joinWaitlist() {
        eventController.joinWaitlist(eventId, userId, success -> {
            if (success) {
                Toast.makeText(getContext(), "Successfully joined waitlist", Toast.LENGTH_SHORT).show();
                onWaitlist = true;
                updateWaitlistButton();
                fetchWaitlistCount(); // Refresh count
            } else {
                Toast.makeText(getContext(), "Failed to join waitlist", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void leaveWaitlist() {
        eventController.leaveWaitlist(eventId, userId, success -> {
            if (success) {
                Toast.makeText(getContext(), "Successfully left waitlist", Toast.LENGTH_SHORT).show();
                onWaitlist = false;
                updateWaitlistButton();
                fetchWaitlistCount(); // Refresh count
            } else {
                Toast.makeText(getContext(), "Failed to leave waitlist", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateWaitlistButton() {
        if (onWaitlist) {
            binding.btnJoinWaitlist.setText("Leave Waitlist");
            binding.btnJoinWaitlist.setBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.holo_red_dark));
        } else {
            binding.btnJoinWaitlist.setText("Join Waitlist");
            binding.btnJoinWaitlist.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.button_purple_light));
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}