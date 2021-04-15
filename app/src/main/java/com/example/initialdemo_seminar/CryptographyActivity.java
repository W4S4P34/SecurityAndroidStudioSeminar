package com.example.initialdemo_seminar;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Build;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.SignatureException;
import java.security.cert.Certificate;

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
    private MaterialAlertDialogBuilder materialAlertDialogBuilder;

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cryptography);
        materialAlertDialogBuilder = new MaterialAlertDialogBuilder(this);
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
                    materialAlertDialogBuilder.setTitle("Error Message");
                    materialAlertDialogBuilder.setMessage(ignore.getMessage());
                    materialAlertDialogBuilder.setCancelable(true);
                    materialAlertDialogBuilder.show();
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
        Log.d("Nothing","Encrypt message: " + EncoderFun(msg));
        Log.d("Nothing", "Encrypt message: " + msg);
        return new String(msg);
    }
    private String Decrypt(byte[] cipherText) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        if(textViewCipherKey.getText().toString().isEmpty())
            return null;
        byte[] keyBytes = DecoderFun(textViewCipherKey.getText().toString());
        SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");
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
            textViewCipherKey.setText(EncoderFun(key.getEncoded()));
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
    private static PrivateKey privateKey;
    private static PublicKey publicKey;
    private byte[] message = new byte[] {'h','i'};

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)

    private void SignatureDemo(){
        TextInputEditText editTextSignatureMessage = findViewById(R.id.et_signature_message);
        TextInputEditText textViewSignatureMessage = findViewById(R.id.tv_signature_msg);
        TextInputEditText editTextSignatureSign = findViewById(R.id.tv_signature_sign);
        TextInputEditText textViewSignatureVerifyResult = findViewById(R.id.tv_verify_result);

        Button buttonSign = findViewById(R.id.btn_sign);
        buttonSign.setOnClickListener(v -> {
            if(!editTextSignatureMessage.getText().toString().isEmpty()){
                GeneratePairKeys();
                byte[] messageBytes = editTextSignatureMessage.getText().toString().getBytes(StandardCharsets.UTF_8);
                //byte [] messageBytes = message;
                try {
                    Signature s = Signature.getInstance("SHA1withRSA");
                    s.initSign(privateKey);
                    s.update(messageBytes);
                    //Log.e("Nothing", Base64.encodeToString(s.sign(), Base64.DEFAULT));
                    byte[] signature = s.sign();
                    editTextSignatureSign.setText(EncoderFun(signature));
                    editTextSignatureMessage.setText("");
                    textViewSignatureVerifyResult.setText("");
                    textViewSignatureMessage.setText(new String(messageBytes));

                } catch (Exception ignore) {
                    ignore.printStackTrace();
                }
            }
        });

        Button buttonVerify = findViewById(R.id.btn_verify);
        buttonVerify.setOnClickListener(v -> {
            if(!editTextSignatureSign.getText().toString().isEmpty()){
                byte[] messageBytes = textViewSignatureMessage.getText().toString().getBytes(StandardCharsets.UTF_8);
                byte[] signature = DecoderFun(editTextSignatureSign.getText().toString());
                //byte [] messageBytes = message;
                try {
                    Signature s = Signature.getInstance("SHA1withRSA");
                    s.initVerify(publicKey);
                    s.update(messageBytes);
                    if (s.verify(signature)) {
                        textViewSignatureVerifyResult.setText("Verify successful");
                    } else {
                        textViewSignatureVerifyResult.setText("Verify failed");
                    }

                    Log.d("Nothing", new String(messageBytes));
                } catch (Exception ignore) {
                    ignore.printStackTrace();
                }
            }
        });

    }
    public void GeneratePairKeys() {
        SecureRandom random = null;
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            random = SecureRandom.getInstance("SHA1PRNG");
            keyGen.initialize(1024, random);

            KeyPair pair = keyGen.generateKeyPair();

            privateKey = pair.getPrivate();
            publicKey = pair.getPublic();
            Log.d("Nothing", "Private key: " + new String(privateKey.getEncoded()));
            Log.d("Nothing", "Public key: " + new String(publicKey.getEncoded()));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
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