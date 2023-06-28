package com.example.adventuretravellog;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;

public class ForgetPasswordActivity extends AppCompatActivity {

    private ImageView backButton;
    private EditText email;
    private Button sendMailButton;

    private FirebaseAuth auth;

    @SuppressLint({"WrongViewCast", "MissingInflatedId"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_forget_password);

        backButton = findViewById(R.id.back_icon);
        email = findViewById(R.id.emailForForgetPassword);
        sendMailButton = findViewById(R.id.sendMailButton);

        auth = FirebaseAuth.getInstance();

        sendMailButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String emailString = email.getText().toString();

                if (!checkIfFieldsAreFilled(emailString)){
                    //Toast.makeText(getApplicationContext(), "Please add the proper email address", Toast.LENGTH_LONG).show();
                    displayToast(250, "Please add a proper email.");
                }else{
                    auth.sendPasswordResetEmail(emailString)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                   // Toast.makeText(ForgetPasswordActivity.this, "Email sent!", Toast.LENGTH_SHORT).show();
                                    displayToast(250, "Email Sent!");
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    //Toast.makeText(ForgetPasswordActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                    displayToast(250, e.getMessage());
                                }
                            });
                }
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ForgetPasswordActivity.this, SigninActivity.class));
                finish();
            }
        });
    }

    private boolean checkIfFieldsAreFilled(
            String emailString
    ) {
        return !emailString.equals("");
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