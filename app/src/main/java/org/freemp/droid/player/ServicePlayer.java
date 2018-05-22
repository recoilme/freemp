/*
 * This is a simple foregroundservice example using Android 2.0 API. For more information, backward compatibility, etc. visit:
 * http://developer.android.com/resources/samples/ApiDemos/src/com/example/android/apis/app/ForegroundService.html
 */

package org.freemp.droid.player;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.RemoteControlClient;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.flurry.android.FlurryAgent;
import com.un4seen.bass.BASS;

import org.freemp.droid.ClsTrack;
import org.freemp.droid.FileUtils;
import org.freemp.droid.MediaUtils;
import org.freemp.droid.NotificationUtils;
import org.freemp.droid.playlist.MakePlaylistFS;

import java.io.File;
import java.util.ArrayList;
import java.util.Random;

public class ServicePlayer extends Service implements AudioManager.OnAudioFocusChangeListener {


    private static final int NOTIFICATION_ID = 101;
    // Bass Service Binder
    private final IBinder mBinder = new BassServiceBinder();
    public long startVolumeUpFlag;
    ClsTrack currentTrack = null;
    // our RemoteControlClient object, which will use remote control APIs available in
    // SDK level >= 14, if they're available.
    RemoteControlClient remoteControlClient = null;
    Handler timerHandler = new Handler();
    // Notification
    private Notification notification;
    // Pending Intent to be called if a user click on the notification
    private PendingIntent pendIntent;
    private boolean repeat;
    private int errorCount = 0;
    //media button counter
    private long mediabtnLastEventTime = 0;
    private int mediabtnPressCounter = 0;
    // Channel Handle
    private int chan;
    //TrackList
    private ArrayList<ClsTrack> tracks = new ArrayList<ClsTrack>();
    //currentPosition
    private int position = 0;
    // Activity with implemented BassInterface
    private InterfacePlayer activity;
    private int screenHeight, screenWidth;
    // Properties: BassInterface
    private String plugins;
    private double duration = 0.0;
    private double progress = 0.0;
    private boolean shuffle = false;
    private boolean firstVolumeUpFlag;
    private boolean activityStarted;
    Runnable timerRunnable = new Runnable() {

        @Override
        public void run() {
            if (BASS.BASS_ChannelIsActive(chan) == BASS.BASS_ACTIVE_PLAYING) {
                if (activity != null && activityStarted) {

                    progress = BASS.BASS_ChannelBytes2Seconds(chan, BASS.BASS_ChannelGetPosition(chan, BASS.BASS_POS_BYTE));
                    activity.onProgressChanged(progress);
                }
            }
            timerHandler.postDelayed(this, 200);//looks like laggy timer on more then 200 values
        }
    };
    final BASS.SYNCPROC EndSync = new BASS.SYNCPROC() {
        public void SYNCPROC(int handle, int channel, int data, Object user) {
            if (!isRepeat())
                playNext();
            else
                play(position);
        }
    };
    private boolean isUnpluggedFlag;
    private TelephonyManager tm;
    private MyBroadcastReceiver myBroadcastReceiver;
    private AudioManager mAudioManager;
    private PhoneStateListener telephone = new PhoneStateListener() {
        boolean onhook = false;
        RING_STATE callstaet;

        public void onCallStateChanged(int state, String number) {
            switch (state) {
                case TelephonyManager.CALL_STATE_RINGING: {
                    callstaet = RING_STATE.STATE_RINGING;
                    if (isPlaying()) {
                        pause();
                        onhook = true;
                        //setResumeStop(CALL_RESUME);
                    }
                }
                break;
                case TelephonyManager.CALL_STATE_OFFHOOK: {
                    if (callstaet == RING_STATE.STATE_RINGING) {
                        callstaet = RING_STATE.STATE_OFFHOOK;
                    } else {
                        callstaet = RING_STATE.STATE_NORMAL;
                        if (isPlaying()) {
                            pause();
                            onhook = true;
                            //setResumeStop(CALL_RESUME);
                        }
                    }
                }
                break;
                case TelephonyManager.CALL_STATE_IDLE: {
                    if (onhook) {
                        onhook = false;
                        if (isPaused())
                            playFromPause();
                        //setResumeStart(5, CALL_RESUME);
                    }
                    callstaet = RING_STATE.STATE_NORMAL;
                }
                break;
                default: {

                }
            }
        }
    };

    public boolean isShuffle() {
        return shuffle;
    }

    public void setShuffle(boolean shuffle) {
        this.shuffle = shuffle;
    }

