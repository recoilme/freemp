package org.freemp.droid.player;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.view.KeyEvent;

/**
 * Created by recoilme on 21/01/14.
 */
public class RcvMediaControl extends BroadcastReceiver {


    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_MEDIA_BUTTON)) {
            KeyEvent event = (KeyEvent) intent
                    .getParcelableExtra(Intent.EXTRA_KEY_EVENT);
            if (event == null) {
                return;
            }
            if (event.getAction() != KeyEvent.ACTION_DOWN) {
                return;
            }

            switch (event.getKeyCode()) {
                case KeyEvent.KEYCODE_MEDIA_STOP:
                case KeyEvent.KEYCODE_HEADSETHOOK:
                case KeyEvent.KEYCODE_MEDIA_PLAY:
                    sendMessage(context, "play");
                    break;
                case KeyEvent.KEYCODE_MEDIA_PAUSE:
                    sendMessage(context, "play");
                    break;
                case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:

                    sendMessage(context, "play");
                    break;
                case KeyEvent.KEYCODE_MEDIA_NEXT:
                    sendMessage(context, "next");
                    break;
                case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
                    sendMessage(context, "prev");
                    break;
                case KeyEvent.KEYCODE_VOLUME_UP:
                    sendMessage(context, "voup");
                    break;
                case KeyEvent.KEYCODE_VOLUME_DOWN:
                    sendMessage(context, "vodn");
                    break;
            }
        }

    }

    void sendMessage(Context context, String msg) {
        Intent sendIntent = null;
        PendingIntent pendingIntent = null;
        sendIntent = new Intent(msg);
        sendIntent.setComponent(new ComponentName(context, ServicePlayer.class));
        pendingIntent = PendingIntent.getService(context, 0, sendIntent, 0);
        try {
            pendingIntent.send();
        } catch (PendingIntent.CanceledException e) {
            e.printStackTrace();
        }
    }
}
