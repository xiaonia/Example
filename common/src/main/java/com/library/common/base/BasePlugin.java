package com.library.common.base;

import android.app.Activity;

/**
 * Created by zhangle on 03/03/2017.
 */
public abstract class BasePlugin {

    protected String mPluginName;

    protected BasePlugin(String name) {
        this.mPluginName = name;
    }

    public String getPluginName() {
        return mPluginName;
    }

    abstract public void init();

    abstract public void start(Activity host);

    abstract public void prepareAppContext(Activity host);
}
