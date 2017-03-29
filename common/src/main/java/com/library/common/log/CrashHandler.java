package com.library.common.log;

import android.content.Context;

public class CrashHandler implements Thread.UncaughtExceptionHandler {

    public static final String TAG = CrashHandler.class.getSimpleName();

    private static final CrashHandler sInstance = new CrashHandler();

    private Context mContext;

    public static CrashHandler getInstance() {
        return sInstance;
    }

    private CrashHandler() {
    }

    public void init(Context context) {
        mContext = context;
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        // un-install the handler
        Thread.setDefaultUncaughtExceptionHandler(null);

        try {
            FileLogger.openPrint(mContext);
            FileLogger.print("Error happened", ex);
            Throwable causes = ex.getCause();
            while (causes != null) {
                FileLogger.print("Error cause", causes);
                causes = causes.getCause();
            }
            FileLogger.flush();
            FileLogger.closePrint();
        } catch (Throwable e) {
            e.printStackTrace();
        }
        System.exit(1);
    }
}


