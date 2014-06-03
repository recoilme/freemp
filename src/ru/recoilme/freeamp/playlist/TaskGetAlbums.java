package ru.recoilme.freeamp.playlist;

import android.app.Activity;
import android.os.AsyncTask;
import com.androidquery.util.AQUtility;
import ru.recoilme.freeamp.ClsTrack;
import ru.recoilme.freeamp.FileUtils;
import ru.recoilme.freeamp.GetHttpData;
import ru.recoilme.freeamp.MediaUtils;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
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
        ArrayList<ClsTrack> albumsTracks = new ArrayList<ClsTrack>();
        if (!refresh) {
            albumsTracks = (ArrayList<ClsTrack>) FileUtils.readObject("albumsTracks",activity);
            if (albumsTracks!=null && albumsTracks.size()>0) {
                return albumsTracks;
            }
            albumsTracks = new ArrayList<ClsTrack>();
        }

        ArrayList<ClsArrTrack> arrTracks = new MakePlaylistFS(activity, refresh).getArrTracks();

        //выкидываем все дубликаты альбомов
        //сортируем по альбому
        for (ClsArrTrack t:arrTracks) {
            ArrayList<ClsTrack> allTracks = t.getPlaylists();

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
                final String currentAlbum = ""+track.getAlbum().toLowerCase();
                if (currentAlbum.equals(album) || currentAlbum.equals("") || track.getAlbumId()<=0) {
                    iterator.remove();
                }
                else {
                    album = currentAlbum;
                    if (track.getArtist().toLowerCase().contains("coil")) {
                        AQUtility.debug("coil",currentAlbum);
                    }
                    if (MediaUtils.getArtworkQuick(activity, track.getAlbumId(), 300, 300)!=null) {
                        boolean skip = false;
                        for (ClsTrack clsTrack:albumsTracks) {
                            if (clsTrack.getAlbum().toLowerCase().equals(track.getAlbum().toLowerCase())) {
                                skip = true;
                                continue;
                            }
                        }
                        if (!skip) {
                            albumsTracks.add(track);
                        }
                        continue;
                    }/*
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
                    */
                }
            }
        }
        FileUtils.writeObject("albumsTracks",activity,albumsTracks);
        return albumsTracks;
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