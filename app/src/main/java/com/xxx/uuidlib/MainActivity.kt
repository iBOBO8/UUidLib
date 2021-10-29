package com.xxx.oaidlib

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.xxx.uuidlib.R
import kotlinx.coroutines.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        getUuidJavaSync()
        getUuidJavaAsync()

        getUuidKotlin(this)
    }

    private fun getUuidKotlin(context: Context){
        CoroutineScope(Dispatchers.IO).launch {
            var uuid = UuidUtil.getUUID(context)
            Log.e("UUID kotlin",uuid)
        }
    }

    private fun getUuidJavaSync(){
        var uuid = UUIDUtils.getUUID(this);
        Log.e("UUID sync",uuid)
    }
    private fun getUuidJavaAsync(){
        UUIDUtils.getUUID(this
        ) { uuid -> Log.e("UUID async", uuid!!) }

    }
}