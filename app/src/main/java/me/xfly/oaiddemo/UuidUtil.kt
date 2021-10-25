package me.xfly.oaiddemo

import android.content.Context
import com.github.gzuliyujiang.oaid.DeviceID
import com.github.gzuliyujiang.oaid.IGetter
import kotlinx.coroutines.*
import java.lang.Exception
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

object UuidUtil {
    suspend fun getOAID(context: Context): String = suspendCoroutine { continuation ->
        DeviceID.getOAID(context, object : IGetter {
            override fun onOAIDGetComplete(result: String?) {
                result?.let { continuation.resume(it) }
                continuation.resume("")
            }

            override fun onOAIDGetError(error: Exception?) {
                continuation.resumeWithException(error!!)
            }
        })
    }

    fun getOaid(context: Context): String {
        var oaid = ""
        CoroutineScope(Dispatchers.IO).launch {
             oaid = getOAID(context);
        }

        return oaid
    }
}