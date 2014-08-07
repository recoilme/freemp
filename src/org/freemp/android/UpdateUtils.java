package org.freemp.android;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.lang.ref.WeakReference;
import java.util.Locale;

/**
 * Created by recoil on 06.08.14.
 * example file:

 {
 "notifications": [{ id":1, "title":"Sorry we are deleted from GPlay", "text":"Please click for download new app",
 "version":105 , "action":"market://search?q=freemp", "locale":"ru_Ru"}]
 }

  - id: unique number of message
  - title: title of message
  - text: text of message
  - version: if not set message for all (may be not set)
  - action: default action (may be not set)
  - locale: may be not set

 */

public class UpdateUtils {

    public static final String MESSAGEURL = "https://github.com/recoilme/freemp/blob/master/message.json?raw=true";
    private Context context;
    private int versionCode;
    private String locale;
    private final WeakReference<Activity> activityContainer;

    public UpdateUtils(Activity activity) {
        activityContainer = new WeakReference<Activity>(activity);
        new Update().execute();
    }

    private class Update extends AsyncTask<Void,Void,String> {

        @Override
        protected String doInBackground(Void... params) {
            Activity activity = activityContainer.get();
            if (null == activity) {
                return "";
            }
            try {
                versionCode = activity.getPackageManager()
                        .getPackageInfo(activity.getPackageName(), 0).versionCode;
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
            locale = Locale.getDefault().toString();
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
                Activity activity = activityContainer.get();
                if (null == activity) {
                    return;
                }
                context = activity.getApplicationContext();
                if (context==null) {
                    return;
                }
                //string with showed messages
                String showedMessages = PreferenceManager.getDefaultSharedPreferences(context).getString(MESSAGEURL,"");
                for (int i=0;i<notifications.length();i++) {
                    JSONObject jsonNotification = notifications.optJSONObject(i);

                    if (jsonNotification==null) break;

                    final int version = jsonNotification.optInt("version",-1);
                    if (version>0 && version!=versionCode) {
                        continue;
                    }

                    final String localeTarget = jsonNotification.optString("locale","all");
                    if (!TextUtils.equals("all",localeTarget) && !TextUtils.equals(localeTarget, locale)) {
                        continue;
                    }

                    final int id = jsonNotification.optInt("id");
                    if (showedMessages.contains(id+";")) {
                        continue;
                    }
                    else {
                        showedMessages+=id+";";
                        PreferenceManager.getDefaultSharedPreferences(context).edit().putString(MESSAGEURL,showedMessages).commit();

                        Intent intent = null;
                        if (!TextUtils.equals("",jsonNotification.optString("action",""))) {
                            // if has action add it
                            intent = new Intent(Intent.ACTION_VIEW, Uri.parse(
                                    jsonNotification.optString("action","")));
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                        }
                        else {
                            // if no - just inform
                            intent = new Intent(activity,activity.getClass());
                        }
                        PendingIntent pIntent = PendingIntent.getActivity(context, id, intent, 0);

                        // if you don't use support library, change NotificationCompat on Notification
                        Notification noti = new NotificationCompat.Builder(context)
                                .setContentTitle(jsonNotification.optString("title",""))
                                .setContentText(jsonNotification.optString("text",""))
                                .setSmallIcon(R.drawable.icon)//change this on your icon
                                .setContentIntent(pIntent).build();
                        NotificationManager notificationManager = (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);
                        // hide the notification after its selected
                        noti.flags |= Notification.FLAG_AUTO_CANCEL;

                        notificationManager.notify(id, noti);
                        break;
                    }
                }

                //process update if exists
                JSONObject update = jsonResult.optJSONObject("update");
                if (update==null) {
                    return;
                }

                final int version = update.optInt("version",-1);
                if (version<0 || version>=versionCode) {
                    return;
                }
                else {
                    //need update
                    String url = update.optString("file");
                    if (!TextUtils.equals("",url)) {
                        new Download().execute(new String[]{url, "" + version});
                    }
                }
            }
        }

    }

    private class Download extends AsyncTask<String,Void,String>  {

        @Override
        protected String doInBackground(String... urls) {
            boolean success = false;
            String path = Environment.getExternalStorageDirectory()+"/"+urls[1]+".apk";
            File file = new File(path);
            DefaultHttpClient client = new DefaultHttpClient();
            HttpGet httpGet = new HttpGet(urls[0]);
            try {
                HttpResponse execute = client.execute(httpGet);
                InputStream content = execute.getEntity().getContent();
                FileOutputStream fileOutput = new FileOutputStream(file);

                byte[] buffer = new byte[1024];
                int bufferLength = 0;
                while ( (bufferLength = content.read(buffer)) > 0 ) {
                    fileOutput.write(buffer, 0, bufferLength);
                }
                fileOutput.close();

                success = true;

            } catch (Exception e) {
                e.printStackTrace();
            }
            if (success) {
                if (file.exists() && file.length()>0) {
                    return path;
                }
                else {
                    return "";
                }
            }
            else {
                return "";
            }
        }

        @Override
        protected void onPostExecute(String result) {
            if (!TextUtils.equals("",result)) {
                //file downloaded
                Activity activity = activityContainer.get();
                if (null == activity) {
                    return;
                }
                Intent promptInstall = new Intent(Intent.ACTION_VIEW)
                        .setDataAndType(Uri.parse("file:///"+result),
                                "application/vnd.android.package-archive");
                activity.startActivity(promptInstall);
            }
        }
    }
}
