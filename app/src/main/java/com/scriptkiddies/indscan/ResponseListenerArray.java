package com.scriptkiddies.indscan;

import org.json.JSONArray;
import org.json.JSONException;

public interface ResponseListenerArray {
    void onError(String message);
    void onResponse(JSONArray response) throws JSONException;
}
