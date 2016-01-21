package keev.i2u2child;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.http.SslError;
import android.os.PowerManager;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.webkit.ValueCallback;

import com.firebase.client.Firebase;

import org.xwalk.core.XWalkResourceClient;
import org.xwalk.core.XWalkView;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class xwalkActivity extends AppCompatActivity {
    private class MyResourceClient extends XWalkResourceClient {
        public MyResourceClient(XWalkView view) {
            super(view);
        }

        @Override
        public void onReceivedSslError(XWalkView view,
                                       ValueCallback<Boolean> callback, SslError error) {
            callback.onReceiveValue(true);
        }
    }
    MyResourceClient resourceClient;
    private final String TAG = "xwalkActivity";
    private XWalkView mXWalkView;
    public Context global_context;
    private String call_data;
    private String address = null;
    public BluetoothAdapter mAdapter = null;
    //public BluetoothSocket btSocket = null;
    private BluetoothDevice device =null;
    public ConnectThread mConnectThread;
    public ConnectedThread mConnectedThread;
    private String ArduinoPacket;
    private String roomName;
    private boolean BLUEBOOL = true;  //TODO: toggle for development
    private Firebase botref;
    private final int MY_PERMISSIONS_REQUEST = 24;
    Map<String, Object> botMap = new HashMap<String, Object>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAdapter = BluetoothAdapter.getDefaultAdapter();
        Firebase.setAndroidContext(this);
        botref = new Firebase("https://i2u2robot.firebaseio.com/bots/");
        //Get prev intent message :
        Bundle extras = getIntent().getExtras();
        if(extras !=null) {
            roomName = extras.getString("CALL_DATA"); // use call data if json is used in future for more options
            call_data = roomName;
        }
        botMap.put("status", "online");
        botref.child(roomName).updateChildren(botMap);
        botMap = new HashMap<String, Object>();

        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.WRITE_SETTINGS},
                MY_PERMISSIONS_REQUEST);


        setContentView(R.layout.activity_xwalk);
        mXWalkView = (XWalkView) findViewById(R.id.activity_main);
        resourceClient =new MyResourceClient(mXWalkView);
        mXWalkView.setResourceClient(resourceClient);
        mXWalkView.addJavascriptInterface(new WebAppInterface(this), "Android");
        mXWalkView.load("file:///android_asset/index.html", null);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); // keeps screen on
       // global_context = this; //TODO: re-use later
        if (mAdapter == null) {
            Log.d("TAG","No bluetooth adapter available");
        }
        if(BLUEBOOL) {
            Set<BluetoothDevice> pairedDevices = mAdapter.getBondedDevices();
            if (pairedDevices.size() > 0) {
                for (BluetoothDevice bt : pairedDevices) {
                    if (bt.getName().equals("I2U2")) {
                        device = bt;
                        break;
                    }
                }
            }
            // Start the thread to connect with the given device
            mConnectThread = new ConnectThread(device);
            mConnectThread.start();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        public ConnectThread(BluetoothDevice device) {
            mmDevice = device;
            BluetoothSocket tmp = null;
            UUID myUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
            // Get a BluetoothSocket for a connection with the
            // given BluetoothDevice
            try {
                    tmp = mmDevice.createRfcommSocketToServiceRecord(myUID);

            } catch (IOException e) {
                Log.e(TAG, "Socket create() failed", e);
            }
            mmSocket = tmp;
        }

        public void run() {
            Log.i(TAG, "BEGIN mConnectThread");

            // Always cancel discovery because it will slow down a connection
            mAdapter.cancelDiscovery();

            // Make a connection to the BluetoothSocket
            try {
                // This is a blocking call and will only return on a
                // successful connection or an exception
                mmSocket.connect();
            } catch (IOException e) {
                // Close the socket
                try {
                    mmSocket.close();
                } catch (IOException e2) {
                    Log.e(TAG, "unable to close() socket during connection failure", e2);
                }
                return;
            }

            // Start the connected thread
            mConnectedThread = new ConnectedThread(mmSocket);
            mConnectedThread.start();
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of connect socket failed", e);
            }
        }
    }


    /**
     * This thread runs during a connection with a remote device.
     * It handles all incoming and outgoing transmissions.
     */
    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            Log.d(TAG, "create ConnectedThread: ");
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the BluetoothSocket input and output streams
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.e(TAG, "temp sockets not created", e);
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            Log.i(TAG, "BEGIN mConnectedThread");
            byte[] buffer = new byte[1024];
            int bytes;
            int readBufferPosition = 0;
            // Keep listening to the InputStream while connected
            while (true) {
                try {
                    int bytesAvailable = mmInStream.available();
                    if(bytesAvailable>0)
                    {
                        byte[] packetBytes = new byte[bytesAvailable];
                        mmInStream.read(packetBytes);
                        for(int i=0;i<bytesAvailable;i++) {
                            byte b = packetBytes[i];
                            buffer[readBufferPosition++] = b;
                        }
                        byte[] encodedBytes = new byte[readBufferPosition];
                        System.arraycopy(buffer, 0, encodedBytes, 0, encodedBytes.length);
                        ArduinoPacket = new String(encodedBytes, "US-ASCII");
                        //Log.d(TAG,ArduinoPacket);
                        readBufferPosition = 0;
                        try {
                            sleep(50);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                } catch (IOException e) {
                    Log.e(TAG, "disconnected", e);
                    // Start the service over to restart listening mode
                    break;
                }
            }
        }

        /*
         * Write to the connected OutStream.
         *
         * @param buffer The bytes to write
         */
        public void write(String s) {
//            int NumBytes;
//            int count_int;
//            byte[] writeBuffer;
//            writeBuffer = new byte[512];
//            if(s.length() != 0) {
//                NumBytes = s.length();
//                for(count_int = 0; count_int < NumBytes; count_int++) {
//                    writeBuffer[count_int] = (byte)s.charAt(count_int);
//                }
//            }
            try {
                mmOutStream.write(s.getBytes());
            } catch (IOException e) {
                Log.e(TAG, "Exception during write", e);
            }
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of connect socket failed", e);
            }
        }
    }

        public class WebAppInterface {
            Context mContext;

            /**
             * Instantiate the interface and set the context
             */
            WebAppInterface(Context c) {
                mContext = c;
            }

            /**
             * Show a toast from the web page
             */
            @org.xwalk.core.JavascriptInterface
            public String getCallData() {
                return call_data;
            }

            @org.xwalk.core.JavascriptInterface
            public String getArduinoPacket() {
                return ArduinoPacket;
            } // TODO: Return AndroidPacket here

            @org.xwalk.core.JavascriptInterface
            public void Arduino(String s) {
                mConnectedThread.write(s);
            }
        }
    @Override
    protected void onDestroy() {
        super.onDestroy();
//        mConnectedThread.cancel();
//        mConnectThread.cancel();
        mConnectedThread.write("b");
        botMap.put("status", "offline");
        botref.child(roomName).updateChildren(botMap);
    }
}
