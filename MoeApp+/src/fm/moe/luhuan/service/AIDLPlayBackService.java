package fm.moe.luhuan.service;

import java.util.ArrayList;

import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import fm.moe.luhuan.IPlaybackService;
import fm.moe.luhuan.beans.data.SimpleData;
import fm.moe.luhuan.utils.DataStorageHelper;

public class AIDLPlayBackService extends Service{
	private MediaPlayer mPlayer;
	private NotificationManager ntfManager;
	private ArrayList<SimpleData> playList;
	private String playListId;
	private int nowIndex;
	private DataStorageHelper dataHelper;
	private int startId;
	private NotificationCompat.Builder nBuilder;
	private boolean isPrepared;
	
	public static final String EXTRA_PLAYLIST = "playList";
	public static final String EXTRA_PLAYLIST_ID = "playListId";
	public static final String EXTRA_SELECTED_INDEX = "selectedIndex";
	@Override
	public void onCreate() {
		mPlayer = new MediaPlayer();
		setPlayerListeners();
		ntfManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		nBuilder = new NotificationCompat.Builder(this);
		dataHelper = new DataStorageHelper(this);
		
		super.onCreate();
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if(intent!=null){
			Bundle data = intent.getExtras();
			playList = (ArrayList<SimpleData>) data.get(EXTRA_PLAYLIST);
			nowIndex = data.getInt(EXTRA_SELECTED_INDEX);
			playListId = data.getString(EXTRA_PLAYLIST_ID);
			playSongAtIndex(nowIndex);
		}
		
		return START_NOT_STICKY;
	}
	
	private void setPlayerListeners() {
		mPlayer.setOnBufferingUpdateListener(onSongBuffering);
		mPlayer.setOnErrorListener(onPlayerErr);
		mPlayer.setOnPreparedListener(onPlayerPrepared);
		mPlayer.setOnCompletionListener(onPlayComplete);
		mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
	}
	private void playSongAtIndex(int n){
		mPlayer.reset();
		isPrepared = false;
		String url = "";
		try{
			url = dataHelper.getItemMp3Url(playList.get(n));
		}catch(Exception e){
			Log.e("", "",e);
		}
		

		try {
			// fav songµÄÊ±ºòÎªNULL£¿
			mPlayer.setDataSource(url);
			isPrepared = false;

		} catch (Exception e) {
			Log.e("setDataSource err", "", e);
		}
		//notificationManager.cancel(NOTIFICATION_ID);
		mPlayer.prepareAsync();
		nowIndex = n;
	}
	
	public class PlayBackServiceImpl extends IPlaybackService.Stub {

		@Override
		public int getNowIndex() throws RemoteException {
			return nowIndex;
		}

		@Override
		public int getSongDuration() throws RemoteException {
			
			return mPlayer.getDuration();
		}

		@Override
		public int getSongCurrentPosition() throws RemoteException {
			return mPlayer.getCurrentPosition();
		}

		@Override
		public void playSongAtIndex(int n) throws RemoteException {
			playSongAtIndex(n);
		}

		@Override
		public void pause() throws RemoteException {
			mPlayer.pause();
		}

		@Override
		public void start() throws RemoteException {
			mPlayer.start();
		}

		@Override
		public boolean isPlayerPrepared() throws RemoteException {
			
			return isPrepared;
		}

		@Override
		public boolean isPlayerPlaying() throws RemoteException {
			// TODO Auto-generated method stub
			return mPlayer.isPlaying();
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return new PlayBackServiceImpl();
	}
	private OnErrorListener onPlayerErr = new OnErrorListener() {

		public boolean onError(MediaPlayer mp, int what, int extra) {
			
			// Log.e("broadcast", "send");
			return true;
		}
	};
	private OnPreparedListener onPlayerPrepared = new OnPreparedListener() {

		public void onPrepared(MediaPlayer mp) {
			isPrepared = true;
			mp.start();
			// Log.e("player", "prepare");
		}
	};
	
	private OnCompletionListener onPlayComplete = new OnCompletionListener() {

		public void onCompletion(MediaPlayer mp) {
			if (playList.size() - 1 > nowIndex) {
				
			}

			
			// Log.e("broadcast", "send");

		}
	};
	private OnBufferingUpdateListener onSongBuffering = new OnBufferingUpdateListener() {

		public void onBufferingUpdate(MediaPlayer mp, int percent) {
			
			Log.e("buffered update", percent + "");

		}
	};
	private BroadcastReceiver receiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getIntExtra("state", 1) == 0) {
				
			}

		}
	};

	

	
	
}
