package com.library.common.widget;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.net.http.SslError;
import android.os.Build;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

public class CloudWebView extends WebView {

    public static final String TAG = "CloudWebView";

    public static final int DEFAULT_MINIMUM_TIME_MS = 1500;
    public static final int DEFAULT_MAXIMUM_TIME_MS = 9000;

    private long mMinimumTimeoutTimeMs = DEFAULT_MINIMUM_TIME_MS;
    private long mMaximumTimeoutTimeMs = DEFAULT_MAXIMUM_TIME_MS;
    private long mTimeoutTimeMs = DEFAULT_MAXIMUM_TIME_MS;

    private WebListener mWebListener;
    private CloudWebViewTimeoutRunnable mTimeoutRunnable;

    private int mAnimHeight;
    private boolean mIsHidden;
    private boolean mPaddingEnabled;
    private boolean mStackCountEnabled;
    private OnAnimScrollListener mScrollListener;
    private boolean mTouchEnable = true;

    public CloudWebView(Context context) {
        super(context);
        init();
    }

    public CloudWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CloudWebView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public CloudWebView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        mTimeoutRunnable = new CloudWebViewTimeoutRunnable(this);
        //TODO set allow https
        setUnsafeMode();//set allow mix content
    }

    public CloudWebView enablePadding(boolean enabled) {
        mPaddingEnabled = enabled;
        return this;
    }

    public CloudWebView enableStackCount(boolean enabled) {
        mStackCountEnabled = enabled;
        return this;
    }

    @Override
    public void setWebViewClient(WebViewClient client) {
        super.setWebViewClient(client);
        if (!(client instanceof CloudWebViewClient)) {
            throw new IllegalArgumentException("CloudWebView client should be a instance of CloudWebViewClient.");
        }
    }

    @Override
    public void setWebChromeClient(WebChromeClient client) {
        super.setWebChromeClient(client);
        if (!(client instanceof CloudWebChromeClient)) {
            throw new IllegalArgumentException("CloudWebView client should be a instance of CloudWebChromeClient.");
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    public CloudWebView setJavaScriptEnabled(boolean enabled) {
        getSettings().setJavaScriptEnabled(enabled);
        return this;
    }

    public CloudWebView setDefaultTextEncodingName(String encodingName) {
        getSettings().setDefaultTextEncodingName(encodingName);
        return this;
    }

    public CloudWebView setSupportZoom(boolean support) {
        getSettings().setSupportZoom(support);
        return this;
    }

    private void setTimeoutRunnable(boolean timing) {
        if (timing) {
            mTimeoutRunnable.start();
        } else {
            mTimeoutRunnable.stop();
        }
    }

    @Override
    public void loadUrl(String url) {
        loadUrl(url, false);
    }

    public void loadUrl(String url, boolean timing) {
        super.loadUrl(url);
        setTimeoutRunnable(timing);
    }

    @Override
    public void loadUrl(String url, Map<String, String> additionalHttpHeaders) {
        loadUrl(url, additionalHttpHeaders, false);
    }

    public void loadUrl(String url, Map<String, String> additionalHttpHeaders, boolean timing) {
        super.loadUrl(url, additionalHttpHeaders);
        setTimeoutRunnable(timing);
    }

    public void setCookie(String url, Map<String, String> cookies) {
        if (!TextUtils.isEmpty(url)) {
            CookieManager cookieManager = CookieManager.getInstance();
            cookieManager.setAcceptCookie(true);
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                CookieSyncManager.createInstance(getContext());
                cookieManager.removeAllCookie();
            } else {
                cookieManager.removeAllCookies(null);
            }
            if (cookies != null && cookies.size() > 0) {
                for (Map.Entry<String, String> cookie : cookies.entrySet()) {
                    StringBuilder sbCookie;
                    sbCookie = new StringBuilder();
                    sbCookie.append(cookie.getKey()).append("=").append(cookie.getValue());
                    cookieManager.setCookie(url, sbCookie.toString());
                }
            }
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                CookieSyncManager.getInstance().sync();
            } else {
                cookieManager.flush();
            }
        }
    }

    public void loadUrlWithCookie(String url, Map<String, String> cookies) {
        this.setCookie(url, cookies);
        this.loadUrl(url);
    }

    public void loadUrlWithCookie(String url, Map<String, String> cookies, boolean timing) {
        this.setCookie(url, cookies);
        this.loadUrl(url, timing);
    }

    @Override
    public void postUrl(String url, byte[] postData) {
        postUrl(url, postData, false);
    }

    public void postUrl(String url, byte[] postData, boolean timing) {
        super.postUrl(url, postData);
        setTimeoutRunnable(timing);

    }

    @Override
    public void loadData(String data, String mimeType, String encoding) {
        loadData(data, mimeType, encoding, false);
    }

    public void loadData(String data, String mimeType, String encoding, boolean timing) {
        super.loadData(data, mimeType, encoding);
        setTimeoutRunnable(timing);
    }

    @Override
    public void loadDataWithBaseURL(String baseUrl, String data, String mimeType, String encoding, String failUrl) {
        loadDataWithBaseURL(baseUrl, data, mimeType, encoding, failUrl, false);
    }

    public void loadDataWithBaseURL(String baseUrl, String data, String mimeType, String encoding, String failUrl, boolean timing) {
        super.loadDataWithBaseURL(baseUrl, data, mimeType, encoding, failUrl);
        setTimeoutRunnable(timing);
    }

    public CloudWebView clear() {
        clearHistory();
        clearCache(true);
        loadUrl("javascript:clear();");
        return this;
    }

    @Override
    public void stopLoading() {
        super.stopLoading();
        mTimeoutRunnable.stop();
    }

    public CloudWebView setWebListener(WebListener webListener) {
        mWebListener = webListener;
        return this;
    }

    public CloudWebView setTimeoutTimeMs(long timeoutTimeMs) {
        if (timeoutTimeMs < mMinimumTimeoutTimeMs) {
            timeoutTimeMs = mMinimumTimeoutTimeMs;
        } else if (timeoutTimeMs > mMaximumTimeoutTimeMs) {
            timeoutTimeMs = mMaximumTimeoutTimeMs;
        }
        mTimeoutTimeMs = timeoutTimeMs;
        return this;
    }

    public CloudWebView setMinimumTimeoutTimeMs(long minimumTimeoutTimeMs) {
        mMinimumTimeoutTimeMs = Math.max(minimumTimeoutTimeMs, DEFAULT_MINIMUM_TIME_MS);
        mTimeoutTimeMs = Math.max(mMinimumTimeoutTimeMs, mTimeoutTimeMs);
        return this;
    }

    public CloudWebView setMaximumTimeoutTimeMs(long maximumTimeoutTimeMs) {
        mMaximumTimeoutTimeMs = Math.min(maximumTimeoutTimeMs, DEFAULT_MAXIMUM_TIME_MS);
        mTimeoutTimeMs = Math.min(mMaximumTimeoutTimeMs, mTimeoutTimeMs);
        return this;
    }

    public CloudWebView setAnimScrollListener(OnAnimScrollListener scrollListener, int animHeight) {
        mScrollListener = scrollListener;
        mAnimHeight = animHeight;
        return this;
    }

    public CloudWebView setWebPadding(int left, int top, int right, int bottom) {
        super.setPadding(left, top, right, bottom);
        return this;
    }

    @Override
    protected void onScrollChanged(int l, int scrollY, int oldl, int lastScrollY) {
        super.onScrollChanged(l, scrollY, oldl, lastScrollY);
        if (mAnimHeight == 0 || mScrollListener == null) {
            return;
        }

        if (scrollY >= mAnimHeight) {
            if (!mIsHidden) {
                if (scrollY > lastScrollY) {
                    mScrollListener.onAnimScrollHidden();
                    mIsHidden = true;
                }
            } else {
                if (scrollY < lastScrollY && Math.abs(scrollY - lastScrollY) > dp2px(getContext(), 2)) {
                    mScrollListener.onAnimScrollShown();
                    mIsHidden = false;
                }
            }
        } else {
            if (mIsHidden) {
                if (lastScrollY > mAnimHeight) {
                    mScrollListener.onAnimScrollShown();
                    mIsHidden = false;
                }
            }
        }

        if (computeVerticalScrollRange() <= getScrollY() + getHeight()) {
            mScrollListener.onScrollToBottom();
            if (mIsHidden) {
                mScrollListener.onAnimScrollShown();
                mIsHidden = false;
            }
        } else if (getScrollY() == 0) {
            mScrollListener.onScrollToTop();
        }
    }

    /**
     * This runnable is used to do web view load timeout settings.
     * Maybe the WebView has been destroyed before the set timeout to be executed.
     */
    public static class CloudWebViewTimeoutRunnable implements Runnable {

        private final WeakReference<CloudWebView> mView;

        public CloudWebViewTimeoutRunnable(CloudWebView view) {
            mView = new WeakReference<CloudWebView>(view);
        }

        private boolean mTimeout = false;

        public void start() {
            final CloudWebView webView = mView.get();
            webView.removeCallbacks(this);
            webView.postDelayed(this, webView.mTimeoutTimeMs);
            mTimeout = false;
        }

        public void stop() {
            final CloudWebView webView = mView.get();
            webView.removeCallbacks(this);
        }

        @Override
        public void run() {
            final CloudWebView webView = mView.get();
            if (webView == null) {
                return;
            }

            if (webView.mWebListener != null) {
                webView.mWebListener.onTimeout(webView);
            }

            mTimeout = true;
        }
    }

    public static class CloudWebChromeClient extends WebChromeClient {

    }

    /**
     * Custom WebView client used for CloudWebView.
     */
    public static class CloudWebViewClient extends WebViewClient {

        private boolean mErrorHappens;

        @Override
        public final boolean shouldOverrideUrlLoading(WebView view, String url) {
            final CloudWebView webView = (CloudWebView) view;
            final int size = webView.mStackCountEnabled ? view.copyBackForwardList().getSize() : -1;
            return shouldOverrideUrlLoading(webView, url, size);
        }

        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            final CloudWebView webView = (CloudWebView) view;
            webView.mTimeoutRunnable.stop();

            if (webView.mWebListener != null) {
                webView.mWebListener.onError(webView);
                mErrorHappens = true;
            }

            super.onReceivedError(view, errorCode, description, failingUrl);
        }

        @Override
        public final void onPageFinished(WebView view, String url) {
            if (mErrorHappens) {
                mErrorHappens = false;
                return;
            }
            final CloudWebView webView = (CloudWebView) view;
            webView.mTimeoutRunnable.stop();

            if (webView.mPaddingEnabled) {
                view.loadUrl("javascript:document.body.style.margin=\""
                        + webView.getPaddingTop() + "px "
                        + webView.getPaddingRight() + "px "
                        + webView.getPaddingBottom() + "px "
                        + webView.getPaddingLeft() + "px\";void 0");
            }
            onPageFinished(webView, url, webView.getProgress(), webView.mTimeoutRunnable.mTimeout);
        }

        /**
         * This method does same work as {@link #onPageFinished(WebView, String)}
         * but has an extra parameter 'timeout', which indicates that timeout event would be triggered
         * if the web loading time beyond the limitation.
         */
        public void onPageFinished(CloudWebView view, String url, int progress, boolean timeout) {
            super.onPageFinished(view, url);
        }

        /**
         * This method does same work as {@link #shouldOverrideUrlLoading(WebView, String)}
         * but has an extra parameter 'stackSize", which indicates the history stack size.
         * Because it's time consuming task to copy data, so stackSize only worked if you specified
         * default stackSize is -1.
         */
        public boolean shouldOverrideUrlLoading(CloudWebView view, String url, int stackSize) {
            return super.shouldOverrideUrlLoading(view, url);
        }

        @Override
        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
            handler.proceed();
        }

    }

    /**
     * Callback to be invoked when scroll web view.
     */
    public interface OnAnimScrollListener {

        /**
         * With this callback, hide some views.
         */
        public void onAnimScrollHidden();

        /**
         * With this callback, show hidden views.
         */
        public void onAnimScrollShown();

        /**
         * With this callback, something can be done when scroll to top.
         */
        public void onScrollToTop();

        /**
         * With this callback, something can be done when scroll to bottom.
         */
        public void onScrollToBottom();
    }

    public interface WebListener {

        public void onTimeout(CloudWebView webView);

        public void onError(CloudWebView webView);
    }


    public void setUnsafeMode() {
        try {
            WebSettings settings = getSettings();
            Class classSetting = settings.getClass();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                settings.setAllowUniversalAccessFromFileURLs(true);
            } else {
                Method method = classSetting.getMethod("setAllowUniversalAccessFromFileURLs", new Class[]{Boolean.TYPE});
                if (method != null) {
                    method.invoke(settings, new Object[]{Boolean.valueOf(true)});
                }
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
            } else {
                Method method = classSetting.getMethod("setMixedContentMode", new Class[]{Integer.TYPE});
                if (method != null) {
                    method.invoke(settings, new Object[]{Integer.valueOf(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW)});
                }
            }
        } catch (IllegalArgumentException iare) {
            iare.printStackTrace();
        } catch (NoSuchMethodException nsme) {
            nsme.printStackTrace();
        } catch (IllegalAccessException iae) {
            iae.printStackTrace();
        } catch (InvocationTargetException ite) {
            ite.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setOnTouchEnable(boolean enable) {
        this.mTouchEnable = enable;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return mTouchEnable && super.onTouchEvent(event);
    }

    private int dp2px(Context context, float value) {
        if (context != null && value > 0) {
            return (int) (context.getResources().getDisplayMetrics().density * value + 0.5);
        }
        return 0;
    }

}
