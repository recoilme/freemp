package ru.recoilme.freeamp.playlist.albums;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.androidquery.AQuery;
import com.androidquery.util.AQUtility;
import ru.recoilme.freeamp.ClsTrack;
import ru.recoilme.freeamp.Constants;
import ru.recoilme.freeamp.FillMediaStoreTracks;
import ru.recoilme.freeamp.playlist.ActPlaylist;
import ru.recoilme.freeamp.playlist.TaskGetAlbums;

import java.util.ArrayList;

/**
 * Created by recoil on 02.06.14.
 */
public class FragmentAlbums extends Fragment implements TaskGetAlbums.OnTaskGetAlbums{

    private Activity activity;
    private AQuery aq;

    //UI
    private GridView gridView;
    private AdpArtworks adapter;
    private ProgressBar progressBar;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        activity = getActivity();
        aq = new AQuery(activity);

        //UI
        final LinearLayout linearLayout = new LinearLayout(activity);
        linearLayout.setOrientation(LinearLayout.VERTICAL);

        //Progress
        progressBar = new ProgressBar(activity,null,android.R.attr.progressBarStyleHorizontal);
        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        progressBar.setLayoutParams(layoutParams);
        progressBar.setVisibility(View.GONE);
        gridView = new GridView(activity);

        linearLayout.addView(progressBar);
        linearLayout.addView(gridView);
        return linearLayout;
    }

    @Override
    public void onViewCreated(android.view.View view, android.os.Bundle savedInstanceState) {
        super.onViewCreated(view,savedInstanceState);
        Bundle args = getArguments();

        if (args != null) {
            String title = ""+args.getCharSequence(Constants.KEY_TITLE);
        }
        update(activity,1,false);
    }

    public void update(Activity activity, int type, boolean refresh) {

        TaskGetAlbums taskGetAlbums = new TaskGetAlbums(activity,type,refresh, this);
        taskGetAlbums.execute();
    }

    @Override
    public void OnTaskResult(Object result) {
        if (null!=result && isAdded()) {
            ArrayList<ClsTrack> allTracks = (ArrayList<ClsTrack>) result;
            AQUtility.debug("Alb OnTaskResult",allTracks.size());
            applyAdapter(allTracks);
        }
    }

    void applyAdapter(ArrayList<ClsTrack> tracks) {
        if (tracks == null) return;
        adapter = new AdpArtworks(activity,tracks);
        int iDisplayWidth = getResources().getDisplayMetrics().widthPixels ;
        int numColumns = iDisplayWidth / 310;
        gridView.setColumnWidth( (iDisplayWidth / numColumns) );
        gridView.setNumColumns(numColumns);
        gridView.setStretchMode( GridView.NO_STRETCH ) ;
        gridView.setAdapter(adapter);
        gridView.invalidateViews();
        gridView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                adapter.setScrollState(scrollState);
                if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
                    adapter.notifyDataSetChanged();
                    gridView.invalidateViews();
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

            }
        });
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ClsTrack track = (ClsTrack) adapter.getItem(position);
                final String album = track.getAlbum();
                final String artist = track.getArtist();
                ArrayList<ClsTrack> tracks = new FillMediaStoreTracks(activity).getTracks();
                ArrayList<ClsTrack> tracksFiltered = new ArrayList<ClsTrack>();
                for(ClsTrack t: tracks) {
                    if (t.getAlbum().equals(album) && t.getArtist().equals(artist)) {
                        tracksFiltered.add(t);
                    }
                }
                ((ActPlaylist)activity).close(tracksFiltered);
            }
        });
    }
}