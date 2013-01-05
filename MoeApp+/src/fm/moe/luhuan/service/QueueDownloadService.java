package fm.moe.luhuan.service;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.Executor;
import java.util.concurrent.FutureTask;

import fm.moe.luhuan.MusicPlay;
import fm.moe.luhuan.beans.data.SimpleData;
import fm.moe.luhuan.http.CommonHttpHelper;
import fm.moe.luhuan.utils.DataStorageHelper;

import android.R;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat.Builder;
import android.util.Log;

public class QueueDownloadService extends Service {
	private List<SimpleData> downloadList = new LinkedList<SimpleData>();
	private Executor executor = new Executor() {

		public void execute(Runnable command) {
			new Thread(command).start();

		}
	};
	private boolean hasStarted = false;
	private boolean onlyByWifi = true;
	private CommonHttpHelper http = new CommonHttpHelper();
	private Builder notificationBuilder;
	private NotificationManager notificationManager;
	// private PendingIntent pendingIntent;
	private DataStorageHelper fileHelper;
	
	private ConnectivityManager connectivityManager;
	public static final int NOTIFICATION_ID = 0;
	public static final String EXTRA_SONG_ITEM = "a item";
	public static final String EXTRA_SONG_ITEM_LIST = "a item list";
	public static final String EXTRA_CONN_PROBLEM_INFO = "network problem info";
	// 0 for downloading,1 for complete,-1 for err
	public static final String EXTRA_DOWNLOAD_STATE = "download state extra";

	public static final String ACTION_DOWNLOAD_STATE_CHANGE = "download state change";

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		fileHelper = new DataStorageHelper(this);
		notificationBuilder = new Builder(this);
		notificationBuilder.setAutoCancel(true);
		notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		
		// to be change
		onlyByWifi = getSharedPreferences("App_settings", MODE_PRIVATE)
				.getBoolean(
						getResources()
								.getString(
										fm.moe.luhuan.R.string.pref_key_download_only_on_wifi),
						false);

		connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (intent != null) {
			List<SimpleData> list = (List<SimpleData>) intent
					.getSerializableExtra(EXTRA_SONG_ITEM_LIST);
			SimpleData item = (SimpleData) intent
					.getSerializableExtra(EXTRA_SONG_ITEM);
			addTask(item);
			addTasks(list);
			startTasks();

			Intent resumePlayActivity = new Intent(this, MusicPlay.class);
			resumePlayActivity.putExtras(intent.getExtras());
			resumePlayActivity.setAction("resume");
			PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
					resumePlayActivity, PendingIntent.FLAG_UPDATE_CURRENT);
			notificationBuilder.setContentIntent(pendingIntent);
		}
		return START_NOT_STICKY;
	}

	private void addTask(SimpleData item) {

		downloadList.add(item);
		
		// task.
	};

	private void addTasks(List<SimpleData> l) {
		if(l!=null){
			downloadList.addAll(l);
		}
		
	}

	private void startTasks() {
		if(hasStarted)return;
		
		hasStarted = true;
		SimpleData item = downloadList.get(0);
		DownloadTask task = new DownloadTask(new DownloadRunnable(item));
		
		executor.execute(task);
	}

//	private void initNotification(Bundle bundle) {

