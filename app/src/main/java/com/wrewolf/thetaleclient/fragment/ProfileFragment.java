package com.wrewolf.thetaleclient.fragment;

import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.TextView;

import com.wrewolf.thetaleclient.DataViewMode;
import com.wrewolf.thetaleclient.R;
import com.wrewolf.thetaleclient.TheTaleClientApplication;
import com.wrewolf.thetaleclient.api.ApiResponseCallback;
import com.wrewolf.thetaleclient.api.dictionary.RatingItem;
import com.wrewolf.thetaleclient.api.model.AccountPlaceHistoryInfo;
import com.wrewolf.thetaleclient.api.model.RatingItemInfo;
import com.wrewolf.thetaleclient.api.request.AccountInfoRequest;
import com.wrewolf.thetaleclient.api.response.AccountInfoResponse;
import com.wrewolf.thetaleclient.util.PreferencesManager;
import com.wrewolf.thetaleclient.util.UiUtils;

import java.net.CookieManager;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;

import javax.inject.Inject;

import okhttp3.OkHttpClient;

/**
 * @author Hamster
 * @since 20.02.2015
 */
public class ProfileFragment extends WrapperFragment {

    private static final int NARROWNESS_MULTIPLIER_THRESHOLD = 30;
    private static final int PLACES_HISTORY_COUNT_POLITICS = 10;

    @Inject OkHttpClient client;
    @Inject CookieManager manager;

    private LayoutInflater layoutInflater;
    private View rootView;

    private TextView textName;
    private TextView textAffectGame;
    private TextView textMight;
    private TextView textAchievementPoints;
    private TextView textCollectionItemsCount;
    private TextView textReferralsCount;
    private ViewGroup tableRatings;
    private TextView textRatingsDescription;
    private ViewGroup tablePlacesHistory;
    private TextView tablePlacesHistorySwitcher;

    private boolean isNarrowMode;
    private boolean isTablePlacesHistoryCollapsed;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        TheTaleClientApplication
                .getComponentProvider()
                .getAppComponent()
                .inject(this);


        layoutInflater = inflater;
        rootView = inflater.inflate(R.layout.fragment_profile, container, false);

        textName = rootView.findViewById(R.id.profile_name);
        textAffectGame = rootView.findViewById(R.id.profile_affect_game);
        textMight = rootView.findViewById(R.id.profile_might);
        textAchievementPoints = rootView.findViewById(R.id.profile_achievement_points);
        textCollectionItemsCount = rootView.findViewById(R.id.profile_collection_items_count);
        textReferralsCount = rootView.findViewById(R.id.profile_referrals_count);
        tableRatings = rootView.findViewById(R.id.profile_container_ratings);
        textRatingsDescription = rootView.findViewById(R.id.profile_ratings_description);
        tablePlacesHistory = rootView.findViewById(R.id.profile_container_places_history);
        tablePlacesHistorySwitcher = rootView.findViewById(R.id.profile_container_places_history_switcher);

