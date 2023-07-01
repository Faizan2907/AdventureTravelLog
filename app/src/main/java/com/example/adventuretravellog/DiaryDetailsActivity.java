package com.example.adventuretravellog;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.VideoView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.adventuretravellog.Model.DiaryEntry;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class DiaryDetailsActivity extends AppCompatActivity {

    // Inside your activity's onCreate() method or wherever you handle the media file
    ImageView imageMediaFile;
    PlayerView videoMediaFile;
    TextView diaryEntryInText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diary_details);

        diaryEntryInText = findViewById(R.id.diaryEntryInText);
        imageMediaFile = findViewById(R.id.imageMediaFile);
        videoMediaFile = findViewById(R.id.videoMediaFile);

        String diaryId = getIntent().getStringExtra("diaryId");
        String tripId = getIntent().getStringExtra("tripId");
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        DatabaseReference diaryRef = FirebaseDatabase.getInstance().getReference()
                .child("trips")
                .child(userId)
                .child(tripId)
                .child("diary")
                .child(diaryId);

        diaryRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    DiaryEntry diaryEntry = dataSnapshot.getValue(DiaryEntry.class);
                    if (diaryEntry != null) {
                        // Set the diary entry text
                        diaryEntryInText.setText(diaryEntry.getText());

                        String mediaType = diaryEntry.getMediaType();
                        String mediaUrl = diaryEntry.getMediaUrl();

                        if (mediaType != null && mediaUrl != null) {
                            if (mediaType.startsWith("image")) {
                                Glide.with(DiaryDetailsActivity.this)
                                        .load(mediaUrl)
                                        .into(imageMediaFile);
                                imageMediaFile.setVisibility(View.VISIBLE);
                            } else if (mediaType.startsWith("video")) {
                                // Play the video using ExoPlayer
                                videoMediaFile.setVisibility(View.VISIBLE);
                                initializeExoPlayer(mediaUrl);
                            }
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle the error
            }
        });
    }

    private void initializeExoPlayer(String videoUrl) {
        SimpleExoPlayer exoPlayer = new SimpleExoPlayer.Builder(this).build();
        PlayerView playerView = findViewById(R.id.videoMediaFile);
        playerView.setPlayer(exoPlayer);

        MediaItem mediaItem = MediaItem.fromUri(videoUrl);
        exoPlayer.setMediaItem(mediaItem);
        exoPlayer.prepare();
        exoPlayer.play();
    }
}