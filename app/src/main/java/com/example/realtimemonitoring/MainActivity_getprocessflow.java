package com.example.realtimemonitoring;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

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

public class MainActivity_getprocessflow extends AsyncTask<String, Void, String> {
    public AsyncResponse delegate = null;
    AlertDialog dialog;
    @SuppressLint("StaticFieldLeak")
    Context context;
    String JSON_STRING;
    public MainActivity_getprocessflow(Context context){
        this.context = context;
    }
    @Override
    protected void onPreExecute() {
        dialog = new AlertDialog.Builder(context).create();
    }

    @Override
    protected String doInBackground(String... strings) {
        StringBuilder result = new StringBuilder();
        try{
            String RoutingCode = strings[0];
            String serverip = strings[1];

            if(RoutingCode != null){
                String connstr = "http://"+ serverip +"/machiningrealtimemonitoring/routing.php";
                URL url = new URL(connstr);
                HttpURLConnection http = (HttpURLConnection) url.openConnection();
                http.setRequestMethod("POST");
                http.setDoInput(true);
                http.setDoOutput(true);

                OutputStream ops = http.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(ops, StandardCharsets.UTF_8));
                String data = URLEncoder.encode("RoutingCode", "UTF-8") + "=" + URLEncoder.encode(RoutingCode, "UTF-8");
                writer.write(data);
                writer.flush();
                writer.close();
                ops.close();

                InputStream ips = http.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(ips, StandardCharsets.ISO_8859_1));
                while((JSON_STRING = reader.readLine()) != null){
                    result.append(JSON_STRING).append("\n");
                }
                reader.close();
                ips.close();
                http.disconnect();
                return result.toString();
            }
        }catch(Exception e){
            result = new StringBuilder("Exception detected in fetching process flow");
        }
        return result.toString().trim();
    }

    @Override
    protected void onPostExecute(String s) { delegate.MainActivity_getprocessflow_processFinish(s); context = null;}

    public interface AsyncResponse{ void MainActivity_getprocessflow_processFinish(String output); }
}
