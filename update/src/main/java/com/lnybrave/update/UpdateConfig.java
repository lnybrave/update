package com.lnybrave.update;

import java.io.Serializable;

/**
 * Created by lny on 2018/5/12.
 */
class UpdateConfig implements Serializable {
    String mTitle;
    String mFileName;
    int mSmallIcon;
    boolean mShowNotify = true;
    boolean mAutoInstall = true;
    boolean mOnlyWifi = false;
}
