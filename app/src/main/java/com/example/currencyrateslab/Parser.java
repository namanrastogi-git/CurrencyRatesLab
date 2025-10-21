package com.example.currencyrateslab;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Parser {

    public static List<String> parseRates(String jsonString) {
        List<String> result = new ArrayList<>();
        try {
            JSONObject root = new JSONObject(jsonString);
            JSONObject rates = root.getJSONObject("rates");
            Iterator<String> keys = rates.keys();
            while (keys.hasNext()) {
                String currency = keys.next();
                double value = rates.getDouble(currency);
                result.add(currency + " - " + value);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;
    }
}