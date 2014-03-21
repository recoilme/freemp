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

/**
 * Response from an attempt to login
 */
public class LoginResponse {

    private LoginResult loginResult;
    private PlaySession playSession;

    public LoginResponse(LoginResult loginResult, PlaySession playSession) {
        if (null == loginResult) {
            throw new IllegalArgumentException("loginResult is null");
        }

        if (LoginResult.SUCCESS == loginResult) {
            if (null == playSession) {
                throw new IllegalArgumentException("When loginResult is SUCCESS, must provide a PlaySession");
            }
        }
        else {
            if (playSession != null) {
                throw new IllegalArgumentException("playSession must be null when loginResult is not SUCCESS");
            }
        }

        this.loginResult = loginResult;
        this.playSession = playSession;
    }

    public LoginResult getLoginResult() {
        return loginResult;
    }

    public PlaySession getPlaySession() {
        return playSession;
    }
}
