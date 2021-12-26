package com.tcgr.extratocht;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        PackageInfo packageInfo;
        try {
            packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            TextView textView = (TextView) findViewById(R.id.version);
            TextView textView1 = (TextView) findViewById(R.id.sugestoes);
            if (textView != null && textView1 != null) {
                textView.setText(String.format(getString(R.string.versao), packageInfo.versionName));
                textView1.setText(String.format(getString(R.string.sugestoes), getString(R.string.myMail)));
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }
}
