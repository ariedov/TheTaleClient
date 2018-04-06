package com.dleibovych.epictale.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.dleibovych.epictale.util.PreferencesManager;

/**
 * @author Hamster
 * @since 26.11.2014
 */
public class BootBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if(Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            if(PreferencesManager.shouldServiceStartBoot()) {
                context.startService(new Intent(context, WatcherService.class));
            }
        }
    }

}
