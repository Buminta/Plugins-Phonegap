package org.plugins;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import org.json.JSONArray;
import org.json.JSONException;

import org.apache.cordova.api.Plugin;
import org.apache.cordova.api.PluginResult;

import android.util.Log;


public class ExtractZipFile extends Plugin {

	private static final String TAG = "ExtractZipFile";

	
	/**
	 * Unzip a zip file.  Will overwrite existing files.
	 * 
	 * @param zipFile Full path of the zip file you'd like to unzip.
	 * @param location Full path of the directory you'd like to unzip to (will be created if it doesn't exist).
	 * @throws java.io.IOException
	 */
	public static void unzip(String zipFile, String location) throws IOException {
	    int size, BUFFER_SIZE = 1024;
	    byte[] buffer = new byte[BUFFER_SIZE];

	    try {
	        if ( !location.endsWith("/") ) {
	            location += "/";
	        }
	        File f = new File(location);
	        if(!f.isDirectory()) {
	            f.mkdirs();
	        }
	        ZipInputStream zin = new ZipInputStream(new BufferedInputStream(new FileInputStream(zipFile), BUFFER_SIZE));
	        try {
	            ZipEntry ze = null;
	            while ((ze = zin.getNextEntry()) != null) {
	                String path = location + ze.getName();
	                File unzipFile = new File(path);

	                if (ze.isDirectory()) {
	                    if(!unzipFile.isDirectory()) {
	                        unzipFile.mkdirs();
	                    }
	                } else {
	                    // check for and create parent directories if they don't exist
	                    File parentDir = unzipFile.getParentFile();
	                    if ( null != parentDir ) {
	                        if ( !parentDir.isDirectory() ) {
	                            parentDir.mkdirs();
	                        }
	                    }

	                    // unzip the file
	                    FileOutputStream out = new FileOutputStream(unzipFile, false);
	                    BufferedOutputStream fout = new BufferedOutputStream(out, BUFFER_SIZE);
	                    try {
	                        while ( (size = zin.read(buffer, 0, BUFFER_SIZE)) != -1 ) {
	                            fout.write(buffer, 0, size);
	                        }

	                        zin.closeEntry();
	                    }
	                    finally {
	                        fout.flush();
	                        fout.close();
	                    }
	                }
	            }
	        }
	        finally {
	            zin.close();
	        }
	    }
	    catch (Exception e) {
	        Log.e(TAG, "Unzip exception", e);
	    }
	}
	
	@Override
	public PluginResult execute(String arg0, JSONArray args, String arg2) {
		PluginResult.Status status = PluginResult.Status.OK;
        JSONArray result = new JSONArray();
        try {
			String filename = args.getString(0);
            Log.d(TAG, "Extract:"+filename);
			File file = new File(filename);
			String[] dirToSplit=filename.split(File.separator);
			String dirToInsert="";
			for(int i=0;i<dirToSplit.length-1;i++)
			{
				dirToInsert+=dirToSplit[i]+File.separator;
			}
			Log.d(TAG, "vuongtm dirToInsert " +dirToInsert);
			BufferedOutputStream dest = null;
			BufferedInputStream is = null;
			ZipEntry entry;
			ZipFile zipfile;
			try {
				zipfile = new ZipFile(file);
				
				
				Enumeration<? extends ZipEntry> e = zipfile.entries();
				
				while (e.hasMoreElements()) 
				  {
					  entry = (ZipEntry) e.nextElement();
					  is = new BufferedInputStream(zipfile.getInputStream(entry));
					  int count;
					  byte data[] = new byte[102222];
					  String fileName = dirToInsert + entry.getName();
					  File outFile = new File(fileName);
					  //vuongtm added
					  File pf = new File(outFile.getParent());
					  pf.mkdirs();
					  
					  Log.d(TAG,"vuongtm outFile " + fileName);
					  //end vuongtm added
					  if (entry.isDirectory()) 
					  {
						  outFile.mkdirs();
					  } 
					  else 
					  {
						  FileOutputStream fos = new FileOutputStream(outFile);
						  dest = new BufferedOutputStream(fos, 102222);
						  while ((count = is.read(data, 0, 102222)) != -1)
						  {
							  dest.write(data, 0, count);
						  }
						  dest.flush();
						  dest.close();
						  is.close();
					  }
				  }
			} catch (ZipException e1) {
				Log.e(TAG, "vuongtm 1 " + e1.getMessage());
				return new PluginResult(PluginResult.Status.MALFORMED_URL_EXCEPTION);
			} catch (IOException e1) {
				Log.e(TAG, "vuongtm 2 " + e1.getMessage());
				return new PluginResult(PluginResult.Status.IO_EXCEPTION);
			}

		} catch (JSONException e) {
			Log.e(TAG, "vuongtm 3 " + e.getMessage());
			return new PluginResult(PluginResult.Status.JSON_EXCEPTION);
		}
        return new PluginResult(status);
	}
}