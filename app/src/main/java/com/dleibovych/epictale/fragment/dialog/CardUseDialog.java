package com.dleibovych.epictale.fragment.dialog;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.dleibovych.epictale.R;
import com.dleibovych.epictale.TheTaleApplication;
import com.dleibovych.epictale.api.ApiResponseCallback;
import com.dleibovych.epictale.api.dictionary.CardTargetType;
import com.dleibovych.epictale.api.model.CardInfo;
import com.dleibovych.epictale.api.model.CouncilMemberInfo;
import com.dleibovych.epictale.api.model.PlaceInfo;
import com.dleibovych.epictale.api.request.PlaceRequest;
import com.dleibovych.epictale.api.request.PlacesRequest;
import com.dleibovych.epictale.api.request.UseCardRequest;
import com.dleibovych.epictale.api.response.CommonResponse;
import com.dleibovych.epictale.api.response.PlaceResponse;
import com.dleibovych.epictale.api.response.PlacesResponse;
import com.dleibovych.epictale.util.DialogUtils;
import com.dleibovych.epictale.util.RequestUtils;
import com.dleibovych.epictale.util.UiUtils;

import java.net.CookieManager;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import okhttp3.OkHttpClient;

/**
 * @author Hamster
 * @since 03.05.2015
 */
public class CardUseDialog extends BaseDialog {

    private static final String PARAM_TITLE = "PARAM_TITLE";
    private static final String PARAM_CARD = "PARAM_CARD";

    @Inject OkHttpClient client;
    @Inject CookieManager cookieManager;

    private Runnable onSuccess;
    
    private List<PlaceInfo> places;
    private Map<Integer, List<CouncilMemberInfo>> persons;
    private CardInfo card;

    private View viewAction;
    private View blockPlace;
    private View blockPerson;
    private TextView textPlace;
    private TextView textPerson;

    public static CardUseDialog newInstance(final String title, final CardInfo card) {
        final CardUseDialog dialog = new CardUseDialog();

        final Bundle args = new Bundle();
        args.putString(PARAM_TITLE, title);
        args.putParcelable(PARAM_CARD, card);

        dialog.setArguments(args);
        return dialog;
    }

