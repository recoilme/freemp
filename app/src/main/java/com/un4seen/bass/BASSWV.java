/*
	BASSWV 2.4 Java class
	Copyright (c) 2007-2012 Un4seen Developments Ltd.

	See the BASSWV.CHM file for more detailed documentation
*/

package com.un4seen.bass;

import java.nio.ByteBuffer;

public class BASSWV {
    // BASS_CHANNELINFO type
    public static final int BASS_CTYPE_STREAM_WV = 0x10500;

    static {
        System.loadLibrary("basswv");
    }

    public static native int BASS_WV_StreamCreateFile(String file, long offset, long length, int flags);

    public static native int BASS_WV_StreamCreateFile(ByteBuffer file, long offset, long length, int flags);

    public static native int BASS_WV_StreamCreateURL(String url, int offset, int flags, BASS.DOWNLOADPROC proc, Object user);

    public static native int BASS_WV_StreamCreateFileUser(int system, int flags, BASS.BASS_FILEPROCS procs, Object user);
}
