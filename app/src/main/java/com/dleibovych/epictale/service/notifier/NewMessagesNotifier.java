package com.dleibovych.epictale.service.notifier;

import android.app.PendingIntent;
import android.content.Context;

import com.dleibovych.epictale.R;
import com.dleibovych.epictale.TheTaleApplication;
import com.dleibovych.epictale.api.response.GameInfoResponse;
import com.dleibovych.epictale.game.GameFragment;
import com.dleibovych.epictale.util.PreferencesManager;
import com.dleibovych.epictale.util.UiUtils;
import com.dleibovych.epictale.util.onscreen.OnscreenPart;

/**
 * @author Hamster
 * @since 08.12.2014
 */
public class NewMessagesNotifier implements Notifier {

    private GameInfoResponse gameInfoResponse;

    @Override
    public void setInfo(GameInfoResponse gameInfoResponse) {
        this.gameInfoResponse = gameInfoResponse;
    }

    @Override
    public boolean isNotifying() {
        final int value = getValue();
        PreferencesManager.setLastNotificationNewMessages(value);
        if((value > 0) && (value != PreferencesManager.getLastShownNotificationNewMessages())) {
            if(PreferencesManager.shouldNotifyNewMessages()
                    && PreferencesManager.shouldShowNotificationNewMessages()
                    && !TheTaleApplication.Companion.getOnscreenStateWatcher().isOnscreen(OnscreenPart.GAME_INFO)) {
                return true;
            }
            PreferencesManager.setShouldShowNotificationNewMessages(false);
        } else {
            PreferencesManager.setShouldShowNotificationNewMessages(true);
        }
        return false;
    }

    @Override
    public String getNotification(Context context) {
        return context.getString(R.string.notification_new_messages, getValue());
    }

    private int getValue() {
        return gameInfoResponse.account.newMessagesCount;
    }

    @Override
    public PendingIntent getPendingIntent(Context context) {
        return UiUtils.getApplicationIntent(context, GameFragment.GamePage.GAME_INFO, true);
    }

    @Override
    public void onNotificationDelete() {
        PreferencesManager.setShouldShowNotificationNewMessages(false);
        PreferencesManager.setLastShownNotificationNewMessages(PreferencesManager.getLastNotificationNewMessages());
    }

}
