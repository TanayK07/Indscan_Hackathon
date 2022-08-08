package com.scriptkiddies.indscan;

import android.os.Build;

import androidx.annotation.RequiresApi;

import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class Decrypt {


    public static void main(String[] args) {

    }
    @RequiresApi(api = Build.VERSION_CODES.O)
    public String decode(String encrypted, String key) {
        String s = "";
        try
        {
            //aK7+UX24ttBgfTnAndz9aQ==
            String data = encrypted;
            String iv = "1234567812345678";

            Base64.Decoder decoder = Base64.getDecoder();
            byte[] encrypted1 = decoder.decode(data);

            Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
            SecretKeySpec keyspec = new SecretKeySpec(key.getBytes(), "AES");
            IvParameterSpec ivspec = new IvParameterSpec(iv.getBytes());

            cipher.init(Cipher.DECRYPT_MODE, keyspec, ivspec);

            byte[] original = cipher.doFinal(encrypted1);
            String originalString = new String(original);
            System.out.println("encryptedMsg "+ originalString.trim());
            s = originalString.trim();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return s;
    }
}