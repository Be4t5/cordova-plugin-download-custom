package com.download;

import org.apache.cordova.*;
import org.json.JSONArray;
import org.json.JSONException;
import java.io.File;
import java.util.Locale;

import android.app.NotificationChannel;
import android.app.PendingIntent;
import android.app.UiModeManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.Uri;

import androidx.annotation.NonNull;


import androidx.core.app.NotificationCompat;
import android.app.Notification;
import android.app.NotificationManager;
import android.R;
import android.os.Build;

import com.thin.downloadmanager.*;

import static android.content.Context.UI_MODE_SERVICE;


public class Download extends CordovaPlugin {

	CallbackContext callbackContext1;
	private NotificationCompat.Builder builder;
	private NotificationManager manager;
	Context context;
	private NotificationCompat.Action action;
	ButtonReceiver buttonReceiver;
	int downloadId;
	ThinDownloadManager downloadManager;
	long taskStart;
	long lastUpdate;

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
							builder.setStyle(new NotificationCompat.BigTextStyle().bigText(""));
							builder.setSmallIcon(R.drawable.stat_sys_download_done);
							builder.setProgress(0, 0, false);
							builder.mActions.clear();
							builder.setAutoCancel(true);
							builder.setOngoing(false);
							manager.notify(id, builder.build());


						}

						@Override
						public void onDownloadFailed(int id, int errorCode, String errorMessage) {
							callbackContext1.success(errorMessage);
							builder.setContentText("Errore");
							builder.setAutoCancel(true);
							builder.setOngoing(false);
							builder.setSmallIcon(R.drawable.stat_sys_download_done);
							builder.setProgress(0, 0, false);
							builder.setStyle(new NotificationCompat.BigTextStyle().bigText(""));
							manager.notify(id, builder.build());
						}

						@Override
						public void onProgress(int id, long totalBytes, long downlaodedBytes, int progress) {

							if(System.currentTimeMillis() - lastUpdate > 1000){
								lastUpdate = System.currentTimeMillis();
								PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, progress);
								pluginResult.setKeepCallback(true); // keep callback
								callbackContext1.sendPluginResult(pluginResult);

								String downloaded = humanReadableBytes(downlaodedBytes, true);
								String total = humanReadableBytes(totalBytes, true);

								builder.setContentText("Download in corso...");
								builder.setStyle(new NotificationCompat.BigTextStyle().bigText(downloaded + "/" + total +" (" + progress + "%)"));
								builder.setProgress((int)totalBytes, (int) downlaodedBytes, false);
								manager.notify(downloadId, builder.build());
							}

						}
					});
			downloadManager = new ThinDownloadManager();
			downloadId = downloadManager.add(downloadRequest);
			taskStart = System.currentTimeMillis();
			lastUpdate = System.currentTimeMillis();
			initNotification(title);

			builder.setTicker("taskStart");
			builder.setOngoing(true);
			builder.setAutoCancel(false);
			builder.setContentText("Avvio download in corso...");
			builder.setProgress(0, 0, true);
			manager.notify(downloadId, builder.build());


			return true;

		}else if(action.equals("downloadWithADM")){
			String url = data.getString(0);
			String path = data.getString(1);
			String fileName = data.getString(2);
			String title = data.getString(3);
			Intent intent = new Intent ("android.intent.action.MAIN");
			intent.setClassName ("com.dv.adm", "com.dv.adm.AEditor");// for the PRO version of “com.dv.adm.pay”, for DVGet “com.dv.get” and “com.dv.get.pro”
			intent.putExtra ("com.dv.get.ACTION_LIST_ADD", url + "<info>" + fileName);
			intent.putExtra ("com.dv.get.ACTION_LIST_PATH", path);

			try {
				cordova.getActivity().startActivity(intent);
				callbackContext1.success("ok");
			} catch (Exception e) {
				callbackContext1.error("not found");
			}

			return true;
		} else if(action.equals("downloadWithADMPro")){

			String url = data.getString(0);
			String path = data.getString(1);
			String fileName = data.getString(2);
			String title = data.getString(3);
			Intent intent = new Intent ("android.intent.action.MAIN");
			intent.setClassName ("com.dv.adm.pay", "com.dv.adm.pay.AEditor");// for the PRO version of “com.dv.adm.pay”, for DVGet “com.dv.get” and “com.dv.get.pro”
			intent.putExtra ("com.dv.get.ACTION_LIST_ADD", url + "<info>" + fileName);
			intent.putExtra ("com.dv.get.ACTION_LIST_PATH", path);
			try {
				cordova.getActivity().startActivity(intent);
				callbackContext1.success("ok");
			} catch (Exception e) {
				callbackContext1.error("not found");
			}


			return true;
		}  else if(action.equals("downloadWithLJ")){

      String url = data.getString(0);
      String path = data.getString(1);
      String fileName = data.getString(2);
      String title = data.getString(3);
      PackageManager manager = cordova.getActivity().getPackageManager();
      Intent downloadIntent = manager.getLaunchIntentForPackage("com.leavjenn.m3u8downloader"); // Lj video downloader package name
      if (downloadIntent == null) {
        // Lj video downloader is not installed, maybe notify the user?
        callbackContext1.error("not found");
      }
      downloadIntent.setDataAndType(Uri.parse(url), "video/hls"); // the video url

      try {
        cordova.getActivity().startActivity(downloadIntent);
        callbackContext1.success("ok");
      } catch (Exception e) {
        callbackContext1.error("not found");
      }


      return true;
    }else if (action.equals("isAndroidTv")){

        cordova.getActivity().runOnUiThread(new Runnable() {
          @Override
          public void run() {
            UiModeManager uiModeManager = (UiModeManager) cordova.getActivity().getApplicationContext().getSystemService(UI_MODE_SERVICE);
            if (uiModeManager.getCurrentModeType() == Configuration.UI_MODE_TYPE_TELEVISION) {
              callbackContext1.success("true");
            } else {
              callbackContext1.success("false");
            }
          }
        });
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

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      /* Create or update. */
      NotificationChannel channel = new NotificationChannel("veeziedownload",
        "Channel human readable title",
        NotificationManager.IMPORTANCE_DEFAULT);

      manager.createNotificationChannel(channel);
    }

    builder = new NotificationCompat.Builder(context, "veeziedownload");

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
      .setChannelId("veeziedownload")
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
