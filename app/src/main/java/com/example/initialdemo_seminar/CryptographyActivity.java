package com.example.initialdemo_seminar;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class CryptographyActivity extends AppCompatActivity {

    private static SecretKey key = null;
    private TextInputEditText textViewCipherKey;
    private byte[] msg;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cryptography);

        CipherDemo();
        MessageDigestDemo();
        SignatureDemo();
    }

    //#region Cipher Demo
    private void CipherDemo(){
        TextInputEditText editTextCipherMessage = findViewById(R.id.et_cipher_message);
        TextInputEditText textViewCipherEncryptMessage = findViewById(R.id.tv_cipher_encrypt_message);
        TextInputEditText textViewCipherDecryptMessage = findViewById(R.id.tv_cipher_decrypt_message);
        textViewCipherKey = findViewById(R.id.tv_cipher_key);

        MaterialButton buttonDecode = findViewById(R.id.btn_decode);
        buttonDecode.setOnClickListener(v -> {
            String msg = textViewCipherEncryptMessage.getText().toString();
            Log.d("Nothing", msg);
            if(!msg.isEmpty()){
                try {
                    String cipherEncodeText = Decrypt(this.msg);
                    textViewCipherDecryptMessage.setText(cipherEncodeText);
                    textViewCipherEncryptMessage.setText("");
                }catch (Exception ignore){
                    Log.d("Nothing", ignore.getMessage());
                }
            }
        });

        MaterialButton buttonEncode = findViewById(R.id.btn_encode);
        buttonEncode.setOnClickListener(v -> {
            String msg = editTextCipherMessage.getText().toString();
            if(!msg.isEmpty()){
                try {
                    GenerateKey();
                    String cipherEncodeText = Encrypt(msg);
                    textViewCipherEncryptMessage.setText(cipherEncodeText);

                }catch (Exception ignore){
                    Log.d("Nothing",ignore.getMessage());
                }
            }
        });
    }
    private String Encrypt(String message) throws NoSuchAlgorithmException, NoSuchPaddingException, BadPaddingException, IllegalBlockSizeException, InvalidKeyException {
        byte[] plaintext = message.trim().getBytes();
        SecretKeySpec keySpec = new SecretKeySpec(key.getEncoded(), "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, keySpec);
        msg = cipher.doFinal(plaintext);
        for (byte b : msg ) {
            Log.d("Nothing", String.valueOf(b));
        }
        Log.d("Nothing","Encrypt message: " + EncoderFun(msg));
        Log.d("Nothing", "Encrypt message: " + msg);
        return new String(msg);
    }
    private String Decrypt(byte[] cipherText) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        SecretKeySpec keySpec = new SecretKeySpec(key.getEncoded(), "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, keySpec);
        byte[] decrypted = cipher.doFinal(cipherText);
        Log.d("Nothing", "Decrypt message: " + decrypted);
        return new String(decrypted);
    }
    private void GenerateKey(){
        try{
            KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
            keyGenerator.init(256);
            key = keyGenerator.generateKey();
            Log.d("Nothing", "Key: " + key.getEncoded());
            textViewCipherKey.setText(new String(key.getEncoded()));
        }catch (Exception ignore){}
    }
    //#endregion

    //#region Message Digest Demo
    private void MessageDigestDemo() {
        TextInputEditText editTextMessageDigestMessage = findViewById(R.id.et_msg_digest_message);
        TextInputEditText textViewMessageDigestMessage = findViewById(R.id.tv_msg_digest_message);

        Button buttonEncode = findViewById(R.id.btn_encode_msg_digest);
        buttonEncode.setOnClickListener(v -> {
            if(editTextMessageDigestMessage.getText().toString().isEmpty())
                return;
            try {
                byte[] message = editTextMessageDigestMessage.getText().toString().getBytes();
                MessageDigest md = null;
                md = MessageDigest.getInstance("SHA-256");
                byte[] digest = md.digest(message);
                String digestMessage = new String(digest);
                textViewMessageDigestMessage.setText(digestMessage);
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
        });
    }
    //#endregion

    //#region Signature Demo
    private void SignatureDemo(){

    }
    //#endregion


    public static byte[] DecoderFun(String encodeValue) {
        byte[] conVal = Base64.decode(encodeValue, Base64.DEFAULT);
        return conVal;
    }
    public static String EncoderFun(byte[] decodeValue) {
        String conVal= Base64.encodeToString(decodeValue,Base64.DEFAULT);
        return conVal;
    }


}