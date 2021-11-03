package com.example.ai_bsrs;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    EditText email,pass;
    Button login;
    TextView mRegister, mForgotPassword;
    private ProgressBar mProgressBar;
    private SharedPreferences prf;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseFirestore db;
    private CollectionReference dbCollectionRef;
    private User user;
    private String username;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        prf = getSharedPreferences("login_details",MODE_PRIVATE);
        getSharedPreferences("login_details", Context.MODE_PRIVATE).edit().clear().apply();
        mFirebaseAuth =FirebaseAuth.getInstance();

        if (mFirebaseAuth.getCurrentUser() != null){
            SharedPreferences.Editor editor = prf.edit();
            userId  = mFirebaseAuth.getUid();
            editor.putString("userId", userId);
            editor.commit();
            startActivity(new Intent(getApplicationContext(), FragmentController.class));
        }

        //getSharedPreferences("login_details", Context.MODE_PRIVATE).edit().clear().apply();

        email = (EditText)findViewById(R.id.emailLogin);
        pass= (EditText) findViewById(R.id.pass);
        login= (Button) findViewById(R.id.login);
        mRegister = (TextView)findViewById(R.id.register_signIn);
        mForgotPassword = (TextView) findViewById(R.id.forgot_password);
        mProgressBar = (ProgressBar)findViewById(R.id.progressBar);
        mProgressBar.setVisibility(View.GONE);
        mRegister.setOnClickListener(this);
        login.setOnClickListener(this);
        mForgotPassword.setOnClickListener(this);
        db = FirebaseFirestore.getInstance();

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.register_signIn:
                Intent i = new Intent(this, RegisterActivity.class);
                startActivity(i);
                break;
            case R.id.login:
                loginAccount();
                break;
            case R.id.forgot_password:
                forgotPassword(view);
                break;

        }
    }
    private void loginAccount(){
        String password = pass.getText().toString().trim();
        String emailLogin = email.getText().toString().trim();

        if (emailLogin.isEmpty()){
            email.setError("Email is required!");
            email.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(emailLogin).matches()){
            email.setError("Please provide valid email");
            email.requestFocus();
            return;
        }

        if (password.isEmpty()){
            pass.setError("Password is required!");
            pass.requestFocus();
            return;
        }

        if (password.length()<6){
            pass.setError("Min password length should be 6 characters");
            pass.requestFocus();
            return;
        }

        mProgressBar.setVisibility(View.VISIBLE);

        mFirebaseAuth.signInWithEmailAndPassword(emailLogin,password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()){
                            SharedPreferences.Editor editor = prf.edit();
                            userId  = mFirebaseAuth.getUid();
                            String databasePath = String.format("User/%1$s/Record", userId);
                            dbCollectionRef = db.collection(databasePath);
                            editor.putString("userId", userId);
                            editor.commit();
                            loadDatabaseRetrieveUsername();
                            getSharedPreferences("result_details", MODE_PRIVATE).edit().clear().apply();
                            startActivity(new Intent(getApplicationContext(), FragmentController.class));
                        }
                        else{
                            Toast.makeText(LoginActivity.this, "Error! " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            mProgressBar.setVisibility(View.GONE);
                        }
                    }
                });
    }

    private void loadDatabaseRetrieveUsername() {

        user = new User();
        if (dbCollectionRef.get()!=null){

            dbCollectionRef.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                @Override
                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                    for (QueryDocumentSnapshot documentSnapshot:queryDocumentSnapshots){
                        user = documentSnapshot.toObject(User.class);
                        username = user.getUsername();
                        Toast.makeText(LoginActivity.this, "Welcome "+  username, Toast.LENGTH_SHORT).show();
                    }
                }
        });
        }
    }

    private void forgotPassword(View view) {
        EditText resetMail = new EditText(view.getContext());
        AlertDialog.Builder passwordResetDialog = new AlertDialog.Builder(view.getContext(), R.style.CustomAlertDialog);

        passwordResetDialog.setTitle("Reset Password?");
        passwordResetDialog.setMessage("Enter your E-mail to receive reset password link");
        passwordResetDialog.setView(resetMail);
        passwordResetDialog.setCancelable(false);

        passwordResetDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //extract the email and send the link
                String mail = resetMail.getText().toString().trim();
                if (mail.isEmpty()){
                    Toast.makeText(LoginActivity.this, "Email is required!", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!Patterns.EMAIL_ADDRESS.matcher(mail).matches()){
                    Toast.makeText(LoginActivity.this, "Please provide valid email", Toast.LENGTH_SHORT).show();
                    return;
                }else{
                    mFirebaseAuth.sendPasswordResetEmail(mail).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(LoginActivity.this, "Reset Link Sent To Your Email", Toast.LENGTH_SHORT).show();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(LoginActivity.this, "Error! Reset Link is not sent" + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });

        passwordResetDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        AlertDialog alertDialog = passwordResetDialog.create();
        alertDialog.show();
        Button btnNegative = alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE);
        btnNegative.setTextColor(Color.RED);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finishAffinity();
    }
}