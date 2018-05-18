package org.freemp.droid.playlist;

import android.content.Context;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;

import com.androidquery.util.AQUtility;
import com.un4seen.bass.BASS;
import com.un4seen.bass.TAGS;

import org.freemp.droid.ClsTrack;
import org.freemp.droid.FileUtils;
import org.freemp.droid.FillMediaStoreTracks;
import org.freemp.droid.StringUtils;
import org.mozilla.universalchardet.UniversalDetector;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;

/**
 * Created by recoilme on 05/12/13.
 */
public class MakePlaylistFS extends MakePlaylistAbstract {

    //base tags for scan
    private static final int[] formats = {BASS.BASS_TAG_ID3V2, BASS.BASS_TAG_OGG, BASS.BASS_TAG_APE, BASS.BASS_TAG_MP4, BASS.BASS_TAG_ID3};
    //encoding detector
    UniversalDetector detector;
    private ArrayList<ClsTrack> tempAllTracks, tempAllTracksMediaStore;
    private boolean refresh;
    private FillMediaStoreTracks fillMediaStoreTracks;

    public MakePlaylistFS(Context context, boolean refresh) {
        super(context, refresh);
    }

    @Override
    public void getAllTracks(Context context, boolean refresh) {
        this.refresh = refresh;
        t = System.currentTimeMillis();
        String scanDir = PreferenceManager.getDefaultSharedPreferences(context).getString("scanDir", Environment.getExternalStorageDirectory().getAbsolutePath().toString());
        File currentDir = new File(scanDir);

        tempAllTracks = (ArrayList<ClsTrack>) FileUtils.readObject("alltracksfs", context);
        tempAllTracksMediaStore = (ArrayList<ClsTrack>) FileUtils.readObject("alltracksms", context);

        if (refresh || tempAllTracksMediaStore == null || tempAllTracksMediaStore.size() == 0) {
            fillMediaStoreTracks = new FillMediaStoreTracks(context);
            tempAllTracksMediaStore = fillMediaStoreTracks.getTracks();
        }

        if (!refresh && tempAllTracks != null && tempAllTracks.size() > 0) {
            allTracks = new ArrayList<ClsTrack>(tempAllTracks);
        } else {
            if (BASS.BASS_Init(-1, 44100, 0)) {
                String nativePath = context.getApplicationInfo().nativeLibraryDir;
                String[] listPlugins = new File(nativePath).list();
                for (String s : listPlugins) {
                    int plug = BASS.BASS_PluginLoad(nativePath + "/" + s, 0);
                }
            }
            detector = new UniversalDetector(null);

            walk(context, currentDir);

            FileUtils.writeObject("alltracksfs", context, allTracks);

            AQUtility.debug("time", "(ms):" + (System.currentTimeMillis() - t)); //5000 //81000  //7000
        }
    }

