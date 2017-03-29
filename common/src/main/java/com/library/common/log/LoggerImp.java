package com.library.common.log;

import android.content.Context;
import android.os.Environment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * 日志输出的具体实现类
 */
class LoggerImp {

    private static String sCrashLogDir = "/Log/crash/";

    private Context context;

    static LoggerImp instance = new LoggerImp();

    // 日志存放的队列
    private List<String> printOutList = new ArrayList<String>();

    // 日志文件
    private FileOutputStream fos = null;

    // 日志输出流
    private PrintStream print = null;

    // 时间格式
    private DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    // 当前天，每天生成一个日志文件
    private int currDay = -1;

    class GcCheck implements Runnable {

        boolean flag = true;

        @Override
        public void run() {
            int count = 40;
            StringBuilder logs = new StringBuilder();
            while (flag) {
                if (count >= 50) {
                    long freeMemory = Runtime.getRuntime().freeMemory() / 1024;
                    long totalMemory = Runtime.getRuntime().totalMemory() / 1024;
                    long maxMemory = Runtime.getRuntime().maxMemory() / 1024;
                    logs.append("\t[Memory_free]:").append(freeMemory)
                            .append(" kb");
                    logs.append("\t[Memory_total]:").append(totalMemory)
                            .append(" kb");
                    logs.append("\t[Memory_max]:").append(maxMemory)
                            .append(" kb");
                    synchronized (LoggerImp.class) {
                        printOutList.add(logs.toString());
                    }
//					Log.i("Memory", logs.toString());
                    logs.setLength(0);
                    if (freeMemory < 400) {
                        System.gc();
                        count = 40;
                        logs.append("<GC>");
                    } else {
                        count = 0;
                    }
                }
                try {
                    count++;
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    ;

    /**
     * 得到单例对象 [url=home.php?mod=space&uid=309376]@return[/url] LoggerImp
     */
    public static LoggerImp getInstance() {
        return instance;
    }

    /**
     * 私有方法，单例
     */
    private LoggerImp() {
    }

    // void listenGC(){
    // gcRun.flag = true;
    // new Thread(gcRun).start();
    // }

    // void stopLintenGC(){
    // gcRun.flag = false;
    // }

    // 初始化文件输出流
    private void initPrint() {
        Calendar date = Calendar.getInstance();
        currDay = date.get(Calendar.DAY_OF_YEAR);
        DateFormat dfm = new SimpleDateFormat("yyyy-MM-dd");
        String fileName = new String(dfm.format(date.getTime()) + ".txt");
        String path;
        try {
            if (null != print) {
                close();
            }
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                path = Environment.getExternalStorageDirectory().getAbsolutePath() + sCrashLogDir;
                File dir = new File(path);
                if (!dir.exists()) {
                    dir.mkdirs();
                }
                File output = new File(path + fileName);
                fos = new FileOutputStream(output, true);
            } else {
                fos = context.openFileOutput(fileName, Context.MODE_WORLD_READABLE);
            }
            print = new PrintStream(fos, true);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setContext(Context ctx) {
        this.context = ctx;
    }

    public void close() {
        if (print != null) {
            print.flush();
            print.close();
            print = null;
        }
        try {
            if (fos != null) {
                fos.close();
                fos = null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 向队列中增加日志数据
     *
     * @param msg String 日志数据
     */
    protected synchronized void submitMsg(String msg) {
        synchronized (LoggerImp.class) {
            printOutList.add(msg);
        }
    }

    // 线程需要重复执行的操作
    public void print() throws Exception {
        String line;
        while (!printOutList.isEmpty()) {
            line = printOutList.remove(0);
            if (null != line) {
                printToFile(line);
            }
        }
    }

    // 把数据持久到文件
    private void printToFile(String line) {
        Calendar date = Calendar.getInstance();
        int day = date.get(Calendar.DAY_OF_YEAR);
        if (day != currDay) {
            initPrint();
        }
        if (null == print) {
            return;
        }
        print.println(">>> " + format.format(date.getTime()) + " -- " + line);
        print.flush();
    }

    public static String getCrashLogDir() {
        return sCrashLogDir;
    }

    public static void setCrashLogDir(String dirs) {
        if (dirs != null) {
            LoggerImp.sCrashLogDir = dirs;
        }
    }

}