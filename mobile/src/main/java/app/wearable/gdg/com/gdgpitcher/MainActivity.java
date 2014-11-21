package app.wearable.gdg.com.gdgpitcher;

import android.os.Build;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.Toast;

import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.github.lzyzsd.circleprogress.DonutProgress;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;
import java.net.URISyntaxException;



public class MainActivity extends ActionBarActivity {
    WebView webView;
    Button button;
    JsHandler js;
    private WebSocketClient mWebSocketClient;
    FloatingActionsMenu fam;

    private DonutProgress donutProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.fab_lost).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "I'm lost", Toast.LENGTH_SHORT).show();
            }
        });

        findViewById(R.id.fab_takenote).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "note", Toast.LENGTH_SHORT).show();
            }
        });
/*
        findViewById(R.id.pink_icon).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "Clicked pink Floating Action Button", Toast.LENGTH_SHORT).show();
            }
        });
*/
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

        connectWebSocket();

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

    private void connectWebSocket() {
        URI uri;
        try {
            uri = new URI("ws://132.72.234.215:9000/viewerWS");
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return;
        }

        mWebSocketClient = new WebSocketClient(uri) {
            @Override
            public void onOpen(ServerHandshake serverHandshake) {
                Log.i("Websocket", "Opened");
                JSONObject object = new JSONObject();
                try {
                    object.put("type", "clearhands");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                System.out.println(object);
                mWebSocketClient.send(object.toString());
                Log.i("Webservice", object.toString());

            }

            @Override
            public void onMessage(String s) {
                final String message = s;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //TextView textView = (TextView)findViewById(R.id.messages);
                        //textView.setText(textView.getText() + "\n" + message);
                        Log.i("webservice", message.toString());
                    }
                });
            }

            @Override
            public void onClose(int i, String s, boolean b) {
                Log.i("Websocket", "Closed " + s);
            }

            @Override
            public void onError(Exception e) {
                Log.i("Websocket", "Error " + e.getMessage());
            }
        };
        mWebSocketClient.connect();
    }

    public void sendMessage(View view) {
        mWebSocketClient.send("test");
    }
}
