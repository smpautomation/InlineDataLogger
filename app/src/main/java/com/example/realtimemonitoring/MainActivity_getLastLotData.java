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

public class MainActivity_getLastLotData extends AsyncTask<String, Void, String> {
    public AsyncResponse delegate = null;
    AlertDialog dialog;
    @SuppressLint("StaticFieldLeak")
    Context context;
    String JSON_STRING;

    public MainActivity_getLastLotData(Context context) {
        this.context = context;
    }

    @Override
    protected void onPreExecute() {
        dialog = new AlertDialog.Builder(context).create();
    }

    @Override
    protected String doInBackground(String... strings) {
        StringBuilder result = new StringBuilder();
        try {
            String serverip = strings[0];
            String db_name = strings[1];


            String connstr = "http://" + serverip + "/machiningrealtimemonitoring/getlastlotdata.php";
            URL url = new URL(connstr);
            HttpURLConnection http = (HttpURLConnection) url.openConnection();
            http.setRequestMethod("POST");
            http.setDoInput(true);
            http.setDoOutput(true);

            OutputStream ops = http.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(ops, StandardCharsets.UTF_8));
            String data = URLEncoder.encode("db_name", "UTF-8") + "=" + URLEncoder.encode(db_name, "UTF-8");
            writer.write(data);
            writer.flush();
            writer.close();
            ops.close();

            InputStream ips = http.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(ips, StandardCharsets.ISO_8859_1));
            while ((JSON_STRING = reader.readLine()) != null) {
                result.append(JSON_STRING).append("\n");
            }
            reader.close();
            ips.close();
            http.disconnect();
            return result.toString();

        } catch (Exception e) {
            result = new StringBuilder("Exception detected in fetching lot details");
        }
        return result.toString().trim();
    }

    @Override
    protected void onPostExecute(String s) {
        delegate.MainActivity_getLastLotData_processFinish(s);
        context = null;
    }

    public interface AsyncResponse {
        void MainActivity_getLastLotData_processFinish(String output);
    }
}
