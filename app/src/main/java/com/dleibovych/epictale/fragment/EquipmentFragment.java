package com.dleibovych.epictale.fragment;

import android.graphics.Typeface;
import android.os.Bundle;
import androidx.annotation.NonNull;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.dleibovych.epictale.DataViewMode;
import com.dleibovych.epictale.R;
import com.dleibovych.epictale.TheTaleApplication;
import com.dleibovych.epictale.api.ApiResponseCallback;
import com.dleibovych.epictale.api.cache.prerequisite.InfoPrerequisiteRequest;
import com.dleibovych.epictale.api.cache.prerequisite.PrerequisiteRequest;
import org.thetale.api.enumerations.Action;
import org.thetale.api.enumerations.ArtifactEffect;
import org.thetale.api.enumerations.ArtifactType;
import org.thetale.api.enumerations.EquipmentType;
import com.dleibovych.epictale.api.model.ArtifactInfo;
import com.dleibovych.epictale.api.request.AbilityUseRequest;
import com.dleibovych.epictale.api.request.GameInfoRequest;
import com.dleibovych.epictale.api.response.CommonResponse;
import com.dleibovych.epictale.api.response.GameInfoResponse;
import com.dleibovych.epictale.api.response.InfoResponse;
import com.dleibovych.epictale.util.DialogUtils;
import com.dleibovych.epictale.util.ObjectUtils;
import com.dleibovych.epictale.util.PreferencesManager;
import com.dleibovych.epictale.util.RequestUtils;
import com.dleibovych.epictale.util.UiUtils;
import com.dleibovych.epictale.widget.RequestActionView;

import java.net.CookieManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import okhttp3.OkHttpClient;

/**
 * @author Hamster
 * @since 06.10.2014
 */
public class EquipmentFragment extends WrapperFragment {

    @Inject
    OkHttpClient client;
    @Inject
    CookieManager manager;

    private LayoutInflater layoutInflater;

    private View rootView;

    private View equipmentEffects;
    private ViewGroup equipmentContainer;
    private TextView bagCaption;
    private ViewGroup bagContainer;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ((TheTaleApplication) getActivity().getApplication())
                .getApplicationComponent()
                .inject(this);


        layoutInflater = inflater;
        rootView = layoutInflater.inflate(R.layout.fragment_equipment, container, false);

        equipmentEffects = rootView.findViewById(R.id.equipment_effects);
        equipmentContainer = rootView.findViewById(R.id.equipment_container);
        bagCaption = rootView.findViewById(R.id.bag_caption);
        bagContainer = rootView.findViewById(R.id.bag_container);

