package com.example.ai_bsrs.ui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ai_bsrs.R;
import com.example.ai_bsrs.transaction_module.Transaction;
import com.example.ai_bsrs.transaction_module.TransactionAdapter;
import com.example.ai_bsrs.transaction_module.TransactionDetailsPage;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;

public class TransactionFragment extends Fragment {

    public static final int ACTIVITY_3 = 1003;
    private ImageView mBookingHeader;
    private TextView mTvImgMessage;
    private RecyclerView rV;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private ArrayList<Transaction> mTransactionArrayList, mTransactionDetailsList;
    private TransactionAdapter mTransactionAdapter;
    private Transaction mTransaction;
    private SharedPreferences prf, prfLogin;

    private String userId;
    private String databasePath;
    private CollectionReference dbCollectionRef;
    private List<String> mBreadName, mBreadQty, mBreadPrice;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_transaction, container, false);
        ((AppCompatActivity) getActivity()).getSupportActionBar().hide();
        getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getContext();



        prf = getActivity().getSharedPreferences("transaction_details",MODE_PRIVATE);
        mTransactionArrayList = new ArrayList<Transaction>();
        mTransaction = new Transaction();
        mBookingHeader = (ImageView) view.findViewById(R.id.bookingHeader);
        mTvImgMessage = (TextView) view.findViewById(R.id.tvImgMessage);


        prfLogin = getActivity().getSharedPreferences("login_details", MODE_PRIVATE);
        userId = prfLogin.getString("userId", "");
        databasePath = String.format("TransactionRecord/%1$s/Record", userId);
        dbCollectionRef = db.collection(databasePath);

        mTvImgMessage.setVisibility(View.GONE);

        rV = view.findViewById(R.id.transactionRecyclerView);
        rV.setLayoutManager(new LinearLayoutManager(getActivity()));
        rV.setHasFixedSize(true);
        rV.setAdapter(mTransactionAdapter);
        loadDataFromFirebase();

        return view;
    }

    private void loadDataFromFirebase() {

        if (dbCollectionRef.get() != null) {

            dbCollectionRef.orderBy("KEY_TIMESTAMP", Query.Direction.DESCENDING)
                    .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                @Override
                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                    for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {


                        mTransaction = documentSnapshot.toObject(Transaction.class);

                        /*mBreadName = documentSnapshot.toObject(Transaction.class).getKEY_BREAD_NAME();
                        mBreadQty = documentSnapshot.toObject(Transaction.class).getKEY_BREAD_QUANTITY();
                        mBreadPrice = documentSnapshot.toObject(Transaction.class).getKEY_BREAD_PRICE();*/

                        mTransactionArrayList.add(new Transaction(
                                            documentSnapshot.getId(),
                                            mTransaction.getKEY_BREAD_NAME(),
                                            mTransaction.getKEY_BREAD_PRICE(),
                                            mTransaction.getKEY_BREAD_QUANTITY(),
                                            mTransaction.getKEY_BREAD_TOTAL_PRICE(),
                                            mTransaction.getKEY_BREAD_DATE_CHECKOUT(),
                                            mTransaction.getKEY_BREAD_TIME_CHECKOUT(),
                                            mTransaction.getKEY_IMAGE(),
                                            mTransaction.getKEY_IMAGE_RESULT(),
                                            mTransaction.getKEY_STATUS(),
                                            mTransaction.getKEY_TIMESTAMP()));
                    }
                    setView();
                }
            });
        } else {
            mTvImgMessage.setVisibility(View.VISIBLE);
        }
    }


    private void setView() {

        mTransactionAdapter = new TransactionAdapter(getActivity(), mTransactionArrayList);
        rV.setHasFixedSize(true);
        rV.setAdapter(mTransactionAdapter);
        mTransactionAdapter.notifyDataSetChanged();

        mTransactionAdapter.setOnItemClickListener(new TransactionAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {

                rV.setMotionEventSplittingEnabled(false);       //disable multitouch of the adapter view
                getActivity().getSharedPreferences("transaction_details", Context.MODE_PRIVATE).edit().clear().apply();
                SharedPreferences.Editor editor = prf.edit();

                mTransactionDetailsList = new ArrayList<Transaction>();
                mTransactionDetailsList.add(new Transaction(mTransactionArrayList.get(position).getKEY_BREAD_NAME(),
                                            mTransactionArrayList.get(position).getKEY_BREAD_PRICE(),
                                            mTransactionArrayList.get(position).getKEY_BREAD_QUANTITY()));

                Gson gson = new Gson();
                String jsonArray = gson.toJson(mTransactionDetailsList);
                editor.putString("transactionDetailsList", jsonArray);
                editor.putString("dateTransaction", mTransactionArrayList.get(position).getKEY_BREAD_DATE_CHECKOUT());
                editor.putString("timeTransaction", mTransactionArrayList.get(position).getKEY_BREAD_TIME_CHECKOUT());
                editor.putString("priceTransaction", mTransactionArrayList.get(position).getKEY_BREAD_TOTAL_PRICE());
                editor.putString("transactionId", mTransactionArrayList.get(position).getTransactionId());
                editor.putString("imageInf", mTransactionArrayList.get(position).getKEY_IMAGE());
                editor.putString("imageInfResult", mTransactionArrayList.get(position).getKEY_IMAGE_RESULT());
                editor.putString("statusTransaction", mTransactionArrayList.get(position).getKEY_STATUS());
                editor.putString("timestampTransaction", String.valueOf(mTransactionArrayList.get(position).getKEY_TIMESTAMP()));
                editor.commit();
                Intent intent = new Intent (getActivity(), TransactionDetailsPage.class);
                startActivity(intent);
            }
        });
        if (mTransactionArrayList.size()<1){
            mTvImgMessage.setVisibility(View.VISIBLE);
        }
        else{
            mTvImgMessage.setVisibility(View.GONE);
        }
    }
}