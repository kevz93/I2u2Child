package keev.i2u2child;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class RefreshScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        finish();
        setContentView(R.layout.activity_screen_refresh);
    }
}
