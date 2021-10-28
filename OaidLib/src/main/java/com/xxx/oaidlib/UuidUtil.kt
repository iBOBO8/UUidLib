package com.xxx.oaidlib


import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

object UuidUtil {

    /*var uuid: String? = null;
    private val mmkv = MMKV.mmkvWithID("unique_id")
    private const val FILE_NAME = "unique_file";
    private const val MMKV_KEY = "unique_id";

    suspend fun getUUID(context: Context): String {
        uuid?.let {
            return it;
        }
        var file = File(context.filesDir, FILE_NAME);
        if (!file.exists()) {
            uuid = getSavedUniqueID(context);
            uuid?.let {
                writeUniqueID2File(file, it)
            }
        }
        uuid = readUniqueIDFromFile(file)
        return uuid!!
    }

    @Throws(IOException::class)
    fun readUniqueIDFromFile(file: File): String? {
        val f = RandomAccessFile(file, "r");
        val bytes = ByteArray(f.length().toInt());
        f.readFully(bytes)
        f.close()
        return String(bytes);
    }

    @Throws(IOException::class)
    fun writeUniqueID2File(file: File, id: String) {
        val out = FileOutputStream(file);
        out.write(id.toByteArray());
        out.close()
    }

    suspend fun getUniqueID(context: Context): String {
        var imei = DeviceID.getUniqueID(context)
        if (!TextUtils.isEmpty(imei)) {
            return imei
        }

        var oaid = getOAID(context)
        if (!TextUtils.isEmpty(oaid)) {
            return oaid;
        }

        var androidID = DeviceID.getAndroidID(context);
        if (!TextUtils.isEmpty(androidID)) {
            return androidID;
        }


        var wideWineID = DeviceID.getWidevineID();
        if (!TextUtils.isEmpty(wideWineID)) {
            return wideWineID;
        }

        var pseudoID = DeviceID.getPseudoID();
        if (!TextUtils.isEmpty(pseudoID)) {
            return pseudoID;
        }

        return DeviceID.getGUID(context)
    }

    suspend fun getSavedUniqueID(context: Context):String{
        var encodeID: String? = mmkv.getString("unique_id", null)
        encodeID?.let {
            return it;
        }
        var uniqueid = getUniqueID(context);

        var hashedUniqueID = getHashedUniqueID(uniqueid)

        mmkv.encode(MMKV_KEY,hashedUniqueID)

        return hashedUniqueID;
    }

     fun getHashedUniqueID(uniqueid:String): String {

        try {
            //获取md5加密对象
            val instance: MessageDigest = MessageDigest.getInstance("MD5")
            //对字符串加密，返回字节数组
            val digest: ByteArray = instance.digest(uniqueid.toByteArray())
            var sb: StringBuffer = StringBuffer()
            for (b in digest) {
                //获取低八位有效值
                var i: Int = b.toInt() and 0xff
                //将整数转化为16进制
                var hexString = Integer.toHexString(i)
                if (hexString.length < 2) {
                    //如果是一位的话，补0
                    hexString = "0" + hexString
                }
                sb.append(hexString)
            }
            return sb.toString()

        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        }

        return ""
    }

    suspend fun getOAID(context: Context): String = suspendCoroutine { continuation ->
        DeviceID.getOAID(context, object : IGetter {
            override fun onOAIDGetComplete(result: String?) {
                result?.let {
                    continuation.resume(it)
                    return
                }
                continuation.resume("")
            }

            override fun onOAIDGetError(error: Exception?) {
                continuation.resumeWithException(error!!)
            }
        })
    }*/


    suspend fun getUUID(context: Context): String = suspendCoroutine { continuation ->
        APPUtil.getUUID(context
        ) { uuid -> continuation.resume(uuid!!) }
    }
}