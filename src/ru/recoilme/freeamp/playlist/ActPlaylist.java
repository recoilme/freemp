package ru.recoilme.freeamp.playlist;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.*;
import android.widget.ExpandableListView;
import android.widget.Toast;
import com.androidquery.AQuery;
import com.androidquery.util.AQUtility;
import com.flurry.android.FlurryAgent;
import ru.recoilme.freeamp.ClsTrack;
import ru.recoilme.freeamp.FileUtils;
import ru.recoilme.freeamp.R;

import java.io.File;
import java.util.ArrayList;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: recoilme
 * Date: 25/11/13
 * Time: 13:46
 * To change this template use File | Settings | File Templates.
 */
public class ActPlaylist extends ActionBarActivity {

    private AQuery aq;
    private Menu optionsMenu;
    private boolean refreshing = true;
    private AdpPlaylist adapter;
    private ArrayList<ClsArrTrack> items;
    private Activity activity;
    private ExpandableListView listView;
    private MakePlaylistAbstract playlist;
    public static final int TYPE_MS = 0;
    public static final int TYPE_FS = 1;
    public static final int TYPE_GM = 2;

    public int type;
    private String scanDir;
    private DlgChooseDirectory.Result dialogResult;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_DITHER, WindowManager.LayoutParams.FLAG_DITHER);

        setContentView(R.layout.view_playlist);

        activity = this;
        aq = new AQuery(activity);
        FlurryAgent.onStartSession(activity, getString(R.string.flurry));

        ActionBar actionBar = getSupportActionBar();
        actionBar.setBackgroundDrawable(getResources().getDrawable(R.drawable.ab_bgr));
        actionBar.setDisplayHomeAsUpEnabled(true);

        listView = aq.id(R.id.expandableListView).getExpandableListView();
        listView.setGroupIndicator(null);

        Bundle extras = getIntent().getExtras();
        if (extras==null) {
            return;
        }
        else {
            type = extras.getInt("type");
        }


        aq.id(R.id.textViewSave).clicked(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                save();
            }
        });

        dialogResult = new DlgChooseDirectory.Result() {
                    @Override
                    public void onChooseDirectory(String dir) {
                        scanDir = dir;
                        PreferenceManager.getDefaultSharedPreferences(activity).edit().putString("scanDir", dir).commit();
                        update(true);
                    }
                };

    }


    public void update(boolean refresh) {
        if (items!=null) {
            adapter.notifyDataSetInvalidated();
            items.clear();
            adapter.invalidate();
        }
        AQUtility.debug("Update progress");
        refreshing = true;
        setRefreshActionButtonState();

        TaskGetPlaylist taskGetPlaylist = new TaskGetPlaylist();
        taskGetPlaylist.setContext(getApplicationContext());
        taskGetPlaylist.setType(type);
        taskGetPlaylist.setRefresh(refresh);
        taskGetPlaylist.execute(new ArrayList<String>());
    }

    public class TaskGetPlaylist extends AsyncTask {
        private Context context;
        private int type;
        private boolean refresh;

        @Override
        protected Object doInBackground(Object... params) {
            switch (this.type){
                case TYPE_MS:

                    return (new MakePlaylistMS(context,refresh)).getArrTracks();
                case TYPE_FS:
                    return new MakePlaylistFS(context,refresh).getArrTracks();
                case TYPE_GM:
                    return new MakePlaylistGM(context,refresh).getArrTracks();
                default:
                    return new MakePlaylistMS(context,refresh).getArrTracks();
            }
        }

        public void setContext(Context context) {
            this.context = context;
        }

        protected void onPostExecute(Object result) {
            if (result!=null) {
                items = (ArrayList<ClsArrTrack>) result;
                adapter = new AdpPlaylist(activity, items);
                listView.setAdapter(adapter);
            }
            refreshing = false;
            setRefreshActionButtonState();
        }

        public int getType() {
            return type;
        }

        public void setType(int type) {
            this.type = type;
        }

        public void setRefresh(boolean refresh) {
            this.refresh = refresh;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.optionsMenu = menu;
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.playlist, menu);
        if (type==TYPE_FS) {
            MenuItem item = this.optionsMenu.add (R.string.setup_scandir);
            item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    DlgChooseDirectory dlgChooseDirectory = new DlgChooseDirectory(activity,dialogResult,
                            scanDir);
                    return true;
                }
            });


        }

        scanDir = PreferenceManager.getDefaultSharedPreferences(activity).getString("scanDir","");
        if (scanDir.equals("")) {
            Map<String, File> externalLocations = FileUtils.getAllStorageLocations();
            AQUtility.debug(externalLocations.get(FileUtils.SD_CARD),externalLocations.get(FileUtils.EXTERNAL_SD_CARD));

            String defaultScanDir = Environment.getExternalStorageDirectory().toString();

            if (externalLocations.get(FileUtils.EXTERNAL_SD_CARD)!=null && !externalLocations.get(FileUtils.EXTERNAL_SD_CARD).toString().equals("")) {
                defaultScanDir = externalLocations.get(FileUtils.EXTERNAL_SD_CARD).toString();
            }

            DlgChooseDirectory dlgChooseDirectory = new DlgChooseDirectory(activity,dialogResult,
                    defaultScanDir);
        }
        else {
            update(false);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;

            case R.id.menu_refresh:

                update(true);
                return true;
            case R.id.menu_save:

                save();
                return true;
            case R.id.menu_select_all:

                select_all();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void setRefreshActionButtonState() {

        if (optionsMenu != null) {
            final MenuItem refreshItem = optionsMenu
                    .findItem(R.id.menu_refresh);
            if (refreshItem != null) {
                if (refreshing) {
                    MenuItemCompat.setActionView(refreshItem,R.layout.actionbar_indeterminate_progress);
                } else {
                    MenuItemCompat.setActionView(refreshItem,null);
                }
            }
        }
    }

    public void save() {
        ArrayList<ClsTrack> tracks = null;
        if (adapter!=null) {
            tracks = adapter.getSelected();
        }
        String fileName = "tracks";
        if (tracks==null || tracks.size()==0) {
            Toast.makeText(activity,getString(R.string.select_pls),Toast.LENGTH_LONG).show();
            return;
        }
        if (FileUtils.writeObject("tracks", activity, tracks)) {
            setResult(RESULT_OK, null);
            finish();
        }
    }

    public void select_all() {
        if (adapter==null) {
            return;
        }
        ArrayList<ClsTrack> tracks = adapter.getSelected();
        if (tracks.size()>0) {
            setSelection(false);
        }
        else {
            setSelection(true);
        }
    }

    public void setSelection(boolean isSelected) {
        adapter.notifyDataSetInvalidated();
        for (int j=0;j<adapter.data.size();j++) {
            ClsArrTrack o = adapter.data.get(j);
            ArrayList<ClsTrack> tracks = o.getPlaylists();
            for (int i=0;i<tracks.size();i++) {
                ClsTrack t = tracks.get(i);
                t.setSelected(isSelected);
                tracks.set(i,t);
            }
            o.setPlaylists(tracks);
            adapter.data.set(j,o);
        }
        adapter.invalidate();
    }

    public void updateColor(){
        ArrayList<ClsTrack> tmp = adapter.getSelected();
        if (tmp==null || tmp.size()==0) {
            aq.id(R.id.textViewSave).textColor(Color.GRAY);
        }
        else {
            aq.id(R.id.textViewSave).textColor(Color.parseColor("#FDC332"));
        }
    }

    @Override
    public void onDestroy() {

        FlurryAgent.onEndSession(activity);
        super.onDestroy();

    }

    @Override
    public void onResume() {
        super.onResume();

    }
}