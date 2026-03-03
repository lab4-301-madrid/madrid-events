package com.example.lotterappjava;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.lotterappjava.databinding.FragmentOrganizerEventDetailsBinding;

import java.util.ArrayList;
import java.util.List;

public class OrganizerEventDetailsFragment extends Fragment {

    private FragmentOrganizerEventDetailsBinding binding;
    private String eventId;
    private EventController eventController;
    private UserAdapter entrantAdapter;
    private List<User> entrantList = new ArrayList<>();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            eventId = getArguments().getString("eventId");
        }
        eventController = new EventController();
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentOrganizerEventDetailsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupRecyclerView();

        if (eventId != null) {
            fetchEventDetails();
            fetchEntrants();
        } else {
            Toast.makeText(getContext(), "Error: Event ID missing", Toast.LENGTH_SHORT).show();
        }

        binding.btnSendToAll.setOnClickListener(v -> {
            // TODO: Implement send notification to all entrants
            Toast.makeText(getContext(), "Send to all clicked", Toast.LENGTH_SHORT).show();
        });

        binding.btnSendToSelected.setOnClickListener(v -> {
            // TODO: Implement send notification to selected entrants
            Toast.makeText(getContext(), "Send to selected clicked", Toast.LENGTH_SHORT).show();
        });
    }

    private void setupRecyclerView() {
        entrantAdapter = new UserAdapter(entrantList, false); // Don't show delete button
        binding.recyclerEntrants.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerEntrants.setAdapter(entrantAdapter);
    }

    private void fetchEventDetails() {
        eventController.getAllEvents(events -> {
            for (Event event : events) {
                if (event.getEventId().equals(eventId)) {
                    binding.eventTitleDetails.setText(event.getTitle());
                    break;
                }
            }
        });
    }

    private void fetchEntrants() {
        eventController.getEntrantsForEvent(eventId, entrants -> {
            entrantList.clear();
            entrantList.addAll(entrants);
            entrantAdapter.notifyDataSetChanged();
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}