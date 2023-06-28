package com.example.adventuretravellog;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.adventuretravellog.Adapter.ActivitiesAdapter;
import com.example.adventuretravellog.Model.Trip;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TripDetailsActivity extends AppCompatActivity {

    private String tripId;
    private Trip trip;
    private List<String> photosList;

    private EditText editTextTripName;
    private EditText editTextDescription;
    private EditText editTextDestination;
    private EditText editTextStartDate;
    private EditText editTextEndDate;
    private GridView gridViewPhotos;
    private RecyclerView activitiesRecyclerView;
    private List<String> activitiesList;
    private Button saveChangesButton;
    private ImageView chooseStartDateImageView;
    private ImageView chooseEndDateImageView;
    private Calendar startDateCalendar;
    private Calendar endDateCalendar;
    private SimpleDateFormat dateFormat;
    private Button myDiaryButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_details);

        // Retrieve the trip ID from the intent extras
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            tripId = extras.getString("tripId");
        }

        editTextTripName = findViewById(R.id.editTextTripName);
        editTextDescription = findViewById(R.id.editTextDescription);
        editTextDestination = findViewById(R.id.editTextDestination);
        editTextStartDate = findViewById(R.id.editTextStartDate);
        editTextEndDate = findViewById(R.id.editTextEndDate);
        gridViewPhotos = findViewById(R.id.gridViewPhotos);
        activitiesRecyclerView = findViewById(R.id.activitiesRecyclerView);
        activitiesList = new ArrayList<>();
        saveChangesButton = findViewById(R.id.saveChangesButton);
        chooseStartDateImageView = findViewById(R.id.chooseStartDate);
        chooseEndDateImageView = findViewById(R.id.chooseEndDate);
        myDiaryButton = findViewById(R.id.myDiaryButton);

        startDateCalendar = Calendar.getInstance();
        endDateCalendar = Calendar.getInstance();

        dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

        // Set up the layout manager and adapter for the RecyclerView
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        activitiesRecyclerView.setLayoutManager(layoutManager);
        ActivitiesAdapter activitiesAdapter = new ActivitiesAdapter(activitiesList);
        activitiesRecyclerView.setAdapter(activitiesAdapter);

        chooseStartDateImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePicker(startDateCalendar, editTextStartDate);
            }
        });

        chooseEndDateImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePicker(endDateCalendar, editTextEndDate);
            }
        });

        saveChangesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateDates()) {
                    updateTrip();
                }
            }
        });

        myDiaryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TripDetailsActivity.this, DiaryActivity.class);
                intent.putExtra("tripId", tripId); // Move this line before startActivity()
                startActivity(intent);
            }
        });


        fetchTripDetails();
    }

    private void updateTrip() {
        String newName = editTextTripName.getText().toString().trim();
        String newDescription = editTextDescription.getText().toString().trim();
        String newDestination = editTextDestination.getText().toString().trim();
        String newStartDate = editTextStartDate.getText().toString().trim();
        String newEndDate = editTextEndDate.getText().toString().trim();

        // Update the trip details in the database
        DatabaseReference tripRef = FirebaseDatabase.getInstance().getReference("trips")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child(tripId);

        tripRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Trip trip = dataSnapshot.getValue(Trip.class);
                    if (trip != null) {
                        // Update the trip object with the new values
                        trip.setName(newName);
                        trip.setDescription(newDescription);
                        trip.setDestination(newDestination);
                        trip.setStartDate(newStartDate);
                        trip.setEndDate(newEndDate);

                        // Update the trip object in the database
                        tripRef.setValue(trip)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        displayToast(250, "Trip details updated successfully");
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        displayToast(250, "Failed to update trip details: " + e.getMessage());
                                    }
                                });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                displayToast(250, "Failed to fetch trip details");
            }
        });
    }

    private void fetchTripDetails() {
        DatabaseReference tripRef = FirebaseDatabase.getInstance().getReference("trips").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child(tripId);
        tripRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Trip trip = dataSnapshot.getValue(Trip.class);
                    if (trip != null) {
                        // Update the UI with the fetched trip details
                        editTextTripName.setText(trip.getName());
                        editTextDescription.setText(trip.getDescription());
                        editTextDestination.setText(trip.getDestination());
                        editTextStartDate.setText(trip.getStartDate());
                        editTextEndDate.setText(trip.getEndDate());

                        // Fetch the photos list from the trip object
                        List<String> photosList = trip.getPhotos();
                        if (photosList != null) {
                            displayPhotos();
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                displayToast(250, "Failed to fetch trip details");
            }
        });
    }

    private void showDatePicker(final Calendar calendar, final EditText editText) {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                TripDetailsActivity.this,
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
            editTextEndDate.setError("End date must be greater than start date");
            return false;
        } else {
            editTextEndDate.setError(null);
            return true;
        }
    }

    private void displayPhotos() {
        // Create an adapter for the GridView to display the photos
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.item_photo, R.id.imageViewPhoto, photosList) {
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View view = super.getView(position, convertView, parent);

                // Load and display the photo from the URL using an image loading library like Glide
                ImageView imageViewPhoto = view.findViewById(R.id.imageViewPhoto);
                String photoUrl = getItem(position);
                Glide.with(TripDetailsActivity.this)
                        .load(photoUrl)
                        .into(imageViewPhoto);

                return view;
            }
        };
        gridViewPhotos.setAdapter(adapter);
    }

    private void displayToast(int yOffSet, String message){
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