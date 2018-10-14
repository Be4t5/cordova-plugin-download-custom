package com.download;

import org.apache.cordova.*;
import org.json.JSONArray;
import org.json.JSONException;

import com.zxt.download2.*;


public class Download extends CordovaPlugin {
		
    @Override
    public boolean execute(String action, JSONArray data, CallbackContext callbackContext) throws JSONException {

        if (action.equals("download")) {

            String url = data.getString(0);
            String path = data.getString(1);
            String fileName = data.getString(2);
            String title = data.getString(3);
			
			DownloadTask downloadTask4 = new DownloadTask(url, path, fileName, title, null);
            //downloadTask4.setThumbnail("file:///sdcard/hobbit.jpg"); //use image file uri
            DownloadTaskManager.getInstance(cordova.getActivity()).registerListener(downloadTask4,
                    new DownloadNotificationListener(cordova.getActivity().getApplicationContext(), downloadTask4));
            DownloadTaskManager.getInstance(cordova.getActivity()).startDownload(downloadTask4);
			
            callbackContext.success("ok");

            return true;

        } 
		else {
            
            return false;

        }
		
		
    }
}
