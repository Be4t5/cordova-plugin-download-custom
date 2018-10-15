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
			
			FetchConfiguration fetchConfiguration = new FetchConfiguration.Builder(this)
                .setDownloadConcurrentLimit(3)
                .build();

			fetch = Fetch.Impl.getInstance(fetchConfiguration);

			String file = path + fileName;
			
			final Request request = new Request(url, file);
			request.setPriority(Priority.HIGH);
			request.setNetworkType(NetworkType.ALL);
			
			fetch.enqueue(request, updatedRequest -> {
				//Request was successfully enqueued for download.
				callbackContext.success("ok");
			}, error -> {
				//An error occurred enqueuing the request.
				callbackContext.success("no");
			});
			
            callbackContext.success("ok");

            return true;

        } 
		else {
            
            return false;

        }
		
		
    }
}
