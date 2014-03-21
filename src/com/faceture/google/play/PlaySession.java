/*
 * Copyright (c) 2012. Faceture Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.faceture.google.play;

import java.io.Serializable;

/**
 * Session information needed when interacting with Play web services
 */
public class PlaySession implements Serializable {

    private String xtCookie;
    private String sjsaidCookie;
    private String authToken;

    public PlaySession(String xtCookie, String sjsaidCookie, String authToken) {
        if (null == xtCookie || xtCookie.isEmpty()) {
            throw new IllegalArgumentException("xtCookie is null or empty");
        }
        if (null == sjsaidCookie || sjsaidCookie.isEmpty()) {
            throw new IllegalArgumentException("sjsaidCookie is null or empty");
        }
        if (null == authToken || authToken.isEmpty()) {
            throw new IllegalArgumentException("authToken is null or empty");
        }

        this.xtCookie = xtCookie;
        this.sjsaidCookie = sjsaidCookie;
        this.authToken = authToken;
    }

    public String getXtCookie() {
        return xtCookie;
    }

    public String getSjsaidCookie() {
        return sjsaidCookie;
    }

    public String getAuthToken() {
        return authToken;
    }

}
