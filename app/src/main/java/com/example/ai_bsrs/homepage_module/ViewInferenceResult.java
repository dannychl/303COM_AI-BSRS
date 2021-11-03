package com.example.ai_bsrs.homepage_module;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Base64;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ai_bsrs.FragmentController;
import com.example.ai_bsrs.R;
import com.example.ai_bsrs.ui.HomeFragment;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Scanner;

public class ViewInferenceResult extends AppCompatActivity implements View.OnTouchListener, View.OnClickListener {

    private ImageView mImgUpload, mIvTotalOfBreadDetected, mIvProceedCheckOut;
    private TextView mTvResult, mTvTotalOfBreadDetected;
    private SharedPreferences prf;
    private Bitmap imageBit, tempBit;
    private String xMin, xMax, yMin, yMax, confidenceLevel, nameOfBread, numOfClass, sizeOfRectWidth, sizeOfRectHeight, tempConfidence;
    private ArrayList<InferenceDataset> totalOfBreadQuantity;
    private ArrayList<Bread> mBreadLists;
    private int numOfResult = 0;
    private Paint inferenceImgPaint, textPaint, trPaint;
    private Canvas tempCanvas;
    private float textSize;
    private int quantityOfMexico = 0, quantityOfPortart = 0, quantityOfChocDonut = 0, quantityOfPoloBun = 0;
    public static final int ACTIVITY_1 = 1001;
    private Uri imgResultUri, imgUri;
    private File imageFile = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_inference_result);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mImgUpload = (ImageView)findViewById(R.id.imgUpload);
        mTvResult = (TextView)findViewById(R.id.tvResults);
        mTvTotalOfBreadDetected = (TextView) findViewById(R.id.tvTotalOfBreadDetected);
        mIvTotalOfBreadDetected = (ImageView) findViewById(R.id.ivTotalOfBreadDetected);
        mIvProceedCheckOut = (ImageView) findViewById(R.id.ivProceedCheckOut);
        inferenceImgPaint = new Paint();
        textPaint = new Paint();
        trPaint = new Paint();


        prf = getSharedPreferences("result_details",MODE_PRIVATE);
        String imagePref = prf.getString("imagePreference", "");
        String result = prf.getString("resultInference", "");

        imageBit = decodeToBase64(imagePref);                           //decode the original image into base64 of bitmap
        tempBit = Bitmap.createBitmap(imageBit.getWidth(), imageBit.getHeight(), Bitmap.Config.RGB_565);
        tempCanvas = new Canvas(tempBit);                               // declare to set the bounding box and draw the canvas
        tempCanvas.drawBitmap(imageBit, 0, 0, null);  // draw the original image to the tempCanvas in order to draw bounding box later
        totalOfBreadQuantity = loadResults(result); // load returned json into loadResult function and return back to totalOfBreadQuantity

        if(!imagePref.equals("")) {
                for (int s= 0; s<totalOfBreadQuantity.size(); s++){
                inferenceImgPaint.setStrokeWidth(10);
                inferenceImgPaint.setStyle(Paint.Style.STROKE);
                textPaint.setColor(Color.WHITE);
                trPaint.setStyle(Paint.Style.FILL);
                numOfClass = totalOfBreadQuantity.get(s).getNumOfClass();
                setImageBoundingBoxColor();
                sizeOfRectWidth = String.valueOf(tempCanvas.getWidth());
                sizeOfRectHeight = String.valueOf(tempCanvas.getHeight());
                tempConfidence = String.format("%.2f",Float.parseFloat(totalOfBreadQuantity.get(s).getConfidenceLevel()));
                validateImageScale();
                tempCanvas.drawRect(Float.parseFloat(totalOfBreadQuantity.get(s).getxMin()),
                        Float.parseFloat(totalOfBreadQuantity.get(s).getxMax()),
                        Float.parseFloat(totalOfBreadQuantity.get(s).getyMin()),
                        Float.parseFloat(totalOfBreadQuantity.get(s).getyMax()), inferenceImgPaint);
                measureText(totalOfBreadQuantity.get(s).getNameOfBread(), tempConfidence, s);
            }
            mImgUpload.setImageBitmap(tempBit);     //image with bounding box
        }
        mImgUpload.setOnTouchListener(this);
        mIvProceedCheckOut.setOnClickListener(this);
    }

    private void convertBitmapToUri(Bitmap tempBit) {

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageName = "jpg_" + timeStamp+ "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        try{
            imageFile = new File(Environment.getExternalStorageDirectory() + imageFile.separator + imageName);
            imageFile =  File.createTempFile(imageName, ".jpg", storageDir);
            imageFile.createNewFile();

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            tempBit.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] bitmapData = baos.toByteArray();

            //Write bytes into file
            FileOutputStream mFos = new FileOutputStream(imageFile);
            mFos.write(bitmapData);
            mFos.flush();
            mFos.close();

        }catch (Exception e){
            e.printStackTrace();
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }


    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {

        int action = motionEvent.getAction();
        switch (action) {
            case MotionEvent.ACTION_UP:
                mImgUpload.setImageBitmap(tempBit);  // image with bounding box
                break;
            default:
                mImgUpload.setImageBitmap(imageBit);
                break;
        }
        return true;
    }

    private void setImageBoundingBoxColor() {
        switch (Integer.parseInt(numOfClass)){
            case 0: //mexico Color
                inferenceImgPaint.setColor(Color.RED);                              //Red
                trPaint.setColor(Color.RED);
                break;
            case 1: //Polo Bun Color
                inferenceImgPaint.setColor(Color.parseColor("#ffa3d3"));    //pink
                trPaint.setColor(Color.parseColor("#ffa3d3"));
                break;
            case 2: //Chocolate Donut Color
                inferenceImgPaint.setColor(Color.parseColor("#ebae34"));    // light orange
                trPaint.setColor(Color.parseColor("#ebae34"));
                break;
            case 3: //Portugese tart Color
                inferenceImgPaint.setColor(Color.parseColor("#eb7a34"));    // dark orange
                trPaint.setColor(Color.parseColor("#eb7a34"));
                break;
        }
    }


    private void validateImageScale() {
        textSize = 0;
        if(tempBit.getWidth() > tempBit.getHeight()){        //enter this statement when images are landscape view
            textSize = 20;
            textPaint.setTextSize(textSize);
        }else if(tempBit.getWidth() < tempBit.getHeight()) { // enter this statement when images are portrait view
            textSize = 40;
            textPaint.setTextSize(textSize);
        }else {                                             // enter this statement when equal size of images
            textSize = 20;
            textPaint.setTextSize(textSize);
        }
    }

    private void measureText(String bread, String confidenceLevel, int size) {
        float textWidth = textPaint.measureText(bread +" "+ confidenceLevel +"%")/2;
        float text_center_x = Float.parseFloat(totalOfBreadQuantity.get(size).getxMin())-2;
        float text_center_y = Float.parseFloat(totalOfBreadQuantity.get(size).getxMax()) - textSize;
        textPaint.setTextAlign(Paint.Align.CENTER);
        tempCanvas.drawRect(text_center_x, text_center_y, text_center_x +2 * textWidth, text_center_y + textSize, trPaint);
        tempCanvas.drawText(bread +" "+ confidenceLevel +"%", text_center_x + textWidth, text_center_y + textSize, textPaint);
    }

    public ArrayList<InferenceDataset> loadResults(String result){
        Scanner scanner = new Scanner(result);
        StringBuilder builder = new StringBuilder();
        ArrayList<String> res = new ArrayList<String>();
        mBreadLists = new ArrayList<Bread>();
        while (scanner.hasNextLine()){
            String s = scanner.nextLine();
            res.addAll(Arrays.asList(s.replaceAll("[\\[\\]]", "").split(",")));
        }
        for (int i=0; i<res.size(); i++){
            builder.append(res.get(i)+"\n");
        }
        int loop = 0;
        totalOfBreadQuantity = new ArrayList<InferenceDataset>();
        for (String write : res){
            write = write.split(":")[1];
            loop %=7;
            switch (loop){
                case 0:{
                    xMin = write;
                }
                break;
                case 1:{
                    xMax = write;
                }
                break;
                case 2:{
                    yMin = write;
                }
                break;
                case 3:{
                    yMax = write;
                }
                break;
                case 4:{
                    confidenceLevel = write;
                }
                break;
                case 5:{
                    numOfClass = write;
                }
                break;
                case 6:{
                    nameOfBread = write;
                    StringBuilder data = new StringBuilder();
                    for (int x = 0; x<nameOfBread.length(); x++){
                        char c = nameOfBread.charAt(x);
                        if (c == '"' || c == '}' || c == ']') {
                            continue;
                        }
                        else
                            data.append(c);
                    }
                    nameOfBread = data.toString().toUpperCase();
                    calculateQuantityOfBread(nameOfBread);
                }
            }
            loop++;

            if (loop%7==0){
                totalOfBreadQuantity.add(new InferenceDataset(xMin, xMax, yMin, yMax, confidenceLevel, nameOfBread, numOfClass));
                numOfResult++;
            }
        }
        if (numOfResult>0){
            mTvTotalOfBreadDetected.setText(mTvTotalOfBreadDetected.getText().toString()+ totalOfBreadQuantity.size());
            String data = "";

            if (quantityOfMexico>0){
                data+="Mexico: "+ quantityOfMexico +"\n";
                mBreadLists.add(new Bread("Mexico", String.valueOf(quantityOfMexico), "2.00"));
            }
            if (quantityOfPoloBun>0){
                data+="Polo Bun: "+ quantityOfPoloBun +"\n";
                mBreadLists.add(new Bread("Polo Bun", String.valueOf(quantityOfPoloBun), "3.00"));
            }
            if (quantityOfChocDonut>0){
                data+="Chocolate Donut: "+ quantityOfChocDonut + "\n";
                mBreadLists.add(new Bread("Chocolate Donut", String.valueOf(quantityOfChocDonut), "1.80"));
            }
            if (quantityOfPortart>0){
                data+="Portugese Tart: "+ quantityOfPortart;
                mBreadLists.add(new Bread("Portugese Tart", String.valueOf(quantityOfPortart), "2.50"));
            }


            Gson gson = new Gson();
            String jsonArray = gson.toJson(mBreadLists);
            SharedPreferences.Editor editor = prf.edit();

            editor.putString("breadList", jsonArray);
            editor.commit();
            mTvResult.setText(data);
        }
        else{
            mTvResult.setText("PS: The confidence rate is too low to identify this bread...\n" +
                                "Please manually add in the new bread or scan again");
        }
        return totalOfBreadQuantity;
    }

    private void calculateQuantityOfBread(String nameOfBread) {

        String data = nameOfBread;

        switch(data){
            case "MEXICO":
                if (data.equals("MEXICO")){
                    quantityOfMexico++;
                }
                break;
            case "POLO BUN":
                if (data.equals("POLO BUN")){
                    quantityOfPoloBun++;
                }
                break;
            case "CHOCOLATE DONUT":
                if (data.equals("CHOCOLATE DONUT")){
                    quantityOfChocDonut++;
                }
                break;
            case "PORTUGESE TART":
                if (data.equals("PORTUGESE TART")){
                    quantityOfPortart++;
                }
        }
    }

    public String encodeToBase64(Bitmap bitmap) {
        ByteArrayOutputStream encode = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, encode);
        byte[] b = encode.toByteArray();
        String temp = Base64.encodeToString(b, Base64.DEFAULT);
        return temp;
    }

    public static Bitmap decodeToBase64(String input) {
        byte[] decodedByte = Base64.decode(input, 0);
        return BitmapFactory.decodeByteArray(decodedByte, 0, decodedByte.length);
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()){
            case R.id.ivProceedCheckOut:{
                Intent intent = new Intent (ViewInferenceResult.this, FragmentController.class);
                intent.putExtra("CallingActivity", ViewInferenceResult.ACTIVITY_1);

                SharedPreferences.Editor editor = prf.edit();
                String resultImage = encodeToBase64(imageBit);
                String inferenceImage = encodeToBase64(tempBit);

                convertBitmapToUri(imageBit);
                imgUri = Uri.fromFile(imageFile);
                if (imgUri!=null){
                    editor.putString("imageUri", String.valueOf(imgUri));
                }

                convertBitmapToUri(tempBit);
                imgResultUri = Uri.fromFile(imageFile);
                if (imgResultUri!=null){
                    editor.putString("imageResultUri", String.valueOf(imgResultUri));
                }

                editor.putString("imgResultInference", resultImage);
                editor.putString("imgInference", inferenceImage);
                editor.putBoolean("proceedCheckOut", true);
                editor.commit();
                startActivity(intent);
            }
        }

    }
}