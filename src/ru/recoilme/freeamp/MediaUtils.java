package ru.recoilme.freeamp;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;
import com.androidquery.util.AQUtility;

import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: recoilme
 * Date: 02/12/13
 * Time: 13:54
 * To change this template use File | Settings | File Templates.
 */
public class MediaUtils {

    public static final Uri sArtworkUri = Uri.parse("content://media/external/audio/albumart");
    private static final BitmapFactory.Options sBitmapOptionsCache = new BitmapFactory.Options();


    // Get album art for specified album. This method will not try to
    // fall back to getting artwork directly from the file, nor will
    // it attempt to repair the database.
    public static Bitmap getArtworkQuick(Context context, int album_id, int w, int h) {
        // NOTE: There is in fact a 1 pixel frame in the ImageView used to
        // display this drawable. Take it into account now, so we don't have to
        // scale later.
        if (album_id==0) {
            return null;
        }
        //w -= 2; //GOOGOL, что тi делаешь, в космосе? ТЫ ЖЕ ТiGR!!!
        //h -= 2;
        ContentResolver res = context.getContentResolver();
        Uri uri = ContentUris.withAppendedId(sArtworkUri, album_id);
        if (uri != null) {
            ParcelFileDescriptor fd = null;
            try {
                fd = res.openFileDescriptor(uri, "r");

                int sampleSize = 1;

                // Compute the closest power-of-two scale factor
                // and pass that to sBitmapOptionsCache.inSampleSize, which will
                // result in faster decoding and better quality
                sBitmapOptionsCache.inJustDecodeBounds = true;
                BitmapFactory.decodeFileDescriptor(
                        fd.getFileDescriptor(), null, sBitmapOptionsCache);
                int nextWidth = sBitmapOptionsCache.outWidth >> 1;
                int nextHeight = sBitmapOptionsCache.outHeight >> 1;
                while (nextWidth>w && nextHeight>h) {
                    sampleSize <<= 1;
                    nextWidth >>= 1;
                    nextHeight >>= 1;
                }

                sBitmapOptionsCache.inSampleSize = sampleSize;
                sBitmapOptionsCache.inJustDecodeBounds = false;
                Bitmap b = BitmapFactory.decodeFileDescriptor(
                        fd.getFileDescriptor(), null, sBitmapOptionsCache);//теперь падает тут)

                if (b != null) {
                    // finally rescale to exactly the size we need
                    if (sBitmapOptionsCache.outWidth != w || sBitmapOptionsCache.outHeight != h) {
                        Bitmap tmp = Bitmap.createScaledBitmap(b, w, h, true);  //тут падало с аут оф мемори
                        b.recycle();
                        b = tmp;
                    }
                }

                return b;
            } catch (FileNotFoundException e) {
                AQUtility.debug("Error", e.toString());
                //FlurryAgent.onError("3", "3", e);
                return null;
            } finally {
                try {
                    if (fd != null)
                        fd.close();
                } catch (IOException e) {
                    //FlurryAgent.onError("4", "4", e);
                }
            }
        }
        return null;
    }

    public static void setRingtoneWithCoping(Context context,ClsTrack track)
    {
        /*
        http://www.stealthcopter.com/blog/2010/01/android-saving-a-sound-file-to-sd-from-resource-and-setting-as-ringtone/

        File path = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_RINGTONES);
        path.mkdirs(); // Ensure the directory exists
        File file = new File(path, track.getPath());
        try {
            OutputStream os = new FileOutputStream(file);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        */
    }

    public static void setRingtone(Context context,ClsTrack track)
    {

        ContentValues values = new ContentValues();
        values.put(MediaStore.MediaColumns.DATA, track.getPath());
        values.put(MediaStore.MediaColumns.TITLE, track.getTitle());
        //values.put(MediaStore.MediaColumns.SIZE, 1024*1024);
        values.put(MediaStore.MediaColumns.MIME_TYPE, "audio/*");
        values.put(MediaStore.Audio.Media.ARTIST, track.getArtist());
        //values.put(MediaStore.Audio.Media.DURATION, 5000);
        values.put(MediaStore.Audio.Media.IS_RINGTONE, true);
        values.put(MediaStore.Audio.Media.IS_NOTIFICATION, false);
        values.put(MediaStore.Audio.Media.IS_ALARM, false);
        values.put(MediaStore.Audio.Media.IS_MUSIC, true);

        Uri uri = MediaStore.Audio.Media.getContentUriForPath(track.getPath());

        if(uri == null || context.getContentResolver()==null)
        {
            Toast.makeText(context, context.getString(R.string.error), Toast.LENGTH_SHORT).show();
            return;
        }
        //TODO check this may be better copy file in ringtone dir before?
        context.getContentResolver().delete(uri, MediaStore.MediaColumns.DATA + "=\"" + track.getPath() + "\"", null);
        Uri newUri = context.getContentResolver().insert(uri, values);

        if(newUri == null)
        {
            Toast.makeText(context,context.getString(R.string.error),Toast.LENGTH_SHORT).show();
        }
        else
        {
            RingtoneManager.setActualDefaultRingtoneUri(context, RingtoneManager.TYPE_RINGTONE, newUri);
            Toast.makeText(context,context.getString(R.string.set_as_ringtone),Toast.LENGTH_SHORT).show();
        }
    }

    //i steal it from http://www.netmite.com/android/mydroid/packages/apps/Music/src/com/android/music/MusicUtils.java
    //http://www.lastfm.ru/api/show/album.getInfo
    static void setRingtone(Context context, long id) {
        ContentResolver resolver = context.getContentResolver();
        // Set the flag in the database to mark this as a ringtone
        Uri ringUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id);
        try {
            ContentValues values = new ContentValues(2);
            values.put(MediaStore.Audio.Media.IS_RINGTONE, "1");
            values.put(MediaStore.Audio.Media.IS_ALARM, "1");
            resolver.update(ringUri, values, null, null);
        } catch (UnsupportedOperationException ex) {
            // most likely the card just got unmounted
            Log.e("e", "couldn't set ringtone flag for id " + id);
            return;
        }

        String[] cols = new String[] {
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.TITLE
        };

        String where = MediaStore.Audio.Media._ID + "=" + id;
        Cursor cursor = query(context, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                cols, where , null, null);
        try {
            if (cursor != null && cursor.getCount() == 1) {
                // Set the system setting to make this the current ringtone
                cursor.moveToFirst();
                Settings.System.putString(resolver, Settings.System.RINGTONE, ringUri.toString());
                String message = context.getString(R.string.set_as_ringtone) + cursor.getString(2);
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public static Cursor query(Context context, Uri uri, String[] projection,
                               String selection, String[] selectionArgs, String sortOrder) {
        try {
            ContentResolver resolver = context.getContentResolver();
            if (resolver == null) {
                return null;
            }
            return resolver.query(uri, projection, selection, selectionArgs, sortOrder);
        } catch (UnsupportedOperationException ex) {
            return null;
        }

    }
}
