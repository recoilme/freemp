package ru.recoilme.freeamp.playlist;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import com.androidquery.util.AQUtility;
import org.json.JSONArray;
import org.json.JSONObject;
import ru.recoilme.freeamp.*;

import java.io.*;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;

/**
 * Created by recoil on 01.06.14.
 */
public class TaskGetAlbums extends AsyncTask {

    private final WeakReference<Activity> mActivity;
    private final WeakReference<OnTaskGetAlbums> mOnTaskGetAlbums;
    private int type;
    private boolean refresh;
    private int newArtworksCounter;

    public static interface OnTaskGetAlbums {
        public void OnTaskResult(Object result);
    }

    public TaskGetAlbums(Activity activity, int type, boolean refresh, OnTaskGetAlbums onTaskGetAlbums)  {
        mActivity = new WeakReference<Activity>(activity);
        mOnTaskGetAlbums = new WeakReference<OnTaskGetAlbums>(onTaskGetAlbums);
        this.type = type;
        this.refresh = refresh;
    }

    @Override
    protected Object doInBackground(Object... params) {
        Activity activity = mActivity.get();
        if (null == activity) {
            return null;
        }
        ArrayList<ClsTrack> allTracks = null;

        FillMediaStoreTracks fillMediaStoreTracks = new FillMediaStoreTracks(activity);
        allTracks =  fillMediaStoreTracks.getTracks();

        if (allTracks == null) {
            return new ArrayList<ClsTrack>();
        }
        //выкидываем все дубликаты альбомов
        //сортируем по альбому
        Collections.sort(allTracks, new Comparator<ClsTrack>() {
            @Override
            public int compare(ClsTrack lhs, ClsTrack rhs) {
                return lhs.getAlbum().toLowerCase().compareTo(rhs.getAlbum().toLowerCase());
            }
        });



        //Создаем итератор и выкидываем дубли
        Iterator<ClsTrack> iterator = allTracks.iterator();
        String album = "";

        GetHttpData getHttpData = null;
        int total = allTracks.size();
        int step = 0;
        newArtworksCounter = 0;
        while (iterator.hasNext()) {

            publishProgress((int) (100 * step / total));
            step++;

            ClsTrack track = iterator.next();
            final String currentAlbum = track.getAlbum().toLowerCase();
            if (currentAlbum.equals(album)) {
                iterator.remove();
            }
            else {
                album = currentAlbum;
                if (!refresh) {
                    continue;
                }
                if (MediaUtils.getArtworkQuick(activity, track.getAlbumId(), 300, 300)!=null) {
                    continue;
                }

                String url = String.format("http://ws.audioscrobbler.com/2.0/?method=album.getinfo&api_key=0cb75104931acd7f44f571ed12cff105&artist=%s&album=%s&format=json", Uri.encode(track.getArtist()),Uri.encode(currentAlbum));
                getHttpData = new GetHttpData();
                getHttpData.setUrl(url);
                getHttpData.request();
                String result = new String(getHttpData.getByteArray());
                AQUtility.debug("result", result);
                String albumArtImageLink = "";
                if (result!=null) {
                    try {
                        JSONObject jsonObject = new JSONObject(result);
                        jsonObject = jsonObject.getJSONObject("album");
                        JSONArray image = jsonObject.getJSONArray("image");
                        for (int i=0;i<image.length();i++) {
                            jsonObject = image.getJSONObject(i);
                            if (jsonObject.getString("size").equals("extralarge")) {
                                albumArtImageLink = Uri.decode(jsonObject.getString("#text"));

                                AQUtility.debug(track.getArtist()+":"+currentAlbum,albumArtImageLink);
                            }
                        }
                        if (!albumArtImageLink.equals("")) {
                            //download image
                            getHttpData = new GetHttpData();
                            getHttpData.setUrl(albumArtImageLink);
                            getHttpData.request();

                            ContentResolver res = activity.getContentResolver();
                            Bitmap bm = BitmapFactory.decodeByteArray(getHttpData.getByteArray(), 0, getHttpData.getByteArray().length);
                            if (bm != null) {
                                // Put the newly found artwork in the database.
                                // Note that this shouldn't be done for the "unknown" album,
                                // but if this method is called correctly, that won't happen.

                                // first write it somewhere
                                String file = Environment.getExternalStorageDirectory()
                                        + "/albumthumbs/" + String.valueOf(System.currentTimeMillis());
                                if (FileUtils.ensureFileExists(file)) {
                                    try {
                                        OutputStream outstream = new FileOutputStream(file);
                                        if (bm.getConfig() == null) {
                                            bm = bm.copy(Bitmap.Config.RGB_565, false);
                                            if (bm == null) {
                                                //return getDefaultArtwork(context);
                                            }
                                        }
                                        boolean success = bm.compress(Bitmap.CompressFormat.JPEG, 75, outstream);
                                        outstream.close();
                                        bm.recycle();
                                        if (success) {
                                            ContentValues values = new ContentValues();
                                            values.put("album_id", track.getAlbumId());
                                            values.put("_data", file);
                                            Uri newuri = res.insert(MediaUtils.sArtworkUri, values);
                                            if (newuri == null) {
                                                // Failed to insert in to the database. The most likely
                                                // cause of this is that the item already existed in the
                                                // database, and the most likely cause of that is that
                                                // the album was scanned before, but the user deleted the
                                                // album art from the sd card.
                                                // We can ignore that case here, since the media provider
                                                // will regenerate the album art for those entries when
                                                // it detects this.
                                                success = false;
                                            }
                                            else {
                                                newArtworksCounter++;
                                            }
                                        }
                                        if (!success) {
                                            File f = new File(file);
                                            f.delete();
                                            iterator.remove();
                                        }
                                    } catch (FileNotFoundException e) {
                                        AQUtility.debug( "error creating file", e);
                                    } catch (IOException e) {
                                        AQUtility.debug( "error creating file", e);
                                    }
                                }
                            }
                        }
                        else {
                            //art not found

                            iterator.remove();
                        }
                    }
                    catch (Exception e) {

                        iterator.remove();
                        e.printStackTrace();
                    }
                }
                else {
                    AQUtility.debug("info",getHttpData.getInfo());
                    AQUtility.debug("err",getHttpData.getError());
                }
            }
        }
        System.gc();
        return allTracks;
    }

    protected void onPostExecute(Object result) {
        OnTaskGetAlbums onTaskGetAlbums = mOnTaskGetAlbums.get();
        Activity activity = mActivity.get();
        if (null != onTaskGetAlbums) {
            onTaskGetAlbums.OnTaskResult(result);
        }
        if (null != activity) {
            ((ActPlaylist) activity).setRefreshing(false);
            ((ActPlaylist) activity).setRefreshActionButtonState();
        }
    }
}