package com.example.realtimemonitoring;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

public class EngineerActivity extends AppCompatActivity implements MainActivity_insertstatus.AsyncResponse{
    //region var declaration
    Activity activity;
    Context context;
    IntentIntegrator intentIntegrator;
    private TextView tv_hour, tv_minute, tv_second;
    CountDownTimer cTimer = null;
    String scannedString, db_name, curr_stats, model, lot_no, engr, remarks, operator, ServerIP, quantity;
    String[] splittedString;
    Button btnmachinesetup, lottravelnotavail, btnmanualencode;
    EditText textmodelname;
    TextView note;
    int ctrl;
    Boolean notavail;
    //endregion
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_engineer);

        //region get extras
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        db_name = extras.getString("extra_db_name");
        curr_stats = extras.getString("extra_current_ctatus");
        model = extras.getString("extra_model");
        lot_no = extras.getString("extra_lot_no");
        engr = extras.getString("extra_engr");
        operator = extras.getString("extra_optr");
        ServerIP = extras.getString("extra_serverip");
        quantity = extras.getString("extra_quantity");
        //endregion
        //region findviewbyid
        btnmachinesetup = findViewById(R.id.btnmachinesetup);
        tv_hour = findViewById(R.id.tv_hour);
        tv_minute = findViewById(R.id.tv_minute);
        tv_second = findViewById(R.id.tv_second);
        activity = (Activity) context;
        lottravelnotavail = findViewById(R.id.lottravelnotavail);
        btnmanualencode = findViewById(R.id.btnmanualencode);
        textmodelname = findViewById(R.id.textmodelname);
        note = findViewById(R.id.note);
        //endregion
        //region startup code to run
        notavail = false;
        startTimer(60);
        btnmachinesetup.setEnabled(false);
        //endregion
    }

    //region countdowntimer
    public void startTimer(int min){
        cTimer = new CountDownTimer((1000*60)*min, 1000) {
            @SuppressLint("SetTextI18n")
            @Override
            public void onTick(long millisUntilFinished) {
                tv_hour.setText(""+(millisUntilFinished/(1000*60*60))%24);
                tv_minute.setText(""+(millisUntilFinished/(1000*60))%60) ;
                tv_second.setText(""+(millisUntilFinished/1000)%60);
            }
            @Override
            public void onFinish() {
                try {
                    cancelTimer();
                    MainActivity.resetBluetoothConnection();
                    remarks = "Time is Up";
                    curr_stats = "Setup time expired";
                    main_insertstatus();
                    Toast.makeText(EngineerActivity.this, "Timer's up", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(EngineerActivity.this, MainActivity.class);
                    Bundle extras = new Bundle();
                    extras.putString("extra_db_name", db_name);
                    extras.putString("extra_current_ctatus", curr_stats);
                    extras.putString("extra_model", model);
                    extras.putString("extra_lot_no", lot_no);
                    extras.putString("extra_optr", operator);
                    extras.putString("FLAG", "true");
                    intent.putExtras(extras);
                    startActivity(intent);
                } catch (Exception e) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(EngineerActivity.this);
                    builder.setTitle("Time's up");
                    builder.setMessage(e.getMessage());
                    builder.show();
                }
            }
        };
        cTimer.start();
    }
    public void cancelTimer() {
        if(cTimer!=null)
            cTimer.cancel();
    }

    public void add30Mins(View view) {
        intentIntegrator = new IntentIntegrator(this);
        intentIntegrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES);
        intentIntegrator.setPrompt("PLEASE SCAN A PIC BARCODE ID");
        intentIntegrator.initiateScan();
    }


    //endregion2

    //region Buttons Onclick


    public void machinesetup(View view) {
        if(!notavail){
            backtomain();
        }else{
            activity = (Activity) context;
            intentIntegrator = new IntentIntegrator(this);
            //arrays.asList
            //Collections.singletonList(IntentIntegrator.QR_CODE
            intentIntegrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES);
            intentIntegrator.setPrompt("Please scan your Barcode ID");
            intentIntegrator.initiateScan();
        }


    }

    public void sendstatus(View view) {
        try{
            curr_stats = "On-Going Setup";
            model = textmodelname.getText().toString();
            main_insertstatus();
            ctrl = 0;
            textmodelname.setVisibility(View.GONE);
            btnmanualencode.setVisibility(View.GONE);
            note.setVisibility(View.GONE);
            btnmachinesetup.setEnabled(true);
            notavail = true;
        }catch(Exception e){
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    //endregion

    //region AsyncTask
    public void main_insertstatus(){
        try{
            if (!db_name.isEmpty() && !curr_stats.isEmpty() && engr != null) {
                if (model == null){
                    model = " ";
                    lot_no = " ";
                    quantity = "0";
                }
                if(lot_no == null){
                    lot_no = " ";
                }
                if (remarks == null || remarks.equals("")){
                    remarks = " ";
                }
                if (quantity == null || quantity.equals("")){
                    quantity = "0";
                }
                MainActivity_insertstatus main_insertstatus = new MainActivity_insertstatus(this);
                main_insertstatus.execute(db_name, curr_stats, model, lot_no, engr, remarks, ServerIP, quantity);
                main_insertstatus.delegate = this;
            }else{
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                if (engr == null){
                    builder.setTitle("Please scan your barcode ID");
                    builder.setMessage("Operator name not found");
                    builder.setNegativeButton("OK", (dialog, which) -> dialog.dismiss());
                }else {
                    builder.setTitle("Error Encountered");
                    builder.setMessage("Please contact your PIC");
                    builder.setNegativeButton("OK", (dialog, which) -> dialog.dismiss());
                }
                builder.show();
            }
        }catch (Exception e){
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void MainActivity_insertstatus_processFinish(String output) {
        if(output != null){
            try{
                Toast.makeText(this, output, Toast.LENGTH_LONG).show();

            }catch (Exception e){
                e.printStackTrace();
                AlertDialog.Builder builder = new AlertDialog.Builder(EngineerActivity.this);
                builder.setTitle("Sending Status Error");
                builder.setMessage(e.getMessage());
                builder.show();
            }
        }
    }


    //endregion

    //region qrscanner
    public void scanlot(View view) {
        activity = (Activity) context;
        intentIntegrator = new IntentIntegrator(this);
        //arrays.asList
        //Collections.singletonList(IntentIntegrator.QR_CODE
        intentIntegrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES);
        intentIntegrator.setPrompt("Scan your Lot Traveller");
        intentIntegrator.initiateScan();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //region qrscan
        IntentResult result = IntentIntegrator.parseActivityResult(resultCode, data);
        scannedString = result.getContents();
        splittedString = scannedString.split(";");
        if (splittedString[0].equals("01") && !notavail) {
            if (Integer.parseInt(tv_hour.getText().toString()) != 0){
                int h = Integer.parseInt(tv_hour.getText().toString());
                int m = Integer.parseInt(tv_minute.getText().toString());
                if (h == 1){cancelTimer();startTimer(60+m+31);}
                else if (h == 2){cancelTimer();startTimer(120+m+31);}
                else if (h == 3){cancelTimer();startTimer(180+m+31);}
            }else {
                int m = Integer.parseInt(tv_minute.getText().toString());
                cancelTimer();
                startTimer(m + 31);
            }
        }else if(splittedString[1].matches(".*\\d.*") && splittedString[2].matches(".*\\d.*")){
            model = splittedString[1];
            lot_no = splittedString[2];
            curr_stats = "On-Going Setup";
            try {
                main_insertstatus();
            } catch (Exception e) {
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
            }
            btnmachinesetup.setEnabled(true);
        }else if(splittedString[0].equals("01") && notavail){
            backtomain();
        }else {
            Toast.makeText(this, "Please scan a correct barcode",Toast.LENGTH_LONG).show();
        }
        //endregion
    }
    //endregion

    @SuppressLint("SetTextI18n")
    public void lottravelnotavail(View view) {
        if(ctrl == 1){
            ctrl = 0;
            textmodelname.setVisibility(View.GONE);
            btnmanualencode.setVisibility(View.GONE);
            note.setVisibility(View.GONE);
        }else{
            ctrl = 1;
            textmodelname.setVisibility(View.VISIBLE);
            btnmanualencode.setVisibility(View.VISIBLE);
            note.setVisibility(View.VISIBLE);
        }
    }

    public void backtomain(){
        try {
            //MainActivity.resetBluetoothConnection();
            cancelTimer();
            curr_stats = "Setup Done";
            Thread.sleep(3000);
            main_insertstatus();
            /*Intent intent = new Intent(this, MainActivity.class);
            Bundle extras = new Bundle();
            extras.putString("extra_db_name", db_name);
            extras.putString("extra_current_ctatus", curr_stats);
            extras.putString("extra_model", model);
            if (lot_no != null){extras.putString("extra_lot_no", lot_no);}else{extras.putString("extra_lot_no", " ");}
            extras.putString("extra_optr", operator);
            extras.putString("FLAG", "true");
            intent.putExtras(extras);
            startActivity(intent);*/
            this.finish();
        } catch (Exception e) {
            Toast.makeText(this, "Setup Done Button Error. Please Contact PIC", Toast.LENGTH_LONG).show();
        }
    }

}