package com.xxx.oaidlib;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

import android.content.Context;
import android.text.TextUtils;

import androidx.core.app.ActivityCompat;

import com.github.gzuliyujiang.oaid.DeviceID;
import com.github.gzuliyujiang.oaid.IGetter;
import com.tencent.mmkv.MMKV;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;

public class UUIDUtils {

    //文件名 如果有文件权限，uuid 会存在文件中
    private static final String FILE_NAME = "INSTALLATION";
    //mmkv key
    private static final String UNIQUE_ID_KEY = "deviceId";
    private static String uuid = null;
    // TODO: 2021/10/29 切换成 MMKVUtil
    private static final MMKV mmkv = MMKV.mmkvWithID(UNIQUE_ID_KEY);

    /**
     * 同步获取 UUID，第一次有可能为空
     * */
    public static String getUUID(Context context) {

        if (!TextUtils.isEmpty(uuid)) {
            return uuid;
        }

        uuid = getUuidFromMMKV();

        if (!TextUtils.isEmpty(uuid)) {
            return uuid;
        }

        uuid = getUuidFromFile(context);

        if (!TextUtils.isEmpty(uuid)) {
            mmkv.encode(UNIQUE_ID_KEY, uuid);
            return uuid;
        }

        return "";
    }

    /**
     * 异步获取UUID，OAID 第一次会通过 AIDL 调用系统服务获取
     * */
    public static void getUUID(Context context, UUIDCallback callback) {
        //成员变量如果不为 null，直接返回
        if (!TextUtils.isEmpty(uuid)) {
            callback.onSuccess(uuid);
            return;
        }

        //从 MMKV 中查找
        uuid = getUuidFromMMKV();

        if (!TextUtils.isEmpty(uuid)) {
            callback.onSuccess(uuid);
            return;
        }

        //如果有文件权限，从文件中读取
        uuid = getUuidFromFile(context);

        if (!TextUtils.isEmpty(uuid)) {
            mmkv.encode(UNIQUE_ID_KEY, uuid);
            callback.onSuccess(uuid);
            return;
        }
        //IMEI、OAID 可以保证唯一的 id
        getUniqueID(context, new IGetter() {
            @Override
            public void onOAIDGetComplete(String result) {
                if (!TextUtils.isEmpty(result)) {
                    // hash 保证长度统一
                    uuid = hash(result);
                    //存到 mmkv
                    mmkv.encode(UNIQUE_ID_KEY, uuid);
                    //如果有文件权限，存到文件中
                    writeString2File(context, uuid);
                    //返回结果
                    callback.onSuccess(uuid);
                }

            }

            @Override
            public void onOAIDGetError(Exception error) {
            }
        });
    }

    /**
     * sha1 hash
     * */
    public static String hash(String uniqueID) {
        //获取sha1加密对象
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA1");
            digest.reset();
            digest.update(uniqueID.getBytes(StandardCharsets.UTF_8));
            byte[] bytes = digest.digest();

            StringBuilder builder = new StringBuilder();
            String temp;
            for (int n = 0; n < bytes.length; n++) {
                temp = (Integer.toHexString(bytes[n] & 0xFF));
                if (temp.length() == 1) {
                    builder.append("0");
                }
                builder.append(temp);
            }
            return builder.toString().toUpperCase(Locale.CHINA);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        return "";
    }

    /**
     * 获取 imei,oaid 等可以保证唯一的 id
     * */
    private static void getUniqueID(Context context, IGetter iGetter) {
        //获取 imei
        String uniqueID = DeviceID.getUniqueID(context);
        if (!TextUtils.isEmpty(uniqueID)) {
            iGetter.onOAIDGetComplete(uniqueID);
            return;
        }

        //异步获取 oaid
        DeviceID.getOAID(context, new IGetter() {
            @Override
            public void onOAIDGetComplete(String result) {
                if (!TextUtils.isEmpty(result)) {
                    iGetter.onOAIDGetComplete(result);
                    return;
                }
                //如果获取失败，则走备用方案
                getMaybeNotUniqueID(context, iGetter);
            }

            @Override
            public void onOAIDGetError(Exception error) {
                getMaybeNotUniqueID(context, iGetter);
            }
        });


    }

    /**
     * 可能不唯一的 id，
     * AndroidId 不同签名 key，值也不同，重置手机也会变
     * 作为备用方案
     * */
    private static void getMaybeNotUniqueID(Context context, IGetter iGetter) {
        String androidID = DeviceID.getAndroidID(context);
        if (!TextUtils.isEmpty(androidID)) {
            iGetter.onOAIDGetComplete(androidID);
            return;
        }


        String wideWineID = DeviceID.getWidevineID();
        if (!TextUtils.isEmpty(wideWineID)) {
            iGetter.onOAIDGetComplete(wideWineID);
            return;
        }

        String pseudoID = DeviceID.getPseudoID();
        if (!TextUtils.isEmpty(pseudoID)) {
            iGetter.onOAIDGetComplete(pseudoID);
            return;
        }

        iGetter.onOAIDGetComplete(DeviceID.getGUID(context));
    }

    /**
     * 从文件中获取存储的 UUID
     * */
    private static String getUuidFromFile(Context context) {

        int permission = ActivityCompat.checkSelfPermission(context,
                "android.permission.READ_EXTERNAL_STORAGE");
        //判断文件权限
        if (permission != PERMISSION_GRANTED) {
            return "";
        }
        return readStringFromFile(context);
    }

    /**
     * 从 MMKV 中读取 uuid
     */
    private static String getUuidFromMMKV() {
        return mmkv.getString(UNIQUE_ID_KEY, "");
    }

    /**
     * 将 uuid 写入文件
     * */
    private static void writeString2File(Context context, String str) {
        //判断文件权限
        int permission = ActivityCompat.checkSelfPermission(context,
                "android.permission.WRITE_EXTERNAL_STORAGE");
        if (permission != PERMISSION_GRANTED) {
            return;
        }

        try {
            File file = new File(context.getFilesDir(), FILE_NAME);
            FileOutputStream outputStream = new FileOutputStream(file);
            outputStream.write(str.getBytes());
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    /***
     * 从文件中读取 uuid
     */
    private static String readStringFromFile(Context context) {
        File file = new File(context.getFilesDir(), FILE_NAME);
        if (!file.exists()) {
            return "";
        }

        try {
            RandomAccessFile f = new RandomAccessFile(file, "r");
            byte[] bytes = new byte[(int) file.length()];
            f.readFully(bytes);
            f.close();
            return new String(bytes);
        } catch (IOException e) {
            return "";
        }
    }


}
