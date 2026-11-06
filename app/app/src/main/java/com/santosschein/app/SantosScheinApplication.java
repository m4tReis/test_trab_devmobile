package com.santosschein.app;

import android.app.Application;

import com.santosschein.app.core.config.AppContainer;

public final class SantosScheinApplication extends Application {

    private AppContainer appContainer;

    @Override
    public void onCreate() {
        super.onCreate();
        appContainer = new AppContainer(this);
    }

    public AppContainer getAppContainer() {
        return appContainer;
    }
}
