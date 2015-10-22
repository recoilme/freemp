/*
	BASSOPUS 2.4 Java class
	Copyright (c) 2012 Un4seen Developments Ltd.

	See the BASSOPUS.CHM file for more detailed documentation
*/

package com.un4seen.bass;

import java.nio.ByteBuffer;

public class BASSOPUS {
    // BASS_CHANNELINFO type
    public static final int BASS_CTYPE_STREAM_OPUS = 0x11200;

    // Additional attributes
    public static final int BASS_ATTRIB_OPUS_ORIGFREQ = 0x13000;

    static {
        System.loadLibrary("bassopus");
    }

    public static native int BASS_OPUS_StreamCreateFile(String file, long offset, long length, int flags);

    public static native int BASS_OPUS_StreamCreateFile(ByteBuffer file, long offset, long length, int flags);

    public static native int BASS_OPUS_StreamCreateURL(String url, int offset, int flags, BASS.DOWNLOADPROC proc, Object user);

    public static native int BASS_OPUS_StreamCreateFileUser(int system, int flags, BASS.BASS_FILEPROCS procs, Object user);
}
