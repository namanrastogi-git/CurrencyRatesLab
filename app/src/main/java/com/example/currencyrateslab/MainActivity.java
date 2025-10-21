package com.example.currencyrateslab;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements DataLoader.Listener {

    // Two free endpoints (no API key). We’ll try #1; if it fails to parse, we’ll try #2.
    private static final String RATES_URL_1 = "https://api.exchangerate.host/latest?base=USD";
    private static final String RATES_URL_2 = "https://open.er-api.com/v6/latest/USD";

    private EditText txtFilter;
    private ListView lvRates;
    private Button btnLoad;

    private ArrayAdapter<String> adapter;
    private List<String> allRates = new ArrayList<>();

    // Keep last raw response to show in a dialog if parsing fails
    private String lastRaw = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtFilter = findViewById(R.id.txtFilter);
        lvRates   = findViewById(R.id.lvRates);
        btnLoad   = findViewById(R.id.btnLoad);

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, new ArrayList<>());
        lvRates.setAdapter(adapter);

        btnLoad.setOnClickListener(v -> loadRatesPrimary());
        loadRatesPrimary(); // auto-load once

        txtFilter.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { filterRates(s.toString()); }
            @Override public void afterTextChanged(Editable s) {}
        });

        // Long-press the Load button to show the last raw response for debugging
        btnLoad.setOnLongClickListener(v -> {
            showRawPreview();
            return true;
        });
    }

    private void loadRatesPrimary() {
        new DataLoader(getApplicationContext(), this).execute(RATES_URL_1);
    }

    private void loadRatesSecondary() {
        new DataLoader(getApplicationContext(), this).execute(RATES_URL_2);
    }

    @Override
    public void onDataLoaded(String data, String previewLog) {
        // Save last response for debugging dialog
        lastRaw = data == null ? "" : data;
        Log.d("MainActivity", "Response preview:\n" + previewLog);

        List<String> parsed = Parser.parseRates(data);
        if (!parsed.isEmpty()) {
            allRates = parsed;
            adapter.clear();
            adapter.addAll(allRates);
            adapter.notifyDataSetChanged();

            String q = txtFilter.getText().toString();
            if (!q.isEmpty()) filterRates(q);
        } else {
            // First endpoint returned something we couldn't parse -> try the fallback endpoint once
            if (previewLog != null && !previewLog.contains("ERAPI_FALLBACK_DONE")) {
                // Mark that we already tried fallback to avoid loop
                Log.w("MainActivity", "Primary parse empty; trying secondary endpoint...");
                loadRatesSecondary();
            } else {
                Toast.makeText(this, "Loaded but no rates parsed.", Toast.LENGTH_LONG).show();
                showRawPreview();
            }
        }
    }

    @Override
    public void onError(String what) {
        Toast.makeText(this, "Load failed: " + what, Toast.LENGTH_LONG).show();
        Log.e("MainActivity", "Load failed: " + what);
        showRawPreview();
    }

    private void filterRates(String query) {
        String q = query.trim().toLowerCase();
        List<String> filtered = new ArrayList<>();
        for (String line : allRates) {
            if (line.toLowerCase().contains(q)) filtered.add(line);
        }
        adapter.clear();
        adapter.addAll(filtered);
        adapter.notifyDataSetChanged();
    }

    private void showRawPreview() {
        if (lastRaw == null || lastRaw.isEmpty()) {
            Toast.makeText(this, "No raw response captured yet.", Toast.LENGTH_SHORT).show();
            return;
        }
        String snippet = lastRaw.length() > 800 ? lastRaw.substring(0, 800) + " …" : lastRaw;
        new AlertDialog.Builder(this)
                .setTitle("Raw API Response (first 800 chars)")
                .setMessage(snippet)
                .setPositiveButton(android.R.string.ok, null)
                .show();
    }
}
