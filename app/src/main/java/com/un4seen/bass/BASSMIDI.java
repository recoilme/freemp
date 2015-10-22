/*
	BASSMIDI 2.4 Java class
	Copyright (c) 2006-2013 Un4seen Developments Ltd.

	See the BASSMIDI.CHM file for more detailed documentation
*/

package com.un4seen.bass;

import java.nio.ByteBuffer;

public class BASSMIDI {
    // Additional BASS_SetConfig options
    public static final int BASS_CONFIG_MIDI_COMPACT = 0x10400;
    public static final int BASS_CONFIG_MIDI_VOICES = 0x10401;
    public static final int BASS_CONFIG_MIDI_AUTOFONT = 0x10402;

    // Additional BASS_SetConfigPtr options
    public static final int BASS_CONFIG_MIDI_DEFFONT = 0x10403;

    // Additional sync types
    public static final int BASS_SYNC_MIDI_MARK = 0x10000;
    public static final int BASS_SYNC_MIDI_MARKER = 0x10000;
    public static final int BASS_SYNC_MIDI_CUE = 0x10001;
    public static final int BASS_SYNC_MIDI_LYRIC = 0x10002;
    public static final int BASS_SYNC_MIDI_TEXT = 0x10003;
    public static final int BASS_SYNC_MIDI_EVENT = 0x10004;
    public static final int BASS_SYNC_MIDI_TICK = 0x10005;
    public static final int BASS_SYNC_MIDI_TIMESIG = 0x10006;
    public static final int BASS_SYNC_MIDI_KEYSIG = 0x10007;

    // Additional BASS_MIDI_StreamCreateFile/etc flags
    public static final int BASS_MIDI_DECAYEND = 0x1000;
    public static final int BASS_MIDI_NOFX = 0x2000;
    public static final int BASS_MIDI_DECAYSEEK = 0x4000;
    public static final int BASS_MIDI_NOCROP = 0x8000;
    public static final int BASS_MIDI_SINCINTER = 0x800000;

