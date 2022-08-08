package com.scriptkiddies.indscan

import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.View
import net.vishesh.scanner.presentation.BaseScannerActivity

class scanningscreenActivity : BaseScannerActivity() {
    lateinit var ipcode: String
    lateinit var pairingCode: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                        View.SYSTEM_UI_FLAG_FULLSCREEN or
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION)
        setRequestedOrientation(
            ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        //get ipcode from intent
        ipcode = intent.getStringExtra("ipcode")!!
        pairingCode = intent.getStringExtra("pairingcode")!!

    }

    override fun onError(throwable: Throwable) {
        TODO("Not yet implemented")
    }

    override fun onDocumentAccepted(bitmap: String) {

        val i = Intent(this, FilterActivity::class.java)
        i.putExtra("ipcode", ipcode)
        i.putExtra("pairingcode", pairingCode)
        i.putExtra("image", bitmap )
        startActivity(i)
    }

    override fun onClose() {
        TODO("Not yet implemented")
    }
}