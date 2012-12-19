package fm.moe.luhuan.service;

import java.io.BufferedInputStream;

import java.io.ByteArrayOutputStream;


import java.io.IOException;

import java.io.Serializable;


import fm.moe.luhuan.FileStorageHelper;

import fm.moe.luhuan.activities.MusicPlay;
import fm.moe.luhuan.beans.data.SimpleData;
import fm.moe.luhuan.http.CommonHttpHelper;

import android.R;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;

import android.graphics.Bitmap;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import android.net.wifi.WifiManager;

import android.os.IBinder;

import android.support.v4.app.NotificationCompat.Builder;

import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

public class DownloadService extends IntentService {
	public DownloadService(String name) {
		super(name);
		// TODO Auto-generated constructor stub
	}

	public DownloadService() {
		super("");
		// TODO Auto-generated constructor stub
	}

	private LocalBroadcastManager broadcastManager;
	private Intent broadcast = new Intent();
	
	private boolean onWifi = false;
	private boolean onConnect = true;
	private boolean onlyByWifi = true;
	private boolean shouldDownload = true;
	private CommonHttpHelper http = new CommonHttpHelper();
	private Builder notificationBuilder;
	private NotificationManager notificationManager;
	private PendingIntent pendingIntent;
	private FileStorageHelper fileHelper;

	public static final String PREF_KEY_DOWNLOAD_JUST_ON_WIFI = "just on wifi";

	public static final String EXTRA_SONG_ITEM = "a item";
	public static final String EXTRA_CONN_PROBLEM_INFO = "network problem info";

	public static final String ACTION_NET_CONN_PROBLEM = "network problem";

	public static final String ACTION_DOWNLOAD_STATE_CHANGE = "download state change";
	// 0 for start,1 for complete,-1 for err
	public static final String EXTRA_DOWNLOAD_STATE = "download state extra";

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		fileHelper = new FileStorageHelper(this);

		notificationBuilder = new Builder(this);
		broadcastManager = LocalBroadcastManager
				.getInstance(getApplicationContext());
		notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

		Intent resumePlayActivity = new Intent(this, MusicPlay.class);
		resumePlayActivity.setAction(MusicPlay.ACTION_RESUME);
		pendingIntent = PendingIntent.getActivity(this, 0, resumePlayActivity,
				PendingIntent.FLAG_UPDATE_CURRENT);
		notificationBuilder.setContentIntent(pendingIntent);

		
		SharedPreferences pref = getSharedPreferences("App_settings",
				MODE_PRIVATE);
		onlyByWifi = pref.getBoolean(PREF_KEY_DOWNLOAD_JUST_ON_WIFI, true);
		ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo netInfo = cm.getActiveNetworkInfo();
		if (!netInfo.isConnected()) {
			onConnect = false;
		} else if (!netInfo.isAvailable()) {
			onConnect = false;
		} else {
			onConnect = true;
		}
		WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		if (wifiManager.isWifiEnabled()) {
			onWifi = true;
		} else {
			onWifi = false;
		}

		checkShouldDownload();

