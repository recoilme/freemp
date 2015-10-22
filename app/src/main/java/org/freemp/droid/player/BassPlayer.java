package org.freemp.droid.player;

import android.content.Context;
import android.content.pm.ApplicationInfo;

import com.flurry.android.FlurryAgent;
import com.un4seen.bass.BASS;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class BassPlayer {
    private static int chan;
    private static int[] fxBands = new int[10];
    private static int fxReverb;
    private final Object lock = new Object();
    public boolean isPlaying;
    int req;
    private int totalTime;
    private OnCompletionListener completionListener;
    private final BASS.SYNCPROC EndSync = new BASS.SYNCPROC() {
        public void SYNCPROC(int handle, int channel, int data, Object user) {
            isPlaying = false;
            if (completionListener != null)
                completionListener.onCompletion();
        }
    };
    private FileChannel fc;
    private String filePath;
    private final BASS.DOWNLOADPROC StatusProc = new BASS.DOWNLOADPROC() {
        @Override
        public void DOWNLOADPROC(ByteBuffer buffer, int length, Object user) {
            //&& (Integer)user == req
            if (filePath != null) {
                try {
                    if (buffer != null) {
                        if (fc == null)
                            fc = new FileOutputStream(new File(filePath)).getChannel();
                        fc.write(buffer);
                    } else if (fc != null) {
                        fc.close();
                        fc = null;
                    }
                } catch (IOException e) {
                    FlurryAgent.onError("5", "5", e);
                }
            }

        }
    };

    public BassPlayer(Context context) {
        BASS.BASS_Init(-1, 44100, 0);
        BASS.BASS_SetConfig(BASS.BASS_CONFIG_FLOATDSP, 32);
        chan = 0;
        ApplicationInfo info = context.getApplicationInfo();
        if (info != null) {
            String path = info.nativeLibraryDir;
            String[] list = new File(path).list();
            for (String s : list) {
                BASS.BASS_PluginLoad(path + "/" + s, 0);
            }
        }
    }

    public static void updateFX(int progress, int n) {
        BASS.BASS_DX8_PARAMEQ p = new BASS.BASS_DX8_PARAMEQ();
        BASS.BASS_FXGetParameters(fxBands[n], p);
        //p.fGain = EqualizerBandsFragment.convertProgressToGain(progress);
        BASS.BASS_FXSetParameters(fxBands[n], p);
    }

    public static void setReverb(int progress) {
        BASS.BASS_DX8_REVERB p = new BASS.BASS_DX8_REVERB();
        BASS.BASS_FXGetParameters(fxReverb, p);
        p.fReverbMix = (float) (progress > 15 ? Math.log((double) progress / 20.0) * 20.0 : -96.0);
        BASS.BASS_FXSetParameters(fxReverb, p);
    }

    public static void setLowFreq(int progress) {
        for (int i = 0; i < 5; i++) {
            BASS.BASS_DX8_PARAMEQ p = new BASS.BASS_DX8_PARAMEQ();
            BASS.BASS_FXGetParameters(fxBands[i], p);

            if (i < 3)
                p.fBandwidth = 0.5f * convertProgressToFreq(progress);
            else
                p.fBandwidth = 2f * convertProgressToFreq(progress);

            BASS.BASS_FXSetParameters(fxBands[i], p);
        }

    }

    public static void setHightFreq(int progress) {
        for (int i = 6; i < 10; i++) {
            BASS.BASS_DX8_PARAMEQ p = new BASS.BASS_DX8_PARAMEQ();
            BASS.BASS_FXGetParameters(fxBands[i], p);
            if (i < 8)
                p.fBandwidth = 4f * convertProgressToFreq(progress);
            else
                p.fBandwidth = 8f * convertProgressToFreq(progress);
            BASS.BASS_FXSetParameters(fxBands[i], p);
        }
    }

    public static float convertProgressToFreq(int progress) {
        return ((float) progress * 3f / 100f) + 1;
    }

    public void setOnCompletetion(OnCompletionListener listener) {
        this.completionListener = listener;
    }

    public void prepareFile(String url) throws IOException {
        chan = BASS.BASS_StreamCreateFile(url, 0L, 0L, 0);

        if (chan == 0) {
            throw new IOException("prepare exception " + BASS.BASS_ErrorGetCode());
        }

        long bytes = BASS.BASS_ChannelGetLength(chan, BASS.BASS_POS_BYTE);
        totalTime = (int) BASS.BASS_ChannelBytes2Seconds(chan, bytes);
        BASS.BASS_CHANNELINFO info = new BASS.BASS_CHANNELINFO();
        BASS.BASS_ChannelGetInfo(chan, info);
        BASS.BASS_ChannelSetSync(chan, BASS.BASS_SYNC_END, 0, EndSync, 0);

        setUpEffects();

    }

    private void setUpEffects() {
        fxBands[0] = BASS.BASS_ChannelSetFX(chan, BASS.BASS_FX_DX8_PARAMEQ, 0);
        fxBands[1] = BASS.BASS_ChannelSetFX(chan, BASS.BASS_FX_DX8_PARAMEQ, 0);
        fxBands[2] = BASS.BASS_ChannelSetFX(chan, BASS.BASS_FX_DX8_PARAMEQ, 0);
        fxBands[3] = BASS.BASS_ChannelSetFX(chan, BASS.BASS_FX_DX8_PARAMEQ, 0);
        fxBands[4] = BASS.BASS_ChannelSetFX(chan, BASS.BASS_FX_DX8_PARAMEQ, 0);
        fxBands[5] = BASS.BASS_ChannelSetFX(chan, BASS.BASS_FX_DX8_PARAMEQ, 0);
        fxBands[6] = BASS.BASS_ChannelSetFX(chan, BASS.BASS_FX_DX8_PARAMEQ, 0);
        fxBands[7] = BASS.BASS_ChannelSetFX(chan, BASS.BASS_FX_DX8_PARAMEQ, 0);
        fxBands[8] = BASS.BASS_ChannelSetFX(chan, BASS.BASS_FX_DX8_PARAMEQ, 0);
        fxBands[9] = BASS.BASS_ChannelSetFX(chan, BASS.BASS_FX_DX8_PARAMEQ, 0);
        fxReverb = BASS.BASS_ChannelSetFX(chan, BASS.BASS_FX_DX8_REVERB, 0);

        BASS.BASS_DX8_PARAMEQ p = new BASS.BASS_DX8_PARAMEQ();

        p.fGain = 0;
        p.fBandwidth = 0.5f;
        p.fCenter = 32;
        BASS.BASS_FXSetParameters(fxBands[0], p);
        p.fCenter = 64;
        BASS.BASS_FXSetParameters(fxBands[1], p);
        p.fCenter = 125;
        BASS.BASS_FXSetParameters(fxBands[2], p);
        p.fBandwidth = 2f;
        p.fCenter = 250;
        BASS.BASS_FXSetParameters(fxBands[3], p);
        p.fCenter = 500;
        BASS.BASS_FXSetParameters(fxBands[4], p);
        p.fBandwidth = 4f;
        p.fCenter = 1000;
        BASS.BASS_FXSetParameters(fxBands[5], p);
        p.fCenter = 2000;
        BASS.BASS_FXSetParameters(fxBands[6], p);
        p.fCenter = 4000;
        BASS.BASS_FXSetParameters(fxBands[7], p);
        p.fCenter = 8000;
        p.fBandwidth = 8f;
        BASS.BASS_FXSetParameters(fxBands[8], p);
        p.fCenter = 16000;
        BASS.BASS_FXSetParameters(fxBands[9], p);


        //int[] bounds  = EqualizerBandsFragment.getSavedBounds();
        //for(int i = 0 ; i < bounds.length ; i++ )
        //    updateFX(bounds[i], i);

        //int[] effecs = EqualizerEffectsFragment.getSavedEffects();
        //setLowFreq(effecs[0]);
        //setHightFreq(effecs[1]);
        //setReverb(effecs[2]);


    }

    public void prepareNet(String url) throws IOException {
        int r;
        synchronized (lock) {
            r = ++req;
        }

        int c = BASS.BASS_StreamCreateURL(url, 0, BASS.BASS_STREAM_STATUS, StatusProc, r);

        synchronized (lock) {
            if (r != req) {
                if (c != 0) {
                    BASS.BASS_StreamFree(c);
                }
                throw new IOException("prepare exception");
            }
            chan = c;
        }

        if (chan == 0) {
            throw new IOException("prepare exception");
        }

        long bytes = BASS.BASS_ChannelGetLength(chan, BASS.BASS_POS_BYTE);
        totalTime = (int) BASS.BASS_ChannelBytes2Seconds(chan, bytes);
        BASS.BASS_CHANNELINFO info = new BASS.BASS_CHANNELINFO();
        BASS.BASS_ChannelGetInfo(chan, info);
        BASS.BASS_ChannelSetSync(chan, BASS.BASS_SYNC_END, 0, EndSync, 0);

        setUpEffects();

    }

    public void start() {
        BASS.BASS_ChannelPlay(chan, false);
        isPlaying = true;
    }

    public void pause() {
        BASS.BASS_ChannelPause(chan);
        isPlaying = false;
    }

    public int getCurrentPosition() {
        return (int) BASS.BASS_ChannelBytes2Seconds(chan, BASS.BASS_ChannelGetPosition(chan, BASS.BASS_POS_BYTE));
    }

    public int getTotalTime() {
        return totalTime;
    }

    public void seekTo(int to) {
        BASS.BASS_ChannelSetPosition(chan, BASS.BASS_ChannelSeconds2Bytes(chan, to), BASS.BASS_POS_BYTE);
    }

    public void releaseTotal() {
        BASS.BASS_Free();
        BASS.BASS_PluginFree(0);
    }

    public void release() {
        BASS.BASS_MusicFree(chan);
        BASS.BASS_StreamFree(chan);

        assert fc == null;
        filePath = null;
    }

    public int getPercentage() {
        if (getTotalTime() == 0)
            return 0;
        return (100 * getCurrentPosition()) / getTotalTime();
    }

    public interface OnCompletionListener {
        void onCompletion();
    }
}
