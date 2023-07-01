package com.example.adventuretravellog;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.adventuretravellog.Adapter.DiaryAdapter;
import com.example.adventuretravellog.Model.DiaryEntry;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class DiaryActivity extends AppCompatActivity {
    private EditText diaryEntryEditText, editTextSearch;
    private Button saveEntryButton, attachedMediaButton;
    private RecyclerView diaryRecyclerView;
    private List<String> itemList; // List to hold items for the RecyclerView
    private DiaryAdapter diaryAdapter; // Adapter for the RecyclerView
    private List<String> diaryIdsList;
    private static final int REQUEST_MEDIA = 1;
    String tripId;
    private Uri mediaUri;
    DatabaseReference tripRef;
    private Uri selectedImageUri;
    List<DiaryEntry> diaryEntries;

    String diaryId;
    String mediaType;
    String mediaUrl;
    String text;
    private Uri selectedMediaUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diary);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            tripId = extras.getString("tripId");
        }

        tripRef = FirebaseDatabase.getInstance().getReference("trips").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child(tripId);

        diaryEntryEditText = findViewById(R.id.editTextDiaryEntry);
//        editTextSearch = findViewById(R.id.editTextSearch);

        saveEntryButton = findViewById(R.id.buttonSaveEntry);
        attachedMediaButton = findViewById(R.id.buttonAttachMedia);

        diaryRecyclerView = findViewById(R.id.diaryItemRecyclerView);
        diaryRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        String diaryString = diaryEntryEditText.getText().toString();

        itemList = new ArrayList<>();
        diaryIdsList = new ArrayList<>();

        diaryEntries = new ArrayList<>();

        diaryAdapter = new DiaryAdapter(diaryEntries, tripId); // Create an adapter with the item list
        diaryRecyclerView.setAdapter(diaryAdapter);

        // Add the code to retrieve diary entries
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference diaryRef = FirebaseDatabase.getInstance().getReference()
                .child("trips")
                .child(userId)
                .child(tripId)
                .child("diary");

        diaryRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot diarySnapshot : dataSnapshot.getChildren()) {
                    diaryId = diarySnapshot.getKey();
                    mediaType = diarySnapshot.child("mediaType").getValue(String.class);
                    mediaUrl = diarySnapshot.child("mediaUrl").getValue(String.class);
                    text = diarySnapshot.child("text").getValue(String.class);

                    // Create a DiaryEntry object using the retrieved values
                    DiaryEntry diaryEntry = new DiaryEntry(diaryId, mediaType, mediaUrl, text);
                    diaryEntries.add(diaryEntry);
                }

                // Notify the adapter that the data has changed
                diaryAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle the error
            }
        });

        ActivityResultLauncher<Intent> launcher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null) {
                            Uri selectedMediaUri = data.getData();

                            // Upload the selected media file to Firebase Storage
                            uploadMediaToStorage(selectedMediaUri);
                        }
                    }
                });

        attachedMediaButton.setOnClickListener(v -> {
            // Open the gallery using an intent
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.setType("image/* video/*"); // Allow selection of both images and videos
            launcher.launch(intent);
        });

        saveEntryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Save the diary entry and attached media
                String diaryText = diaryEntryEditText.getText().toString();

                // Check if the diary entry text is empty
                if (diaryText.isEmpty()) {
                    // Display an error message or perform any required validation
                    Toast.makeText(getApplicationContext(), "Diary entry is empty", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Check if the selectedMediaUri is null
                if (selectedMediaUri == null) {
                    // Display an error message or perform any required validation
                    Toast.makeText(getApplicationContext(), "No media selected", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Upload the selected media file to Firebase Storage
                uploadMediaToStorage(selectedMediaUri);
                diaryAdapter.addDiaryEntry(diaryId, mediaType, mediaUrl, diaryText);

                diaryEntryEditText.setText("");
                selectedMediaUri = null;

                Toast.makeText(getApplicationContext(), "Diary entry saved successfully.", Toast.LENGTH_SHORT).show();
            }
        });

    }


    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_MEDIA && resultCode == RESULT_OK && data != null) {
            selectedMediaUri = data.getData();

            if (selectedMediaUri != null) {
                // Upload the selected media file to Firebase Storage
                uploadMediaToStorage(selectedMediaUri);
            } else {
                // Handle the case when the selected media URI is null
                Toast.makeText(getApplicationContext(), "Failed to retrieve media URI", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void uploadMediaToStorage(Uri selectedMediaUri) {
        // Get a reference to the Firebase Storage
        StorageReference storageRef = FirebaseStorage.getInstance().getReference();

        // Create a unique filename for the media file
        String mediaFileName = UUID.randomUUID().toString();

        // Determine the file extension based on the media type
        String fileExtension;
        ContentResolver contentResolver = getContentResolver();
        String mimeType = contentResolver.getType(selectedMediaUri);
        if (mimeType != null && mimeType.contains("image")) {
            fileExtension = ".jpg";
        } else if (mimeType != null && mimeType.contains("video")) {
            fileExtension = ".mp4";
        } else {
            // Unsupported media type
            return;
        }

        // Create a reference to the media file in Firebase Storage
        StorageReference mediaFileRef = storageRef.child("Diary").child(mediaFileName + fileExtension);

        // Upload the media file to Firebase Storage
        mediaFileRef.putFile(selectedMediaUri)
                .addOnSuccessListener(taskSnapshot -> {
                    // Get the download URL of the uploaded media file
                    mediaFileRef.getDownloadUrl().addOnSuccessListener(downloadUri -> {
                        String mediaDownloadUrl = downloadUri.toString();

                        // Create a DiaryEntry object with the media download URL and other details
                        DiaryEntry diaryEntry = new DiaryEntry();
                        diaryEntry.setId(UUID.randomUUID().toString());
                        diaryEntry.setMediaType(mimeType);
                        diaryEntry.setMediaUrl(mediaDownloadUrl);
                        diaryEntry.setText(diaryEntryEditText.getText().toString());

                        // Save the DiaryEntry object to the Firebase Realtime Database
                        DatabaseReference diaryRef = tripRef.child("diary").child(diaryEntry.getId());
                        diaryRef.setValue(diaryEntry);

                    }).addOnFailureListener(e -> {

                    });
                })
                .addOnFailureListener(e -> {
                });
    }
}