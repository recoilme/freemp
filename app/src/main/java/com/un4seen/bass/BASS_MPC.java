package com.un4seen.bass;

import java.nio.ByteBuffer;

public class BASS_MPC {
    // BASS_CHANNELINFO type
    public static final int BASS_CTYPE_STREAM_MPC = 0x10a00;

    static {
        System.loadLibrary("bass_mpc");
    }

    public static native int BASS_MPC_StreamCreateFile(String file, long offset, long length, int flags);

    public static native int BASS_MPC_StreamCreateFile(ByteBuffer file, long offset, long length, int flags);

    public static native int BASS_MPC_StreamCreateURL(String url, int offset, int flags, BASS.DOWNLOADPROC proc, Object user);

    public static native int BASS_MPC_StreamCreateFileUser(int system, int flags, BASS.BASS_FILEPROCS procs, Object user);
}
