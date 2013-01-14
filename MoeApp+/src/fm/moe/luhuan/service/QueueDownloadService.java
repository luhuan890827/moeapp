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
import android.text.Html;
import android.util.Log;

public class QueueDownloadService extends Service {
	private LinkedList<SimpleData> downloadList = new LinkedList<SimpleData>();
	private Executor executor = new Executor() {

		public void execute(Runnable command) {
			new Thread(command).start();

		}
	};

	private CommonHttpHelper http = new CommonHttpHelper();
	private Builder notificationBuilder;
	private NotificationManager notificationManager;

	private DataStorageHelper fileHelper;
	private DownloadTask task;
	public static final int NOTIFICATION_ID = 0;
	public static final String EXTRA_SONG_ITEM = "a item";
	public static final String EXTRA_SONG_ITEM_LIST = "a item list";

	// 0 for downloading,1 for complete,-1 for err
	public static final String EXTRA_DOWNLOAD_STATE = "download state extra";

	public static final String ACTION_DOWNLOAD_STATE_CHANGE = "download state change";

	@Override
	public void onCreate() {
		super.onCreate();
		fileHelper = new DataStorageHelper(this);
		notificationBuilder = new Builder(this);
		notificationBuilder.setAutoCancel(true);
		notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (intent != null) {
			// List<SimpleData> list = (List<SimpleData>) intent
			// .getSerializableExtra(EXTRA_SONG_ITEM_LIST);
			// list = intent.getParcelableArrayListExtra(name);
			SimpleData item = (SimpleData) intent
					.getParcelableExtra(EXTRA_SONG_ITEM);
			addTask(item);
			// addTasks(list);
			startTasks();
		}
		return START_NOT_STICKY;
	}

	private void addTask(SimpleData item) {
		downloadList.add(item);
		Bundle b = new Bundle();
		b.putInt(EXTRA_DOWNLOAD_STATE, 0);
		sendBroadcast(ACTION_DOWNLOAD_STATE_CHANGE, b);
	};

	private void addTasks(List<SimpleData> l) {
		if (l != null) {
			downloadList.addAll(l);
		}

	}

	private void startTasks() {
		if(task==null||task.isDone()){
			SimpleData item = downloadList.get(0);
			task = new DownloadTask(new DownloadRunnable(item));
			executor.execute(task);
		}
	}

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

	private void refreshProgress(int max, int progress) {
		notificationBuilder.setProgress(max, progress, false);
		notificationManager
				.notify(NOTIFICATION_ID, notificationBuilder.build());
	}

	private class DownloadTask extends FutureTask<Void> {

		public DownloadTask(Runnable runnable) {

			super(runnable, null);

		}
	}

	private class DownloadRunnable implements Runnable {
		private SimpleData item;

		public DownloadRunnable(SimpleData data) {
			item = data;
		}

		public void run() {
			refreshProgress(100, 0);
			sendNotification(R.drawable.stat_sys_download, "正在下载-"+item.getTitle(), "正在下载", Html
					.fromHtml(item.getParentTitle()+"-"+item.getTitle()).toString());
			Log.e("new runnable start", item.getTitle());
			int nStart = fileHelper.getSongFileLength(item);
			int nRead = 0;
			int fLength = 0;

			while (fLength < 10) {
				fLength = http.getFileLength(item.getMp3Url());
			}
			
			byte[] data = new byte[8 * 1024];

			ByteArrayOutputStream bao = new ByteArrayOutputStream();
			while (nStart < fLength) {

				// consider custom the view of the notification to show the
				// download progress
				Log.e("download", nStart + "");
				BufferedInputStream bis = new BufferedInputStream(
						http.downloadRanged(item.getMp3Url(), nStart, fLength));
				try {
					while ((nRead = bis.read(data, 0, 1024 * 8)) > 0) {

						bao.write(data, 0, nRead);
						refreshProgress(fLength, nStart);
						nStart += nRead;

					}
				} catch (Exception e) {
					//Log.e("download service", "downloading retry");
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

			
			fileHelper.insertItemIntoDb(item);
			downloadList.removeFirst();
			if (downloadList.size() > 0) {
				SimpleData nextItem = downloadList.getFirst();
				DownloadRunnable runnable = new DownloadRunnable(nextItem);
				DownloadTask task = new DownloadTask(runnable);
				executor.execute(task);
			} else {
				sendNotification(R.drawable.stat_sys_download_done, item.getTitle()
						+ " 下载完毕", "下载完毕", Html
						.fromHtml(item.getParentTitle()+"-"+item.getTitle()).toString());
				QueueDownloadService.this.stopSelf();
			}
		}

	}
}
