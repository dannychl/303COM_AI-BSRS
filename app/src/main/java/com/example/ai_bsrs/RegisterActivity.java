package com.example.ai_bsrs;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener{

    private EditText namestr, emailstr, passstr, passConfirm;
    private Button register;
    private String message = "";
    private DatabaseReference mDatabaseReference;
    private List<String> myList;
    private ProgressBar mProgressBar;
    private TextView mToLogin;

    private FirebaseAuth mFirebaseAuth;
    private FirebaseFirestore db;
    private CollectionReference dbCollectionRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        mDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Username");

        namestr = (EditText) findViewById(R.id.username);
        emailstr = (EditText) findViewById(R.id.email);
        passstr = (EditText) findViewById(R.id.pass);
        passConfirm = (EditText) findViewById(R.id.passConfirm);
        register = (Button) findViewById(R.id.register);
        mProgressBar = (ProgressBar)findViewById(R.id.progressBar);
        mToLogin = (TextView) findViewById(R.id.to_login);
        myList = new ArrayList<>();

        db = FirebaseFirestore.getInstance();

        //firebase
        mFirebaseAuth = FirebaseAuth.getInstance();

        register.setOnClickListener(this);
        mToLogin.setOnClickListener(this);

        /*register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String Email = emailstr.getText().toString();
                String Password = passstr.getText().toString();
                mFirebaseAuth.createUserWithEmailAndPassword(Email, Password)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    mFirebaseAuth.getCurrentUser().sendEmailVerification()
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()){
                                                        Toast.makeText(MainActivity.this, "User registered successfully", Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.d("This email", e.getMessage());
                                        }
                                    });
                                }
                            }
                        });
            }
        });*/
    }

    public void validationRegister(){
        String username = namestr.getText().toString();
        String email = emailstr.getText().toString();
        String password = passstr.getText().toString();
        String z = "";
        boolean isSuccess = false;

        String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
        final String blockCharacterSet = "~#^|$%&*!";

        //message = "";
        //message = checkAllUsernameMatch(username);

        if (username.trim().equals("") || email.trim().equals("") || password.trim().equals(""))
            z = "Please enter all fields....";
        else if (message.equals("Username already used....")) {
            z = "Username already used....";
        } else if (username.trim().length() > 9)
            z = "Username only 9 character allowed...";
        else if (username.contains(blockCharacterSet))
            z = "Special character (~#^|$%&*!) not allowed";
        else if (!email.matches(emailPattern)) {
            z = "Invalid email address...";
        }
        else if (emailstr.length() > 254) {
            z = "Email exceed maximum limit... ";
        }

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.register:
                registerUser();
                break;
            case R.id.to_login:
                Intent i = new Intent(this, LoginActivity.class);
                startActivity(i);
        }
    }

    private void registerUser() {
        String email = emailstr.getText().toString().trim();
        String password = passstr.getText().toString().trim();
        String confirmPassword = passConfirm.getText().toString().trim();
        String username = namestr.getText().toString().trim();

        if (username.isEmpty()){
            namestr.setError("Full name is required");
            namestr.requestFocus();
            return;
        }

        if (email.isEmpty()){
            emailstr.setError("Email is required!");
            emailstr.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            emailstr.setError("Please provide valid email");
            emailstr.requestFocus();
            return;
        }

        if (password.isEmpty()){
            passstr.setError("Password is required!");
            passstr.requestFocus();
            return;
        }

        if (confirmPassword.isEmpty()){
            passConfirm.setError("Confirm password is required!");
            passConfirm.requestFocus();
            return;
        }

        if (password.length()<6 || confirmPassword.length()<6){
            passstr.setError("Min password length should be 6 characters");
            passstr.requestFocus();
            return;
        }

        if (!password.equals(confirmPassword)){
            passConfirm.setError("Password and Confirm Password should be same!");
            passConfirm.requestFocus();
            return;
        }

        mProgressBar.setVisibility(View.VISIBLE);

        mFirebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()){
                            User user = new User(username);
                            String id  = FirebaseAuth.getInstance().getCurrentUser().getUid();
                            String databasePath = String.format("User/%1$s/Record", id);
                            dbCollectionRef = db.collection(databasePath);

                            dbCollectionRef.add(user).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                @Override
                                public void onSuccess(DocumentReference documentReference) {
                                    Toast.makeText(RegisterActivity.this, "Account Successfuly Registered!", Toast.LENGTH_SHORT).show();
                                    mProgressBar.setVisibility(View.GONE);
                                    Intent i = new Intent(getApplicationContext(), LoginActivity.class);
                                    startActivity(i);
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(RegisterActivity.this, "Failed to register! Try again...", Toast.LENGTH_SHORT).show();
                                    mProgressBar.setVisibility(View.GONE);
                                }
                            });
                        }
                        else{
                            Toast.makeText(RegisterActivity.this, "This email has been used! \nPlease try with other existing email...", Toast.LENGTH_SHORT).show();
                            mProgressBar.setVisibility(View.GONE);
                        }
                    }
                });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finishAffinity();
    }
}