package com.example.adventuretravellog;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.adventuretravellog.Model.Users;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class UserProfile extends AppCompatActivity {

    private EditText emailEditText;
    private EditText userNameEditText;
    private EditText mobileEditText;
    private FirebaseAuth firebaseAuth;
    private Button saveButton;
    private Button logoutButton;
    private Dialog deleteDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        emailEditText = findViewById(R.id.emailForSignup);
        userNameEditText = findViewById(R.id.userName);
        mobileEditText = findViewById(R.id.mobileNo);

        saveButton = findViewById(R.id.saveButton);
        logoutButton = findViewById(R.id.logoutButton);

        firebaseAuth = FirebaseAuth.getInstance();
        deleteDialog = new Dialog(this);

        String userId = firebaseAuth.getCurrentUser().getUid();
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("users").child(userId);
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Users user = snapshot.getValue(Users.class);
                    if (user != null) {
                        emailEditText.setText(user.getEmail());
                        userNameEditText.setText(user.getUsername());
                        mobileEditText.setText(user.getMobile());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                displayToast(250, error.getMessage());
            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newEmail = emailEditText.getText().toString();
                String newUserName = userNameEditText.getText().toString();
                String newMobile = mobileEditText.getText().toString();

                String userId = firebaseAuth.getCurrentUser().getUid();
                DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("users").child(userId);
                userRef.child("email").setValue(newEmail);
                userRef.child("username").setValue(newUserName);
                userRef.child("mobile").setValue(newMobile)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    displayToast(250, "Changes saved successfully!");
                                } else {
                                    displayToast(250, task.getException().getMessage());
                                }
                            }
                        });
            }
        });

        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmLogout();
            }
        });


    }

    private void confirmLogout() {
        deleteDialog.setContentView(R.layout.confirm_logout);
        deleteDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        CardView confirmCard = deleteDialog.findViewById(R.id.delete_card);
        deleteDialog.show();

        FrameLayout confirmButton = deleteDialog.findViewById(R.id.confirm_logout);
        FrameLayout cancelButton = deleteDialog.findViewById(R.id.cancel_logout);

        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                firebaseAuth.signOut();
                startActivity(new Intent(UserProfile.this, SigninActivity.class));
                finish();
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteDialog.dismiss();
            }
        });
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