    public boolean isRepeat() {
        return repeat;
    }

    public void setRepeat(boolean repeat) {
        this.repeat = repeat;
    }

    public void setActivityStarted(boolean activityStarted) {
        this.activityStarted = activityStarted;
    }

    @Override
    public void onAudioFocusChange(int focusChange) {

    }

    public int getPlayingPosition() {
        return position;
    }

    // Set Activity
    public void setActivity(InterfacePlayer activity) {
        this.activity = activity;
        if (activity != null) {
            activity.onPluginsLoaded(plugins);
            activity.onFileLoaded(null, duration, "", "", 0, 0);
            activity.onProgressChanged(progress);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        // initialize default output device
        if (!BASS.BASS_Init(-1, 44100, 0)) {
            return;
        }

        // look for plugins
        plugins = "";
        String path = getApplicationInfo().nativeLibraryDir;
        String[] list = new File(path).list();
        for (String s : list) {
            int plug = BASS.BASS_PluginLoad(path + "/" + s, 0);
            if (plug != 0) { // plugin loaded...
                plugins += s + "\n"; // add it to the list
            }
        }
        if (plugins.equals("")) plugins = "no plugins - visit the BASS webpage to get some\n";
        if (activity != null) {
            activity.onPluginsLoaded(plugins);
        }
        BASS.BASS_SetConfig(BASS.BASS_CONFIG_BUFFER, 1000);
        Log.w("BASS.BASS_CONFIG_BUFFER", "" + BASS.BASS_GetConfig(BASS.BASS_CONFIG_BUFFER));
        //screen
        screenHeight = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getInt("screenHeight", 1000);
        screenWidth = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getInt("screenWidth", 800);

        // Pending Intend
        Intent intent = new Intent(this, ActPlayer.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        pendIntent = PendingIntent.getActivity(this, 0, intent, 0);

        //tracklist
        updateTrackList();

        tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        tm.listen(telephone, PhoneStateListener.LISTEN_CALL_STATE);
        myBroadcastReceiver = new MyBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_HEADSET_PLUG);
        intentFilter.addAction("android.media.VOLUME_CHANGED_ACTION");
        intentFilter.addAction(Intent.ACTION_POWER_DISCONNECTED);
        registerReceiver(myBroadcastReceiver, intentFilter);


        mAudioManager = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
        mAudioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN);
        ComponentName rcvMedia = new ComponentName(getPackageName(), RcvMediaControl.class.getName());
        mAudioManager.registerMediaButtonEventReceiver(rcvMedia);

        // Use the remote control APIs (if available) to set the playback state
        if (android.os.Build.VERSION.SDK_INT >= 14 && remoteControlClient == null) {
            registerRemoteControl(rcvMedia);
        }
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    void registerRemoteControl(ComponentName rcvMedia) {
        mAudioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN);
        Intent mediaButtonIntent = new Intent(Intent.ACTION_MEDIA_BUTTON);
        mediaButtonIntent.setComponent(rcvMedia);
        PendingIntent mediaPendingIntent = PendingIntent.getBroadcast(getApplicationContext(),
                0, mediaButtonIntent, 0);
        remoteControlClient = new RemoteControlClient(mediaPendingIntent);

        remoteControlClient.setTransportControlFlags(
                RemoteControlClient.FLAG_KEY_MEDIA_PLAY_PAUSE |
                        RemoteControlClient.FLAG_KEY_MEDIA_NEXT |
                        RemoteControlClient.FLAG_KEY_MEDIA_PREVIOUS
        );
        mAudioManager.registerRemoteControlClient(remoteControlClient);
    }

    public void updateTrackList() {
        String fileName = "tracks";
        tracks = (ArrayList<ClsTrack>) FileUtils.readObject("tracks", getApplicationContext());
    }

    public void updateTrackList(ArrayList<ClsTrack> data) {
        if (data != null) {
            tracks = data;
            if (tracks != null && position >= 0) {
                int newPlayingTrackIndex = data.indexOf(currentTrack);
                if (newPlayingTrackIndex >= 0) {
                    position = newPlayingTrackIndex;
                }
            }
        } else {
            tracks = new ArrayList<ClsTrack>();
        }
    }

