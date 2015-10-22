package org.freemp.droid.player;

import org.freemp.droid.ClsTrack;

public interface InterfacePlayer {
    public void onPluginsLoaded(String plugins);

    public void onFileLoaded(ClsTrack track, double duration, String artist, String title, int position, int albumId);

    public void onProgressChanged(double progress);

    public void onUpdatePlayPause();
}
