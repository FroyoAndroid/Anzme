package com.meapp.anzme;

import android.content.Context;
import android.content.SharedPreferences;
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
    private String NEURA_APP_UID = "16c1b4abfde7972184aebf4d1142e16a89b9cb1b94569dd7c2c7faeb306f83b6",
            NEURA_APP_SECRET = "debae31896806c4e252c7d5b3b2ea14bd266dfc7465b2e6566bb78aaf44ca7b0",
            API_URL = "https://finup.co/order/get9/adduser.php?",
            NEURA_USERID = "test",
            ACCESS_TOKEN = "test";
    private WebView clientWebview;
    private Context context;
    SharedPreferences sharedPref;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        clientWebview = (WebView) findViewById(R.id.clientWebview);
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

        context = this.getApplicationContext();
        sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        editor = sharedPref.edit();
        String ID = sharedPref.getString("NEURA_ID","");
        String TOKEN = sharedPref.getString("AUTH_TOKEN", "");
        Toast.makeText(getApplicationContext(), "ID : " + ID + ", TOKEN :" + TOKEN, Toast.LENGTH_LONG).show();
        if(ID == "" && TOKEN == "") {
            init();
        }else {
            clientWebview.loadUrl("https://www.finup.co/c2/sigma/mobile/index.php?neurauser=" + ID + "&authkey=" + TOKEN);
        }
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
                clientWebview.loadUrl("https://www.finup.co/c2/sigma/mobile/index.php?neurauser=" + NEURA_USERID + "&authkey=" + ACCESS_TOKEN);
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
        //saving as persistence storage
        editor.putString("NEURA_ID", NEURA_USERID);
        editor.putString("AUTH_TOKEN", ACCESS_TOKEN);
        editor.commit();
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
