package com.example.scandal;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class AdminImageActivity extends AppCompatActivity {
    FrameLayout backToHomepage;
    ListView listView;
    FirebaseFirestore db;
    CustomImageAdapter adapter; // Custom adapter to handle Bitmaps
    List<Bitmap> imagesList = new ArrayList<>();
    List<String> imageIds = new ArrayList<>(); // Store image document IDs for deletion

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_image_list_page);

        listView = findViewById(R.id.listView_ImageListPage);
        backToHomepage = findViewById(R.id.buttonBack_ImageListPage);

        db = FirebaseFirestore.getInstance();
        adapter = new CustomImageAdapter(this, imagesList); // Initialize the custom adapter
        listView.setAdapter(adapter); // Set the adapter to the ListView

        loadImages();

        backToHomepage.setOnClickListener(v -> {
            Intent intent = new Intent(AdminImageActivity.this, AdminActivity.class);
            startActivity(intent);
        });

        // Set click listener for the ListView items
        listView.setOnItemClickListener((parent, view, position, id) -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(AdminImageActivity.this);
            builder.setMessage("Are you sure you want to delete this image?")
                    .setCancelable(false)
                    .setPositiveButton("Yes", (dialog, which) -> {
                        // Delete the image from Firebase
                        deleteImage(imageIds.get(position));
                        // Remove the image from the list and notify the adapter
                        imagesList.remove(position);
                        imageIds.remove(position);
                        adapter.notifyDataSetChanged();
                    })
                    .setNegativeButton("No", (dialog, which) -> dialog.cancel());
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        });
    }

    private void loadImages() {
        db.collection("events").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                imagesList.clear();
                imageIds.clear();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    String imageString = document.getString("posterImage");
                    if (imageString != null && !imageString.isEmpty()) {
                        Bitmap bitmap = convertImageStringToBitmap(imageString);
                        if (bitmap != null) {
                            imagesList.add(bitmap);
                            imageIds.add(document.getId()); // Add document ID for deletion
                        }
                    }
                }
                adapter.notifyDataSetChanged(); // Notify the adapter that the dataset has changed
            } else {
                // Handle the error
            }
        });
    }

    private void deleteImage(String imageId) {
        db.collection("events").document(imageId)
                .update("posterImage", "")
                .addOnSuccessListener(aVoid -> Log.d("AdminImageActivity", "Image deleted successfully"))
                .addOnFailureListener(e -> Log.e("AdminImageActivity", "Error deleting image: " + e.getMessage()));
    }

    private Bitmap convertImageStringToBitmap(String imageString) {
        try {
            byte[] decodedString = Base64.decode(imageString, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
