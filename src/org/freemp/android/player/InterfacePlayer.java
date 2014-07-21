package org.freemp.android.player;

import org.freemp.android.ClsTrack;

public interface InterfacePlayer {
	public void onPluginsLoaded(String plugins);
	public void onFileLoaded(ClsTrack track, double duration, String artist, String title, int position, int albumId);
	public void onProgressChanged(double progress);
    public void onUpdatePlayPause();
}
