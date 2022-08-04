package com.scriptkiddies.indscan

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
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.activity_pairing_code)
        var codeTE:TextView = findViewById(R.id.code)
        //generate random six digit code
        var code = (100000..999999).random().toString()
        codeTE.text = code
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
            Log.d("code",text)
        }
    }
}