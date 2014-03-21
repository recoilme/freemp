/*
 * vim: set sta sw=4 et:
 *
 * Copyright (C) 2012 Liu DongMiao <thom@piebridge.me>
 *
 * This program is free software. It comes without any warranty, to
 * the extent permitted by applicable law. You can redistribute it
 * and/or modify it under the terms of the Do What The Fuck You Want
 * To Public License, Version 2, as published by Sam Hocevar. See
 * http://sam.zoy.org/wtfpl/COPYING for more details.
 *
 */

package me.piebridge.curl;

public class Curl
{
    public static final int CURLINFO_STRING   = 0x100000;
    public static final int CURLINFO_LONG     = 0x200000;
    public static final int CURLINFO_DOUBLE   = 0x300000;
    public static final int CURLINFO_SLIST    = 0x400000;
    public static final int CURLINFO_EFFECTIVE_URL    = CURLINFO_STRING + 1;
    public static final int CURLINFO_RESPONSE_CODE    = CURLINFO_LONG   + 2;
    public static final int CURLINFO_TOTAL_TIME       = CURLINFO_DOUBLE + 3;
    public static final int CURLINFO_NAMELOOKUP_TIME  = CURLINFO_DOUBLE + 4;
    public static final int CURLINFO_CONNECT_TIME     = CURLINFO_DOUBLE + 5;
    public static final int CURLINFO_PRETRANSFER_TIME = CURLINFO_DOUBLE + 6;
    public static final int CURLINFO_SIZE_UPLOAD      = CURLINFO_DOUBLE + 7;
    public static final int CURLINFO_SIZE_DOWNLOAD    = CURLINFO_DOUBLE + 8;
    public static final int CURLINFO_SPEED_DOWNLOAD   = CURLINFO_DOUBLE + 9;
    public static final int CURLINFO_SPEED_UPLOAD     = CURLINFO_DOUBLE + 10;
    public static final int CURLINFO_HEADER_SIZE      = CURLINFO_LONG   + 11;
    public static final int CURLINFO_REQUEST_SIZE     = CURLINFO_LONG   + 12;
    public static final int CURLINFO_SSL_VERIFYRESULT = CURLINFO_LONG   + 13;
    public static final int CURLINFO_FILETIME         = CURLINFO_LONG   + 14;
    public static final int CURLINFO_CONTENT_LENGTH_DOWNLOAD   = CURLINFO_DOUBLE + 15;
    public static final int CURLINFO_CONTENT_LENGTH_UPLOAD     = CURLINFO_DOUBLE + 16;
    public static final int CURLINFO_STARTTRANSFER_TIME = CURLINFO_DOUBLE + 17;
    public static final int CURLINFO_CONTENT_TYPE     = CURLINFO_STRING + 18;
    public static final int CURLINFO_REDIRECT_TIME    = CURLINFO_DOUBLE + 19;
    public static final int CURLINFO_REDIRECT_COUNT   = CURLINFO_LONG   + 20;
    public static final int CURLINFO_PRIVATE          = CURLINFO_STRING + 21;
    public static final int CURLINFO_HTTP_CONNECTCODE = CURLINFO_LONG   + 22;
    public static final int CURLINFO_HTTPAUTH_AVAIL   = CURLINFO_LONG   + 23;
    public static final int CURLINFO_PROXYAUTH_AVAIL  = CURLINFO_LONG   + 24;
    public static final int CURLINFO_OS_ERRNO         = CURLINFO_LONG   + 25;
    public static final int CURLINFO_NUM_CONNECTS     = CURLINFO_LONG   + 26;
    public static final int CURLINFO_SSL_ENGINES      = CURLINFO_SLIST  + 27;
    public static final int CURLINFO_COOKIELIST       = CURLINFO_SLIST  + 28;
    public static final int CURLINFO_LASTSOCKET       = CURLINFO_LONG   + 29;
    public static final int CURLINFO_FTP_ENTRY_PATH   = CURLINFO_STRING + 30;
    public static final int CURLINFO_REDIRECT_URL     = CURLINFO_STRING + 31;
    public static final int CURLINFO_PRIMARY_IP       = CURLINFO_STRING + 32;
    public static final int CURLINFO_APPCONNECT_TIME  = CURLINFO_DOUBLE + 33;
    public static final int CURLINFO_CERTINFO         = CURLINFO_SLIST  + 34;
    public static final int CURLINFO_CONDITION_UNMET  = CURLINFO_LONG   + 35;
    public static final int CURLINFO_RTSP_SESSION_ID  = CURLINFO_STRING + 36;
    public static final int CURLINFO_RTSP_CLIENT_CSEQ = CURLINFO_LONG   + 37;
    public static final int CURLINFO_RTSP_SERVER_CSEQ = CURLINFO_LONG   + 38;
    public static final int CURLINFO_RTSP_CSEQ_RECV   = CURLINFO_LONG   + 39;
    public static final int CURLINFO_PRIMARY_PORT     = CURLINFO_LONG   + 40;
    public static final int CURLINFO_LOCAL_IP         = CURLINFO_STRING + 41;
    public static final int CURLINFO_LOCAL_PORT       = CURLINFO_LONG   + 42;

