package com.example.ai_bsrs.notification_module;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.example.ai_bsrs.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class NotificationHelper extends ContextWrapper {

    public static final String channelID = "channelID";
    public static final String channelName = "Channel Name";
    private String id = "";
    private NotificationManager mManager;
    private static int alarmIdQty;
    private PendingIntent pendingIntent;
    private SharedPreferences prf;
    private ArrayList<String> intentChecked;
    private int createNotificationCounter;

    public NotificationHelper(Context base, String id) {
        super(base);
        this.id = id;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createChannel();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void createChannel() {
        NotificationChannel channel = new NotificationChannel(channelID, channelName, NotificationManager.IMPORTANCE_HIGH);
        getManager().createNotificationChannel(channel);
    }

    public NotificationManager getManager() {
        if (mManager == null) {
            mManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        }

        return mManager;
    }

    public NotificationCompat.Builder getChannelNotification() {
        prf = getSharedPreferences("notification_details", MODE_PRIVATE);
        String intentCheck = prf.getString("notificationIntent", "");

        if (!intentCheck.isEmpty()) {
            Gson gson = new Gson();
            Type type = new TypeToken<ArrayList<String>>() {
            }.getType();
            intentChecked = gson.fromJson(intentCheck, type);
        }else{
            intentChecked = new ArrayList<String>();
        }

        // 1. get the array list and check whether there are any string inside
        // 2. if there is no thing inside the counter by default will be 0, if alarmset called,
        //      the counter will be 0 and close the alarm manager with 0
        // 3. if alarm manager with 0 has been set and not yet cancel, intentCheck.size will become 1
        //      and counter will become 1 as well

        for (int i = 0; i<intentChecked.size(); i++){
            createNotificationCounter = Integer.parseInt(intentChecked.get(i));
        }

        Intent i = new Intent(getApplicationContext(), CreateNotificationPage.class);
        i.putExtra("idNotification", id);
        pendingIntent = PendingIntent.getActivity(getApplicationContext(), createNotificationCounter, i, PendingIntent.FLAG_ONE_SHOT);
        return new NotificationCompat.Builder(getApplicationContext(), channelID)
                .setSmallIcon(R.drawable.bell)
                .setContentTitle("【NOTIFICATION】: BREAD KEEP TODAY! ⚠")
                .setContentText("It's almost expired.. View bread keep now")
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent);
    }
}
