package com.scriptkiddies.indscan

import android.graphics.Bitmap
import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import net.vishesh.scanner.presentation.BaseScannerActivity

class scanningscreenActivity : BaseScannerActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN);
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