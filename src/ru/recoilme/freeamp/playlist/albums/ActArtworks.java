package ru.recoilme.freeamp.playlist.albums;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.view.*;
import android.widget.*;
import com.androidquery.AQuery;
import com.androidquery.util.AQUtility;
import org.json.JSONArray;
import org.json.JSONObject;
import ru.recoilme.freeamp.*;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;

/**
 * Created by recoil on 26.01.14.
 */
public class ActArtworks extends ActionBarActivity {

    private AQuery aq;
    private Menu optionsMenu;
    private boolean refreshing = true;
    private Activity activity;
    private ArrayList<ClsTrack> tracks;
    //UI
    private GridView gridView;
    private AdpArtworks adapter;
    private ProgressBar progressBar;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //устанавливаем кастомный бэкграунд акшенбара
        getSupportActionBar().setBackgroundDrawable(getResources().getDrawable(R.drawable.ab_bgr));
        //добавляем кнопку назад
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        activity = this;
        aq = new AQuery(activity);

        //UI
        final LinearLayout linearLayout = new LinearLayout(activity);
        linearLayout.setOrientation(LinearLayout.VERTICAL);

        //Progress
        progressBar = new ProgressBar(activity,null,android.R.attr.progressBarStyleHorizontal);
        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        progressBar.setLayoutParams(layoutParams);
        progressBar.setVisibility(View.GONE);
        gridView = new GridView(activity);

        linearLayout.addView(progressBar);
        linearLayout.addView(gridView);
        setContentView(linearLayout);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.optionsMenu = menu;
        //создаем меню в акшенбаре
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.artwork, menu);

        //только после того как меню создано - запускаем обновление
        update();
        return super.onCreateOptionsMenu(menu);
    }

    public void update() {

        //устанавливаем статус в "обновляется"
        refreshing = true;
        //раскручиваем колесеко
        setRefreshActionButtonState();

        TaskArtwork taskArtwork = new TaskArtwork();
        taskArtwork.execute();

    }

    public class TaskArtwork extends AsyncTask<Void,Integer,ArrayList<ClsTrack>> {

        private int newArtworksCounter;

        @Override
        protected ArrayList<ClsTrack> doInBackground(Void... params) {
            ArrayList<ClsTrack> allTracks = (ArrayList<ClsTrack>) FileUtils.readObject("alltracksms", activity);

            if (allTracks == null || allTracks.size()==0) {
                FillMediaStoreTracks fillMediaStoreTracks = new FillMediaStoreTracks(activity);
                allTracks =  fillMediaStoreTracks.getTracks();
            }

            if (allTracks == null) {
                allTracks = new ArrayList<ClsTrack>();
            }

            //выкидываем все дубликаты альбомов
            //сортируем по альбому
            Collections.sort(allTracks,new Comparator<ClsTrack>() {
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
                    if (MediaUtils.getArtworkQuick(activity,track.getAlbumId(),300,300)!=null) {
                        continue;
                    }

                    String url = String.format("http://ws.audioscrobbler.com/2.0/?method=album.getinfo&api_key=0cb75104931acd7f44f571ed12cff105&artist=%s&album=%s&format=json", Uri.encode(track.getArtist()),Uri.encode(currentAlbum));
                    getHttpData = new GetHttpData();
                    getHttpData.setUrl(url);
                    getHttpData.request();
                    String result = new String(getHttpData.getByteArray());
                    AQUtility.debug("result",result);
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
                                Bitmap bm = BitmapFactory.decodeByteArray(getHttpData.getByteArray(),0,getHttpData.getByteArray().length);
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
        @Override
        protected void onPostExecute(ArrayList<ClsTrack> result) {
            if (result!=null) {
                tracks = result;
                applyAdapter();
            }
            progressBar.setVisibility(View.GONE);
            progressBar.setProgress(0);
            refreshing = false;
            setRefreshActionButtonState();
            String text = getResources()
                    .getQuantityString(R.plurals.notification_newartworks, newArtworksCounter,
                            newArtworksCounter);
            Toast.makeText(activity,text,Toast.LENGTH_SHORT).show();
        }


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setProgress(0);
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            super.onProgressUpdate(progress);
            progressBar.setProgress(progress[0]);
        }
    }

    void applyAdapter() {
        if (tracks == null) return;
        adapter = new AdpArtworks(activity,tracks);
        int iDisplayWidth = getResources().getDisplayMetrics().widthPixels ;
        int numColumns = iDisplayWidth / 310;
        gridView.setColumnWidth( (iDisplayWidth / numColumns) );
        gridView.setNumColumns(numColumns);
        gridView.setStretchMode( GridView.NO_STRETCH ) ;
        gridView.setAdapter(adapter);
        gridView.invalidateViews();
        gridView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                adapter.setScrollState(scrollState);
                if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
                    adapter.notifyDataSetChanged();
                    gridView.invalidateViews();
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

            }
        });
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        applyAdapter();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                //закрываем активити на нажатие кнопки домой
                finish();
                return true;
            case R.id.menu_refresh:
                update();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void setRefreshActionButtonState() {

        //если статус обновляется - заменяем иконку обновить на крутящийся прогрессбар
        if (optionsMenu != null) {
            final MenuItem refreshItem = optionsMenu
                    .findItem(R.id.menu_refresh);
            if (refreshItem != null) {
                if (refreshing) {
                    MenuItemCompat.setActionView(refreshItem, R.layout.actionbar_indeterminate_progress);
                } else {
                    MenuItemCompat.setActionView(refreshItem,null);
                }
            }
        }
    }
}