    public static final int LONG          = 0;
    public static final int OBJECTPOINT   = 10000;
    public static final int FUNCTIONPOINT = 20000;
    public static final int OFF_T         = 30000;
    public static final int CURLOPT_FILE = OBJECTPOINT + 1;
    public static final int CURLOPT_URL =  OBJECTPOINT + 2;
    public static final int CURLOPT_PORT = LONG + 3;
    public static final int CURLOPT_PROXY = OBJECTPOINT + 4;
    public static final int CURLOPT_USERPWD = OBJECTPOINT + 5;
    public static final int CURLOPT_PROXYUSERPWD = OBJECTPOINT + 6;
    public static final int CURLOPT_RANGE = OBJECTPOINT + 7;
    public static final int CURLOPT_INFILE = OBJECTPOINT + 9;
    public static final int CURLOPT_ERRORBUFFER = OBJECTPOINT + 10;
    public static final int CURLOPT_WRITEFUNCTION = FUNCTIONPOINT + 11;
    public static final int CURLOPT_READFUNCTION = FUNCTIONPOINT + 12;
    public static final int CURLOPT_TIMEOUT = LONG + 13;
    public static final int CURLOPT_INFILESIZE = LONG + 14;
    public static final int CURLOPT_POSTFIELDS = OBJECTPOINT + 15;
    public static final int CURLOPT_REFERER = OBJECTPOINT + 16;
    public static final int CURLOPT_FTPPORT = OBJECTPOINT + 17;
    public static final int CURLOPT_USERAGENT = OBJECTPOINT + 18;
    public static final int CURLOPT_LOW_SPEED_LIMIT = LONG + 19;
    public static final int CURLOPT_LOW_SPEED_TIME = LONG + 20;
    public static final int CURLOPT_RESUME_FROM = LONG + 21;
    public static final int CURLOPT_COOKIE = OBJECTPOINT + 22;
    public static final int CURLOPT_HTTPHEADER = OBJECTPOINT + 23;
    public static final int CURLOPT_HTTPPOST = OBJECTPOINT + 24;
    public static final int CURLOPT_SSLCERT = OBJECTPOINT + 25;
    public static final int CURLOPT_KEYPASSWD = OBJECTPOINT + 26;
    public static final int CURLOPT_CRLF = LONG + 27;
    public static final int CURLOPT_QUOTE = OBJECTPOINT + 28;
    public static final int CURLOPT_WRITEHEADER = OBJECTPOINT + 29;
    public static final int CURLOPT_COOKIEFILE = OBJECTPOINT + 31;
    public static final int CURLOPT_SSLVERSION = LONG + 32;
    public static final int CURLOPT_TIMECONDITION = LONG + 33;
    public static final int CURLOPT_TIMEVALUE = LONG + 34;
    public static final int CURLOPT_CUSTOMREQUEST = OBJECTPOINT + 36;
    public static final int CURLOPT_STDERR = OBJECTPOINT + 37;
    public static final int CURLOPT_POSTQUOTE = OBJECTPOINT + 39;
    public static final int CURLOPT_WRITEINFO = OBJECTPOINT + 40;
    public static final int CURLOPT_VERBOSE = LONG + 41;
    public static final int CURLOPT_HEADER = LONG + 42;
    public static final int CURLOPT_NOPROGRESS = LONG + 43;
    public static final int CURLOPT_NOBODY = LONG + 44;
    public static final int CURLOPT_FAILONERROR = LONG + 45;
    public static final int CURLOPT_UPLOAD = LONG + 46;
    public static final int CURLOPT_POST = LONG + 47;
    public static final int CURLOPT_DIRLISTONLY = LONG + 48;
    public static final int CURLOPT_APPEND = LONG + 50;
    public static final int CURLOPT_NETRC = LONG + 51;
    public static final int CURLOPT_FOLLOWLOCATION = LONG + 52;
    public static final int CURLOPT_TRANSFERTEXT = LONG + 53;
    public static final int CURLOPT_PUT = LONG + 54;
    public static final int CURLOPT_PROGRESSFUNCTION = FUNCTIONPOINT + 56;
    public static final int CURLOPT_PROGRESSDATA = OBJECTPOINT + 57;
    public static final int CURLOPT_AUTOREFERER = LONG + 58;
    public static final int CURLOPT_PROXYPORT = LONG + 59;
    public static final int CURLOPT_POSTFIELDSIZE = LONG + 60;
    public static final int CURLOPT_HTTPPROXYTUNNEL = LONG + 61;
    public static final int CURLOPT_INTERFACE = OBJECTPOINT + 62;
    public static final int CURLOPT_KRBLEVEL = OBJECTPOINT + 63;
    public static final int CURLOPT_SSL_VERIFYPEER = LONG + 64;
    public static final int CURLOPT_CAINFO = OBJECTPOINT + 65;
    public static final int CURLOPT_MAXREDIRS = LONG + 68;
    public static final int CURLOPT_FILETIME = LONG + 69;
    public static final int CURLOPT_TELNETOPTIONS = OBJECTPOINT + 70;
    public static final int CURLOPT_MAXCONNECTS = LONG + 71;
    public static final int CURLOPT_CLOSEPOLICY = LONG + 72;
    public static final int CURLOPT_FRESH_CONNECT = LONG + 74;
    public static final int CURLOPT_FORBID_REUSE = LONG + 75;
    public static final int CURLOPT_RANDOM_FILE = OBJECTPOINT + 76;
    public static final int CURLOPT_EGDSOCKET = OBJECTPOINT + 77;
    public static final int CURLOPT_CONNECTTIMEOUT = LONG + 78;
    public static final int CURLOPT_HEADERFUNCTION = FUNCTIONPOINT + 79;
    public static final int CURLOPT_HTTPGET = LONG + 80;
    public static final int CURLOPT_SSL_VERIFYHOST = LONG + 81;
    public static final int CURLOPT_COOKIEJAR = OBJECTPOINT + 82;
    public static final int CURLOPT_SSL_CIPHER_LIST = OBJECTPOINT + 83;
    public static final int CURLOPT_HTTP_VERSION = LONG + 84;
    public static final int CURLOPT_FTP_USE_EPSV = LONG + 85;
    public static final int CURLOPT_SSLCERTTYPE = OBJECTPOINT + 86;
    public static final int CURLOPT_SSLKEY = OBJECTPOINT + 87;
    public static final int CURLOPT_SSLKEYTYPE = OBJECTPOINT + 88;
    public static final int CURLOPT_SSLENGINE = OBJECTPOINT + 89;
    public static final int CURLOPT_SSLENGINE_DEFAULT = LONG + 90;
    public static final int CURLOPT_DNS_USE_GLOBAL_CACHE = LONG + 91;
    public static final int CURLOPT_DNS_CACHE_TIMEOUT = LONG + 92;
    public static final int CURLOPT_PREQUOTE = OBJECTPOINT + 93;
    public static final int CURLOPT_DEBUGFUNCTION = FUNCTIONPOINT + 94;
    public static final int CURLOPT_DEBUGDATA = OBJECTPOINT + 95;
    public static final int CURLOPT_COOKIESESSION = LONG + 96;
    public static final int CURLOPT_CAPATH = OBJECTPOINT + 97;
    public static final int CURLOPT_BUFFERSIZE = LONG + 98;
    public static final int CURLOPT_NOSIGNAL = LONG + 99;
    public static final int CURLOPT_SHARE = OBJECTPOINT + 100;
    public static final int CURLOPT_PROXYTYPE = LONG + 101;
    public static final int CURLOPT_ACCEPT_ENCODING = OBJECTPOINT + 102;
    public static final int CURLOPT_PRIVATE = OBJECTPOINT + 103;
    public static final int CURLOPT_HTTP200ALIASES = OBJECTPOINT + 104;
    public static final int CURLOPT_UNRESTRICTED_AUTH = LONG + 105;
    public static final int CURLOPT_FTP_USE_EPRT = LONG + 106;
    public static final int CURLOPT_HTTPAUTH = LONG + 107;
    public static final int CURLOPT_SSL_CTX_FUNCTION = FUNCTIONPOINT + 108;
    public static final int CURLOPT_SSL_CTX_DATA = OBJECTPOINT + 109;
    public static final int CURLOPT_FTP_CREATE_MISSING_DIRS = LONG + 110;
    public static final int CURLOPT_PROXYAUTH = LONG + 111;
    public static final int CURLOPT_FTP_RESPONSE_TIMEOUT = LONG + 112;
    public static final int CURLOPT_IPRESOLVE = LONG + 113;
    public static final int CURLOPT_MAXFILESIZE = LONG + 114;
    public static final int CURLOPT_INFILESIZE_LARGE = OFF_T + 115;
    public static final int CURLOPT_RESUME_FROM_LARGE = OFF_T + 116;
    public static final int CURLOPT_MAXFILESIZE_LARGE = OFF_T + 117;
    public static final int CURLOPT_NETRC_FILE = OBJECTPOINT + 118;
    public static final int CURLOPT_USE_SSL = LONG + 119;
    public static final int CURLOPT_POSTFIELDSIZE_LARGE = OFF_T + 120;
    public static final int CURLOPT_TCP_NODELAY = LONG + 121;
    public static final int CURLOPT_FTPSSLAUTH = LONG + 129;
    public static final int CURLOPT_IOCTLFUNCTION = FUNCTIONPOINT + 130;
    public static final int CURLOPT_IOCTLDATA = OBJECTPOINT + 131;
    public static final int CURLOPT_FTP_ACCOUNT = OBJECTPOINT + 134;
    public static final int CURLOPT_COOKIELIST = OBJECTPOINT + 135;
    public static final int CURLOPT_IGNORE_CONTENT_LENGTH = LONG + 136;
    public static final int CURLOPT_FTP_SKIP_PASV_IP = LONG + 137;
    public static final int CURLOPT_FTP_FILEMETHOD = LONG + 138;
    public static final int CURLOPT_LOCALPORT = LONG + 139;
    public static final int CURLOPT_LOCALPORTRANGE = LONG + 140;
    public static final int CURLOPT_CONNECT_ONLY = LONG + 141;
    public static final int CURLOPT_CONV_FROM_NETWORK_FUNCTION = FUNCTIONPOINT + 142;
    public static final int CURLOPT_CONV_TO_NETWORK_FUNCTION = FUNCTIONPOINT + 143;
    public static final int CURLOPT_CONV_FROM_UTF8_FUNCTION = FUNCTIONPOINT + 144;
    public static final int CURLOPT_MAX_SEND_SPEED_LARGE = OFF_T + 145;
    public static final int CURLOPT_MAX_RECV_SPEED_LARGE = OFF_T + 146;
    public static final int CURLOPT_FTP_ALTERNATIVE_TO_USER = OBJECTPOINT + 147;
    public static final int CURLOPT_SOCKOPTFUNCTION = FUNCTIONPOINT + 148;
    public static final int CURLOPT_SOCKOPTDATA = OBJECTPOINT + 149;
    public static final int CURLOPT_SSL_SESSIONID_CACHE = LONG + 150;
    public static final int CURLOPT_SSH_AUTH_TYPES = LONG + 151;
    public static final int CURLOPT_SSH_PUBLIC_KEYFILE = OBJECTPOINT + 152;
    public static final int CURLOPT_SSH_PRIVATE_KEYFILE = OBJECTPOINT + 153;
    public static final int CURLOPT_FTP_SSL_CCC = LONG + 154;
    public static final int CURLOPT_TIMEOUT_MS = LONG + 155;
    public static final int CURLOPT_CONNECTTIMEOUT_MS = LONG + 156;
    public static final int CURLOPT_HTTP_TRANSFER_DECODING = LONG + 157;
    public static final int CURLOPT_HTTP_CONTENT_DECODING = LONG + 158;
    public static final int CURLOPT_NEW_FILE_PERMS = LONG + 159;
    public static final int CURLOPT_NEW_DIRECTORY_PERMS = LONG + 160;
    public static final int CURLOPT_POSTREDIR = LONG + 161;
    public static final int CURLOPT_SSH_HOST_PUBLIC_KEY_MD5 = OBJECTPOINT + 162;
    public static final int CURLOPT_OPENSOCKETFUNCTION = FUNCTIONPOINT + 163;
    public static final int CURLOPT_OPENSOCKETDATA = OBJECTPOINT + 164;
    public static final int CURLOPT_COPYPOSTFIELDS = OBJECTPOINT + 165;
    public static final int CURLOPT_PROXY_TRANSFER_MODE = LONG + 166;
    public static final int CURLOPT_SEEKFUNCTION = FUNCTIONPOINT + 167;
    public static final int CURLOPT_SEEKDATA = OBJECTPOINT + 168;
    public static final int CURLOPT_CRLFILE = OBJECTPOINT + 169;
    public static final int CURLOPT_ISSUERCERT = OBJECTPOINT + 170;
    public static final int CURLOPT_ADDRESS_SCOPE = LONG + 171;
    public static final int CURLOPT_CERTINFO = LONG + 172;
    public static final int CURLOPT_USERNAME = OBJECTPOINT + 173;
    public static final int CURLOPT_PASSWORD = OBJECTPOINT + 174;
    public static final int CURLOPT_PROXYUSERNAME = OBJECTPOINT + 175;
    public static final int CURLOPT_PROXYPASSWORD = OBJECTPOINT + 176;
    public static final int CURLOPT_NOPROXY = OBJECTPOINT + 177;
    public static final int CURLOPT_TFTP_BLKSIZE = LONG + 178;
    public static final int CURLOPT_SOCKS5_GSSAPI_SERVICE = OBJECTPOINT + 179;
    public static final int CURLOPT_SOCKS5_GSSAPI_NEC = LONG + 180;
    public static final int CURLOPT_PROTOCOLS = LONG + 181;
    public static final int CURLOPT_REDIR_PROTOCOLS = LONG + 182;
    public static final int CURLOPT_SSH_KNOWNHOSTS = OBJECTPOINT + 183;
    public static final int CURLOPT_SSH_KEYFUNCTION = FUNCTIONPOINT + 184;
    public static final int CURLOPT_SSH_KEYDATA = OBJECTPOINT + 185;
    public static final int CURLOPT_MAIL_FROM = OBJECTPOINT + 186;
    public static final int CURLOPT_MAIL_RCPT = OBJECTPOINT + 187;
    public static final int CURLOPT_FTP_USE_PRET = LONG + 188;
    public static final int CURLOPT_RTSP_REQUEST = LONG + 189;
    public static final int CURLOPT_RTSP_SESSION_ID = OBJECTPOINT + 190;
    public static final int CURLOPT_RTSP_STREAM_URI = OBJECTPOINT + 191;
    public static final int CURLOPT_RTSP_TRANSPORT = OBJECTPOINT + 192;
    public static final int CURLOPT_RTSP_CLIENT_CSEQ = LONG + 193;
    public static final int CURLOPT_RTSP_SERVER_CSEQ = LONG + 194;
    public static final int CURLOPT_INTERLEAVEDATA = OBJECTPOINT + 195;
    public static final int CURLOPT_INTERLEAVEFUNCTION = FUNCTIONPOINT + 196;
    public static final int CURLOPT_WILDCARDMATCH = LONG + 197;
    public static final int CURLOPT_CHUNK_BGN_FUNCTION = FUNCTIONPOINT + 198;
    public static final int CURLOPT_CHUNK_END_FUNCTION = FUNCTIONPOINT + 199;
    public static final int CURLOPT_FNMATCH_FUNCTION = FUNCTIONPOINT + 200;
    public static final int CURLOPT_CHUNK_DATA = OBJECTPOINT + 201;
    public static final int CURLOPT_FNMATCH_DATA = OBJECTPOINT + 202;
    public static final int CURLOPT_RESOLVE = OBJECTPOINT + 203;
    public static final int CURLOPT_TLSAUTH_USERNAME = OBJECTPOINT + 204;
    public static final int CURLOPT_TLSAUTH_PASSWORD = OBJECTPOINT + 205;
    public static final int CURLOPT_TLSAUTH_TYPE = OBJECTPOINT + 206;
    public static final int CURLOPT_TRANSFER_ENCODING = LONG + 207;
    public static final int CURLOPT_CLOSESOCKETFUNCTION = FUNCTIONPOINT + 208;
    public static final int CURLOPT_CLOSESOCKETDATA = OBJECTPOINT + 209;
    public static final int CURLOPT_GSSAPI_DELEGATION = LONG + 210;
    public static final int CURLOPT_DNS_SERVERS = OBJECTPOINT + 211;
    public static final int CURLOPT_ACCEPTTIMEOUT_MS = LONG + 212;
    public static final int CURLOPT_TCP_KEEPALIVE = LONG + 213;
    public static final int CURLOPT_TCP_KEEPIDLE = LONG + 214;
    public static final int CURLOPT_TCP_KEEPINTVL = LONG + 215;
    public static final int CURLOPT_SSL_OPTIONS = LONG + 216;
    public static final int CURLOPT_MAIL_AUTH = OBJECTPOINT + 217;
    public static final int CURLOPT_WRITEDATA = CURLOPT_FILE;
    public static final int CURLOPT_READDATA = CURLOPT_INFILE;

