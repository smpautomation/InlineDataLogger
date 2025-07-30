package com.example.realtimemonitoring;


import static android.content.ContentValues.TAG;
import static com.example.realtimemonitoring.R.drawable.design_currentproc;
import static com.example.realtimemonitoring.R.drawable.design_doneproc;
import static com.example.realtimemonitoring.R.drawable.design_notdoneproc;
import static com.example.realtimemonitoring.R.drawable.design_pflow;
import static com.example.realtimemonitoring.R.drawable.round_button;
import static com.example.realtimemonitoring.R.drawable.round_button_all;
import static com.example.realtimemonitoring.R.drawable.round_button_green;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.bluetooth.le.ScanResult;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.text.InputFilter;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;


import android.util.Log;
import android.view.View;


import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;


import java.io.IOException;
import java.io.InputStream;
import java.util.Set;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.BluetoothStatusCodes;
import android.content.BroadcastReceiver;


public class MainActivity extends AppCompatActivity implements MainActivity_getmachinedata.AsyncResponse, MainActivity_insertstatus.AsyncResponse, MainActivity_machineinsertstatus.AsyncResponse, MainActivity_getprocessflow.AsyncResponse, MainActivity_getcurrentprocess.AsyncResponse, MainActivity_getLotDetails.AsyncResponse, MainActivity_getLastLotData.AsyncResponse{
    //region variable declarations
    Button btnOP, btnAdjustment, btnChangeLot, btnNoWIP, btnBreaktime, btnCalib, btnUndereepm, btnStandby, btnSetup, btnMachineStop, btnmanualenc, btnOPProto, btnOthers;
    ImageButton qrButton;
    IntentIntegrator intentIntegrator;
    Activity activity;
    Context context;
    String scannedString, ip_add, db_name, machine_name, location, operator, model, lot_no, curr_stats, remarks, bluetoothname, arduinotext, routingcode, pflowcode, currentprocess, quantity, workorderID;
    String[] splittedString;
    String[] pflow;
    String[] curprocarr;
    TextView txtOperator, txtLotNo, txtModel, txtCurrStats, txtMachineName, txtLocation, txtIPAdd, txtRemarks, tv, txtCharger;
    JSONObject jsonObject;
    JSONArray jsonArray;
    NestedScrollView constraintLayout;
    View mDecorView;
    //Thread thread;
    //Handler handler;
    MediaPlayer mp;
    CountDownTimer cTimer = null;
    CountDownTimer ccTimer = null;
    Boolean bop = false, bmerr = false, bchlt = false, bnw = false, bbt = false, bms = false, bueepm = false, bst = false, bmain = false, bmf = false, bopp = false, bothers = false;
    Boolean[] bolarray = {bop, bmerr, bchlt, bnw, bbt, bms, bueepm, bst, bmain, bmf, bopp, bothers};
    Intent intent;
    TableRow row1pflow, row2pflow, row3pflow, row4pflow, row5pflow, bufferrow;
    Button p1, p2, p3, p4, p5, p6, p7, p8, p9, p10, p11, p12, p13, p14, p15, p16, p17, p18, p19, p0;
    Button[] buttonArray;
    TableRow[] row;
    String ServerIP = "";
    Boolean isconnected = false;
    String flag=" ";
    Boolean adminmode=false;
    Boolean bol_manualenc = false;
    Boolean bypassed=false;
    TextView remindtext;
    //ProgressDialog progressdialog;
    //endregion

    //region bluetooth var
    BluetoothAdapter mBluetoothAdapter;
    static BluetoothSocket mmSocket;
    BluetoothDevice mmDevice;
    static OutputStream mmOutputStream;
    static InputStream mmInputStream;
    Thread workerThread;
    byte[] readBuffer;
    int readBufferPosition;
    //int counter;
    volatile boolean stopWorker;
    //endregion

    private static final String ADMIN_PASSWORD = "loyalblood"; // Change this
    private boolean isKioskModeEnabled = true;

    @SuppressLint({"SetTextI18n", "NewApi"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},1);
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        AlertDialog.Builder builder2 = new AlertDialog.Builder(MainActivity.this);

        enableKioskMode();
        setupPasswordDialog();

