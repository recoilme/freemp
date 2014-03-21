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
 * Results of a search
 */
public class SearchResults {

    private Collection<Song> artists;
    private Collection<Song> albums;
    private Collection<Song> songs;

    public Collection<Song> getArtists() {
        return artists;
    }

    public void setArtists(Collection<Song> artists) {
        this.artists = artists;
    }

    public Collection<Song> getAlbums() {
        return albums;
    }

    public void setAlbums(Collection<Song> albums) {
        this.albums = albums;
    }

    public Collection<Song> getSongs() {
        return songs;
    }

    public void setSongs(Collection<Song> songs) {
        this.songs = songs;
    }
}
