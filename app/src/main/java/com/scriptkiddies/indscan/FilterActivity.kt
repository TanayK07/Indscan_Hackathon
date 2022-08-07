package com.scriptkiddies.indscan

import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.namangarg.androiddocumentscannerandfilter.DocumentFilter
import java.io.File

class FilterActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_filter)
        window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                        View.SYSTEM_UI_FLAG_FULLSCREEN or
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION)
        setRequestedOrientation(
            ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        var gb1: Bitmap? = null

        if (intent.hasExtra("image")){
            //convert to bitmap

            val image = File(intent.getStringExtra("image").toString())
            val bmOptions = BitmapFactory.Options()
            gb1 = BitmapFactory.decodeFile(image.absolutePath, bmOptions)
            image.delete()
        }
        var converted: Bitmap? = gb1
        val imageView = findViewById<ImageView>(R.id.imagemain)
        imageView.setImageBitmap(gb1)
        val documentFilter = DocumentFilter()
        var g1: android.widget.ImageButton? = null;
        findViewById<ImageButton>(R.id.scanpure).also { g1 = it };
        var g2: android.widget.ImageButton? = null;
        findViewById<ImageButton>(R.id.scangrey).also { g2 = it };
        var g3: android.widget.ImageButton? = null;
        findViewById<ImageButton>(R.id.scanmagiccolor).also { g3 = it };
        var g4: android.widget.ImageButton? = null;
        findViewById<ImageButton>(R.id.scancool).also { g4 = it };
        g1?.setOnClickListener {
            imageView.setImageBitmap(gb1)
        }
        g2!!.setOnClickListener {
            documentFilter!!.getGreyScaleFilter(
                gb1
            ) { bitmap ->
                converted = bitmap
                (imageView as ImageView?)?.setImageBitmap(bitmap) }
        }
        g4!!.setOnClickListener {
            documentFilter!!.getLightenFilter(
                gb1
            ) { bitmap ->
                converted = bitmap
                (imageView as ImageView?)?.setImageBitmap(bitmap) }
        }
        g3!!.setOnClickListener {
            documentFilter!!.getMagicFilter(
                gb1
            ) { bitmap ->
                converted = bitmap
                (imageView as ImageView?)?.setImageBitmap(bitmap) }
        }
        var rotLeft: View = findViewById(R.id.left)
        rotLeft.setOnClickListener {
            if (converted != null) {
                //rotate image
                val matrix = android.graphics.Matrix()
                matrix.postRotate(-90f)
                val rotated = Bitmap.createBitmap(
                    converted!!, 0, 0,
                    converted!!.width,
                    converted!!.height, matrix, true
                )
                converted = rotated
                (imageView as ImageView?)?.setImageBitmap(converted)
            }
        }
        var rotRight: View = findViewById(R.id.right)
        rotRight.setOnClickListener {
            if (converted != null) {
                //rotate image
                val matrix = android.graphics.Matrix()
                matrix.postRotate(90f)
                val rotated = Bitmap.createBitmap(
                    converted!!, 0, 0,
                    converted!!.width,
                    converted!!.height, matrix, true
                )
                converted = rotated
                (imageView as ImageView?)?.setImageBitmap(converted)
            }
        }
        var back = findViewById<View>(R.id.back)
        back.setOnClickListener {
            finish()
        }




    }
}