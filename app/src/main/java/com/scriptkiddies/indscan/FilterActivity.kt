package com.scriptkiddies.indscan

import android.content.ContentValues.TAG
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.AuthFailureError
import com.android.volley.NetworkResponse
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.HttpHeaderParser
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.brijconceptorg.brijconcept.User
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
import java.io.ByteArrayOutputStream
import java.io.File


class FilterActivity : AppCompatActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    var handler: Handler? = Handler()
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_filter)



        /*window.decorView.systemUiVisibility = (
                 View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                         View.SYSTEM_UI_FLAG_FULLSCREEN or
                         View.SYSTEM_UI_FLAG_HIDE_NAVIGATION)*/
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

        var activityContext = this


        var converted: Bitmap? = gb1
        val image = InputImage.fromBitmap(gb1!!, 0)

        //OCR
        val recognizerEnglish = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        val recognizerHindi = TextRecognition.getClient(DevanagariTextRecognizerOptions.Builder().build())
        var entityJsonString = ""
        var hindiText = ""
        var englishText = ""

        var isAadharCard = false
        var isPanCard = false

        //boolean utils aadhar card
        var aadharTextBool = false
        var aadharNumberBool = false

        var aadharNumber = ""

        //boolean utils pan card
        var panTextBool = false
        var panNumberBool = false

        var panNumber = ""

        recognizerEnglish.process(image)
            .addOnSuccessListener { result ->
                // Task completed successfully
                val resultText = result.text
                for (block in result.textBlocks) {
                    val blockText = block.text

                    /*var tmp = blockText.replace(" ", "")


                    if(tmp.isDigitsOnly() && tmp.length == 12 && AadhaarUtil.isAadhaarNumberValid(tmp)){
                        aadharNumberBool = true
                        aadharNumber = tmp
                    }*/
                    var aadharRegex = Regex("[2-9]{1}[0-9]{3}\\s[0-9]{4}\\s[0-9]{4}")
                    val aadharmatches = aadharRegex.findAll(blockText)
                    for (match in aadharmatches) {
                        //Log.d("aadhar", "aahdar "+match.groupValues[0].replace(" ","")+" "+AadhaarUtil.isAadhaarNumberValid(match.groupValues[0].replace(" ","")) );
                        if(AadhaarUtil.isAadhaarNumberValid(match.groupValues[0].replace(" ",""))){
                            aadharNumberBool = true
                            aadharNumber = match.groupValues[0].replace(" ","")
                        }
                    }
                    if(blockText.contains("INCOME TAX DEPARTMENT")){
                        panTextBool = true
                    }
                    val regex = Regex("[A-Z]{5}[0-9]{4}[A-Z]{1}")
                    val matches = regex.findAll(blockText)
                    val names = matches.map { it.groupValues[0] }.joinToString()
                    if(names.isNotEmpty() && names.length == 10){
                        panNumberBool = true
                        panNumber = names
                    }





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
                /*
                 Entity extraction types:
                    Address
                    Date time
                    Email
                    Phone number
                    Money
                    URL
                    Tracking number
                * */
                var address = mutableListOf<String>()
                var date = mutableListOf<String>()
                var email = mutableListOf<String>()
                var phone = mutableListOf<String>()
                var money = mutableListOf<String>()
                var url = mutableListOf<String>()
                var tracking = mutableListOf<String>()

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

                                    for (entitiy in it) {
                                        val listOfEntities = entitiy.entities
                                        for (entity in listOfEntities) {

                                            when (entity.type) {
                                                Entity.TYPE_ADDRESS -> {
                                                    //binding.textView.append("address " + entitiy.annotatedText + "\n")
                                                    address.add(entitiy.annotatedText)
                                                }
                                                Entity.TYPE_DATE_TIME -> {
                                                    //binding.textView.append("date time " + entitiy.annotatedText + "\n")
                                                    date.add(entitiy.annotatedText)
                                                }
                                                Entity.TYPE_EMAIL -> {
                                                    //binding.textView.append("email " + entitiy.annotatedText + "\n")
                                                    email.add(entitiy.annotatedText)
                                                }


                                                Entity.TYPE_MONEY -> {
                                                    //binding.textView.append("money " + entitiy.annotatedText + "\n")
                                                    money.add(entitiy.annotatedText)
                                                }

                                                Entity.TYPE_PHONE -> {
                                                    //binding.textView.append("phone " + entitiy.annotatedText + "\n")
                                                    phone.add(entitiy.annotatedText)

                                                }
                                                Entity.TYPE_TRACKING_NUMBER -> {
                                                    //binding.textView.append("tracking number " + entitiy.annotatedText + "\n")
                                                    tracking.add(entitiy.annotatedText)
                                                }
                                                Entity.TYPE_URL -> {
                                                    //binding.textView.append("type url " + entitiy.annotatedText + "\n")
                                                    url.add(entitiy.annotatedText)
                                                }
                                                else -> {
                                                    //binding.textView.append(entitiy.annotatedText + "\n")
                                                }
                                            }



                                    }

                                }

                            // log all the entities
                                Log.d(TAG, "Entity Address: $address")
                                Log.d(TAG, "Entity Date: $date")
                                Log.d(TAG, "Entity Email: $email")
                                Log.d(TAG, "Entity Money: $money")
                                Log.d(TAG, "Entity Phone: $phone")
                                Log.d(TAG, "Entity Tracking: $tracking")
                                Log.d(TAG, "Entity URL: $url")
                                //generate json
                                /*
                                Sample json creation

                                  JSON generatedJsonObject = JSON.create(
                                        JSON.dic(
                                                "someKey", "someValue",
                                                "someArrayKey", JSON.array(
                                                        "first",
                                                        1,
                                                        2,
                                                        JSON.dic(
                                                                "emptyArrayKey", JSON.array()
                                                        )
                                                )
                                        )
                                );
                                */
                                var addressJSONArray = JSON.array()
                                for(addressX in address){
                                    addressJSONArray.put(addressX)
                                }
                                var dateJSONArray = JSON.array()
                                for(dateX in date){
                                    dateJSONArray.put(dateX)
                                }
                                var emailJSONArray = JSON.array()
                                for(emailX in email){
                                    emailJSONArray.put(emailX)
                                }
                                var moneyJSONArray = JSON.array()
                                for(moneyX in money){
                                    moneyJSONArray.put(moneyX)
                                }
                                var phoneJSONArray = JSON.array()
                                for(phoneX in phone){
                                    phoneJSONArray.put(phoneX)
                                }
                                var trackingJSONArray = JSON.array()
                                for(trackingX in tracking){
                                    trackingJSONArray.put(trackingX)
                                }
                                var urlJSONArray = JSON.array()
                                for(urlX in url){
                                    urlJSONArray.put(urlX)
                                }
                                var json = JSON.create(
                                    JSON.dic(
                                        "address", addressJSONArray,
                                        "date", dateJSONArray,
                                        "email", emailJSONArray,
                                        "money", moneyJSONArray,
                                        "phone", phoneJSONArray,
                                        "tracking", trackingJSONArray,
                                        "url", urlJSONArray
                                    )
                                )

                                entityJsonString = json.toString()
                                Log.d(TAG, "Entity Json: $entityJsonString")


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

        fun String.intOrString() = try { // returns Any
            toInt()
        } catch(e: NumberFormatException) {
            this
        }

        fun countOccurrences(s: String, ch: Char): Int {
            return s.filter { it == ch }.count()
        }
        recognizerHindi.process(image)
            .addOnSuccessListener { result ->
                // Task completed successfully

                val resultText = result.text
                for (block in result.textBlocks) {
                    val blockText = block.text

                    if(blockText.contains("आधार")){
                        aadharTextBool = true
                    }





                    val blockCornerPoints = block.cornerPoints
                    val blockFrame = block.boundingBox
                    if(hindiText.isEmpty()){
                        hindiText = blockText
                    }
                    else{
                        hindiText = hindiText + "<br>" + blockText
                    }

                }
                Log.d(TAG, "EnglishText HindiText: $hindiText")




                // ...
            }
            .addOnFailureListener { e ->
                // Task failed with an exception
                // ...
            }

        var loading = findViewById<View>(R.id.progress)
        loading.visibility = View.GONE
        findViewById<View>(R.id.accept_scan_btn).setOnClickListener {
            if(aadharTextBool && aadharNumberBool){
                isAadharCard = true
            }
            if(panTextBool && panNumberBool){
                isPanCard = true
            }
            //Log aadharcard
            Log.d(TAG, "AadharCard: $isAadharCard")

            //Log pancard
            Log.d(TAG, "PanCard: $isPanCard")

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
                            "entities", entityJsonString,
                            "isAadharCard", isAadharCard,
                            "aadharNumber", aadharNumber,
                            "isPanCard", isPanCard,
                            "panNumber", panNumber,
                            "sessionId", User.CURRENT_SESSION_ID
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
                                /*val intent = Intent(applicationContext, scanningscreenActivity::class.java)
                                intent.putExtra("ipcode", ipcode)

                                intent.putExtra("pairingcode", pairingcode)
                                startActivity(intent)
                                finish()*/
                                val resultIntent = Intent()
                                setResult(RESULT_OK, resultIntent)
                                finish()
                            })
                            fun onResponse(response: String?) {

                                Log.d("VOLLEY", response!!)
                            }
                        }, Response.ErrorListener() {
                            Log.d("VOLLEY", it.toString())
                            Toast.makeText(
                                applicationContext,
                                "Error connecting to web console, make sure you are connected on the same network",
                                Toast.LENGTH_LONG
                            ).show()

                            //Redirect to MainActivity if error occurs
                            val intent = Intent(applicationContext, MainActivity::class.java)
                            startActivity(intent)
                            finish()
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