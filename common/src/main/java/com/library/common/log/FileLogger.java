package com.library.common.log;

import android.content.Context;

/**
 * 日志记录
 *
 * @author Administrator
 */
public class FileLogger {

    private LoggerImp instance;

    // 日志名称
    private String loggerName;

    // 调试时可以设置为true；发布时需要设置为false;
    protected static boolean isOpen = true;

    /**
     * 开始输入日志信息<br\>
     * （只作为程序日志开关，在个人设置中开启，其他应用中不得调用）
     */
    public static void openPrint(Context ctx) {
        if (isOpen) {
            LoggerImp.instance.setContext(ctx);
        }
    }

    /**
     * 关闭日志打印 <br\>
     * （只作为程序日志开关，在个人设置中开启，其他应用中不得调用）
     */
    public static void closePrint() {
        if (isOpen) {
            LoggerImp.instance.close();
        }
    }

    public static void flush() throws Exception {
        if (isOpen) {
            LoggerImp.instance.print();
        }
    }

    private static FileLogger logger = new FileLogger("[FileLogger]");

    /**
     * 输出日志信息
     *
     * @param msg String 日志
     */
    public synchronized static void print(String msg) {
        if (isOpen) {
            logger.output(msg);
        }
    }

    /**
     * 输出日志信息及异常发生的详细信息
     *
     * @param msg String 日志
     * @param e   Exception
     */
    public synchronized static void print(String msg, Throwable e) {
        if (isOpen) {
            logger.output(msg, e);
        }
    }

    /**
     * 构造函数
     *
     * @param name String
     */
    public FileLogger(String name) {
        loggerName = name;
        instance = LoggerImp.getInstance();
    }

    /**
     * 输出日志信息
     *
     * @param msg String 日志
     */
    public synchronized void output(String msg) {
        if (isOpen) {
            instance.submitMsg(loggerName + " " + msg);
        }
    }

    /**
     * 输出日志信息及异常发生的详细信息
     *
     * @param msg String 日志
     * @param e   Exception
     */
    public synchronized void output(String msg, Throwable e) {
        if (isOpen) {
            StringBuilder sb = new StringBuilder(msg);
            sb.append(loggerName).append(" : ").append(msg).append("\n");
            sb.append(e.getClass()).append(" : ");
            sb.append(e.getLocalizedMessage());
            sb.append("\n");
            StackTraceElement[] stack = e.getStackTrace();
            for (StackTraceElement trace : stack) {
                sb.append("\t at ").append(trace.toString()).append("\n");
            }
            instance.submitMsg(sb.toString());
        }
    }

    /**
     * 打印当前的内存信息
     */
    public void printCurrentMemory() {
        if (isOpen) {
            StringBuilder logs = new StringBuilder();
            long freeMemory = Runtime.getRuntime().freeMemory() / 1024;
            long totalMemory = Runtime.getRuntime().totalMemory() / 1024;
            long maxMemory = Runtime.getRuntime().maxMemory() / 1024;
            logs.append("\t[Memory_free]: ").append(freeMemory).append(" kb");
            logs.append("\t[Memory_total]: ").append(totalMemory).append(" kb");
            logs.append("\t[Memory_max]: ").append(maxMemory).append(" kb");
            instance.submitMsg(loggerName + " " + logs.toString());
        }
    }
}