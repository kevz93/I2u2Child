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
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.ViewFlipper;

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

public class botSelectActivity extends AppCompatActivity { //TODO : handle non new users
    private JSONObject auth_data;
    private Firebase usersref,botref;
    public String TAG = "keev.i2u2child.botSelectActivity";
    boolean newUser = true;
    boolean nameAvail = false;
    private String botName = null;
    Map<String, Object> emailMap = new HashMap<String, Object>();
    Map<String, Object> botMap = new HashMap<String, Object>();
    startMain foo;
    TextView tv;
    EditText inpName;
    Button goButton;
    ViewFlipper vf;
    boolean botFound = false;
    boolean emailFound = false;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;
    public void addAccess(){
        vf.showNext();
        EditText newBot = (EditText) findViewById(R.id.newBot);
        EditText newMail = (EditText) findViewById(R.id.newMail);
        final TextView accessTV = (TextView) findViewById(R.id.accessTV);
        final Button addBotButton =(Button) findViewById(R.id.addBot);
        accessTV.setText("Enter the bot name and associated email-ID to request access.");
        accessTV.animate().alpha(1f);

        newBot.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                accessTV.animate().alpha(0f);
                addBotButton.setEnabled(false);
                String name = s.toString();
                botref.child(name).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        if (snapshot.getValue() == null) {
                            accessTV.setTextColor(Color.rgb(0, 255, 0));
                            accessTV.setText("No bot with that name yo!");
                            botFound = false;
                            addBotButton.setEnabled(false);
                            accessTV.animate().alpha(1f);
                        } else {
                            accessTV.setTextColor(Color.rgb(255, 0, 0));
                            accessTV.setText("We found the bot!");
                            botFound = true;
                            accessTV.animate().alpha(1f);
                        }

                        if (botFound&&emailFound)
                            addBotButton.setEnabled(true);
                    }

                    @Override
                    public void onCancelled(FirebaseError e) {
                    }
                });
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        });




    }
    private class startMain{
        private startMain(){
            final TextView mybotTV = (TextView) findViewById(R.id.myBotTV);
            tv.animate().alpha(0.0f);
            vf.showNext();
            emailMap.put("online", true);
            emailMap.put("id", getAuth("id"));
            usersref.child(getAuth("email")).updateChildren(emailMap);
            FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
//                    Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                            .setAction("Action", null).show();
                   addAccess();
                }
            });

            usersref.child(getAuth("email")).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    botName = dataSnapshot.child("mybot").getValue().toString();
                    mybotTV.setText(botName);
                    mybotTV.animate().alpha(1f);
                }

                @Override
                public void onCancelled(FirebaseError firebaseError) {

                }
            });
            final CardView myBotCard = (CardView) findViewById(R.id.mybotcard);
            final ViewFlipper myBotFlipper =(ViewFlipper) findViewById(R.id.myBotFlipper);
            Button callmybotButton = (Button) findViewById(R.id.callmybotButton);
            myBotCard.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                            myBotFlipper.showNext();
                }
            });
            }
        }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Firebase.setAndroidContext(this);
        setContentView(R.layout.bot_select_flipper);
        usersref = new Firebase("https://i2u2robot.firebaseio.com/users/");
        botref = new Firebase("https://i2u2robot.firebaseio.com/bots/");
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            
            try {
                auth_data = new JSONObject(extras.getString("AUTH_DATA"));
            } catch (JSONException e) {
                e.printStackTrace();
                Log.d(TAG, "Malformed JSON object found");
            }
        }
        inpName = (EditText) findViewById(R.id.inp);
        goButton = (Button) findViewById(R.id.goButton);
        tv = (TextView) findViewById(R.id.diagTV);
        tv.animate().alpha(1.0f);
        tv.setText("Welcome to i2u2 (:");
        vf = (ViewFlipper) findViewById( R.id.viewFlipper );
        vf.setInAnimation(AnimationUtils.loadAnimation(this,
                android.R.anim.fade_in));
        vf.setOutAnimation(AnimationUtils.loadAnimation(this,
                android.R.anim.fade_out));
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
        usersref.child(getAuth("email")).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {

                if (snapshot.getValue() == null) {
                    Log.d(TAG, "null");
                    newUser = true;
                    newDialogue();
                } else {
                    Log.d(TAG, "Not null");
                    newUser = false;
                    Log.d(TAG, snapshot.toString());
                    foo = new startMain();
                }
              }
            @Override
            public void onCancelled(FirebaseError e) {

            }

        });
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState){
            super.onPostCreate(savedInstanceState);
            Log.d(TAG, auth_data.toString());
            try {
                Log.d(TAG, auth_data.getString("id"));          //TODO: delete method here
            } catch (JSONException e) {
                e.printStackTrace();
            }


        }

    public void newDialogue(){
        final TextView errorTV = (TextView) findViewById(R.id.eTV);
        tv.setText("Hi there ! Welcome to the i2u2 experience.\n Enter a unique name to get started (:");
        tv.animate().alpha(1.0f);
        goButton.animate().alpha(1.0f);
        inpName.animate().alpha(1.0f);
        goButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (nameAvail) {
                    botName = inpName.getText().toString();
                    botMap.put("owner", getAuth("email"));
                    botref.child(botName).updateChildren(botMap);  //TODO : update for non new users too
                    emailMap.put("online", true);
                    emailMap.put("mybot",botName);
                    emailMap.put("id", getAuth("id"));
                    usersref.child(getAuth("email")).updateChildren(emailMap);
                    newUser = false;
                    foo = new startMain();
                }
            }
        });
        goButton.setEnabled(false);
        inpName.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                goButton.setEnabled(false);
                // you can call or do what you want with your EditText here
                if ((s.toString().length() >= 5) && (s.toString().length() <= 10)) {
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
                    errorTV.setText("Please enter min 5 and max 10 letters and avoid '#','@','!','%','$','&','*'");
                }
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        });
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
        if(!newUser) {
            emailMap.put("online", true);
            usersref.child(getAuth("email")).updateChildren(emailMap);
        }
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
            botMap.put("online",false);
            if(botName!=null)
            botref.child(botName).updateChildren(botMap);
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
