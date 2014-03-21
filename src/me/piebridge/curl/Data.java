package me.piebridge.curl;

import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Created by recoilme on 04/02/14.
 */
public class Data extends Curl {
    private String data = "";
    private byte[] binaryData;
    private ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

    public ByteArrayOutputStream getOutputStream() {
        return outputStream;
    }
    public byte[] getBinaryData(){
        return binaryData;
    }
    public String getData()
    {
        return data;
    }
    public String getURL(String url)
    {
        int curl = curl_init();

        if (0 == curl) {
            return "curl_init failed";
        }

        curl_setopt(curl, CURLOPT_URL, url);
        curl_setopt(curl, CURLOPT_VERBOSE, 1);

        // CURLOPT_WRITEFUNCTION will have header too if CURLOPT_HEADER is true
        // curl_setopt(curl, CURLOPT_HEADER, 1);

        // or set CURLOPT_WRITEHEADER to file path
        curl_setopt(curl, CURLOPT_HEADERFUNCTION, new Curl.Write() {
            public int callback(byte[] ptr) {
                if (ptr == null) {
                    Log.d("CURL-J-HEADER", "write null");
                    return -1;
                }
                data += new String(ptr);
                Log.d("CURL-J-HEADER1", new String(ptr));
                return ptr.length;
            }
        });

        // or set CURLOPT_WRITEDATA to file path
        curl_setopt(curl, CURLOPT_WRITEFUNCTION, new Curl.Write() {
            public int callback(byte[] ptr) {
                if (ptr == null) {
                    Log.d("CURL-J-WRITE2", "write null");
                    return -1;
                }
                try {
                    outputStream.write(ptr);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                //binaryData += ptr;
                //Log.d("CURL-J-WRITE3", new String(binaryData));
                //data += new String(ptr);
                return ptr.length;
            }
        });

        // or set CURLOPT_STDERR to file path
        curl_setopt(curl, CURLOPT_DEBUGFUNCTION, new Curl.Debug() {
            public int callback(int type, byte[] ptr) {
                if (ptr == null) {
                    Log.d("CURL-J-DEBUG1", CURLINFO[type] + ": write null");
                    return 0;
                }
                if (type == CURLINFO_TEXT) {
                    Log.d("CURL-J-DEBUG2", CURLINFO[type] + ": " + new String(ptr));
                }
                return 0;
            }
        });

        // simple progress function
        curl_setopt(curl, CURLOPT_NOPROGRESS, 0);
        curl_setopt(curl, CURLOPT_PROGRESSFUNCTION, new Curl.Progress() {
            public int callback(double dltotal, double dlnow, double ultotal, double ulnow) {
                return 0;
            }
        });

        curl_setopt(curl, CURLOPT_FOLLOWLOCATION, 1);

        // support ipv4 only
        curl_setopt(curl, CURLOPT_IPRESOLVE, CURL_IPRESOLVE_V4);

        // set dns servers
        curl_setopt(curl, CURLOPT_DNS_SERVERS, "8.8.8.8");

        // disable ssl verify
        curl_setopt(curl, CURLOPT_SSL_VERIFYPEER, 0);

        // enable cookie engine
        curl_setopt(curl, CURLOPT_COOKIELIST, "");

        // enable certificate chain info gatherer
        curl_setopt(curl, CURLOPT_CERTINFO, 1);

        if (!curl_perform(curl)) {
            data = curl_error();
        } else {
            data += "=====getinfo=====\n";
            data += "total_time: " + curl_getinfo(curl, CURLINFO_TOTAL_TIME) + "\n";
            data += "namelookup_time: " + curl_getinfo(curl, CURLINFO_NAMELOOKUP_TIME) + "\n";
            data += "connect_time: " + curl_getinfo(curl, CURLINFO_CONNECT_TIME) + "\n";
            data += "starttransfer_time: " + curl_getinfo(curl, CURLINFO_STARTTRANSFER_TIME) + "\n";
            data += "redirect_time: " + curl_getinfo(curl, CURLINFO_REDIRECT_TIME) + "\n";
        }

        String[] cookies = curl_getinfo_list(curl, CURLINFO_COOKIELIST);
        if (cookies != null) {
            for (String cookie: cookies) {
                Log.d("CURL-J-COOKIE", cookie);
            }
        }

        Object[] certinfos = curl_getinfo_certinfo(curl, CURLINFO_CERTINFO);
        if (certinfos != null) {
            for (Object certinfo: certinfos) {
                for (String cert: (String[])certinfo) {
                    Log.d("CURL-J-CERT", cert);
                }
            }
        }

        curl_cleanup(curl);
        return data;
    }
}
