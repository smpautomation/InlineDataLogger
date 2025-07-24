package com.example.realtimemonitoring;

import static com.example.realtimemonitoring.R.drawable.round_button_green;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

public class OthersActivity extends AppCompatActivity implements MainActivity_insertstatus.AsyncResponse{

    Button btnStartUpShutDown, btnDataCheckDimCheck, btnJigVal, btnMeetingOrient, btnMachChecklist, btnWaitTool, btn5s, btnWaitEngTech;
    String db_name, model, lot_no, quantity, operator, ServerIP, curr_stats, remarks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_others);

        //region get extras
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        db_name = extras.getString("extra_db_name");
        model = extras.getString("extra_model");
        lot_no = extras.getString("extra_lot_no");
        quantity = extras.getString("extra_quantity");
        operator  = extras.getString("extra_optr");
        ServerIP = extras.getString("extra_serverip");
        //endregion

        //region findview
        btnStartUpShutDown = findViewById(R.id.btn_StartUpShutDown);
        btnDataCheckDimCheck = findViewById(R.id.btn_Datacheck);
        btnJigVal = findViewById(R.id.btn_JigVal);
        btnMeetingOrient = findViewById(R.id.btn_meetorient);
        btnMachChecklist = findViewById(R.id.btn_machinechecklist);
        btnWaitTool = findViewById(R.id.btn_waittoolings);
        btn5s = findViewById(R.id.btn_5s);
        btnWaitEngTech = findViewById(R.id.btn_waitengtech);
        //endregion

        //region btnOnLongClickListener
        btnStartUpShutDown.setOnLongClickListener(v -> {
            curr_stats = "START-UP/SHUTDOWN";
            if(main_insertstatus()){retToMain();}
            return true;
        });
        btnDataCheckDimCheck.setOnLongClickListener(v -> {
            curr_stats = "DATA CHECKING/DIMENSION CHECK";
            if(main_insertstatus()){retToMain();}
            return true;
        });
        btnJigVal.setOnLongClickListener(v -> {
            curr_stats = "JIG VALIDATION";
            if(main_insertstatus()){retToMain();}
            return true;
        });
        btnMeetingOrient.setOnLongClickListener(v -> {
            curr_stats = "MEETING/ORIENTATION";
            if(main_insertstatus()){retToMain();}
            return true;
        });
        btnMachChecklist.setOnLongClickListener(v -> {
            curr_stats = "MACHINE CHECKLIST";
            if(main_insertstatus()){retToMain();}
            return true;
        });
        btnWaitTool.setOnLongClickListener(v -> {
            curr_stats = "WAITING FOR TOOLINGS";
            if(main_insertstatus()){retToMain();}
            return true;
        });
        btn5s.setOnLongClickListener(v -> {
            curr_stats = "5S";
            if(main_insertstatus()){retToMain();}
            return true;
        });
        btnWaitEngTech.setOnLongClickListener(v -> {
            curr_stats = "WAITING ENGINEER/TECH";
            if(main_insertstatus()){retToMain();}
            return true;
        });
        //endregion

    }

    public void retToMain(){
        this.finish();
    }

    //region Asynctask
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
                builder.show();
                return false;
            }
        } catch (Exception e) {
            alertdialogset(e.getMessage());
            return false;
        }
    }

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
            alertdialogset(output + "STATUS NOT SENT");
        }
    }
    //endregion

    public void alertdialogset(String message) {
        AlertDialog.Builder ab = new AlertDialog.Builder(this);
        ab.setMessage(message);
        ab.show();
    }
}