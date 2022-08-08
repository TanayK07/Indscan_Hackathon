package com.scriptkiddies.indscan

import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import io.github.g00fy2.quickie.QRResult
import io.github.g00fy2.quickie.ScanQRCode

class PairingCode : AppCompatActivity() {
    val scanQrCode = registerForActivityResult(ScanQRCode(), ::handleResult)

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                        View.SYSTEM_UI_FLAG_FULLSCREEN or
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION)
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
            val intent = Intent(this, scanningscreenActivity::class.java)
            intent.putExtra("ipcode", text)
            var code = findViewById<TextView>(R.id.code).text.toString()
            intent.putExtra("pairingcode", code)
            startActivity(intent)
        }
    }
}
