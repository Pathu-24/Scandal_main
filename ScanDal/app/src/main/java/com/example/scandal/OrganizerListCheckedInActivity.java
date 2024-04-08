package com.example.scandal;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class OrganizerListCheckedInActivity extends AppCompatActivity {
    FrameLayout backMain;
    ListView userList;
    FirebaseFirestore db;
    String attendeeNames;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.my_events_page); // Assume this is the correct layout
        TextView txtMyEvents = findViewById(R.id.txtMyEvents);
        txtMyEvents.setText("CheckedIn Attendees");

        backMain = findViewById(R.id.buttonBack_MyEventsPage);
        userList = findViewById(R.id.listView_MyEventsPage);
        db = FirebaseFirestore.getInstance();

        // Retrieve the event name from the intent
        String eventName = getIntent().getStringExtra("eventName");

        backMain.setOnClickListener(v -> finish());

        loadCheckedInUsers(eventName); // Modify to pass eventName
        userList.setOnItemClickListener((parent, view, position, id) -> {
            attendeeNames = (String) parent.getItemAtPosition(position);
            Toast.makeText(OrganizerListCheckedInActivity.this, eventName+" is selected", Toast.LENGTH_SHORT).show();
        });

    }

    /**
     * Retrieves and displays users checked in for the specified event.
     */
    private void loadCheckedInUsers(String eventName) {
        List<String> checkedInAttendeeNamesWithCount = new ArrayList<>();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, checkedInAttendeeNamesWithCount);
        userList.setAdapter(adapter);

        db.collection("events")
                .whereEqualTo("name", eventName)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        DocumentSnapshot eventDoc = queryDocumentSnapshots.getDocuments().get(0);
                        Map<String, Object> eventData = eventDoc.getData();
                        if (eventData != null) {
                            List<String> checkedInDeviceIds = (List<String>) eventData.get("checkedIn");
                            Map<String, String> signedUpUsers = (Map<String, String>) eventData.get("signedUp");
                            Map<String, Long> checkedInCount = (Map<String, Long>) eventData.get("checkedIn_count");

                            if (checkedInDeviceIds != null && signedUpUsers != null && checkedInCount != null) {
                                for (String deviceId : checkedInDeviceIds) {
                                    String attendeeName = signedUpUsers.get(deviceId);
                                    Long count = checkedInCount.get(deviceId);
                                    if (attendeeName != null && count != null) {
                                        String displayText = attendeeName + "               (count " + count + ")";
                                        checkedInAttendeeNamesWithCount.add(displayText);
                                    }
                                }
                                adapter.notifyDataSetChanged();
                            }
                        }
                    } else {
                        Toast.makeText(OrganizerListCheckedInActivity.this, "No attendees checked in yet.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(OrganizerListCheckedInActivity.this, "Error loading checked in users.", Toast.LENGTH_SHORT).show());
    }



}
