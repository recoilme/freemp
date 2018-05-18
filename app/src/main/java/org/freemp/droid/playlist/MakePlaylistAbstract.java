package org.freemp.droid.playlist;

import android.content.Context;

import org.freemp.droid.ClsTrack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Created with IntelliJ IDEA.
 * User: recoilme
 * Date: 25/11/13
 * Time: 13:51
 * To change this template use File | Settings | File Templates.
 */
public abstract class MakePlaylistAbstract {

    private static final String RECENTLY_ADDED = "RECENTLY_ADDED";
    private static final String OTHERS = "OTHERS";
    public ArrayList<ClsTrack> allTracks;
    long t = System.currentTimeMillis();
    private ArrayList<ClsTrack> tmpTracks = new ArrayList<ClsTrack>();
    private ArrayList<ClsArrTrack> arrTracks = new ArrayList<ClsArrTrack>();

    public MakePlaylistAbstract(Context context, boolean refresh) {

        allTracks = new ArrayList<ClsTrack>();
        getAllTracks(context, refresh);
        //save all tracks
        //раскомментируй это в тяжелый день

        //http://developer.android.com/reference/android/provider/MediaStore.Audio.AudioColumns.html

        //sort descending
        Collections.sort(allTracks, new Comparator<ClsTrack>() {
            @Override
            public int compare(ClsTrack o1, ClsTrack o2) {
                return o1.getLastModified() > o2.getLastModified() ? -1 : o1.getLastModified() == o2.getLastModified() ? 0 : 1;
            }
        });
        logTime();//1ms,2,2

        tmpTracks.clear();
        HashMap<String, Integer> foldersMap = new HashMap<String, Integer>();
        HashMap<String, String> artistsMap = new HashMap<String, String>();
        //get recently added
        long firstRecentlyAddedTrack = 0;
        Iterator<ClsTrack> iterator = allTracks.iterator();
        String artistInRecenlyAddedFolder = "";
        while (iterator.hasNext()) {
            ClsTrack playlist = iterator.next();
            if (firstRecentlyAddedTrack == 0) firstRecentlyAddedTrack = playlist.getLastModified();

            //если трек был добавлен недавно по сравнению с последним добавленным
            if (firstRecentlyAddedTrack - playlist.getLastModified() <= 1 * 60 * 1000) {
                //треки добавленные в течение n минут от 1 трека (на реальных данных интервал при копировании порядка 7 секунд)
                ClsTrack clsTrack = ClsTrack.newInstance(playlist);
                clsTrack.setGroup(RECENTLY_ADDED);
                tmpTracks.add(clsTrack);

                if (!artistInRecenlyAddedFolder.contains(playlist.getArtist())) {
                    if (!artistInRecenlyAddedFolder.equals("")) {
                        artistInRecenlyAddedFolder += ", ";
                    }
                    artistInRecenlyAddedFolder += playlist.getArtist();
                }
                //delete old
                iterator.remove();
            } else {
                String currFolder = playlist.getFolder();
                if (foldersMap.containsKey(currFolder)) {
                    foldersMap.put(currFolder, foldersMap.get(currFolder) + 1);
                    if (!(artistsMap.get(currFolder) + "").contains("" + playlist.getArtist())) {
                        artistsMap.put(currFolder, artistsMap.get(currFolder) + "," + playlist.getArtist());
                    }
                } else {
                    foldersMap.put(currFolder, 1);
                    artistsMap.put(currFolder, "" + playlist.getArtist());
                }
            }
            //Log.w(menu_playlist.getTitle()," :: "+(menu_playlist.getLastModified() - firstRecentlyAddedTrack));
        }
        //Log.w("allTracks:",""+allTracks.size());
        logTime();//2,2
        arrTracks.clear();
        addToTracks(RECENTLY_ADDED, artistInRecenlyAddedFolder);

        //bigFolders finder
        //sort by folder name and track number

        Collections.sort(allTracks, new Comparator<ClsTrack>() {
            @Override
            public int compare(ClsTrack lhs, ClsTrack rhs) {

                return (lhs.getFolder() + (lhs.getArtist())).compareTo(rhs.getFolder() + (rhs.getArtist()));
            }
        });

        iterator = allTracks.iterator();
        String prevFolder = null;
        while (iterator.hasNext()) {
            ClsTrack playlist = iterator.next();

            String currFolder = playlist.getFolder();
            if (prevFolder == null) {
                prevFolder = currFolder;

            } else if (!prevFolder.equals(currFolder)) {
                addToTracks(prevFolder, artistsMap.get(prevFolder));
            }
            playlist.setGroup(artistsMap.get(currFolder));
            /*
            if (foldersMap.get(currFolder)>20) {
                //add bigfolders
                ClsTrack clsTrack = ClsTrack.newInstance(menu_playlist);
                clsTrack.setGroup("BIG_FOLDERS:"+currFolder);
                tmpTracks.add(clsTrack);
                //delete old
                iterator.remove();
                prevFolder = currFolder;
            }
            else {
                menu_playlist.setGroup(artistsMap.get(currFolder));
            }
            */
        }
        logTime(); //40,39,70,44
        addToTracks(prevFolder, artistsMap.get(prevFolder));


        Collections.sort(allTracks, new Comparator<ClsTrack>() {
            @Override
            public int compare(ClsTrack lhs, ClsTrack rhs) {

                return (lhs.getGroup() + lhs.getFolder() + (lhs.getTrack() + 1000)).compareTo(rhs.getGroup() + rhs.getFolder() + (rhs.getTrack() + 1000));
            }
        });


        iterator = allTracks.iterator();
        prevFolder = null;
        while (iterator.hasNext()) {
            ClsTrack playlist = iterator.next();
            String currFolder = playlist.getFolder();
            if (prevFolder == null) {
                prevFolder = currFolder;
            } else if (!prevFolder.equals(currFolder)) {
                addToTracks(prevFolder, artistsMap.get(prevFolder));
            }
            if (foldersMap.get(currFolder) > 3) {
                ClsTrack clsTrack = ClsTrack.newInstance(playlist);
                clsTrack.setGroup(artistsMap.get(currFolder));
                tmpTracks.add(clsTrack);
                prevFolder = currFolder;
                //delete old
                iterator.remove();
            }
        }
        addToTracks(prevFolder, artistsMap.get(prevFolder));
        // Others
        String artistInOthersFolder = "";
        iterator = allTracks.iterator();

        while (iterator.hasNext()) {
            ClsTrack playlist = iterator.next();
            ClsTrack clsTrack = ClsTrack.newInstance(playlist);
            clsTrack.setGroup(OTHERS);
            tmpTracks.add(clsTrack);
            if (!artistInOthersFolder.contains(playlist.getArtist())) {
                if (!artistInOthersFolder.equals("")) {
                    artistInOthersFolder += ", ";
                }
                artistInOthersFolder += playlist.getArtist();
            }
        }
        addToTracks(OTHERS, artistInOthersFolder);

    }

    public abstract void getAllTracks(Context context, boolean refresh);

    void addToTracks(String desc, String artists) {
        if (tmpTracks.size() > 0) {
            ClsArrTrack clsArrTrack = new ClsArrTrack();
            clsArrTrack.setDescription(desc);
            clsArrTrack.setArtists(artists);
            clsArrTrack.setPlaylists(tmpTracks);
            arrTracks.add(clsArrTrack);
            tmpTracks = new ArrayList<ClsTrack>();
        }
    }

    void logTime() {
        //Log.w("time","(ms):"+(System.currentTimeMillis()-t)/1);
        t = System.currentTimeMillis();
    }

    public ArrayList<ClsArrTrack> getArrTracks() {
        return arrTracks;
    }


}
