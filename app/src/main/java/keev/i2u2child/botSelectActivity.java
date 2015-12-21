package keev.i2u2child;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.firebase.client.Firebase;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class botSelectActivity extends AppCompatActivity {
    private JSONObject auth_data;
    private Firebase usersref;
    public String TAG = "keev.i2u2child.botSelectActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Firebase.setAndroidContext(this);
        usersref = new Firebase("https://i2u2robot.firebaseio.com/users/");

        Bundle extras = getIntent().getExtras();
        if(extras !=null) {
            try {
                auth_data = new JSONObject(extras.getString("AUTH_DATA"));
            } catch (JSONException e) {
                e.printStackTrace();
                Log.d(TAG,"Malformed JSON object found");
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
        Map<String, Object> idMap = new HashMap<String, Object>();
        idMap.put("email",getAuth("email"));
        usersref.child(getAuth("id")).updateChildren(idMap);


    }

    public String getAuth(String auth)  {
        try {
            switch (auth) {
                case "id" : return auth_data.getString("id");
                case "email": return auth_data.getString("email");

            }
        }catch(JSONException e){
            Log.d(TAG,"JSON parse problem");
        }
        return "null";
    }
}
