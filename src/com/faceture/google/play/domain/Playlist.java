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

package com.faceture.google.play.domain;

import java.util.Collection;

/**
 * A Google Play Playlist
 */
public class Playlist {

    private String title;
	private String playlistId;
	private double requestTime;
	private String continuationToken;
	private boolean differentialUpdate;
	private Collection<Song> playlist;
	private boolean continuation;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPlaylistId() {
        return playlistId;
    }

    public void setPlaylistId(String playlistId) {
        this.playlistId = playlistId;
    }

    public double getRequestTime() {
        return requestTime;
    }

    public void setRequestTime(double requestTime) {
        this.requestTime = requestTime;
    }

    public String getContinuationToken() {
        return continuationToken;
    }

    public void setContinuationToken(String continuationToken) {
        this.continuationToken = continuationToken;
    }

    public boolean isDifferentialUpdate() {
        return differentialUpdate;
    }

    public void setDifferentialUpdate(boolean differentialUpdate) {
        this.differentialUpdate = differentialUpdate;
    }

    public Collection<Song> getPlaylist() {
        return playlist;
    }

    public void setPlaylist(Collection<Song> playlist) {
        this.playlist = playlist;
    }

    public boolean isContinuation() {
        return continuation;
    }

    public void setContinuation(boolean continuation) {
        this.continuation = continuation;
    }
}
