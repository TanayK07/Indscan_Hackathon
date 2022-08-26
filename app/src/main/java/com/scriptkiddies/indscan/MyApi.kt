package com.brijconceptorg.brijconcept

import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import android.widget.Toast
import com.android.volley.AuthFailureError
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.scriptkiddies.indscan.ResponseListener
import com.scriptkiddies.indscan.ResponseListenerArray
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

class MyApi {
    var result: JSONObject? = null
    private var url: String? = null
    //private var baseUrl:String="https://9tpvdsrell.execute-api.ap-south-1.amazonaws.com"
    private var baseUrl:String="http://172.20.10.2:8000"
   // private var baseUrl:String="http://127.0.0.1:8000";
    var isCustomToken=false;
    var customToken="";
    //private static final String extension="https://api.brijconcept.com/api/";
    fun build_api(context: Context, extension: String) {
        url = baseUrl+ extension
    }

    fun execute(
        context: Context,
        method: Int,
        endurl: String,
        params: MutableMap<String, String>,
        listener: ResponseListener
    ) {
        build_api(context, extension)
        val parameters = JSONObject(params as Map<*, *>?)
        val jsonObjectRequest: JsonObjectRequest = object : JsonObjectRequest(
            method, url + endurl, parameters,
            Response.Listener { response ->
                try {
                    listener.onResponse(response)
                } catch (e: Exception) {
                }
            },
            Response.ErrorListener { error ->
                if (error.networkResponse != null) {
                    listener.onError(error.message)
                }
            }
        ) {
            //This is for Headers If You Needed
            @Throws(AuthFailureError::class)
            override fun getHeaders(): Map<String, String> {
                val params: MutableMap<String, String> = HashMap()
                params["Content-Type"] = "application/json; charset=UTF-8"
                try {
                    params["Authorization"] = "Bearer " + User.getToken(context)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                return params
            }
        }
        jsonObjectRequest.retryPolicy = DefaultRetryPolicy(
            5000,
            DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )
        val queue = Volley.newRequestQueue(context)
        queue.add(jsonObjectRequest)
    }

    fun execute(context: Context,method: Int, endurl: String, listener: ResponseListener) {
        build_api(context, extension)
        val parameters = JSONObject()
        try {
            val currentVersion =
                context.packageManager.getPackageInfo(context.packageName, 0).versionCode
            parameters.put("appVersion", currentVersion.toString())
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        val jsonObjectRequest: JsonObjectRequest = object : JsonObjectRequest(
            method, url + endurl, parameters,
            Response.Listener { response ->
                    listener.onResponse(response)
                Log.d("Debugging","Im executed");
            },
            Response.ErrorListener { error ->
                if(error.networkResponse?.statusCode == 401){
                    if(User.isUserLoggedIn(context)){
                        //User.logout(context);
                    }
                    else{
                        Toast.makeText(context,"Incorrect Credentials", Toast.LENGTH_LONG).show();
                    }

                }
                else if (error.networkResponse != null) {
                    listener.onError(error.message)
                }

            }
        ) {
            //This is for Headers If You Needed
            @Throws(AuthFailureError::class)
            override fun getHeaders(): Map<String, String> {
                val params: MutableMap<String, String> = HashMap()
                params["Content-Type"] = "application/json; charset=UTF-8"
                if(isCustomToken){
                    params["Authorization"] = "Bearer " + customToken;

                }else{

                    if(User.isUserLoggedIn(context)){
                        try {
                            params["Authorization"] = "Bearer " + User.getToken(context)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
                return params
            }
        }
        jsonObjectRequest.retryPolicy = DefaultRetryPolicy(
            5000,
            DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )
        val queue = Volley.newRequestQueue(context)
        queue.add(jsonObjectRequest)
    }

    @Throws(JSONException::class)
    fun execute_array(
        context: Context,
        method: Int,
        endurl: String,
        parameters: Map<String?, String?>?,
        listener: ResponseListenerArray
    ) {
        build_api(context, extension)
        val `object` = JSONObject(parameters)
        try {
            val currentVersion =
                context.packageManager.getPackageInfo(context.packageName, 0).versionCode
            `object`.put("appVersion", currentVersion.toString())
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        val array = JSONArray("[$`object`]")
        val jsonArrayRequest: JsonArrayRequest = object : JsonArrayRequest(
            method, url + endurl, array,
            Response.Listener { response ->
                try {
                    listener.onResponse(response)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            },
            Response.ErrorListener { error ->
                if (error.networkResponse != null) {
                    listener.onError(error.message)
                }
            }
        ) {
            //This is for Headers If You Needed
            @Throws(AuthFailureError::class)
            override fun getHeaders(): Map<String, String> {
                val params: MutableMap<String, String> = HashMap()
                params["Content-Type"] = "application/json; charset=UTF-8"
                try {
                    params["Authorization"] = "Bearer " + User.getToken(context)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                return params
            }
        }
        jsonArrayRequest.retryPolicy = DefaultRetryPolicy(
            5000,
            DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )
        val queue = Volley.newRequestQueue(context)
        queue.add(jsonArrayRequest)
    }

    fun execute_array(context: Context,method: Int, endurl: String, listener: ResponseListenerArray) {
        build_api(context, extension)
        val `object` = JSONObject()
        try {
            val currentVersion =
                context.packageManager.getPackageInfo(context.packageName, 0).versionCode
            `object`.put("appVersion", currentVersion.toString())
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        var array: JSONArray? = null
        try {
            array = JSONArray("[$`object`]")
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        val jsonArrayRequest: JsonArrayRequest = object : JsonArrayRequest(
            method, url + endurl, array,
            Response.Listener { response ->
                try {
                    listener.onResponse(response)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            },
            Response.ErrorListener { error ->
                if (error.networkResponse != null) {
                    listener.onError(error.message)
                }
            }
        ) {
            //This is for Headers If You Needed
            @Throws(AuthFailureError::class)
            override fun getHeaders(): Map<String, String> {
                val params: MutableMap<String, String> = HashMap()
                params["Content-Type"] = "application/json; charset=UTF-8"
                try {
                    params["Authorization"] = "Bearer " + User.getToken(context)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                return params
            }
        }
        jsonArrayRequest.retryPolicy = DefaultRetryPolicy(
            5000,
            DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )
        val queue = Volley.newRequestQueue(context)
        queue.add(jsonArrayRequest)
    }

    companion object {
        private const val TAG = "ApiDebugging"

        //private static final String extension="https://api.brijconcept.com/api/";
        private const val extension = "/api/"
    }
}