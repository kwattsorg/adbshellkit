package net.kwatts.android.droidcommandpro;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import timber.log.Timber;


public class WelcomeActivity extends AppCompatActivity //implements ActivityCompat.OnRequestPermissionsResultCallback
{

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private static final int AUDIO_PERMISSION_REQUEST_CODE = 2;

    WebView mWebView;


    public void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);

            boolean userIsOnboard = App.mSharedPref.getBoolean("first_time_run", false);
            if (userIsOnboard) {
                startMainActivity();
            }
            setContentView(R.layout.welcome_activity);

            //ImageView image = (ImageView) findViewById(R.id.welcome_netscanner_logo);
            //image.setImageResource(R.drawable.antenna_blue_48x48);

            mWebView = findViewById(R.id.welcome_webview);
            StringBuilder outXML = new StringBuilder();
            outXML.append("<html><head><style type=\"text/css\">");
            outXML.append("body { font-size: 1em; }</style></head><body>");
            outXML.append(App.APP_DESCRIPTION_HTML);
            outXML.append("</body></html>");
            mWebView.loadData(outXML.toString(), "text/html", "utf-8");

            Button startButton = findViewById(R.id.startButton);
            startButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    App.mSharedPref.edit().putBoolean("first_time_run", true).commit();
                    startMainActivity();
                }
            });

            //checkPermissions();

        } catch (Exception e) {
            Timber.e(e);
        }

    }

    protected void onResume() {
        super.onResume();
    }

    protected void onPause() {
        super.onPause();
    }

    protected void onStart() {
        super.onStart();
    }

    protected void onDestroy() {
        super.onDestroy();
    }

    public void startMainActivity() {
        Intent intent = new Intent(App.INSTANCE, MainActivity.class);
        startActivity(intent);
        this.finish();
    }

    /*

    private void checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Request permission without stopping activity
            PermissionUtils.requestPermission(this, LOCATION_PERMISSION_REQUEST_CODE,
                    Manifest.permission.ACCESS_FINE_LOCATION, false);
        } else {
            Timber.d("We already have permission for location.");
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            // Request permission without stopping activity
            PermissionUtils.requestPermission(this, AUDIO_PERMISSION_REQUEST_CODE,
                    Manifest.permission.RECORD_AUDIO, false);
        } else {
            Timber.d("We already have permission for audio.");
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (PermissionUtils.isPermissionGranted(permissions, grantResults, Manifest.permission.ACCESS_FINE_LOCATION)) {
                Timber.d("We have location permission");
            } else {
                Timber.d("Location permission denied");
                //mPermissionDenied = true;
                //requestMyLocation();
            }
        }

        if (requestCode == AUDIO_PERMISSION_REQUEST_CODE) {
            if (PermissionUtils.isPermissionGranted(permissions, grantResults, Manifest.permission.RECORD_AUDIO)) {
                Timber.d("We have record audio permission");
            } else {
                Timber.d("Record audio permission denied");
            }

        }
    }

    */


}


