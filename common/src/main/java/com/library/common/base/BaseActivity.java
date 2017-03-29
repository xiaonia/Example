package com.library.common.base;

import com.aibao.evaluation.common.R;
import com.library.common.utils.ResourcesUtils;
import com.library.common.utils.UiUtils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import static android.view.inputmethod.InputMethodManager.HIDE_NOT_ALWAYS;

public abstract class BaseActivity extends AppCompatActivity implements ApplicationContext, ActivityContext, UiHandler.UiCallback {
    /**
     * Do not enable the strict mode for production.
     */
    private static final boolean STRICT_MODE = false;

    protected Handler mHandler;
    protected UiHandler mUiHandler;

    @Override
    public Context getActivityContext() {
        return this;
    }

    @Override
    public SharedPreferences getPreferences() {
        return BaseApplication.getInstance().getPreferences();
    }

    public FragmentTransaction getSupportFragmentTransaction() {
        return getSupportFragmentManager().beginTransaction();
    }

    /**
     * Finds a view that was identified by the id attribute from the XML that
     * was processed in {@link #onCreate}.
     *
     * @return The view if found or null otherwise.
     * @see #findViewById(int)
     */
    protected <T extends View> T findView(int id) {
        return (T) findViewById(id);
    }

    @Nullable
    @SuppressWarnings("unchecked")
    protected <T extends BaseFragment> T findFragment(int id) {
        return (T) getSupportFragmentManager().findFragmentById(id);
    }

    @Nullable
    @SuppressWarnings("unchecked")
    protected <T extends BaseFragment> T findFragment(String tag) {
        return (T) getSupportFragmentManager().findFragmentByTag(tag);
    }

