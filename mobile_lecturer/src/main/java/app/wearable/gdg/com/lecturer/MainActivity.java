package app.wearable.gdg.com.lecturer;

import android.os.Build;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;

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
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        webView = (WebView) findViewById(R.id.web_view);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new Callback());
        webView.loadUrl("http://slides.com/idannakav/dsdsds/live#/");

        js = new JsHandler(this,webView);


        button = (Button) findViewById(R.id.right);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                js.javaFnCall("Reveal.right();");
            }
        });
        connectWebSocket();
    }
    private class Callback extends WebViewClient {  //HERE IS THE MAIN CHANGE.

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            return (false);
        }

    }

    private void connectWebSocket() {
        URI uri;
        try {
            uri = new URI("ws://132.72.234.215:9000/deckWS");
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
                mWebSocketClient.send(object.toString());
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
}