//	}

	private void sendBroadcast(String action, Bundle extras) {
		Intent intent = new Intent();
		intent.setAction(action);
		intent.putExtras(extras);
		sendBroadcast(intent);
	}

	private void sendNotification(int drawableId, String tickerText,
			String title, String content) {
		notificationBuilder.setSmallIcon(drawableId);
		notificationBuilder.setTicker("萌否音乐:" + tickerText);
		notificationBuilder.setContentTitle("萌否音乐:" + title);
		notificationBuilder.setContentText(content);
		notificationManager
				.notify(NOTIFICATION_ID, notificationBuilder.build());
	}
	private void refreshProgress(int max,int progress) {
		notificationBuilder.setProgress(max, progress, false);
		notificationManager
				.notify(NOTIFICATION_ID, notificationBuilder.build());
	}
	private int checkConectivity() {
		NetworkInfo ni = connectivityManager.getActiveNetworkInfo();
		if (ni == null || !ni.isAvailable() || !ni.isConnected()) {
			return -1;
		}
		return ni.getType();
	}

	private String getConnProblemText() {
		String info = null;
		int connState = checkConectivity();
		if (connState == -1) {
			info = "当前无可用网络连接";
		} else if (connState == ConnectivityManager.TYPE_MOBILE && onlyByWifi) {
			info = "当前为非wifi网络";
		}
		return info;
	};

	private class DownloadTask extends FutureTask<Void> {
		

		public DownloadTask(Runnable runnable) {

			super(runnable, null);
			
		}
	}

	private class DownloadRunnable implements Runnable {
		private SimpleData item;
		public DownloadRunnable(SimpleData data){
			item = data;
		}
		public void run() {
			String connInfo = getConnProblemText();
			if (connInfo ==null ) {
				Bundle bundle = new Bundle();
				bundle.putInt(EXTRA_DOWNLOAD_STATE, 0);
				sendBroadcast(ACTION_DOWNLOAD_STATE_CHANGE, bundle);
				sendNotification(R.drawable.stat_sys_download,"正在下载", "正在下载...",
						item.getTitle() + "-" + item.getArtist());
				
			} else {
				Bundle bundle = new Bundle();
				bundle.putString(EXTRA_CONN_PROBLEM_INFO, connInfo);
				bundle.putInt(EXTRA_DOWNLOAD_STATE, -1);
				sendBroadcast(ACTION_DOWNLOAD_STATE_CHANGE, bundle);
				sendNotification(R.drawable.stat_notify_error,"下载已暂停", "下载已暂停",
						getConnProblemText() + ",请检查你的网络");
				return;

			}

			int nStart = fileHelper.getSongFileLength(item);
			int nRead = 0;
			int fLength = 0;

			while (fLength < 10) {
				fLength = http.getFileLength(item.getMp3Url());
			}

			byte[] data = new byte[8 * 1024];

			ByteArrayOutputStream bao = new ByteArrayOutputStream();
			while (nStart < fLength) {
				String tempConnInfo = getConnProblemText();
				if (tempConnInfo!=null) {
					Bundle bundle = new Bundle();
					bundle.putInt(EXTRA_DOWNLOAD_STATE, -1);
					bundle.putString(EXTRA_CONN_PROBLEM_INFO, tempConnInfo);
					sendBroadcast(ACTION_DOWNLOAD_STATE_CHANGE, bundle);
					sendNotification(R.drawable.stat_notify_error, "下载已暂停","下载已暂停",
							tempConnInfo + ",请检查你的网络");
					return;
				}
					//consider custom the view of the notification to show the download progress
				
				BufferedInputStream bis = new BufferedInputStream(
						http.downloadRanged(item.getMp3Url(), nStart, fLength));
				try {
					while ((nRead = bis.read(data, 0, 1024 * 8)) > 0) {

						// bos.write(data, 0, nRead);
						bao.write(data, 0, nRead);
						refreshProgress(fLength,nStart);
						nStart += nRead;

					}
				} catch (Exception e) {
					Log.e("download service", "downloading retry");
					continue;
				}

			}
			fileHelper.writeDataToSong(item, bao.toByteArray(), nRead);
			try {
				bao.close();
			} catch (IOException e) {
				e.printStackTrace();
			}

			if (fileHelper.getItemCoverBitmap(item) == null) {
				Bitmap bm = http.getBitmap(item.getAlbumnCoverUrl());
				fileHelper.saveCover(item, bm);
			}
			
			sendNotification(R.drawable.stat_sys_download_done,item.getTitle()+"下载完毕", item.getTitle()+"下载完毕",
					item.getTitle() + "-" + item.getArtist());
			fileHelper.insertItemIntoDb(item);
			downloadList.remove(0);
			if(downloadList.size()>0){
				SimpleData nextItem = downloadList.get(0);
				DownloadRunnable runnable = new DownloadRunnable(nextItem);
				DownloadTask task = new DownloadTask(runnable);
				executor.execute(task);
			}else{
				QueueDownloadService.this.stopSelf();
			}
		}

	}
}
