package org.freemp.droid.playlist.folders;

import android.Manifest;
import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.TextView;

import com.androidquery.AQuery;
import com.androidquery.util.AQUtility;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.karumi.dexter.listener.single.PermissionListener;

import org.freemp.droid.Constants;
import org.freemp.droid.R;
import org.freemp.droid.playlist.ActPlaylist;
import org.freemp.droid.playlist.AdpPlaylist;
import org.freemp.droid.playlist.ClsArrTrack;
import org.freemp.droid.playlist.TaskGetPlaylist;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by recoil on 29.05.14.
 */
public class FragmentFolders extends Fragment implements TaskGetPlaylist.OnTaskGetPlaylist {

    public AdpPlaylist adapter;
    public ArrayList<ClsArrTrack> items;
    private Activity activity;
    private AQuery aq;
    private ExpandableListView listView;

    public static FragmentFolders newInstance(CharSequence title) {
        Bundle bundle = new Bundle();
        bundle.putCharSequence(Constants.KEY_TITLE, title);

        FragmentFolders fragment = new FragmentFolders();
        fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.playlist, container, false);
    }

    @Override
    public void onActivityCreated(Bundle state) {
        super.onActivityCreated(state);
        activity = getActivity();
        items = new ArrayList<ClsArrTrack>();
        adapter = new AdpPlaylist(activity, items);
        listView.setAdapter(adapter);
    }

    @Override
    public void onViewCreated(android.view.View view, android.os.Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Bundle args = getArguments();

        if (args != null) {
            String title = "" + args.getCharSequence(Constants.KEY_TITLE);
        }
        listView = (ExpandableListView) view.findViewById(R.id.expandableListView);
        listView.setGroupIndicator(null);
        final TextView textView = (TextView) view.findViewById(R.id.textViewSave);
        textView.setClickable(true);
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((ActPlaylist) activity).save();
            }
        });

    }

    public void update(Activity activity, int type, boolean refresh) {
        if (items != null) {
            adapter.notifyDataSetInvalidated();
            items.clear();
            adapter.invalidate();
        }

        new TaskGetPlaylist(activity, type, refresh, FragmentFolders.this);


    }

    @Override
    public void onResume() {
        super.onResume();
        AQUtility.debug("onResume", "Folders");

    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void OnTaskResult(Object result) {
        if (null != result && isAdded()) {
            items.addAll((ArrayList<ClsArrTrack>) result);

            adapter.notifyDataSetChanged();
        }
    }
}