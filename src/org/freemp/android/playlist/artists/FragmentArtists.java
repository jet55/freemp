package org.freemp.android.playlist.artists;

import android.app.Activity;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import com.androidquery.AQuery;
import com.androidquery.util.AQUtility;
import org.freemp.android.ClsTrack;
import org.freemp.android.Constants;
import org.freemp.android.FileUtils;
import org.freemp.android.playlist.ActPlaylist;
import org.freemp.android.playlist.TaskGetArtists;

import java.util.ArrayList;

/**
 * Created by recoil on 02.06.14.
 */
public class FragmentArtists extends Fragment implements TaskGetArtists.OnTaskGetArtists, TaskGetArtists.OnProgressUpdateMy{

    private Activity activity;
    private AQuery aq;

    //UI
    private GridView gridView;
    public AdpArtists adapter;
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
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (adapter==null) return;
                ClsTrack track = (ClsTrack) adapter.getItem(position);
                final String artist = track.getArtist();
                ArrayList<ClsTrack> tracks = (ArrayList<ClsTrack>) FileUtils.readObject("alltracksfs", activity);


                ArrayList<ClsTrack> tracksFiltered = new ArrayList<ClsTrack>();
                for(ClsTrack t: tracks) {
                    if (t.getArtist().equalsIgnoreCase(artist)) {
                        tracksFiltered.add(t);
                    }
                }
                ((ActPlaylist)activity).close(tracksFiltered);
            }
        });

        linearLayout.addView(progressBar);
        linearLayout.addView(gridView);
        return linearLayout;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view,savedInstanceState);
        Bundle args = getArguments();

        if (args != null) {
            String title = ""+args.getCharSequence(Constants.KEY_TITLE);
        }
        //ArrayList<ClsTrack> albumsTracks = (ArrayList<ClsTrack>) FileUtils.readObject("albumsTracks", activity);
        //if (albumsTracks!=null && albumsTracks.size()>0) {
        OnTaskResult((ArrayList<ClsTrack>) FileUtils.readObject("artistsTracks", activity));
        //}
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        updateGridView();
    }

    public void update(Activity activity, int type, boolean refresh) {
        if (progressBar!=null && progressBar.getVisibility()==View.GONE) {
            progressBar.setVisibility(View.VISIBLE);
        }
        TaskGetArtists taskGetArtists = new TaskGetArtists(activity,type,refresh, this,this);
        taskGetArtists.execute();
    }

    @Override
    public void OnTaskResult(Object result) {
        if (null!=result && isAdded()) {
            ArrayList<ClsTrack> allTracks = (ArrayList<ClsTrack>) result;
            applyAdapter(allTracks);
            if (progressBar.getVisibility()==View.VISIBLE) {
                progressBar.setVisibility(View.GONE);
            }
        }
    }

    void updateGridView() {
        int iDisplayWidth = Math.max(320,getResources().getDisplayMetrics().widthPixels);
        int numColumns = iDisplayWidth / 310;
        gridView.setColumnWidth( (iDisplayWidth / numColumns) );
        gridView.setNumColumns(numColumns);
        gridView.setStretchMode( GridView.NO_STRETCH ) ;
        gridView.invalidateViews();
    }

    void applyAdapter(ArrayList<ClsTrack> tracks) {
        if (tracks == null) return;
        adapter = new AdpArtists(activity,tracks);
        gridView.setAdapter(adapter);
        updateGridView();
    }

    @Override
    public void OnArtistsProgress(final int progress) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressBar.setProgress(progress);
            }
        });

    }
    @Override
    public void onResume() {
        super.onResume();
        AQUtility.debug("onResume", "Albums");

    }
}