package com.wrewolf.thetaleclient.service.watcher;

import com.wrewolf.thetaleclient.api.request.TakeCardRequest;
import com.wrewolf.thetaleclient.api.response.GameInfoResponse;
import com.wrewolf.thetaleclient.util.PreferencesManager;

import java.net.CookieManager;

import okhttp3.OkHttpClient;

/**
 * @author Hamster
 * @since 14.11.2014
 */
public class CardTaker implements GameStateWatcher {

    private final OkHttpClient client;
    private final CookieManager cookieManager;

    public CardTaker(OkHttpClient client, CookieManager cookieManager) {
        this.client = client;
        this.cookieManager = cookieManager;
    }


    @Override
    public void processGameState(GameInfoResponse gameInfoResponse) {
        if(PreferencesManager.shouldAutoactionCardTake()) {
            if(gameInfoResponse.account.hero.cards.cardHelpCurrent >= gameInfoResponse.account.hero.cards.cardHelpBarrier) {
                new TakeCardRequest(client, cookieManager).execute(null);
            }
        }
    }

}
