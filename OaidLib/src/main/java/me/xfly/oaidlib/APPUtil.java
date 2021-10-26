package me.xfly.oaidlib;

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

public class APPUtil {

    private static final String FILE_NAME = "unique_id_file";
    private static final String UNIQUE_ID_KEY = "unique_id";
    private static String uuid = null;
    private static final MMKV mmkv = MMKV.mmkvWithID(UNIQUE_ID_KEY);


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

    public static void getUUID(Context context, UUIDCallback callback) {
        if (!TextUtils.isEmpty(uuid)) {
            callback.onSuccess(uuid);
            return;
        }

        uuid = getUuidFromMMKV();

        if (!TextUtils.isEmpty(uuid)) {
            callback.onSuccess(uuid);
            return;
        }

        uuid = getUuidFromFile(context);

        if (!TextUtils.isEmpty(uuid)) {
            mmkv.encode(UNIQUE_ID_KEY, uuid);
            callback.onSuccess(uuid);
            return;
        }
        getUniqueID(context, new IGetter() {
            @Override
            public void onOAIDGetComplete(String result) {
                if (!TextUtils.isEmpty(result)) {

                    uuid = MD5(result);
                    mmkv.encode(UNIQUE_ID_KEY, uuid);
                    writeString2File(context, uuid);
                    callback.onSuccess(uuid);
                }

            }

            @Override
            public void onOAIDGetError(Exception error) {
            }
        });
    }


    public static String MD5(String uniqueID) {
        //获取md5加密对象
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

    private static void getUniqueID(Context context, IGetter iGetter) {
        String uniqueID = DeviceID.getUniqueID(context);
        if (!TextUtils.isEmpty(uniqueID)) {
            iGetter.onOAIDGetComplete(uniqueID);
            return;
        }

        DeviceID.getOAID(context, new IGetter() {
            @Override
            public void onOAIDGetComplete(String result) {
                if (!TextUtils.isEmpty(result)) {
                    iGetter.onOAIDGetComplete(result);
                    return;
                }
                getMaybeNotUniqueID(context, iGetter);
            }

            @Override
            public void onOAIDGetError(Exception error) {
                getMaybeNotUniqueID(context, iGetter);
            }
        });


    }

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

    private static String getUuidFromFile(Context context) {

        int permission = ActivityCompat.checkSelfPermission(context,
                "android.permission.READ_EXTERNAL_STORAGE");
        if (permission != PERMISSION_GRANTED) {
            return "";
        }
        return readStringFromFile(context);
    }

    private static String getUuidFromMMKV() {
        return mmkv.getString(UNIQUE_ID_KEY, "");
    }

    private static void writeString2File(Context context, String str) {
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
