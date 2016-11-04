package net.businessmonk.tienda.tienda;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;

public class Settings extends AppCompatActivity {
    TinyDB tinyDB;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        tinyDB = new TinyDB(getApplicationContext());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            getWindow().setStatusBarColor(0xFF30003E);
        }
    }

    public void sendReview(View view) {
        Log.e("settings","gfgf");
        appReview.showFeedBackDialog(Settings.this);
    }

    public void logout(View view) {
            tinyDB.putBoolean("login",false);
        tinyDB.putString("token","0");
            startActivity(new Intent(Settings.this,LoginActivity.class));
            Settings.this.finish();

    }
}
