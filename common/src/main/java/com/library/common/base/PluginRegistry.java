package com.library.common.base;

import com.library.common.utils.StringUtils;

import android.content.Context;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;

import dalvik.system.DexFile;

/**
 * Created by zhangle on 03/03/2017.
 */
public class PluginRegistry {
    private static final PluginRegistry sInstance = new PluginRegistry();

    private Hashtable<String, BasePlugin> mRegistry = new Hashtable<String, BasePlugin>();

    public static PluginRegistry getInstance() {
        return sInstance;
    }

    public void init(Context appCtx) {
        try {
            DexFile df = new DexFile(appCtx.getPackageCodePath());
            for (Enumeration<String> iter = df.entries(); iter.hasMoreElements(); ) {
                String cName = iter.nextElement();
                if (!cName.toLowerCase().contains("plugin")) {
                    continue;
                }
                Class c = getClassByName(cName);
                if (c != null && BasePlugin.class.isAssignableFrom(c)) {
                    try {
                        c.newInstance();
                    } catch (InstantiationException e) {
                        e.printStackTrace();
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void register(BasePlugin plugin) throws Exception {
        if (plugin == null) {
            return;
        }
        if (mRegistry.contains(plugin.getPluginName())) {
            throw new Exception("Plugin '" + plugin.getPluginName() + "' is already registered.");
        }
        if (StringUtils.isEmpty(plugin.getPluginName())) {
            throw new Exception("Plugin name is empty");
        }
        mRegistry.put(plugin.getPluginName(), plugin);
    }

    public BasePlugin getPlugin(String name) {
        return mRegistry.get(name);
    }

    private Class getClassByName(String className) {
        Class c = null;
        try {
            c = ClassLoader.getSystemClassLoader().loadClass(className);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        if (c == null) {
            try {
                c = PluginRegistry.class.getClassLoader().loadClass(className);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        return c;
    }
}
