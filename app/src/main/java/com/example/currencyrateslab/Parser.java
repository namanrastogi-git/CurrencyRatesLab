package com.example.currencyrateslab;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

class Parser {

    static List<String> parseRates(String jsonString) {
        List<String> out = new ArrayList<>();
        if (jsonString == null) return out;

        // Clean BOM/whitespace
        jsonString = jsonString.trim().replace("\uFEFF", "");

        try {
            JSONObject root = new JSONObject(jsonString);

            // Common shapes we support:
            // A) exchangerate.host: { success:true, base:"USD", rates:{ "EUR":0.93, ... } }
            // B) open.er-api.com:   { result:"success", base_code:"USD", rates:{ "EUR":0.93, ... } }

            JSONObject rates = null;
            if (root.has("rates") && root.get("rates") instanceof JSONObject) {
                rates = root.getJSONObject("rates");
            } else if (root.has("data") && root.get("data") instanceof JSONObject) {
                // Some APIs use "data" instead of "rates"
                rates = root.getJSONObject("data");
            }

            if (rates == null) return out;

            Iterator<String> keys = rates.keys();
            while (keys.hasNext()) {
                String code = keys.next();
                // values can be int/double/long; get as Number then format
                Object v = rates.get(code);
                if (v instanceof Number) {
                    out.add(code + " - " + ((Number) v).doubleValue());
                } else {
                    // if it's a string, try to parse as double
                    try {
                        double d = Double.parseDouble(String.valueOf(v));
                        out.add(code + " - " + d);
                    } catch (Exception ignored) {}
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return out;
    }
}
