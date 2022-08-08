package com.scriptkiddies.indscan

import android.content.ContentValues.TAG
import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.*
import com.android.volley.toolbox.HttpHeaderParser
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.mlkit.nl.entityextraction.Entity
import com.google.mlkit.nl.entityextraction.EntityExtraction
import com.google.mlkit.nl.entityextraction.EntityExtractionParams
import com.google.mlkit.nl.entityextraction.EntityExtractorOptions
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.devanagari.DevanagariTextRecognizerOptions
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.namangarg.androiddocumentscannerandfilter.DocumentFilter
import eu.amirs.JSON
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONException
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.UnsupportedEncodingException


class FilterActivity : AppCompatActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
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
        //get ipcode
        val ipcode = intent.getStringExtra("ipcode").toString()
        //get pairing code
        val pairingcode = intent.getStringExtra("pairingcode").toString()
        val json = JSON(ipcode)
        val encrtptedIP = json.key("ip").stringValue();
        var decrypt: Decrypt = Decrypt()
        val decryptedIP = decrypt.decode(encrtptedIP,pairingcode+pairingcode+"4132")

        var converted: Bitmap? = gb1
        val image = InputImage.fromBitmap(gb1!!, 0)

        //OCR
        val recognizerEnglish = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        val recognizerHindi = TextRecognition.getClient(DevanagariTextRecognizerOptions.Builder().build())

        var hindiText = ""
        var englishText = ""

        recognizerEnglish.process(image)
            .addOnSuccessListener { result ->
                // Task completed successfully
                val resultText = result.text
                for (block in result.textBlocks) {
                    val blockText = block.text
                    val blockCornerPoints = block.cornerPoints
                    val blockFrame = block.boundingBox
                    if(englishText.isEmpty()){
                        englishText = blockText
                    }
                    else{
                        englishText = englishText + "<br>" + blockText
                    }
                }
                Log.d(TAG, "EnglishText: $englishText")
                //entity extraction
                val entityExtractor =
                    EntityExtraction.getClient(
                        EntityExtractorOptions.Builder(EntityExtractorOptions.ENGLISH)
                            .build())

                entityExtractor
                    .downloadModelIfNeeded()
                    .addOnSuccessListener { _ ->
                        val params =
                            EntityExtractionParams.Builder(englishText)
                                .build()
                        /* Model downloading succeeded, you can call extraction API here. */
                        entityExtractor
                            .annotate(params)
                            .addOnSuccessListener {
                                // Annotation process was successful, you can parse the EntityAnnotations list here.
                                for (entityAnnotation in it) {
                                    val entities: List<Entity> = entityAnnotation.entities
                                    for (entity in entities) {
                                        Log.d(TAG, "Entity: " + entity + " - " + entity.type)
                                    }

                                }

                            }
                            .addOnFailureListener {
                                // Check failure message here.
                                Log.d(TAG, "EntityExtraction failed: $it")
                            }
                    }
                    .addOnFailureListener { _ -> /* Model downloading failed. */ }



                // ...
            }
            .addOnFailureListener { e ->
                // Task failed with an exception
                // ...
            }
        recognizerHindi.process(image)
            .addOnSuccessListener { result ->
                // Task completed successfully
                val resultText = result.text
                for (block in result.textBlocks) {
                    val blockText = block.text
                    val blockCornerPoints = block.cornerPoints
                    val blockFrame = block.boundingBox
                    if(hindiText.isEmpty()){
                        hindiText = blockText
                    }
                    else{
                        hindiText = hindiText + "<br>" + blockText
                    }

                }
                // ...
            }
            .addOnFailureListener { e ->
                // Task failed with an exception
                // ...
            }
        var loading = findViewById<View>(R.id.progress)
        loading.visibility = View.GONE
        findViewById<View>(R.id.accept_scan_btn).setOnClickListener {
            loading.visibility = View.VISIBLE
            GlobalScope.launch(Dispatchers.IO) {
                //convert converted to base64 image
                val byteArrayOutputStream = ByteArrayOutputStream()
                converted!!.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
                val byteArray: ByteArray = byteArrayOutputStream.toByteArray()
                val encoded: String = Base64.encodeToString(byteArray, Base64.DEFAULT)

                val generatedJsonObject = JSON.create(
                    JSON.array(
                        JSON.dic(
                            "docImage", encoded,
                            "englishText", englishText,
                            "hindiText", hindiText,

                            )
                    )
                )
                val jsonString = generatedJsonObject.toString()
                Log.d(TAG, "jsonString: $jsonString")
                try {
                    val requestQueue = Volley.newRequestQueue(applicationContext)
                    val URL = "http://"+decryptedIP+":3001/queue"
                    //val jsonBody = JSONObject()
                    //jsonBody.put("Title", "Android Volley Demo")
                    //jsonBody.put("Author", "BNK")
                    //val requestBody = jsonBody.toString()
                    val stringRequest: StringRequest = object :
                        StringRequest(Method.POST, URL, Response.Listener() {
                            Log.d("VOLLEY", it)
                            this@FilterActivity.runOnUiThread(java.lang.Runnable {
                                loading.visibility = View.GONE
                            })
                            fun onResponse(response: String?) {

                                Log.d("VOLLEY", response!!)
                            }
                        }, Response.ErrorListener() {
                            Log.d("VOLLEY", it.toString())
                            fun onErrorResponse(error: VolleyError) {
                                Log.d("VOLLEY", error.toString())
                            }
                        }) {
                        override fun getBodyContentType(): String {
                            return "application/json; charset=utf-8"
                        }

                        @Throws(AuthFailureError::class)
                        override fun getBody(): ByteArray {
                            return jsonString.toByteArray()
                        }

                        override fun parseNetworkResponse(response: NetworkResponse): Response<String> {
                            var responseString = ""
                            if (response != null) {
                                responseString = response.data.toString()

                                // can get more details such as response.headers
                            }
                            return Response.success(
                                responseString,
                                HttpHeaderParser.parseCacheHeaders(response)
                            )
                        }
                    }
                    requestQueue.add(stringRequest)
                } catch (e: JSONException) {
                    e.printStackTrace()
                }


            }
        }
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