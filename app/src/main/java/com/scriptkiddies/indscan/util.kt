package com.scriptkiddies.indscan

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.android.volley.AuthFailureError
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.brijconceptorg.brijconcept.MyApi
import com.brijconceptorg.brijconcept.User
import org.json.JSONObject


class checkConnection(context: Context) {

    var redirected = false
    var context: Context? = null
    //init
    init {
        this.context = context
    }
    fun isConnectedToConsole(context: Context) {
        if(User.CURRENT_PAIRING_CODE!=-1 && User.CURRENT_SESSION_ID.length>0){
            val api=MyApi();
            val params="pairing_code="+ User.CURRENT_PAIRING_CODE+"&session_id="+User.CURRENT_SESSION_ID;
            api.execute(context, Request.Method.GET,"pairing_codes/isLinked?"+params,object:ResponseListener{
                override fun onResponse(response: JSONObject?) {
                    if (response != null) {
                        if(!response.getBoolean("is_linked")){
                            handleFail();
                        }
                    }
                    else{
                        handleFail();
                    }
                }

                override fun onError(message: String?) {
                    handleFail();
                }

            })    ;
        }
    }
    fun handleFail(){
        Toast.makeText(
            context,
            "Disconnected from console",
            Toast.LENGTH_LONG
        ).show()

        if(!redirected){
            redirected = true
            val intent = Intent(context, MainActivity::class.java)
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtra("EXIT", true);
            context?.startActivity(intent)
            (context as Activity).finish()
        }
    }

}
