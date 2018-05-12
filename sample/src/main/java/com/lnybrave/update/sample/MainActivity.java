package com.lnybrave.update.sample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.lnybrave.update.Update;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void updateApp(View v) {
        String url = "http://answer.sskh.net/download/apk/kangyisheng.apk";
        Update.with(this)
                .url(url)
                .title("大禹电气")
                .smallIcon(R.drawable.updater_default_icon)
                .notification()
                .autoInstall()
                .wifi()
                .callback(new Update.Callback() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onFailure(int error) {
                        Log.d("update", error + "");
                    }
                })
                .update();
    }
}
