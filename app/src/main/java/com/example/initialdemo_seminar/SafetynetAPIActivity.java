package com.example.initialdemo_seminar;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.safetynet.SafetyNet;
import com.google.android.gms.safetynet.SafetyNetApi;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.Random;
import java.util.function.Consumer;
import java.util.function.Function;

public class SafetynetAPIActivity extends AppCompatActivity {

    private final Random mRandom = new SecureRandom();
    private String mResult;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_safetynet_a_p_i);
        AttestationAPIDemo();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void AttestationAPIDemo(){
        //#region Local Function
        Function<String, byte[]> getRequestNonce = data ->{
            ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
            byte[] bytes = new byte[24];
            mRandom.nextBytes(bytes);
            try {
                byteStream.write(bytes);
                byteStream.write(data.getBytes());
            } catch (IOException e) {
                return null;
            }
            return byteStream.toByteArray();
        };
        Consumer<String> sendSafetyNetRequest = s -> {
            // The SafetyNet Attestation API is available.
            String nonceData = "Safety Net Sample: " + System.currentTimeMillis();
            byte[] nonce = getRequestNonce.apply(nonceData);
            // The nonce should be at least 16 bytes in length.
            // You must generate the value of API_KEY in the Google APIs dashboard.
            SafetyNet.getClient(this).attest(nonce, AppConfig.ADV_API_KEY)
                    .addOnSuccessListener(this,
                            new OnSuccessListener<SafetyNetApi.AttestationResponse>() {
                                @Override
                                public void onSuccess(SafetyNetApi.AttestationResponse response) {
                                    // Indicates communication with the service was successful.
                                    // Use response.getJwsResult() to get the result data.
                                    mResult = response.getJwsResult();
                                    Log.d("Nothing", "Success! SafetyNet result:\n" + mResult + "\n");
                                }
                            })
                    .addOnFailureListener(this, new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // An error occurred while communicating with the service.
                            if (e instanceof ApiException) {
                                // An error with the Google Play services API contains some
                                // additional details.
                                ApiException apiException = (ApiException) e;
                                // You can retrieve the status code using the
                                // apiException.getStatusCode() method.
                                Log.d("Nothing", "Error: " +
                                        CommonStatusCodes.getStatusCodeString(apiException.getStatusCode()) + ": " +
                                        apiException.getStatusMessage());
                            } else {
                                // A different, unknown type of error occurred.
                                Log.d("Nothing", "Error: " + e.getMessage());
                            }
                        }
                    });
        };
        //#endregion

        // To check whether the installed version of Google Play services is compatible with the version of the Android SDK you're using
        if (GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this) == ConnectionResult.SUCCESS) {
            sendSafetyNetRequest.accept(null);
        } else {
            // Prompt user to update Google Play services.
        }
    }


}