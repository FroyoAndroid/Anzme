package com.meapp.anzme;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

//Neura import
import com.neura.standalonesdk.service.NeuraApiClient;
import com.neura.standalonesdk.util.Builder;
import com.neura.resources.authentication.AuthenticateCallback;
import com.neura.resources.authentication.AuthenticateData;
import com.neura.standalonesdk.util.SDKUtils;

public class MainActivity extends AppCompatActivity {

    private NeuraApiClient mNeuraApiClient;
    private String NEURA_APP_UID = "ba13394b9ff69d5880b6bf13fcffccd3c642f4d5515967ec3156de20f7e13f54";
    private String NEURA_APP_SECRET = "8b1dac7a7aba06b14f182b28c2680f7b877d8f87ca7527cc7fe105a082334256";
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

    //Neura CLient initialistion
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
            }

            @Override
            public void onFailure(int i) {
                Log.e(getClass().getSimpleName(), "Failed to authenticate with neura. "
                        + "Reason : " + SDKUtils.errorCodeToString(i));
            }
        });
    }
}
