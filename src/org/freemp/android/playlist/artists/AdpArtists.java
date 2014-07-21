package org.freemp.android.playlist.artists;

import android.app.Activity;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import com.androidquery.AQuery;
import org.freemp.android.ClsTrack;
import org.freemp.android.MediaUtils;
import org.freemp.android.R;

import java.util.ArrayList;

/**
 * Created by recoil on 29.01.14.
 */
public class AdpArtists extends BaseAdapter {

    private final AQuery listAq;
    ArrayList<ClsTrack> data;
    Activity activity;
    private int scrollState;
    AbsListView.LayoutParams layoutParams;
    int width;
    Animation fadeIn;

    public AdpArtists(Activity activity, ArrayList<ClsTrack> data){
        this.data = data;
        this.activity = activity;

        listAq = new AQuery(activity);

        int iDisplayWidth= Math.max(320,PreferenceManager.getDefaultSharedPreferences(activity.getApplicationContext()).getInt("screenWidth",800));
        int numColumns = (int)(iDisplayWidth / 310);
        if (numColumns==0) numColumns =1;
        width = (iDisplayWidth / numColumns);
        layoutParams= new AbsListView.LayoutParams(width,width);

        fadeIn = new AlphaAnimation(0, 1);
        fadeIn.setDuration(300);
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


        final ClsTrack track = data.get(position);
        if(aq.shouldDelay(position, view, parent, ""+track.getArtist())) {
            aq.id(view).image(R.drawable.row_bgr);
        }
        else {
            aq.id(view).image(MediaUtils.getArtistQuick(activity,track,300,300)).animate(fadeIn);
        }
        return view;
    }

    public int getScrollState() {
        return scrollState;
    }

    public void setScrollState(int scrollState) {
        this.scrollState = scrollState;
    }
}
