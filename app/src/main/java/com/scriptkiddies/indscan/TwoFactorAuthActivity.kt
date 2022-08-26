package com.scriptkiddies.indscan

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.android.volley.Request
import com.brijconceptorg.brijconcept.MyApi
import org.json.JSONObject


class TwoFactorAuthActivity : AppCompatActivity() {
    companion object{
        var TAG="TwoFactorAuthDebug";
    }
    val resultIntent = Intent()
    val myapi=MyApi();
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.twofactor_auth);
        val container=findViewById<ConstraintLayout>(R.id.container_setup_auth);
        val sk_text=findViewById<TextView>(R.id.secret_key);
        val continue_btn=findViewById<View>(R.id.continue_btn);

        val copyBtn=findViewById<View>(R.id.group234);
        val otp=findViewById<EditText>(R.id.rectanglesdflkn_1);
        val temptoken=intent.getStringExtra("temp-token")
        myapi.isCustomToken=true;
        myapi.customToken=temptoken.toString();
        container.visibility= View.GONE
        fun onnew2fa(){
            container.visibility=View.VISIBLE;
        }
        copyBtn.setOnClickListener {
            if(sk_text.text.toString().isNotEmpty()){
                val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("auth_secret_key", sk_text.text.toString())
                clipboard.setPrimaryClip(clip)
            }

        }
        continue_btn.setOnClickListener {
            if(otp.text.toString().isNotEmpty()){
                verifyOtp(otp.text.toString());
            }

        }

        myapi.execute(applicationContext, Request.Method.GET,"users/2fa",object: ResponseListener{
            override fun onError(message: String?) {
                Log.d(TAG,"Error in 2fa");

            }

            override fun onResponse(response: JSONObject?) {
                if (response != null) {
                    if(response.getBoolean("success")){
                        sk_text.text=response.getString("secret_key");
                        onnew2fa()
                        /*

                         */
                    }
                }
            }

        });

    }
    fun verifyOtp(otp:String){

        myapi.execute(applicationContext, Request.Method.POST,"users/2fa?secret_key="+otp,object: ResponseListener{
            override fun onError(message: String?) {
                Log.d(TAG,"Verification failed");
                setResult(RESULT_CANCELED,resultIntent)
            }

            override fun onResponse(response: JSONObject?) {
                if(response?.getBoolean("success") == true){
                    if(response.has("token")){
                        resultIntent.putExtra("token",response.getString("token"));
                    }
                    setResult(RESULT_OK, resultIntent)
                }
                else{
                    setResult(RESULT_CANCELED,resultIntent)
                }
                finish();
            }

        })
    }

}