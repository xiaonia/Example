package com.library.common.base;

import com.library.common.utils.ResourcesUtils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.LoaderManager;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import static android.content.Context.INPUT_METHOD_SERVICE;
import static android.view.inputmethod.InputMethodManager.HIDE_NOT_ALWAYS;

public class BaseFragment extends Fragment implements ActivityContext, ApplicationContext, UiHandler.UiCallback {

    private static final String STATE_EXTRA_ARGUMENTS = "state_extra_arguments";

    protected Handler mHandler;
    protected UiHandler mUiHandler;
    /**
     *  extra arguments for the fragment
     */
    private Bundle extraArguments;

    public BaseFragment() {
        // Default Constructor is necessary!
    }

    @Override
    public Context getApplicationContext() {
        return BaseApplication.getInstance().getApplicationContext();
    }

    @Override
    public Context getActivityContext() {
        return getActivity();
    }

    public BaseActivity getBaseActivity() {
        return getActivity() != null ? (BaseActivity) getActivity() : null;
    }

    @Override
    public SharedPreferences getPreferences() {
        return BaseApplication.getInstance().getPreferences();
    }

    /**
     * Return the LoaderManager for this fragment, creating it if needed.
     */
    public LoaderManager getSupportLoaderManager() {
        return getActivity() != null ? getActivity().getSupportLoaderManager() : null;
    }

    /**
     * Return the FragmentManager for this fragment, creating if if needed.
     */
    public FragmentManager getSupportFragmentManager() {
        return getActivity() != null ? getActivity().getSupportFragmentManager() : null;
    }

    @Override
    public boolean handleUiMessage(Message msg, int what, boolean enabled) {
        return false;
    }

    /**
     * If this fragment does not have retain state, and have already set a view with
     * {@link #onCreateView(android.view.LayoutInflater, android.view.ViewGroup, Bundle)}
     * this method is used to retrieve a specified child view.
     *
     * @return The view if found or null otherwise.
     * @see #onCreateView(android.view.LayoutInflater, android.view.ViewGroup, Bundle)
     * @see #onViewCreated(View, Bundle)
     */
    @SuppressWarnings("unchecked")
    protected <T extends View> T findView(@IdRes int id) {
        if (getView() != null) {
            return (T) getView().findViewById(id);
        }
        return null;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mUiHandler = new UiHandler(this);
        mHandler = mUiHandler.getHandler();
    }

    /**
     * Set whether the activity do response to Back Button Pressed Event.
     * Default is true.
     */
    protected boolean enableBackPressed() {
        return true;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Try to add Back Button Event listener.
        if (view != null && enableBackPressed()) {
            view.setFocusableInTouchMode(true);
            view.requestFocus();
            view.setOnKeyListener(new View.OnKeyListener() {
                @Override
                public boolean onKey(View v, int keyCode, KeyEvent event) {
                    if (event.getAction() == KeyEvent.ACTION_UP) {
                        if (keyCode == KeyEvent.KEYCODE_BACK) {
                            return onFragmentBackPressed() || onActivityBackPressed();
                        }
                    }
                    return false;
                }
            });
        }
    }

