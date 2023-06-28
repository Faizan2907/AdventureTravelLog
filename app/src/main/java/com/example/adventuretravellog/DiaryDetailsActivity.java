package com.example.adventuretravellog;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.adventuretravellog.Model.DiaryEntry;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class DiaryDetailsActivity extends AppCompatActivity {

    private TextView diaryEntryInText;
    private ImageView mediaFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diary_details);

        mediaFile = findViewById(R.id.mediaFile);
        diaryEntryInText = findViewById(R.id.diaryEntryInText);

        String diaryId = getIntent().getStringExtra("diaryId");

//        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
//        DatabaseReference diaryRef = FirebaseDatabase.getInstance().getReference()
//                .child("trips")
//                .child(userId)
//                .child(tripId)
//                .child("diary");
//
//        diaryRef.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                if (dataSnapshot.exists()) {
//                    for (DataSnapshot entrySnapshot : dataSnapshot.getChildren()) {
//                        DiaryEntry diaryEntry = entrySnapshot.getValue(DiaryEntry.class);
//
//                        // Retrieve the necessary values from the diaryEntry object
//                        String mediaUrl = diaryEntry.getMediaUrl();
//                        String text = diaryEntry.getText();
//
//                        // Use the retrieved values as needed
//                    }
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//                // Handle the error
//            }
//        });

    }
}