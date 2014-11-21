package app.wearable.gdg.com.lecturer;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.PaintDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

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
import com.google.gson.Gson;

public class MainActivity extends Activity implements   MessageApi.MessageListener,
        GoogleApiClient.ConnectionCallbacks
 {

    private TextView mTextView;
    private GoogleApiClient mGoogleApiClient;
    private Node peerNode;
    private ImageButton right,left;
    private ViewGroup container;
    int currentPage;
    private TextView timer;
     long seconds;
    int totalPages;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i("WEAR", "OnCreate");
        setContentView(R.layout.activity_main);

        //  Is needed for communication between the wearable and the device.
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
        mGoogleApiClient.connect();

        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                mTextView = (TextView) stub.findViewById(R.id.text);
                mTextView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.i("WEAR","Sending");
                        sendMessage("start");
                    }
                });

                left = (ImageButton) stub.findViewById(R.id.left);
                right = (ImageButton) stub.findViewById(R.id.right);
                left.setVisibility(View.GONE);
                left.setOnClickListener(new View.OnClickListener() {


                    @Override
                    public void onClick(View v) {
                        container.setBackgroundColor(getResources().getColor(R.color.g1));
                        if(currentPage >1) {
                            sendMessage("left");
                            currentPage--;
                            mTextView.setText(""+currentPage+"/"+totalPages);
                            right.setVisibility(View.VISIBLE);
                            if(currentPage == 1)
                                left.setVisibility(View.GONE);
                        }
                    }
                });

                right.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        container.setBackgroundColor(getResources().getColor(R.color.g1));
                        if(currentPage <totalPages) {
                            sendMessage("right");
                            currentPage++;
                            mTextView.setText(""+currentPage+"/"+totalPages);
                            left.setVisibility(View.VISIBLE);
                            if(currentPage == totalPages)
                                right.setVisibility(View.GONE);
                        }

                    }
                });

                container = (ViewGroup) stub.findViewById(R.id.container);

/*                new CountDownTimer(300000000, 1000) {

                    public void onTick(long millisUntilFinished) {
                        seconds++;
                        timer.setText(""+seconds);
                    }

                    public void onFinish() {
                        timer.setText("done!");
                    }
                }.start();*/

            }
        });


    }

     @Override
     public void onConnected(Bundle bundle) {
         Log.v("Wear", "connected to Google Play Services on Wear!");
         Wearable.MessageApi.addListener(mGoogleApiClient, this).setResultCallback(resultCallback);
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

     /**
      * This method will generate all the nodes that are attached to a Google Api Client.
      * Now, theoretically, only one should be: the phone. However, they return us more
      * a list. In the case where the phone happens to not be the first/only, I decided to
      * make a List of all the nodes and we'll loop through them and send each of them
      * a message. After getting the list of nodes, it sends a message to each of them telling
      * it to start. One the last successful node, it saves it as our one peerNode.
      */
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
      * This simply sends a message to the phone with the path "/lights/all/on" or "lights/all/off".
      * This is set up to be expandable, so you can target specific lights, but will probably never
      * become that right now. {@code setResultCallback} can be used in place of {@code await}; the
      * former will make the call asynchronously and provide a callback for when it's completed.
      * @param path
      */
     public void sendMessage(String path){
         String togglePath = "/wear/" + path;


         PendingResult<MessageApi.SendMessageResult> result = Wearable.MessageApi.sendMessage(
                 mGoogleApiClient,
                 peerNode.getId(),
                 togglePath,
                 null
         );


         result.setResultCallback(new ResultCallback<MessageApi.SendMessageResult>() {
             @Override
             public void onResult(MessageApi.SendMessageResult sendMessageResult) {
             }
         });
     }


     /**
      * This method receives messages from the connected device.
      * For some reason, trying to alter views in this method threw thread errors.
      * To solve this, I simple use {@code runOnUiThread}.
      * @param messageEvent
      */
     @Override
     public void onMessageReceived(final MessageEvent messageEvent) {


        /*
        This method apparently runs in a background thread.
         */


         runOnUiThread(new Runnable() {
             @Override
             public void run() {
               parseMessage(messageEvent.getPath());
             }
         });


         Log.v("Wear", "Message received on wear: " + messageEvent.getPath());
     }


     @Override
     protected void onStop() {
         super.onStop();
         Wearable.MessageApi.removeListener(mGoogleApiClient, this);
     }


     @Override
     public void onConnectionSuspended(int i) {

     }


private void setBackground(int hands,int total) {
    final float from = (float)hands/(float)total;
    ShapeDrawable.ShaderFactory sf = new ShapeDrawable.ShaderFactory() {
        @Override
        public Shader resize(int width, int height) {
            LinearGradient lg = new LinearGradient(width/2, 0, width/2, height,
                    new int[]{getResources().getColor(R.color.r1),
                            getResources().getColor(R.color.r2),
                            getResources().getColor(R.color.g1),
                            getResources().getColor(R.color.g2)},
                    new float[] {
                            0, from, from+0.01f, 1 }, Shader.TileMode.REPEAT);
            return lg;
        }
    };

    PaintDrawable p=new PaintDrawable();
    p.setShape(new RectShape());
    p.setShaderFactory(sf);
    container.setBackground(p);
    container.invalidate();
}


     /** ASSUMING MSG IS TAG/VALUE **/
private void parseMessage(String msg) {
    String[] p = msg.split("//");
    if(p[0].equals("totalPages")) {
        currentPage = 1;
        totalPages = Integer.parseInt(p[1]);
        mTextView.setText(""+currentPage+"/"+totalPages);
    }


    if(p[0].equals("hands")) {
        Gson gson = new Gson();
        Hands hands = gson.fromJson(p[1], Hands.class);
        setBackground(hands.hands,hands.total);
    }


}


}
