package fm.moe.luhuan.service;

import java.io.BufferedInputStream;

import java.io.ByteArrayOutputStream;

import java.io.IOException;

import java.io.Serializable;

import org.apache.http.client.ClientProtocolException;


import fm.moe.luhuan.MusicPlay;
import fm.moe.luhuan.beans.data.SimpleData;
import fm.moe.luhuan.http.CommonHttpHelper;
import fm.moe.luhuan.utils.DataStorageHelper;

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

import android.os.Bundle;
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
	// private Intent broadcast = new Intent();

	private boolean onlyByWifi = true;
	private CommonHttpHelper http = new CommonHttpHelper();
	private Builder notificationBuilder;
	private NotificationManager notificationManager;
	// private PendingIntent pendingIntent;
	private DataStorageHelper fileHelper;

	private ConnectivityManager connectivityManager;
	public static final int NOTIFICATION_ID = 0;
	public static final String EXTRA_SONG_ITEM = "a item";
	public static final String EXTRA_CONN_PROBLEM_INFO = "network problem info";
	// 0 for downloading,1 for complete,-1 for err
	public static final String EXTRA_DOWNLOAD_STATE = "download state extra";
	

	public static final String ACTION_DOWNLOAD_STATE_CHANGE = "download state change";
	

	@Override
	public void onCreate() {
		super.onCreate();
		fileHelper = new DataStorageHelper(this);

		initNotification();
		//to be change
		onlyByWifi = getSharedPreferences("App_settings", MODE_PRIVATE)
				.getBoolean(getResources().getString(fm.moe.luhuan.R.string.pref_key_download_only_on_wifi), false);

		connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
	}

	/*
	 * if all the tasks have been done ,system will instantiate a new instance
	 * of the service when startService() is called
	 */
	@Override
	protected void onHandleIntent(Intent intent) {
		
		SimpleData item = (SimpleData) intent
				.getSerializableExtra(EXTRA_SONG_ITEM);
		String connInfo = getConnProblemText();
		if (connInfo ==null ) {
			Bundle bundle = new Bundle();
			bundle.putInt(EXTRA_DOWNLOAD_STATE, 0);
			sendBroadcast(ACTION_DOWNLOAD_STATE_CHANGE, bundle);
			sendNotification(R.drawable.stat_sys_download,"��������", "��������...",
					item.getTitle() + "-" + item.getArtist());
			
		} else {
			Bundle bundle = new Bundle();
			bundle.putString(EXTRA_CONN_PROBLEM_INFO, connInfo);
			bundle.putInt(EXTRA_DOWNLOAD_STATE, -1);
			sendBroadcast(ACTION_DOWNLOAD_STATE_CHANGE, bundle);
			sendNotification(R.drawable.stat_notify_error,"��������ͣ", "��������ͣ",
					getConnProblemText() + ",�����������");
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
				sendNotification(R.drawable.stat_notify_error, "��������ͣ","��������ͣ",
						tempConnInfo + ",�����������");
				return;
			}
				//consider custom the view of the notification to show the download progress
			
			BufferedInputStream bis = null;
			try {
				bis = new BufferedInputStream(
						http.downloadRanged(item.getMp3Url(), nStart, fLength));
			} catch (ClientProtocolException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			try {
				while ((nRead = bis.read(data, 0, 1024 * 8)) > 0) {

					// bos.write(data, 0, nRead);
					bao.write(data, 0, nRead);
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (fileHelper.getItemCoverBitmap(item) == null) {
			Bitmap bm = null;
			try {
				bm = http.getBitmap(item.getAlbumnCoverUrl());
			} catch (IllegalStateException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			fileHelper.saveCover(item, bm);
		}
		
		sendNotification(R.drawable.stat_sys_download_done,"��������ͣ", "������ϣ�",
				item.getTitle() + "-" + item.getArtist());
		fileHelper.insertItemIntoDb(item);
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();

	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
	private void initNotification(){
		notificationBuilder = new Builder(this);
		broadcastManager = LocalBroadcastManager
				.getInstance(getApplicationContext());
		notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		Intent resumePlayActivity = new Intent(this, MusicPlay.class);
		PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
				resumePlayActivity, PendingIntent.FLAG_UPDATE_CURRENT);
		notificationBuilder.setContentIntent(pendingIntent);
	}
	private void sendBroadcast(String action, Bundle extras) {
		Intent intent = new Intent();
		intent.setAction(action);
		intent.putExtras(extras);
		broadcastManager.sendBroadcast(intent);
	}

	private void sendNotification(int drawableId, String tickerText,String title, String content) {
		notificationBuilder.setSmallIcon(drawableId);
		notificationBuilder.setTicker("�ȷ�����:" + tickerText);
		notificationBuilder.setContentTitle("�ȷ�����:" + title);
		notificationBuilder.setContentText(content);
		notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());
	}

	private int checkConectivity() {
		NetworkInfo ni = connectivityManager.getActiveNetworkInfo();
		if (ni == null ||  !ni.isAvailable() ||!ni.isConnected()) {
			return -1;
		}
		return ni.getType();
	}

	private String getConnProblemText() {
		String info = null;
		int connState = checkConectivity();
		if (connState == -1) {
			info = "��ǰ�޿�����������";
		} else if (connState == ConnectivityManager.TYPE_MOBILE && onlyByWifi) {
			info = "��ǰΪ��wifi����";
		}
		return info;
	}
}
