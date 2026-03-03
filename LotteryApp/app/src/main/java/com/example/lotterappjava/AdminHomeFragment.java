package com.example.lotterappjava;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lotterappjava.databinding.FragmentAdminHomeBinding;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class AdminHomeFragment extends Fragment {

    private FragmentAdminHomeBinding binding;
    private UserController userController;
    private EventController eventController;
    private NotificationController notificationController;
    private UserAdapter userAdapter;
    private EventAdapter eventAdapter;
    private ImageAdapter imageAdapter;
    private NotificationAdapter notificationAdapter;
    private List<User> userList = new ArrayList<>();
    private List<Event> eventList = new ArrayList<>();
    private List<String> imageUrlList = new ArrayList<>();
    private List<Notification> notificationList = new ArrayList<>();

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentAdminHomeBinding.inflate(inflater, container, false);
        userController = new UserController();
        eventController = new EventController();
        notificationController = new NotificationController();
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupRecyclerViews();
        setupSearch();

        binding.adminTopNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_admin_users) {
                loadUsers();
                return true;
            } else if (itemId == R.id.nav_admin_events) {
                loadEvents();
                return true;
            } else if (itemId == R.id.nav_admin_images) {
                loadImages();
                return true;
            } else if (itemId == R.id.nav_admin_logs) {
                loadNotifications();
                return true;
            }
            return false;
        });

        // Default to the Users tab
        binding.adminTopNav.setSelectedItemId(R.id.nav_admin_users);
    }

    private void setupRecyclerViews() {
        userAdapter = new UserAdapter(userList, true);
        userAdapter.setOnDeleteClickListener(position -> {
            User userToDelete = userList.get(position);
            userController.deleteUser(userToDelete.getDeviceId(), aVoid -> {
                Toast.makeText(getContext(), R.string.user_deleted, Toast.LENGTH_SHORT).show();
                loadUsers(); // Refresh the list
            });
        });

        eventAdapter = new EventAdapter(eventList);
        eventAdapter.setOnDeleteClickListener(position -> {
            Event eventToDelete = eventList.get(position);
            deleteEvent(eventToDelete);
        });

        imageAdapter = new ImageAdapter(imageUrlList);
        imageAdapter.setOnDeleteClickListener(position -> {
            String imageUrlToDelete = imageUrlList.get(position);
            deleteImage(imageUrlToDelete);
        });

        notificationAdapter = new NotificationAdapter(notificationList);
    }

    private void setupSearch() {
        binding.searchViewAdmin.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                RecyclerView.Adapter<?> currentAdapter = binding.recyclerAdminContent.getAdapter();
                if (currentAdapter instanceof UserAdapter) {
                    ((UserAdapter) currentAdapter).getFilter().filter(newText);
                } else if (currentAdapter instanceof EventAdapter) {
                    ((EventAdapter) currentAdapter).getFilter().filter(newText);
                }
                return false;
            }
        });
    }

    private void loadUsers() {
        userController.getAllUsers(users -> {
            userList.clear();
            userList.addAll(users);
            userAdapter = new UserAdapter(userList, true);
            setupRecyclerViews();
            binding.recyclerAdminContent.setLayoutManager(new LinearLayoutManager(getContext()));
            binding.recyclerAdminContent.setAdapter(userAdapter);
        });
    }

    private void loadEvents() {
        eventController.getAllEvents(events -> {
            eventList.clear();
            eventList.addAll(events);
            eventAdapter = new EventAdapter(eventList);
            setupRecyclerViews();
            binding.recyclerAdminContent.setLayoutManager(new LinearLayoutManager(getContext()));
            binding.recyclerAdminContent.setAdapter(eventAdapter);
        });
    }

    private void loadImages() {
        eventController.getAllImageUrls(imageUrls -> {
            imageUrlList.clear();
            imageUrlList.addAll(imageUrls);
            imageAdapter = new ImageAdapter(imageUrlList);
            setupRecyclerViews();
            binding.recyclerAdminContent.setLayoutManager(new GridLayoutManager(getContext(), 2));
            binding.recyclerAdminContent.setAdapter(imageAdapter);
        });
    }

    private void loadNotifications() {
        notificationController.getAllNotifications(notifications -> {
            notificationList.clear();
            notificationList.addAll(notifications);
            notificationAdapter = new NotificationAdapter(notificationList);
            setupRecyclerViews();
            binding.recyclerAdminContent.setLayoutManager(new LinearLayoutManager(getContext()));
            binding.recyclerAdminContent.setAdapter(notificationAdapter);
        });
    }

    private void deleteEvent(Event event) {
        if (event.getPosterUrl() != null && !event.getPosterUrl().isEmpty()) {
            StorageReference photoRef = FirebaseStorage.getInstance().getReferenceFromUrl(event.getPosterUrl());
            photoRef.delete().addOnSuccessListener(aVoid -> {
                // File deleted successfully
                deleteEventFromFirestore(event.getEventId());
            }).addOnFailureListener(exception -> {
                // Uh-oh, an error occurred!
                Toast.makeText(getContext(), R.string.failed_to_delete_event_image, Toast.LENGTH_SHORT).show();
            });
        } else {
            deleteEventFromFirestore(event.getEventId());
        }
    }

    private void deleteEventFromFirestore(String eventId) {
        eventController.deleteEvent(eventId, success -> {
            if (success) {
                Toast.makeText(getContext(), R.string.event_deleted, Toast.LENGTH_SHORT).show();
                loadEvents(); // Refresh the list
            } else {
                Toast.makeText(getContext(), R.string.failed_to_delete_event, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteImage(String imageUrl) {
        StorageReference photoRef = FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl);
        photoRef.delete().addOnSuccessListener(aVoid -> {
            eventController.deleteEventImage(imageUrl, success -> {
                if (success) {
                    Toast.makeText(getContext(), R.string.image_deleted, Toast.LENGTH_SHORT).show();
                    loadImages(); // Refresh the list
                } else {
                    Toast.makeText(getContext(), R.string.failed_to_update_event, Toast.LENGTH_SHORT).show();
                }
            });
        }).addOnFailureListener(exception -> {
            Toast.makeText(getContext(), R.string.failed_to_delete_image, Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}