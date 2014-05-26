/*
 * This is a simple foregroundservice example using Android 2.0 API. For more information, backward compatibility, etc. visit:
 * http://developer.android.com/resources/samples/ApiDemos/src/com/example/android/apis/app/ForegroundService.html
 */

package ru.recoilme.freeamp.player;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.*;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.RemoteControlClient;
import android.os.*;
import android.preference.PreferenceManager;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import com.androidquery.util.AQUtility;
import com.faceture.google.play.PlayClient;
import com.faceture.google.play.PlayClientBuilder;
import com.faceture.google.play.PlaySession;
import com.flurry.android.FlurryAgent;
import com.un4seen.bass.BASS;
import ru.recoilme.freeamp.ClsTrack;
import ru.recoilme.freeamp.FileUtils;
import ru.recoilme.freeamp.MediaUtils;
import ru.recoilme.freeamp.NotificationUtils;
import ru.recoilme.freeamp.playlist.MakePlaylistFS;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;

public class ServicePlayer extends Service implements AudioManager.OnAudioFocusChangeListener {


	// Notification
	private Notification notification;
	
	// Pending Intent to be called if a user click on the notification
	private PendingIntent pendIntent;
    private boolean repeat;
    private int errorCount = 0;
    //media button counter
    private long mediabtnLastEventTime=0;
    private int mediabtnPressCounter=0;

    public boolean isShuffle() {
        return shuffle;
    }

    public void setShuffle(boolean shuffle){
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
        AQUtility.debug("focus change");
    }

    // Bass Service Binder Class
	public class BassServiceBinder extends Binder {
		public ServicePlayer getService() {
            return ServicePlayer.this;
        }
    }
	
	// Channel Handle
	private int chan;
    //TrackList
    private ArrayList<ClsTrack> tracks = new ArrayList<ClsTrack>();

    ClsTrack currentTrack = null;

    private PlaySession playSession = null;
    //currentPosition
    private int position = 0;

    public int getPlayingPosition() {
        return position;
    }

    // Activity with implemented BassInterface
	private InterfacePlayer activity;
    public static int screenHeight, screenWidth;

    // our RemoteControlClient object, which will use remote control APIs available in
    // SDK level >= 14, if they're available.
    RemoteControlClient remoteControlClient = null;
	
	// Set Activity
	public void setActivity(InterfacePlayer activity) {
		this.activity = activity;
		if(activity != null) {
			activity.onPluginsLoaded(plugins);
			activity.onFileLoaded("", duration, "", "",0,0);
			activity.onProgressChanged(progress);
		}
	}

	// Properties: BassInterface
	private String plugins;
	private double duration = 0.0;
	private double progress = 0.0;
    private boolean shuffle = false;
    private boolean firstVolumeUpFlag;
    public long startVolumeUpFlag;
    private boolean activityStarted;
    private boolean isUnpluggedFlag;

	// Bass Service Binder
	private final IBinder mBinder = new BassServiceBinder();

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

    private TelephonyManager tm;
    private MyBroadcastReceiver myBroadcastReceiver;
    private AudioManager mAudioManager;

    enum RING_STATE {
        STATE_RINGING, STATE_OFFHOOK, STATE_NORMAL
    }

    Handler timerHandler = new Handler();
    Runnable timerRunnable = new Runnable() {

        @Override
        public void run() {
            if(BASS.BASS_ChannelIsActive(chan) == BASS.BASS_ACTIVE_PLAYING) {
                if(activity != null && activityStarted) {

                    progress = BASS.BASS_ChannelBytes2Seconds(chan, BASS.BASS_ChannelGetPosition(chan, BASS.BASS_POS_BYTE));
                    activity.onProgressChanged(progress);
                }
            }
            timerHandler.postDelayed(this, 200);//looks like laggy timer on more then 200 values
        }
    };

