package keev.i2u2child;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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

import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class botSelectActivity extends AppCompatActivity {
    private JSONObject auth_data;
    public String CALL_DATA = "CALL_DATA";
    private Firebase usersref,botref;
    public String TAG = "keev.i2u2child.botSelectActivity";
    boolean newUser = true;
    boolean nameAvail = false;
    private String botName = null;
    Map<String, Object> emailMap = new HashMap<String, Object>();
    Map<String, Object> botMap = new HashMap<String, Object>();
    startMain foo;
    TextView tv;
    RecyclerView rv;
    EditText inpName;
    Button goButton;
    ViewFlipper vf,vfList;
    boolean botFound = false;
    boolean emailFound = false;
    String addEmail;
    boolean NOBOT = false;

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Firebase.setAndroidContext(this);
        setContentView(R.layout.bot_select_flipper);
        usersref = new Firebase("https://i2u2robot.firebaseio.com/users/");
        botref = new Firebase("https://i2u2robot.firebaseio.com/bots/");
        rv = (RecyclerView)findViewById(R.id.rv);
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            
            try {
                auth_data = new JSONObject(extras.getString("AUTH_DATA"));
                Log.d(TAG,auth_data.toString());
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
                    Log.d(TAG,"Firebase error : "+ e);
            }

        });
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState){
            super.onPostCreate(savedInstanceState);

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
                    botref.child(botName).updateChildren(botMap);
                    emailMap.put("online", true);
                    emailMap.put("mybot",botName);
                    emailMap.put("id", getAuth("id"));
                    emailMap.put("profileURL",getAuth("profileURL"));
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
                case "name":
                    return auth_data.getString("displayName"); //TODO : should make real name ?
                case "profileURL":
                    return auth_data.getString("profileImageURL").replace("\\/", "/");
            }
        } catch (JSONException e) {
            Log.d(TAG, "JSON parse problem");
        }
        return "null";
    }
    public void addAccess(){
        vf.showNext();
        final EditText newMail = (EditText) findViewById(R.id.newMail);
        final TextView accessTV = (TextView) findViewById(R.id.accessTV);
        final Button addBotButton =(Button) findViewById(R.id.addBotbutton);
        botMap = new HashMap<String, Object>();
        emailMap = new HashMap<String, Object>();
        accessTV.setText("Enter email-ID of your bud xD.");
        accessTV.animate().alpha(1f);

        newMail.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                accessTV.animate().alpha(0f);
                addBotButton.setEnabled(false);
                final String name = s.toString();
                Log.d(TAG,s.toString());
                usersref.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        if (!newMail.getText().toString().matches("")) {
                            if (snapshot.hasChild(name.replace(".", ""))) {
                                accessTV.setTextColor(Color.rgb(0, 255, 0));
                                accessTV.setText("Found email");
                                addEmail = name.replace(".", "");
                                emailFound = true;
                                accessTV.animate().alpha(1f);
                                addBotButton.setEnabled(true);
                            } else {
                                addBotButton.setEnabled(false);
                                accessTV.setTextColor(Color.rgb(255, 0, 0));
                                accessTV.setText("linked email not found :(");
                                emailFound = false;
                                accessTV.animate().alpha(1f);
                            }
                        }
                        if (newMail.getText().toString().matches("")){
                            addBotButton.setEnabled(false);
                            accessTV.setTextColor(Color.rgb(255, 0, 0));
                            accessTV.setText("linked email not found :(");
                            emailFound = false;
                            accessTV.animate().alpha(1f);
                        }
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
        addBotButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                usersref.child(addEmail).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        emailMap.put(addEmail, true);
                        emailMap.put(getAuth("email"), true);
                        usersref.child(addEmail).child("friends").updateChildren(emailMap);
                        usersref.child(getAuth("email")).child("friends").updateChildren(emailMap);
                        vf.showPrevious();
                    }
                    @Override
                    public void onCancelled(FirebaseError firebaseError) {
                    }
                });
            }
        });

    }



    public class friend{
        String name,email,status,botname;
        String photoLink;

        friend(String name, String email,String status,String botname, String photoLink) {
            this.name = name;
            this.email = email;
            this.status = status;
            this.botname = botname;
            this.photoLink = photoLink;
        }
    }

    private List<friend> myFriendList;

    public class RVAdapter extends RecyclerView.Adapter<RVAdapter.FriendViewHolder>{
        List<friend> myFriendList;
        public class FriendViewHolder extends RecyclerView.ViewHolder {
            CardView cv;
            TextView friendName;
            TextView friendMail;
            TextView friendStatus;
            TextView friendBot;
            ImageView personPhoto;
            ViewFlipper friendFlipper;
            Button callFriendButton;
            FriendViewHolder(View itemView) {
                super(itemView);
                cv = (CardView)itemView.findViewById(R.id.friendcv);
                friendName = (TextView)itemView.findViewById(R.id.my_name);
                friendMail = (TextView)itemView.findViewById(R.id.friend_email);
                friendStatus = (TextView)itemView.findViewById(R.id.friend_status);
                friendBot = (TextView)itemView.findViewById(R.id.friendbot);
                personPhoto = (ImageView)itemView.findViewById(R.id.my_photo);
                friendFlipper =(ViewFlipper)itemView.findViewById(R.id.friendFlipper);
                callFriendButton =(Button)itemView.findViewById(R.id.callfriendButton);
            }
        }
        RVAdapter(List<friend> myFriendList){
            this.myFriendList = myFriendList;
        }
        @Override
        public RVAdapter.FriendViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.friend_card, viewGroup, false);
            FriendViewHolder fvh = new FriendViewHolder(v);
            return fvh;
        }

        @Override
        public void onBindViewHolder(final RVAdapter.FriendViewHolder friendViewHolder, int i) {
            friendViewHolder.friendName.setText(myFriendList.get(i).name);
            friendViewHolder.friendMail.setText(myFriendList.get(i).email);
            friendViewHolder.friendStatus.setText(myFriendList.get(i).status);
            friendViewHolder.friendBot.setText(myFriendList.get(i).botname);
            new ImageDownloaderTask(friendViewHolder.personPhoto).execute(myFriendList.get(i).photoLink);
            friendViewHolder.cv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    friendViewHolder.friendFlipper.showNext();
                }
            });
            friendViewHolder.callFriendButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent xwalkActivityIntent = new Intent(botSelectActivity.this, xwalkActivity.class);
                    String roomName = botName;
                    xwalkActivityIntent.putExtra(CALL_DATA, roomName);
                    startActivity(xwalkActivityIntent);
                }
            });
        }

        @Override
        public int getItemCount() {
            return myFriendList.size();
        }

        @Override
        public void onAttachedToRecyclerView(RecyclerView recyclerView) {
            super.onAttachedToRecyclerView(recyclerView);
        }

    }
        class ImageDownloaderTask extends AsyncTask<String, Void, Bitmap> {
        private final WeakReference<ImageView> imageViewReference;

        public ImageDownloaderTask(ImageView imageView) {
            imageViewReference = new WeakReference<ImageView>(imageView);
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            return downloadBitmap(params[0]);
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (isCancelled()) {
                bitmap = null;
            }

            if (imageViewReference != null) {
                ImageView imageView = imageViewReference.get();
                if (imageView != null) {
                    if (bitmap != null) {
                        imageView.setImageBitmap(bitmap);
                    }
//                     else {
//                        Drawable placeholder = imageView.getContext().getResources().getDrawable(R.drawable.placeholder);
//                        imageView.setImageDrawable(placeholder);
//                    }
                }
            }
        }
    }

    private Bitmap downloadBitmap(String url) {
        HttpURLConnection urlConnection = null;
        try {
            URL uri = new URL(url);
            urlConnection = (HttpURLConnection) uri.openConnection();
            int statusCode = urlConnection.getResponseCode();
            if (statusCode != HttpURLConnection.HTTP_OK) {
                return null;
            }

            InputStream inputStream = urlConnection.getInputStream();
            if (inputStream != null) {
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                return bitmap;
            }
        } catch (Exception e) {
            urlConnection.disconnect();
            Log.w("ImageDownloader", "Error downloading image from " + url);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
        return null;
    }
    private void addDetails(final String email){
        Log.d("add details TAG",email);
        usersref.child(email).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String status;
                String botname;
                String name = dataSnapshot.child("name").getValue().toString();
                String photoLink = dataSnapshot.child("profileURL").getValue().toString().replace("\\/", "/");
                status = (boolean)dataSnapshot.child(("online")).getValue()?"Online":"Offline";
                if (dataSnapshot.child("mybot").getValue() == null){
                    botname = "Your friend has not bot!";
                } else {
                    botname = dataSnapshot.child("mybot").getValue().toString();
                }
                myFriendList.add(new friend(name, email, status, botname, photoLink));
                RVAdapter adapter = new RVAdapter(myFriendList);
                rv.setAdapter(adapter);
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });


    }
    private void getFriends(){
        usersref.child(getAuth("email")).child("friends").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot d : dataSnapshot.getChildren()) {
                    String tempKey = d.getKey().toString();
                    addDetails(tempKey);
                    // about to drop mic....*corrected* dropped mic
                    Log.d("snap ----", tempKey);
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });

    }

    private class startMain{
        private startMain(){
            rv.setHasFixedSize(false); // true only if the size is fixed -- better performance
            vf.showNext();
            final TextView mybotTV = (TextView) findViewById(R.id.my_bot);
            TextView myNameTV = (TextView) findViewById(R.id.my_name);
            TextView myEmailTV = (TextView) findViewById(R.id.my_email);
            ImageView myPhotoTV = (ImageView) findViewById(R.id.my_photo);
            new ImageDownloaderTask(myPhotoTV).execute(getAuth("profileURL"));
            myEmailTV.setText(getAuth("email"));
            myNameTV.setText(getAuth("name"));
            tv.animate().alpha(0.0f);
            emailMap= new HashMap<>();
            emailMap.put("online", true);
            emailMap.put("id", getAuth("id"));
            emailMap.put("name", getAuth("name"));
            emailMap.put("profileURL", getAuth("profileURL"));
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
                    if(dataSnapshot.child("mybot").getValue()!=null) {
                        botName = dataSnapshot.child("mybot").getValue().toString();
                        mybotTV.setText(botName);
                    }
                    else {
                        NOBOT = true;
                        botName = "null";
                        mybotTV.setText("You dont have a bot");
                    }
                    mybotTV.animate().alpha(1f);
                }

                @Override
                public void onCancelled(FirebaseError firebaseError) {

                }
            });
            final CardView myBotCard = (CardView) findViewById(R.id.mybotcard);
            final ViewFlipper myBotFlipper =(ViewFlipper) findViewById(R.id.myBotFlipper);
            Button callmybotButton = (Button) findViewById(R.id.callmybotButton); //TODO:call throuch this button , add eventlistenr
            myBotCard.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    myBotFlipper.showNext();
                }
            });
            LinearLayoutManager llm = new LinearLayoutManager(botSelectActivity.this); // TODO: try changing context
            rv.setLayoutManager(llm);
            myFriendList = new ArrayList<>();
            getFriends();   //iterating and getting list update here bow chika wow wow

            //call the adapter to set info on the myFriendList ...kevin I think youre losing it =O
            RVAdapter adapter = new RVAdapter(myFriendList);
            rv.setAdapter(adapter);

        }
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
        emailMap.put("online", false);
        usersref.child(getAuth("email")).updateChildren(emailMap);
    }
}
