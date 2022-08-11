package com.scriptkiddies.indscan

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.android.volley.AuthFailureError
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley

class checkConnection(context: Context) {

    var redirected = false
    var context: Context? = null
    //init
    init {
        this.context = context
    }
    fun isConnectedToConsole(context: Context, ip: String) {
        val queue = Volley.newRequestQueue(context)

        val url = "http://"+ip+":3001/isconnected"
        val sr: StringRequest =
            object : StringRequest(
                Method.GET, url,
                Response.Listener { response ->
                    Log.e(
                        "HttpClient",
                        "success! response: $response"
                    )
                    if(response.toString().contains("false")==true){
                        Toast.makeText(
                            context,
                            "Disconnected from console",
                            Toast.LENGTH_LONG
                        ).show()

                        //Redirect to MainActivity if error occurs
                        if(!redirected){
                            redirected = true
                            val intent = Intent(context, MainActivity::class.java)
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            intent.putExtra("EXIT", true);
                            context.startActivity(intent)
                            (context as Activity).finish()
                        }

                    }


                },
                Response.ErrorListener { error ->
                    if(!redirected){
                        redirected = true
                        val intent = Intent(context, MainActivity::class.java)
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        intent.putExtra("EXIT", true);
                        context.startActivity(intent)
                        (context as Activity).finish()
                    }
                    Log.e("HttpClient", "error: $error")

                }) {
                override fun getParams(): Map<String, String>? {
                    val params: MutableMap<String, String> = HashMap()

                    return params
                }

                @Throws(AuthFailureError::class)
                override fun getHeaders(): Map<String, String> {
                    val params: MutableMap<String, String> = HashMap()
                    params["Content-Type"] = "application/x-www-form-urlencoded"
                    return params
                }
            }
        queue.add(sr)
    }

}
