package com.dleibovych.epictale.fragment.dialog;

import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.dleibovych.epictale.R;
import com.dleibovych.epictale.api.dictionary.CardRarity;
import com.dleibovych.epictale.api.model.CardInfo;

/**
 * @author Hamster
 * @since 21.03.2015
 */
public class CardInfoDialog extends BaseDialog {

    private static final String PARAM_TITLE = "PARAM_TITLE";
    private static final String PARAM_CARD = "PARAM_CARD";

    private Runnable onDismissListener = null;

    public static CardInfoDialog newInstance(final String title, final CardInfo card) {
        final CardInfoDialog dialog = new CardInfoDialog();

        final Bundle args = new Bundle();
        args.putString(PARAM_TITLE, title);
        args.putParcelable(PARAM_CARD, card);

        dialog.setArguments(args);
        return dialog;
    }

    public void setOnDismissListener(final Runnable listener) {
        this.onDismissListener = listener;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.dialog_content_card_info, container, false);

        final CardInfo card = getArguments().getParcelable(PARAM_CARD);
        final CardRarity rarity = card.type == null ? card.rarity : card.type.getRarity();
        final Spannable cardName = new SpannableString(card.name);
        cardName.setSpan(new ForegroundColorSpan(getResources().getColor(rarity.getColorResId())),
                0, cardName.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        ((TextView) view.findViewById(R.id.dialog_card_info_name)).setText(cardName);
        final TextView cardDescription = (TextView) view.findViewById(R.id.dialog_card_info_description);
        if(card.type == null) {
            cardDescription.setVisibility(View.GONE);
        } else {
            cardDescription.setVisibility(View.VISIBLE);
            cardDescription.setText(card.type.getDescription());
        }
        view.findViewById(R.id.dialog_card_info_tradable).setVisibility(card.isTradable ? View.VISIBLE : View.GONE);

        return wrapView(inflater, view, getArguments().getString(PARAM_TITLE));
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        if(onDismissListener != null) {
            onDismissListener.run();
        }
    }

}
