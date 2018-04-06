package com.dleibovych.epictale.fragment.dialog;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.dleibovych.epictale.DataViewMode;
import com.dleibovych.epictale.R;

/**
 * @author Hamster
 * @since 09.10.2014
 */
public abstract class BaseDialog extends DialogFragment {

    private TextView caption;
    private ViewGroup content;
    private View contentProgress;

    protected View wrapView(final LayoutInflater layoutInflater, final View view, final String caption) {
        final View wrapped = layoutInflater.inflate(R.layout.dialog_base, null);

        this.caption = (TextView) wrapped.findViewById(R.id.dialog_caption);
        setCaption(caption);

        wrapped.findViewById(R.id.dialog_close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        contentProgress = wrapped.findViewById(R.id.dialog_content_progress);
        content = (ViewGroup) wrapped.findViewById(R.id.dialog_content);
        content.addView(view);

        return wrapped;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        setCancelable(true);
        setStyle(DialogFragment.STYLE_NO_TITLE, R.style.Dialog);

        final Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.setCanceledOnTouchOutside(true);
        return dialog;
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        dismiss();
        super.onCancel(dialog);
    }

    @Override
    public void onPause() {
        dismiss();
        super.onPause();
    }

    public void setMode(final DataViewMode mode) {
        switch(mode) {
            case DATA:
                contentProgress.setVisibility(View.GONE);
                content.setVisibility(View.VISIBLE);
                break;

            case LOADING:
                contentProgress.setVisibility(View.VISIBLE);
                content.setVisibility(View.GONE);
                break;

            case ERROR:
                throw new IllegalArgumentException("Error data viewStates mode is not supported");
        }
    }

    public void setCaption(final CharSequence caption) {
        this.caption.setText(caption);
    }

}
