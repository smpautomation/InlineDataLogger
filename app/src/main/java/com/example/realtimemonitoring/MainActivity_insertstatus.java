package com.example.realtimemonitoring;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public class MainActivity_insertstatus extends AsyncTask<String, Void, String> {
    public AsyncResponse delegate = null;
    AlertDialog dialog;
    @SuppressLint("StaticFieldLeak")
    Context context;
    String JSON_STRING;
    public MainActivity_insertstatus(Context context){
        this.context = context;
    }
    @Override
    protected void onPreExecute() {
        dialog = new AlertDialog.Builder(context).create();
    }

    @Override
    protected String doInBackground(String... strings) {
        String result = "";
        try{
            String db_name = strings[0];
            String Current_Status = strings[1];
            String Model = strings[2];
            String Lot_No = strings[3];
            String Operator = strings[4];
            String Remarks = strings[5];
            String serverip = strings[6];
            String quantity = strings[7];

            if(db_name != null){
                String connstr = "http://"+ serverip +"/machiningrealtimemonitoring/insertstatus.php";
                URL url = new URL(connstr);
                HttpURLConnection http = (HttpURLConnection) url.openConnection();
                http.setRequestMethod("POST");
                http.setDoInput(true);
                http.setDoOutput(true);

                OutputStream ops = http.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(ops, StandardCharsets.UTF_8));
                String data = URLEncoder.encode("db_name", "UTF-8") + "=" + URLEncoder.encode(db_name, "UTF-8")
                        + "&&" + URLEncoder.encode("Current_Status", "UTF-8") + "=" + URLEncoder.encode(Current_Status, "UTF-8")
                        + "&&" + URLEncoder.encode("Model", "UTF-8") + "=" + URLEncoder.encode(Model, "UTF-8")
                        + "&&" + URLEncoder.encode("Lot_No", "UTF-8") + "=" + URLEncoder.encode(Lot_No, "UTF-8")
                        + "&&" + URLEncoder.encode("Operator", "UTF-8") + "=" + URLEncoder.encode(Operator, "UTF-8")
                        + "&&" + URLEncoder.encode("Remarks", "UTF-8") + "=" + URLEncoder.encode(Remarks, "UTF-8")
                        + "&&" + URLEncoder.encode("Quantity", "UTF-8") + "=" + URLEncoder.encode(quantity, "UTF-8");
                writer.write(data);
                writer.flush();
                writer.close();
                ops.close();

                InputStream ips = http.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(ips, StandardCharsets.ISO_8859_1));
                String line;
                while ((line = reader.readLine()) != null) {
                    result = result + line;
                }
                reader.close();
                ips.close();
                http.disconnect();
                return result;
            }
        }catch(Exception e){
            result = e.getMessage();
        }
        return result;
    }

    @Override
    protected void onPostExecute(String s) { delegate.MainActivity_insertstatus_processFinish(s); context = null; }

    public interface AsyncResponse{ void MainActivity_insertstatus_processFinish(String output); }
}
