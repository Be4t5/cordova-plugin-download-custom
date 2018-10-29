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

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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


public class Download extends CordovaPlugin {

	CallbackContext callbackContext1;
	private NotificationCompat.Builder builder;
	private NotificationManager manager;
	Context context;
	DownloadTask task;
	private NotificationCompat.Action action;
	ButtonReceiver buttonReceiver;

    @Override
    public boolean execute(String action, JSONArray data, CallbackContext callbackContext) throws JSONException {
		callbackContext1 = callbackContext;
		
        if (action.equals("download")) {

            String url = data.getString(0);
            String path = data.getString(1);
            String fileName = data.getString(2);
            String title = data.getString(3);
			final File parentFile = new File(path);
			task = new DownloadTask.Builder(url, parentFile)
                .setFilename(fileName)
                // the minimal interval millisecond for callback progress
                .setMinIntervalMillisCallbackProcess(500)
                // ignore the same task has already completed in the past.
                .setPassIfAlreadyCompleted(false)
					.setAutoCallbackToUIThread(false)

                .build();


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
											   @NonNull SpeedCalculator taskSpeed) {
					/*

					final String progressStatus = readableOffset + "/" + readableTotalLength;
					final String speed = taskSpeed.speed();
					final float percent = ((float) currentOffset / totalLength) * 100;
					final String progressStatusWithSpeed = "PROGRESS|"+progressStatus + "(" + speed + ")" + "|" + percent;
					PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, progressStatusWithSpeed);
					pluginResult.setKeepCallback(true); // keep callback
					callbackContext1.sendPluginResult(pluginResult);

					Log.d("OKDOWNLOAD 4", "In progress");
					Log.d("OKDOWNLOAD 4", taskSpeed.speed());

					*/

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


            return true;

        } 
		else {
            
            return false;

        }
		
		
    }

	@Override public void onDestroy() {
		super.onDestroy();
		cordova.getActivity().unregisterReceiver(buttonReceiver);
	}

	public void initNotification(String title) {
		context = cordova.getActivity().getApplicationContext();
		manager = (NotificationManager)
				context.getSystemService(context.NOTIFICATION_SERVICE);

		builder = new NotificationCompat.Builder(context);


		// for cancel action on notification.
		IntentFilter filter = new IntentFilter(ButtonReceiver.ACTION);
		buttonReceiver = new ButtonReceiver(task);
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

		private DownloadTask task;

		ButtonReceiver(@NonNull DownloadTask task) {
			this.task = task;
		}
		@Override
		public void onReceive(Context context, Intent intent) {

			//int notificationId = intent.getIntExtra("notificationId", 0);
			task.cancel();
			// if you want cancel notification
			NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
			manager.cancel(task.getId());

			OkDownload.with().breakpointStore().remove(task.getId());
		}
	}
}
