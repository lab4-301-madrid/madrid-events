package com.example.lotterappjava;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.lotterappjava.databinding.FragmentEntrantHomeBinding;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class EntrantHomeFragment extends Fragment {

    private FragmentEntrantHomeBinding binding;
    private EventAdapter adapter;
    private List<Event> allEvents = new ArrayList<>();
    private EventController eventController;
    private String currentUserId;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentEntrantHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        currentUserId = DeviceIdManager.getDeviceId(requireContext());
        eventController = new EventController();
        
        binding.recyclerEvents.setLayoutManager(new LinearLayoutManager(getContext()));
        
        setupFilters();
        setupSearch();

        binding.btnNotifications.setOnClickListener(v -> {
            NavHostFragment.findNavController(EntrantHomeFragment.this)
                    .navigate(R.id.action_entrantHomeFragment_to_notificationFragment);
        });

        binding.btnProfile.setOnClickListener(v -> {
            NavHostFragment.findNavController(EntrantHomeFragment.this)
                    .navigate(R.id.action_entrantHomeFragment_to_entrantProfileFragment);
        });

        fetchAllEvents();
    }

    private void setupFilters() {
        binding.filterAll.setOnClickListener(v -> {
            updateFilterUI(binding.filterAll);
            displayEvents(allEvents);
        });

        binding.filterOpen.setOnClickListener(v -> {
            updateFilterUI(binding.filterOpen);
            List<Event> openEvents = new ArrayList<>();
            Date now = new Date();
            for (Event event : allEvents) {
                if (event.getRegistrationEnd() != null && event.getRegistrationEnd().after(now)) {
                    openEvents.add(event);
                }
            }
            displayEvents(openEvents);
        });

        binding.filterMyApplications.setOnClickListener(v -> {
            updateFilterUI(binding.filterMyApplications);
            eventController.getEventsForUser(currentUserId, events -> {
                displayEvents(events);
            });
        });

        // Initial state
        updateFilterUI(binding.filterAll);
    }

    private void updateFilterUI(TextView selectedFilter) {
        // Reset all
        resetFilterStyle(binding.filterAll);
        resetFilterStyle(binding.filterOpen);
        resetFilterStyle(binding.filterMyApplications);

        // Set selected
        selectedFilter.setTextColor(Color.parseColor("#4F46E5"));
    }

    private void resetFilterStyle(TextView textView) {
        textView.setTextColor(Color.parseColor("#6B7280"));
    }

    private void setupSearch() {
        binding.searchViewEvents.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (adapter != null) {
                    adapter.getFilter().filter(newText);
                }
                return false;
            }
        });
    }

    private void fetchAllEvents() {
        eventController.getAllEvents(events -> {
            allEvents.clear();
            allEvents.addAll(events);
            displayEvents(allEvents);
        });
    }

    private void displayEvents(List<Event> events) {
        adapter = new EventAdapter(new ArrayList<>(events));
        adapter.setOnItemClickListener(event -> {
            Bundle bundle = new Bundle();
            bundle.putString("eventId", event.getEventId());
            NavHostFragment.findNavController(EntrantHomeFragment.this)
                    .navigate(R.id.action_entrantHomeFragment_to_entrantEventDetailsFragment, bundle);
        });
        binding.recyclerEvents.setAdapter(adapter);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}