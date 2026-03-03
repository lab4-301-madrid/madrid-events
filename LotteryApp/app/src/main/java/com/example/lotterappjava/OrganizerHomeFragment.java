package com.example.lotterappjava;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.lotterappjava.databinding.FragmentOrganizerHomeBinding;

import java.util.ArrayList;
import java.util.List;

public class OrganizerHomeFragment extends Fragment {

    private FragmentOrganizerHomeBinding binding;
    private EventController eventController;
    private EventAdapter eventAdapter;
    private List<Event> eventList = new ArrayList<>();
    private String organizerId;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentOrganizerHomeBinding.inflate(inflater, container, false);
        eventController = new EventController();
        organizerId = DeviceIdManager.getDeviceId(requireContext());
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupRecyclerView();
        loadEvents();

        binding.btnCreateNewEvent.setOnClickListener(v ->
                NavHostFragment.findNavController(OrganizerHomeFragment.this)
                        .navigate(R.id.action_organizerHomeFragment_to_organizerCreateEventFragment)
        );
    }

    private void setupRecyclerView() {
        eventAdapter = new EventAdapter(eventList);
        eventAdapter.setOnItemClickListener(event -> {
            Bundle bundle = new Bundle();
            bundle.putString("eventId", event.getEventId());
            NavHostFragment.findNavController(OrganizerHomeFragment.this)
                    .navigate(R.id.action_organizerHomeFragment_to_organizerEventDetailsFragment, bundle);
        });
        binding.recyclerMyEvents.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerMyEvents.setAdapter(eventAdapter);
    }

    private void loadEvents() {
        eventController.getEventsForOrganizer(organizerId, events -> {
            eventList.clear();
            eventList.addAll(events);
            eventAdapter.notifyDataSetChanged();
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}