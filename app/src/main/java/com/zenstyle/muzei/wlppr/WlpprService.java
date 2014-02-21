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

import java.util.List;

import retrofit.RetrofitError;
import retrofit.http.GET;
import retrofit.http.Query;

/**
 * A service interface representing available endpoints for wlppr.com. This interface is intended to
 * be used by a {@link retrofit.RestAdapter} instance.
 */
public interface WlpprService {

    /**
     * Returns the last N wallpapers available.
     *
     * @param number The number of wallpaper to return (Optional, can be null)
     * @return A {@link WlpprResponse}
     * @throws RetrofitError if something goes wrong during the request
     */
    @GET("/ajax/pile")
    WlpprResponse getWallPapers(@Query("number") Integer number) throws RetrofitError;

    /**
     * A object representing the returned response from wlppr.com
     */
    static class WlpprResponse {
        List<Wallpaper> wallpapers;
    }

    /**
     * A underlying object representing a returned wallpaper from wlppr.com
     */
    static class Wallpaper {
        int id;
        String RGBColor;
        String borderColor;
    }
}
