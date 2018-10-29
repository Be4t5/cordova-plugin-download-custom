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
import java.util.Locale;
import java.util.Map;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
//import org.jetbrains.annotations.Nullable;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import com.liulishuo.okdownload.DownloadTask;
import com.liulishuo.okdownload.SpeedCalculator;
import com.liulishuo.okdownload.StatusUtil;
import com.liulishuo.okdownload.core.Util;
import com.liulishuo.okdownload.core.breakpoint.BlockInfo;
import com.liulishuo.okdownload.core.breakpoint.BreakpointInfo;
import com.liulishuo.okdownload.core.cause.EndCause;
import com.liulishuo.okdownload.core.dispatcher.DownloadDispatcher;
import com.liulishuo.okdownload.core.listener.DownloadListener4WithSpeed;
import com.liulishuo.okdownload.core.listener.assist.Listener4SpeedAssistExtend;
import com.liulishuo.okdownload.*;
import android.support.v4.app.NotificationCompat;
import android.app.Notification;
import android.app.NotificationManager;
import android.R;
import com.thin.downloadmanager.*;


public class Download extends CordovaPlugin {

	CallbackContext callbackContext1;
	private NotificationCompat.Builder builder;
	private NotificationManager manager;
	Context context;
	DownloadTask task;
	private NotificationCompat.Action action;
	ButtonReceiver buttonReceiver;
	int downloadId;
	ThinDownloadManager downloadManager;

