package app.wearable.gdg.com.gdgpitcher;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;

import me.dm7.barcodescanner.zbar.Result;
import me.dm7.barcodescanner.zbar.ZBarScannerView;

public class ZBarActivity extends ActionBarActivity implements ZBarScannerView.ResultHandler {

    private ZBarScannerView mScannerView;

    private static final String TAG = "ZXINGDemo" ;

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.qrlayout);
        //mScannerView = new ZBarScannerView(this);    // Programmatically initialize the scanner view
        mScannerView = (ZBarScannerView) findViewById(R.id.scanner_view);
        //setContentView(mScannerView);                // Set the scanner view as the content view
    }

    @Override
    public void onResume() {
        super.onResume();
        mScannerView.setResultHandler(this); // Register ourselves as a handler for scan results.
        mScannerView.startCamera();          // Start camera on resume
    }

    @Override
    public void onPause() {
        super.onPause();
        mScannerView.stopCamera();           // Stop camera on pause
    }

    @Override
    public void handleResult(Result rawResult) {
        Intent mIntent = new Intent(this, MainActivity.class);
        Bundle mBundle = new Bundle();
        mBundle.putString("url", rawResult.getContents());
        mIntent.putExtras(mBundle);
        startActivity(mIntent);
        // Do something with the result here
        Log.v(TAG, rawResult.getContents()); // Prints scan results
        Log.v(TAG, rawResult.getBarcodeFormat().toString()); // Prints the scan format (qrcode, pdf417 etc.)
    }

}