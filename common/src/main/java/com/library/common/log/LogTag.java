package com.library.common.log;

public class LogTag {
    private static String sLogTag = "Log";
    private static final String LOG_PREFIX = sLogTag;
    private static final int LOG_PREFIX_LENGTH = sLogTag.length();
    private static final int MAX_LOG_TAG_LENGTH = 23;


    /**
     * Get the log tag to apply to logging.
     */
    public static String getLogTag() {
        return sLogTag;
    }

    /**
     * Sets the app-wide log tag to be used in most log messages, and for
     * enabling logging verbosity. This should be called at most once, during
     * app start-up.
     */
    public static void setLogTag(final String logTag) {
        sLogTag = logTag;
    }

    public static String makeLogTag(String str) {
        if (str.length() > MAX_LOG_TAG_LENGTH - LOG_PREFIX_LENGTH) {
            return LOG_PREFIX + str.substring(0, MAX_LOG_TAG_LENGTH - LOG_PREFIX_LENGTH - 1);
        }

        return LOG_PREFIX + str;
    }
}
