package com.dleibovych.epictale.fragment.dialog;

import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.dleibovych.epictale.R;
import org.thetale.api.enumerations.ArtifactEffect;
import org.thetale.api.enumerations.ArtifactType;
import com.dleibovych.epictale.api.model.ArtifactInfo;
import com.dleibovych.epictale.util.UiUtils;

/**
 * @author Hamster
 * @since 09.10.2014
 */
public class ArtifactDialog extends BaseDialog {

    private static final String PARAM_ARTIFACT_INFO = "PARAM_ARTIFACT_INFO";

    public static ArtifactDialog newInstance(final ArtifactInfo artifactInfo) {
        final ArtifactDialog dialog = new ArtifactDialog();

        Bundle args = new Bundle();
        args.putParcelable(PARAM_ARTIFACT_INFO, artifactInfo);

        dialog.setArguments(args);
        return dialog;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.dialog_content_artifact, container, false);
        final ViewGroup artifactInfoContainer = (ViewGroup) view.findViewById(R.id.dialog_artifact_content);
        final ArtifactInfo artifactInfo = getArguments().getParcelable(PARAM_ARTIFACT_INFO);

        final Spannable artifactType = new SpannableString(artifactInfo.type == ArtifactType.JUNK ?
                artifactInfo.type.getTypeName() : artifactInfo.rarity.getRarityName());
        artifactType.setSpan(new ForegroundColorSpan(getResources().getColor(artifactInfo.rarity.getColorResId())),
                0, artifactType.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        addLine(inflater, artifactInfoContainer, artifactType);

        if(artifactInfo.type != ArtifactType.JUNK) {
            addLine(inflater, artifactInfoContainer,
                    UiUtils.getInfoItem(getString(R.string.artifact_equipment_type), artifactInfo.type.getTypeName()));
            addLine(inflater, artifactInfoContainer,
                    UiUtils.getInfoItem(getString(R.string.artifact_power_physical), String.valueOf(artifactInfo.powerPhysical)));
            addLine(inflater, artifactInfoContainer,
                    UiUtils.getInfoItem(getString(R.string.artifact_power_magical), String.valueOf(artifactInfo.powerMagical)));
            addLine(inflater, artifactInfoContainer,
                    UiUtils.getInfoItem(getString(R.string.artifact_integrity), getString(R.string.artifact_integrity_value,
                            100.0 * artifactInfo.integrityCurrent / artifactInfo.integrityTotal,
                            artifactInfo.integrityCurrent, artifactInfo.integrityTotal)));
            addLine(inflater, artifactInfoContainer,
                    UiUtils.getInfoItem(getString(R.string.artifact_rating), String.format("%.2f", artifactInfo.rating)));
        }

        boolean showNoEffect = true;
        if(artifactInfo.effect != ArtifactEffect.NO_EFFECT) {
            addLine(inflater, artifactInfoContainer, getEffectString(artifactInfo.effect));
            showNoEffect = false;
        }
        if(artifactInfo.effectSpecial != ArtifactEffect.NO_EFFECT) {
            addLine(inflater, artifactInfoContainer, getEffectString(artifactInfo.effectSpecial));
            showNoEffect = false;
        }
        if(showNoEffect && (artifactInfo.type != ArtifactType.JUNK)) {
            addLine(inflater, artifactInfoContainer, getEffectString(ArtifactEffect.NO_EFFECT));
        }

        return wrapView(inflater, view, artifactInfo.name);
    }

    private void addLine(final LayoutInflater layoutInflater, final ViewGroup container, final CharSequence line) {
        final View lineView = layoutInflater.inflate(R.layout.item_text, container, false);
        ((TextView) lineView.findViewById(R.id.item_text_content)).setText(line);
        container.addView(lineView);
    }

    private Spannable getEffectString(final ArtifactEffect artifactEffect) {
        final Spannable effect = new SpannableString(artifactEffect.getDescription());
        effect.setSpan(new StyleSpan(Typeface.ITALIC), 0, effect.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return effect;
    }

}
