package com.un4seen.bass;

import java.nio.ByteBuffer;

public class BASS_ALAC {
    // BASS_CHANNELINFO type
    public static final int BASS_CTYPE_STREAM_ALAC = 0x10e00;

    static {
        System.loadLibrary("bass_alac");
    }

    public static native int BASS_ALAC_StreamCreateFile(String file, long offset, long length, int flags);

    public static native int BASS_ALAC_StreamCreateFile(ByteBuffer file, long offset, long length, int flags);

    public static native int BASS_ALAC_StreamCreateURL(String url, int offset, int flags, BASS.DOWNLOADPROC proc, Object user);

    public static native int BASS_ALAC_StreamCreateFileUser(int system, int flags, BASS.BASS_FILEPROCS procs, Object user);
}
