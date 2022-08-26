package com.scriptkiddies.indscan
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import com.android.volley.Request
import com.brijconceptorg.brijconcept.MyApi
import com.brijconceptorg.brijconcept.User
import com.mikhaellopez.biometric.BiometricHelper
import com.mikhaellopez.biometric.BiometricPromptInfo
import com.mikhaellopez.biometric.BiometricType
import eu.amirs.JSON
import io.github.g00fy2.quickie.QRResult
import io.github.g00fy2.quickie.ScanQRCode
import org.json.JSONObject

class PairingCodeFragment: Fragment() {
    @RequiresApi(Build.VERSION_CODES.O)
    val scanQrCode = registerForActivityResult(ScanQRCode(), ::handleResult)

    companion object {
        fun newInstance(): PairingCodeFragment = PairingCodeFragment()
    }

    private val biometricHelper by lazy { BiometricHelper(this) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View =
        inflater.inflate(R.layout.pairingcode, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        biometricHelper.showBiometricPrompt(
            BiometricPromptInfo(
                title = "Indscan", // Mandatory
                negativeButtonText = "Cancel", // Mandatory
                subtitle = "Place your finger or scan your face",
                description = "",
                confirmationRequired = true
            )
        ) {
            // Do something when success
            var codeTE: TextView = view.findViewById(R.id.code)
            //generate random six digit code
            //codeTE.text = (100000..999999).shuffled().last().toString()
            var loading = view.findViewById<View>(R.id.progress)
            val api= MyApi()


// BiometricType = FACE, FINGERPRINT, IRIS, MULTIPLE or NONE
            val biometricType: BiometricType = biometricHelper.getBiometricType()
            loading.visibility=View.VISIBLE

            api.execute(requireActivity().applicationContext, Request.Method.POST,"pairing_codes", object:ResponseListener{
                override fun onResponse(response: JSONObject?) {
                    loading.visibility=View.GONE
                    if (response != null) {
                        if(response.getBoolean("success")){
                            codeTE.text=response.getJSONObject("data").getInt("id").toString();
                            User.CURRENT_SESSION_ID="";
                            User.CURRENT_PAIRING_CODE=codeTE.text.toString().toInt()
                        } else{
                            Log.d("ERROR","Pair code not found")
                        }
                    }

                }

                override fun onError(message: String?) {
                    loading.visibility=View.GONE
                    Log.d("ERROR","Pair Code not found")
                }
            });
            view.findViewById<View>(R.id.continue_btn).setOnClickListener {
                scanQrCode.launch(null)
            }
        }

    }
    @RequiresApi(Build.VERSION_CODES.O)
    fun handleResult(result: QRResult) {
        val text = when (result) {
            is QRResult.QRSuccess -> result.content.rawValue
            QRResult.QRUserCanceled -> ""
            QRResult.QRMissingPermission -> ""
            is QRResult.QRError -> ""
        }
        if (text != "") {
            //log the code
            // var decrypt: Decrypt = Decrypt()
            //        //decrypt.decode()
            //        //Log.d("decrypt", decrypt.decode())

            //start scan activity
            //get ipcode
            val ipcode = text
            //get pairing code
            var code = requireView().findViewById<TextView>(R.id.code).text.toString()
            val pairingcode = code
            val json = JSON(ipcode)
            val encrtptedIP = json.key("ip").stringValue();
            var decrypt: Decrypt = Decrypt()
            val decryptedIP = decrypt.decode(encrtptedIP, pairingcode + pairingcode + "4132")
            val decryptedToken = decrypt.decode(
                json.key("token").stringValue(),
                pairingcode + pairingcode + "4132"
            )
            val decryptedSessionId = decrypt.decode(
                json.key("sessionId").stringValue(),
                pairingcode + pairingcode + "4132"
            )
            User.CURRENT_SESSION_ID = decryptedSessionId.toString();


            //Link Api
            val myapi = MyApi()
            var codeTE: TextView = requireView().findViewById(R.id.code)
            var hashMap: MutableMap<String, String> = HashMap<String, String>()
            hashMap["pairing_code"] = codeTE.text.toString();
            hashMap["session_id"] = decryptedSessionId.toString();
            myapi.execute(
                requireActivity().applicationContext,
                Request.Method.POST,
                "pairing_codes/link",
                hashMap,
                object : ResponseListener {
                    override fun onResponse(response: JSONObject?) {
                        if (response != null) {
                            if (response.getBoolean("success")) {
                                val intent = Intent(
                                    requireActivity().applicationContext,
                                    scanningscreenActivity::class.java
                                )
                                intent.putExtra("ipcode", text)
                                intent.putExtra("pairingcode", code)
                                startActivity(intent)
                                requireActivity().finish()
                            } else {
                                Log.d("ApiDebugging", "Linking failed");
                            }

                        }
                    }

                    override fun onError(message: String?) {
                        Log.d("ApiDebugging", "Linking request error");
                    }
                })
        }
    }
}
