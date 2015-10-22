package com.un4seen.bass;

import java.nio.ByteBuffer;

public class BASS_APE {
    // BASS_CHANNELINFO type
    public static final int BASS_CTYPE_STREAM_APE = 0x10700;

    static {
        System.loadLibrary("bass_ape");
    }

    public static native int BASS_APE_StreamCreateFile(String file, long offset, long length, int flags);

    public static native int BASS_APE_StreamCreateFile(ByteBuffer file, long offset, long length, int flags);

    public static native int BASS_APE_StreamCreateFileUser(int system, int flags, BASS.BASS_FILEPROCS procs, Object user);
}
