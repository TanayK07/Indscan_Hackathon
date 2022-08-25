package com.scriptkiddies.indscan;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;

public interface ResponseListener {
    void onError(String message);
    void onResponse(JSONObject response) throws JSONException, ParseException;
}
