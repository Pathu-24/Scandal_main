package com.example.scandal;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.content.FileProvider;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
/** Activity for managing the creation of a new event */
public class NewEventActivity extends AppCompatActivity {

    /**
     * ImageView for displaying the check-in QR code.
     */
    ImageView checkinQRCode;

    /**
     * ImageView for displaying the promo QR code.
     */
    ImageView promoQRCode;

    /**
     * Button for saving the check-in code.
     */
    AppCompatButton saveCheckinCode;

    /**
     * Button for saving the promo code.
     */
    AppCompatButton savePromoCode;
    /**
     * Button for going back.
     */
    FrameLayout backButton;
    /**
     * Text congratulating user on new event creation.
     */
    TextView congratsText;
    /**
     * String containing source of activity intent
     */
    String intentSource;
    /**
     * Text informing user of new event creation
     */
    TextView newEventText;
    /**
     * Button for saving project.
     */
    AppCompatButton saveProj;
    /**
     * Firebase Firestore instance for database operations
     */
    FirebaseFirestore db;

    /**
     * QRCode object for generating and handling QR codes.
     */
    QRCode promoQR;
    QRCode checkinQR;
    /**
     * token to be encoded in the default QR code for checkins
     */
    String token;
    /**
     *  the token to be encoded for the default QR code for event promo
     */
    String token2;
    /**
     * string of the event poster to make the passed intents smaller
     */
    Button shareCheckin;
    Button sharePromo;

    String name;
    String attendeeLimit;
    String description;
    //String imageString = getIntent().getStringExtra("posterImage");
    String eventLocation;
    String eventTime;
    static String imageString;
    /**
     *
     * Called when the activity is starting. This is where most initialization should go:
     * calling setContentView(int) to inflate the activity's UI, initializing objects, etc.
     * param savedInstanceState If the activity is being re-initialized after previously being shut down
     *                           then this Bundle contains the data it most recently supplied in onSaveInstanceState(Bundle).
     *                           Note: Otherwise, it is null.
     */
    //  @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.events_created_page);
        db = FirebaseFirestore.getInstance();
        // Initialize your components here
        initializeUI();
        shareCheckin = findViewById(R.id.shareCheckin);
        sharePromo = findViewById(R.id.sharePromoCode);

        intentSource = getIntent().getStringExtra("source");

        if (intentSource != null) {
            Log.e("etowsley", "Successfully passed intent.");
            saveCheckinCode.setVisibility(View.INVISIBLE);
            savePromoCode.setVisibility(View.INVISIBLE);
            saveProj.setVisibility(View.INVISIBLE);
            congratsText.setVisibility(View.INVISIBLE);
            newEventText.setVisibility(View.INVISIBLE);
            token = getIntent().getStringExtra("CheckInQRCodeEventDetails");
            token2 = getIntent().getStringExtra("PromoQRCodeEventDetails");
            if (token == null) {
                Log.e("etowsley", "Token was null");
            }
            else if (token2 == null) {
                Log.e("etowsley", "Token2 was null");
            }
                Log.e("etowsley", "intentSource was not null");
            }
        else {
            name = getIntent().getStringExtra("name");
            description = getIntent().getStringExtra("description");
            //String imageString = getIntent().getStringExtra("posterImage");
            eventLocation = getIntent().getStringExtra("Location");
            eventTime = getIntent().getStringExtra("Time");
            attendeeLimit = getIntent().getStringExtra("attendeeLimit");
            token = getIntent().getStringExtra("CheckinToken");
            token2 = getIntent().getStringExtra("PromoToken");
            Log.e("etowsley", "NewEventActivity Source Intent was null");
        }

        generateQRs();
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e("hpeebles", "Going to event page");
                finish();
            }
        });
        saveProj.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Map<String, Object> event = new HashMap<>();
                event.put("name", name);
                event.put("attendeeLimit", attendeeLimit);
                event.put("time", eventTime);
                event.put("location", eventLocation);
                event.put(" ", description);
                event.put("checkinToken", token);
                event.put("promoToken", token2);
                event.put("posterImage", imageString); // Add the image string to the event map
                final String deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
                event.put("organizer", deviceId); // Add device ID as organizer
                //event.put("attendeeCount", 0);
                // Save event to Firestore
                Log.e("hpeebles", "before storing in db");
                db.collection("events")
                        .add(event)
                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                            @Override
                            public void onSuccess(DocumentReference documentReference) {
                                Log.e("hpeebles", "Added to DB");
                                Toast.makeText(NewEventActivity.this, "Event saved successfully", Toast.LENGTH_SHORT).show();
                                Intent homePage = new Intent(NewEventActivity.this, HomeActivity.class);
                                startActivity(homePage);
                            }
                        })
                        .addOnFailureListener(e -> Toast.makeText(getApplicationContext(), "Failed to create event", Toast.LENGTH_SHORT).show());
