package com.dleibovych.epictale.service.notifier;

import android.app.PendingIntent;
import android.content.Context;

import com.dleibovych.epictale.R;
import com.dleibovych.epictale.TheTaleApplication;
import com.dleibovych.epictale.api.response.GameInfoResponse;
import com.dleibovych.epictale.fragment.GameFragment;
import com.dleibovych.epictale.util.PreferencesManager;
import com.dleibovych.epictale.util.UiUtils;
import com.dleibovych.epictale.util.onscreen.OnscreenPart;

/**
 * @author Hamster
 * @since 08.12.2014
 */
public class EnergyNotifier implements Notifier {

    private GameInfoResponse gameInfoResponse;

    @Override
    public void setInfo(GameInfoResponse gameInfoResponse) {
        this.gameInfoResponse = gameInfoResponse;
    }

    @Override
    public boolean isNotifying() {
        final int value = getValue();
        PreferencesManager.setLastNotificationEnergy(value);
        if((value >= PreferencesManager.getNotificationThresholdEnergy()) && (value != PreferencesManager.getLastShownNotificationEnergy())) {
            if(PreferencesManager.shouldNotifyEnergy()
                    && PreferencesManager.shouldShowNotificationEnergy()
                    && !TheTaleApplication.Companion.getOnscreenStateWatcher().isOnscreen(OnscreenPart.GAME_INFO)) {
                return true;
            }
            PreferencesManager.setShouldShowNotificationEnergy(false);
        } else {
            PreferencesManager.setShouldShowNotificationEnergy(true);
        }
        return false;
    }

    @Override
    public String getNotification(Context context) {
        return context.getString(R.string.notification_high_energy, getValue());
    }

    private int getValue() {
        return 0;
    }

    @Override
    public PendingIntent getPendingIntent(Context context) {
        return UiUtils.getApplicationIntent(context, GameFragment.GamePage.GAME_INFO, true);
    }

    @Override
    public void onNotificationDelete() {
        PreferencesManager.setShouldShowNotificationEnergy(false);
        PreferencesManager.setLastShownNotificationEnergy(PreferencesManager.getLastNotificationEnergy());
    }

}