        return wrapView(layoutInflater, rootView);
    }

    @Override
    public void refresh(final boolean isGlobal) {
        super.refresh(isGlobal);

        if(isGlobal) {
            isTablePlacesHistoryCollapsed = true;
            isNarrowMode = true;
        }

        final int watchingAccountId = PreferencesManager.getWatchingAccountId();
        final int accountId = watchingAccountId == 0 ? PreferencesManager.getAccountId() : watchingAccountId;
        new AccountInfoRequest(client, manager, accountId).execute(new ApiResponseCallback<AccountInfoResponse>() {
            @Override
            public void processResponse(final AccountInfoResponse response) {
                if(!isAdded()) {
                    return;
                }

                if(isGlobal) {
                    tableRatings.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                        @Override
                        public void onGlobalLayout() {
                            if(tableRatings.getWidth() > 0) {
                                final Rect textRect = new Rect();
                                textRatingsDescription.getPaint().getTextBounds("M", 0, 1, textRect);
                                isNarrowMode = ((double) tableRatings.getWidth() / textRect.width()) < NARROWNESS_MULTIPLIER_THRESHOLD;

                                UiUtils.removeGlobalLayoutListener(tableRatings, this);
                                if(!isNarrowMode) {
                                    fillRatings(response);
                                }
                            }
                        }
                    });
                }

                textName.setText(Html.fromHtml(getString(R.string.find_player_info_short, response.name)));
                textAffectGame.setText(getString(response.canAffectGame ? R.string.game_affect_true : R.string.game_affect_false));
                textMight.setText(String.valueOf((int) Math.floor(response.might)));
                textAchievementPoints.setText(String.valueOf(response.achievementPoints));
                textCollectionItemsCount.setText(String.valueOf(response.collectionItemsCount));
                textReferralsCount.setText(String.valueOf(response.referralsCount));

                fillRatings(response);
                fillPlacesHistory(response);

                setMode(DataViewMode.DATA);
            }

            @Override
            public void processError(AccountInfoResponse response) {
                setError(response.errorMessage);
            }
        });
    }

    private View getTableRow(final CharSequence text1, final CharSequence text2, final CharSequence text3) {
        final View row = layoutInflater.inflate(R.layout.item_profile_table, null);
        ((TextView) row.findViewById(R.id.item_profile_table_text_1)).setText(text1);
        ((TextView) row.findViewById(R.id.item_profile_table_text_2)).setText(text2);
        ((TextView) row.findViewById(R.id.item_profile_table_text_3)).setText(text3);
        return row;
    }

    private void fillRatings(final AccountInfoResponse accountInfoResponse) {
        tableRatings.removeAllViews();

        final Spannable captionRatingValue;
        if(isNarrowMode) {
            captionRatingValue = null;
        } else {
            captionRatingValue = new SpannableString(getString(R.string.profile_rating_caption_value));
            captionRatingValue.setSpan(new StyleSpan(Typeface.BOLD), 0, captionRatingValue.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        final Spannable captionRatingName = new SpannableString(getString(R.string.profile_rating_caption_name));
        captionRatingName.setSpan(new StyleSpan(Typeface.BOLD), 0, captionRatingName.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        final Spannable captionRatingPlace = new SpannableString(getString(R.string.profile_rating_caption_place));
        captionRatingPlace.setSpan(new StyleSpan(Typeface.BOLD), 0, captionRatingPlace.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        tableRatings.addView(getTableRow(captionRatingName, captionRatingValue, captionRatingPlace));

        for(final Map.Entry<RatingItem, RatingItemInfo> ratingEntry : accountInfoResponse.ratings.entrySet()) {
            layoutInflater.inflate(R.layout.item_profile_table_delimiter, tableRatings, true);
            final RatingItemInfo ratingItemInfo = ratingEntry.getValue();
            final String value = ratingEntry.getKey().getValue(ratingItemInfo.value);
            final String place;
            if(isNarrowMode) {
                place = ratingItemInfo.value == 0 ?
                        getString(R.string.profile_rating_item_no_place_short) :
                        String.valueOf(ratingItemInfo.place);
            } else {
                place = ratingItemInfo.value == 0 ?
                        getString(R.string.profile_rating_item_no_place) :
                        getString(R.string.profile_rating_item_place, ratingItemInfo.place);
            }
            tableRatings.addView(getTableRow(ratingItemInfo.name, value, place));
        }
    }

    private void fillPlacesHistory(final AccountInfoResponse accountInfoResponse) {
        tablePlacesHistory.removeAllViews();

        final Spannable captionPlacesHistoryName = new SpannableString(getString(R.string.profile_places_history_caption_name));
        captionPlacesHistoryName.setSpan(new StyleSpan(Typeface.BOLD), 0, captionPlacesHistoryName.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        final Spannable captionPlacesHistoryValue = new SpannableString(getString(R.string.profile_places_history_caption_value));
        captionPlacesHistoryValue.setSpan(new StyleSpan(Typeface.BOLD), 0, captionPlacesHistoryValue.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        final Spannable captionPlacesHistoryPlace = new SpannableString(getString(R.string.profile_places_history_caption_place));
        captionPlacesHistoryPlace.setSpan(new StyleSpan(Typeface.BOLD), 0, captionPlacesHistoryPlace.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        tablePlacesHistory.addView(getTableRow(captionPlacesHistoryPlace, captionPlacesHistoryName, captionPlacesHistoryValue));

        final int size = accountInfoResponse.placesHistory.size();
        if(size > 0) {
            Collections.sort(accountInfoResponse.placesHistory, new Comparator<AccountPlaceHistoryInfo>() {
                @Override
                public int compare(AccountPlaceHistoryInfo lhs, AccountPlaceHistoryInfo rhs) {
                    if (lhs.helpCount == rhs.helpCount) {
                        return lhs.name.compareToIgnoreCase(rhs.name);
                    } else {
                        return rhs.helpCount - lhs.helpCount;
                    }
                }
            });
            int lastCount = accountInfoResponse.placesHistory.get(0).helpCount;
            for (int i = 0; i < size; i++) {
                final AccountPlaceHistoryInfo accountPlaceHistoryInfo = accountInfoResponse.placesHistory.get(i);
                if(isTablePlacesHistoryCollapsed) {
                    if ((i >= PLACES_HISTORY_COUNT_POLITICS) && (accountPlaceHistoryInfo.helpCount < lastCount)) {
                        break;
                    } else {
                        lastCount = accountPlaceHistoryInfo.helpCount;
                    }
                }

                layoutInflater.inflate(R.layout.item_profile_table_delimiter, tablePlacesHistory, true);
                tablePlacesHistory.addView(getTableRow(String.valueOf(i + 1),
                        accountPlaceHistoryInfo.name, String.valueOf(accountPlaceHistoryInfo.helpCount)));
            }
        }

        tablePlacesHistorySwitcher.setText(getString(isTablePlacesHistoryCollapsed ?
                R.string.profile_places_history_expand :
                R.string.profile_places_history_collapse));
        tablePlacesHistorySwitcher.setOnClickListener(v -> {
            isTablePlacesHistoryCollapsed = !isTablePlacesHistoryCollapsed;
            fillPlacesHistory(accountInfoResponse);
        });
    }

}
