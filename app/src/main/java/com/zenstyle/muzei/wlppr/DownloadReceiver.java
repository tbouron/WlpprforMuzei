package com.zenstyle.muzei.wlppr;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

/**
 * Created by tbouron on 20/02/14.
 */
public class DownloadReceiver extends BroadcastReceiver {

    public static final String ACTION_DOWNLOAD = "com.zenstyle.muzei.wlppr.DOWNLOAD";
    public static final String URL = "url";
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