    @Override
    public boolean handleUiMessage(Message msg, int what, boolean enabled) {
        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (STRICT_MODE) {
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                    .detectDiskReads().detectDiskWrites().detectNetwork()
                    .detectCustomSlowCalls().build());

            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                    .detectActivityLeaks().detectLeakedClosableObjects()
                    .detectLeakedSqlLiteObjects()
                    .build());
        }

        mUiHandler = new UiHandler(this);
        mHandler = mUiHandler.getHandler();
    }


    @Override
    protected void onResume() {
        super.onResume();
        mUiHandler.setEnabled(true);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mUiHandler.setEnabled(false);
        mUiHandler = null;
        mHandler = null;
    }

    public boolean onActivityBackPressed() {
        super.onBackPressed();
        return true;
    }

    /**
     * {@link #backFragment(String, Bundle, boolean, boolean)}
     */
    public <T extends BaseFragment> T backFragment (String toFragmentTag) {
        return backFragment(toFragmentTag, null);
    }

    /**
     * {@link #backFragment(String, Bundle, boolean, boolean)}
     */
    public <T extends BaseFragment> T backFragment (String toFragmentTag, Bundle bundle) {
        return backFragment(toFragmentTag, bundle, false, true);
    }

    /**
     *  go back to certain fragment
     * @param toFragmentTag tag of target fragment
     * @param bundle extra arguments
     * @param include true if you want to pop target fragment included
     * @param immediate true if you want to pop it immediately
     * @return return target fragment or null if not found
     */
    @SuppressWarnings("unchecked")
    public <T extends BaseFragment> T  backFragment(String toFragmentTag, Bundle bundle, boolean include, boolean immediate) {
        BaseFragment toFragment = findFragment(toFragmentTag);
        if (!include && toFragment != null && bundle != null) {
            toFragment.setExtraArguments(bundle);
        }

        if (immediate) {
            getSupportFragmentManager().popBackStackImmediate(toFragmentTag, include? FragmentManager.POP_BACK_STACK_INCLUSIVE : 0);
        } else {
            getSupportFragmentManager().popBackStack(toFragmentTag, include? FragmentManager.POP_BACK_STACK_INCLUSIVE : 0);
        }

        if (toFragment == null) {
            return null;
        }
        return (T) toFragment;
    }

    /**
     * {@link #switchFragment(BaseFragment, String, Bundle, boolean, boolean)}
     */
    public <T extends BaseFragment> T switchFragment(BaseFragment fromFragment, String toFragmentTag) {
        return switchFragment(fromFragment, toFragmentTag, null);
    }

    /**
     * {@link #switchFragment(BaseFragment, String, Bundle, boolean, boolean)}
     */
    public <T extends BaseFragment> T switchFragment(BaseFragment fromFragment, String toFragmentTag, Bundle bundle) {
        return switchFragment(fromFragment, toFragmentTag, bundle, true);
    }

    /**
     * {@link #switchFragment(BaseFragment, String, Bundle, boolean, boolean)}
     */
    public <T extends BaseFragment> T switchFragment(BaseFragment fromFragment, String toFragmentTag, Bundle bundle, boolean addStack) {
        return switchFragment(fromFragment, toFragmentTag, bundle, addStack, false);
    }

    /**
     *  switch to certain fragment and hide current fragment
     * @param fromFragment current fragment
     * @param toFragmentTag target fragment
     * @param bundle extra arguments
     * @param addStack true if you want to add to back stack
     * @param popStack true if you want to pop current state from stack
     * @return return target fragment
     */
    @SuppressWarnings("unchecked")
    public <T extends BaseFragment> T switchFragment(BaseFragment fromFragment, String toFragmentTag, Bundle bundle, boolean addStack, boolean popStack) {
        if (popStack) {
            getSupportFragmentManager().popBackStackImmediate();
        }

        final android.support.v4.app.FragmentTransaction ft = getSupportFragmentTransaction();
        if (fromFragment != null) {
            ft.hide(fromFragment);
        }

        BaseFragment toFragment = findFragment(toFragmentTag);
        if (toFragment == null) {
            toFragment = createFragment(toFragmentTag, bundle);
            if (toFragment == null) {
                return null;
            }
            if (bundle != null) {
                toFragment.setExtraArguments(bundle);
            }
            if (addStack) {
                ft.add(R.id.container, toFragment, toFragmentTag).addToBackStack(toFragmentTag);
            } else {
                ft.add(R.id.container, toFragment, toFragmentTag);
            }

        } else {
            if (bundle != null) {
                toFragment.setExtraArguments(bundle);
            }
            if (toFragment.isHidden()) {
                ft.show(toFragment);
            } else {
                toFragment.onResume();
            }
        }

        ft.commit();
        return (T) toFragment;
    }

    /**
     * create new fragment
     * @param fragmentTag tag of target fragment
     * @param bundle extra arguments
     * @return new fragment or null if not match
     */
    protected abstract BaseFragment createFragment (String fragmentTag, Bundle bundle);

    /**
     *  hide system status bar
     */
    protected void hideSystemStatusBar() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            }
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
    }

    /**
     *  show custom status bar holder
     */
    protected void showStatusBarHolder() {
        //show status bar place holder view
        int statusBarHeight = UiUtils.getStatusBarHeight(this);
        if (statusBarHeight > 0 && getActivityContext() != null) {
            View statusBarHolder = findViewById(
                    ResourcesUtils.getId(getActivityContext(), "status_bar_holder"));

            if (statusBarHolder != null && statusBarHolder.getLayoutParams() != null) {
                statusBarHolder.getLayoutParams().height = statusBarHeight;
            }
        }
    }

    /**
     *  check permission in runtime
     */
    public void checkPermissions(String permission, int requestCode, String warning) {
        if (ContextCompat.checkSelfPermission(this, permission)
                != PackageManager.PERMISSION_GRANTED) {

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                //Android6.0以下不支持运行时申请权限
                if (warning != null) {
                    Toast.makeText(getActivityContext(), warning, Toast.LENGTH_SHORT).show();
                }
            } else {
                requestPermissions(new String[]{permission}, requestCode);
            }
        }
    }

    /**
     * To hide keypad
     */
    public void hideKeypad() {
        final View v = getCurrentFocus();
        if (v != null) {
            final InputMethodManager imm = (InputMethodManager) getActivityContext().getSystemService(INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(v.getWindowToken(), HIDE_NOT_ALWAYS);
        }
    }

    /**
     * To hide keypad
     */
    public void hideKeyBoard() {
        InputMethodManager imm = (InputMethodManager) getActivityContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {

            imm.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(), 0);
        }
    }

    /**
     * show keyboard
     */
    public void showKeyBoard(View view) {
        InputMethodManager imm = (InputMethodManager) getActivityContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.showSoftInput(view, InputMethodManager.SHOW_FORCED);
        }
    }

    /**
     * check if key board shown?
     */
    public boolean isKeyBoardShown() {
        InputMethodManager imm = (InputMethodManager) getActivityContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        return imm != null && imm.isActive();
    }

}
