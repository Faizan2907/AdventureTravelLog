package com.example.adventuretravellog;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.adventuretravellog.Adapter.TripAdapter;
import com.example.adventuretravellog.Model.Trip;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class DashboardActivity extends AppCompatActivity {

    private FloatingActionButton floatingActionButton;
    private FirebaseAuth firebaseAuth;
    private boolean doubleBackToExitPressedOnce = false;
    private ImageButton profileButtonTop;
    private RecyclerView recyclerViewTrips;
    private List<String> tripNames;
    private List<String> tripIds;
    private TripAdapter tripAdapter;
    private DatabaseReference tripsRef;
    private EditText editTextSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        floatingActionButton = findViewById(R.id.floatingActionButton);
        firebaseAuth = FirebaseAuth.getInstance();
        profileButtonTop = findViewById(R.id.profileButton);
        editTextSearch = findViewById(R.id.editTextSearch);

        tripNames = new ArrayList<>();
        tripIds = new ArrayList<>();

        recyclerViewTrips = findViewById(R.id.recyclerViewTrips);
        recyclerViewTrips.setLayoutManager(new LinearLayoutManager(this));

        tripAdapter = new TripAdapter(tripNames, tripIds);
        recyclerViewTrips.setAdapter(tripAdapter);

        tripsRef = FirebaseDatabase.getInstance().getReference().child("trips");

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        tripsRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<String> tripNames = new ArrayList<>();
                List<String> tripIds = new ArrayList<>();

                for (DataSnapshot tripSnapshot : dataSnapshot.getChildren()) {
                    Trip trip = tripSnapshot.getValue(Trip.class);
                    String tripName = trip.getName();
                    String tripId = tripSnapshot.getKey();

                    tripNames.add(tripName);
                    tripIds.add(tripId);
                }

                // Update the adapter with the new trip names and IDs
                tripAdapter.setTripData(tripNames, tripIds);
                tripAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle any error that occurs
                displayToast(250, "Failed to retrieve trips: " + databaseError.getMessage());
            }
        });

        profileButtonTop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(DashboardActivity.this, UserProfile.class));
            }
        });

        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(DashboardActivity.this, TripCreationActivity.class));
            }
        });

        editTextSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Not needed in this case
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterData(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Not needed in this case
            }
        });

        tripsRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                tripNames.clear();
                tripIds.clear();

                for (DataSnapshot tripSnapshot : dataSnapshot.getChildren()) {
                    Trip trip = tripSnapshot.getValue(Trip.class);
                    String tripName = trip.getName();
                    String tripId = tripSnapshot.getKey();

                    tripNames.add(tripName);
                    tripIds.add(tripId);
                }

                // Update the adapter with the new trip names and IDs
                tripAdapter.setTripData(tripNames, tripIds);
                tripAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle any error that occurs
                displayToast(250, "Failed to retrieve trips: " + databaseError.getMessage());
            }
        });

    }

    private void filterData(String query) {
        List<String> filteredTripNames = new ArrayList<>();
        List<String> filteredTripIds = new ArrayList<>();

        for (int i = 0; i < tripNames.size(); i++) {
            String tripName = tripNames.get(i);
            String tripId = tripIds.get(i);

            // Apply your filtering logic here
            if (tripName.toLowerCase().contains(query.toLowerCase())) {
                filteredTripNames.add(tripName);
                filteredTripIds.add(tripId);
            }
        }

        // Update the adapter with the filtered trip names and IDs
        tripAdapter.setTripData(filteredTripNames, filteredTripIds);
        tripAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currUSer = firebaseAuth.getCurrentUser();
        if(currUSer == null){
            startActivity(new Intent(getApplicationContext(), SigninActivity.class));
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            finishAffinity(); // Exit the app completely
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        displayToast(250, "Press back again to exit");

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                doubleBackToExitPressedOnce = false;
            }
        }, 2000); // 2 seconds delay to allow the user to press back again
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