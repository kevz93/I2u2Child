package keev.i2u2child;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class botSelectActivity extends AppCompatActivity {
    private JSONObject auth_data;
    private Firebase usersref,botref;
    public String TAG = "keev.i2u2child.botSelectActivity";
    boolean newUser = false;
    boolean nameAvail = false;
    Map<String, Object> emailMap;
    Map<String, Object> botMap ;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Firebase.setAndroidContext(this);
        usersref = new Firebase("https://i2u2robot.firebaseio.com/users/");
        botref = new Firebase("https://i2u2robot.firebaseio.com/bots/");
        botMap = new HashMap<String, Object>();
        emailMap = new HashMap<String, Object>();
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            try {
                auth_data = new JSONObject(extras.getString("AUTH_DATA"));
            } catch (JSONException e) {
                e.printStackTrace();
                Log.d(TAG, "Malformed JSON object found");
            }
        }
        setContentView(R.layout.activity_bot_select);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        Log.d(TAG, auth_data.toString());
        try {
            Log.d(TAG, auth_data.getString("id"));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        usersref.child(getAuth("email")).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {

                if (snapshot.getValue() == null) {
                    Log.d(TAG, "null");
                    newUser = true;
                    newDialogue();
                } else {
                    Log.d(TAG, "Not null");
                    emailMap.put("online", true);
                    usersref.child(getAuth("email")).updateChildren(emailMap);
                }
            }


            @Override
            public void onCancelled(FirebaseError e) {

            }

        });

    }

    public void newDialogue(){
        // create a Dialog component
        final Dialog dialog = new Dialog(this);
        //tell the Dialog to use the dialog.xml as it's layout description
        dialog.setContentView(R.layout.new_diag);
        dialog.setTitle("i2u2");
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.MATCH_PARENT;
        dialog.getWindow().setAttributes(lp);
        final EditText inpName = (EditText) dialog.findViewById(R.id.inp);
        final TextView errorTV = (TextView) dialog.findViewById(R.id.eTV);
        final Button goButton = (Button) dialog.findViewById(R.id.goButton);
        goButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG,"cllllliiickkkkk");
                if(nameAvail) {
                    botMap.put("online",true);
                    botref.child(inpName.getText().toString()).updateChildren(botMap);
                    emailMap.put("online", true);
                    usersref.child(getAuth("email")).updateChildren(emailMap);
                    dialog.hide();
                }
            }
        });
        goButton.setEnabled(false);
        inpName.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                // you can call or do what you want with your EditText here
                if (s.toString().length() > 5) {            //TODO: make a max limit too
                    String name = s.toString();
                    botref.child(name).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot snapshot) {
                            if (snapshot.getValue() == null) {
                                errorTV.setTextColor(Color.rgb(0, 255, 0));
                                errorTV.setText("Name available");
                                nameAvail = true;
                                goButton.setEnabled(true);
                            } else {
                                errorTV.setTextColor(Color.rgb(255, 0, 0));
                                goButton.setEnabled(false);
                                nameAvail = false;
                                errorTV.setText("Name not available");
                            }
                        }


                        @Override
                        public void onCancelled(FirebaseError e) {
                        }
                    });
                } else {
                    errorTV.setTextColor(Color.rgb(255, 0, 0));
                    goButton.setEnabled(false);
                    errorTV.setText("Please enter min 5 letters and avoid '#','@','!','%','$','&','*'");
                }
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        });
        dialog.show();
    }

    public String getAuth(String auth) {
        try {
            switch (auth) {
                case "id":
                    return auth_data.getString("id");
                case "email":
                    return auth_data.getString("email").replace(".", "");

            }
        } catch (JSONException e) {
            Log.d(TAG, "JSON parse problem");
        }
        return "null";
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "botSelect Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://keev.i2u2child/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
    }

    @Override
    public void onStop() {
        super.onStop();
        if(!newUser) {
            emailMap.put("online", false);
            usersref.child(getAuth("email")).updateChildren(emailMap);
        }
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "botSelect Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://keev.i2u2child/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        client.disconnect();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
