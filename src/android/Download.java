package com.download;

import org.apache.cordova.*;
import org.json.JSONArray;
import org.json.JSONException;

import com.thin.downloadmanager.*;


public class Download extends CordovaPlugin {
		
    @Override
    public boolean execute(String action, JSONArray data, CallbackContext callbackContext) throws JSONException {

        if (action.equals("download")) {

            String url = data.getString(0);
            String path = data.getString(1);
            String fileName = data.getString(2);
            String title = data.getString(3);
			
			Uri downloadUri = Uri.parse(url);
		   Uri destinationUri = Uri.parse(path+fileName);
		   DownloadRequest downloadRequest = new DownloadRequest(downloadUri)
				   .setRetryPolicy(new DefaultRetryPolicy())
				   .setDestinationURI(destinationUri).setPriority(DownloadRequest.Priority.HIGH)
				   .setDownloadListener(new DownloadStatusListener() {
					   @Override
					   public void onDownloadComplete(int id) {
							callbackContext.success("ok");
					   }

					   @Override
					   public void onDownloadFailed(int id, int errorCode, String errorMessage) {
							callbackContext.success("ok");
					   }

					   @Override
					   public void onProgress(int id, long totalBytes, long downlaodedBytes, int progress) {
							callbackContext.success(progress);
					   }
				   });
			
			int downloadId = downloadManager.add(downloadRequest);

            return true;

        } 
		else {
            
            return false;

        }
		
		
    }
}
