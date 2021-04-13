package com.example.initialdemo_seminar;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    //#region OnClick Methods
    public void OnClickCrypto(View view){
        Intent cryptoIntent = new Intent(this, CryptographyActivity.class);
        startActivity(cryptoIntent);
    }
    public void OnClickSafetyNet(View view){
        Intent safetyNetIntent = new Intent(this, SafetynetAPIActivity.class);
        startActivity(safetyNetIntent);
    }
    public void OnClickEncryptedFile(View view){
        Intent encryptedFileIntent = new Intent(this, EncryptedFileDemo.class);
        startActivity(encryptedFileIntent);
    }
    public void OnClickEncryptedSP(View view){
        Intent encryptedSPIntent = new Intent(this, EncryptedSharedPreferencesDemo.class);
        startActivity(encryptedSPIntent);
    }
    public void OnClickKeystore(View view){
        Intent keystoreIntent = new Intent(this, AndroidKeyStoreDemo.class);
        startActivity(keystoreIntent);
    }
    //#endregion
}