    // BASS_MIDI_FontInit flags
    public static final int BASS_MIDI_FONT_MEM = 0x10000;
    public static final int BASS_MIDI_FONT_MMAP = 0x20000;
    // BASS_MIDI_StreamSet/GetFonts flag
    public static final int BASS_MIDI_FONT_EX = 0x1000000; // BASS_MIDI_FONTEX (auto-detected)
    // Marker types
    public static final int BASS_MIDI_MARK_MARKER = 0;    // marker
    public static final int BASS_MIDI_MARK_CUE = 1;        // cue point
    public static final int BASS_MIDI_MARK_LYRIC = 2;        // lyric
    public static final int BASS_MIDI_MARK_TEXT = 3;        // text
    public static final int BASS_MIDI_MARK_TIMESIG = 4;    // time signature
    public static final int BASS_MIDI_MARK_KEYSIG = 5;    // key signature
    public static final int BASS_MIDI_MARK_COPY = 6;        // copyright notice
    public static final int BASS_MIDI_MARK_TRACK = 7;        // track name
    public static final int BASS_MIDI_MARK_INST = 8;        // instrument name
    public static final int BASS_MIDI_MARK_TICK = 0x10000; // FLAG: get position in ticks (otherwise bytes)
    // MIDI events
    public static final int MIDI_EVENT_NOTE = 1;
    public static final int MIDI_EVENT_PROGRAM = 2;
    public static final int MIDI_EVENT_CHANPRES = 3;
    public static final int MIDI_EVENT_PITCH = 4;
    public static final int MIDI_EVENT_PITCHRANGE = 5;
    public static final int MIDI_EVENT_DRUMS = 6;
    public static final int MIDI_EVENT_FINETUNE = 7;
    public static final int MIDI_EVENT_COARSETUNE = 8;
    public static final int MIDI_EVENT_MASTERVOL = 9;
    public static final int MIDI_EVENT_BANK = 10;
    public static final int MIDI_EVENT_MODULATION = 11;
    public static final int MIDI_EVENT_VOLUME = 12;
    public static final int MIDI_EVENT_PAN = 13;
    public static final int MIDI_EVENT_EXPRESSION = 14;
    public static final int MIDI_EVENT_SUSTAIN = 15;
    public static final int MIDI_EVENT_SOUNDOFF = 16;
    public static final int MIDI_EVENT_RESET = 17;
    public static final int MIDI_EVENT_NOTESOFF = 18;
    public static final int MIDI_EVENT_PORTAMENTO = 19;
    public static final int MIDI_EVENT_PORTATIME = 20;
    public static final int MIDI_EVENT_PORTANOTE = 21;
    public static final int MIDI_EVENT_MODE = 22;
    public static final int MIDI_EVENT_REVERB = 23;
    public static final int MIDI_EVENT_CHORUS = 24;
    public static final int MIDI_EVENT_CUTOFF = 25;
    public static final int MIDI_EVENT_RESONANCE = 26;
    public static final int MIDI_EVENT_RELEASE = 27;
    public static final int MIDI_EVENT_ATTACK = 28;
    public static final int MIDI_EVENT_REVERB_MACRO = 30;
    public static final int MIDI_EVENT_CHORUS_MACRO = 31;
    public static final int MIDI_EVENT_REVERB_TIME = 32;
    public static final int MIDI_EVENT_REVERB_DELAY = 33;
    public static final int MIDI_EVENT_REVERB_LOCUTOFF = 34;
    public static final int MIDI_EVENT_REVERB_HICUTOFF = 35;
    public static final int MIDI_EVENT_REVERB_LEVEL = 36;
    public static final int MIDI_EVENT_CHORUS_DELAY = 37;
    public static final int MIDI_EVENT_CHORUS_DEPTH = 38;
    public static final int MIDI_EVENT_CHORUS_RATE = 39;
    public static final int MIDI_EVENT_CHORUS_FEEDBACK = 40;
    public static final int MIDI_EVENT_CHORUS_LEVEL = 41;
    public static final int MIDI_EVENT_CHORUS_REVERB = 42;
    public static final int MIDI_EVENT_DRUM_FINETUNE = 50;
    public static final int MIDI_EVENT_DRUM_COARSETUNE = 51;
    public static final int MIDI_EVENT_DRUM_PAN = 52;
    public static final int MIDI_EVENT_DRUM_REVERB = 53;
    public static final int MIDI_EVENT_DRUM_CHORUS = 54;
    public static final int MIDI_EVENT_DRUM_CUTOFF = 55;
    public static final int MIDI_EVENT_DRUM_RESONANCE = 56;
    public static final int MIDI_EVENT_DRUM_LEVEL = 57;
    public static final int MIDI_EVENT_SOFT = 60;
    public static final int MIDI_EVENT_SYSTEM = 61;
    public static final int MIDI_EVENT_TEMPO = 62;
    public static final int MIDI_EVENT_SCALETUNING = 63;
    public static final int MIDI_EVENT_CONTROL = 64;
    public static final int MIDI_EVENT_CHANPRES_VIBRATO = 65;
    public static final int MIDI_EVENT_CHANPRES_PITCH = 66;
    public static final int MIDI_EVENT_CHANPRES_FILTER = 67;
    public static final int MIDI_EVENT_CHANPRES_VOLUME = 68;
    public static final int MIDI_EVENT_MODRANGE = 69;
    public static final int MIDI_EVENT_BANK_LSB = 70;
    public static final int MIDI_EVENT_MIXLEVEL = 0x10000;
    public static final int MIDI_EVENT_TRANSPOSE = 0x10001;
    public static final int MIDI_EVENT_SYSTEMEX = 0x10002;
    public static final int MIDI_EVENT_END = 0;
    public static final int MIDI_EVENT_END_TRACK = 0x10003;
    public static final int MIDI_SYSTEM_DEFAULT = 0;
    public static final int MIDI_SYSTEM_GM1 = 1;
    public static final int MIDI_SYSTEM_GM2 = 2;
    public static final int MIDI_SYSTEM_XG = 3;
    public static final int MIDI_SYSTEM_GS = 4;
    // BASS_MIDI_StreamEvents modes
    public static final int BASS_MIDI_EVENTS_STRUCT = 0; // BASS_MIDI_EVENT structures
    public static final int BASS_MIDI_EVENTS_RAW = 0x10000; // raw MIDI event data
    public static final int BASS_MIDI_EVENTS_SYNC = 0x1000000; // FLAG: trigger event syncs
    // BASS_CHANNELINFO type
    public static final int BASS_CTYPE_STREAM_MIDI = 0x10d00;
    // Additional attributes
    public static final int BASS_ATTRIB_MIDI_PPQN = 0x12000;
    public static final int BASS_ATTRIB_MIDI_CPU = 0x12001;
    public static final int BASS_ATTRIB_MIDI_CHANS = 0x12002;
    public static final int BASS_ATTRIB_MIDI_VOICES = 0x12003;
    public static final int BASS_ATTRIB_MIDI_VOICES_ACTIVE = 0x12004;
    public static final int BASS_ATTRIB_MIDI_TRACK_VOL = 0x12100; // + track #
    // Additional tag type
    public static final int BASS_TAG_MIDI_TRACK = 0x11000;    // + track #, track text : array of null-terminated ANSI strings
    // BASS_ChannelGetLength/GetPosition/SetPosition mode
    public static final int BASS_POS_MIDI_TICK = 2;        // tick position

