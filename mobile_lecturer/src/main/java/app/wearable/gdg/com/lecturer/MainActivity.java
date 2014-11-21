package app.wearable.gdg.com.lecturer;

import android.app.ActivityManager;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.ValueCallback;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;
import java.net.URISyntaxException;


public class MainActivity extends ActionBarActivity implements   MessageApi.MessageListener,
        GoogleApiClient.ConnectionCallbacks {
    WebView webView;
    Button right,left;
    JsHandler js;
    Node peerNode;
    private int totalPages;
    String url = "http://slides.com/idannakav/slidewear/live#/";

    String speaker = "http://slides.com/idannakav/slidewear/speaker";
    private GoogleApiClient mGoogleApiClient;
    private WebSocketClient mWebSocketClient;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        webView = (WebView) findViewById(R.id.web_view);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new Callback());
        webView.loadUrl(speaker);

        js = new JsHandler(this,webView);


        right = (Button) findViewById(R.id.right);
        right.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                js.javaFnCall("Reveal.right();");
            }
        });
        left = (Button) findViewById(R.id.left);
        left.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                js.javaFnCall("Reveal.left();");
            }
        });
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(ConnectionResult result) {
                        Log.d("Wear", "onConnectionFailed: " + result);
                    }
                })
                .addApi(Wearable.API)
                .build();

        webView.setWebViewClient(new WebViewClient() {

            public void onPageFinished(WebView view, String url) {
                webView.evaluateJavascript("Reveal.getTotalSlides()",new ValueCallback<String>() {
                    @Override
                    public void onReceiveValue(String value) {
                    try {
                       totalPages= Integer.parseInt(value);
                        Wearable.MessageApi.sendMessage(
                                mGoogleApiClient,
                                peerNode.getId(),
                                "totalPages//"+totalPages,
                                null
                        );
                    }catch (RuntimeException ignored) {}
                    }
                });

            }



        });



        mGoogleApiClient.connect();

    }
    private class Callback extends WebViewClient {  //HERE IS THE MAIN CHANGE.

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            return (false);
        }

    }
    @Override
    public void onConnected(Bundle bundle) {
        Log.v("Wear", "connected to Google Play Services on Wear!");
        Wearable.MessageApi.addListener(mGoogleApiClient, this).setResultCallback(resultCallback);
    }
    @Override
    public void onMessageReceived(final MessageEvent messageEvent) {


        /*
        This method apparently runs in a background thread.
         */


        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(messageEvent.getPath().endsWith("connected")){

                }
                else{

                }
            }
        });

        if(messageEvent.getPath().contains("right"))
            js.javaFnCall("Reveal.right();");
        else
            js.javaFnCall("Reveal.left();");

        Log.v("Handled", "Message received on wear: " + messageEvent.getPath());

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
    protected void onStop() {
        super.onStop();
        Wearable.MessageApi.removeListener(mGoogleApiClient, this);

    }

    @Override
    protected void onResume() {
        super.onResume();
        connectWebSocket();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mWebSocketClient.close();
    }

    @Override
    public void onConnectionSuspended(int i) {
    }
    private void sendStartMessage(){


        NodeApi.GetConnectedNodesResult rawNodes =
                Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).await();


        for (final Node node : rawNodes.getNodes()) {
            Log.v("Wear", "Node: " + node.getId());
            PendingResult<MessageApi.SendMessageResult> result = Wearable.MessageApi.sendMessage(
                    mGoogleApiClient,
                    node.getId(),
                    "/start",
                    null
            );


            result.setResultCallback(new ResultCallback<MessageApi.SendMessageResult>() {
                @Override
                public void onResult(MessageApi.SendMessageResult sendMessageResult) {
                    //  The message is done sending.
                    //  This doesn't mean it worked, though.
                    Log.v("Wear", "Our callback is done.");
                    peerNode = node;    //  Save the node that worked so we don't have to loop again.
                }
            });
        }
    }

    /**
     * Not needed, but here to show capabilities. This callback occurs after the MessageApi
     * listener is added to the Google API Client.
     */
    private ResultCallback<Status> resultCallback =  new ResultCallback<Status>() {
        @Override
        public void onResult(Status status) {
            Log.v("Wear", "Status: " + status.getStatus().isSuccess());
            new AsyncTask<Void, Void, Void>(){
                @Override
                protected Void doInBackground(Void... params) {
                    sendStartMessage();
                    return null;
                }
            }.execute();
        }
    };
    private void connectWebSocket() {
        URI uri;
        try {
            uri = new URI("ws://132.72.234.215:9000/deckWS?p="+url);
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return;
        }

        mWebSocketClient = new WebSocketClient(uri) {
            @Override
            public void onOpen(ServerHandshake serverHandshake) {

            }

            @Override
            public void onMessage(final String s) {
                final String message = s;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {


                    }
                });
                Wearable.MessageApi.sendMessage(
                        mGoogleApiClient,
                        peerNode.getId(),
                        "hands//"+s,
                        null
                );
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
