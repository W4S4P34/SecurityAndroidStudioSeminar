package com.example.initialdemo_seminar;

import android.os.Bundle;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.nio.charset.Charset;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.UnrecoverableEntryException;
import java.security.cert.Certificate;
import java.util.Enumeration;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class AndroidKeyStoreDemo extends AppCompatActivity {
    KeyStore keyStore;
    KeyPair keyPair;

    TextView keyInfo;
    EditText clearText;
    TextView encryptText;
    TextView decryptText;
    EditText entered;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_android_key_store_demo);
        keyInfo = findViewById(R.id.textKeyInfo);
        clearText = findViewById(R.id.editClearText);
        encryptText = findViewById(R.id.textEncrypt);
        decryptText = findViewById(R.id.textDecrypt);
        entered = findViewById(R.id.editAlias);

        initKeyStore();
        findViewById(R.id.buttonGenerateKey).setOnClickListener(v -> {
            String alias = entered.getText().toString();
            createKey(alias);
            try {
                getKeyInfo(alias);
            } catch (UnrecoverableEntryException | NoSuchAlgorithmException | KeyStoreException e) {
                e.printStackTrace();
            }
        });

        findViewById(R.id.buttonEncrypt).setOnClickListener(v -> {
            String alias = entered.getText().toString();
            String clear = clearText.getText().toString();
            if (!clear.isEmpty() && !alias.isEmpty()) {
                try {
                    encryptString(clear, alias);
                } catch (KeyStoreException | NoSuchPaddingException | NoSuchAlgorithmException | InvalidKeyException | BadPaddingException | IllegalBlockSizeException e) {
                    e.printStackTrace();
                }
            }
        });
        findViewById(R.id.buttonDecrypt).setOnClickListener(v -> {
            String alias = entered.getText().toString();
            String cipherText = encryptText.getText().toString();
            if (!cipherText.isEmpty() && !alias.isEmpty()) {
                try {
                    decryptString(cipherText, alias);
                } catch (KeyStoreException | NoSuchPaddingException | NoSuchAlgorithmException | InvalidKeyException | BadPaddingException | IllegalBlockSizeException | UnrecoverableEntryException e) {
                    e.printStackTrace();
                }
            }

        });
    }

    private void initKeyStore() {
        try {
            keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null);
        } catch (Exception e) {
            Log.d("MY_DEMO", e.toString());
        }
    }

    private void createKey(String alias) {
        try {
            if (!keyStore.containsAlias(alias)) {
                KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_RSA, "AndroidKeyStore");
                KeyGenParameterSpec parameterSpec = new KeyGenParameterSpec.Builder(
                        alias,
                        KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                        .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_RSA_PKCS1)
                        .setDigests(KeyProperties.DIGEST_SHA1)
                        .build();
                keyPairGenerator.initialize(parameterSpec);
                keyPair = keyPairGenerator.genKeyPair();
            } else Toast.makeText(this, "Alias exist!!", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.d("MY_DEMO", e.toString());
        }
    }

    private void getKeyInfo(String alias) throws UnrecoverableEntryException, NoSuchAlgorithmException, KeyStoreException {
        KeyStore.PrivateKeyEntry pkEntry = (KeyStore.PrivateKeyEntry) (keyStore.getEntry(alias, null));
        PrivateKey privateKey = pkEntry.getPrivateKey();
        Certificate cert = keyStore.getCertificate(alias);
        PublicKey publicKey = cert.getPublicKey();

        byte[] publicKeyBytes = Base64.encode(publicKey.getEncoded(), Base64.DEFAULT);
        String pubKeyString = new String(publicKeyBytes);

        // Cannot do this due to security
//        byte[] privateKeyBytes = Base64.encode(private.getEncoded(), Base64.DEFAULT);
//        String priKeyString = new String(privateKeyBytes);
        Log.d("MY_DEMO", "Public key: ${pubKeyString}$");

        String temp = "Public key: " + pubKeyString;
        keyInfo.setText(temp);
    }

    private void encryptString(String clearText, String alias) throws KeyStoreException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        PublicKey publicKey = keyStore.getCertificate(alias).getPublicKey();
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        byte[] cipherText = cipher.doFinal(clearText.getBytes(Charset.forName("UTF-8")));

        encryptText.setText(Base64.encodeToString(cipherText, Base64.DEFAULT));
    }

    private void decryptString(String cipherText, String alias) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException, KeyStoreException, UnrecoverableEntryException {
        KeyStore.PrivateKeyEntry pkEntry = (KeyStore.PrivateKeyEntry) keyStore.getEntry(alias, null);
        PrivateKey privateKey = pkEntry.getPrivateKey();
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        byte[] decryptTextByte = cipher.doFinal(Base64.decode(cipherText, Base64.DEFAULT));

        decryptText.setText(new String(decryptTextByte));
    }
}