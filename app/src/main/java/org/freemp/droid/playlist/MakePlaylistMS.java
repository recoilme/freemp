package org.freemp.droid.playlist;

import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;
import android.text.TextUtils;

import com.flurry.android.FlurryAgent;

import org.freemp.droid.ClsTrack;
import org.freemp.droid.FileUtils;

import java.io.File;

/**
 * Created by recoilme on 05/12/13.
 */
public class MakePlaylistMS extends MakePlaylistAbstract {

    public MakePlaylistMS(Context context, boolean refresh) {
        super(context, refresh);
    }

    @Override
    public void getAllTracks(Context context, boolean refresh) {
        String selection = MediaStore.Audio.Media.IS_MUSIC + " != 0";

        String[] projection = {
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.COMPOSER,
                MediaStore.Audio.Media.YEAR,
                MediaStore.Audio.Media.TRACK,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.ALBUM_ID
        };

        Cursor cursor = context.getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                null,
                null);


        t = System.currentTimeMillis();

        while (cursor.moveToNext()) {
            try {
                String folder = "";
                String path = cursor.getString(7);
                String[] pathArray = path.split(
                        TextUtils.equals(System.getProperty("file.separator"), "") ? "/" : System.getProperty("file.separator")
                );
                if (pathArray != null && pathArray.length > 1) {
                    folder = pathArray[pathArray.length - 2];
                }

                allTracks.add(new ClsTrack(cursor.getString(0),
                        cursor.getString(1),
                        cursor.getString(2),
                        cursor.getString(3),
                        cursor.getInt(4),
                        cursor.getInt(5),
                        (cursor.getInt(6)/1000),
                        cursor.getString(7),
                        folder,
                        new File(path).lastModified(),
                        cursor.getInt(8)
                ));
                //Log.w("folder",folder);
            } catch (Exception e) {
                FlurryAgent.onError("1", "1", e);
                e.printStackTrace();
            }
        }
        cursor.close();
        logTime();//149ms,89,121  Arrrgh!...
        FileUtils.writeObject("alltracksms", context, allTracks);

    }
}
