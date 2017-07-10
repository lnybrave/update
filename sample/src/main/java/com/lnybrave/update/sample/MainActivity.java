package com.lnybrave.update.sample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.lnybrave.update.Updater;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void updateApp(View v) {
        String url = "http://dayu.whhuiyu.com:80/media/files/dayu-v101-201703011446-sign.apk";
        Updater.with(this)
                .url(url)
                .title("大禹电气")
                .smallIcon(R.drawable.updater_default_icon)
                .notification()
                .autoInstall()
                .wifi()
                .update(new Updater.Callback() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onFailure(int error) {

                    }
                });
    }
}
