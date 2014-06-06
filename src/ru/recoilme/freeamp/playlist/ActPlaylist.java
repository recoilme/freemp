package ru.recoilme.freeamp.playlist;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.Toast;
import com.androidquery.AQuery;
import com.flurry.android.FlurryAgent;
import ru.recoilme.freeamp.ClsTrack;
import ru.recoilme.freeamp.Constants;
import ru.recoilme.freeamp.FileUtils;
import ru.recoilme.freeamp.R;
import ru.recoilme.freeamp.playlist.albums.FragmentAlbums;
import ru.recoilme.freeamp.playlist.folders.FragmentFolders;
import ru.recoilme.freeamp.view.SlidingTabLayout;

import java.io.File;
import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: recoilme
 * Date: 25/11/13
 * Time: 13:46
 * To change this template use File | Settings | File Templates.
 */
public class ActPlaylist extends ActionBarActivity {

    private Activity activity;
    private AQuery aq;
    private Menu optionsMenu;
    private boolean refreshing = true;
    private AdpPagerAdapter adpPagerAdapter;

    private ViewPager mViewPager;
    private SlidingTabLayout mSlidingTabLayout;

    public int type;
    private String scanDir;
    private DlgChooseDirectory.Result dialogResult;

    private FragmentFolders playlistFragment = new FragmentFolders();
    private FragmentAlbums  albumsFragment   = new FragmentAlbums();

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_DITHER, WindowManager.LayoutParams.FLAG_DITHER);

        setContentView(R.layout.playlist_tabs);

        activity = this;
        aq = new AQuery(activity);
        FlurryAgent.onStartSession(activity, getString(R.string.flurry));

        ActionBar actionBar = getSupportActionBar();
        actionBar.setBackgroundDrawable(getResources().getDrawable(R.drawable.ab_bgr));
        actionBar.setDisplayHomeAsUpEnabled(true);

        Bundle extras = getIntent().getExtras();
        if (extras==null) {
            return;
        }
        else {
            type = extras.getInt("type");
        }
        dialogResult = new DlgChooseDirectory.Result() {
                    @Override
                    public void onChooseDirectory(String dir) {
                        scanDir = dir;
                        PreferenceManager.getDefaultSharedPreferences(activity).edit().putString("scanDir", dir).commit();
                        update(true);
                    }
                };

        mViewPager = (ViewPager) aq.id(R.id.viewpager).getView();
        adpPagerAdapter = new AdpPagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(adpPagerAdapter);

        mSlidingTabLayout = (SlidingTabLayout) findViewById(R.id.sliding_tabs);
        mSlidingTabLayout.setViewPager(mViewPager);

        mSlidingTabLayout.setCustomTabColorizer(new SlidingTabLayout.TabColorizer() {

            @Override
            public int getIndicatorColor(int position) {
                return
                        getResources().getColor(R.color.yellow);
            }

            @Override
            public int getDividerColor(int position) {
                return Color.GRAY;
            }

        });

    }

    public void setRefreshing(boolean refreshing) {
        this.refreshing = refreshing;
    }

    public void update(boolean refresh) {
        refreshing = true;
        setRefreshActionButtonState();
        Fragment fragment = adpPagerAdapter.getItem(mViewPager.getCurrentItem());
        if (fragment!=null) {
            if (fragment instanceof FragmentFolders) {
                playlistFragment.update(activity, type, refresh);
            }
            if (fragment instanceof FragmentAlbums) {
                albumsFragment.update(activity, 1, refresh);
            }
        }
    }

    public AdpPlaylist getAdapter() {
        Fragment fragment = adpPagerAdapter.getItem(mViewPager.getCurrentItem());
        if (fragment!=null) {
            if (fragment instanceof FragmentFolders) {
                return playlistFragment.adapter;
            }
        }
        return null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.optionsMenu = menu;
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.playlist, menu);
        if (type==Constants.TYPE_FS) {
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
            final File primaryExternalStorage = Environment.getExternalStorageDirectory();
            String defaultScanDir = primaryExternalStorage.toString();
            File secondaryExternalStorage = primaryExternalStorage;
            final String state = Environment.getExternalStorageState();

            if ( Environment.MEDIA_MOUNTED.equals(state) || Environment.MEDIA_MOUNTED_READ_ONLY.equals(state) ) {  // we can read the External Storage...

                //Retrieve the External Storages root directory:
                final String externalStorageRootDir;
                if ( (externalStorageRootDir = primaryExternalStorage.getParent()) == null ) {
                }
                else {
                    final File externalStorageRoot = new File( externalStorageRootDir );
                    final File[] files = externalStorageRoot.listFiles();

                    for ( final File file : files ) {
                        if ( file.isDirectory() && file.canRead() && (file.listFiles().length > 0) ) {
                            // it is a real directory (not a USB drive)...
                            if (!file.getAbsolutePath().equals(primaryExternalStorage.getAbsolutePath())) {
                                defaultScanDir = file.getParent();
                            }
                        }
                    }
                }
            }
            //Map<String, File> externalLocations = FileUtils.getAllStorageLocations();

            //String defaultScanDir = secondaryExternalStorage.toString();//Environment.getExternalStorageDirectory().toString();

            //if (externalLocations.get(FileUtils.EXTERNAL_SD_CARD)!=null && !externalLocations.get(FileUtils.EXTERNAL_SD_CARD).toString().equals("")) {
            //    defaultScanDir = externalLocations.get(FileUtils.EXTERNAL_SD_CARD).toString();
            //}

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
        if (getAdapter()!=null) {
            tracks = getAdapter().getSelected();
        }
        String fileName = "tracks";
        if (tracks==null || tracks.size()==0) {
            Toast.makeText(activity,getString(R.string.select_pls),Toast.LENGTH_LONG).show();
            return;
        }
        close(tracks);
    }

    public void close(ArrayList<ClsTrack> tracks) {
        if (FileUtils.writeObject("tracks", activity, tracks)) {
            setResult(RESULT_OK, null);
            finish();
        }
    }

    public void select_all() {
        if (getAdapter()==null) {
            return;
        }
        ArrayList<ClsTrack> tracks = getAdapter().getSelected();
        if (tracks.size()>0) {
            setSelection(false);
        }
        else {
            setSelection(true);
        }
    }

    public void setSelection(boolean isSelected) {
        if (getAdapter()==null) {
            return;
        }
        getAdapter().notifyDataSetInvalidated();
        for (int j=0;j<getAdapter().data.size();j++) {
            ClsArrTrack o = getAdapter().data.get(j);
            ArrayList<ClsTrack> tracks = o.getPlaylists();
            for (int i=0;i<tracks.size();i++) {
                ClsTrack t = tracks.get(i);
                t.setSelected(isSelected);
                tracks.set(i,t);
            }
            o.setPlaylists(tracks);
            getAdapter().data.set(j,o);
        }
        getAdapter().invalidate();
    }

    public void updateColor(){
        if (getAdapter()==null) {
            return;
        }
        ArrayList<ClsTrack> tmp = getAdapter().getSelected();
        if (tmp==null || tmp.size()==0) {
            aq.id(R.id.textViewSave).textColor(Color.GRAY);
        }
        else {
            aq.id(R.id.textViewSave).textColor(getResources().getColor(R.color.yellow));
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

    class AdpPagerAdapter extends FragmentPagerAdapter {

        public AdpPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            return Constants.NUM_ITEMS;
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return playlistFragment ;
                case 1:
                    return albumsFragment;
            }
            return null;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return getString(R.string.tab_folders);
                case 1:
                    return getString(R.string.tab_albums);
            }
            return "-";
        }
    }
}