    /**
     *  restore state
     */
    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState != null) {
            extraArguments = savedInstanceState.getBundle(STATE_EXTRA_ARGUMENTS);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        onFragmentResume();
    }

    /**
     * Callback to be invoked when the fragment needs to be resume without depends on activity's lifecycle.
     * With this callback, UI update can be done.
     */
    public void onFragmentResume() {
        mUiHandler.setEnabled(true);
    }

    @Override
    public void onPause() {
        super.onPause();
        onFragmentPause();
    }

    /**
     * Callback to be invoked when this fragment needs to be paused independent on activity's lifecycle.
     */
    public void onFragmentPause() {

    }

    /**
     *  save state
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBundle(STATE_EXTRA_ARGUMENTS, extraArguments);
    }

    /**
     *  fragment shown or hided
     */
    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            onResume();
        } else {
            onPause();
        }
    }

    /**
     * Callback to be invoked when the fragment monitored the System back button pressed.
     * With this callback, exits prompt dialog can be shown.
     *
     * @return true if this fragment consumed this action, otherwise false.
     */
    public boolean onFragmentBackPressed() {
        return false;
    }

    /**
     * Callback to be invoked when the activity monitored the system back button pressed.
     * This is an equivalence of {@link Activity#onBackPressed()}, but this will work with fragment.
     */
    public boolean onActivityBackPressed() {
        return getBaseActivity().onActivityBackPressed();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mUiHandler != null) {
            mUiHandler.setEnabled(false);
        }
        mUiHandler = null;
        mHandler = null;
    }

    /**
     * To hide keypad
     */
    protected void hideKeypad() {
        final View v = getActivity().getCurrentFocus();
        if (v != null) {
            final InputMethodManager imm = (InputMethodManager) getActivityContext().getSystemService(INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(v.getWindowToken(), HIDE_NOT_ALWAYS);
        }
    }

    /**
     * To hide keypad
     */
    protected void hideKeyBoard() {
        InputMethodManager imm = (InputMethodManager) getActivityContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {

            imm.hideSoftInputFromWindow(getActivity().getWindow().getDecorView().getWindowToken(),
                    0);
        }
    }

    /**
     * show keyboard
     */
    protected void showKeyBoard(View view) {
        InputMethodManager imm = (InputMethodManager) getActivityContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.showSoftInput(view, InputMethodManager.SHOW_FORCED);
        }
    }

    /**
     * check if key board shown?
     */
    protected boolean isKeyBoardShown() {
        InputMethodManager imm = (InputMethodManager) getActivityContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        return imm != null && imm.isActive();
    }

    /**
     *  show status bar holder
     */
    protected void showStatusBarHolder() {
        //show status bar place holder view
        int statusBarHeight = getStatusBarHeight();
        if (statusBarHeight > 0 && getActivityContext() != null && getView() != null) {
            View statusBarHolder = getView().findViewById(
                    ResourcesUtils.getId(getActivityContext(), "status_bar_holder"));

            if (statusBarHolder != null && statusBarHolder.getLayoutParams() != null) {
                statusBarHolder.getLayoutParams().height = statusBarHeight;
            }
        }
    }

    /**
     *  get system bar height
     */
    protected int getStatusBarHeight() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && getActivityContext() != null) {
            Rect rect = new Rect();
            ((Activity) getActivityContext()).getWindow().getDecorView().getWindowVisibleDisplayFrame(rect);
            if (rect.top > 0) {
                return rect.top;
            }

            int resourceId = getActivityContext().getResources()
                    .getIdentifier("status_bar_height", "dimen", "android");
            if (resourceId > 0) {
                return getActivityContext().getResources()
                        .getDimensionPixelSize(resourceId);
            }
        }

        return 0;
    }

    public Bundle getExtraArguments() {
        return extraArguments;
    }

    public void setExtraArguments(Bundle arguments) {
        this.extraArguments = arguments;
    }

    /**
     *  go to another fragment
     */
    public boolean switchFragment(String targetTag) {
        BaseActivity activity = getBaseActivity();
        if (activity != null
                && activity.switchFragment(this, targetTag) != null) {

            return true;
        }
        return false;
    }

    /**
     *  go to another fragment
     */
    public boolean switchFragment(String targetTag, Bundle bundle) {
        BaseActivity activity = getBaseActivity();
        if (activity != null
                && activity.switchFragment(this, targetTag, bundle, true) != null) {

            return true;
        }
        return false;
    }

    /**
     *  back to another fragment
     */
    public boolean backFragment(String targetTag) {
        BaseActivity activity = getBaseActivity();
        if (activity != null
                && activity.backFragment(targetTag) != null) {

            return true;
        }
        return false;
    }

    /**
     *  back to another fragment
     */
    public boolean backFragment(String targetTag, Bundle bundle) {
        BaseActivity activity = getBaseActivity();
        if (activity != null
                && activity.backFragment(targetTag, bundle) != null) {

            return true;
        }
        return false;
    }

}
