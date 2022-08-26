package com.scriptkiddies.indscan

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.brijconceptorg.brijconcept.MyApi
import com.brijconceptorg.brijconcept.User
import org.json.JSONObject


class LoginActivity : AppCompatActivity() {
    fun handle2faFailed(){
        Toast.makeText(applicationContext,"Two factor authenticaion failed",Toast.LENGTH_SHORT).show();
    }
    var resultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            if(result.data?.hasExtra("token") == true){
                val token= result.getData()?.getStringExtra("token");
                if(token != null){
                    if(User.saveToken(applicationContext,token)){
                        val intent= Intent(applicationContext,PairingCode::class.java);
                        startActivity(intent);
                        finish()
                    }
                    else{
                        handle2faFailed();
                    }
                }
                else{
                    handle2faFailed()
                }

            }
            else{
                handle2faFailed();
            }

        }
        else if(result.resultCode == Activity.RESULT_CANCELED) {
            handle2faFailed();
        }
    }
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
                        /*if(User.saveToken(applicationContext,token)){
                            /*;*/

                        }
                        else{
                            handleLoginFailed();
                        }*/
                        val intent=Intent(applicationContext,TwoFactorAuthActivity::class.java);
                        intent.putExtra("temp-token",token)
                        resultLauncher.launch(intent)


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