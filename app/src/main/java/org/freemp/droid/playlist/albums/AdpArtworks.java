package org.freemp.droid.playlist.albums;

import android.app.Activity;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.androidquery.AQuery;

import org.freemp.droid.ClsTrack;
import org.freemp.droid.MediaUtils;
import org.freemp.droid.R;

import java.util.ArrayList;

/**
 * Created by recoil on 29.01.14.
 */
public class AdpArtworks extends BaseAdapter {

    private final AQuery listAq;
    ArrayList<ClsTrack> data;
    Activity activity;
    AbsListView.LayoutParams layoutParams;
    int width;
    Animation fadeIn;
    private int scrollState;
    final int imgid = 112;
    final int tvid = 113;

    public AdpArtworks(Activity activity, ArrayList<ClsTrack> data) {
        this.data = data;
        this.activity = activity;

        listAq = new AQuery(activity);

        int iDisplayWidth = Math.max(320, PreferenceManager.getDefaultSharedPreferences(activity.getApplicationContext()).getInt("screenWidth", 800));
        int numColumns = (int) (iDisplayWidth / 310);
        if (numColumns == 0) numColumns = 1;
        width = (iDisplayWidth / numColumns);
        layoutParams = new AbsListView.LayoutParams(width, width);

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
            final RelativeLayout rl = new RelativeLayout(activity);
            rl.setLayoutParams(layoutParams);

            final ImageView img = new ImageView(activity);
            RelativeLayout.LayoutParams imglp = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.MATCH_PARENT,RelativeLayout.LayoutParams.MATCH_PARENT);

            img.setPadding(10, 10, 0, 0);
            img.setId(imgid);
            //img.setLayoutParams(layoutParams);
            rl.addView(img,imglp);

            TextView tv = new TextView(activity);
            //tv.setSingleLine();
            tv.setPadding(16,0,40,0);
            RelativeLayout.LayoutParams lptv = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            lptv.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, img.getId());
            tv.setShadowLayer(1,-2,-2, Color.BLACK);
            tv.setId(tvid);

            rl.addView(tv,lptv);
            view = rl;
        }

        AQuery aq = listAq.recycle(view);


        final ClsTrack track = data.get(position);
        if (aq.shouldDelay(position, view, parent, "" + track.getAlbumId())) {
            aq.id(imgid).image(R.drawable.row_bgr);
        } else {
            aq.id(imgid).image(MediaUtils.getArtworkQuick(activity, track, 300, 300)).animate(fadeIn);
        }
        aq.id(tvid).getTextView().setText((""+track.getArtist()));
        return view;
    }

    public int getScrollState() {
        return scrollState;
    }

    public void setScrollState(int scrollState) {
        this.scrollState = scrollState;
    }
}
