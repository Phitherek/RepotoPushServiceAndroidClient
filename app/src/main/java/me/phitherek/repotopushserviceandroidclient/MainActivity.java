package me.phitherek.repotopushserviceandroidclient;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button mLoginButton = (Button) findViewById(R.id.main_login_button);
        Button mRegisterButton = (Button) findViewById(R.id.main_register_button);

        mLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(intent);
            }
        });

        mRegisterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), RegisterActivity.class);
                startActivity(intent);
            }
        });

        Context ctx = getApplicationContext();
        SharedPreferences sharedPrefs = ctx.getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        String defaultValue = "";
        String currentToken = sharedPrefs.getString(getString(R.string.shared_preferences_current_token_key), defaultValue);
        if(!currentToken.equals("")) {
            Intent intent = new Intent(getApplicationContext(), LoggedInActivity.class);
            startActivity(intent);
            return;
        }
        if(RepotoPushServiceClientService.getRunning()) {
            stopService(new Intent(getApplicationContext(), RepotoPushServiceClientService.class));
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        Intent mainActivity = new Intent(Intent.ACTION_MAIN);
        mainActivity.addCategory(Intent.CATEGORY_HOME);
        mainActivity.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(mainActivity);
    }
}