    @Override
    public void onDestroy() {
        if (tm != null) {
            tm.listen(telephone, PhoneStateListener.LISTEN_NONE);
            tm = null;
        }
        if (myBroadcastReceiver != null) {
            unregisterReceiver(myBroadcastReceiver);
        }
        if (mAudioManager != null) {
            mAudioManager.unregisterMediaButtonEventReceiver(new ComponentName(getPackageName(), RcvMediaControl.class.getName()));
        }
        if (android.os.Build.VERSION.SDK_INT >= 14 && remoteControlClient != null) {
            unregisterRemoteControl();
        }
        // "free" the output device and all plugins
        BASS.BASS_Free();
        BASS.BASS_PluginFree(0);

        // Stop foreground
        stopForeground(true);
        stopUpdateProgress();

        super.onDestroy();
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    void unregisterRemoteControl() {
        mAudioManager.unregisterRemoteControlClient(remoteControlClient);
    }

    // Play file
    public void play(int pos) {
        if (tracks == null) return;
        startUpdateProgress();

        this.position = pos;

        // Play File
        String path = "";
        if (tracks != null && tracks.size() > position) {
            currentTrack = tracks.get(position);
            path = currentTrack.getPath();
        }
        if (path.equals("")) {
            onPlayError("empty");
            return;
        }

        BASS.BASS_StreamFree(chan);
        if ((chan = BASS.BASS_StreamCreateFile(path, 0, 0, 0)) == 0) {
            onPlayError(path);

            // Stop Foreground
            stopForeground(true);

            return;
        }
        // Play File
        int result = BASS.BASS_ChannelSetSync(chan, BASS.BASS_SYNC_END, 0, EndSync, 0);
        //ByteBuffer byteBuffer = (ByteBuffer)BASS.BASS_ChannelGetTags(chan, BASS.BASS_TAG_ID3V2);

        BASS.BASS_ChannelPlay(chan, false);

        // Update Properties
        this.duration = BASS.BASS_ChannelBytes2Seconds(chan, BASS.BASS_ChannelGetLength(chan, BASS.BASS_POS_BYTE));
        this.progress = 0.0;

        // Notify Activity
        if (activity != null) {
            activity.onFileLoaded(currentTrack, this.duration,
                    currentTrack.getArtist(),
                    currentTrack.getTitle(), position,
                    currentTrack.getAlbumId());
            activity.onProgressChanged(progress);
            activity.onUpdatePlayPause();
        }

        // Start foreground
        fireNotification();

        //Remote control
        if (android.os.Build.VERSION.SDK_INT >= 14 && remoteControlClient != null) {
            updateRemoteControl();
        }
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    void updateRemoteControlState(int state) {
        remoteControlClient.setPlaybackState(state);
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    void updateRemoteControl() {
        updateRemoteControlState(RemoteControlClient.PLAYSTATE_PLAYING);
        remoteControlClient.setTransportControlFlags(
                RemoteControlClient.FLAG_KEY_MEDIA_PREVIOUS |
                        RemoteControlClient.FLAG_KEY_MEDIA_PLAY_PAUSE |
                        RemoteControlClient.FLAG_KEY_MEDIA_NEXT
        );

        // Update the remote controls
        Bitmap bitmap = MediaUtils.getArtworkQuick(this, currentTrack, screenWidth / 2, screenWidth / 2);
        int redTop = 0, greenTop = 0, blueTop = 0, pixelsTop = 0;
        int redBtm = 0, greenBtm = 0, blueBtm = 0, pixelsBtm = 0;
        int colorTop = 0, colorBtm = 0;

        if (bitmap != null) {
            int w = bitmap.getWidth();
            int h = bitmap.getHeight();
            for (int i = 0; i < w; i++) {
                try {
                    colorTop = bitmap.getPixel(i, 0);
                    redTop += Color.red(colorTop);
                    greenTop += Color.green(colorTop);
                    blueTop += Color.blue(colorTop);
                    pixelsTop += 1;

                    colorBtm = bitmap.getPixel(i, h - 1);
                    redBtm += Color.red(colorBtm);
                    greenBtm += Color.green(colorBtm);
                    blueBtm += Color.blue(colorBtm);
                    pixelsBtm += 1;
                } catch (Exception e) {
                }
            }
            if (pixelsTop > 0 && pixelsBtm > 0) {
                colorTop = Color.rgb(redTop / pixelsTop, greenTop / pixelsTop, blueTop / pixelsTop); //EDE7E9
                colorBtm = Color.rgb(redBtm / pixelsBtm, greenBtm / pixelsBtm, blueBtm / pixelsBtm);
                Shader shader = new LinearGradient(w / 2, 0, w / 2, h, colorTop, colorBtm, Shader.TileMode.CLAMP);
                Bitmap bitmapBgr = Bitmap.createBitmap(w, screenHeight / 2, Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(bitmapBgr);
                Paint paint = new Paint();
                paint.setShader(shader);
                canvas.drawRect(0, 0, w, screenHeight / 2, paint);
                canvas.drawBitmap(bitmap, 0, (screenHeight / 2 - screenWidth / 2) / 2, null);
                bitmap.recycle();
                bitmap = bitmapBgr;
            }
        } else {
            //create random color
            bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
            Random rnd = new Random();
            bitmap.eraseColor(Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256)));
        }
        remoteControlClient.editMetadata(true)
                .putString(MediaMetadataRetriever.METADATA_KEY_ARTIST, currentTrack.getArtist())
                .putString(MediaMetadataRetriever.METADATA_KEY_ALBUM, currentTrack.getAlbum())
                .putString(MediaMetadataRetriever.METADATA_KEY_TITLE, currentTrack.getTitle())
                .putLong(MediaMetadataRetriever.METADATA_KEY_DURATION, currentTrack.getDuration())
                .putBitmap(RemoteControlClient.MetadataEditor.BITMAP_KEY_ARTWORK, bitmap)
                .apply();
    }

    private void fireNotification() {
        notification = NotificationUtils.getNotification(this, pendIntent, (tracks != null && tracks.size() > position) ? tracks.get(position) : null, isPlaying());
        if (notification != null) {
            startForeground(NOTIFICATION_ID, notification);
        } else {
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.cancel(NOTIFICATION_ID);
            stopForeground(true);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent.getAction();
        if ("play".equals(action)) {
            if (mediabtnLastEventTime == 0) {
                mediabtnLastEventTime = System.currentTimeMillis();
                mediabtnPressCounter++;
                Handler mediaHandler = new Handler();
                Runnable mediaRunnable = new Runnable() {

                    @Override
                    public void run() {

                        if (mediabtnPressCounter <= 1) {
                            if (isPaused())
                                playFromPause();
                            else
                                pause();
                        } else {
                            playNext();
                        }
                        fireNotification();
                        mediabtnPressCounter = 0;
                        mediabtnLastEventTime = 0;
                    }
                };
                mediaHandler.postDelayed(mediaRunnable, 500);
            } else {
                if ((System.currentTimeMillis() - mediabtnLastEventTime) < 500) {
                    //в течение секунды жмаки идут
                    mediabtnLastEventTime = System.currentTimeMillis();
                    mediabtnPressCounter++;
                } else {
                    //обнуляем
                    mediabtnLastEventTime = 0;
                    mediabtnPressCounter = 0;
                }
            }


        } else if ("next".equals(action)) {
            playNext();
        } else if ("prev".equals(action)) {
            playPrev();
        } else if ("voup".equals(action)) {
            volumeUp();
        } else if ("vodn".equals(action)) {
            volumeDown();
        }
        fireNotification();
        return START_NOT_STICKY;
    }

    public void playNext() {
        if (tracks == null) return;
        if (tracks.size() > (position + 1)) {
            play(position + 1);
        } else if (tracks.size() > 0) {
            //Play(0);
            stop();
        }
    }

    public void playPrev() {
        if (tracks == null) return;
        if ((position - 1) >= 0) {
            position = position - 1;
        } else {
            return;
        }
        play(position);
    }

    public void onPlayError(String e) {
        // Update Properties
        this.duration = 0.0;
        this.progress = 0.0;

        // Notify activity
        if (activity != null) {
            activity.onFileLoaded(tracks.get(position), this.duration, "", "", 0, 0);
            activity.onProgressChanged(progress);
            activity.onUpdatePlayPause();
        }

        stopUpdateProgress();

        //skip 1st n errors on play
        if (errorCount < 3) {
            errorCount++;
            playNext();
        } else {
            FlurryAgent.onError("onPlayError", e, "");
            stop();
        }
    }

    // Seek to position
    public void seekTo(int progress) {
        BASS.BASS_ChannelSetPosition(chan, BASS.BASS_ChannelSeconds2Bytes(chan, progress), BASS.BASS_POS_BYTE);
    }

    public void pause() {
        BASS.BASS_ChannelPause(chan);
        stopForeground(true);
        stopUpdateProgress();
        // Notify activity
        if (activity != null) {
            activity.onUpdatePlayPause();
        }
        // Tell any remote controls that our playback state is 'paused'.
        if (remoteControlClient != null) {
            updateRemoteControlState(RemoteControlClient.PLAYSTATE_PAUSED);
        }
    }

    public void stop() {
        BASS.BASS_ChannelStop(chan);
        stopUpdateProgress();
        if (activity != null) {
            activity.onUpdatePlayPause();
        }
        if (remoteControlClient != null) {
            updateRemoteControlState(RemoteControlClient.PLAYSTATE_STOPPED);

        }

    }

    public void stopUpdateProgress() {
        timerHandler.removeCallbacks(timerRunnable);
    }

    public void startUpdateProgress() {
        //start update progress
        timerHandler.postDelayed(timerRunnable, 0);
    }

    public boolean isPlaying() {
        if (!(BASS.BASS_ACTIVE_PLAYING == BASS.BASS_ChannelIsActive(chan))) {
            stopForeground(true);
            stopUpdateProgress();
        }
        if (BASS.BASS_ACTIVE_PLAYING == BASS.BASS_ChannelIsActive(chan)) {
            startUpdateProgress();
        }
        return BASS.BASS_ACTIVE_PLAYING == BASS.BASS_ChannelIsActive(chan);
    }

    public boolean isPaused() {
        return BASS.BASS_ACTIVE_PAUSED == BASS.BASS_ChannelIsActive(chan);
    }

    public void playFromPause() {
        BASS.BASS_ChannelPlay(chan, false);
        startUpdateProgress();
        // Notify activity
        if (activity != null) {
            activity.onUpdatePlayPause();
        }
        if (remoteControlClient != null) {
            updateRemoteControlState(RemoteControlClient.PLAYSTATE_PLAYING);
        }
        fireNotification();
    }

    public void volumeUp() {
        AudioManager am =
                (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        if (am == null) return;
        int currVolume = am.getStreamVolume(AudioManager.STREAM_MUSIC);
        int maxVolume = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        currVolume = currVolume + (maxVolume / 10);
        if (currVolume > maxVolume)
            currVolume = maxVolume;
        am.setStreamVolume(AudioManager.STREAM_MUSIC, currVolume, AudioManager.FLAG_SHOW_UI);
    }

    public void volumeDown() {
        AudioManager am =
                (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        if (am == null) return;
        int currVolume = am.getStreamVolume(AudioManager.STREAM_MUSIC);
        int maxVolume = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        currVolume = currVolume - (maxVolume / 10);
        if (currVolume < 0)
            currVolume = 0;
        am.setStreamVolume(AudioManager.STREAM_MUSIC, currVolume, AudioManager.FLAG_SHOW_UI);
    }


    enum RING_STATE {
        STATE_RINGING, STATE_OFFHOOK, STATE_NORMAL
    }

    // Bass Service Binder Class
    public class BassServiceBinder extends Binder {
        public ServicePlayer getService() {
            return ServicePlayer.this;
        }
    }

    public class UpdateAllFiles extends AsyncTask {

        @Override
        protected Object doInBackground(Object[] params) {
            try {
                new MakePlaylistFS(getApplicationContext(), true).getArrTracks();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    private class MediaButtonReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

        }
    }

    private class MyBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent == null) {
                return;
            }
            if (intent.getAction().equals(Intent.ACTION_POWER_DISCONNECTED)) {
                //on power disconnect scan for new files
                //new UpdateAllFiles().execute(new ArrayList<String>());
            }
            if (intent.getAction().equals("android.media.VOLUME_CHANGED_ACTION")) {

                AudioManager am =
                        (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                if (am == null) return;
                int currVolume = am.getStreamVolume(AudioManager.STREAM_MUSIC);
                int maxVolume = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
                if (currVolume == maxVolume && (System.currentTimeMillis() - startVolumeUpFlag) > 2000) {
                    if (firstVolumeUpFlag) {
                        firstVolumeUpFlag = false;
                        if (isPlaying()) {
                            startVolumeUpFlag = System.currentTimeMillis();
                            playNext();
                        }
                    } else {
                        firstVolumeUpFlag = true;
                    }

                } else {
                    startVolumeUpFlag = System.currentTimeMillis();
                }
            }
            if (intent.getAction().equals(Intent.ACTION_HEADSET_PLUG)) {
                int state = intent.getIntExtra("state", -1);
                switch (state) {
                    case 0:
                        if (isPlaying()) {
                            isUnpluggedFlag = true;
                            pause();

                        }
                        break;
                    case 1:
                        if (isUnpluggedFlag && isPaused()) {
                            isUnpluggedFlag = false;
                            playFromPause();

                        }

                        break;
                    default:

                        break;
                }
            }

        }
    }
}
