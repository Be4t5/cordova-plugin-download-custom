package com.download;

import org.apache.cordova.*;
import org.json.JSONArray;
import org.json.JSONException;
import java.io.File;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.MessageDigest;
import java.util.List;
import java.util.Map;
import android.support.annotation.NonNull;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import com.liulishuo.okdownload.DownloadTask;
import com.liulishuo.okdownload.SpeedCalculator;
import com.liulishuo.okdownload.StatusUtil;
import com.liulishuo.okdownload.core.Util;
import com.liulishuo.okdownload.core.breakpoint.BlockInfo;
import com.liulishuo.okdownload.core.breakpoint.BreakpointInfo;
import com.liulishuo.okdownload.core.cause.EndCause;
import com.liulishuo.okdownload.core.listener.DownloadListener4WithSpeed;
import com.liulishuo.okdownload.core.listener.assist.Listener4SpeedAssistExtend;
import com.liulishuo.okdownload.*;


public class Download extends CordovaPlugin {
	
	CallbackContext callbackContext1;
	
    @Override
    public boolean execute(String action, JSONArray data, CallbackContext callbackContext) throws JSONException {
	
		callbackContext1 = callbackContext;
		
        if (action.equals("download")) {

            String url = data.getString(0);
            String path = data.getString(1);
            String fileName = data.getString(2);
            String title = data.getString(3);
			
			final File parentFile = new File(path);
			DownloadTask task = new DownloadTask.Builder(url, parentFile)
                .setFilename(fileName)
                // the minimal interval millisecond for callback progress
                .setMinIntervalMillisCallbackProcess(16)
                // ignore the same task has already completed in the past.
                .setPassIfAlreadyCompleted(false)
                .build();
				
			
			task.enqueue(new DownloadListener4WithSpeed() {
				private long totalLength;
				private String readableTotalLength;

				

				@Override public void progress(@NonNull DownloadTask task, long currentOffset,
											   @NonNull SpeedCalculator taskSpeed) {
					//final String readableOffset = Util.humanReadableBytes(currentOffset, true);
					//final String progressStatus = readableOffset + "/" + readableTotalLength;
					//final String speed = taskSpeed.speed();
					//final String progressStatusWithSpeed = progressStatus + "(" + speed + ")";
					
					//statusTv.setText(progressStatusWithSpeed);
					//DemoUtil.calcProgressToView(progressBar, currentOffset, totalLength);
				}


			});
			
			/*
			ThinDownloadManager downloadManager = new ThinDownloadManager(4);
			
			Uri downloadUri = Uri.parse(url);
		   Uri destinationUri = Uri.parse(path+fileName);
		   DownloadRequest downloadRequest = new DownloadRequest(downloadUri)
				   .setRetryPolicy(new DefaultRetryPolicy())
				   .setDestinationURI(destinationUri).setPriority(DownloadRequest.Priority.HIGH)
				   .setDownloadListener(new DownloadStatusListener() {
					   @Override
					   public void onDownloadComplete(int id) {
							callbackContext1.success("ok");
					   }

					   @Override
					   public void onDownloadFailed(int id, int errorCode, String errorMessage) {
							callbackContext1.success("ok");
					   }

					   @Override
					   public void onProgress(int id, long totalBytes, long downlaodedBytes, int progress) {
							callbackContext1.success(progress);
					   }
				   });
			
			downloadManager.add(downloadRequest);
			
			*/

            return true;

        } 
		else {
            
            return false;

        }
		
		
    }
}
