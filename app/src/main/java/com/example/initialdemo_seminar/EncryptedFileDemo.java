package com.example.initialdemo_seminar;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.security.crypto.EncryptedFile;
import androidx.security.crypto.MasterKey;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.security.GeneralSecurityException;
import java.util.Arrays;

public class EncryptedFileDemo extends AppCompatActivity {
    Context context;
    MasterKey mainKey;
    EditText textFileName;
    EditText textInput;
    TextView textDecryptData;
    TextView textEncryptData;

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

        textFileName = findViewById(R.id.file_name);
        textInput = findViewById(R.id.input_string);
        textDecryptData = findViewById(R.id.textDecryptData);
        textEncryptData = findViewById(R.id.textEncryptData);

        findViewById(R.id.buttonSaveFile).setOnClickListener(v -> {
            String filename = textFileName.getText().toString();
            String data = textInput.getText().toString();

            if (!filename.isEmpty() && !data.isEmpty()) {
                writeFile(filename, data);
            }

            textEncryptData.setText("");
            textDecryptData.setText("");

            Toast toast = new Toast(getApplicationContext());
            toast.setText("File is saved");
            toast.show();
        });

        findViewById(R.id.buttonReadFileNormal).setOnClickListener(v -> {
            String filename = textFileName.getText().toString();
            if (!filename.isEmpty()) {
                readFileNormal(filename);
            }
        });


        findViewById(R.id.buttonReadFileEncrypted).setOnClickListener(v -> {
            String filename = textFileName.getText().toString();
            if (!filename.isEmpty()) {
                readFile(filename);
            }
        });
    }

    private void readFileNormal(String filename) {
        File file = new File(getFilesDir(), filename);

        StringBuilder text = new StringBuilder();

        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;

            while ((line = br.readLine()) != null) {
                text.append(line);
                text.append('\n');
            }
            br.close();
        } catch (IOException e) {
            Toast toast = new Toast(getApplicationContext());
            toast.setText(e.getMessage());
            toast.show();
        }

        textEncryptData.setText(text);
    }

    private void readFile(String filename) {
        try {
            EncryptedFile encryptedFile = new EncryptedFile.Builder(context,
                    new File(getFilesDir(), filename),
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

            String result = new String(plaintext);
            textDecryptData.setText(result);

            byteArrayOutputStream.close();
            inputStream.close();
        } catch (GeneralSecurityException | IOException e) {
            Toast toast = new Toast(getApplicationContext());
            toast.setText(e.getMessage());
            toast.show();
        }
    }

    private void writeFile(String filename, String data) {
        try {
            EncryptedFile encryptedFile = new EncryptedFile.Builder(context,
                    new File(getFilesDir(), filename),
                    mainKey,
                    EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB
            ).build();

            byte[] fileContent = data.getBytes(Charset.forName("UTF-8"));
            OutputStream outputStream = encryptedFile.openFileOutput();
            outputStream.write(fileContent);
            outputStream.flush();
            outputStream.close();
        } catch (GeneralSecurityException | IOException e) {
            Toast toast = new Toast(getApplicationContext());
            toast.setText(e.getMessage());
            toast.show();
        }
    }
}