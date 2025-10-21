package com.example.currencyrateslab;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

class DataLoader extends AsyncTask<String, Void, String> {

    interface Listener {
        // previewLog contains first ~200 chars to help debug which endpoint we hit
        void onDataLoaded(String data, String previewLog);
        void onError(String what);
    }

    private final Listener listener;
    private final Context appCtx;
    private String errorMsg = null;
    private String preview = "";

    DataLoader(Context context, Listener listener) {
        this.appCtx = context.getApplicationContext();
        this.listener = listener;
    }

    @Override
    protected void onPreExecute() {
        Toast.makeText(appCtx, appCtx.getString(R.string.toast_loading), Toast.LENGTH_SHORT).show();
    }

    @Override
    protected String doInBackground(String... urls) {
        HttpURLConnection conn = null;
        try {
            String urlString = urls[0];
            URL url = new URL(urlString);
            conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(12000);
            conn.setReadTimeout(15000);
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");
            conn.setRequestProperty("User-Agent", "CurrencyRatesLab/1.0");

            int code = conn.getResponseCode();
            if (code != HttpURLConnection.HTTP_OK) {
                errorMsg = "HTTP " + code;
                // Try to read error stream body for debugging
                try {
                    BufferedReader er = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
                    StringBuilder esb = new StringBuilder();
                    String line;
                    while (er != null && (line = er.readLine()) != null) esb.append(line).append('\n');
                    preview = "ERROR BODY: " + esb.toString();
                    if (er != null) er.close();
                } catch (Exception ignored) {}
                return null;
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String line; int ln = 0;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append('\n');
                if (preview.length() < 400 && ln < 12) preview += line + "\n";
                ln++;
            }
            reader.close();

            // Mark fallback attempt in preview to prevent retry loop
            if (urlString.contains("open.er-api.com")) {
                preview = "ERAPI_FALLBACK_DONE\n" + preview;
            }
            return sb.toString();
        } catch (Exception e) {
            errorMsg = e.getClass().getSimpleName() + ": " + e.getMessage();
            Log.e("DataLoader", "Fetch failed", e);
            return null;
        } finally {
            if (conn != null) conn.disconnect();
        }
    }

    @Override
    protected void onPostExecute(String result) {
        if (result != null) {
            Log.d("DataLoader", "First bytes:\n" + preview);
            listener.onDataLoaded(result, preview);
        } else {
            listener.onError(errorMsg == null ? "Unknown error" : errorMsg);
        }
    }
}