        //region read textfile for server ip
        StringBuilder myData = new StringBuilder();
        File myExternalFile = new File(Environment.getExternalStorageDirectory(),"server.txt");
        try {
            FileInputStream fis = new FileInputStream(myExternalFile);
            DataInputStream in = new DataInputStream(fis);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));

            String strLine;
            while ((strLine = br.readLine()) != null) {
                myData.append(strLine).append("\n");
            }
            br.close();
            in.close();
            fis.close();
        } catch (IOException e) {
            e.printStackTrace();
            alertdialogset("Error reading server address :: " + e.getMessage());
        } finally {
            String[] keyval, servip, bypsd;
            keyval = myData.toString().split("\n");
            servip = keyval[0].split(": ");
            bypsd = keyval[1].split(": ");
            ServerIP = servip[1].trim();
            bypassed = bypsd[1].trim().equals("true");
            //alertdialogset(ServerIP + " + " + bypassed);
        }

        //endregion

        //region findViewById
        txtOperator = findViewById(R.id.txtOperator);
        txtLotNo = findViewById(R.id.txtLotNo);
        txtModel = findViewById(R.id.txtModel);
        txtCurrStats = findViewById(R.id.txtCurStat);
        txtMachineName = findViewById(R.id.txtMachineName);
        txtLocation = findViewById(R.id.txtLocation);
        txtIPAdd = findViewById(R.id.txtIPAdd);
        txtRemarks = findViewById(R.id.txtRemarks);
        txtCharger = findViewById(R.id.ChargerText);
        btnOP = findViewById(R.id.btn_operation);
        btnAdjustment = findViewById(R.id.btn_adjustment);
        btnOPProto = findViewById(R.id.btn_operationproto);
        btnChangeLot = findViewById(R.id.btn_changelot);
        btnNoWIP = findViewById(R.id.btn_nowip);
        btnBreaktime = findViewById(R.id.btn_Breaktime);
        btnCalib = findViewById(R.id.btn_calib);
        btnUndereepm = findViewById(R.id.btn_undereepm);
        btnStandby = findViewById(R.id.btn_Standby);
        btnSetup = findViewById(R.id.btn_Setup);
        btnMachineStop = findViewById(R.id.btn_machinestop);
        btnmanualenc = findViewById(R.id.btn_manualencode);
        btnOthers = findViewById(R.id.btn_others);
        constraintLayout = findViewById(R.id.constraintlayout);
        tv = findViewById(R.id.tv);
        row1pflow = findViewById(R.id.row1pflow);
        row2pflow = findViewById(R.id.row2pflow);
        row3pflow = findViewById(R.id.row3pflow);
        row4pflow = findViewById(R.id.row4pflow);
        row5pflow = findViewById(R.id.row5pflow);
        bufferrow = findViewById(R.id.bufferrow);
        p1 = findViewById(R.id.p1);
        p2 = findViewById(R.id.p2);
        p3 = findViewById(R.id.p3);
        p4 = findViewById(R.id.p4);
        p5 = findViewById(R.id.p5);
        p6 = findViewById(R.id.p6);
        p7 = findViewById(R.id.p7);
        p8 = findViewById(R.id.p8);
        p9 = findViewById(R.id.p9);
        p10 = findViewById(R.id.p10);
        p11 = findViewById(R.id.p11);
        p12 = findViewById(R.id.p12);
        p13 = findViewById(R.id.p13);
        p14 = findViewById(R.id.p14);
        p15 = findViewById(R.id.p15);
        p16 = findViewById(R.id.p16);
        p17 = findViewById(R.id.p17);
        p18 = findViewById(R.id.p18);
        p19 = findViewById(R.id.p19);
        p0 = findViewById(R.id.p0);
        remindtext = findViewById(R.id.remindtxt);
        //endregion findViewById6

        //region Button On Long Click Listener
        btnmanualenc.setOnLongClickListener(v -> {
            try {
                builder.setTitle("Enter Work Order ID");
                // Set up the input
                final EditText input = new EditText(this);
                // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
                //input.setInputType(InputType.TYPE_CLASS_TEXT);
                input.setFilters(new InputFilter[]{new InputFilter.AllCaps()});
                builder.setView(input);

                // Set up the buttons
                builder.setPositiveButton("OK", (dialog, which) -> {
                    workorderID = input.getText().toString();
                    if (main_getLotDetails()) {
                        bol_manualenc = true;
                    }
                });
                builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
                builder.show();
            } catch (Exception e) {
                alertdialogset(e.getMessage());
            }
            return true;
        });
        btnOP.setOnLongClickListener(v -> {
            cancelTimerwaitforStop();
            cancelTimer();
            try {
                if (model == null || txtModel.getText() == null || txtModel.getText() == " " || model.equals(" ")) {
                    builder.setTitle("Model Not Found.");
                    builder.setMessage("Please scan your lot traveller/denpyo.");
                    builder.show();
                } else {
                    curr_stats = "Operation MASSPRO";
                    if (main_insertstatus()) { //TODO disabled temp for debug
                        txtCurrStats.setText(curr_stats);
                        constraintLayout.setBackgroundColor(Color.argb(100, 76, 175, 80));
                        qrButton.setEnabled(false);
                        remindtext.setVisibility(View.VISIBLE);
                        btnmanualenc.setEnabled(false);

                        btnOP.setBackground(ContextCompat.getDrawable(context, round_button_green));
                        bop = true;
                        resetbtnColor();

                        //region bluetooth2
                        try{
                            sendData("11"); //ON
                            Toast.makeText(this, "data sent(on)", Toast.LENGTH_SHORT).show();
                        }catch(Exception e){
                            Toast.makeText(this, ":::ERROR SENDING 'ON' TO BLUETOOTH:::", Toast.LENGTH_LONG).show();
                        }
                        //endregion

                    }

                    sendData("13");// check patlite

                }
            } catch (Exception e) {
                alertdialogset("OPERATION MASSPRO BUTTON ERROR :::: " + e.getMessage());
            }
            btnMachineStop.setEnabled(true);
            return true;
        });
        btnAdjustment.setOnLongClickListener(v -> {
            /*curr_stats = "Adjustment";
            txtRemarks.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }

                @Override
                public void afterTextChanged(Editable s) {
                    cancelTimerwaitforStop();
                    cancelTimer();
                    txtCurrStats.setText(curr_stats);
                    remarks = "";
                    btnMachineErr.setBackground(ContextCompat.getDrawable(context, round_button));
                    bmerr = true;
                    resetbtnColor();
                }
            });
            btnMachineStop.setEnabled(true);
            alertdialogremarksinput();*/

            curr_stats = "Adjustment";
            txtCurrStats.setText(curr_stats);
            cancelTimerwaitforStop();
            cancelTimer();
            //constraintLayout.setBackgroundColor(Color.argb(100, 255, 255, 255));
            btnAdjustment.setBackground(ContextCompat.getDrawable(context, round_button_green));
            bmerr = true;
            resetbtnColor();
            btnMachineStop.setEnabled(true);
            main_insertstatus();
            try{
                sendData("13");// check patlite
            }catch (Exception ex){
                Toast.makeText(this, "Check Patlite Error: " + ex.getMessage(), Toast.LENGTH_SHORT).show();
            }
            return true;
        });
        btnChangeLot.setOnLongClickListener(v -> {
            btnMachineStop.setEnabled(true);
            builder2.setMessage("Last Lot Done?");
            builder2.setPositiveButton("YES", (dialog, which) -> {
                cancelTimerwaitforStop();
                cancelTimer();
                curr_stats = "FLUSH OUT";
                if (main_insertstatus()) {
                    txtCurrStats.setText(curr_stats);
                    txtModel.setText("");
                    model = " ";
                    txtLotNo.setText("");
                    lot_no = " ";
                    txtOperator.setText("");
                    operator = null;
                    txtRemarks.setText("");
                    remarks = null;
                    quantity = "0";
                    btnChangeLot.setBackground(ContextCompat.getDrawable(context, round_button_green));
                    bchlt = true;

                    resetbtnColor();
                    row1pflow.setVisibility(View.GONE);
                    row2pflow.setVisibility(View.GONE);
                    row3pflow.setVisibility(View.GONE);
                    row4pflow.setVisibility(View.GONE);
                    row5pflow.setVisibility(View.GONE);
                    qrButton.setEnabled(true);
                    remindtext.setVisibility(View.GONE);
                    btnmanualenc.setEnabled(true);
                }
                try{
                    sendData("13");// check patlite
                }catch (Exception ex){
                    Toast.makeText(this, "Check Patlite Error: " + ex.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
            builder2.setNegativeButton("NO", (dialog, which) -> dialog.dismiss());
            builder2.show();

            return true;
        });
        btnNoWIP.setOnLongClickListener(v -> {
            curr_stats = "Waiting Raw Materials";
            txtCurrStats.setText(curr_stats);
            cancelTimerwaitforStop();
            cancelTimer();
            //constraintLayout.setBackgroundColor(Color.argb(100, 255, 255, 255));
            btnNoWIP.setBackground(ContextCompat.getDrawable(context, round_button_green));
            bnw = true;
            resetbtnColor();
            btnMachineStop.setEnabled(true);
            main_insertstatus();
            try{
                sendData("13");// check patlite
            }catch (Exception ex){
                Toast.makeText(this, "Check Patlite Error: " + ex.getMessage(), Toast.LENGTH_SHORT).show();
            }
            return true;
        });
        btnBreaktime.setOnLongClickListener(v -> {
            curr_stats = "Breaktime";
            txtCurrStats.setText(curr_stats);
            cancelTimerwaitforStop();
            cancelTimer();
            //constraintLayout.setBackgroundColor(Color.argb(100, 255, 255, 255));
            btnBreaktime.setBackground(ContextCompat.getDrawable(context, round_button_green));
            bbt = true;
            resetbtnColor();
            btnMachineStop.setEnabled(true);
            main_insertstatus();
            try{
                sendData("13");// check patlite
            }catch (Exception ex){
                Toast.makeText(this, "Check Patlite Error: " + ex.getMessage(), Toast.LENGTH_SHORT).show();
            }
            return true;
        });
        btnMachineStop.setOnLongClickListener(v -> {

            if (!bms) {
                curr_stats = "Machine Stop";
                if(main_insertstatus()){ //TODO disabled temp for debug
                    txtCurrStats.setText(curr_stats);
                    constraintLayout.setBackgroundColor(getResources().getColor(R.color.redish));
                    btnMachineStop.setBackground(ContextCompat.getDrawable(context, round_button));
                    waitforStop(15);
                    resetbtnColor();

                    //region bluetooth2
                    try {
                        sendData("10"); //OFF
                        Toast.makeText(this, "data sent(off)", Toast.LENGTH_SHORT).show();
                    } catch (IOException e) {
                        builder.setTitle(":::ERROR:::");
                        builder.setMessage(e.getMessage());
                        builder.show();
                    }
                    btnMachineStop.setEnabled(false);
                    //endregion
                }
            }
            try{
                sendData("13");// check patlite
            }catch (Exception ex){
                Toast.makeText(this, "Check Patlite Error: " + ex.getMessage(), Toast.LENGTH_SHORT).show();
            }

            //region bluetooth
            /*try {
                mBTSocket.getOutputStream().write(off.getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }*/
            //endregion
            return true;
        });
        btnUndereepm.setOnLongClickListener(v -> {
            curr_stats = "Under EE/PM";
            txtCurrStats.setText(curr_stats);
            cancelTimerwaitforStop();
            cancelTimer();
            //constraintLayout.setBackgroundColor(Color.argb(100, 255, 255, 255));
            btnUndereepm.setBackground(ContextCompat.getDrawable(context, round_button_green));
            bueepm = true;
            resetbtnColor();
            btnMachineStop.setEnabled(true);
            main_insertstatus();
            try{
                sendData("13");// check patlite
            }catch (Exception ex){
                Toast.makeText(this, "Check Patlite Error: " + ex.getMessage(), Toast.LENGTH_SHORT).show();
            }
            return true;
        });
        btnStandby.setOnLongClickListener(v -> {
            curr_stats = "Standby";
            txtCurrStats.setText(curr_stats);
            cancelTimerwaitforStop();
            cancelTimer();
            //constraintLayout.setBackgroundColor(Color.argb(100, 255, 255, 255));

            btnStandby.setBackground(ContextCompat.getDrawable(context, round_button_green));
            bst = true;
            resetbtnColor();
            /*alertdialogremarksinput();
            txtRemarks.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }
                @Override
                public void afterTextChanged(Editable s) {
                    txtCurrStats.setText(curr_stats);
                    remarks = "";
                    constraintLayout.setBackgroundColor(Color.argb(100, 255, 255, 255));
                }
            });*/
            btnMachineStop.setEnabled(true);
            main_insertstatus();
            try{
                sendData("13");// check patlite
            }catch (Exception ex){
                Toast.makeText(this, "Check Patlite Error: " + ex.getMessage(), Toast.LENGTH_SHORT).show();
            }
            return true;
        });
        btnSetup.setOnLongClickListener(v -> {
            curr_stats = "Machine Setup";
            txtCurrStats.setText(curr_stats);
            //constraintLayout.setBackgroundColor(Color.argb(100, 255, 255, 255));
            cancelTimerwaitforStop();
            cancelTimer();

            btnSetup.setBackground(ContextCompat.getDrawable(context, round_button_green));
            bmain = true;
            resetbtnColor();
            btnMachineStop.setEnabled(true);
            main_insertstatus();
            try{
                sendData("13");// check patlite
            }catch (Exception ex){
                Toast.makeText(this, "Check Patlite Error: " + ex.getMessage(), Toast.LENGTH_SHORT).show();
            }
            return true;
        });
        btnCalib.setOnLongClickListener(v -> {
            /*curr_stats = "Machine Fixed";
            txtRemarks.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }

                @Override
                public void afterTextChanged(Editable s) {
                    txtCurrStats.setText(curr_stats);
                    //constraintLayout.setBackgroundColor(Color.argb(100, 255, 255, 255));
                    cancelTimerwaitforStop();
                    cancelTimer();
                    remarks = "";
                    btnMachineFix.setBackground(ContextCompat.getDrawable(context, round_button_green));
                    bmf = true;
                    resetbtnColor();
                    btnMachineStop.setEnabled(true);
                }
            });
            alertdialogremarksinput();*/

            curr_stats = "Calibration";
            txtCurrStats.setText(curr_stats);
            cancelTimerwaitforStop();
            cancelTimer();
            //constraintLayout.setBackgroundColor(Color.argb(100, 255, 255, 255));
            btnCalib.setBackground(ContextCompat.getDrawable(context, round_button_green));
            bmf = true;
            resetbtnColor();
            btnMachineStop.setEnabled(true);
            main_insertstatus();
            try{
                sendData("13");// check patlite
            }catch (Exception ex){
                Toast.makeText(this, "Check Patlite Error: " + ex.getMessage(), Toast.LENGTH_SHORT).show();
            }
            return true;
        });
        btnOPProto.setOnLongClickListener(v -> {
            cancelTimerwaitforStop();
            cancelTimer();
            try {
                curr_stats = "Operation PROTO";
                if (main_insertstatus()) { //TODO disabled temp for debug
                    txtCurrStats.setText(curr_stats);
                    constraintLayout.setBackgroundColor(Color.argb(100, 76, 175, 80));
                    qrButton.setEnabled(false);
                    remindtext.setVisibility(View.VISIBLE);
                    btnmanualenc.setEnabled(false);

                    btnOPProto.setBackground(ContextCompat.getDrawable(context, round_button_green));
                    bopp = true;
                    resetbtnColor();

                    //region bluetooth2
                    try{
                        sendData("11"); //ON
                    }catch(Exception e){
                        builder.setTitle(":::ERROR:::");
                        builder.setMessage(e.getMessage());
                        builder.show();
                    }
                    //endregion
                }
            } catch (Exception e) {
                builder.setTitle(":::ERROR:::");
                builder.setMessage(e.getMessage());
                builder.show();
            }
            try{
                sendData("13");// check patlite
            }catch (Exception ex){
                Toast.makeText(this, "Check Patlite Error: " + ex.getMessage(), Toast.LENGTH_SHORT).show();
            }
            btnMachineStop.setEnabled(true);
            return true;
        });
        btnOthers.setOnLongClickListener(v -> {
            if(operator != null) {
                Intent intent = new Intent(this, OthersActivity.class);
                Bundle extr = new Bundle();
                extr.putString("extra_db_name", db_name);
                extr.putString("extra_model", model);
                extr.putString("extra_lot_no", lot_no);
                extr.putString("extra_quantity", quantity);
                extr.putString("extra_optr", operator);
                extr.putString("extra_serverip", ServerIP);
                intent.putExtras(extr);
                startActivity(intent);
                curr_stats = "Others";
                txtCurrStats.setText(curr_stats); //TODO Disabled to remove redundancy ~enabled sept 23, not yet built.
                btnOthers.setBackground(ContextCompat.getDrawable(context, round_button_green));
                bothers = true;
                resetbtnColor();
            }else{
                alertdialogset("Please scan operator id first");
            }
            try{
                sendData("13");// check patlite
            }catch (Exception ex){
                Toast.makeText(this, "Check Patlite Error: " + ex.getMessage(), Toast.LENGTH_SHORT).show();
            }
            return true;
        });
        //endregion

        //region qr scanner
        activity = (Activity) context;
        qrButton = findViewById(R.id.qr_button);
        intentIntegrator = new IntentIntegrator(this);
        qrButton.setOnLongClickListener(v -> {
            //arrays.asList
            //Collections.singletonList(IntentIntegrator.QR_CODE
            intentIntegrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES);
            intentIntegrator.setPrompt("Scan your Denpyo/ID Barcode");
            intentIntegrator.setTorchEnabled(true);
            intentIntegrator.initiateScan();
            return true;
        });
        //endregion qr scanner

        //region startup code to run
        buttonArray = new Button[]{p0, p1, p2, p3, p4, p5, p6, p7, p8, p9, p10, p11, p12, p13, p14, p15, p16, p17, p18, p19};
        row = new TableRow[]{row1pflow, row2pflow, row3pflow, row4pflow, row5pflow, bufferrow};
        constraintLayout.setBackgroundColor(getResources().getColor(R.color.white));
        context = getApplicationContext();
        ip_add = getLocalIpAddress();
        if (ip_add == null) {
            alertdialogset("NO IP ADDRESS DETECTED. MAKE SURE YOU ARE CONNECTED TO THE NETWORK");
            txtIPAdd.setText("NO IP ADDRESS");
        }
        main_getmachinedata();    //TODO disable temp for debug build

        try {
            intent = getIntent();
            Bundle extras = intent.getExtras();
            if (extras != null) {
                db_name = extras.getString("extra_db_name");
                curr_stats = extras.getString("extra_current_ctatus");
                model = extras.getString("extra_model");
                lot_no = extras.getString("extra_lot_no");
                operator = extras.getString("extra_optr");
                flag = extras.getString("FLAG");
                txtCurrStats.setText(curr_stats);
                //txtModel.setText(model);
                //txtLotNo.setText(lot_no);
                txtOperator.setText(operator);
                bluetoothname = extras.getString("extra_btname");
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, ":::: " + e.getMessage());
            alertdialogset("::: startupcode :::" + e.getMessage()); //TODO disable temp for debug build
        }

        mDecorView = getWindow().getDecorView();
        //hideSystemUI();

        mp = MediaPlayer.create(this, R.raw.buzzer);

        //waitforinit(); TODO replaced by thread

        try {
            blueAsyncTask blue = new blueAsyncTask(MainActivity.this);
            blue.execute();

            IntentFilter filter = new IntentFilter();
            filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
            filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED);
            filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
            this.registerReceiver(mReceiver, filter);
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        }


        /*if(flag.equals("true")){ //TODO enable if will provide time for disconnecting bluetooth.
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                alertdialogset(e.getMessage());
            }
        }*/


        //TODO check battery percentage.
        try {
            final Handler handler = new Handler();
            final int delay = 10000; // 1000 milliseconds == 1 second

            handler.postDelayed(new Runnable() {
                public void run() {
                    batt_check(); // Do your work here
                    handler.postDelayed(this, delay);
                }
            }, delay);
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        }

        //TODO check patlite and sendData.
        try {
            final Handler handler = new Handler();
            final int delay = 420000; // 1000 milliseconds == 1 second || SET TO 7 mins

            handler.postDelayed(new Runnable() {
                public void run() {
                    try {
                        sendData("13");// Do your work here
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    handler.postDelayed(this, delay);
                }
            }, delay);
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        }

        //endregion

    }

    //region kiosk mode
    private void enableKioskMode() {
        // Hide status bar and navigation bar
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_FULLSCREEN |
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY |
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        );

        // Keep screen on
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    public void onBackPressed() {
        if (isKioskModeEnabled) {
            // Don't allow back button to exit
            return;
        }
        super.onBackPressed();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isKioskModeEnabled) {
            // Immediately return to foreground
            ActivityManager activityManager = (ActivityManager) getApplicationContext()
                    .getSystemService(Context.ACTIVITY_SERVICE);

            activityManager.moveTaskToFront(getTaskId(), 0);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (isKioskModeEnabled) {
            // Disable home button, recent apps, etc.
            if (keyCode == KeyEvent.KEYCODE_HOME ||
                    keyCode == KeyEvent.KEYCODE_MENU ||
                    keyCode == KeyEvent.KEYCODE_SEARCH ||
                    keyCode == KeyEvent.KEYCODE_BACK) {
                return true;
            }
        }

        // Handle recent apps button for newer Android versions
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            if (keyCode == 187) { // KEYCODE_APP_SWITCH (recent apps)
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    private void setupPasswordDialog() {
        // Add a hidden button or gesture to trigger password dialog
        // For example, long press on a specific area
        View triggerView = findViewById(R.id.hidden_trigger); // Add this view to your layout
        if (triggerView != null) {
            triggerView.setOnLongClickListener(v -> {
                showPasswordDialog();
                return true;
            });
        }
    }

    private void showPasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Exit Kiosk Mode");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        input.setHint("Enter admin password");
        builder.setView(input);

        builder.setPositiveButton("OK", (dialog, which) -> {
            String password = input.getText().toString();
            if (ADMIN_PASSWORD.equals(password)) {
                disableKioskMode();
            } else {
                Toast.makeText(this, "Incorrect password", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        AlertDialog dialog = builder.create();
        dialog.setCancelable(false);
        dialog.show();
    }

    private void disableKioskMode() {
        isKioskModeEnabled = false;

        // Restore normal UI visibility
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);

        // Remove keep screen on flag
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        Toast.makeText(this, "Kiosk mode disabled", Toast.LENGTH_SHORT).show();

        // Optionally finish the activity or navigate elsewhere
        // finish();
    }

    // Override to prevent app from being killed
    @Override
    protected void onStop() {
        super.onStop();
        if (isKioskModeEnabled) {
            // Restart the activity if it's being stopped
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (isKioskModeEnabled && hasFocus) {
            enableKioskMode();
        }
    }
//endregion

    //region bluetooth
    void beginListenForData() {
        try {
            final Handler handler = new Handler();
            final byte delimiter = 10; //This is the ASCII code for a newline character

            stopWorker = false;
            readBufferPosition = 0;
            readBuffer = new byte[1024];
            workerThread = new Thread(() -> {
                while (!Thread.currentThread().isInterrupted() && !stopWorker) {
                    try {
                        int bytesAvailable = mmInputStream.available();
                        if (bytesAvailable > 0) {
                            byte[] packetBytes = new byte[bytesAvailable];
                            mmInputStream.read(packetBytes);
                            for (int i = 0; i < bytesAvailable; i++) {
                                byte b = packetBytes[i];
                                if (b == delimiter) {
                                    byte[] encodedBytes = new byte[readBufferPosition];
                                    System.arraycopy(readBuffer, 0, encodedBytes, 0, encodedBytes.length);
                                    final String data = new String(encodedBytes, StandardCharsets.US_ASCII);
                                    readBufferPosition = 0;

                                    handler.post(() -> {
                                        tv.setText(data);
                                        arduinotext = data;
                                        processarduinotext();
                                    });
                                } else {
                                    readBuffer[readBufferPosition++] = b;
                                }
                            }
                        }
                    } catch (IOException ex) {
                        stopWorker = true;
                    }
                }
            });

            workerThread.start();
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @SuppressLint("SetTextI18n")
    void sendData(String msg) throws IOException {
        msg += "\n";
        mmOutputStream.write(msg.getBytes());
        Toast.makeText(this, "data " + msg + " sent to microcontroller", Toast.LENGTH_LONG).show();
        //tv.setText("Data Sent");
    }
    //endregion

    //region check if bluetooth is connected

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            try {
                if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                    //Device found
                    isconnected = true;
                } else if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {
                    //Device is connected
                    isconnected = true;
                    try {
                        Thread.sleep(500);
                    }catch(Exception ex) {
                        Toast.makeText(MainActivity.this, "thread sleep error at bluetooth data send(13) :::: " + ex.getMessage(), Toast.LENGTH_LONG).show();
                    }
                    //region bluetooth2
                    try{
                        sendData("13"); //Bluetooth connected
                    }catch(Exception e){
                        Toast.makeText(MainActivity.this, "error sending 13 to bluetooth ::: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                    //endregion
                    if(txtCurrStats.getText().toString().equals("") || txtCurrStats.getText().toString().equals(" ") || txtCurrStats.getText().toString() == null){
                        main_getLastLotData();
                    }

                } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                    //Done searching
                    isconnected = true;
                } else if (BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED.equals(action)) {
                    //Device is about to disconnect
                    isconnected = false;
                } else if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
                    //Device has disconnected
                    blueAsyncTask blue = new blueAsyncTask(MainActivity.this);
                    blue.execute();
                }
            } catch (Exception e) {
                Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
    };


    //endregion

    //region qr scanner process result
    @SuppressLint({"SourceLockedOrientationActivity", "SetTextI18n"})
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //region qrscan
        IntentResult result = IntentIntegrator.parseActivityResult(resultCode, data);
        try {
            scannedString = result.getContents();
            splittedString = scannedString.split(";");
            if (splittedString[0].equals("00")) {
                if (!adminmode) {
                    operator = splittedString[2];
                    txtOperator.setText(operator);
                } else {
                    alertdialogset("Admin Mode enabled. Please scan admin ID again to disable or Restart Application");
                }
            } else if (splittedString[0].equals("01")) {
                if (splittedString[2].contains("SHINETSUADMINISTRATOR")) {
                    if (adminmode) {
                        adminmode = false;
                        adminmode();
                        tv.setText("Admin Mode Disabled");
                    } else {
                        adminmode = true;
                        adminmode();
                        try {
                            sendData("11"); //ON
                        } catch (Exception e) {
                            alertdialogset(e.getMessage());
                        }
                        tv.setText("Admin Mode");
                    }
                } else {
                    if (!adminmode) {
                        if(bol_manualenc){
                            txtModel.setText(model);
                            txtLotNo.setText(lot_no);
                            pflowcode = workorderID;
                            pflow = new String[20];
                            getprocessflow();     //TODO disable temp for debug build
                            assigndetails();
                            waitforserver();
                            bol_manualenc = false;
                        }else{
                            /*Intent intent = new Intent(this, PromptEngActivity.class);
                            Bundle extras = new Bundle();
                            extras.putString("extra_db_name", db_name);
                            extras.putString("extra_current_ctatus", curr_stats);
                            extras.putString("extra_model", model);
                            extras.putString("extra_lot_no", lot_no);
                            extras.putString("extra_quantity", quantity);
                            extras.putString("extra_engr", splittedString[2]);
                            if (isconnected) {
                                extras.putString("extra_bluetooth", "true");
                            } else {
                                extras.putString("extra_bluetooth", "false");
                            }
                            if (operator != null && !operator.equals("") && !operator.equals(" ")) {
                                extras.putString("extra_optr", operator);
                            } else {
                                extras.putString("extra_optr", " ");
                            }
                            try {
                                sendData("11"); //ON
                            } catch (Exception e) {
                                alertdialogset(e.getMessage());
                            }
                            extras.putString("extra_serverip", ServerIP);
                            intent.putExtras(extras);
                            startActivity(intent);*/
                            alertdialogset("RECORDED. PLEASE PROCEED WITH YOUR SETUP");
                        }
                    } else {
                        alertdialogset("Admin Mode enabled. Please scan admin ID again to disable or Restart Application");
                    }
                }
            } else {
                if (!adminmode) {
                    clearpflow();
                    model = splittedString[1];
                    lot_no = splittedString[2];
                    quantity = splittedString[3];
                    txtModel.setText(model);
                    txtLotNo.setText(lot_no);
                    routingcode = splittedString[4];
                    pflowcode = splittedString[0];
                    pflow = new String[20];
                    getprocessflow();     //TODO disable temp for debug build
                    getcurrentprocess();
                    waitforserver();
                } else {
                    alertdialogset("Admin Mode enabled. Please scan admin ID again to disable or Restart Application");
                }
            }
        } catch (Exception e) {
            alertdialogset("PLEASE SCAN THE CORRECT CODE" + "\n SPECIFIC ERROR CODE: '" + e.getMessage() + "'");
        }

        //endregion
    }
    //endregion qr scanner process result

    //region Asynctask

    //call asynctask for MainActivity_getLotDetails
    public void main_getLastLotData(){
        try {
            MainActivity_getLastLotData main_getlastlotdata = new MainActivity_getLastLotData(this);
            main_getlastlotdata.execute(ServerIP, db_name);
            main_getlastlotdata.delegate = this;
        }catch (Exception e){
            alertdialogset(e.getMessage() + " main_getLastLotData " );
        }
    }

    public boolean main_getLotDetails(){
        try {
            MainActivity_getLotDetails main_getlotdetails = new MainActivity_getLotDetails(this);
            main_getlotdetails.execute(workorderID, ServerIP);
            main_getlotdetails.delegate = this;
            return true;
        }catch (Exception e){
            alertdialogset(e.getMessage() + " main_getLotDetails " );
            return false;
        }

    }

    //call asynctask for MainActivity_getmachinedata
    public void main_getmachinedata() {
        ip_add = getLocalIpAddress();
        MainActivity_getmachinedata main_getmachdata = new MainActivity_getmachinedata(this);
        main_getmachdata.execute(ip_add, ServerIP);
        main_getmachdata.delegate = this;
    }

    //call asynctask for MainActivity_insertstatus
    public boolean main_insertstatus() {
        try {
            if (!db_name.isEmpty() && !curr_stats.isEmpty() && operator != null && !operator.equals("") && !operator.equals(" ")) {
                if (model == null) {
                    model = " ";
                    lot_no = " ";
                    quantity = "0";
                }
                if (remarks == null) {
                    remarks = " ";
                }
                MainActivity_insertstatus main_insertstatus = new MainActivity_insertstatus(this);
                main_insertstatus.execute(db_name, curr_stats, model, lot_no, operator, remarks, ServerIP, quantity);
                main_insertstatus.delegate = this;
                return true;
            } else {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                if (operator == null || operator.equals("") || operator.equals(" ")) {
                    builder.setTitle("Please scan your barcode ID");
                    builder.setMessage("Operator name not found");
                    builder.setNegativeButton("OK", (dialog, which) -> dialog.dismiss());
                } else {
                    builder.setTitle("Error Encountered");
                    builder.setMessage("Please contact your PIC");
                    builder.setNegativeButton("OK", (dialog, which) -> dialog.dismiss());
                }
                //region resetbuttoncolor
                for(int i = 0;i<bolarray.length-1;i++){
                    if(bolarray[i]){
                        bolarray[i] = false;
                    }
                }
                resetbtnColor();
                //endregion
                txtCurrStats.setText("");
                builder.show();
                return false;
            }
        } catch (Exception e) {
            alertdialogset(e.getMessage());
            //region resetbuttoncolor
            for(int i = 0;i<bolarray.length-1;i++){
                if(bolarray[i]){
                    bolarray[i] = false;
                }
            }
            resetbtnColor();
            //endregion
            txtCurrStats.setText("");
            return false;
        }
    }

    public void main_machineinsertstatus(String machinestats) {
        try {
            if (model == null) {
                model = " ";
                lot_no = " ";
                quantity = "0";
            }
            MainActivity_machineinsertstatus main_machineinsertstatus = new MainActivity_machineinsertstatus(this);
            main_machineinsertstatus.execute(db_name, machinestats, ServerIP, model, lot_no, quantity);
            main_machineinsertstatus.delegate = this;
        } catch (Exception e) {
            alertdialogset("Machine Insert Status Error :::::: " + e.getMessage() + " ::::::");
        }

    }

    public void getprocessflow() {
        MainActivity_getprocessflow main_getprocessflow = new MainActivity_getprocessflow(this);
        main_getprocessflow.execute(routingcode, ServerIP);
        main_getprocessflow.delegate = this;
    }

    public void getcurrentprocess() {
        MainActivity_getcurrentprocess main_getcurrentprocess = new MainActivity_getcurrentprocess(this);
        main_getcurrentprocess.execute(pflowcode, ServerIP);
        main_getcurrentprocess.delegate = this;
    }

    //end code for asynctask call

    //results from asycntask for MainActivity_getmachinedata
    @Override
    public void MainActivity_getmachinedata_processFinish(String output) {
        if (output != null) {
            try {
                jsonObject = new JSONObject(output);
                jsonArray = jsonObject.getJSONArray("server_response");
                int count = 0;

                //use while for multiple array in json. inthis case json array is only one so while is not needed.
                //while(count < jsonObject.length()){
                JSONObject JO = jsonArray.getJSONObject(count);
                machine_name = JO.getString("Machine_Name");
                db_name = JO.getString("db_name");
                location = JO.getString("Location");
                bluetoothname = JO.getString("Bluetooth");
                //count++;
                //}

                //call process to assign values on the textView
                assigndetails();
            } catch (Exception e) {
                e.printStackTrace();
                alertdialogset("No machine assigned at the current IP Address. Please contact PIC");
            } finally {
                jsonObject = null;
                jsonArray = null;
            }
        }
    }
    //end asynctask for MainActivity_getmachinedata

    //results from asynctask for MainActivity_inserstatus
    @Override
    public void MainActivity_insertstatus_processFinish(String output) {
        if (output != null && output.equals("Status Sent")) {
            try {
                Toast.makeText(this, output, Toast.LENGTH_LONG).show();
            } catch (Exception e) {
                e.printStackTrace();
                alertdialogset(e.getMessage() + "(JSON process finish)");
            }
        }else{
            alertdialogset(output + " STATUS NOT SENT");
            //region resetbuttoncolor
            for(int i = 0;i<bolarray.length-1;i++){
                if(bolarray[i]){
                    bolarray[i] = false;
                }
            }
            resetbtnColor();
            //endregion
            txtCurrStats.setText("");
        }
    }
    //end asynctask for MainActivity_insertstatus

    //results from asynctask for MainActivity_machineinsertstatus
    @Override
    public void MainActivity_machineinsertstatus_processFinish(String output) {
        if (output != null && output.equals("Status Sent(Machine)")) {
            try {
                Toast.makeText(this, output, Toast.LENGTH_LONG).show();
            } catch (Exception e) {
                e.printStackTrace();
                alertdialogset(e.getMessage() + "(machine process finish :::ERROR:::)");
            }
        }else{
            //alertdialogset(output + " STATUS NOT SENT");
            Toast.makeText(this, output + " STATUS NOT SENT", Toast.LENGTH_SHORT).show();
            //region resetbuttoncolor
            for(int i = 0;i<bolarray.length-1;i++){
                if(bolarray[i]){
                    bolarray[i] = false;
                }
            }
            resetbtnColor();
            //endregion
            txtCurrStats.setText("");
        }
    }
    //end asynctask for MainActivity_machineinsertstatus

    //results from asycntask for MainActivity_getprocessflow
    @Override
    public void MainActivity_getprocessflow_processFinish(String output) {
        if (output != null) {
            try {
                jsonObject = new JSONObject(output);
                jsonArray = jsonObject.getJSONArray("server_response");
                int count = 0;

                //use while for multiple array in json.
                while (count < jsonArray.length()) {
                    JSONObject JO = jsonArray.getJSONObject(count);
                    pflow[count] = JO.getString("Description");
                    count++;
                }

            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, e.getMessage() + "::::ERROR:::: getprocessflow ::::ERROR::::", Toast.LENGTH_LONG).show();
            } finally {
                jsonObject = null;
                jsonArray = null;
            }
        }
    }
    //end asynctask for MainActivity_getprocessflow

    //results from asycntask for MainActivity_getmachinedata
    @Override
    public void MainActivity_getcurrentprocess_processFinish(String output) {
        if (output != null) {
            try {
                jsonObject = new JSONObject(output);
                jsonArray = jsonObject.getJSONArray("server_response");
                int count = 0;

                //use while for multiple array in json. inthis case json array is only one so while is not needed.
                //while(count < jsonArray.length()){
                JSONObject JO = jsonArray.getJSONObject(count);
                currentprocess = JO.getString("Process");
                curprocarr = currentprocess.split(" ");

                //count++;
                //}

                //call process to assign values on the textView
                assigndetails();
            } catch (Exception e) {
                e.printStackTrace();
                alertdialogset(e.getMessage() + "::::ERROR:::: getcurrentprocess ::::ERROR::::");
            } finally {
                jsonObject = null;
                jsonArray = null;
            }
        }
    }
    //end asynctask for MainActivity_getmachinedata

    @SuppressLint("StaticFieldLeak")
    class blueAsyncTask extends AsyncTask<String, Void, String> {
        private ProgressDialog dialog;
        private final AlertDialog.Builder alertDialog;

        public blueAsyncTask(MainActivity activity) {
            dialog = new ProgressDialog(activity);
            alertDialog = new AlertDialog.Builder(MainActivity.this);
        }

        @Override
        protected void onPreExecute() {
            dialog.setMessage("Connecting to Bluetooth, Please Wait...");
            dialog.setCancelable(false);
            dialog.show();
            alertDialog.setCancelable(false);
        }

        @SuppressLint("MissingPermission")
        @Override
        protected String doInBackground(String... args) {
            String res;
            try {
                Thread.sleep(1000);
                mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                assert mBluetoothAdapter != null;
                if (!mBluetoothAdapter.isEnabled()) {
                    Intent enableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableBluetooth, 0);
                }

                @SuppressLint("MissingPermission") Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
                if (pairedDevices.size() > 0) {
                    for (BluetoothDevice device : pairedDevices) {
                        if (device.getName().equals(bluetoothname)) {
                            mmDevice = device;
                            break;
                        }
                    }
                }


                UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); //Standard SerialPortService ID
                mmSocket = mmDevice.createRfcommSocketToServiceRecord(uuid);
                mmSocket.connect();
                mmOutputStream = mmSocket.getOutputStream();
                mmInputStream = mmSocket.getInputStream();

                res = "Bluetooth Connected";



            } catch (Exception e) {
                e.printStackTrace();
                res = "Bluetooth connection error";
            }
            return res;
        }

        @Override
        protected void onPostExecute(String result) {
            // do UI work here
            if (result.equals("Bluetooth Connected")) {
                if (dialog.isShowing()) {
                    beginListenForData();
                    tv.setText(result);
                    dialog.dismiss();
                    dialog = null;
                    activity = null;
                    try{
                        sendData("99"); //send initial data to return value of patlite
                    }catch(Exception e){
                        alertdialogset(e.getMessage());
                    }
                }
            } else {
                tv.setText(result);
                dialog.dismiss();
                alertDialog.setMessage("Bluetooth connection failed. Please retry or restart the application");
                alertDialog.setPositiveButton("RESTART", (dialog, which) -> {
                    PackageManager packageManager = context.getPackageManager();
                    Intent intent = packageManager.getLaunchIntentForPackage(context.getPackageName());
                    ComponentName componentName = intent.getComponent();
                    Intent mainIntent = Intent.makeRestartActivityTask(componentName);
                    context.startActivity(mainIntent);
                    Runtime.getRuntime().exit(0);
                });
                alertDialog.setNegativeButton("RETRY", (dialog, which) -> {
                    blueAsyncTask blue = new blueAsyncTask(MainActivity.this);
                    blue.execute();
                });
                alertDialog.show();
                dialog = null;
                activity = null;

            }
        }
    }

    //results from asynctask for MainActivity_getLotDetails
    public void MainActivity_getLotDetails_processFinish(String output){
        if(output != null){
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            try {
                jsonObject = new JSONObject(output);
                jsonArray = jsonObject.getJSONArray("server_response");
                int count = 0;

                //use while for multiple array in json. inthis case json array is only one so while is not needed.
                //while(count < jsonArray.length()){
                JSONObject JO = jsonArray.getJSONObject(count);
                model = JO.getString("Model_Name");
                lot_no = JO.getString("Lot_No");
                quantity = JO.getString("Running_Quantity_2");
                routingcode = JO.getString("RoutingCode");
                currentprocess = JO.getString("Process");
                //count++;
                //}

                if(!bypassed) {
                    if(currentprocess.contains(machine_name)) {
                        //builder.setTitle(workorderID);
                        builder.setMessage("Model Name:   " + model + "\n\n" + "Lot No:   " + lot_no + "\n\n" + "Quantity:   " + quantity + "\n");
                        builder.setPositiveButton("Accept", (dialogg, whichh) -> {
                            intentIntegrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES);
                            intentIntegrator.setPrompt("Model Name: " + model + "\n" + "Lot No: " + lot_no + "\n");
                            intentIntegrator.setTorchEnabled(true);
                            intentIntegrator.initiateScan();
                        });
                        builder.setNegativeButton("Cancel", (dialogg, whichh) -> dialogg.cancel());

                        AlertDialog alert = builder.create();
                        alert.show();
                        alert.getWindow().getAttributes();

                        TextView textView = alert.findViewById(android.R.id.message);
                        textView.setTextSize(30);
                        //builder.show();
                    }else
                        {
                        startBuzzerforWrongProcess();
                        builder.setMessage("WRONG PROCESS");
                        // Set up the buttons
                        builder.setPositiveButton("OK", (dialog, which) -> {
                            model = null;
                            lot_no = null;
                            txtModel.setText("");
                            txtLotNo.setText("");
                            ccancelTimer();
                        });
                        builder.setCancelable(false);
                        AlertDialog alert = builder.create();
                        alert.show();
                        alert.getWindow().getAttributes();

                        TextView textView = alert.findViewById(android.R.id.message);
                        textView.setTextSize(30);
                        //builder.show();
                    }
                }else{
                    //builder.setTitle(workorderID);
                    builder.setMessage("Model Name:   " + model + "\n\n" + "Lot No:   " + lot_no + "\n\n" + "Quantity:   " + quantity + "\n");
                    builder.setPositiveButton("Accept", (dialogg, whichh) -> {
                        intentIntegrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES);
                        intentIntegrator.setPrompt("Model Name: " + model + "\n" + "Lot No: " + lot_no + "\n");
                        intentIntegrator.setTorchEnabled(true);
                        intentIntegrator.initiateScan();
                    });
                    builder.setNegativeButton("Cancel", (dialogg, whichh) -> dialogg.cancel());

                    AlertDialog alert = builder.create();
                    alert.show();
                    alert.getWindow().getAttributes();

                    TextView textView = alert.findViewById(android.R.id.message);
                    textView.setTextSize(30);
                    //builder.show();
                }

            } catch (Exception e) {
                e.printStackTrace();
                alertdialogset("PLEASE INPUT THE CORRECT WORKORDER ID \n\n " + e.getMessage());
            } finally {
                jsonObject = null;
                jsonArray = null;
            }
        }
    }

    //results from asynctask for MainActivity_getLastLotData
    public void MainActivity_getLastLotData_processFinish(String output){
        if(output != null){
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            try {
                jsonObject = new JSONObject(output);
                jsonArray = jsonObject.getJSONArray("server_response");
                int count = 0;

                //use while for multiple array in json. inthis case json array is only one so while is not needed.
                //while(count < jsonArray.length()){
                JSONObject JO = jsonArray.getJSONObject(count);
                model = JO.getString("Model");
                lot_no = JO.getString("Lot_No");
                operator = JO.getString("Operator");
                curr_stats = JO.getString("Current_Status");
                quantity = JO.getString("Quantity");
                remarks = JO.getString("Remarks");
                //count++;
                //}

                Thread.sleep(1000);

                if(operator != null || !operator.equals(" ") || !operator.equals("")) {
                    if (!model.equals(" ") && model != null && !model.equals("") && !curr_stats.equals("FLUSH OUT")) {
                        txtModel.setText(model);
                        txtLotNo.setText(lot_no);
                    }
                    txtOperator.setText(operator);
                    txtCurrStats.setText(curr_stats);
                    //region buttonclick
                    if (curr_stats.equals("Operation MASSPRO")) {
                        btnOP.performLongClick();
                    }
                    if (curr_stats.equals("Operation PROTO")) {
                        btnOPProto.performLongClick();
                    }
                    if (curr_stats.equals("FLUSH OUT")) {
                       txtOperator.setText("");
                       operator = "";
                       model = "";
                       lot_no = "";
                    }
                    /*else if(curr_stats.equals("Machine Setup")){
                        btnSetup.performLongClick();
                    }else if(curr_stats.equals("Adjustment")){
                        btnAdjustment.performLongClick();3
                    }else if(curr_stats.equals("Standby")){
                        btnStandby.performLongClick();
                    }else if(curr_stats.equals("Breaktime")){
                        btnBreaktime.performLongClick();
                    }else if(curr_stats.equals("Calibration")){
                        btnCalib.performLongClick();
                    }else if(curr_stats.equals("Under EE/PM")){
                        btnUndereepm.performLongClick();
                    }else if(curr_stats.equals("Waiting Raw Materials")){
                        btnNoWIP.performLongClick();
                    }else if(curr_stats.equals("FLUSH OUT")){
                        btnChangeLot.performLongClick();
                    }else if(curr_stats.equals("MACHINE STOP")){
                        btnMachineStop.performLongClick();
                    }else if(curr_stats.equals("")||curr_stats.equals(" ")||curr_stats == null){
                        //TODO nothing lmao
                    }*/
                        //endregion
                }
            }catch (Exception ex){
                alertdialogset(ex.getMessage() + " ERROR IN FETCHING LAST LOT DATA ");
            }
        }
    }

    //endregion

    //region public methods

    //region battery charge
    public void batt_check(){

        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = context.registerReceiver(null, ifilter);

        int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING;

        if (!isCharging) {
            if (getBatteryPercentage(this) <= 20) {
                try {
                    sendData("20");
                    txtCharger.setText("⚡");
                } catch (Exception e) {
                    //alertdialogset("Cannot send data for battery %");
                }
            }
        }
        if (isCharging) {
            if (getBatteryPercentage(this) >= 90){
                try {
                    sendData("90");
                    txtCharger.setText("!⚡");
                } catch (Exception e) {
                    //alertdialogset("Cannot send data for battery %");
                }
            }
        }
    }

    @SuppressLint("ObsoleteSdkInt")
    public int getBatteryPercentage(Context context) {

        /*if (Build.VERSION.SDK_INT >= 21) {*/
        int battper=0;
        try {
            BatteryManager bm = (BatteryManager) context.getSystemService(BATTERY_SERVICE);
            battper = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
        } catch (Exception e) {
            alertdialogset(e.getMessage());
        }
        return battper;


        /*} else {

            IntentFilter iFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
            Intent batteryStatus = context.registerReceiver(null, iFilter);

            int level = batteryStatus != null ? batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) : -1;
            int scale = batteryStatus != null ? batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1) : -1;

            double batteryPct = level / (double) scale;

            return (int) (batteryPct * 100);
        }*/
    }

    //endregion

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {// If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // permission was granted, yay! Do the
                // contacts-related task you need to do.
            } else {

                // permission denied, boo! Disable the
                // functionality that depends on this permission.
                Toast.makeText(MainActivity.this, "Permission denied to read your External storage", Toast.LENGTH_SHORT).show();
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    public static String getLocalIpAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
                        return inetAddress.getHostAddress();
                    }
                }
            }
        } catch (SocketException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    //region waitforserver
    public void waitforserver() {
        cTimer = new CountDownTimer(/*1000*60)**/2000, 1000) {
            @SuppressLint("SetTextI18n")
            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {
                assignprocessflow();
                cancelTimerwaitforserver();
            }
        };
        cTimer.start();
    }

    public void cancelTimerwaitforserver() {
        if (cTimer != null)
            cTimer.cancel();
    }
    //endregion

    //region buzzer loop
    public void waitforStop(int min) {
        if (!bms) {
            bms = true;
            cTimer = new CountDownTimer((1000*60)*min, 1000) {
                @SuppressLint("SetTextI18n")
                @Override
                public void onTick(long millisUntilFinished) {

                }

                @Override
                public void onFinish() {
                    cancelTimerwaitforStop();
                    startBuzzer(480);
                }
            };
            cTimer.start();
        }
    }

    public void cancelTimerwaitforStop() {
        if (cTimer != null)
            cTimer.cancel();
    }

    public void startBuzzer(int min) {
        cTimer = new CountDownTimer((1000 * 60) * min, 1000) {
            @SuppressLint("SetTextI18n")
            @Override
            public void onTick(long millisUntilFinished) {
                ColorDrawable viewColor = (ColorDrawable) constraintLayout.getBackground();
                int color = getResources().getColor(R.color.redish);
                int colorconst = viewColor.getColor();
                if (colorconst == color) {
                    constraintLayout.setBackgroundColor(getResources().getColor(R.color.white));
                } else {
                    constraintLayout.setBackgroundColor(getResources().getColor(R.color.redish));
                }
                mp.start();
            }

            @Override
            public void onFinish() {
                try {
                    cancelTimer();
                    mp.stop();
                    constraintLayout.setBackgroundColor(getResources().getColor(R.color.white));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        cTimer.start();
    }

    public void cancelTimer() {
        if (cTimer != null)
            cTimer.cancel();
        constraintLayout.setBackgroundColor(getResources().getColor(R.color.white));

    }

    public void startBuzzerforWrongProcess(){
        ccTimer = new CountDownTimer((1000 * 60) * 60, 1000) {
            @SuppressLint("SetTextI18n")
            @Override
            public void onTick(long millisUntilFinished) {
                mp.start();
            }

            @Override
            public void onFinish() {
                try {
                    cancelTimer();
                    mp.stop();
                } catch (Exception e) {
                    alertdialogset(e.getMessage());
                }
            }
        };
        ccTimer.start();
    }

    public void ccancelTimer() {
        if (ccTimer != null)
            ccTimer.cancel();
    }

    //endregion buzzer loop

    public void resetbtnColor() {
        if (bop) {
            bmerr = false;
            btnAdjustment.setBackground(ContextCompat.getDrawable(context, round_button_all));
            bchlt = false;
            btnChangeLot.setBackground(ContextCompat.getDrawable(context, round_button_all));
            bnw = false;
            btnNoWIP.setBackground(ContextCompat.getDrawable(context, round_button_all));
            bbt = false;
            btnBreaktime.setBackground(ContextCompat.getDrawable(context, round_button_all));
            bms = false;
            btnMachineStop.setBackground(ContextCompat.getDrawable(context, round_button_all));
            bueepm = false;
            btnUndereepm.setBackground(ContextCompat.getDrawable(context, round_button_all));
            bst = false;
            btnStandby.setBackground(ContextCompat.getDrawable(context, round_button_all));
            bmain = false;
            btnSetup.setBackground(ContextCompat.getDrawable(context, round_button_all));
            bmf = false;
            btnCalib.setBackground(ContextCompat.getDrawable(context, round_button_all));
            bopp = false;
            btnOPProto.setBackground(ContextCompat.getDrawable(context, round_button_all));
            bothers = false;
            btnOthers.setBackground(ContextCompat.getDrawable(context, round_button_all));
        } else if (bmerr) {
            bop = false;
            btnOP.setBackground(ContextCompat.getDrawable(context, round_button_all));
            bchlt = false;
            btnChangeLot.setBackground(ContextCompat.getDrawable(context, round_button_all));
            bnw = false;
            btnNoWIP.setBackground(ContextCompat.getDrawable(context, round_button_all));
            bbt = false;
            btnBreaktime.setBackground(ContextCompat.getDrawable(context, round_button_all));
            bms = false;
            btnMachineStop.setBackground(ContextCompat.getDrawable(context, round_button_all));
            bueepm = false;
            btnUndereepm.setBackground(ContextCompat.getDrawable(context, round_button_all));
            bst = false;
            btnStandby.setBackground(ContextCompat.getDrawable(context, round_button_all));
            bmain = false;
            btnSetup.setBackground(ContextCompat.getDrawable(context, round_button_all));
            bmf = false;
            btnCalib.setBackground(ContextCompat.getDrawable(context, round_button_all));
            bopp = false;
            btnOPProto.setBackground(ContextCompat.getDrawable(context, round_button_all));
            bothers = false;
            btnOthers.setBackground(ContextCompat.getDrawable(context, round_button_all));
        } else if (bchlt) {
            bmerr = false;
            btnAdjustment.setBackground(ContextCompat.getDrawable(context, round_button_all));
            bop = false;
            btnOP.setBackground(ContextCompat.getDrawable(context, round_button_all));
            bnw = false;
            btnNoWIP.setBackground(ContextCompat.getDrawable(context, round_button_all));
            bbt = false;
            btnBreaktime.setBackground(ContextCompat.getDrawable(context, round_button_all));
            bms = false;
            btnMachineStop.setBackground(ContextCompat.getDrawable(context, round_button_all));
            bueepm = false;
            btnUndereepm.setBackground(ContextCompat.getDrawable(context, round_button_all));
            bst = false;
            btnStandby.setBackground(ContextCompat.getDrawable(context, round_button_all));
            bmain = false;
            btnSetup.setBackground(ContextCompat.getDrawable(context, round_button_all));
            bmf = false;
            btnCalib.setBackground(ContextCompat.getDrawable(context, round_button_all));
            bopp = false;
            btnOPProto.setBackground(ContextCompat.getDrawable(context, round_button_all));
            bothers = false;
            btnOthers.setBackground(ContextCompat.getDrawable(context, round_button_all));
        } else if (bnw) {
            bmerr = false;
            btnAdjustment.setBackground(ContextCompat.getDrawable(context, round_button_all));
            bchlt = false;
            btnChangeLot.setBackground(ContextCompat.getDrawable(context, round_button_all));
            bop = false;
            btnOP.setBackground(ContextCompat.getDrawable(context, round_button_all));
            bbt = false;
            btnBreaktime.setBackground(ContextCompat.getDrawable(context, round_button_all));
            bms = false;
            btnMachineStop.setBackground(ContextCompat.getDrawable(context, round_button_all));
            bueepm = false;
            btnUndereepm.setBackground(ContextCompat.getDrawable(context, round_button_all));
            bst = false;
            btnStandby.setBackground(ContextCompat.getDrawable(context, round_button_all));
            bmain = false;
            btnSetup.setBackground(ContextCompat.getDrawable(context, round_button_all));
            bmf = false;
            btnCalib.setBackground(ContextCompat.getDrawable(context, round_button_all));
            bopp = false;
            btnOPProto.setBackground(ContextCompat.getDrawable(context, round_button_all));
            bothers = false;
            btnOthers.setBackground(ContextCompat.getDrawable(context, round_button_all));
        } else if (bbt) {
            bmerr = false;
            btnAdjustment.setBackground(ContextCompat.getDrawable(context, round_button_all));
            bchlt = false;
            btnChangeLot.setBackground(ContextCompat.getDrawable(context, round_button_all));
            bnw = false;
            btnNoWIP.setBackground(ContextCompat.getDrawable(context, round_button_all));
            bop = false;
            btnOP.setBackground(ContextCompat.getDrawable(context, round_button_all));
            bms = false;
            btnMachineStop.setBackground(ContextCompat.getDrawable(context, round_button_all));
            bueepm = false;
            btnUndereepm.setBackground(ContextCompat.getDrawable(context, round_button_all));
            bst = false;
            btnStandby.setBackground(ContextCompat.getDrawable(context, round_button_all));
            bmain = false;
            btnSetup.setBackground(ContextCompat.getDrawable(context, round_button_all));
            bmf = false;
            btnCalib.setBackground(ContextCompat.getDrawable(context, round_button_all));
            bopp = false;
            btnOPProto.setBackground(ContextCompat.getDrawable(context, round_button_all));
            bothers = false;
            btnOthers.setBackground(ContextCompat.getDrawable(context, round_button_all));
        } else if (bms) {
            bmerr = false;
            btnAdjustment.setBackground(ContextCompat.getDrawable(context, round_button_all));
            bchlt = false;
            btnChangeLot.setBackground(ContextCompat.getDrawable(context, round_button_all));
            bnw = false;
            btnNoWIP.setBackground(ContextCompat.getDrawable(context, round_button_all));
            bbt = false;
            btnBreaktime.setBackground(ContextCompat.getDrawable(context, round_button_all));
            bop = false;
            btnOP.setBackground(ContextCompat.getDrawable(context, round_button_all));
            bueepm = false;
            btnUndereepm.setBackground(ContextCompat.getDrawable(context, round_button_all));
            bst = false;
            btnStandby.setBackground(ContextCompat.getDrawable(context, round_button_all));
            bmain = false;
            btnSetup.setBackground(ContextCompat.getDrawable(context, round_button_all));
            bmf = false;
            btnCalib.setBackground(ContextCompat.getDrawable(context, round_button_all));
            bopp = false;
            btnOPProto.setBackground(ContextCompat.getDrawable(context, round_button_all));
            bothers = false;
            btnOthers.setBackground(ContextCompat.getDrawable(context, round_button_all));
        } else if (bueepm) {
            bmerr = false;
            btnAdjustment.setBackground(ContextCompat.getDrawable(context, round_button_all));
            bchlt = false;
            btnChangeLot.setBackground(ContextCompat.getDrawable(context, round_button_all));
            bnw = false;
            btnNoWIP.setBackground(ContextCompat.getDrawable(context, round_button_all));
            bbt = false;
            btnBreaktime.setBackground(ContextCompat.getDrawable(context, round_button_all));
            bms = false;
            btnMachineStop.setBackground(ContextCompat.getDrawable(context, round_button_all));
            bop = false;
            btnOP.setBackground(ContextCompat.getDrawable(context, round_button_all));
            bst = false;
            btnStandby.setBackground(ContextCompat.getDrawable(context, round_button_all));
            bmain = false;
            btnSetup.setBackground(ContextCompat.getDrawable(context, round_button_all));
            bmf = false;
            btnCalib.setBackground(ContextCompat.getDrawable(context, round_button_all));
            bopp = false;
            btnOPProto.setBackground(ContextCompat.getDrawable(context, round_button_all));
            bothers = false;
            btnOthers.setBackground(ContextCompat.getDrawable(context, round_button_all));
        } else if (bst) {
            bmerr = false;
            btnAdjustment.setBackground(ContextCompat.getDrawable(context, round_button_all));
            bchlt = false;
            btnChangeLot.setBackground(ContextCompat.getDrawable(context, round_button_all));
            bnw = false;
            btnNoWIP.setBackground(ContextCompat.getDrawable(context, round_button_all));
            bbt = false;
            btnBreaktime.setBackground(ContextCompat.getDrawable(context, round_button_all));
            bms = false;
            btnMachineStop.setBackground(ContextCompat.getDrawable(context, round_button_all));
            bueepm = false;
            btnUndereepm.setBackground(ContextCompat.getDrawable(context, round_button_all));
            bop = false;
            btnOP.setBackground(ContextCompat.getDrawable(context, round_button_all));
            bmain = false;
            btnSetup.setBackground(ContextCompat.getDrawable(context, round_button_all));
            bmf = false;
            btnCalib.setBackground(ContextCompat.getDrawable(context, round_button_all));
            bopp = false;
            btnOPProto.setBackground(ContextCompat.getDrawable(context, round_button_all));
            bothers = false;
            btnOthers.setBackground(ContextCompat.getDrawable(context, round_button_all));
        } else if (bmain) {
            bmerr = false;
            btnAdjustment.setBackground(ContextCompat.getDrawable(context, round_button_all));
            bchlt = false;
            btnChangeLot.setBackground(ContextCompat.getDrawable(context, round_button_all));
            bnw = false;
            btnNoWIP.setBackground(ContextCompat.getDrawable(context, round_button_all));
            bbt = false;
            btnBreaktime.setBackground(ContextCompat.getDrawable(context, round_button_all));
            bms = false;
            btnMachineStop.setBackground(ContextCompat.getDrawable(context, round_button_all));
            bueepm = false;
            btnUndereepm.setBackground(ContextCompat.getDrawable(context, round_button_all));
            bst = false;
            btnStandby.setBackground(ContextCompat.getDrawable(context, round_button_all));
            bop = false;
            btnOP.setBackground(ContextCompat.getDrawable(context, round_button_all));
            bmf = false;
            btnCalib.setBackground(ContextCompat.getDrawable(context, round_button_all));
            bopp = false;
            btnOPProto.setBackground(ContextCompat.getDrawable(context, round_button_all));
            bothers = false;
            btnOthers.setBackground(ContextCompat.getDrawable(context, round_button_all));
        } else if (bmf) {
            bmerr = false;
            btnAdjustment.setBackground(ContextCompat.getDrawable(context, round_button_all));
            bchlt = false;
            btnChangeLot.setBackground(ContextCompat.getDrawable(context, round_button_all));
            bnw = false;
            btnNoWIP.setBackground(ContextCompat.getDrawable(context, round_button_all));
            bbt = false;
            btnBreaktime.setBackground(ContextCompat.getDrawable(context, round_button_all));
            bms = false;
            btnMachineStop.setBackground(ContextCompat.getDrawable(context, round_button_all));
            bueepm = false;
            btnUndereepm.setBackground(ContextCompat.getDrawable(context, round_button_all));
            bst = false;
            btnStandby.setBackground(ContextCompat.getDrawable(context, round_button_all));
            bmain = false;
            btnSetup.setBackground(ContextCompat.getDrawable(context, round_button_all));
            bop = false;
            btnOP.setBackground(ContextCompat.getDrawable(context, round_button_all));
            bopp = false;
            btnOPProto.setBackground(ContextCompat.getDrawable(context, round_button_all));
            bothers = false;
            btnOthers.setBackground(ContextCompat.getDrawable(context, round_button_all));
        }else if(bopp){
            bmerr = false;
            btnAdjustment.setBackground(ContextCompat.getDrawable(context, round_button_all));
            bchlt = false;
            btnChangeLot.setBackground(ContextCompat.getDrawable(context, round_button_all));
            bnw = false;
            btnNoWIP.setBackground(ContextCompat.getDrawable(context, round_button_all));
            bbt = false;
            btnBreaktime.setBackground(ContextCompat.getDrawable(context, round_button_all));
            bms = false;
            btnMachineStop.setBackground(ContextCompat.getDrawable(context, round_button_all));
            bueepm = false;
            btnUndereepm.setBackground(ContextCompat.getDrawable(context, round_button_all));
            bst = false;
            btnStandby.setBackground(ContextCompat.getDrawable(context, round_button_all));
            bmain = false;
            btnSetup.setBackground(ContextCompat.getDrawable(context, round_button_all));
            bop = false;
            btnOP.setBackground(ContextCompat.getDrawable(context, round_button_all));
            bmf = false;
            btnCalib.setBackground(ContextCompat.getDrawable(context, round_button_all));
            bothers = false;
            btnOthers.setBackground(ContextCompat.getDrawable(context, round_button_all));
        }else if(bothers){
            bmerr = false;
            btnAdjustment.setBackground(ContextCompat.getDrawable(context, round_button_all));
            bchlt = false;
            btnChangeLot.setBackground(ContextCompat.getDrawable(context, round_button_all));
            bnw = false;
            btnNoWIP.setBackground(ContextCompat.getDrawable(context, round_button_all));
            bbt = false;
            btnBreaktime.setBackground(ContextCompat.getDrawable(context, round_button_all));
            bms = false;
            btnMachineStop.setBackground(ContextCompat.getDrawable(context, round_button_all));
            bueepm = false;
            btnUndereepm.setBackground(ContextCompat.getDrawable(context, round_button_all));
            bst = false;
            btnStandby.setBackground(ContextCompat.getDrawable(context, round_button_all));
            bmain = false;
            btnSetup.setBackground(ContextCompat.getDrawable(context, round_button_all));
            bop = false;
            btnOP.setBackground(ContextCompat.getDrawable(context, round_button_all));
            bmf = false;
            btnCalib.setBackground(ContextCompat.getDrawable(context, round_button_all));
            bopp = false;
            btnOPProto.setBackground(ContextCompat.getDrawable(context, round_button_all));
        } else{
            bmerr = false;
            btnAdjustment.setBackground(ContextCompat.getDrawable(context, round_button_all));
            bchlt = false;
            btnChangeLot.setBackground(ContextCompat.getDrawable(context, round_button_all));
            bnw = false;
            btnNoWIP.setBackground(ContextCompat.getDrawable(context, round_button_all));
            bbt = false;
            btnBreaktime.setBackground(ContextCompat.getDrawable(context, round_button_all));
            bms = false;
            btnMachineStop.setBackground(ContextCompat.getDrawable(context, round_button_all));
            bueepm = false;
            btnUndereepm.setBackground(ContextCompat.getDrawable(context, round_button_all));
            bst = false;
            btnStandby.setBackground(ContextCompat.getDrawable(context, round_button_all));
            bmain = false;
            btnSetup.setBackground(ContextCompat.getDrawable(context, round_button_all));
            bop = false;
            btnOP.setBackground(ContextCompat.getDrawable(context, round_button_all));
            bmf = false;
            btnCalib.setBackground(ContextCompat.getDrawable(context, round_button_all));
            bopp = false;
            btnOPProto.setBackground(ContextCompat.getDrawable(context, round_button_all));
            bothers = false;
            btnOthers.setBackground(ContextCompat.getDrawable(context, round_button_all));
        }
        bmerr = false;
        bchlt = false;
        bnw = false;
        bbt = false;bms = false;
        bueepm = false;
        bst = false;
        bmain = false;
        bop = false;
        bmf = false;
        bopp = false;
        bothers = false;
    }

    public void processarduinotext() {
        if (arduinotext.equals("STOP")) {
            btnMachineStop.performClick();
        }
        if (arduinotext.equals("GREEN")) {
            if (model == null || txtModel.getText() == null || txtModel.getText() == " " || model.equals(" ")) {
                model = " ";
                lot_no = " ";
                quantity = "0";
            }
            main_machineinsertstatus("Operation");

        }
        if (arduinotext.equals("RED")) {
            if (model == null || txtModel.getText() == null || txtModel.getText() == " " || model.equals(" ")) {
                model = " ";
                lot_no = " ";
                quantity = "0";
            }
            main_machineinsertstatus("Machine Stop");

        }
        if (arduinotext.equals("REDFLASH")) {
            if (model == null || txtModel.getText() == null || txtModel.getText() == " " || model.equals(" ")) {
                model = " ";
                lot_no = " ";
                quantity = "0";
            }
            main_machineinsertstatus("Machine Error");

        }
    }

    public void assigndetails() {
        txtMachineName.setText(machine_name);
        txtLocation.setText(location);
        txtIPAdd.setText(ip_add);
    }

    public void alertdialogremarksinput() {
        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Remarks");

            // Set up the input
            final EditText input = new EditText(this);
            // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
            input.setInputType(InputType.TYPE_CLASS_TEXT);
            builder.setView(input);

            // Set up the buttons
            builder.setPositiveButton("OK", (dialog, which) -> {
                remarks = input.getText().toString();
                if (main_insertstatus()) {
                    txtRemarks.setText(remarks);
                }
            });
            builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
            builder.show();
        } catch (Exception e) {
            alertdialogset(e.getMessage());
        }
    }

    public void assignprocessflow() {
        boolean foundcurrproc = false;
        int j = 0;
        while (pflow[j] != null) {
            buttonArray[j].setText(pflow[j]);
            buttonArray[j].setTextColor(getApplication().getResources().getColor(R.color.black));
            if(foundcurrproc){
                buttonArray[j].setBackground(ContextCompat.getDrawable(context, design_notdoneproc));
            }else{
                buttonArray[j].setBackground(ContextCompat.getDrawable(context, design_doneproc));
            }
            if (pflow[j].trim().contains(currentprocess.trim())) {
                buttonArray[j].setBackground(ContextCompat.getDrawable(context, design_currentproc));
                buttonArray[j].setTextColor(getApplication().getResources().getColor(R.color.white));
                foundcurrproc = true;
            }
            j++;
        }
        if (!foundcurrproc) {
            buttonArray[j].setText(currentprocess);
            buttonArray[j].setBackground(ContextCompat.getDrawable(context, design_currentproc));
            j++;
        }
        if (j > 0) {
            bufferrow.setVisibility(View.GONE);
            row1pflow.setVisibility(View.VISIBLE);
        }
        if (j > 3) {
            bufferrow.setVisibility(View.GONE);
            row2pflow.setVisibility(View.VISIBLE);
        }
        if (j > 7) {
            bufferrow.setVisibility(View.GONE);
            row3pflow.setVisibility(View.VISIBLE);
        }
        if (j > 11) {
            bufferrow.setVisibility(View.GONE);
            row4pflow.setVisibility(View.VISIBLE);
        }
        if (j > 15) {
            bufferrow.setVisibility(View.GONE);
            row5pflow.setVisibility(View.VISIBLE);
        }
        for (int i = j; i < buttonArray.length - 1; i++) {
            buttonArray[i].setVisibility(View.GONE);
        }
        if(!bypassed) {
            if(!currentprocess.contains(machine_name)){
                startBuzzerforWrongProcess();
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("WRONG PROCESS");
                // Set up the buttons
                builder.setPositiveButton("OK", (dialog, which) -> {
                    model = null;
                    lot_no = null;
                    txtModel.setText("");
                    txtLotNo.setText("");
                    clearpflow();
                    ccancelTimer();
                });
                builder.setCancelable(false);
                AlertDialog alert = builder.create();
                alert.show();
                alert.getWindow().getAttributes();

                TextView textView = alert.findViewById(android.R.id.message);
                textView.setTextSize(30);
                //builder.show();
            }
        }
        pflow = null;
        pflowcode = null;
        routingcode = null;
        currentprocess = null;
    }

    public void alertdialogset(String message) {
        AlertDialog.Builder ab = new AlertDialog.Builder(this);
        ab.setMessage(message);
        ab.show();
    }

    public static String resetBluetoothConnection() {
        try {
            //send off signal for relay for normal operations
            String msg = "10\n";
            mmOutputStream.write(msg.getBytes());

            Thread.sleep(300);
            mmInputStream.close();
            mmInputStream = null;
            mmOutputStream.close();
            mmOutputStream = null;
            mmSocket.close();
            mmSocket = null;
        } catch (Exception e) {
            return e.getMessage();
        }
        return null;
    }

    public void adminmode(){
        if(adminmode){
            btnAdjustment.setEnabled(false);
            btnSetup.setEnabled(false);
            btnMachineStop.setEnabled(false);
            btnStandby.setEnabled(false);
            btnBreaktime.setEnabled(false);
            btnUndereepm.setEnabled(false);
            btnNoWIP.setEnabled(false);
            btnChangeLot.setEnabled(false);
            btnOP.setEnabled(false);
            btnCalib.setEnabled(false);
            btnOthers.setEnabled(false);
            btnmanualenc.setEnabled(false);
            btnOPProto.setEnabled(false);
            btnUndereepm.setEnabled(false);
        }else{
            btnAdjustment.setEnabled(true);
            btnSetup.setEnabled(true);
            btnMachineStop.setEnabled(true);
            btnStandby.setEnabled(true);
            btnBreaktime.setEnabled(true);
            btnUndereepm.setEnabled(true);
            btnNoWIP.setEnabled(true);
            btnChangeLot.setEnabled(true);
            btnOP.setEnabled(true);
            btnCalib.setEnabled(true);
            btnOthers.setEnabled(true);
            btnmanualenc.setEnabled(true);
            btnOPProto.setEnabled(true);
            btnUndereepm.setEnabled(true);
        }
    }

    public void clearpflow(){
        for(Button bt : buttonArray){
            bt.setText(null);
            bt.setVisibility(View.VISIBLE);
            bt.setBackground(ContextCompat.getDrawable(context, design_pflow));
        }

        for(TableRow r : row){
            r.setVisibility(View.GONE);
        }
    }

    //endregion

}



