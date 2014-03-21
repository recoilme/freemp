package ru.recoilme.freeamp.player;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import ru.recoilme.freeamp.ClsTrack;
import ru.recoilme.freeamp.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.nhaarman.listviewanimations.ArrayAdapter;

/**
 * Created with IntelliJ IDEA.
 * User: recoilme
 * Date: 28/11/13
 * Time: 17:25
 * To change this template use File | Settings | File Templates.
 */
public class AdpPlayer extends com.nhaarman.listviewanimations.ArrayAdapter {

    Activity activity;
    float scale;
    LayoutInflater mInflater;
    int mSelectedTrackColor,mDefaultTrackColor;

    static class CellViewHolder {
        public TextView index;
        public TextView artist;
        public TextView title;
        public TextView duration;
    }

    public void replaceTrackList(List<ClsTrack> data){
        if(data == mItems){
            return;
        }
        if(data != null){
            mItems = data;
        }else{
            mItems = new ArrayList<ClsTrack>();
        }
        notifyDataSetChanged();
    }

    public AdpPlayer(Activity activity, List<ClsTrack> data){
        super(data, false);
        this.activity = activity;
        scale = activity.getResources().getDisplayMetrics().density;
        mSelectedTrackColor = activity.getResources().getColor(R.color.text_header);
        mDefaultTrackColor = activity.getResources().getColor(R.color.text_rowslave);
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).hashCode();  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        CellViewHolder holder;

        if(convertView == null){
            if(mInflater == null){
                Context context = parent.getContext();
                mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            }
            convertView = mInflater.inflate(R.layout.player_cell, null);

            holder = new CellViewHolder();
            holder.index = (TextView) convertView.findViewById(R.id.cell_index);
            holder.artist = (TextView) convertView.findViewById(R.id.cell_artist);
            holder.title = (TextView) convertView.findViewById(R.id.cell_title);
            holder.duration = (TextView) convertView.findViewById(R.id.cell_duration);

            convertView.setTag(holder);
        }else{
            holder = (CellViewHolder) convertView.getTag();
        }

        ClsTrack currentTrack = (ClsTrack)getItem(position);
        int sec = currentTrack.getDuration() / 1000;
        int min = sec / 60;
        sec %= 60;

        holder.index.setText((position + 1)+".");
        holder.artist.setText(currentTrack.getArtist());
        holder.title.setText(currentTrack.getTitle());
        holder.duration.setText(String.format("%2d:%02d", min, sec));

        int currentTrackColor = (position == ActPlayer.selected) ? mSelectedTrackColor : mDefaultTrackColor;

        holder.title.setTextColor(currentTrackColor);

        return convertView;
    }

    @Override
    public boolean hasStableIds(){
        return true;
    }
}