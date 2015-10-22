package org.freemp.droid.playlist;

import org.freemp.droid.ClsTrack;

import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: recoilme
 * Date: 26/11/13
 * Time: 19:40
 * To change this template use File | Settings | File Templates.
 */
public class ClsArrTrack {
    private String description;
    private String artists;
    private ArrayList<ClsTrack> playlists;

    String getDescription() {
        return description;
    }

    void setDescription(String description) {
        this.description = description;
    }

    ArrayList<ClsTrack> getPlaylists() {
        return playlists;
    }

    void setPlaylists(ArrayList<ClsTrack> playlists) {
        this.playlists = playlists;
    }

    public int checkSelection() {
        int i = 0;
        for (ClsTrack t : this.getPlaylists()) {
            if (t.isSelected()) {
                i++;
            }
        }
        return i == 0 ? -1 : (i == this.getPlaylists().size() ? 1 : 0);
    }

    public String getArtists() {
        return artists;
    }

    public void setArtists(String artists) {
        this.artists = artists;
    }
}
