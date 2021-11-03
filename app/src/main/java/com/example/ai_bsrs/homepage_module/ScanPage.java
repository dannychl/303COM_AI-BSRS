package com.example.ai_bsrs.homepage_module;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.lifecycle.ProcessLifecycleOwner;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ai_bsrs.FragmentController;
import com.example.ai_bsrs.R;

import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ScanPage extends AppCompatActivity implements View.OnClickListener, LifecycleObserver {

    //private String postUrl = "http://192.168.8.107:5000/test";
    private String postUrl = "http://192.168.0.7:5000/test";
    private ImageView mImgUploadBackground, mImgGallery, mImgPhoto, mImgUpload, mImgProceed, mImgCancel, mImgViewResult;
    private TextView mTvImgUpload, mTvRespondText;
    public static final int PICK_IMAGE = 1;
    ActivityResultLauncher<Intent> launchActivityForGalleryResult;
    ActivityResultLauncher<Intent> launchActivityForCameraResult;
    private Uri imageToUploadUri;
    String currentImagePath = null;
    private static final int IMAGE_REQUEST = 1;
    private static final int CAMERA_PERM_CODE = 101;
    public static final int ACTIVITY_1 = 1001;
    private SharedPreferences prf;
    /*    private String ipv4Address= "192.168.8.107";
        private String portNumber = "5000";*/
    private ProgressBar mProgressBar;
    private Uri captureImgUri, galleryImgUri;

    public static final int All_PERMS_CODE = 1101;

    private File output = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_page);

        ProcessLifecycleOwner.get().getLifecycle().addObserver(this);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        prf = getSharedPreferences("result_details", Context.MODE_PRIVATE);

        checkPreviousDataExist();
        mImgViewResult = (ImageView) findViewById(R.id.imgViewResults);
        mImgUploadBackground = (ImageView) findViewById(R.id.imgUploadBackground);
        mImgUpload = (ImageView) findViewById(R.id.imgUpload);
        mImgGallery = (ImageView) findViewById(R.id.imgGallery);
        mImgPhoto = (ImageView) findViewById(R.id.imgTakePhoto);
        mTvImgUpload = (TextView) findViewById(R.id.tvUploadImage);
        mImgProceed = (ImageView) findViewById(R.id.imgProceed);
        mImgCancel = (ImageView) findViewById(R.id.imgCancel);
        mTvRespondText = (TextView) findViewById(R.id.responseText);
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);

        mImgPhoto.setOnClickListener(this);
        mImgGallery.setOnClickListener(this);
        mImgCancel.setOnClickListener(this);
        mImgProceed.setOnClickListener(this);
        mImgViewResult.setOnClickListener(this);

        //Pick from gallery
        launchActivityForGalleryResult = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        try {
                            if (result.getData() != null) {
                                Intent data = result.getData();
                                galleryImgUri = data.getData();
                                String[] filePath = {MediaStore.Images.Media.DATA};
                                Cursor c = getContentResolver().query(galleryImgUri, filePath, null, null, null);
                                c.moveToFirst();
                                int columnIndex = c.getColumnIndex(filePath[0]);
                                currentImagePath = c.getString(columnIndex);
                                c.close();
                                Bitmap thumbnail = (BitmapFactory.decodeFile(currentImagePath));
                                String mimeType = MimeTypeMap.getFileExtensionFromUrl(currentImagePath);
                                if (mimeType != null && mimeType.endsWith("mp4")) {
                                    Toast.makeText(ScanPage.this, "Uploaded file cannot be video, Please try again...", Toast.LENGTH_SHORT).show();
                                    mImgProceed.setVisibility(View.GONE);
                                    mImgCancel.setVisibility(View.GONE);
                                    mImgUpload.setVisibility(View.GONE);
                                } else {
                                    if (mimeType!=null && thumbnail!=null){
                                        mImgUpload.setVisibility(View.VISIBLE);
                                        mImgUpload.setImageBitmap(thumbnail);
                                        mTvImgUpload.setVisibility(View.GONE);
                                        viewProceedCancelButton();
                                    }else{
                                        Toast.makeText(ScanPage.this, "Upload Failed, Please try again...", Toast.LENGTH_SHORT).show();
                                        mImgProceed.setVisibility(View.GONE);
                                        mImgCancel.setVisibility(View.GONE);
                                        mImgUpload.setVisibility(View.GONE);
                                    }
                                }
                            } else {
                                Toast.makeText(ScanPage.this, "No image uploaded", Toast.LENGTH_SHORT).show();
                                mProgressBar.setVisibility(View.GONE);
                            }
                        } catch (Exception e) {
                            Toast.makeText(ScanPage.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });

        // Take photo from camera
        launchActivityForCameraResult = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                        @Override
                        public void onActivityResult(ActivityResult result) {
                            try {
                                if (result.getResultCode() == -1) {
                                    Bitmap thumbnail = (BitmapFactory.decodeFile(currentImagePath));
                                    String mimeType = MimeTypeMap.getFileExtensionFromUrl(currentImagePath);
                                    if (mimeType != null && mimeType.endsWith("mp4")) {
                                        Toast.makeText(ScanPage.this, "Uploaded file cannot be video, Please try again...", Toast.LENGTH_SHORT).show();
                                        mImgProceed.setVisibility(View.GONE);
                                        mImgCancel.setVisibility(View.GONE);
                                        mImgUpload.setVisibility(View.GONE);
                                    } else {
                                        if (mimeType != null && thumbnail != null) {
                                            mImgUpload.setVisibility(View.VISIBLE);
                                            mImgUpload.setImageBitmap(thumbnail);
                                            mTvImgUpload.setVisibility(View.GONE);
                                            viewProceedCancelButton();
                                        } else {
                                            Toast.makeText(ScanPage.this, "Upload Failed, Please try again...", Toast.LENGTH_SHORT).show();
                                            mImgProceed.setVisibility(View.GONE);
                                            mImgCancel.setVisibility(View.GONE);
                                            mImgUpload.setVisibility(View.GONE);
                                        }
                                    }
                                } else if (result.getResultCode() == 0){
                                    Toast.makeText(ScanPage.this, "No image uploaded...", Toast.LENGTH_SHORT).show();
                                    mTvImgUpload.setVisibility(View.VISIBLE);
                                    mImgProceed.setVisibility(View.GONE);
                                    mImgCancel.setVisibility(View.GONE);
                                    mImgUpload.setVisibility(View.GONE);
                                }
                            }catch (Exception e){
                                Toast.makeText(ScanPage.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                });
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    private void onAppBackgrounded() {
        Log.d("MyApp", "App in background");
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    private void onAppForegrounded() {
        Log.d("MyApp", "App in foreground");
    }

    @NotNull
    private File getImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageName = "jpg_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File imageFile = null;
        imageFile = File.createTempFile(imageName, ".jpg", storageDir);
        currentImagePath = imageFile.getAbsolutePath();
        Toast.makeText(this, "Save:" + currentImagePath, Toast.LENGTH_SHORT).show();
        return imageFile;
    }

    public void captureImage() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (cameraIntent.resolveActivity(getPackageManager()) != null) {
            File imageFile = null;
            try {
                imageFile = getImageFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (imageFile != null && currentImagePath!=null) {
                try {
                    captureImgUri = FileProvider.getUriForFile(this, "com.example.android.fileprovider", imageFile);
                    cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, captureImgUri);
                    launchActivityForCameraResult.launch(cameraIntent);
                } catch (Exception e) {
                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

            if (requestCode == IMAGE_REQUEST && resultCode == -1) {
                Log.d("tag", String.valueOf(resultCode));

                Bitmap thumbnail = (BitmapFactory.decodeFile(currentImagePath));
                String mimeType = MimeTypeMap.getFileExtensionFromUrl(currentImagePath);
                if (mimeType != null && mimeType.endsWith("mp4")) {
                    Toast.makeText(ScanPage.this, "Uploaded file cannot be video, Please try again...", Toast.LENGTH_SHORT).show();
                    mImgProceed.setVisibility(View.GONE);
                    mImgCancel.setVisibility(View.GONE);
                    mImgUpload.setVisibility(View.GONE);
                } else {
                    if (mimeType!=null && thumbnail!=null){
                        mImgUpload.setVisibility(View.VISIBLE);
                        mImgUpload.setImageBitmap(thumbnail);
                        mTvImgUpload.setVisibility(View.GONE);
                        viewProceedCancelButton();
                    }else{
                        Toast.makeText(ScanPage.this, "Upload Failed, Please try again...", Toast.LENGTH_SHORT).show();
                        mImgProceed.setVisibility(View.GONE);
                        mImgCancel.setVisibility(View.GONE);
                        mImgUpload.setVisibility(View.GONE);
                    }
            }
        } else if (resultCode == 0) {
            Toast.makeText(this, "No image uploaded...", Toast.LENGTH_SHORT).show();
            mTvImgUpload.setVisibility(View.VISIBLE);
            mImgProceed.setVisibility(View.GONE);
            mImgCancel.setVisibility(View.GONE);
            mImgUpload.setVisibility(View.GONE);
        }
}

    private void askCameraPermission(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.CAMERA}, CAMERA_PERM_CODE);
        }else{
            captureImage();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERM_CODE) {
            if (grantResults.length < 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                captureImage();
            } else {
                Toast.makeText(this, "Camera Permission is required to use camera", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent (ScanPage.this, FragmentController.class);
        intent.putExtra("CallingActivity", ViewInferenceResult.ACTIVITY_1);
        startActivity(intent);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.imgGallery: {
                try {
                    mImgViewResult.setVisibility(View.GONE);
                    mTvRespondText.setText("");
                    Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    launchActivityForGalleryResult.launch(intent);
                }catch(Exception e){
                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
            break;
            case R.id.imgTakePhoto: {
                try {
                    mImgViewResult.setVisibility(View.GONE);
                    mTvRespondText.setText("");
                    askCameraPermission();
                }catch(Exception e){
                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
            break;
            case R.id.imgProceed:{
                sendImageToFlask();
            }
            break;
            case R.id.imgCancel:{
                onBackPressed();
            }
            break;
            case R.id.imgViewResults:{

                SharedPreferences.Editor editor = prf.edit();

                if (captureImgUri !=null){
                    editor.putString("imageCapture", String.valueOf(captureImgUri));
                }
                else if (galleryImgUri !=null){
                    editor.putString("imageGallery", String.valueOf(currentImagePath));
                }
                editor.commit();
                Intent i = new Intent(this, ViewInferenceResult.class);
                startActivity(i);
            }
        }
    }

    private void checkPreviousDataExist() {

        if (prf.getString("imgResultInference", null)!=null){
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            StringBuilder strBuilder = new StringBuilder();

            builder.setCancelable(false);
            builder.setTitle("Previous Detected Result will be erased if proceed now");

            builder.setMessage(strBuilder.toString());
            builder.setPositiveButton("Proceed", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    getSharedPreferences("result_details", Context.MODE_PRIVATE).edit().clear().apply();
                    Toast.makeText(ScanPage.this, "Erasing...", Toast.LENGTH_SHORT).show();
                }
            });

            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    Toast.makeText(ScanPage.this, "Erase Cancelled", Toast.LENGTH_SHORT).show();
                    onBackPressed();
                }
            });

            builder.show();
        }
    }

    public void viewProceedCancelButton(){
        mImgProceed.setVisibility(View.VISIBLE);
        mImgCancel.setVisibility(View.VISIBLE);
    }

    public static String getFileToByte(String filePath){
        Bitmap bmp = null;
        ByteArrayOutputStream bos = null;
        byte[] bt = null;
        String encodeString = null;
        try{
            bmp = BitmapFactory.decodeFile(filePath);
            bos = new ByteArrayOutputStream();
            bmp.compress(Bitmap.CompressFormat.JPEG, 50, bos);
            bt = bos.toByteArray();
            encodeString = Base64.encodeToString(bt, Base64.DEFAULT);
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return encodeString;
    }

    public void sendImageToFlask(){

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.RGB_565;

        SharedPreferences.Editor editor = prf.edit();
        String imagePath = getFileToByte(currentImagePath);
        editor.putString("imagePreference", imagePath);
        editor.commit();
        Bitmap bitmap = BitmapFactory.decodeFile(currentImagePath, options);
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, stream);
        byte[] byteArray = stream.toByteArray();

        RequestBody postBodyImage = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("image", "androidFlask.jpg", RequestBody.create(MediaType.parse("image/*jpg"), byteArray))
                .build();

        mTvRespondText.setText("Please wait...");
        mProgressBar.setVisibility(View.VISIBLE);

        postRequest(postUrl, postBodyImage);
    }

    public void postRequest(String postUrl, RequestBody postBody) {

        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(postUrl)
                .post(postBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                // Cancel the post on failure.
                call.cancel();
                // In order to access the TextView inside the UI thread, the code is executed inside runOnUiThread()
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mTvRespondText.setText(e.getMessage());
                    }
                });
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                // In order to access the TextView inside the UI thread, the code is executed inside runOnUiThread()
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            String results = response.body().string();

                            if (!results.equals("") && !results.equals(null) && !results.equals("[]")){
                                mTvRespondText.setText("Image Analyzed");
                                mImgProceed.setVisibility(View.GONE);
                                mImgCancel.setVisibility(View.GONE);
                                SharedPreferences.Editor editor = prf.edit();
                                editor.putString("resultInference", results);
                                editor.commit();
                                mImgViewResult.setVisibility(View.VISIBLE);
                                mProgressBar.setVisibility(View.GONE);
                            }
                            else{
                                mImgViewResult.setVisibility(View.GONE);
                                mProgressBar.setVisibility(View.GONE);
                                mTvRespondText.setText("No detection of bread class found... ");
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                            Toast.makeText(ScanPage.this, "Please try to upload again...", Toast.LENGTH_SHORT).show();
                            mTvRespondText.setText("");
                            mProgressBar.setVisibility(View.GONE);
                        }catch (Exception e){
                            e.printStackTrace();
                            Toast.makeText(ScanPage.this, "Please try to upload again...", Toast.LENGTH_SHORT).show();
                            mTvRespondText.setText("");
                            mProgressBar.setVisibility(View.GONE);
                        }
                    }
                });
            }
        });
    }
}
