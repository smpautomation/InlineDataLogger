package com.example.realtimemonitoring;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class Engineer_RepairActivity extends AppCompatActivity implements MainActivity_insertstatus.AsyncResponse{
    String db_name, curr_stats, model, lot_no, engr, remarks,operator, bluetooth, ServerIP, quantity;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_engineer_repair);
        //region get extras
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        db_name = extras.getString("extra_db_name");
        curr_stats = extras.getString("extra_current_ctatus");
        model = extras.getString("extra_model");
        lot_no = extras.getString("extra_lot_no");
        engr = extras.getString("extra_engr");
        operator  = extras.getString("extra_optr");
        ServerIP = extras.getString("extra_serverip");
        quantity = extras.getString("extra_quantity");



        //endregion
    }

    public void machinefixed(View view) {
        try {
            if(curr_stats==null || !curr_stats.equals("Machine Fixed")){
                curr_stats = "Machine Fixed";
            }
            alertdialogremarksinput();
            remarks = "";

        } catch (Exception e) {
            Toast.makeText(this, "machine fixed error" + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
    public void alertdialogremarksinput(){
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
                main_insertstatus();
                //String ss = MainActivity.resetBluetoothConnection();
                //redirectActivity(this, MainActivity.class);
            });
            builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
            builder.show();
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    //region AsyncTask
    public void main_insertstatus(){
        try{
            if (!db_name.isEmpty() && !curr_stats.isEmpty() && engr != null) {
                if (model == null){
                    model = " ";
                    lot_no = " ";
                    quantity = "0";
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
                    builder.setMessage("PIC barcode ID name not found");
                    builder.setNegativeButton("OK", (dialog, which) -> dialog.dismiss());
                }else {
                    builder.setTitle("Error Encountered");
                    builder.setMessage("Please contact your PIC");
                    builder.setNegativeButton("OK", (dialog, which) -> dialog.dismiss());
                }
                builder.show();
            }
        }catch (Exception e){
            Toast.makeText(this, e.getMessage() + "insert", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void MainActivity_insertstatus_processFinish(String output) {
        if(output != null){
            try{
                Toast.makeText(this, output, Toast.LENGTH_LONG).show();
                this.finish();
            }catch (Exception e){
                e.printStackTrace();
                AlertDialog.Builder builder = new AlertDialog.Builder(Engineer_RepairActivity.this);
                builder.setTitle("Sending Status Error");
                builder.setMessage(e.getMessage());
                builder.show();
            }
        }
    }
    //endregion

    public void redirectActivity(Activity activity, Class aclass){
        /*Intent intent = new Intent(activity, aclass);
        Bundle extras = new Bundle();
        extras.putString("extra_db_name", db_name);
        extras.putString("extra_current_ctatus", curr_stats);
        extras.putString("extra_model", model);
        extras.putString("extra_lot_no", lot_no);
        extras.putString("extra_optr", operator);
        extras.putString("FLAG", "true");
        intent.putExtras(extras);
        startActivity(intent);*/

    }

}