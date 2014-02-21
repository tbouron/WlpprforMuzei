package com.zenstyle.muzei.wlppr;

import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import com.google.android.apps.muzei.api.Artwork;
import com.google.android.apps.muzei.api.RemoteMuzeiArtSource;
import com.google.android.apps.muzei.api.UserCommand;
import com.squareup.okhttp.OkHttpClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import retrofit.ErrorHandler;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.OkClient;

import static com.zenstyle.muzei.wlppr.LogUtil.LOGD;
import static com.zenstyle.muzei.wlppr.LogUtil.LOGW;

/**
 * Created by tbouron on 20/02/14.
 */
public class WlpprArtSource extends RemoteMuzeiArtSource {

    public static final String TAG = LogUtil.makeLogTag(WlpprArtSource.class);

    private static final int ROTATE_TIME_MILLIS = 24 * 60 * 60 * 1000;
    private static final int COMMAND_ID_SHARE = MAX_CUSTOM_COMMAND_ID - 1;
    private static final int COMMAND_ID_DOWNLOAD = MAX_CUSTOM_COMMAND_ID - 2;

    public WlpprArtSource() {
        super(TAG);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        List<UserCommand> commands = new ArrayList<UserCommand>();
        commands.add(new UserCommand(BUILTIN_COMMAND_ID_NEXT_ARTWORK));
        commands.add(new UserCommand(COMMAND_ID_SHARE, getString(R.string.action_share_artwork)));
        commands.add(new UserCommand(COMMAND_ID_DOWNLOAD, getString(R.string.action_download_artwork)));
        setUserCommands(commands);
    }

    @Override
    protected void onCustomCommand(int id) {
        super.onCustomCommand(id);

        Artwork currentArtwork = getCurrentArtwork();

        LOGD(TAG, "Custom action called: " + id);

        switch (id) {
            case COMMAND_ID_SHARE:
                if (currentArtwork == null) {
                    LOGW(TAG, "No current artwork, can't share.");
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(WlpprArtSource.this,
                                    R.string.action_no_artwork_to_share,
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
                    return;
                }
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_TEXT, "Check out my Android wallpaper of the day"
                        + " #WlpprForMuzei\n\n"
                        + currentArtwork.getViewIntent().getDataString());
                shareIntent = Intent.createChooser(shareIntent, getString(R.string.action_share_artwork));
                shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(shareIntent);
                break;
            case COMMAND_ID_DOWNLOAD:
                if (currentArtwork == null) {
                    LOGW(TAG, "No current artwork, can't download.");
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(WlpprArtSource.this,
                                    R.string.action_no_artwork_to_download,
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
                    return;
                }
                Intent downloadIntent = new Intent(DownloadReceiver.ACTION_DOWNLOAD);
                downloadIntent.putExtra(DownloadReceiver.TITLE, currentArtwork.getTitle());
                downloadIntent.putExtra(DownloadReceiver.URL, currentArtwork.getViewIntent().getDataString());
                sendBroadcast(downloadIntent);
                break;
        }
    }

    @Override
    protected void onTryUpdate(int reason) throws RetryException {
        String currentToken = (getCurrentArtwork() != null) ? getCurrentArtwork().getToken() : null;

        LOGD(TAG, "Start trying to update Wlppr for Muzei");

        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint("http://wlppr.com")
                .setClient(new OkClient(new OkHttpClient()))
                .setErrorHandler(new ErrorHandler() {
                    @Override
                    public Throwable handleError(RetrofitError retrofitError) {
                        int statusCode = retrofitError.getResponse().getStatus();
                        if (retrofitError.isNetworkError() || (500 <= statusCode && statusCode < 600)) {
                            return new RetryException();
                        }
                        scheduleUpdate(System.currentTimeMillis() + ROTATE_TIME_MILLIS);
                        return retrofitError;
                    }
                })
                .setLogLevel(BuildConfig.DEBUG ? RestAdapter.LogLevel.FULL : RestAdapter.LogLevel.BASIC)
                .build();

        WlpprService service = restAdapter.create(WlpprService.class);
        WlpprService.WlpprResponse response = service.getWallPaper(100);

        if (response == null || response.wallpapers == null) {
            throw new RetryException();
        }

        if (response.wallpapers.size() == 0) {
            LOGW(TAG, "No wallpaper returned from API.");
            scheduleUpdate(System.currentTimeMillis() + ROTATE_TIME_MILLIS);
            return;
        }

        Random random = new Random();
        WlpprService.Wallpaper wallpaper;
        String token;
        int id;
        while (true) {
            wallpaper = response.wallpapers.get(random.nextInt(response.wallpapers.size()));
            id = wallpaper.id;
            token = Integer.toString(id);
            if (!token.equals(currentToken)) {
                break;
            }
        }

        String url = String.format("http://wlppr.com/wallpapers/%1$d/%1$d.jpg", id);

        LOGD(TAG, "Wallpaper URL: " + url);

        publishArtwork(new Artwork.Builder()
                .title(getString(R.string.wallpaper_name, id))
                .byline(getString(R.string.byline))
                .imageUri(Uri.parse(url))
                .token(token)
                .viewIntent(new Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                .build());

        scheduleUpdate(System.currentTimeMillis() + ROTATE_TIME_MILLIS);
    }
}
