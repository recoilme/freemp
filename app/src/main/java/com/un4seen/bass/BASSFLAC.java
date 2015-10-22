/*
	BASSFLAC 2.4 Java class
	Copyright (c) 2004-2011 Un4seen Developments Ltd.

	See the BASSFLAC.CHM file for more detailed documentation
*/

package com.un4seen.bass;

import java.nio.ByteBuffer;

public class BASSFLAC {
    // BASS_CHANNELINFO type
    public static final int BASS_CTYPE_STREAM_FLAC = 0x10900;
    public static final int BASS_CTYPE_STREAM_FLAC_OGG = 0x10901;

    // Additional tag types
    public static final int BASS_TAG_FLAC_CUE = 12;        // cuesheet : TAG_FLAC_CUE structure
    public static final int BASS_TAG_FLAC_PICTURE = 0x12000;    // + index #, picture : TAG_FLAC_PICTURE structure
    // TAG_FLAC_CUE_TRACK flags
    public static final int TAG_FLAC_CUE_TRACK_DATA = 1; // data track
    public static final int TAG_FLAC_CUE_TRACK_PRE = 2; // pre-emphasis

    static {
        System.loadLibrary("bassflac");
    }

    public static native int BASS_FLAC_StreamCreateFile(String file, long offset, long length, int flags);

    public static native int BASS_FLAC_StreamCreateFile(ByteBuffer file, long offset, long length, int flags);

    public static native int BASS_FLAC_StreamCreateURL(String url, int offset, int flags, BASS.DOWNLOADPROC proc, Object user);

    public static native int BASS_FLAC_StreamCreateFileUser(int system, int flags, BASS.BASS_FILEPROCS procs, Object user);

    public static class TAG_FLAC_PICTURE {
        public int apic;            // ID3v2 "APIC" picture type
        public String mime;    // mime type
        public String desc;    // description
        public int width;
        public int height;
        public int depth;
        public int colors;
        public int length;        // data length
        public ByteBuffer data;
    }

    public static class TAG_FLAC_CUE_TRACK_INDEX {
        public long offset;            // index offset relative to track offset (samples)
        public int number;            // index number
    }

    public static class TAG_FLAC_CUE_TRACK {
        public long offset;            // track offset (samples)
        public int number;            // track number
        public String isrc;        // ISRC
        public int flags;
        public int nindexes;            // number of indexes
        public TAG_FLAC_CUE_TRACK_INDEX[] indexes; // the indexes
    }

    public static class TAG_FLAC_CUE {
        public String catalog;    // media catalog number
        public int leadin;            // lead-in (samples)
        public boolean iscd;                // a CD?
        public int ntracks;            // number of tracks
        public TAG_FLAC_CUE_TRACK[] tracks; // the tracks
    }
}
