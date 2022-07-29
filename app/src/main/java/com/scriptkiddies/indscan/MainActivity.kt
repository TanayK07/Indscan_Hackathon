package com.scriptkiddies.indscan

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.view.Window
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.activity_main)
        checkRunTimePermission();
        val btn:View = findViewById(R.id.connect_btn)
        btn.setOnClickListener {
            val intent = Intent(this, scanningscreenActivity::class.java)
            startActivity(intent)
        }
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

