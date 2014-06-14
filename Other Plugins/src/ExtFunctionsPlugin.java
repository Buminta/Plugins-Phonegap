package org.plugins;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.security.MessageDigest;

import android.content.*;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.view.WindowManager;
import android.widget.Toast;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.widget.EditText;

import org.apache.cordova.CordovaActivity;
import org.apache.cordova.api.Plugin;

import org.apache.cordova.api.PluginResult;

import com.edc.classbook.util.encryption.ClassBookEncryption;
import com.tvb.classbook.store.MainActivity;
import com.tvb.classbook.store.R;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.NotificationManager;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.media.audiofx.BassBoost.Settings;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.text.style.ForegroundColorSpan;
import android.util.Log;

import static java.lang.Integer.getInteger;

public class ExtFunctionsPlugin extends Plugin {
    private NotificationManager mNotifyManager;
    private NotificationCompat.Builder mBuilder;

    @Override
    public PluginResult execute(String action, JSONArray args, String callbackId) {

        if (action.equals("installAPK")) {
            try {
                String list = this.installAPK(args.getString(0));
                return new PluginResult(PluginResult.Status.OK, list);
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        if (action.equals("closeServiceAPK")) {
            this.onOffAPKCall(false);
            return new PluginResult(PluginResult.Status.OK);
        }
        if (action.equals("renameFile")) {
            try {
                this.renameFile(args.getString(0), args.getString(1));
                return new PluginResult(PluginResult.Status.OK);
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        if (action.equals("moveDir")) {
            try {
                this.moveDir(args.getString(0), args.getString(1));
                return new PluginResult(PluginResult.Status.OK);
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        if (action.equals("checkMD5")) {
            try {
                JSONArray json = args.getJSONArray(0);
                String err = "";
                int checkSum = 0;
                for (int i = 0; i < json.length(); i++) {
                    JSONObject tmp = json.getJSONObject(i);
                    String md5 = this.fileToMD5(tmp.getString("file"));
                    Log.d("NGOC TRINH MD5: ", md5 + "-" + tmp.getString("md5"));
                    if (md5.equals(tmp.getString("md5")) || (!tmp.getBoolean("checkMD5")))
                        checkSum++;
                    else {
                        err += (tmp.getString("file") + ";");
                    }
                }
                if (checkSum == json.length())
                    return new PluginResult(PluginResult.Status.OK, true);
                return new PluginResult(PluginResult.Status.ERROR, err);
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        if (action.equals("getRomVersion")) {
            String romVersion = Build.DISPLAY;
            return new PluginResult(PluginResult.Status.OK, romVersion);
        }
        if (action.equals("getSerial")) {
            String model1 = Build.SERIAL;
            String model2 = getSystemProperty("ro.boot.serialno");
            String model3 = getSystemProperty("ro.serialno");
            String model = (model1 != "unknown") ? model1
                    : ((model2 != "") ? model2 : ((model3 != "") ? model3 : ""));
            return new PluginResult(PluginResult.Status.OK, model);
        }
        if (action.equals("notify")) {
            try {
                this.showNoti(args.getString(0), args.getString(1),
                        args.getInt(2), args.getInt(3));
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return new PluginResult(PluginResult.Status.OK);
        }
        if (action.equals("maxOrderForShelf")) {
            try {
                return new PluginResult(PluginResult.Status.OK, String.valueOf(this.maxOrderForShelf(args.getString(0))));
            } catch (Exception e) {
                e.printStackTrace();
                return new PluginResult(PluginResult.Status.ERROR, e.toString());
            }
        }
        if (action.equals("maxOrderByShelf")) {
            try {
                return new PluginResult(PluginResult.Status.OK, String.valueOf(this.maxOrderByShelf(args.getString(0), args.getString(1))));
            } catch (Exception e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                return new PluginResult(PluginResult.Status.ERROR, e.toString());
            }
        }
        if (action.equals("notifyOff")) {
            try {
                this.hideNoti(args.getString(0), args.getInt(1), args.getInt(2));
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return new PluginResult(PluginResult.Status.OK);
        }
        if (action.equals("encodeRC4")) {
            ClassBookEncryption cls = new ClassBookEncryption(
                    ClassBookEncryption.RC4);
            try {
                String str = new String();
                for (int i = 0; i < args.length(); i++) {
                    str += i == 0 ? cls.encryptString(args.getString(i)) : ";"
                            + cls.encryptString(args.getString(i));
                }
                return new PluginResult(PluginResult.Status.OK, str);
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        if (action.equals("decodeRC4")) {
            ClassBookEncryption cls = new ClassBookEncryption(
                    ClassBookEncryption.RC4);
            try {
                String str = new String();
                for (int i = 0; i < args.length(); i++) {
                    str += i == 0 ? cls.decryptString(args.getString(i)) : ";"
                            + cls.decryptString(args.getString(i));
                }
                return new PluginResult(PluginResult.Status.OK, str);
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return new PluginResult(PluginResult.Status.ERROR);
    }

    private int maxOrderForShelf(String dbfile) throws Exception {
        try {
            SQLiteDatabase mydb = SQLiteDatabase.openOrCreateDatabase(dbfile, null);
            Cursor c = mydb.rawQuery("SELECT * FROM tbl_shelf_list", null);
            int max = 0;
            ClassBookEncryption cls = new ClassBookEncryption(
                    ClassBookEncryption.RC4);
            c.moveToFirst();
            while (c.isAfterLast() == false) {
                int tmp = Integer.valueOf(cls.decryptString(c.getString(c.getColumnIndex("_order"))));
                if (tmp > max)
                    max = tmp;
                c.moveToNext();
            }
            return max;
        } catch (Exception e) {
            return 0;
        }
    }

    private int maxOrderByShelf(String dbfile, String shelf) throws Exception {
        try {
            SQLiteDatabase mydb = SQLiteDatabase.openOrCreateDatabase(dbfile, null);
            ClassBookEncryption cls = new ClassBookEncryption(
                    ClassBookEncryption.RC4);
            Cursor c = mydb.rawQuery("SELECT * FROM tbl_shelf_content WHERE shelf_code like '" + cls.encryptString(shelf) + "' ", null);
            int max = 0;
            c.moveToFirst();
            while (c.isAfterLast() == false) {
                int tmp = Integer.valueOf(cls.decryptString(c.getString(c.getColumnIndex("_order"))));
                if (tmp > max)
                    max = tmp;
                c.moveToNext();
            }
            c.close();
            mydb.close();
            return max;
        } catch (Exception e) {
            return 0;
        }
    }

    private void hideNoti(String title, int process, int transID) {
        Context context = this.cordova.getActivity().getApplicationContext();
        mNotifyManager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        mBuilder = new NotificationCompat.Builder(context).setSmallIcon(
                R.drawable.download).setContentText(title);
        mBuilder.setProgress(100, process, false);
        mNotifyManager.notify(transID, mBuilder.build());
    }

    private void showNoti(String title, String text, int process, int transID) {
        Context context = this.cordova.getActivity().getApplicationContext();
        mNotifyManager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        mBuilder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.download)
                .setContentTitle("Classbook [" + title + "]")
                .setContentText(text);
        mBuilder.setProgress(100, process, false);
        // mBuilder.setProgress(0, 0, true);
        mNotifyManager.notify(transID, mBuilder.build());
    }

    private String installAPK(String uri) throws IOException {
        Context context = this.cordova.getActivity().getApplicationContext();
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        File f = new File(uri);
        File[] files = f.listFiles();
        String list = "";
        for (File inFile : files) {
            String extension = android.webkit.MimeTypeMap
                    .getFileExtensionFromUrl(Uri.fromFile(inFile).toString());
            String mimetype = android.webkit.MimeTypeMap.getSingleton()
                    .getMimeTypeFromExtension(extension);
            if (inFile.isFile()
                    && (extension.equals("apk") || extension.equals("APK"))) {
                list += (inFile.getCanonicalPath() + ";");
                try {
                    this.copyDirectoriesDataApk(uri);
                    this.onOffAPKCall(true);
                    Thread.sleep(1000);
                    intent.setDataAndType(Uri.fromFile(inFile), mimetype);
                    context.startActivity(intent);
                    Thread.sleep(5000);
                    this.onOffAPKCall(false);
                } catch (Exception e) {
                    return "FALSE";
                }
            }
        }
        return list;
    }

    private boolean copyDirectoriesDataApk(String uri) throws IOException {
        try {
            InputStream in = new FileInputStream(uri + "/config.cfg");
            if (in != null) {
                InputStreamReader input = new InputStreamReader(in);
                BufferedReader buffreader = new BufferedReader(input);
                String line = "";
                while ((line = buffreader.readLine()) != null) {
                    String tmp = line.replace(" ", "");
                    if (tmp.startsWith("des=")) {
                        tmp = tmp.substring(4);
                        if (!tmp.startsWith("/sdcard")) return false;
                        File f = new File(uri);
                        File[] files = f.listFiles();
                        for (File inFile : files) {
                            if (inFile.isDirectory()) {
                                Log.d(inFile.toString(), tmp + "/" + inFile.getName());
                                this.moveDir(inFile.toString(), tmp + "/" + inFile.getName());
                            }
                        }
                    }
                }
                buffreader.close();
            }
            in.close();
        } catch (Exception e) {
        }
        return false;
    }


    private boolean onOffAPKCall(boolean key) {
        Log.d("ON OFF APK INSTALL", key?"ON":"OFF");
        String PACKAGE_NAME = "com.tvb.cbservice";
        String CLASS_NAME = PACKAGE_NAME + ".CBService";
        int CMD_OWNERINFO = 1;
        int CMD_AUTOROTATE = 2; // read the CBService.java file to find the correct command's name
        int CMD_UNKNOWSOURCE = 3;
        Intent ii = new Intent();
        ii.setComponent(new ComponentName(PACKAGE_NAME, CLASS_NAME));
        // put command name
        ii.putExtra("CMD", CMD_UNKNOWSOURCE);
        // put command's argument
        ii.putExtra("UNKNOWSOURCE", key);
        //start CBService activity with intent.
        this.cordova.getActivity().startActivity(ii);
        return true;
    }

    private void renameFile(String pathA, String pathB) {
        File pathOld = new File(pathA);
        File pathNew = new File(pathB);
        pathOld.renameTo(pathNew);
    }

    private void moveDir(String dirA, String dirB) {
        File dirOld = new File(dirA);
        File dirNew = new File(dirB);
        if (!dirNew.exists()) {
            dirNew.mkdirs();
        }
        try {
            File[] files = dirOld.listFiles();
            for (File inFile : files) {
                File tmp = new File(dirNew.getAbsolutePath() + "/" + inFile.getName());
                inFile.renameTo(tmp);
            }
            dirOld.delete();
        } catch (Exception e) {
        }
    }

    public static String fileToMD5(String filePath) {
        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(filePath);
            byte[] buffer = new byte[1024];
            MessageDigest digest = MessageDigest.getInstance("MD5");
            int numRead = 0;
            while (numRead != -1) {
                numRead = inputStream.read(buffer);
                if (numRead > 0)
                    digest.update(buffer, 0, numRead);
            }
            byte[] md5Bytes = digest.digest();
            return convertHashToString(md5Bytes);
        } catch (Exception e) {
            return null;
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (Exception e) {
                }
            }
        }
    }

    private static String convertHashToString(byte[] md5Bytes) {
        String returnVal = "";
        for (int i = 0; i < md5Bytes.length; i++) {
            returnVal += Integer.toString((md5Bytes[i] & 0xff) + 0x100, 16)
                    .substring(1);
        }
        return returnVal.toUpperCase();
    }

    private String getSystemProperty(String propName) {
        Class<?> clsSystemProperties = tryClassForName("android.os.SystemProperties");
        Method mtdGet = tryGetMethod(clsSystemProperties, "get", String.class);
        return tryInvoke(mtdGet, null, propName);
    }

    private Class<?> tryClassForName(String className) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    private Method tryGetMethod(Class<?> cls, String name,
                                Class<?>... parameterTypes) {
        try {
            return cls.getDeclaredMethod(name, parameterTypes);
        } catch (Exception e) {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private <T> T tryInvoke(Method m, Object object, Object... args) {
        try {
            return (T) m.invoke(object, args);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (Exception e) {
            return null;
        }
    }

}