package ru.recoilme.freeamp.playlist.folders;

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
import ru.recoilme.freeamp.Constants;
import ru.recoilme.freeamp.R;
import ru.recoilme.freeamp.playlist.ActPlaylist;
import ru.recoilme.freeamp.playlist.AdpPlaylist;
import ru.recoilme.freeamp.playlist.ClsArrTrack;
import ru.recoilme.freeamp.playlist.TaskGetPlaylist;

import java.util.ArrayList;

/**
 * Created by recoil on 29.05.14.
 */
public class FragmentFolders extends Fragment implements TaskGetPlaylist.OnTaskGetPlaylist {

    private Activity activity;
    private AQuery aq;
    private ExpandableListView listView;

    public AdpPlaylist adapter;
    public ArrayList<ClsArrTrack> items;

    public static FragmentFolders newInstance(CharSequence title) {
        Bundle bundle = new Bundle();
        bundle.putCharSequence(Constants.KEY_TITLE, title);

        FragmentFolders fragment = new FragmentFolders();
        fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.view_playlist, container, false);
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
        super.onViewCreated(view,savedInstanceState);
        Bundle args = getArguments();

        if (args != null) {
            String title = ""+args.getCharSequence(Constants.KEY_TITLE);
        }
        listView = (ExpandableListView) view.findViewById(R.id.expandableListView);
        listView.setGroupIndicator(null);
        final TextView textView = (TextView)view.findViewById(R.id.textViewSave);
        textView.setClickable(true);
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((ActPlaylist) activity).save();
            }
        });

    }

    public void update(Activity activity, int type, boolean refresh) {
        if (items!=null) {
            adapter.notifyDataSetInvalidated();
            items.clear();
            adapter.invalidate();
        }
        TaskGetPlaylist taskGetPlaylist = new TaskGetPlaylist(activity,type,refresh, this);
        taskGetPlaylist.execute();
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
        if (null!=result && isAdded()) {
            items.addAll((ArrayList<ClsArrTrack>) result);

            adapter.notifyDataSetChanged();
        }
    }
}