    static {
        System.loadLibrary("bassmidi");
    }

    public static native int BASS_MIDI_StreamCreate(int channels, int flags, int freq);

    public static native int BASS_MIDI_StreamCreateFile(String file, long offset, long length, int flags, int freq);

    public static native int BASS_MIDI_StreamCreateFile(ByteBuffer file, long offset, long length, int flags, int freq);

    public static native int BASS_MIDI_StreamCreateURL(String url, int offset, int flags, BASS.DOWNLOADPROC proc, Object user, int freq);

    public static native int BASS_MIDI_StreamCreateFileUser(int system, int flags, BASS.BASS_FILEPROCS procs, Object user, int freq);

    public static native int BASS_MIDI_StreamCreateEvents(BASS_MIDI_EVENT[] events, int ppqn, int flags, int freq);

    public static native boolean BASS_MIDI_StreamGetMark(int handle, int type, int index, BASS_MIDI_MARK mark);

    public static native int BASS_MIDI_StreamGetMarks(int handle, int track, int type, BASS_MIDI_MARK[] marks);

    public static native boolean BASS_MIDI_StreamSetFonts(int handle, BASS_MIDI_FONT[] fonts, int count);

    public static native boolean BASS_MIDI_StreamSetFonts(int handle, BASS_MIDI_FONTEX[] fonts, int count);

    public static native int BASS_MIDI_StreamGetFonts(int handle, BASS_MIDI_FONT[] fonts, int count);

    public static native int BASS_MIDI_StreamGetFonts(int handle, BASS_MIDI_FONTEX[] fonts, int count);

    public static native boolean BASS_MIDI_StreamLoadSamples(int handle);

    public static native boolean BASS_MIDI_StreamEvent(int handle, int chan, int event, int param);

    public static native int BASS_MIDI_StreamEvents(int handle, int mode, BASS_MIDI_EVENT[] events, int length);

    public static native int BASS_MIDI_StreamEvents(int handle, int mode, ByteBuffer events, int length);

    public static native int BASS_MIDI_StreamGetEvent(int handle, int chan, int event);

    public static native int BASS_MIDI_StreamGetEvents(int handle, int track, int filter, BASS_MIDI_EVENT[] events);

    public static native int BASS_MIDI_FontInit(String file, int flags);

    public static native int BASS_MIDI_FontInit(ByteBuffer file, int flags);

    public static native int BASS_MIDI_FontInitUser(BASS.BASS_FILEPROCS procs, Object user, int flags);

    public static native boolean BASS_MIDI_FontFree(int handle);

    public static native boolean BASS_MIDI_FontGetInfo(int handle, BASS_MIDI_FONTINFO info);
//	public static native int BASS_MIDI_StreamGetChannel(int handle, int chan);

    public static native boolean BASS_MIDI_FontGetPresets(int handle, int[] presets);

    public static native String BASS_MIDI_FontGetPreset(int handle, int preset, int bank);

    public static native boolean BASS_MIDI_FontLoad(int handle, int preset, int bank);

    public static native boolean BASS_MIDI_FontUnload(int handle, int preset, int bank);

    public static native boolean BASS_MIDI_FontCompact(int handle);

    public static native boolean BASS_MIDI_FontUnpack(int handle, String outfile, int flags);

    public static native boolean BASS_MIDI_FontSetVolume(int handle, float volume);

    public static native float BASS_MIDI_FontGetVolume(int handle);

    public static class BASS_MIDI_FONT {
        public int font;        // soundfont
        public int preset;        // preset number (-1=all)
        public int bank;
    }

    public static class BASS_MIDI_FONTEX {
        public int font;        // soundfont
        public int spreset;        // source preset number
        public int sbank;        // source bank number
        public int dpreset;        // destination preset/program number
        public int dbank;        // destination bank number
        public int dbanklsb;    // destination bank number LSB
    }

    public static class BASS_MIDI_FONTINFO {
        public String name;
        public String copyright;
        public String comment;
        public int presets;        // number of presets/instruments
        public int samsize;        // total size (in bytes) of the sample data
        public int samload;        // amount of sample data currently loaded
        public int samtype;        // sample format (CTYPE) if packed
    }

    public static class BASS_MIDI_MARK {
        public int track;        // track containing marker
        public int pos;            // marker position
        public String text;        // marker text
    }

    public static class BASS_MIDI_EVENT {
        public int event;        // MIDI_EVENT_xxx
        public int param;
        public int chan;
        public int tick;        // event position (ticks)
        public int pos;            // event position (bytes)
    }

    public static class BASS_MIDI_DEVICEINFO {
        public String name;    // description
        public int id;
        public int flags;
    }
}
