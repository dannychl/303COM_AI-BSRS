package com.example.ai_bsrs.notification_module;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.format.DateFormat;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.ai_bsrs.FragmentController;
import com.example.ai_bsrs.R;
import com.example.ai_bsrs.homepage_module.ScanPage;
import com.example.ai_bsrs.homepage_module.ViewInferenceResult;
import com.example.ai_bsrs.ui.NotificationsFragment;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Scanner;

public class CreateNotificationPage extends AppCompatActivity implements View.OnClickListener {

    private EditText mEtBreadName;
    private ImageView mIvBreadIn, mIvBreadKeep, mIvBreadOut, mIvTime, mIvCreate, mIvComplete;
    private String dateIn, dateKeep, dateOut, timeKeep, alarmDateKeep, alarmTimeKeep, idNotification, notes;
    private TextView mTvBreadIn, mTvBreadKeep, mTvBreadOut, mTvTimeNotify, mTvSound;
    private int yearIn, monthIn, dayIn, idPressed;
    private MediaPlayer mMediaPlayer;
    private boolean soundPressed = true, checkCreateNew;
    private ArrayList<Notification> totalNotifications;
    private NotificationAdapter mNotificationAdapter;
    private String weekOfDay = "", id = "", alarmId = "";
    private static int alarmIdQty = 0;
    private SharedPreferences prf, notification, prfLogin;
    private AlarmManager alarmManager;
    private ArrayList<Notification> mNotificationArrayList;
    private Notification mNotification;
    private String prfBreadName, prfDateKeep, prfDateIn, prfDateOut, prfTimeKeep, prfId, prfNotes;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String userId;
    private String databasePath;
    private CollectionReference dbCollectionRef;
    private int cancelAlarmCounter;
    private TextInputEditText mEtNotes;
    int size;

