package ru.recoilme.freeamp.playlist.albums;

import android.app.Activity;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import com.androidquery.AQuery;
import ru.recoilme.freeamp.ClsTrack;
import ru.recoilme.freeamp.MediaUtils;
import ru.recoilme.freeamp.R;

import java.util.ArrayList;

/**
 * Created by recoil on 29.01.14.
 */
public class AdpArtworks extends BaseAdapter {

    private final AQuery listAq;
    ArrayList<ClsTrack> data;
    Activity activity;
    private int scrollState;
    Bitmap placeHolder;
    AbsListView.LayoutParams layoutParams;
    Animation fadeIn;
    int width;

    //Создаем LruCache: http://developer.android.com/training/displaying-bitmaps/cache-bitmap.html
    /*
    final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
    final int cacheSize = Math.min(20 * 360000,(int)maxMemory/2); // <7MiB = 300width * 300heigth * 4bytesperpixel * 20images

    LruCache bitmapCache = new LruCache(Math.max((int)maxMemory/8,cacheSize)) {
        protected int sizeOf(int key, Bitmap value) {
            return value.getRowBytes() * value.getHeight();//здесь по чесноку считаем
        }
    };


    public void addBitmapToMemoryCache(Activity activity, int key, Bitmap bitmap) {
        synchronized (bitmapCache) {
            if (getBitmapFromMemCache(activity, key) == null) {
                bitmapCache.put(key, bitmap);
            }
        }
    }

    public Bitmap getBitmapFromMemCache(Activity activity,int key) {
        return (Bitmap) bitmapCache.get(key);
    }
    */

    public AdpArtworks(Activity activity, ArrayList<ClsTrack> data){
        this.data = data;
        this.activity = activity;

        listAq = new AQuery(activity);

        int iDisplayWidth= PreferenceManager.getDefaultSharedPreferences(activity.getApplicationContext()).getInt("screenWidth",800);
        int numColumns = (int)(iDisplayWidth / 310);
        if (numColumns==0) numColumns =1;
        width = (iDisplayWidth / numColumns);
        layoutParams= new AbsListView.LayoutParams(width,width);

        final Drawable imgBgr = activity.getResources().getDrawable(R.drawable.row_bgr);
        final Bitmap bitmap = Bitmap.createBitmap(width, width, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        imgBgr.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        imgBgr.draw(canvas);
        this.placeHolder = bitmap;

        Animation fadeIn = new AlphaAnimation(0, 1);
        fadeIn.setDuration(500);
        fadeIn.setInterpolator(new DecelerateInterpolator());
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {

        if (view == null) {
            final ImageView img = new ImageView(activity);
            img.setPadding(10,10,0,0);
            img.setLayoutParams(layoutParams);
            view = img;
        }

        AQuery aq = listAq.recycle(view);


        final int albumId = data.get(position).getAlbumId();
        //Uri uri = ContentUris.withAppendedId(MediaUtils.sArtworkUri, albumId);
        //String p =  convertMediaUriToPath(uri);
        //if (p!=null) {
            if(aq.shouldDelay(position, view, parent, "")) {
                aq.id(view).image(R.drawable.row_bgr);//).image(placeHolder,1f);
            }
            else {
                aq.id(view).image(MediaUtils.getArtworkQuick(activity,albumId,300,300));//new File(p),width);
            }
        //}
        //if(aq.shouldDelay(position, img, parent, "")){
            //aq.id(img).image(new File(uri.getPath()),300);
            //aq.id(R.id.icon_type).image(null,1f);
        //}else{

        //}
        /*
        final Bitmap bitmap = getBitmapFromMemCache(activity,albumId);
        if (bitmap != null  && !bitmap.isRecycled()) {
            img.setImageBitmap(bitmap);
        } else {
            if (!(this.getScrollState() == AbsListView.OnScrollListener.SCROLL_STATE_FLING)) {
                new Thread() {
                    public void run() {
                        final Bitmap b = MediaUtils.getArtworkQuick(activity, albumId, 300, 300);
                        if (b!=null && !b.isRecycled()) {
                            addBitmapToMemoryCache(activity,albumId,b);

                            activity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (b!=null && !b.isRecycled()) {
                                        img.setImageBitmap(b);
                                        img.postInvalidate();
                                    }
                                }
                            });
                        }
                    }
                }.start();
            }
            else {
                img.setImageBitmap(placeHolder);
            }
        }
        */
        return view;
    }

    protected String convertMediaUriToPath(Uri uri) {
        String [] proj={MediaStore.Images.Media.DATA};
        Cursor cursor = activity.getContentResolver().query(uri, proj,  null, null, null);
        if (cursor==null) {
            return null;
        }
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        if (cursor.moveToFirst()) {
            String path = cursor.getString(column_index);
            cursor.close();
            return path;
        }
        else {
            return "";
        }
    }

    public int getScrollState() {
        return scrollState;
    }

    public void setScrollState(int scrollState) {
        this.scrollState = scrollState;
    }
}
