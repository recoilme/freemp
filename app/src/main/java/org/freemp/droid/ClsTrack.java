package org.freemp.droid;

import java.io.Serializable;

/**
 * Created with IntelliJ IDEA.
 * User: recoilme
 * Date: 25/11/13
 * Time: 13:47
 * To change this template use File | Settings | File Templates.
 */
public class ClsTrack implements Serializable {

    private static final long serialVersionUID = 1L;
    private String artist;
    private String title;
    private String album;
    private String composer;
    private int year;
    private int track;
    private int duration;
    private String path;
    private String folder;
    private long lastModified;
    private String group;
    private boolean selected;
    private int albumId;

    public ClsTrack(String artist, String title, String album, String composer, int year, int track, int duration,
                    String path, String folder, long lastModified, int albumId) {
        this.artist = artist;
        this.title = title;
        this.album = album;
        this.composer = composer;
        this.year = year;
        this.track = track;
        this.duration = duration;
        this.path = path;
        this.folder = folder;
        this.lastModified = lastModified;
        this.group = "";
        this.albumId = albumId;
    }

    public static ClsTrack newInstance(ClsTrack o) {
        return new ClsTrack(o.getArtist(), o.getTitle(), o.getAlbum(), o.getComposer(), o.getYear(), o.getTrack(), o.getDuration(),
                o.getPath(), o.getFolder(), o.getLastModified(), o.getAlbumId());
    }

    @Override
    public String toString() {
        return "[" + getGroup() + "," + getFolder() + "," + getTrack() + "," + getArtist() + "," + getTitle() + "]";
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public String getFolder() {
        return folder;
    }

    public void setFolder(String folder) {
        this.folder = folder;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public String getComposer() {
        return composer;
    }

    public void setComposer(String composer) {
        this.composer = composer;
    }

    public int getTrack() {
        return track;
    }

    public void setTrack(int track) {
        this.track = track;
    }

    public long getLastModified() {
        return lastModified;
    }

    public void setLastModified(long lastModified) {
        this.lastModified = lastModified;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public int getAlbumId() {
        return albumId;
    }

    public void setAlbumId(int albumId) {
        this.albumId = albumId;
    }
}
