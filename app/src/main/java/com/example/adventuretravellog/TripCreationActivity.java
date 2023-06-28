package com.example.adventuretravellog;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.DatePickerDialog;
import android.content.ClipData;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.transition.Transition;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.FutureTarget;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.example.adventuretravellog.Adapter.ActivitiesAdapter;
import com.example.adventuretravellog.Model.Trip;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class TripCreationActivity extends AppCompatActivity {
    private static final int REQUEST_CODE_SELECT_IMAGES = 1;
    private EditText nameEditText;
    private EditText destinationEditText;
    private EditText descriptionEditText;
    private Button createButton;
    private List<String> photosList;
    private List<String> activitiesList;
    private EditText startDateEditText;
    private EditText endDateEditText;
    private EditText activityNameEditText;
    private ImageView chooseStartDateImageView;
    private ImageView chooseEndDateImageView;
    private Calendar startDateCalendar;
    private Calendar endDateCalendar;
    private SimpleDateFormat dateFormat;
    private DatabaseReference tripsRef;
    private Button addActivityButton, uploadPhotosButton;
    private RecyclerView activitiesRecyclerView;
    private ActivitiesAdapter activitiesAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_creation);

        // Initialize Firebase Database reference
        tripsRef = FirebaseDatabase.getInstance().getReference().child("trips");

        // Get references to views
        nameEditText = findViewById(R.id.nameEditText);
        destinationEditText = findViewById(R.id.destinationEditText);
        descriptionEditText = findViewById(R.id.descriptionEditText);
        createButton = findViewById(R.id.createButton);
        uploadPhotosButton = findViewById(R.id.uploadPhotoButton);

        startDateEditText = findViewById(R.id.startDate);
        endDateEditText = findViewById(R.id.endDate);
        chooseStartDateImageView = findViewById(R.id.chooseStartDate);
        chooseEndDateImageView = findViewById(R.id.chooseEndDate);

        addActivityButton = findViewById(R.id.addActivity);
        activityNameEditText = findViewById(R.id.activityNameEditText);
        activitiesRecyclerView = findViewById(R.id.activitiesRecyclerView);

        activitiesList = new ArrayList<>();
        activitiesAdapter = new ActivitiesAdapter(activitiesList);
        activitiesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        activitiesRecyclerView.setAdapter(activitiesAdapter);

        startDateCalendar = Calendar.getInstance();
        endDateCalendar = Calendar.getInstance();

        dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

        uploadPhotosButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Open the gallery to select multiple images
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                startActivityForResult(Intent.createChooser(intent, "Select Images"), REQUEST_CODE_SELECT_IMAGES);
            }
        });

        chooseStartDateImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePicker(startDateCalendar, startDateEditText);
            }
        });

        chooseEndDateImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePicker(endDateCalendar, endDateEditText);
            }
        });

        // Initialize photos and activities lists
        photosList = new ArrayList<>();
        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateDates()) {
                    createTrip();
                }
            }
        });

        addActivityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String activityName = activityNameEditText.getText().toString().trim();

                if (!TextUtils.isEmpty(activityName)) {
                    activitiesList.add(activityName);
                    activitiesAdapter.notifyItemInserted(activitiesList.size() - 1);

                    activityNameEditText.setText("");
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_SELECT_IMAGES && resultCode == RESULT_OK) {
            if (data != null) {
                // Get selected images
                ClipData clipData = data.getClipData();
                if (clipData != null) {
                    // Multiple images selected
                    for (int i = 0; i < clipData.getItemCount(); i++) {
                        Uri imageUri = clipData.getItemAt(i).getUri();
                        // Add image reference to the photosList
                        photosList.add(imageUri.toString());
                    }
                } else {
                    // Single image selected
                    Uri imageUri = data.getData();
                    // Add image reference to the photosList
                    photosList.add(imageUri.toString());
                }
                // Show a message or perform any desired action
                Toast.makeText(this, "Images selected: " + photosList.size(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void showDatePicker(final Calendar calendar, final EditText editText) {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                TripCreationActivity.this,
                (view, year, monthOfYear, dayOfMonth) -> {
                    calendar.set(year, monthOfYear, dayOfMonth);
                    Date selectedDate = calendar.getTime();
                    editText.setText(dateFormat.format(selectedDate));
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));

        datePickerDialog.getDatePicker().setMinDate(calendar.getTimeInMillis()); // Restrict selecting past dates
        datePickerDialog.show();
    }

    private boolean validateDates() {
        Date startDate = startDateCalendar.getTime();
        Date endDate = endDateCalendar.getTime();

        if (endDate.before(startDate)) {
            endDateEditText.setError("End date must be greater than start date");
            return false;
        } else {
            endDateEditText.setError(null);
            return true;
        }
    }

    private void createTrip() {
        String name = nameEditText.getText().toString().trim();
        String destination = destinationEditText.getText().toString().trim();
        String startDate = startDateEditText.getText().toString().trim();
        String endDate = endDateEditText.getText().toString().trim();
        String description = descriptionEditText.getText().toString().trim();

        // Perform validation on user input
        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(destination) ||
                TextUtils.isEmpty(startDate) || TextUtils.isEmpty(endDate)) {
            displayToast(250, "Please fill in all required fields");
            return;
        }

        // Get the current user's ID
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Create a new Trip object
        Trip trip = new Trip(name, destination, startDate, endDate, null, description, activitiesList);

        // Generate a unique key for the trip entry in Firebase Realtime Database
        String tripKey = tripsRef.child(userId).push().getKey();

        // Store the trip details under the generated key, within the user's trips node
        tripsRef.child(userId).child(tripKey).setValue(trip)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            // Store the images in Firebase Storage
                            storeImagesInStorage(userId, tripKey);
                        } else {
                            displayToast(250, "Failed to create trip: " + task.getException().toString());
                        }
                    }
                });
    }

    private void storeImagesInStorage(String userId, String tripKey) {
        // Get a reference to the Firebase Storage
        StorageReference storageRef = FirebaseStorage.getInstance().getReference();

        // Loop through the photosList and upload each image to Firebase Storage
        for (int i = 0; i < photosList.size(); i++) {
            String imageUrl = photosList.get(i);

            // Generate a unique image filename
            String imageFileName = UUID.randomUUID().toString();

            // Create a reference to the image file in Firebase Storage
            StorageReference imageRef = storageRef.child(userId).child(tripKey).child(imageFileName);

            // Use Glide to load the image and upload it to Firebase Storage
            int finalI = i;
            Glide.with(this)
                    .downloadOnly()
                    .load(imageUrl)
                    .apply(new RequestOptions().override(Target.SIZE_ORIGINAL))
                    .addListener(new RequestListener<File>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<File> target, boolean isFirstResource) {
                            // Handle the failure case
                            displayToast(250, "Failed to load image: " + e.getMessage());
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(File resource, Object model, Target<File> target, DataSource dataSource, boolean isFirstResource) {
                            // Upload the image file to Firebase Storage
                            Uri imageUri = Uri.fromFile(resource);
                            imageRef.putFile(imageUri)
                                    .addOnSuccessListener(taskSnapshot -> {
                                        // Get the download URL of the uploaded image
                                        imageRef.getDownloadUrl().addOnSuccessListener(downloadUri -> {
                                            // Store the image reference in the Realtime Database under the trip node
                                            String imageDownloadUrl = downloadUri.toString();
                                            tripsRef.child(userId).child(tripKey).child("photosList").child(String.valueOf(finalI)).setValue(imageDownloadUrl);
                                        });
                                    })
                                    .addOnFailureListener(e -> {
                                        // Handle the failure case
                                        displayToast(250, "Failed to upload image: " + e.getMessage());
                                    });
                            return false;
                        }
                    })
                    .submit();

        }

        // Display a success message or perform any desired action
        displayToast(250, "Trip created successfully");
        finish();
    }

    private void displayToast(int yOffSet, String message) {
        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout.toast_layout, null);

        TextView text = (TextView) layout.findViewById(R.id.toast_text);
        text.setText(message);

        Toast toast = new Toast(getApplicationContext());
        toast.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, yOffSet);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(layout);

        // Set the custom background drawable
        View toastView = toast.getView();
        toastView.setBackgroundResource(R.drawable.toast_background);

        toast.show();
    }

}