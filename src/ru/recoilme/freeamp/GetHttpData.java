package ru.recoilme.freeamp;

import me.piebridge.curl.Curl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Created by recoilme on 04/02/14.
 */
public class GetHttpData extends Curl {

    private ByteArrayOutputStream outputStream;
    private String url;
    private String error;
    private String header;
    private String info;

    public GetHttpData() {
        outputStream = new ByteArrayOutputStream();
        error = "";
        header = "";
        info = "";
    }

    public void request() {
        int curl = curl_init();

        if (0 == curl) {
            setError("curl_init failed");
        }

        curl_setopt(curl, CURLOPT_URL, url);
        curl_setopt(curl, CURLOPT_VERBOSE, 1);

        // CURLOPT_WRITEFUNCTION will have header too if CURLOPT_HEADER is true
        // curl_setopt(curl, CURLOPT_HEADER, 1);

        // or set CURLOPT_WRITEHEADER to file path
        curl_setopt(curl, CURLOPT_HEADERFUNCTION, new Curl.Write() {
            public int callback(byte[] ptr) {
                if (ptr == null) {
                    return -1;
                }
                header += new String(ptr);
                return ptr.length;
            }
        });

        // or set CURLOPT_WRITEDATA to file path
        curl_setopt(curl, CURLOPT_WRITEFUNCTION, new Curl.Write() {
            public int callback(byte[] ptr) {
                if (ptr == null) {
                    return -1;
                }
                try {
                    outputStream.write(ptr);
                } catch (IOException e) {
                    setError(e.toString());
                }
                return ptr.length;
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
        //curl_setopt(curl, CURLOPT_DNS_SERVERS, "8.8.8.8");

        // disable ssl verify
        curl_setopt(curl, CURLOPT_SSL_VERIFYPEER, 0);

        // enable cookie engine
        curl_setopt(curl, CURLOPT_COOKIELIST, "");

        // enable certificate chain info gatherer
        curl_setopt(curl, CURLOPT_CERTINFO, 1);

        if (!curl_perform(curl)) {
            setError(curl_error());
        } else {

            info += "total_time: " + curl_getinfo(curl, CURLINFO_TOTAL_TIME) + "\n";
            info += "namelookup_time: " + curl_getinfo(curl, CURLINFO_NAMELOOKUP_TIME) + "\n";
            info += "connect_time: " + curl_getinfo(curl, CURLINFO_CONNECT_TIME) + "\n";
            info += "starttransfer_time: " + curl_getinfo(curl, CURLINFO_STARTTRANSFER_TIME) + "\n";
            info += "redirect_time: " + curl_getinfo(curl, CURLINFO_REDIRECT_TIME) + "\n";
        }

        String[] cookies = curl_getinfo_list(curl, CURLINFO_COOKIELIST);
        if (cookies != null) {
            for (String cookie: cookies) {
                info += cookie;
            }
        }

        Object[] certinfos = curl_getinfo_certinfo(curl, CURLINFO_CERTINFO);
        if (certinfos != null) {
            for (Object certinfo: certinfos) {
                for (String cert: (String[])certinfo) {
                    info += cert;
                }
            }
        }

        curl_cleanup(curl);


    }


    public void setUrl(String url) {
        this.url = url;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public byte[] getByteArray() {
        return outputStream.toByteArray();
    }

    public String getInfo() {
        return info;
    }
}
