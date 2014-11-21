package app.wearable.gdg.com.lecturer;

/**
 * Created by idanakav on 20/11/14.
 */


        import android.util.Log;

        import com.google.android.gms.common.api.GoogleApiClient;
        import com.google.android.gms.wearable.DataEventBuffer;
        import com.google.android.gms.wearable.MessageEvent;
        import com.google.android.gms.wearable.Node;
        import com.google.android.gms.wearable.Wearable;
        import com.google.android.gms.wearable.WearableListenerService;

        import org.java_websocket.client.WebSocketClient;
        import org.java_websocket.handshake.ServerHandshake;
        import org.json.JSONException;
        import org.json.JSONObject;

        import java.net.URI;
        import java.net.URISyntaxException;


public class WearService extends WearableListenerService {

    Node peerNode;
    GoogleApiClient googleApiClient;
    private WebSocketClient mWebSocketClient;
    URI uri;
    @Override
    public void onCreate() {

        super.onCreate();
        Log.i(WearService.class.getSimpleName(), "WEAR create");
        googleApiClient=  new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .build();
        googleApiClient.connect();

        try {
            uri = new URI("ws://132.72.234.215:9000/deckWS?p=http://slides.com/idannakav/slidewear/live#/");
        } catch (URISyntaxException e) {
            e.printStackTrace();
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
                        Log.i("webservice", s);

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


    @Override
    public void onDestroy() {
        super.onDestroy();
        mWebSocketClient.close();
        Log.i(WearService.class.getSimpleName(), "WEAR destroy");
    }


    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        super.onDataChanged(dataEvents);
        Log.i(WearService.class.getSimpleName(), "WEAR Data changed " );
    }


    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        super.onMessageReceived(messageEvent);
        Log.i(WearService.class.getSimpleName(), "WEAR Message " + messageEvent.getPath());


        // Send the RPC

        String blat= "blat";
        Wearable.MessageApi.sendMessage(
                googleApiClient,
                messageEvent.getSourceNodeId(),
                "wear",
                blat.getBytes()
        );




    }


    @Override
    public void onPeerConnected(Node peer) {
        super.onPeerConnected(peer);
        Log.i(WearService.class.getSimpleName(), "WEAR Connected ");
        peerNode = peer;



    }


    @Override
    public void onPeerDisconnected(Node peer) {
        super.onPeerDisconnected(peer);
        Log.i(WearService.class.getSimpleName(), "WEAR Disconnected");
    }
}