    @Override
    public boolean execute(String action, JSONArray data, CallbackContext callbackContext) throws JSONException {
		callbackContext1 = callbackContext;
		
        if (action.equals("download")) {

            String url = data.getString(0);
            String path = data.getString(1);
            String fileName = data.getString(2);
            String title = data.getString(3);
			final File parentFile = new File(path);


			PluginResult pluginResult = new  PluginResult(PluginResult.Status.NO_RESULT);
			pluginResult.setKeepCallback(true);
			callbackContext.sendPluginResult(pluginResult);

			Uri downloadUri = Uri.parse(url);
			Uri destinationUri = Uri.parse(path+"/" +fileName);
			DownloadRequest downloadRequest = new DownloadRequest(downloadUri)
					.setRetryPolicy(new DefaultRetryPolicy())
					.setDestinationURI(destinationUri).setPriority(DownloadRequest.Priority.HIGH)
					.setDownloadListener(new DownloadStatusListener() {
						@Override
						public void onDownloadComplete(int id) {
							callbackContext1.success("ok");
							builder.setContentText("Download completato");

							builder.setSmallIcon(R.drawable.stat_sys_download_done);
							builder.setProgress(0, 0, false);

							manager.notify(id, builder.build());
						}

						@Override
						public void onDownloadFailed(int id, int errorCode, String errorMessage) {
							callbackContext1.success(errorMessage);
							builder.setContentText("Errore");

							builder.setSmallIcon(R.drawable.stat_sys_download_done);
							builder.setProgress(0, 0, false);

							manager.notify(id, builder.build());
						}

						@Override
						public void onProgress(int id, long totalBytes, long downlaodedBytes, int progress) {
							PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, progress);
							pluginResult.setKeepCallback(true); // keep callback
							callbackContext1.sendPluginResult(pluginResult);

							String downloaded = humanReadableBytes(downlaodedBytes, true);
							String total = humanReadableBytes(totalBytes, true);

							builder.setContentText(downloaded + "/" + total +"(" + progress + "%)");
							builder.setProgress((int)totalBytes, (int) downlaodedBytes, false);
							manager.notify(downloadId, builder.build());
						}
					});
			downloadManager = new ThinDownloadManager();
			downloadId = downloadManager.add(downloadRequest);
			initNotification(title);

			builder.setTicker("taskStart");
			builder.setOngoing(true);
			builder.setAutoCancel(false);
			builder.setContentText("Avvio download in corso...");
			builder.setProgress(0, 0, true);
			manager.notify(downloadId, builder.build());

			/*
			task = new DownloadTask.Builder(url, parentFile)
                .setFilename(fileName)
                // the minimal interval millisecond for callback progress
                .setMinIntervalMillisCallbackProcess(80)
                // ignore the same task has already completed in the past.
                .setPassIfAlreadyCompleted(false)
					.setAutoCallbackToUIThread(false)

					.setConnectionCount(1)

                .build();

			DownloadDispatcher.setMaxParallelRunningCount(1);

			PluginResult pluginResult = new  PluginResult(PluginResult.Status.NO_RESULT);
			pluginResult.setKeepCallback(true);
			callbackContext.sendPluginResult(pluginResult);

			initNotification(title);

			task.enqueue(new DownloadListener4WithSpeed() {
				private long totalLength;
				private String readableTotalLength;

				@Override public void taskStart(@NonNull DownloadTask task) {
					builder.setTicker("taskStart");
					builder.setOngoing(true);
					builder.setAutoCancel(false);
					builder.setContentText("Avvio download in corso...");
					builder.setProgress(0, 0, true);
					manager.notify(task.getId(), builder.build());
				}

				@Override
				public void infoReady(@NonNull DownloadTask task, @NonNull BreakpointInfo info,
									  boolean fromBreakpoint,
									  @NonNull Listener4SpeedAssistExtend.Listener4SpeedModel model) {
					totalLength = info.getTotalLength();
					readableTotalLength = Util.humanReadableBytes(totalLength, true);


					if (fromBreakpoint) {
						builder.setTicker("fromBreakpoint");
					} else {
						builder.setTicker("fromBeginning");
					}

					//builder.setContentText("This task is download fromBreakpoint[" + fromBreakpoint + "]");
					builder.setProgress((int) info.getTotalLength(), (int) info.getTotalOffset(), true);
					manager.notify(task.getId(), builder.build());

					totalLength = (int) info.getTotalLength();
				}

				@Override public void connectStart(@NonNull DownloadTask task, int blockIndex,
												   @NonNull Map<String, List<String>> requestHeaders) {
					builder.setTicker("connectStart");
					//builder.setContentText("The connect of " + blockIndex + " block for this task is connecting");
					builder.setProgress(0, 0, true);
					manager.notify(task.getId(), builder.build());
				}

				@Override
				public void connectEnd(@NonNull DownloadTask task, int blockIndex, int responseCode,
									   @NonNull Map<String, List<String>> responseHeaders) {
					builder.setTicker("connectStart");
					//builder.setContentText("The connect of " + blockIndex + " block for this task is connected");
					builder.setProgress(0, 0, true);
					manager.notify(task.getId(), builder.build());
				}

				@Override
				public void progressBlock(@NonNull DownloadTask task, int blockIndex,
										  long currentBlockOffset,
										  @NonNull SpeedCalculator blockSpeed) {
				}

				@Override public void progress(@NonNull DownloadTask task, long currentOffset,




					final String readableOffset = Util.humanReadableBytes(currentOffset, true);
					final String progressStatus = readableOffset + "/" + readableTotalLength;
					builder.setContentText(progressStatus + "(" + taskSpeed.speed() + ")");
					builder.setProgress((int)totalLength, (int) currentOffset, false);
					manager.notify(task.getId(), builder.build());
				}

				@Override
				public void blockEnd(@NonNull DownloadTask task, int blockIndex, BlockInfo info,
									 @NonNull SpeedCalculator blockSpeed) {
				}

				@Override public void taskEnd(@NonNull DownloadTask task, @NonNull EndCause cause,
											  Exception realCause,
											  @NonNull SpeedCalculator taskSpeed) {
					final String statusWithSpeed = cause.toString() + " " + taskSpeed.averageSpeed();
					if(cause.toString() == "ERROR")
						callbackContext1.success("ERROR " + realCause.toString());
					else
						callbackContext1.success(statusWithSpeed);
					builder.setOngoing(false);
					builder.setAutoCancel(true);

					OkDownload.with().breakpointStore().remove(task.getId());

					builder.setTicker("taskEnd " + cause);
					builder.setContentText("Download completato");
					if (cause == EndCause.COMPLETED) {
						builder.setSmallIcon(R.drawable.stat_sys_download_done);
						builder.setProgress(0, 0, false);
					}
					manager.notify(task.getId(), builder.build());
				}
			});

*/
            return true;

        } 
		else {
            
            return false;

        }
		
		
    }

	public static String humanReadableBytes(long bytes, boolean si) {
		int unit = si ? 1000 : 1024;
		if (bytes < unit) return bytes + " B";
		int exp = (int) (Math.log(bytes) / Math.log(unit));
		String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp - 1) + (si ? "" : "i");
		return String.format(Locale.ENGLISH, "%.1f %sB", bytes / Math.pow(unit, exp), pre);
	}

	public void initNotification(String title) {
		context = cordova.getActivity().getApplicationContext();
		manager = (NotificationManager)
				context.getSystemService(context.NOTIFICATION_SERVICE);

		builder = new NotificationCompat.Builder(context);


		// for cancel action on notification.
		IntentFilter filter = new IntentFilter(ButtonReceiver.ACTION);
		buttonReceiver = new ButtonReceiver(downloadManager,downloadId);
		cordova.getActivity().registerReceiver(buttonReceiver, filter);

		Intent intent=new Intent(ButtonReceiver.ACTION);
		PendingIntent pendingIntent= PendingIntent.getBroadcast(cordova.getActivity(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

		builder.setDefaults(Notification.DEFAULT_LIGHTS)
				.setOngoing(true)
				.setOnlyAlertOnce(true)
				.setPriority(NotificationCompat.PRIORITY_HIGH)
				.setContentTitle(title)
				.setContentText("")
				//.setSubText("prova prova prova")
				.addAction(R.drawable.ic_menu_delete, "Annulla", pendingIntent)

				.setSmallIcon(R.drawable.stat_sys_download);

		if (action != null) {
			builder.addAction(action);
		}
	}
	public static class ButtonReceiver extends BroadcastReceiver {

		static final String ACTION = "cancelOkdownload";

		private ThinDownloadManager downloadManager;
		int downloadId;

		ButtonReceiver(@NonNull ThinDownloadManager downloadManager, int downloadId) {

			this.downloadManager = downloadManager;
			this.downloadId = downloadId;
		}
		@Override
		public void onReceive(Context context, Intent intent) {

			//int notificationId = intent.getIntExtra("notificationId", 0);

			downloadManager.cancel(downloadId);
			// if you want cancel notification
			NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
			manager.cancel(downloadId);
		}
	}
}
