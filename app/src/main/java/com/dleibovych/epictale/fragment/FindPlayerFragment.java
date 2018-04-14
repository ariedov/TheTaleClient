package com.dleibovych.epictale.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Pair;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ListView;
import android.widget.TextView;

import com.dleibovych.epictale.DataViewMode;
import com.dleibovych.epictale.DrawerItem;
import com.dleibovych.epictale.R;
import com.dleibovych.epictale.game.MainActivity;
import com.dleibovych.epictale.fragment.dialog.ChoiceDialog;
import com.dleibovych.epictale.util.DialogUtils;
import com.dleibovych.epictale.util.PreferencesManager;
import com.dleibovych.epictale.util.UiUtils;
import com.dleibovych.epictale.util.WebsiteUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * @author Hamster
 * @since 04.02.2015
 */
public class FindPlayerFragment extends WrapperFragment {

    private static final int ACCOUNTS_PER_PAGE = 25;
    private static final int ACCOUNTS_COUNT_THRESHOLD = 500;
    private static final int ACCOUNTS_COUNT_GRANULARITY = 100;

    private static final Handler handler = new Handler(Looper.getMainLooper());

    private LayoutInflater layoutInflater;
    private View rootView;

    private TextView textQuery;
    private View actionSearch;
    private ListView accountsList;
    private TextView textError;
    private TextView textDescription;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        layoutInflater = inflater;
        rootView = inflater.inflate(R.layout.fragment_find_player, container, false);

        textQuery = (TextView) rootView.findViewById(R.id.find_player_prefix);
        actionSearch = rootView.findViewById(R.id.find_player_search);
        accountsList = (ListView) rootView.findViewById(R.id.find_player_choices);
        textError = (TextView) rootView.findViewById(R.id.find_player_error);
        textDescription = (TextView) rootView.findViewById(R.id.find_player_description);

