package me.xfly.oaiddemo;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.github.gzuliyujiang.oaid.DeviceID;
import com.github.gzuliyujiang.oaid.DeviceIdentifier;
import com.github.gzuliyujiang.oaid.IGetter;

import java.io.File;
import java.io.RandomAccessFile;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_READ_PHONE_STATE = 1;
    private static String[] PERMISSIONS_READ_PHONE_STATE = {
            "android.permission.READ_PHONE_STATE",
            "android.permission.READ_PRIVILEGED_PHONE_STATE"};
    public static final String TAG = "MainActivity";
    TextView tv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tv = findViewById(R.id.tv);
        //verifyStoragePermissions(this);
        getIMEI();

    }


    public void verifyStoragePermissions(Activity activity) {

        try {
            //检测是否有写的权限
            int permission = ActivityCompat.checkSelfPermission(activity,
                    "android.permission.READ_PRIVILEGED_PHONE_STATE");
            if (permission != PERMISSION_GRANTED) {
                // 没有写的权限，去申请写的权限，会弹出对话框
                ActivityCompat.requestPermissions(activity, PERMISSIONS_READ_PHONE_STATE, REQUEST_READ_PHONE_STATE);
            } else {
                getIMEI();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && grantResults[0] == PERMISSION_GRANTED) {
            getIMEI();
        } else {
            Toast.makeText(this, "You denied the permission", Toast.LENGTH_SHORT).show();
        }
    }

    void getIMEI() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        StringBuilder builder = new StringBuilder();
        //DeviceIdentifier.register(getApplication());
        // 获取IMEI，只支持Android 10之前的系统，需要READ_PHONE_STATE权限，可能为空
        String imei = DeviceIdentifier.getIMEI(this);
        builder.append("IMEI=");
        builder.append("\n");
        builder.append(imei);
        builder.append("\n");
        builder.append("\n");
        // 获取安卓ID，可能为空
        String androidid = DeviceIdentifier.getAndroidID(this);
        builder.append("androidid=");
        builder.append("\n");
        builder.append(androidid);
        builder.append("\n");
        builder.append("\n");
        // 获取数字版权管理ID，可能为空
        String widevineid = DeviceIdentifier.getWidevineID();
        builder.append("widevineid=");
        builder.append("\n");
        builder.append(widevineid);
        builder.append("\n");
        builder.append("\n");
        // 获取伪造ID，根据硬件信息生成，不会为空，有大概率会重复
        String pseudoid = DeviceIdentifier.getPseudoID();
        builder.append("pseudoid=");
        builder.append("\n");
        builder.append(pseudoid);
        builder.append("\n");
        builder.append("\n");
        // 获取GUID，随机生成，不会为空
        String guid = DeviceIdentifier.getGUID(this);
        // 是否支持OAID/AAID
        boolean flag  = DeviceID.supportedOAID(this);
        // 获取OAID/AAID，同步调用
        String oaid = DeviceIdentifier.getOAID(this);
         oaid = DeviceID.getOAID();

        // 获取OAID/AAID，异步回调
        DeviceID.getOAID(this, new IGetter() {
            @Override
            public void onOAIDGetComplete(String result) {
                // 不同厂商的OAID/AAID格式是不一样的，可进行MD5、SHA1之类的哈希运算统一
                Log.i(TAG, result);
                builder.append("OAID=");
                builder.append("\n");
                builder.append(result);
                builder.append("\n");
                builder.append("\n");
                tv.setText(builder.toString());
            }

            @Override
            public void onOAIDGetError(Exception error) {
                // 获取OAID/AAID失败
            }
        });
    }
}