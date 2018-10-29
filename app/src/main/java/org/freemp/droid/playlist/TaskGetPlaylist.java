package org.freemp.droid.playlist;

import android.Manifest;
import android.app.Activity;
import android.os.AsyncTask;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import org.freemp.droid.Constants;
import org.freemp.droid.playlist.albums.FragmentAlbums;
import org.freemp.droid.playlist.artists.FragmentArtists;
import org.freemp.droid.playlist.folders.FragmentFolders;

import java.lang.ref.WeakReference;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by recoil on 01.06.14.
 */
public class TaskGetPlaylist {

    private final WeakReference<Activity> mActivity;
    private final WeakReference<OnTaskGetPlaylist> mOnTaskGetPlaylist;
    private int type;
    private boolean refresh;

    public TaskGetPlaylist(Activity activity, int type, boolean refresh, OnTaskGetPlaylist onTaskGetPlaylist) {
        mActivity = new WeakReference<Activity>(activity);
        mOnTaskGetPlaylist = new WeakReference<OnTaskGetPlaylist>(onTaskGetPlaylist);
        String scanDir = PreferenceManager.getDefaultSharedPreferences(mActivity.get()).getString("scanDir", Environment.getExternalStorageDirectory().getAbsolutePath().toString());
        Log.w("ScanDir is:",scanDir);
        if (scanDir == null || scanDir.equals("/") || scanDir.equals("/sdcard") || scanDir.equals("/storage/emulated/0")) {
            type = 0;
        }
        this.type = type;
        this.refresh = refresh;

        int finalType = type;
        Dexter.withActivity(activity)
                .withPermissions(Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        getPlayList(activity, finalType, refresh, onTaskGetPlaylist)
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(object -> {
                                    OnTaskGetPlaylist onTaskGetPlaylist1 = mOnTaskGetPlaylist.get();
                                    Activity activityResult = mActivity.get();
                                    if (null != onTaskGetPlaylist1) {
                                        onTaskGetPlaylist1.OnTaskResult(object);
                                    }
                                    if (null != activityResult) {
                                        ((ActPlaylist) activityResult).setRefreshing(false);
                                        ((ActPlaylist) activityResult).setRefreshActionButtonState();
                                    }
                                });
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).check();


    }

    private Single<Object> getPlayList(Activity activity, final int type, final boolean refresh, OnTaskGetPlaylist onTaskGetPlaylist){
        return new Single<Object>() {
            @Override
            protected void subscribeActual(SingleObserver<? super Object> observer) {
                Activity activity = mActivity.get();
                if (null == activity) {
                    observer.onError(new Exception("Activity is null"));
                }
                switch (type) {
                    case Constants.TYPE_MS:
                        observer.onSuccess((new MakePlaylistMS(activity, refresh)).getArrTracks());
                        return ;
                    case Constants.TYPE_FS:
                        observer.onSuccess(( new MakePlaylistFS(activity, refresh).getArrTracks()));
                    default:
                        observer.onSuccess(( new MakePlaylistMS(activity, refresh).getArrTracks()));
                }
            }
        };
    }


    public static interface OnTaskGetPlaylist {
        public void OnTaskResult(Object result);
    }
}