package keev.i2u2child;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

public class blueActivity extends AppCompatActivity {
    private static final String TAG = "blueActivity";
    TextView myTV;
    BluetoothAdapter mBluetoothAdapter;
    Thread workerThread;

    byte[] readBuffer;
    int readBufferPosition;
    volatile boolean stopWorker;
    boolean botFound=false;
    boolean  botPaired = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blue);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        myTV = (TextView) findViewById(R.id.TV);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "woooooohoooooooooooooo", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                mBluetoothAdapter.cancelDiscovery();
                mBluetoothAdapter.disable();
                Intent loginIntent = new Intent(blueActivity.this, LoginActivity.class);
                startActivity(loginIntent);
            }
        });
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            myTV.setText("No bluetooth adapter available");
        }
        else {
            myTV.setText("Bluetooth available.");
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBluetooth, 0);
            }
            myTV.setText("Checking Paired list");
            Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
            if (pairedDevices.size() > 0) {
                for (BluetoothDevice device : pairedDevices) {
                    if (device.getName().equals("I2U2")) {
                        myTV.setText("Pair found. Now scanning for bot.");
                        botPaired = true;
                        break;
                    }
                }
            }

            IntentFilter filter = new IntentFilter();
            filter.addAction(BluetoothDevice.ACTION_FOUND);
            filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
            this.registerReceiver(mReceiver, filter); // Register the BroadcastReceiver

            myTV.setText("Now scanning");
            mBluetoothAdapter.startDiscovery();
            myTV.setText("Discovery started");
        }
    }


    // Create a BroadcastReceiver for ACTION_FOUND
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                myTV.setText("Found :" + device.getName());
                if (device.getName().equals("I2U2")) {
                    unregisterReceiver(mReceiver);
                    myTV.setText("i2u2 bot found. ^_^");
                    mBluetoothAdapter.cancelDiscovery();
                    Log.v(TAG, "going next");
                    if(botPaired) {
                        Intent loginIntent = new Intent(blueActivity.this, LoginActivity.class);
                        startActivity(loginIntent);
                    }
                    else
                        myTV.setText("Bot found but not paired. Please pair first.");
                }
            }
            else if (mBluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                myTV.setText("No bot nearby :( ");
                if(!botPaired)
                    myTV.setText("Please pair bot first =)");
            }
        }
    };


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_blue, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mBluetoothAdapter.cancelDiscovery();
       // mBluetoothAdapter.disable();
    }
}
