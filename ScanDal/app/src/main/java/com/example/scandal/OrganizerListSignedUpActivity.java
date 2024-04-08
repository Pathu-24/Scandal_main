package com.example.scandal;

import android.content.Intent;
import android.os.Bundle;
import android.util.Pair;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Activity for displaying the list of user who signed up for an event
 */
public class OrganizerListSignedUpActivity extends AppCompatActivity implements CustomArrayAdapter.OnItemClickListener {
    /**
     * FrameLayout for navigating back to the main page.
     */
    FrameLayout backMain;
    /**
     * ListView for displaying signed up users.
     */
    ListView userList;
    /**
     * Firebase Firestore instance for database operations.
     */
    FirebaseFirestore db;
    /**
     * Called when the activity is starting.
     *
     * @param savedInstanceState If the activity is being re-initialized after being previously shut down, this Bundle contains the data it most recently supplied. Otherwise, it is null.
     */
    String attendeeNames;
    private Button viewLocationBtn;
    CustomArrayAdapter adapter;
    List<Pair<String, String>> userNames;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.events_attendees_page); // Ensure this is the correct layout
        TextView txtMyEvents = findViewById(R.id.list_view_header);
        txtMyEvents.setText("SignedUp Attendees");

        backMain = findViewById(R.id.buttonBack_EventsAttendeesPage);
        userList = findViewById(R.id.listView_EventsAttendeesPage);
        viewLocationBtn = findViewById(R.id.buttonViewLocation_EventsAttendeesPage);
        db = FirebaseFirestore.getInstance();
        backMain.setOnClickListener(v -> finish());

        // Retrieve the event name from the intent
        String eventName = getIntent().getStringExtra("eventName");

        //Initialize ArrayAdapter
        userNames = new ArrayList<>();
        adapter = new CustomArrayAdapter(this, R.layout.list_item_layout, userNames);
        adapter.setOnItemClickListener(OrganizerListSignedUpActivity.this);
        userList.setAdapter(adapter);
        loadUsers(eventName); // Pass the eventName to the method

        viewLocationBtn.setOnClickListener(v -> {
            if(attendeeNames !=null){
                db.collection("profiles")
                        .whereEqualTo("name", attendeeNames)
                        .limit(1)
                        .get()
                        .addOnSuccessListener(queryDocumentSnapshots -> {
                            if (!queryDocumentSnapshots.isEmpty()) {
                                // Device is already registered, fetch and display profile data
                                DocumentSnapshot documentSnapshot = queryDocumentSnapshots.getDocuments().get(0);
                                Map<String, Object> profileData = documentSnapshot.getData();
                                if (Integer.parseInt(profileData.get("GeoTracking").toString()) == 1 && profileData.get("userLocation") != null) {
                                    Intent mapIntent = new Intent(OrganizerListSignedUpActivity.this, MapActivity.class);
                                    mapIntent.putExtra("attendeeName", attendeeNames);
                                    startActivity(mapIntent);

                                } else {
                                    Toast.makeText(getApplicationContext(), "user disabled geo-tracking", Toast.LENGTH_SHORT).show();
                                }
                            }
                        })
                        .addOnFailureListener(e -> Toast.makeText(getApplicationContext(), "Failed to fetch profile data", Toast.LENGTH_SHORT).show());
            }
            else {
                Toast.makeText(OrganizerListSignedUpActivity.this, "Please select an attendee first", Toast.LENGTH_SHORT).show();
            }

        });

    }
    /**
     * Retrieves and displays users signed up for the specified event.
     */
    private void loadUsers(String eventName) {
        userNames.clear();
        //Make Header
        userNames.add(new Pair<>("Name", ""));
        //Load rest of data
        db.collection("events")
                .whereEqualTo("name", eventName) // Filter by the event name
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        Map<String, Object> eventData = documentSnapshot.getData();
                        if (eventData.containsKey("signedUp")) {
                            Map<String, Object> signedUpUsers = (Map<String, Object>) eventData.get("signedUp");
                            for (Object userNameObj : signedUpUsers.values()) {
                                String userName = (String) userNameObj;
                                if (userName != null) {
                                    userNames.add(new Pair<>(userName, ""));
                                    adapter.notifyDataSetChanged();
                                }
                            }
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    // Handle errors
                });
    }

    @Override
    public void onItemClick(int position) {
        Pair<String, String> eventObject = adapter.getItem(position);
        attendeeNames = eventObject.first;
        Toast.makeText(OrganizerListSignedUpActivity.this, attendeeNames+" is selected", Toast.LENGTH_SHORT).show();
    }
}
