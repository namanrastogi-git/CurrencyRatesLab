package com.example.currencyrateslab;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements DataLoader.Listener {

    private EditText txtFilter;
    private ListView lvRates;
    private ArrayAdapter<String> adapter;
    private List<String> allRates = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtFilter = findViewById(R.id.txtFilter);
        lvRates = findViewById(R.id.lvRates);
        Button btnLoad = findViewById(R.id.btnLoad);

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, allRates);
        lvRates.setAdapter(adapter);

        btnLoad.setOnClickListener(v -> {
            new DataLoader(this, this)
                    .execute("https://api.exchangerate.host/latest?base=USD");
        });

        txtFilter.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { filterRates(s.toString()); }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    @Override
    public void onDataLoaded(String data) {
        allRates = Parser.parseRates(data);
        adapter.clear();
        adapter.addAll(allRates);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onError() {
        Toast.makeText(this, getString(R.string.toast_error), Toast.LENGTH_SHORT).show();
    }

    private void filterRates(String query) {
        if (allRates.isEmpty()) return;
        List<String> filtered = new ArrayList<>();
        for (String item : allRates) {
            if (item.toLowerCase().contains(query.toLowerCase())) filtered.add(item);
        }
        adapter.clear();
        adapter.addAll(filtered);
        adapter.notifyDataSetChanged();
    }
}
