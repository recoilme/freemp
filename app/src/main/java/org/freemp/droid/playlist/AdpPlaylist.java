package org.freemp.droid.playlist;

import android.app.Activity;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.androidquery.AQuery;

import org.freemp.droid.ClsTrack;
import org.freemp.droid.R;
import org.freemp.droid.StringUtils;

import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: recoilme
 * Date: 27/11/13
 * Time: 18:30
 * To change this template use File | Settings | File Templates.
 */
public class AdpPlaylist extends BaseExpandableListAdapter {

    ArrayList<ClsArrTrack> data;
    Activity activity;
    float scale;
    int med = 18, sml = 14;

    public AdpPlaylist(Activity activity, ArrayList<ClsArrTrack> data) {
        this.data = data;
        med = (int) activity.getResources().getDimension(R.dimen.medium_text);
        sml = (int) activity.getResources().getDimension(R.dimen.small_text);

        this.activity = activity;
        scale = activity.getResources().getDisplayMetrics().density;
    }

    @Override
    public int getGroupCount() {
        return data.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return data.get(groupPosition).getPlaylists().size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return data.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return data.get(groupPosition).getPlaylists().get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    public ArrayList<ClsTrack> getSelected() {
        ArrayList<ClsTrack> tracks = new ArrayList<ClsTrack>();
        for (ClsArrTrack arrTrack : data) {
            if (arrTrack.getPlaylists() == null) {
                continue;
            }
            for (ClsTrack track : arrTrack.getPlaylists()) {
                if (track.isSelected()) {
                    tracks.add(track);
                }
            }
        }
        return tracks;
    }


    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = activity.getLayoutInflater().inflate(R.layout.playlist_group_row, null);
        }
        AQuery listAq = new AQuery(convertView);

        final ClsArrTrack o = data.get(groupPosition);

        final String section = o.getArtists();
        listAq.id(R.id.section).gone();
        if (groupPosition == 0 && !TextUtils.equals(section, "")) {
            //recently added
            listAq.id(R.id.section).visible();
            listAq.id(R.id.section).text(StringUtils.capitalizeFully(section));
        } else {
            if (!TextUtils.equals(section, "") && (groupPosition > 0)) {
                if (!TextUtils.equals(section, data.get(groupPosition - 1).getArtists())) {
                    listAq.id(R.id.section).visible();
                    listAq.id(R.id.section).text(StringUtils.capitalizeFully(section));
                }
            }
        }
        listAq.id(R.id.section).text(StringUtils.capitalizeFully(o.getArtists()));
        listAq.id(R.id.textView).text(StringUtils.capitalizeFully(o.getDescription()));

        final CheckBox checkBox = listAq.id(R.id.checkBox).getCheckBox();


        final int checkSelection = o.checkSelection();

        checkBox.setChecked((checkSelection >= 0));
        if (checkSelection == 1) {
            listAq.id(R.id.textView).textColor(Color.parseColor("#FDC332"));
        } else if (checkSelection == 0) {
            listAq.id(R.id.textView).textColor(Color.parseColor("#CC681F"));
        } else {
            listAq.id(R.id.textView).textColor(Color.parseColor("#F6FFFF"));
        }

        final int pos = groupPosition;
        listAq.id(R.id.relativeLayout).clicked(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkBox.setChecked(!(checkSelection >= 0));
                ArrayList<ClsTrack> tracks = o.getPlaylists();
                for (int i = 0; i < tracks.size(); i++) {
                    ClsTrack t = tracks.get(i);
                    t.setSelected(!(checkSelection >= 0));
                    tracks.set(i, t);
                }
                data.set(pos, o);
                invalidate();
            }
        });
        return convertView;
    }

    public void invalidate() {
        ((ActPlaylist) activity).updateColor();
        this.notifyDataSetChanged();
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        final LinearLayout linearLayout = new LinearLayout(activity);
        linearLayout.setOrientation(LinearLayout.VERTICAL);

        int dpAsPixels = (int) (10 * scale + 0.5f);
        linearLayout.setPadding(dpAsPixels, dpAsPixels, dpAsPixels, dpAsPixels);

        final ClsTrack o = data.get(groupPosition).getPlaylists().get(childPosition);

        final TextView artist = new TextView(activity);
        //artist.setTextAppearance(activity,android.R.attr.textAppearanceMedium);
        artist.setTextSize(TypedValue.COMPLEX_UNIT_PX, med);
        artist.setText(o.getArtist());

        final TextView title = new TextView(activity);
        title.setTextSize(TypedValue.COMPLEX_UNIT_PX, sml);
        title.setText(o.getTitle());

        linearLayout.addView(artist);
        linearLayout.addView(title);

        //selected
        if (o.isSelected()) {
            title.setTextColor(Color.parseColor("#FDC332"));
        } else {
            title.setTextColor(Color.GRAY);
        }

        //selection
        final int posGroup = groupPosition;
        final int posChild = childPosition;
        linearLayout.setClickable(true);
        linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!o.isSelected()) {
                    title.setTextColor(Color.parseColor("#FDC332"));
                } else {
                    title.setTextColor(Color.GRAY);
                }
                o.setSelected(!o.isSelected());

                ClsArrTrack group = data.get(posGroup);
                ArrayList<ClsTrack> tracks = group.getPlaylists();

                ClsTrack t = tracks.get(posChild);
                t.setSelected(o.isSelected());

                tracks.set(posChild, t);

                data.set(posGroup, group);
                invalidate();
            }
        });
        return linearLayout;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}