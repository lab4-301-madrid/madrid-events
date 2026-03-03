package com.example.lotterappjava;

import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class EventController {
    private static final String TAG = "EventController";
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    public interface OnEventsLoadedListener {
        void onEventsLoaded(List<Event> events);
    }

    public interface OnEventActionCompleteListener {
        void onComplete(boolean success);
    }

    public interface OnImageUrlsLoadedListener {
        void onImageUrlsLoaded(List<String> imageUrls);
    }

    public interface OnEntrantsLoadedListener {
        void onEntrantsLoaded(List<User> entrants);
    }

    public interface OnWaitlistCheckCompleteListener {
        void onComplete(boolean onWaitlist);
    }

    public void createEvent(Event event, String organizerId, OnEventActionCompleteListener listener) {
        String eventId = UUID.randomUUID().toString();
        event.setEventId(eventId);
        event.setOrganizerId(organizerId);
        db.collection("events").document(eventId).set(event)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Event created with ID: " + eventId);
                    if (listener != null) listener.onComplete(true);
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "Error creating event", e);
                    if (listener != null) listener.onComplete(false);
                });
    }

    public void getEventsForOrganizer(String organizerId, OnEventsLoadedListener listener) {
        db.collection("events").whereEqualTo("organizerId", organizerId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Event> events = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        events.add(document.toObject(Event.class));
                    }
                    listener.onEventsLoaded(events);
                })
                .addOnFailureListener(e -> Log.w(TAG, "Error getting events for organizer", e));
    }

    public void getAllEvents(OnEventsLoadedListener listener) {
        db.collection("events")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Event> events = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        events.add(document.toObject(Event.class));
                    }
                    listener.onEventsLoaded(events);
                })
                .addOnFailureListener(e -> Log.w(TAG, "Error getting all events", e));
    }

    public void getEventsForUser(String userId, OnEventsLoadedListener listener) {
        // Find all events where this user is in the entrants subcollection
        db.collectionGroup("entrants").whereEqualTo("userId", userId).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Task<DocumentSnapshot>> eventTasks = new ArrayList<>();
                    for (QueryDocumentSnapshot entrantDoc : queryDocumentSnapshots) {
                        // The parent of the entrant document is the 'entrants' collection,
                        // and its parent is the event document.
                        eventTasks.add(entrantDoc.getReference().getParent().getParent().get());
                    }

                    if (eventTasks.isEmpty()) {
                        listener.onEventsLoaded(new ArrayList<>());
                        return;
                    }

                    Tasks.whenAllSuccess(eventTasks).addOnSuccessListener(eventSnapshots -> {
                        List<Event> events = new ArrayList<>();
                        for (Object snapshot : eventSnapshots) {
                            DocumentSnapshot eventDoc = (DocumentSnapshot) snapshot;
                            if (eventDoc.exists()) {
                                events.add(eventDoc.toObject(Event.class));
                            }
                        }
                        listener.onEventsLoaded(events);
                    });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error getting events for user", e);
                    listener.onEventsLoaded(new ArrayList<>());
                });
    }

    public void getAllImageUrls(OnImageUrlsLoadedListener listener) {
        db.collection("events").get().addOnSuccessListener(queryDocumentSnapshots -> {
            List<String> imageUrls = new ArrayList<>();
            for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                Event event = document.toObject(Event.class);
                if (event.getPosterUrl() != null && !event.getPosterUrl().isEmpty()) {
                    imageUrls.add(event.getPosterUrl());
                }
            }
            listener.onImageUrlsLoaded(imageUrls);
        });
    }

    public void deleteEvent(String eventId, OnEventActionCompleteListener listener) {
        db.collection("events").document(eventId).delete()
                .addOnSuccessListener(aVoid -> {
                    if (listener != null) listener.onComplete(true);
                })
                .addOnFailureListener(e -> {
                    if (listener != null) listener.onComplete(false);
                });
    }

    public void deleteEventImage(String imageUrl, OnEventActionCompleteListener listener) {
        db.collection("events").whereEqualTo("posterUrl", imageUrl).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        document.getReference().update("posterUrl", null);
                    }
                    if (listener != null) listener.onComplete(true);
                })
                .addOnFailureListener(e -> {
                    if (listener != null) listener.onComplete(false);
                });
    }

    public void getEntrantsForEvent(String eventId, OnEntrantsLoadedListener listener) {
        db.collection("events").document(eventId).collection("entrants").get()
                .addOnSuccessListener(entrantsSnapshot -> {
                    List<Task<DocumentSnapshot>> userTasks = new ArrayList<>();
                    for (QueryDocumentSnapshot entrantDoc : entrantsSnapshot) {
                        String userId = entrantDoc.getId();
                        userTasks.add(db.collection("users").document(userId).get());
                    }
                    Tasks.whenAllSuccess(userTasks).addOnSuccessListener(userSnapshots -> {
                        List<User> entrants = new ArrayList<>();
                        for (Object snapshot : userSnapshots) {
                            DocumentSnapshot userDoc = (DocumentSnapshot) snapshot;
                            if (userDoc.exists()) {
                                entrants.add(userDoc.toObject(User.class));
                            }
                        }
                        listener.onEntrantsLoaded(entrants);
                    });
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error getting entrants for event", e));
    }

    public void joinWaitlist(String eventId, String userId, OnEventActionCompleteListener listener) {
        java.util.Map<String, Object> entrantData = new java.util.HashMap<>();
        entrantData.put("userId", userId);
        entrantData.put("joinedAt", com.google.firebase.Timestamp.now());
        
        db.collection("events").document(eventId).collection("entrants").document(userId).set(entrantData)
                .addOnSuccessListener(aVoid -> {
                    if (listener != null) listener.onComplete(true);
                })
                .addOnFailureListener(e -> {
                    if (listener != null) listener.onComplete(false);
                });
    }

    public void leaveWaitlist(String eventId, String userId, OnEventActionCompleteListener listener) {
        db.collection("events").document(eventId).collection("entrants").document(userId).delete()
                .addOnSuccessListener(aVoid -> {
                    if (listener != null) listener.onComplete(true);
                })
                .addOnFailureListener(e -> {
                    if (listener != null) listener.onComplete(false);
                });
    }

    public void isUserOnWaitlist(String eventId, String userId, OnWaitlistCheckCompleteListener listener) {
        db.collection("events").document(eventId).collection("entrants").document(userId).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        listener.onComplete(document.exists());
                    }
                });
    }
}
