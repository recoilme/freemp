package ru.recoilme.freeamp.player;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.*;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.internal.widget.ListPopupWindow;
import android.text.format.Time;
import android.view.*;
import android.widget.*;
import com.androidquery.AQuery;
import com.androidquery.util.AQUtility;
import com.flurry.android.FlurryAgent;
import com.nhaarman.listviewanimations.widget.DynamicListView;
import ru.recoilme.freeamp.ClsTrack;
import ru.recoilme.freeamp.FileUtils;
import ru.recoilme.freeamp.MediaUtils;
import ru.recoilme.freeamp.R;
import ru.recoilme.freeamp.freemp.org.ActFreemporg;
import ru.recoilme.freeamp.playlist.ActPlaylist;

import java.text.SimpleDateFormat;
import java.util.*;


/**
 * Created with IntelliJ IDEA.
 * User: recoilme
 * Date: 28/11/13
 * Time: 15:10
 * To change this template use File | Settings | File Templates.
 */
public class ActPlayer extends ActionBarActivity implements InterfacePlayer {

    private static final int PLAYLIST_CODE = 100;
    public static final int LOGIN_RESULT = 101;
    private AQuery aq;
    private AdpPlayer adapter;
    private ArrayList<ClsTrack> items;
    private ArrayList<ClsTrack> sourceItemsList;
    private Activity activity;
    private DynamicListView listView;
    private SeekBar seekBar;
    private TextView txtDur, artist, title;
    public static int selected = -1;
    private ImageView albumImage;
    private ImageView artworkBgr;
    private static Random randomGenerator;

    // Bass Service
    private ServicePlayer mBoundService = null;

