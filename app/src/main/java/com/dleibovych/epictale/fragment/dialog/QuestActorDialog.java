package com.dleibovych.epictale.fragment.dialog;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dleibovych.epictale.R;
import com.dleibovych.epictale.TheTaleApplication;
import com.dleibovych.epictale.game.MainActivity;
import com.dleibovych.epictale.api.CommonResponseCallback;
import com.dleibovych.epictale.api.cache.prerequisite.GameInfoPrerequisiteRequest;
import com.dleibovych.epictale.api.model.QuestActorInfo;
import com.dleibovych.epictale.api.request.MapRequest;
import com.dleibovych.epictale.api.response.MapResponse;
import com.dleibovych.epictale.util.PreferencesManager;
import com.dleibovych.epictale.util.RequestUtils;
import com.dleibovych.epictale.util.UiUtils;

import java.net.CookieManager;

import javax.inject.Inject;

import okhttp3.OkHttpClient;

/**
 * @author Hamster
 * @since 10.10.2014
 */
public class QuestActorDialog extends BaseDialog {

    private static final String PARAM_QUEST_ACTOR_INFO = "PARAM_QUEST_ACTOR_INFO";

    @Inject OkHttpClient client;
    @Inject CookieManager manager;

    public static QuestActorDialog newInstance(final QuestActorInfo questActorInfo) {
        final QuestActorDialog dialog = new QuestActorDialog();

        Bundle args = new Bundle();
        args.putParcelable(PARAM_QUEST_ACTOR_INFO, questActorInfo);

        dialog.setArguments(args);
        return dialog;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        TheTaleApplication
                .getComponentProvider()
                .getAppComponent()
                .inject(this);

        final QuestActorInfo questActorInfo = getArguments().getParcelable(PARAM_QUEST_ACTOR_INFO);
        final View view;

        switch(questActorInfo.type) {
            case PERSON:
                view = inflater.inflate(R.layout.dialog_content_quest_actor_person, container, false);
                UiUtils.setText(view.findViewById(R.id.dialog_quest_actor_person_name),
                        UiUtils.getInfoItem(getString(R.string.quest_actor_name), questActorInfo.personInfo.name));
                UiUtils.setText(view.findViewById(R.id.dialog_quest_actor_person_race),
                        UiUtils.getInfoItem(getString(R.string.quest_actor_race), questActorInfo.personInfo.race.getName()));
                UiUtils.setText(view.findViewById(R.id.dialog_quest_actor_person_gender),
                        UiUtils.getInfoItem(getString(R.string.quest_actor_gender), questActorInfo.personInfo.gender.getName()));
                UiUtils.setText(view.findViewById(R.id.dialog_quest_actor_person_profession),
                        UiUtils.getInfoItem(getString(R.string.quest_actor_profession), questActorInfo.personInfo.profession.getName()));
                UiUtils.setText(view.findViewById(R.id.dialog_quest_actor_person_mastery),
                        UiUtils.getInfoItem(getString(R.string.quest_actor_mastery), questActorInfo.personInfo.mastery));
                new GameInfoPrerequisiteRequest(client, manager, () -> new MapRequest(PreferencesManager.getMapVersion()).execute(RequestUtils.wrapCallback(new CommonResponseCallback<MapResponse, String>() {
                    @Override
                    public void processResponse(MapResponse response) {
                        setPlaceLink(
                                view.findViewById(R.id.dialog_quest_actor_person_place),
                                getString(R.string.quest_actor_place),
                                response.places.get(questActorInfo.personInfo.placeId).name,
                                questActorInfo.personInfo.placeId);
                    }

                    @Override
                    public void processError(String error) {
                    }
                }, QuestActorDialog.this)), null, this).execute();
                break;

            case PLACE:
                view = inflater.inflate(R.layout.dialog_content_quest_actor_place, container, false);
                setPlaceLink(
                        view.findViewById(R.id.dialog_quest_actor_place_name),
                        getString(R.string.map_place_name),
                        questActorInfo.placeInfo.name,
                        questActorInfo.placeInfo.id);
                new GameInfoPrerequisiteRequest(client, manager, () -> new MapRequest(PreferencesManager.getMapVersion()).execute(RequestUtils.wrapCallback(new CommonResponseCallback<MapResponse, String>() {
                    @Override
                    public void processResponse(MapResponse response) {
                        UiUtils.setText(view.findViewById(R.id.dialog_quest_actor_place_size), UiUtils.getInfoItem(
                                getString(R.string.map_place_size),
                                String.valueOf(response.places.get(questActorInfo.placeInfo.id).size)));
                    }

                    @Override
                    public void processError(String error) {
                    }
                }, QuestActorDialog.this)), null, this).execute();
                break;

            case SPENDING:
                view = inflater.inflate(R.layout.dialog_content_quest_actor_spending, container, false);
                UiUtils.setText(view.findViewById(R.id.dialog_quest_actor_spending_description),
                        questActorInfo.spendingInfo.description);
                break;

            default:
                view = null;
                break;
        }

        return wrapView(inflater, view, questActorInfo.type.getName());
    }

    private void setPlaceLink(final View view, final CharSequence caption, final CharSequence info, final int placeId) {
        final Activity activity = getActivity();
        if(!(activity instanceof MainActivity)) {
            return;
        }

        final Spannable linkText = new SpannableString(info);
        linkText.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.common_link)),
                0, linkText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        UiUtils.setText(view, UiUtils.getInfoItem(caption, linkText));
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PreferencesManager.setMapCenterPlaceId(placeId);
                dismiss();
//                ((MainActivity) activity).onNavigationDrawerItemSelected(DrawerItem.MAP);
            }
        });
    }

}
