package com.scriptkiddies.indscan

import android.Manifest
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import com.brijconceptorg.brijconcept.User
import com.scottyab.rootbeer.RootBeer
import com.zeugmasolutions.localehelper.LocaleAwareCompatActivity
import com.zeugmasolutions.localehelper.Locales
import java.util.*


class MainActivity : LocaleAwareCompatActivity() {
    @RequiresApi(Build.VERSION_CODES.O)

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        /*window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                        View.SYSTEM_UI_FLAG_FULLSCREEN or
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION)*/
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setRequestedOrientation(
            ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_main)
        checkRunTimePermission();
        val btn:View = findViewById(R.id.accept_scan_btn)
        val logout_btn:Button=findViewById(R.id.logout_btn);
        if(!User.isUserLoggedIn(applicationContext)){
            logout_btn.visibility=View.GONE;
        }
        else{
            logout_btn.setOnClickListener{
                User.logout(applicationContext);
            }
        }
        btn.setOnClickListener {
            val rootProvider=RootBeer(applicationContext);
            //if(!rootProvider.isRooted()){
            if(1 == 1){
                val intent = Intent(applicationContext, if(User.isUserLoggedIn(this)) PairingCode::class.java else LoginActivity::class.java);
                startActivity(intent)
            }
            else{
                Toast.makeText(applicationContext,"Root access detected, try on another device",Toast.LENGTH_LONG).show();
            }

        }
        val current: Locale = resources.configuration.locale
        Log.d("Current Locale", current.toString())
        if (current.toString() == "en_US") {
            findViewById<TextView>(R.id.english).setTextColor(resources.getColor(R.color.black))
            findViewById<TextView>(R.id.hindi).setTextColor(resources.getColor(R.color.semiblack))
        }
        else {
            findViewById<TextView>(R.id.english).setTextColor(resources.getColor(R.color.semiblack))
            findViewById<TextView>(R.id.hindi).setTextColor(resources.getColor(R.color.black))
        }

        findViewById<View>(R.id.english).setOnClickListener {
            updateLocale(Locales.English)
            findViewById<TextView>(R.id.english).setTextColor(resources.getColor(R.color.black))
            findViewById<TextView>(R.id.hindi).setTextColor(resources.getColor(R.color.semiblack))
        }
        findViewById<View>(R.id.hindi).setOnClickListener {
            updateLocale(Locales.Hindi)
            findViewById<TextView>(R.id.english).setTextColor(resources.getColor(R.color.semiblack))
            findViewById<TextView>(R.id.hindi).setTextColor(resources.getColor(R.color.black))
        }

        //var decrypt: Decrypt = Decrypt()
        //val decryptedIP = decrypt.decode("GNMai6EzGmKNZPWZIBx0AQ==","2753621234567891")
        //Log.d("ipcode",decryptedIP)

    }
    private fun checkRunTimePermission() {
        val permissionArrays =
            arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        requestPermissions(permissionArrays, 11111)
    }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        val openActivityOnce = true
        val openDialogOnce = true
        var isPermitted:Boolean = false
        if (requestCode == 11111) {
            for (i in grantResults.indices) {
                val permission = permissions[i]
                isPermitted = grantResults[i] == PackageManager.PERMISSION_GRANTED
                if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                    // user rejected the permission
                    val showRationale = shouldShowRequestPermissionRationale(permission!!)
                    if (!showRationale) {
                        //execute when 'never Ask Again' tick and permission dialog not show
                    } else {
                        if (openDialogOnce) {
                            alertView()
                        }
                    }
                }
            }

        }
    }
    private fun alertView() {
        val dialog = AlertDialog.Builder(this)
        dialog.setTitle("Permission Denied")
            .setInverseBackgroundForced(true) //.setIcon(R.drawable.ic_info_black_24dp)
            .setMessage("Without those permission the app is unable to " +
                    "use camera. Are you sure you want to deny this permission?")
            .setNegativeButton(
                "I'M SURE"
            ) { dialoginterface, i -> dialoginterface.dismiss() }
            .setPositiveButton(
                "RE-TRY"
            ) { dialoginterface, i ->
                dialoginterface.dismiss()
                checkRunTimePermission()
            }.show()
    }

}