//                Intent homePage = new Intent(NewEventActivity.this, HomeActivity.class);
//                startActivity(homePage);
            }
        });

        /**
         * activates the QR scanner to get the custom qr code for checkins
         */
        saveCheckinCode.setOnClickListener(v -> {
            QRCodeScanner.imageString = imageString;
            Intent scanner = new Intent(NewEventActivity.this, QRCodeScanner.class);
            scanner.putExtra("Activity", 2);
            scanner.putExtra("name", name);
            scanner.putExtra("attendeeLimit", attendeeLimit);
            scanner.putExtra("Time", eventTime);
            scanner.putExtra("Location", eventLocation);
            scanner.putExtra("description", description);
            scanner.putExtra("PromoQRCode", token2);
            startActivity(scanner);
        });
        /**
         * activates the QR scanner to get the custom qr code for event promotion
         */
        savePromoCode.setOnClickListener(v -> {
            QRCodeScanner.imageString = imageString;
            Intent scanner = new Intent(NewEventActivity.this, QRCodeScanner.class);
            scanner.putExtra("Activity", 2);
            scanner.putExtra("name", name);
            scanner.putExtra("attendeeLimit", attendeeLimit);
            scanner.putExtra("Time", eventTime);
            scanner.putExtra("Location", eventLocation);
            scanner.putExtra("description", description);
            scanner.putExtra("QRCode", token);
            startActivity(scanner);        });
        shareCheckin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                shareImage(checkinQR.getQRPic(), "Check in QR shared from Scandal");
            }
        });
        sharePromo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                shareImage(promoQR.getQRPic(), "Promotional QR shared from Scandal");
            }
        });
    }
    private void initializeUI(){
        backButton = findViewById(R.id.buttonBack_EventsCreatedPage);
        checkinQRCode = findViewById(R.id.checkinQRCode);
        promoQRCode = findViewById(R.id.promoQRCode);
        saveCheckinCode = findViewById(R.id.buttonSaveCheckinCode);
        savePromoCode = findViewById(R.id.buttonSavePromoCode);
        saveProj = findViewById(R.id.buttonSaveProject);
        congratsText = findViewById(R.id.textCongratEventsCreated);
        newEventText = findViewById(R.id.textNewEventsCreated);
    }
    private void generateQRs(){
        checkinQR = new QRCode(); // Assuming you have a default constructor
        promoQR = new QRCode();

        if (checkinQR.generateQR(checkinQRCode, token)) {
            checkinQRCode.setImageBitmap(checkinQR.getQRPic());
        } else {
            Log.e("NewEventActivity", "Checkin QR generation failed");
        }
        if (promoQR.generateQR(promoQRCode, token2)) {
            promoQRCode.setImageBitmap(promoQR.getQRPic());
        } else {
            Log.e("NewEventActivity", "Promo QR generation failed");
        }
    }

    /**
     * Shares in image pic with other apps and send the textAccompany with it
     * @param pic img being shared
     * @param textAccompany text to be sent with img
     */
    protected void shareImage(Bitmap pic, String textAccompany){
        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType("image/jpeg");
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        pic.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        File f = new File(Environment.getExternalStorageDirectory().getPath() + File.separator + "temporary_file.jpg");
        try {
            f.createNewFile();
            FileOutputStream fo = new FileOutputStream(f);
            fo.write(bytes.toByteArray());
        } catch (IOException e) {
            e.printStackTrace();
        }
        share.putExtra(Intent.EXTRA_STREAM, Uri.parse('/'+textAccompany+".jpg"));
        startActivity(Intent.createChooser(share, "Share Image"));
//        Intent share = new Intent(Intent.ACTION_SENDTO);
//        share.setType("image/*");
//        Uri picUri;
//        picUri = saveImage(pic, getApplicationContext());
//        share.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        share.putExtra(Intent.EXTRA_STREAM, picUri);
//        share.putExtra(Intent.EXTRA_SUBJECT, "Share To Apps");
//        share.putExtra(Intent.EXTRA_TEXT, textAccompany);
//        startActivity(Intent.createChooser(share, "Share Data"));
    }

    /**
     * This method takes a bit map and the context and converts and returns the uri of the image
     * @param pic bitmap to be converted to uri format
     * @param instance the context in which the method is called
     * @return a uri version of the bitmap passed
     */
    private Uri saveImage(Bitmap pic, Context instance){
        File imageFolder = new File(instance.getCacheDir(), "images");
        Uri picUri = null;
        try{
            imageFolder.mkdir();
            File file = new File(imageFolder, "share_codes.jpg");
            FileOutputStream stream = new FileOutputStream(file);
            pic.compress(Bitmap.CompressFormat.JPEG, 90, stream);
            stream.flush();
            stream.close();
            picUri = FileProvider.getUriForFile(Objects.requireNonNull(instance.getApplicationContext()),
                    "com.example.scandal"+".provider", file);
            Log.e("hpeebles", picUri.toString());
        } catch (IOException error){
            Log.d("saveImage", "Exception"+error.getMessage());
        }
        return picUri;
    }
}