    public void setOnSuccessListener(final Runnable listener) {
        this.onSuccess = listener;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ((TheTaleApplication)getActivity().getApplication())
                .getApplicationComponent()
                .inject(this);


        final View view = inflater.inflate(R.layout.dialog_content_card_use, container, false);

        card = getArguments().getParcelable(PARAM_CARD);
        final Spannable cardName = new SpannableString(card.name);
        cardName.setSpan(new ForegroundColorSpan(getResources().getColor(card.type.getRarity().getColorResId())),
                0, cardName.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        UiUtils.setText(view.findViewById(R.id.dialog_card_use_name), cardName);
        UiUtils.setText(view.findViewById(R.id.dialog_card_use_description), card.type.getDescription());

        persons = new HashMap<>();
        final boolean isPlacePresent = isPlacePresent(card.type.getTargetType());
        final boolean isPersonPresent = isPersonPresent(card.type.getTargetType());

        viewAction = view.findViewById(R.id.dialog_card_use_action);
        blockPlace = view.findViewById(R.id.dialog_card_use_place_block);
        blockPerson = view.findViewById(R.id.dialog_card_use_person_block);
        textPlace = view.findViewById(R.id.dialog_card_use_place);
        textPerson = view.findViewById(R.id.dialog_card_use_person);

        if(isPlacePresent) {
            viewAction.setEnabled(false);

            blockPlace.setVisibility(View.VISIBLE);
            textPlace.setEnabled(false);
            textPlace.setText(getString(R.string.common_loading));

            if(isPersonPresent) {
                blockPerson.setVisibility(View.VISIBLE);
                ((TextView) view.findViewById(R.id.dialog_card_use_person_title)).setText(getString(
                        card.type.getTargetType() == CardTargetType.BUILDING
                                ? R.string.game_card_use_building
                                : R.string.game_card_use_person));
                textPerson.setEnabled(false);
                textPerson.setText(getString(R.string.common_loading));
            } else {
                blockPerson.setVisibility(View.GONE);
            }

            new PlacesRequest(client, cookieManager).execute(RequestUtils.wrapCallback(new ApiResponseCallback<PlacesResponse>() {
                @Override
                public void processResponse(final PlacesResponse response) {
                    places = response.places;
                    Collections.sort(places, (lhs, rhs) -> lhs.name.compareTo(rhs.name));
                    final int count = places.size();
                    final String[] placeNames = new String[count];
                    for(int i = 0; i < count; i++) {
                        placeNames[i] = places.get(i).name;
                    }

                    textPlace.setEnabled(true);
                    onPlaceSelected(0);
                    textPlace.setOnClickListener(v -> DialogUtils.showChoiceDialog(
                            getChildFragmentManager(),
                            getString(R.string.game_card_use_place),
                            placeNames,
                            new ChoiceDialog.ItemChooseListener() {
                                @Override
                                public void onItemSelected(int position) {
                                    onPlaceSelected(position);
                                }
                            }));
                }

                @Override
                public void processError(PlacesResponse response) {
                    textPlace.setText(getString(R.string.common_error_hint));
                    if(isPersonPresent) {
                        textPerson.setText(getString(R.string.common_error_hint));
                    }
                }
            }, this));
        } else {
            blockPlace.setVisibility(View.GONE);
            blockPerson.setVisibility(View.GONE);
            viewAction.setEnabled(true);
            viewAction.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final ProgressDialog progressDialog = ProgressDialog.show(getActivity(),
                            getString(R.string.game_card_use),
                            getString(R.string.game_card_use_progress),
                            true, false);
                    new UseCardRequest(client, cookieManager).execute(card.id, getCardUseCallback(progressDialog));
                }
            });
        }

        return wrapView(inflater, view, getArguments().getString(PARAM_TITLE));
    }

    private boolean isPlacePresent(final CardTargetType targetType) {
        return (targetType == CardTargetType.PLACE)
                || (targetType == CardTargetType.PERSON)
                || (targetType == CardTargetType.BUILDING);
    }

    private boolean isPersonPresent(final CardTargetType targetType) {
        return (targetType == CardTargetType.PERSON)
                || (targetType == CardTargetType.BUILDING);
    }

    private void onPlaceSelected(final int placeIndex) {
        final PlaceInfo place = places.get(placeIndex);
        textPlace.setText(place.name);
        if(isPersonPresent(card.type.getTargetType())) {
            viewAction.setEnabled(false);
            textPerson.setEnabled(false);

            final List<CouncilMemberInfo> council = persons.get(placeIndex);
            if(council == null) {
                textPerson.setText(getString(R.string.common_loading));
                new PlaceRequest(client, cookieManager, place.id).execute(RequestUtils.wrapCallback(new ApiResponseCallback<PlaceResponse>() {
                    @Override
                    public void processResponse(PlaceResponse response) {
                        if(card.type.getTargetType() == CardTargetType.BUILDING) {
                            for(final Iterator<CouncilMemberInfo> councilIterator = response.council.iterator();
                                councilIterator.hasNext();) {
                                if(councilIterator.next().buildingId == null) {
                                    councilIterator.remove();
                                }
                            }
                        }
                        Collections.sort(response.council, new Comparator<CouncilMemberInfo>() {
                            @Override
                            public int compare(CouncilMemberInfo lhs, CouncilMemberInfo rhs) {
                                return Double.compare(rhs.power.power, lhs.power.power);
                            }
                        });
                        persons.put(placeIndex, response.council);
                        fillPersons(placeIndex);
                    }

                    @Override
                    public void processError(PlaceResponse response) {
                        textPerson.setText(getString(R.string.common_error_hint));
                    }
                }, this));
            } else {
                fillPersons(placeIndex);
            }
        } else {
            viewAction.setEnabled(true);
            viewAction.setOnClickListener(v -> {
                final ProgressDialog progressDialog = ProgressDialog.show(getActivity(),
                        getString(R.string.game_card_use),
                        getString(R.string.game_card_use_progress),
                        true, false);
                new UseCardRequest(client, cookieManager).execute(
                        card.id, card.type.getTargetType(), place.id,
                        getCardUseCallback(progressDialog));
            });
        }
    }

    private void fillPersons(final int placeIndex) {
        final List<CouncilMemberInfo> council = persons.get(placeIndex);
        final int personsCount = council.size();
        if(personsCount == 0) {
            textPerson.setEnabled(false);
            textPerson.setText(getString(R.string.game_card_use_no_buildings));
            return;
        }

        textPerson.setEnabled(true);
        final String[] personNames = new String[personsCount];
        for(int i = 0; i < personsCount; i++) {
            personNames[i] = council.get(i).name;
        }

        onPersonSelected(placeIndex, 0);
        textPerson.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogUtils.showChoiceDialog(
                        getChildFragmentManager(),
                        getString(card.type.getTargetType() == CardTargetType.BUILDING
                                ? R.string.game_card_use_building : R.string.game_card_use_person),
                        personNames,
                        new ChoiceDialog.ItemChooseListener() {
                            @Override
                            public void onItemSelected(int position) {
                                onPersonSelected(placeIndex, position);
                            }
                        });
            }
        });
    }

    private void onPersonSelected(final int placeIndex, final int personIndex) {
        final CouncilMemberInfo councilMemberInfo = persons.get(placeIndex).get(personIndex);
        textPerson.setText(councilMemberInfo.name);
        viewAction.setEnabled(true);
        viewAction.setOnClickListener(v -> {
            final ProgressDialog progressDialog = ProgressDialog.show(getActivity(),
                    getString(R.string.game_card_use),
                    getString(R.string.game_card_use_progress),
                    true, false);
            new UseCardRequest(client, cookieManager).execute(
                    card.id, card.type.getTargetType(),
                    card.type.getTargetType() == CardTargetType.BUILDING
                            ? councilMemberInfo.buildingId : councilMemberInfo.id,
                    getCardUseCallback(progressDialog));
        });
    }

    private ApiResponseCallback<CommonResponse> getCardUseCallback(final ProgressDialog progressDialog) {
        return RequestUtils.wrapCallback(new ApiResponseCallback<CommonResponse>() {
            @Override
            public void processResponse(CommonResponse response) {
                progressDialog.dismiss();
                dismiss();
                if(onSuccess != null) {
                    onSuccess.run();
                }
            }

            @Override
            public void processError(CommonResponse response) {
                progressDialog.dismiss();
                DialogUtils.showCommonErrorDialog(getActivity().getSupportFragmentManager(), getActivity());
                dismiss();
            }
        }, CardUseDialog.this);
    }

}
