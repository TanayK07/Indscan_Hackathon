package com.scriptkiddies.indscan

import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import android.view.Window
import net.vishesh.scanner.presentation.BaseScannerActivity

class scanningscreenActivity : BaseScannerActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().getDecorView()
            .setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            )
        setContentView(R.layout.scanningscreen)
    }

    override fun onError(throwable: Throwable) {
        TODO("Not yet implemented")
    }

    override fun onDocumentAccepted(bitmap: Bitmap) {
        TODO("Not yet implemented")
    }

    override fun onClose() {
        TODO("Not yet implemented")
    }
}