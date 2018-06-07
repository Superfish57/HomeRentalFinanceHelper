package com.suckow.homerentalfinancehelper;

import android.annotation.TargetApi;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.nfc.Tag;
import android.os.Build;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import ca.mimic.oauth2library.OAuth2Client;
import ca.mimic.oauth2library.OAuthError;
import ca.mimic.oauth2library.OAuthResponse;
import okhttp3.Authenticator;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Credentials;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.Route;
import okhttp3.internal.http2.Header;
import okio.Buffer;

public class LoginActivity extends AppCompatActivity {

    String oauthUrl;
    String oauthClientId;
    String oauthSecret;

    Button signInButton;
    ProgressBar loginLoading;
    Dialog auth_dialog;

    public static final MediaType FORM = MediaType.parse("application/x-www-form-urlencoded");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        oauthUrl = getString(R.string.homeaway_oauth_url);
        oauthClientId = getString(R.string.homeaway_client_id);
        oauthSecret = getString(R.string.homeaway_secret);
        final String authTokenUrl = "https://ws.homeaway.com/oauth/token";


        loginLoading = findViewById(R.id.loginLoading);

        signInButton = findViewById(R.id.signInButton);
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                OAuth2Client oauthClient = new OAuth2Client.Builder("csuckow@live.com", "Lavabird0", oauthClientId, oauthSecret, authTokenUrl).build();
                try {
                    OAuthResponse response = oauthClient.requestAccessToken();
                    if (response.isSuccessful()) {
                        String accessToken = response.getAccessToken();
                        String refreshToken = response.getRefreshToken();
                        Log.d("-----whatever", "Logged in correctly");
                        Log.d("-----whatever", "Access Token: " + accessToken);
                        Log.d("-----whatever", "Refresh Token: " + refreshToken);
                    } else {
                        OAuthError error = response.getOAuthError();
                        String errorMsg = error.getError();
                        Log.e("OhNo", "Error logging in");
                        Log.e("OhNo", errorMsg);
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        });

    }


    //Old method

    private void startSignIn() {
        auth_dialog = new Dialog(this);
        auth_dialog.setContentView(R.layout.dialog_auth);
        WebView web = auth_dialog.findViewById(R.id.webv);
        web.setWebViewClient(new WebViewClient(){




            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                if(request.getUrl().toString().contains("?code=")) {
                    Log.d("-------Tag or something", "Please do something");
                    auth_dialog.dismiss();
                    loginLoading.setVisibility(View.VISIBLE);
                    signInButton.setVisibility(View.INVISIBLE);
                    Uri uri = Uri.parse(request.getUrl().toString());
                    String authCode = uri.getQueryParameter("code");
                    try {
                        authWithCode(authCode);
                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.d("-------Tag or something", "authwithcode error");
                        Toast errorToast = Toast.makeText(getApplicationContext(), "Encountered an error while signing in, please try again.", Toast.LENGTH_LONG);
                        errorToast.show();
                        loginLoading.setVisibility(View.INVISIBLE);
                        signInButton.setVisibility(View.VISIBLE);
                    }
                } else {
                    view.loadUrl(request.getUrl().toString());
                }
                return true;
            }


            @Override
            public void onPageFinished(WebView view, String url) {
                if(url.contains("?code=")) {
                    Log.d("-------Tag or something", "Please do something");
                    auth_dialog.dismiss();
                    loginLoading.setVisibility(View.VISIBLE);
                    signInButton.setVisibility(View.INVISIBLE);
                    Uri uri = Uri.parse(url);
                    String authCode = uri.getQueryParameter("code");
                    try {
                        authWithCode(authCode);
                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.d("-------Tag or something", "authwithcode error");
                        Toast errorToast = Toast.makeText(getApplicationContext(), "Encountered an error while signing in, please try again.", Toast.LENGTH_LONG);
                        errorToast.show();
                        loginLoading.setVisibility(View.INVISIBLE);
                        signInButton.setVisibility(View.VISIBLE);
                    }
                }
            }
        });
        web.getSettings().setJavaScriptEnabled(true);
        web.loadUrl(oauthUrl + oauthClientId);
        auth_dialog.show();
        auth_dialog.setTitle("Sign In");
    }

    private void authWithCode(String code) throws IOException, JSONException {

            String authTokenUrl = "https://ws.homeaway.com/oauth/token";

            //OkHttpClient client = createAuthenticatedClient(encodeBase64(oauthClientId), encodeBase64(oauthSecret));
            OkHttpClient client = new OkHttpClient();
            RequestBody body = RequestBody.create(FORM, "code=" + code);

            Request request = new Request.Builder()
                    .url(authTokenUrl)
                    .header("Authorization", encodeBase64(oauthClientId) + encodeBase64(oauthSecret))
                    .post(body)
                    .build();
            Log.d("-------Tag or something", request.headers().value(0));
            Log.d("-------Tag or something", bodyToString(request));
            Call call = client.newCall(request);

            call.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {

                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String responseBody = response.body().string();

                    Log.e("------TAG or something","Got response!");


                    SharedPreferences.Editor editor = getSharedPreferences("authTokens", MODE_PRIVATE).edit();

                    JSONObject mainObject;
                    try {
                        mainObject = new JSONObject(responseBody);
                        String token_type = mainObject.getString("token_type");
                        String access_token = mainObject.getString("access_token");
                        String refresh_token = mainObject.getString("refresh_token");
                        String expires_in = mainObject.getString("expires_in");
                        String email = mainObject.getString("email");

                        editor.putString("token_type", token_type);
                        editor.putString("access_token", access_token);
                        editor.putString("refresh_token", refresh_token);
                        editor.putString("expires_in", expires_in);
                        editor.putString("email", email);

                        editor.apply();

                        finish();
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Log.e("------TAG or something", "JSON decoding error, or wrong data back");
                        Log.e("------TAG or something", responseBody);

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast errorToast = Toast.makeText(getApplicationContext(), "Encountered an error while signing in, please try again.", Toast.LENGTH_LONG);
                                errorToast.show();
                                loginLoading.setVisibility(View.INVISIBLE);
                                signInButton.setVisibility(View.VISIBLE);
                            }
                        });

                    }



                }
            });
    }

    private static String bodyToString(final Request request){

        try {
            final Request copy = request.newBuilder().build();
            final Buffer buffer = new Buffer();
            copy.body().writeTo(buffer);
            return buffer.readUtf8();
        } catch (final IOException e) {
            return "did not work";
        }
    }

    private static OkHttpClient createAuthenticatedClient(final String username,
                                                          final String password) {
        // build client with authentication information.
        OkHttpClient httpClient = new OkHttpClient.Builder().authenticator(new Authenticator() {
            public Request authenticate(Route route, Response response) throws IOException {
                String credential = Credentials.basic(username, password);
                return response.request().newBuilder().header("Authorization", username+password).build();
            }
        }).build();
        return httpClient;
    }

    private String encodeBase64(String text) throws UnsupportedEncodingException {
        byte[] data = text.getBytes("UTF-8");
        return Base64.encodeToString(data, Base64.DEFAULT);
    }



}
