package com.example.realtimemonitoring;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;

public class PromptEngActivity extends AppCompatActivity implements MainActivity_insertstatus.AsyncResponse{
    String db_name, curr_stats, model, lot_no, engr, remarks, operator, bluetooth, ServerIP, quantity;
    TextView tv_engr;
    Boolean rep = false, set = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prompt_eng);
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
        tv_engr = findViewById(R.id.tv_engr);
        //endregion
        tv_engr.setText(engr);
    }

    public void ClickMachineSetup(View view) {
        try {
            //curr_stats = "Machine Setup";
            //main_insertstatus();
            set = true;
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        }

    }

    public void ClickMachineRepair(View view) {
        try {
            curr_stats = "Machine Fixing";
            main_insertstatus();
            rep = true;
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        }

    }

    public void redirectActivity(Activity activity,Class aclass){
        Intent intent = new Intent(activity, aclass);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Bundle extras = new Bundle();
        extras.putString("extra_db_name", db_name);
        extras.putString("extra_current_ctatus", curr_stats);
        extras.putString("extra_model", model);
        extras.putString("extra_lot_no", lot_no);
        extras.putString("extra_engr", engr);
        extras.putString("extra_optr", operator);
        extras.putString("extra_quantity", quantity);
        extras.putString("extra_serverip", ServerIP);
        intent.putExtras(extras);
        activity.startActivity(intent);
        this.finish();
    }

    //region AsyncTask
    public void main_insertstatus(){
        try{
            if (!db_name.isEmpty() && !curr_stats.isEmpty() && engr != null) {
                if (model == null){
                    model = " ";
                    lot_no = " ";
                }
                if(lot_no == null){
                    lot_no = " ";
                }
                if (remarks == null || remarks.equals("")){
                    remarks = " ";
                }
                MainActivity_insertstatus main_insertstatus = new MainActivity_insertstatus(this);
                main_insertstatus.execute(db_name, curr_stats, model, lot_no, engr, remarks, ServerIP, "0");
                main_insertstatus.delegate = this;


            }else{
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                if (engr == null){
                    builder.setTitle("!!!");
                    builder.setMessage("Barcode ID not found");
                    builder.setNegativeButton("OK", (dialog, which) -> dialog.dismiss());
                }else {
                    builder.setTitle("Error Encountered");
                    builder.setMessage("Please contact your PIC");
                    builder.setNegativeButton("OK", (dialog, which) -> dialog.dismiss());
                }
                builder.show();
            }
        }catch (Exception e){
            Toast.makeText(this, e.getMessage() + "prompt eng err", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void MainActivity_insertstatus_processFinish(String output) {
        if(output != null){
            try{
                Toast.makeText(this, output, Toast.LENGTH_LONG).show();
                if(rep){redirectActivity(this, Engineer_RepairActivity.class); rep = false;}
                if(set){redirectActivity(this, EngineerActivity.class); set = false;}
            }catch (Exception e){
                e.printStackTrace();
                AlertDialog.Builder builder = new AlertDialog.Builder(PromptEngActivity.this);
                builder.setTitle("Sending Status Error!");
                builder.setMessage(e.getMessage());
                builder.show();
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        String msg = "10\n";
        try {
            MainActivity.mmOutputStream.write(msg.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //endregion

}