    public static final int CURL_IPRESOLVE_WHATEVER = 0;
    public static final int CURL_IPRESOLVE_V4 = 1;
    public static final int CURL_IPRESOLVE_V6 = 2;

    public static native int curl_init();
    public static native int curl_errno();
    public static native String curl_error();
    // curl_easy_setopt with long, unsigned long, curl_off_t
    public static native boolean curl_setopt(int curl, int option, long value);
    // curl_easy_setopt with char *
    public static native boolean curl_setopt(int curl, int option, String value);
    // curl_easy_setopt with curl_slit
    public static native boolean curl_setopt(int curl, int option, String[] value);
    // curl_easy_setopt with void *
    public static native boolean curl_setopt(int curl, int option, byte[] value);
    // curl_easy_setopt with callback, Object is a class implements callback
    public static native boolean curl_setopt(int curl, int option, Object value);
    public static native boolean curl_perform(int curl);
    // curl_easy_getinfo with long
    public static native long curl_getinfo_long(int curl, int info);
    // curl_easy_getinfo with double
    public static native double curl_getinfo_double(int curl, int info);
    // curl_easy_getinfo with curl_slist
    public static native String[] curl_getinfo_list(int curl, int info);
    // curl_easy_getinfo with certinfo, certinfo is set of curl_slist
    // Object[] is equal String[][]
    public static native Object[] curl_getinfo_certinfo(int curl, int info);
    // curl_easy_getinfo with string, including long and double
    public static native String curl_getinfo(int curl, int info);
    public static native void curl_cleanup(int curl);