    @Override
	public void onCreate() {
		super.onCreate();

		// initialize default output device
		if (!BASS.BASS_Init(-1, 44100, 0)) {
			return;
		}

		// look for plugins
		plugins="";
        String path=getApplicationInfo().nativeLibraryDir;
		String[] list=new File(path).list();
		for (String s: list) {
			int plug=BASS.BASS_PluginLoad(path+"/"+s, 0);
			if (plug!=0) { // plugin loaded...
				plugins+=s+"\n"; // add it to the list
			}
		}
		if (plugins.equals("")) plugins="no plugins - visit the BASS webpage to get some\n";
		if(activity != null) {
			activity.onPluginsLoaded(plugins);
		}
        BASS.BASS_SetConfig(BASS.BASS_CONFIG_BUFFER,1000);
        Log.w("BASS.BASS_CONFIG_BUFFER",""+BASS.BASS_GetConfig(BASS.BASS_CONFIG_BUFFER));
        //screen
        screenHeight= PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getInt("screenHeight",1000);
        screenWidth= PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getInt("screenWidth",800);
		
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

    public void updateTrackList(){
        String fileName = "tracks";
        tracks = (ArrayList<ClsTrack>)FileUtils.readObject("tracks",getApplicationContext());
        playSession = (PlaySession)FileUtils.readObject("playSession",getApplicationContext());
    }

    public void updateTrackList(ArrayList<ClsTrack> data){
        if(data != null){
            tracks = data;
            if(tracks != null && position >= 0){
                int newPlayingTrackIndex = data.indexOf(currentTrack);
                if(newPlayingTrackIndex >= 0){
                    position = newPlayingTrackIndex;
                }
            }
        }else{
            tracks = new ArrayList<ClsTrack>();
        }
    }

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

    final BASS.SYNCPROC EndSync=new BASS.SYNCPROC() {
        public void SYNCPROC(int handle, int channel, int data, Object user) {
            if (!isRepeat())
                playNext();
            else
                play(position);
        }
    };

    // Play file
	public void play(int pos) {
        if (tracks==null) return;
        startUpdateProgress();

        this.position = pos;

		// Play File
        String path = "";
        if (tracks!=null && tracks.size()>position) {
            currentTrack = tracks.get(position);
            path = currentTrack.getPath();
        }
        if (path.equals("")) {
            onPlayError("empty");
            return;
        }
        if (path.startsWith("gmid:")) {
            String[] href = {
                    path.replace("gmid:","")
            };
            try {
                path = (String) new GetGMStream().execute(href).get();
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (path==null) {
                onPlayError("null");
                return;
            }
            AQUtility.debug("getSongStream", path);
            //int c = BASS.BASS_StreamCreateURL(url, 0, BASS.BASS_STREAM_STATUS, StatusProc, r);
            BASS.BASS_StreamFree(chan);
            if ((chan=BASS.BASS_StreamCreateURL(path, 0, 0, null, 0))==0) {

                onPlayError("gmid");

                // Stop Foreground
                //stopForeground(true);

                return;
            }
        }
        else {
            BASS.BASS_StreamFree(chan);
            if ((chan=BASS.BASS_StreamCreateFile(path, 0, 0, 0/*BASS.BASS_SAMPLE_LOOP*/))==0) {

                onPlayError(path);

                // Stop Foreground
                stopForeground(true);

                return;
            }
        }

		// Play File
        int result = BASS.BASS_ChannelSetSync(chan, BASS.BASS_SYNC_END, 0, EndSync, 0);
        //ByteBuffer byteBuffer = (ByteBuffer)BASS.BASS_ChannelGetTags(chan, BASS.BASS_TAG_ID3V2);

        BASS.BASS_ChannelPlay(chan, false);

		// Update Properties
		this.duration = BASS.BASS_ChannelBytes2Seconds(chan, BASS.BASS_ChannelGetLength(chan, BASS.BASS_POS_BYTE));
		this.progress = 0.0;

		// Notify Activity
		if(activity != null) {
            AQUtility.debug("Playing title:",currentTrack.getTitle());
			activity.onFileLoaded(currentTrack.getPath(), this.duration,
                    currentTrack.getArtist(),
                    currentTrack.getTitle(),position,
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
        Bitmap bitmap = MediaUtils.getArtworkQuick(this, currentTrack.getAlbumId(), screenWidth, screenHeight);
        if (bitmap == null) {
            bitmap = Bitmap.createBitmap(screenWidth, screenHeight, Bitmap.Config.ARGB_8888);
        }
        remoteControlClient.editMetadata(false)
                .putString(MediaMetadataRetriever.METADATA_KEY_ARTIST, currentTrack.getArtist())
                .putString(MediaMetadataRetriever.METADATA_KEY_ALBUM, currentTrack.getAlbum())
                .putString(MediaMetadataRetriever.METADATA_KEY_TITLE, currentTrack.getTitle())
                .putLong(MediaMetadataRetriever.METADATA_KEY_DURATION,currentTrack.getDuration())
                .putBitmap(RemoteControlClient.MetadataEditor.BITMAP_KEY_ARTWORK, bitmap)
                .apply();
    }

    private void fireNotification() {
        notification = NotificationUtils.getNotification(this, pendIntent, (tracks!=null && tracks.size()>0)?tracks.get(position):null, isPlaying());
        startForeground(1, notification);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent.getAction();
        AQUtility.debug("!", action);
        if ("play".equals(action)) {
            if (mediabtnLastEventTime == 0) {
                mediabtnLastEventTime = System.currentTimeMillis();
                mediabtnPressCounter ++;
                Handler  mediaHandler = new Handler();
                Runnable mediaRunnable = new Runnable() {

                    @Override
                    public void run() {

                        if (mediabtnPressCounter<=1) {
                            if (isPaused())
                                playFromPause();
                            else
                                pause();
                        }
                        else {
                            playNext();
                        }
                        fireNotification();
                        mediabtnPressCounter = 0;
                        mediabtnLastEventTime = 0;
                    }
                };
                mediaHandler.postDelayed(mediaRunnable,500);
            }
            else {
                if ((System.currentTimeMillis()-mediabtnLastEventTime)<500) {
                    //в течение секунды жмаки идут
                    mediabtnLastEventTime = System.currentTimeMillis();
                    mediabtnPressCounter ++;
                }
                else {
                    //обнуляем
                    mediabtnLastEventTime = 0;
                    mediabtnPressCounter = 0;
                }
            }



        } else if ("next".equals(action)){
            playNext();
        } else if ("prev".equals(action)){
            playPrev();
        } else if ("voup".equals(action)){
            volumeUp();
        } else if("vodn".equals(action)){
            volumeDown();
        }
        fireNotification();
        return START_NOT_STICKY;
    }

    public void playNext(){
        if (tracks==null) return;
        if (tracks.size()>(position+1)) {
            play(position + 1);
        }
        else if (tracks.size()>0){
            //Play(0);
            stop();
        }
    }

    public void playPrev(){
        if (tracks==null) return;
        if ((position-1)>=0) {
            position = position-1;
        }
        else {
            return;
        }
        play(position);
    }

    public void onPlayError(String e) {
        // Update Properties
        this.duration = 0.0;
        this.progress = 0.0;

        // Notify activity
        if(activity != null) {
            activity.onFileLoaded(tracks.get(position).getPath(), this.duration, "", "", 0,0);
            activity.onProgressChanged(progress);
            activity.onUpdatePlayPause();
        }

        stopUpdateProgress();

        //skip 1st n errors on play
        if (errorCount<3) {
            errorCount++;
            playNext();
        }
        else {
            FlurryAgent.onError("onPlayError",e,"");
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
        if(activity != null) {
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
        if(activity != null) {
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
        return BASS.BASS_ACTIVE_PLAYING ==BASS.BASS_ChannelIsActive(chan);
    }

    public boolean isPaused() {
        return BASS.BASS_ACTIVE_PAUSED ==BASS.BASS_ChannelIsActive(chan);
    }
    public void playFromPause() {
        AQUtility.debug("playFromPause");
        BASS.BASS_ChannelPlay(chan, false);
        startUpdateProgress();
        // Notify activity
        if(activity != null) {
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
        if (am==null) return;
        int currVolume = am.getStreamVolume(AudioManager.STREAM_MUSIC);
        int maxVolume = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        currVolume = currVolume +(maxVolume/10);
        if (currVolume>maxVolume)
            currVolume =maxVolume;
        am.setStreamVolume(AudioManager.STREAM_MUSIC,currVolume,AudioManager.FLAG_SHOW_UI);
    }

    public void volumeDown() {
        AudioManager am =
                (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        if (am==null) return;
        int currVolume = am.getStreamVolume(AudioManager.STREAM_MUSIC);
        int maxVolume = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        currVolume = currVolume - (maxVolume/10);
        if (currVolume<0)
            currVolume = 0;
        am.setStreamVolume(AudioManager.STREAM_MUSIC,currVolume,AudioManager.FLAG_SHOW_UI);
    }

    public class GetGMStream extends AsyncTask {

        @Override
        protected Object doInBackground(Object[] params) {
            try {
                //String deviceId = Settings.Secure.getString(getApplicationContext().getContentResolver(),
                //        Settings.Secure.ANDROID_ID);
                PlayClient playClient = new PlayClientBuilder().create();
                if (playSession==null) {
                    playSession = (PlaySession)FileUtils.readObject("playSession",getApplicationContext());
                }

                URI uri = playClient.getPlayURI(((String) params[0]), playSession);

                return uri.toString();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    public class UpdateAllFiles extends AsyncTask {

        @Override
        protected Object doInBackground(Object[] params) {
            try {
                new MakePlaylistFS(getApplicationContext(),true).getArrTracks();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    private class MediaButtonReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            AQUtility.debug("ACTION",intent.getAction()+":");
        }
    }
    private class MyBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent==null) {
                return;
            }

            AQUtility.debug("ACTION",intent.getAction()+":");
            if (intent.getAction().equals(Intent.ACTION_POWER_DISCONNECTED)){
                //on power disconnect scan for new files
                new UpdateAllFiles().execute(new ArrayList<String>());
            }
            if (intent.getAction().equals("android.media.VOLUME_CHANGED_ACTION")){

                AudioManager am =
                        (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                if (am==null) return;
                int currVolume = am.getStreamVolume(AudioManager.STREAM_MUSIC);
                int maxVolume = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
                if (currVolume==maxVolume && (System.currentTimeMillis()- startVolumeUpFlag)>2000) {
                    if (firstVolumeUpFlag) {
                        firstVolumeUpFlag = false;
                        if (isPlaying()) {
                            startVolumeUpFlag = System.currentTimeMillis();
                            playNext();
                        }
                    }
                    else {
                        firstVolumeUpFlag = true;
                    }

                }
                else {
                    startVolumeUpFlag = System.currentTimeMillis();
                }
            }
            if (intent.getAction().equals(Intent.ACTION_HEADSET_PLUG)) {
                int state = intent.getIntExtra("state",-1);
                switch (state){
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
