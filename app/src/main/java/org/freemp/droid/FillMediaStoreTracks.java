package org.freemp.droid;

import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;
import android.text.TextUtils;

import com.flurry.android.FlurryAgent;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by recoil on 26.01.14.
 */
public class FillMediaStoreTracks {

    private ArrayList<ClsTrack> tempAllTracksMediaStore;

    public FillMediaStoreTracks(Context context) {
        tempAllTracksMediaStore = new ArrayList<ClsTrack>();
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
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    projection,
                    selection,
                    null,
                    null);

            while (cursor != null && cursor.moveToNext()) {

                String folder = "";
                String path = cursor.getString(7);
                String[] pathArray = path.split(
                        TextUtils.equals(System.getProperty("file.separator"), "") ? "/" : System.getProperty("file.separator")
                );
                if (pathArray != null && pathArray.length > 1) {
                    folder = pathArray[pathArray.length - 2];
                }

                tempAllTracksMediaStore.add(new ClsTrack(
                        cursor.getString(0),
                        cursor.getString(1),
                        cursor.getString(2),
                        cursor.getString(3),
                        cursor.getInt(4),
                        cursor.getInt(5),
                        (cursor.getInt(6) / 1000),
                        cursor.getString(7),
                        folder,
                        new File(path).lastModified(),
                        cursor.getInt(8)
                ));
            }
            if (cursor != null) {
                cursor.close();
            }
        } catch (Exception e) {
            FlurryAgent.onError("1", "1", e.toString());
            try {
                cursor.close();
            } catch (Exception ee) {
            }
            //e.printStackTrace();
        }

        FileUtils.writeObject("alltracksms", context, tempAllTracksMediaStore);
    }

    public ArrayList<ClsTrack> getTracks() {
        return tempAllTracksMediaStore;
    }
}
