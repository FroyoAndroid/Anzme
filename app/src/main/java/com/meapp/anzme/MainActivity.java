package com.meapp.anzme;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

//Neura import
import com.neura.standalonesdk.service.NeuraApiClient;
import com.neura.standalonesdk.util.Builder;
import com.neura.resources.authentication.AuthenticateCallback;
import com.neura.resources.authentication.AuthenticateData;
import com.neura.standalonesdk.util.SDKUtils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    private NeuraApiClient mNeuraApiClient;
    private String NEURA_APP_UID = "ba13394b9ff69d5880b6bf13fcffccd3c642f4d5515967ec3156de20f7e13f54",
            NEURA_APP_SECRET = "8b1dac7a7aba06b14f182b28c2680f7b877d8f87ca7527cc7fe105a082334256",
            API_URL = "https://finup.co/order/get9/adduser.php?",
            NEURA_USERID = "",
            ACCESS_TOKEN = "";
    private WebView clientWebview;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        clientWebview = (WebView) findViewById(R.id.clientWebview);
        clientWebview.loadUrl("https://www.finup.co/c2/sigma/mobile/");
        WebSettings clientWebSetting = clientWebview.getSettings();
        clientWebSetting.setBuiltInZoomControls(true);
        clientWebSetting.setJavaScriptEnabled(true);
        clientWebview.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return false;
            }
        });
        init();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            switch (keyCode) {
                case KeyEvent.KEYCODE_BACK:
                    if (clientWebview.canGoBack()) {
                        clientWebview.goBack();
                    } else {
                        finish();
                    }
                    return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    //Neura Client initialisation
    private void init() {
        Builder builder = new Builder(getApplicationContext());
        mNeuraApiClient = builder.build();
        mNeuraApiClient.setAppUid(NEURA_APP_UID);
        mNeuraApiClient.setAppSecret(NEURA_APP_SECRET);
        mNeuraApiClient.connect();
        authenticateNeura();
    }

    private void authenticateNeura() {
        mNeuraApiClient.authenticate(new AuthenticateCallback() {
            @Override
            public void onSuccess(AuthenticateData authenticateData) {
                Log.i(getClass().getSimpleName(), "Successfully authenticate with neura. "
                        + "NeuraUserId = " + authenticateData.getNeuraUserId() + " "
                        + "AccessToken = " + authenticateData.getAccessToken() + " "
                        + "There are " + authenticateData.getEvents().size() + " "
                        + "events you've declared to your app.");
                NEURA_USERID = authenticateData.getNeuraUserId();
                ACCESS_TOKEN = authenticateData.getAccessToken();
                saveNeuraDetails();
            }

            @Override
            public void onFailure(int i) {
                Log.e(getClass().getSimpleName(), "Failed to authenticate with neura. "
                        + "Reason : " + SDKUtils.errorCodeToString(i));
            }
        });
    }

    private void saveNeuraDetails() {
        // call the execute method here
        new SendData().execute();
    }

    class SendData extends AsyncTask<Void, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Toast.makeText(getApplicationContext(), "Saving Data to server", Toast.LENGTH_LONG).show();
        }

        @Override
        protected String doInBackground(Void... urls) {
            try {
                URL url = new URL(API_URL + "neurauser=" + NEURA_USERID + "&authkey=" + ACCESS_TOKEN);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                try {
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                    StringBuilder stringBuilder = new StringBuilder();
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        stringBuilder.append(line).append("\n");
                    }
                    bufferedReader.close();
                    return stringBuilder.toString();
                } finally {
                    urlConnection.disconnect();
                }
            } catch (Exception e) {
                Log.e("ERROR", e.getMessage(), e);
                Toast.makeText(getApplicationContext(), "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                return null;
            }
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Toast.makeText(getApplicationContext(), "Sent", Toast.LENGTH_SHORT).show();
        }
    }
}
