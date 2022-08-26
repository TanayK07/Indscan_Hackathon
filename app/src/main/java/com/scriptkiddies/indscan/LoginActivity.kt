package com.scriptkiddies.indscan

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.brijconceptorg.brijconcept.MyApi
import com.brijconceptorg.brijconcept.User
import org.json.JSONObject


class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login);
        var loading = findViewById<View>(R.id.progress)
        loading.visibility = View.GONE
        fun postVolley(email: String, password: String) {
            val api=MyApi();
            api.execute(applicationContext, Request.Method.POST,"login?email="+email+"&password="+password,object: ResponseListener{
                override fun onResponse(response: JSONObject?) {
                    if(response!!.getBoolean("status")){
                        //Login SuccessFull
                        val token=response.getString("token");
                        if(User.saveToken(applicationContext,token)){
                            val intent= Intent(applicationContext,PairingCode::class.java);
                            startActivity(intent);
                            finish();
                        }
                        else{
                            handleLoginFailed();
                        }

                    }
                    else{
                        handleLoginFailed();
                    }
                }
                override fun onError(message: String?) {

                    if (message != null) {
                        Log.d("Error",message)
                    };
                }
            })

        }
        findViewById<View>(R.id.continue_btn).setOnClickListener {
            val email: String = (findViewById<EditText>(R.id.email)).text.toString()
            val password: String = (findViewById<EditText>(R.id.pass_field)).text.toString()
            postVolley(email, password);
        }
    }
    fun handleLoginFailed(){
        Toast.makeText(applicationContext,"Incorrect Credentials",Toast.LENGTH_LONG).show();
    }
}