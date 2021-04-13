package com.example.initialdemo_seminar;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Map;

public class EncryptedSharedPreferencesDemo extends AppCompatActivity {
    private final String FILE_NAME_ENCRYPTED = "FILE_ENCRYPTED";
    private final String FILE_NAME_NORMAL = "FILE_NORMAL";

    MasterKey mainKey;

    SharedPreferences normalPref;
    SharedPreferences.Editor normalEditor;
    SharedPreferences encryptedPref;
    SharedPreferences.Editor encryptedEditor;

    EditText textKey;
    EditText textValue;
    TextView textPairs;
    TextView textDecryptPairs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_encrypted_shared_preferences_demo);

        textKey = findViewById(R.id.textKey);
        textValue = findViewById(R.id.textValue);
        textPairs = findViewById(R.id.textPairs);
        textDecryptPairs = findViewById(R.id.textDecryptData);

        try {
            mainKey = new MasterKey.Builder(getApplicationContext())
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();

            normalPref = getSharedPreferences(FILE_NAME_NORMAL, Context.MODE_PRIVATE);
            normalEditor = normalPref.edit();

            encryptedPref = EncryptedSharedPreferences.create(getApplicationContext(),
                    FILE_NAME_ENCRYPTED,
                    mainKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM);
            encryptedEditor = encryptedPref.edit();
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
        }

        findViewById(R.id.buttonAdd).setOnClickListener(v -> {
            String key = textKey.getText().toString();
            String value = textValue.getText().toString();

            normalEditor.putString(key, value);
            normalEditor.apply();

            Map<String, ?> mapAll = getNormal();
            String temp = "";

            for (Map.Entry<String, ?> entry : mapAll.entrySet())
                temp += entry.getKey() + ": " + entry.getValue() + "\n";

            textPairs.setText(temp);
        });

        findViewById(R.id.buttonAddStorage).setOnClickListener(v -> {
            Map<String, ?> mapAll = getNormal();

            for (Map.Entry<String, ?> entry : mapAll.entrySet())
                encryptedEditor.putString(entry.getKey(), (String) entry.getValue());

            encryptedEditor.apply();

            Toast toast = new Toast(getApplicationContext());
            toast.setText("Data added");
            toast.show();
        });

        findViewById(R.id.buttonGetReadable).setOnClickListener(v -> {
            Map<String, ?> mapAllEncrypted = getEncrypted();
            String temp = "";

            for (Map.Entry<String, ?> entry : mapAllEncrypted.entrySet())
                temp += entry.getKey() + ": " + entry.getValue() + "\n";

            textDecryptPairs.setText(temp);
        });
    }

    private Map<String, ?> getNormal() {
        return normalPref.getAll();
    }

    private Map<String, ?> getEncrypted() {
        return encryptedPref.getAll();
    }
}