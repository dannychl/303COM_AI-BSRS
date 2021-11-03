package com.example.ai_bsrs.ui;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ai_bsrs.notification_module.CreateNotificationPage;
import com.example.ai_bsrs.notification_module.Notification;
import com.example.ai_bsrs.notification_module.NotificationAdapter;
import com.example.ai_bsrs.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

import static android.content.Context.MODE_PRIVATE;

public class NotificationsFragment extends Fragment{

    private ImageView mIvHomeNotificationBg, mBookingHeader, mIvCreateNewNotifications;
    public static final int ACTIVITY_2 = 1002;
    private RecyclerView rV;
    private ArrayList<Notification> mNotificationArrayList;
    private NotificationAdapter mNotificationAdapter;
    private Notification mNotification;
    private TextView mTvImgMessage, mTvNotificationList;
    private SharedPreferences prfLogin, prf;


    private FirebaseFirestore db;

    private String id;
    private String databasePath;
    private CollectionReference dbCollectionRef;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_notifications, container, false);
        ((AppCompatActivity) getActivity()).getSupportActionBar().hide();
        getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getContext();
        mBookingHeader = (ImageView) view.findViewById(R.id.bookingHeader);
        mIvHomeNotificationBg = (ImageView) view.findViewById(R.id.ivHomeNotificationBg);
        mIvCreateNewNotifications = (ImageView) view.findViewById(R.id.createNewNotification);
        mTvImgMessage = (TextView) view.findViewById(R.id.tvImgMessage);
        mTvNotificationList = (TextView)view.findViewById(R.id.tvNotificationList);

        mTvImgMessage.setVisibility(View.GONE);
        prf = getActivity().getSharedPreferences("notification_details",MODE_PRIVATE);
        prfLogin = getActivity().getSharedPreferences("login_details",MODE_PRIVATE);

        id = prfLogin.getString("userId", "");
        databasePath = String.format("Notification/%1$s/Record", id);
        db = FirebaseFirestore.getInstance();
        dbCollectionRef = db.collection(databasePath);

        mNotificationArrayList = new ArrayList<Notification>();
        mNotification = new Notification();

        initRecylerView(view);

        mIvCreateNewNotifications.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getActivity(), CreateNotificationPage.class);
                i.putExtra("checkCreateNew", true);
                startActivity(i);
            }
        });
        return view;
    }

    //Toast.makeText(getActivity(), mNotificationArrayList.get(position).getBreadName(), Toast.LENGTH_SHORT).show();
    ItemTouchHelper.SimpleCallback itemTouchHelperCallBack = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.CustomAlertDialog);
            StringBuilder strBuilder = new StringBuilder();

            builder.setCancelable(false);
            builder.setTitle("Delete this Notification?");


            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    removeItem(viewHolder.getAbsoluteAdapterPosition(), mNotificationArrayList);
                }
            });

            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    setView();
                    Toast.makeText(getActivity(), "Undo notification delete...", Toast.LENGTH_SHORT).show();
                }
            });

            AlertDialog alertDialog = builder.create();
            alertDialog.show();
            Button btnNegative = alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE);
            btnNegative.setTextColor(Color.RED);
        }
    };

        private void loadDataFromFirebase() {
            if (dbCollectionRef.get() != null) {
                dbCollectionRef.orderBy("notificationTimeStamp", Query.Direction.DESCENDING).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                            mNotification = documentSnapshot.toObject(Notification.class);

                            mNotificationArrayList.add(new Notification(documentSnapshot.getId(), mNotification.getBreadName(),
                                    mNotification.getDateIn(), mNotification.getDateKeep(), mNotification.getDateOut(),
                                    mNotification.getTimeKeep(), mNotification.getNotes(), mNotification.getNotificationTimeStamp()));
                        }
                        setView();
                    }
                });
            } else {
                mTvImgMessage.setVisibility(View.VISIBLE);
            }
        }

        private void removeItem(int position, ArrayList<Notification> mNotificationArrayList) {
            if (position < 0 || position >= mNotificationArrayList.size()) {
                return;
            }
            dbCollectionRef.document(mNotificationArrayList.get(position).getNotificationId()).delete();
            Snackbar snackbar = Snackbar
                    .make(rV, "Notification removed successfully", Snackbar.LENGTH_LONG);
            snackbar.show();

            mNotificationArrayList.remove(position);
            mNotificationAdapter.notifyItemRemoved(position);
            mNotificationAdapter.notifyDataSetChanged();
            if (mNotificationArrayList.size()<1){
                mTvImgMessage.setVisibility(View.VISIBLE);
                mTvNotificationList.setVisibility(View.GONE);
            }
            else{
                mTvImgMessage.setVisibility(View.GONE);
                mTvNotificationList.setVisibility(View.VISIBLE);
            }
        }

        private void initRecylerView(View view) {
            rV = view.findViewById(R.id.notificationRecyclerView);
            rV.setLayoutManager(new LinearLayoutManager(getActivity()));
            loadDataFromFirebase();
        }

    private void setView(){
            new ItemTouchHelper(itemTouchHelperCallBack).attachToRecyclerView(rV);
            mNotificationAdapter = new NotificationAdapter(getActivity(), mNotificationArrayList);
            rV.setHasFixedSize(true);
            rV.setAdapter(mNotificationAdapter);
            mNotificationAdapter.notifyDataSetChanged();

            mNotificationAdapter.setOnItemClickListener(new NotificationAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(int position, String name, String date, String time) {
                    SharedPreferences.Editor editor = prf.edit();
                    editor.putString("breadName", mNotificationArrayList.get(position).getBreadName());
                    editor.putString("dateIn", mNotificationArrayList.get(position).getDateIn());
                    editor.putString("dateKeep", mNotificationArrayList.get(position).getDateKeep());
                    editor.putString("dateOut", mNotificationArrayList.get(position).getDateOut());
                    editor.putString("timeKeep", mNotificationArrayList.get(position).getTimeKeep());
                    editor.putString("id", mNotificationArrayList.get(position).getNotificationId());
                    editor.putString("notes", mNotificationArrayList.get(position).getNotes());
                    editor.commit();
                    Intent intent = new Intent (getActivity(), CreateNotificationPage.class);
                    startActivity(intent);
                }
            });

            if (mNotificationArrayList.size()<1){
                mTvImgMessage.setVisibility(View.VISIBLE);
                mTvNotificationList.setVisibility(View.GONE);
            }
            else{
                mTvImgMessage.setVisibility(View.GONE);
                mTvNotificationList.setVisibility(View.VISIBLE);
            }
        }
}