		// accept the broadcast about the net connection,wifi and connectivity
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
		intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
		intentFilter.addAction(MusicPlay.ACTION_RESUME);
		registerReceiver(receiver, intentFilter);
	}

	/*
	 * if all the tasks have been done ,system will instantiate a new instance
	 * of the service when startService() is called
	 */
	@Override
	protected void onHandleIntent(Intent intent) {
		if (!shouldDownload) {

			broadcast.setAction(ACTION_NET_CONN_PROBLEM);
			broadcast.putExtra(EXTRA_CONN_PROBLEM_INFO, getNetProblemText());
			broadcastManager.sendBroadcast(broadcast);
			return;
		} else {
			broadcast.setAction(ACTION_DOWNLOAD_STATE_CHANGE);
			broadcast.putExtra(EXTRA_DOWNLOAD_STATE, 0);
			broadcastManager.sendBroadcast(broadcast);
			notificationBuilder.setSmallIcon(R.drawable.stat_sys_download);
			notificationBuilder.setContentTitle("开始下载....");
			notificationBuilder.setContentText("某某歌");
			Notification notification = notificationBuilder.build();
			notificationManager.notify(0, notification);

		}

		SimpleData item = (SimpleData) intent
				.getSerializableExtra(EXTRA_SONG_ITEM);
		int nStart = fileHelper.getSongFileLength(item);
		int nRead = 0;
		int fLength = 0;

		

		fLength = http.getFileLength(item.getMp3Url());

		byte[] data = new byte[8 * 1024];

		ByteArrayOutputStream bao = new ByteArrayOutputStream();
		while (nStart < fLength) {
			if (!shouldDownload) {
				broadcast.setAction(ACTION_NET_CONN_PROBLEM);
				broadcast.putExtra(EXTRA_CONN_PROBLEM_INFO, getNetProblemText()
						+ ",下载已暂停");
				return;
			}
			BufferedInputStream bis = new BufferedInputStream(
					http.downloadRanged(item.getMp3Url(), nStart, fLength));
			try {
				while ((nRead = bis.read(data, 0, 1024 * 8)) > 0) {

					//bos.write(data, 0, nRead);
					bao.write(data, 0, nRead);
					nStart += nRead;
					
				}
			} catch (Exception e) {
				Log.e("download service", "downloading retry");
				continue;
			}

		}
		fileHelper.writeDataToSong(item,bao.toByteArray(),nRead);
		try {
			bao.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if(fileHelper.getItemCoverBitmap(item)==null){
			Bitmap bm = http.getBitmap(item.getAlbumnCoverUrl());
			fileHelper.saveCover(item, bm);
		}
		broadcast.setAction(ACTION_DOWNLOAD_STATE_CHANGE);
		broadcast.putExtra(EXTRA_DOWNLOAD_STATE, 1);
		notificationBuilder.setSmallIcon(R.drawable.stat_sys_download_done);
		notificationBuilder.setContentTitle("下载完毕！");
		notificationBuilder.setContentText("某某歌");
		Notification notification = notificationBuilder.build();
		notificationManager.notify(0, notification);



		fileHelper.insertItemIntoDb(item);
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		unregisterReceiver(receiver);
	}

	private BroadcastReceiver receiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			Log.e("action", action);
			if (action.equals(WifiManager.WIFI_STATE_CHANGED_ACTION)) {
				int wifiState = intent.getIntExtra(
						WifiManager.EXTRA_WIFI_STATE, 0);
				if (wifiState == WifiManager.WIFI_STATE_ENABLED) {
					onWifi = true;
				} else {
					onWifi = false;
				}
			}

			if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
				if (intent.getBooleanExtra(
						ConnectivityManager.EXTRA_NO_CONNECTIVITY, false)) {
					onConnect = false;
				} else {
					onConnect = true;
				}
			}

			checkShouldDownload();
			if (!shouldDownload) {
				broadcast.setAction(ACTION_NET_CONN_PROBLEM);
				intent.putExtra(EXTRA_CONN_PROBLEM_INFO, getNetProblemText()
						+ ",任务已暂停");
				broadcastManager.sendBroadcast(broadcast);
			}

			if (action.equals(MusicPlay.ACTION_RESUME)) {
				startActivity(new Intent(DownloadService.this, MusicPlay.class));
			}
		}

	};

	private void checkShouldDownload() {
		// TODO Auto-generated method stub
		if (onConnect) {
			if (onWifi) {
				shouldDownload = true;
			} else if (!onlyByWifi) {
				shouldDownload = true;
			} else {
				shouldDownload = false;
			}
		} else {
			shouldDownload = false;
		}
	}

	private String getNetProblemText() {
		String errInfo = "";
		if (!onConnect) {
			errInfo = "没有可用的网络连接";

		} else if (onlyByWifi && !onWifi) {
			errInfo = "当前为非wifi网络";
		}
		return errInfo;
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	public static class DownloadTaskResult implements Serializable {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		public String url;
		public int nRead;
		public int fullLength;
	}
}
