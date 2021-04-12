package com.example.initialdemo_seminar;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.security.crypto.EncryptedFile;
import androidx.security.crypto.MasterKey;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.security.GeneralSecurityException;
import java.util.Arrays;

public class EncryptedFileDemo extends AppCompatActivity {
    Context context;
    MasterKey mainKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_encrypted_file_demo);
        context = getApplicationContext();
        try {
            mainKey = new MasterKey.Builder(context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
        }

        Log.d("MY_DEMO", String.valueOf(context.getFilesDir()));

        writeFile("Hello world");
        readFile();
    }

    private void readFile() {
        try {
            String fileToRead = "my_sensitive_data.txt";
            EncryptedFile encryptedFile = new EncryptedFile.Builder(context,
                    new File(getFilesDir(), fileToRead),
                    mainKey,
                    EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB
            ).build();

            InputStream inputStream = encryptedFile.openFileInput();
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            int nextByte = inputStream.read();
            while (nextByte != -1) {
                byteArrayOutputStream.write(nextByte);
                nextByte = inputStream.read();
            }

            byte[] plaintext = byteArrayOutputStream.toByteArray();
            Log.d("MY_DEMO", new String(plaintext));

            byteArrayOutputStream.close();
            inputStream.close();
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
        }
    }

    private void writeFile(String data) {
        try {
            String fileToWrite = "my_sensitive_data.txt";
            EncryptedFile encryptedFile = new EncryptedFile.Builder(context,
                    new File(getFilesDir(), fileToWrite),
                    mainKey,
                    EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB
            ).build();

            byte[] fileContent = data.getBytes(Charset.forName("UTF-8"));
            OutputStream outputStream = encryptedFile.openFileOutput();
            outputStream.write(fileContent);
            outputStream.flush();
            outputStream.close();
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
        }
    }
}