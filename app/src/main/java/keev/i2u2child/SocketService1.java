package keev.i2u2child;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;


import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;


/**
 * Created by Kev on 28.12.2015.
 */

public class SocketService1 extends Service {
    private final String TAG="SocketService1";
    private JSONObject user_data;
    private Firebase usersref, botref;
    Map<String, Object> emailMap = new HashMap<String, Object>();
    Map<String, Object> botMap = new HashMap<String, Object>();

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
    String roomName;
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //TODO do something useful
        Firebase.setAndroidContext(this);
        usersref = new Firebase("https://i2u2robot.firebaseio.com/users/");
        botref = new Firebase("https://i2u2robot.firebaseio.com/bots/");
        Log.d(TAG, "onStartCommand");
        extras = intent.getExtras();
        try {
            roomName = new JSONObject(extras.getString("CALL_DATA")).getString("botName");
        } catch (JSONException e) {
            //
        }
//        Thread d = new Thread(new Runnable() {
//            @Override
//            public void run() {
//            }
//        });
//        d.start();
        botMap.put("status", "online");
        botref.child(roomName).updateChildren(botMap);
        botMap = new HashMap<String, Object>();
        botref.child(roomName).child("incoming").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() == false) {
                    // Think of some handle here =P
                } else {
                    Intent xwalk = new Intent(SocketService1.this, xwalkActivity.class);
                    xwalk.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    xwalk.putExtra("CALL_DATA", roomName);
                    startActivity(xwalk);
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
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
        botMap = new HashMap<String, Object>();
        botMap.put("status", "offline");
        botref.child(roomName).updateChildren(botMap);
        botMap = new HashMap<String, Object>();
        Log.d(TAG,"onDestroy");
    }

}