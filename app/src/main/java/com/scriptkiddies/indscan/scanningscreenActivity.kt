package com.scriptkiddies.indscan

import android.app.Activity
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import eu.amirs.JSON
import net.vishesh.scanner.presentation.BaseScannerActivity

class scanningscreenActivity : BaseScannerActivity() {
    lateinit var ipcode: String
    lateinit var pairingCode: String
    var resultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // parse result and perform action
        }
    }
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        /*window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                        View.SYSTEM_UI_FLAG_FULLSCREEN or
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION)*/
        setRequestedOrientation(
            ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        //get ipcode from intent
        ipcode = intent.getStringExtra("ipcode")!!
        pairingCode = intent.getStringExtra("pairingcode")!!
        val json = JSON(ipcode)
        val encrtptedIP = json.key("ip").stringValue();
        var decrypt: Decrypt = Decrypt()
        val decryptedIP = decrypt.decode(encrtptedIP,pairingCode+pairingCode+"4132")
        var activityContext = this

        val mainHandler = Handler(Looper.getMainLooper())
        val checkConnection = checkConnection(activityContext)

        mainHandler.post(object : Runnable {
            override fun run() {
                checkConnection.isConnectedToConsole(activityContext)

                if(!checkConnection.redirected)
                    mainHandler.postDelayed(this, 3000)
            }
        });






    }

    override fun onError(throwable: Throwable) {
        TODO("Not yet implemented")
    }

    override fun onDocumentAccepted(bitmap: String) {

        val i = Intent(this, FilterActivity::class.java)
        i.putExtra("ipcode", ipcode)
        i.putExtra("pairingcode", pairingCode)
        i.putExtra("image", bitmap )
        resultLauncher.launch(i)
        //startActivity(i)
        //finish()
    }

    override fun onClose() {
        TODO("Not yet implemented")
    }
}