    // Bass Service Connection
    private ServiceConnection mConnection = new ServiceConnection() {

        public void onServiceConnected(ComponentName name, IBinder service) {
            mBoundService = ((ServicePlayer.BassServiceBinder)service).getService();
            onBassServiceConnected();

            setShuffleMode(PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getBoolean("Shuffle", false));
        }

        public void onServiceDisconnected(ComponentName name) {
            mBoundService = null;
        }

    };


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_DITHER, WindowManager.LayoutParams.FLAG_DITHER);

        AQUtility.setDebug(true);

        setContentView(R.layout.view_player);

        activity = this;
        Display display = getWindowManager().getDefaultDisplay();
        PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putInt("screenWidth",display.getWidth()).commit();
        PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putInt("screenHeight",display.getHeight()).commit();
        aq = new AQuery(activity);
        if(randomGenerator == null){
            Time now = new Time();
            randomGenerator = new Random(now.toMillis(true));
        }
        FlurryAgent.onStartSession(activity, getString(R.string.flurry));

        View customView = activity.getLayoutInflater().inflate(R.layout.player_ab,null);

        ActionBar actionBar = getSupportActionBar();

        actionBar.setCustomView(customView, new ActionBar.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.WRAP_CONTENT));//(int) (90*scale + 0.5f)));
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        listView = (DynamicListView)aq.id(R.id.listView).getListView();
        txtDur = (TextView) customView.findViewById(R.id.textViewDur);
        artist = (TextView) customView.findViewById(R.id.textViewArttist);
        title = (TextView) customView.findViewById(R.id.textViewTitle);
        seekBar = (SeekBar) customView.findViewById(R.id.seekBar);
        albumImage = (ImageView) customView.findViewById(R.id.album_img);
        artworkBgr = (ImageView) findViewById(R.id.artworkBgr);

        sourceItemsList = items = new ArrayList<ClsTrack>();
        adapter = new AdpPlayer(activity,items);

        listView.setAdapter(adapter);
        listView.setOnItemMovedListener(new DynamicListView.OnItemMovedListener() {
            @Override
            public void onItemMoved(final int newPosition) {
                if(synchronizeTrackList()){
                    adapter.notifyDataSetChanged();
                }
                Toast.makeText(getApplicationContext(), ((ClsTrack)adapter.getItem(newPosition)).getTitle() + " moved to position " + newPosition, Toast.LENGTH_SHORT).show();
            }
        });

        albumImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(selected >= 0 && selected < adapter.getCount() && seekBar.getMax() > 0){
                    showContextMenu(view,(ClsTrack)adapter.getItem(selected));
                }
            }
        });

       // listView.setAdapter(adapter);

        // Start Service
        startService(new Intent(this, ServicePlayer.class));

        // Bind Service
        bindService(new Intent(this, ServicePlayer.class), mConnection, Context.BIND_AUTO_CREATE);

    }

    // onBassServiceConnected: Put some activity stuff here
    public void onBassServiceConnected() {


        // Register Activity
        mBoundService.setActivity(this);


        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onStopTrackingTouch(SeekBar seekBar) {
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    final int prgr = progress;
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            mBoundService.seekTo(prgr);
                        }
                    }).start();

                }
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final int pos = position;
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if (mBoundService.isShuffle()) {
                            updatePlayPause();
                        }
                        mBoundService.startVolumeUpFlag = System.currentTimeMillis();
                        mBoundService.play(pos);
                        //aq.id(R.id.btnPlay).background(R.drawable.base_pause_button);
                        updatePlayPause();
                    }
                }).start();

            }
        });
       /* listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                showContextMenu(view,(ClsTrack)parent.getAdapter().getItem(position));
                return true;
            }
        });*/

        aq.id(R.id.btnFf).clicked(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (mBoundService!=null) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            mBoundService.playNext();
                            updatePlayPause();
                        }
                    }).start();
                }
            }
        });
        aq.id(R.id.btnRew).clicked(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mBoundService!=null) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            mBoundService.playPrev();
                            updatePlayPause();
                        }
                    }).start();
                }
            }
        });
        aq.id(R.id.btnSfl).clicked(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mBoundService!=null) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                           boolean mode = !mBoundService.isShuffle();
                           setShuffleMode(mode);

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(activity,getString(R.string.shuffle_is)+
                                            (mBoundService.isShuffle() ?" on":" off"),Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }).start();

                }
            }
        });
        aq.id(R.id.btnRept1).clicked(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mBoundService!=null) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            mBoundService.setRepeat(!mBoundService.isRepeat());
                            updatePlayPause();
                        }
                    }).start();
                }
            }
        });
        aq.id(R.id.btnPlay).clicked(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mBoundService!=null) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            if (mBoundService.isPlaying()) {
                                mBoundService.pause();
                                //aq.id(R.id.btnPlay).background(R.drawable.base_play_button);
                            }
                            else {
                                if (mBoundService.isPaused()) {
                                    mBoundService.playFromPause();
                                    mBoundService.startVolumeUpFlag = System.currentTimeMillis();
                                    //aq.id(R.id.btnPlay).background(R.drawable.base_pause_button);
                                }
                                else {
                                    int pos = listView.getSelectedItemPosition()>0?listView.getSelectedItemPosition():0;
                                    if (!adapter.isEmpty() && adapter.size()>pos) {
                                        mBoundService.play(pos);
                                        mBoundService.startVolumeUpFlag = System.currentTimeMillis();
                                        //aq.id(R.id.btnPlay).background(R.drawable.base_pause_button);
                                    }
                                }
                            }
                            updatePlayPause();
                        }
                    }).start();

                }

            }
        });

        update();
    }

    private void setShuffleMode(boolean mode){
        PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putBoolean("Shuffle",mode).commit();
        mBoundService.setShuffle(mode);
        shuffleItemsList();
        updatePlayPause();
    }

    public void startPlaylist(int type) {
        Intent intent = new Intent(activity,ActPlaylist.class);
        intent.putExtra("type",type);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivityForResult(intent, PLAYLIST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case PLAYLIST_CODE:
                if (resultCode == RESULT_OK) {
                    update();
                }

                break;
            case LOGIN_RESULT:
                if (resultCode == RESULT_OK) {
                    startPlaylist(2);
                }
        }
    }

    private void shuffleItemsList(){
        synchronized (this) {
            if(sourceItemsList != null /*&&
                    sourceItemsList.size() > 2*/){
                items = new ArrayList<ClsTrack>(sourceItemsList);
                if(mBoundService.isShuffle()){
                    Collections.shuffle(items, randomGenerator);
                }
            }
            synchronizeTrackList();
        }

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                adapter.replaceTrackList(items);
            }
        });
    }

    private boolean synchronizeTrackList(){
        mBoundService.updateTrackList(items);
        int newPlayingTrackIndex = mBoundService.getPlayingPosition();
        if(newPlayingTrackIndex != selected){
            selected = newPlayingTrackIndex;
            return true;
        }
        return false;
    }

    public void update() {
        if (items!=null) {
            //if (mBoundService!=null) mBoundService.Stop();
            //TODO clear player|progress status in interface (reset)
            adapter.notifyDataSetInvalidated();
            items.clear();
            adapter.notifyDataSetChanged();
        }
        TaskGetPlaylist taskGetPlaylist = new TaskGetPlaylist();
        taskGetPlaylist.setContext(getApplicationContext());
        taskGetPlaylist.execute(new ArrayList<String>());
    }

    public class TaskGetPlaylist extends AsyncTask {
        private Context context;

        @Override
        protected Object doInBackground(Object... params) {

            ArrayList<ClsTrack> o = null;
            o = (ArrayList<ClsTrack>) FileUtils.readObject("tracks", activity);
            return o;
        }

        public void setContext(Context context) {
            this.context = context;
        }

        protected void onPostExecute(Object result) {
            if (result!=null) {

                sourceItemsList = (ArrayList<ClsTrack>) result;

                AQUtility.debug("items size:", sourceItemsList.size());
                adapter.notifyDataSetInvalidated();
                shuffleItemsList();
            }
            else {
                if (PreferenceManager.getDefaultSharedPreferences(activity).getString("scanDir","").equals("")) {
                    AlertDialog.Builder dlg = new AlertDialog.Builder(activity);
                    dlg.setMessage(R.string.createNewPlaylist);
                    dlg.setCancelable(true);

                    dlg.setNeutralButton(android.R.string.ok,new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            startPlaylist(1);
                        }
                    });
                    dlg.setNegativeButton(android.R.string.cancel,null);
                    dlg.show();
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.player, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_open_fs:
                startPlaylist(1);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroy() {

        // Unbind Service
        unbindService(mConnection);

        FlurryAgent.onEndSession(activity);
        super.onDestroy();

    }

    // BassInterface: onPluginsLoaded
    public void onPluginsLoaded(String plugins) {

    }

    // BassInterface: onFileLoaded
    public void onFileLoaded(String file, final double _duration, final String _artist, final String _title,
                             final int position, final int albumId) {


        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                selected = position;

                title.setText(_title);
                seekBar.setMax((int) _duration);

                artist.setText(_artist);

                adapter.notifyDataSetChanged();
                listView.smoothScrollToPosition(position);
                listView.invalidate();
                Bitmap artwork = null;
                if (activity!=null && !activity.isFinishing()) {
                    artwork = MediaUtils.getArtworkQuick(activity, albumId, 180, 180);
                }
                if (artwork!=null) {
                    albumImage.setImageBitmap(artwork);
                    int min = Math.min(listView.getWidth(),listView.getHeight());
                    Bitmap bitmap = MediaUtils.getArtworkQuick(activity, albumId, min, min);
                    artworkBgr.setImageBitmap(bitmap);
                }
                else {
                    artworkBgr.setImageBitmap(null);
                    albumImage.setImageDrawable(getResources().getDrawable(R.drawable.artwork));
                }
            }
        });

    }

    // BassInterface: onProgressChanged
    public void onProgressChanged(final double progress) {
        seekBar.setProgress((int) progress);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String f = "mm:ss";
                if (progress>=3600d) {
                    f = "HH:mm:ss";
                }
                txtDur.setText(new SimpleDateFormat(f) {{
                    setTimeZone(TimeZone.getTimeZone("UTC"));
                    }}.format(new Date((int)progress*1000)));

            }
        });

    }

    @Override
    public void onUpdatePlayPause() {
        updatePlayPause();
    }

    @Override
    public void onResume(){
        super.onResume();
        updatePlayPause();

    }

    public void updatePlayPause() {

        if (mBoundService!=null) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mBoundService.setActivityStarted(true);

                    if (mBoundService.isPlaying()) {
                        aq.id(R.id.btnPlay).background(R.drawable.base_pause_button);
                    }
                    else {
                        aq.id(R.id.btnPlay).background(R.drawable.base_play_button);
                    }
                    if (mBoundService.isShuffle()) {
                        aq.id(R.id.btnSfl).background(R.drawable.base_shuffle_button_on);
                    }
                    else {
                        aq.id(R.id.btnSfl).background(R.drawable.base_shuffle_button_off);
                    }
                    if (mBoundService.isRepeat()) {
                        aq.id(R.id.btnRept1).background(R.drawable.base_repeat_button_on);
                    }
                    else {
                        aq.id(R.id.btnRept1).background(R.drawable.base_repeat_button_off);
                    }
                }
            });

        }

    }

    @Override
    public void onPause() {
        super.onPause();
        if (mBoundService!=null) {
            mBoundService.setActivityStarted(false);
        }
    }

    private void showContextMenu(View anchorView,final ClsTrack o) {

        List<String> menuMusic = new ArrayList<String>();
        menuMusic.add(activity.getString(R.string.search)+": "+o.getArtist());
        menuMusic.add(activity.getString(R.string.contextmenu_setasringtone));


        final ListPopupWindow
                    popup = new ListPopupWindow(activity);
        popup.setAdapter(new ArrayAdapter<String>(activity, android.R.layout.simple_list_item_1,menuMusic.toArray(new String[menuMusic.size()]) ));
        popup.setAnchorView(anchorView);
        popup.setModal(true);
        popup.setWidth(Math.max(400, anchorView.getWidth()));

        popup.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                popup.dismiss();
                switch (position){
                    case 0:
                        Intent intent = new Intent(activity,ActFreemporg.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        intent.putExtra("q",o.getArtist());
                        startActivity(intent);
                        break;
                    case 1:
                        MediaUtils.setRingtone(activity,o);
                        break;
                }
            }
        });
        popup.show();
    }
}