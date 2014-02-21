package com.zenstyle.muzei.wlppr;

import java.util.List;

import retrofit.http.GET;
import retrofit.http.Query;

/**
 * Created by tbouron on 20/02/14.
 */
public interface WlpprService {

    @GET("/ajax/pile")
    WlpprResponse getWallPaper(@Query("number") Integer number);

    static class WlpprResponse {
        List<Wallpaper> wallpapers;
    }

    static class Wallpaper {
        int id;
        String RGBColor;
        String borderColor;
    }
}
