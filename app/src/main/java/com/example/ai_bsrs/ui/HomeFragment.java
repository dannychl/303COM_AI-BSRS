package com.example.ai_bsrs.ui;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.InputType;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.ai_bsrs.transaction_module.Transaction;
import com.example.ai_bsrs.homepage_module.Bread;
import com.example.ai_bsrs.homepage_module.BreadScannedDetailsAdapter;
import com.example.ai_bsrs.FragmentController;
import com.example.ai_bsrs.R;
import com.example.ai_bsrs.homepage_module.BreadTransactionRestoreAdapter;
import com.example.ai_bsrs.homepage_module.ScanPage;
import com.example.ai_bsrs.homepage_module.ViewInferenceResult;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static android.content.Context.MODE_PRIVATE;
import static java.lang.Thread.sleep;

public class HomeFragment extends Fragment implements View.OnTouchListener {

    private ImageView mIvClickScan, mIvUploadedImage, mIvCheckOut,
                    mIvRemoveBread, mIvDropList, mIvMessage;
    private TextView mTvImgMessage, mTvTotalBreadPrice, mTvTitleDialog, mTvBreadPrice;
    private EditText etInputBreadName, etInputBreadQty, etInputBreadPrice;
    private String addNewBreadName, addNewBreadQuantity, addNewBreadPrice, imageInf,
            imageResultInf, imageResult, image, id, databasePath, imageUriLink, imageResultUriLink,
            prfTransactionDetailsList, prfTotalPrice, prfTransactionId, currentDate, currentTime;
    private String[] breadName, breadPrice, breadQuantity;
    private SharedPreferences prf, prfTransaction, prfLogin;

    private Bitmap imageBit, tempBit;
    private BreadScannedDetailsAdapter adapter;
    private BreadTransactionRestoreAdapter transactionRestoreAdapter;
    private RecyclerView rV;
    private ArrayList<Bread> allBreadLists, mTransactionRestoreBreadLists;
    private ArrayList<Transaction> mTransactionArrayList;
    private int inputQty = 0;
    private double totalPrice = 0.00, inputPrice = 0.00;
    private FloatingActionButton addNewBreadPressed;
    private Spinner mSpinnerBreadName;
    private Uri imageUri, imageResultUri;
    private NestedScrollView mNestedScrollView;

