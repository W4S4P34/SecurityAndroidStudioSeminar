package com.example.initialdemo_seminar;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import java.io.IOException;
import java.security.GeneralSecurityException;

public class EncryptedSharedPreferencesDemo extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_encrypted_shared_preferences_demo);

        try {
            MasterKey mainKey = new MasterKey.Builder(getApplicationContext())
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();

            SharedPreferences pref = EncryptedSharedPreferences.create(getApplicationContext(),
                    "FILE",
                    mainKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM);

            SharedPreferences.Editor editor = pref.edit();

            editor.putString("name", "A name here");
            editor.putBoolean("isDev", true);
            editor.apply();

            String string = pref.getString("name", "No name here");
            Log.d("MY_DEMO", string);

        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
        }

    }
}