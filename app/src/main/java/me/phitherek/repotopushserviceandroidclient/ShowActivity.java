package me.phitherek.repotopushserviceandroidclient;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

public class ShowActivity extends AppCompatActivity {

    public static final String ACTION_SHOW_CONTENT = "me.phitherek.repotopushserviceandroidclient.action.SHOW_CONTENT";
    public static final String EXTRA_PUSHMEMO_CONTENT = "me.phitherek.repotopushserviceandroidclient.extra.PUSHMEMO_CONTENT";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show);
        Intent intent = getIntent();
        if(intent.getAction().equals(ACTION_SHOW_CONTENT)) {
            String pushMemoContent = intent.getStringExtra(EXTRA_PUSHMEMO_CONTENT);
            TextView memoContentTV = (TextView) findViewById(R.id.show_pushmemo_content_TV);
            memoContentTV.setText(pushMemoContent);
        } else {
            Intent mainActivity = new Intent(Intent.ACTION_MAIN);
            mainActivity.addCategory(Intent.CATEGORY_HOME);
            mainActivity.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(mainActivity);
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