        textQuery.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(actionId == EditorInfo.IME_ACTION_SEARCH) {
                    actionSearch.performClick();
                    return true;
                } else {
                    return false;
                }
            }
        });

        actionSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UiUtils.hideKeyboard(getActivity());

                setMode(DataViewMode.LOADING);
                accountsList.setVisibility(View.VISIBLE);
                textError.setVisibility(View.GONE);
                textDescription.setVisibility(View.GONE);

                final String query = textQuery.getText().toString();
                final List<Pair<Integer, String>> choices = Collections.synchronizedList(new ArrayList<Pair<Integer, String>>());

                final WebsiteUtils.AccountPageCallback callback = new WebsiteUtils.AccountPageCallback() {
                    @Override
                    public boolean processPagesCount(int count) {
                        return true;
                    }

                    @Override
                    public void processAccounts(Map<Integer, String> accounts) {
                        final List<Pair<Integer, String>> currentChoices = new ArrayList<>(accounts.size());
                        for(final Map.Entry<Integer, String> entry : accounts.entrySet()) {
                            currentChoices.add(Pair.create(entry.getKey(), entry.getValue()));
                        }
                        choices.addAll(currentChoices);
                    }

                    @Override
                    public void onError(final WebsiteUtils.ErrorType errorType, int id, final String message) {
                        showError(errorType, id, message);
                    }

                    @Override
                    public void onFinish() {
                        Collections.sort(choices, new Comparator<Pair<Integer, String>>() {
                            @Override
                            public int compare(Pair<Integer, String> lhs, Pair<Integer, String> rhs) {
                                return lhs.second.compareToIgnoreCase(rhs.second);
                            }
                        });

                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                setItems(choices);
                                setMode(DataViewMode.DATA);
                            }
                        });
                    }
                };

                WebsiteUtils.enumAccountPages(query, new WebsiteUtils.AccountPageCallback() {
                    @Override
                    public boolean processPagesCount(int count) {
                        final Runnable cancelRunnable = new Runnable() {
                            @Override
                            public void run() {
                                setMode(DataViewMode.DATA);
                            }
                        };
                        final int approximate = count * ACCOUNTS_PER_PAGE;
                        if(approximate > ACCOUNTS_COUNT_THRESHOLD) {
                            if(!isAdded() || UiUtils.getMainActivity(FindPlayerFragment.this).isPaused()) {
                                cancelRunnable.run();
                            } else {
                                DialogUtils.showConfirmationDialog(
                                        getChildFragmentManager(),
                                        getString(R.string.common_dialog_attention_title),
                                        getString(R.string.find_player_too_many,
                                                (approximate / ACCOUNTS_COUNT_GRANULARITY) * ACCOUNTS_COUNT_GRANULARITY),
                                        null, new Runnable() {
                                            @Override
                                            public void run() {
                                                WebsiteUtils.enumAccountPages(query, callback);
                                            }
                                        },
                                        null, cancelRunnable, cancelRunnable);
                            }
                        } else {
                            WebsiteUtils.enumAccountPages(query, callback);
                        }
                        return false;
                    }

                    @Override
                    public void processAccounts(Map<Integer, String> accounts) {
                    }

                    @Override
                    public void onError(final WebsiteUtils.ErrorType errorType, int id, final String message) {
                        showError(errorType, id, message);
                    }

                    @Override
                    public void onFinish() {
                    }
                });
            }
        });

        rootView.findViewById(R.id.find_player_history).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UiUtils.hideKeyboard(getActivity());

                final List<Pair<Integer, String>> choices = PreferencesManager.getFindPlayerHistory();
                if(choices.size() == 0) {
                    setErrorText(getString(R.string.find_player_history_empty));
                } else {
                    Collections.reverse(choices);
                    setItems(choices);
                }
            }
        });

        rootView.findViewById(R.id.find_player_reset).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setAccount(0, null);
            }
        });

        return wrapView(layoutInflater, rootView);
    }

    private void showError(final WebsiteUtils.ErrorType errorType, int id, final String message) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                switch(errorType) {
                    case GLOBAL:
                        setError(message);
                        break;

                    case ITEMS_LIST:
                        setErrorText(message);
                        setMode(DataViewMode.DATA);
                        break;

                    case ITEM:
                        break;

                    default:
                        setMode(DataViewMode.DATA);
                }
            }
        });
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        textQuery.setText(PreferencesManager.getFindPlayerLastQuery());
    }

    @Override
    public void refresh(final boolean isGlobal) {
        super.refresh(isGlobal);

        textQuery.requestFocus();
        UiUtils.showKeyboard(getActivity());

        setMode(DataViewMode.DATA);
    }

    @Override
    public void onOffscreen() {
        super.onOffscreen();

        PreferencesManager.setFindPlayerLastQuery(textQuery.getText().toString());
        UiUtils.hideKeyboard(getActivity());
    }

    private void setItems(final List<Pair<Integer, String>> items) {
        final List<String> itemValues = new ArrayList<>(items.size());
        for(final Pair<Integer, String> item : items) {
            itemValues.add(item.second);
        }

        accountsList.setAdapter(new ChoiceDialog.ChoiceAdapter(
                layoutInflater,
                itemValues,
                new ChoiceDialog.ItemChooseListener() {
                    @Override
                    public void onItemSelected(int position) {
                        final Pair<Integer, String> item = items.get(position);
                        setAccount(item.first, item.second);
                    }
                }));

        textError.setVisibility(View.GONE);
        textDescription.setVisibility(View.GONE);
        accountsList.setVisibility(View.VISIBLE);
    }

    private void setErrorText(final String error) {
        textError.setText(error);

        textDescription.setVisibility(View.GONE);
        accountsList.setVisibility(View.GONE);
        textError.setVisibility(View.VISIBLE);
    }

    private void setDescriptionText(final String description) {
        textDescription.setText(description);

        accountsList.setVisibility(View.GONE);
        textError.setVisibility(View.GONE);
        textDescription.setVisibility(View.VISIBLE);
    }

    private void setAccount(final int id, final String name) {
        PreferencesManager.setWatchingAccount(id, name);
        if((id != 0) && !TextUtils.isEmpty(name)) {
            PreferencesManager.addFindPlayerHistory(id, name);
        }

        final Activity activity = getActivity();
        if(activity instanceof MainActivity) {
//            ((MainActivity) activity).onNavigationDrawerItemSelected(DrawerItem.GAME);
        }
    }

}
