package app.wearable.gdg.com.gdgpitcher;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;

import com.github.lzyzsd.circleprogress.DonutProgress;


public class MainActivity extends ActionBarActivity {
    WebView webView;
    Button button;
    JsHandler js;

    private DonutProgress donutProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        donutProgress = (DonutProgress) findViewById(R.id.donut_progress);

        webView = (WebView) findViewById(R.id.web_view);
        MainActivity.this.webView.setVisibility(View.INVISIBLE);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new Callback());
        webView.setWebChromeClient(new MyWebViewClient());

        donutProgress = (DonutProgress) findViewById(R.id.donut_progress);
        donutProgress.setMax(100);

        webView.loadUrl("http://slides.com/idannakav/dsdsds/live#/");

        js = new JsHandler(this,webView);

    }
    private class Callback extends WebViewClient {  //HERE IS THE MAIN CHANGE.

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            return (false);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            MainActivity.this.donutProgress.setVisibility(View.INVISIBLE);
            MainActivity.this.webView.setVisibility(View.VISIBLE);
        }
    }

    private class MyWebViewClient extends WebChromeClient {
        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            MainActivity.this.setValue(newProgress);
            super.onProgressChanged(view, newProgress);
        }
    }

    public void setValue(int progress) {
        this.donutProgress.setProgress(progress);
    }
}
