package org.freemp.android;

import android.os.AsyncTask;
import android.text.TextUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by recoil on 06.08.14.
 */
public class UpdateUtils {

    static final String MESSAGEURL = "";

    public void UpdateUtils() {
        new Update().execute();
    }

    private class Update extends AsyncTask<Void,Void,String> {

        @Override
        protected String doInBackground(Void... params) {
            String response = "";
            DefaultHttpClient client = new DefaultHttpClient();
            HttpGet httpGet = new HttpGet(MESSAGEURL);
            try {
                HttpResponse httpResponse = client.execute(httpGet);
                InputStream content = httpResponse.getEntity().getContent();
                BufferedReader buffer = new BufferedReader(new InputStreamReader(content));
                String s = "";
                while ((s = buffer.readLine()) != null) {
                    response += s;
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            return response;
        }

        @Override
        protected void onPostExecute(String result) {
            if (!TextUtils.equals("",result)) {

            }
        }
    }
}
