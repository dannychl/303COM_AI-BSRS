package com.example.ai_bsrs;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.ai_bsrs.ui.TransactionFragment;
import com.example.ai_bsrs.ui.HomeFragment;
import com.example.ai_bsrs.ui.NotificationsFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

public class FragmentController extends AppCompatActivity implements View.OnClickListener {

    private HomeFragment mHomeFragment;
    private NotificationsFragment mNotificationsFragment;
    private TransactionFragment mTransactionFragment;
    private TextView mTvLogout;
    private ImageView mIvLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_controller);

        mHomeFragment = new HomeFragment();
        mNotificationsFragment = new NotificationsFragment();
        mTransactionFragment = new TransactionFragment();


        BottomNavigationView btmView = findViewById(R.id.bottom_navigation);
        btmView.setOnNavigationItemSelectedListener(navListener);

        int callingActivity = getIntent().getIntExtra("CallingActivity", 0);

        if (callingActivity == NotificationsFragment.ACTIVITY_2){
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, mNotificationsFragment).commit();
            btmView.getMenu().findItem(R.id.navigation_notifications).setChecked(true);
        }
        else if (callingActivity == TransactionFragment.ACTIVITY_3){
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, mTransactionFragment).commit();
            btmView.getMenu().findItem(R.id.navigation_account).setChecked(true);
        }
        else
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, mHomeFragment).commit();
    }

    private BottomNavigationView.OnNavigationItemSelectedListener navListener= new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {

            switch (item.getItemId()) {
                case R.id.navigation_home:
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, mHomeFragment).commit();
                    break;
                case R.id.navigation_notifications:
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, mNotificationsFragment).commit();
                    break;
                case R.id.navigation_account:
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, mTransactionFragment).commit();
                    break;
            }
            return true;
        }
    };

    public void logout(View view){
        FirebaseAuth.getInstance().signOut();
        startActivity(new Intent(getApplicationContext(), LoginActivity.class));
    }

    @Override
    public void onClick(View view) {

        switch(view.getId()){
            case R.id.tvLogout:
            case R.id.ivLogout:
                logout(view);
        }
    }

    public void onBackPressed() {

        LayoutInflater layoutInflater = getLayoutInflater();
        View view = layoutInflater.inflate(R.layout.dialog_log_out, null);

        mIvLogout = view.findViewById(R.id.ivLogout);
        mTvLogout = view.findViewById(R.id.tvLogout);

        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.CustomAlertDialog);
        builder.setCancelable(false);

        mIvLogout.setOnClickListener(this);
        mTvLogout.setOnClickListener(this);

        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                finishAffinity();
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.setView(view);
        alertDialog.show();
        Button btnNegative = alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE);
        btnNegative.setTextColor(Color.RED);
    }
}