    private ArrayList<String> intentChecked;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_notification_page);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        intentChecked = new ArrayList<String>();
        idNotification = getIntent().getStringExtra("idNotification");      // will only getIntent value from Notification Helper class call
        prf = getSharedPreferences("notification_details", MODE_PRIVATE);
        notification = getSharedPreferences("notification_alarm", MODE_PRIVATE);
        prfBreadName = prf.getString("breadName", "");
        prfDateIn = prf.getString("dateIn", "");
        prfDateKeep = prf.getString("dateKeep", "");
        prfDateOut = prf.getString("dateOut", "");
        prfTimeKeep = prf.getString("timeKeep", "");
        prfNotes = prf.getString("notes", "");
        prfId = prf.getString("id", "");
        prfLogin = getSharedPreferences("login_details",MODE_PRIVATE);


        userId = prfLogin.getString("userId", "");
        databasePath = String.format("Notification/%1$s/Record", userId);
        dbCollectionRef = db.collection(databasePath);

        checkCreateNew = getIntent().getBooleanExtra("checkCreateNew", false);

        /*if (alarmManager != null){
            String intentCheck = notification.getString("notificationIntent", "");
            if (!intentCheck.isEmpty()) {
                Gson gson = new Gson();
                Type type = new TypeToken<ArrayList<String>>() {
                }.getType();
                intentChecked = gson.fromJson(intentCheck, type);
            }
            for (int i = 0; i<intentChecked.size(); i++){
                Toast.makeText(this, intentChecked.get(i), Toast.LENGTH_SHORT).show();
                intentChecked.remove(i);
                //cancelAlarm();

                Gson gson = new Gson();
                String jsonArray = gson.toJson(intentChecked);
                SharedPreferences.Editor editor = notification.edit();
                editor.putString("notificationIntent", jsonArray);
                editor.commit();

                if (intentChecked.size()<1){
                    getSharedPreferences("notification_alarm", Context.MODE_PRIVATE).edit().clear().apply();
                }
                break;
            }
        }*/

            mEtBreadName = (EditText) findViewById(R.id.etBreadName);
            mTvBreadIn = (TextView) findViewById(R.id.tvBreadIn);
            mTvBreadKeep = (TextView) findViewById(R.id.tvBreadKeep);
            mTvBreadOut = (TextView) findViewById(R.id.tvBreadOut);
            mTvTimeNotify = (TextView) findViewById(R.id.tvTimeNotify);
            mIvBreadIn = (ImageView) findViewById(R.id.ivBreadInNext);
            mIvBreadKeep = (ImageView) findViewById(R.id.ivBreadKeepNext);
            mIvBreadOut = (ImageView) findViewById(R.id.ivBreadOutNext);
            mIvTime = (ImageView) findViewById(R.id.ivTimeNext);
            mIvCreate = (ImageView) findViewById(R.id.ivCreate);
            mIvComplete = (ImageView) findViewById(R.id.ivComplete);
            mEtNotes = (TextInputEditText) findViewById(R.id.editNotes);


            // Only enter this if statement when Notification Helper call this activity page with along with intent.extra
        if (idNotification != null && !idNotification.isEmpty()){   // idNotification only have value when Notification helper call the intent with value

            mNotificationArrayList = new ArrayList<Notification>();
            mNotification = new Notification();

            if (dbCollectionRef.get() != null) {
                dbCollectionRef.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                            mNotification = documentSnapshot.toObject(Notification.class);
                            mNotificationArrayList.add(new Notification(documentSnapshot.getId(), mNotification.getBreadName(), mNotification.getDateIn(), mNotification.getDateKeep(),
                                    mNotification.getDateOut(), mNotification.getTimeKeep(), mNotification.getNotes(), System.currentTimeMillis()));
                        }

                        for (int i = 0; i<mNotificationArrayList.size();i++){

                            if (idNotification.equals(mNotificationArrayList.get(i).getNotificationId())){
                                mEtBreadName.setText(mNotificationArrayList.get(i).getBreadName());
                                mTvBreadIn.setText(mNotificationArrayList.get(i).getDateIn());
                                mTvBreadKeep.setText(mNotificationArrayList.get(i).getDateKeep());
                                mTvBreadOut.setText(mNotificationArrayList.get(i).getDateOut());
                                mTvTimeNotify.setText(mNotificationArrayList.get(i).getTimeKeep());
                                mEtNotes.setText(mNotificationArrayList.get(i).getNotes());

                                mIvBreadIn.setVisibility(View.GONE);
                                mIvBreadKeep.setVisibility(View.GONE);
                                mIvBreadOut.setVisibility(View.GONE);
                                mIvTime.setVisibility(View.GONE);
                                mIvComplete.setVisibility(View.VISIBLE);
                            }
                        }
                        if (mEtBreadName.getText().toString().isEmpty() && mTvBreadKeep.getText().toString().isEmpty()){
                            Toast.makeText(CreateNotificationPage.this, "Previous notification might already deleted... Will close in 3 seconds...", Toast.LENGTH_SHORT).show();
                            Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                public void run() {
                                    finish();
                                }
                            }, 3000);
                        }
                    }
                });
            }
        }

            if (checkCreateNew) {
                mIvCreate.setBackgroundResource(R.drawable.donecreate);             // display done button at bottom when user create new notification
            } else if (!prfBreadName.isEmpty() && !prfDateIn.isEmpty() && !prfDateKeep.isEmpty() && !prfDateOut.isEmpty()
                    && !prfTimeKeep.isEmpty() && !prfId.isEmpty()) {
                mEtBreadName.setText(prfBreadName);
                mTvBreadIn.setText(prfDateIn);
                mTvBreadKeep.setText(prfDateKeep);
                mTvBreadOut.setText(prfDateOut);
                mTvTimeNotify.setText(prfTimeKeep);
                mEtNotes.setText(prfNotes);
                mIvCreate.setBackgroundResource(R.drawable.updatenotification);    // display update button at bottom when user click on existing notification
            }

            //mIvBreadIn.setOnClickListener(this);
            mIvBreadKeep.setOnClickListener(this);
            mIvBreadOut.setOnClickListener(this);
            mIvTime.setOnClickListener(this);
            mIvCreate.setOnClickListener(this);
            mIvComplete.setOnClickListener(this);

            final Calendar c = Calendar.getInstance();
            Date date = c.getTime();
            yearIn = c.get(Calendar.YEAR);
            monthIn = c.get(Calendar.MONTH);
            dayIn = c.get(Calendar.DAY_OF_MONTH);
            //weekDayIn = new SimpleDateFormat("EEEE", Locale.ENGLISH).format(date.getTime());

            dateIn = dayIn + " / " + (monthIn + 1) + " / " + yearIn;
            mTvBreadIn.setText("(" + new SimpleDateFormat("EE", Locale.ENGLISH).format(date.getTime()) + ") " + dayIn + " / " + (monthIn + 1) + " / " + yearIn);

        }
        private DatePickerDialog.OnDateSetListener dateListener = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {

                boolean checkValidate;

                try {
                    String input_date = dayOfMonth + "/ " + (monthOfYear + 1) + "/ " + year;
                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
                    Date dateChoose = dateFormat.parse(input_date);
                    SimpleDateFormat format2 = new SimpleDateFormat("EE");
                    weekOfDay = format2.format(dateChoose.getTime());
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                alarmDateKeep = dayOfMonth + "/" + (monthOfYear + 1) + "/" + year;
                String datePicked = "(" + weekOfDay + ") " + dayOfMonth + " / " + (monthOfYear + 1) + " / " + year;


                if (idPressed == R.id.ivBreadKeepNext) {
                    checkValidate = setDatePicked(dayOfMonth, monthOfYear, year, idPressed);
                    if (checkValidate) {
                        dateKeep = dayOfMonth + " / " + (monthOfYear + 1) + " / " + year;
                        mTvBreadKeep.setText(datePicked);
                        Snackbar snackbar = Snackbar
                                .make(findViewById(idPressed), "Bread Keep Notification Date Set", Snackbar.LENGTH_LONG);
                        snackbar.show();
                    } else {
                        mTvBreadKeep.setText("");
                    }
                }
                if (idPressed == R.id.ivBreadOutNext) {
                    setDatePicked(dayOfMonth, monthOfYear, year, idPressed);
                    checkValidate = setDatePicked(dayOfMonth, monthOfYear, year, idPressed);
                    if (checkValidate) {
                        dateOut = dayOfMonth + " / " + (monthOfYear + 1) + " / " + year;
                        mTvBreadOut.setText(datePicked);
                        Snackbar snackbar = Snackbar
                                .make(findViewById(idPressed), "Bread Out Notification Date Set", Snackbar.LENGTH_LONG);
                        snackbar.show();
                    } else {
                        mTvBreadOut.setText("");
                    }
                }


            }
        };

        private TimePickerDialog.OnTimeSetListener timePickerListener1 = new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                String amPm = "";
                if (hourOfDay >= 12) {
                    amPm = " PM";
                    if (hourOfDay >12){
                        hourOfDay -= 12;
                    }
                } else
                    amPm = " AM";

                alarmTimeKeep = (String.format("%02d:%02d", hourOfDay, minute));

                timeKeep = (String.format("%02d:%02d", hourOfDay, minute) + amPm);

                String timeString = "", h = "", m = "";
                Calendar calendar = Calendar.getInstance();

                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
                timeString = sdf.format(calendar.getTime());
                h = timeString.substring(0, 2);
                m = timeString.substring(3, 5);

                mTvTimeNotify.setText(timeKeep);
            }
        };

        private boolean setDatePicked ( int dayOfMonth, int monthOfYear, int year, int idPressed){
            String datePicked = dayOfMonth + " / " + (monthOfYear + 1) + " / " + year;
            String dateString = "";
            Calendar calendar = Calendar.getInstance();

            SimpleDateFormat sdf = new SimpleDateFormat("dd / MM / yyyy");
            dateString = sdf.format(calendar.getTime());
            Date check = null;
            Date strDate = null;

            try {
                strDate = sdf.parse(datePicked);
                check = sdf.parse(dateString);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            if (check.compareTo(strDate) > 0) {
                Snackbar snackbar = Snackbar
                        .make(findViewById(idPressed), "Notification cannot create with past date!", Snackbar.LENGTH_LONG);
                snackbar.show();
                return false;
            }

            return true;
        }

        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        @Override
        public void onClick (View view){
            switch (view.getId()) {
                case R.id.ivBreadKeepNext: {
                    idPressed = R.id.ivBreadKeepNext;
                    DatePickerDialog dateDialog = new DatePickerDialog(this, dateListener, yearIn, monthIn, dayIn);
                    dateDialog.show();
                }
                break;
                case R.id.ivBreadOutNext: {
                    idPressed = R.id.ivBreadOutNext;
                    DatePickerDialog dateDialog = new DatePickerDialog(this, dateListener, yearIn, monthIn, dayIn);
                    dateDialog.show();

                }
                break;
                case R.id.ivNote: {

                    break;
                }
                case R.id.ivTimeNext: {

                    final Calendar calendar = Calendar.getInstance();
                    int hour = calendar.get(Calendar.HOUR_OF_DAY);
                    int minute = calendar.get(Calendar.MINUTE);

                    TimePickerDialog timePickerDialog = new TimePickerDialog(this, timePickerListener1, hour, minute,
                            DateFormat.is24HourFormat(this));

                    timePickerDialog.show();
                }
                break;

                // enter here when user create a new notification nor update an existing notification
                case R.id.ivCreate: {

                    String name = mEtBreadName.getText().toString().trim();
                    String dateOut = mTvBreadOut.getText().toString();
                    String dateKeep = mTvBreadKeep.getText().toString();
                    String time = mTvTimeNotify.getText().toString();

                    if (name.isEmpty()) {
                        Snackbar snackbar = Snackbar
                                .make(findViewById(R.id.ivCreate), "Bread Name cannot be empty", Snackbar.LENGTH_LONG);
                        snackbar.show();
                    } else if (dateKeep.isEmpty() || dateOut.isEmpty()) {
                        Snackbar snackbar = Snackbar
                                .make(findViewById(R.id.ivCreate), "Bread Keep and Bread Out Date cannot be empty", Snackbar.LENGTH_LONG);
                        snackbar.show();
                    } else if (time.isEmpty()){
                        Snackbar snackbar = Snackbar
                                .make(findViewById(R.id.ivCreate), "Time Keep cannot be empty", Snackbar.LENGTH_LONG);
                        snackbar.show();
                    }
                    else {
                        String dateValidate = dateKeep.substring(6);
                        StringBuilder dateValidateBuilder = new StringBuilder();
                        Scanner scanner = new Scanner(dateValidate);
                        while (scanner.hasNext()) {
                            dateValidateBuilder.append(scanner.next());
                        }
                        String timeValidate = time.substring(0, 5);
                        String amPm = time.substring(6);

                        //----
                        String arrayDate [] = String.valueOf(dateValidateBuilder).split("/");
                        String day = arrayDate[0];
                        String month = arrayDate[1];
                        String year = arrayDate[2];

                        String arrayTime[] = timeValidate.split(":");
                        String hour = arrayTime[0];
                        String minute = arrayTime[1];

                        if (amPm.equals("PM")){
                            switch (hour){
                                case "01":
                                case "02":
                                case "03":
                                case "04":
                                case "05":
                                case "06":
                                case "07":
                                case "08":
                                case "09":
                                case "10":
                                case "11":
                                    int hours = Integer.parseInt(hour)+12;
                                    hour = String.valueOf(hours);
                            }
                        }

                        Calendar calendar = Calendar.getInstance();
                        Calendar calendarNow = Calendar.getInstance();

                        calendar.set(Calendar.DAY_OF_MONTH, Integer.parseInt(day));
                        calendar.set(Calendar.MONTH, Integer.parseInt(month)-1);
                        calendar.set(Calendar.YEAR, Integer.parseInt(year));
                        calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(hour));
                        calendar.set(Calendar.MINUTE, Integer.parseInt(minute));
                        calendar.set(Calendar.SECOND, 0);

                        if (calendar.compareTo(calendarNow) <=0){
                            Snackbar snackbar = Snackbar
                                    .make(findViewById(R.id.ivCreate), "Pass date or time cannot be set! \nPlease Try Again", Snackbar.LENGTH_LONG);
                            snackbar.show();
                        }else {
                            if (checkCreateNew) {
                                if (mEtNotes.getText().toString().trim().isEmpty())
                                    mEtNotes.setError("Notes field can't be empty");
                                else {
                                    notes = mEtNotes.getText().toString().trim();

                                    totalNotifications = new ArrayList<Notification>();
                                    totalNotifications.add(new Notification(id, name, dateIn, dateKeep, dateOut, time, notes, System.currentTimeMillis()));
                                    loadDatabase(calendar, name, time);

                                    if (isNetworkAvailable()){
                                        dbCollectionRef.add(new Notification(id, name, dateIn, dateKeep, dateOut, timeKeep, notes, System.currentTimeMillis())).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                            @Override
                                            public void onSuccess(DocumentReference documentReference) {
                                                Toast.makeText(CreateNotificationPage.this, "Notification created successfully", Toast.LENGTH_SHORT).show();
                                                Intent intent = new Intent(CreateNotificationPage.this, FragmentController.class);
                                                intent.putExtra("CallingActivity", NotificationsFragment.ACTIVITY_2);
                                                startActivity(intent);
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Toast.makeText(CreateNotificationPage.this, "Error Create Data! " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    }else{
                                        Toast.makeText(this, "Please connect to internet for better user experience...", Toast.LENGTH_SHORT).show();
                                    }

                                }
                            } else {

                                if (mEtNotes.getText().toString().trim().isEmpty())
                                    mEtNotes.setError("Notes field can't be empty");
                                else {
                                    notes = mEtNotes.getText().toString().trim();

                                    totalNotifications = new ArrayList<Notification>();
                                    //totalNotifications.add(new Notification(id, name, dateIn, dateKeep, dateOut, timeKeep));
                                    loadDatabase(calendar, name, time);

                                    if (isNetworkAvailable()){
                                        dbCollectionRef.document(prfId).set(new Notification(prfId, name, dateIn, dateKeep, dateOut, time, notes, System.currentTimeMillis())).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Toast.makeText(CreateNotificationPage.this, "Notification updated sucessfully", Toast.LENGTH_SHORT).show();
                                                getSharedPreferences("notification_details", Context.MODE_PRIVATE).edit().clear().apply();
                                                Intent intent = new Intent(CreateNotificationPage.this, FragmentController.class);
                                                intent.putExtra("CallingActivity", NotificationsFragment.ACTIVITY_2);
                                                startActivity(intent);
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Toast.makeText(CreateNotificationPage.this, "Error Update Data! " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    }
                                    else{
                                        Toast.makeText(this, "Please connect to internet for better user experience...", Toast.LENGTH_SHORT).show();
                                    }

                                }
                            }
                        }
                    }
                }
                break;
                // enter here when notification alarm is ring and user click the notification bar to make this button visible to user
                case R.id.ivComplete:{
                    dbCollectionRef.document(String.valueOf(idNotification)).delete();
                    Toast.makeText(this, "Notification for Bread Keep Completed...", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(CreateNotificationPage.this, FragmentController.class);
                    intent.putExtra("CallingActivity", NotificationsFragment.ACTIVITY_2);
                    startActivity(intent);
                }
            }
        }

    private void loadDatabase(Calendar calendar, String breadName, String timeKeep) {

        mNotificationArrayList = new ArrayList<Notification>();
        mNotification = new Notification();

        if (dbCollectionRef.get() != null) {
            dbCollectionRef.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                @Override
                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                    for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        mNotification = documentSnapshot.toObject(Notification.class);

                        mNotificationArrayList.add(new Notification(documentSnapshot.getId(), mNotification.getBreadName(),
                                mNotification.getDateIn(), mNotification.getDateKeep(), mNotification.getDateOut(),
                                mNotification.getTimeKeep(), mNotification.getNotes(), System.currentTimeMillis()));
                    }
                    for (int i = 0; i<mNotificationArrayList.size();i++){

                        if (breadName.equals(mNotificationArrayList.get(i).getBreadName()) && timeKeep.equals(mNotificationArrayList.get(i).getTimeKeep())){
                            alarmId = mNotificationArrayList.get(i).getNotificationId();
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                                setAlarm(calendar,  alarmId);
                            }
                        }
                    }
                }
            });
        }
    }


    private void setAlarm (Calendar calendar, String idSetAlarm){

        //int counter = Integer.parseInt(prf.getString("notificationIntent", "0"));
        String intentCheck = notification.getString("notificationIntent", "");

        // Check whether the intentCheck is empty, if not empty fetch the data with shared preference
        // if intentCheck is empty a new array list intent check will be created
        if (!intentCheck.isEmpty()) {
            Gson gson = new Gson();
            Type type = new TypeToken<ArrayList<String>>() {
            }.getType();
            intentChecked = gson.fromJson(intentCheck, type);
        }

        // 1. get the array list and check whether there are any string inside
        // 2. if there is no thing inside the counter by default will be 0, if alarmset called,
        //      the counter will be 0 and close the alarm manager with 0
        // 3. if alarm manager with 0 has been set and not yet cancel, intentCheck.size will become 1
        //      and counter will become 1 as well
        int counter = 1;

        for (int i = 0; i<intentChecked.size(); i++){
            counter++;
        }

        intentChecked.add(String.valueOf(counter));

        Gson gson = new Gson();
        String jsonArray = gson.toJson(intentChecked);
        SharedPreferences.Editor editor = notification.edit();
        editor.putString("notificationIntent", jsonArray);
        editor.commit();

       /* String intent1 = notification.getString("notificationIntent", "");
        if (!intent1.isEmpty()) {
            Gson gsons = new Gson();
            Type type = new TypeToken<ArrayList<String>>() {
            }.getType();
            intentChecked = gsons.fromJson(intent1, type);
        }*/

        alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(getApplicationContext(), AlarmBootUpReceiver.class);
        intent.putExtra("notificationAlarmId", idSetAlarm);         // the notificationId created in Firebase

        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, counter, intent, PendingIntent.FLAG_ONE_SHOT);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
        }
        else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            AlarmManager.AlarmClockInfo alarmClockInfo = new AlarmManager.AlarmClockInfo(calendar.getTimeInMillis(), pendingIntent);
            alarmManager.setAlarmClock(alarmClockInfo, pendingIntent);
        }
        else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
        }else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent (CreateNotificationPage.this, FragmentController.class);
        intent.putExtra("CallingActivity", NotificationsFragment.ACTIVITY_2);
        startActivity(intent);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}