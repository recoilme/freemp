package ru.recoilme.freeamp.player;

import android.graphics.Bitmap;

public interface InterfacePlayer {
	public void onPluginsLoaded(String plugins);
	public void onFileLoaded(String file, double duration, String artist, String title, int position, int albumId);
	public void onProgressChanged(double progress);
    public void onUpdatePlayPause();
}