    private static final String TAG = "HomeFragment";
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference dbCollectionRef;
    private StorageReference dbStorageRef = FirebaseStorage.getInstance().getReference("Images");
    private boolean restoreValidate = false, dropListDisable = false,
                    imageUriCheck = false, imageResultUriCheck = false,
                    addNewCheck = false, checkOutPressed = false, floatingActionBtnCheck = false;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home, container, false);
        ((AppCompatActivity) getActivity()).getSupportActionBar().hide();
        getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getContext();

        prfLogin = getActivity().getSharedPreferences("login_details", MODE_PRIVATE);
        id = prfLogin.getString("userId", "");
        databasePath = String.format("TransactionRecord/%1$s/Record", id);

        prf = getActivity().getSharedPreferences("result_details", MODE_PRIVATE);
        prfTransaction = getActivity().getSharedPreferences("transaction_details", MODE_PRIVATE);
        dbCollectionRef = db.collection(databasePath);
        mIvClickScan = (ImageView) view.findViewById(R.id.imgClickScan);
        mIvUploadedImage = (ImageView) view.findViewById(R.id.uploadImage);
        mTvImgMessage = (TextView) view.findViewById(R.id.tvImgMessage);
        mIvMessage = (ImageView) view.findViewById(R.id.ivMessage);
        mTvTotalBreadPrice = (TextView) view.findViewById(R.id.totalBreadPrice);
        mIvCheckOut = (ImageView) view.findViewById(R.id.ivCheckOut);
        mNestedScrollView = (NestedScrollView) view.findViewById(R.id.scroll);
        addNewBreadPressed =  (FloatingActionButton) view.findViewById(R.id.addNewBread);

        addNewBreadPressed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                floatingActionBtnCheck = true;
                addOrDeleteBread(0, "", "", "");
            }
        });

        addNewBreadPressed.setVisibility(View.GONE);
        mIvCheckOut.setVisibility(View.GONE);

        rV = view.findViewById(R.id.recyclerView);
        rV.setLayoutManager(new LinearLayoutManager(getActivity()));

        allBreadLists = new ArrayList<Bread>();
        mTransactionRestoreBreadLists =new ArrayList<Bread>();



        restoreValidate = getActivity().getIntent().getBooleanExtra("restoreValidate", false);
        prfTransactionDetailsList = prfTransaction.getString("transactionArrayList", "");

        imageInf = prf.getString("imgInference", "");
        imageResultInf = prf.getString("imgResultInference", "");

        image = prf.getString("imageUri", "");
        imageResult = prf.getString("imageResultUri", "");
        String breadList = prf.getString("breadList", "");
        boolean checkProceedCheckOut = prf.getBoolean("proceedCheckOut", false);

        if (!breadList.isEmpty() && checkProceedCheckOut) {
            Gson gson = new Gson();
            Type type = new TypeToken<ArrayList<Bread>>() {
            }.getType();
            allBreadLists = gson.fromJson(breadList, type);
        }

        if (restoreValidate && prfTransactionDetailsList != null) {
            prfTotalPrice = prfTransaction.getString("priceTransaction", "");
            imageUriLink = prfTransaction.getString("imageInf", "");
            imageResultUriLink = prfTransaction.getString("imageInfResult", "");
            prfTransactionId = prfTransaction.getString("transactionId", "");

            if (!prfTransactionDetailsList.isEmpty()) {
                Gson gson = new Gson();
                Type type = new TypeToken<ArrayList<Transaction>>() {
                }.getType();
                mTransactionArrayList = gson.fromJson(prfTransactionDetailsList, type);
                Glide.with(getActivity()).load(Uri.parse(imageUriLink)).into(mIvUploadedImage);
                Glide.with(getActivity()).load(Uri.parse(imageResultUriLink)).into(mIvUploadedImage);

                mTvImgMessage.setVisibility(View.GONE);
                mIvMessage.setVisibility(View.GONE);
                mTvTotalBreadPrice.setText("RM " + prfTotalPrice);
                mTransactionRestoreBreadLists = new ArrayList<Bread>();

                for (int i = 0; i < mTransactionArrayList.size(); i++) {
                    mTransactionRestoreBreadLists.add(new Bread(
                            mTransactionArrayList.get(i).getBreadName().trim(),
                            mTransactionArrayList.get(i).getBreadQty().trim(),
                            mTransactionArrayList.get(i).getBreadPriceEach().trim()));
                }

                if (mTransactionRestoreBreadLists.size() > 0){
                    scrollToBtm();
                    displayBreadDetectedDetails(restoreValidate, mTransactionRestoreBreadLists);
                }
            }
        }

        if (!(imageInf.isEmpty() && imageResultInf.isEmpty())) {
            tempBit = decodeToBase64(imageInf);
            imageBit = decodeToBase64(imageResultInf);
            mIvUploadedImage.setImageBitmap(tempBit);
            mTvImgMessage.setVisibility(View.GONE);
            mIvMessage.setVisibility(View.GONE);
        }


        mIvUploadedImage.setOnTouchListener(this);

        mIvClickScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getActivity(), ScanPage.class);
                startActivity(i);
            }
        });

        mIvCheckOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!restoreValidate && allBreadLists.size() > 0){
                    saveIntoFirebase(allBreadLists);
                }
                else if (restoreValidate && mTransactionRestoreBreadLists.size()>0){
                    saveIntoFirebase(mTransactionRestoreBreadLists);

                }
                else {
                    Toast.makeText(getActivity(), "No bread in the lists.. Please try add some", Toast.LENGTH_SHORT).show();
                }
            }
        });

        if (allBreadLists != null) {
            if (allBreadLists.size() > 0) {
                scrollToBtm();
                displayBreadDetectedDetails(restoreValidate, allBreadLists);
            }
        }

        return view;
    }

    private void scrollToBtm() {
        mNestedScrollView.postDelayed(new Runnable() {
            @Override
            public void run() {
                mNestedScrollView.fullScroll(ScrollView.FOCUS_DOWN);
            }
        }, 1000);
        mIvCheckOut.setVisibility(View.VISIBLE);
        addNewBreadPressed.setVisibility(View.VISIBLE);
    }

    private void getCurrentDateTime() {
        String amPm = "";

        Date date = Calendar.getInstance().getTime();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");

        final Calendar calendar = Calendar.getInstance();

        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        if (hour >= 12) {
            amPm = "PM";
            hour -= 12;
        } else
            amPm = "AM";

        currentDate = simpleDateFormat.format(date);
        currentTime = (String.format("%02d:%02d", hour, minute) + amPm);
    }

    private Map<String, Object> storeData(ArrayList<Bread> breadLists, Map<String, Object> breadTransactionDetail){
        // one list of breadname's column can have many breadName in Cloud firestore
        // one list of breadprice's column can have many breadPrice in Cloud firestore
        // one list of breadQty's column can have many breadQty in Cloud firestore
        breadName = new String[breadLists.size()];
        breadPrice = new String[breadLists.size()];
        breadQuantity = new String[breadLists.size()];


        for (int load = 0; load < breadLists.size(); load++) {
            breadName[load] = breadLists.get(load).getName();
            breadPrice[load] = breadLists.get(load).getPrice();
            breadQuantity[load] = breadLists.get(load).getQuantity();
        }

        getCurrentDateTime();
        breadTransactionDetail.put("KEY_BREAD_NAME", Arrays.asList(breadName));
        breadTransactionDetail.put("KEY_BREAD_QUANTITY", Arrays.asList(breadQuantity));
        breadTransactionDetail.put("KEY_BREAD_PRICE", Arrays.asList(breadPrice));
        breadTransactionDetail.put("KEY_BREAD_TOTAL_PRICE", String.format("%.2f", totalPrice));
        breadTransactionDetail.put("KEY_BREAD_TIME_CHECKOUT", currentTime);
        breadTransactionDetail.put("KEY_BREAD_DATE_CHECKOUT", currentDate);
        breadTransactionDetail.put("KEY_TIMESTAMP", System.currentTimeMillis());
        return breadTransactionDetail;
    }


    public void runCheckOutProcess (ArrayList<Bread> breadLists){
        class MyTask extends AsyncTask<Void, Void, Void> {

            Map<String, Object> breadTransactionDetail = new HashMap<>();

            @Override
            protected Void doInBackground(Void... voids) {

                breadTransactionDetail = storeData(breadLists, breadTransactionDetail);

                try{
                    if (!image.isEmpty() && !imageResult.isEmpty()){
                        imageUri = Uri.parse(image);
                        imageResultUri = Uri.parse(imageResult);
                    }

                    if (imageUri!=null && imageResultUri!=null){

                        String nameOfImgInf = String.valueOf(imageUri);
                        String nameOfImgInfResult = String.valueOf(imageResultUri);
                        StorageReference imgRef = dbStorageRef.child(nameOfImgInf);
                        StorageReference imgRefResult = dbStorageRef.child(nameOfImgInfResult);


                        UploadTask imgRefUploadTask = imgRef.putFile(imageUri);
                        UploadTask imgRefResultUploadTask = imgRefResult.putFile(imageResultUri);

                        imgRefResultUploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                            @Override
                            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                                if (!task.isSuccessful()){
                                    throw task.getException();
                                }
                                return imgRefResult.getDownloadUrl();
                            }
                        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                            @Override
                            public void onComplete(@NonNull Task<Uri> task) {
                                if (task.isSuccessful()){
                                    imageResultUriCheck = true;  // set to true to wait both image Uri completely upload to Firebase Storage
                                    imageResultUriLink = task.getResult().toString();

                                    if (imageResultUriCheck && imageUriCheck){
                                        uploadIntoDatabase(breadLists, breadTransactionDetail);
                                    }
                                    Toast.makeText(getActivity(), "Loading Complete.", Toast.LENGTH_SHORT).show();
                                }
                                else if (!task.isSuccessful()){
                                    Toast.makeText(getActivity(), String.valueOf(task.getException()), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

                        imgRefUploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                            @Override
                            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                                if (!task.isSuccessful()){
                                    throw task.getException();
                                }
                                return imgRef.getDownloadUrl();

                            }
                        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                            @Override
                            public void onComplete(@NonNull Task<Uri> task) {
                                if (task.isSuccessful()){
                                    imageUriLink = task.getResult().toString();
                                    imageUriCheck = true;

                                    if (imageUriCheck && imageResultUriCheck){
                                        uploadIntoDatabase(breadLists, breadTransactionDetail);
                                    }
                                }
                                else if (!task.isSuccessful()){
                                    Toast.makeText(getActivity(), String.valueOf(task.getException()), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }else{
                        Toast.makeText(getActivity(), "Uploaded Image in Null", Toast.LENGTH_SHORT).show();
                    }
                }catch (Exception e){
                    Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();

                }
                return null;
            }

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }

            protected void onPostExecute() {
            }

        }
        MyTask myTask = new MyTask();
        myTask.doInBackground();
        Toast.makeText(getActivity(), "Storing data into database... Please wait a moment..", Toast.LENGTH_SHORT).show();
    }



    private void uploadIntoDatabase(ArrayList<Bread> breadLists, Map<String, Object> breadTransactionDetail) {

        if (breadLists.size()>0) {      // enter this if statement when create new and upload to database

            if (imageUriLink != null && imageResultUriLink != null) {
                breadTransactionDetail.put("KEY_IMAGE", imageUriLink);
                breadTransactionDetail.put("KEY_IMAGE_RESULT", imageResultUriLink);
            }

            if (!restoreValidate && breadTransactionDetail.size() > 0) {
                breadTransactionDetail.put("KEY_STATUS", "Paid");
                dbCollectionRef.add(breadTransactionDetail).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        if (!checkOutPressed) {
                            Toast.makeText(getActivity(), "Transaction Record Successfully Saved", Toast.LENGTH_SHORT).show();
                            checkOutPressed = true;
                            getActivity().getSharedPreferences("result_details", Context.MODE_PRIVATE).edit().clear().apply();

                            Intent intent = new Intent(getActivity(), FragmentController.class);
                            intent.putExtra("CallingActivity", ViewInferenceResult.ACTIVITY_1);
                            startActivity(intent);

                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getActivity(), "Error! Transaction Record Unsuccessfully Save", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, e.toString());
                    }
                });
            } else {    // enter this if statement when user try to restore the data to make changes
                breadTransactionDetail.put("KEY_STATUS", "Changes Made");
                dbCollectionRef.document(prfTransactionId).set(breadTransactionDetail).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        if (!checkOutPressed) {

                            Toast.makeText(getActivity(), "Transaction Updated Successfully", Toast.LENGTH_SHORT).show();
                            checkOutPressed = true;
                            getActivity().getSharedPreferences("transaction_details", Context.MODE_PRIVATE).edit().clear().apply();

                            Intent intent = new Intent(getActivity(), FragmentController.class);
                            intent.putExtra("CallingActivity", ViewInferenceResult.ACTIVITY_1);
                            startActivity(intent);

                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getActivity(), "Error! Transaction Update Unsuccessfully", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, e.toString());
                    }
                });
            }
        }


        // enter this if statement when user try to restore the data to make changes
        /*}else if(breadLists.size()>0){

            breadTransactionDetail.put("KEY_IMAGE", imageUriLink);
            breadTransactionDetail.put("KEY_IMAGE_RESULT", imageResultUriLink);
            breadTransactionDetail.put("KEY_STATUS", "Changes Made");

            if (!restoreValidate && breadTransactionDetail.size()>0){
                dbCollectionRef.add(breadTransactionDetail).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        if (!checkOutPressed) {

                            Toast.makeText(getActivity(), "Transaction Record Successfully Saved", Toast.LENGTH_SHORT).show();
                            checkOutPressed = true;
                            getActivity().getSharedPreferences("result_details", Context.MODE_PRIVATE).edit().clear().apply();

                            Intent intent = new Intent(getActivity(), FragmentController.class);
                            intent.putExtra("CallingActivity", ViewInferenceResult.ACTIVITY_1);
                            startActivity(intent);

                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getActivity(), "Error! Transaction Record Unsuccessfully Save", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, e.toString());
                    }
                });
            }else{
                dbCollectionRef.document(prfTransactionId).set(breadTransactionDetail).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        if (!checkOutPressed) {

                            Toast.makeText(getActivity(), "Transaction Updated Successfully", Toast.LENGTH_SHORT).show();
                            checkOutPressed = true;
                            getActivity().getSharedPreferences("transaction_details", Context.MODE_PRIVATE).edit().clear().apply();

                            Intent intent = new Intent(getActivity(), FragmentController.class);
                            intent.putExtra("CallingActivity", ViewInferenceResult.ACTIVITY_1);
                            startActivity(intent);

                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getActivity(), "Error! Transaction Update Unsuccessfully", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, e.toString());
                    }
                });
            }
        }*/

    }

    private void saveIntoFirebase(ArrayList<Bread> breadLists) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.CustomAlertDialog);

        builder.setCancelable(false);
        builder.setTitle("Confirm Check-Out?");


        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (isNetworkAvailable()){
                    if(!restoreValidate){
                        runCheckOutProcess(breadLists);         // create new data
                    }else{
                        updateCheckOutProcess(breadLists);      // restore an existing data
                    }
                }else{
                    Toast.makeText(getActivity(), "Please connect to internet for better user experience...", Toast.LENGTH_SHORT).show();
                }
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Toast.makeText(getActivity(), "Check-out cancelled...", Toast.LENGTH_SHORT).show();
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
        Button btnNegative = alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE);
        btnNegative.setTextColor(Color.RED);

    }

    private void updateCheckOutProcess(ArrayList<Bread> breadLists) {

        Map<String, Object> breadTransactionDetail = new HashMap<>();
        breadTransactionDetail = storeData(breadLists, breadTransactionDetail);
        uploadIntoDatabase(breadLists, breadTransactionDetail);
    }


    private void displayBreadDetectedDetails(boolean restoreValidate, ArrayList<Bread> breadLists) {


        if (!restoreValidate) {
            updateTotalPrice(breadLists);
            adapter = new BreadScannedDetailsAdapter(getActivity(), allBreadLists);
            rV.setHasFixedSize(true);
            rV.setAdapter(adapter);

            adapter.setOnItemClickListener(new BreadScannedDetailsAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(int position, String name, String price, String quantity) {

                    addOrDeleteBread(position, name, price, quantity);
                    //line  = different bread (if only one bread will only have one line)
                    // numOfData = each bread have how many quantity
                    // res = all data of bread with name and quantity
                }});

        } else {
            transactionRestoreAdapter = new BreadTransactionRestoreAdapter(getActivity(), mTransactionRestoreBreadLists);
            rV.setHasFixedSize(true);
            rV.setAdapter(transactionRestoreAdapter);

            transactionRestoreAdapter.setOnItemClickListener(new BreadTransactionRestoreAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(int position, String name, String price, String qty) {
                    addOrDeleteBread(position, name, price, qty);
                }
            });
        }
    }

    private void updateTotalPrice(ArrayList<Bread> breadList) {

        totalPrice = 0.00;
        int qty;

        for (int i = 0; i < breadList.size(); i++) {

            String price = breadList.get(i).getPrice();
            String quantity = breadList.get(i).getQuantity();
            qty = Integer.parseInt(String.valueOf(quantity).trim());

            double cost = Double.parseDouble(price);
            qty = Integer.parseInt(String.valueOf(quantity).trim());
            totalPrice += (cost * qty);
        }
        mTvTotalBreadPrice.setText("RM " + String.format("%.2f", totalPrice));

    }


    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        int action = motionEvent.getAction();
        switch (action) {
            case MotionEvent.ACTION_UP:
                if (!restoreValidate){
                    mIvUploadedImage.setImageBitmap(tempBit);
                }else{
                    Glide.with(getActivity()).load(Uri.parse(imageResultUriLink)).into(mIvUploadedImage);
                }
                break;
            default:
                if (!restoreValidate){
                    mIvUploadedImage.setImageBitmap(imageBit);
                }else{
                    Glide.with(getActivity()).load(Uri.parse(imageUriLink)).into(mIvUploadedImage);
                }
                break;
        }
        return true;
    }

    public static Bitmap decodeToBase64(String input) {
        byte[] decodedByte = Base64.decode(input, 0);
        return BitmapFactory.decodeByteArray(decodedByte, 0, decodedByte.length);
    }

    public String encodeToBase64(Bitmap bitmap) {
        ByteArrayOutputStream encode = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, encode);
        byte[] b = encode.toByteArray();
        String temp = Base64.encodeToString(b, Base64.DEFAULT);
        return temp;
    }

    private void addOrDeleteBread(int position, String name, String price, String quantity) {

        LayoutInflater layoutInflater = getActivity().getLayoutInflater();
        final View view = layoutInflater.inflate(R.layout.dialog_add_edit_bread, null);
        AlertDialog alertDialog = new AlertDialog.Builder(getActivity(), R.style.CustomAlertDialog)
                .setPositiveButton("Save", null)
                .setNegativeButton("Cancel", null).create();

        alertDialog.setCancelable(false);

        mTvBreadPrice = (TextView) view.findViewById(R.id.tvBreadPrice);
        mTvTitleDialog = (TextView) view.findViewById(R.id.titleOfDialog);
        etInputBreadName = (EditText) view.findViewById(R.id.editBreadName);
        etInputBreadQty = (EditText) view.findViewById(R.id.editBreadQuantity);
        etInputBreadPrice = (EditText) view.findViewById(R.id.editBreadPrice);
        mIvRemoveBread = (ImageView) view.findViewById(R.id.removeBread);
        mSpinnerBreadName = (Spinner) view.findViewById(R.id.spinnerBreadNames);
        mIvDropList = (ImageView) view.findViewById(R.id.ivDropList);


        mIvRemoveBread.setBackgroundResource(R.drawable.removebread);


        mIvDropList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (!dropListDisable){
                    addNewCheck = false;
                    dropDownListDisabledView();
                    dropListDisable = true;
                    etInputBreadName.setText(name);
                }
                else{
                    addNewCheck = false;
                    int positionSelect = getSpinnerPosition(name);
                    if (positionSelect == 4){
                        dropListDisable = false;

                        dropDownListEnabledView();
                        mSpinnerBreadName.setSelection(0);
                    }else{
                        dropListDisable = false;
                        dropDownListEnabledView();
                        if (positionSelect >=0 && positionSelect<4)
                            mSpinnerBreadName.setSelection(positionSelect);
                        else
                            mSpinnerBreadName.setSelection(0);
                    }
                }
            }
        });

        //only enter this statement when user pressed on the add floating action button to add new bread
            if (floatingActionBtnCheck) {
                mTvTitleDialog.setText("Add New");

                // triggered this onclick when user pressed on the drop down list
                mIvDropList.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        // enter this statement when user click on the dropdownlist
                        // by default dropListDisable is false
                        if (!dropListDisable) {
                            dropListDisable = true;
                            dropDownListDisabledView();
                            etInputBreadName.setText(null);
                            etInputBreadPrice.setText("");
                            etInputBreadPrice.setEnabled(true);
                        } else {
                            dropListDisable = false;
                            dropDownListEnabledView();
                            mSpinnerBreadName.setSelection(0);
                            etInputBreadPrice.setText("1.80");
                            etInputBreadPrice.setEnabled(false);
                        }
                    }
                });
                addNewCheck = true; // set addNewCheck become true to make validation later
                dropListDisable = true; // set dropListDisable become true as by default is false
                dropDownListDisabledView();

                mIvRemoveBread.setVisibility(View.GONE);

                mTvBreadPrice.setVisibility(View.VISIBLE);
                etInputBreadPrice.setVisibility(View.VISIBLE);
                etInputBreadPrice.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_NUMBER_FLAG_SIGNED);
                etInputBreadName.setText(null);
                etInputBreadQty.setText(null);
                floatingActionBtnCheck = false;
            }else{
                addNewCheck = false;
                dropListDisable = true;
                dropDownListDisabledView();
            }


        mIvRemoveBread.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                etInputBreadName.setFocusable(false);
                etInputBreadQty.setFocusable(false);
                mSpinnerBreadName.setEnabled(false);
                mTvTitleDialog.setText("Confirm to Remove?");

                alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setText("Remove");
                alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setText("Cancel");
                Button removeBtn = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                mIvRemoveBread.setVisibility(View.GONE);

                removeBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Toast.makeText(getActivity(), "Bread Removed...", Toast.LENGTH_SHORT).show();

                        if (!restoreValidate){
                            allBreadLists.remove(position);
                            Gson gson = new Gson();
                            String jsonArray = gson.toJson(allBreadLists);
                            SharedPreferences.Editor editor = prf.edit();
                            editor.putString("breadList", jsonArray);
                            editor.commit();
                            adapter.notifyDataSetChanged();
                            updateTotalPrice(allBreadLists);
                        }
                        else {
                            mTransactionRestoreBreadLists.remove(position);
                            transactionRestoreAdapter.notifyDataSetChanged();
                            updateTotalPrice(mTransactionRestoreBreadLists);
                        }
                        alertDialog.dismiss();
                    }
                });


            }
        });

        // Set the name and price when adapter of position clicked
        int positionSelect = getSpinnerPosition(name);
        if (positionSelect == 4){
            dropDownListDisabledView();
            etInputBreadName.setText(name);
        }else{
            if (positionSelect >=0 && positionSelect<4)
                mSpinnerBreadName.setSelection(positionSelect);
            else
                mSpinnerBreadName.setSelection(0);
        }
        etInputBreadName.setText(name);
        etInputBreadQty.setText(quantity);

        mSpinnerBreadName.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                addNewBreadName = adapterView.getItemAtPosition(i).toString();

                switch (mSpinnerBreadName.getSelectedItemPosition()){
                    case 0:
                        etInputBreadPrice.setText("1.80");
                        etInputBreadPrice.setEnabled(false);
                        break;
                    case 1:
                        etInputBreadPrice.setText("2.00");
                        etInputBreadPrice.setEnabled(false);
                        break;
                    case 2:
                        etInputBreadPrice.setText("3.00");
                        etInputBreadPrice.setEnabled(false);
                        break;
                    case 3:
                        etInputBreadPrice.setText("2.50");
                        etInputBreadPrice.setEnabled(false);
                        break;
                }


            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                Button button = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                Button btnCancel = alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE);
                button.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {

                        // add a new bread
                        if (addNewCheck) {

                            // when user click the add button, by default dropListDisable will be false and will go to else statement
                            // enter this statement when droplistDisable is true and validation will goes here
                            if (dropListDisable){
                                boolean addNewBreadNameCheck = etInputBreadName.getText().toString().trim().isEmpty();
                                boolean inputQtyCheck = etInputBreadQty.getText().toString().trim().isEmpty();

                                boolean addNewBreadPriceCheck = etInputBreadPrice.getText().toString().isEmpty();

                                if (addNewBreadNameCheck) {
                                    etInputBreadName.setError("Name cannot be empty");
                                    etInputBreadName.requestFocus();
                                }
                                if (addNewBreadPriceCheck) {
                                    etInputBreadPrice.setError("Price cannot be empty");
                                    etInputBreadPrice.requestFocus();
                                }

                                if (inputQtyCheck) {
                                    etInputBreadQty.setError("Quantity cannot be empty");
                                    etInputBreadQty.requestFocus();
                                }
                                if (!inputQtyCheck && !addNewBreadNameCheck && !addNewBreadPriceCheck) {

                                    addNewBreadName = etInputBreadName.getText().toString();
                                    addNewBreadPrice = String.format("%.2f", Double.parseDouble(etInputBreadPrice.getText().toString()));
                                    inputQty = Integer.parseInt(String.valueOf(etInputBreadQty.getText()).trim());
                                    inputPrice = Double.parseDouble(addNewBreadPrice);
                                    if (inputQty < 1 && !inputQtyCheck) {
                                        etInputBreadQty.setError("Minimum quantity only allowed 10");
                                        etInputBreadQty.requestFocus();
                                    } else if (inputQty > 1000 && !inputQtyCheck) {
                                        etInputBreadQty.setError("Maximum quantity breads only allowed 1000");
                                        etInputBreadQty.requestFocus();
                                    }else if (!addNewBreadPriceCheck && inputPrice < 1){
                                        etInputBreadPrice.setError("Minimum price only allowed start from RM1");
                                        etInputBreadPrice.requestFocus();
                                    }else if( !addNewBreadPriceCheck && inputPrice > 100){
                                        etInputBreadPrice.setError("Maximum price only allowed up to RM100");
                                        etInputBreadPrice.requestFocus();
                                    }
                                    else {
                                        addNewBreadQuantity = String.valueOf(inputQty);
                                        Toast.makeText(getActivity(), "New Bread Added!", Toast.LENGTH_SHORT).show();

                                        if (!restoreValidate){
                                            allBreadLists.add(new Bread(addNewBreadName, addNewBreadQuantity, addNewBreadPrice));
                                            Gson gson = new Gson();
                                            String jsonArray = gson.toJson(allBreadLists);
                                            SharedPreferences.Editor editor = prf.edit();
                                            editor.putString("breadList", jsonArray);
                                            editor.commit();
                                        }else{
                                            mTransactionRestoreBreadLists.add(new Bread(addNewBreadName, addNewBreadQuantity, addNewBreadPrice));
                                        }

                                        alertDialog.dismiss();
                                    }
                                }
                            }
                            //enter this statement when droplistDisable is false and validation will goes here
                            else{
                                boolean addNewBreadNameCheck = addNewBreadName.trim().isEmpty();
                                boolean inputQtyCheck = etInputBreadQty.getText().toString().trim().isEmpty();
                                boolean addNewBreadPriceCheck = etInputBreadPrice.getText().toString().isEmpty();

                                if (addNewBreadNameCheck) {
                                    Toast.makeText(getActivity(), "Name cannot be empty", Toast.LENGTH_SHORT).show();
                                }
                                if (addNewBreadPriceCheck) {
                                    etInputBreadPrice.setError("Price cannot be empty");
                                    etInputBreadPrice.requestFocus();
                                }

                                if (inputQtyCheck) {
                                    etInputBreadQty.setError("Quantity cannot be empty");
                                    etInputBreadQty.requestFocus();
                                }
                                if (!inputQtyCheck && !addNewBreadNameCheck && !addNewBreadPriceCheck) {
                                    addNewBreadPrice = String.format("%.2f", Double.parseDouble(etInputBreadPrice.getText().toString()));
                                    inputQty = Integer.parseInt(String.valueOf(etInputBreadQty.getText()).trim());
                                    if (inputQty < 1 && !inputQtyCheck) {
                                        etInputBreadQty.setError("Minimum quantity only allowed 1");
                                        etInputBreadQty.requestFocus();
                                    } else if (inputQty > 1000 && !inputQtyCheck) {
                                        etInputBreadQty.setError("Maximum quantity breads only allowed 1000");
                                        etInputBreadQty.requestFocus();
                                    }
                                    else {
                                        addNewBreadQuantity = String.valueOf(inputQty);
                                        Toast.makeText(getActivity(), "New Bread Added!", Toast.LENGTH_SHORT).show();

                                        if (!restoreValidate){
                                            allBreadLists.add(new Bread(addNewBreadName, addNewBreadQuantity, addNewBreadPrice));
                                            Gson gson = new Gson();
                                            String jsonArray = gson.toJson(allBreadLists);
                                            SharedPreferences.Editor editor = prf.edit();
                                            editor.putString("breadList", jsonArray);
                                            editor.commit();
                                        }else{
                                            mTransactionRestoreBreadLists.add(new Bread(addNewBreadName, addNewBreadQuantity, addNewBreadPrice));
                                        }

                                        alertDialog.dismiss();
                                    }
                                }
                            }

                        } else if (!addNewCheck) {   // change the bread description

                            if (dropListDisable){
                                boolean editBreadNameCheck = etInputBreadName.getText().toString().trim().isEmpty();
                                boolean editBreadQtyCheck = etInputBreadQty.getText().toString().trim().isEmpty();
                                //addNewCheck = false;

                                if (editBreadNameCheck) {
                                    etInputBreadName.setError("Name cannot be empty");
                                    etInputBreadName.requestFocus();
                                }

                                if (editBreadQtyCheck) {
                                    etInputBreadQty.setError("Quantity cannot be empty");
                                    etInputBreadQty.requestFocus();
                                }
                                if (!editBreadNameCheck && !editBreadQtyCheck) {
                                    inputQty = Integer.parseInt(String.valueOf(etInputBreadQty.getText()).trim());
                                    if (inputQty < 1 && !editBreadQtyCheck) {
                                        etInputBreadQty.setError("Minimum quantity only allowed 1");
                                        etInputBreadQty.requestFocus();
                                    } else if (inputQty > 1000 && !editBreadQtyCheck) {
                                        etInputBreadQty.setError("Maximum quantity breads only allowed 1000");
                                        etInputBreadQty.requestFocus();
                                    } else {

                                        addNewBreadQuantity = String.valueOf(inputQty);
                                       /* if (name.equals(etInputBreadName.getText().toString())){
                                            Toast.makeText(getActivity(), "Bread same!", Toast.LENGTH_SHORT).show();
                                        }*/
                                        if (!name.equals(etInputBreadName.getText().toString())
                                                || !quantity.equals(addNewBreadQuantity)) {
                                            Toast.makeText(getActivity(), "Bread changed!", Toast.LENGTH_SHORT).show();

                                            Gson gson = new Gson();
                                            if (!restoreValidate){
                                                allBreadLists.get(position).setName(etInputBreadName.getText().toString());
                                                allBreadLists.get(position).setQuantity(addNewBreadQuantity);

                                                String jsonArray = gson.toJson(allBreadLists);
                                                SharedPreferences.Editor editor = prf.edit();
                                                editor.putString("breadList", jsonArray);
                                                editor.commit();
                                            }else{
                                                mTransactionRestoreBreadLists.get(position).setName(etInputBreadName.getText().toString());
                                                mTransactionRestoreBreadLists.get(position).setQuantity(addNewBreadQuantity);
                                            }
                                        }
                                        alertDialog.dismiss();
                                    }
                                }
                            }else{
                                boolean editBreadNameCheck = addNewBreadName.trim().isEmpty();
                                //boolean editBreadNameCheck = etInputBreadName.getText().toString().trim().isEmpty();
                                boolean editBreadQtyCheck = etInputBreadQty.getText().toString().trim().isEmpty();
                                //addNewCheck = false;

                                if (editBreadNameCheck) {
                                    etInputBreadName.setError("Name cannot be empty");
                                    etInputBreadName.requestFocus();
                                }

                                if (editBreadQtyCheck) {
                                    etInputBreadQty.setError("Quantity cannot be empty");
                                    etInputBreadQty.requestFocus();
                                }
                                if (!editBreadNameCheck && !editBreadQtyCheck) {
                                    inputQty = Integer.parseInt(String.valueOf(etInputBreadQty.getText()).trim());
                                    if (inputQty < 1 && !editBreadQtyCheck) {
                                        etInputBreadQty.setError("Minimum quantity only allowed 1");
                                        etInputBreadQty.requestFocus();
                                    } else if (inputQty > 1000 && !editBreadQtyCheck) {
                                        etInputBreadQty.setError("Maximum quantity breads only allowed 1000");
                                        etInputBreadQty.requestFocus();
                                    } else {

                                        addNewBreadQuantity = String.valueOf(inputQty);
                                        addNewBreadPrice = String.format("%.2f", getBreadPrice(addNewBreadName));
                                       /* if (name.equals(etInputBreadName.getText().toString())){
                                            Toast.makeText(getActivity(), "Bread same!", Toast.LENGTH_SHORT).show();
                                        }*/
                                        if (!name.equals(addNewBreadName)
                                                || !quantity.equals(addNewBreadQuantity)) {
                                            Toast.makeText(getActivity(), "Bread changed!", Toast.LENGTH_SHORT).show();

                                            Gson gson = new Gson();
                                            if (!restoreValidate) {
                                                allBreadLists.get(position).setName(addNewBreadName);
                                                allBreadLists.get(position).setQuantity(addNewBreadQuantity);
                                                allBreadLists.get(position).setPrice(addNewBreadPrice);

                                                String jsonArray = gson.toJson(allBreadLists);
                                                SharedPreferences.Editor editor = prf.edit();
                                                editor.putString("breadList", jsonArray);
                                                editor.commit();
                                            } else {
                                                mTransactionRestoreBreadLists.get(position).setName(addNewBreadName);
                                                mTransactionRestoreBreadLists.get(position).setQuantity(addNewBreadQuantity);
                                                mTransactionRestoreBreadLists.get(position).setPrice(addNewBreadPrice);
                                            }
                                        }
                                        alertDialog.dismiss();
                                    }
                                }
                            }
                        }
                        if (!restoreValidate) {
                            adapter.notifyDataSetChanged();
                            updateTotalPrice(allBreadLists);
                        } else {
                            transactionRestoreAdapter.notifyDataSetChanged();
                            updateTotalPrice(mTransactionRestoreBreadLists);
                        }
                    }
                });
                btnCancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        addNewCheck = false;
                        Toast.makeText(getActivity(), "Cancelled Clicked", Toast.LENGTH_SHORT).show();
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

    private double getBreadPrice(String name) {

        double price = 0.00;
        switch (name){
            case "Chocolate Donut":
                price =  1.80;
                break;
            case "Mexico":
                price = 2.00;
                break;
            case "Polo Bun":
                price = 3.00;
                break;
            case "Portugese Tart":
                price = 2.50;
                break;
        }
        return price;
    }

    private void dropDownListEnabledView(){
        mSpinnerBreadName.setVisibility(View.VISIBLE);
        etInputBreadName.setVisibility(View.GONE);
    }

    private void dropDownListDisabledView(){
        mSpinnerBreadName.setVisibility(View.GONE);
        etInputBreadName.setVisibility(View.VISIBLE);
    }

    private int getSpinnerPosition(String name) {
        if (name.equals("Chocolate Donut"))
            return 0;
        else if (name.equals("Mexico"))
            return 1;
        else if (name.equals("Polo Bun"))
            return 2;
        else if (name.equals("Portugese Tart"))
            return 3;

        return 4;
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}
