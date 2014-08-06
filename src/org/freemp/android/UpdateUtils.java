package org.freemp.android;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by recoil on 06.08.14.
 */
public class UpdateUtils {

    public static final String MESSAGEURL = "https://github.com/recoilme/freemp/blob/master/message.json?raw=true";
    private Context context;
    private Activity activity;

    public UpdateUtils(Activity activity) {
        this.activity = activity;
        context = activity.getApplicationContext();
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
                JSONObject jsonResult = null;
                try {
                    jsonResult = new JSONObject(result);
                }
                catch (JSONException e) {
                    e.printStackTrace();
                    return;
                }
                //process notifications if exists
                JSONArray notifications = jsonResult.optJSONArray("notifications");
                if (notifications==null) {
                    return;
                }
                //string with showed messages
                String showedMessages = PreferenceManager.getDefaultSharedPreferences(context).getString(MESSAGEURL,"");
                for (int i=0;i<notifications.length();i++) {
                    JSONObject jsonNotification = notifications.optJSONObject(i);
                    if (jsonNotification==null) break;
                    final int id = jsonNotification.optInt("id");
                    if (showedMessages.contains(id+";")) {
                        continue;
                    }
                    else {
                        showedMessages+=id+";";
                        //PreferenceManager.getDefaultSharedPreferences(context).edit().putString(MESSAGEURL,showedMessages).commit();
                        // Prepare intent which is triggered if the notification is selected
                        Intent intent = new Intent(activity,activity.getClass());
                        intent.putExtra("msgid",id);
                        intent.putExtra("msgtitle",jsonNotification.optString("title",""));
                        intent.putExtra("msgtext",jsonNotification.optString("text",""));
                        intent.putExtra("msgaction",jsonNotification.optString("action",""));
                        PendingIntent pIntent = PendingIntent.getActivity(context, 0, intent, 0);

                        // if you don't use support library, change NotificationCompat on Notification
                        Notification noti = new NotificationCompat.Builder(context)
                                .setContentTitle(jsonNotification.optString("title",""))
                                .setContentText(jsonNotification.optString("text",""))
                                .setSmallIcon(R.drawable.icon)//change this on your icon
                                .setContentIntent(pIntent).build();
                        NotificationManager notificationManager = (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);
                        // hide the notification after its selected
                        noti.flags |= Notification.FLAG_AUTO_CANCEL;

                        notificationManager.notify(0, noti);
                        break;
                    }
                }

            }
        }

    }
}
