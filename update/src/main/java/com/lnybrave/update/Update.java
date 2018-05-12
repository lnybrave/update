package com.lnybrave.update;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by lny on 2017/3/17.
 * 版本升级
 */
public class Update {

    public static final int SUCCESS = 0;
    public static final int ERROR_INVALID_URL = -1;
    public static final int ERROR_PERMISSION_DENIED = -2;
    public static final int ERROR_DOWNLOADING = -3;
    public static final int ERROR_NO_WIFI = -4;

    public int update(Context context, String url, UpdateConfig config) {
        if (!isRunning(context, "com.xliu11.update.UpdateService")) {
            Intent intent = new Intent();
            intent.putExtra(UpdateService.PARAM_UPDATE_URL, url);
            intent.putExtra(UpdateService.PARAM_UPDATE_CONFIG, config);
            intent.setClass(context, UpdateService.class);
            context.startService(intent);
            return SUCCESS;
        }

        return ERROR_DOWNLOADING;
    }

    private boolean isRunning(Context context, String className) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        ArrayList<ActivityManager.RunningServiceInfo> rs = (ArrayList<ActivityManager.RunningServiceInfo>) am.getRunningServices(30);
        for (int i = 0; i < rs.size(); i++) {
            if (rs.get(i).service.getClassName().equals(className)) {
                return true;
            }
        }
        return false;
    }

    public static Builder with(Context context) {
        if (context == null) {
            throw new NullPointerException("context must be not null");
        }
        return new Builder(context.getApplicationContext());
    }

    public static class Builder implements Serializable {

        private Context mContext;
        private String mUrl;
        private UpdateConfig mConfig;
        private Callback mCallback;

        Builder(Context context) {
            this.mContext = context;
            this.mConfig = new UpdateConfig();
        }

        public Builder url(String url) {
            mUrl = url;
            return this;
        }

        public Builder title(String title) {
            mConfig.mTitle = title;
            return this;
        }

        public Builder smallIcon(int smallIcon) {
            mConfig.mSmallIcon = smallIcon;
            return this;
        }

        public Builder notification() {
            mConfig.mShowNotify = true;
            return this;
        }

        public Builder autoInstall() {
            mConfig.mAutoInstall = true;
            return this;
        }

        public Builder wifi() {
            mConfig.mOnlyWifi = true;
            return this;
        }

        public Builder callback(Callback callback){
            mCallback = callback;
            return this;
        }

        public void update() {
            if (mCallback == null) {
                mCallback = new SampleCallback();
            }
            Callback callback = mCallback;

            if (!UpdateUtils.hasPermission(mContext)) {
                callback.onFailure(ERROR_PERMISSION_DENIED);
                return;
            }

            if (mConfig.mOnlyWifi && !UpdateUtils.isConnectedWifi(mContext)) {
                callback.onFailure(ERROR_NO_WIFI);
                return;
            }

            if (!UpdateUtils.isValidUrl(mUrl)) {
                callback.onFailure(ERROR_INVALID_URL);
                return;
            }

            if (TextUtils.isEmpty(mConfig.mFileName)) {
                mConfig.mFileName = UpdateUtils.getFileNameByUrl(mUrl);
            }

            Update update = new Update();
            int ret = update.update(mContext, mUrl, mConfig);
            if (ret != SUCCESS) {
                callback.onFailure(ret);
            } else {
                callback.onSuccess();
            }
        }
    }

    public interface Callback {

        void onSuccess();

        void onFailure(int error);
    }

    private static class SampleCallback implements Callback {
        @Override
        public void onSuccess() {

        }

        @Override
        public void onFailure(int error) {

        }
    }
}
