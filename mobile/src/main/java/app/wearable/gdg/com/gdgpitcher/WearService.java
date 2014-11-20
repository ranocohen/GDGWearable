package app.wearable.gdg.com.gdgpitcher;

/**
 * Created by idanakav on 20/11/14.
 */


        import android.util.Log;

        import com.google.android.gms.common.ConnectionResult;
        import com.google.android.gms.common.api.GoogleApiClient;
        import com.google.android.gms.wearable.DataEventBuffer;
        import com.google.android.gms.wearable.MessageEvent;
        import com.google.android.gms.wearable.Node;
        import com.google.android.gms.wearable.Wearable;
        import com.google.android.gms.wearable.WearableListenerService;

        import java.util.concurrent.TimeUnit;


public class WearService extends WearableListenerService {

    Node peerNode;
    GoogleApiClient googleApiClient;
    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(WearService.class.getSimpleName(), "WEAR create");
        googleApiClient=  new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .build();
        googleApiClient.connect();

    }


    @Override
    public void onDestroy() {
        super.onDestroy();
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