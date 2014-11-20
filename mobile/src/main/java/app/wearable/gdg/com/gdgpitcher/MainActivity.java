package app.wearable.gdg.com.gdgpitcher;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.webkit.WebView;

public class MainActivity extends ActionBarActivity {
    WebView webView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        webView = (WebView) findViewById(R.id.web_view);

        Intent intent = new Intent(this, ZXingDemo.class);
        startActivity(intent);



    }
}
