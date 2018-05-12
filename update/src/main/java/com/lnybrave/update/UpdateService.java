package com.lnybrave.update;

import android.app.DownloadManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.IBinder;
import android.util.Log;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import java.io.File;

import static android.os.Environment.DIRECTORY_DOWNLOADS;

public class UpdateService extends Service {

    private static final String TAG = UpdateService.class.getSimpleName();

    public static final String PARAM_UPDATE_URL = "update_url";
    public static final String PARAM_UPDATE_CONFIG = "update_config";

    private long downloadId;
    private DownloadManager manager;
    private CompleteReceiver receiver;
    private Cache cache = null;

    private void ensureCache(Context context) {
        if (cache == null) {
            cache = new PrefsCache(context);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        receiver = new CompleteReceiver();
        registerReceiver(receiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));

        manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        if (manager == null) {
            stopSelf();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String url = intent.getStringExtra(PARAM_UPDATE_URL);
        UpdateConfig config = (UpdateConfig) intent.getSerializableExtra(PARAM_UPDATE_CONFIG);

        long id = getDownloadIdByUrl(url);
        if (id > 0 && isDownloadCompleted(id)) {
            installApp(id);
        } else {
            id = download(url, config);

            ensureCache(this);
            cache.setDownloadId(url, id);
            downloadId = id;
        }

        return Service.START_NOT_STICKY;
    }

    private long getDownloadIdByUrl(String url) {
        ensureCache(this);
        long id = cache.getDownloadId(url);
        return id > 0 && isRedownload(id) ? 0 : id;
    }

    private boolean isRedownload(long id) {
        boolean ret = true;

        DownloadManager.Query query = new DownloadManager.Query();
        query.setFilterById(id);
        Cursor cursor = manager.query(query);
        if (cursor != null && cursor.moveToFirst()) {
            int columnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS);
            int status = cursor.getInt(columnIndex);
            int columnReason = cursor.getColumnIndex(DownloadManager.COLUMN_REASON);
            int reason = cursor.getInt(columnReason);
            Log.d(TAG, "id=" + id + ", status=" + status + ", reason=" + reason);
            switch (status) {
                case DownloadManager.STATUS_FAILED:
                    ret = true;
                    break;
                case DownloadManager.STATUS_PAUSED:
                    ret = false;
                    break;
                case DownloadManager.STATUS_PENDING:
                case DownloadManager.STATUS_RUNNING:
                    ret = false;
                    break;
                case DownloadManager.STATUS_SUCCESSFUL:
                    ret = false;
                    break;
            }
        }

        if (ret) {
            manager.remove(id);
        }

        return ret;
    }

    private boolean isDownloadCompleted(long id) {
        if (id > 0) {
            DownloadManager.Query query = new DownloadManager.Query();
            query.setFilterById(id);
            Cursor cursor = manager.query(query);
            if (cursor != null && cursor.moveToFirst()) {
                int columnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS);
                int status = cursor.getInt(columnIndex);
                return status == DownloadManager.STATUS_SUCCESSFUL;
            }
        }
        return false;
    }

    private long download(String url, UpdateConfig config) {
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));

        if (config.mOnlyWifi) {
            request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI);
        } else {
            request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE | DownloadManager.Request.NETWORK_WIFI);
        }

        request.setAllowedOverRoaming(false);
        request.allowScanningByMediaScanner();

        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        String mimeString = mimeTypeMap.getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl(url));
        request.setMimeType(mimeString);

        request.setNotificationVisibility(config.mShowNotify ? DownloadManager.Request.VISIBILITY_VISIBLE : DownloadManager.Request.VISIBILITY_HIDDEN);
        request.setVisibleInDownloadsUi(true);
        request.setDestinationInExternalPublicDir(DIRECTORY_DOWNLOADS, config.mFileName);
        request.setTitle(config.mTitle);
        return manager.enqueue(request);
    }

    @Override
    public void onDestroy() {
        if (receiver != null) {
            unregisterReceiver(receiver);
        }
        super.onDestroy();
    }

    class CompleteReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(intent.getAction())) {
                long downId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
                if (downloadId == downId) {
                    installApp(downId);
                }
            }
        }
    }

    private void installApp(long downId) {
        Context context = getApplicationContext();
        File apkFile = UpdateUtils.queryDownloadedApk(context, downId);
        if (apkFile != null) {
            UpdateUtils.installApp(context, apkFile);
        } else {
            Toast.makeText(context, "安装失败", Toast.LENGTH_SHORT).show();
        }
        this.stopSelf();
    }
}