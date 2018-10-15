package com.download;

import org.apache.cordova.*;
import org.json.JSONArray;
import org.json.JSONException;

import com.mindorks.android.*;


public class Download extends CordovaPlugin {
		
    @Override
    public boolean execute(String action, JSONArray data, CallbackContext callbackContext) throws JSONException {

        if (action.equals("download")) {

            String url = data.getString(0);
            String path = data.getString(1);
            String fileName = data.getString(2);
            String title = data.getString(3);
			
			PRDownloader.initialize(this.cordova.getActivity().getApplicationContext());
			
			int downloadId = PRDownloader.download(url, path, fileName)
                        .build()
                        .setOnStartOrResumeListener(new OnStartOrResumeListener() {
                            @Override
                            public void onStartOrResume() {
                               
                            }
                        })
                        .setOnPauseListener(new OnPauseListener() {
                            @Override
                            public void onPause() {
                               
                            }
                        })
                        .setOnCancelListener(new OnCancelListener() {
                            @Override
                            public void onCancel() {
                                
                            }
                        })
                        .setOnProgressListener(new OnProgressListener() {
                            @Override
                            public void onProgress(Progress progress) {
                               
                            }
                        })
                        .start(new OnDownloadListener() {
                            @Override
                            public void onDownloadComplete() {
                               
                            }

                            @Override
                            public void onError(Error error) {
                               
                            }
                        });            
			
            callbackContext.success("ok");

            return true;

        } 
		else {
            
            return false;

        }
		
		
    }
}
