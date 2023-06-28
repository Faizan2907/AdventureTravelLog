package com.example.adventuretravellog;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.adventuretravellog.Model.Users;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.auth.User;

public class SignupActivity extends AppCompatActivity {


    private TextView terms;
    private EditText email, password, mobile, username;
    private CheckBox agreeRadioButton;
    private Button signupButton;
    private LinearLayout haveAnAccount;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        email = findViewById(R.id.emailForSignup);
        password = findViewById(R.id.passwordForSignup);
        username = findViewById(R.id.userName);
        mobile = findViewById(R.id.mobileNo);

        signupButton = findViewById(R.id.signUpButton);
        haveAnAccount = findViewById(R.id.alreadyHaveAccount);

        agreeRadioButton = findViewById(R.id.agree);

        terms = findViewById(R.id.term);

        auth = FirebaseAuth.getInstance();

        FirebaseUser firebaseUser = auth.getCurrentUser();

        terms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SignupActivity.this, TermsAndConditions.class));
            }
        });

        haveAnAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), SigninActivity.class));
                finish();
            }
        });

        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String emailString = email.getText().toString();
                String passwordString = password.getText().toString();
                String userNameString = username.getText().toString();
                String mobileString = mobile.getText().toString();

                if (!checkIfFieldsAreFilled(emailString, passwordString)) {
                    displayToast(250, "Please fill all the fields");
                } else {
                    if (!agreeRadioButton.isChecked()) {
                        Toast.makeText(SignupActivity.this, "Please select the Terms and Conditions", Toast.LENGTH_SHORT).show();
                        return; // exit the method without executing the sign-up code
                    } else {
                        auth.createUserWithEmailAndPassword(emailString, passwordString)
                                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                        if (task.isSuccessful()) {
                                            String userId = auth.getCurrentUser().getUid();
                                            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("users").child(userId);
                                            Users user = new Users(userNameString, emailString, mobileString);
                                            userRef.setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        displayToast(250, "Registered Successfully!");
                                                        startActivity(new Intent(getApplicationContext(), SigninActivity.class));
                                                        finish();
                                                    } else {
                                                        Toast.makeText(SignupActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            });
                                        } else {
                                            Toast.makeText(SignupActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                    }
                }
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            Intent intent = new Intent(this, DashboardActivity.class);
            startActivity(intent);
        }
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

    private boolean checkIfFieldsAreFilled(
            String emailString, String passwordString
    ) {
        return !emailString.equals("") && !passwordString.equals("");
    }

}