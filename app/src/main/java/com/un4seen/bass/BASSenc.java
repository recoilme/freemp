/*
	BASSenc 2.4 Java class
	Copyright (c) 2003-2014 Un4seen Developments Ltd.

	See the BASSENC.CHM file for more detailed documentation
*/

package com.un4seen.bass;

import java.nio.ByteBuffer;

public class BASSenc {
    // Additional error codes returned by BASS_ErrorGetCode
    public static final int BASS_ERROR_CAST_DENIED = 2100;    // access denied (invalid password)

    // Additional BASS_SetConfig options
    public static final int BASS_CONFIG_ENCODE_PRIORITY = 0x10300;
    public static final int BASS_CONFIG_ENCODE_QUEUE = 0x10301;
    public static final int BASS_CONFIG_ENCODE_CAST_TIMEOUT = 0x10310;

    // Additional BASS_SetConfigPtr options
    public static final int BASS_CONFIG_ENCODE_CAST_PROXY = 0x10311;

    // BASS_Encode_Start flags
    public static final int BASS_ENCODE_NOHEAD = 1;        // don't send a WAV header to the encoder
    public static final int BASS_ENCODE_FP_8BIT = 2;        // convert floating-point sample data to 8-bit integer
    public static final int BASS_ENCODE_FP_16BIT = 4;        // convert floating-point sample data to 16-bit integer
    public static final int BASS_ENCODE_FP_24BIT = 6;        // convert floating-point sample data to 24-bit integer
    public static final int BASS_ENCODE_FP_32BIT = 8;        // convert floating-point sample data to 32-bit integer
    public static final int BASS_ENCODE_BIGEND = 16;        // big-endian sample data
    public static final int BASS_ENCODE_PAUSE = 32;        // start encording paused
    public static final int BASS_ENCODE_PCM = 64;        // write PCM sample data (no encoder)
    public static final int BASS_ENCODE_RF64 = 128;        // send an RF64 header
    public static final int BASS_ENCODE_QUEUE = 0x200;    // queue data to feed encoder asynchronously
    public static final int BASS_ENCODE_WFEXT = 0x400;    // WAVEFORMATEXTENSIBLE "fmt" chunk
    public static final int BASS_ENCODE_CAST_NOLIMIT = 0x1000;    // don't limit casting data rate
    public static final int BASS_ENCODE_LIMIT = 0x2000;    // limit data rate to real-time
    public static final int BASS_ENCODE_AIFF = 0x4000;    // send an AIFF header rather than WAV
    public static final int BASS_ENCODE_AUTOFREE = 0x40000; // free the encoder when the channel is freed

    // BASS_Encode_GetCount counts
    public static final int BASS_ENCODE_COUNT_IN = 0;    // sent to encoder
    public static final int BASS_ENCODE_COUNT_OUT = 1;    // received from encoder
    public static final int BASS_ENCODE_COUNT_CAST = 2;    // sent to cast server
    public static final int BASS_ENCODE_COUNT_QUEUE = 3;    // queued
    public static final int BASS_ENCODE_COUNT_QUEUE_LIMIT = 4;    // queue limit
    public static final int BASS_ENCODE_COUNT_QUEUE_FAIL = 5;    // failed to queue

    // BASS_Encode_CastInit content MIME types
    public static final String BASS_ENCODE_TYPE_MP3 = "audio/mpeg";
    public static final String BASS_ENCODE_TYPE_OGG = "application/ogg";
    public static final String BASS_ENCODE_TYPE_AAC = "audio/aacp";

    // BASS_Encode_CastGetStats types
    public static final int BASS_ENCODE_STATS_SHOUT = 0;    // Shoutcast stats
    public static final int BASS_ENCODE_STATS_ICE = 1;    // Icecast mount-point stats
    public static final int BASS_ENCODE_STATS_ICESERV = 2;    // Icecast server stats
    // Encoder notifications
    public static final int BASS_ENCODE_NOTIFY_ENCODER = 1;    // encoder died
    public static final int BASS_ENCODE_NOTIFY_CAST = 2;    // cast server connection died
    public static final int BASS_ENCODE_NOTIFY_CAST_TIMEOUT = 0x10000; // cast timeout
    public static final int BASS_ENCODE_NOTIFY_QUEUE_FULL = 0x10001;    // queue is out of space
    public static final int BASS_ENCODE_NOTIFY_FREE = 0x10002;    // encoder has been freed
    // BASS_Encode_ServerInit flags
    public static final int BASS_ENCODE_SERVER_NOHTTP = 1;    // no HTTP headers

    static {
        System.loadLibrary("bassenc");
    }

    public static native int BASS_Encode_GetVersion();

    public static native int BASS_Encode_Start(int handle, String cmdline, int flags, ENCODEPROC proc, Object user);

    public static native int BASS_Encode_StartLimit(int handle, String cmdline, int flags, ENCODEPROC proc, Object user, int limit);

    public static native boolean BASS_Encode_AddChunk(int handle, String id, ByteBuffer buffer, int length);

    public static native int BASS_Encode_IsActive(int handle);

    public static native boolean BASS_Encode_Stop(int handle);

    public static native boolean BASS_Encode_StopEx(int handle, boolean queue);

    public static native boolean BASS_Encode_SetPaused(int handle, boolean paused);

    public static native boolean BASS_Encode_Write(int handle, ByteBuffer buffer, int length);

    public static native boolean BASS_Encode_SetNotify(int handle, ENCODENOTIFYPROC proc, Object user);

    public static native long BASS_Encode_GetCount(int handle, int count);

    public static native boolean BASS_Encode_SetChannel(int handle, int channel);

    public static native int BASS_Encode_GetChannel(int handle);

    public interface ENCODEPROC {
        void ENCODEPROC(int handle, int channel, ByteBuffer buffer, int length, Object user);
        /* Encoding callback function.
        handle : The encoder
		channel: The channel handle
		buffer : Buffer containing the encoded data
		length : Number of bytes
		user   : The 'user' parameter value given when calling BASS_Encode_Start */
    }

    public interface ENCODECLIENTPROC {
        boolean ENCODECLIENTPROC(int handle, boolean connect, String client, String headers, Object user);
        /* Client connection notification callback function.
		handle : The encoder
		connect: true/false=client is connecting/disconnecting
		client : The client's address (xxx.xxx.xxx.xxx:port)
		headers: Request headers (optionally response headers on return)
		user   : The 'user' parameter value given when calling BASS_Encode_ServerInit
		RETURN : true/false=accept/reject connection (ignored if connect=false) */
    }

    public interface ENCODENOTIFYPROC {
        void ENCODENOTIFYPROC(int handle, int status, Object user);
        /* Encoder death notification callback function.
		handle : The encoder
		status : Notification (BASS_ENCODE_NOTIFY_xxx)
		user   : The 'user' parameter value given when calling BASS_Encode_SetNotify */
    }
}
