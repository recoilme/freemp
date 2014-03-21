package ru.recoilme.freeamp.playlist;

import android.content.Context;
import android.preference.PreferenceManager;
import com.faceture.google.play.*;
import ru.recoilme.freeamp.ClsTrack;
import ru.recoilme.freeamp.FileUtils;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collection;

//import static com.android.gm.api.GoogleMusicApi.*;

/**
 * Created by recoilme on 09/12/13.
 */
public class MakePlaylistGM extends MakePlaylistAbstract {

    public MakePlaylistGM(Context context, boolean refresh) {
        super(context,refresh);
    }

    @Override
    public void getAllTracks(Context context,boolean refresh) {
        PlayClient playClient = new PlayClientBuilder().create();
        LoginResponse loginResponse = null;
        try {
            loginResponse = playClient.login(PreferenceManager.getDefaultSharedPreferences(context).getString("mEmail",""),
                    PreferenceManager.getDefaultSharedPreferences(context).getString("mPassword",""));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        if (loginResponse!=null && loginResponse.getLoginResult()== LoginResult.SUCCESS) {
            PlaySession playSession = loginResponse.getPlaySession();
            FileUtils.writeObject("playSession",context,playSession);

            Collection<com.faceture.google.play.domain.Song> gmTracks = null;

            try {
                gmTracks = playClient.loadAllTracks(loginResponse.getPlaySession());
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (gmTracks!=null && gmTracks.size()>0) {
                for (com.faceture.google.play.domain.Song song:gmTracks) {
                    String storeId = (""+song.getStoreId()).toUpperCase();
                    if (storeId.length()>0 && storeId.substring(0,1).equals("T")) {
                        continue; //not your tracks
                    }
                    ClsTrack t = new ClsTrack(
                            song.getArtist(),
                            song.getTitle(),
                            song.getAlbum(),
                            song.getComposer(),
                            song.getYear(),
                            song.getTrack(),
                            (int)song.getDurationMillis(),
                            "gmid:"+song.getId(),
                            song.getArtist()+" - "+song.getAlbum(),
                            (long)song.getCreationDate(),
                            0);
                    allTracks.add(t);

                }
            }
        }

    }
}
