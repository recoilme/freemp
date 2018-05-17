package org.freemp.droid.playlist;

import android.app.Activity;
import android.net.Uri;
import android.os.AsyncTask;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;

import org.freemp.droid.ClsTrack;
import org.freemp.droid.FileUtils;
import org.freemp.droid.MediaUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by recoil on 01.06.14.
 */
public class TaskGetArtists extends AsyncTask {

    private final WeakReference<Activity> mActivity;
    private final WeakReference<OnTaskGetArtists> mOnTaskGetArtists;
    private final WeakReference<OnProgressUpdateMy> mOnProgressUpdate;
    private boolean refresh;
    private int newArtworksCounter;
    private AQuery aq;

    public TaskGetArtists(Activity activity, int type, boolean refresh, OnTaskGetArtists onTaskGetArtists, OnProgressUpdateMy onProgressUpdateMy) {
        mActivity = new WeakReference<Activity>(activity);
        mOnTaskGetArtists = new WeakReference<OnTaskGetArtists>(onTaskGetArtists);
        mOnProgressUpdate = new WeakReference<OnProgressUpdateMy>(onProgressUpdateMy);
        this.refresh = refresh;
    }

    @Override
    protected Object doInBackground(Object... params) {
        Activity activity = mActivity.get();
        if (null == activity) {
            return null;
        }
        ArrayList<ClsTrack> artistsTracks = new ArrayList<ClsTrack>();
        if (!refresh) {
            artistsTracks = (ArrayList<ClsTrack>) FileUtils.readObject("artistsTracks", activity);
            if (artistsTracks != null && artistsTracks.size() > 0) {
                return artistsTracks;
            }
            artistsTracks = new ArrayList<ClsTrack>();
        }

        ArrayList<ClsArrTrack> arrTracks = new MakePlaylistMS(activity, refresh).getArrTracks();

        //выкидываем все дубликаты альбомов
        //сортируем по альбому
        aq = new AQuery(activity);
        for (ClsArrTrack t : arrTracks) {
            ArrayList<ClsTrack> allTracks = t.getPlaylists();

            //Создаем итератор и выкидываем дубли
            Iterator<ClsTrack> iterator = allTracks.iterator();
            String artist = "";

            int total = allTracks.size();
            int step = 0;
            newArtworksCounter = 0;
            while (iterator.hasNext()) {

                onUpdate((int) (100 * step / total));
                step++;

                ClsTrack track = iterator.next();
                final String currentArtist = "" + track.getArtist().toLowerCase();
                if (currentArtist.equals(artist) || currentArtist.equals("")) {
                    continue;
                } else {
                    artist = currentArtist;
                    boolean stop = false;
                    for (ClsTrack clsTrack : artistsTracks) {
                        if (clsTrack.getArtist().toLowerCase().equals(artist)) {
                            stop = true;
                            break;
                        }
                    }
                    if (stop) {
                        continue;
                    }
                    if (MediaUtils.getArtistQuick(activity, track, 300, 300) != null) {
                        artistsTracks.add(track);
                        continue;
                    } else {
                        //noartwork (refresh or first start)
                        String url = String.format("http://ws.audioscrobbler.com/2.0/?method=artist.getinfo&api_key=0cb75104931acd7f44f571ed12cff105&artist=%s&format=json", Uri.encode(track.getArtist()));
                        AjaxCallback<JSONObject> cb = new AjaxCallback<JSONObject>();
                        cb.url(url).type(JSONObject.class).fileCache(true).expire(3600 * 60 * 1000);
                        aq.sync(cb);
                        JSONObject result = cb.getResult();

                        if (result != null) {
                            JSONObject jsonObject = null;
                            String albumArtImageLink = null;
                            try {
                                jsonObject = result.getJSONObject("artist");
                                JSONArray image = jsonObject.getJSONArray("image");
                                for (int i = 0; i < image.length(); i++) {
                                    jsonObject = image.getJSONObject(i);
                                    if (jsonObject.getString("size").equals("extralarge")) {
                                        albumArtImageLink = Uri.decode(jsonObject.getString("#text"));
                                    }
                                }
                                if (!albumArtImageLink.equals("")) {
                                    //download image

                                    String path = MediaUtils.getArtistPath(track);
                                    if (path == null) {
                                        continue;
                                    }
                                    File file = new File(path);
                                    AjaxCallback<File> cbFile = new AjaxCallback<File>();
                                    cbFile.url(albumArtImageLink).type(File.class).targetFile(file);
                                    aq.sync(cbFile);
                                    AjaxStatus status = cbFile.getStatus();
                                    if (status.getCode() == 200) {
                                        artistsTracks.add(track);
                                        newArtworksCounter++;
                                    } else {
                                        file.delete();
                                        continue;
                                    }
                                } else {
                                    //art not found
                                    continue;
                                }
                            } catch (Exception e) {
                                iterator.remove();
                                e.printStackTrace();
                            }
                        } else {
                            iterator.remove();
                        }
                    }

                }
            }
        }
        FileUtils.writeObject("artistsTracks", activity, artistsTracks);
        return artistsTracks;
    }

    protected void onUpdate(Integer... progress) {
        OnProgressUpdateMy onProgressUpdateMy = mOnProgressUpdate.get();
        if (onProgressUpdateMy != null) {
            onProgressUpdateMy.OnArtistsProgress(progress[0]);
        }
    }

    protected void onPostExecute(Object result) {
        OnTaskGetArtists onTaskGetArtists = mOnTaskGetArtists.get();
        Activity activity = mActivity.get();
        if (null != onTaskGetArtists) {
            onTaskGetArtists.OnTaskResult(result);
        }
        if (null != activity) {
            ((ActPlaylist) activity).setRefreshing(false);
            ((ActPlaylist) activity).setRefreshActionButtonState();
        }
    }

    public static interface OnTaskGetArtists {
        public void OnTaskResult(Object result);
    }

    public static interface OnProgressUpdateMy {
        public void OnArtistsProgress(int progress);
    }
}