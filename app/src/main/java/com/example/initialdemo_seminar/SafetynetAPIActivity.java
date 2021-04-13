package com.example.initialdemo_seminar;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.safetynet.SafeBrowsingThreat;
import com.google.android.gms.safetynet.SafetyNet;
import com.google.android.gms.safetynet.SafetyNetApi;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Tasks;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import org.w3c.dom.Text;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import java.util.function.Function;

public class SafetynetAPIActivity extends AppCompatActivity {

    private final Random mRandom = new SecureRandom();

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_safetynet_a_p_i);
        // To check whether the installed version of Google Play services is compatible with the version of the Android SDK you're using
        if (GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this) == ConnectionResult.SUCCESS) {
            try {
                AttestationAPIDemo();
                RecaptchaAPIDemo();
                //new LoadThreatData().execute();
            }catch (Exception ignore){}
        } else {
            // Prompt user to update Google Play services.
        }
    }



    @RequiresApi(api = Build.VERSION_CODES.N)
    private void AttestationAPIDemo(){
        TextInputEditText textViewAttestationResultCode = findViewById(R.id.tv_attestation_result_code);
        TextInputEditText textViewAttestationResult = findViewById(R.id.tv_attestation_result);

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
                                    String mResult = response.getJwsResult();
                                    Log.d("Nothing", "Success! SafetyNet result:\n" + mResult + "\n");
                                    textViewAttestationResult.setText("Attestation successful");
                                    textViewAttestationResultCode.setText(mResult);
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
                            textViewAttestationResult.setText("Attestation failed");
                        }
                    });
        };
        //#endregion

        MaterialButton buttonRequest = findViewById(R.id.btn_attestation_request);
        buttonRequest.setOnClickListener(v -> {
            sendSafetyNetRequest.accept(null);
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void SafeBrowsingAPIDemo() throws ExecutionException, InterruptedException {

        TextInputEditText textViewSafeBrowsingResponseResult = findViewById(R.id.tv_safe_browsing_result);
        TextInputEditText editTextSafeBrowsingMessage = findViewById(R.id.et_safe_browsing_message);

        //#region Local Function
        Consumer<String> sendCheckUrlRequest = s -> {
            Log.d("Nothing", "Send request");
            String url = editTextSafeBrowsingMessage.getText().toString();
            if(url.isEmpty())
                return;
            SafetyNet.getClient(this).lookupUri(url,
                    AppConfig.SB_API_KEY,
                    SafeBrowsingThreat.TYPE_POTENTIALLY_HARMFUL_APPLICATION,
                    SafeBrowsingThreat.TYPE_SOCIAL_ENGINEERING)
                    .addOnSuccessListener(this,
                            new OnSuccessListener<SafetyNetApi.SafeBrowsingResponse>() {
                                @Override
                                public void onSuccess(SafetyNetApi.SafeBrowsingResponse sbResponse) {
                                    Log.d("Nothing", "Res success");
                                    // Indicates communication with the service was successful.
                                    // Identify any detected threats.
                                    if (sbResponse.getDetectedThreats().isEmpty()) {
                                        // No threats found.
                                        textViewSafeBrowsingResponseResult.setText("No Threats found");
                                    } else {
                                        // Threats found!
                                        textViewSafeBrowsingResponseResult.setText("Threats found");
                                    }
                                }
                            })
                    .addOnFailureListener(this, new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // An error occurred while communicating with the service.
                            if (e instanceof ApiException) {
                                // An error with the Google Play Services API contains some
                                // additional details.
                                ApiException apiException = (ApiException) e;
                                Log.d("Nothing", "Error: " + CommonStatusCodes
                                        .getStatusCodeString(apiException.getStatusCode()));

                                // Note: If the status code, apiException.getStatusCode(),
                                // is SafetyNetstatusCode.SAFE_BROWSING_API_NOT_INITIALIZED,
                                // you need to call initSafeBrowsing(). It means either you
                                // haven't called initSafeBrowsing() before or that it needs
                                // to be called again due to an internal error.
                            } else {
                                // A different, unknown type of error occurred.
                                Log.d("Nothing", "Error: " + e.getMessage());
                            }
                        }
                    });
        };
        //#endregion

        MaterialButton buttonRequest = findViewById(R.id.btn_safe_browsing_request);
        buttonRequest.setOnClickListener(v -> {
            Log.d("Nothing", "On click button");
            sendCheckUrlRequest.accept(null);
        });
    }

    @SuppressLint("StaticFieldLeak")
    class LoadThreatData extends AsyncTask<Void, Integer, Void>{

        @Override
        protected Void doInBackground(Void... voids) {
            Log.d("Nothing", "Wait");
            try {
                Tasks.await(SafetyNet.getClient(getApplicationContext()).initSafeBrowsing());
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        protected void onPostExecute(Void aVoid) {
            try {
                Log.d("Nothing", "End Wait");
                SafeBrowsingAPIDemo();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void RecaptchaAPIDemo(){
        TextInputEditText textViewTokenResult = findViewById(R.id.tv_recaptcha_token);

        MaterialButton buttonRequest = findViewById(R.id.btn_recaptcha_request);
        buttonRequest.setOnClickListener(v -> {
            Log.d("Nothing", "On click button");
            SafetyNet.getClient(this).verifyWithRecaptcha(AppConfig.RC_API_KEY)
                    .addOnSuccessListener( this,
                            new OnSuccessListener<SafetyNetApi.RecaptchaTokenResponse>() {
                                @Override
                                public void onSuccess(SafetyNetApi.RecaptchaTokenResponse response) {
                                    // Indicates communication with reCAPTCHA service was
                                    // successful.
                                    Log.d("Nothing","Success");
                                    String userResponseToken = response.getTokenResult();
                                    if (!userResponseToken.isEmpty()) {
                                        // Validate the user response token using the
                                        // reCAPTCHA siteverify API.
                                        Log.d("Nothing","Token: " + userResponseToken);
                                        textViewTokenResult.setText(userResponseToken);
                                    }
                                }
                            })
                    .addOnFailureListener( this, new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            if (e instanceof ApiException) {
                                // An error occurred when communicating with the
                                // reCAPTCHA service. Refer to the status code to
                                // handle the error appropriately.
                                ApiException apiException = (ApiException) e;
                                int statusCode = apiException.getStatusCode();
                                Log.d("Nothing", "Error: " + CommonStatusCodes
                                        .getStatusCodeString(statusCode));
                            } else {
                                // A different, unknown type of error occurred.
                                Log.d("Nothing", "Error: " + e.getMessage());
                            }
                        }
                    });
        });
    }
}