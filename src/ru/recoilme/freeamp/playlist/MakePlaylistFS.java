package ru.recoilme.freeamp.playlist;

import android.content.Context;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import com.androidquery.util.AQUtility;
import com.un4seen.bass.BASS;
import com.un4seen.bass.TAGS;
import ru.recoilme.freeamp.ClsTrack;
import ru.recoilme.freeamp.FileUtils;
import ru.recoilme.freeamp.FillMediaStoreTracks;
import ru.recoilme.freeamp.StringUtils;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by recoilme on 05/12/13.
 */
public class MakePlaylistFS extends MakePlaylistAbstract {

    private ArrayList<ClsTrack> tempAllTracks,tempAllTracksMediaStore;
    private boolean refresh;
    private FillMediaStoreTracks fillMediaStoreTracks;

    public MakePlaylistFS(Context context, boolean refresh) {
        super(context,refresh);
    }

    @Override
    public void getAllTracks(Context context, boolean refresh) {

        t = System.currentTimeMillis();
        String scanDir = PreferenceManager.getDefaultSharedPreferences(context).getString("scanDir",Environment.getExternalStorageDirectory().getAbsolutePath());
        File currentDir = new File(scanDir);

        tempAllTracks = (ArrayList<ClsTrack>) FileUtils.readObject("alltracksfs", context);
        tempAllTracksMediaStore = (ArrayList<ClsTrack>) FileUtils.readObject("alltracksms", context);

        if (tempAllTracksMediaStore == null || tempAllTracksMediaStore.size()==0) {
            fillMediaStoreTracks = new FillMediaStoreTracks(context);
            tempAllTracksMediaStore =  fillMediaStoreTracks.getTracks();
        }

        if (!refresh && tempAllTracks !=null && tempAllTracks.size()>0) {
            allTracks = new ArrayList<ClsTrack>(tempAllTracks);
        }
        else {
            if (BASS.BASS_Init(-1, 44100, 0)) {
                String nativePath = context.getApplicationInfo().nativeLibraryDir;
                String[] listPlugins=new File(nativePath).list();
                for (String s: listPlugins) {
                    int plug=BASS.BASS_PluginLoad(nativePath+"/"+s, 0);
                }
            }
            walk(currentDir);

            FileUtils.writeObject("alltracksfs", context, allTracks);

            AQUtility.debug("time","(ms):"+(System.currentTimeMillis()-t)); //5000 //81000  //7000
        }
    }

    public void walk(File root) {

        File[] list = root.listFiles();

        if (list==null) return;
        int chan = 0;
        for (File f : list) {
            if (f.isDirectory()) {

                walk(f);
            }
            else {
                String path = f.getAbsolutePath().toString();

                int lengthPath = path.length();
                if (lengthPath<4) continue;
                String endOfPath = path.substring(lengthPath-4).toLowerCase();
                if (endOfPath.equals(".mp3")
                        || endOfPath.equals("flac") || endOfPath.equals(".ogg")
                        || endOfPath.equals(".oga") || endOfPath.equals(".aac")
                        || endOfPath.equals(".m4a") || endOfPath.equals(".m4b")
                        || endOfPath.equals(".m4p") || endOfPath.equals("opus")
                        || endOfPath.equals(".wma") || endOfPath.equals(".wav")
                        || endOfPath.equals(".mpc") || endOfPath.equals(".ape")) {

                    //if (path.toLowerCase().contains("mp3") || path.toLowerCase().contains("ogg")) {
                        //continue;
                    //}
                    if (tempAllTracks !=null && tempAllTracks.size()>0) {
                        ClsTrack track = null;
                        for (ClsTrack t: tempAllTracks) {
                            if (t.getPath().equals(path)) {
                                track = t;
                                break;
                            }
                        }
                        if (track!=null) {
                            allTracks.add(track);
                            continue;
                        }
                    }

                    String folder = "";
                    String[] pathArray = path.split(
                            TextUtils.equals(System.getProperty("file.separator"), "")?"/":System.getProperty("file.separator")
                    );
                    if (pathArray!=null && pathArray.length>1) {
                        folder = pathArray[pathArray.length-2];
                    }
                    long lastModified = f.lastModified();


                    BASS.BASS_StreamFree(chan);
                    chan = BASS.BASS_StreamCreateFile(path, 0, 0, 0);

                    String tags =""+TAGS.TAGS_Read(chan, "%ARTI@%YEAR@%TRCK@%TITL@%ALBM@%COMP"+" ");

                    if (tags.equals("")) {
                        if (endOfPath.equals(".ape")) {
                            tags = "@@@@@ ";//dummy tags for ape
                        }
                        else {
                            continue;
                        }

                    }

                    String[] tagsArray = tags.split("@");
                    if (path.toLowerCase().contains(".ogg") && tagsArray[0].equals("") && tagsArray[3].equals("")) {
                        //это говно какое-то типа музыки из игры скорее всего
                        continue;
                    }
                    if (tags.contains("????")) {
                        //try convert to utf-8
                        String mask = "%ARTI@%YEAR@%TRCK@%TITL@%ALBM@%COMP";
                        String[] maskArray = mask.split("@");
                        mask = "";

                        for (int i=0;i<maskArray.length;i++) {
                            if (!mask.equals("")) {
                                mask+="@";
                            }
                            if (tagsArray[i].contains("????")) {
                                mask+="%UTF8("+ maskArray[i] + ")";
                            }
                            else {
                                mask+= maskArray[i];
                            }
                        }
                        tags =""+TAGS.TAGS_Read(chan, mask+" ");
                    }

                    tagsArray = tags.split("@");
                    int duration = 0;
                    int albumId = 0;
                    if (tempAllTracksMediaStore !=null && tempAllTracksMediaStore.size()>0) {
                        ClsTrack track = null;
                        for (ClsTrack t: tempAllTracksMediaStore) {
                            if (t.getPath().equals(path)) {
                                duration = t.getDuration();
                                albumId = t.getAlbumId();
                                break;
                            }
                        }
                    }
                    if (duration==0) {
                        duration = (int) (0.5d+BASS.BASS_ChannelBytes2Seconds(chan, BASS.BASS_ChannelGetLength(chan, BASS.BASS_POS_BYTE)));
                    }
                    add2list(tagsArray[0],tagsArray[1],tagsArray[2],tagsArray[3],tagsArray[4],tagsArray[5].trim(),
                            path,folder,lastModified,pathArray[pathArray.length-1],duration,albumId);

                }
            }
        }
    }

    public void add2list(String artist,String yearS,String trackS,String title,String album,String composer,
                         String path,String folder, long lastModified,String filename,int duration,int albumId){
        int year = 0;
        int track = 0;

        try {
            if (!yearS.equals("")) {
                if (yearS.length()>3) {
                    yearS = yearS.substring(0,4);
                }
                year  = Integer.parseInt(yearS.replaceAll("[^\\d.]", ""));
            }

            if (!trackS.equals(""))
                track = Integer.parseInt(trackS.replaceAll("[^\\d.]", ""));
        } catch (Exception e) {AQUtility.debug(e.toString());}

        allTracks.add(new ClsTrack(
                artist.equals("") ?"unknown": StringUtils.capitalizeFully(artist),
                title.equals("") ?filename:StringUtils.capitalizeFully(title),
                album,
                composer,
                year,
                track,
                duration,
                path,
                folder,
                lastModified,
                albumId));
    }
}