    /*
     * CURLOPT_WRITEFUNCTION
     * CURLOPT_HEADERFUNCTION
     * size_t function(void *ptr, size_t size, size_t nmemb, void *userdata);
     */
    public interface Write
    {
        public int callback(byte[] ptr);
    }

    /*
     * CURLOPT_READFUNCTION
     * size_t function(void *ptr, size_t size, size_t nmemb, void *userdata);
     */
    public interface Read
    {
        public int callback(byte[] ptr);
    }

    /*
     * CURLOPT_DEBUGFUNCTION
     * int curl_debug_callback(CURL *, curl_infotype, char *, size_t, void *);
     */
    public interface Debug
    {
        public int callback(int type, byte[] ptr);

        public final int CURLINFO_TEXT = 0;
        public final int CURLINFO_HEADER_IN = 1;
        public final int CURLINFO_HEADER_OUT = 2;
        public final int CURLINFO_DATA_IN = 3;
        public final int CURLINFO_DATA_OUT = 4;
        public final int CURLINFO_SSL_DATA_IN = 5;
        public final int CURLINFO_SSL_DATA_OUT = 6;
        public final String[] CURLINFO = {
            "TEXT",
            "HEADER_IN",
            "HEADER_OUT",
            "DATA_IN",
            "DATA_OUT",
            "SSL_DATA_IN",
            "SSL_DATA_OUT",
            "END",
        };
    }

    /*
     * CURLOPT_PROGRESSFUNCTION
     * typedef int (*curl_progress_callback)(void *clientp, double dltotal, double dlnow, double ultotal, double ulnow);
     */
    public interface Progress
    {
        public int callback(double dltotal, double dlnow, double ultotal, double ulnow);
    }

    static {
        try {
            System.loadLibrary("curl");
        } catch (UnsatisfiedLinkError e) {
            throw new UnsatisfiedLinkError();
        }
    }

}
