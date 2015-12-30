package keev.i2u2child;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.IO.Options;
import com.github.nkzawa.socketio.client.Socket;
import com.github.nkzawa.socketio.client.SocketIOException;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;


/**
 * Created by Kev on 28.12.2015.
 */

public class SocketService1 extends Service {
    private final String TAG="SocketService1";
    private JSONObject user_data;
    private Socket socket;
    private final IBinder mBinder = new MyBinder();
    public class MyBinder extends Binder {
        SocketService1 getService() {
            return SocketService1.this;
        }
    }

    @Override
    public void onCreate(){
        super.onCreate();

        Log.d(TAG, "onCreate");

    }
    Bundle extras;
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //TODO do something useful
        Log.d(TAG, "onStartCommand");
        extras = intent.getExtras();

        Thread d = new Thread(new Runnable() {
            @Override
            public void run() {
            }
        });
        d.start();

        Options opts = new Options();
        opts.forceNew = true;
        opts.reconnection = true;
        try {
            socket = IO.socket("http://52.88.42.61:7070", opts);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        socket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Log.d(getClass().getCanonicalName(), "Connected to server");
            }

        }).on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {

            @Override
            public void call(Object... arg0) {
                Log.d(getClass().getCanonicalName(), "Disconnected from server");
            }

        }).on("friendcall", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                JSONObject data = (JSONObject) args[0];
                Log.d(TAG, "Handling friendcall");
                try {
                    String callFrom = data.getString("from");
                    Log.d(TAG, "Call from : " + callFrom);
                } catch (JSONException e) {
                    Log.d(TAG, "friend call object cannot be parsed");
                }
                Intent serviceToXwalk = new Intent(SocketService1.this, xwalkActivity.class);
                try {
                    serviceToXwalk.putExtra("CALL_DATA", user_data.getString("botName"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                startActivity(serviceToXwalk);
            }
        });
                    if(extras!=null){
                        try {
                            user_data = new JSONObject(extras.getString("USER_DATA"));
                            socket.emit("giveMeID", user_data.getString("email"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
        socket.connect();

        return Service.START_REDELIVER_INTENT;
    }


    @Override
    public IBinder onBind(Intent intent) {
        //TODO for communication return IBinder implementation
        return null;
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        socket.disconnect();
        Log.d(TAG,"onDestroy");
    }

}