    public void walk(Context context, File root) {

        File[] list = null;

        if (root.getAbsolutePath().toString().equals("/")) {
            list = new File[1];
            list[0] = Environment.getExternalStorageDirectory();
            File extSd = FileUtils.getExternalSdCardPath(context);
            boolean needAdd = true;
            if (extSd != null) {
                for (File file : list) {
                    if (file.getAbsolutePath().equals(extSd.getAbsolutePath())) {
                        needAdd = false;
                        break;
                    }
                }
            } else {
                needAdd = false;
            }
            if (extSd != null && needAdd) {
                File[] extSdlist = new File[1];
                extSdlist[0] = extSd;
                File[] newlist = new File[list.length + 1];

                newlist = FileUtils.concatenate(extSdlist, list);
                list = newlist;
            }
        } else {
            list = root.listFiles();
        }
        if (list == null) return;
        int chan = 0;
        for (File f : list) {

            if (f.isDirectory()) {
                walk(context, f);
            } else {
                String path = f.getAbsolutePath().toString();
                if (path.contains("Richter")) {
                    Log.w("Richter", path);
                }
                int lengthPath = path.length();
                if (lengthPath < 4) continue;//file without extension
                String endOfPath = path.substring(lengthPath - 4).toLowerCase();
                if (endOfPath.equals(".mp3")
                        || endOfPath.equals("flac") || endOfPath.equals(".ogg")
                        || endOfPath.equals(".oga") || endOfPath.equals(".aac")
                        || endOfPath.equals(".m4a") || endOfPath.equals(".m4b")
                        || endOfPath.equals(".m4p") || endOfPath.equals("opus")
                        || endOfPath.equals(".wma") || endOfPath.equals(".wav")
                        || endOfPath.equals(".mpc") || endOfPath.equals(".ape")

                        ) {
                    if (!this.refresh && tempAllTracks != null && tempAllTracks.size() > 0) {
                        ClsTrack track = null;
                        for (ClsTrack t : tempAllTracks) {
                            if (t.getPath().equals(path)) {
                                track = t;
                                break;
                            }
                        }
                        if (track != null) {
                            allTracks.add(track);
                            continue;
                        }
                    }

                    String folder = "";
                    String[] pathArray = path.split(
                            TextUtils.equals(System.getProperty("file.separator"), "") ? "/" : System.getProperty("file.separator")
                    );
                    if (pathArray != null && pathArray.length > 1) {
                        folder = pathArray[pathArray.length - 2];
                    }
                    long lastModified = f.lastModified();


                    BASS.BASS_StreamFree(chan);
                    chan = BASS.BASS_StreamCreateFile(path, 0, 0, 0);
                    //check base tags and get encoding
                    String tags = null;
                    if (android.os.Build.VERSION.SDK_INT >= 9) {
                        for (int format = 0; format < formats.length; format++) {
                            final ByteBuffer byteBuffer = (ByteBuffer) TAGS.TAGS_ReadExByte(chan, "%ARTI@%YEAR@%TRCK@%TITL@%ALBM@%COMP" + " ", formats[format]);

                            final int bufferSize = byteBuffer.capacity();
                            if (bufferSize < 10) {
                                //so if no tags it return something strange, like this "??" - skip it for optimization

                                continue;
                            }
                            //byteBuffer dont have array (direct access?), so copy it
                            final ByteBuffer frameBuf = ByteBuffer.allocate(bufferSize);
                            frameBuf.put(byteBuffer);

                            detector.handleData(frameBuf.array(), 0, bufferSize);
                            detector.dataEnd();
                            final String encoding = detector.getDetectedCharset();
                            boolean wrongencoding = false;
                            try {
                                tags = new String(frameBuf.array(), 0, bufferSize, Charset.forName(encoding));
                            } catch (Exception e) {
                                wrongencoding = true;
                            } finally {
                                detector.reset();
                            }
                            if (wrongencoding) {
                                continue;
                            }
                            if (!TextUtils.isEmpty(tags)) {
                                if (tags.split("@").length >= 4) {
                                    break;
                                }
                            }
                        }
                    }
                    if (TextUtils.isEmpty(tags)) {
                        //it may have tags from http forexample so handle it with default way (utf8 encoding)
                        tags = TAGS.TAGS_Read(chan, "%UTF8(%ARTI)@%YEAR@%TRCK@%UTF8(%TITL)@%UTF8(%ALBM)@%UTF8(%COMP)" + " ");
                    }

                    if (TextUtils.isEmpty(tags)) {
                        //check file without tags on exists in mediastore
                        if (tempAllTracksMediaStore != null && tempAllTracksMediaStore.size() > 0) {
                            for (ClsTrack t : tempAllTracksMediaStore) {
                                if (t.getPath().equals(path)) {
                                    if (t != null) {
                                        allTracks.add(t);
                                    }
                                    break;
                                }
                            }
                        }
                        continue;
                    }

                    String[] tagsArray = tags.split("@");
                    if (tagsArray == null || tagsArray.length <= 4) {
                        //это говно какое-то типа музыки из игры скорее всего
                        continue;
                    }

                    tagsArray = tags.split("@");
                    int duration = 0;
                    int albumId = 0;
                    if (tempAllTracksMediaStore != null && tempAllTracksMediaStore.size() > 0) {
                        ClsTrack track = null;
                        for (ClsTrack t : tempAllTracksMediaStore) {
                            if (t.getPath().equals(path)) {
                                duration = t.getDuration();
                                albumId = t.getAlbumId();
                                break;
                            }
                        }
                    }
                    if (duration == 0) {
                        duration = (int) (0.5d + BASS.BASS_ChannelBytes2Seconds(chan, BASS.BASS_ChannelGetLength(chan, BASS.BASS_POS_BYTE)));
                    }

                    if (pathArray.length > 0) {
                        add2list(tagsArray[0], tagsArray[1], tagsArray[2], tagsArray[3], tagsArray[4], tagsArray[5].trim(),
                                path, folder, lastModified, pathArray[pathArray.length - 1], duration, albumId);
                    }
                }
            }
        }
    }

    public void add2list(String artist, String yearS, String trackS, String title, String album, String composer,
                         String path, String folder, long lastModified, String filename, int duration, int albumId) {
        int year = 0;
        int track = 0;

        try {
            if (!yearS.equals("")) {
                if (yearS.length() > 3) {
                    yearS = yearS.substring(0, 4);
                }
                year = Integer.parseInt(yearS.replaceAll("[^\\d.]", ""));
            }

            if (!trackS.equals(""))
                track = Integer.parseInt(trackS.replaceAll("[^\\d.]", ""));
        } catch (Exception e) {
            AQUtility.debug(e.toString());
        }

        allTracks.add(new ClsTrack(
                artist.equals("") ? "unknown" : StringUtils.capitalizeFully(artist),
                title.equals("") ? filename : StringUtils.capitalizeFully(title),
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
