package org.freemp.droid.playlist;

import android.Manifest;
import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.ViewConfiguration;
import android.view.WindowManager;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.flurry.android.FlurryAgent;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import org.freemp.droid.ClsTrack;
import org.freemp.droid.Constants;
import org.freemp.droid.FileUtils;
import org.freemp.droid.R;
import org.freemp.droid.playlist.albums.FragmentAlbums;
import org.freemp.droid.playlist.artists.FragmentArtists;
import org.freemp.droid.playlist.folders.FragmentFolders;
import org.freemp.droid.view.SlidingTabLayout;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: recoilme
 * Date: 25/11/13
 * Time: 13:46
 * To change this template use File | Settings | File Templates.
 */
public class ActPlaylist extends AppCompatActivity {

    public int type;
    private Activity activity;
    private AQuery aq;
    private Menu optionsMenu;
    private boolean refreshing = true;
    private AdpPagerAdapter adpPagerAdapter;
    private ViewPager mViewPager;
    private SlidingTabLayout mSlidingTabLayout;
    private String scanDir;
    private DlgChooseDirectory.Result dialogResult;

    private FragmentFolders playlistFragment = new FragmentFolders();
    private FragmentAlbums albumsFragment = new FragmentAlbums();
    private FragmentArtists artistsFragment = new FragmentArtists();

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_DITHER, WindowManager.LayoutParams.FLAG_DITHER);

        setContentView(R.layout.playlist_tabs);

        activity = this;
        aq = new AQuery(activity);
        FlurryAgent.onStartSession(activity, getString(R.string.flurry));

        ActionBar actionBar = getSupportActionBar();
        actionBar.setBackgroundDrawable(getResources().getDrawable(R.drawable.actionbar_bgr));
        actionBar.setDisplayHomeAsUpEnabled(true);

        try {
            ViewConfiguration config = ViewConfiguration.get(activity);
            Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");
            if (menuKeyField != null) {
                menuKeyField.setAccessible(true);
                menuKeyField.setBoolean(config, false);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        Bundle extras = getIntent().getExtras();
        if (extras == null) {
            return;
        } else {
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
        mViewPager.setOffscreenPageLimit(2);
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

        Dexter.withActivity(this)
                .withPermissions(Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        refreshing = true;
                        setRefreshActionButtonState();
                        Fragment fragment = adpPagerAdapter.getItem(mViewPager.getCurrentItem());
                        if (fragment != null) {
                            if (fragment instanceof FragmentFolders) {
                                playlistFragment.update(activity, Constants.TYPE_FS, refresh);
                            }
                            if (fragment instanceof FragmentAlbums) {
                                albumsFragment.update(activity, Constants.TYPE_MS, refresh);
                            }
                            if (fragment instanceof FragmentArtists) {
                                artistsFragment.update(activity, Constants.TYPE_MS, refresh);
                            }
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).check();


    }

    public AdpPlaylist getAdapter() {
        return playlistFragment.adapter;
        /*
        Fragment fragment = adpPagerAdapter.getItem(mViewPager.getCurrentItem());
        if (fragment!=null) {
            if (fragment instanceof FragmentFolders) {
                return playlistFragment.adapter;
            }
        }
        return null;
        */
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.optionsMenu = menu;
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_playlist, menu);
        if (type == Constants.TYPE_FS) {
            MenuItem item = this.optionsMenu.add(R.string.setup_scandir);
            item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    DlgChooseDirectory dlgChooseDirectory = new DlgChooseDirectory(activity, dialogResult,
                            scanDir);
                    return true;
                }
            });


        }

        scanDir = PreferenceManager.getDefaultSharedPreferences(activity).getString("scanDir", "");
        if (scanDir.equals("")) {
            DlgChooseDirectory dlgChooseDirectory = new DlgChooseDirectory(activity, dialogResult,
                    "/");
        } else {
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
                    MenuItemCompat.setActionView(refreshItem, R.layout.actionbar_progress);
                } else {
                    MenuItemCompat.setActionView(refreshItem, null);
                }
            }
        }
    }

    public void save() {
        ArrayList<ClsTrack> tracks = null;
        if (getAdapter() != null) {
            tracks = getAdapter().getSelected();
        }
        String fileName = "tracks";
        if (tracks == null || tracks.size() == 0) {
            Toast.makeText(activity, getString(R.string.select_pls), Toast.LENGTH_LONG).show();
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
        if (getAdapter() == null) {
            return;
        }
        ArrayList<ClsTrack> tracks = getAdapter().getSelected();
        if (tracks.size() > 0) {
            setSelection(false);
        } else {
            setSelection(true);
        }
    }

    public void setSelection(boolean isSelected) {
        if (getAdapter() == null) {
            return;
        }
        getAdapter().notifyDataSetInvalidated();
        for (int j = 0; j < getAdapter().data.size(); j++) {
            ClsArrTrack o = getAdapter().data.get(j);
            ArrayList<ClsTrack> tracks = o.getPlaylists();
            for (int i = 0; i < tracks.size(); i++) {
                ClsTrack t = tracks.get(i);
                t.setSelected(isSelected);
                tracks.set(i, t);
            }
            o.setPlaylists(tracks);
            getAdapter().data.set(j, o);
        }
        getAdapter().invalidate();
    }

    public void updateColor() {
        if (getAdapter() == null) {
            return;
        }
        ArrayList<ClsTrack> tmp = getAdapter().getSelected();
        if (tmp == null || tmp.size() == 0) {
            aq.id(R.id.textViewSave).textColor(Color.GRAY);
        } else {
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
                    return playlistFragment;
                case 1:
                    return albumsFragment;
                case 2:
                    return artistsFragment;
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
                case 2:
                    return getString(R.string.tab_artists);
            }
            return "-";
        }
    }
}