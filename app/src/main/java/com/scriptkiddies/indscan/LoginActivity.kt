package com.scriptkiddies.indscan

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request.Method.POST
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.brijconceptorg.brijconcept.User
import eu.amirs.JSON
import java.nio.charset.Charset


class LoginActivity : AppCompatActivity() {
    var url : String = "http://172.17.19.118:8000/api/"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login);
        var loading = findViewById<View>(R.id.progress)
        loading.visibility = View.GONE
        fun postVolley(email: String, password: String) {
            val queue = Volley.newRequestQueue(this)
            var urlX = url+"login?email=${email}&password=${password}"
            var requestBody = ""
            loading.visibility=View.VISIBLE;
            val stringReq : StringRequest =
                object : StringRequest(
                    POST, urlX,
                    Response.Listener { response ->
                        loading.visibility=View.GONE;
                        // response
                        var strResp = response.toString()
                        val json = JSON(strResp)
                        val token = json.key("token").stringValue()
                        val isSuccess=json.key("status").booleanValue()
                        if(isSuccess){
                            if(User.saveToken(applicationContext, token)){
                                val intent = Intent(applicationContext, PairingCode::class.java);
                                startActivity(intent)
                            }
                        }
                        else{
                            handleLoginFailed();
                        }
                    },
                    Response.ErrorListener { error ->
                        //Log.d("API", "error => $error")
                        loading.visibility=View.GONE;
                        handleLoginFailed();
                    }
                ){
                    override fun getBody(): ByteArray {
                        return requestBody.toByteArray(Charset.defaultCharset())
                    }
                }
            queue.add(stringReq)
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