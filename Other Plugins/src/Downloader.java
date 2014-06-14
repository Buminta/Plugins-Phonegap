package org.plugins;

import java.io.File;
import org.plugins.DeleteDir;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.BreakIterator;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.R.string;
import android.util.Log;
import android.os.Environment;
import android.os.Process;

import org.apache.cordova.api.Plugin;
import org.apache.cordova.api.PluginResult;

import com.edc.classbook.util.codec.digest.Md5Crypt;
import com.edc.classbook.util.encryption.ClassBookEncryption;
import com.edc.classbook.util.encryption.RC4;

public class Downloader extends Plugin {
	private ArrayList<JSONObject> listTransID = new ArrayList<JSONObject>();
	private int sum = 0;
    private String token = "";

	@Override
	public PluginResult execute(String action, JSONArray args, String callbackId) {
		if (action.equals("testDownload")) {
			try {
				return this.tesDownload(args.getString(0),callbackId);
			} catch (IOException e) { 
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		if (!action.equals("downloadFile") && !action.equals("stop")
				&& !action.equals("pause") && !action.equals("play")
				&& !action.equals("deleteDir"))
			return new PluginResult(PluginResult.Status.INVALID_ACTION);

		if (action.equals("stop"))
			try {
				return this.stop(args.getInt(0));
			} catch (InterruptedException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			} catch (JSONException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			}
		if (action.equals("pause"))
			try {
				return this.pause(args.getInt(0));
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		if (action.equals("play"))
			try {
                if (args.length() > 1)
                    this.token = args.getString(1);
				return this.play(args.getInt(0));
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		if (action.equals("deleteDir"))
			try {
				return this.deleteDir(args.getString(0));
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		try {
			int trans_id = this.sum++;

			return this.downloadUrl(args, callbackId, trans_id);

		} catch (JSONException e) {
			e.printStackTrace();
			return new PluginResult(PluginResult.Status.JSON_EXCEPTION,
					e.getMessage());

		} catch (InterruptedException e) {
			e.printStackTrace();
			return new PluginResult(PluginResult.Status.ERROR, e.getMessage());
		}

	}

	private PluginResult deleteDir(String url) throws InterruptedException,
			JSONException {
		DeleteDir.remove(url);
		return new PluginResult(PluginResult.Status.OK);
	}

	private PluginResult pause(int trans_id) throws InterruptedException,
			JSONException {
		this.listTransID.get(trans_id).putOpt("pause", true);
		return new PluginResult(PluginResult.Status.OK);
	}

	private PluginResult stop(int trans_id) throws InterruptedException,
			JSONException {
		this.listTransID.get(trans_id).putOpt("stop", true);
		this.listTransID.get(trans_id).putOpt("pause", true);
		return new PluginResult(PluginResult.Status.OK);
	}

	private PluginResult play(int trans_id) throws InterruptedException,
			JSONException {
		this.listTransID.get(trans_id).putOpt("pause", false);
		this.listTransID.get(trans_id).putOpt("stop", false);
		return new PluginResult(PluginResult.Status.OK);
	}

	private PluginResult tesDownload(String fileURL, String callbackId) throws IOException {
		int readed = 0, totalReaded = 0, progress = 0;
		try {
			URL url = new URL(fileURL);
			HttpURLConnection ucon = (HttpURLConnection) url.openConnection();
			ucon.setRequestMethod("POST");
			ucon.connect();
			int totalSize = ucon.getContentLength();
			String[] arrName = fileURL.split("/");
			String fileName = arrName[arrName.length-1];
			String md5 = fileName.split("\\.")[0];
			String dirName = Environment.getExternalStorageDirectory()
					.getPath() + "/download";

			Boolean overwrite = true;

			File dir = new File(dirName);
			if (!dir.exists()) {
				dir.mkdirs();
			}

			File file = new File(dirName, fileName);
			InputStream is = ucon.getInputStream();
			byte[] buffer = new byte[1024];

			FileOutputStream fos = new FileOutputStream(file);
			while (true) {
				if ((readed = is.read(buffer)) <= 0)
					break;
				fos.write(buffer, 0, readed);
				totalReaded += readed;
				int newProgess = (int) (((float) totalReaded * 100) / (float) totalSize);
				if(newProgess!=progress){
					progress = newProgess;
					PluginResult res = new PluginResult(PluginResult.Status.OK, progress);
					res.setKeepCallback(true);
					success(res, callbackId);
				}
			}
			fos.close();
			if(!ExtFunctionsPlugin.fileToMD5(dirName+"/"+fileName).equals(md5.toUpperCase())){
                Log.d("NGOC TRINH: ",md5.toUpperCase()+'-'+ExtFunctionsPlugin.fileToMD5(dirName+'/'+fileName));
				return new PluginResult(PluginResult.Status.ERROR, "MD5 Checksum : "+md5+" "+ExtFunctionsPlugin.fileToMD5(dirName+"/"+fileName));
            }
            return new PluginResult(PluginResult.Status.OK, (progress+1));
		} catch (FileNotFoundException e) {
			return new PluginResult(PluginResult.Status.ERROR, 404);
		} catch (IOException e) {
			return new PluginResult(PluginResult.Status.ERROR, e.getMessage() + ": "+ progress + "%");
		}
	}
    private InputStream skipStream(InputStream is, long count) throws IOException {
        InputStream isTmp = is;
        long skipTmp = 0;
        long skiped = 0;

        while(skipTmp<count){
            skiped = isTmp.skip(count-skipTmp);
            Log.d("Downloading Skiped",String.valueOf(skiped));
            skipTmp += skiped;
        }
        Log.d("Downloading Total Skip",String.valueOf(count));

        return isTmp;
    }
	private PluginResult downloadUrl(JSONArray args, String callbackId,
			int trans_id) throws InterruptedException, JSONException {
		try {
			JSONObject objTransID = new JSONObject();
			objTransID.put("pause", false);
			objTransID.put("stop", false);
			this.listTransID.add(trans_id, objTransID);
			// Log.d("PhoneGapLog", "Downloading " + fileUrl + " into " +
			// dirName
			// + "/" + fileName);
			int i = 0;
			int readed = 0, totalReaded = 0, progress = 0, sumSize = 0;
			String fileName = "", dirName = "";
			JSONArray MD5 = new JSONArray();
			ArrayList<HttpURLConnection> ucon = new ArrayList<HttpURLConnection>();
			while (i < args.length()) {
				JSONObject params = args.getJSONObject(i);
				String fileUrl = params.getString("url");
//                fileUrl = "http://192.168.82.75/cbs/tools/test_xsend";

				URL url = new URL(fileUrl);
				ucon.add((HttpURLConnection) url.openConnection());
				ucon.get(i).setRequestMethod("POST");
				ucon.get(i).connect();
				sumSize += ucon.get(i).getContentLength();
				i++;
			}
			i = 0;
            JSONArray log_id = new JSONArray();
			while (i < args.length()) {
				JSONObject params = args.getJSONObject(i);
				String fileUrl = params.getString("url");
//                fileUrl = "http://192.168.82.75/cbs/tools/test_xsend";
                this.token = fileUrl.split("/")[fileUrl.split("/").length - 4];
				fileName = params.has("fileName") ? params
						.getString("fileName") : fileUrl.substring(fileUrl
						.lastIndexOf("/") + 1);

				dirName = params.has("dirName") ? params.getString("dirName")
						: Environment.getExternalStorageDirectory().getPath()
								+ "/download";

				Boolean overwrite = params.has("overwrite") ? params
						.getBoolean("overwrite") : false;

				File dir = new File(dirName);
				if (!dir.exists()) {
					// Log.d("PhoneGapLog", "directory " + dirName +
					// " created");
					dir.mkdirs();
				}

				File file = new File(dirName, fileName);

				if (!overwrite && file.exists()) {
					// Log.d("DownloaderPlugin", "File already exist");

					JSONObject obj = new JSONObject();
					obj.put("totalReaded", 0);
					obj.put("status", 1);
					obj.put("total", 0);
					obj.put("file", fileName);
					obj.put("dir", dirName);
					obj.put("progress", 100);
					obj.put("transID", trans_id);
					obj.put("MD5", MD5);
					return new PluginResult(PluginResult.Status.OK, obj);
				}
				JSONObject tmp = new JSONObject();
				tmp.put("file", dirName + "/" + fileName);
                Log.d("Content Service: ",ucon.get(i).getHeaderField("Content-Disposition"));
                Log.d("MD5 Content: ", ucon.get(i).getHeaderField("Content-Disposition")
                        .split("=")[1].split("\\.")[0].toUpperCase());
				tmp.put("md5",
						ucon.get(i).getHeaderField("Content-Disposition")
								.split("=")[1].split("\\.")[0].toUpperCase());
                if(ucon.get(i).getHeaderField("Content-LogID") != null){
                    log_id.put(ucon.get(i).getHeaderField("Content-LogID"));
                }
                boolean checkFailer = false;

				// Log.d("PhoneGapLog", "Download start");
				InputStream is = ucon.get(i).getInputStream();
				byte[] buffer = new byte[1024];
                int readedTmp = 0;
				FileOutputStream fos = new FileOutputStream(file);
				while (true) {
					if ((Boolean) this.listTransID.get(trans_id).get("stop")) {
						JSONObject obj = new JSONObject();
						obj.put("totalReaded", totalReaded);
						obj.put("status", 0);
						obj.put("total", sumSize);
						obj.put("file", fileName);
						obj.put("dir", dirName);
						obj.put("progress", progress);
						obj.put("transID", trans_id);
						obj.put("MD5", MD5);
                        obj.put("logID", log_id);
						return new PluginResult(PluginResult.Status.ERROR, "STOP");
					}
					if ((Boolean) this.listTransID.get(trans_id).get("pause")) {
						continue;
					}
                    try{
                        if ((readed = is.read(buffer)) <= 0)
                            break;
                    }
                    catch (Exception streamError){
                        Log.d("Downloading Read Error: ",fileName);
                        int count = 0;
                        boolean tryGet = false;
                        while(true){
                            if(count>3||tryGet) break;
//                            if(tryGet) break;
                            count++;
                            try{
                                String tmpUrl = fileUrl;
                                String oldToken = tmpUrl.split("/")[tmpUrl.split("/").length - 4];
                                fileUrl = tmpUrl.replace(oldToken, this.token);
//                                fileUrl = "http://192.168.82.75/cbs/tools/test_xsend";
                                URL urlTmp = new URL(fileUrl);
                                HttpURLConnection uconTmp = (HttpURLConnection) urlTmp.openConnection();
                                uconTmp.setRequestMethod("POST");
                                Log.d("Total download", String.valueOf(readedTmp));
                                uconTmp.setRequestProperty("Range", "bytes=" + String.valueOf(readedTmp) + "-");
                                uconTmp.connect();
                                is = uconTmp.getInputStream();
//                                is = skipStream(uconTmp.getInputStream(),(long) readedTmp);
                                tryGet = true;
                            }
                            catch (FileNotFoundException e){
                                tryGet = false;
                                Log.d("Downloading Connect Error 404: ", "URL: "+fileUrl+" / "+e.getMessage());
                                if(count==3){
                                    this.pause(trans_id);
                                    PluginResult res = new PluginResult(PluginResult.Status.ERROR, "D404");
                                    res.setKeepCallback(true);
                                    error(res, callbackId);
                                }
                            } catch (IOException e) {
                                tryGet = false;
                                Log.d("Downloading Connect Error IO: ", "URL: "+fileUrl+" / "+e.getMessage());
                                if(count==3){
                                    this.pause(trans_id);
                                    PluginResult res = new PluginResult(PluginResult.Status.ERROR, "D404");
                                    res.setKeepCallback(true);
                                    error(res, callbackId);
                                }
                            } catch (Exception e){
                                tryGet = false;
                                Log.d("Downloading Connect Error: ", "URL: "+fileUrl+" / "+e.getMessage());
                                if(count==3){
                                    this.pause(trans_id);
                                    PluginResult res = new PluginResult(PluginResult.Status.ERROR, "D404");
                                    res.setKeepCallback(true);
                                    error(res, callbackId);
                                }
                            }

                        }
//                        checkFailer = true;
                        continue;
                    }
                    Log.d("Downloading: ",fileName);
					fos.write(buffer, 0, readed);
					totalReaded += readed;
                    readedTmp += readed;

					int newProgress = (int) (((float) totalReaded * 100) / (float) sumSize);
					if (newProgress != progress) {
						progress = informProgress(sumSize, totalReaded,
								newProgress, dirName, fileName, callbackId,
								trans_id, MD5, log_id);
					}
				}

                if (checkFailer)
                    tmp.put("checkMD5", false);
                else  tmp.put("checkMD5", true);
                MD5.put(tmp);

				fos.close();
				i++;
			}

			// Log.d("PhoneGapLog", "Download finished");

			JSONObject obj = new JSONObject();
			obj.put("totalReaded", totalReaded);
			obj.put("status", 1);
			obj.put("total", sumSize);
			obj.put("file", fileName);
			obj.put("dir", dirName);
			obj.put("progress", progress);
			obj.put("transID", trans_id);
			obj.put("MD5", MD5);
            obj.put("logID", log_id);
            Log.d("NGOC TRINH", obj.toString());
			return new PluginResult(PluginResult.Status.OK, obj);

		} catch (FileNotFoundException e) {
			// Log.d("PhoneGapLog", "File Not Found: " + e);
			return new PluginResult(PluginResult.Status.ERROR, 404);
		} catch (IOException e) {
			return new PluginResult(PluginResult.Status.ERROR, e.getMessage());
		}

	}

	private int informProgress(int sumSize, int totalReaded, int progress,
			String dirName, String fileName, String callbackId, int trans_id,
			JSONArray MD5, JSONArray log_id) throws InterruptedException, JSONException {

		JSONObject obj = new JSONObject();
		obj.put("totalReaded", totalReaded);
		obj.put("status", 0);
		obj.put("total", sumSize);
		obj.put("file", fileName);
		obj.put("dir", dirName);
		obj.put("progress", progress);
		obj.put("transID", trans_id);
		obj.put("MD5", MD5);
        obj.put("logID", log_id);

		PluginResult res = new PluginResult(PluginResult.Status.OK, obj);
		res.setKeepCallback(true);
		success(res, callbackId);
		// Give a chance for the progress to be sent to javascript
		// Thread.sleep(10);

		return progress;
	}
}