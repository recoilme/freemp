/*
	BASSmix 2.4 Java class
	Copyright (c) 2005-2012 Un4seen Developments Ltd.

	See the BASSMIX.CHM file for more detailed documentation
*/

package com.un4seen.bass;

import java.nio.ByteBuffer;

public class BASSmix {
    // additional BASS_SetConfig option
    public static final int BASS_CONFIG_MIXER_BUFFER = 0x10601;
    public static final int BASS_CONFIG_MIXER_POSEX = 0x10602;
    public static final int BASS_CONFIG_SPLIT_BUFFER = 0x10610;

    // BASS_Mixer_StreamCreate flags
    public static final int BASS_MIXER_END = 0x10000;    // end the stream when there are no sources
    public static final int BASS_MIXER_NONSTOP = 0x20000;    // don't stall when there are no sources
    public static final int BASS_MIXER_RESUME = 0x1000;    // resume stalled immediately upon new/unpaused source
    public static final int BASS_MIXER_POSEX = 0x2000;    // enable BASS_Mixer_ChannelGetPositionEx support

    // source flags
    public static final int BASS_MIXER_BUFFER = 0x2000;    // buffer data for BASS_Mixer_ChannelGetData/Level
    public static final int BASS_MIXER_LIMIT = 0x4000;    // limit mixer processing to the amount available from this source
    public static final int BASS_MIXER_MATRIX = 0x10000;    // matrix mixing
    public static final int BASS_MIXER_PAUSE = 0x20000;    // don't process the source
    public static final int BASS_MIXER_DOWNMIX = 0x400000; // downmix to stereo/mono
    public static final int BASS_MIXER_NORAMPIN = 0x800000; // don't ramp-in the start

    // splitter flags
    public static final int BASS_SPLIT_SLAVE = 0x1000;    // only read buffered data
    // envelope types
    public static final int BASS_MIXER_ENV_FREQ = 1;
    public static final int BASS_MIXER_ENV_VOL = 2;
    public static final int BASS_MIXER_ENV_PAN = 3;
    public static final int BASS_MIXER_ENV_LOOP = 0x10000; // FLAG: loop
    // additional sync type
    public static final int BASS_SYNC_MIXER_ENVELOPE = 0x10200;
    public static final int BASS_SYNC_MIXER_ENVELOPE_NODE = 0x10201;
    // BASS_CHANNELINFO type
    public static final int BASS_CTYPE_STREAM_MIXER = 0x10800;
    public static final int BASS_CTYPE_STREAM_SPLIT = 0x10801;

    static {
        System.loadLibrary("bassmix");
    }

    public static native int BASS_Mixer_GetVersion();

    public static native int BASS_Mixer_StreamCreate(int freq, int chans, int flags);

    public static native boolean BASS_Mixer_StreamAddChannel(int handle, int channel, int flags);

    public static native boolean BASS_Mixer_StreamAddChannelEx(int handle, int channel, int flags, long start, long length);

    public static native int BASS_Mixer_ChannelGetMixer(int handle);

    public static native int BASS_Mixer_ChannelFlags(int handle, int flags, int mask);

    public static native boolean BASS_Mixer_ChannelRemove(int handle);

    public static native boolean BASS_Mixer_ChannelSetPosition(int handle, long pos, int mode);

    public static native long BASS_Mixer_ChannelGetPosition(int handle, int mode);

    public static native long BASS_Mixer_ChannelGetPositionEx(int channel, int mode, int delay);

    public static native int BASS_Mixer_ChannelGetLevel(int handle);

    public static native int BASS_Mixer_ChannelGetData(int handle, ByteBuffer buffer, int length);

    public static native int BASS_Mixer_ChannelSetSync(int handle, int type, long param, BASS.SYNCPROC proc, Object user);

    public static native boolean BASS_Mixer_ChannelRemoveSync(int channel, int sync);

    public static native boolean BASS_Mixer_ChannelSetMatrix(int handle, float[][] matrix);

    public static native boolean BASS_Mixer_ChannelGetMatrix(int handle, float[][] matrix);

    public static native boolean BASS_Mixer_ChannelSetEnvelope(int handle, int type, BASS_MIXER_NODE[] nodes, int count);

    public static native boolean BASS_Mixer_ChannelSetEnvelopePos(int handle, int type, long pos);

    public static native long BASS_Mixer_ChannelGetEnvelopePos(int handle, int type, Float value);

    public static native int BASS_Split_StreamCreate(int channel, int flags, int[] chanmap);

    public static native int BASS_Split_StreamGetSource(int handle);

    public static native int BASS_Split_StreamGetSplits(int handle, int[] splits, int count);

    public static native boolean BASS_Split_StreamReset(int handle);

    public static native boolean BASS_Split_StreamResetEx(int handle, int offset);

    public static native int BASS_Split_StreamGetAvailable(int handle);

    // envelope node
    public static class BASS_MIXER_NODE {
        public long pos;
        public float value;

        public BASS_MIXER_NODE() {
        }

        public BASS_MIXER_NODE(long _pos, float _value) {
            pos = _pos;
            value = _value;
        }
    }
}
