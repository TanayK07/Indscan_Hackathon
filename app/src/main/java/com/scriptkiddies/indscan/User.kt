package com.brijconceptorg.brijconcept

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import androidx.security.crypto.EncryptedFile
import androidx.security.crypto.MasterKey
import com.scriptkiddies.indscan.MainActivity
import java.io.*
import java.nio.charset.StandardCharsets
import java.security.GeneralSecurityException


class User : AppCompatActivity() {
    companion object {
        private var token: String? = null
        private const val TEXT = "login_oauth_token"
        public var CURRENT_PAIRING_CODE=-1;
        var CURRENT_SESSION_ID="";
        var hasCreatedThread = false;

        fun getToken(context: Context): String {
            /*SharedPreferences preferences = context.getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        token = preferences.getString(TEXT, "");
        return token;*/
            var mainKey: MasterKey? = null
            return try {
                mainKey = MasterKey.Builder(context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build()
                val encryptedFile: EncryptedFile = EncryptedFile.Builder(
                    context,
                    File(context.filesDir, TEXT),
                    mainKey,
                    EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB
                ).build()
                val inputStream: InputStream = encryptedFile.openFileInput()
                val byteArrayOutputStream = ByteArrayOutputStream()
                var nextByte = inputStream.read()
                while (nextByte != -1) {
                    byteArrayOutputStream.write(nextByte)
                    nextByte = inputStream.read()
                }
                val plaintext = byteArrayOutputStream.toByteArray()
                val returnText = String(plaintext)
                token = returnText
                returnText
            } catch (e: GeneralSecurityException) {
                e.printStackTrace()
                token = ""
                ""
            } catch (e: IOException) {
                e.printStackTrace()
                token = ""
                ""
            }
        }

        fun isUserLoggedIn(context: Context): Boolean {
            val file = File(context.filesDir, TEXT)
            return if (file.exists()) {
                getToken(context)
                !token!!.equals("", ignoreCase = true)
            } else {
                false
            }
        }

        fun saveToken(context: Context, token: String): Boolean {
            return try {
                val mainKey: MasterKey = MasterKey.Builder(context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build()
                val file = File(context.filesDir, TEXT)
                val deleted = file.delete()
                val encryptedFile: EncryptedFile = EncryptedFile.Builder(
                    context,
                    File(context.filesDir, TEXT),
                    mainKey,
                    EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB
                ).build()
                val fileContent: ByteArray = token.toByteArray(StandardCharsets.UTF_8)
                var outputStream: OutputStream? = null
                outputStream = encryptedFile.openFileOutput()
                outputStream.write(fileContent)
                outputStream.flush()
                outputStream.close()
                true
            } catch (e: GeneralSecurityException) {
                e.printStackTrace()
                false
            } catch (e: IOException) {
                e.printStackTrace()
                false
            }
        }

        fun logout(context: Context) {
            val file = File(context.filesDir, TEXT)
            file.delete()
            val intent = Intent(context, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
        }

    }
}