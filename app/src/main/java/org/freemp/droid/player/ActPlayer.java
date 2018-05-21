package org.freemp.droid.player;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.format.Time;
import android.view.Display;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.androidquery.util.AQUtility;
import com.flurry.android.FlurryAgent;

import org.freemp.droid.ClsTrack;
import org.freemp.droid.Constants;
import org.freemp.droid.FileUtils;
import org.freemp.droid.MediaUtils;
import org.freemp.droid.R;
import org.freemp.droid.UpdateUtils;
import org.freemp.droid.playlist.ActPlaylist;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;


/**
 * Created with IntelliJ IDEA.
 * User: recoilme
 * Date: 28/11/13
 * Time: 15:10
 * To change this template use File | Settings | File Templates.
 */
public class ActPlayer extends AppCompatActivity implements InterfacePlayer {

    public static final int LOGIN_RESULT = 101;
    private static final int PLAYLIST_CODE = 100;
    //private int READ_STORAGE_PERMISSION_REQUEST_CODE;
    private static final int PERMISSION_REQUEST_CODE = 200;
    public static int selected = -1;
    private static Random randomGenerator;
    private AQuery aq;
    private AdpPlayer adapter;
    private ArrayList<ClsTrack> items;
    private ArrayList<ClsTrack> sourceItemsList;
    private Activity activity;
    private ListView listView;
    private SeekBar seekBar;
    private TextView txtDur, artist, title;
    private ImageView albumImage;
    private ImageView artworkBgr;
    private int screenHeight, screenWidth;
    private String[] perms = {"android.permission.WRITE_EXTERNAL_STORAGE", "android.permission.READ_EXTERNAL_STORAGE"};
    //private int WRITE_SETTINGS_PERMISSION_REQUEST_CODE;
    // Bass Service
    private ServicePlayer mBoundService = null;

    // Bass Service Connection
    private ServiceConnection mConnection = new ServiceConnection() {

        public void onServiceConnected(ComponentName name, IBinder service) {
            mBoundService = ((ServicePlayer.BassServiceBinder) service).getService();
            onBassServiceConnected();

            setShuffleMode(PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getBoolean("Shuffle", false));
        }

        public void onServiceDisconnected(ComponentName name) {
            mBoundService = null;
        }

    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_DITHER, WindowManager.LayoutParams.FLAG_DITHER);

        AQUtility.setDebug(Constants.DEBUG);

        setContentView(R.layout.player);

        activity = this;
        if (!checkPermission()) {

            requestPermission();

        }



        Display display = getWindowManager().getDefaultDisplay();
        screenWidth = display.getWidth();
        screenHeight = display.getHeight();
        PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putInt("screenWidth", screenWidth).commit();
        PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putInt("screenHeight", screenHeight).commit();
        aq = new AQuery(activity);
        if (randomGenerator == null) {
            Time now = new Time();
            randomGenerator = new Random(now.toMillis(true));
        }
        FlurryAgent.onStartSession(activity, getString(R.string.flurry));

        View customView = activity.getLayoutInflater().inflate(R.layout.player_ab, null);

        ActionBar actionBar = getSupportActionBar();

