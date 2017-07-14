package com.lnybrave.update;

import android.content.Context;

/**
 * Created by lny on 2017/6/8.
 */
public class Helper {

    private Cache cache = null;

    public void setDownloadId(Context context, String url, long id) {
        ensureCache(context);
        cache.setDownloadId(url, id);
    }

    public long getDownloadId(Context context, String url) {
        ensureCache(context);
        return cache.getDownloadId(url);
    }

    private void ensureCache(Context context) {
        if (cache == null) {
            cache = new PrefsCache(context);
        }
    }
}
