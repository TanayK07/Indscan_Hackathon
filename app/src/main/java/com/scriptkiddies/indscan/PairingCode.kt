package com.scriptkiddies.indscan

import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.AuthFailureError
import com.android.volley.NetworkResponse
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.HttpHeaderParser
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import eu.amirs.JSON
import io.github.g00fy2.quickie.QRResult
import io.github.g00fy2.quickie.ScanQRCode
import org.json.JSONException

class PairingCode : AppCompatActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    val scanQrCode = registerForActivityResult(ScanQRCode(), ::handleResult)

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        /*window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                        View.SYSTEM_UI_FLAG_FULLSCREEN or
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION)*/
        setRequestedOrientation(
            ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.activity_pairing_code)
        var codeTE: TextView = findViewById(R.id.code)
        //generate random six digit code

        codeTE.text = (100000..999999).shuffled().last().toString()
        findViewById<View>(R.id.continue_btn).setOnClickListener {
            scanQrCode.launch(null)
        }
    }
    @RequiresApi(Build.VERSION_CODES.O)
    fun handleResult(result: QRResult) {
        val text = when (result) {
            is QRResult.QRSuccess -> result.content.rawValue
            QRResult.QRUserCanceled -> ""
            QRResult.QRMissingPermission -> ""
            is QRResult.QRError -> ""
        }
        if(text!=""){
            //log the code
            // var decrypt: Decrypt = Decrypt()
            //        //decrypt.decode()
            //        //Log.d("decrypt", decrypt.decode())

            //start scan activity
            //get ipcode
            val ipcode = text
            //get pairing code
            var code = findViewById<TextView>(R.id.code).text.toString()
            val pairingcode = code
            val json = JSON(ipcode)
            val encrtptedIP = json.key("ip").stringValue();
            var decrypt: Decrypt = Decrypt()
            val decryptedIP = decrypt.decode(encrtptedIP,pairingcode+pairingcode+"4132")
            val decryptedToken = decrypt.decode(json.key("token").stringValue(),pairingcode+pairingcode+"4132")

            //send request to decryptedIP/connect with decryptedToken
            try {
                val requestQueue = Volley.newRequestQueue(applicationContext)
                val URL = "http://"+decryptedIP+":3001/connectX?token="+decryptedToken
                //val jsonBody = JSONObject()
                //jsonBody.put("Title", "Android Volley Demo")
                //jsonBody.put("Author", "BNK")
                //val requestBody = jsonBody.toString()
                val stringRequest: StringRequest = object :
                    StringRequest(Method.GET, URL, Response.Listener() {
                        Log.d("VOLLEY", it)
                        this@PairingCode.runOnUiThread(java.lang.Runnable {

                            val intent = Intent(this, scanningscreenActivity::class.java)
                            intent.putExtra("ipcode", text)

                            intent.putExtra("pairingcode", code)
                            startActivity(intent)
                            finish()
                        })
                        fun onResponse(response: String?) {

                            Log.d("VOLLEY", response!!)
                        }
                    }, Response.ErrorListener() {
                        Log.d("VOLLEY", it.toString())
                        //Show error toast
                        Toast.makeText(
                            applicationContext,
                            "Error connecting to web console, make sure you are connected on the same network",
                            Toast.LENGTH_LONG
                        ).show()

                        //Redirect to MainActivity if error occurs
                        val intent = Intent(this, MainActivity::class.java)
                        startActivity(intent)
                        finish()

                        fun onErrorResponse(error: VolleyError) {
                            Log.d("VOLLEY", error.toString())
                        }
                    }) {
                    override fun getBodyContentType(): String {
                        return "application/json; charset=utf-8"
                    }

                    @Throws(AuthFailureError::class)
                    override fun getBody(): ByteArray {
                        return "{}".toByteArray()
                    }

                    override fun parseNetworkResponse(response: NetworkResponse): Response<String> {
                        var responseString = ""
                        if (response != null) {
                            responseString = response.data.toString()

                            // can get more details such as response.headers
                        }
                        return Response.success(
                            responseString,
                            HttpHeaderParser.parseCacheHeaders(response)
                        )
                    }
                }
                requestQueue.add(stringRequest)
            } catch (e: JSONException) {
                e.printStackTrace()
            }


        }
    }
}
