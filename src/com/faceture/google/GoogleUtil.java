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

package com.faceture.google;

import com.faceture.google.play.Const;

/**
 * Utility class for Google-related activities
 */
public class GoogleUtil {

    public String createAuthHeaderValue(String authToken) {
        if (null == authToken || authToken.isEmpty()) {
            throw new IllegalArgumentException("authToken is null or empty");
        }

        return GoogleConst.AUTH_HEADER_START + authToken;
    }

    public String getAuthTokenFromLoginResponse(String loginResponseBody) {
        if (null == loginResponseBody || loginResponseBody.isEmpty()) {
            throw new IllegalArgumentException("loginResponseBody is null or empty");
        }

        int beginIndex = loginResponseBody.indexOf(Const.GOOLE_LOGIN_AUTH) + Const.GOOLE_LOGIN_AUTH.length();
        int endIndex = loginResponseBody.indexOf("\n", beginIndex);

        String authToken = loginResponseBody.substring(beginIndex, endIndex);

        return authToken;
    }
}
