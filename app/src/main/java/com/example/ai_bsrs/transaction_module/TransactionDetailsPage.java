package com.example.ai_bsrs.transaction_module;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.ai_bsrs.FragmentController;
import com.example.ai_bsrs.R;
import com.example.ai_bsrs.homepage_module.ViewInferenceResult;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class TransactionDetailsPage extends AppCompatActivity implements View.OnTouchListener {

    private RecyclerView rV;
    private SharedPreferences prf, prfLogin;
    private String prfTransactionId, prfDateTransaction, prfTimeTransaction, prfTotalPrice, prfTransactionDetailsList, prfStatusTransaction;
    private long prfTimestampTransaction;
    private TransactionDetailsAdapter mTransactionDetailsAdapter;
    private Transaction mTransaction;
    private ArrayList<Transaction> mTransactionArrayList, mTransactionArrayListWithExactPrice;
    private ImageView mIvRestore, mIvUploadImg;
    private String quantity, priceEach, breadName, prices, imgInf, imgInfResult, userId, databasePath;
    private double price = 0.00;
    public static final int ACTIVITY_3 = 1003;
    private NestedScrollView mNestedScrollView;
    private TextInputEditText mEtReason;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference dbCollectionRef;
    private Menu item;
    private TextView mTvDateTransaction, mTvTimeTransaction, mTvTransactionID, mTvTotalPayment, mTvStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction_details_page);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mNestedScrollView = (NestedScrollView) findViewById(R.id.scrollTransaction);
        rV = (RecyclerView) findViewById(R.id.recyclerView);
        rV.setLayoutManager(new LinearLayoutManager(this));
        mTransaction = new Transaction();
        mTransactionArrayList = new ArrayList<Transaction>();
        mTransactionArrayListWithExactPrice = new ArrayList<Transaction>();
        prf = getSharedPreferences("transaction_details", MODE_PRIVATE);

        mTvTransactionID = (TextView)findViewById(R.id.tvTransactionIdAns);
        mTvDateTransaction = (TextView)findViewById(R.id.tvDateTransactionAns);
        mTvTimeTransaction = (TextView) findViewById(R.id.tvTimeTransactionAns);
        mTvTotalPayment = (TextView) findViewById(R.id.tvTotalPaymentTransactionAns);
        mTvStatus = (TextView) findViewById(R.id.tvStatusTransactionAns);
        mIvUploadImg = (ImageView) findViewById(R.id.uploadImage);

        prfTransactionDetailsList = prf.getString("transactionDetailsList", "");
        prfDateTransaction = prf.getString("dateTransaction", "");
        prfTimeTransaction = prf.getString("timeTransaction", "");
        prfTotalPrice = prf.getString("priceTransaction", "");
        prfTransactionId = prf.getString("transactionId", "");
        prfStatusTransaction = prf.getString("statusTransaction", "");
        imgInf = prf.getString("imageInf", "");
        imgInfResult = prf.getString("imageInfResult", "");
        prfTimestampTransaction = Long.parseLong(prf.getString("timestampTransaction", ""));

        prfLogin = getSharedPreferences("login_details", MODE_PRIVATE);
        userId = prfLogin.getString("userId", "");
        databasePath = String.format("TransactionRecord/%1$s/Record", userId);

        dbCollectionRef = db.collection(databasePath);

        mIvUploadImg.setOnTouchListener(TransactionDetailsPage.this);

        setTransactionDetailsPageView();
    }

    private void setTransactionDetailsPageView() {
        if (!prfTransactionDetailsList.isEmpty()){
            Gson gson = new Gson();
            Type type = new TypeToken<ArrayList<Transaction>>() {}.getType();
            mTransactionArrayList = gson.fromJson(prfTransactionDetailsList, type);

            if (!prfDateTransaction.isEmpty()
                    && !prfTimeTransaction.isEmpty() && !prfTotalPrice.isEmpty() && !prfTransactionId.isEmpty()){

                mTvDateTransaction.setText(prfDateTransaction);
                mTvTimeTransaction.setText(prfTimeTransaction);
                mTvTransactionID.setText(prfTransactionId);
                mTvTotalPayment.setText(prfTotalPrice);
                mTvStatus.setText(prfStatusTransaction);
                Glide.with(getApplicationContext()).load(Uri.parse(imgInf)).into(mIvUploadImg);         // image without bounding box
                Glide.with(getApplicationContext()).load(Uri.parse(imgInfResult)).into(mIvUploadImg);   // image with bounding box

                String [] breadList, priceList, qtyList;

                for (int x = 0; x<mTransactionArrayList.size(); x++) {
                    breadList = String.valueOf(mTransactionArrayList.get(x).getKEY_BREAD_NAME())
                            .replace("[", "")
                            .replace("]", "")
                            .split(",");
                    priceList = String.valueOf(mTransactionArrayList.get(x).getKEY_BREAD_PRICE())
                            .replace("[", "")
                            .replace("]", "")
                            .split(",");
                    qtyList = String.valueOf(mTransactionArrayList.get(x).getKEY_BREAD_QUANTITY())
                            .replace("[", "")
                            .replace("]", "")
                            .split(",");

                    for (int i = 0; i < breadList.length; i++) {

                        breadName = breadList[i];
                        priceEach = priceList[i];
                        quantity = qtyList[i];

                   /* breadName = String.valueOf(mTransactionArrayList.get(i).getKEY_BREAD_NAME()).replace("[","").replace("]","");
                    priceEach = String.valueOf(mTransactionArrayList.get(i).getKEY_BREAD_PRICE()).replace("[","").replace("]","");;
                    quantity = String.valueOf(mTransactionArrayList.get(i).getKEY_BREAD_QUANTITY()).replace("[","").replace("]","");;*/

                        // the totalPrice after calculate with totalQuantity of each bread
                        price = Double.parseDouble(priceEach.trim()) * Integer.parseInt(quantity.trim());

                        // breadName, eachPrice, totalOfEachPrice, quantity
                        // each type of bread with the name, each price, totalOfPrice base on the quantity and total quantity
                        mTransactionArrayListWithExactPrice.add(new Transaction(breadName, priceEach.trim(), String.format("%.2f", price).trim(), quantity.trim()));
                    }
                }
                mTransactionDetailsAdapter = new TransactionDetailsAdapter(TransactionDetailsPage.this, mTransactionArrayListWithExactPrice);
                rV.setHasFixedSize(true);
                rV.setAdapter(mTransactionDetailsAdapter);

            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(this, FragmentController.class);
        intent.putExtra("CallingActivity", TransactionDetailsPage.ACTIVITY_3);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.action_bar_transaction_details, menu);
        MenuItem itemDelete = menu.findItem(R.id.menu_delete);
        MenuItem itemRestore = menu.findItem(R.id.menu_retore);

        if (prfStatusTransaction.equals("Paid") || prfStatusTransaction.equals("Changes Made")) {
            itemDelete.setVisible(true);            // set delete button can be seen
            itemRestore.setVisible(true);           // set restore button can be seen
        }
        else if(prfStatusTransaction.substring(0,6).equals("Cancel")){
            itemDelete.setVisible(false);           // set delete button cannot be seen
            itemRestore.setVisible(false);          // set restore button cannot be seen
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()){

            case android.R.id.home:{
                onBackPressed();
            }
            break;

            case R.id.menu_delete:{

                LayoutInflater layoutInflater = this.getLayoutInflater();
                final View view = layoutInflater.inflate(R.layout.dialog_cancel_transaction, null);

                AlertDialog alertDialog = new AlertDialog.Builder(this, R.style.CustomAlertDialog)
                        .setPositiveButton("Submit", null)
                        .setNegativeButton("Close", null).create();

                alertDialog.setCancelable(false);

                alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface dialogInterface) {
                        Button btnDeleteTransaction = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                        Button btnClose = alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE);

                        mEtReason = (TextInputEditText) view.findViewById(R.id.editReason);

                        btnDeleteTransaction.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                            if (mEtReason.getText().toString().trim().isEmpty()){
                                mEtReason.setError("Reason field can't be empty");
                            }
                            else{
                                String reason = mEtReason.getText().toString().trim();
                                Map<String, Object> breadTransactionDetail = new HashMap<>();
                                breadTransactionDetail.put("KEY_BREAD_NAME", mTransactionArrayList.get(0).getKEY_BREAD_NAME());
                                breadTransactionDetail.put("KEY_BREAD_QUANTITY", mTransactionArrayList.get(0).getKEY_BREAD_QUANTITY());
                                breadTransactionDetail.put("KEY_BREAD_PRICE", mTransactionArrayList.get(0).getKEY_BREAD_PRICE());
                                breadTransactionDetail.put("KEY_BREAD_TOTAL_PRICE", prfTotalPrice);
                                breadTransactionDetail.put("KEY_BREAD_TIME_CHECKOUT", prfTimeTransaction);
                                breadTransactionDetail.put("KEY_BREAD_DATE_CHECKOUT", prfDateTransaction);
                                breadTransactionDetail.put("KEY_STATUS", "Cancel\n" + "(Reason: " +reason+ ")");
                                breadTransactionDetail.put("KEY_IMAGE", imgInf);
                                breadTransactionDetail.put("KEY_IMAGE_RESULT", imgInfResult);
                                breadTransactionDetail.put("KEY_TIMESTAMP", prfTimestampTransaction);      // put back the existing timestamp record as only transaction update and new transaction will put a new timestamp
                                dbCollectionRef.document(prfTransactionId).set(breadTransactionDetail).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        alertDialog.dismiss();
                                        Toast.makeText(TransactionDetailsPage.this, "Transaction Cancelled Sucessfully", Toast.LENGTH_SHORT).show();
                                        onBackPressed();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {

                                    }
                                });
                            }
                        }
                    });


                        btnClose.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Toast.makeText(TransactionDetailsPage.this, "Closing...", Toast.LENGTH_SHORT).show();
                                alertDialog.dismiss();
                            }
                        });
                    }
                });

                alertDialog.setView(view);
                alertDialog.show();
                Button btnNegative = alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE);
                btnNegative.setTextColor(Color.RED);

            }
            break;
            case R.id.menu_retore:{
                //mTransactionArrayListWithExactPrice is an ArrayList with

                SharedPreferences.Editor editor = prf.edit();
                Gson gson = new Gson();
                String jsonArray = gson.toJson(mTransactionArrayListWithExactPrice);
                editor.putString("transactionArrayList", jsonArray);
                editor.commit();
                Intent intent = new Intent (TransactionDetailsPage.this, FragmentController.class);
                intent.putExtra("CallingActivity", ViewInferenceResult.ACTIVITY_1);
                intent.putExtra("restoreValidate", true);
                startActivity(intent);
            }
        }

        return false;
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        int action = motionEvent.getAction();
        switch (action) {
            case MotionEvent.ACTION_UP:
                Glide.with(getApplicationContext()).load(Uri.parse(imgInfResult)).into(mIvUploadImg);
                break;
            default:
                Glide.with(getApplicationContext()).load(Uri.parse(imgInf)).into(mIvUploadImg);
                break;
        }
        return true;
    }
}