package org.freemp.droid;

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

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.androidquery.util.AQUtility;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.lang.ref.WeakReference;

/**
 * Created by recoil on 06.08.14.
 * example file:
 * <p>
 * {
 * <p>
 * "notifications":
 * [
 * {
 * "id": 1 ,
 * "title": "Sorry we are deleted from GPlay" ,
 * "text": "Please click for download new app" ,
 * "version": 105 ,
 * "action": "market://search?q=freemp" ,
 * "locale": "ru_Ru"
 * }
 * ] ,
 * "update": {
 * "version": 106 ,
 * "file": "https://github.com/recoilme/freemp/blob/master/freemp.apk?raw=true" ,
 * "title": "New version avialable" ,
 * "text": "Click for update"
 * }
 * <p>
 * }
 * //noti
 * - id: unique number of message
 * - title: title of message
 * - text: text of message
 * - version: if not set message for all (may be not set)
 * - action: default action (may be not set)
 * - locale: may be not set
 * //update
 * - version: current version of update
 */

public class UpdateUtils {

    public static final String MESSAGEURL = "https://github.com/recoilme/freemp/blob/master/message.json?raw=true";
    private final WeakReference<Activity> activityContainer;
    private Context context;
    private int versionCode;
    private String locale;
    private AQuery aq;

    public UpdateUtils(Activity activity) {
        activityContainer = new WeakReference<Activity>(activity);
        aq = new AQuery(activity);
        new Update().execute();
    }

    private void buildNotification(String title, String text, PendingIntent pIntent, int id) {
        if (TextUtils.equals("", title) && TextUtils.equals("", text)) {
            return;
        }
        // if you don't use support library, change NotificationCompat on Notification
        Notification noti = new NotificationCompat.Builder(context)
                .setContentTitle(title)
                .setContentText(text)
                .setSmallIcon(R.drawable.freemp)//change this on your freemp
                .setContentIntent(pIntent).build();
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);
        // hide the notification after its selected
        noti.flags |= Notification.FLAG_AUTO_CANCEL;

        notificationManager.notify(id, noti);
    }

    private class Update extends AsyncTask<Void, Void, String> {

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
            locale = activity.getResources().getConfiguration().locale.toString();//Locale.getDefault().toString();
            AQUtility.debug("locale", locale);
            String response = "";

            AjaxCallback<String> cb = new AjaxCallback<String>();
            cb.url(MESSAGEURL).type(String.class);

            aq.sync(cb);

            response = cb.getResult();
            return response;
        }

        @Override
        protected void onPostExecute(String result) {
            if (result != null && !TextUtils.equals("", result)) {
                JSONObject jsonResult = null;
                try {
                    jsonResult = new JSONObject(result);
                } catch (Exception e) {
                    e.printStackTrace();
                    return;
                }
                //process notifications if exists
                JSONArray notifications = jsonResult.optJSONArray("notifications");
                if (notifications == null) {
                    return;
                }
                Activity activity = activityContainer.get();
                if (null == activity) {
                    return;
                }
                context = activity.getApplicationContext();
                if (context == null) {
                    return;
                }
                //string with showed messages
                String showedMessages = PreferenceManager.getDefaultSharedPreferences(context).getString(MESSAGEURL, "");
                for (int i = 0; i < notifications.length(); i++) {
                    JSONObject jsonNotification = notifications.optJSONObject(i);

                    if (jsonNotification == null) break;

                    final int version = jsonNotification.optInt("version", -1);
                    if (version > 0 && version != versionCode) {
                        continue;
                    }

                    final String localeTarget = jsonNotification.optString("locale", "all");
                    if (!TextUtils.equals("all", localeTarget) && !TextUtils.equals(localeTarget, locale)) {
                        continue;
                    }

                    final int id = jsonNotification.optInt("id");
                    if (showedMessages.contains(id + ";")) {
                        continue;
                    } else {
                        showedMessages += id + ";";
                        PreferenceManager.getDefaultSharedPreferences(context).edit().putString(MESSAGEURL, showedMessages).commit();

                        Intent intent = null;
                        if (!TextUtils.equals("", jsonNotification.optString("action", ""))) {
                            // if has action add it
                            intent = new Intent(Intent.ACTION_VIEW, Uri.parse(
                                    jsonNotification.optString("action", "")));
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                        } else {
                            // if no - just inform
                            intent = new Intent(activity, activity.getClass());
                        }
                        PendingIntent pIntent = PendingIntent.getActivity(context, id, intent, 0);

                        buildNotification(jsonNotification.optString("title", ""), jsonNotification.optString("text", ""),
                                pIntent, id);
                        break;
                    }
                }

                //process update if exists
                JSONObject update = jsonResult.optJSONObject("update");
                if (update == null) {
                    return;
                }

                final int version = update.optInt("version", -1);
                if (version < 0 || version <= versionCode) {
                    return;
                } else {
                    //need update
                    String url = update.optString("file");
                    if (!TextUtils.equals("", url)) {
                        //new Download(update.optString("title"),update.optString("text"),version).execute(new String[]{url});
                        final String title = update.optString("title");
                        final String text = update.optString("text");
                        final int id = version;

                        final String path = Environment.getExternalStorageDirectory() + "/" + id + ".apk";
                        File file = new File(path);

                        aq.download(url, file, new AjaxCallback<File>() {

                            public void callback(String url, File file, AjaxStatus status) {

                                if (file != null) {
                                    Activity activity = activityContainer.get();
                                    if (null == activity) {
                                        return;
                                    }
                                    context = activity.getApplicationContext();
                                    Intent promptInstall = new Intent(Intent.ACTION_VIEW)
                                            .setDataAndType(Uri.parse("file:///" + result),
                                                    "application/vnd.android.package-archive");
                                    promptInstall.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    PendingIntent pIntent = PendingIntent.getActivity(context, id, promptInstall, 0);
                                    buildNotification(title, text, pIntent, id);
                                } else {
                                    //do nothing
                                }
                            }

                        });
                    }
                }
            }
        }

    }
}
