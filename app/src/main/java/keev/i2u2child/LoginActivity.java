package keev.i2u2child;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.firebase.client.AuthData;
import com.firebase.client.Firebase;
import com.firebase.ui.auth.core.FirebaseLoginBaseActivity;
import com.firebase.ui.auth.core.FirebaseLoginError;
import com.firebase.ui.auth.core.SocialProvider;

import org.json.JSONException;
import org.json.JSONObject;

public class LoginActivity extends FirebaseLoginBaseActivity {

    String AUTH_DATA="AUTH_DATA";

    public void console(String s){
        final TextView tV =(TextView) findViewById(R.id.tV);
        tV.setText(s);
    }
    @Override
    public Firebase getFirebaseRef() {
        Firebase ref = new Firebase("https://i2u2robot.firebaseio.com/");
        return ref;
    }

    @Override
    public void onFirebaseLoggedIn(AuthData authData) {
        // TODO: Handle successful login
        console("Logged in : " + authData);
        console(authData.getProviderData().toString());
        JSONObject jObject = new JSONObject(authData.getProviderData());
//        try {
//            console(jObject.toString());
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
        Intent botSelectIntent = new Intent(LoginActivity.this, botSelectActivity.class); //
        botSelectIntent.putExtra(AUTH_DATA, jObject.toString());
        startActivity(botSelectIntent);

    }

    @Override
    public void onFirebaseLoggedOut() {
        // TODO: Handle logout
    }

    @Override
    public void onFirebaseLoginProviderError(FirebaseLoginError firebaseError) {
        // TODO: Handle an error from the authentication provider
        console("User Error :  "+firebaseError);
    }

    @Override
    public void onFirebaseLoginUserError(FirebaseLoginError firebaseError) {
        // TODO: Handle an error from the user
        console("Error : "+firebaseError);
    }

    @Override
    protected void onStart() {
        super.onStart();
        // All providers are optional! Remove any you don't want.
        setEnabledAuthProvider(SocialProvider.google);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Firebase.setAndroidContext(this);
        setContentView(R.layout.activity_login);
        Button mLoginButton=(Button) findViewById(R.id.loginButton);
        mLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showFirebaseLoginPrompt();
            }
        });
    }

}