        actionBar.setCustomView(customView, new ActionBar.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.WRAP_CONTENT));//(int) (90*scale + 0.5f)));
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        listView = aq.id(R.id.listView).getListView();
        txtDur = (TextView) customView.findViewById(R.id.textViewDur);
        artist = (TextView) customView.findViewById(R.id.textViewArttist);
        title = (TextView) customView.findViewById(R.id.textViewTitle);
        seekBar = (SeekBar) customView.findViewById(R.id.seekBar);
        albumImage = (ImageView) customView.findViewById(R.id.album_img);
        artworkBgr = (ImageView) findViewById(R.id.artworkBgr);

        sourceItemsList = items = new ArrayList<ClsTrack>();
        adapter = new AdpPlayer(activity, items);

        listView.setAdapter(adapter);

        // Start Service
        startService(new Intent(this, ServicePlayer.class));

        // Bind Service
        bindService(new Intent(this, ServicePlayer.class), mConnection, Context.BIND_AUTO_CREATE);
        new UpdateUtils(activity);

    }

    private boolean checkPermission() {
        int result = ContextCompat.checkSelfPermission(getApplicationContext(), "android.permission.WRITE_EXTERNAL_STORAGE");
        int result2 = ContextCompat.checkSelfPermission(getApplicationContext(), "android.permission.READ_EXTERNAL_STORAGE");
        return result == PackageManager.PERMISSION_GRANTED && result2 == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission() {

        ActivityCompat.requestPermissions(this,perms, PERMISSION_REQUEST_CODE);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0) {
                    boolean ws = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean rs = false;
                    if (grantResults.length > 1) {
                        rs = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    }

                    if (ws && rs) {
                        return;
                    } else {

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                showMessageOKCancel("You need to allow access to both the permissions",
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                                    requestPermissions(perms, PERMISSION_REQUEST_CODE);
                                                }
                                            }
                                        });
                                return;
                            //}
                        }

                    }
                }


                break;
        }
    }
    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(ActPlayer.this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
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
                        //aq.id(R.id.btnPlay).background(R.drawable.player_pause_button);
                        updatePlayPause();
                    }
                }).start();

            }
        });


        aq.id(R.id.btnFf).clicked(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (mBoundService != null) {
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
                if (mBoundService != null) {
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
                if (mBoundService != null) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            boolean mode = !mBoundService.isShuffle();
                            setShuffleMode(mode);

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(activity, getString(R.string.shuffle_is) +
                                            (mBoundService.isShuffle() ? " on" : " off"), Toast.LENGTH_SHORT).show();
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
                if (mBoundService != null) {
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
                if (mBoundService != null) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            if (mBoundService.isPlaying()) {
                                mBoundService.pause();
                                //aq.id(R.id.btnPlay).background(R.drawable.player_play_button);
                            } else {
                                if (mBoundService.isPaused()) {
                                    mBoundService.playFromPause();
                                    mBoundService.startVolumeUpFlag = System.currentTimeMillis();
                                    //aq.id(R.id.btnPlay).background(R.drawable.player_pause_button);
                                } else {
                                    int pos = listView.getSelectedItemPosition() > 0 ? listView.getSelectedItemPosition() : 0;
                                    if (!adapter.isEmpty() && adapter.getCount() > pos) {
                                        mBoundService.play(pos);
                                        mBoundService.startVolumeUpFlag = System.currentTimeMillis();
                                        //aq.id(R.id.btnPlay).background(R.drawable.player_pause_button);
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

    private void setShuffleMode(boolean mode) {
        PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putBoolean("Shuffle", mode).commit();
        mBoundService.setShuffle(mode);
        shuffleItemsList();
        updatePlayPause();
    }

    public void startPlaylist(int type) {
        Intent intent = new Intent(activity, ActPlaylist.class);
        intent.putExtra("type", type);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivityForResult(intent, PLAYLIST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
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

    private void shuffleItemsList() {
        synchronized (this) {
            if (sourceItemsList != null /*&&
                    sourceItemsList.size() > 2*/) {
                items = new ArrayList<ClsTrack>(sourceItemsList);
                if (mBoundService.isShuffle()) {
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

    private boolean synchronizeTrackList() {
        mBoundService.updateTrackList(items);
        int newPlayingTrackIndex = mBoundService.getPlayingPosition();
        if (newPlayingTrackIndex != selected) {
            selected = newPlayingTrackIndex;
            return true;
        }
        return false;
    }

    public void update() {
        if (items != null) {
            //if (mBoundService!=null) mBoundService.Stop();
            //TODO clear menu_player|progress status in interface (reset)
            adapter.notifyDataSetInvalidated();
            items.clear();
            adapter.notifyDataSetChanged();
        }
        TaskGetPlaylist taskGetPlaylist = new TaskGetPlaylist();
        taskGetPlaylist.setContext(getApplicationContext());
        taskGetPlaylist.execute(new ArrayList<String>());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_player, menu);
        return super.onCreateOptionsMenu(menu);
    }
    /*
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        MenuItem item= menu.findItem(action_settings);
        item.setVisible(false);
        return true;
    }
    */

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
    public void onFileLoaded(final ClsTrack track, final double _duration, final String _artist, final String _title,
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
                /*
                Drawable drawable = ((ImageView)view).getDrawable();
                if(drawable instanceof BitmapDrawable)
                {
                    BitmapDrawable bitmapDrawable = (BitmapDrawable)drawable;
                    bitmapDrawable.getBitmap().recycle();
                }

                final BitmapDrawable aid = ((BitmapDrawable) albumImage.getDrawable());
                if (aid!=null && aid.getBitmap()!=null) aid.getBitmap().recycle();

                final BitmapDrawable ari = ((BitmapDrawable) artworkBgr.getDrawable());
                if (ari!=null && ari.getBitmap()!=null) ari.getBitmap().recycle();
                */
                if (activity != null && !activity.isFinishing()) {
                    artwork = MediaUtils.getArtworkQuick(activity, track, 180, 180);
                }
                if (artwork != null) {

                    albumImage.setImageBitmap(artwork);
                    int min = Math.min(screenWidth, screenHeight) / 2;
                    Bitmap bitmap = MediaUtils.getArtworkQuick(activity, track, min, min);
                    artworkBgr.setImageBitmap(bitmap);
                    listView.setBackgroundResource(R.drawable.player_listview_bgr);
                } else {
                    artworkBgr.setImageBitmap(null);
                    albumImage.setImageDrawable(getResources().getDrawable(R.drawable.empty_artwork));
                    listView.setBackgroundColor(getResources().getColor(R.color.bgr));
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
                //String f = "mm:ss";
                int sec = (int) progress;
                int min = sec / 60;
                sec %= 60;
                if (progress >= 3600d) {
                    //f = "HH:mm:ss";
                    txtDur.setText(String.format("%2d:%2d:%02d", ((int) sec / 3600), min, sec));
                } else {
                    txtDur.setText(String.format("%2d:%02d", min, sec));
                }

                //txtDur.setText(new SimpleDateFormat(f) {{
                //    setTimeZone(TimeZone.getTimeZone("UTC"));
                //    }}.format(new Date((int)progress*1000)));

            }
        });

    }

    @Override
    public void onUpdatePlayPause() {
        updatePlayPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        updatePlayPause();
    }

    public void updatePlayPause() {

        if (mBoundService != null) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mBoundService.setActivityStarted(true);

                    if (mBoundService.isPlaying()) {
                        aq.id(R.id.btnPlay).background(R.drawable.player_pause_button);
                    } else {
                        aq.id(R.id.btnPlay).background(R.drawable.player_play_button);
                    }
                    if (mBoundService.isShuffle()) {
                        aq.id(R.id.btnSfl).background(R.drawable.player_shuffle_button_on);
                    } else {
                        aq.id(R.id.btnSfl).background(R.drawable.player_shuffle_button_off);
                    }
                    if (mBoundService.isRepeat()) {
                        aq.id(R.id.btnRept1).background(R.drawable.player_repeat_button_on);
                    } else {
                        aq.id(R.id.btnRept1).background(R.drawable.player_repeat_button_off);
                    }
                }
            });

        }

    }

    @Override
    public void onPause() {
        super.onPause();
        if (mBoundService != null) {
            mBoundService.setActivityStarted(false);
        }
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
            if (result != null) {

                sourceItemsList = (ArrayList<ClsTrack>) result;
                adapter.notifyDataSetInvalidated();
                shuffleItemsList();
            } else {
                if (PreferenceManager.getDefaultSharedPreferences(activity).getString("scanDir", "").equals("")) {
                    AlertDialog.Builder dlg = new AlertDialog.Builder(activity);
                    dlg.setMessage(R.string.createNewPlaylist);
                    dlg.setCancelable(true);

                    dlg.setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            startPlaylist(1);
                        }
                    });
                    dlg.setNegativeButton(android.R.string.cancel, null);
                    if (checkPermission()) {
                        dlg.show();
                    }

                }
            }
        }
    }

}