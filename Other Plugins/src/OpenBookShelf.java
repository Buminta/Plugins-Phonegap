package org.plugins;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import android.content.pm.PackageManager;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import org.apache.cordova.CordovaActivity;
import org.apache.cordova.api.Plugin;
import org.apache.cordova.api.PluginResult;

import com.edc.classbook.util.encryption.ClassBookEncryption;
import com.tvb.classbook.store.MainActivity;
import com.tvb.classbook.store.R;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.text.style.ForegroundColorSpan;
import android.util.Log;

public class OpenBookShelf extends Plugin {
    private Context context;

    @Override
    public PluginResult execute(String action, JSONArray args, String callbackId) {
        if (action.equals("openBook")) {
            try {
                return this.openBookFromApp(args.getString(0));
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        if (action.equals("checkAppInstall")) {
            try {
                return this.checkAppInstall(args.getString(0));
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        if (action.equals("reload")) {
            try {
                return this.reloadBookShelf(args.getString(0));
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        if (action.equals("openApp")) {
            try {
                return this.openApp(args.getString(0));
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return new PluginResult(PluginResult.Status.ERROR);
    }

    private PluginResult reloadBookShelf(String shelf) {
        context = this.cordova.getActivity().getApplicationContext();
        Intent it = new Intent("com.tinhvan.tvb.bookshelf.action.reload");
        it.putExtra("shelfcode", shelf);
        context.sendBroadcast(it);
        return new PluginResult(PluginResult.Status.OK);
    }

    private PluginResult openBookFromApp(String path) {
        context = this.cordova.getActivity().getApplicationContext();
        Intent intentOpen = context.getPackageManager()
                .getLaunchIntentForPackage("com.tinhvan.tvb.classbook");
        if (intentOpen == null) {
            // TODO xu li TH ko tim thay ereader
            return new PluginResult(PluginResult.Status.ERROR,
                    "Thiếu trình đọc sách");
        }
        Uri uri = Uri.parse(path);// vi
        // du:/sdcard/storage0/TVB/SGK01/GK01TBH08/GK01TBH08.pdf;
        // khong hardcode /sdcard, su
        // dung
        // Environment.getExternalStorage
        // de lay duong dan toi sdcard
        intentOpen.setData(uri);
        intentOpen.setAction(Intent.ACTION_VIEW);
        openBook(intentOpen);
        return new PluginResult(PluginResult.Status.OK);
    }

    private PluginResult openApp(String packageName) {
        Activity activity = this.cordova.getActivity();
        Intent LaunchIntent = activity.getPackageManager().getLaunchIntentForPackage(packageName);
        activity.startActivity(LaunchIntent);
        return new PluginResult(PluginResult.Status.OK);
    }

    private PluginResult checkAppInstall(String packagename) {
        context = this.cordova.getActivity().getApplicationContext();
        PackageManager pm = context.getPackageManager();
        try {
            pm.getPackageInfo(packagename, PackageManager.GET_ACTIVITIES);
            return new PluginResult(PluginResult.Status.OK);
        } catch (PackageManager.NameNotFoundException e) {
            return new PluginResult(PluginResult.Status.ERROR);
        }
    }

    private void openBook(Intent intentOpen) {
        if (context != null) {
            killClassbookProcess();
            intentOpen.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intentOpen.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intentOpen.addFlags(Intent.FLAG_ACTIVITY_TASK_ON_HOME);
            intentOpen.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
            context.startActivity(intentOpen);
        }

    }

    private void killClassbookProcess() {
        try {

            ActivityManager activityManager = (ActivityManager) context
                    .getSystemService(Activity.ACTIVITY_SERVICE);
            activityManager
                    .killBackgroundProcesses("com.tinhvan.tvb.classbook");

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}