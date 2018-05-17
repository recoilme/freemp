package org.freemp.droid.player;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.ListPopupWindow;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.freemp.droid.ClsTrack;
import org.freemp.droid.MediaUtils;
import org.freemp.droid.R;
import org.freemp.droid.web.ActFreemporg;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: recoilme
 * Date: 28/11/13
 * Time: 17:25
 * To change this template use File | Settings | File Templates.
 */
public class AdpPlayer extends BaseAdapter {

    Activity activity;
    List<ClsTrack> mItems = new ArrayList<>();
    float scale;
    LayoutInflater mInflater;
    int mSelectedTrackColor, mDefaultTrackColor;
    private int WRITE_SETTINGS_PERMISSION_REQUEST_CODE;

    public AdpPlayer(Activity activity, List<ClsTrack> data) {
        //super(data, false);
        this.activity = activity;
        mItems = data;
        scale = activity.getResources().getDisplayMetrics().density;
        mSelectedTrackColor = activity.getResources().getColor(R.color.text_header);
        mDefaultTrackColor = activity.getResources().getColor(R.color.text_rowslave);
    }

    public void replaceTrackList(List<ClsTrack> data) {
        if (data == mItems) {
            return;
        }
        if (data != null) {
            mItems = data;
        } else {
            mItems = new ArrayList<ClsTrack>();
        }
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mItems.size();
    }

    @Override
    public Object getItem(int position) {
        return mItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).hashCode();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        CellViewHolder holder;

        if (convertView == null) {
            if (mInflater == null) {
                mInflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            }
            convertView = mInflater.inflate(R.layout.player_cell, null);

            holder = new CellViewHolder();
            holder.index = (TextView) convertView.findViewById(R.id.cell_index);
            holder.artist = (TextView) convertView.findViewById(R.id.cell_artist);
            holder.title = (TextView) convertView.findViewById(R.id.cell_title);
            holder.duration = (TextView) convertView.findViewById(R.id.cell_duration);
            holder.menu = (ImageView) convertView.findViewById(R.id.cell_menu);

            convertView.setTag(holder);
        } else {
            holder = (CellViewHolder) convertView.getTag();
        }

        final ClsTrack currentTrack = (ClsTrack) getItem(position);
        int sec = currentTrack.getDuration();
        int min = sec / 60;
        sec %= 60;

        holder.index.setText((position + 1) + ".");
        holder.artist.setText(currentTrack.getArtist());
        holder.title.setText(currentTrack.getTitle());
        holder.duration.setText(String.format("%2d:%02d", min, sec));
        holder.menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showContextMenu(v, currentTrack);
            }
        });

        int currentTrackColor = (position == ActPlayer.selected) ? mSelectedTrackColor : mDefaultTrackColor;

        holder.title.setTextColor(currentTrackColor);

        return convertView;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    private void showContextMenu(View anchorView, final ClsTrack o) {

        List<String> menuMusic = new ArrayList<String>();
        menuMusic.add(activity.getString(R.string.search) + ": " + o.getArtist());
        menuMusic.add(activity.getString(R.string.contextmenu_setasringtone));


        final ListPopupWindow
                popup = new ListPopupWindow(activity);
        popup.setAdapter(new ArrayAdapter<String>(activity, android.R.layout.simple_list_item_1, menuMusic.toArray(new String[menuMusic.size()])));
        popup.setAnchorView(anchorView);
        popup.setModal(true);
        popup.setWidth(Math.max(600, anchorView.getWidth()));

        popup.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                popup.dismiss();
                switch (position) {
                    case 0:
                        Intent intent = new Intent(activity, ActFreemporg.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        intent.putExtra("q", o.getArtist());
                        activity.startActivity(intent);
                        break;
                    case 1:
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            if (Settings.System.canWrite(activity)) {
                                AlertDialog.Builder bld = new AlertDialog.Builder(activity);
                                bld.setMessage(R.string.areyousure);
                                bld.setNeutralButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        MediaUtils.setRingtone(activity, o);
                                    }
                                });

                                bld.create().show();
                            }
                            else {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                    Intent inten = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
                                    inten.setData(Uri.parse("package:" + activity.getPackageName()));
                                    activity.startActivity(inten);
                                }
                            }
                        } else {
                            AlertDialog.Builder bld = new AlertDialog.Builder(activity);
                            bld.setMessage(R.string.areyousure);
                            bld.setNeutralButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    MediaUtils.setRingtone(activity, o);
                                }
                            });

                            bld.create().show();
                        }


                        break;
                }
            }
        });
        popup.show();
    }

    static class CellViewHolder {
        public TextView index;
        public TextView artist;
        public TextView title;
        public TextView duration;
        public ImageView menu;
    }


}