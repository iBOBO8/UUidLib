package com.xxx.oaidlib

import android.app.Application
import com.tencent.mmkv.MMKV

class MyApp: Application() {
    override fun onCreate() {
        super.onCreate()
        MMKV.initialize(this);
    }
}