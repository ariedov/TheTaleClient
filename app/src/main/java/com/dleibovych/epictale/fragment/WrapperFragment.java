package com.dleibovych.epictale.fragment;

import android.app.Activity;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import com.dleibovych.epictale.DataViewMode;
import com.dleibovych.epictale.R;
import com.dleibovych.epictale.game.MainActivity;
import com.dleibovych.epictale.util.UiUtils;
import com.dleibovych.epictale.util.onscreen.OnscreenStateListener;

/**
 * @author Hamster
 * @since 07.10.2014
 */
public class WrapperFragment extends Fragment implements Refreshable, OnscreenStateListener {

    private View dataView;
    private View loadingView;
    private View errorView;

    @Override
    public void onResume() {
        super.onResume();
        if(isAdded()) {
            refresh(true);
        }
    }

    protected View wrapView(final LayoutInflater layoutInflater, final View view) {
        final View wrapped = layoutInflater.inflate(R.layout.fragment_wrapper, null);

        dataView = wrapped.findViewById(R.id.fragment_part_data);
        loadingView = wrapped.findViewById(R.id.fragment_part_loading);
        errorView = wrapped.findViewById(R.id.fragment_part_error);

        ((FrameLayout) dataView).addView(view);

        return wrapped;
    }

    public void setMode(final DataViewMode mode) {
        if(!isAdded()) {
            return;
        }

        final Activity activity = getActivity();
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                dataView.setVisibility(mode == DataViewMode.DATA ? View.VISIBLE : View.GONE);
                loadingView.setVisibility(mode == DataViewMode.LOADING ? View.VISIBLE : View.GONE);
                errorView.setVisibility(mode == DataViewMode.ERROR ? View.VISIBLE : View.GONE);

                if(activity instanceof MainActivity) {
                    switch(mode) {
                        case DATA:
                        case ERROR:
                            break;

                        case LOADING:
                            break;
                    }
                }
            }
        });
    }

    public void setError(final String error) {
        if(!isAdded()) {
            return;
        }

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                setMode(DataViewMode.ERROR);
                UiUtils.setText(errorView.findViewById(R.id.fragment_part_error_text), error);
            }
        });
    }

    @Override
    public void refresh(final boolean isGlobal) {
//        if(!isAdded()) {
//            return;
//        }

        if(isGlobal) {
            setMode(DataViewMode.LOADING);
        }
    }

    @Override
    public void onOffscreen() {
    }

    @Override
    public void onOnscreen() {
    }

}
