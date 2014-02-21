/*
 * Copyright 2014 Thomas Bouron.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zenstyle.muzei.wlppr;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

/**
 * A broadcast receiver used to download wallpapers from wlppr.com into the default download folder
 * on the current device.
 */
public class DownloadReceiver extends BroadcastReceiver {

    /** Action filter to activate this receiver */
    public static final String ACTION_DOWNLOAD = "com.zenstyle.muzei.wlppr.DOWNLOAD";
    /** Extra string used to store the wallpaper URL */
    public static final String URL = "url";
    /** Extra string used to store the wallpaper title */
    public static final String TITLE ="title";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Bundle extra = intent.getExtras();

        if (ACTION_DOWNLOAD.equals(action) && extra != null) {
            String url = extra.getString(URL);
            String title =  extra.getString(TITLE);

            DownloadManager dlManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
            dlManager.enqueue(new DownloadManager.Request(Uri.parse(url))
                    .setTitle(title)
                    .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED));
        }
    }
}
