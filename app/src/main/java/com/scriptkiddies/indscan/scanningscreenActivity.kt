package com.scriptkiddies.indscan

import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.View
import net.vishesh.scanner.presentation.BaseScannerActivity

class scanningscreenActivity : BaseScannerActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                        View.SYSTEM_UI_FLAG_FULLSCREEN or
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION)
        setRequestedOrientation(
            ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

    }

    override fun onError(throwable: Throwable) {
        TODO("Not yet implemented")
    }

    override fun onDocumentAccepted(bitmap: String) {

        val i = Intent(this, FilterActivity::class.java)

        i.putExtra("image", bitmap )
        startActivity(i)
    }

    override fun onClose() {
        TODO("Not yet implemented")
    }
}