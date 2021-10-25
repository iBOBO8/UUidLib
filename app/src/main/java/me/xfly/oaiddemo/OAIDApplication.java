package me.xfly.oaiddemo;

import android.app.Application;

import com.github.gzuliyujiang.oaid.DeviceIdentifier;

public class OAIDApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        DeviceIdentifier.register(this);
        String oaid = DeviceIdentifier.getOAID(this);

    }
}