        return wrapView(layoutInflater, rootView);
    }

    @Override
    public void refresh(final boolean showLoading) {
        super.refresh(showLoading);

        final ApiResponseCallback<GameInfoResponse> callback = RequestUtils.wrapCallback(new ApiResponseCallback<GameInfoResponse>() {
            @Override
            public void processResponse(GameInfoResponse response) {
                equipmentContainer.removeAllViews();
                final List<ArtifactEffect> equipmentEffectsList = new ArrayList<>();
                for (final EquipmentType equipmentType : EquipmentType.values()) {
                    final View equipmentEntryView = layoutInflater.inflate(R.layout.item_equipment, equipmentContainer, false);
                    final ArtifactInfo artifactInfo = response.account.hero.equipment.get(equipmentType);

                    final ImageView imageIcon = equipmentEntryView.findViewById(R.id.equipment_icon);
                    final TextView textName = equipmentEntryView.findViewById(R.id.equipment_name);
                    final TextView textPower = equipmentEntryView.findViewById(R.id.equipment_power);

                    imageIcon.setImageResource(equipmentType.getDrawableResId());

                    if (artifactInfo == null) {
                        textName.setVisibility(View.GONE);
                        textPower.setVisibility(View.GONE);
                    } else {
                        final Spanned[] artifactStrings = getArtifactString(artifactInfo, true, 1);
                        textName.setText(artifactStrings[0]);
                        textPower.setText(artifactStrings[1]);

                        if (artifactInfo.effect != ArtifactEffect.NO_EFFECT) {
                            equipmentEffectsList.add(artifactInfo.effect);
                        }
                        if (artifactInfo.effectSpecial != ArtifactEffect.NO_EFFECT) {
                            equipmentEffectsList.add(artifactInfo.effectSpecial);
                        }

                        equipmentEntryView.setOnClickListener(v -> DialogUtils.showArtifactDialog(getFragmentManager(), artifactInfo));
                    }

                    equipmentContainer.addView(equipmentEntryView);
                }

                if (equipmentEffectsList.size() == 0) {
                    equipmentEffects.setVisibility(View.GONE);
                } else {
                    final Map<ArtifactEffect, Integer> effects = ObjectUtils.getItemsCountList(
                            equipmentEffectsList,
                            (lhs, rhs) -> lhs.getEffectName().compareTo(rhs.getEffectName()));
                    final SpannableStringBuilder effectsStringBuilder = new SpannableStringBuilder();
                    boolean first = true;
                    for (final Map.Entry<ArtifactEffect, Integer> entry : effects.entrySet()) {
                        if (first) {
                            first = false;
                        } else {
                            effectsStringBuilder.append("\n");
                        }
                        final ArtifactEffect effect = entry.getKey();
                        effectsStringBuilder.append(UiUtils.getInfoItem(effect.getEffectName(), effect.getDescription()));
                        if (entry.getValue() != 1) {
                            effectsStringBuilder.append(getString(R.string.common_item_count, entry.getValue()));
                        }
                    }
                    equipmentEffects.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            DialogUtils.showMessageDialog(getFragmentManager(),
                                    getString(R.string.game_bag_effects_title), effectsStringBuilder);
                        }
                    });
                    equipmentEffects.setVisibility(View.VISIBLE);
                }

                bagCaption.setText(getString(R.string.game_title_bag,
                        response.account.hero.basicInfo.bagItemsCount,
                        response.account.hero.basicInfo.bagCapacity));

                bagContainer.removeAllViews();

                if (response.account.isOwnInfo) {
                    final View dropView = layoutInflater.inflate(R.layout.item_bag_drop, bagContainer, false);
                    final RequestActionView dropActionView = dropView.findViewById(R.id.bag_drop);
                    if (response.account.hero.basicInfo.bagItemsCount > 0) {
                        final GameInfoResponse gameInfoResponse = response;
                        new InfoPrerequisiteRequest(client, manager, new Runnable() {
                            @Override
                            public void run() {
                                Integer energy = gameInfoResponse.account.hero.energy;
                                if (energy != null && energy >
                                        PreferencesManager.getAbilityCost(Action.DROP_ITEM)) {
                                    dropActionView.setEnabled(true);
                                    dropActionView.setActionClickListener(() -> {
                                        if (PreferencesManager.isConfirmationBagDropEnabled()) {
                                            DialogUtils.showConfirmationDialog(
                                                    getChildFragmentManager(),
                                                    getString(R.string.game_bag_drop_item),
                                                    getString(R.string.game_bag_drop_item_confirmation),
                                                    new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            dropItem(dropActionView);
                                                        }
                                                    });
                                        } else {
                                            dropItem(dropActionView);
                                        }
                                    });
                                } else{
                                    dropActionView.setEnabled(false);
                                }
                            }
                        }, new PrerequisiteRequest.ErrorCallback<InfoResponse>() {
                            @Override
                            public void processError(InfoResponse response) {
                                dropActionView.setErrorText(response.errorMessage);
                            }
                        }, EquipmentFragment.this).execute();
                    } else {
                        dropActionView.setEnabled(false);
                    }
                    bagContainer.addView(dropView);
                }

                final Map<ArtifactInfo, Integer> bagItemsList = ObjectUtils.getItemsCountList(
                        response.account.hero.bag.values(),
                        (lhs, rhs) -> {
                            if (lhs.name.equals(rhs.name)) {
                                if (lhs.powerPhysical == rhs.powerPhysical) {
                                    if (lhs.powerMagical == rhs.powerMagical) {
                                        if (lhs.type == ArtifactType.JUNK) {
                                            return rhs.type == ArtifactType.JUNK ? 0 : 1;
                                        } else {
                                            return rhs.type == ArtifactType.JUNK ? -1 : 0;
                                        }
                                    } else {
                                        return lhs.powerMagical - rhs.powerMagical;
                                    }
                                } else {
                                    return lhs.powerPhysical - rhs.powerPhysical;
                                }
                            } else {
                                return lhs.name.compareTo(rhs.name);
                            }
                        });
                for (final Map.Entry<ArtifactInfo, Integer> bagEntry : bagItemsList.entrySet()) {
                    final View bagEntryView = layoutInflater.inflate(R.layout.item_bag, bagContainer, false);
                    final ArtifactInfo artifactInfo = bagEntry.getKey();

                    ((TextView) bagEntryView.findViewById(R.id.bag_item_name)).setText(getArtifactString(artifactInfo, false, bagEntry.getValue())[0]);
                    bagEntryView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            DialogUtils.showArtifactDialog(getFragmentManager(), artifactInfo);
                        }
                    });

                    bagContainer.addView(bagEntryView);
                }

                setMode(DataViewMode.DATA);
            }

            @Override
            public void processError(GameInfoResponse response) {
                setError(response.errorMessage);
            }
        }, this);

        final int watchingAccountId = PreferencesManager.getWatchingAccountId();
        if (watchingAccountId == 0) {
            new GameInfoRequest(client, manager, true).execute(callback, true);
        } else {
            new GameInfoRequest(client, manager, true).execute(watchingAccountId, callback, true);
        }
    }

    private Spanned[] getArtifactString(final ArtifactInfo artifactInfo, final boolean isEquipped, final int count) {
        final String countString = count == 1 ? "" : getString(R.string.common_item_count, count);

        final Spannable name = new SpannableString(artifactInfo.name);
        name.setSpan(new ForegroundColorSpan(getResources().getColor(artifactInfo.rarity.getColorResId())),
                0, name.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        if (artifactInfo.type == ArtifactType.JUNK) {
            return new Spanned[]{(Spanned) TextUtils.concat(name, countString)};
        } else {
            final Spannable powerPhysical = new SpannableString(String.valueOf(artifactInfo.powerPhysical));
            powerPhysical.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.artifact_power_physical)),
                    0, powerPhysical.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

            final Spannable powerMagical = new SpannableString(String.valueOf(artifactInfo.powerMagical));
            powerMagical.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.artifact_power_magical)),
                    0, powerMagical.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

            if (artifactInfo.rarity.isExceptional()) {
                name.setSpan(new StyleSpan(Typeface.BOLD), 0, name.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                powerPhysical.setSpan(new StyleSpan(Typeface.BOLD), 0, powerPhysical.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                powerMagical.setSpan(new StyleSpan(Typeface.BOLD), 0, powerMagical.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }

            if (isEquipped) {
                return new Spanned[]{name, (Spanned) TextUtils.concat(powerPhysical, " ", powerMagical)};
            } else {
                return new Spanned[]{(Spanned) TextUtils.concat(name, " ", powerPhysical, " ", powerMagical, countString)};
            }
        }

    }

    private void dropItem(final RequestActionView dropActionView) {
        dropActionView.setMode(RequestActionView.Mode.LOADING);
        new AbilityUseRequest(client, manager, Action.DROP_ITEM).execute(0, RequestUtils.wrapCallback(new ApiResponseCallback<CommonResponse>() {
            @Override
            public void processResponse(CommonResponse response) {
                refresh(false);
            }

            @Override
            public void processError(CommonResponse response) {
                dropActionView.setErrorText(response.errorMessage);
            }
        }, EquipmentFragment.this));
    }

}
